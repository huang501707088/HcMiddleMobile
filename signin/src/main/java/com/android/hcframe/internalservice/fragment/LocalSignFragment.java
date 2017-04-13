package com.android.hcframe.internalservice.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcObserver;
import com.android.hcframe.HcSubject;
import com.android.hcframe.HcUtil;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.HttpRequestQueue;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.http.ResponseCodeInfo;
import com.android.hcframe.internalservice.sign.MonthCalendarActivity;
import com.android.hcframe.internalservice.sign.SignCache;
import com.android.hcframe.internalservice.sign.SignLoctionUtils;
import com.android.hcframe.internalservice.sign.WorkDetailActivity;
import com.android.hcframe.internalservice.signcls.Loction;
import com.android.hcframe.internalservice.signin.R;
import com.android.hcframe.sql.SettingHelper;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Administrator on 2016/4/29 0029.
 */
public class LocalSignFragment extends Fragment implements View.OnClickListener, IHttpResponse, HcObserver {

    private static final String TAG = "LocalSignFragment";

    private LocalSignResponse mResponse;
    private Button goWorkImgBtn;
    private Button offWorkImgBtn;
    //自动打卡
    private TextView localTimeText;
    private TextView localDateText;
    private TextView localWeekText;
    private TextView signDetails;
    private View localSignView;

    //小时钟
    private static final String DATE_FORMAT = "%02d:%02d";
    private static final int REFRESH_DELAY = 500;

    private String imei;
    private String myImei;
    private final Handler handler = new Handler();


    private final Runnable mTimeRefresher = new Runnable() {


        @Override
        public void run() {
            final Date d = new Date();
            localTimeText.setText(String.format(DATE_FORMAT, d.getHours(),
                    d.getMinutes(), d.getSeconds()));
            Calendar c = Calendar.getInstance();
            c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
            String mYear = String.valueOf(c.get(Calendar.YEAR));
            String mMonth = String.valueOf(c.get(Calendar.MONTH) + 1);// 获取当前月份
            String mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));// 获取当前月份的日期号码
            String mWay = String.valueOf(c.get(Calendar.DAY_OF_WEEK));
            if ("1".equals(mWay)) {
                mWay = "日";
            } else if ("2".equals(mWay)) {
                mWay = "一";
            } else if ("3".equals(mWay)) {
                mWay = "二";
            } else if ("4".equals(mWay)) {
                mWay = "三";
            } else if ("5".equals(mWay)) {
                mWay = "四";
            } else if ("6".equals(mWay)) {
                mWay = "五";
            } else if ("7".equals(mWay)) {
                mWay = "六";
            }
            localDateText.setText(mYear + "年" + mMonth + "月" + mDay + "日");
            localWeekText.setText("周" + mWay);
            handler.postDelayed(this, REFRESH_DELAY);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        localSignView = inflater.inflate(R.layout.local_sign_home_layout, container, false);//关联布局文件
        goWorkImgBtn = (Button) localSignView.findViewById(R.id.go_work_img_btn);
        offWorkImgBtn = (Button) localSignView.findViewById(R.id.off_work_img_btn);
        signDetails = (TextView) localSignView.findViewById(R.id.sign_details);
        localTimeText = (TextView) localSignView.findViewById(R.id.time);
        localDateText = (TextView) localSignView.findViewById(R.id.date);
        localWeekText = (TextView) localSignView.findViewById(R.id.week);

