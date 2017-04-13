/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2014-4-3 下午3:56:55
*/
package com.android.hcframe.update;

public interface ResourceUtil {

	/**
	 * 获取下载对话框的样式
	 * @author jrjin
	 * @time 2014-9-12 上午9:21:39
	 * @return
	 */
	public int getDialogTheme();
	/**
	 * 获取下载对话框的布局ID
	 * @author jrjin
	 * @time 2014-9-12 上午9:22:07
	 * @return
	 */
	public int getDownloadDialogLayout();
	/**
	 * 对话框标题ID
	 * @author jrjin
	 * @time 2014-9-12 上午9:22:30
	 * @return
	 */
	public int getDialogTitle();
	/**
	 * 对话框更新内容ID
	 * @author jrjin
	 * @time 2014-9-12 上午9:22:55
	 * @return
	 */
	public int getDialogContent();
	/**
	 * 对话框确认按钮ID
	 * @author jrjin
	 * @time 2014-9-12 上午9:23:11
	 * @return
	 */
	public int getDialogOkBtn();
	/**
	 * 对话框取消按钮ID
	 * @author jrjin
	 * @time 2014-9-12 上午9:23:28
	 * @return
	 */
	public int getDialogCanelBtn();
	/**
	 * 获取在通知栏里的布局ID
	 * @author jrjin
	 * @time 2014-9-12 上午9:24:09
	 * @return
	 */
	public int getRemoteLayout();
	/**
	 * 应用图标ID
	 * @author jrjin
	 * @time 2014-9-12 上午9:24:43
	 * @return
	 */
	public int getAppIcon();
	/**
	 * 进度条ID
	 * @author jrjin
	 * @time 2014-9-12 上午9:24:59
	 * @return
	 */
	public int getRemoteProgress();
	/**
	 * 百分比ID
	 * @author jrjin
	 * @time 2014-9-12 上午9:25:12
	 * @return
	 */
	public int getRemoteText();

}
