/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-5-26 下午2:07:12
*/
package com.android.hcframe.menu;

import android.app.Activity;
import android.view.ViewGroup;

import com.android.hcframe.AbstractPage;

public class WebMenuPage extends MenuPage {

	@Override
	public AbstractPage createPage(String appId, Activity context,
			ViewGroup parent) {
		// TODO Auto-generated method stub
		return new MenuWebPage(context, parent, appId);
	}

}
