package com.xter.pichub.demo;

import com.xter.pichub.R;
import com.xter.pichub.util.BitmapUtils;
import com.xter.pichub.util.SysUtils;
import com.xter.pichub.view.BitmapSurface;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class DemoActiivty extends Activity {

	private BitmapSurface bitSur;
	private ImageView image;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demo_actiivty);
		initLayout();
		initData();
	}

	protected void initLayout(){
		bitSur = (BitmapSurface) findViewById(R.id.bit_sur);
		image = (ImageView) findViewById(R.id.image_demo);
	}

	protected void initData(){
		Bitmap[] bitmaps = new Bitmap[4];
		for(int i=0;i<4;i++){
			bitmaps[i] = BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher);
		}
		image.setImageBitmap(BitmapUtils.combineBitmaps(bitmaps,100,2));
		SysUtils.setAlarmTime(this, 15*1000);
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
		SysUtils.cancelAlarm(this);
		super.onPause();
	}
	
}
