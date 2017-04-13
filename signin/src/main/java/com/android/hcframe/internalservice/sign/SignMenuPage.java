/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2016-1-4 下午2:21:39
*/
package com.android.hcframe.internalservice.sign;

import android.app.Activity;
import android.view.ViewGroup;
import com.android.hcframe.AbstractPage;
import com.android.hcframe.menu.MenuPage;

public class SignMenuPage extends MenuPage {

	@Override
	public AbstractPage createPage(String appId, Activity context,
			ViewGroup parent) {
		// TODO Auto-generated method stub
			return new SignMenuHomePage(context, parent, appId);
	}

}
