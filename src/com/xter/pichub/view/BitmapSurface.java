package com.xter.pichub.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.xter.pichub.R;

/**
 * Created by XTER on 2016/3/14.
 */
public class BitmapSurface extends SurfaceView implements SurfaceHolder.Callback {

	private SurfaceHolder holder;

	public BitmapSurface(Context context) {
		super(context);
		init();
	}

	public BitmapSurface(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BitmapSurface(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	protected void init() {
		holder = getHolder();
		holder.addCallback(this);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		new Thread(new DrawLoop()).start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	class DrawLoop implements Runnable {

		@Override
		public void run() {
			Canvas c = holder.lockCanvas();
			Paint paint = new Paint();
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
			c.drawBitmap(bitmap,null,new Rect(0,0,200,200),paint);
			holder.unlockCanvasAndPost(c);
		}

	}
}
