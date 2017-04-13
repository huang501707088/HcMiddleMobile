/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-6-23 上午11:11:52
*/
package com.android.hcframe.service;

import java.util.List;

import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ServiceBroadcast extends BroadcastReceiver {

	private static final String TAG = "ServiceBroadcast";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		HcLog.D(TAG + " it is onReceive action ="+action);
		Intent service = new Intent();
		service.setAction(context.getPackageName() + ".HcService");
		service.setPackage(context.getPackageName());

		Intent im = new Intent(HcUtil.IM_APP_STARTED_ACTION);
		im.setPackage(context.getPackageName());

		if (action.equals(context.getPackageName() + ".ServiceBroadcast")) {
			// 判断service是否存活
			ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningServiceInfo> serviceList = manager.getRunningServices(100);
			int size = serviceList.size();
			boolean isImRunning = Build.VERSION.SDK_INT >= 21;
			if (size > 0) {
				boolean isRunning = false;
				RunningServiceInfo info = null;
				for (int i = 0; i < size; i++) {
					info = serviceList.get(i);
	                if (info.service.getPackageName().equals(context.getPackageName())) {
	            	   HcLog.D(TAG + " service package = "+info.service.getPackageName() + " app package = "+context.getPackageName());
	                   if (info.service.getClassName().equals(HcUtil.SERVICE_CLASS) == true) {
						   isRunning = true;
					   } else if (info.service.getClassName().equals("com.android.hcframe.im.IMService")) {
						   isImRunning = true;
					   }
						if (isImRunning && isRunning)
	                   		break;
	                }
		        }
				if (!isRunning) {
//					context.startService(new Intent(context.getPackageName() + ".HcService"));
					context.startService(service);
				} else if (!isImRunning) {
					context.startService(im);
				}
			} else { // 说明服务没有启动
//				context.startService(new Intent(context.getPackageName() + ".HcService"));
				context.startService(service);
				if (!isImRunning) { // 说明SDK < 21
					context.startService(im);
				}
			}
		} else if (action.equals("android.intent.action.BOOT_COMPLETED")) {
//			context.startService(new Intent(context.getPackageName() + ".HcService"));
			context.startService(service);
			if (Build.VERSION.SDK_INT < 21) { // 说明SDK < 21
				context.startService(im);
			} else {
				Intent job = new Intent(HcUtil.IM_APP_STARTED_ACTION);
				intent.setPackage(context.getPackageName());
				context.sendBroadcast(job);
			}

		}
	}

}
