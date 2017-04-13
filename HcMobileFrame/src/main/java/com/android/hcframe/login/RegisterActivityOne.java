package com.android.hcframe.login;

import android.view.ViewGroup;

import com.android.hcframe.AbsActiviy;
import com.android.hcframe.R;
import com.android.hcframe.TopBarView;

public class RegisterActivityOne extends AbsActiviy {

	@Override
	protected void onInitView() {
		setContentView(R.layout.activity_base_center);
		mTopBarView = (TopBarView) findViewById(R.id.center_top_bar);
		mParent = (ViewGroup) findViewById(R.id.center_parent);
	}

	@Override
	protected void onInitData() {
		mTopBarView.setReturnBtnIcon(R.drawable.center_close);
		mTopBarView.setTitle(getString(R.string.register_new_user));
	}

	@Override
	protected void setPameter() {
		menuPage = "com.android.hcframe.login.RegisterOneMenuPage";
	}

}
