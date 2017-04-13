/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-14 下午5:56:44
*/
package com.android.hcframe.internalservice.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.TopBarView;
import com.android.hcframe.share.ShareActivity;

import java.io.File;

public class QRCodeActivity extends HcBaseActivity {

	private static final String TAG = "QRCodeActivity";
	
	private TopBarView mTopBarView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_qrcode_layout);
		mTopBarView = (TopBarView) findViewById(R.id.qrcode_top_bar);
		mTopBarView.setReturnBtnIcon(R.drawable.center_close);
		mTopBarView.setMenuListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				showShareActivity();
			}
		});
	}

	private void showShareActivity() {
		Intent intent = new Intent(this, ShareActivity.class);
		intent.putExtra(ShareActivity.SHARE_KEY_TYPE, ShareActivity.SHARE_IMAGE);
//		intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(((HcApplication) getApplication()).getQRCodePath()));
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(((HcApplication) getApplication()).getQRCodePath())));
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_from_bottom, 0);
	}
}
