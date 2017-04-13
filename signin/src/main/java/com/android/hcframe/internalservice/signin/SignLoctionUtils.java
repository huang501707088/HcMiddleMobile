/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2016-1-5 下午4:46:32
*/
package com.android.hcframe.internalservice.signin;

import android.content.Context;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcLog;
//import com.baidu.location.BDLocation;
//import com.baidu.location.BDLocationListener;
//import com.baidu.location.LocationClient;
//import com.baidu.location.LocationClientOption;

/**
 * @deprecated
 * @see com.android.hcframe.internalservice.sign.SignLoctionUtils
 */
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
//	private static LocationClient mLocationClient;
	/** 定位10秒更新一次 */
	private static final int LOCATION_UPDATE_TIME = 10 * 1000;
	
//	private static BDLocationListener mListener = new BDLocationListener() {
//
//		@Override
//		public void onReceiveLocation(BDLocation arg0) {
//			// TODO Auto-generated method stub
//			HcLog.D(TAG+"# it is in onReceiveLocation!");
//			if (arg0 == null) return;
//			HcLog.D(TAG+" onReceiveLocation lat = "+arg0.getLatitude()+" lag = "+arg0.getLongitude());
//			HcLog.D(TAG+" onReceiveLocation address = "+arg0.getAddrStr()+" city name = "+arg0.getCity() +" city code = "+arg0.getCityCode());
//			setLocationInfo(arg0);
//		}
//
//		@Override
//		public void onReceivePoi(BDLocation arg0) {
//			// TODO Auto-generated method stub
//			HcLog.D(TAG+"# it is in onReceivePoi! location = "+arg0);
//		}
//
//	};
	
	private static void initLocation(Context context) {
//		mLocationClient = new LocationClient(context);
//		mLocationClient.registerLocationListener(mListener);
//		LocationClientOption option = new LocationClientOption();
//		option.setOpenGps(true);
//		option.setAddrType("all");//返回的定位结果包含地址信息
////		option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
//		option.setCoorType("gcj02");
//		option.setScanSpan(LOCATION_UPDATE_TIME);//设置发起定位请求的间隔时间为5000ms
//		option.disableCache(true);//禁止启用缓存定位
//		option.setPoiNumber(5);	//最多返回POI个数
//		option.setPoiDistance(1000); //poi查询距离
//		option.setPoiExtraInfo(true); //是否需要POI的电话和地址等详细信息
//		mLocationClient.setLocOption(option);
//		mLocationClient.start();
	}
	
//	private static void setLocationInfo(BDLocation location) {
//		if(HcApplication.getContext() == null || location == null)
//			return;
//		String lat = SignCache.getInstance().getLatitude();
//		String lng = SignCache.getInstance().getLongitude();
//		mLat = location.getLatitude();
//		mLng = location.getLongitude();
//		if (lat != null && lng != null) {
//			double distance = getDistance(location.getLatitude(), location.getLongitude(),
//					Double.parseDouble(lat), Double.parseDouble(lng));
//			mDistance = "" + distance;
//		}
//	}
	
	public static void startLocation(){
//		if (mLocationClient != null) {
//			if(!mLocationClient.isStarted())
//				mLocationClient.start();
//		}else{
//			if(HcApplication.getContext() == null)
//				return;
//			initLocation(HcApplication.getContext());
//		}
	}
	
	public static void stopLocation() {
//		if (mLocationClient != null) {
//			mLocationClient.stop();
//			mLocationClient = null;
//		}
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
}
