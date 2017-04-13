package com.android.hcframe.pcenter;

import java.util.Observable;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.android.hcframe.internalservice.signin.TimerUtils;
import com.android.hcframe.internalservice.signin.TimerUtils.ITimerListerner;
import com.android.hcframe.login.LoginManager.CodeType;
import com.android.hcframe.modifypwd.ModifyAct;
import com.android.hcframe.push.HcAppState;
import com.android.hcframe.sql.SettingHelper;

public class ModifyBindTwoPage extends AbstractPage implements
        OnFocusChangeListener, HcObserver, ITimerListerner {

    private EditText mobile_bind_two_et;

    private EditText modify_bind_icode_et;

    private Button modify_bind_icode_btn;

    private Button bind_confirm_submit_btn;

    private TextView pwd_error_tv;

    private TimerUtils codeTimer = new TimerUtils();

    private Handler mHander;

    private int count = 0;

    private boolean isError = false;

    private boolean isFirst = true;

    private PCenterManager pCM = new PCenterManager();

    private String oldcode;

    private String newcode;

    private String oldmobile;

    private String newmobile;

    protected ModifyBindTwoPage(Activity context, ViewGroup group) {
        super(context, group);
    }

    @Override
    public void update(Observable observable, Object data) {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.modify_bind_icode_btn) {
            String mobile = mobile_bind_two_et.getText().toString();
            if (HcUtil.isEmpty(mobile)) {
                isError = true;
                pwd_error_tv.setVisibility(View.VISIBLE);
                pwd_error_tv.setText(R.string.mobile_is_null);
                return;
            }
            codeTimer.startTimer();
            modify_bind_icode_btn.setEnabled(false);
            HcDialog.showProgressDialog(mContext,
                    R.string.dialog_title_get_data);
            // 发送获取验证码的请求
            pCM.getCode(mobile, mobile, CodeType.REGISTER.ordinal() + "");
        } else if (id == R.id.bind_confirm_submit_btn) {
            String newmobile = mobile_bind_two_et.getText().toString();
            String newcode = modify_bind_icode_et.getText().toString();
            if (HcUtil.isEmpty(newcode) || HcUtil.isEmpty(newmobile)) {
                pwd_error_tv.setVisibility(View.VISIBLE);
                pwd_error_tv.setText(R.string.modify_bind_one_is_null);
                isError = true;
                return;
            }
            HcDialog.showProgressDialog(mContext,
                    R.string.pull_to_refresh_refreshing_label);
            // 发送提交请求
            pCM.sendBindPhoneCommand(SettingHelper.getAccount(mContext),
                    oldmobile, newmobile, oldcode, newcode);
        }
    }

    @Override
    public void initialized() {
        if (isFirst) {
            isFirst = !isFirst;
            // 初始化数据
            oldcode = ((ModifyBindTwoActivity) mContext).getOldcode();
            oldmobile = SettingHelper.getMobile(mContext);

            modify_bind_icode_btn.setOnClickListener(this);
            bind_confirm_submit_btn.setOnClickListener(this);
            mobile_bind_two_et.setOnFocusChangeListener(this);
            modify_bind_icode_et.setOnFocusChangeListener(this);

            mHander = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    String data = (String) msg.obj;
                    if ("0".equals(data)) {
                        modify_bind_icode_btn.setEnabled(true);
                        count = 0;
                        modify_bind_icode_btn.setText(R.string.get_icode);
                        codeTimer.stopTimer();
                    } else {
                        modify_bind_icode_btn.setText(String.format(
                                mContext.getString(R.string.timer_code), data));
                    }
                }

            };

            codeTimer.initTimer(0, 1000, this, mHander);
        }
    }

    @Override
    public void setContentView() {
        if (mView == null) {
            mView = mInflater.inflate(R.layout.modify_bindphone_two_page, null);
            mobile_bind_two_et = (EditText) mView
                    .findViewById(R.id.mobile_bind_two_et);
            modify_bind_icode_et = (EditText) mView
                    .findViewById(R.id.modify_bind_icode_et);
            modify_bind_icode_btn = (Button) mView
                    .findViewById(R.id.modify_bind_icode_btn);
            bind_confirm_submit_btn = (Button) mView
                    .findViewById(R.id.bind_confirm_submit_btn);
            pwd_error_tv = (TextView) mView.findViewById(R.id.pwd_error_tv);

        }
    }

    @Override
    public void onFocusChange(View view, boolean isFocused) {
        int id = view.getId();
        if (isFocused) {
            if (id == R.id.mobile_bind_two_et
                    || id == R.id.modify_bind_icode_et) {
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
        pCM.addObserver(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        pCM.removeObserver(this);
    }

    @Override
    public void updateData(HcSubject subject, Object data,
                           RequestCategory request, ResponseCategory response) {
        HcDialog.deleteProgressDialog();
        if (subject != null && subject instanceof PCenterManager) {
            if (request == RequestCategory.GETCODE) {
                switch (response) {
                    case SESSION_TIMEOUT:
                    case NETWORK_ERROR:
                        HcUtil.toastTimeOut(mContext);
                        break;
                    case DATA_ERROR:
                        HcUtil.toastDataError(mContext);
                        break;
                    case SYSTEM_ERROR:
                        HcUtil.toastSystemError(mContext, data);
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
                    default:
                        break;

                }

            } else if (request == RequestCategory.BINDP) {
                switch (response) {
                    case SESSION_TIMEOUT:
                    case NETWORK_ERROR:
                        HcUtil.toastTimeOut(mContext);
                        break;
                    case DATA_ERROR:
                        HcUtil.toastDataError(mContext);
                        break;
                    case SYSTEM_ERROR:
                        HcUtil.toastSystemError(mContext, data);
                        break;
                    case SUCCESS:
                        SettingHelper.setMobile(mContext, newmobile);
                        mContext.setResult(1);
                        mContext.finish();
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
                    default:
                        break;

                }

            }
        }
    }

    @SuppressWarnings({"unchecked", "hiding"})
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
