package com.android.hcframe.pcenter;

import java.util.Observable;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
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
import com.android.hcframe.sql.SettingHelper;

public class NickNamePage extends AbstractPage implements
        OnFocusChangeListener, HcObserver {

    private EditText modify_nickname_et;

    private Button confirm_modify_btn;

    private TextView pwd_error_tv;

    private boolean isError = false;

    private boolean isFirst = true;

    private String nickT = "";

    private PCenterManager pCM = new PCenterManager();

    protected NickNamePage(Activity context, ViewGroup group) {
        super(context, group);
    }

    @Override
    public void update(Observable observable, Object data) {

    }

    @Override
    public void onClick(View view) {
        nickT = modify_nickname_et.getText().toString();
        if (HcUtil.isEmpty(nickT)) {
            pwd_error_tv.setVisibility(View.VISIBLE);
            pwd_error_tv.setText(R.string.nickname_is_null);
            isError = true;
            return;
        }

        HcDialog.showProgressDialog(mContext, R.string.dialog_title_post_data);
        // 发送提交请求
        pCM.sendNicknameCommand(SettingHelper.getAccount(mContext), nickT);
    }

    @Override
    public void initialized() {
        if (isFirst) {
            isFirst = !isFirst;
            confirm_modify_btn.setOnClickListener(this);
            modify_nickname_et.setOnFocusChangeListener(this);
            // 初始化昵称
            String nick = SettingHelper.getName(mContext);
            if (!TextUtils.isEmpty(nick)) {
                modify_nickname_et.setText(nick);
                modify_nickname_et.setSelection(nick.length());
            }

        }
    }

    @Override
    public void setContentView() {
        if (mView == null) {
            mView = mInflater.inflate(R.layout.modify_nickname_page, null);
            modify_nickname_et = (EditText) mView
                    .findViewById(R.id.modify_nickname_et);
            confirm_modify_btn = (Button) mView
                    .findViewById(R.id.confirm_modify_btn);
            pwd_error_tv = (TextView) mView.findViewById(R.id.pwd_error_tv);
        }
    }

    @Override
    public void onFocusChange(View view, boolean isFocused) {
        int id = view.getId();
        if (isFocused) {
            if (id == R.id.modify_nickname_et) {
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
            if (request == RequestCategory.NICKNAME) {

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
                        SettingHelper.setName(mContext, nickT);
                        mContext.setResult(1);
                        mContext.finish();
                        break;

                    default:
                        break;
                }

            }
        }
    }
}
