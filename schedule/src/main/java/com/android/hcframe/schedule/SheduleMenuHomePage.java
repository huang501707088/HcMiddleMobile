package com.android.hcframe.schedule;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.HttpRequestQueue;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshExpandableListView;
import com.android.hcframe.schedule.data.ScheduleOperatorDatabase;
import com.android.hcframe.sql.SettingHelper;
import com.android.hcframe.view.OneBtnAlterDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

public class SheduleMenuHomePage extends AbstractPage implements PullToRefreshBase.OnRefreshBothListener {

    private static final String TAG = "SheduleMenuHomePage";
    /**
     * 跳转发布日程安排页面
     */
    private int SHEDULE_ADD_CODE = 0;
    //    private int SHEDULE_COLLEAGUE_CODE = 1;
    private int SHEDULE_DETAIL_CODE = 1;
    private List<ScheduleDateInfo> scheduleDateInfoList;
    private List<ScheduleDateInfo> scheduleDateInfoListAll;
    private PullToRefreshExpandableListView expandListView;
    private ScheduleInfoExpandAdapter mAdapter;
    private RelativeLayout mScheduleColleagueBtn;
    private RelativeLayout mScheduleAddBtn;
    private LinearLayout mScheduleTopBtn;
    private final Handler mHandler = new Handler();
    private String userId;
    private String mRefreshDate;
    private View mLine;
    /**
     * 控制下拉刷新时，数据不重复
     */
    private boolean mRefreshFlag = false;

    /**
     * 用于将第一次数据存储近数据库
     */
    private boolean mIsFristFlag = false;
    /**
     * 控制下拉刷新时，数据走new adapter,规避一条child数据越界问题
     */
    private boolean mRefreshDownFlag = false;
//    /**
//     * RequestFlag,请求返回为true
//     */
//    private boolean mColleagueFlag = false;

    protected SheduleMenuHomePage(Activity context, ViewGroup group, String appId) {
        super(context, group);
    }

    @Override
    public void initialized() {

    }

    @Override
    public void setContentView() {
        initView();
        initEvent();
    }

    private void initView() {
        mView = mInflater.inflate(R.layout.schedule_menu_home_page_layout, null);
        expandListView = (PullToRefreshExpandableListView) mView.findViewById(R.id.expandlist);
        mScheduleColleagueBtn = (RelativeLayout) mView.findViewById(R.id.schedule_colleague_btn);
        mScheduleAddBtn = (RelativeLayout) mView.findViewById(R.id.schedule_add_btn);
        mScheduleTopBtn = (LinearLayout) mView.findViewById(R.id.schedule_top_btn);
        mLine = mView.findViewById(R.id.line);
        mLine.setVisibility(View.VISIBLE);
        mScheduleTopBtn.setVisibility(View.VISIBLE);
        initData();
        expandListView.setMode(PullToRefreshBase.Mode.BOTH);
        expandListView.setOnRefreshBothListener(this);
    }

