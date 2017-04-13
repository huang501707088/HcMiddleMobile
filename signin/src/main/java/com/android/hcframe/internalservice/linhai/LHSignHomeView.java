package com.android.hcframe.internalservice.linhai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.container.ContainerActivity;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.HttpRequestQueue;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.internalservice.sign.SignCache;
import com.android.hcframe.internalservice.sign.SignLoctionUtils;
import com.android.hcframe.internalservice.sign.SignSubmitActivity;
import com.android.hcframe.internalservice.signin.R;
import com.android.hcframe.push.HcAppState;
import com.android.hcframe.push.HcPushManager;
import com.android.hcframe.sql.SettingHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Observable;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-7-1 08:37.
 */
public class LHSignHomeView extends AbstractPage implements SignLoctionUtils.DistanceCallback {

    private static final String TAG = "LHSignHomeView";

    private TextView mDate;

    private TextView mTime;

    private TextView mSignBtn;

    private TextView mDetailBtn;

    private SignResponse mResponse;

    private final String mAppId;

    private TimeHander mHandler = new TimeHander();

    public LHSignHomeView(Activity context, ViewGroup group, String appId) {
        super(context, group);
        mAppId = appId;
    }

    @Override
    public void initialized() {
        String date = HcUtil.getDate(HcUtil.FORMAT_SIGN_YEAR_MONTH_DAY, System.currentTimeMillis());
        String week = HcUtil.getDayOfWeek(mContext, System.currentTimeMillis());
        mDate.setText(date + " " + week);
    }

    @Override
    public void setContentView() {

        if (mView == null) {
            mView = mInflater.inflate(R.layout.linhai_home_layout, null);

            mDate = (TextView) mView.findViewById(R.id.linhai_sign_home_date);
            mTime = (TextView) mView.findViewById(R.id.linhai_sign_home_time);
            mSignBtn = (TextView) mView.findViewById(R.id.linhai_sign_home_sign_btn);
            mDetailBtn = (TextView) mView.findViewById(R.id.linhai_sign_home_detail_btn);

            mSignBtn.setOnClickListener(this);
            mDetailBtn.setOnClickListener(this);
        }

    }

