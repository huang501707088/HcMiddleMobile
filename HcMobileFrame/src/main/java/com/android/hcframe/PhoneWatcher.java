/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2014-9-19 下午4:20:20
*/
package com.android.hcframe;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class PhoneWatcher implements TextWatcher {

	private EditText mEditText;
	
	private boolean isPhoneNumber = false;
	
	public PhoneWatcher(EditText text) {
		mEditText = text;
		mEditText.addTextChangedListener(this);
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
		if (null == s || mEditText == null) return;
		int length = s.length();
		if (length >= 8 && length <= 12)
			isPhoneNumber = true;
		else if (length < 8) {
			isPhoneNumber = false;
		} else {
			isPhoneNumber = false;
			mEditText.setText(s.subSequence(0, length - 1));
			mEditText.setSelection(12);
		}
	}

	public boolean isPhoneNumber() {
		return isPhoneNumber;
	}
}
