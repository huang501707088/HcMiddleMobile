package com.android.hcframe.internalservice.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.http.ResponseCodeInfo;
import com.android.hcframe.internalservice.sign.MonthCalendarActivity;
import com.android.hcframe.internalservice.sign.SignCache;
import com.android.hcframe.internalservice.sign.SignLoctionUtils;
import com.android.hcframe.internalservice.sign.SignSubmitActivity;
import com.android.hcframe.internalservice.sign.SignSubmitActivity2;
import com.android.hcframe.internalservice.sign.WorkDetailActivity;
import com.android.hcframe.internalservice.signin.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Administrator on 2016/4/29 0029.
 */
public class RemoteSignFragment extends Fragment implements
        View.OnClickListener, IHttpResponse {
    private String TAG = "RemoteSignFragment";

    private View remoteSignView;
    private Button workImgBtn;
    private TextView signDetails;
    //自动打卡
    private TextView legText;
    private TextView localDateText;
    private TextView localWeekText;
    //小时钟
    private static final String DATE_FORMAT = "%02d:%02d";
    private static final int REFRESH_DELAY = 500;

    private static final String DETAIL_URL = "/fuiem/p/attend_list";

    private final Handler handler = new Handler();
    private final Runnable mTimeRefresher = new Runnable() {

        @Override
        public void run() {
            final Date d = new Date();
            legText.setText(String.format(DATE_FORMAT, d.getHours(),
                    d.getMinutes(), d.getSeconds()));
            Calendar c = Calendar.getInstance();
            c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
            String mYear = String.valueOf(c.get(Calendar.YEAR));// 获取当前月份
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


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        remoteSignView = inflater.inflate(R.layout.remote_sign_home_layout, container, false);//关联布局文件

        workImgBtn = (Button) remoteSignView.findViewById(R.id.sign_work_img_btn);
        legText = (TextView) remoteSignView.findViewById(R.id.time);
        localDateText = (TextView) remoteSignView.findViewById(R.id.date);
        localWeekText = (TextView) remoteSignView.findViewById(R.id.week);
        signDetails = (TextView) remoteSignView.findViewById(R.id.sign_details);
        workImgBtn.setOnClickListener(this);
        signDetails.setOnClickListener(this);
        return remoteSignView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(mTimeRefresher);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(mTimeRefresher);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.sign_work_img_btn) {

            //如果设备号与服务端传出的设备号相等或为空，则可以进行打卡操作，否则不可进行打卡操作
            String imei = SignCache.getInstance().getImei();
            String myImei = HcUtil.getIMEI(getActivity());
            HcLog.D("imei = " + imei + "," + "myImei = " + myImei);
            if (isEqualsImei(imei, myImei)) {
                startSignActivity();
            } else {
                showImeiDialog();
            }
        } else if (i == R.id.sign_details) {
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
//            Intent intent=new Intent();
//            intent.setClass(getActivity(), WorkDetailActivity.class);
//            String url = HcUtil.getScheme() + DETAIL_URL;
//            Intent htmlActivity = new Intent(getActivity(), HtmlActivity.class);
//            htmlActivity.putExtra("title", "签到详情");
//            htmlActivity.putExtra("url", url);
//            startActivity(intent);
        }
    }


    private void startSignActivity() {
        if (TextUtils.isEmpty(SignLoctionUtils.getAddress())) {
            HcUtil.showToast(getActivity(), "正在定位...");
            return;
        } else {
            Intent intent = new Intent(getActivity(), SignSubmitActivity2.class);
            startActivity(intent);
        }
    }

    /**
     * 详情
     *
     * @param data     返回的数据
     * @param request  请求的类型
     * @param category 返回的类型
     */
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
                                        HcLog.D("getActivity()=================================================" + getActivity());
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

