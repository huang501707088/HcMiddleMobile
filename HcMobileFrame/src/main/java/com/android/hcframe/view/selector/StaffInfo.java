package com.android.hcframe.view.selector;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-8-8 11:47.
 */

import android.os.Parcel;

/**
 * 员工信息
 */
public class StaffInfo extends ItemInfo {

    private String mUserId;

    /**
     * 列表是否支持多选
     */
    private boolean mMultipled;

    public StaffInfo() {}

    public StaffInfo(Parcel p) {
        super(p);
        mUserId = p.readString();
        mMultipled = p.readInt() == 1 ? true : false;
    }

    @Override
    public String getUserId() {
        return mUserId;
    }
    @Override
    public void setUserId(String userId) {
        mUserId = userId;
    }

    public boolean isMultipled() {
        return mMultipled;
    }

    public void setMultipled(boolean multipled) {
        mMultipled = multipled;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mUserId);
        dest.writeInt(mMultipled ? 1 : 0);
    }

    public static final Creator<StaffInfo> CREATOR = new Creator<StaffInfo>() {
        @Override
        public StaffInfo createFromParcel(Parcel in) {
            return new StaffInfo(in);
        }

        @Override
        public StaffInfo[] newArray(int size) {
            return new StaffInfo[size];
        }
    };
}
