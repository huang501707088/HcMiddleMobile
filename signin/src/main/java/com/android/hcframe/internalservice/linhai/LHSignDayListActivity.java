package com.android.hcframe.internalservice.linhai;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.internalservice.signin.R;
import com.android.hcframe.sql.SettingHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-7-1 11:20.
 */
public class LHSignDayListActivity extends HcBaseActivity {

    private static final String TAG = "LHSignDayListActivity";

    private TopBarView mTopBarView;

    private TextView mDate;

    private ImageView mLeftBtn;

    private ImageView mRightBtn;

    private ListView mListView;

    private LHSignDayAdapter mAdapter;

    /**
     * key:请求的时间,格式为 2016-07-05
     * value:人员的签到列表
     */
    private Map<String, List<LHSignDayInfo>> mDaySignInfos = new HashMap<String, List<LHSignDayInfo>>();

    /**
     * 迟到/考勤异常
     */
    public static final String SIGN_TYPE_LATE = "2";
    /**
     * 外勤
     */
    public static final String SIGN_TYPE_FIELD = "3";
    /**
     * 请假
     */
    public static final String SIGN_TYPE_LEAVE = "5";
    /**
     * 未考勤
     */
    public static final String SIGN_TYPE_UNSIGN = "4";
    /**
     * 已考勤,正常.
     */
    public static final String SIGN_TYPE_SUCCESS = "1";

    private SignRecordResponse mResponse;

    private Handler mHandler = new Handler();

    private static final long DAY = 24 * 60 * 60 * 1000;

    private String mTitleDate; // 当前显示的时间标题
    private String mRequestDate; // 当前去服务端请求的时间

    private List<LHSignDayInfo> mInfos = new ArrayList<LHSignDayInfo>();

