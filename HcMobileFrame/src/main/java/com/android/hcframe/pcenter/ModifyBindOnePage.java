package com.android.hcframe.pcenter;

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
import com.android.hcframe.login.LoginManager;
import com.android.hcframe.login.RetrieveActivityTwo;
import com.android.hcframe.login.LoginManager.CodeType;
import com.android.hcframe.sql.SettingHelper;

public class ModifyBindOnePage extends AbstractPage implements ITimerListerner,
        HcObserver, OnFocusChangeListener {

    private boolean isFirst = true;

    private TextView bind_cur_phone_value_tv;

    private EditText modifyphone_one_et;

    private Button modifyphone_one_icode_btn;

    private Button modifyphone_next_btn;

    private TextView pwd_error_tv;

    private String mobile = "";

    private TimerUtils codeTimer = new TimerUtils();

    private Handler mHander;

    private int count = 0;

    private boolean isError = false;

    private PCenterManager pCM = new PCenterManager();

    protected ModifyBindOnePage(Activity context, ViewGroup group) {
        super(context, group);
    }

    @Override
    public void update(Observable observable, Object data) {

    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        if (id == R.id.modifyphone_one_icode_btn) {
            if (HcUtil.isEmpty(mobile)) {
                isError = true;
                pwd_error_tv.setVisibility(View.VISIBLE);
                pwd_error_tv.setText(R.string.mobile_is_null);
                return;
            }
            codeTimer.startTimer();
            modifyphone_one_icode_btn.setEnabled(false);
            HcDialog.showProgressDialog(mContext,
                    R.string.dialog_title_get_data);
            // 发送获取验证码的请求
            pCM.getCode(mobile, "", CodeType.RETRIEVE.ordinal() + "");
        } else if (id == R.id.modifyphone_next_btn) {
            String code = modifyphone_one_et.getText().toString();
            if (HcUtil.isEmpty(code) || HcUtil.isEmpty(mobile)) {
                pwd_error_tv.setVisibility(View.VISIBLE);
                pwd_error_tv.setText(R.string.modify_bind_one_is_null);
                isError = true;
                return;
            }
            HcDialog.showProgressDialog(mContext,
                    R.string.pull_to_refresh_refreshing_label);
            // 发送验证验证码
            pCM.checkCode(SettingHelper.getAccount(mContext), mobile,
                    CodeType.RETRIEVE.ordinal() + "", code);
        }
    }

    @Override
    public void initialized() {
        if (isFirst) {
            isFirst = !isFirst;

            // 初始化数据
            mobile = SettingHelper.getMobile(mContext);
            bind_cur_phone_value_tv.setText(HcUtil.isEmpty(mobile) ? ""
                    : mobile);

            modifyphone_one_icode_btn.setOnClickListener(this);
            modifyphone_next_btn.setOnClickListener(this);
            modifyphone_one_et.setOnFocusChangeListener(this);
            mHander = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    String data = (String) msg.obj;
                    if ("0".equals(data)) {
                        modifyphone_one_icode_btn.setEnabled(true);
                        count = 0;
                        modifyphone_one_icode_btn.setText(R.string.get_icode);
                        codeTimer.stopTimer();
                    } else {
                        modifyphone_one_icode_btn.setText(String.format(
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
            mView = mInflater.inflate(R.layout.modify_bindphone_one_page, null);
            bind_cur_phone_value_tv = (TextView) mView
                    .findViewById(R.id.bind_cur_phone_value_tv);
            modifyphone_one_et = (EditText) mView
                    .findViewById(R.id.modifyphone_one_et);
            modifyphone_one_icode_btn = (Button) mView
                    .findViewById(R.id.modifyphone_one_icode_btn);
            modifyphone_next_btn = (Button) mView
                    .findViewById(R.id.modifyphone_next_btn);
            pwd_error_tv = (TextView) mView.findViewById(R.id.pwd_error_tv);
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

    @Override
    public void updateData(HcSubject subject, Object data,
                           RequestCategory request, ResponseCategory response) {
        HcDialog.deleteProgressDialog();
        if (subject != null && subject instanceof PCenterManager) {

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
                        // 2,跳转界面
                        String code = modifyphone_one_et.getText().toString();
                        Intent intent = new Intent(mContext,
                                ModifyBindTwoActivity.class);
                        intent.putExtra("oldcode", code);
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
            if (id == R.id.modifyphone_one_et) {
                if (isError) {
                    isError = !isError;
                    pwd_error_tv.setVisibility(View.GONE);
                }
            }
        }

    }

}
