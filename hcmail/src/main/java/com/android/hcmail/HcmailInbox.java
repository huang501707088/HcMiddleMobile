package com.android.hcmail;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zhujiabin on 2017/3/16.
 */

public class HcmailInbox implements Parcelable {

    private String mInboxId;
    private String mInboxImg;
    private String mInboxTitle;
    private String mInboxName;
    private String mInboxDate;
    private boolean mChecked = false;

    public HcmailInbox() {
        super();
    }

    protected HcmailInbox(Parcel in) {
        mInboxId = in.readString();
        mInboxImg = in.readString();
        mInboxTitle = in.readString();
        mInboxName = in.readString();
        mInboxDate = in.readString();
        mChecked = in.readByte() != 0;
    }

    public static final Creator<HcmailInbox> CREATOR = new Creator<HcmailInbox>() {
        @Override
        public HcmailInbox createFromParcel(Parcel in) {
            return new HcmailInbox(in);
        }

        @Override
        public HcmailInbox[] newArray(int size) {
            return new HcmailInbox[size];
        }
    };

    public String getmInboxId() {
        return mInboxId;
    }

    public void setmInboxId(String mInboxId) {
        this.mInboxId = mInboxId;
    }

    public String getmInboxImg() {
        return mInboxImg;
    }

    public void setmInboxImg(String mInboxImg) {
        this.mInboxImg = mInboxImg;
    }

    public String getmInboxTitle() {
        return mInboxTitle;
    }

    public void setmInboxTitle(String mInboxTitle) {
        this.mInboxTitle = mInboxTitle;
    }

    public String getmInboxName() {
        return mInboxName;
    }

    public void setmInboxName(String mInboxName) {
        this.mInboxName = mInboxName;
    }

    public String getmInboxDate() {
        return mInboxDate;
    }

    public void setmInboxDate(String mInboxDate) {
        this.mInboxDate = mInboxDate;
    }

    public boolean ismChecked() {
        return mChecked;
    }

    public void setmChecked(boolean mChecked) {
        this.mChecked = mChecked;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mInboxId);
        dest.writeString(mInboxImg);
        dest.writeString(mInboxTitle);
        dest.writeString(mInboxName);
        dest.writeString(mInboxDate);
        dest.writeByte((byte) (mChecked ? 1 : 0));
    }
}
