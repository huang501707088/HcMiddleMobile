package com.android.hcframe.internalservice.sign;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MonthCalendarActivity extends HcBaseActivity implements OnClickListener, OnViewChangeListener, CalendarView.OnItemClickListener {
    private static final String TAG = "MonthCalendarActivity";
    private MonthCalendarResponse mResponse;
    private Map<String, List<SignListByMonth>> mSignListByMonthInfos = new HashMap<String, List<SignListByMonth>>();
    /**
     * 横向滑动
     */
    private ScrollLayout mScrollLayout;
    private int count;
    /**
     * 自定义头部
     */
    private TopBarView mTopBarView;
    private CalendarView calendaLeft;
    private CalendarView calendar;
    private CalendarView calendaRight;
    private ImageButton calendarLeft;
    private TextView calendarCenter;
    private ImageButton calendarRight;
    private String lastSearchDate;
    private String searchDate;
    private String yearAndmonth;
    private String account;
    private int year;
    private int month;
    private List<SignListByMonth> signListByMonthList;
    List<Integer> signInTimeList = new ArrayList<>();
    List<Integer> signOutTimeList = new ArrayList<>();
    List<Integer> signExceptionTimeList = new ArrayList<>();
    List<Integer> signLeaveTimeList = new ArrayList<>();
    List<Integer> signAskForLeaveList = new ArrayList<>();
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

    private String mUserId;
    private String mName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.carlendar_layout);
        initView();
        initData();
        //获取日历中年月 ya[0]为年，ya[1]为月（格式大家可以自行在日历控件中改）
        yearAndmonth = calendar.getYearAndmonth();
        String[] ya = yearAndmonth.split("-");
        calendarCenter.setText(ya[0] + "年" + ya[1] + "月");
    }

    private void initView() {
        mScrollLayout = (ScrollLayout) findViewById(R.id.scrollLayout);
        count = mScrollLayout.getChildCount();
        HcLog.D("MonthCalendarActivity#count=" + count);
        calendarLeft = (ImageButton) findViewById(R.id.calendarLeft);
        calendarCenter = (TextView) findViewById(R.id.calendarCenter);
        calendarRight = (ImageButton) findViewById(R.id.calendarRight);
        calendar = (CalendarView) findViewById(R.id.calendar);
        calendaLeft = (CalendarView) findViewById(R.id.calendar_left);
        calendaRight = (CalendarView) findViewById(R.id.calendar_right);
        mTopBarView = (TopBarView) findViewById(R.id.center_top_bar);
        calendaLeft.setOnItemClickListener(this);
        calendar.setOnItemClickListener(this);
        calendaRight.setOnItemClickListener(this);
        calendarLeft.setOnClickListener(this);
        calendarRight.setOnClickListener(this);
        mScrollLayout.SetOnViewChangeListener(this);
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
        }else{
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
        HcLog.D("onCreate#yearAndmonth=" + yearAndmonth);
        searchDate = CorrectDate(String.valueOf(year), String.valueOf(month));
        HcLog.D("initData#searchDate=" + searchDate);
        signExceptionTimeList.clear();
        signLeaveTimeList.clear();
        signOutTimeList.clear();
        signInTimeList.clear();
        signAskForLeaveList.clear();
        MonthCalendarRequest request = new MonthCalendarRequest(searchDate, account);
        if (mResponse == null) {
            mResponse = new MonthCalendarResponse();
        }
        HcDialog.showProgressDialog(this, R.string.dialog_title_get_data);
        request.sendRequestCommand(RequestCategory.SIGNLISTBYMONTH, mResponse, false);
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.calendarLeft) {
            signInTimeList.clear();
            signOutTimeList.clear();
            signLeaveTimeList.clear();
            signAskForLeaveList.clear();
            signExceptionTimeList.clear();
            String leftYearAndmonth;
            View rightView = mScrollLayout.getChildAt(2);
            mScrollLayout.removeView(rightView);
            mScrollLayout.addView(rightView, 0);
            calendaLeft = (CalendarView) mScrollLayout.getChildAt(0);
            calendar = (CalendarView) mScrollLayout.getChildAt(1);
            calendaRight = (CalendarView) mScrollLayout.getChildAt(2);
            Date curDate = StrToDate(yearAndmonth);
            leftYearAndmonth = calendar.clickLeftMonth(curDate);
            yearAndmonth = leftYearAndmonth;
            String[] ya = leftYearAndmonth.split("-");
            String searchLeftDate = CorrectDate(ya[0], ya[1]);
            calendarCenter.setText(ya[0] + "年" + ya[1] + "月");
            //左滑获取存储在Map中的list数据
            List<SignListByMonth> signListByMonthList = mSignListByMonthInfos.get(searchLeftDate);
            if (signListByMonthList != null && signListByMonthList.size() > 0) {
                DateToCalender(signListByMonthList);
            } else {
                MonthCalendarRequest request = new MonthCalendarRequest(searchLeftDate, account);
                if (mResponse == null) {
                    mResponse = new MonthCalendarResponse();
                }
                HcDialog.showProgressDialog(this, R.string.dialog_title_get_data);
                request.sendRequestCommand(RequestCategory.SIGNLISTBYMONTH, mResponse, false);
            }
        } else if (i == R.id.calendarRight) {
            signInTimeList.clear();
            signOutTimeList.clear();
            signLeaveTimeList.clear();
            signAskForLeaveList.clear();
            signExceptionTimeList.clear();
            //点击下一月
            String rightYearAndmonth;
            //向右滑动,删除最左测得view,在最右侧添加一个view
            View leftView = mScrollLayout.getChildAt(0);
            mScrollLayout.removeView(leftView);
            mScrollLayout.addView(leftView, 2);
            calendaLeft = (CalendarView) mScrollLayout.getChildAt(0);
            calendar = (CalendarView) mScrollLayout.getChildAt(1);
            calendaRight = (CalendarView) mScrollLayout.getChildAt(2);
            Date curDate = StrToDate(yearAndmonth);
            rightYearAndmonth = calendar.clickRightMonth(curDate);
            yearAndmonth = rightYearAndmonth;
            String[] ya = rightYearAndmonth.split("-");
            String searchRightDate = CorrectDate(ya[0], ya[1]);
            calendarCenter.setText(ya[0] + "年" + ya[1] + "月");
            //右滑获取存储在Map中的list数据
            List<SignListByMonth> signListByMonthList = mSignListByMonthInfos.get(searchRightDate);
            if (signListByMonthList != null && signListByMonthList.size() > 0) {
                DateToCalender(signListByMonthList);
            } else {
                MonthCalendarRequest request = new MonthCalendarRequest(searchRightDate, account);
                if (mResponse == null) {
                    mResponse = new MonthCalendarResponse();
                }
                HcDialog.showProgressDialog(this, R.string.dialog_title_get_data);
                request.sendRequestCommand(RequestCategory.SIGNLISTBYMONTH, mResponse, false);
            }
        }
    }

    @Override
    public void OnViewChange(int view) {
        HcLog.D("OnViewChange#view = " + view);
        signInTimeList.clear();
        signOutTimeList.clear();
        signLeaveTimeList.clear();
        signAskForLeaveList.clear();
        signExceptionTimeList.clear();
        if (view == 0) {
            //向左滑动,删除最右测得view,在最左侧添加一个view
            View rightView = mScrollLayout.getChildAt(2);
            mScrollLayout.removeView(rightView);
            mScrollLayout.addView(rightView, 0);
            calendaLeft = (CalendarView) mScrollLayout.getChildAt(0);
            calendar = (CalendarView) mScrollLayout.getChildAt(1);
            calendaRight = (CalendarView) mScrollLayout.getChildAt(2);
            String[] yearAndmonths = yearAndmonth.split("-");
            if (!"1".equals(yearAndmonths[1])) {
                int month = Integer.valueOf(yearAndmonths[1]);
                yearAndmonths[1] = String.valueOf(month - 1);
                yearAndmonth = yearAndmonths[0] + "-" + yearAndmonths[1];
            } else {
                yearAndmonths[1] = String.valueOf(12);
                int year = Integer.valueOf(yearAndmonths[0]) - 1;
                yearAndmonths[0] = String.valueOf(year);
                yearAndmonth = yearAndmonths[0] + "-" + yearAndmonths[1];
            }
            calendarCenter.setText(yearAndmonths[0] + "年" + yearAndmonths[1] + "月");
            Date curDate = StrToDate(yearAndmonth);
            calendar.moveToMonth(curDate);
            final String searchMoveLeftDate = CorrectDate(yearAndmonths[0], yearAndmonths[1]);
            //左滑获取存储在Map中的list数据
            List<SignListByMonth> signListByMonthList = mSignListByMonthInfos.get(searchMoveLeftDate);
            if (signListByMonthList != null && signListByMonthList.size() > 0) {
                DateToCalender(signListByMonthList);
            } else {
                MonthCalendarRequest request = new MonthCalendarRequest(searchMoveLeftDate, account);
                if (mResponse == null) {
                    mResponse = new MonthCalendarResponse();
                }
                HcDialog.showProgressDialog(this, R.string.dialog_title_get_data);
                request.sendRequestCommand(RequestCategory.SIGNLISTBYMONTH, mResponse, false);
            }
        } else if (view == 1) {
            String[] yearAndmonths = yearAndmonth.split("-");
            //向右滑动,删除最左测得view,在最右侧添加一个view
            View leftView = mScrollLayout.getChildAt(0);
            mScrollLayout.removeView(leftView);
            mScrollLayout.addView(leftView, 2);
            calendaLeft = (CalendarView) mScrollLayout.getChildAt(0);
            calendar = (CalendarView) mScrollLayout.getChildAt(1);
            calendaRight = (CalendarView) mScrollLayout.getChildAt(2);
            if (!"12".equals(yearAndmonths[1])) {
                int month = Integer.valueOf(yearAndmonths[1]);
                yearAndmonths[1] = String.valueOf(month + 1);
                yearAndmonth = yearAndmonths[0] + "-" + yearAndmonths[1];
            } else {
                yearAndmonths[1] = String.valueOf(1);
                int year = Integer.valueOf(yearAndmonths[0]) + 1;
                yearAndmonths[0] = String.valueOf(year);
                yearAndmonth = yearAndmonths[0] + "-" + yearAndmonths[1];
            }
            calendarCenter.setText(yearAndmonths[0] + "年" + yearAndmonths[1] + "月");
            Date curDate = StrToDate(yearAndmonth);
            calendar.moveToMonth(curDate);
            String searchMoveRightDate = CorrectDate(yearAndmonths[0], yearAndmonths[1]);
            List<SignListByMonth> signListByMonthList = mSignListByMonthInfos.get(searchMoveRightDate);
            //右滑获取存储在Map中的list数据
            if (signListByMonthList != null && signListByMonthList.size() > 0) {
                DateToCalender(signListByMonthList);
            } else {
                MonthCalendarRequest request = new MonthCalendarRequest(searchMoveRightDate, account);
                if (mResponse == null) {
                    mResponse = new MonthCalendarResponse();
                }
                HcDialog.showProgressDialog(this, R.string.dialog_title_get_data);
                request.sendRequestCommand(RequestCategory.SIGNLISTBYMONTH, mResponse, false);
            }
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

    public static Date StrToDate(String str) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        Date date = null;
        try {
            date = format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
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
        HcLog.D("MonthCalendarActivity#signListByMonthList= " + signListByMonthList + " " + calendaLeft.getYearAndmonth());
        final String finalSearchDate = searchDate;
        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString("searchDate", finalSearchDate);
        message.setData(bundle);
        calendarHandler.sendMessage(message);
    }

    @Override
    public void OnItemClick(Date downDate) {
        if (HcUtil.isNetWorkAvailable(this)) {
            HcLog.D("MonthCalendarActivity #OnItemClick Date downDate = " + downDate);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Intent intent = new Intent(this, SignListByDayActivity.class);
            intent.putExtra("signDay", format.format(downDate));
            intent.putExtra("account", mUserId);
            intent.putExtra("name", mName);
            startActivity(intent);
        }
    }

    private class MonthCalendarRequest extends AbstractHttpRequest {
        private static final String TAG = MonthCalendarActivity.TAG + "$MonthCalendarRequest";

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

        private static final String TAG = MonthCalendarActivity.TAG + "$MonthCalendarResponse";

        public MonthCalendarResponse() {
        }

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            List<SignListByMonth> signListByMonthListCopy = new ArrayList<>();
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
                    signListByMonthListCopy.add(signListByMonth);
                }
                //当数据为空时，new一个对象添加进去
                if (jsonBodyArray.length() == 0) {
                    SignListByMonth mSignListByMonth = new SignListByMonth();
                    signListByMonthListCopy.add(mSignListByMonth);
                    mSignListByMonthInfos.put(searchDate, signListByMonthListCopy);
                } else {
                    mSignListByMonthInfos.put(searchDate, signListByMonthListCopy);
                }
                HcLog.D(TAG + "signListByMonthListCopy.size() = " + signListByMonthListCopy.size() + "searchDate=" + searchDate);
            } catch (JSONException e) {
                HcLog.D(TAG + " #onSuccess parseJson error =" + e);
            }
            DateToCalender(signListByMonthListCopy);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
            HcLog.D(TAG + " #notifyRequestMd5Url md5Url = " + md5Url + " request = " + request);
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcDialog.deleteProgressDialog();
            mSignListByMonthInfos.clear();
            HcUtil.reLogining(data, MonthCalendarActivity.this, msg);
        }
    }
}
