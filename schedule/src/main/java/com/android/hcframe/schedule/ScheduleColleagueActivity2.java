package com.android.hcframe.schedule;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
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
import com.android.hcframe.schedule.data.ScheduleColleagueInfo;
import com.android.hcframe.view.datepicker.DatePickerDialog;
import com.android.hcframe.view.scroll.ScrollLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 17-2-24 14:45.
 */

public class ScheduleColleagueActivity2 extends HcBaseActivity implements DatePickerDialog.OnDateSetListener,
        ScrollLayout.OnViewChangeListener {

    private static String TAG = "ScheduleColleagueActivity2";

    private TopBarView mTopBarView;

    /** key:日期(2017.02.02) */
    private Map<String, ScheduleColleagueInfo> mInfos = new HashMap<String, ScheduleColleagueInfo>();

    private static final String DATA_FORMAT = "yyyy.MM.dd";
    private static final int DAY = 24 * 60 * 60 * 1000;

    private Calendar mCalendar;
    /**
     * 日期框
     */
    private DatePickerDialog mDatePickerDialog;

    public static final String DATEPICKER_TAG = "datepicker";

    private String mTaskUrl;

    private Handler mHandler = new Handler();

    private int mPosition;

    private String mCurrentDate;

    private long mCurrentTimeMillis;

    private ScrollLayout mScrollLayout;

    private View[] mContents = new View[3];

    private Map<View, ScheduleColleagueAdapter> mAdapterMap = new HashMap<View, ScheduleColleagueAdapter>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_activity_colleague);

        initViews();
        initData();
    }

    private void initViews() {
        mTopBarView = (TopBarView) findViewById(R.id.schedule_colleague_top_bar);
        mScrollLayout = (ScrollLayout) findViewById(R.id.schedule_colleague_scrollLayout);
        mScrollLayout.setOnViewChangeListener(this);
        LayoutInflater inflater = getLayoutInflater();
        for (int i = 0; i < 3; i++) {
        	mContents[i] = inflater.inflate(R.layout.schedule_colleague_pager, null);
        }
        for (View content : mContents) {
            mScrollLayout.addView(content);
        }
    }

    private void initData() {
        mCalendar = Calendar.getInstance();
        mTopBarView.setMenuBtnVisiable(View.VISIBLE);
        mTopBarView.setMenuSrc(R.drawable.schedule_colleague_calendar);
        mTopBarView.setMenuListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //显示日历
                mCalendar.setTimeInMillis(System.currentTimeMillis());
                if (mDatePickerDialog == null) {
                    mDatePickerDialog = new DatePickerDialog();
                }
                mDatePickerDialog.initialize(ScheduleColleagueActivity2.this, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH), false);
                //弹出日历框
                mDatePickerDialog.setVibrate(false);
                mDatePickerDialog.setYearRange(1985, 2028);
                mDatePickerDialog.setCloseOnSingleTapDay(false);
                mDatePickerDialog.show(getFragmentManager(), DATEPICKER_TAG);
            }
        });

        long time = System.currentTimeMillis();
        updateTitle(time);
        ScheduleColleagueInfo info;
        ScheduleColleagueAdapter adapter;
        ListView content;
        for (int i = 0; i < 3; i++) {
            info = new ScheduleColleagueInfo();
            content = (ListView) mContents[i].findViewById(R.id.schedule_colleague_pager_listview);
            mInfos.put(HcUtil.getDate(DATA_FORMAT, time + (i - 1) * DAY), info);
            adapter = new ScheduleColleagueAdapter(ScheduleColleagueActivity2.this, new ArrayList<List<ScheduleDetailsInfo>>(info.getScheduleColleagueInfos().values()));
            content.setAdapter(adapter);
            content.setEmptyView(mContents[i].findViewById(R.id.schedule_colleague_pager_empty_data));
            HcLog.D(TAG + " #initData adapter = "+adapter);
            mAdapterMap.put(mContents[i], adapter);
        }

        mPosition = 1;
        updateData(mPosition, mCurrentDate, true, mAdapterMap.get(mContents[mPosition]));
        updateData(mPosition + 1, HcUtil.getDate(DATA_FORMAT, time + DAY), false, mAdapterMap.get(mContents[mPosition + 1]));
        updateData(mPosition - 1, HcUtil.getDate(DATA_FORMAT, time - DAY), false, mAdapterMap.get(mContents[mPosition - 1]));

    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        HcLog.D("onDateSet year = " + year + "," + month + "," + day);
        mCalendar.set(year, month, day);
        updateTitle(mCalendar.getTimeInMillis());
        updateData(mPosition, mCurrentDate, true, mAdapterMap.get(mContents[mPosition]));
    }

    public class ScheduleColleagueRequest extends AbstractHttpRequest {

        private final String mDate;

        public ScheduleColleagueRequest(String searchDate) {
            mDate = searchDate;
        }

        @Override
        public String getRequestMethod() {
            return "getJuniorScheduleListByDate";
        }

        @Override
        public String getParameterUrl() {

            return "search_date=" + mDate;
        }
    }

    private class ScheduleColleagueResponse extends AbstractHttpResponse {

        private String mDate;

        private ScheduleColleagueAdapter mAdapter;

        public ScheduleColleagueResponse(String date, ScheduleColleagueAdapter adapter) {
            mDate = date;
            mAdapter = adapter;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);

            final ScheduleColleagueInfo colleagueInfo = new ScheduleColleagueInfo();
            try {
                JSONObject object = new JSONObject((String) data);
                JSONArray jsonDateArray = object.getJSONArray("list");
                int jsonDateLenth = jsonDateArray.length();
                ScheduleDetailsInfo scheduleInfo;
                String userId = "";
                String userName = "";
                if (jsonDateLenth > 0) {
                    for (int i = 0; i < jsonDateLenth; i++) {

                        object = jsonDateArray.getJSONObject(i);
                        if (HcUtil.hasValue(object, "juniorId")) {
                            userId = object.getString("juniorId");
                        }
                        if (HcUtil.hasValue(object, "juniorName")) {
                            userName = object.getString("juniorName");
                        }

                        if (HcUtil.hasValue(object, "scheduleList")) {
                            JSONArray jsonBodyArray = object.getJSONArray("scheduleList");
                            int jsonArrayLenth = jsonBodyArray.length();
                            if (jsonArrayLenth > 0) {
                                JSONObject scheduleInfoObj;
                                for (int j = 0; j < jsonArrayLenth; j++) {
                                    scheduleInfo = new ScheduleDetailsInfo();
                                    scheduleInfo.setmUserId(userId);
                                    scheduleInfo.setmName(userName);
                                    scheduleInfoObj = jsonBodyArray.getJSONObject(j);
                                    if (HcUtil.hasValue(scheduleInfoObj, "scheduleId")) {
                                        String scheduleId = scheduleInfoObj.getString("scheduleId");
                                        scheduleInfo.setId(scheduleId);
                                    }
                                    if (HcUtil.hasValue(scheduleInfoObj, "theme")) {
                                        String theme = scheduleInfoObj.getString("theme");
                                        scheduleInfo.setTheme(theme);
                                    }
                                    if (HcUtil.hasValue(scheduleInfoObj, "startTime")) {
                                        String startTime = scheduleInfoObj.getString("startTime");
                                        scheduleInfo.setStartTime(startTime);
                                    }
                                    if (HcUtil.hasValue(scheduleInfoObj, "endTime")) {
                                        String endTime = scheduleInfoObj.getString("endTime");
                                        scheduleInfo.setEndTime(endTime);
                                    }
                                    if (HcUtil.hasValue(scheduleInfoObj, "theme")) {
                                        String theme = scheduleInfoObj.getString("theme");
                                        scheduleInfo.setTheme(theme);
                                    }

                                    colleagueInfo.addInfo(userId, scheduleInfo);
                                }
                            } else {
                                scheduleInfo = new ScheduleDetailsInfo();
                                scheduleInfo.setmUserId(userId);
                                scheduleInfo.setmName(userName);
                                colleagueInfo.addInfo(userId, scheduleInfo);
                            }

                        } else {
                            scheduleInfo = new ScheduleDetailsInfo();
                            scheduleInfo.setmUserId(userId);
                            scheduleInfo.setmName(userName);
                            colleagueInfo.addInfo(userId, scheduleInfo);
                        }


                    }
                }
            } catch (JSONException e) {
                HcLog.D(TAG + " #onSuccess parseJson error =" + e);
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    HcDialog.deleteProgressDialog();
                    mInfos.put(mDate, colleagueInfo);
                    HcLog.D(TAG + " #onSuccess mCurrentDate = "+mCurrentDate + " mDate = "+mDate);
                    ScheduleColleagueAdapter adapter = mAdapter;
                    mAdapter = null;
                    updateData(mPosition, mDate, false, false, adapter);
//                    if (mCurrentDate.equals(mDate))
//                        updateData(mPosition, mDate, false, false, mAdapter);
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
            canelTask(false);
            HcDialog.deleteProgressDialog();
            HcUtil.reLogining(data, ScheduleColleagueActivity2.this, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
            HcLog.D(TAG + " #notifyRequestMd5Url md5Url = " + md5Url + " request = " + request);
            mTaskUrl = md5Url;
        }

        @Override
        public void onConnectionTimeout(RequestCategory request) {
            super.onConnectionTimeout(request);
            canelTask(false);
        }

        @Override
        public void onNetworkInterrupt(RequestCategory request) {
            super.onNetworkInterrupt(request);
            canelTask(false);
        }

        @Override
        public void onParseDataError(RequestCategory request) {
            super.onParseDataError(request);
            canelTask(false);
        }

        @Override
        public void onRequestFailed(int code, String msg, RequestCategory request) {
            super.onRequestFailed(code, msg, request);
            canelTask(false);
        }

        @Override
        public void onResponseFailed(int code, RequestCategory request) {
            super.onResponseFailed(code, request);
            canelTask(false);
        }

        @Override
        public void unknown(RequestCategory request) {
            super.unknown(request);
            canelTask(false);
        }
    }

    /**
     *
     * @param position 当前页的position
     * @param date 当前页的日期,格式为yyyy.MM.dd
     * @param showDialog 是否显示对话框
     */
    private void updateData(int position, String date, boolean showDialog, ScheduleColleagueAdapter adapter) {
        updateData(position, date, showDialog, true, adapter);
    }

    private void startColleagueScheduleTask(String date, boolean showDialog, ScheduleColleagueAdapter adapter) {
        try {
            ScheduleColleagueRequest task = new ScheduleColleagueRequest(ScheduleUtils.dateToStamp(date));
            ScheduleColleagueResponse response = new ScheduleColleagueResponse(date, adapter);
            if (showDialog)
                HcDialog.showProgressDialog(this, R.string.dialog_title_get_data);
            task.sendRequestCommand(RequestCategory.NONE, response, false);
        } catch(Exception e) {
            HcLog.D(TAG + " #startColleagueScheduleTask e ="+e);
        }

    }

    private void canelTask(boolean canel) {
        if (canel) {
            if (!TextUtils.isEmpty(mTaskUrl)) {
                String url = mTaskUrl;
                mTaskUrl = null;
                HttpRequestQueue.getInstance().cancelRequest(url);
            }
        } else {
            mTaskUrl = null;
        }

    }

    /**
     *
     * @param position 当前页的position
     * @param date 当前页的日期,格式为yyyy.MM.dd
     * @param showDialog
     * @param request 没有数据是否需要到服务端请求数据
     */
    private void updateData(int position, String date, boolean showDialog, boolean request, ScheduleColleagueAdapter adapter) {
        ScheduleColleagueInfo info = mInfos.get(date);
        HcLog.D(TAG + " #updateData position = "+position + " date = "+date + " info = "+info);
        if (info != null) {
            if (request) {
                if (info.isEmpty()) {
                    startColleagueScheduleTask(date, showDialog, adapter);
                } else {
                    if (adapter != null) {
                        adapter.updateData(info);
                    }
                }
            } else {
                if (adapter != null) {
                    adapter.updateData(info);
                }
            }

        } else {
            if (request) {
                info = new ScheduleColleagueInfo();
                mInfos.put(date, info);
                startColleagueScheduleTask(date, showDialog, adapter);
            }
        }
    }

    public void updateTitle(long timeMillis) {
        mCurrentTimeMillis = timeMillis;
        mCurrentDate = HcUtil.getDate(DATA_FORMAT, mCurrentTimeMillis);
        mTopBarView.setTitle(mCurrentDate);
    }

    @Override
    public void OnViewChange(int position) {
        int prePostion = mPosition;
        mPosition = position;
        HcLog.D(TAG + " #OnViewChange  position = "+position + " prePostion = "+prePostion);
        if (mPosition == prePostion) return;
        if (mPosition > prePostion) { // 向左滑
            updateTitle(mCurrentTimeMillis + DAY);

        } else { // 向右滑
            updateTitle(mCurrentTimeMillis - DAY);
        }
        if (mContents == null) return;
        updateData(mPosition, mCurrentDate, true, mAdapterMap.get(mContents[mPosition]));

    }


    @Override
    public void onComputeScroll(int position) {
        HcLog.D(TAG + " #onComputeScroll position = "+position);
        if (position == 1 || mContents == null) return;
        if (position == 0) {
            mScrollLayout.removeViewAt(2);
            View[] temp = new View[3];
            temp[0] = mContents[2];
            temp[1] = mContents[0];
            temp[2] = mContents[1];
            mContents = temp;
            mScrollLayout.setCurrentScreen(1);
            mPosition = 1;
            mScrollLayout.addView(mContents[0], 0);
        } else if (position == 2) {
            mScrollLayout.removeViewAt(0);
            View[] temp = new View[3];
            temp[0] = mContents[1];
            temp[1] = mContents[2];
            temp[2] = mContents[0];
            mContents = temp;
            mScrollLayout.setCurrentScreen(1);
            mPosition = 1;
            mScrollLayout.addView(mContents[2], 2);
        }
    }

    private View getView(String content) {
        TextView view = new TextView(this);
        view.setText(content);
        return view;
    }

    @Override
    protected void onDestroy() {
        canelTask(true);
        for (ScheduleColleagueAdapter adapter : mAdapterMap.values()) {
        	adapter.clear();
        }
        mAdapterMap.clear();
        for (ScheduleColleagueInfo info : mInfos.values()) {
        	info.clear();
        }
        mInfos.clear();
        mScrollLayout.removeAllViews();
        mContents = null;
        super.onDestroy();
    }
}
