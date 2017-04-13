package com.android.hcframe.modifypwd;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcObserver;
import com.android.hcframe.HcSubject;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.TopBarView;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.http.ResponseCodeInfo;
import com.android.hcframe.push.HcAppState;
import com.android.hcframe.sql.SettingHelper;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class ModifyAct extends HcBaseActivity implements OnClickListener,
		HcObserver {

	private EditText everpwd_et;

	private EditText nowpwd_et;

	private EditText confirmpwd_et;

	private Button modifypwd_btn;

	private ModifyManager mManager;

	// private TextView settings_back_pwd;

	private TopBarView mTopBarView;

	private ImageView everpwd_iv;

	private ImageView nowpwd_iv;

	private ImageView confirmpwd_iv;
	
	private boolean mOldPw = false;
	
	private boolean mNewPw = false;
	
	private boolean mConfirmPw = false;
	

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_pwd);

		everpwd_et = (EditText) findViewById(R.id.everpwd_et);
		nowpwd_et = (EditText) findViewById(R.id.nowpwd_et);
		confirmpwd_et = (EditText) findViewById(R.id.confirmpwd_et);
		modifypwd_btn = (Button) findViewById(R.id.modifypwd_btn);
		// settings_back_pwd=(TextView) findViewById(R.id.settings_back_pwd);
		everpwd_iv = (ImageView) findViewById(R.id.everpwd_iv);
		nowpwd_iv = (ImageView) findViewById(R.id.nowpwd_iv);
		confirmpwd_iv = (ImageView) findViewById(R.id.confirmpwd_iv);

		initListener();

		mTopBarView = (TopBarView) findViewById(R.id.pwd_top_bar);
		mTopBarView.setTitle(getString(R.string.modify_pw_str));
		mTopBarView.setSettingsVisiable(View.GONE);
		mTopBarView.setReturnBtnVisiable(View.VISIBLE);
		mTopBarView.setReturnViewListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				HcAppState.getInstance().removeActivity(ModifyAct.this);
				finish();
			}
		});
		// settings_back_pwd.setOnClickListener(this);
		mManager = new ModifyManager();
		
		setEnabled();
	}

	private void initListener() {
		everpwd_et.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
//				String ever = everpwd_et.getText().toString();
//				if (ever.length() > 0) {
//					everpwd_iv.setVisibility(View.VISIBLE);
//					mOldPw = true;
//				} else {
//					everpwd_iv.setVisibility(View.INVISIBLE);
//					mOldPw = false;
//				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				setText(s, everpwd_et, everpwd_iv);
			}
		});
		nowpwd_et.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {

//				String ever = nowpwd_et.getText().toString();
//				if (ever.length() > 0) {
//					nowpwd_iv.setVisibility(View.VISIBLE);
//				} else {
//					nowpwd_iv.setVisibility(View.INVISIBLE);
//				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				setText(s, nowpwd_et, nowpwd_iv);
			}
		});
		confirmpwd_et.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
//				String ever = confirmpwd_et.getText().toString();
//				if (ever.length() > 0) {
//					confirmpwd_iv.setVisibility(View.VISIBLE);
//				} else {
//					confirmpwd_iv.setVisibility(View.INVISIBLE);
//				}
				
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				setText(s, confirmpwd_et, confirmpwd_iv);
			}
		});
		modifypwd_btn.setOnClickListener(this);
		everpwd_iv.setOnClickListener(this);
		nowpwd_iv.setOnClickListener(this);
		confirmpwd_iv.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();
		if (id == R.id.modifypwd_btn) {
			String everpwd = everpwd_et.getText().toString();
			String nowpwd = nowpwd_et.getText().toString();
			String confirmpwd = confirmpwd_et.getText().toString();

			if (HcUtil.isEmpty(everpwd) || HcUtil.isEmpty(nowpwd)
					|| HcUtil.isEmpty(confirmpwd)) {
				HcUtil.showToast(this, R.string.pwdnull);
				return;
			}

			if (!nowpwd.equals(confirmpwd)) {
				HcUtil.showToast(this, R.string.pwd_not_equal);
				return;
			}

			if (!HcUtil.isNetWorkError(this)) {
				if (HcUtil.isEmpty(SettingHelper.getAccount(this))) {
					HcUtil.showToast(this, R.string.pleaselogin);
					return;
				} else {
					HcDialog.showProgressDialog(this, R.string.dialog_title_post_data);
					mManager.modifypwd(SettingHelper.getAccount(this), everpwd,
							nowpwd);
				}
			} 
		} else if (id == R.id.everpwd_iv) {
			everpwd_et.setText("");
		} else if (id == R.id.nowpwd_iv) {
			nowpwd_et.setText("");
		} else if (id == R.id.confirmpwd_iv) {
			confirmpwd_et.setText("");
		}

	}

	@Override
	protected void onResume() {
		mManager.addObserver(this);
		super.onResume();
	}

	@Override
	protected void onPause() {
		mManager.removeObserver(this);
		super.onPause();
	}

	@Override
	public void updateData(HcSubject subject, Object data,
			RequestCategory request, ResponseCategory response) {
		if (subject != null && subject instanceof ModifyManager) {
			HcDialog.deleteProgressDialog();
			switch (response) {
			case SESSION_TIMEOUT:
			case NETWORK_ERROR:
				HcUtil.toastTimeOut(this);
				break;
			case DATA_ERROR:
				HcUtil.toastDataError(this);
				break;
			case SYSTEM_ERROR:
				HcUtil.toastSystemError(this, data);
				break;
			case SUCCESS:
				setResult(1, getIntent());
				HcAppState.getInstance().removeActivity(ModifyAct.this);
				finish();
				break;
			case REQUEST_FAILED:
				ResponseCodeInfo info = (ResponseCodeInfo) data;
				/**
				 * @author zhujb
				 * @date 2016-04-13 下午4:19:07
				 */
				if (info.getCode() == HcHttpRequest.REQUEST_ACCOUT_EXCLUDED ||
						info.getCode() == HcHttpRequest.REQUEST_TOKEN_FAILED) {
					HcUtil.reLogining(info.getBodyData(), this, info.getMsg());
				}else {
					HcUtil.showToast(this, info.getMsg());
				}
				break;
			default:
				break;

			}
		}
	}
	
	private void setText(Editable s, EditText et, ImageView clear) {
		boolean lable = false;
		if (s != null) {
			int length = s.length();
			if (length > 0) {
				clear.setVisibility(View.VISIBLE);
				if (length < 6) {
					lable = false;
				} else {
					lable = true;
					
					if (length > 20) {
						et.setText(s.subSequence(0, length - 1));
						et.setSelection(20);
					}
				}

			} else {
				lable = false;
				clear.setVisibility(View.INVISIBLE);
			}
			 
		} else {
			lable = false;
			clear.setVisibility(View.INVISIBLE);
			et.setText("");
		}
		if (et == confirmpwd_et) {
			mConfirmPw = lable;
		} else if (et == everpwd_et) {
			mOldPw = lable;
		} else if (et == nowpwd_et) {
			mNewPw = lable;
		}
		setEnabled();
	}
	
	private void setEnabled() {
//		HcLog.D(" ModifyAct setEnabled! old = "+mOldPw + " new = "+mNewPw + " confirm = "+mConfirmPw);
		if (!mOldPw || !mNewPw || !mConfirmPw) {
			modifypwd_btn.setEnabled(false);
		} else {
			
			modifypwd_btn.setEnabled(true);
		}
	}
}
