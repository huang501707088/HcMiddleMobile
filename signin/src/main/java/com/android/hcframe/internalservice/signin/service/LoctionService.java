package com.android.hcframe.internalservice.signin.service;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;


import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.internalservice.signin.Loction;
import com.android.hcframe.sql.SettingHelper;

public class LoctionService extends Service implements IHttpResponse {

	private static final String TAG = "LoctionService";

	private Timer mTimer;

	private TimerTask mSignInTask;
	
	private TimerTask mSignOutTask;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		HcLog.D(TAG + " it is onDestroy!");
		com.android.hcframe.internalservice.sign.SignLoctionUtils.stopLocation();
		if (mSignInTask != null) {
			mSignInTask.cancel();
			mSignInTask = null;
		}
		if (mSignOutTask != null) {
			mSignOutTask.cancel();
			mSignOutTask = null;
		}
		
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		super.onDestroy();
		
	}


	@Override
	public void notify(Object data, RequestCategory request,
			ResponseCategory category) {
		// TODO Auto-generated method stub
		;
	}

	@Override
	public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		com.android.hcframe.internalservice.sign.SignLoctionUtils.startLocation();
		if (mTimer == null) {
			mTimer = new Timer();
		}
		HcLog.D(TAG + " it is onCreate!");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		HcLog.D(TAG + " #onStartCommand intent = "+intent);
		if (intent != null && intent.getExtras() != null) {
			Bundle bundle = intent.getExtras();
			int signFlag = bundle.getInt("signFlag", -1);
			if (signFlag == HcUtil.SIGNIN_REQ) {
				String signDate = SettingHelper.getSigninTime(this);
				if (!TextUtils.isEmpty(signDate)) {
					if (signDate.equals(HcUtil.getDate(HcUtil.FORMAT_YEAR_MONTH_DAY, System.currentTimeMillis()))) {
						// 不需要再次签到了
						stopSelf();
						return START_STICKY;
					}
				}
				
				if (mSignInTask == null) {
					mSignInTask = new SignTask(signFlag);
					mTimer.schedule(mSignInTask, 0, 30 * 1000);
				}
				
			} else if (signFlag == HcUtil.SIGNOUT_REQ) {
				if (mSignOutTask == null) {
					mSignOutTask = new SignTask(signFlag);
					mTimer.schedule(mSignInTask, 0, 30 * 1000);
				}
			} else {
				stopSelf();
			}
		} else {
			stopSelf();
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	private class SignTask extends TimerTask {

		private final int mFlag;
		
		public SignTask(int flag) {
			mFlag = flag;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (mFlag == HcUtil.SIGNIN_REQ) {
				String signDate = SettingHelper.getSigninTime(getApplicationContext());
				if (!TextUtils.isEmpty(signDate)) {
					if (signDate.equals(HcUtil.getDate(HcUtil.FORMAT_YEAR_MONTH_DAY, System.currentTimeMillis()))) {
						// 不需要再次签到了
						cancel();
						if (mSignOutTask == null)
							stopSelf();
						return;
					}
				}
				if (canSign()) {
					uploadSignInfo(0);
				}
				
			} else if (mFlag == HcUtil.SIGNOUT_REQ) {
				String signDate = SettingHelper.getSignoutTime(getApplicationContext());
				if (!TextUtils.isEmpty(signDate)) {
					if (signDate.equals(HcUtil.getDate(HcUtil.FORMAT_YEAR_MONTH_DAY, System.currentTimeMillis()))) {
						// 不需要再次签到了
						cancel();
						if (mSignInTask == null)
							stopSelf();
						return;
					}
				}
				if (canSign()) {
					uploadSignInfo(1);
				}
			} else {
				cancel();
				stopSelf();
			}
		}
		
	}
	
	private boolean canSign() {
		if (HcUtil.isNetWorkAvailable(getApplicationContext())) {
			String distance = com.android.hcframe.internalservice.sign.SignLoctionUtils.getDistance();
			if (!TextUtils.isEmpty(distance)) {
				double dis = Double.parseDouble(distance);
				if (dis < com.android.hcframe.internalservice.sign.SignCache.getInstance().getMaxDistance()) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void uploadSignInfo(int mSignFlag) {
		Loction loction = new Loction();
		loction.setmAddressLatitude(com.android.hcframe.internalservice.sign.SignLoctionUtils.getLat() + "");
		loction.setmAddressLongitude(com.android.hcframe.internalservice.sign.SignLoctionUtils.getLng() + "");
		loction.setmSignFlag(mSignFlag + "");
		loction.setmSignType("0");
		HcHttpRequest.getRequest().sendSignCommand(loction.getmSignFlag(), loction.getmSignFlag(),
				loction.getmAddressLongitude(), loction.getmAddressLatitude(), com.android.hcframe.internalservice.sign.SignLoctionUtils.getAddress(), this);
	}
}
