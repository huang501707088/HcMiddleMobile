package com.android.hcframe.netdisc.image;

import com.android.hcframe.pcenter.headportrait.ImageInfo;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-7-28 14:09.
 */
public class NetdiscImageInfo extends ImageInfo {

    private boolean mSelected;

    /**
     * 文件名不带扩展名
     */
    private String mFileName;

    private String mSize;

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
}