        offWorkImgBtn.setOnClickListener(this);
        goWorkImgBtn.setOnClickListener(this);
        signDetails.setOnClickListener(this);
        return localSignView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        SignCache.getInstance().addObserver(this);
        super.onResume();
        imei = SignCache.getInstance().getImei();
        myImei = HcUtil.getIMEI(getActivity());
        handler.post(mTimeRefresher);
//        String signTimeStr = SettingHelper.getTodaySignTime(getActivity(), PREFERENCE_KEY_SIGN_TIME);
//        if (!TextUtils.isEmpty(signTimeStr)) {
//            String[] signTimes = signTimeStr.split("#");
//            dateTime = HcUtil.getDate(HcUtil.FORMAT_YEAR_MONTH_DAY, System.currentTimeMillis());
//            if (!signTimes[0].equals(dateTime)) {
//                goWorkImgBtn.setText("签入" + "\n");
//                offWorkImgBtn.setText("签出" + "\n");
//            } else {
//                String[] accountTimes = signTimes[1].split(";");
//                for (String accountTime : accountTimes) {
//                    String[] account = accountTime.split("_");
//                    if (SettingHelper.getAccount(getActivity()).equals(account[0])) {
//                        String[] time = account[1].split("&");
//                        if (!TextUtils.isEmpty(time[0]) && !"null".equals(time[0])) {
//                            goWorkImgBtn.setText("签入成功" + "\n" + simpleDate(time[0]));
//                        } else {
//                            goWorkImgBtn.setText("签入" + "\n");
//                        }
//                        if (!TextUtils.isEmpty(time[1]) && !"null".equals(time[1])) {
//                            offWorkImgBtn.setText("签出成功" + "\n" + simpleDate(time[1]));
//                        } else {
//                            offWorkImgBtn.setText("签出" + "\n");
//                        }
//                    }
//                }
//            }
//        }
//
//        if (!TextUtils.isEmpty(SignCache.getInstance().getSignInTime())) {
//            goWorkImgBtn.setText("签入成功" + "\n" + simpleDate(SignCache.getInstance().getSignInTime()));
//        } else {
//            goWorkImgBtn.setText("签入" + "\n");
//        }
//        if (!TextUtils.isEmpty(SignCache.getInstance().getSignOutTime())) {
//            offWorkImgBtn.setText("签出成功" + "\n" + simpleDate(SignCache.getInstance().getSignOutTime()));
//        } else {
//            offWorkImgBtn.setText("签出" + "\n");
//        }
//        /**
//         * 缓存数据
//         * */
//        //缓存时间，用户和具体打卡时间
//        String date = HcUtil.getDate(HcUtil.FORMAT_YEAR_MONTH_DAY, System.currentTimeMillis());
//        SettingHelper.setTodaySignTime(getActivity(), PREFERENCE_KEY_SIGN_TIME, date, SignCache.getInstance().getSignInTime(), SignCache.getInstance().getSignOutTime());
        String in = SettingHelper.getSigninTime(getActivity());
        String out = SettingHelper.getSignoutTime(getActivity());
        if (!TextUtils.isEmpty(in)) {
            goWorkImgBtn.setText("签入成功" + "\n" + simpleDate(in));
        } else {
            goWorkImgBtn.setText("签入" + "\n");
        }

