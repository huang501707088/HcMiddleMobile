package com.android.hcframe.hctask;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.container.ContainerCircleImageView;
import com.android.hcframe.view.datepicker.DatePickerDialog;
import com.android.hcframe.hctask.state.TaskState;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.sql.SettingHelper;
import com.android.hcframe.view.selector.HcChooseHomeView;
import com.android.hcframe.view.selector.ItemInfo;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pc on 2016/8/5.
 */
public class TaskChangeActivity extends HcBaseActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    private static final String TAG = "TaskChangeActivity";
    private EditText mTaskChangeContent;
    private TextView mTaskChangeDeadtime;
    private TextView mTaskChangeExecutor;
    private EditText mTaskChangeReason;
    private TextView mTaskChangeSubmit;
    private TextView mTaskDeleteSubmit;
    private TopBarView mTopBarView;
    private ContainerCircleImageView mTaskChangeImg;
    private String content = "", deadTime = "", executor = "", taskId = "", executorUrl = "", executorId = "";
    private String mContent = "", mDeadTime = "", mExecutor = "", mReason = "", mIsUpdateExecuteUser = "", mExecutorId = "";
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;
    private DatePickerDialog datePickerDialog;
    public static final String DATEPICKER_TAG = "datepicker";
    public static final String ITEM_KEY = "click_info";
    /**
     * 提交发布数据
     */
    private TaskChangeResponse cResponse;
    private Handler mHandler = new Handler();
    /**
     * 该变量用来判断是否是TaskDetailActivity进入
     */
    String taskDetail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_change_layout);
        mTaskChangeContent = (EditText) findViewById(R.id.task_change_content);
        mTaskChangeDeadtime = (TextView) findViewById(R.id.task_change_deadtime);
        mTaskChangeExecutor = (TextView) findViewById(R.id.task_change_executor);
        mTaskChangeReason = (EditText) findViewById(R.id.task_change_why);
        mTaskChangeSubmit = (TextView) findViewById(R.id.task_change_submit);
        mTaskDeleteSubmit = (TextView) findViewById(R.id.task_change_delete);
        mTopBarView = (TopBarView) findViewById(R.id.task_change_topview);
        mTaskChangeImg = (ContainerCircleImageView) findViewById(R.id.task_change_img);
        /**
         * 用来加载网络图片
         * */
        mOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageOnLoading(R.drawable.default_icon)
                .showImageForEmptyUri(R.drawable.default_icon)
                .showImageOnFail(R.drawable.default_icon).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888).build();
        mImageLoader = ImageLoader.getInstance();
        final Calendar calendar = Calendar.getInstance();
        datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
        initData();
        mTopBarView.setTitle("变更任务");
        mTaskChangeSubmit.setOnClickListener(this);
        mTaskDeleteSubmit.setOnClickListener(this);
        mTaskChangeDeadtime.setOnClickListener(this);
        /**
         * 点击执行人编辑框
         * */
        mTaskChangeExecutor.setOnClickListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            TaskState task = intent.getParcelableExtra("task");
            taskId = task.getId();
            content = task.getDescription();
            deadTime = task.getEndDate();
            executor = task.getExecutor();
            executorUrl = task.getExecutorUrl();
            mExecutorId = task.getExecutorId();
            if (!"".equals(executorUrl) && executorUrl != null)
                mImageLoader.displayImage(executorUrl, mTaskChangeImg, mOptions);
            if (!"".equals(content) && content != null)
                mTaskChangeContent.setText(content);
            if (!"".equals(deadTime) && deadTime != null)
                mTaskChangeDeadtime.setText(deadTime);
            if (!"".equals(executor) && executor != null) {
                mTaskChangeExecutor.setText(executor);
            }
            taskDetail = intent.getStringExtra("taskDetail");
            mTaskChangeContent.setSelection(mTaskChangeContent.getText().length());
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
        int i = v.getId();
        if (i == R.id.task_change_submit) {
            mContent = mTaskChangeContent.getText().toString().trim();
            mDeadTime = mTaskChangeDeadtime.getText().toString().trim();
            mReason = mTaskChangeReason.getText().toString().trim();
            mExecutor = mTaskChangeExecutor.getText().toString().trim();
            //假如截止日期小于今天的日期，则提示个用户不能提交
            String[] mDeadTimes = mDeadTime.split("-");
            //获取当前系统时间
            Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH) + 1;
            int mDay = c.get(Calendar.DAY_OF_MONTH);
            if (Integer.valueOf(mDeadTimes[0]) < mYear) {
                HcUtil.showToast(this, "截止日期不能早于今天!");
            } else {
                if (Integer.valueOf(mDeadTimes[1]) < mMonth) {
                    HcUtil.showToast(this, "截止日期不能早于今天!");
                } else if (Integer.valueOf(mDeadTimes[2]) < mDay) {
                    HcUtil.showToast(this, "截止日期不能早于今天!");
                } else {
                    if (!mExecutor.equals(executor)) {
                        mIsUpdateExecuteUser = "1";
                    } else {
                        mIsUpdateExecuteUser = "0";
                    }
                    if (!TextUtils.isEmpty(taskId) && !TextUtils.isEmpty(mContent) && !TextUtils.isEmpty(mDeadTime) && !TextUtils.isEmpty(mExecutorId) && !TextUtils.isEmpty(mReason)) {
                        TaskChangeRequest cRequest = new TaskChangeRequest(taskId, mContent, mDeadTime, mExecutorId, mReason, mIsUpdateExecuteUser);
                        if (cResponse == null) {
                            if ("taskDetail".equals(taskDetail)) {
                                cResponse = new TaskChangeResponse(taskDetail);
                            } else {
                                cResponse = new TaskChangeResponse();
                            }
                        }
                        HcDialog.showProgressDialog(TaskChangeActivity.this, "提交数据中");
                        cRequest.sendRequestCommand(RequestCategory.NONE, cResponse, false);
                    } else {
                        if (TextUtils.isEmpty(mContent)) {
                            HcUtil.showToast(this, "请输入任务内容!");
                        } else if (TextUtils.isEmpty(mDeadTime)) {
                            HcUtil.showToast(this, "请输入截止日期!");
                        } else if (TextUtils.isEmpty(mExecutor)) {
                            HcUtil.showToast(this, "请输入任务执行人!");
                        } else if (TextUtils.isEmpty(mReason)) {
                            HcUtil.showToast(this, "请输入变更原因!");
                        }
                    }
                }
            }

        } else if (i == R.id.task_change_delete) {
            //提交数据到服务端，出现加载的提示对话框，用户不可以操作
            TaskDeleteRequest lRequest = new TaskDeleteRequest(taskId);
            TaskDeleteResponse lResponse = null;
            if (lResponse == null) {
                lResponse = new TaskDeleteResponse(taskId, taskDetail);
            }
            HcDialog.showProgressDialog(TaskChangeActivity.this, "删除数据中");
            lRequest.sendRequestCommand(RequestCategory.NONE, lResponse, false);
        } else if (i == R.id.task_change_deadtime) {
            //弹出日历框
            datePickerDialog.setVibrate(false);
            datePickerDialog.setYearRange(1985, 2028);
            datePickerDialog.setCloseOnSingleTapDay(false);
            datePickerDialog.show(getFragmentManager(), DATEPICKER_TAG);
        } else if (i == R.id.task_change_executor) {
            //点击执行人框选择执行人
            Intent intent = new Intent(this, ChoosePersonnelActivity.class);
            startActivityForResult(intent, HcChooseHomeView.REQUEST_CODE);
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        //requestCode标示请求的标示   resultCode表示有数据
        if (requestCode == HcChooseHomeView.REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                ItemInfo mItemInfo = data.getParcelableExtra(ITEM_KEY);
                mExecutor = mItemInfo.getItemValue();
                mExecutorId = mItemInfo.getUserId();
                String mUserImg = mItemInfo.getIconUrl();
                ImageLoader.getInstance().displayImage(mUserImg,
                        mTaskChangeImg, mOptions);
                mTaskChangeExecutor.setText(mExecutor);
            }
        }
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        mTaskChangeDeadtime.setTextColor(Color.parseColor("#333333"));
        //获取当前系统时间
        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH) + 1;
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        if (mYear == year) {
            if (mMonth == (month + 1)) {
                if (mDay <= day) {
                    if (day < 10) {
                        String days = "0" + day;
                        if ((month + 1) < 10) {
                            String months = "0" + String.valueOf(month + 1);
                            mTaskChangeDeadtime.setText(year + "-" + months + "-" + days);
                        } else {
                            mTaskChangeDeadtime.setText(year + "-" + (month + 1) + "-" + days);
                        }
                    } else {
                        if ((month + 1) < 10) {
                            String months = "0" + String.valueOf(month + 1);
                            mTaskChangeDeadtime.setText(year + "-" + months + "-" + day);
                        } else {
                            mTaskChangeDeadtime.setText(year + "-" + (month + 1) + "-" + day);
                        }
                    }
                }
            } else if (mMonth < (month + 1)) {
                if (day < 10) {
                    String days = "0" + day;
                    if ((month + 1) < 10) {
                        String months = "0" + String.valueOf(month + 1);
                        mTaskChangeDeadtime.setText(year + "-" + months + "-" + days);
                    } else {
                        mTaskChangeDeadtime.setText(year + "-" + (month + 1) + "-" + days);
                    }
                } else {
                    if ((month + 1) < 10) {
                        String months = "0" + String.valueOf(month + 1);
                        mTaskChangeDeadtime.setText(year + "-" + months + "-" + day);
                    } else {
                        mTaskChangeDeadtime.setText(year + "-" + (month + 1) + "-" + day);
                    }
                }
            }
        } else if (mYear < year) {
            //do nothing
            if (mMonth == (month + 1)) {
                if (mDay <= day) {
                    if (day < 10) {
                        String days = "0" + day;
                        if ((month + 1) < 10) {
                            String months = "0" + String.valueOf(month + 1);
                            mTaskChangeDeadtime.setText(year + "-" + months + "-" + days);
                        } else {
                            mTaskChangeDeadtime.setText(year + "-" + (month + 1) + "-" + days);
                        }
                    } else {
                        if ((month + 1) < 10) {
                            String months = "0" + String.valueOf(month + 1);
                            mTaskChangeDeadtime.setText(year + "-" + months + "-" + day);
                        } else {
                            mTaskChangeDeadtime.setText(year + "-" + (month + 1) + "-" + day);
                        }
                    }
                }
            } else if (true) {
                if (day < 10) {
                    String days = "0" + day;
                    if ((month + 1) < 10) {
                        String months = "0" + String.valueOf(month + 1);
                        mTaskChangeDeadtime.setText(year + "-" + months + "-" + days);
                    } else {
                        mTaskChangeDeadtime.setText(year + "-" + (month + 1) + "-" + days);
                    }
                } else {
                    if ((month + 1) < 10) {
                        String months = "0" + String.valueOf(month + 1);
                        mTaskChangeDeadtime.setText(year + "-" + months + "-" + day);
                    } else {
                        mTaskChangeDeadtime.setText(year + "-" + (month + 1) + "-" + day);
                    }
                }
            }
        }
    }

    private class TaskChangeRequest extends AbstractHttpRequest {
        private static final String TAG = TaskChangeActivity.TAG + "$TaskChangeActivity";
        Map<String, String> httpparams = new HashMap<String, String>();

        public TaskChangeRequest(String taskId, String content, String deadTime, String executorId, String reason, String isUpdateExecuteUser) {
            httpparams.put("taskId", taskId);
            httpparams.put("taskContent", content);
            httpparams.put("deadline", deadTime);
            httpparams.put("executeUserId", executorId);
            httpparams.put("reason", reason);
            httpparams.put("isUpdateExecuteUser", isUpdateExecuteUser);

        }

        @Override
        public String getRequestMethod() {
            return "updatetask";
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

    private class TaskChangeResponse extends AbstractHttpResponse {

        private static final String TAG = TaskChangeActivity.TAG + "$TaskChangeActivity";
        private String mTaskDetail = "";

        public TaskChangeResponse() {
        }

        public TaskChangeResponse(String taskDetail) {
            mTaskDetail = taskDetail;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            HcDialog.deleteProgressDialog();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    HcUtil.showToast(getApplicationContext(), "任务变更成功!");
                    if (mTaskDetail.equals("taskDetail")) {
                        Bundle bundle = new Bundle();
                        bundle.putString("taskChange", "taskChange");
                        TaskChangeActivity.this.setResult(RESULT_OK, TaskChangeActivity.this.getIntent().putExtras(bundle));
                        TaskChangeActivity.this.finish();
                    } else {
                        TaskChangeActivity.this.setResult(RESULT_OK);
                        TaskChangeActivity.this.finish();
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
            HcUtil.reLogining(data, TaskChangeActivity.this, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
            HcLog.D(TAG + " #notifyRequestMd5Url md5Url = " + md5Url + " request = " + request);
        }

    }

    private class TaskDeleteRequest extends AbstractHttpRequest {

        private String mTaskId;

        public TaskDeleteRequest(String taskId) {
            mTaskId = taskId;
        }

        @Override
        public String getParameterUrl() {
            return "?userId=" + SettingHelper.getUserId(TaskChangeActivity.this)
                    + "&taskId=" + mTaskId + "&status=4";
        }

        @Override
        public String getRequestMethod() {
            return "updatetaskstatus";
        }
    }

    private class TaskDeleteResponse extends AbstractHttpResponse {

        private static final String TAG = TaskChangeActivity.TAG + "$TaskDeleteResponse";

        private final String mTaskId;
        private String mTaskDetail;

        /**
         * @param taskId 需要处理的任务的Id
         */
        public TaskDeleteResponse(String taskId, String taskDetail) {
            mTaskDetail = taskDetail;
            mTaskId = taskId;
        }

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            //返回到首页，并传递taskId,让首页的任务消失
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    HcUtil.showToast(getApplicationContext(), "任务删除成功!");
                    if (!"".equals(mTaskDetail) && mTaskDetail != null) {
                        if (mTaskDetail.equals("taskDetail")) {
                            Bundle bundle = new Bundle();
                            bundle.putString("taskChange", "taskChange");
                            TaskChangeActivity.this.setResult(RESULT_OK, TaskChangeActivity.this.getIntent().putExtras(bundle));
                            TaskChangeActivity.this.finish();
                        }
                    } else {
                        TaskChangeActivity.this.setResult(RESULT_OK);
                        TaskChangeActivity.this.finish();
                    }
                }
            });
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcDialog.deleteProgressDialog();
            HcUtil.reLogining(data, TaskChangeActivity.this, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

        }
    }

}
