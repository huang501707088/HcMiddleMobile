/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-6-23 上午9:38:34
*/
package com.android.hcframe.service;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.data.HcNewsData;
import com.android.hcframe.doc.data.DocCacheData;
import com.android.hcframe.menu.MenuBaseActivity;
import com.android.hcframe.push.HcPushManager;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.util.TimeUtils;

public class HcService extends Service {

	private static final String TAG = "HcService";
	
	private static final int TIME_INTERVAL = 20 * 1000; // 20s存一次
	
	private Timer mTimer;

	private boolean mNetWorkAvailabled = false;
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			HcLog.D(TAG + " action = "+intent.getAction() + " network available = "+HcUtil.isNetWorkAvailable(context));
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				HcUtil.isIntranet(context);
				if (!mNetWorkAvailabled && HcUtil.isNetWorkAvailable(context)) {
					mNetWorkAvailabled = true;
					// 说明从无网络---->有网络
					Intent intent2 = new Intent(/*this, DownloadService.class*/);
					intent2.setAction(getPackageName() + ".DownloadService");
					intent2.setPackage(getPackageName());
					startService(intent2);
//					HcPushManager.getInstance().sendCheckDevice(context);
				} else {
					// 说明从有网络---->无网络
					mNetWorkAvailabled = false;
				}
			}
			String networkAction = context.getPackageName() + MenuBaseActivity.NETWORK_ACTION;
			Intent networkIntent = new Intent(networkAction);
			networkIntent.setPackage(context.getPackageName());
//			context.sendBroadcast(new Intent(networkAction));
			context.sendBroadcast(networkIntent);
		}
	};
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		HcLog.D(TAG + " it is onBind! intent = "+intent);
		return new LocalBinder();
	}

	public class LocalBinder extends Binder {
		
		public HcService getLocalService() {
			return HcService.this;
		}
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		HcLog.D(TAG + " it is onCreate!" + " timer = "+mTimer);
		super.onCreate();
//		if (null == mTimer) {
//			mTimer = new Timer();
//		}
//		IntentFilter filter = new IntentFilter();
//		filter.addAction(Intent.ACTION_TIME_TICK);
//		registerReceiver(mReceiver, filter);
		mNetWorkAvailabled = HcUtil.isNetWorkAvailable(getApplicationContext());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stonStartCommandub
		HcLog.D(TAG + " it is onStartCommand! intent = "+intent + " flags = "+flags + " startId = "+startId);
//		stopSelf(startId);
		if (null == mTimer) {
			mTimer = new Timer();
//			mTimer.schedule(new RequestTask(), 1000, 30 * 60 * 1000);
//			mTimer.schedule(new AliveTask(), 1000, 60 * 1000);
			/** 监听网络切换 */
			IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
			registerReceiver(mReceiver, filter);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		HcLog.D(TAG + " it is onDestory!");
		if (null != mTimer) {
			mTimer.cancel();
			mTimer = null;
		}
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
		}
		super.onDestroy();
//		Process.killProcess(Process.myPid());
	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		HcLog.D(TAG + " it is onLowMemory!");
		super.onLowMemory();
	}
	
	private class TimeHandler extends Handler {

		private static final int MESSAGE_TIMER = 0;
		
		private void resume() {
			if (!hasMessages(MESSAGE_TIMER)) {
				sendEmptyMessage(MESSAGE_TIMER);
			}
				
		}
		
		private void pause() {
			removeMessages(MESSAGE_TIMER);
		}
		
		
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
		}

	} 
	
	/**
	 * 访问服务端的数据是否有变更
	 * @author jrjin
	 * @time 2015-7-24 上午11:06:02
	 */
	private class RequestTask extends TimerTask {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			HcNewsData.getInstance().refreshColumns();
			DocCacheData.getInstance().refreshDocColumns();
		}
		
	}
	
	/**
	 * 判断服务是否存活
	 * @author jrjin
	 * @time 2015-7-24 上午11:07:30
	 */
	private class AliveTask extends TimerTask {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
