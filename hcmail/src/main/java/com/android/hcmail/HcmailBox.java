package com.android.hcmail;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zhujiabin on 2017/3/22.
 */

public class HcmailBox implements Parcelable {

    private String boxId;
    private String boxImg;
    private String boxTitle;
    private String boxName;
    private String boxDate;
    private boolean checked = false;

    public HcmailBox() {
    }

    protected HcmailBox(Parcel in) {
        boxId = in.readString();
        boxImg = in.readString();
        boxTitle = in.readString();
        boxName = in.readString();
        boxDate = in.readString();
        checked = in.readByte() != 0;
    }

    public static final Creator<HcmailBox> CREATOR = new Creator<HcmailBox>() {
        @Override
        public HcmailBox createFromParcel(Parcel in) {
            return new HcmailBox(in);
        }

        @Override
        public HcmailBox[] newArray(int size) {
            return new HcmailBox[size];
        }
    };

    public String getBoxId() {
        return boxId;
    }

    public void setBoxId(String boxId) {
        this.boxId = boxId;
    }

    public String getBoxImg() {
        return boxImg;
    }

    public void setBoxImg(String boxImg) {
        this.boxImg = boxImg;
    }

    public String getBoxTitle() {
        return boxTitle;
    }

    public void setBoxTitle(String boxTitle) {
        this.boxTitle = boxTitle;
    }

    public String getBoxName() {
        return boxName;
    }

    public void setBoxName(String boxName) {
        this.boxName = boxName;
    }

    public String getBoxDate() {
        return boxDate;
    }

    public void setBoxDate(String boxDate) {
        this.boxDate = boxDate;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(boxId);
        dest.writeString(boxImg);
        dest.writeString(boxTitle);
        dest.writeString(boxName);
        dest.writeString(boxDate);
        dest.writeByte((byte) (checked ? 1 : 0));
    }
}
