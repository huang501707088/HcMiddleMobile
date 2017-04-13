package com.android.hcframe.hctask;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.view.datepicker.DatePickerDialog;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.RequestCategory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pc on 2016/8/5.
 */
public class TaskBackActivity extends HcBaseActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    private static final String TAG = "TaskBackActivity";
    private EditText mTaskBackReason;
    private TextView mTaskBackDeadtime;
    private TextView mTaskBackSubmit;
    private String mTaskId = "";
    private String deadTime = "";
    private String mReason = "";
    private String mDeadTime = "";
    private TopBarView mTopBarView;
    private DatePickerDialog datePickerDialog;
    public static final String DATEPICKER_TAG = "datepicker";
    private Handler mHandler = new Handler();
    /**
     * 退回整改请求
     */
    private TaskBackResponse bResponse;
    /**
     * 该变量用来判断是否是TaskDetailActivity进入
     */
    String taskDetail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_back_layout);
        mTaskBackReason = (EditText) findViewById(R.id.task_back_content);
        mTaskBackDeadtime = (TextView) findViewById(R.id.task_back_deadtime);
        mTaskBackSubmit = (TextView) findViewById(R.id.task_back_submit);
        mTopBarView = (TopBarView) findViewById(R.id.task_back_topview);
        final Calendar calendar = Calendar.getInstance();
        datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
        initData();
        mTopBarView.setTitle("退回整改");
        mTaskBackSubmit.setOnClickListener(this);
        mTaskBackDeadtime.setOnClickListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            mTaskId = intent.getStringExtra("taskId");
            deadTime = intent.getStringExtra("deadTime");
            if (!"".equals(deadTime) && deadTime != null)
                mTaskBackDeadtime.setText(deadTime);
            taskDetail = intent.getStringExtra("taskDetail");
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
        if (i == R.id.task_back_submit) {
            //点击确认发布按钮，将数据传递给服务端，并跳转到首页
            mReason = mTaskBackReason.getText().toString().trim();
            mDeadTime = mTaskBackDeadtime.getText().toString().trim();
            //获取当前系统时间
            Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH) + 1;
            int mDay = c.get(Calendar.DAY_OF_MONTH);
            //假如截止日期小于今天的日期，则提示个用户不能提交
            String[] mDeadTimes = mDeadTime.split("-");
            if (Integer.valueOf(mDeadTimes[0]) < mYear) {
                HcUtil.showToast(this, "截止日期不能早于今天!");
            } else {
                if (Integer.valueOf(mDeadTimes[1]) < mMonth) {
                    HcUtil.showToast(this, "截止日期不能早于今天!");
                } else if (Integer.valueOf(mDeadTimes[2]) < mDay) {
                    HcUtil.showToast(this, "截止日期不能早于今天!");
                } else {
                    if (!TextUtils.isEmpty(mReason) && !TextUtils.isEmpty(mDeadTime) && !TextUtils.isEmpty(mTaskId)) {
                        TaskBackRequest bRequest = new TaskBackRequest(mTaskId, mReason, mDeadTime);
                        if (bResponse == null) {
                            if ("taskDetail".equals(taskDetail)) {
                                bResponse = new TaskBackResponse(taskDetail);
                            } else {
                                bResponse = new TaskBackResponse();
                            }
                        }
                        HcDialog.showProgressDialog(TaskBackActivity.this, "退回整改中");
                        bRequest.sendRequestCommand(RequestCategory.NONE, bResponse, false);
                    } else {
                        if (TextUtils.isEmpty(mReason)) {
                            HcUtil.showToast(this, "请输入退回整改原因!");
                        } else if (TextUtils.isEmpty(mDeadTime)) {
                            HcUtil.showToast(this, "请输入截止日期!");
                        }
                    }
                }
            }
        } else if (i == R.id.task_back_deadtime) {
            //弹出日历框
            datePickerDialog.setVibrate(false);
            datePickerDialog.setYearRange(1985, 2028);
            datePickerDialog.setCloseOnSingleTapDay(false);
            datePickerDialog.show(getFragmentManager(), DATEPICKER_TAG);
        }
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
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
                            mTaskBackDeadtime.setText(year + "-" + months + "-" + days);
                        } else {
                            mTaskBackDeadtime.setText(year + "-" + (month + 1) + "-" + days);
                        }
                    } else {
                        if ((month + 1) < 10) {
                            String months = "0" + String.valueOf(month + 1);
                            mTaskBackDeadtime.setText(year + "-" + months + "-" + day);
                        } else {
                            mTaskBackDeadtime.setText(year + "-" + (month + 1) + "-" + day);
                        }
                    }
                }
            } else if (mMonth < (month + 1)) {
                if (day < 10) {
                    String days = "0" + day;
                    if ((month + 1) < 10) {
                        String months = "0" + String.valueOf(month + 1);
                        mTaskBackDeadtime.setText(year + "-" + months + "-" + days);
                    } else {
                        mTaskBackDeadtime.setText(year + "-" + (month + 1) + "-" + days);
                    }
                } else {
                    if ((month + 1) < 10) {
                        String months = "0" + String.valueOf(month + 1);
                        mTaskBackDeadtime.setText(year + "-" + months + "-" + day);
                    } else {
                        mTaskBackDeadtime.setText(year + "-" + (month + 1) + "-" + day);
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
                            mTaskBackDeadtime.setText(year + "-" + months + "-" + days);
                        } else {
                            mTaskBackDeadtime.setText(year + "-" + (month + 1) + "-" + days);
                        }
                    } else {
                        if ((month + 1) < 10) {
                            String months = "0" + String.valueOf(month + 1);
                            mTaskBackDeadtime.setText(year + "-" + months + "-" + day);
                        } else {
                            mTaskBackDeadtime.setText(year + "-" + (month + 1) + "-" + day);
                        }
                    }
                }
            } else if (true) {
                if (day < 10) {
                    String days = "0" + day;
                    if ((month + 1) < 10) {
                        String months = "0" + String.valueOf(month + 1);
                        mTaskBackDeadtime.setText(year + "-" + months + "-" + days);
                    } else {
                        mTaskBackDeadtime.setText(year + "-" + (month + 1) + "-" + days);
                    }
                } else {
                    if ((month + 1) < 10) {
                        String months = "0" + String.valueOf(month + 1);
                        mTaskBackDeadtime.setText(year + "-" + months + "-" + day);
                    } else {
                        mTaskBackDeadtime.setText(year + "-" + (month + 1) + "-" + day);
                    }
                }
            }
        }
    }

    private class TaskBackRequest extends AbstractHttpRequest {
        private static final String TAG = TaskBackActivity.TAG + "$TaskBackActivity";
        Map<String, String> httpparams = new HashMap<String, String>();

        public TaskBackRequest(String taskId, String reason, String deadTime) {
            httpparams.put("taskId", taskId);
            httpparams.put("discussContent", reason);
            httpparams.put("deadline", deadTime);
        }

        @Override
        public String getRequestMethod() {
            return "sendbacktask";
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

    private class TaskBackResponse extends AbstractHttpResponse {

        private static final String TAG = TaskBackActivity.TAG + "$TaskBackActivity";
        private String mTaskDetail = "";

        public TaskBackResponse() {
        }

        public TaskBackResponse(String taskDetail) {
            mTaskDetail = taskDetail;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            HcDialog.deleteProgressDialog();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    HcUtil.showToast(getApplicationContext(), "退回整改成功!");
                    if (mTaskDetail.equals("taskDetail")) {
                        Bundle bundle = new Bundle();
                        bundle.putString("taskBack", "taskBack");
                        TaskBackActivity.this.setResult(RESULT_OK, TaskBackActivity.this.getIntent().putExtras(bundle));
                        TaskBackActivity.this.finish();
                    } else {
                        TaskBackActivity.this.setResult(RESULT_OK);
                        TaskBackActivity.this.finish();
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
            HcUtil.reLogining(data, TaskBackActivity.this, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
            HcLog.D(TAG + " #notifyRequestMd5Url md5Url = " + md5Url + " request = " + request);
        }

    }
}
