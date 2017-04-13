package com.android.hcframe.monitor;

import android.content.Context;

import com.android.hcframe.HcConfig;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-2-19 13:49.
 */
public class OperationLogInfo {

    private static final String TAG = "OperationLogInfo";

    /**
     *应用编号
     */
    private String mAppId;

    /**
     * 应用模块编号
     */
    private String mModuleId;

    /**
     * 进入时间/开始时间
     */
    private String mStartTime;

    /**
     * 退出时间/结束时间
     */
    private String mEndTime;

    /**
     * 操作类型 1:应用程序；2：应用模块；3：接口请求
     */
    private int mType;

    /**
     * 应用版本
     */
    private String mVersion;

    /**
     * 当前登录帐号
     */
    private String mAccount;

    /**
     * 设备IMEI码
     */
    private String mImei;
    
    /**
     * 应用程序名/应用模块名/接口名
     */
    private String mName;

    /**
     * 应用程序名
     */
    private String mAppName;
    
    /**
     * 接口请求结果；
     * 接口请求是否成功 0：成功；1：失败
     */
    private int mResult = -1;

    public OperationLogInfo() {
        mAppName = HcConfig.getConfig().getVersionName();
        mAppId = HcConfig.getConfig().getClientId();
    }

    public OperationLogInfo(OperationLogInfo info) {
        mAccount = info.mAccount;
        mAppId = info.mAppId;
        mEndTime = info.mEndTime;
        mImei = info.mImei;
        mModuleId = info.mModuleId;
        mStartTime = info.mStartTime;
        mType = info.mType;
        mVersion = info.mVersion;
        mResult = info.mResult;
        mName = info.mName;
    }

    /**
     * 获取操作类型
     * @return 操作类型 1:应用程序；2：应用模块；3：接口请求
     */
    public int getType() {
        return mType;
    }

    /**
     * 获取操作时的帐号
     * @see com.android.hcframe.sql.SettingHelper#getAccount(Context)
     * @return 操作时的帐号
     */
    public String getAccount() {
        return mAccount;
    }

    /**
     * 获取操作的结束时间
     * @return 操作的结束时间,单位为毫秒.
     */
    public String getEndTime() {
        return mEndTime;
    }

    /**
     * 获取应用模块编号
     * @return 应用模块编号
     */
    public String getModuleId() {
        return mModuleId;
    }

    /**
     * 获取应用编号
     * @return 应用编号
     */
    public String getAppId() {
        return mAppId;
    }

    /**
     * 获取设备IMEI码
     * @return 设备IMEI码
     */
    public String getImei() {
        return mImei;
    }

    /**
     * 获取操作开始时间
     * @return 操作开始时间
     */
    public String getStartTime() {
        return mStartTime;
    }

    /**
     * 获取应用版本号
     * @return 应用版本号
     */
    public String getVersion() {
        return mVersion;
    }

    /**
     * 设置操作的当前用户
     * @param account 当前操作的用户
     */
    public void setAccount(String account) {
        mAccount = account;
    }


    /**
     * 设置操作结束时间
     * @param endTime 操作结束时间
     */
    public void setEndTime(String endTime) {
        mEndTime = endTime;
    }

    /**
     * 设置设备IMEI码
     * @param imei 设备IMEI码
     */
    public void setImei(String imei) {
        mImei = imei;
    }

    /**
     * 设置应用模块编号
     * @param moduleId 应用模块编号
     */
    public void setModuleId(String moduleId) {
        mModuleId = moduleId;
    }

    /**
     * 设置操作开始时间
     * @param startTime 操作开始时间
     */
    public void setStartTime(String startTime) {
        mStartTime = startTime;
    }

    /**
     * 设置操作类型
     * @param type 操作类型 1:应用程序；2：应用模块；3：接口请求
     */
    public void setType(int type) {
        mType = type;
    }

    /**
     * 设置日志的当前应用版本号
     * @param version 应用版本
     */
    public void setVersion(String version) {
        mVersion = version;
    }

    /**
     * 获取设备类别
     * @author jrjin
     * @time 2016-2-23 下午1:59:55
     * @return 0
     */
    public int getDeviceType() {
    	return 0;
    }
    
    public void setName(String name) {
    	mName = name;
    }
    
    public String getName() {
    	return mName;
    }
    
    public void setResult(int result) {
    	mResult = result;
    }
    
    public int getResult() {
    	return mResult;
    }

    public String getAppName() {
        return mAppName;
    }
}
