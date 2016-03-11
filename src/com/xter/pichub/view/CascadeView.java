package com.xter.pichub.view;

import com.xter.pichub.R;
import com.xter.pichub.util.LogUtils;
import com.xter.pichub.util.ViewUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class CascadeView extends View {

	public CascadeView(Context context) {
		super(context);
	}

	public CascadeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initAttrs(context, attrs);
	}

	public CascadeView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initAttrs(context, attrs);
	}

	@SuppressLint("NewApi")
	public CascadeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		initAttrs(context, attrs);
	}

	private boolean goMeasure;
	private boolean goLayout;
	private boolean goDraw;
	private int totalSize;
	private int imgSize;
	private Bitmap[] bitmaps;

	private int columns;
	private int imgCounts;

	private Rect rect;
	private Paint paint;

	protected void initAttrs(Context context, AttributeSet attrs) {
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CascadeView);
		final int count = a.getIndexCount();
		for (int i = 0; i < count; i++) {
			int attr = a.getIndex(i);
			switch (attr) {
			case R.styleable.CascadeView_columns:
				columns = a.getInt(attr, 2);
				break;
			}
		}
		a.recycle();
		rect = new Rect();
		paint = new Paint();

		totalSize = ViewUtils.getDefaultSize();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (goMeasure) {
			// int specWidthMode = MeasureSpec.getMode(widthMeasureSpec);
			int specWidthSize = MeasureSpec.getSize(widthMeasureSpec);

			Log.i("specWidth", ":" + specWidthSize);

			// if (specWidthMode == MeasureSpec.EXACTLY) {
			// totalWidth = specWidthSize;
			// } else {
			// int totalBitmapWidth = imgSize * columns + getPaddingLeft() +
			// getPaddingRight();
			// if (specWidthMode == MeasureSpec.AT_MOST) {
			// totalWidth = Math.min(specWidthSize, totalBitmapWidth);
			// }
			// }

			// int specHeightMode = MeasureSpec.getMode(heightMeasureSpec);
			int specHeightSize = MeasureSpec.getSize(heightMeasureSpec);

			Log.i("specHeight", ":" + specHeightSize);

			// if (specHeightMode == MeasureSpec.EXACTLY) {
			// totalHeight = specHeightSize;
			// } else {
			// int totalBitmapHeight = imgSize * row + getPaddingTop() +
			// getPaddingBottom();
			// if (specHeightMode == MeasureSpec.AT_MOST) {
			// totalHeight = Math.min(specHeightSize, totalBitmapHeight);
			// }
			// }
			// Log.i("size", ":" + totalWidth + "," + totalHeight + "-->" +
			// imgSize);
			int tempSize = 0;
			if (specHeightSize == 0 || specWidthSize == 0)
				tempSize = Math.max(specHeightSize, specWidthSize);
			else
				tempSize = Math.min(specHeightSize, specWidthSize);
			totalSize = Math.min(totalSize, tempSize);
			LogUtils.i("totalSize:" + totalSize);
			goLayout = true;
			imgSize = totalSize / columns;
			setMeasuredDimension(totalSize, totalSize);
		} else {
			setMeasuredDimension(totalSize, totalSize);
		}
		LogUtils.d("obj:" + toString());
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		LogUtils.d("obj:" + toString());
		if (!goLayout) {
			super.onLayout(changed, left, top, right, bottom);
			goDraw = true;
		} else {
			goLayout = true;
		}

	}

	@Override
	protected void onDraw(Canvas canvas) {
		LogUtils.d("obj:" + toString());
		if (goDraw) {
			for (int i = 0; i < imgCounts; i++) {
				rect.left = imgSize * (i % columns);
				rect.top = imgSize * (i / columns);
				rect.right = imgSize + rect.left;
				rect.bottom = imgSize + rect.top;
				LogUtils.i("location:" + rect.left + "," + rect.top + "," + rect.right + "," + rect.bottom);
				canvas.drawBitmap(bitmaps[i], null, rect, paint);
			}
		}
	}

	public void setBitmaps(Bitmap[] bitmaps) {
		this.bitmaps = bitmaps;
		imgCounts = bitmaps.length;
		if (imgCounts == 1)
			columns = 1;
		goMeasure = true;
		LogUtils.d("count" + imgCounts);
		requestLayout();
	}

}
