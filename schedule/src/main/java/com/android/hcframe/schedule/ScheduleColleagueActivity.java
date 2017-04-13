package com.android.hcframe.schedule;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.view.datepicker.DatePickerDialog;

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
import java.util.Queue;

/**
 * Created by zhujiabin on 2016/11/18.
 */

public class ScheduleColleagueActivity extends HcBaseActivity implements View.OnClickListener, ScheduleOnViewChangeListener, AdapterView.OnItemClickListener, DatePickerDialog.OnDateSetListener {
    private String TAG = "ScheduleColleagueActivity";
    private TopBarView mTopBarView;
    private ScheduleColleagueAdapter mAdapter;
    private List<List<ScheduleDetailsInfo>> mInfos;
    //    private LinearLayout mScheduleSearchShow;
//    private LinearLayout mScheduleSearch;
    private ListView mListLeft;
    private ListView mList;
    private ListView mListRight;
    private final Handler mHandler = new Handler();
    private String searchDate;
    private Map<String, List<List<ScheduleDetailsInfo>>> mScheduleInfoList = new HashMap<String, List<List<ScheduleDetailsInfo>>>();
    private ScheduleScrollLayout mScrollLayout;
    private String mToday;
    private String mDate;
    /**
     * 日历
     */
    private Calendar mCalendar;
    /**
     * 日期框
     */
    private DatePickerDialog datePickerDialog;
    /**
     * 日期tag
     */
    public static final String DATEPICKER_TAG = "datepicker";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_colleague_layout);
        mTopBarView = (TopBarView) findViewById(R.id.schedule_colleague_top_bar);
        mListLeft = (ListView) findViewById(R.id.schedule_colleague_list_left);
        mList = (ListView) findViewById(R.id.schedule_colleague_list);
        mListRight = (ListView) findViewById(R.id.schedule_colleague_list_right);
        mScrollLayout = (ScheduleScrollLayout) findViewById(R.id.scrollLayout);
//        mScheduleSearchShow = (LinearLayout) findViewById(R.id.schedule_search_show);
//        mScheduleSearch = (LinearLayout) findViewById(R.id.schedule_search);
        mTopBarView.setMenuBtnVisiable(View.VISIBLE);
        mTopBarView.setMenuSrc(R.drawable.schedule_colleague_calendar);
        mTopBarView.setMenuListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //显示日历
                mCalendar = Calendar.getInstance();
                mCalendar.setTimeInMillis(System.currentTimeMillis());
                if (datePickerDialog == null) {
                    datePickerDialog = new DatePickerDialog();
                }
                datePickerDialog.initialize(ScheduleColleagueActivity.this, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH), false);
                //弹出日历框
                datePickerDialog.setVibrate(false);
                datePickerDialog.setYearRange(1985, 2028);
                datePickerDialog.setCloseOnSingleTapDay(false);
                datePickerDialog.show(getFragmentManager(), DATEPICKER_TAG);
            }
        });
        mScrollLayout.SetOnViewChangeListener(this);
