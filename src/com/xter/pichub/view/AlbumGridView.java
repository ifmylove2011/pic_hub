package com.xter.pichub.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Created by XTER on 2016/1/13.
 */
public class AlbumGridView extends GridView {
	public AlbumGridView(Context context) {
		super(context);
	}

	public AlbumGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AlbumGridView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int height = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE>>2,MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, height);
	}
}
