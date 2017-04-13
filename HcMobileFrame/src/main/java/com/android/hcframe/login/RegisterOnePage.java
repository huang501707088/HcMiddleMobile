package com.android.hcframe.login;

import java.util.Observable;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.Countdown;
import com.android.hcframe.Countdown.HandlerCallback;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcObserver;
import com.android.hcframe.HcSubject;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.http.ResponseCodeInfo;
import com.android.hcframe.login.LoginManager.CodeType;
import com.android.hcframe.sql.SettingHelper;

public class RegisterOnePage extends AbstractPage implements HcObserver,
        OnFocusChangeListener, HandlerCallback {

    private EditText mobile_register_one_et;

    private EditText icode_register_one_et;

    private Button register_get_icode_btn;

    private Button register_next_btn;

    private TextView pwd_error_tv;

    private boolean isError = false;

    private LoginManager mLoginMger = new LoginManager();

    private String mobile;

    /**
     * 手机号码是否可用
     */
    private boolean mPhoneEnabled = false;

    /**
     * 验证码是否已经输入
     */
    private boolean mCodeEnabled = false;

    private Countdown mCountdown;

    protected RegisterOnePage(Activity context, ViewGroup group) {
        super(context, group);
    }

    @Override
    public void update(Observable observable, Object data) {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.register_get_icode_btn) {
            String mobile = mobile_register_one_et.getText().toString();
            if (HcUtil.isEmpty(mobile)) {
                isError = true;
                pwd_error_tv.setVisibility(View.VISIBLE);
                pwd_error_tv.setText("手机号不能为空！");
                return;
            }
            if (!mPhoneEnabled) {
                isError = true;
                pwd_error_tv.setVisibility(View.VISIBLE);
                pwd_error_tv.setText("手机号格式不对！");
                return;
            }
            pwd_error_tv.setVisibility(View.INVISIBLE);

            if (mCountdown == null) {
                mCountdown = new Countdown();
                mCountdown.setHandlerCallback(this);
            } else {
                mCountdown.reset();
            }
            mCountdown.resume();


            register_get_icode_btn.setEnabled(false);
            HcDialog.showProgressDialog(mContext,
                    R.string.dialog_title_get_data);
            mLoginMger
                    .getCode(mobile, mobile, CodeType.REGISTER.ordinal() + "");
        } else if (id == R.id.register_next_btn) {
            String mobile = mobile_register_one_et.getText().toString();
            String code = icode_register_one_et.getText().toString();
            if (HcUtil.isEmpty(mobile) || HcUtil.isEmpty(code)) {
                pwd_error_tv.setVisibility(View.VISIBLE);
                pwd_error_tv.setText(R.string.retrieve_one_error);
                isError = true;
                return;
            }
            HcDialog.showProgressDialog(mContext,
                    R.string.pull_to_refresh_refreshing_label);
            mLoginMger.checkCode(mobile, mobile, CodeType.REGISTER.ordinal()
                    + "", code);
        }
    }

    @Override
    public void initialized() {


    }

    @Override
    public void setContentView() {
        if (mView == null) {
            mView = mInflater.inflate(R.layout.register_one_page, null);
            mobile_register_one_et = (EditText) mView
                    .findViewById(R.id.mobile_register_one_et);
            icode_register_one_et = (EditText) mView
                    .findViewById(R.id.icode_register_one_et);
            register_get_icode_btn = (Button) mView
                    .findViewById(R.id.register_get_icode_btn);
            register_next_btn = (Button) mView
                    .findViewById(R.id.register_next_btn);
            pwd_error_tv = (TextView) mView.findViewById(R.id.pwd_error_tv);


            register_get_icode_btn.setOnClickListener(this);
            register_next_btn.setOnClickListener(this);
            mobile_register_one_et.setOnFocusChangeListener(this);
            icode_register_one_et.setOnFocusChangeListener(this);

            mobile_register_one_et.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void afterTextChanged(Editable s) {
                    // TODO Auto-generated method stub
                    if (s != null) {
                        int length = s.length();
                        if (length >= 8 && length <= 12)
                            mPhoneEnabled = true;
                        else if (length < 8) {
                            mPhoneEnabled = false;
                        } else {
                            mPhoneEnabled = false;
                            mobile_register_one_et.setText(s.subSequence(0, length - 1));
                            mobile_register_one_et.setSelection(12);
                        }
                    } else {
                        mPhoneEnabled = false;
                    }

                    if (mCodeEnabled && mPhoneEnabled) {
                        register_next_btn.setEnabled(true);
                    } else {
                        register_next_btn.setEnabled(false);
                    }
                }
            });

            icode_register_one_et.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void afterTextChanged(Editable s) {
                    // TODO Auto-generated method stub
                    if (s != null && s.length() > 0) {
                        mCodeEnabled = true;
                    } else {
                        mCodeEnabled = false;
                    }

                    if (mCodeEnabled && mPhoneEnabled) {
                        register_next_btn.setEnabled(true);
                    } else {
                        register_next_btn.setEnabled(false);
                    }
                }
            });

            register_next_btn.setEnabled(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mLoginMger.addObserver(this);
        String date = SettingHelper.getRegisterCode(mContext);
        if (!TextUtils.isEmpty(date)) { // 有可能需要设置倒计时
            String[] times = date.split(";");
            int count = Integer.valueOf(times[0]);
            long lastTime = Long.valueOf(times[1]);
            long current = System.currentTimeMillis();
            if (((current - lastTime) / 1000 - count) >= 0) { // 说明倒计时不需要了
                register_get_icode_btn.setText(R.string.get_icode);
                SettingHelper.setRegisterCode(mContext, "");
            } else {
                register_get_icode_btn.setEnabled(false);
                if (mCountdown == null) {
                    mCountdown = new Countdown((int) (count - (current - lastTime) / 1000));
                    mCountdown.setHandlerCallback(this);
                    mCountdown.resume();
                } else {
                    mCountdown.setTotleTime((int) (count - (current - lastTime) / 1000));
                }

            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mLoginMger.removeObserver(this);
    }

    private void reset() {
        if (mCountdown != null) {
            mCountdown.pause();
            mCountdown.setHandlerCallback(null);
            mCountdown.reset();
        }
        register_get_icode_btn.setText(R.string.get_icode);
        register_get_icode_btn.setEnabled(true);
    }

    @Override
    public void updateData(HcSubject subject, Object data,
                           RequestCategory request, ResponseCategory response) {

        HcDialog.deleteProgressDialog();
        if (subject != null && subject instanceof LoginManager) {
            if (request == RequestCategory.GETCODE) {
                switch (response) {
                    case SESSION_TIMEOUT:
                    case NETWORK_ERROR:
                        HcUtil.toastNetworkError(mContext);
                        break;
                    case SYSTEM_ERROR:
                        HcUtil.toastSystemError(mContext, data);
                        break;
                    case DATA_ERROR:
                        HcUtil.toastDataError(mContext);
                        break;
                    case REQUEST_FAILED:
                        ResponseCodeInfo info = (ResponseCodeInfo) data;
                        /**
                         * @author zhujb
                         * @date 2016-04-13 下午4:19:07
                         */
                        if (info.getCode() == HcHttpRequest.REQUEST_ACCOUT_EXCLUDED ||
                                info.getCode() == HcHttpRequest.REQUEST_TOKEN_FAILED) {
                            HcUtil.reLogining(info.getBodyData(), mContext, info.getMsg());
                        } else {
                            HcUtil.showToast(mContext, info.getMsg());
                        }
                        break;
                    case SUCCESS:
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

                        break;

                    default:
                        break;
                }

            } else if (request == RequestCategory.CHECKCODE) {
                switch (response) {
                    case SESSION_TIMEOUT:
                    case NETWORK_ERROR:
                        HcUtil.toastNetworkError(mContext);
                        break;
                    case SYSTEM_ERROR:
                        HcUtil.toastSystemError(mContext, data);
                        break;
                    case DATA_ERROR:
                        HcUtil.toastDataError(mContext);
                        break;
                    case REQUEST_FAILED:
                        ResponseCodeInfo info = (ResponseCodeInfo) data;
                        /**
                         * @author zhujb
                         * @date 2016-04-13 下午4:19:07
                         */
                        if (info.getCode() == HcHttpRequest.REQUEST_ACCOUT_EXCLUDED ||
                                info.getCode() == HcHttpRequest.REQUEST_TOKEN_FAILED) {
                            HcUtil.reLogining(info.getBodyData(), mContext, info.getMsg());
                        } else {
                            HcUtil.showToast(mContext, info.getMsg());
                        }
                        break;
                    case SUCCESS:
                        // 1,解析数据
                        String json = (String) data;
                        // 2,跳转界面
                        String mobile = mobile_register_one_et.getText().toString();
                        String code = icode_register_one_et.getText().toString();
                        Intent intent = new Intent(mContext,
                                RegisterActivityTwo.class);
                        intent.putExtra("account", mobile);
                        intent.putExtra("mobile", mobile);
                        intent.putExtra("code", code);
                        mContext.startActivity(intent);
                        mContext.finish();
                        break;

                    default:
                        break;
                }


            }
        }

    }

    @Override
    public void onFocusChange(View view, boolean isFocused) {
        int id = view.getId();
        if (isFocused) {
            if (id == R.id.mobile_register_one_et
                    || id == R.id.icode_register_one_et) {
                if (isError) {
                    isError = !isError;
                    pwd_error_tv.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void release() {
        super.release();
        if (mCountdown != null) {
            mCountdown.setHandlerCallback(null);
            mCountdown.pause();
            int totle = mCountdown.getTotleTime();
            if (totle > 0) {
                SettingHelper.setRegisterCode(mContext, totle + ";" + System.currentTimeMillis());
            }
            mCountdown = null;
        }
    }

    @Override
    public void setTime(int totle) {
        // TODO Auto-generated method stub
        HcLog.D("RegisterOnePage setTime totle = " + totle);
        if (totle <= 0) {
            register_get_icode_btn.setText(R.string.get_icode);
            register_get_icode_btn.setEnabled(true);
            SettingHelper.setRegisterCode(mContext, "");
        } else {
            register_get_icode_btn.setText(mContext.getResources().getString(R.string.timer_code, "" + totle));
        }
    }

}