//        mScheduleSearchShow.setOnClickListener(this);
        mList.setOnItemClickListener(this);
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        searchDate = sdf.format(date);
        mToday = searchDate;
        mTopBarView.setTitle(searchDate);
        initData();
    }

    private void initData() {
        //发送请求获取数据
        Intent intent = getIntent();
        if (intent != null) {
            ScheduleColleagueRequest request = null;
            try {
                mDate = ScheduleUtils.dateToStamp(searchDate);
                request = new ScheduleColleagueRequest(mDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ScheduleColleagueResponse response = new ScheduleColleagueResponse();
            HcDialog.showProgressDialog(this, R.string.dialog_title_get_data);
            request.sendRequestCommand(RequestCategory.NONE, response, false);
        }
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
//        if (v.getId() == R.id.schedule_search_show) {
//            mScheduleSearchShow.setVisibility(View.GONE);
//            mScheduleSearch.setVisibility(View.VISIBLE);
//        }

    }

    @Override
    public void OnViewChange(int view) {
        if (view == 0) {
            //向左滑动,删除最右测得view,在最左侧添加一个view
            mToday = ScheduleUtils.getSpecifiedDayBefore(mToday);
            mTopBarView.setTitle(mToday);
            ScheduleColleagueRequest request = null;
            try {
                mDate = ScheduleUtils.dateToStamp(mToday);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            List<List<ScheduleDetailsInfo>> mScheduleInfo = mScheduleInfoList.get(mDate);
            if (mScheduleInfo != null && mScheduleInfo.size() > 0) {
                scheduleInfoList(mScheduleInfo);
            } else {
                request = new ScheduleColleagueRequest(mDate);
                ScheduleColleagueResponse response = new ScheduleColleagueResponse();
                HcDialog.showProgressDialog(this, R.string.dialog_title_post_data);
                request.sendRequestCommand(RequestCategory.NONE, response, false);
            }
        } else if (view == 1) {
            //向右滑动,删除最左测得view,在最右侧添加一个view
            mToday = ScheduleUtils.getSpecifiedDayAfter(mToday);
            mTopBarView.setTitle(mToday);
            ScheduleColleagueRequest request = null;
            try {
                mDate = ScheduleUtils.dateToStamp(mToday);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            List<List<ScheduleDetailsInfo>> mScheduleInfo = mScheduleInfoList.get(mDate);
            if (mScheduleInfo != null && mScheduleInfo.size() > 0) {
                scheduleInfoList(mScheduleInfo);
            } else {
                request = new ScheduleColleagueRequest(mDate);
                ScheduleColleagueResponse response = new ScheduleColleagueResponse();
                HcDialog.showProgressDialog(this, R.string.dialog_title_get_data);
                request.sendRequestCommand(RequestCategory.NONE, response, false);
            }
        }
    }

    private void scheduleInfoList(List<List<ScheduleDetailsInfo>> mScheduleInfo) {
        mAdapter = new ScheduleColleagueAdapter(ScheduleColleagueActivity.this, mScheduleInfo);
        mList.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mInfos.get(position).size() != 0) {
            Intent intent = new Intent(this, ScheduleColleagueInfoActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("scheduleName", mInfos.get(position).get(0).getmName());
            bundle.putString("scheduleId", mInfos.get(position).get(0).getmUserId());
            intent.putExtras(bundle);
            startActivity(intent);
//            setResult(Activity.RESULT_OK, intent); //intent为A传来的带有Bundle的intent，当然也可以自己定义新的Bundle
//            finish();//此处一定要调用finish()方法
        }
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        HcLog.D("Time = " + year + "," + (month + 1) + "," + day);
        StringBuilder sb = new StringBuilder();
        sb.append(year);
        sb.append(".");
        sb.append(month + 1);
        sb.append(".");
        sb.append(day);
        String date = sb.toString();
        mToday = date;
        mTopBarView.setTitle(date);
        try {
            mDate = ScheduleUtils.dateToStamp(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ScheduleColleagueRequest request = new ScheduleColleagueRequest(mDate);
        ScheduleColleagueResponse response = new ScheduleColleagueResponse();
        HcDialog.showProgressDialog(this, R.string.dialog_title_get_data);
        request.sendRequestCommand(RequestCategory.NONE, response, false);
    }

    public class ScheduleColleagueRequest extends AbstractHttpRequest {
        Map<String, String> httpparams = new HashMap<String, String>();

        public ScheduleColleagueRequest(String searchDate) {
            httpparams.put("search_date", searchDate);
        }

        @Override
        public String getRequestMethod() {
            return "getJuniorScheduleListByDate";
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

    private class ScheduleColleagueResponse extends AbstractHttpResponse {
        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            HcDialog.deleteProgressDialog();
            String mId = null, mName = null;
            try {
//                info = new ScheduleColleagueInfo();
                final StringBuilder sb = new StringBuilder();
                JSONObject object = new JSONObject((String) data);
                JSONArray jsonDateArray = object.getJSONArray("list");
                mInfos = new ArrayList<>();
                int jsonDateLenth = jsonDateArray.length();
                if (jsonDateLenth > 0) {
                    for (int i = 0; i < jsonDateLenth; i++) {
                        JSONObject scheduleObj = jsonDateArray.getJSONObject(i);
                        if (HcUtil.hasValue(scheduleObj, "juniorId")) {
                            mId = scheduleObj.getString("juniorId");
                        }
                        if (HcUtil.hasValue(scheduleObj, "juniorName")) {
                            mName = scheduleObj.getString("juniorName");
                        }
                        List<ScheduleDetailsInfo> scheduleInfoList = new ArrayList<>();
                        if (HcUtil.hasValue(scheduleObj, "scheduleList")) {
                            JSONArray jsonBodyArray = scheduleObj.getJSONArray("scheduleList");
                            int jsonArrayLenth = jsonBodyArray.length();
                            if (jsonArrayLenth > 0) {
                                for (int j = 0; j < jsonArrayLenth; j++) {
                                    ScheduleDetailsInfo scheduleInfo = new ScheduleDetailsInfo();
                                    JSONObject scheduleInfoObj = jsonBodyArray.getJSONObject(j);
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
                                    scheduleInfo.setmUserId(mId);
                                    scheduleInfo.setmName(mName);
                                    scheduleInfoList.add(scheduleInfo);
                                }
                            } else {
                                ScheduleDetailsInfo scheduleInfo = new ScheduleDetailsInfo();
                                scheduleInfo.setmName(mName);
                                scheduleInfo.setmUserId(mId);
                                scheduleInfoList.add(scheduleInfo);
                            }
                        }
                        mInfos.add(scheduleInfoList);
                    }
                }
            } catch (JSONException e) {
                HcLog.D(TAG + " #onSuccess parseJson error =" + e);
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mScheduleInfoList.get(mToday) == null) {
                        mScheduleInfoList.put(mDate, mInfos);
                    }
                    mAdapter = new ScheduleColleagueAdapter(ScheduleColleagueActivity.this, mInfos);
                    mList.setAdapter(mAdapter);
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
            HcUtil.reLogining(data, ScheduleColleagueActivity.this, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
            HcLog.D(TAG + " #notifyRequestMd5Url md5Url = " + md5Url + " request = " + request);
        }

    }

}
