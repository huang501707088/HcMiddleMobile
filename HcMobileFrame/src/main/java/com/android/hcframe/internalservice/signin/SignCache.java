/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2016-1-5 下午2:58:45
*/
package com.android.hcframe.internalservice.signin;

import java.util.Observable;

import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.android.hcframe.CacheManager;
import com.android.hcframe.HcApplication;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TemporaryCache;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.sql.SettingHelper;

public class SignCache extends Observable implements IHttpResponse, TemporaryCache {

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
		if (exit) mConfig = null;
		else {
			clearCache();
		}

	}

	private static class SignCofig {
		/** 纬度 */
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

	@Override
	public void notify(Object data, RequestCategory request,
			ResponseCategory category) {
		// TODO Auto-generated method stub
		switch (request) {
		case SIGNADDR:
			switch (category) {
			case SUCCESS:
				if (data != null && data instanceof String) {
					parseSignConfig((String) data);
					SettingHelper.setSignInfo(HcApplication.getContext(), (String) data);
				}
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
	 * @author jrjin
	 * @time 2016-1-5 下午3:16:08
	 * @return
	 */
	public boolean configExist(Context context) {
		if (TextUtils.isEmpty(mConfig.mLatitude)) {
			String data = SettingHelper.getSignInfo(context);
			if (!TextUtils.isEmpty(data))
				parseSignConfig(data);
			else { // 去服务器获取数据
				if (HcUtil.isNetWorkAvailable(context)) {
					HcHttpRequest.getRequest().sendSignAddrCommand(this);
				}
			}
		}
		return !TextUtils.isEmpty(mConfig.mLatitude);
	}
	
	private void parseSignConfig(String data) {
		try {
			JSONObject body = new JSONObject(data);
			if (HcUtil.hasValue(body, "latitude")) {
				mConfig.mLatitude = body.getString("latitude");
			}
			if (HcUtil.hasValue(body, "longitude")) {
				mConfig.mLongitude = body.getString("longitude");
			}
			if (HcUtil.hasValue(body, "signInTime")) {
				mConfig.mSignInTime = body.getString("signInTime");
				
			}
			if (HcUtil.hasValue(body, "signOutTime")) {
				mConfig.mSignOutTime = body.getString("signOutTime");
				
			}
			if (HcUtil.hasValue(body, "workInTime")) {
				mConfig.mWorkInTime = body.getString("workInTime");
				
			}
			if (HcUtil.hasValue(body, "workOutTime")) {
				mConfig.mWorkOutTime = body.getString("workOutTime");
				
			}
			if (HcUtil.hasValue(body, "maxDistance")) {
				mConfig.mDistance = body.getInt("maxDistance");
				HcLog.D(TAG + " #parseSignConfig mDistance = " +mConfig.mDistance);
			}
		} catch (Exception e) {
			// TODO: handle exception
			HcLog.D(TAG + " #parseSignConfig e = "+e);
		}
	}
	
	public void getSignList(String date) {
		HcHttpRequest.getRequest().sendSignItemCommand(date, this);
	}
	/**
	 * 清除缓存
	 * @author jrjin
	 * @time 2016-1-28 下午1:45:10
	 */
	public void clearCache() {
		mConfig = new SignCofig();
	}
}
