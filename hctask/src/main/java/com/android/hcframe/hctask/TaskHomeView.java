package com.android.hcframe.hctask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.hctask.state.CancelledState;
import com.android.hcframe.hctask.state.CompletedState;
import com.android.hcframe.hctask.state.EndState;
import com.android.hcframe.hctask.state.ProcessingState;
import com.android.hcframe.hctask.state.ReceivingState;
import com.android.hcframe.hctask.state.TaskState;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshListView;
import com.android.hcframe.push.HcPushManager;
import com.android.hcframe.push.PushInfo;
import com.android.hcframe.sql.SettingHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-8-3 14:12.
 */
public class TaskHomeView extends AbstractPage implements TaskOperator, PullToRefreshBase.OnRefreshBothListener {

    private static final String TAG = "TaskHomeView";

    private final String mAppId;

    private List<TaskState> mTasks = new ArrayList<TaskState>();

    private PullToRefreshListView mTaskList;

    private RelativeLayout mHistory;

    private RelativeLayout mRelease;

    private TaskAdapter mAdapter;
    /**
     * 发布任务
     */
    private int PUBLISH_REQUEST_CODE = 1;
    /**
     * 查看任务
     */
    private int FIND_REQUEST_CODE = 2;
    /**
     * 更改任务
     */
    private int CHANGE_REQUEST_CODE = 3;
    /**
     * 退回整改
     */
    private int BACK_REQUEST_CODE = 4;
    /**
     * 获取任务列表请求
     */
    private TaskListResponse tResponse;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String taskId = "";
            Iterator<TaskState> iterator;
            TaskState task;
            TaskState newTask;
            switch (msg.what) {
                case TaskState.STATUS_CANCELLED:
                    taskId = (String) msg.obj;
                    iterator = mTasks.iterator();
                    while (iterator.hasNext()) {
                        task = iterator.next();
                        if (taskId.equals(task.getId())) {
                            /**
                             *要将该任务删除，重新装载数据
                             * */
                            iterator.remove();
                            break;
                        }
                    }
                    sortTasks();
                    if (mAdapter == null) {
                        // 实例化自定义的TaskAdapter
                        mAdapter = new TaskAdapter(mContext, mTasks, TaskHomeView.this);
                        // 绑定Adapter
                        mTaskList.setAdapter(mAdapter);
                    } else {
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                case TaskState.STATUS_COMPLETED:
                    taskId = (String) msg.obj;
                    iterator = mTasks.iterator();
                    newTask = null;
                    while (iterator.hasNext()) {
                        task = iterator.next();
                        if (taskId.equals(task.getId())) {
                            newTask = new CompletedState(task);
                            iterator.remove();
                            break;
                        }
                    }
                    if (newTask != null)
                        mTasks.add(newTask);
                    sortTasks();
                    if (mAdapter == null) {
                        // 实例化自定义的TaskAdapter
                        mAdapter = new TaskAdapter(mContext, mTasks, TaskHomeView.this);
                        // 绑定Adapter
                        mTaskList.setAdapter(mAdapter);
                    } else {
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                case TaskState.STATUS_END:
                    taskId = (String) msg.obj;
                    iterator = mTasks.iterator();
                    while (iterator.hasNext()) {
                        task = iterator.next();
                        if (taskId.equals(task.getId())) {
                            iterator.remove();
                            break;
                        }
                    }
                    sortTasks();
                    if (mAdapter == null) {
                        // 实例化自定义的TaskAdapter
                        mAdapter = new TaskAdapter(mContext, mTasks, TaskHomeView.this);
                        // 绑定Adapter
                        mTaskList.setAdapter(mAdapter);
                    } else {
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                case TaskState.STATUS_PROCESSING:
                    taskId = (String) msg.obj;
                    iterator = mTasks.iterator();
                    newTask = null;
                    while (iterator.hasNext()) {
                        task = iterator.next();
                        if (taskId.equals(task.getId())) {
                            newTask = new ProcessingState(task);
                            iterator.remove();
                            break;
                        }
                    }
                    if (newTask != null)
                        mTasks.add(newTask);
                    sortTasks();
                    if (mAdapter == null) {
                        // 实例化自定义的TaskAdapter
                        mAdapter = new TaskAdapter(mContext, mTasks, TaskHomeView.this);
                        // 绑定Adapter
                        mTaskList.setAdapter(mAdapter);
                    } else {
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public TaskHomeView(Activity context, ViewGroup group, String appId) {
        super(context, group);
        mAppId = appId;
    }

    @Override
    public void initialized() {
        if (isFirst) {
            isFirst = !isFirst;
//            test();
//            sortTasks();
            //如果数据库中数据不为空，则从数据中获取数据，否则发送请求，获取数据
            mTasks = TaskOperatorDatabase.getTasks(mContext);
            if (mTasks != null) {
                /**
                 * 按照状态进行排序
                 * */
                sortTasks();
                //数据库中的数据
                mAdapter = new TaskAdapter(mContext, mTasks, this);
                mTaskList.setAdapter(mAdapter);

            }
            // 如果有网络,则发送请求获得的数据
            if (HcUtil.isNetWorkAvailable(mContext)) {
                TaskListRequest tRequest = new TaskListRequest();
                if (tResponse == null) {
                    tResponse = new TaskListResponse(this);
                }
                HcDialog.showProgressDialog(mContext, "获取任务列表");
                tRequest.sendRequestCommand(RequestCategory.NONE, tResponse, false);
            }
//            mAdapter = new TaskAdapter(mContext, mTasks, this);
//            mTaskList.setAdapter(mAdapter);
        }
    }

    @Override
    public void setContentView() {
        if (mView == null) {
            mView = mInflater.inflate(R.layout.task_home_layout, null);

            mHistory = (RelativeLayout) mView.findViewById(R.id.task_home_history_btn);
            mRelease = (RelativeLayout) mView.findViewById(R.id.task_home_release_btn);
            mTaskList = (PullToRefreshListView) mView.findViewById(R.id.task_home_listview);

            mHistory.setOnClickListener(this);
            mRelease.setOnClickListener(this);
            mTaskList.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            mTaskList.setScrollingWhileRefreshingEnabled(false);
            mTaskList.setOnRefreshBothListener(this);
            mTaskList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //点击每个item,查看任务详情
                    Intent intent = new Intent(mContext, TaskDetailsActivity.class);
                    TaskState mTaskState = (TaskState) parent.getAdapter().getItem(position);
                    intent.putExtra("task", mTaskState);
                    mContext.startActivityForResult(intent, FIND_REQUEST_CODE);
                }
            });
        }
    }

    @Override
    public void update(Observable observable, Object data) {

    }

    @Override
    public void onResume() {
        super.onResume();
        //推送
        PushInfo info = HcPushManager.getInstance().getPushInfo();
        if (info != null) {
            HcPushManager.getInstance().setPushInfo(null);
            if (mAppId.equals(info.getAppId())) {
                String content = String.valueOf(info.getContent());
                Intent intent = new Intent(mContext, TaskDetailsActivity.class);
                intent.putExtra("pushTaskId",content);
                mContext.startActivityForResult(intent, FIND_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.task_home_history_btn) {
            //跳转到历史数据Activity
            Intent intent = new Intent(mContext, HistoryTaskActivity.class);
            mContext.startActivity(intent);
        } else if (i == R.id.task_home_release_btn) {
            //跳转到发布新任务Activity
            Intent intent = new Intent(mContext, PublicTaskActivity.class);
            mContext.startActivityForResult(intent, PUBLISH_REQUEST_CODE);
        }
    }

    /**
     * 发布者变更任务
     * <p>对待接收的任务和进行中的任务可以进行操作</p>
     */
    @Override
    public void changeTask(TaskState task) {
        Intent intent = new Intent(mContext, TaskChangeActivity.class);
        //传递需要变更任务的内容
        intent.putExtra("task", task);
        mContext.startActivityForResult(intent, CHANGE_REQUEST_CODE);
    }

    /**
     * 1.发布者提醒执行者接收任务
     * <p>2.执行者提醒发布者检查任务</p>
     */
    @Override
    public void sendRemindMsg(TaskState task) {
        if (!HcUtil.isNetWorkError(mContext)) {
            String msgType = "", msgUserId = "";
            if (SettingHelper.getUserId(mContext).equals(task.getPublisherId())) {
                //点击提醒接收按钮
                msgType = "0";
                msgUserId = task.getExecutorId();
            } else {
                //点击提醒检查按钮
                msgType = "1";
                msgUserId = task.getPublisherId();
            }
            CallReceiverTaskRequest request = new CallReceiverTaskRequest(task.getId(), msgType, msgUserId);
            CallReceiverTaskResponse response = new CallReceiverTaskResponse();
            HcDialog.showProgressDialog(mContext, "正在任务提醒");
            request.sendRequestCommand(RequestCategory.NONE, response, false);
        }

    }

    @Override
    public void receiveTask(TaskState task) {
        if (!HcUtil.isNetWorkError(mContext)) {
            ReceiverTaskRequest request = new ReceiverTaskRequest(task);
            ReceiverTaskResponse response = new ReceiverTaskResponse(task.getId(), this);
            HcDialog.showProgressDialog(mContext, "正在申请接收");
            request.sendRequestCommand(RequestCategory.NONE, response, false);
        }
    }

    /**
     * 发布者退回整改
     * <p>对已完成的任务可以进行整改操作</p>
     *
     * @param task
     */
    @Override
    public void rectificationTask(TaskState task) {
        if (!HcUtil.isNetWorkError(mContext)) {
            Intent intent = new Intent(mContext, TaskBackActivity.class);
            intent.putExtra("taskId", task.getId());
            intent.putExtra("deadTime", task.getEndDate());
            mContext.startActivityForResult(intent, BACK_REQUEST_CODE);
        }
    }

    /**
     * 发布者结束任务
     * <p>对已完成的任务可以进行结束操作</p>
     *
     * @param task
     */
    @Override
    public void endTask( final TaskState task) {
        //点击结束任务，弹出是否结束任务提示框，确定任务是否结束
        //点击新建文件夹按钮，弹出新建文件夹Dialog
        final AlertDialog dialog = new AlertDialog.Builder(mContext)
                .create();
        dialog.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mDialogView = inflater.inflate(R.layout.task_endtask_dialog, null);
        dialog.show();
        dialog.getWindow().setContentView(R.layout.task_endtask_dialog);
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
                if (!HcUtil.isNetWorkError(mContext)) {
                    EndTaskRequest request = new EndTaskRequest(task);
                    EndTaskResponse response = new EndTaskResponse(task.getId(),TaskHomeView.this);
                    HcDialog.showProgressDialog(mContext, "正在结束任务");
                    request.sendRequestCommand(RequestCategory.NONE, response, false);
                }
            }
        });

    }

    /**
     * 执行者提交任务
     *
     * @param task
     */
    @Override
    public void commitTask(TaskState task) {
        if (!HcUtil.isNetWorkError(mContext)) {
            CommitTaskRequest request = new CommitTaskRequest(task);
            CommitTaskResponse response = new CommitTaskResponse(task.getId(), this);
            HcDialog.showProgressDialog(mContext, "正在提交任务");
            request.sendRequestCommand(RequestCategory.NONE, response, false);
        }
    }

    private void test() {
        TaskState task;
        task = new ReceivingState();
        task.setPublisherUrl("");
        task.setPublisher("测试人员");
        task.setPublisherId("");
        task.setPublishDate("2016.05.10");
        task.setDescription("测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试");
        task.setEndDate("2016.10.10");
        task.setExecutor("办事人员");
        task.setExecutorId("");
        task.setExecutorUrl("");
        task.setId("1");
        mTasks.add(task);
        TaskState taskState = new CompletedState(task);
        taskState.setEndDate("2016.06.10");
        mTasks.add(taskState);
        taskState = new EndState(task);
        taskState.setEndDate("2016.06.10");
        taskState.setDescription("看你个SB");
        mTasks.add(taskState);
        taskState = new ProcessingState(task);
        mTasks.add(taskState);
        taskState = new CancelledState(task);
        mTasks.add(taskState);
        taskState = new ReceivingState(task);
        taskState.setPublisherId(SettingHelper.getUserId(mContext));
        taskState.setEndDate("2016.09.10");
        mTasks.add(taskState);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
        if (HcUtil.isNetWorkAvailable(mContext)) {
            TaskListRequest tRequest = new TaskListRequest();
            if (tResponse == null) {
                tResponse = new TaskListResponse(this);
            }
            tRequest.sendRequestCommand(RequestCategory.NONE, tResponse, false);
        }
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {

    }

    private class ReceiverTaskRequest extends AbstractHttpRequest {

        private final TaskState mTask;

        public ReceiverTaskRequest(TaskState task) {
            mTask = task;
        }

        @Override
        public String getParameterUrl() {
            return "?userId=" + SettingHelper.getUserId(mContext)
                    + "&taskId=" + mTask.getId() + "&status=1";
        }

        @Override
        public String getRequestMethod() {
            return "updatetaskstatus";
        }
    }

    private class ReceiverTaskResponse extends AbstractHttpResponse {

        private static final String TAG = TaskHomeView.TAG + "$ReceiverTaskResponse";

        private final String mTaskId;
        private TaskOperator mTaskOperator;

        /**
         * @param taskId 需要处理的任务的Id
         */
        public ReceiverTaskResponse(String taskId, TaskOperator taskOperator) {
            mTaskId = taskId;
            mTaskOperator = taskOperator;
        }

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcDialog.deleteProgressDialog();
            //（考虑到任务不会很多,直接放在主线程里处理）
            // 1.更改状态 （待接收--->执行中）
            // 1.1.删除原有状态的任务
            // 1.2.添加新状态的任务
            // 2.重新排序
            Message task = new Message();
            task.what = TaskState.STATUS_PROCESSING;
            task.obj = mTaskId;
            mHandler.sendMessage(task);
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcDialog.deleteProgressDialog();
            HcUtil.reLogining(data, mContext, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

        }
    }

    private void sortTasks() {
        Collections.sort(mTasks, new Comparator<TaskState>() {
            @Override
            public int compare(TaskState lhs, TaskState rhs) {
                int sort = lhs.getSort() - rhs.getSort();
//                HcLog.D(TAG + " #sortTasks#compare sort = "+sort);
                if (sort == 0) {
                    return lhs.getEndDate().compareTo(rhs.getEndDate());
                }
                return sort;
            }
        });
    }

    private class TaskListRequest extends AbstractHttpRequest {
        private static final String TAG = TaskHomeView.TAG + "$TaskHomeView";
        Map<String, String> httpparams = new HashMap<String, String>();

        public TaskListRequest() {

        }

        @Override
        public String getRequestMethod() {
            return "gettasklist";
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

    private class TaskListResponse extends AbstractHttpResponse {

        private static final String TAG = TaskHomeView.TAG + "$TaskHomeView";
        private TaskOperator mTaskOperator;

        public TaskListResponse(TaskOperator taskOperator) {
            mTaskOperator = taskOperator;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            HcDialog.deleteProgressDialog();
            final List<TaskState> tasks = new ArrayList<TaskState>();
//            mTasks.clear();
            try {
                JSONObject object = new JSONObject((String) data);
                JSONArray jsonSubmitArray = object.getJSONArray("submitList");
                int jsonSubmitArrayNum = jsonSubmitArray.length();
                if (jsonSubmitArrayNum > 0) {
                    for (int i = 0; i < jsonSubmitArrayNum; i++) {
                        TaskState mTaskState = new CompletedState();
                        String mTaskId = "", mCreateUserId = "", mCreateUserName = "", mCreateUserImg = "", mTaskContent = "", mExecuteUserId = "", mExecuteUserName = "", mExecuteUserImg = "", mDeadline = "";
                        JSONObject jsonSubmitObj = jsonSubmitArray.getJSONObject(i);
                        if (HcUtil.hasValue(jsonSubmitObj, "taskId")) {
                            mTaskId = jsonSubmitObj.getString("taskId");
                            mTaskState.setId(mTaskId);
                        }
                        if (HcUtil.hasValue(jsonSubmitObj, "createUserId")) {
                            mCreateUserId = jsonSubmitObj.getString("createUserId");
                            mTaskState.setPublisherId(mCreateUserId);
                        }
                        if (HcUtil.hasValue(jsonSubmitObj, "createUserName")) {
                            mCreateUserName = jsonSubmitObj.getString("createUserName");
                            mTaskState.setPublisher(mCreateUserName);
                        }
                        if (HcUtil.hasValue(jsonSubmitObj, "createUserImg")) {
                            mCreateUserImg = jsonSubmitObj.getString("createUserImg");
                            mTaskState.setPublisherUrl(mCreateUserImg);
                        }
                        if (HcUtil.hasValue(jsonSubmitObj, "taskContent")) {
                            mTaskContent = jsonSubmitObj.getString("taskContent");
                            mTaskState.setDescription(mTaskContent);
                        }
                        if (HcUtil.hasValue(jsonSubmitObj, "executeUserId")) {
                            mExecuteUserId = jsonSubmitObj.getString("executeUserId");
                            mTaskState.setExecutorId(mExecuteUserId);
                        }
                        if (HcUtil.hasValue(jsonSubmitObj, "executeUserName")) {
                            mExecuteUserName = jsonSubmitObj.getString("executeUserName");
                            mTaskState.setExecutor(mExecuteUserName);
                        }
                        if (HcUtil.hasValue(jsonSubmitObj, "executeUserImg")) {
                            mExecuteUserImg = jsonSubmitObj.getString("executeUserImg");
                            mTaskState.setExecutorUrl(mExecuteUserImg);
                        }
                        if (HcUtil.hasValue(jsonSubmitObj, "deadline")) {
                            mDeadline = jsonSubmitObj.getString("deadline");
                            mTaskState.setEndDate(mDeadline);
                        }
                        tasks.add(mTaskState);
//                        mTasks.add(mTaskState);
                    }

                }
                JSONArray jsonNotReceiveArray = object.getJSONArray("notReceiveList");
                int jsonNotReceiveArrayNum = jsonNotReceiveArray.length();
                if (jsonNotReceiveArrayNum > 0) {
                    for (int i = 0; i < jsonNotReceiveArrayNum; i++) {
                        TaskState mTaskState = new ReceivingState();
                        String mTaskId = "", mCreateUserId = "", mCreateUserName = "", mCreateUserImg = "", mTaskContent = "", mExecuteUserId = "", mExecuteUserName = "", mExecuteUserImg = "", mDeadline = "";
                        JSONObject jsonReceivingObj = jsonNotReceiveArray.getJSONObject(i);
                        if (HcUtil.hasValue(jsonReceivingObj, "taskId")) {
                            mTaskId = jsonReceivingObj.getString("taskId");
                            mTaskState.setId(mTaskId);
                        }
                        if (HcUtil.hasValue(jsonReceivingObj, "createUserId")) {
                            mCreateUserId = jsonReceivingObj.getString("createUserId");
                            mTaskState.setPublisherId(mCreateUserId);
                        }
                        if (HcUtil.hasValue(jsonReceivingObj, "createUserName")) {
                            mCreateUserName = jsonReceivingObj.getString("createUserName");
                            mTaskState.setPublisher(mCreateUserName);
                        }
                        if (HcUtil.hasValue(jsonReceivingObj, "createUserImg")) {
                            mCreateUserImg = jsonReceivingObj.getString("createUserImg");
                            mTaskState.setPublisherUrl(mCreateUserImg);
                        }
                        if (HcUtil.hasValue(jsonReceivingObj, "taskContent")) {
                            mTaskContent = jsonReceivingObj.getString("taskContent");
                            mTaskState.setDescription(mTaskContent);
                        }
                        if (HcUtil.hasValue(jsonReceivingObj, "executeUserId")) {
                            mExecuteUserId = jsonReceivingObj.getString("executeUserId");
                            mTaskState.setExecutorId(mExecuteUserId);
                        }
                        if (HcUtil.hasValue(jsonReceivingObj, "executeUserName")) {
                            mExecuteUserName = jsonReceivingObj.getString("executeUserName");
                            mTaskState.setExecutor(mExecuteUserName);
                        }
                        if (HcUtil.hasValue(jsonReceivingObj, "executeUserImg")) {
                            mExecuteUserImg = jsonReceivingObj.getString("executeUserImg");
                            mTaskState.setExecutorUrl(mExecuteUserImg);
                        }
                        if (HcUtil.hasValue(jsonReceivingObj, "deadline")) {
                            mDeadline = jsonReceivingObj.getString("deadline");
                            mTaskState.setEndDate(mDeadline);
                        }
                        tasks.add(mTaskState);
//                        mTasks.add(mTaskState);
                    }

                }
                JSONArray jsonStartArray = object.getJSONArray("startList");
                int jsonStartArrayNum = jsonStartArray.length();
                if (jsonStartArrayNum > 0) {
                    for (int i = 0; i < jsonStartArrayNum; i++) {
                        TaskState mTaskState = new ProcessingState();
                        String mTaskId = "", mCreateUserId = "", mCreateUserName = "", mCreateUserImg = "", mTaskContent = "", mExecuteUserId = "", mExecuteUserName = "", mExecuteUserImg = "", mDeadline = "";
                        JSONObject jsonProcessingObj = jsonStartArray.getJSONObject(i);
                        if (HcUtil.hasValue(jsonProcessingObj, "taskId")) {
                            mTaskId = jsonProcessingObj.getString("taskId");
                            mTaskState.setId(mTaskId);
                        }
                        if (HcUtil.hasValue(jsonProcessingObj, "createUserId")) {
                            mCreateUserId = jsonProcessingObj.getString("createUserId");
                            mTaskState.setPublisherId(mCreateUserId);
                        }
                        if (HcUtil.hasValue(jsonProcessingObj, "createUserName")) {
                            mCreateUserName = jsonProcessingObj.getString("createUserName");
                            mTaskState.setPublisher(mCreateUserName);
                        }
                        if (HcUtil.hasValue(jsonProcessingObj, "createUserImg")) {
                            mCreateUserImg = jsonProcessingObj.getString("createUserImg");
                            mTaskState.setPublisherUrl(mCreateUserImg);
                        }
                        if (HcUtil.hasValue(jsonProcessingObj, "taskContent")) {
                            mTaskContent = jsonProcessingObj.getString("taskContent");
                            mTaskState.setDescription(mTaskContent);
                        }
                        if (HcUtil.hasValue(jsonProcessingObj, "executeUserId")) {
                            mExecuteUserId = jsonProcessingObj.getString("executeUserId");
                            mTaskState.setExecutorId(mExecuteUserId);
                        }
                        if (HcUtil.hasValue(jsonProcessingObj, "executeUserName")) {
                            mExecuteUserName = jsonProcessingObj.getString("executeUserName");
                            mTaskState.setExecutor(mExecuteUserName);
                        }
                        if (HcUtil.hasValue(jsonProcessingObj, "executeUserImg")) {
                            mExecuteUserImg = jsonProcessingObj.getString("executeUserImg");
                            mTaskState.setExecutorUrl(mExecuteUserImg);
                        }
                        if (HcUtil.hasValue(jsonProcessingObj, "deadline")) {
                            mDeadline = jsonProcessingObj.getString("deadline");
                            mTaskState.setEndDate(mDeadline);
                        }
                        tasks.add(mTaskState);
//                        mTasks.add(mTaskState);
                    }

                }
                /**
                 * 保存数据到数据库,更新数据库
                 * */
                TaskOperatorDatabase.insertTasks(mContext, mTasks);
            } catch (JSONException e) {
                HcLog.D(TAG + " #onSuccess parseJson error =" + e);
            }
            //此处这样做是为了解决:The content of the adapter has changed but ListView did not receive a notification.
//            final List<TaskState> mTaskState = new ArrayList<TaskState>();
//            mTaskState.addAll(mTasks);
//            sortTasks();
            Collections.sort(tasks, new Comparator<TaskState>() {
                @Override
                public int compare(TaskState lhs, TaskState rhs) {
                    int sort = lhs.getSort() - rhs.getSort();
//                HcLog.D(TAG + " #sortTasks#compare sort = "+sort);
                    if (sort == 0) {
                        return lhs.getEndDate().compareTo(rhs.getEndDate());
                    }
                    return sort;
                }
            });
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mTaskList.onRefreshComplete();
                    mTasks.clear();
                    mTasks.addAll(tasks);
                    tasks.clear();
                    if (mAdapter == null) {
                        // 实例化自定义的MySkydriveAdapter
                        mAdapter = new TaskAdapter(mContext, mTasks, mTaskOperator);
                        // 绑定Adapter
                        mTaskList.setAdapter(mAdapter);
                    } else {
                        mAdapter.notifyDataSetChanged();
                    }
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
            mTaskList.onRefreshComplete();
            HcUtil.reLogining(data, mContext, msg);
        }

        @Override
        public void onNetworkInterrupt(RequestCategory request) {
            mTaskList.onRefreshComplete();
            super.onNetworkInterrupt(request);
        }

        @Override
        public void onConnectionTimeout(RequestCategory request) {
            mTaskList.onRefreshComplete();
            super.onConnectionTimeout(request);
        }

        @Override
        public void onParseDataError(RequestCategory request) {
            mTaskList.onRefreshComplete();
            super.onParseDataError(request);
        }

        @Override
        public void onRequestFailed(int code, String msg, RequestCategory request) {
            mTaskList.onRefreshComplete();
            super.onRequestFailed(code, msg, request);
        }

        @Override
        public void onResponseFailed(int code, RequestCategory request) {
            mTaskList.onRefreshComplete();
            super.onResponseFailed(code, request);
        }

        @Override
        public void unknown(RequestCategory request) {
            mTaskList.onRefreshComplete();
            super.unknown(request);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
            HcLog.D(TAG + " #notifyRequestMd5Url md5Url = " + md5Url + " request = " + request);
        }

    }

    private class CommitTaskRequest extends AbstractHttpRequest {

        private final TaskState mTask;

        public CommitTaskRequest(TaskState task) {
            mTask = task;
        }

        @Override
        public String getParameterUrl() {
            return "?userId=" + SettingHelper.getUserId(mContext)
                    + "&taskId=" + mTask.getId() + "&status=2";
        }

        @Override
        public String getRequestMethod() {
            return "updatetaskstatus";
        }
    }

    private class CommitTaskResponse extends AbstractHttpResponse {

        private static final String TAG = TaskHomeView.TAG + "$ReceiverTaskResponse";

        private final String mTaskId;
//        private TaskOperator mTaskOperator;

        /**
         * @param taskId 需要处理的任务的Id
         */
        public CommitTaskResponse(String taskId, TaskOperator taskOperator) {
            mTaskId = taskId;
        }

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcDialog.deleteProgressDialog();
            //（考虑到任务不会很多,直接放在主线程里处理）
            // 1.更改状态 （待接收--->执行中）
            // 1.1.删除原有状态的任务
            // 1.2.添加新状态的任务
            // 2.重新排序
            Message task = new Message();
            task.what = TaskState.STATUS_COMPLETED;
            task.obj = mTaskId;
            mHandler.sendMessage(task);
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcDialog.deleteProgressDialog();
            HcUtil.reLogining(data, mContext, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

        }
    }

    private class EndTaskRequest extends AbstractHttpRequest {

        private final TaskState mTask;

        public EndTaskRequest(TaskState task) {
            mTask = task;
        }

        @Override
        public String getParameterUrl() {
            return "?userId=" + SettingHelper.getUserId(mContext)
                    + "&taskId=" + mTask.getId() + "&status=3";
        }

        @Override
        public String getRequestMethod() {
            return "updatetaskstatus";
        }
    }

    private class EndTaskResponse extends AbstractHttpResponse {

        private static final String TAG = TaskHomeView.TAG + "$ReceiverTaskResponse";

        private final String mTaskId;
        private TaskOperator mTaskOperator;

        /**
         * @param taskId 需要处理的任务的Id
         */
        public EndTaskResponse(String taskId, TaskOperator taskOperator) {
            mTaskId = taskId;
            mTaskOperator = taskOperator;
        }

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcDialog.deleteProgressDialog();
            //（考虑到任务不会很多,直接放在主线程里处理）
            // 1.更改状态 （待接收--->执行中）
            // 1.1.删除原有状态的任务
            // 1.2.添加新状态的任务
            // 2.重新排序
            Message task = new Message();
            task.what = TaskState.STATUS_END;
            task.obj = mTaskId;
            mHandler.sendMessage(task);
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcDialog.deleteProgressDialog();
            HcUtil.reLogining(data, mContext, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

        }
    }

    private class CallReceiverTaskRequest extends AbstractHttpRequest {
        private static final String TAG = TaskHomeView.TAG + "$TaskHomeView";
        Map<String, String> httpparams = new HashMap<String, String>();

        public CallReceiverTaskRequest(String taskId, String msgUserId, String msgType) {
            httpparams.put("taskId", taskId);
            httpparams.put("msgUserId", msgUserId);
            httpparams.put("msgType", "msgType");
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

        @Override
        public String getRequestMethod() {
            return "taskremind";
        }
    }

    private class CallReceiverTaskResponse extends AbstractHttpResponse {

        private static final String TAG = TaskHomeView.TAG + "$TaskHomeView";

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            HcDialog.deleteProgressDialog();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    HcUtil.showToast(mContext, "任务提醒成功!");
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
            HcUtil.reLogining(data, mContext, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
            HcLog.D(TAG + " #notifyRequestMd5Url md5Url = " + md5Url + " request = " + request);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == mContext.RESULT_OK) {
            if (requestCode == PUBLISH_REQUEST_CODE) {
                if (HcUtil.isNetWorkAvailable(mContext)) {
                    TaskListRequest tRequest = new TaskListRequest();
                    if (tResponse == null) {
                        tResponse = new TaskListResponse(this);
                    }
                    tRequest.sendRequestCommand(RequestCategory.NONE, tResponse, false);
                }
            } else if (requestCode == FIND_REQUEST_CODE) {
                if (!"".equals(data) && data != null) {
                    Bundle bundle = data.getExtras();
                    TaskState taskState = bundle.getParcelable("task");
                    String taskId = taskState.getId();
                    int status = bundle.getInt("status");
                    Message task = new Message();
                    task.what = status;
                    task.obj = taskId;
                    mHandler.sendMessage(task);
                } else {
                    //发送请求更新首页
                    if (HcUtil.isNetWorkAvailable(mContext)) {
                        TaskListRequest tRequest = new TaskListRequest();
                        if (tResponse == null) {
                            tResponse = new TaskListResponse(this);
                        }
                        tRequest.sendRequestCommand(RequestCategory.NONE, tResponse, false);
                    }
                }
            } else if (requestCode == CHANGE_REQUEST_CODE) {
                //发送请求更新首页
                if (HcUtil.isNetWorkAvailable(mContext)) {
                    TaskListRequest tRequest = new TaskListRequest();
                    if (tResponse == null) {
                        tResponse = new TaskListResponse(this);
                    }
                    tRequest.sendRequestCommand(RequestCategory.NONE, tResponse, false);
                }
            } else if (requestCode == BACK_REQUEST_CODE) {
                if (HcUtil.isNetWorkAvailable(mContext)) {
                    TaskListRequest tRequest = new TaskListRequest();
                    if (tResponse == null) {
                        tResponse = new TaskListResponse(this);
                    }
                    tRequest.sendRequestCommand(RequestCategory.NONE, tResponse, false);
                }
            }
        }
    }
}
