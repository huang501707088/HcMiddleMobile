/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-5-2 上午10:01:29
*/
package com.android.hcframe.data;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.sql.SettingHelper;

import java.io.File;
import java.util.List;


public class NativeAppInfo extends AppInfo {

	private static final String TAG = "NativeAppInfo";
	
	private static final String START_ACTION = ".ZJ.HC";
	
	public NativeAppInfo() {
		super();
	}
	
	public NativeAppInfo(NativeAppInfo info) {
		super(info);
	}

	@Override
	public void startApp(Context context) {
		// TODO Auto-generated method stub
		if (getAppState() == HcUtil.APP_NORMAL)
			downlaodApp(context);
		else {
			startNativeActivity(context, getAppPackage());

		}
	}

	@Override
	public void updateApp(Context context) {
		// TODO Auto-generated method stub
		String apps = SettingHelper.getDownloadAppInfo(context);
		HcLog.D(TAG + " updateApp apps = " + apps + " info version = " + mVersion);
		File apkFile = new File(HcApplication.getAppDownloadPath() + "/"
				+ "apk_" + mId + "_" + mVersion
				+ "_zjhc.apk");
		if (apkFile.exists() && apkFile.length() / 1024 > mSize - 5) {
			if (!TextUtils.isEmpty(apps)) {
				String[] infos = apps.split("&");
				for (String s : infos) {
					if (mId.equals(s.split(";")[0])
							&& mVersion.equals(s.split(";")[1])) {
						installApk(apkFile, context);
						return;
					}
				}
			}

			// 说明文件有问题，可能上次没有下载完成，需要重新下载
			apkFile.delete();
			downlaodApp(context);

		} else {
			downlaodApp(context);
		}

	}

	@Override
	public void installApk(File apk, Context context) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.fromFile(apk),
				"application/vnd.android.package-archive");
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
	}

	@Override
	public void downlaodApp(Context context) {
		File apk = new File(HcApplication.getAppDownloadPath() + "/" + "apk_" + mId + "_" + mVersion + "_zjhc.apk");
		HcLog.D(TAG + " apk exist = "+apk.exists() + " apk path = "+apk.getAbsolutePath() + " apk lenght = "+apk.length() / 1024 + " info size = "+mSize);
		if (apk.exists() && apk.length() / 1024 > mSize - 5) { // 安装
			installApk(apk,context);
		} else {
			if (!HcUtil.isNetWorkError(context)) {
				if (!TextUtils.isEmpty(getAppUrl())) {
					HcAppData.getInstance().onProgress(getAppId(), 0, Integer.valueOf(getAppSize()));
//					download.put(info.getAppId(), info);		
					HcHttpRequest.getRequest().downloadApp(getAppId(), getAppUrl(), HcAppData.getInstance());
				} else {
					HcUtil.showToast(context, "下载地址不能为空！");
				}
			}
			
		}
	}
	
	private void startNativeActivity(Context context, String pkg) {
		Intent intent = new Intent(pkg + START_ACTION);
		intent.addCategory("android.intent.category.DEFAULT");
		if (activityExist(intent, context)) {
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			intent.putExtra("user_name", SettingHelper.getAccount(context));
			intent.putExtra("app_id", getAppId());
			context.startActivity(intent);
			if (context instanceof Activity)
				((Activity) context).overridePendingTransition(0, 0);
		}
	}

	/**
	 * 判断需要启动的Activity是否存在
	 * 
	 * @author jrjin
	 * @time 2015-5-29 下午5:24:51
	 * @param intent
	 * @return
	 */
	private boolean activityExist(Intent intent, Context context) {
		/** 添加检测Activity的功能 */
		final PackageManager packageManager = context.getPackageManager();
		final List<ResolveInfo> apps = packageManager.queryIntentActivities(
				intent, 0);
		HcLog.D(TAG + " #activityExist apps size = " + apps.size());
		if (apps != null && apps.size() > 0) {
			apps.clear();
			return true;
		} else {// Activity不存在，可能是因为更改了包名
			// 重新下载
			downlaodApp(context);
		}
		return false;
	}
}
