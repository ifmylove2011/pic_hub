package com.xter.pichub.util;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;

public class BitmapUtils {
	/**
	 * 得出方形图
	 *
	 * @param bitmap 传入位图
	 * @return bitmap     付出位图
	 */
	public static Bitmap getSquareBitmap(Bitmap bitmap) {
		if (bitmap == null)
			return null;
		final int rawWidth = bitmap.getWidth();
		final int rawHeight = bitmap.getHeight();
		LogUtils.i("raw:" + rawWidth + "," + rawHeight);
		final int startX, startY, reqSize;
		if (rawWidth > rawHeight) {
			reqSize = rawHeight;
			startX = (rawWidth - reqSize) / 2;
			startY = 0;
		} else {
			reqSize = rawWidth;
			startX = 0;
			startY = (rawHeight - reqSize) / 2;
		}
		return Bitmap.createBitmap(bitmap, startX, startY, reqSize, reqSize);
	}

	/**
	 * @param bitmap  传入位图对象
	 * @param reqSize 要求大小
	 * @return bitmap 位图对象
	 */
	public static Bitmap getSquareBitmap(Bitmap bitmap, int reqSize) {
		if (bitmap == null || reqSize <= 0)
			return null;
		Bitmap tempBitmap = getSquareBitmap(bitmap);
		return Bitmap.createScaledBitmap(tempBitmap, reqSize, reqSize, true);
	}

	/**
	 * 得到图像大小
	 *
	 * @param bitmap 传入位图对象
	 * @return bytes 图像大小
	 */
	@SuppressLint("NewApi")
	public static int getBitmapSize(Bitmap bitmap) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // API 19
			return bitmap.getAllocationByteCount();
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {// API
			// 12
			return bitmap.getByteCount();
		}
		return bitmap.getRowBytes() * bitmap.getHeight(); // earlier version
	}

	/**
	 * 从路径解析位图
	 *
	 * @param path      路径
	 * @param reqWidth  要求宽度
	 * @param reqHeight 要求高度
	 * @return
	 * @throws IOException
	 */
	public static Bitmap decodeBitmapFromPath(String path, int reqWidth, int reqHeight)
			throws IOException {
		@SuppressWarnings("resource")
		FileDescriptor fd = new FileInputStream(path).getFD();
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFileDescriptor(fd, null, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		options.inPreferredConfig = Config.RGB_565;
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFileDescriptor(fd, null, options);
	}

	/**
	 * 由文件资源标识得到解析位图
	 *
	 * @param fd        文件标识
	 * @param reqWidth  要求宽度
	 * @param reqHeight 要求高度
	 * @return bitmap       传出位图
	 * @throws IOException
	 */
	public static Bitmap decodeBitmapFromFileDescriptor(FileDescriptor fd, int reqWidth, int reqHeight) throws IOException {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFileDescriptor(fd, null, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		options.inPreferredConfig = Config.RGB_565;
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFileDescriptor(fd, null, options);
	}

	/**
	 * 得到方形图
	 *
	 * @param fd        文件标识符
	 * @param reqWidth  要求宽度
	 * @param reqHeight 要求高度
	 * @return bitmap      传出位图
	 * @throws IOException
	 */
	public static Bitmap getSquareBitmapFromFileDescriptor(FileDescriptor fd, int reqWidth, int reqHeight) throws IOException {
		return getSquareBitmap(decodeBitmapFromFileDescriptor(fd, reqWidth, reqHeight));
	}

	/**
	 * 由图像原始大小与目标View比较得出采样率
	 *
	 * @param options   参数设置
	 * @param reqWidth  目标宽度
	 * @param reqHeight 目标高度
	 * @return inSampleSize 采样率	
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		if (reqHeight == 0 || reqWidth == 0)
			return 1;
		// 原始宽高信息
		final int height = options.outHeight;
		final int width = options.outWidth;
		// 计算采样率
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			final int h = height / 2;
			final int w = width / 2;
			while ((h / inSampleSize) >= reqHeight && (w / inSampleSize) >= reqWidth)
				inSampleSize *= 2;
		}
		return inSampleSize;
	}

	/**
	 * 自定义图像大小
	 *
	 * @param res       文件资源
	 * @param resId     布局组件ID
	 * @param reqWidth  要求宽度
	 * @param reqHeight 要求高度
	 * @return bitmap 图像资源
	 */
	public static Bitmap decodeBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);
		// 计算采样率（大小比例）
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	public static Bitmap getDefaultBitmap() {
		return Bitmap.createBitmap(1, 1, Config.RGB_565);
	}


	/**
	 * 合并图片
	 *
	 * @param bitmaps 位图资源
	 * @param size    大小
	 * @param padding 间距
	 * @return bitmap      位图
	 */
	public static Bitmap combineBitmaps(Bitmap[] bitmaps, int size, int padding) {
		int length = bitmaps.length;
		int columns = (int) (length / Math.sqrt(length) >= Math.sqrt(length) ? Math.sqrt(length) : Math.sqrt(length) + 1);
		Bitmap bitmap = Bitmap.createBitmap(columns * size + (columns - 1) * padding, columns * size + (columns - 1) * padding, Config.RGB_565);
		Paint paint = new Paint();
		Canvas c = new Canvas(bitmap);
		for (int i = 0; i < length; i++) {
			c.drawBitmap(bitmaps[i], i % columns * size + (i % columns - 1) * padding, i / columns * size + (i / columns - 1) * padding, paint);
		}
		return bitmap;
	}
}
