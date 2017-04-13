/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-14 下午4:13:59
*/
package com.android.hcframe.internalservice.about;


import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcUtil;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.login.LoginActivity;
import com.android.hcframe.sql.SettingHelper;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class FeedbackActivity extends HcBaseActivity implements TextWatcher, IHttpResponse {

	private EditText mEditText;
	
	private TextView mSubmit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feedback_layout);
		
		mEditText = (EditText) findViewById(R.id.feedback_text);
		mSubmit = (TextView) findViewById(R.id.feedback_ok_btn);
		mEditText.addTextChangedListener(this);
		mSubmit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (TextUtils.isEmpty(mEditText.getText().toString())) {
					HcUtil.showToast(FeedbackActivity.this, "反馈的内容不能为空！");
				} else if (TextUtils.isEmpty(SettingHelper.getAccount(FeedbackActivity.this))) {
					// 跳转到登录界面
					startLoginActivity();
				} else {
					HcHttpRequest.getRequest().sendFeedbackCommand(mEditText.getText().toString());
					HcUtil.showToast(FeedbackActivity.this, "提交成功！");
					finish();
				}
			}
		});
		if (savedInstanceState != null) {
			String content = savedInstanceState.getString("content", "");
			if (!TextUtils.isEmpty(content)) {
				mEditText.setText(content);
				mEditText.setSelection(content.length());
			}
		}
	}

	private void startLoginActivity() {
		Intent login = new Intent(this, LoginActivity.class);
		login.putExtra("loginout", false);
		startActivityForResult(login, HcUtil.REQUEST_CODE_LOGIN);
		overridePendingTransition(0, 0);
	}
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		if (s != null) {
			int length = s.length();
			if (length > 180) {
				mEditText.setText(s.subSequence(0, length - 1));
				mEditText.setSelection(180);
			}
		}
	}

	@Override
	public void notify(Object data, RequestCategory request,
			ResponseCategory category) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		if (!TextUtils.isEmpty(mEditText.getText().toString()))
			outState.putString("content", mEditText.getText().toString());
	}

}