    /**
     * 今天的时间
     */
    private long mToday;
    /**
     * 当前显示的时间
     */
    private long mCurrentDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.linhai_activity_sign_daylist_layout);
        initViews();
        initData();
    }

    private void initViews() {
        mTopBarView = (TopBarView) findViewById(R.id.linhai_daylist_top_bar);
        mDate = (TextView) findViewById(R.id.linhai_daylist_date);
        mLeftBtn = (ImageView) findViewById(R.id.linhai_daylist_left_btn);
        mRightBtn = (ImageView) findViewById(R.id.linhai_daylist_right_btn);
        mListView = (ListView) findViewById(R.id.linhai_daylist_listview);

        mLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1.变更时间;2.判断是否是今天;3.今天就重新获取;4.不是直接读取缓存,有就有
                if (mRightBtn.getVisibility() != View.VISIBLE)
                    mRightBtn.setVisibility(View.VISIBLE);
                mCurrentDay -= DAY;
                setSignData();
            }
        });
        mRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentDay += DAY;
                if (mCurrentDay == mToday) {
                    if (mRightBtn.getVisibility() == View.VISIBLE) {
                        mRightBtn.setVisibility(View.GONE);
                    }
                }
                setSignData();
            }
        });
    }

    private void initData() {
        mTopBarView.setTitle(/*SettingHelper.getName(this)*/"考勤详情");
        if (mAdapter == null) {
            mAdapter = new LHSignDayAdapter(this, mInfos);
            mListView.setAdapter(mAdapter);
        }
        mRightBtn.setVisibility(View.GONE);
        mCurrentDay = mToday = System.currentTimeMillis();
        setSignData();
    }

    private class SignRecordRequest extends AbstractHttpRequest {

        private static final String TAG = LHSignDayListActivity.TAG + "$SignRecordRequest";

        private final String mDate;

        public SignRecordRequest(String date) {
            mDate = date;
        }

        @Override
        public String getParameterUrl() {
            return "?userId=" + SettingHelper.getUserId(LHSignDayListActivity.this) + "&searchDate="+mDate;
        }

        @Override
        public String getRequestMethod() {
            return "lhgetsignlist";
        }
    }

    private class SignRecordResponse extends AbstractHttpResponse {

        private static final String TAG = "SignRecordResponse";

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcDialog.deleteProgressDialog();
            try {
                JSONObject object = new JSONObject((String) data);
                String date = null;
                if (HcUtil.hasValue(object, "searchDate")) {
                    date = object.getString("searchDate");
                }
                HcLog.D(TAG + " #onSuccess parse date = "+date);
                if (HcUtil.hasValue(object, "signList")) {
                    JSONArray array = object.getJSONArray("signList");
                    int size = array.length();
                    List<LHSignDayInfo> infos = new ArrayList<LHSignDayInfo>();
                    LHSignDayInfo info;
                    HcLog.D(TAG + " #onSuccess parse array length = "+size);
                    if (size > 0) {
                        for (int i = 0; i < size; i++) {
                        	info = new LHSignDayInfo();
                            object = array.getJSONObject(i);
                            info.setDate(date);
                            if (HcUtil.hasValue(object, "userId")) {
                                info.setUserId(object.getString("userId"));
                            }
                            if (HcUtil.hasValue(object, "realName")) {
                                info.setName(object.getString("realName"));
                            }
                            if (HcUtil.hasValue(object, "signType")) {
                                info.setType(object.getString("signType"));
                            }
                            if (HcUtil.hasValue(object, "signStatus")) {
                                info.setStatus(object.getString("signStatus"));
                            }
                            if (HcUtil.hasValue(object, "signInTime")) {
                                info.setSigninTime(object.getString("signInTime"));
                            }
                            if (HcUtil.hasValue(object, "signOutTime")) {
                                info.setSignoutTime(object.getString("signOutTime"));
                            }
                            if (HcUtil.hasValue(object, "inAddressName")) {
                                info.setSigninAddress(object.getString("inAddressName"));
                            }
                            if (HcUtil.hasValue(object, "outAddressName")) {
                                info.setSignoutAddress(object.getString("outAddressName"));
                            }
                            if (HcUtil.hasValue(object, "signInImg")) {
                                info.setShowSigninIcon("1".equals(object.getString("signInImg")));
                            }
                            if (HcUtil.hasValue(object, "signOutImg")) {
                                info.setShowSignoutIcon("1".equals(object.getString("signOutImg")));
                            }
                            infos.add(info);
                        }
                    }
                    mDaySignInfos.put(date, infos);
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setDatas();
                    }
                });
            } catch(JSONException e) {
                HcLog.D(TAG + " #onSuccess parse error e = "+e);
            }
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcDialog.deleteProgressDialog();
            mDaySignInfos.clear();
            mAdapter.notifyDataSetChanged();
            HcUtil.reLogining(data, LHSignDayListActivity.this, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

        }
    }

    private void setDatas() {

        mInfos.clear();
        List<LHSignDayInfo> infos = mDaySignInfos.get(mRequestDate);
        HcLog.D(TAG + " #setDatas info = "+infos + " RequestDate="+mRequestDate);
        if (infos != null && infos.size() > 0) {
            mInfos.addAll(infos);
        }
        if (mAdapter == null) {
            mAdapter = new LHSignDayAdapter(this, mInfos);
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }

    }

    private void setSignData() {
        mTitleDate = HcUtil.getDate(HcUtil.FORMAT_SIGN_YEAR_MONTH_DAY, mCurrentDay);
        mRequestDate = HcUtil.getDate("yyyy-MM-dd", mCurrentDay);
        mDate.setText(mTitleDate);
        List<LHSignDayInfo> infos = mDaySignInfos.get(mRequestDate);
        if (infos != null) {
            HcLog.D(TAG + " #setSignData RequestDate= "+mRequestDate + " infos = "+infos);
            mInfos.clear();
            mInfos.addAll(infos);
            // 直接显示
            if (mAdapter == null) {
                mAdapter = new LHSignDayAdapter(this, mInfos);
                mListView.setAdapter(mAdapter);
            } else {
                mAdapter.notifyDataSetChanged();
            }
            // 判断是否是当天,当天即使有数据也要重新获取
            if (mCurrentDay == mToday) {
                SignRecordRequest request = new SignRecordRequest(mRequestDate);
                if (mResponse == null) {
                    mResponse = new SignRecordResponse();
                }
                HcDialog.showProgressDialog(this, "正在获取数据...");
                request.sendRequestCommand(RequestCategory.NONE, mResponse, false);
            }
        } else { // 没有去获取过数据
            if (mAdapter != null) {
                mInfos.clear();
                mAdapter.notifyDataSetChanged();
            }
            SignRecordRequest request = new SignRecordRequest(mRequestDate);
            if (mResponse == null) {
                mResponse = new SignRecordResponse();
            }
            HcDialog.showProgressDialog(this, "正在获取数据...");
            request.sendRequestCommand(RequestCategory.NONE, mResponse, false);
        }
    }
}
