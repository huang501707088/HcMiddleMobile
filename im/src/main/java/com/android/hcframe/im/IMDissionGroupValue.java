package com.android.hcframe.im;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by pc on 2016/10/13.
 */
public class IMDissionGroupValue implements Parcelable {

    private String mIMDissionGroupUserId;
    private String mIMDissionGroupUrl;
    private String mIMDissionGroupName;
    private String mNickname;

    public IMDissionGroupValue() {
    }

    protected IMDissionGroupValue(Parcel in) {
        mIMDissionGroupUserId = in.readString();
        mIMDissionGroupUrl = in.readString();
        mIMDissionGroupName = in.readString();
        mNickname = in.readString();
    }

    public static final Creator<IMDissionGroupValue> CREATOR = new Creator<IMDissionGroupValue>() {
        @Override
        public IMDissionGroupValue createFromParcel(Parcel in) {
            return new IMDissionGroupValue(in);
        }

        @Override
        public IMDissionGroupValue[] newArray(int size) {
            return new IMDissionGroupValue[size];
        }
    };

    public String getmIMDissionGroupUserId() {
        return mIMDissionGroupUserId;
    }

    public void setmIMDissionGroupUserId(String mIMDissionGroupUserId) {
        this.mIMDissionGroupUserId = mIMDissionGroupUserId;
    }

    public String getmIMDissionGroupUrl() {
        return mIMDissionGroupUrl;
    }

    public void setmIMDissionGroupUrl(String mIMDissionGroupUrl) {
        this.mIMDissionGroupUrl = mIMDissionGroupUrl;
    }

    public String getmIMDissionGroupName() {
        return mIMDissionGroupName;
    }

    public void setmIMDissionGroupName(String mIMDissionGroupName) {
        this.mIMDissionGroupName = mIMDissionGroupName;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mIMDissionGroupUserId);
        dest.writeString(mIMDissionGroupUrl);
        dest.writeString(mIMDissionGroupName);
        dest.writeString(mNickname);
    }

    public String getNickname() {
        return mNickname;
    }

    public void setNickname(String nickname) {
        this.mNickname = nickname;
    }
}
