/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-12 下午2:35:21
*/
package com.android.hcframe.container;

import com.android.hcframe.container.data.AppViewInfo;
import com.android.hcframe.container.data.ViewInfo;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * 应用视图工厂,只负责创建应用视图
 * @author jrjin
 * @time 2015-11-12 下午2:35:29
 */
public interface AppViewFactory {

	/**
	 * 
	 * @author jrjin
	 * @time 2015-11-17 上午10:59:30
	 * @param context
	 * @param parent 应用视图的容器视图
	 * @param info {@link AppViewInfo}
	 * @return 应用视图
	 */
	public View createAppView(Context context, ViewGroup parent, ViewInfo info);
}
