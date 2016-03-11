package com.xter.pichub.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class AbsListViewForScrollView extends ScrollView {

	public AbsListViewForScrollView(Context context) {
		super(context);
	}

	public AbsListViewForScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public AbsListViewForScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int height = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, height);
	}
}
