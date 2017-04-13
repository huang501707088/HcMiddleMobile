/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-4-29 上午9:45:10
*/
package com.android.hcframe.menu;

import android.os.Bundle;

import com.android.hcframe.R;

public class Menu3Activity extends MenuBaseActivity {

	private boolean mAnnual = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mHighMenuId = R.id.menu3;
		super.onCreate(savedInstanceState);
		if (mAnnual && "261".equals(mAppId)) {
			mTopBarView.setBackgroundColor(getResources().getColor(R.color.annual_topbar_bg));
		}
	}
}
