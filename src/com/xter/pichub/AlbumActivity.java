package com.xter.pichub;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.ListView;

import com.xter.pichub.base.BaseActivity;
import com.xter.pichub.element.Folder;
import com.xter.pichub.element.Photo;
import com.xter.pichub.fragment.FolderFragment;
import com.xter.pichub.util.ImageLoader;
import com.xter.pichub.view.CascadeView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by XTER on 2016/3/7.
 */
public class AlbumActivity extends BaseActivity implements FolderFragment.OnFolderClickListener{

	//全屏flag
	public static int FULLSCREEN_STATE = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.INVISIBLE;

	FragmentManager fm;
	private FolderFragment folderFragment;
	private DrawerLayout drawerMenu;
	private ListView lvDrawerMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_album);
		initLayout();
		initData();
	}

	@Override
	protected void initLayout() {
		drawerMenu = (DrawerLayout) findViewById(R.id.drawer_menu);
		lvDrawerMenu = (ListView) findViewById(R.id.lv_album_menu);
		setDefaultFragment();
	}

	@Override
	protected void initData() {

	}

	protected void setDefaultFragment(){
		fm = getFragmentManager();
		if (folderFragment == null) {
			folderFragment = new FolderFragment();
			//准备传递数据
			new LoadMediaDataTask().execute();
		}
	}

	/**
	 * 获取图库信息
	 * @return list     有媒体的文件夹列表
	 */
	protected List<Folder> getFolders() {
		//定义将要查询的列
		String[] columns = new String[]{"Distinct " + MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
		//获取数据游标
		Cursor cursor = this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, MediaStore.Images.Media.DISPLAY_NAME + " IS NOT NULL", null, MediaStore.Images.Media._ID);
		//得到索引
		int indexFolderId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID);
		int indexFolderName = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
		//填充值
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
	 * @param folderId      文件夹ID
	 * @return  list        文件列表
	 */
	protected List<Photo> getPhotos(long folderId) {
		//定义将要查询的列
		String[] columns = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA, MediaStore.Images.Media.WIDTH, MediaStore.Images.Media.HEIGHT, MediaStore.Images.Media.SIZE, MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media.DATE_MODIFIED};
		//获取数据游标
		Cursor cursor = this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, MediaStore.Images.Media.BUCKET_ID + " = ?", new String[]{String.valueOf(folderId)}, MediaStore.Images.Media._ID);
		//得到索引
		int indexPhotoId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
		int indexPhotoName = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
		int indexPhotoPath = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		int indexPhotoWidth = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH);
		int indexPhotoHeight = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT);
		int indexPhotoSize = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
		int indexPhotoDateAdded = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);
		int indexPhotoDateModified = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED);
		//填充值
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

	protected void setFolderCoverUris(Folder folder){
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

	@Override
	public void onFolderClick(Folder folder) {

	}

	private class LoadMediaDataTask extends AsyncTask<Void,Void,List<Folder>>{

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
			ft.add(R.id.album_content, folderFragment, "folder");
			ft.commit();
		}
	}
}
