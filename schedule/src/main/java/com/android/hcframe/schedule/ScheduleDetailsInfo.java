package com.android.hcframe.schedule;

import android.os.Parcel;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-12-5 11:01.
 */

public class ScheduleDetailsInfo extends ScheduleInfo {

    private static final String TAG = "ScheduleDetailsInfo";


    private String mUserId;
    /**
     * 执行人
     * */
    private String mExecutor;

    /**
     * 附件：
     * 格式id:name;id:name
     */
    private String mAnnexList;

    public ScheduleDetailsInfo() {super();}

    public ScheduleDetailsInfo(Parcel p) {
        super(p);
        mExecutor = p.readString();
        mAnnexList = p.readString();
    }

    public ScheduleDetailsInfo(ScheduleInfo info) {
        setId(info.getId());
        setTheme(info.getTheme());
        setContent(info.getContent());
        setDate(info.getDate());
        setAddition(info.getAddition());
        setCreatFlag(info.getCreatFlag());
        setCreator(info.getCreator());
        setEndTime(info.getEndTime());
        setmName(info.getmName());
        setStartTime(info.getStartTime());
        setTaskMembers(info.getTaskMembers());
        setTaskType(info.getTaskType());
    }

    public String getAnnexList() {
        return mAnnexList;
    }

    public void setAnnexList(String annexList) {
        mAnnexList = annexList;
    }

    public String getExecutor() {
        return mExecutor;
    }

    public void setExecutor(String executor) {
        mExecutor = executor;
    }

    public String getmUserId() {
        return mUserId;
    }

    public void setmUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mExecutor);
        dest.writeString(mAnnexList);
        dest.writeString(mUserId);
    }

    public static final Creator<ScheduleDetailsInfo> CREATOR = new Creator<ScheduleDetailsInfo>() {
        @Override
        public ScheduleDetailsInfo createFromParcel(Parcel in) {
            return new ScheduleDetailsInfo(in);
        }

        @Override
        public ScheduleDetailsInfo[] newArray(int size) {
            return new ScheduleDetailsInfo[size];
        }
    };
}
