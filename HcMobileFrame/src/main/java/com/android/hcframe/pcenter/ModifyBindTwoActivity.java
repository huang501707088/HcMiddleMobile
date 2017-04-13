package com.android.hcframe.pcenter;

import android.view.ViewGroup;

import com.android.hcframe.AbsActiviy;
import com.android.hcframe.R;
import com.android.hcframe.TopBarView;

public class ModifyBindTwoActivity extends AbsActiviy {

	private String oldcode;

	@Override
	protected void onInitView() {
		setContentView(R.layout.activity_base_center);
		mTopBarView = (TopBarView) findViewById(R.id.center_top_bar);
		mParent = (ViewGroup) findViewById(R.id.center_parent);
	}

	@Override
	protected void onInitData() {
		mTopBarView.setReturnBtnIcon(R.drawable.center_close);
		mTopBarView.setTitle(getString(R.string.modify_phone));
	}

	@Override
	protected void setPameter() {
		menuPage = "com.android.hcframe.pcenter.ModifyBindTwoMenuPage";
		oldcode = getIntent().getStringExtra("oldcode");
	}

	public String getOldcode() {
		return oldcode;
	}
}
