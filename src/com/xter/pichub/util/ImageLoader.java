package com.xter.pichub.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.support.v4.util.LruCache;

import com.xter.pichub.R;
import com.xter.pichub.lib.DiskLruCache;
import com.xter.pichub.view.CascadeView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageLoader {

	// 载入位图线程标识
	public static final int MESSAGE_POST_RESULT = 1;
	// 磁盘索引
	private static final int DISK_CACHE_INDEX = 0;
	// 磁盘缓存大小
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 50;
	// CPU数量
	private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
	// 核心线程数--保持存活
	private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
	// 最大线程数
	private static int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
	// 非核心线程闲置回收时间
	private static final long KEEP_ALIVE = 10L;
	// 缓存流大小
	private static final int IO_BUFFER_SIZE = 18 * 1024;
	// TAG标识
	private static final int TAG_KEY_URI = R.id.imageloader_uri;

	// Least recent use Cache
	private LruCache<String, Bitmap> mMemoryCache;
	private DiskLruCache mDiskLruCache;

	// 磁盘缓存是否就绪
	private boolean mIsDiskLruCacheCreated = false;
	// 是否为方形
	private boolean mIsSquare;
	// 位图是否载入完毕
	// private boolean loadFlag = true;

	private Context mContext;

	/**
	 * 新线程创建
	 */
	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "ImageLoader#" + mCount.getAndIncrement());
		}

	};

	/**
	 * 线程池管理
	 */
	public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
			KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), sThreadFactory);

	//	private Handler mMainHandler = new Handler(Looper.getMainLooper()) {
