package com.xter.pichub.demo;

import java.util.List;

import com.xter.pichub.R;
import com.xter.pichub.element.Folder;
import com.xter.pichub.element.Photo;
import com.xter.pichub.util.ImageLoader4;
import com.xter.pichub.view.RootView;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class DemoActiivty extends Activity {

	private RootView rootView;
	ImageLoader4 loader;
	List<Folder> folders;
	Folder folder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demo_actiivty);
		initLayout();
		initData();
	}

	protected void initLayout() {
		rootView = (RootView) findViewById(R.id.root);
	}

	protected void initData() {
		folder = getIntent().getParcelableExtra("folder");
		List<Photo> photos = folder.getPhotos();
		int size = photos.size();
		loader = ImageLoader4.build(this);
		String[] uris = new String[size];
		for (int i = 0; i < size; i++) {
			uris[i] = "file://" + photos.get(i).getPath();
		}
		loader.bindBitmap(uris, rootView, true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_demo_actiivty, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		// SysUtils.cancelAlarm(this);
		super.onPause();
	}

}
