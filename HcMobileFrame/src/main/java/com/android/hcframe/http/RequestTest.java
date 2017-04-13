package com.android.hcframe.http;

import android.os.Bundle;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.sql.SettingHelper;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-6-12 14:41.
 */
public class RequestTest extends HcBaseActivity {

    private static final String TAG = "RequestTest";

    private SignResponse mResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 请求数据
        SignRequest request = new SignRequest("");
        if (mResponse == null) {
            mResponse = new SignResponse();
        }
        HcDialog.showProgressDialog(this, "正在获取数据...");
        request.sendRequestCommand(RequestCategory.SIGNLISTBYMONTH, mResponse, false);

    }

    private class SignRequest extends AbstractHttpRequest {

        private static final String TAG = RequestTest.TAG + "$SignRequest";

        private final String mDate;

        public SignRequest(String date) {
            mDate = date;
        }

        @Override
        public String getParameterUrl() {
            return "?account=" + HttpRequestQueue.URLEncode(SettingHelper.getAccount(RequestTest.this)) + "&searchDate="+mDate;
        }

        @Override
        public String getRequestMethod() {
            return "getsignlistByMonth";
        }
    }

    private class SignResponse extends AbstractHttpResponse {

        private static final String TAG = RequestTest.TAG + "$SignResponse";

        public SignResponse() {
        }

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            HcDialog.deleteProgressDialog();
            try {
                JSONObject object = new JSONObject((String) data);
                if (HcUtil.hasValue(object, "searchDate")) {
                    String month = object.getString("searchDate");

                }
            } catch(JSONException e) {
                HcLog.D(TAG + " #onSuccess parseJson error ="+e);
            }
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
            HcLog.D(TAG + " #notifyRequestMd5Url md5Url = "+md5Url + " request = "+request);
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcDialog.deleteProgressDialog();
            HcUtil.reLogining(data, RequestTest.this, msg);
        }
    }
}
