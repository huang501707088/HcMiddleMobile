/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @author zjbin
* @data 2016-11-14 下午2:21:39
*/
package com.android.hcframe.schedule;

import android.app.Activity;
import android.view.ViewGroup;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.menu.MenuPage;

public class SheduleMenuPage extends MenuPage {

	@Override
	public AbstractPage createPage(String appId, Activity context,
			ViewGroup parent) {
		// TODO Auto-generated method stub
			return new SheduleMenuHomePage(context, parent, appId);
	}

}
