/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-7-22 上午11:29:12
*/
package com.android.hcframe.container;

import com.android.hcframe.HcLog;

import android.app.Activity;
import android.view.ViewGroup;

public class ContainerPageFactory implements IActivity {

	private static final String TAG = "ContainerPageFactory";
	
	private Class<?> mClass;
	
	private PageFactory mPageFactory;
	
	public ContainerPageFactory(String className) {
		try {
			mClass = Class.forName(className);
			mPageFactory = (PageFactory) mClass.newInstance();
		} catch (ClassNotFoundException e) {
			// TODO: handle exception
			HcLog.D(TAG + " ClassNotFoundException e = "+e + " className = "+className);
		} catch (Exception e) {
			// TODO: handle exception
			HcLog.D(TAG + " Exception e = "+e);
		}
	}
	
	@Override
	public void onCreate(String appId, Activity context, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (mPageFactory != null) {
			mPageFactory.onCreate(appId, context, parent);
		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		if (mPageFactory != null) {
			mPageFactory.onResume();
		}
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		if (mPageFactory != null) {
			mPageFactory.onPause();
		}
	}

	@Override
	public void onDestory() {
		// TODO Auto-generated method stub
		if (mPageFactory != null) {
			mPageFactory.onDestory();
		}
	}

}
