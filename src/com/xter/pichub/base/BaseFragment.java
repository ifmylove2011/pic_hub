package com.xter.pichub.base;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by XTER on 2016/3/7.
 */
public abstract class BaseFragment extends Fragment {

	/* 初始布局 */
	protected abstract void initLayout(View view);

	/* 初始数据 */
	protected abstract void initData();

	/**
	 * 得到上下文
	 *
	 * @return context
	 */
	public Context getContext() {
		return getActivity();
	}
}
