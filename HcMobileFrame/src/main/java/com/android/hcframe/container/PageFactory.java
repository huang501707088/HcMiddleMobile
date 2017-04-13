/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-7-22 上午11:18:26
*/
package com.android.hcframe.container;

import android.app.Activity;
import android.view.ViewGroup;

import com.android.hcframe.AbstractPage;

public abstract class PageFactory implements IActivity, IPageFactory {

	private static final String TAG = "PageFactory";
	
	protected AbstractPage mPage;
	
	protected String mAppId;
	
	public PageFactory() {}

	@Override
	public void onCreate(String appId, Activity context, ViewGroup parent) {
		// TODO Auto-generated method stub
		mPage = createPage(context, parent);
		mAppId = appId;
		mPage.changePages();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		mPage.onResume();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		mPage.onPause();
	}

	@Override
	public void onDestory() {
		// TODO Auto-generated method stub
		mPage.onDestory();
		mPage = null;
	}
	
	
}
