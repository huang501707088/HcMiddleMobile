/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-5-25 上午10:59:18
*/
package com.android.hcframe.menu;

import android.app.Activity;
import android.content.Intent;
import android.view.ViewGroup;

import com.android.hcframe.HcLog;

public class MenuPageFactory {

	private static final String TAG = "MenuPageFactory";
	
	private MenuPage mMenuPage;
	
	private Class<?> mClass;
	
	public MenuPageFactory() {
		
	}
	
	public void initMenu(String className) {
		
		try {
			mClass = Class.forName(className);
			mMenuPage = (MenuPage) mClass.newInstance();
		} catch (ClassNotFoundException e) {
			// TODO: handle exception
			HcLog.D(TAG + " ClassNotFoundException e = "+e + " className = "+className);
		} catch (Exception e) {
			// TODO: handle exception
			HcLog.D(TAG + " Exception e = "+e);
		}
		
	}
	
	public void onCreate(String appId, Activity context, ViewGroup parent) {
		if (mMenuPage != null) {
			mMenuPage.onCreate(appId, context, parent);
		}
	}
	
	public void onResume() {
		if (mMenuPage != null) {
			mMenuPage.onResume();
		}
	}
	
	public void onPause() {
		if (mMenuPage != null) {
			mMenuPage.onPause();
		}
	}
	
	public void onDestory() {
		if (mMenuPage != null) {
			mMenuPage.onDestory();
			mMenuPage = null;
		}
		
		mClass = null;
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mMenuPage != null) {
			mMenuPage.onActivityResult(requestCode, resultCode, data);
		}
	}
}
