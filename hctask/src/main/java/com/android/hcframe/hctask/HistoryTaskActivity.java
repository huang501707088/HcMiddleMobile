package com.android.hcframe.hctask;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.hctask.state.CancelledState;
import com.android.hcframe.hctask.state.EndState;
import com.android.hcframe.hctask.state.TaskState;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pc on 2016/8/4.
 */
public class HistoryTaskActivity extends HcBaseActivity implements View.OnClickListener, PullToRefreshBase.OnRefreshBothListener {
    private static final String TAG = "HistoryTaskActivity ";
    private PullToRefreshListView mTaskHistoryListview;
    private TopBarView mTopBarView;
    /**
     * 获取历史任务列表请求
     */
    private HistoryTaskListResponse hResponse;
    private HistoryTaskAdapter HistoryTaskAdapter;
    private List<TaskState> mTasks = new ArrayList<TaskState>();
    private List<TaskState> mAllTasks = new ArrayList<TaskState>();
    private List<TaskState> mAllRefreshTasks = new ArrayList<TaskState>();
    private Handler mHandler = new Handler();
    private String taskId = "-1";
    private String pullTaskId;
    private String refreshPullTaskId = "-1";
    /**
     * 查看历史任务
     */
    private int FIND_HISTORY_REQUEST_CODE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_history_layout);
        mTaskHistoryListview = (PullToRefreshListView) findViewById(R.id.task_history_listview);
        mTopBarView = (TopBarView) findViewById(R.id.task_history_topview);
        initData();
        mTopBarView.setTitle("历史任务");
        mTaskHistoryListview.setScrollingWhileRefreshingEnabled(false);
        mTaskHistoryListview.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mTaskHistoryListview.setOnRefreshBothListener(HistoryTaskActivity.this);
        mTaskHistoryListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //点击历史任务，跳转到任务详情页面
                Intent intent = new Intent(HistoryTaskActivity.this, TaskDetailsActivity.class);
                TaskState mTaskState = (TaskState) parent.getAdapter().getItem(position);
                intent.putExtra("task", mTaskState);
                intent.putExtra("history", "history");
                HistoryTaskActivity.this.startActivityForResult(intent, FIND_HISTORY_REQUEST_CODE);
            }
        });
    }

    private void initData() {
        if (HcUtil.isNetWorkAvailable(HistoryTaskActivity.this)) {
            //清空list缓存
            mAllRefreshTasks.clear();
            TaskSharedHelper.cleanHistoryList(this);
            HistoryTaskListRequest hRequest = new HistoryTaskListRequest(taskId);
            if (hResponse == null) {
                hResponse = new HistoryTaskListResponse();
            }
            HcDialog.showProgressDialog(HistoryTaskActivity.this, "获取历史列表");
            hRequest.sendRequestCommand(RequestCategory.NONE, hResponse, false);
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

    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
        HistoryTaskListRequest hRequest = new HistoryTaskListRequest(refreshPullTaskId);
        hResponse = new HistoryTaskListResponse(true);
        hRequest.sendRequestCommand(RequestCategory.NONE, hResponse, false);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {
        refreshPullTaskId = pullTaskId;
        HistoryTaskListRequest hRequest = new HistoryTaskListRequest(pullTaskId);
        hResponse = new HistoryTaskListResponse(false);
        hRequest.sendRequestCommand(RequestCategory.NONE, hResponse, false);
    }

    private class HistoryTaskListRequest extends AbstractHttpRequest {
        private static final String TAG = HistoryTaskActivity.TAG + "$HistoryTaskRequest ";
        Map<String, String> httpparams = new HashMap<String, String>();

        public HistoryTaskListRequest(String taskId) {
            httpparams.put("taskId", taskId);
        }

        @Override
        public String getRequestMethod() {
            return "getendtasklist";
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

    private class HistoryTaskListResponse extends AbstractHttpResponse {

        private static final String TAG = HistoryTaskActivity.TAG + "$HistoryTaskResponse";
        private boolean mRefresh;

        public HistoryTaskListResponse() {
        }

        public HistoryTaskListResponse(boolean refresh) {
            mRefresh = refresh;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            HcDialog.deleteProgressDialog();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mTaskHistoryListview.onRefreshComplete();
                }
            });
            mTasks.clear();
            try {
                JSONObject object = new JSONObject((String) data);
                JSONArray jsonEndArray = object.getJSONArray("endList");
                int jsonEndArrayNum = jsonEndArray.length();
                if (jsonEndArrayNum > 0) {
                    for (int i = 0; i < jsonEndArrayNum; i++) {
                        JSONObject jsonEndObj = jsonEndArray.getJSONObject(i);
                        /**
                         * 要有状态判断已取消与已结束
                         * */
                        String mStatus = "";
                        TaskState mTaskState = null;
                        String mTaskId = "", mCreateUserId = "", mCreateUserName = "", mCreateUserImg = "", mTaskContent = "", mExecuteUserId = "", mExecuteUserName = "", mExecuteUserImg = "", mDeadline = "";
                        if (HcUtil.hasValue(jsonEndObj, "status")) {
                            mStatus = jsonEndObj.getString("status");
                        }
                        if (mStatus.equals("3")) {
                            mTaskState = new EndState();
                        } else if (mStatus.equals("4")) {
                            mTaskState = new CancelledState();
                        }
                        if (HcUtil.hasValue(jsonEndObj, "taskId")) {
                            mTaskId = jsonEndObj.getString("taskId");
                            if (i == (jsonEndArrayNum - 1)) {
                                pullTaskId = mTaskId;
                            }
                            mTaskState.setId(mTaskId);
                        }
                        if (HcUtil.hasValue(jsonEndObj, "createUserId")) {
                            mCreateUserId = jsonEndObj.getString("createUserId");
                            mTaskState.setPublisherId(mCreateUserId);
                        }
                        if (HcUtil.hasValue(jsonEndObj, "createUserName")) {
                            mCreateUserName = jsonEndObj.getString("createUserName");
                            mTaskState.setPublisher(mCreateUserName);
                        }
                        if (HcUtil.hasValue(jsonEndObj, "createUserImg")) {
                            mCreateUserImg = jsonEndObj.getString("createUserImg");
                            mTaskState.setPublisherUrl(mCreateUserImg);
                        }
                        if (HcUtil.hasValue(jsonEndObj, "taskContent")) {
                            mTaskContent = jsonEndObj.getString("taskContent");
                            mTaskState.setDescription(mTaskContent);
                        }
                        if (HcUtil.hasValue(jsonEndObj, "executeUserId")) {
                            mExecuteUserId = jsonEndObj.getString("executeUserId");
                            mTaskState.setExecutorId(mExecuteUserId);
                        }
                        if (HcUtil.hasValue(jsonEndObj, "executeUserName")) {
                            mExecuteUserName = jsonEndObj.getString("executeUserName");
                            mTaskState.setExecutor(mExecuteUserName);
                        }
                        if (HcUtil.hasValue(jsonEndObj, "executeUserImg")) {
                            mExecuteUserImg = jsonEndObj.getString("executeUserImg");
                            mTaskState.setExecutorUrl(mExecuteUserImg);
                        }
                        if (HcUtil.hasValue(jsonEndObj, "deadline")) {
                            mDeadline = jsonEndObj.getString("deadline");
                            mTaskState.setEndDate(mDeadline);
                        }
                        mTasks.add(mTaskState);
                    }
                }
                if (mRefresh == false) {
                    //缓存list中的数据
                    if (mAllTasks.size() > 20 && !taskId.equals("-1")) {
                        mAllTasks = TaskSharedHelper.getHistoryList(HistoryTaskActivity.this);
                        mAllRefreshTasks.addAll(mAllTasks);
                        //加在mAllTask的末尾
                        mAllTasks.addAll(mTasks);
                        //缓存mAllTasks中的数据
                        TaskSharedHelper.setHistoryList(HistoryTaskActivity.this, mAllTasks);
                    } else if (taskId.equals("-1")) {
                        //将mTasks中的数据赋值给mAllTasks,并缓存
                        mAllRefreshTasks.addAll(mAllTasks);
                        mAllTasks.addAll(mTasks);
                        TaskSharedHelper.setHistoryList(HistoryTaskActivity.this, mAllTasks);
                    }
                }
            } catch (JSONException e) {
                HcLog.D(TAG + " #onSuccess parseJson error =" + e);
            }
            /**
             * 按时间排序后要重新设置布局***(如果是一个时间间隔，希望传给我一条特殊的数据)
             * */
            sortTasksByTime();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    HcLog.D("mTasks.size = " + mTasks.size());
                    /**
                     * 设置下拉刷新，上拉获取更多
                     * */
                    if (mTasks.size() < 20) {
                        mTaskHistoryListview.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                        mAllRefreshTasks.addAll(mTasks);
                    } else if (mTasks.size() == 20) {
                        mTaskHistoryListview.setMode(PullToRefreshBase.Mode.BOTH);
                    }
                    if (HistoryTaskAdapter == null) {
                        // 实例化自定义的TaskAdapter
                        HistoryTaskAdapter = new HistoryTaskAdapter(HistoryTaskActivity.this, mAllTasks, null);
                        // 绑定Adapter
                        mTaskHistoryListview.setAdapter(HistoryTaskAdapter);
                    } else {
                        HistoryTaskAdapter.notifyDataSetChanged();
                    }

                }
            });
        }

        private void sortTasksByTime() {
            Collections.sort(mAllTasks, new Comparator<TaskState>() {
                @Override
                public int compare(TaskState lhs, TaskState rhs) {
                    return rhs.getEndDate().compareTo(lhs.getEndDate());
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
            mTaskHistoryListview.onRefreshComplete();
            HcUtil.reLogining(data, HistoryTaskActivity.this, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
            HcLog.D(TAG + " #notifyRequestMd5Url md5Url = " + md5Url + " request = " + request);
        }

        @Override
        public void onNetworkInterrupt(RequestCategory request) {
            mTaskHistoryListview.onRefreshComplete();
            super.onNetworkInterrupt(request);
        }

        @Override
        public void onConnectionTimeout(RequestCategory request) {
            mTaskHistoryListview.onRefreshComplete();
            super.onConnectionTimeout(request);
        }

        @Override
        public void onParseDataError(RequestCategory request) {
            mTaskHistoryListview.onRefreshComplete();
            super.onParseDataError(request);
        }

        @Override
        public void onRequestFailed(int code, String msg, RequestCategory request) {
            mTaskHistoryListview.onRefreshComplete();
            super.onRequestFailed(code, msg, request);
        }

        @Override
        public void onResponseFailed(int code, RequestCategory request) {
            mTaskHistoryListview.onRefreshComplete();
            super.onResponseFailed(code, request);
        }

        @Override
        public void unknown(RequestCategory request) {
            mTaskHistoryListview.onRefreshComplete();
            super.unknown(request);
        }

    }
}
