package com.android.hcframe.schedule;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhujiabin on 2016/11/18.
 */

public class ScheduleDateInfo implements Parcelable {

    private String mSheduleDate;

    private String mSheduleStr;
    /* 二级状态list */
    private List<ScheduleInfo> mScheduleInfoList;

    public ScheduleDateInfo() {
        mScheduleInfoList = new ArrayList<ScheduleInfo>();
    }

    public ScheduleDateInfo(String mSheduleDate, List<ScheduleInfo> mScheduleInfoList) {
        this.mSheduleDate = mSheduleDate;
        this.mScheduleInfoList = mScheduleInfoList;
    }

    protected ScheduleDateInfo(Parcel in) {
        mSheduleDate = in.readString();
    }

    public String getmSheduleDate() {
        return mSheduleDate;
    }

    public void setmSheduleDate(String mSheduleDate) {
        this.mSheduleDate = mSheduleDate;
    }

    public String getmSheduleStr() {
        return mSheduleStr;
    }

    public void setmSheduleStr(String mSheduleStr) {
        this.mSheduleStr = mSheduleStr;
    }

    public List<ScheduleInfo> getScheduleInfoList() {
        return mScheduleInfoList;
    }

    public void setScheduleInfoList(List<ScheduleInfo> mScheduleInfoList) {
        this.mScheduleInfoList = mScheduleInfoList;
    }

    public static final Creator<ScheduleDateInfo> CREATOR = new Creator<ScheduleDateInfo>() {
        @Override
        public ScheduleDateInfo createFromParcel(Parcel in) {
            return new ScheduleDateInfo(in);
        }

        @Override
        public ScheduleDateInfo[] newArray(int size) {
            return new ScheduleDateInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSheduleDate);
    }

    public void addScheduleInfo(ScheduleInfo info) {
        if (!mScheduleInfoList.contains(info))
            mScheduleInfoList.add(info);
    }
}
