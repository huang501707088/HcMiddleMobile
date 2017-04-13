/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-5-22 上午11:46:00
*/
package com.android.hcframe.menu;

import android.app.Activity;
import android.content.Intent;
import android.view.ViewGroup;

import com.android.hcframe.AbstractPage;

public abstract class MenuPage implements IMenuPageFactory {

	protected AbstractPage mPage;

	public MenuPage() {}
	
	/**
	 * 创建页面，初始化数据
	 * @author jrjin
	 * @time 2015-5-25 下午2:33:49
	 * @param appId
	 * @param context
	 * @param parent
	 */
	public void onCreate(String appId, Activity context, ViewGroup parent) {
		mPage = createPage(appId, context, parent);
		/**
		 * @author jrjin
		 * @date 2016-3-21 14:52
		 * 把这个处理放到onResume里面了
		mPage.changePages();
		*/
	}
	
	public void setParameters(Object ... parameters) {
		mPage.setParameters(parameters);
	}
	
	/**
	 * 更新页面或者数据
	 * @author jrjin
	 * @time 2015-5-25 下午2:34:12
	 */
	public void onResume() {
		/**
		 * 为了控制权限更改
		 * @author jrjin
		 * @date 2016-3-21 14:52
		 */
		if (mPage.getContentView() == null) {
			mPage.changePages();
		}
		mPage.onResume();
	}
	/**
	 * 
	 * @author jrjin
	 * @time 2015-5-25 下午2:34:56
	 */
	public void onPause() {
		mPage.onPause();
	}
	/**
	 * 删除页面
	 * @author jrjin
	 * @time 2015-5-25 下午2:34:34
	 */
	public void onDestory() {
		mPage.onDestory();
		mPage = null;
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		mPage.onActivityResult(requestCode, resultCode, data);
	}
}
