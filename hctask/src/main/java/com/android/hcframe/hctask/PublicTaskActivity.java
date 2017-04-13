package com.android.hcframe.hctask;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.view.datepicker.DatePickerDialog;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.view.selector.HcChooseHomeView;
import com.android.hcframe.view.selector.ItemInfo;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pc on 2016/8/4.
 */
public class PublicTaskActivity extends HcBaseActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    private static final String TAG = "PublicTaskActivity";
    private EditText mTaskReleaseContent;
    private TextView mTaskReleaseDeadtime;
    private TextView mTaskReleaseExecutor;
    private TextView mTaskReleaseSubmit;
    private String mContent = "";
    private String mDeadTime = "";
    private String mExecutor = "";
    private String mExecutorId = "";
    private TopBarView mTopBarView;
    private DatePickerDialog datePickerDialog;
    public static final String DATEPICKER_TAG = "datepicker";
    public static final String ITEM_KEY = "click_info";
    /**
     * 提交发布数据
     */
    private PublishTaskResponse pResponse;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            mContent = intent.getStringExtra("content");
            mExecutorId = intent.getStringExtra("userId");
            mExecutor = intent.getStringExtra("name");
        }
        setContentView(R.layout.task_publish_layout);
        mTaskReleaseContent = (EditText) findViewById(R.id.task_release_content);
        mTaskReleaseDeadtime = (TextView) findViewById(R.id.task_release_deadtime);
        mTaskReleaseExecutor = (TextView) findViewById(R.id.task_release_executor);
        mTaskReleaseSubmit = (TextView) findViewById(R.id.task_release_submit);
        mTopBarView = (TopBarView) findViewById(R.id.task_publish_topview);
        final Calendar calendar = Calendar.getInstance();
        datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
        mTopBarView.setTitle("发布新任务");
        mTaskReleaseSubmit.setOnClickListener(this);
        mTaskReleaseDeadtime.setOnClickListener(this);

        if (!TextUtils.isEmpty(mContent)) {
            mTaskReleaseContent.setText(mContent);
            mTaskReleaseContent.setSelection(mContent.length());
        }
        if (!TextUtils.isEmpty(mExecutor)) {
            mTaskReleaseExecutor.setTextColor(Color.parseColor("#333333"));
            mTaskReleaseExecutor.setText(mExecutor);
            mTaskReleaseExecutor.setEnabled(false);
        } else {
            /**
             * 选择执行人
             * */
            mTaskReleaseExecutor.setOnClickListener(this);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.task_release_submit) {
            //点击确认发布按钮，将数据传递给服务端，并跳转到首页
            mContent = mTaskReleaseContent.getText().toString().trim();
            mDeadTime = mTaskReleaseDeadtime.getText().toString().trim();
            if (!TextUtils.isEmpty(mContent) && !mDeadTime.equals("请填写截止日期") && !TextUtils.isEmpty(mExecutor)) {
                PublishTaskRequest pRequest = new PublishTaskRequest(mContent, mDeadTime, mExecutorId);
                if (pResponse == null) {
                    pResponse = new PublishTaskResponse();
                }
                HcDialog.showProgressDialog(PublicTaskActivity.this, "提交数据中");
                pRequest.sendRequestCommand(RequestCategory.NONE, pResponse, false);
            } else {
                if (TextUtils.isEmpty(mContent)) {
                    HcUtil.showToast(this, "请输入任务内容!");
                } else if (mDeadTime.equals("请填写截止日期")) {
                    HcUtil.showToast(this, "请输入截止日期!");
                } else if (TextUtils.isEmpty(mExecutor)) {
                    HcUtil.showToast(this, "请输入任务执行人!");
                }
            }
        } else if (i == R.id.task_release_deadtime) {
            //弹出日历框
            datePickerDialog.setVibrate(false);
            datePickerDialog.setYearRange(1985, 2028);
            datePickerDialog.setCloseOnSingleTapDay(false);
            datePickerDialog.show(getFragmentManager(), DATEPICKER_TAG);
        } else if (i == R.id.task_release_executor) {
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
                mExecutorId = mItemInfo.getUserId();
                mExecutor = mItemInfo.getItemValue();
                mTaskReleaseExecutor.setTextColor(Color.parseColor("#333333"));
                mTaskReleaseExecutor.setText(mExecutor);
            }
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
                            mTaskReleaseDeadtime.setTextColor(Color.parseColor("#333333"));
                            mTaskReleaseDeadtime.setText(year + "-" + months + "-" + days);
                        } else {
                            mTaskReleaseDeadtime.setTextColor(Color.parseColor("#333333"));
                            mTaskReleaseDeadtime.setText(year + "-" + (month + 1) + "-" + days);
                        }
                    } else {
                        if ((month + 1) < 10) {
                            String months = "0" + String.valueOf(month + 1);
                            mTaskReleaseDeadtime.setTextColor(Color.parseColor("#333333"));
                            mTaskReleaseDeadtime.setText(year + "-" + months + "-" + day);
                        } else {
                            mTaskReleaseDeadtime.setTextColor(Color.parseColor("#333333"));
                            mTaskReleaseDeadtime.setText(year + "-" + (month + 1) + "-" + day);
                        }
                    }
                }
            }else if(mMonth< (month + 1)){
                if (day < 10) {
                    String days = "0" + day;
                    if ((month + 1) < 10) {
                        String months = "0" + String.valueOf(month + 1);
                        mTaskReleaseDeadtime.setTextColor(Color.parseColor("#333333"));
                        mTaskReleaseDeadtime.setText(year + "-" + months + "-" + days);
                    } else {
                        mTaskReleaseDeadtime.setTextColor(Color.parseColor("#333333"));
                        mTaskReleaseDeadtime.setText(year + "-" + (month + 1) + "-" + days);
                    }
                } else {
                    if ((month + 1) < 10) {
                        String months = "0" + String.valueOf(month + 1);
                        mTaskReleaseDeadtime.setTextColor(Color.parseColor("#333333"));
                        mTaskReleaseDeadtime.setText(year + "-" + months + "-" + day);
                    } else {
                        mTaskReleaseDeadtime.setTextColor(Color.parseColor("#333333"));
                        mTaskReleaseDeadtime.setText(year + "-" + (month + 1) + "-" + day);
                    }
                }
            }

        } else if(mYear < year){
            if (mMonth == (month + 1)) {
                if (mDay <= day) {
                    if (day < 10) {
                        String days = "0" + day;
                        if ((month + 1) < 10) {
                            String months = "0" + String.valueOf(month + 1);
                            mTaskReleaseDeadtime.setTextColor(Color.parseColor("#333333"));
                            mTaskReleaseDeadtime.setText(year + "-" + months + "-" + days);
                        } else {
                            mTaskReleaseDeadtime.setTextColor(Color.parseColor("#333333"));
                            mTaskReleaseDeadtime.setText(year + "-" + (month + 1) + "-" + days);
                        }
                    } else {
                        if ((month + 1) < 10) {
                            String months = "0" + String.valueOf(month + 1);
                            mTaskReleaseDeadtime.setTextColor(Color.parseColor("#333333"));
                            mTaskReleaseDeadtime.setText(year + "-" + months + "-" + day);
                        } else {
                            mTaskReleaseDeadtime.setTextColor(Color.parseColor("#333333"));
                            mTaskReleaseDeadtime.setText(year + "-" + (month + 1) + "-" + day);
                        }
                    }
                }
            }else if(true){
                if (day < 10) {
                    String days = "0" + day;
                    if ((month + 1) < 10) {
                        String months = "0" + String.valueOf(month + 1);
                        mTaskReleaseDeadtime.setTextColor(Color.parseColor("#333333"));
                        mTaskReleaseDeadtime.setText(year + "-" + months + "-" + days);
                    } else {
                        mTaskReleaseDeadtime.setTextColor(Color.parseColor("#333333"));
                        mTaskReleaseDeadtime.setText(year + "-" + (month + 1) + "-" + days);
                    }
                } else {
                    if ((month + 1) < 10) {
                        String months = "0" + String.valueOf(month + 1);
                        mTaskReleaseDeadtime.setTextColor(Color.parseColor("#333333"));
                        mTaskReleaseDeadtime.setText(year + "-" + months + "-" + day);
                    } else {
                        mTaskReleaseDeadtime.setTextColor(Color.parseColor("#333333"));
                        mTaskReleaseDeadtime.setText(year + "-" + (month + 1) + "-" + day);
                    }
                }
            }

        }
    }

    private class PublishTaskRequest extends AbstractHttpRequest {
        private static final String TAG = PublicTaskActivity.TAG + "$PublicTaskActivity";
        Map<String, String> httpparams = new HashMap<String, String>();

        public PublishTaskRequest(String content, String deadTime, String executor) {
            httpparams.put("taskContent", content);
            httpparams.put("deadline", deadTime);
            httpparams.put("executeUserId", executor);
        }

        @Override
        public String getRequestMethod() {
            return "addtask";
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

    private class PublishTaskResponse extends AbstractHttpResponse {

        private static final String TAG = PublicTaskActivity.TAG + "$PublicTaskActivity";


        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            HcDialog.deleteProgressDialog();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    HcUtil.showToast(getApplicationContext(), "任务发布成功");
                    PublicTaskActivity.this.setResult(RESULT_OK);
                    PublicTaskActivity.this.finish();
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
            HcUtil.reLogining(data, PublicTaskActivity.this, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
            HcLog.D(TAG + " #notifyRequestMd5Url md5Url = " + md5Url + " request = " + request);
        }

    }
}
