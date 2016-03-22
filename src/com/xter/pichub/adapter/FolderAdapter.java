package com.xter.pichub.adapter;

import java.util.List;

import com.xter.pichub.R;
import com.xter.pichub.element.Folder;
import com.xter.pichub.util.ImageLoader;
import com.xter.pichub.view.CascadeView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Created by XTER on 2016/1/11. 直接存储view以达到缓存已经下载的图片 能否将viewholder和list缓存结合使用？
 */
public class FolderAdapter extends BaseAdapter {

	LayoutInflater layoutInflater;
	List<Folder> folders;
	ImageLoader loader;

	/**
	 * 主要传入文件夹URL值
	 *
	 * @param context
	 *            所依赖的上下文
	 * @param list
	 *            文件夹
	 */
	public FolderAdapter(Context context, List<Folder> list) {
		loader = ImageLoader.build(context);
		this.layoutInflater = LayoutInflater.from(context);
		this.folders = list;
	}

	@Override
	public int getCount() {
		return folders.size();
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
		// 得到封面上的四张图
		View view;
		if (convertView == null)
			view = layoutInflater.inflate(R.layout.item_folder, null);
		else
			view = convertView;
//		if(parent instanceof AlbumGridView){
//			if(((AlbumGridView) parent).isOnMeasure()){
//				return view;
//			}else{
				String[] uris = folders.get(position).getCoverUris();
				CascadeView c = (CascadeView) view.findViewById(R.id.cv_folder_cover);
				loader.bindBitmap(uris, c, true);
//			}
//		}

		return view;
	}

	public void notifyDataSetChanged(List<Folder> folders) {
		this.folders = folders;
		super.notifyDataSetChanged();
	}

}