    @Override
    public void update(Observable observable, Object data) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.linhai_sign_home_sign_btn) {
            switch (mLocation) {
                case NONE:
                    HcUtil.showToast(mContext, "正在定位中...");
                    break;
                case LOCAL:
                    SignRequest request = new SignRequest();
                    if (mResponse == null) {
                        mResponse = new SignResponse();
                    }
                    HcDialog.showProgressDialog(mContext, "正在提交数据...");
                    request.sendRequestCommand(RequestCategory.SIGN, mResponse, false);
                    break;
                case REMOTE: // 跳转到
                    Intent intent = new Intent(mContext, SignSubmitActivity.class);
                    mContext.startActivity(intent);
                    break;

                default:
                    break;
            }
        } else if (id == R.id.linhai_sign_home_detail_btn) {
            Intent intent = new Intent(mContext, LHSignDayListActivity.class);
            mContext.startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        mHandler.resume();
        HcPushManager.getInstance().setPushInfo(null);
        if (!HcUtil.isGPS(mContext)) {
            final AlertDialog dialog = new AlertDialog.Builder(mContext)
                    .create();
            dialog.setCancelable(false);
            dialog.show();
            dialog.getWindow().setContentView(R.layout.enable_gps_dialog);
            TextView netGpsTv = (TextView) dialog.getWindow().findViewById(R.id.net_gps_tv);

            Button unagree_dialog = (Button) dialog.getWindow()
                    .findViewById(R.id.unagree_dialog);
            Button agree_dialog = (Button) dialog.getWindow().findViewById(
                    R.id.agree_dialog);
            netGpsTv.setText("签到考勤需要开启Gps，你同意吗？");
            unagree_dialog.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    // 这里要增加一个判断,要是在一级菜单里就不能退出,因为退出会把应用关闭.
                    if (mContext instanceof ContainerActivity) {
                        HcAppState.getInstance().removeActivity(mContext);
                        mContext.finish();
                    }

                }
            });
            agree_dialog.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    HcUtil.openGPS(mContext);
                }
            });
        } else {
            /**
             * @jrjin
             * @2016-06-20
             */
            SignLoctionUtils.setDistanceCallback(this);
            SignLoctionUtils.startLocation();
        }
        SignCache.getInstance().repeatConfigExist(mContext);
    }

    private Location mLocation = Location.NONE;

    private enum Location {
        NONE,
        LOCAL,
        REMOTE
    }

    @Override
    public void notifyDistance(String distance) {
        // 判断距离
        HcLog.D(TAG + " #notifyDistance current distance = " + distance);

        if (TextUtils.isEmpty(distance)) { // 未设置考勤点
            // 显示远距离
            mLocation = Location.REMOTE;

        } else {
            double d = Double.parseDouble(distance);
            HcLog.D(TAG + " #notifyDistance distance ====" + d + " max distance =====" + SignCache.getInstance().getMaxDistance());
            if (d < SignCache.getInstance().getMaxDistance()) {

                // 本地打卡(用replace,每一次都要去new,这是没办法避免的事，除非用add,hide,show)
                mLocation = Location.LOCAL;
            } else {

                // 远距离打卡
                mLocation = Location.REMOTE;
            }
        }

    }

    private class SignRequest extends AbstractHttpRequest {

        @Override
        public String getParameterUrl() {
            return "?userId="+SettingHelper.getUserId(mContext)
                    + "&signType=0" + "&addressLongitude=" + SignLoctionUtils.getLng()
                    + "&addressLatitude=" + SignLoctionUtils.getLat()
                    + "&address=" + SignLoctionUtils.getAddress();
        }

        @Override
        public String getRequestMethod() {
            return "lhsign";
        }

        @Override
        public void parseJson(String data) {
            HcDialog.deleteProgressDialog();
            HcLog.D(TAG + " #parseJson data = " + data);
            try {
                JSONObject object = new JSONObject(data);
                int status = object.getInt(HttpRequestQueue.STATUS);
                String msg = "";
                if (HcUtil.hasValue(object, HttpRequestQueue.MSG)) {
                    msg = object.getString(HttpRequestQueue.MSG);
                }
                if (status == 610 || status == 620) {
                    object = object.getJSONObject(HttpRequestQueue.BODY);
                    final String toast = msg;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            HcUtil.showToast(mContext, toast);
                        }
                    });
                    onSuccess(object.toString());
                } else if (status == HcHttpRequest.REQUEST_ACCOUT_EXCLUDED ||
                        status == HcHttpRequest.REQUEST_TOKEN_FAILED) {

                    object = object.getJSONObject(HttpRequestQueue.BODY);
                    onAccountExcluded(object.toString(), msg);
                } else {

                    onRequestFailed(status, msg);
                }
            } catch (Exception e) {
                // TODO: handle exception
                HcLog.D(TAG + " #parseJson error e = "+e);
                onParseDataError();
            }
        }
    }

    private class SignResponse extends AbstractHttpResponse {

        private static final String TAG = LHSignHomeView.TAG + "$SignResponse";

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            // 还在线程里面
            try {
                JSONObject object = new JSONObject((String) data);
                if (HcUtil.hasValue(object, "signInTime")) {
                    SettingHelper.setSigninTime(mContext, object.getString("signInTime"));
                }
                if (HcUtil.hasValue(object, "signOutTime")) {
                    SettingHelper.setSignoutTime(mContext, object.getString("signOutTime"));
                }
            } catch (JSONException e) {
                HcLog.D(TAG + " #onSuccess JSONException e ="+e);
            }
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcUtil.reLogining(data, mContext, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

        }
    }

    private class TimeHander extends Handler {

        void resume() {
            if (!hasMessages(0)) {
                sendEmptyMessage(0);
            }
        }

        void pause() {
            removeMessages(0);
        }

        @Override
        public void handleMessage(Message message) {
            // 更新时间
            mTime.setText(HcUtil.getDate("HH:mm", System.currentTimeMillis()));

            // 发送message
            sendEmptyMessageDelayed(0, 1000);

        }
    }

    @Override
    public void onPause() {
        mHandler.pause();
    }
}
