/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-7-22 上午10:02:45
*/
package com.android.hcframe.container;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.R;
import com.android.hcframe.TopBarView;
import com.android.hcframe.menu.MenuPageFactory;
import com.android.hcframe.push.HcAppState;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.FrameLayout;

public class BaseContainerActivity extends HcBaseActivity {

	private static final String TAG = "BaseContainerActivity";

	private TopBarView mTopBarView;
	
	private FrameLayout mParent;
	
	private String mClassName;
	
	private String mAppId;
	
//	private ContainerPageFactory mPageFactory;
	
	private String mAppName;
	
	private MenuPageFactory mPageFactory;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (null != intent) {
			Bundle bundle = intent.getExtras();
			if (null != bundle) {
				mClassName = bundle.getString("className");
				mAppId = bundle.getString("appId");
				mAppName = bundle.getString("appName", "");
				if (TextUtils.isEmpty(mClassName) || TextUtils.isEmpty(mAppId)) {
					finishActivity();
					return;
				} 
			} else {
				finishActivity();
				return;
			}
		} else {
			finishActivity();
			return;
		}
		getWindow().setFormat(PixelFormat.RGBA_8888);
		setContentView(R.layout.activity_base_container);
		mTopBarView = (TopBarView) findViewById(R.id.container_top_bar);
		mParent = (FrameLayout) findViewById(R.id.container_parent);
		mTopBarView.setTitle(mAppName);
		mPageFactory = new MenuPageFactory();
		mPageFactory.initMenu(mClassName);
//		mPageFactory = new ContainerPageFactory(mClassName);
		mPageFactory.onCreate(mAppId, this, mParent);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mPageFactory.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mPageFactory.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mPageFactory.onDestory();
	}
	
	private void finishActivity() {
		HcAppState.getInstance().removeActivity(this);
		finish();
	}
}
