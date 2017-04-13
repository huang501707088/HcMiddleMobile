/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-5-4 下午2:55:52
*/
package com.android.hcframe.market;

import android.graphics.Bitmap;

public interface UpdateCallback {

	/**
	 * 显示原生应用下载的进度
	 * @author jrjin
	 * @time 2015-11-30 下午1:40:09
	 * @param appId 应用ID
	 * @param current 当前进度
	 * @param max 最大值 max = -1表示下载结束;max = Integer.MAX表示下载失败；其他值下载成功
	 */
	public void onProgress(String appId, int current, int max);
	
	/**
	 * 设置应用的图标
	 * @author jrjin
	 * @time 2015-11-30 下午1:40:12
	 * @param appId 应用ID
	 * @param icon 应用图标
	 */
	public void setBitmap(String appId, Bitmap icon);
	
	/**
	 * 数据有变化，通知更新
	 * @author jrjin
	 * @time 2015-11-30 下午1:42:08
	 */
	public void notifyDataChanged();
}
