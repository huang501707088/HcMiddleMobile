package com.android.hcframe.login;

import java.util.Observable;

import android.app.Activity;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

public class RetrieveTwoPage extends AbstractPage implements HcObserver,
        OnFocusChangeListener {

    private EditText new_pwd_center_et;

    private EditText confirm_pwd_center_et;

    private Button confirm_modify_btn;

    private boolean isFirst = true;

    private TextView pwd_error_tv;

    private boolean isError = false;

    private LoginManager mLoginManager = new LoginManager();

    private IRetrieveInfo iRetrieveInfo;

    protected RetrieveTwoPage(Activity context, ViewGroup group) {
        super(context, group);
        iRetrieveInfo = (IRetrieveInfo) context;
    }

    @Override
    public void update(Observable observable, Object data) {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.confirm_modify_btn) {
            String new_pwd = new_pwd_center_et.getText().toString();
            String confirm_pwd = confirm_pwd_center_et.getText().toString();
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
            if (!HcUtil.isNetWorkError(mContext)) {
                HcDialog.showProgressDialog(mContext,
                        R.string.dialog_title_get_data);
                if (iRetrieveInfo != null) {
                    mLoginManager.sendRegetPwdCommand(
                            iRetrieveInfo.getAccount(),
                            iRetrieveInfo.getMobile(), new_pwd,
                            iRetrieveInfo.getCode());
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
            confirm_modify_btn.setOnClickListener(this);
            new_pwd_center_et.setOnFocusChangeListener(this);
            confirm_pwd_center_et.setOnFocusChangeListener(this);
        }
    }

    @Override
    public void setContentView() {
        if (mView == null) {
            mView = mInflater.inflate(R.layout.retrieve_two_step, null);
            new_pwd_center_et = (EditText) mView
                    .findViewById(R.id.new_pwd_center_et);
            confirm_pwd_center_et = (EditText) mView
                    .findViewById(R.id.confirm_pwd_center_et);
            confirm_modify_btn = (Button) mView
                    .findViewById(R.id.confirm_modify_btn);
            pwd_error_tv = (TextView) mView.findViewById(R.id.pwd_error_tv);
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
            if (request == RequestCategory.REGETPWD) {
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

    @Override
    public void onFocusChange(View view, boolean isFocused) {
        int id = view.getId();
        if ((id == R.id.new_pwd_center_et || id == R.id.confirm_pwd_center_tv)
                && isFocused && isError) {
            pwd_error_tv.setVisibility(View.GONE);
        }
    }
}
