/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-5-22 上午10:54:17
*/
package com.android.hcframe.menu;

import android.app.Activity;
import android.view.ViewGroup;

import com.android.hcframe.AbstractPage;

public interface IMenuPageFactory {

	/**
	 * 根据配置文件中的应用ID创建不同的页面
	 * @author jrjin
	 * @time 2015-5-22 上午10:58:53
	 * @since 1.0.0
	 * @param appId
	 * @return 菜单的页面
	 */
	public AbstractPage createPage(String appId, Activity context, ViewGroup parent);
}
