package com.android.hcframe.schedule;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ExpandableListView;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.HttpRequestQueue;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshExpandableListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhujiabin on 2016/12/29.
 */

public class ScheduleColleagueInfoActivity extends HcBaseActivity implements PullToRefreshBase.OnRefreshBothListener {
    private String TAG = "ScheduleColleagueInfoActivity";
    private PullToRefreshExpandableListView expandListView;
    private ScheduleInfoExpandAdapter mAdapter;
    private List<ScheduleDateInfo> scheduleDateInfoList;
    private List<ScheduleDateInfo> scheduleDateInfoListAll;
    private String userId;
    private String name;
    private String mRefreshDate;
    private boolean mRefreshFlag = false;
    private final Handler mHandler = new Handler();
    private TopBarView mScheduleColleagueTop;
    /**
     * 控制下拉刷新时，数据走new adapter,规避一条child数据越界问题
     */
    private boolean mRefreshDownFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_colleague_info_layout);
        initView();
        initData();
        initEvent();
    }

    private void initView() {
        expandListView = (PullToRefreshExpandableListView) findViewById(R.id.expandlist);
        mScheduleColleagueTop = (TopBarView) findViewById(R.id.schedule_colleague_top_bar);
    }

    private void initData() {
        scheduleDateInfoListAll = new ArrayList<ScheduleDateInfo>();
        scheduleDateInfoList = new ArrayList<ScheduleDateInfo>();
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getExtras().getString("scheduleId");
            name = intent.getExtras().getString("scheduleName");
        }
        mScheduleColleagueTop.setTitle(name + "的日程安排");
        mRefreshFlag = true;
        ScheduleMenuRequest request = new ScheduleMenuRequest(userId, "", "3");
        ScheduleMenuResponse response = new ScheduleMenuResponse();
        scheduleDateInfoListAll.clear();
        HcDialog.showProgressDialog(this, R.string.dialog_title_post_data);
        String url = HcUtil.getScheme() + "/terminalServer/szf/getScheduleList?userId=" + userId + "&search_count=3";
        request.sendRequestCommand(url, RequestCategory.NONE, response, false);
        //不能点击收缩
        expandListView.getRefreshableView().setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                return true;
            }
        });
    }

    private void initEvent() {
        expandListView.setMode(PullToRefreshBase.Mode.BOTH);
        expandListView.setOnRefreshBothListener(this);
//        mScheduleColleagueTop.setReturnViewListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {
        //mRefreshDate+1天
        mRefreshDownFlag = false;
        long date = 0;
        if (mRefreshDate != null) {
            date = Long.parseLong(mRefreshDate) + 60 * 60 * 24 * 1000;
        }
        if (date != 0) {
            //上拉加载
            ScheduleMenuRequest request = new ScheduleMenuRequest(userId, String.valueOf(date), "10");
            ScheduleMenuResponse response = new ScheduleMenuResponse();
            HcDialog.showProgressDialog(this, R.string.dialog_title_get_data);
            String url = HcUtil.getScheme() + "/terminalServer/szf/getScheduleList?userId=" + userId + "&search_count=3" + "&search_date=" + date;
            request.sendRequestCommand(url, RequestCategory.NONE, response, false);
        } else {
            expandListView.onRefreshComplete();
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
        //下拉刷新
        mRefreshDownFlag = true;
        mRefreshFlag = true;
        ScheduleMenuRequest request = new ScheduleMenuRequest(userId, "", "10");
        ScheduleMenuResponse response = new ScheduleMenuResponse();
        HcDialog.showProgressDialog(this, R.string.dialog_title_get_data);
        String url = HcUtil.getScheme() + "/terminalServer/szf/getScheduleList?userId=" + userId + "&search_count=3" + "&search_date=" + "";
        request.sendRequestCommand(url, RequestCategory.NONE, response, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public class ScheduleMenuRequest extends AbstractHttpRequest {
        Map<String, String> httpparams = new HashMap<String, String>();

        public ScheduleMenuRequest(String userId, String searchDate, String searchCount) {
            httpparams.put("userId", userId);
            httpparams.put("search_date", searchDate);
            httpparams.put("search_count", searchCount);
        }

        @Override
        public String getRequestMethod() {
            return "getScheduleList";
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
            HcLog.D(TAG + " = " + data);
            try {
                JSONObject object = new JSONObject(data);
                int status = object.getInt(HttpRequestQueue.STATUS);
                if (status == HttpRequestQueue.REQUEST_SUCCESS) {
                    if (HcUtil.hasValue(object, HttpRequestQueue.BODY)) {
                        onSuccess(object.toString());
                    } else {
                        onSuccess("{}");
                    }

                } else if (status == HcHttpRequest.REQUEST_ACCOUT_EXCLUDED ||
                        status == HcHttpRequest.REQUEST_TOKEN_FAILED) {
                    String msg = "";
                    if (HcUtil.hasValue(object, HttpRequestQueue.MSG)) {
                        msg = object.getString(HttpRequestQueue.MSG);
                    }
                    object = object.getJSONObject(HttpRequestQueue.BODY);
                    onAccountExcluded(object.toString(), msg);
                } else {
                    String msg = "";
                    if (HcUtil.hasValue(object, HttpRequestQueue.MSG)) {
                        msg = object.getString(HttpRequestQueue.MSG);
                    }
                    onRequestFailed(status, msg);
                }
            } catch (Exception e) {
                // TODO: handle exception
                HcLog.D(TAG + " #parseJson error e = " + e);
                onParseDataError();
            }
        }
    }

    private class ScheduleMenuResponse extends AbstractHttpResponse {

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            HcDialog.deleteProgressDialog();
            scheduleDateInfoList.clear();
            scheduleDateInfoList = new ArrayList<ScheduleDateInfo>();
            String mDate = null, startTime = null;
            try {
                JSONObject object = new JSONObject((String) data);
                JSONArray jsonDateArray = object.getJSONArray("body");
                int jsonDateLenth = jsonDateArray.length();
                if (jsonDateLenth > 0) {
                    for (int i = 0; i < jsonDateLenth; i++) {
                        ScheduleDateInfo scheduleDateInfo = new ScheduleDateInfo();
                        JSONObject scheduleDateObj = jsonDateArray.getJSONObject(i);
                        String scheduleIdStr = null;
                        String date = null;
                        if (HcUtil.hasValue(scheduleDateObj, "date")) {
                            date = scheduleDateObj.getString("date");
                            mRefreshDate = date;
                            scheduleDateInfo.setmSheduleDate(date);
                        }
                        if (HcUtil.hasValue(scheduleDateObj, "scheduleList")) {
                            List<ScheduleInfo> scheduleInfoList = new ArrayList<ScheduleInfo>();
                            JSONArray jsonBodyArray = scheduleDateObj.getJSONArray("scheduleList");
                            int jsonArrayLenth = jsonBodyArray.length();
                            ScheduleInfo scheduleInfo;
                            if (jsonArrayLenth > 0) {
                                StringBuilder sb = new StringBuilder();
                                for (int j = 0; j < jsonArrayLenth; j++) {
                                    scheduleInfo = new ScheduleInfo();
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
                                        startTime = scheduleInfoObj.getString("startTime");
                                        scheduleInfo.setStartTime(startTime);
                                    }
                                    if (HcUtil.hasValue(scheduleInfoObj, "endTime")) {
                                        String endTime = scheduleInfoObj.getString("endTime");
                                        scheduleInfo.setEndTime(endTime);
                                    }
                                    if (HcUtil.hasValue(scheduleInfoObj, "isParticipanter")) {
                                        String createFlag = scheduleInfoObj.getString("isParticipanter");
                                        scheduleInfo.setCreatFlag(createFlag);
                                    }
                                    if (date != null) {
                                        scheduleInfo.setDate(date);
                                    }
                                    scheduleInfoList.add(scheduleInfo);
                                }
                            } else {
                                scheduleInfo = new ScheduleInfo();
                                if (date != null) {
                                    scheduleInfo.setDate(date);
                                }
                                scheduleInfo.setCreatFlag("0");
                                scheduleInfo.setTheme("没有日程安排");
                                scheduleInfoList.add(scheduleInfo);
                            }
                            if (scheduleInfoList.size() > 0 && scheduleInfoList != null) {
                                scheduleDateInfo.setScheduleInfoList(scheduleInfoList);
                            }
                        }
                        scheduleDateInfoList.add(scheduleDateInfo);
                    }
                }
            } catch (JSONException e) {
                HcLog.D(TAG + " #onSuccess parseJson error =" + e);
            }
            if (mRefreshFlag == false) {
                scheduleDateInfoListAll.addAll(scheduleDateInfoList);
            } else {
                scheduleDateInfoListAll.clear();
                scheduleDateInfoListAll.addAll(scheduleDateInfoList);
                mRefreshFlag = false;
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //清空adapter
                    expandListView.onRefreshComplete();
                    if (mAdapter == null || mRefreshDownFlag == true) {
                        setSheduleInfoList();
                    } else {
                        mAdapter.refresh();
                        expandListView.getRefreshableView().setGroupIndicator(null); // 去掉默认带的箭头
                        // 遍历所有group,将所有项设置成默认展开
                        for (int i = 0; i < scheduleDateInfoListAll.size(); i++) {
                            HcLog.D("scheduleDateInfoListAll.size() = " + scheduleDateInfoListAll.size());
                            expandListView.getRefreshableView().collapseGroup(i);
                            expandListView.getRefreshableView().expandGroup(i);
                        }
                    }
                }

                private void setSheduleInfoList() {
                    mAdapter = new ScheduleInfoExpandAdapter(ScheduleColleagueInfoActivity.this, scheduleDateInfoListAll, userId);
                    expandListView.getRefreshableView().setAdapter(mAdapter);
                    expandListView.getRefreshableView().setGroupIndicator(null); // 去掉默认带的箭头
                    // 遍历所有group,将所有项设置成默认展开
                    int groupCount = expandListView.getRefreshableView().getCount();
                    for (int i = 0; i < groupCount; i++) {
                        expandListView.getRefreshableView().expandGroup(i);
                    }
                }
            });
        }

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            expandListView.onRefreshComplete();
            HcDialog.deleteProgressDialog();
            HcUtil.reLogining(data, ScheduleColleagueInfoActivity.this, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
//            mListMD5Url = md5Url;
        }

        @Override
        public void onNetworkInterrupt(RequestCategory request) {
            expandListView.onRefreshComplete();
            super.onNetworkInterrupt(request);
        }

        @Override
        public void onConnectionTimeout(RequestCategory request) {
            expandListView.onRefreshComplete();
            super.onConnectionTimeout(request);
        }

        @Override
        public void onParseDataError(RequestCategory request) {
            expandListView.onRefreshComplete();
            super.onParseDataError(request);
        }

        @Override
        public void onRequestFailed(int code, String msg, RequestCategory request) {
            expandListView.onRefreshComplete();
            super.onRequestFailed(code, msg, request);
        }

        @Override
        public void onResponseFailed(int code, RequestCategory request) {
            expandListView.onRefreshComplete();
            super.onResponseFailed(code, request);
        }

        @Override
        public void unknown(RequestCategory request) {
            expandListView.onRefreshComplete();
            super.unknown(request);
        }


    }
}


