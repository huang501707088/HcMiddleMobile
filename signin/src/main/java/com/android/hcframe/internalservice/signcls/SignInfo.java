package com.android.hcframe.internalservice.signcls;

import java.util.ArrayList;

/**
 * Created by pc on 2016/6/7.
 */
public class SignInfo {
    public String mId;

    public String mTitle;

    public String mIconUrl;

    public String mAddress;;

    public String mDate;

    public String mContentUrl;
    /**
     * 新闻内容类型：
     */
    public String mContentType;

    public ArrayList<String> mImgs=new ArrayList<String>();

    /** 签到图片张数 */
    public int mCount;

    public boolean mScroll = false;
    public SignInfo() {
    }
}
