/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-12 上午9:46:19
*/
package com.android.hcframe.container.data;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.android.hcframe.container.ContainerActivity;
import com.android.hcframe.menu.HtmlActivity;

/**
 * 应用视图中元素的数据信息，简单理解为View需要显示的内容信息
 * @author jrjin
 * @time 2015-11-12 上午9:46:29
 */
public class ElementInfo extends ViewInfo {

	private static final String TAG = "ElementInfo";
	
	/** 元素显示的值(可能是应用的属性) */
	private String mValue;	
	
	/** 元素点击事件 */
	private String mAction;
	
	/** 属性ID */
	private String mAttrId;
	
	/**
	 * value获取方式
	 * 0:不需要获取,直接显示value；
	 * 1：本地没有需要获取；
	 * 2：每次都要获取
	 */
	private int mRequesType;
	
	
	/** 应用视图实例类型 
	 * 1：单个html应用；
	 * 0：单个内置原生服务
	 * @see AppInfo
	 */
	private int mViewType; // 这里的类型暂和AppInfo一致，估计以后需要修改
	
	private String mAppId; // 应用的ID，和AppInfo一致
	
	private String mAppName; // 应用名，和AppInfo一致

	@Override
	public void setViewAction(String action) {
		// TODO Auto-generated method stub
		mAction = action;
	}

	@Override
	public String getViewAction() {
		// TODO Auto-generated method stub
		return mAction;
	}

	@Override
	public void setElementValue(String value) {
		// TODO Auto-generated method stub
		mValue = value;
	}

	@Override
	public String getElementValue() {
		// TODO Auto-generated method stub
		return mValue;
	}

	@Override
	public void setRequestType(int request) {
		// TODO Auto-generated method stub
		mRequesType = request;
	}

	@Override
	public int getRequestType() {
		// TODO Auto-generated method stub
		return mRequesType;
	}

	@Override
	public void setAttrId(String attrId) {
		// TODO Auto-generated method stub
		mAttrId = attrId;
	}

	@Override
	public String getAttrId() {
		// TODO Auto-generated method stub
		return mAttrId;
	}
	
	@Override
	public void setAppId(String appId) {
		// TODO Auto-generated method stub
		mAppId = appId;
	}
	
	@Override
	public void setAppName(String appName) {
		// TODO Auto-generated method stub
		mAppName = appName;
	}
	
	@Override
	public void setViewType(int type) {
		// TODO Auto-generated method stub
		mViewType = type;
	}
	
	@Override
	public void onClick(Context context, int type) {
		// TODO Auto-generated method stub
		if (!TextUtils.isEmpty(mAction)) {
			switch (mViewType) {
			case VIEW_TYPE_HTML:
				startHtmlActivity(context, type);
				break;
			case VIEW_TYPE_SINGLE:
				startNativeActivity(context, type);
				break;

			default:
				break;
			}
		}
	}
	
	@SuppressWarnings("需要测试")
	private void startHtmlActivity(Context context, int type) {
		/**
		 * @author jinjr
		 * @date 2016-3-25 11:03
		 * 因权限管理,都调用ContainerActivity
		Intent intent = new Intent();
		intent.setClass(context, HtmlActivity.class);
		intent.putExtra("title", mValue);
		intent.putExtra("url", mAction);
		intent.putExtra("mAppId", mAppId);
		context.startActivity(intent);
		if (context instanceof Activity) {
			((Activity)context).overridePendingTransition(0, 0);
		}
		*/
		Intent intent = new Intent();
		intent.setClass(context, ContainerActivity.class);
		intent.putExtra("appId", mAppId);
		intent.putExtra("className", "com.android.hcframe.menu.WebMenuPage");
		intent.putExtra("appName", type == CLICK_TYPE_TEXT ? mValue : mAppName);
		intent.putExtra("menu", false);
		intent.putExtra("url", mAction);

		context.startActivity(intent);
		if (context instanceof Activity) {
			((Activity)context).overridePendingTransition(0, 0);
		}

	}
	
	@SuppressWarnings("需要测试")
	private void startNativeActivity(Context context, int type) {
		Intent intent = new Intent(context, ContainerActivity.class);
		intent.putExtra("appId", mAppId);
		intent.putExtra("className", mAction);
		intent.putExtra("appName", type == CLICK_TYPE_TEXT ? mValue : mAppName);
		intent.putExtra("menu", false);
		context.startActivity(intent);
		if (context instanceof Activity) {
			((Activity)context).overridePendingTransition(0, 0);
		}
	}

	@Override
	public String getAppId() {
		return mAppId;
	}

	@Override
	public String getAppName() {
		return mAppName;
	}
}
