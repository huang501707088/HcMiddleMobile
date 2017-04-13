package com.android.hcframe.netdisc.audio;

import com.android.hcframe.netdisc.image.NetdiscImageInfo;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-8-1 13:46.
 */
public class NetdiscAudioInfo extends NetdiscImageInfo {

    private long mDate;

    private boolean mAudio = true;
    /** 图片资源ID */
    private int mResId;
    /** 带扩展名的文件名 */
    private String mDisplayName;

    public long getDate() {
        return mDate;
    }

    public void setDate(long date) {
        mDate = date;
    }

    public boolean isAudio() {
        return mAudio;
    }

    public void setAudio(boolean audio) {
        mAudio = audio;
    }

    public int getResId() {
        return mResId;
    }

    public void setResId(int resId) {
        mResId = resId;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String displayName) {
        mDisplayName = displayName;
    }
}