    private void initData() {
        userId = SettingHelper.getUserId(mContext);
        scheduleDateInfoListAll = new ArrayList<ScheduleDateInfo>();
        scheduleDateInfoList = new ArrayList<ScheduleDateInfo>();
        //数据库中获取数据
        getFromDatabase();
        //允许存储进数据库
        mRefreshFlag = true;
        mIsFristFlag = true;
        mRefreshDownFlag = true;
        ScheduleMenuRequest request = new ScheduleMenuRequest(userId, "", "10");
        ScheduleMenuResponse response = new ScheduleMenuResponse();
        HcDialog.showProgressDialog(mContext, R.string.dialog_title_get_data);
        String url = HcUtil.getScheme() + "/terminalServer/szf/getScheduleList?userId=" + SettingHelper.getUserId(mContext) + "&search_count=3";
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

    /**
     * 从数据库中获取数据
     */
    private void getFromDatabase() {
        scheduleDateInfoListAll.addAll(ScheduleOperatorDatabase.getScheduleDateInfos(mContext).values());
        HcLog.D("scheduleDateInfoListAll.size() ="+scheduleDateInfoListAll.size());
        if (scheduleDateInfoListAll.size() > 0) {
            mAdapter = new ScheduleInfoExpandAdapter(mContext, scheduleDateInfoListAll, userId);
            expandListView.getRefreshableView().setAdapter(mAdapter);
            expandListView.getRefreshableView().setGroupIndicator(null); // 去掉默认带的箭头
            int length = scheduleDateInfoListAll.size();
            // 遍历所有group,将所有项设置成默认展开
            for (int i = 0; i < length; i++) {
                expandListView.getRefreshableView().expandGroup(i);
            }
        }
    }

    private void initEvent() {
        mScheduleColleagueBtn.setOnClickListener(this);
        mScheduleAddBtn.setOnClickListener(this);
        expandListView.getRefreshableView().setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                ScheduleInfo scheduleInfo = scheduleDateInfoListAll.get(groupPosition).getScheduleInfoList().get(childPosition);
                if (!scheduleInfo.getTheme().equals("没有日程安排") && userId.equals(SettingHelper.getUserId(mContext)) && HcUtil.isNetWorkAvailable(mContext)) {
                    Intent intent = new Intent(mContext, ScheduleDetailActivity.class);
                    intent.putExtra("scheduleInfo", scheduleInfo);
                    mContext.startActivityForResult(intent, SHEDULE_DETAIL_CODE);
                }
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestory() {
        super.onDestory();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == mContext.RESULT_OK) {
            if (requestCode == SHEDULE_ADD_CODE) {
                mRefreshDownFlag = true;
                //发布页面数据反馈,发送请求，刷新数据
                if (data != null && data.getExtras() != null) {
                    boolean failed = data.getBooleanExtra("failed", false);
                    if (failed) { // 附件上传失败了！
                        final OneBtnAlterDialog dialog = OneBtnAlterDialog.createDialog(mContext, "附件未全部上传,请进入详情上传并补传.");
                        dialog.setCancelable(false);
                        dialog.btn_ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                ScheduleMenuRequest request = new ScheduleMenuRequest(userId, "", "3");
                                ScheduleMenuResponse response = new ScheduleMenuResponse();
                                scheduleDateInfoListAll.clear();
//                                HcDialog.showProgressDialog(mContext, R.string.dialog_title_post_data);
                                String url = HcUtil.getScheme() + "/terminalServer/szf/getScheduleList?userId=" + SettingHelper.getUserId(mContext) + "&search_count=3";
                                request.sendRequestCommand(url, RequestCategory.NONE, response, false);
                            }
                        });
                        dialog.show();
                        return;
                    }
                }
                ScheduleMenuRequest request = new ScheduleMenuRequest(userId, "", "3");
                ScheduleMenuResponse response = new ScheduleMenuResponse();
                scheduleDateInfoListAll.clear();
//                HcDialog.showProgressDialog(mContext, R.string.dialog_title_post_data);
                String url = HcUtil.getScheme() + "/terminalServer/szf/getScheduleList?userId=" + SettingHelper.getUserId(mContext) + "&search_count=3";
                request.sendRequestCommand(url, RequestCategory.NONE, response, false);
            }
//            else if (requestCode == SHEDULE_COLLEAGUE_CODE) {
//                //同事页面数据反馈,发送请求，查询该同事的数据页面
//                //同事的userId
//                mRefreshDownFlag = true;
////                mColleagueFlag = true;
//                userId = data.getExtras().getString("scheduleId");//得到新Activity关闭后返回的数据
//                ScheduleMenuRequest request = new ScheduleMenuRequest(userId, "", "3");
//                ScheduleMenuResponse response = new ScheduleMenuResponse();
//                scheduleDateInfoListAll.clear();
////                HcDialog.showProgressDialog(mContext, R.string.dialog_title_post_data);
//                String url = HcUtil.getScheme() + "/terminalServer/szf/getScheduleList?userId=" + userId + "&search_count=3";
//                request.sendRequestCommand(url, RequestCategory.NONE, response, false);
//            }
            else if (requestCode == SHEDULE_DETAIL_CODE) {
                boolean modifyFlag = data.getExtras().getBoolean("isModifyFlag");
                if (modifyFlag) {
                    mRefreshDownFlag = true;
                    mRefreshFlag = true;
                    ScheduleMenuRequest request = new ScheduleMenuRequest(userId, "", "10");
                    ScheduleMenuResponse response = new ScheduleMenuResponse();
//                  HcDialog.showProgressDialog(mContext, R.string.dialog_title_get_data);
                    String url = HcUtil.getScheme() + "/terminalServer/szf/getScheduleList?userId=" + userId + "&search_count=3" + "&search_date=" + "";
                    request.sendRequestCommand(url, RequestCategory.NONE, response, false);
                } else {
                    scheduleDateInfoListAll.clear();
                    getFromDatabase();
                    mRefreshDate = String.valueOf(Long.parseLong(scheduleDateInfoListAll.get(2).getmSheduleDate()));
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.schedule_add_btn) {
            //点击发布日程安排按钮，跳转到发布日程页面
            Intent intent = new Intent(mContext, ScheduleAddActivity.class);
            mContext.startActivityForResult(intent, SHEDULE_ADD_CODE);
        } else if (v.getId() == R.id.schedule_colleague_btn) {
            Intent intent = new Intent(mContext, ScheduleColleagueActivity2.class);
            mContext.startActivity(intent);
//            mContext.startActivityForResult(intent, SHEDULE_COLLEAGUE_CODE);
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
        //下拉刷新
        mRefreshDownFlag = true;
        mRefreshFlag = true;
        ScheduleMenuRequest request = new ScheduleMenuRequest(userId, "", "10");
        ScheduleMenuResponse response = new ScheduleMenuResponse();
        HcDialog.showProgressDialog(mContext, R.string.dialog_title_get_data);
        String url = HcUtil.getScheme() + "/terminalServer/szf/getScheduleList?userId=" + userId + "&search_count=3" + "&search_date=" + "";
        request.sendRequestCommand(url, RequestCategory.NONE, response, false);
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
            HcDialog.showProgressDialog(mContext, R.string.dialog_title_get_data);
            String url = HcUtil.getScheme() + "/terminalServer/szf/getScheduleList?userId=" + userId + "&search_count=3" + "&search_date=" + date;
            request.sendRequestCommand(url, RequestCategory.NONE, response, false);
        } else {
            expandListView.onRefreshComplete();
        }
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
                            /**
                             * 如果有没有日程安排的字段，每个日期只装一次
                             * */
                            boolean dataFlag = true;
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
                                    if (mIsFristFlag == true) {
                                        ScheduleOperatorDatabase.insertScheduleInfo(mContext, scheduleInfo);
                                        HcLog.D("scheduleInfoDate=" + scheduleInfo.getDate() + " schedule id = " + scheduleInfo.getId());
                                    }
                                    scheduleInfoList.add(scheduleInfo);
                                }
                            } else if (jsonArrayLenth == 0) {
                                scheduleInfo = new ScheduleInfo();
                                if (date != null) {
                                    scheduleInfo.setDate(date);
                                }
                                scheduleInfo.setCreatFlag("0");
                                scheduleInfo.setTheme("没有日程安排");
                                scheduleInfoList.add(scheduleInfo);
                                if (mIsFristFlag == true) {
                                    ScheduleOperatorDatabase.insertNoScheduleInfo(mContext, scheduleInfo);
                                    HcLog.D("scheduleInfoDate=" + scheduleInfo.getDate());
                                }
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
            //除了initData时请求的数据，其他刷新的数据都不能存储进数据库
            mIsFristFlag = false;
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
                    if (mAdapter == null) {
                        setSheduleInfoList();
                    } else if (mRefreshDownFlag == false) {
                        mAdapter.refresh();
                        expandListView.getRefreshableView().setGroupIndicator(null); // 去掉默认带的箭头
                        // 遍历所有group,将所有项设置成默认展开
//                        int groupCount = expandListView.getRefreshableView().getCount();
//                        for (int i = 0; i < groupCount; i++) {}
                        for (int i = 0; i < scheduleDateInfoListAll.size(); i++) {
                            HcLog.D("scheduleDateInfoListAll.size() = " + scheduleDateInfoListAll.size());
                            expandListView.getRefreshableView().collapseGroup(i);
                            expandListView.getRefreshableView().expandGroup(i);
                        }
                    } else {
                        setSheduleInfoList();
                    }
                }

                private void setSheduleInfoList() {
                    mAdapter = new ScheduleInfoExpandAdapter(mContext, scheduleDateInfoListAll, userId);
                    expandListView.getRefreshableView().setAdapter(mAdapter);
                    expandListView.getRefreshableView().setGroupIndicator(null); // 去掉默认带的箭头
                    // 遍历所有group,将所有项设置成默认展开
                    int groupCount = expandListView.getRefreshableView().getCount();
                    for (int i = 0; i < groupCount; i++) {
                        expandListView.getRefreshableView().expandGroup(i);
                    }
//                    if (mColleagueFlag == true) {
//                        //隐藏同事日程和新增日期两个按钮
//                        mScheduleTopBtn.setVisibility(View.GONE);
//                        mColleagueFlag = false;
//                        mRefreshDownFlag = false;
//                    }
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
            HcUtil.reLogining(data, mContext, msg);
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

    @Override
    public void update(Observable observable, Object data) {

    }

}
