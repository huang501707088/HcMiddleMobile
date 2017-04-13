package com.android.hcframe.schedule;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zhujiabin on 2016/11/14.
 */

public class ScheduleInfo implements Parcelable {
    /**
     * 日程安排的Id
     */
    private String mId;

    /**
     * 日程开始时间
     */
    private String mStartTime;
    /**
     * 日程结束时间
     */
    private String mEndTime;
    /**
     * 日程的任务类型：外派，内部任务等
     */
    private String mTaskType;
    /**
     * 日程参与人员
     */
    private String mTaskMembers;
    /**
     * 日程主题
     */
    private String mTheme;
    /**
     * 创建者
     */
    private String mCreator;
    /**
     * 是否为创建者
     */
    private String mCreatFlag;
    /**
     * 附件
     */
    private String mAddition;
    /**
     * 日程内容
     * */
    private String mContent;

    /**
     * 日程的日期
     * */
    private String mDate;
    /**
     * 同事人员名称
     * */
    private String mName;
    public ScheduleInfo() {
        super();
    }

    protected ScheduleInfo(Parcel in) {
        mId = in.readString();
        mStartTime = in.readString();
        mEndTime = in.readString();
        mTaskType = in.readString();
        mTaskMembers = in.readString();
        mTheme = in.readString();
        mCreator = in.readString();
        mCreatFlag = in.readString();
        mAddition = in.readString();
        mContent = in.readString();
        mDate = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mStartTime);
        dest.writeString(mEndTime);
        dest.writeString(mTaskType);
        dest.writeString(mTaskMembers);
        dest.writeString(mTheme);
        dest.writeString(mCreator);
        dest.writeString(mCreatFlag);
        dest.writeString(mAddition);
        dest.writeString(mContent);
        dest.writeString(mDate);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ScheduleInfo> CREATOR = new Creator<ScheduleInfo>() {
        @Override
        public ScheduleInfo createFromParcel(Parcel in) {
            return new ScheduleInfo(in);
        }

        @Override
        public ScheduleInfo[] newArray(int size) {
            return new ScheduleInfo[size];
        }
    };

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public String getStartTime() {
        return mStartTime;
    }

    public void setStartTime(String mStartTime) {
        this.mStartTime = mStartTime;
    }

    public String getEndTime() {
        return mEndTime;
    }

    public void setEndTime(String mEndTime) {
        this.mEndTime = mEndTime;
    }

    public String getTaskType() {
        return mTaskType;
    }

    public void setTaskType(String mTaskType) {
        this.mTaskType = mTaskType;
    }

    public String getTaskMembers() {
        return mTaskMembers;
    }

    public void setTaskMembers(String mTaskMembers) {
        this.mTaskMembers = mTaskMembers;
    }

    public String getTheme() {
        return mTheme;
    }

    public void setTheme(String mTheme) {
        this.mTheme = mTheme;
    }

    public String getAddition() {
        return mAddition;
    }

    public void setAddition(String mAddition) {
        this.mAddition = mAddition;
    }

    public String getCreatFlag() {
        return mCreatFlag;
    }

    public void setCreatFlag(String mCreatFlag) {
        this.mCreatFlag = mCreatFlag;
    }

    public String getCreator() {
        return mCreator;
    }

    public void setCreator(String mCreator) {
        this.mCreator = mCreator;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String mContent) {
        this.mContent = mContent;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }
}
