/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2016-1-5 下午2:58:45
*/
package com.android.hcframe.internalservice.sign;

import android.content.Context;
import android.text.TextUtils;

import com.android.hcframe.CacheManager;
import com.android.hcframe.HcApplication;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcObservable;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TemporaryCache;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.sql.SettingHelper;

import org.json.JSONObject;

import java.util.Observable;

public class SignCache extends HcObservable implements IHttpResponse, TemporaryCache {

    private static final String TAG = "SignCache";

    private final static SignCache CACHE = new SignCache();

    private SignCofig mConfig;

    private SignCache() {
        CacheManager.getInstance().addCache(this);
        mConfig = new SignCofig();
    }

    public static SignCache getInstance() {
        return CACHE;
    }

    @Override
    public void clearCache(boolean exit) {
        /**@jrjin
         * 退出应用没有关闭进程
         * @date 2016-09-18
        if (exit) mConfig = null;
        else {
            clearCache();
        }*/
        clearCache();
    }

    private static class SignCofig {
        /**
         * 纬度
         */
        String mLatitude;
        /**
         * 经度
         */
        String mLongitude;
        /**
         * 标准上班时间
         */
        String mWorkInTime;
        /**
         * 标准下班时间
         */
        String mWorkOutTime;
        /**
         * 当天签到时间
         */
        String mSignInTime;
        /**
         * 当天签出时间
         */
        String mSignOutTime;
        /**
         * 用户绑定的设备号，可为空
         */
        String mImei;
        /**
         * 有效距离
         */
        int mDistance;
    }

    public int getMaxDistance() {
        return mConfig.mDistance;
    }

    public void setMaxDistance(int maxDistance) {
        mConfig.mDistance = maxDistance;
    }

    public String getLatitude() {
        return mConfig.mLatitude;
    }

    public void setLatitude(String latitude) {
        mConfig.mLatitude = latitude;
    }

    public String getLongitude() {
        return mConfig.mLongitude;
    }

    public void setLongitude(String longitude) {
        mConfig.mLongitude = longitude;
    }

    public String getWorkInTime() {
        return mConfig.mWorkInTime;
    }

    public void setWorkInTime(String workInTime) {
        mConfig.mWorkInTime = workInTime;
    }

    public String getWorkOutTime() {
        return mConfig.mWorkOutTime;
    }

    public void setWorkOutTime(String workOutTime) {
        mConfig.mWorkOutTime = workOutTime;
    }

    public String getSignInTime() {
        return mConfig.mSignInTime;
    }

    public void setSignInTime(String signInTime) {
        mConfig.mSignInTime = signInTime;
    }

    public String getSignOutTime() {
        return mConfig.mSignOutTime;
    }

    public void setSignOutTime(String signOutTime) {
        mConfig.mSignOutTime = signOutTime;
    }

    public String getImei() {
        return mConfig.mImei;
    }

    public void setImei(String imei) {
        mConfig.mImei = imei;
    }
    @Override
    public void notify(Object data, RequestCategory request,
                       ResponseCategory category) {
        // TODO Auto-generated method stub
        switch (request) {
            case SIGNADDR:
                switch (category) {
                    case SUCCESS:
                        if (data != null && data instanceof String) {
                            parseSignConfig((String) data, true);
                            SettingHelper.setSignInfo(HcApplication.getContext(), (String) data);
                        }
                        notifyObservers(this, data, RequestCategory.CONTACTS_REQUEST, category);
                        break;
                    case NOT_MATCH: // 未设置考勤点
                        clearCache();
                        SettingHelper.setSignInfo(HcApplication.getContext(), "");
                        HcUtil.showToast(HcApplication.getContext(), "未设置考勤点！");
                        break;

                    default:
                        break;
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
        // TODO Auto-generated method stub

    }

    /**
     * 判断签到的一些配置信息是否存在;
     * 此方法不是很严谨
     *
     * @return
     * @author jrjin
     * @time 2016-1-5 下午3:16:08
     */
    public boolean configExist(Context context) {
        // 去服务器获取数据
        if (HcUtil.isNetWorkAvailable(context)) {
            HcHttpRequest.getRequest().sendSignAddrCommand(this);
        }
        return true;
    }

    /**
     * 去服务端获取签到信息的数据
     * @param context
     * @return 信息是否已经存在
     */
    public boolean repeatConfigExist(Context context) {
        if (TextUtils.isEmpty(mConfig.mLatitude)) {
            String data = SettingHelper.getSignInfo(context);
            if (!TextUtils.isEmpty(data)) {
                parseSignConfig(data, false);
            }
        }
        // 每次都去服务器获取数据,因为有当天的签到数据
        if (HcUtil.isNetWorkAvailable(context)) {
            HcHttpRequest.getRequest().sendSignAddrCommand(this);
        }
        return !TextUtils.isEmpty(mConfig.mLatitude);
    }

    /**
     *
     * @param data
     * @param update 是否需要更新替换签到和签出的信息
     */
    private void parseSignConfig(String data, boolean update) {
        if (update) clearCache();
        try {
            JSONObject body = new JSONObject(data);
            if (HcUtil.hasValue(body, "latitude")) {
                mConfig.mLatitude = body.getString("latitude");
            }
            if (HcUtil.hasValue(body, "longitude")) {
                mConfig.mLongitude = body.getString("longitude");
            }
            if (update) {
                if (HcUtil.hasValue(body, "signInTime")) {
                    mConfig.mSignInTime = body.getString("signInTime");

                } else {
                    mConfig.mSignInTime = "";
                }
                if (HcUtil.hasValue(body, "signOutTime")) {
                    mConfig.mSignOutTime = body.getString("signOutTime");

                } else {
                    mConfig.mSignOutTime = "";
                }
            } else {
                mConfig.mSignInTime = SettingHelper.getSigninTime(HcApplication.getContext());
                mConfig.mSignOutTime = SettingHelper.getSignoutTime(HcApplication.getContext());
            }

            if (HcUtil.hasValue(body, "workInTime")) {
                mConfig.mWorkInTime = body.getString("workInTime");

            }
            if (HcUtil.hasValue(body, "workOutTime")) {
                mConfig.mWorkOutTime = body.getString("workOutTime");

            }
            if (HcUtil.hasValue(body, "maxDistance")) {
                mConfig.mDistance = body.getInt("maxDistance");
                HcLog.D(TAG + " #parseSignConfig mDistance = " + mConfig.mDistance);
            }
            if (HcUtil.hasValue(body, "imei")) {
                mConfig.mImei = body.getString("imei");
            }

            if (update) {
                // 更新签到签出的缓存
                SettingHelper.setSigninTime(HcApplication.getContext(), mConfig.mSignInTime);
                SettingHelper.setSignoutTime(HcApplication.getContext(), mConfig.mSignOutTime);
            }
        } catch (Exception e) {
            // TODO: handle exception
            HcLog.D(TAG + " #parseSignConfig e = " + e);
        }
    }


    /**
     * 清除缓存
     *
     * @author jrjin
     * @time 2016-1-28 下午1:45:10
     */
    public void clearCache() {
        mConfig = new SignCofig();
    }

}
