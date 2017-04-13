/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-23 下午1:00:51
*/
package com.android.hcframe.pcenter.headportrait;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class ImageInfo implements Parcelable {

	/** content:// 格式 */
	private String mUri;
	/** 文件的绝对路径 file:// 格式 */
	private String mFilePath;
	/** 原图的数据库位置 */
	private Uri mImageUri;
	/** 原图在数据库中的ID */
	private long mImageId;

	public ImageInfo() {}

	protected ImageInfo(Parcel in) {
		mUri = in.readString();
		mFilePath = in.readString();
		mImageUri = in.readParcelable(Uri.class.getClassLoader());
		mImageId = in.readLong();
	}

	public static final Creator<ImageInfo> CREATOR = new Creator<ImageInfo>() {
		@Override
		public ImageInfo createFromParcel(Parcel in) {
			return new ImageInfo(in);
		}

		@Override
		public ImageInfo[] newArray(int size) {
			return new ImageInfo[size];
		}
	};

	public void setThumbnailsUri(String uri) {
		mUri = uri;
	}
	
	public void setFilePath(String path) {
		mFilePath = path;
	}
	
	public void setImageUri(Uri uri) {
		mImageUri = uri;
	}
	
	public String getThumbnailsUri() {
		return mUri;
	}
	
	public String getImagePath() {
		return mFilePath;
	}
	
	public Uri getImageUri() {
		return mImageUri;
	}
	
	public void setImageId(long id) {
		mImageId = id;
	}
	
	public long getImageId() {
		return mImageId;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mUri);
		dest.writeString(mFilePath);
		dest.writeParcelable(mImageUri, flags);
		dest.writeLong(mImageId);
	}


}
