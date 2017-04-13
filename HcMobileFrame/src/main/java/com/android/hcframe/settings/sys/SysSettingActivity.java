package com.android.hcframe.settings.sys;

import android.view.ViewGroup;

import com.android.hcframe.AbsActiviy;
import com.android.hcframe.R;
import com.android.hcframe.TopBarView;

public class SysSettingActivity extends AbsActiviy {

	@Override
	protected void onInitView() {
		setContentView(R.layout.activity_base_center);
		mTopBarView = (TopBarView) findViewById(R.id.center_top_bar);
		mParent = (ViewGroup) findViewById(R.id.center_parent);
	}

	@Override
	protected void onInitData() {
		mTopBarView.setTitle(getString(R.string.syssettings));
	}

	@Override
	protected void setPameter() {
		menuPage = "com.android.hcframe.settings.sys.SysSettingMenuPage";
	}

}
