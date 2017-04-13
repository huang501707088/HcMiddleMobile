package com.android.hcframe.view.selector.file;

import android.os.Parcel;

import com.android.hcframe.view.selector.StaffInfo;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-11-28 11:11.
 */

public class FileInfo extends StaffInfo {

    /**
     * 文件的绝对路径
     */
    private String mFilePath;

    public FileInfo() {
        super();
    }

    public FileInfo(Parcel p) {
        super(p);
        mFilePath = p.readString();
    }

    @Override
    public String getUserId() {
        return "";
    }

    public static final Creator<FileInfo> CREATOR = new Creator<FileInfo>() {
        @Override
        public FileInfo createFromParcel(Parcel in) {
            return new FileInfo(in);
        }

        @Override
        public FileInfo[] newArray(int size) {
            return new FileInfo[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mFilePath);
    }

    /**
     * 获取文件绝对路径
     * @return
     */
    @Override
    public String getFilePath() {
        return mFilePath;
    }

    @Override
    public void setFilePath(String filePath) {
        mFilePath = filePath;
    }
}
