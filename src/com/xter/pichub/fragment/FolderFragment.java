package com.xter.pichub.fragment;

import java.util.List;

import com.xter.pichub.R;
import com.xter.pichub.adapter.FolderAdapter;
import com.xter.pichub.base.BaseFragment;
import com.xter.pichub.element.Folder;
import com.xter.pichub.util.ViewUtils;
import com.xter.pichub.view.AlbumGridView;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

/**
 * A simple {@link Fragment} subclass. 文件夹页面
 */
public class FolderFragment extends BaseFragment {

	public interface OnFolderClickListener {
		void onFolderClick(Folder folder);
	}

	private View viewSpace;
	private AlbumGridView gvFolderAlbum;
	private FolderAdapter folderAdapter;

	private OnFolderClickListener onFolderClickListener;

	private List<Folder> folders;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		folders = bundle.getParcelableArrayList("folders");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_folder, container, false);
		initLayout(view);
		initData();
		return view;
	}

	@Override
	protected void initLayout(View view) {
		// 设置空白区域（占位）
		viewSpace = view.findViewById(R.id.view_space);
		viewSpace.setLayoutParams(ViewUtils.getSystemBarParam(getActivity()));
		gvFolderAlbum = (AlbumGridView) view.findViewById(R.id.gv_folder);
	}

	@Override
	protected void initData() {
		folderAdapter = new FolderAdapter(getActivity(), folders);
		gvFolderAlbum.setAdapter(folderAdapter);

		// 点击事件
		gvFolderAlbum.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onFolderClickListener.onFolderClick(folders.get(position));
			}
		});
		// 使空白区域获取焦点，避免因为gridview抢夺焦点而使其无法显示空白区域
		// viewSpace.setFocusable(true);
		// viewSpace.setFocusableInTouchMode(true);
		// viewSpace.requestFocus();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (onFolderClickListener == null)
			onFolderClickListener = (OnFolderClickListener) activity;
	}

}
