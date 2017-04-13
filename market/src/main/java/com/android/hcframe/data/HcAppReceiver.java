/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-28 下午8:22:42
*/
package com.android.hcframe.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.hcframe.HcLog;

public class HcAppReceiver extends BroadcastReceiver {

	private static final String TAG = "HcAppReceiver";
	
	private AppInstall mInstall;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		final String action = intent.getAction();
		if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
                || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                || Intent.ACTION_PACKAGE_ADDED.equals(action)) {
			final String packageName = intent.getData().getSchemeSpecificPart();
            final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
            HcLog.D(" action = "+action + " packageName = "+packageName + " replacing = "+replacing);
            if (mInstall != null)
            	mInstall.onInstallCompleted(packageName, action);
		}
	}

	public void setInstallListener(AppInstall install) {
		mInstall = install;
	}
	
}
