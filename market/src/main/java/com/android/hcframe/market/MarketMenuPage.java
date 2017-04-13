/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-5-26 下午2:13:15
*/
package com.android.hcframe.market;

import android.app.Activity;
import android.view.ViewGroup;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.menu.MenuPage;

public class MarketMenuPage extends MenuPage {

	@Override
	public AbstractPage createPage(String appId, Activity context,
			ViewGroup parent) {
		// TODO Auto-generated method stub
		AbstractPage page = null;
		boolean usePage = context.getResources().getBoolean(R.bool.market_use_page);
		if (usePage) {
			page = new MarketViewPortPager(context, parent);
//			page = new MarketViewLandPager(context, parent);
		} else {
			page = new MarketViewPort(context, parent);
		}
		return page;
	}

}
