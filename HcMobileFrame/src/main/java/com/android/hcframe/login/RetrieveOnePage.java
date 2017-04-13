package com.android.hcframe.login;

import java.util.Observable;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcObserver;
import com.android.hcframe.HcSubject;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.internalservice.signin.TimerUtils;
import com.android.hcframe.internalservice.signin.TimerUtils.ITimerListerner;
import com.android.hcframe.login.LoginManager.CodeType;

public class RetrieveOnePage extends AbstractPage implements
		OnFocusChangeListener, HcObserver, ITimerListerner {

	private EditText user_retrieve_one_et;

	private EditText icode_retrieve_one_et;

	private Button get_icode_btn;

	private Button retrieve_next_btn;

	private LinearLayout bind_tel_lly;

	private TextView bindtel_retrieve_one_tv;

	private boolean isFirst = true;

	private TextView pwd_error_tv;

	private boolean isError = false;

	private LoginManager mLoginMger = new LoginManager();

	private String mobile;

	private TimerUtils codeTimer = new TimerUtils();

	private int count = 0;

	private Handler hander;

	protected RetrieveOnePage(Activity context, ViewGroup group) {
		super(context, group);
	}

	@Override
	public void update(Observable observable, Object data) {

	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.get_icode_btn) {
			String account = user_retrieve_one_et.getText().toString();
			if (HcUtil.isEmpty(account)) {
				isError = true;
				pwd_error_tv.setVisibility(View.VISIBLE);
				pwd_error_tv.setText(R.string.user_not_null);
				return;
			}
			codeTimer.startTimer();
			get_icode_btn.setEnabled(false);
			HcDialog.showProgressDialog(mContext,
					R.string.dialog_title_get_data);
			mLoginMger.getCode(account, "", CodeType.RETRIEVE.ordinal() + "");
		} else if (id == R.id.retrieve_next_btn) {
			String account = user_retrieve_one_et.getText().toString();
			String code = icode_retrieve_one_et.getText().toString();
			if (HcUtil.isEmpty(mobile) || HcUtil.isEmpty(account)
					|| HcUtil.isEmpty(code)) {
				pwd_error_tv.setVisibility(View.VISIBLE);
				pwd_error_tv.setText(R.string.retrieve_one_error);
				isError = true;
				return;
			}
			HcDialog.showProgressDialog(mContext,
					R.string.pull_to_refresh_refreshing_label);
			mLoginMger.checkCode(account, mobile, CodeType.RETRIEVE.ordinal()
					+ "", code);
		}
	}

	@Override
	public void initialized() {
		if (isFirst) {
			isFirst = false;
			get_icode_btn.setOnClickListener(this);
			retrieve_next_btn.setOnClickListener(this);
			user_retrieve_one_et.setOnFocusChangeListener(this);
			icode_retrieve_one_et.setOnFocusChangeListener(this);
			hander = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					String data = (String) msg.obj;
					if ("0".equals(data)) {
						get_icode_btn.setEnabled(true);
						count = 0;
						get_icode_btn.setText(R.string.get_icode);
						codeTimer.stopTimer();
					} else {
						get_icode_btn.setText(String.format(
								mContext.getString(R.string.timer_code), data));
					}
				}

			};

			codeTimer.initTimer(0, 1000, this, hander);
		}
	}

	@Override
	public void setContentView() {
		if (mView == null) {
			mView = mInflater.inflate(R.layout.retrieve_one_page, null);

			user_retrieve_one_et = (EditText) mView
					.findViewById(R.id.user_retrieve_one_et);

			icode_retrieve_one_et = (EditText) mView
					.findViewById(R.id.icode_retrieve_one_et);

			get_icode_btn = (Button) mView.findViewById(R.id.get_icode_btn);

			retrieve_next_btn = (Button) mView
					.findViewById(R.id.retrieve_next_btn);

			bind_tel_lly = (LinearLayout) mView.findViewById(R.id.bind_tel_lly);

			pwd_error_tv = (TextView) mView.findViewById(R.id.pwd_error_tv);

			bindtel_retrieve_one_tv = (TextView) mView
					.findViewById(R.id.bindtel_retrieve_one_tv);
		}
	}

	@Override
	public void onFocusChange(View view, boolean isFocused) {
		int id = view.getId();
		if (id == R.id.user_retrieve_one_et) {
			if (isFocused) {
				if (isError) {
					isError = !isError;
					pwd_error_tv.setVisibility(View.GONE);
				}
			} else {
				if (!HcUtil.isEmpty(user_retrieve_one_et.getText().toString())) {
					bind_tel_lly.setVisibility(View.VISIBLE);
					bindtel_retrieve_one_tv.setText(String.format(
							mContext.getString(R.string.bind_tel),
							HcUtil.isEmpty(mobile) ? "" : mobile));
				}
			}
		} else if (id == R.id.icode_retrieve_one_et) {
			if (isFocused) {
				if (isError) {
					isError = !isError;
					pwd_error_tv.setVisibility(View.GONE);
				}
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mLoginMger.addObserver(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		mLoginMger.removeObserver(this);
	}

	@Override
	public void updateData(HcSubject subject, Object data,
			RequestCategory request, ResponseCategory response) {
		HcDialog.deleteProgressDialog();
		if (subject != null && subject instanceof LoginManager) {
			if (request == RequestCategory.GETCODE) {
				if (response == ResponseCategory.SUCCESS) {
					// 1,解析json
					String json = (String) data;
					JSONObject jsonobj;
					try {
						jsonobj = new JSONObject(json);
						JSONObject body = jsonobj.getJSONObject("body");
						if (HcUtil.hasValue(body, "mobile_phone")) {
							mobile = body.getString("mobile_phone");
						}
					} catch (Exception e) {
					}
					// 2,刷新界面
				} else if (response == ResponseCategory.SESSION_TIMEOUT) {
					HcUtil.toastTimeOut(mContext);
				} else if (response == ResponseCategory.DATA_ERROR) {
					HcUtil.toastDataError(mContext);
				}
			} else if (request == RequestCategory.CHECKCODE) {
				if (response == ResponseCategory.SUCCESS) {
					// 1,解析数据
					// 2,跳转界面
					String account = user_retrieve_one_et.getText().toString();
					String code = icode_retrieve_one_et.getText().toString();
					Intent intent = new Intent(mContext,
							RetrieveActivityTwo.class);
					intent.putExtra("account", account);
					intent.putExtra("mobile", mobile);
					intent.putExtra("code", code);
					mContext.startActivity(intent);
					mContext.finish();

				} else if (response == ResponseCategory.SESSION_TIMEOUT) {
					HcUtil.toastTimeOut(mContext);
				} else if (response == ResponseCategory.DATA_ERROR) {
					HcUtil.toastDataError(mContext);
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked", "hiding" })
	@Override
	public <String> String onReturnData() {
		++count;
		return (String) ((60 - count) + "");
	}

	@Override
	public void release() {
		super.release();
		codeTimer.stopTimer();
	}
}
