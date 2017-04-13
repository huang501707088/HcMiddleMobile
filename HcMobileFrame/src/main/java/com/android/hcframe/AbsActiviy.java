package com.android.hcframe;

import com.android.hcframe.menu.MenuPageFactory;
import com.android.hcframe.push.HcAppState;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.ViewGroup;

public abstract class AbsActiviy extends Activity {

	protected ViewGroup mParent;

	protected TopBarView mTopBarView;

	protected MenuPageFactory mPageFactory;

	protected String menuPage;

	protected final String TAG = getClass().getSimpleName();

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		HcAppState.getInstance().addActivity(this);
		initView();
		initData();
	}

	protected void initView() {
		onInitView();
	}

	protected abstract void onInitView();

	protected void initData() {
		setPameter();
		mPageFactory = new MenuPageFactory();

		if (!HcUtil.isEmpty(menuPage)) {
			mPageFactory.initMenu(menuPage);
		}
		mPageFactory.onCreate("", this, mParent);
		onInitData();
	}

	protected abstract void onInitData();

	protected abstract void setPameter();

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mPageFactory.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mPageFactory.onPause();
	}

	@Override
	protected void onDestroy() {
		HcAppState.getInstance().removeActivity(this);
		super.onDestroy();
		mPageFactory.onDestory();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mPageFactory.onActivityResult(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}
}
