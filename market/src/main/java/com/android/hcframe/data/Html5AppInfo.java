/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-5-2 上午9:59:18
*/
package com.android.hcframe.data;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.android.hcframe.menu.HtmlActivity;


public class Html5AppInfo extends AppInfo {

	public Html5AppInfo() {
		super();
	}
	
	public Html5AppInfo(Html5AppInfo info) {
		super(info);
	}

	@Override
	public void startApp(Context context) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(context, HtmlActivity.class);
		intent.putExtra("title", mName);
		intent.putExtra("url", getAppUrl());
		intent.putExtra("mAppId", mId);
		context.startActivity(intent);
		if (context instanceof Activity)
			((Activity) context).overridePendingTransition(0, 0);
	}

}
