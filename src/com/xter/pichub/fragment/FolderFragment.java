package com.xter.pichub.fragment;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.xter.pichub.R;
import com.xter.pichub.adapter.FolderAdapter;
import com.xter.pichub.element.Folder;
import com.xter.pichub.util.ViewUtils;
import com.xter.pichub.view.AlbumGridView;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * 文件夹页面
 */
public class FolderFragment extends Fragment {

	public interface OnFolderClickListener {
		void onFolderClick(Folder folder);
	}

	private View viewSpace;
	private GridView gvFolderAlbum;
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_folder, container, false);
		initLayout(view);
		initData();
		return view;
	}

	protected void initLayout(View view) {
		//设置空白区域（占位）
		gvFolderAlbum = (GridView) view.findViewById(R.id.gv_folder);
	}

	protected void initData() {
		folderAdapter = new FolderAdapter(getActivity(), folders);
		gvFolderAlbum.setAdapter(folderAdapter);

		//点击事件
		gvFolderAlbum.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onFolderClickListener.onFolderClick(folders.get(position));
			}
		});
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (onFolderClickListener == null)
			onFolderClickListener = (OnFolderClickListener) activity;
	}

}
