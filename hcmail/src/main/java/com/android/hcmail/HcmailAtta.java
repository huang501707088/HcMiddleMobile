package com.android.hcmail;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zhujiabin on 2017/3/24.
 */

public class HcmailAtta implements Parcelable {
    private String attaId;
    private String attaName;
    private boolean selected;
    public HcmailAtta() {
    }

    protected HcmailAtta(Parcel in) {
        attaId = in.readString();
        attaName = in.readString();
    }

    public static final Creator<HcmailAtta> CREATOR = new Creator<HcmailAtta>() {
        @Override
        public HcmailAtta createFromParcel(Parcel in) {
            return new HcmailAtta(in);
        }

        @Override
        public HcmailAtta[] newArray(int size) {
            return new HcmailAtta[size];
        }
    };

    public String getAttaId() {
        return attaId;
    }

    public void setAttaId(String attaId) {
        this.attaId = attaId;
    }

    public String getAttaName() {
        return attaName;
    }

    public void setAttaName(String attaName) {
        this.attaName = attaName;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(attaId);
        dest.writeString(attaName);
    }
}
