package com.android.hcframe.internalservice.linhai;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-7-1 15:27.
 */
public class LHSignDayInfo {

    /**
     * 查询日期：年月日2015-06-01
     */
    private String mDate;
    /**
     * 用户id
     */
    private String mUserId;
    /**
     * 用户真实姓名
     */
    private String mName;
    /**
     * 签到类型：0-上班 1-外勤 2-请假
     */
    private String mType;
    /**
     * 考勤状态（中文）
     */
    private String mStatus;
    /**
     * 上午签到时间：08:28:09
     */
    private String mSigninTime;
    /**
     * 下午签出时间：17:30:03
     */
    private String mSignoutTime;
    /**
     * 上午签到地址（请假时，显示上午请假说明）
     */
    private String mSigninAddress;
    /**
     * 下午签到地址（请假时，显示下午请假说明）
     */
    private String mSignoutAddress;
    /**
     * 上午签到是否有图片：0-没有 1-有
     */
    private boolean mShowSigninIcon;
    /**
     * 下午签到是否有图片：0-没有 1-有
     */
    private boolean mShowSignoutIcon;

    public void setDate(String date) {
        mDate = date;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setType(String type) {
        mType = type;
    }

    public void setStatus(String status) {
        mStatus = status;
    }

    public void setSigninTime(String time) {
        mSigninTime = time;
    }

    public void setSignoutTime(String time) {
        mSignoutTime = time;
    }

    public void setSignoutAddress(String address) {
        mSignoutAddress = address;
    }

    public void setSigninAddress(String address) {
        mSigninAddress = address;
    }

    public void setShowSignoutIcon(boolean showSignoutIcon) {
        mShowSignoutIcon = showSignoutIcon;
    }

    public void setShowSigninIcon(boolean showSigninIcon) {
        mShowSigninIcon = showSigninIcon;
    }

    public String getDate() {
        return mDate;
    }

    public String getName() {
        return mName;
    }

    public boolean isShowSigninIcon() {
        return mShowSigninIcon;
    }

    public boolean isShowSignoutIcon() {
        return mShowSignoutIcon;
    }

    public String getSigninAddress() {
        return mSigninAddress;
    }

    public String getSigninTime() {
        return mSigninTime;
    }

    public String getSignoutAddress() {
        return mSignoutAddress;
    }

    public String getSignoutTime() {
        return mSignoutTime;
    }

    public String getStatus() {
        return mStatus;
    }

    public String getType() {
        return mType;
    }

    public String getUserId() {
        return mUserId;
    }
}