//
//		@Override
//		public void handleMessage(Message msg) {
//			switch (msg.what) {
//				case MESSAGE_POST_RESULT:
//					LogUtils.i("setBitmaps");
//					LoaderResult result = (LoaderResult) msg.obj;
//					CascadeView view = result.cascadeView;
//					String[] uris = (String[]) view.getTag(TAG_KEY_URI);
//					if (Arrays.equals(uris, result.uris)) {
//						result.cascadeView.setBitmaps(result.bitmaps);
//					} else {
//						LogUtils.w("uri changed");
//					}
//			}
//			return;
//			// TO DO
//		}
//
//	};
	MainHandler mMainHandler = new MainHandler(Looper.getMainLooper());

	static class MainHandler extends Handler {
		Handler handler;

		MainHandler(Looper looper) {
			handler = new Handler(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MESSAGE_POST_RESULT:
					LogUtils.i("setBitmaps");
					LoaderResult result = (LoaderResult) msg.obj;
					CascadeView view = result.cascadeView;
					String[] uris = (String[]) view.getTag(TAG_KEY_URI);
					if (Arrays.equals(uris, result.uris)) {
						result.cascadeView.setBitmaps(result.bitmaps);
					} else {
						LogUtils.w("uri changed");
					}
					break;
			}
		}

	}

	private ImageLoader(Context context) {
		mContext = context.getApplicationContext();
		int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		int cacheSize = maxMemory / 8;

		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

			@Override
			protected int sizeOf(String key, Bitmap value) {
				int size = value.getRowBytes() * value.getHeight() / 1024;
				LogUtils.i("memoryCache:" + size + "MB");
				return size;
			}

		};

		// 创建磁盘缓存
		File diskCacheDir = new File(SysUtils.getDiskCacheDir(mContext, "bit"));
		if (!diskCacheDir.exists()) {
			diskCacheDir.mkdirs();
		}
		LogUtils.i("diskCachePath:" + diskCacheDir.getAbsolutePath());
		// 可用空间大于自定大小则创建磁盘缓存
		if (getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE) {
			try {
				mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, DISK_CACHE_SIZE);
				mIsDiskLruCacheCreated = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			LogUtils.w("磁盘空间不足！无法创建磁盘缓存");
		}

	}

	/**
	 * 获取实例
	 *
	 * @param context 上下文
	 * @return imageloader     类单例
	 */
	public static ImageLoader build(Context context) {
		return new ImageLoader(context);
	}

	/**
	 * 添加位图至内存缓存
	 *
	 * @param key    键
	 * @param bitmap 位图
	 */
	private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null)
			mMemoryCache.put(key, bitmap);
	}

	/**
	 * 从内存缓存中获取位图
	 *
	 * @param key 键
	 * @return bitmap      位图
	 */
	private Bitmap getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}

	public void bindBitmap(final String[] uris, final CascadeView c, boolean isSquare) {
		mIsSquare = isSquare;
		bindBitmap(uris, c, 0, 0, isSquare);
	}

	/**
	 * 绑定控件与位图
	 *
	 * @param uris        地址
	 * @param cascadeView 组件
	 * @param reqWidth    要求宽度
	 * @param reqHeight   要求高度
	 */
	public void bindBitmap(final String[] uris, final CascadeView cascadeView, final int reqWidth, final int reqHeight, boolean isSquare) {
		mIsSquare = isSquare;
		cascadeView.setTag(TAG_KEY_URI, uris);

		// 开启载入任务
		Runnable loadBitmapTask = new Runnable() {

			@Override
			public void run() {
				int length = uris.length;
				Bitmap[] bitmaps = new Bitmap[length];

				for (int i = 0; i < uris.length; i++)
					bitmaps[i] = loadBitmap(uris[i], reqWidth, reqHeight);
				LoaderResult result = new LoaderResult(uris, bitmaps, cascadeView);
				mMainHandler.obtainMessage(MESSAGE_POST_RESULT, result).sendToTarget();
			}
		};
		THREAD_POOL_EXECUTOR.execute(loadBitmapTask);
	}

	/**
	 * 加载位图
	 *
	 * @param uri       地址
	 * @param reqWidth  要求宽度
	 * @param reqHeight 要求高度
	 * @return bitmap      位图
	 */
	public Bitmap loadBitmap(String uri, int reqWidth, int reqHeight) {
		Bitmap bitmap = loadBitmapFromMemCache(uri);
		if (bitmap != null) {
			LogUtils.d("uri:" + uri);
			return bitmap;
		}

		try {
			bitmap = loadBitmapFromDiskCache(uri, reqWidth, reqHeight);
			if (bitmap != null) {
				LogUtils.d("uri:" + uri);
				return bitmap;
			}
			if (uri.startsWith("http://")) {
				bitmap = loadBitmapFromHttp(uri, reqWidth, reqHeight);
				LogUtils.d("uri:" + uri);
			}
			if (uri.startsWith("file://")) {
				bitmap = loadBitmapFromLocalDisk(uri, reqWidth, reqHeight);
				LogUtils.d("uri:" + uri);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (bitmap == null && !mIsDiskLruCacheCreated) {
			LogUtils.w("disk cache not created");
			bitmap = downloadBitmapFromUrl(uri);
		}

		return bitmap;
	}

	/**
	 * 从内存缓存中加载位图
	 *
	 * @param uri 地址
	 * @return bitmap      位图
	 */
	private Bitmap loadBitmapFromMemCache(String uri) {
		final String key = hashKeyFromUri(uri);
		return getBitmapFromMemCache(key);
	}

	/**
	 * 从磁盘缓存中加载位图
	 *
	 * @param uri       地址
	 * @param reqWidth  要求宽度
	 * @param reqHeight 要求高度
	 * @return bitmap      位图
	 * @throws IOException
	 */
	private Bitmap loadBitmapFromDiskCache(String uri, int reqWidth, int reqHeight) throws IOException {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			LogUtils.w("not recommended");
		}
		if (mDiskLruCache == null)
			return null;

		Bitmap bitmap = null;
		String key = hashKeyFromUri(uri);
		// 取文件
		DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
		if (snapShot != null) {
			FileInputStream fileInputStream = (FileInputStream) snapShot.getInputStream(DISK_CACHE_INDEX);
			FileDescriptor fileDescriptor = fileInputStream.getFD();
			if (mIsSquare)
				bitmap = BitmapUtils.getSquareBitmapFromFileDescriptor(fileDescriptor, reqWidth, reqHeight);
			else
				bitmap = BitmapUtils.decodeBitmapFromFileDescriptor(fileDescriptor, reqWidth, reqHeight);
			if (bitmap != null)
				addBitmapToMemoryCache(key, bitmap);
		}
		return bitmap;
	}

	/**
	 * 从本地磁盘获取位图
	 *
	 * @param uri       地址
	 * @param reqWidth  要求宽度
	 * @param reqHeight 要求高度
	 * @return bitmap      位图
	 * @throws IOException
	 */
	private Bitmap loadBitmapFromLocalDisk(String uri, int reqWidth, int reqHeight) throws IOException {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			throw new RuntimeException("better not visit local disk from UI thread");
		}
		if (mDiskLruCache == null)
			return null;

		// 存文件
		String key = hashKeyFromUri(uri);
		DiskLruCache.Editor editor = mDiskLruCache.edit(key);
		if (editor != null) {
			OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
			if (transFileToStream(uri, outputStream)) {
				editor.commit();
			} else {
				editor.abort();
			}
			mDiskLruCache.flush();
		}
		return loadBitmapFromDiskCache(uri, reqWidth, reqHeight);
	}

	/**
	 * 从网络加载位图
	 *
	 * @param uri       地址
	 * @param reqWidth  要求宽度
	 * @param reqHeight 要求高度
	 * @return bitmap      位图
	 * @throws IOException
	 */
	private Bitmap loadBitmapFromHttp(String uri, int reqWidth, int reqHeight) throws IOException {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			throw new RuntimeException("cannot visit network from UI thread");
		}
		if (mDiskLruCache == null)
			return null;

		// 存文件
		String key = hashKeyFromUri(uri);
		DiskLruCache.Editor editor = mDiskLruCache.edit(key);
		if (editor != null) {
			OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
			if (downloadUrlToStream(uri, outputStream)) {
				editor.commit();
			} else {
				editor.abort();
			}
			mDiskLruCache.flush();
		}
		return loadBitmapFromDiskCache(uri, reqWidth, reqHeight);
	}

	/**
	 * 从URL下载数据流
	 *
	 * @param urlString    网络地址
	 * @param outputStream 输出流
	 * @return boolean     数据流是否准备就绪
	 */
	public boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
		HttpURLConnection urlConnection = null;
		BufferedOutputStream bos = null;
		BufferedInputStream bis = null;
		try {
			final URL url = new URL(urlString);
			urlConnection = (HttpURLConnection) url.openConnection();
			bis = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
			bos = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);

			int b;
			while ((b = bis.read()) != -1)
				bos.write(b);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (urlConnection != null)
				urlConnection.disconnect();
			close(bos);
			close(bis);
		}
		return false;
	}

	/**
	 * 从文件读取流
	 *
	 * @param uri          文件地址
	 * @param outputStream 输出流
	 * @return boolean         文件流是否准备就绪
	 */
	public boolean transFileToStream(String uri, OutputStream outputStream) {
		String path = uri.substring(7);
		BufferedOutputStream bos = null;
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(new File(path)), IO_BUFFER_SIZE);
			bos = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);

			int b;
			while ((b = bis.read()) != -1)
				bos.write(b);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(bos);
			close(bis);
		}
		return false;
	}

	/**
	 * 从URL下载位图
	 *
	 * @param urlString 地址
	 * @return bitmap      位图
	 */
	private Bitmap downloadBitmapFromUrl(String urlString) {
		Bitmap bitmap = null;
		HttpURLConnection urlConnection = null;
		BufferedInputStream bis = null;
		try {
			final URL url = new URL(urlString);
			urlConnection = (HttpURLConnection) url.openConnection();
			bis = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
			bitmap = BitmapFactory.decodeStream(bis);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (urlConnection != null)
				urlConnection.disconnect();
			close(bis);
		}
		return bitmap;
	}

	/**
	 * 获取某路径可用空间
	 *
	 * @param path 文件
	 * @return long        可用大小
	 */
	@SuppressLint("NewApi")
	private long getUsableSpace(File path) {
		if (Build.VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
			return path.getUsableSpace();
		}
		final StatFs stats = new StatFs(path.getPath());
		return stats.getAvailableBytes();
	}

	/**
	 * 加载数据封装类
	 */
	private static class LoaderResult {
		public String[] uris;
		public Bitmap[] bitmaps;
		public CascadeView cascadeView;

		public LoaderResult(String[] uris, Bitmap[] bitmaps, CascadeView cascadeView) {
			super();
			this.uris = uris;
			this.bitmaps = bitmaps;
			this.cascadeView = cascadeView;
		}
	}

	/**
	 * 关闭输入流
	 *
	 * @param is 输入流
	 */
	public void close(InputStream is) {
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 关闭输出流
	 *
	 * @param os 输出流
	 */
	public void close(OutputStream os) {
		if (os != null) {
			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 从URI中取出key
	 *
	 * @param uri 地址
	 * @return string      对应键
	 */
	public String hashKeyFromUri(String uri) {
		String cacheKey;
		try {
			final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.update(uri.getBytes());
			cacheKey = bytesToHexString(messageDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			cacheKey = String.valueOf(uri.hashCode());
		}
		return cacheKey;
	}

	/**
	 * 字节转十六进制
	 *
	 * @param bytes 字节组
	 * @return string      十六进制符
	 */
	public String bytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			String hex = Integer.toHexString(0xFF & b);
			if (hex.length() == 1)
				sb.append("0");
			sb.append(hex);
		}
		return sb.toString();
	}
}
