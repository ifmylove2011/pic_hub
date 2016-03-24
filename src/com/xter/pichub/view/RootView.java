/**
 * 
 */
package com.xter.pichub.view;

import com.xter.pichub.R;
import com.xter.pichub.util.LogUtils;
import com.xter.pichub.util.ViewUtils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

/**
 * @author XTER
 *
 */
public class RootView extends SurfaceView implements Callback, Runnable {

	private Canvas canvas;
	private Paint paint;
	private boolean isRun;
	private SurfaceHolder holder;
	private Bitmap[] bitmaps;
	private int counts;
	private int columns;
	private int size;

	public RootView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		initAttrs(context, attrs);
	}

	public RootView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initAttrs(context, attrs);
	}

	public RootView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initAttrs(context, attrs);
	}

	public RootView(Context context) {
		super(context);
	}

	protected void initAttrs(Context context, AttributeSet attrs) {
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RootView);
		final int count = a.getIndexCount();
		for (int i = 0; i < count; i++) {
			int attr = a.getIndex(i);
			switch (attr) {
			case R.styleable.RootView_columns:
				columns = a.getInt(attr, 2);
				break;
			}
		}

		a.recycle();

		holder = getHolder();
		holder.addCallback(this);
		paint = new Paint();
		
		size = ViewUtils.getDefaultSize()/3;
	}

	protected void draw(int index) {
		canvas.drawBitmap(bitmaps[index], index % columns * size, index / columns * size, paint);
		LogUtils.d("count:" + index);
	}
	

	@Override
	public void run() {
		canvas = holder.lockCanvas();
		for (int i = 0; i < bitmaps.length; i++) {
			draw(i);
		}
		holder.unlockCanvasAndPost(canvas);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		LogUtils.d("created");
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		isRun = false;
	}

	public void setBitmaps(Bitmap[] bitmaps) {
		LogUtils.d("surface bitmaps");
		this.bitmaps = bitmaps;
		isRun = true;
		new Thread(this).start();
	}

}
