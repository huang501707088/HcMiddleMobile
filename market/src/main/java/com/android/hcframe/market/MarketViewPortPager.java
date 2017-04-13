/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-5-29 上午10:25:53
*/
package com.android.hcframe.market;

import com.android.hcframe.data.HcAppData;
import com.android.hcframe.data.HcAppReceiver;
import com.android.hcframe.http.RequestCategory;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.ViewGroup;

public class MarketViewPortPager extends AppItemView {

	private HcAppReceiver mReceiver;

	public MarketViewPortPager(Activity context, ViewGroup group) {
		super(context, group,HcAppData.APP_CATEGORY_ALL, RequestCategory.APP_ALL);
		// TODO Auto-generated constructor stub
		mReceiver = new HcAppReceiver();
		IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filter.addDataScheme("package");
		context.registerReceiver(mReceiver, filter);
		mReceiver.setInstallListener(HcAppData.getInstance());
	}

	@Override
	public void onDestory() {
		if (mReceiver != null) {
			mReceiver.setInstallListener(null);
			mContext.unregisterReceiver(mReceiver);
			mReceiver = null;
		}
		super.onDestory();
	}
}