        if (!TextUtils.isEmpty(out)) {
            offWorkImgBtn.setText("签出成功" + "\n" + simpleDate(out));
        } else {
            offWorkImgBtn.setText("签出" + "\n");
        }
    }

    public String simpleDate(String time) {
        String timeStr[] = time.split(":");
        String date = timeStr[0] + ":" + timeStr[1];
        return date;
    }

    @Override
    public void onPause() {
        SignCache.getInstance().removeObserver(this);
        handler.removeCallbacks(mTimeRefresher);
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.go_work_img_btn) {
            //上班签到
//            String signTimeStr = SettingHelper.getTodaySignTime(getActivity(), PREFERENCE_KEY_SIGN_TIME);
//            if (!TextUtils.isEmpty(signTimeStr)) {
//                String[] signTimes = signTimeStr.split("#");
//                if (!signTimes[0].equals(dateTime)) {
//                    goWorkImgBtn.setText("签入" + "\n");
//                    offWorkImgBtn.setText("签出" + "\n");
//                } else {
//                    String[] accountTimes = signTimes[1].split(";");
//                    for (String accountTime : accountTimes) {
//                        String[] account = accountTime.split("_");
//                        if (SettingHelper.getAccount(getActivity()).equals(account[0])) {
//                            String[] time = account[1].split("&");
//                            if (!TextUtils.isEmpty(time[0]) && !"null".equals(time[0])) {
//                                if (!TextUtils.isEmpty(SignCache.getInstance().getSignInTime())) {
//                                    HcUtil.showToast(getActivity(), "今天您已经签到!");
//                                }
//                                return;
//                            } else if (TextUtils.isEmpty(SignCache.getInstance().getSignInTime())) {
//                                if (HcUtil.isNetWorkAvailable(getActivity())) {
//                                    //上班签到
//                                    uploadSignInfo(0);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
            //如果设备号与服务端传出的设备号相等或为空，则可以进行打卡操作，否则不可进行打卡操作
            if (isEqualsImei(imei, myImei)) {
                String in = SettingHelper.getSigninTime(getActivity());
                if (!TextUtils.isEmpty(in)) {
                    HcUtil.showToast(getActivity(), "今天您已经签到!");
                    return;
                }
                if (HcUtil.isNetWorkAvailable(getActivity()))
                    uploadSignInfo(0);
            } else {
                showImeiDialog();
            }
        } else if (i == R.id.off_work_img_btn) {
            if (isEqualsImei(imei, myImei)) {
                if (HcUtil.isNetWorkAvailable(getActivity())) {
                    //下班签到
                    uploadSignInfo(1);
                }
            } else {
                showImeiDialog();
            }

        } else if (i == R.id.sign_details)

        {
            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            HcDialog.showProgressDialog(getActivity(), R.string.dialog_title_get_data);
            HcHttpRequest.getRequest().sendWorkDetailNowCommand(formatter.format(curDate), this);
//            Intent intent = new Intent(getActivity(), MonthCalendarActivity.class);
//            startActivity(intent);
//            String url = HcUtil.getScheme() + DETAIL_URL;
//            Intent htmlActivity = new Intent(getActivity(), HtmlActivity.class);
//            htmlActivity.putExtra("title", "签到详情");
//            htmlActivity.putExtra("url", url);
//            startActivity(htmlActivity);
        }
    }

    private void uploadSignInfo(int mSignFlag) {
        // 上传签到信息到网络

        Loction loction = new Loction();
        loction.setmSignFlag(mSignFlag + "");
        loction.setmSignType("1");
        loction.setmType("0");
        LocalSignRequest request = new LocalSignRequest(loction.getmSignType(), loction.getmSignFlag(), String.valueOf(SignLoctionUtils.getLng()), String.valueOf(SignLoctionUtils.getLat()), SignLoctionUtils.getAddress());
        if (mResponse == null) {
            mResponse = new LocalSignResponse();
        }
        HcDialog.showProgressDialog(getActivity(), R.string.dialog_title_post_data);
        request.sendRequestCommand(RequestCategory.SIGN, mResponse, false);
//        HcHttpRequest.getRequest().sendSignCommand(loction.getmSignType(), loction.getmSignFlag(),
//                String.valueOf(SignLoctionUtils.getLng()), String.valueOf(SignLoctionUtils.getLat()), SignLoctionUtils.getAddress(), this);
    }

    @Override
    public void notify(Object data, RequestCategory request, ResponseCategory category) {
        HcDialog.deleteProgressDialog();
        if (request != null) {
            switch (request) {
                case WORKDETAIL:
                    if (data != null) {
                        switch (category) {
                            case SUCCESS:
                                HcLog.D(TAG + " ontify SUCCESS callback = " + data.toString());
                                JSONObject object = null;
                                JSONObject body;
                                JSONArray userList = null;
                                try {
                                    object = new JSONObject(data.toString());
                                    body = object.optJSONObject("body");
                                    userList = body.optJSONArray("userList");
                                    if (userList != null && userList.length() > 0) {
                                        if (getActivity() != null) {
                                            Intent intent = new Intent();
                                            intent.putExtra("data", data.toString());
                                            intent.setClass(getActivity(), WorkDetailActivity.class);
                                            startActivity(intent);
                                        }
                                    } else {
                                        if (getActivity() != null) {
                                            Intent intent = new Intent(getActivity(), MonthCalendarActivity.class);
                                            startActivity(intent);
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                break;
                            case REQUEST_FAILED:
                                ResponseCodeInfo info = (ResponseCodeInfo) data;
                                if (HcHttpRequest.REQUEST_ACCOUT_EXCLUDED == info.getCode()
                                        || HcHttpRequest.REQUEST_TOKEN_FAILED == info.getCode()
                                        ) {
                                    HcUtil.reLogining(info.getBodyData(), getActivity(), info.getMsg());
                                } else {
                                    HcUtil.showToast(getActivity(), info.getMsg());
                                }
                                break;
                            case NETWORK_ERROR:
                                HcUtil.toastNetworkError(getActivity());
                                break;
                            case SYSTEM_ERROR:
                                HcUtil.toastSystemError(getActivity(), data);
                                break;
                            case DATA_ERROR:
                                HcUtil.toastDataError(getActivity());
                                break;
                            case SESSION_TIMEOUT:
                                HcUtil.toastTimeOut(getActivity());
                                break;
                            default:
                                break;
                        }
                    } else {
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

    }

    private class LocalSignRequest extends AbstractHttpRequest {

        private static final String TAG = LocalSignFragment.TAG + "$LocalSignRequest";
        Map<String, String> httpparams = new HashMap<String, String>();

        public LocalSignRequest(String singType, String flag, String longitude,
                                String latitude, String address) {
            httpparams.put("signType", singType);
            httpparams.put("signFlag", flag);
            httpparams.put("addressLongitude", longitude);
            httpparams.put("addressLatitude", latitude);
            httpparams.put("address", address);
            httpparams.put("account", HttpRequestQueue.URLEncode(SettingHelper.getAccount(getActivity())));
        }

        @Override
        public String getParameterUrl() {
            String stuxx = "";
            try {
                stuxx = HcUtil.getGetParams(HcUtil.mapToList(httpparams));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return stuxx;
        }

        public void parseJson(String data) {
            HcLog.D(TAG + " it is in parseJson data = " + data);
            try {
                JSONObject object = new JSONObject(data);
                if (object != null) {
                    int status = object.getInt(HttpRequestQueue.STATUS);
                    if (status == /*REQUEST_SUCCESS*/610 || status == 620) {
//                        object = object.getJSONObject(HttpRequestQueue.BODY);
                        onSuccess(object.toString());
                    } else if (status == HcHttpRequest.REQUEST_ACCOUT_EXCLUDED || status == HcHttpRequest.REQUEST_TOKEN_FAILED) {
                        //帐号在其他地方登录，被剔除或TOKEN失效
                        String msg = "";
                        if (HcUtil.hasValue(object, HttpRequestQueue.MSG)) {
                            msg = object.getString(HttpRequestQueue.MSG);
                        }
                        onAccountExcluded(object.toString(), msg);
                    } else { // 除了成功需要处理，其他的返回code都toast服务端返回的msg
                        String msg = "";
                        if (HcUtil.hasValue(object, HttpRequestQueue.MSG)) {
                            msg = object.getString(HttpRequestQueue.MSG);
                        }
                        onRequestFailed(status, msg);
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
                HcLog.D(TAG + " #parseJson error e = " + e);
                onParseDataError();
            }
        }

        @Override
        public String getRequestMethod() {
            return "sign";
        }
    }

    private class LocalSignResponse extends AbstractHttpResponse {

        private static final String TAG = LocalSignFragment.TAG + "$SignResponse";

        public LocalSignResponse() {
        }

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            HcDialog.deleteProgressDialog();
            String signInTime = null, signOutTime = null;
            if (data != null && data instanceof String) {
                try {
                    JSONObject object = new JSONObject((String) data);
                    if (HcUtil.hasValue(object, "msg")) {
                        final String msg = object.getString("msg");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                HcUtil.showToast(getActivity(), msg);
                            }
                        });

                    }
                    if (HcUtil.hasValue(object, "body")) {
                        JSONObject objectBody = object.getJSONObject("body");
                        if (HcUtil.hasValue(objectBody, "signInTime")) {
                            signInTime = objectBody.getString("signInTime");
                            if (!TextUtils.isEmpty(signInTime)) {
                                final String finalSignInTime = signInTime;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        goWorkImgBtn.setText("签入成功" + "\n" + simpleDate(finalSignInTime));
                                        //如果迟到，弹出对话框;如果不迟到，则不弹出;
                                        String workInTime = SignCache.getInstance().getWorkInTime();
                                        boolean beLate = beLate(finalSignInTime, SignCache.getInstance().getWorkInTime());
                                        HcLog.D("signInTime=" + finalSignInTime + " ," + "workInTime=" + SignCache.getInstance().getWorkInTime() + " ," + "beLate=" + beLate);
                                        if (beLate) {
                                            //迟到，弹出对话框，写明迟到原因，再提交
                                            final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                                                    .create();
                                            dialog.setCancelable(false);
                                            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                            View mDialogView = inflater.inflate(R.layout.signin_belate_dialog, null);
                                            dialog.setView(mDialogView);
                                            dialog.show();
                                            dialog.getWindow().setContentView(R.layout.signin_belate_dialog);
                                            final EditText signinEdit = (EditText) dialog.getWindow()
                                                    .findViewById(R.id.signin_edit);
                                            Button unagree_dialog = (Button) dialog.getWindow()
                                                    .findViewById(R.id.unagree_dialog);
                                            Button agree_dialog = (Button) dialog.getWindow().findViewById(
                                                    R.id.agree_dialog);
                                            unagree_dialog.setOnClickListener(new View.OnClickListener() {

                                                @Override
                                                public void onClick(View view) {
                                                    dialog.dismiss();
                                                }
                                            });
                                            agree_dialog.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    //新建文件夹，并将目录结构传给服务器
                                                    dialog.dismiss();
                                                    final String signinEditStr = signinEdit.getText().toString().trim();
                                                    if (!TextUtils.isEmpty(signinEditStr)) {
//                                                        String mSignInTime = getFormat(finalSignInTime);
                                                        //向服务器发送请求，提交迟到原因
                                                        BeLateRequest request = new BeLateRequest("", "迟到原因:" + signinEditStr);
                                                        BeLateResponse response = new BeLateResponse();
                                                        HcDialog.showProgressDialog(getActivity(), "正在提交数据...");
                                                        request.sendRequestCommand(RequestCategory.NONE, response, false);
                                                    }
                                                }
                                            });
                                        } else {
                                            //do nothing
                                        }
                                    }
                                });

                            }
                            SettingHelper.setSigninTime(getActivity(), signInTime);
                        }
                        if (HcUtil.hasValue(objectBody, "signOutTime")) {
                            signOutTime = objectBody.getString("signOutTime");
                            if (!TextUtils.isEmpty(signOutTime)) {
                                final String finalSignOutTime = signOutTime;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        offWorkImgBtn.setText("签出成功" + "\n" + simpleDate(finalSignOutTime));
                                        boolean beOutLate = beOutLate(finalSignOutTime, SignCache.getInstance().getWorkOutTime());
                                        HcLog.D("signOutTime=" + finalSignOutTime + " ," + "workOutTime=" + SignCache.getInstance().getWorkOutTime() + " ," + "beOutLate=" + beOutLate);
                                        if (!beOutLate) {
                                            //早退，弹出对话框，写明迟到原因，再提交
                                            final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                                                    .create();
                                            dialog.setCancelable(false);
                                            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                            View mDialogView = inflater.inflate(R.layout.signin_beleave_dialog, null);
                                            dialog.setView(mDialogView);
                                            dialog.show();
                                            dialog.getWindow().setContentView(R.layout.signin_beleave_dialog);
                                            final EditText signinEdit = (EditText) dialog.getWindow()
                                                    .findViewById(R.id.signin_edit);
                                            Button unagree_dialog = (Button) dialog.getWindow()
                                                    .findViewById(R.id.unagree_dialog);
                                            Button agree_dialog = (Button) dialog.getWindow().findViewById(
                                                    R.id.agree_dialog);
                                            unagree_dialog.setOnClickListener(new View.OnClickListener() {

                                                @Override
                                                public void onClick(View view) {
                                                    dialog.dismiss();
                                                }
                                            });
                                            agree_dialog.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    //新建文件夹，并将目录结构传给服务器
                                                    dialog.dismiss();
                                                    final String signinEditStr = signinEdit.getText().toString().trim();
                                                    if (!TextUtils.isEmpty(signinEditStr)) {
                                                        //向服务器发送请求，提交早退原因

                                                        BeLateRequest request = new BeLateRequest("", "早退原因:" + signinEditStr);
                                                        BeLateResponse response = new BeLateResponse();
                                                        HcDialog.showProgressDialog(getActivity(), "正在提交数据...");
                                                        request.sendRequestCommand(RequestCategory.NONE, response, false);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                            SettingHelper.setSignoutTime(getActivity(), signOutTime);
                        }

                    }
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }

        private boolean beLate(String signInTime, String workInTime) {
            DateFormat df = new SimpleDateFormat("HH:mm:ss");//创建日期转换对象HH:mm:ss为时分秒，年月日为yyyy-MM-dd
            try {
                Date dt1 = df.parse(signInTime);//将字符串转换为date类型
                Date dt2 = df.parse(workInTime);
                Calendar cal = Calendar.getInstance();
                cal.setTime(dt2);
                cal.add(Calendar.MINUTE, 1);
                dt2 = cal.getTime();
                if (dt1.getTime() > dt2.getTime()) {
                    return true;
                } else {
                    return false;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return true;
        }
        private boolean beOutLate(String signInTime, String workInTime) {
            DateFormat df = new SimpleDateFormat("HH:mm:ss");//创建日期转换对象HH:mm:ss为时分秒，年月日为yyyy-MM-dd
            try {
                Date dt1 = df.parse(signInTime);//将字符串转换为date类型
                Date dt2 = df.parse(workInTime);
                Calendar cal = Calendar.getInstance();
                cal.setTime(dt2);
                dt2 = cal.getTime();
                if (dt1.getTime() > dt2.getTime()) {
                    return true;
                } else {
                    return false;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
            HcLog.D(TAG + " #notifyRequestMd5Url md5Url = " + md5Url + " request = " + request);
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            //账号超时进行的操做
            HcDialog.deleteProgressDialog();
            HcUtil.reLogining(data, getActivity(), msg);
        }
    }

    @Override
    public void updateData(HcSubject subject, Object data, RequestCategory request, ResponseCategory response) {
        if (subject instanceof SignCache) {
            String in = SettingHelper.getSigninTime(getActivity());
            String out = SettingHelper.getSignoutTime(getActivity());
            if (!TextUtils.isEmpty(in)) {
                goWorkImgBtn.setText("签入成功" + "\n" + simpleDate(in));
            } else {
                goWorkImgBtn.setText("签入" + "\n");
            }

            if (!TextUtils.isEmpty(out)) {
                offWorkImgBtn.setText("签出成功" + "\n" + simpleDate(out));
            } else {
                offWorkImgBtn.setText("签出" + "\n");
            }
        }

    }

    public class BeLateRequest extends AbstractHttpRequest {
        Map<String, String> httpparams = new HashMap<String, String>();

        public BeLateRequest(String signDate, String remark) {
            httpparams.put("signDate", signDate);
            httpparams.put("remark", remark);
        }

        @Override
        public String getRequestMethod() {
            return "addsignremark";
        }

        @Override
        public String getParameterUrl() {
            String stuxx = "";
            try {
                stuxx = HcUtil.getGetParams(HcUtil.mapToList(httpparams));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return stuxx;
        }
    }

    private class BeLateResponse extends AbstractHttpResponse {
        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            HcDialog.deleteProgressDialog();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    HcUtil.showToast(getActivity(), "数据提交成功!");
                }
            });
        }

        @Override
        public String getTag() {
            return TAG;
        }

        /**
         * 账号超时进行的操作
         */
        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcDialog.deleteProgressDialog();
            HcUtil.reLogining(data, getActivity(), msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
            HcLog.D(TAG + " #notifyRequestMd5Url md5Url = " + md5Url + " request = " + request);
        }

    }

    private boolean isEqualsImei(String imei, String myImei) {
        HcLog.D("imei = " + imei);
        if (imei != null) {
            if (imei.equals(myImei)) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private void showImeiDialog() {
        //弹出提示框，提示用户手机设备号出错
        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .create();
        dialog.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mDialogView = inflater.inflate(R.layout.sign_imei_dialog, null);
        dialog.setView(mDialogView);
        dialog.show();
        dialog.getWindow().setContentView(R.layout.sign_imei_dialog);
        Button agree_dialog = (Button) dialog.getWindow().findViewById(
                R.id.agree_dialog);


        agree_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }
}
