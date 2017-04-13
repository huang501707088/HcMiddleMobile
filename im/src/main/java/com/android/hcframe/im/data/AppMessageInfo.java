package com.android.hcframe.im.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-9-9 15:22.
 */
public class AppMessageInfo implements Parcelable {

    /**
     * 消息应用的ID/群的ID/单聊的ID(双方userId合并的MD5值)/其他(可能是系统的消息ID)
     */
    private String mId;

    /**
     * 消息类型 1.应用模块推送的消息
     * 2.群聊的消息
     * 3.单聊的消息
     * 4.系统的消息
     */
    private int mType;
    /**
     * 消息的标题,可能是应用的名字,群名字,联系人
     */
    private String mTitle;
    /**
     * 消息的内容
     */
    private String mContent;

    /**
     * 消息的发布最后日期
     */
    private String mDate;
    /**
     * 头像地址,要是是单聊对象的话就是userId
     */
    private String mIconUri;
    /** 未读的消息数 */
    private int mCount;

    /** 系统消息的ID,只有一条记录 */
    public static final String SYSTEM_MESSAGE_ID = "100000";

    public AppMessageInfo() {}

    public AppMessageInfo(Parcel p) {
        mId = p.readString();
        mType = p.readInt();
        mTitle = p.readString();
        mContent = p.readString();
        mDate = p.readString();
        mIconUri = p.readString();
        mCount = p.readInt();
    }

    public static final Creator<AppMessageInfo> CREATOR = new Creator<AppMessageInfo>() {
        @Override
        public AppMessageInfo createFromParcel(Parcel in) {
            return new AppMessageInfo(in);
        }

        @Override
        public AppMessageInfo[] newArray(int size) {
            return new AppMessageInfo[size];
        }
    };

    public String getContent() {
        if (mContent == null) mContent = "";
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeInt(mType);
        dest.writeString(mTitle);
        dest.writeString(mContent);
        dest.writeString(mDate);
        dest.writeString(mIconUri);
        dest.writeInt(mCount);
    }

    public String getIconUri() {
        return mIconUri;
    }

    public void setIconUri(String iconUri) {
        mIconUri = iconUri;
    }

    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        mCount = count;
    }
}
