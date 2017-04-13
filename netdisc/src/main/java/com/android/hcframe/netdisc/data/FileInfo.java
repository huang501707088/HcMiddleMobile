package com.android.hcframe.netdisc.data;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-7-28 16:25.
 */
public class FileInfo implements Parcelable {

    /** 文件名不带扩展名 */
    private String mFileName;

    private String mFileSize;

    /**
     * 文件扩展名
     */
    private String mFileExt;

    /**
     * 文件操作状态0：等待；1：进行中；
     */
    private int mStatus;
    /**
     * 文件绝对路径
     */
    private String mFilePath;

    /** 文件ID */
    private String mFileDir;

    /**
     * 文件唯一区别的key
     * <p>文件绝对路径 + 文件大小</p>
     */
    private String mFileKey;

    public FileInfo() {}

    public FileInfo(Parcel p) {
        mFileName = p.readString();
        mFileSize = p.readString();
        mFileExt = p.readString();
        mStatus = p.readInt();
        mFilePath = p.readString();
        mFileDir = p.readString();
        mFileKey = p.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mFileName);
        dest.writeString(mFileSize);
        dest.writeString(mFileExt);
        dest.writeInt(mStatus);
        dest.writeString(mFilePath);
        dest.writeString(mFileDir);
        dest.writeString(mFileKey);
    }

    public static final Creator<FileInfo> CREATOR = new Creator<FileInfo>() {
        @Override
        public FileInfo createFromParcel(Parcel source) {
            return new FileInfo(source);
        }

        @Override
        public FileInfo[] newArray(int size) {
            return new FileInfo[0];
        }
    };

    public String getFileDir() {
        return mFileDir;
    }

    public void setFileDir(String fileDir) {
        mFileDir = fileDir;
    }

    public String getFileExt() {
        return mFileExt;
    }

    public void setFileExt(String fileExt) {
        mFileExt = fileExt;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String filePath) {
        mFilePath = filePath;
    }

    public String getFileSize() {
        return mFileSize;
    }

    public void setFileSize(String fileSize) {
        mFileSize = fileSize;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public String getFileKey() {
        return mFileKey;
    }

    public void setFileKey(String fileKey) {
        mFileKey = fileKey;
    }

    public String getLogs() {
        return "FileName="+mFileName+"&FileSize="+mFileSize+"&FileExt="+mFileExt+
                "&FileDirID="+mFileDir+"&FileKey="+mFileKey;
    }
}
