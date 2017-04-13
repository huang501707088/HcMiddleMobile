/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2016-1-5 下午4:46:32
*/
package com.android.hcframe.internalservice.sign;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.android.hcframe.HcApplication;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;


public class SignLoctionUtils {

	private static final String TAG = "SignLoctionUtils";
	
	private static double EARTH_RADIUS = 6378137;
	
	/**
	 * 目前1到目标2的距离
	 * @param lat1
	 * @param lng1
	 * @param lat2
	 * @param lng2
	 * @return
	 */
	private static double getDistance(double lat1, double lng1, double lat2,
			double lng2) {
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
				+ Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000) / 10000;
		return s;
	}

	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}
	
	/** 定位 */
	private static AMapLocationListener mListener = new AMapLocationListener() {

		@Override
		public void onLocationChanged(AMapLocation amapLocation) {
			HcLog.D(TAG + " #onLocationChanged amapLocation =" + amapLocation);
			if (amapLocation != null) {
				if (amapLocation.getErrorCode() == 0) {
					setLocationInfo(amapLocation);
					//定位成功回调信息，设置相关消息
//					amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
//					amapLocation.getLatitude();//获取纬度
//					amapLocation.getLongitude();//获取经度
//					amapLocation.getAccuracy();//获取精度信息
//					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//					Date date = new Date(amapLocation.getTime());
//					df.format(date);//定位时间
				} else {

					//显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
//					HcLog.D(TAG + "#onLocationChanged" + " AmapError location Error, ErrCode:"
//							+ amapLocation.getErrorCode() + ", errInfo:"
//							+ amapLocation.getErrorInfo());
				}
			}
		}
	};
	private static AMapLocationClient mLocationClient;
	private static AMapLocationClientOption mLocationOption;
	/** 定位10秒更新一次 */
	private static final int LOCATION_UPDATE_TIME = 10 * 1000;

	private static void initLocation(Context context) {

		mLocationClient = new AMapLocationClient(context);
		//初始化定位参数
		mLocationOption = new AMapLocationClientOption();
		//设置定位监听
		mLocationClient.setLocationListener(mListener);
		//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
		mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
		//设置定位间隔,单位毫秒,默认为2000ms
		mLocationOption.setInterval(10000);
		//设置定位参数
		mLocationClient.setLocationOption(mLocationOption);
		// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
		// 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
		// 在定位结束后，在合适的生命周期调用onDestroy()方法
		// 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
		//启动定位
		mLocationClient.startLocation();

	}
	
	private static void setLocationInfo(AMapLocation location) {
		// 清楚原先的距离
		mDistance = "";
		if(HcApplication.getContext() == null || location == null){
			return;
		}
		String lat = SignCache.getInstance().getLatitude();
		String lng = SignCache.getInstance().getLongitude();
		mAddress = location.getAddress();
		mLat = location.getLatitude();
		mLng = location.getLongitude();

		if (lat != null && lng != null) {
			double distance = getDistance(location.getLatitude(), location.getLongitude(), 
					Double.parseDouble(lat), Double.parseDouble(lng));
			mDistance = "" + distance;
		}
		HcLog.D(TAG + "#setLocationInfo address = "+mAddress + " lng = "+mLng + " lat = "+mLat +" distance ======== "+mDistance);
		if (mCallback != null) {
			mCallback.notifyDistance(mDistance);
		}

	}
	
	public static void startLocation(){
		if (mLocationClient != null) {
			if(!mLocationClient.isStarted())
				mLocationClient.startLocation();
		}else{
			if(HcApplication.getContext() == null)
				return;
			initLocation(HcApplication.getContext());
		}
	}
	
	public static void stopLocation() {
		mDistance = null;
		if (mLocationClient != null) {
			mLocationClient.stopLocation();
			mLocationClient = null;
		}
	}
	
	private static String mDistance;
	public static String getDistance() {
		if (mDistance == null) return "";
		return mDistance;
	}
	
	private static double mLat;
	
	private static double mLng;
	
	public static double getLat() {
		return mLat;
	}
	
	public static double getLng() {
		return mLng;
	}

	public interface DistanceCallback {
		void notifyDistance(String distance);
	}

	private static DistanceCallback mCallback;
	public static void setDistanceCallback(DistanceCallback callback) {
		mCallback = callback;
	}

	private static String mAddress;

	public static String getAddress() {
		return mAddress;
	}

}
