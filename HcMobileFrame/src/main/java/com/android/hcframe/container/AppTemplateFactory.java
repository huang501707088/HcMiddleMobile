/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-17 上午10:41:08
*/
package com.android.hcframe.container;

import com.android.hcframe.HcLog;
import com.android.hcframe.container.data.ViewInfo;

import android.content.Context;
import android.view.View;

public class AppTemplateFactory {

	private static final String TAG = "AppTemplateFactory";
	
	private static AppTemplateFactory TEMPLATE = new AppTemplateFactory();
	
	public static AppTemplateFactory getInstance() {
		return TEMPLATE;
	}
	
	public AppViewLayout getAppView(String className) {
		try {
			Class<?> cl = Class.forName(className);
			return (AppViewLayout) cl.newInstance();
		} catch (ClassNotFoundException e) {
			// TODO: handle exception
			HcLog.D(TAG + " ClassNotFoundException e = "+e + " className = "+className);
		} catch (Exception e) {
			// TODO: handle exception
			HcLog.D(TAG + " Exception e = "+e);
		}
		return null;
	}
}
