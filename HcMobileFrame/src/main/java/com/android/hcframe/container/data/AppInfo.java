/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-16 下午2:54:08
*/
package com.android.hcframe.container.data;

import java.util.ArrayList;
import java.util.List;

import com.android.hcframe.HcLog;
import com.android.hcframe.container.ContainerActivity;
import com.android.hcframe.menu.HtmlActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class AppInfo extends ViewInfo {

	private static final String TAG = "AppInfo";
	
	/** 应用视图实例类型 
	 * 1：单个html应用；
	 * 0：单个内置原生服务
	 */
	private int mViewType;
	
	/** 进入应用 
	 * viewType = 1：应用主页url
	 * viewType = 0：应用className
	 */
	private String mViewAction;
	
	private String mAppId;
	
	private String mAppName;
	
	/** 应用视图元素列表 {@link ElementInfo}*/
	private List<ViewInfo> mInfos = new ArrayList<ViewInfo>();
	
	
	@Override
	public void addView(ViewInfo info) {
		// TODO Auto-generated method stub
		if (!mInfos.contains(info) && info instanceof ElementInfo) {
			info.setAppId(mAppId);
			info.setAppName(mAppName);
			info.setViewType(mViewType);
			mInfos.add(info);
		}
			
	}

	@Override
	public ViewInfo getViewInfo(int index) {
		// TODO Auto-generated method stub
		if (index < 0 || index >= mInfos.size()) return null;
		return mInfos.get(index);
	}

	@Override
	public List<ViewInfo> getViewInfos() {
		// TODO Auto-generated method stub
//		HcLog.D(TAG + " getViewInfos ele size = "+mInfos.size());
		return mInfos;
	}
	
	@Override
	public void setViewType(int type) {
		// TODO Auto-generated method stub
		mViewType = type;
	}

	@Override
	public int getViewType() {
		// TODO Auto-generated method stub
		return mViewType;
	}

	@Override
	public void setViewAction(String action) {
		// TODO Auto-generated method stub
		mViewAction = action;
	}

	@Override
	public String getViewAction() {
		// TODO Auto-generated method stub
		return mViewAction;
	}

	@Override
	public void onClick(Context context, int type) {
		// TODO Auto-generated method stub
		if (!TextUtils.isEmpty(mViewAction)) {
			switch (mViewType) {
			case VIEW_TYPE_HTML:
				startHtmlActivity(context);
				break;
			case VIEW_TYPE_SINGLE:
				startNativeActivity(context);
				break;

			default:
				break;
			}
		}
	}
	
	@Override
	public void setAppId(String appId) {
		// TODO Auto-generated method stub
		mAppId = appId;
	}

	@Override
	public String getAppId() {
		// TODO Auto-generated method stub
		return mAppId;
	}

	@Override
	public void setAppName(String appName) {
		// TODO Auto-generated method stub
		mAppName = appName;
	}

	@Override
	public String getAppName() {
		// TODO Auto-generated method stub
		return mAppName;
	}

	private void startHtmlActivity(Context context) {
		/**
		 * @author jinjr
		 * @date 2016-3-25 11:03
		 * 因权限管理,都调用ContainerActivity
		Intent intent = new Intent();
		intent.setClass(context, HtmlActivity.class);
		intent.putExtra("title", mAppName);
		intent.putExtra("url", mViewAction);
		intent.putExtra("mAppId", mAppId);
		context.startActivity(intent);
		if (context instanceof Activity) {
			((Activity)context).overridePendingTransition(0, 0);
		}
		 */
		Intent intent = new Intent();
		intent.setClass(context, ContainerActivity.class);
		intent.putExtra("appId", mAppId);
		intent.putExtra("appName", mAppName);
		intent.putExtra("url", mViewAction);
		intent.putExtra("className", "com.android.hcframe.menu.WebMenuPage");
		intent.putExtra("menu", false);
		context.startActivity(intent);
		if (context instanceof Activity) {
			((Activity)context).overridePendingTransition(0, 0);
		}
	}
	
	private void startNativeActivity(Context context) {
		Intent intent = new Intent(context, ContainerActivity.class);
		intent.putExtra("appId", mAppId);
		intent.putExtra("className", mViewAction);
		intent.putExtra("appName", mAppName);
		intent.putExtra("menu", false);
		context.startActivity(intent);
		if (context instanceof Activity) {
			((Activity)context).overridePendingTransition(0, 0);
		}
	}
}
