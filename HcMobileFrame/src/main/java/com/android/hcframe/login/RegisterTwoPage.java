package com.android.hcframe.login;

import java.util.Observable;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcObserver;
import com.android.hcframe.HcSubject;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.http.ResponseCodeInfo;

public class RegisterTwoPage extends AbstractPage implements
        OnFocusChangeListener, HcObserver {

    private TextView user_register_two_et;

    private EditText nickname_register_two_et;

    private EditText newpwd_register_two_et;

    private EditText confirmpwd_register_two_et;

    private TextView pwd_error_tv;

    private Button confirm_submit_btn;

    private LinearLayout pwd_hint_lly;

    private boolean isFirst = true;

    private boolean isError = false;

    private IRetrieveInfo iRetrieveInfo;

    private LoginManager mLoginManager = new LoginManager();

    private boolean mNewPwEnabled = false;

    private boolean mConfirmPwEnable = false;

    protected RegisterTwoPage(Activity context, ViewGroup group) {
        super(context, group);
        iRetrieveInfo = (IRetrieveInfo) context;
    }

    @Override
    public void update(Observable observable, Object data) {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.confirm_submit_btn) {
            // 确认提交
            String new_pwd = newpwd_register_two_et.getText().toString();
            String confirm_pwd = confirmpwd_register_two_et.getText()
                    .toString();
            String account = user_register_two_et.getText().toString();
            String nickname = nickname_register_two_et.getText().toString();
            if (HcUtil.isEmpty(account) || HcUtil.isEmpty(nickname)) {
                pwd_error_tv.setVisibility(View.VISIBLE);
                pwd_error_tv.setText(R.string.user_account_not_null);
                isError = true;
                return;
            }
            if (HcUtil.isEmpty(new_pwd) || HcUtil.isEmpty(confirm_pwd)) {
                pwd_error_tv.setVisibility(View.VISIBLE);
                pwd_error_tv.setText(R.string.toast_login_pw_null);
                isError = true;
                return;
            }

            if (!new_pwd.equals(confirm_pwd)) {
                pwd_error_tv.setVisibility(View.VISIBLE);
                pwd_error_tv.setText(R.string.pwd_not_equal);
                isError = true;
                return;
            }

            if (!mNewPwEnabled || !mConfirmPwEnable) {
                pwd_error_tv.setVisibility(View.VISIBLE);
                pwd_error_tv.setText("密码格式不对！");
                isError = true;
                return;
            }

            if (!HcUtil.isNetWorkError(mContext)) {
                HcDialog.showProgressDialog(mContext,
                        R.string.dialog_title_get_data);
                if (iRetrieveInfo != null) {
                    mLoginManager.sendRegisterCommand(account, account,
                            new_pwd, iRetrieveInfo.getCode());
                } else {
                    HcUtil.showToast(mContext, R.string.toast_data_error);
                }
            }
        }
    }

    @Override
    public void initialized() {
        if (isFirst) {
            isFirst = false;
            newpwd_register_two_et.setOnFocusChangeListener(this);
            confirmpwd_register_two_et.setOnFocusChangeListener(this);
            confirm_submit_btn.setOnClickListener(this);
            if (iRetrieveInfo != null) {
                user_register_two_et.setText(iRetrieveInfo.getAccount());
            }
        }
    }

    @Override
    public void setContentView() {
        if (mView == null) {
            mView = mInflater.inflate(R.layout.register_two_page, null);
            user_register_two_et = (TextView) mView
                    .findViewById(R.id.user_register_two_et);
            nickname_register_two_et = (EditText) mView
                    .findViewById(R.id.nickname_register_two_et);
            newpwd_register_two_et = (EditText) mView
                    .findViewById(R.id.newpwd_register_two_et);
            confirmpwd_register_two_et = (EditText) mView
                    .findViewById(R.id.confirmpwd_register_two_et);
            pwd_error_tv = (TextView) mView.findViewById(R.id.pwd_error_tv);
            confirm_submit_btn = (Button) mView
                    .findViewById(R.id.confirm_submit_btn);
            pwd_hint_lly = (LinearLayout) mView.findViewById(R.id.pwd_hint_lly);

            newpwd_register_two_et.addTextChangedListener(new TextWatcher() {

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
                    setText(s, newpwd_register_two_et);
                }
            });

            confirmpwd_register_two_et.addTextChangedListener(new TextWatcher() {

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
                    setText(s, confirmpwd_register_two_et);
                }
            });

        }
    }

    private void setText(Editable s, EditText et) {
        boolean lable = false;
        if (s != null) {
            int length = s.length();
            if (length > 0) {
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
            }

        } else {
            lable = false;
            et.setText("");
        }
        if (et == confirmpwd_register_two_et) {
            mConfirmPwEnable = lable;
        } else if (et == newpwd_register_two_et) {
            mNewPwEnabled = lable;
        }
    }

    @Override
    public void onFocusChange(View view, boolean isfocused) {
        int id = view.getId();
        if (isfocused) {
            if (id == R.id.newpwd_register_two_et) {
                if (isError) {
                    isError = !isError;
                    pwd_error_tv.setVisibility(View.GONE);
                } else {
                    pwd_hint_lly.setVisibility(View.VISIBLE);
                }
            } else if (id == R.id.confirmpwd_register_two_et) {
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
        mLoginManager.addObserver(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mLoginManager.removeObserver(this);
    }

    @Override
    public void updateData(HcSubject subject, Object data,
                           RequestCategory request, ResponseCategory response) {
        HcDialog.deleteProgressDialog();
        if (subject != null && subject instanceof LoginManager) {
            if (request == RequestCategory.REGISTER) {
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
                        mContext.finish();
                        break;

                    default:
                        break;
                }

            }
        }
    }
}
