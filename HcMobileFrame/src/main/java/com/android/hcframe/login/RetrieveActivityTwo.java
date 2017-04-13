package com.android.hcframe.login;

import android.view.ViewGroup;

import com.android.hcframe.AbsActiviy;
import com.android.hcframe.R;
import com.android.hcframe.TopBarView;

public class RetrieveActivityTwo extends AbsActiviy implements IRetrieveInfo {

	private String account;

	private String mobile;

	private String code;

	@Override
	protected void onInitView() {
		setContentView(R.layout.activity_base_center);
		mTopBarView = (TopBarView) findViewById(R.id.center_top_bar);
		mParent = (ViewGroup) findViewById(R.id.center_parent);
	}

	@Override
	protected void onInitData() {

		mTopBarView.setReturnBtnIcon(R.drawable.center_close);

		mTopBarView.setTitle(getString(R.string.retrieve_pwd));

	}

	@Override
	protected void setPameter() {
		menuPage = "com.android.hcframe.login.RetrieveTwoMenuPage";
		account = getIntent().getStringExtra("account");
		mobile = getIntent().getStringExtra("mobile");
		code = getIntent().getStringExtra("code");
	}

	@Override
	public String getAccount() {
		return account;
	}

	@Override
	public String getMobile() {
		return mobile;
	}

	@Override
	public String getCode() {
		return code;
	}

}
