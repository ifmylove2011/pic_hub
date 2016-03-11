package com.xter.pichub.fragment;

import java.util.List;

import com.xter.pichub.R;
import com.xter.pichub.adapter.PhotosAdapter;
import com.xter.pichub.base.BaseFragment;
import com.xter.pichub.element.Folder;
import com.xter.pichub.element.Photo;
import com.xter.pichub.util.LogUtils;
import com.xter.pichub.util.ViewUtils;
import com.xter.pichub.view.AlbumGridView;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

public class PhotoFragment extends BaseFragment {

	public interface OnPhotosClickListener {
		void onPhotosClick(List<Photo> pics, int position, String folderName);
	}

	private View viewSpace;
	private AlbumGridView gvPhotosAlbum;
	private PhotosAdapter photosAdapter;

	private OnPhotosClickListener onPhotosClickListener;

	private List<Photo> photos;
	private String folderName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtils.i("photo fragment create");
		Bundle bundle = getArguments();
		Folder folder = bundle.getParcelable("folder");
		photos = folder.getPhotos();
		folderName = folder.getFolderName();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_photo, container, false);
		initLayout(view);
		initData();
		return view;
	}

	@Override
	protected void initLayout(View view) {
		// 设置空白区域（占位）
		viewSpace = view.findViewById(R.id.view_space);
		viewSpace.setLayoutParams(ViewUtils.getSystemBarParam(getActivity()));
		gvPhotosAlbum = (AlbumGridView) view.findViewById(R.id.gv_photos);
	}

	@Override
	protected void initData() {
		photosAdapter = new PhotosAdapter(getActivity(), photos);
		gvPhotosAlbum.setAdapter(photosAdapter);

		gvPhotosAlbum.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onPhotosClickListener.onPhotosClick(photos, position, folderName);
			}
		});
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (onPhotosClickListener == null)
			onPhotosClickListener = (OnPhotosClickListener) activity;
	}

	@Override
	public void onStart() {
		super.onStart();
		getActivity().setTitle(folderName);
	}

	@Override
	public void onStop() {
		super.onStop();
		getActivity().getActionBar().setTitle(getString(R.string.app_name));
	}

}
