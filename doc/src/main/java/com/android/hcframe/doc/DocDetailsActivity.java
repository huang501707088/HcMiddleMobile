package com.android.hcframe.doc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.TopBarView;
import com.android.hcframe.menu.MenuPageFactory;

public class DocDetailsActivity extends HcBaseActivity {
	private static final String TAG = "DocDetailsActivity";

	protected FrameLayout mParent;

	private TopBarView mTopBarView;

	private MenuPageFactory mPageFactory;

	private String data_id;

	private int data_flag;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_doc_details);
		initView();
		initData();
	}

	public void initView() {
		mTopBarView = (TopBarView) findViewById(R.id.details_top_bar);

		mParent = (FrameLayout) findViewById(R.id.details_parent);
	}

	public void initData() {
		Intent intent = getIntent();

		Bundle bundle = intent.getExtras();

		data_id = bundle.getString("data_id");

		data_flag = bundle.getInt("data_flag");
		mPageFactory = new MenuPageFactory();

		mPageFactory.initMenu("com.android.hcframe.doc.DetailsMenuPage");
		mPageFactory.onCreate("", this, mParent);

		mTopBarView.setTitle("资料详情");

	}

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
		super.onDestroy();
		mPageFactory.onDestory();
	}

	public String getData_id() {
		return data_id;
	}

	public int getData_flag() {
		return data_flag;
	}
}
