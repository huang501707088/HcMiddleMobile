package com.android.hcframe.netdisc.data;

import android.os.Parcel;

import com.android.frame.download.DownloadUtil;
import com.android.hcframe.netdisc.util.NetdiscUtil;

import org.apache.http.util.NetUtils;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-7-28 16:53.
 */
public class UploadFileInfo extends FileInfo {

    private String mMd5;

    /** 总共的分片 */
    private int mTotle;
    /** 当前已经上传的分片 */
    private int mCurrent;

    public UploadFileInfo() {
        super();
        mTotle = 1;
    }

    public UploadFileInfo(Parcel p) {
        super(p);
        mMd5 = p.readString();
        mTotle = p.readInt();
        mCurrent = p.readInt();
    }

    public static final Creator<UploadFileInfo> CREATOR = new Creator<UploadFileInfo>() {
        @Override
        public UploadFileInfo createFromParcel(Parcel source) {
            return new UploadFileInfo(source);
        }

        @Override
        public UploadFileInfo[] newArray(int size) {
            return new UploadFileInfo[0];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mMd5);
        dest.writeInt(mTotle);
        dest.writeInt(mCurrent);
    }

    public String getMd5() {
        return mMd5;
    }

    public void setMd5(String md5) {
        mMd5 = md5;
    }

    @Override
    public String getLogs() {
        return super.getLogs() + "&Md5_key = "+mMd5;
    }

    public int getCurrent() {
        return mCurrent;
    }

    public void setCurrent(int current) {
        mCurrent = current;
    }

    public int getTotle() {
        return mTotle;
    }

    @Override
    public void setFileSize(String fileSize) {
        super.setFileSize(fileSize);
        mTotle = (int) (Long.valueOf(fileSize) + DownloadUtil.FILE_CHUNK_SIZE - 1) / DownloadUtil.FILE_CHUNK_SIZE;
    }
}
