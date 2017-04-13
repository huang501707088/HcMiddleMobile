package com.android.hcframe.internalservice.sign;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.HttpRequestQueue;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.internalservice.signcls.SignListByMonth;
import com.android.hcframe.internalservice.signin.R;
import com.android.hcframe.sql.SettingHelper;
import com.android.hcframe.view.scroll.*;
import com.android.hcframe.view.scroll.ScrollLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonthCalendarActivity2 extends HcBaseActivity implements OnClickListener, CalendarView.OnItemClickListener , ScrollLayout.OnViewChangeListener{

    private static final String TAG = "MonthCalendarActivity2";

    private TopBarView mTopBarView;

    /**
     * key:日期(2017.03.08)
     */
    private Map<String, MonthCalendarInfo> mInfos = new HashMap<String, MonthCalendarInfo>();

    private CalendarView calendar;

    private Handler mHandler = new Handler();

    private int mPosition;

    private String mCurrentDate;

    private long mCurrentTimeMillis;

    private ScrollLayout mScrollLayout;

    private View[] mContents = new View[3];

    private String mUserId;

    private String mName;

    private String account;

    private int year;

    private int month;

    private String searchDate;

    private MonthCalendarResponse mResponse;

    private List<SignListByMonth> signListByMonthList;

    private List<Integer> signInTimeList = new ArrayList<>();

    private List<Integer> signOutTimeList = new ArrayList<>();

    private List<Integer> signExceptionTimeList = new ArrayList<>();

    private List<Integer> signLeaveTimeList = new ArrayList<>();

    private List<Integer> signAskForLeaveList = new ArrayList<>();

    private MonthCalendarInfo mMonthCalendarInfo;

    private Handler calendarHandler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            Date date = null;
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-mm");
                date = format.parse(bundle.getString("searchDate"));

            } catch (ParseException e) {
                e.printStackTrace();
            }
            calendar.setCalendarData(date);
            signListByMonthList.clear();
            super.handleMessage(msg);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.month_carlendar_layout);
        initView();
        initData();
        initEvent();
    }

    private void initView() {
        //初始化view
        mTopBarView = (TopBarView) findViewById(R.id.center_top_bar);
        mScrollLayout = (ScrollLayout) findViewById(R.id.scrollLayout);
        mScrollLayout.setOnViewChangeListener(this);
        LayoutInflater inflater = getLayoutInflater();
        for (int i = 0; i < 3; i++) {
            mContents[i] = inflater.inflate(R.layout.month_calendar_pager, null);
        }
        for (View content : mContents) {
            mScrollLayout.addView(content);
        }
    }

    private void initData() {
        /**
         * 获取account数据
         * */
        String mAccount = getIntent().getStringExtra("account");
        mUserId = getIntent().getStringExtra("userId");
        mName = getIntent().getStringExtra("name");
        if (mName != null) {
            mTopBarView.setTitle(mName);
        } else {
            mTopBarView.setTitle("考勤签到");
        }
        if (!TextUtils.isEmpty(mAccount)) {
            account = mAccount;
        } else {
            account = HttpRequestQueue.URLEncode(SettingHelper.getAccount(this));
            mUserId = SettingHelper.getUserId(this);
        }
        signListByMonthList = new ArrayList<>();
        Calendar calendars = Calendar.getInstance();
        year = calendars.get(Calendar.YEAR);
        month = calendars.get(Calendar.MONTH) + 1;
        searchDate = CorrectDate(String.valueOf(year), String.valueOf(month));
        HcLog.D("initData#searchDate=" + searchDate);
        signExceptionTimeList.clear();
        signLeaveTimeList.clear();
        signOutTimeList.clear();
        signInTimeList.clear();
        signAskForLeaveList.clear();
        ListView content;
        for (int i = 0; i < 3; i++) {
            mMonthCalendarInfo = new MonthCalendarInfo();
            calendar = (CalendarView) mContents[i].findViewById(R.id.calendar);
        }
        mPosition = 1;
        MonthCalendarRequest request = new MonthCalendarRequest(searchDate, account);
        if (mResponse == null) {
            mResponse = new MonthCalendarResponse();
        }
        HcDialog.showProgressDialog(this, R.string.dialog_title_get_data);
        request.sendRequestCommand(RequestCategory.SIGNLISTBYMONTH, mResponse, false);
    }

    private void initEvent() {
        calendar.setOnItemClickListener(this);
    }

    @Override
    public void OnViewChange(int position) {

    }

    @Override
    public void onComputeScroll(int position) {

    }

    private class MonthCalendarRequest extends AbstractHttpRequest {
        Map<String, String> httpparams = new HashMap<String, String>();

        public MonthCalendarRequest(String searchDate, String account) {
            httpparams.put("searchDate", searchDate);
            httpparams.put("account", account);
            httpparams.put("userId", mUserId);
        }

        @Override
        public String getParameterUrl() {
            String signListByMonthUrl = "";
            try {
                signListByMonthUrl = HcUtil.getGetParams(HcUtil.mapToList(httpparams));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return signListByMonthUrl;
        }

        @Override
        public String getRequestMethod() {
            return "getsignlistByMonth";
        }

    }
    private class MonthCalendarResponse extends AbstractHttpResponse {

        public MonthCalendarResponse() {
        }

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            MonthCalendarInfo  calendarInfo = new MonthCalendarInfo();
            List<SignListByMonth> signByMonthListCopy = new ArrayList<>();
            String searchDate = null;
            HcDialog.deleteProgressDialog();
            try {
                JSONObject object = new JSONObject((String) data);
                if (HcUtil.hasValue(object, "searchDate")) {
                    searchDate = object.getString("searchDate");
                }
                JSONArray jsonBodyArray = object.getJSONArray("signList");
                for (int i = 0; i < jsonBodyArray.length(); i++) {
                    SignListByMonth signListByMonth = new SignListByMonth();
                    JSONObject signListObj = jsonBodyArray.getJSONObject(i);
                    String signDate = signListObj.getString("signDate");
                    signListByMonth.setSignDate(signDate);
                    String signStatus = signListObj.getString("signStatus");
                    signListByMonth.setSignStatus(signStatus);
                    String userId = signListObj.getString("userId");
                    signListByMonth.setUserId(userId);
                    calendarInfo.addInfo(searchDate,signListByMonth);
                    signByMonthListCopy.add(signListByMonth);
                }

            } catch (JSONException e) {
                HcLog.D(TAG + " #onSuccess parseJson error =" + e);
            }
            DateToCalender(signByMonthListCopy);
        }
        public void DateToCalender(List<SignListByMonth> signListByMonthList) {
            for (int i = 0; i < signListByMonthList.size(); i++) {
                if ("1".equals(signListByMonthList.get(i).getSignStatus())) {
                    signInTimeList.add(Integer.valueOf(signListByMonthList.get(i).getSignDate().substring(signListByMonthList.get(i).getSignDate().length() - 2, signListByMonthList.get(i).getSignDate().length())));
                } else if ("2".equals(signListByMonthList.get(i).getSignStatus())) {
                    signExceptionTimeList.add(Integer.valueOf(signListByMonthList.get(i).getSignDate().substring(signListByMonthList.get(i).getSignDate().length() - 2, signListByMonthList.get(i).getSignDate().length())));
                } else if ("3".equals(signListByMonthList.get(i).getSignStatus())) {
                    signOutTimeList.add(Integer.valueOf(signListByMonthList.get(i).getSignDate().substring(signListByMonthList.get(i).getSignDate().length() - 2, signListByMonthList.get(i).getSignDate().length())));
                } else if ("4".equals(signListByMonthList.get(i).getSignStatus())) {
                    signLeaveTimeList.add(Integer.valueOf(signListByMonthList.get(i).getSignDate().substring(signListByMonthList.get(i).getSignDate().length() - 2, signListByMonthList.get(i).getSignDate().length())));
                } else if ("5".equals(signListByMonthList.get(i).getSignStatus())) {
                    signAskForLeaveList.add(Integer.valueOf(signListByMonthList.get(i).getSignDate().substring(signListByMonthList.get(i).getSignDate().length() - 2, signListByMonthList.get(i).getSignDate().length())));
                }
            }
            calendar.setSignWrongList(signExceptionTimeList);
            calendar.setSignOutList(signOutTimeList);
            calendar.setSignInList(signInTimeList);
            calendar.setSignLeaveList(signLeaveTimeList);
            calendar.setSignAskForLeaveList(signAskForLeaveList);
            final String finalSearchDate = searchDate;
            Message message = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putString("searchDate", finalSearchDate);
            message.setData(bundle);
            calendarHandler.sendMessage(message);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
            HcLog.D(TAG + " #notifyRequestMd5Url md5Url = " + md5Url + " request = " + request);
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcDialog.deleteProgressDialog();
//            mSignListByMonthInfos.clear();
//            HcUtil.reLogining(data, MonthCalendarActivity.this, msg);
        }
    }
    public String CorrectDate(String year, String month) {
        String searchDate = "";
        StringBuilder sb = new StringBuilder();
        if (Integer.valueOf(month) < 10) {
            sb.append(year);
            sb.append("-0");
            sb.append(month);
            searchDate = sb.toString();
        } else {
            sb.append(year);
            sb.append("-");
            sb.append(month);
            searchDate = sb.toString();
        }
        return searchDate;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public void OnItemClick(Date downDate) {

    }
}
