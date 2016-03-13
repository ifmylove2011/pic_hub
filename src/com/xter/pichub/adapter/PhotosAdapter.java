package com.xter.pichub.adapter;

import java.util.List;

import com.xter.pichub.R;
import com.xter.pichub.element.Photo;
import com.xter.pichub.util.ImageLoader;
import com.xter.pichub.view.CascadeView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class PhotosAdapter extends BaseAdapter {

	LayoutInflater layoutInflater;
	List<Photo> photos;
	ImageLoader loader;

	public PhotosAdapter(Context context, List<Photo> list) {
		loader = ImageLoader.build(context);
		this.layoutInflater = LayoutInflater.from(context);
		this.photos = list;
	}

	@Override
	public int getCount() {
		return photos.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		String[] uris = { "file://" + photos.get(position).getPath() };
		View view;
		if (convertView == null)
			view = layoutInflater.inflate(R.layout.item_photo, null);
		else
			view = convertView;

		CascadeView c = (CascadeView) view.findViewById(R.id.cv_photo);
		loader.bindBitmap(uris, c, true);
		
		return view;
	}

}
