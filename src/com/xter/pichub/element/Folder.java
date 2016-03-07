package com.xter.pichub.element;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by XTER on 2016/1/12.
 * 图片文件夹类（父级目录）
 */
public class Folder implements Parcelable{
	private long folderId;
	private String folderName;
	private int imgCount;
	private String coverUrl;

	private List<Photo> photos;

	public Folder() {
	}

	/**
	 *
	 * @param folderId          文件夹ID
	 * @param folderName    文件夹名称
	 * @param imgCount       文件数量
	 * @param coverUrl         封面文件URL
	 * @param photos           文件夹文件集合
	 */
	public Folder(long folderId, String folderName, int imgCount, String coverUrl, List<Photo> photos) {
		this.folderId = folderId;
		this.folderName = folderName;
		this.imgCount = imgCount;
		this.coverUrl = coverUrl;
		this.photos = photos;
	}

	protected Folder(Parcel in) {
		folderId = in.readLong();
		folderName = in.readString();
		imgCount = in.readInt();
		coverUrl = in.readString();
		photos = in.createTypedArrayList(Photo.CREATOR);
	}

	public static final Creator<Folder> CREATOR = new Creator<Folder>() {
		@Override
		public Folder createFromParcel(Parcel in) {
			return new Folder(in);
		}

		@Override
		public Folder[] newArray(int size) {
			return new Folder[size];
		}
	};

	public long getFolderId() {
		return folderId;
	}

	public void setFolderId(long folderId) {
		this.folderId = folderId;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public int getImgCount() {
		return imgCount;
	}

	public void setImgCount(int imgCount) {
		this.imgCount = imgCount;
	}

	public String getCoverUrl() {
		return coverUrl;
	}

	public void setCoverUrl(String coverUrl) {
		this.coverUrl = coverUrl;
	}

	public List<Photo> getPhotos() {
		return photos;
	}

	public void setPhotos(List<Photo> photos) {
		this.photos = photos;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(folderId);
		dest.writeString(folderName);
		dest.writeInt(imgCount);
		dest.writeString(coverUrl);
		dest.writeTypedList(photos);
	}
}
