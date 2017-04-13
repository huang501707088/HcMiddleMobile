package com.android.hcframe.hctask;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.hctask.state.TaskState;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.sql.SettingHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pc on 2016/8/5.
 */
public class TaskDetailsActivity extends HcBaseActivity implements View.OnClickListener {
    private static final String TAG = "TaskDetailsActivity";
    private int mWidth;
    private int CHANGE_REQUEST_CODE = 4;
    private ScrollerListView mTaskDetailsList;
    private List<TaskDetailInfo.DiscussInfo> mTasks = new ArrayList<>();
    private TaskDetailAdapter mDetailAdapter;
    private TopBarView mTopBarView;
    private LinearLayout mTaskDetailsBottom;
    private LinearLayout mTaskDetailsInput;
    private EditText mTaskDetailsEdit;
    private TextView mTaskDetailsText;
    private TextView mTaskDetailsOneText;
    private TextView mTaskDetailsTwoText;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;
    private TaskState task;
    private String taskId = "", tastDetailsContent = "", tastDetailsDeadLine = "", tastDetailsPublisher = "", tastDetailsPublisherImg = "", tastDetailsExecutor = "", tastDetailsExecutorImg = "", tastDetailsPublisherId = "", tastDetailsExecuteId = "";
    private int mStatus = 0;
    private Handler mHandler = new Handler();
    /**
     * 获取任务详情列表请求
     */
    private TaskDetailsListResponse dResponse;
    /**
     * 讨论任务列表请求
     */
    private DiscussTaskResponse diResponse;
    /**
     * 历史任务Item点击进入时，该块不可见
     */
    private LinearLayout mTaskDetailsBar;
    /**
     * listview下边的按钮
     */
    private LinearLayout mTaskDetailsTxt, mTaskDetailsTwoTxt;
    private String mUserId;
    /**
     * 退回整改
     */
    private int BACK_REQUEST_CODE = 5;
    private RelativeLayout mTaskDetailsTopParent;
    private ImageView mTaskDetailsPublishIcon;
    private TextView mTaskDetailsPublishName;
    private ImageView mTaskDetailsExecutorIcon;
    private TextView mTaskDetailsExecutorName;
    private TextView mTaskDetailsTime;
    private TextView mTaskDetailsContent;
    private LinearLayout mPublisherParent;
    private LinearLayout mExecutorParent;
    private ImageView mTaskDetailsInputEllipsis;
    private ImageView mTaskDetailsInputImg;
    private ImageView mTaskDetailsImg;
    private TextView mTaskDetailsCenterText;
    private ScrollView mTaskDetailsScrollview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_details_layout);
        initViews();
        setLayoutParams();
        /**
         * 用来加载网络图片
         * */
        mOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageOnLoading(R.drawable.task_default_head)
                .showImageForEmptyUri(R.drawable.task_default_head)
                .showImageOnFail(R.drawable.task_default_head).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888).build();
        mImageLoader = ImageLoader.getInstance();
        initData();
    }

    private void initViews() {
        mTopBarView = (TopBarView) findViewById(R.id.task_details_topview);
        mTaskDetailsTopParent = (RelativeLayout) findViewById(R.id.task_details_top_parent);
        mPublisherParent = (LinearLayout) findViewById(R.id.task_details_publish_parent);
        mTaskDetailsPublishIcon = (ImageView) findViewById(R.id.task_details_publish_icon);
        mTaskDetailsPublishName = (TextView) findViewById(R.id.task_details_publish_name);
        mExecutorParent = (LinearLayout) findViewById(R.id.task_details_executor_parent);
        mTaskDetailsExecutorIcon = (ImageView) findViewById(R.id.task_details_executor_icon);
        mTaskDetailsExecutorName = (TextView) findViewById(R.id.task_details_executor_name);
        mTaskDetailsTime = (TextView) findViewById(R.id.task_details_time);
        mTaskDetailsContent = (TextView) findViewById(R.id.task_details_content);
        mTaskDetailsList = (ScrollerListView) findViewById(R.id.task_details_listview);
        mTaskDetailsText = (TextView) findViewById(R.id.task_details_text);
        mTaskDetailsOneText = (TextView) findViewById(R.id.task_details_one_text);
        mTaskDetailsTwoText = (TextView) findViewById(R.id.task_details_two_text);
        mTaskDetailsBar = (LinearLayout) findViewById(R.id.task_details_bar);
        mTaskDetailsTxt = (LinearLayout) findViewById(R.id.task_details_txt);
        mTaskDetailsTwoTxt = (LinearLayout) findViewById(R.id.task_details_two_txt);
        mTaskDetailsInputEllipsis = (ImageView) findViewById(R.id.task_details_input_ellipsis);
        mTaskDetailsInputImg = (ImageView) findViewById(R.id.task_details_input_img);
        mTaskDetailsImg = (ImageView) findViewById(R.id.task_details_img);
        mTaskDetailsBottom = (LinearLayout) findViewById(R.id.task_details_bottom);
        mTaskDetailsInput = (LinearLayout) findViewById(R.id.task_details_input);
        mTaskDetailsEdit = (EditText) findViewById(R.id.task_details_edit);
        mTaskDetailsCenterText = (TextView) findViewById(R.id.task_details_center_text);
        mTaskDetailsImg.setOnClickListener(this);
        mTaskDetailsInputImg.setOnClickListener(this);
        mTaskDetailsList.setFocusable(false);
        mTaskDetailsInputEllipsis.setOnClickListener(this);
    }


    private void setLayoutParams() {
        mWidth = HcUtil.getScreenWidth();
        int margin = (int) (mWidth * 0.069);
        int imageWidth = (int) (mWidth * 0.167);
        // 设置背景高度
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mTaskDetailsTopParent.getLayoutParams();
        lp.height = (int) (mWidth * 0.291);
        mTaskDetailsTopParent.setLayoutParams(lp);
        // 设置发布者的头像
        lp = (LinearLayout.LayoutParams) mTaskDetailsPublishIcon.getLayoutParams();
        lp.width = imageWidth;
        lp.height = imageWidth;
        mTaskDetailsPublishIcon.setLayoutParams(lp);
        // 设置执行者的头像
        lp = (LinearLayout.LayoutParams) mTaskDetailsExecutorIcon.getLayoutParams();
        lp.width = imageWidth;
        lp.height = imageWidth;
        mTaskDetailsExecutorIcon.setLayoutParams(lp);
        // 设置左边距
        RelativeLayout.LayoutParams rp = (RelativeLayout.LayoutParams) mPublisherParent.getLayoutParams();
        rp.leftMargin = margin;
        rp.addRule(RelativeLayout.CENTER_VERTICAL);
        rp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        mPublisherParent.setLayoutParams(rp);
        // 设置右边距
        rp = (RelativeLayout.LayoutParams) mExecutorParent.getLayoutParams();
        rp.rightMargin = margin;
        rp.addRule(RelativeLayout.CENTER_VERTICAL);
        rp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mExecutorParent.setLayoutParams(rp);
    }


    private void initData() {
        mTopBarView.setTitle("任务详情");
        Intent intent = getIntent();
        if (intent != null) {
            task = intent.getParcelableExtra("task");
            if (task != null) {
                taskId = task.getId();
                mStatus = task.getStatus();
                mTaskDetailsExecutorName.setText(task.getExecutor());
                mTaskDetailsPublishName.setText(task.getPublisher());
                mImageLoader.displayImage(task.getPublisherUrl(), mTaskDetailsPublishIcon, mOptions);
                mImageLoader.displayImage(task.getExecutorUrl(), mTaskDetailsExecutorIcon, mOptions);
                mUserId = SettingHelper.getUserId(this);
                if (mStatus == 0) {
                    mTaskDetailsCenterText.setText("待接收");
                    //0:待接收
                    if (task.getPublisherId().equals(mUserId)) {
                        mTaskDetailsTxt.setVisibility(View.GONE);
                        mTaskDetailsTwoTxt.setVisibility(View.VISIBLE);
                        mTaskDetailsOneText.setText("变更任务");
                        mTaskDetailsTwoText.setText("提醒接收");
                        mTaskDetailsOneText.setOnClickListener(this);
                        mTaskDetailsTwoText.setOnClickListener(this);
                    } else {
                        mTaskDetailsTxt.setVisibility(View.VISIBLE);
                        mTaskDetailsTwoTxt.setVisibility(View.GONE);
                        mTaskDetailsText.setText("接收任务");
                        mTaskDetailsText.setOnClickListener(this);
                    }
                } else if (mStatus == 1) {
                    mTaskDetailsCenterText.setText("进行中");
                    //1：进行中
                    if (task.getPublisherId().equals(mUserId)) {
                        mTaskDetailsTxt.setVisibility(View.VISIBLE);
                        mTaskDetailsTwoTxt.setVisibility(View.GONE);
                        mTaskDetailsText.setText("变更任务");
                        mTaskDetailsText.setOnClickListener(this);
                    } else {
                        mTaskDetailsTxt.setVisibility(View.VISIBLE);
                        mTaskDetailsTwoTxt.setVisibility(View.GONE);
                        mTaskDetailsText.setText("提交完成");
                        mTaskDetailsText.setOnClickListener(this);
                    }
                } else if (mStatus == 2) {
                    mTaskDetailsCenterText.setText("已完成");
                    //2：已完成
                    if (task.getPublisherId().equals(mUserId)) {
                        mTaskDetailsTxt.setVisibility(View.GONE);
                        mTaskDetailsTwoTxt.setVisibility(View.VISIBLE);
                        mTaskDetailsOneText.setText("退回整改");
                        mTaskDetailsTwoText.setText("结束任务");
                        mTaskDetailsOneText.setOnClickListener(this);
                        mTaskDetailsTwoText.setOnClickListener(this);
                    } else {
                        mTaskDetailsTxt.setVisibility(View.VISIBLE);
                        mTaskDetailsTwoTxt.setVisibility(View.GONE);
                        mTaskDetailsText.setText("提醒检查");
                        mTaskDetailsText.setOnClickListener(this);
                    }
                } else if (mStatus == 3) {
                    mTaskDetailsCenterText.setText("已结束");
                } else if (mStatus == 4) {
                    mTaskDetailsCenterText.setText("已取消");
                }
            }
        }
        String historyTask = intent.getStringExtra("history");
        if (historyTask != null) {
            mTaskDetailsBar.setVisibility(View.GONE);
        }
        String pushTaskId = intent.getStringExtra("pushTaskId");
        if (pushTaskId != null) {
            taskId = pushTaskId;
        }
        TaskDetailsListRequest dRequest = new TaskDetailsListRequest(taskId);
        if (dResponse == null) {
            dResponse = new TaskDetailsListResponse();
        }
        HcDialog.showProgressDialog(this, "获取详情列表");
        dRequest.sendRequestCommand(RequestCategory.NONE, dResponse, false);
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
        int i = v.getId();
        if (i == R.id.task_details_img) {
            mTaskDetailsBottom.setVisibility(View.GONE);
            mTaskDetailsInput.setVisibility(View.VISIBLE);
        } else if (i == R.id.task_details_input_img) {
            //点击发送按钮，向服务器端传递一条数据，并更新当前listview(其他人要刷新的)
            String taskDetailsEdit = mTaskDetailsEdit.getText().toString().trim();
//            /**
//             * 隐藏键盘
//             * */
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    InputMethodManager imm = (InputMethodManager) TaskDetailsActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0); //强制隐藏键盘
//                }
//            });

            if (!TextUtils.isEmpty(taskDetailsEdit)) {
                //向服务器端发送请求.讨论任务
                DiscussTaskRequest diRequest = new DiscussTaskRequest(taskId, taskDetailsEdit);
                if (diResponse == null) {
                    diResponse = new DiscussTaskResponse();
                }
                HcDialog.showProgressDialog(TaskDetailsActivity.this, "正在发送数据");
                diRequest.sendRequestCommand(RequestCategory.NONE, diResponse, false);
            }
        } else if (i == R.id.task_details_input_ellipsis) {
            //点击省略号按钮
            mTaskDetailsBottom.setVisibility(View.VISIBLE);
            mTaskDetailsInput.setVisibility(View.GONE);
        } else if (i == R.id.task_details_text) {
            if (mTaskDetailsText.getText().equals("变更任务")) {
                //跳转到变更任务界面
                Intent intent = new Intent(TaskDetailsActivity.this, TaskChangeActivity.class);
                //传递需要变更任务的内容
                intent.putExtra("task", task);
                intent.putExtra("taskDetail", "taskDetail");
                startActivityForResult(intent, CHANGE_REQUEST_CODE);
            } else if (mTaskDetailsText.getText().equals("接收任务")) {
                if (!HcUtil.isNetWorkError(this)) {
                    ReceiverTaskRequest request = new ReceiverTaskRequest(task);
                    ReceiverTaskResponse response = new ReceiverTaskResponse(task.getId());
                    HcDialog.showProgressDialog(this, "正在申请接收");
                    request.sendRequestCommand(RequestCategory.NONE, response, false);
                }
            } else if (mTaskDetailsText.getText().equals("提交完成")) {
                if (!HcUtil.isNetWorkError(this)) {
                    CommitTaskRequest request = new CommitTaskRequest(task);
                    CommitTaskResponse response = new CommitTaskResponse(task.getId());
                    HcDialog.showProgressDialog(this, "正在提交任务");
                    request.sendRequestCommand(RequestCategory.NONE, response, false);
                }
            } else if (mTaskDetailsText.getText().equals("提醒检查")) {
                if (!HcUtil.isNetWorkError(this)) {
                    String msgType = "", msgUserId = "";
                    //点击提醒检查按钮
                    msgType = "1";
                    msgUserId = task.getPublisherId();
                    CallReceiverTaskRequest request = new CallReceiverTaskRequest(task.getId(), msgType, msgUserId);
                    CallReceiverTaskResponse response = new CallReceiverTaskResponse();
                    HcDialog.showProgressDialog(this, "正在任务提醒");
                    request.sendRequestCommand(RequestCategory.NONE, response, false);
                }
            }
        } else if (i == R.id.task_details_one_text) {
            if (mTaskDetailsOneText.getText().equals("提醒接收")) {
                if (!HcUtil.isNetWorkError(this)) {
                    String msgType = "", msgUserId = "";
                    //点击提醒接收按钮
                    msgType = "0";
                    msgUserId = task.getExecutorId();
                    CallReceiverTaskRequest request = new CallReceiverTaskRequest(task.getId(), msgType, msgUserId);
                    CallReceiverTaskResponse response = new CallReceiverTaskResponse();
                    HcDialog.showProgressDialog(this, "正在任务提醒");
                    request.sendRequestCommand(RequestCategory.NONE, response, false);
                }
            } else if (mTaskDetailsOneText.getText().equals("退回整改")) {
                if (!HcUtil.isNetWorkError(this)) {
                    Intent intent = new Intent(TaskDetailsActivity.this, TaskBackActivity.class);
                    intent.putExtra("taskId", task.getId());
                    intent.putExtra("deadTime", task.getEndDate());
                    intent.putExtra("taskDetail", "taskDetail");
                    TaskDetailsActivity.this.startActivityForResult(intent, BACK_REQUEST_CODE);
                }
            } else if (mTaskDetailsOneText.getText().equals("变更任务")) {
                //跳转到变更任务界面
                Intent intent = new Intent(TaskDetailsActivity.this, TaskChangeActivity.class);
                //传递需要变更任务的内容
                intent.putExtra("task", task);
                intent.putExtra("taskDetail", "taskDetail");
                startActivityForResult(intent, CHANGE_REQUEST_CODE);
            }

        } else if (i == R.id.task_details_two_text) {
            if (mTaskDetailsTwoText.getText().equals("变更任务")) {
                //跳转到变更任务界面
                Intent intent = new Intent(TaskDetailsActivity.this, TaskChangeActivity.class);
                //传递需要变更任务的内容
                intent.putExtra("task", task);
                intent.putExtra("taskDetail", "taskDetail");
                startActivityForResult(intent, CHANGE_REQUEST_CODE);
            } else if (mTaskDetailsTwoText.getText().equals("结束任务")) {
                //点击结束任务，弹出是否结束任务提示框，确定任务是否结束
                //点击新建文件夹按钮，弹出新建文件夹Dialog
                final AlertDialog dialog = new AlertDialog.Builder(this)
                        .create();
                dialog.setCancelable(false);
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                        if (!HcUtil.isNetWorkError(TaskDetailsActivity.this)) {
                            EndTaskRequest request = new EndTaskRequest(task);
                            EndTaskResponse response = new EndTaskResponse(task.getId());
                            HcDialog.showProgressDialog(TaskDetailsActivity.this, "正在结束任务");
                            request.sendRequestCommand(RequestCategory.NONE, response, false);
                        }
                    }
                });

            } else if (mTaskDetailsTwoText.getText().equals("提醒接收")) {
                String msgType = "", msgUserId = "";
                //点击提醒接收按钮
                msgType = "0";
                msgUserId = task.getExecutorId();
                CallReceiverTaskRequest request = new CallReceiverTaskRequest(task.getId(), msgType, msgUserId);
                CallReceiverTaskResponse response = new CallReceiverTaskResponse();
                HcDialog.showProgressDialog(this, "正在任务提醒");
                request.sendRequestCommand(RequestCategory.NONE, response, false);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHANGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                String taskChange = bundle.getString("taskChange");
                if (taskChange.equals("taskChange")) {
                    TaskDetailsActivity.this.setResult(RESULT_OK);
                    finish();
                }

            }
        }
        if (requestCode == BACK_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                String taskBack = bundle.getString("taskBack");
                if (taskBack.equals("taskBack")) {
                    TaskDetailsActivity.this.setResult(RESULT_OK);
                    finish();
                }
            }
        }
    }

    private class TaskDetailsListRequest extends AbstractHttpRequest {
        private static final String TAG = TaskDetailsActivity.TAG + "$TaskDetailsRequest";
        Map<String, String> httpparams = new HashMap<String, String>();

        public TaskDetailsListRequest(String taskId) {
            httpparams.put("taskId", taskId);
        }

        @Override
        public String getRequestMethod() {
            return "gettaskanddiscussdetail";
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

    private class DiscussTaskRequest extends AbstractHttpRequest {
        private static final String TAG = TaskDetailsActivity.TAG + "$TaskDetailsActivity";
        Map<String, String> httpparams = new HashMap<String, String>();

        public DiscussTaskRequest(String taskId, String discussContent) {
            httpparams.put("taskId", taskId);
            httpparams.put("discussContent", discussContent);
        }

        @Override
        public String getRequestMethod() {
            return "discusstask";
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

    private class DiscussTaskResponse extends AbstractHttpResponse {
        private static final String TAG = TaskDetailsActivity.TAG + "$TaskDetailsResponse";

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            HcDialog.deleteProgressDialog();
            mTasks.clear();
            try {
                JSONObject object = new JSONObject((String) data);
                JSONArray jsonDetailsArray = object.getJSONArray("discussList");
                int jsonDetailsArrayNum = jsonDetailsArray.length();
                if (jsonDetailsArrayNum > 0) {
                    for (int i = 0; i < jsonDetailsArrayNum; i++) {
                        TaskDetailInfo.DiscussInfo mDiscussInfo = new TaskDetailInfo.DiscussInfo();
                        String mDiscussContent = "", mCreateTime = "", mCreateUserName = "", mCreateUserImg = "";
                        JSONObject jsonDetailObj = jsonDetailsArray.getJSONObject(i);
                        if (HcUtil.hasValue(jsonDetailObj, "discussContent")) {
                            mDiscussContent = jsonDetailObj.getString("discussContent");
                            mDiscussInfo.setmContent(mDiscussContent);
                        }

                        if (HcUtil.hasValue(jsonDetailObj, "createUserName")) {
                            mCreateUserName = jsonDetailObj.getString("createUserName");
                            //此处是否要通过id得到名称
                            mDiscussInfo.setmName(mCreateUserName);
                        }
                        if (HcUtil.hasValue(jsonDetailObj, "createTime")) {
                            mCreateTime = jsonDetailObj.getString("createTime");
                            //此处是否要通过id得到名称
                            mDiscussInfo.setmDate(mCreateTime);
                        }
                        if (HcUtil.hasValue(jsonDetailObj, "createUserImg")) {
                            mCreateUserImg = jsonDetailObj.getString("createUserImg");
                            //此处是否要通过id得到名称
                            mDiscussInfo.setmUrl(mCreateUserImg);
                        }
                        mTasks.add(mDiscussInfo);
                    }
                }
            } catch (JSONException e) {
                HcLog.D(TAG + " #onSuccess parseJson error =" + e);
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mDetailAdapter == null) {
                        // 实例化自定义的TaskDetailAdapter
                        mDetailAdapter = new TaskDetailAdapter(TaskDetailsActivity.this, mTasks);
                        // 绑定Adapter
                        mTaskDetailsList.setAdapter(mDetailAdapter);
                    } else {
                        mDetailAdapter.notifyDataSetChanged();
                    }

                }
            });
        }

        @Override
        public String getTag() {
            return null;
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcDialog.deleteProgressDialog();
            HcUtil.reLogining(data, TaskDetailsActivity.this, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

        }
    }

    private class TaskDetailsListResponse extends AbstractHttpResponse {
        private static final String TAG = TaskDetailsActivity.TAG + "$TaskDetailsResponse";

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            HcDialog.deleteProgressDialog();
            mTasks.clear();

            try {
                JSONObject object = new JSONObject((String) data);
                JSONArray jsonDetailsArray = object.getJSONArray("discussList");
                int jsonDetailsArrayNum = jsonDetailsArray.length();
                JSONObject jsonTaskObj = object.getJSONObject("task");
                if (HcUtil.hasValue(jsonTaskObj, "taskId")) {
                    taskId = jsonTaskObj.getString("taskId");
                }
                if (HcUtil.hasValue(jsonTaskObj, "createUserId")) {
                    tastDetailsPublisherId = jsonTaskObj.getString("createUserId");
                }
                if (HcUtil.hasValue(jsonTaskObj, "createUserName")) {
                    tastDetailsPublisher = jsonTaskObj.getString("createUserName");
                }
                if (HcUtil.hasValue(jsonTaskObj, "createUserImg")) {
                    tastDetailsPublisherImg = jsonTaskObj.getString("createUserImg");
                }
                if (HcUtil.hasValue(jsonTaskObj, "taskContent")) {
                    tastDetailsContent = jsonTaskObj.getString("taskContent");
                }
                if (HcUtil.hasValue(jsonTaskObj, "executeUserId")) {
                    tastDetailsExecuteId = jsonTaskObj.getString("executeUserId");
                }
                if (HcUtil.hasValue(jsonTaskObj, "executeUserName")) {
                    tastDetailsExecutor = jsonTaskObj.getString("executeUserName");
                }
                if (HcUtil.hasValue(jsonTaskObj, "executeUserImg")) {
                    tastDetailsExecutorImg = jsonTaskObj.getString("executeUserImg");
                }
                if (HcUtil.hasValue(jsonTaskObj, "executeUserImg")) {
                    tastDetailsDeadLine = jsonTaskObj.getString("deadline");
                }
                if (jsonDetailsArrayNum > 0) {
                    for (int i = 0; i < jsonDetailsArrayNum; i++) {
                        TaskDetailInfo.DiscussInfo mDiscussInfo = new TaskDetailInfo.DiscussInfo();
                        String mDiscussContent = "", mCreateTime = "", mCreateUserName = "", mCreateUserImg = "";
                        JSONObject jsonDetailObj = jsonDetailsArray.getJSONObject(i);
                        if (HcUtil.hasValue(jsonDetailObj, "discussContent")) {
                            mDiscussContent = jsonDetailObj.getString("discussContent");
                            mDiscussInfo.setmContent(mDiscussContent);
                        }
                        if (HcUtil.hasValue(jsonDetailObj, "createUserName")) {
                            mCreateUserName = jsonDetailObj.getString("createUserName");
                            //此处是否要通过id得到名称
                            mDiscussInfo.setmName(mCreateUserName);
                        }
                        if (HcUtil.hasValue(jsonDetailObj, "createTime")) {
                            mCreateTime = jsonDetailObj.getString("createTime");
                            //此处是否要通过id得到名称
                            mDiscussInfo.setmDate(mCreateTime);
                        }
                        if (HcUtil.hasValue(jsonDetailObj, "createUserImg")) {
                            mCreateUserImg = jsonDetailObj.getString("createUserImg");
                            //此处是否要通过id得到名称
                            mDiscussInfo.setmUrl(mCreateUserImg);
                        }
                        mTasks.add(mDiscussInfo);
                    }
                }
            } catch (JSONException e) {
                HcLog.D(TAG + " #onSuccess parseJson error =" + e);
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!TextUtils.isEmpty(tastDetailsPublisherImg))
                        mImageLoader.displayImage(tastDetailsPublisherImg, mTaskDetailsPublishIcon, mOptions);
                    if (!TextUtils.isEmpty(tastDetailsExecutorImg))
                        mImageLoader.displayImage(tastDetailsExecutorImg, mTaskDetailsExecutorIcon, mOptions);
                    if (!"".equals(tastDetailsPublisher) && tastDetailsPublisher != null) {
                        mTaskDetailsPublishName.setText(tastDetailsPublisher);
                    }
                    if (!"".equals(tastDetailsExecutor) && tastDetailsExecutor != null) {
                        mTaskDetailsExecutorName.setText(tastDetailsExecutor);
                    }
                    if (!"".equals(tastDetailsDeadLine) && tastDetailsDeadLine != null)
                        mTaskDetailsTime.setText("截止日期:" + tastDetailsDeadLine);
                    if (!"".equals(tastDetailsContent) && tastDetailsContent != null) {
                        mTaskDetailsContent.setText(tastDetailsContent);
                    }
                    if (mDetailAdapter == null) {
                        // 实例化自定义的TaskDetailAdapter
                        mDetailAdapter = new TaskDetailAdapter(TaskDetailsActivity.this, mTasks);
                        // 绑定Adapter
                        mTaskDetailsList.setAdapter(mDetailAdapter);
                    } else {
                        mDetailAdapter.notifyDataSetChanged();
                    }

                }
            });
        }

        @Override
        public String getTag() {
            return null;
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcDialog.deleteProgressDialog();
            HcUtil.reLogining(data, TaskDetailsActivity.this, msg);
        }

        @Override
        public void onNetworkInterrupt(RequestCategory request) {
            super.onNetworkInterrupt(request);
        }

        @Override
        public void onConnectionTimeout(RequestCategory request) {
            super.onConnectionTimeout(request);
        }

        @Override
        public void onParseDataError(RequestCategory request) {
            super.onParseDataError(request);
        }

        @Override
        public void onRequestFailed(int code, String msg, RequestCategory request) {
            super.onRequestFailed(code, msg, request);
        }

        @Override
        public void onResponseFailed(int code, RequestCategory request) {
            super.onResponseFailed(code, request);
        }

        @Override
        public void unknown(RequestCategory request) {
            super.unknown(request);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

        }
    }

    private class CallReceiverTaskRequest extends AbstractHttpRequest {
        private static final String TAG = TaskDetailsActivity.TAG + "$TaskDetailsRequest";
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

        private static final String TAG = TaskDetailsActivity.TAG + "$TaskDetailsResponse ";

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            HcDialog.deleteProgressDialog();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    HcUtil.showToast(TaskDetailsActivity.this, "任务提醒成功!");
                    TaskDetailsActivity.this.setResult(RESULT_OK);
                    finish();
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
            HcUtil.reLogining(data, TaskDetailsActivity.this, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
            HcLog.D(TAG + " #notifyRequestMd5Url md5Url = " + md5Url + " request = " + request);
        }

    }

    private class ReceiverTaskRequest extends AbstractHttpRequest {

        private final TaskState mTask;

        public ReceiverTaskRequest(TaskState task) {
            mTask = task;
        }

        @Override
        public String getParameterUrl() {
            return "?userId=" + SettingHelper.getUserId(TaskDetailsActivity.this)
                    + "&taskId=" + mTask.getId() + "&status=1";
        }

        @Override
        public String getRequestMethod() {
            return "updatetaskstatus";
        }
    }

    private class ReceiverTaskResponse extends AbstractHttpResponse {

        private static final String TAG = TaskDetailsActivity.TAG + "$TaskDetailsResponse";

        private final String mTaskId;

        /**
         * @param taskId 需要处理的任务的Id
         */
        public ReceiverTaskResponse(String taskId) {
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
//            task.setmTaskOperator(mTaskOperator);
            task.setId(mTaskId);
            Bundle bundle = new Bundle();
            bundle.putParcelable("task", task);
            bundle.putInt("status", TaskState.STATUS_PROCESSING);
            TaskDetailsActivity.this.setResult(RESULT_OK, TaskDetailsActivity.this.getIntent().putExtras(bundle));
            finish();
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcDialog.deleteProgressDialog();
            HcUtil.reLogining(data, TaskDetailsActivity.this, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

        }
    }

    private class CommitTaskRequest extends AbstractHttpRequest {

        private final TaskState mTask;

        public CommitTaskRequest(TaskState task) {
            mTask = task;
        }

        @Override
        public String getParameterUrl() {
            return "?userId=" + SettingHelper.getUserId(TaskDetailsActivity.this)
                    + "&taskId=" + mTask.getId() + "&status=2";
        }

        @Override
        public String getRequestMethod() {
            return "updatetaskstatus";
        }
    }

    private class CommitTaskResponse extends AbstractHttpResponse {

        private static final String TAG = TaskDetailsActivity.TAG + "$TaskDetailsResponse";

        private final String mTaskId;

        /**
         * @param taskId 需要处理的任务的Id
         */
        public CommitTaskResponse(String taskId) {
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
//            task.setmTaskOperator(mTaskOperator);
            task.setId(mTaskId);
            Bundle bundle = new Bundle();
            bundle.putParcelable("task", task);
            bundle.putInt("status", TaskState.STATUS_COMPLETED);
            TaskDetailsActivity.this.setResult(RESULT_OK, TaskDetailsActivity.this.getIntent().putExtras(bundle));
            finish();
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcDialog.deleteProgressDialog();
            HcUtil.reLogining(data, TaskDetailsActivity.this, msg);
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
            return "?userId=" + SettingHelper.getUserId(TaskDetailsActivity.this)
                    + "&taskId=" + mTask.getId() + "&status=3";
        }

        @Override
        public String getRequestMethod() {
            return "updatetaskstatus";
        }
    }

    private class EndTaskResponse extends AbstractHttpResponse {

        private static final String TAG = TaskDetailsActivity.TAG + "$TaskDetailsResponse";

        private final String mTaskId;

        /**
         * @param taskId 需要处理的任务的Id
         */
        public EndTaskResponse(String taskId) {
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
//            task.setmTaskOperator(mTaskOperator);
            task.setId(mTaskId);
            Bundle bundle = new Bundle();
            bundle.putParcelable("task", task);
            bundle.putInt("status", TaskState.STATUS_END);
            TaskDetailsActivity.this.setResult(RESULT_OK, TaskDetailsActivity.this.getIntent().putExtras(bundle));
            finish();
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcDialog.deleteProgressDialog();
            HcUtil.reLogining(data, TaskDetailsActivity.this, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

        }
    }

}
