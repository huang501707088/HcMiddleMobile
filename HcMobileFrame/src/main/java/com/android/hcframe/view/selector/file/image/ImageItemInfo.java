package com.android.hcframe.view.selector.file.image;

import android.os.Parcel;

import com.android.hcframe.pcenter.headportrait.ImageInfo;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-10-9 14:11.
 */

public class ImageItemInfo extends ImageInfo {

    private boolean mSelected;

    /**
     * 文件名不带扩展名
     */
    private String mFileName;

    private String mSize;

    public ImageItemInfo() {
        super();
    }

    public ImageItemInfo(Parcel p) {
        super(p);
        mSelected = p.readInt() == 1;
        mFileName = p.readString();
        mSize = p.readString();
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        mSelected = selected;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    public String getSize() {
        return mSize;
    }

    public void setSize(String size) {
        mSize = size;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(mSelected ? 1 : 0);
        dest.writeString(mFileName);
        dest.writeString(mSize);
    }

    public static final Creator<ImageItemInfo> CREATOR = new Creator<ImageItemInfo>() {
        @Override
        public ImageItemInfo createFromParcel(Parcel in) {
            return new ImageItemInfo(in);
        }

        @Override
        public ImageItemInfo[] newArray(int size) {
            return new ImageItemInfo[size];
        }
    };
}
