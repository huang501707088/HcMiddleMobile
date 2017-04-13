/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-28 下午8:25:08
*/
package com.android.hcframe.data;

public interface AppInstall {

	/**
	 * 第三方原生应用安装完成
	 * @author jrjin
	 * @time 2015-11-29 下午3:45:53
	 * @param pkg 安装的应用包名
	 * @param action Intent.ACTION_PACKAGE_ADDED || Intent.ACTION_PACKAGE_CHANGED
	 */
	public void onInstallCompleted(String pkg, String action);
}
