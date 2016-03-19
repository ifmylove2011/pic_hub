package com.xter.pichub;

import java.util.ArrayList;
import java.util.List;

import com.xter.pichub.aidl.ICrypt;
import com.xter.pichub.base.BaseActivity;
import com.xter.pichub.binder.BinderPool;
import com.xter.pichub.binder.ICryptImpl;
import com.xter.pichub.demo.DemoActiivty;
import com.xter.pichub.element.Folder;
import com.xter.pichub.element.Photo;
import com.xter.pichub.fragment.FolderFragment;
import com.xter.pichub.fragment.FolderFragment.OnFolderClickListener;
import com.xter.pichub.fragment.PhotoFragment;
import com.xter.pichub.fragment.PhotoFragment.OnPhotosClickListener;
import com.xter.pichub.lib.SystemBarTintManager;
import com.xter.pichub.util.LogUtils;
import com.xter.pichub.util.ViewUtils;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by XTER on 2016/3/7.
 */
public class AlbumActivity extends BaseActivity implements OnFolderClickListener, OnPhotosClickListener {

	// 全屏flag
	public static int FULLSCREEN_STATE = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.INVISIBLE;

	FragmentManager fm;
	private FolderFragment folderFragment;
	private PhotoFragment photoFragment;

	SystemBarTintManager tintManager;
	 private DrawerLayout drawerMenu;
	private ListView lvDrawerMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_album);
		initSystemBar();
		initLayout();
		initData();
	}

	@Override
	protected void initLayout() {
		// drawerMenu = (DrawerLayout) findViewById(R.id.drawer_menu);
		lvDrawerMenu = (ListView) findViewById(R.id.lv_album_menu);
		setDefaultFragment();
	}

	@Override
	protected void initData() {
		// 使菜单在系统栏之下展开
		lvDrawerMenu.getLayoutParams().width = ViewUtils.getScreenSize().x / 3 * 2;
		((ViewGroup.MarginLayoutParams) lvDrawerMenu.getLayoutParams()).setMargins(0,
				ViewUtils.getSystemBarHeight(this) - 2, 0, 0);
		// 菜单适配器
		// lvDrawerMenu.setAdapter(new
		// DrawerMenuAdpater(this,DataLayer.drawerMenuImages,DataLayer.drawerMenuTexts));
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				doCrypt();
			}
		}).start();
	}

	protected void initSystemBar() {
		tintManager = new SystemBarTintManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setStatusBarTintResource(R.color.darkgrey2);
		changeScreenState(false);
	}

	/**
	 * 设置默认显示的视图
	 */
	protected void setDefaultFragment() {
		fm = getFragmentManager();
		if (folderFragment == null) {
			folderFragment = new FolderFragment();
			// 获取并传递数据
			new LoadMediaDataTask().execute();
		}
	}

	/**
	 * 获取图库信息
	 * 
	 * @return list 有媒体的文件夹列表
	 */
	protected List<Folder> getFolders() {
		// 定义将要查询的列
		String[] columns = new String[] { "Distinct " + MediaStore.Images.Media.BUCKET_ID,
				MediaStore.Images.Media.BUCKET_DISPLAY_NAME };
		// 获取数据游标
		Cursor cursor = this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
				MediaStore.Images.Media.DISPLAY_NAME + " IS NOT NULL", null, MediaStore.Images.Media._ID);
		// 得到索引
		int indexFolderId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID);
		int indexFolderName = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
		// 填充值
		List<Folder> folders = new ArrayList<Folder>();
		while (cursor.moveToNext()) {
			Folder folder = new Folder();
			folder.setFolderId(cursor.getLong(indexFolderId));
			folder.setFolderName(cursor.getString(indexFolderName));
			folder.setPhotos(getPhotos(folder.getFolderId()));
			setFolderCoverUris(folder);
			folder.setImgCount(folder.getPhotos().size());
			folders.add(folder);
		}
		cursor.close();
		return folders;
	}

	/**
	 * 获取一个文件夹中的文件
	 * 
	 * @param folderId
	 *            文件夹ID
	 * @return list 文件列表
	 */
	protected List<Photo> getPhotos(long folderId) {
		// 定义将要查询的列
		String[] columns = new String[] { MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME,
				MediaStore.Images.Media.DATA, MediaStore.Images.Media.WIDTH, MediaStore.Images.Media.HEIGHT,
				MediaStore.Images.Media.SIZE, MediaStore.Images.Media.DATE_ADDED,
				MediaStore.Images.Media.DATE_MODIFIED };
		// 获取数据游标
		Cursor cursor = this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
				MediaStore.Images.Media.BUCKET_ID + " = ?", new String[] { String.valueOf(folderId) },
				MediaStore.Images.Media._ID);
		// 得到索引
		int indexPhotoId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
		int indexPhotoName = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
		int indexPhotoPath = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		int indexPhotoWidth = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH);
		int indexPhotoHeight = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT);
		int indexPhotoSize = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
		int indexPhotoDateAdded = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);
		int indexPhotoDateModified = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED);
		// 填充值
		List<Photo> photos = new ArrayList<Photo>();
		while (cursor.moveToNext()) {
			Photo photo = new Photo();
			photo.setId(cursor.getLong(indexPhotoId));
			photo.setName(cursor.getString(indexPhotoName));
			photo.setPath(cursor.getString(indexPhotoPath));
			photo.setWidth(cursor.getInt(indexPhotoWidth));
			photo.setHeight(cursor.getInt(indexPhotoHeight));
			photo.setSize(cursor.getInt(indexPhotoSize));
			photo.setDateAdded(cursor.getLong(indexPhotoDateAdded));
			photo.setDateModified(cursor.getLong(indexPhotoDateModified));
			photos.add(photo);
		}
		cursor.close();
		return photos;
	}

	/**
	 * 封面需要的位图个数
	 * 
	 * @param folder
	 *            文件夹
	 */
	protected void setFolderCoverUris(Folder folder) {
		List<Photo> photos = folder.getPhotos();
		int size = photos.size();
		if (size > 4)
			size = 4;
		String[] uris = new String[size];
		for (int i = 0; i < size; i++)
			uris[i] = "file://" + photos.get(i).getPath();
		folder.setCoverUris(uris);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

	}

	/* 从一个fragment跳转到另一个 */
	public void switchContent(Fragment from, Fragment to, String tag) {
		FragmentTransaction ft = fm.beginTransaction();
		// 先判断是否被add过
		if (!to.isAdded()) {
			// 隐藏当前的fragment，add下一个到Activity中
			ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
			ft.hide(from).add(R.id.album_content, to, tag);
			ft.addToBackStack(null);
			ft.commit();
		} else {
			// 隐藏当前的fragment，show下一个
			ft.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
			ft.hide(from).show(to);
			ft.commit();
		}
	}

	/* 切换全屏与非全屏状态 */
	protected void changeScreenState(boolean state) {
		if (state) {
			if (getWindow().getDecorView().getSystemUiVisibility() == FULLSCREEN_STATE) {
				getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
				tintManager.setStatusBarTintResource(R.color.darkgrey2);
				getActionBar().show();
			} else {
				getActionBar().hide();
				getWindow().getDecorView().setSystemUiVisibility(FULLSCREEN_STATE);
				tintManager.setStatusBarTintResource(R.color.transparent);
			}
		} else {
			tintManager.setStatusBarTintResource(R.color.darkgrey2);
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
			getActionBar().show();
		}
	}

	private class LoadMediaDataTask extends AsyncTask<Void, Void, List<Folder>> {

		@Override
		protected List<Folder> doInBackground(Void... params) {
			return getFolders();
		}

		@Override
		protected void onPostExecute(List<Folder> folders) {
			Bundle bundle = new Bundle();
			bundle.putParcelableArrayList("folders", (ArrayList<? extends Parcelable>) folders);
			folderFragment.setArguments(bundle);
			FragmentTransaction ft = fm.beginTransaction();
			ft.replace(R.id.album_content, folderFragment, "folders");
			ft.commit();
		}
	}

	@Override
	public void onFolderClick(Folder folder) {
		if (photoFragment == null) {
			photoFragment = new PhotoFragment();
		}
		// 准备传递数据
		Bundle bundle = new Bundle();
		bundle.putParcelable("folder", folder);
		photoFragment.setArguments(bundle);
		switchContent(folderFragment, photoFragment, "photos");
	}

	@Override
	public void onPhotosClick(List<Photo> pics, int position, String folderName) {

	}
	
	protected void doCrypt(){
		BinderPool binderPool = BinderPool.getInstance(AlbumActivity.this);
		IBinder cryptBinder = binderPool.queryBinder(BinderPool.BINDER_CRYPT);
		ICrypt crypt = ICryptImpl.asInterface(cryptBinder);
		LogUtils.d("visit crypt");
		String msg = "I'm coming.The holo world";
		try {
			String password = crypt.md5Encrypt(msg, 2);
			LogUtils.w("encrypt:"+password);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_album, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case R.id.action_settings:
//				Toast.makeText(getApplicationContext(), getString(R.string.action_settings), Toast.LENGTH_SHORT).show();
//				drawerMenu.openDrawer(Gravity.LEFT);
				startActivity(new Intent(AlbumActivity.this,DemoActiivty.class));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
