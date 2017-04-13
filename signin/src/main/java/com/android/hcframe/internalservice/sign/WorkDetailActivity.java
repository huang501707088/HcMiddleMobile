package com.android.hcframe.internalservice.sign;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.hcframe.AbsActiviy;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.http.ResponseCodeInfo;
import com.android.hcframe.internalservice.signin.R;
import com.android.hcframe.zxing.camera.CameraManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 考勤详情
 *
 * @author czx
 */
public class WorkDetailActivity extends AbsActiviy implements OnClickListener, IHttpResponse {

    ListView id_workdetail_listview;
    TextView id_tv_my, id_tv_branch;
    private Context mContext;
    WorkDetailAdapter workDetailAdapter;
    JSONArray userList = null;

    @Override
    protected void onInitView() {
        setContentView(R.layout.workdetail);
        mContext = getApplicationContext();
        mTopBarView = (TopBarView) findViewById(R.id.center_top_bar);
        findViewById(R.id.id_rl_my).setOnClickListener(this);
        id_tv_my = (TextView) findViewById(R.id.id_tv_my);
        id_workdetail_listview = (ListView) findViewById(R.id.id_workdetail_listview);
        id_workdetail_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                JSONObject jsonObject = (JSONObject) adapterView.getAdapter().getItem(i);
                Intent intent = new Intent(WorkDetailActivity.this, MonthCalendarActivity.class);
                intent.putExtra("account", jsonObject.optString("user_name"));
                intent.putExtra("userId", jsonObject.optString("userid"));
                intent.putExtra("name", jsonObject.optString("empName"));
                startActivity(intent);


            }
        });
        id_tv_branch = (TextView) findViewById(R.id.id_tv_branch);
    }

    @Override
    protected void onInitData() {
        mTopBarView.setTitle("考勤详情");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        HcHttpRequest.getRequest().sendWorkDetailNowCommand(formatter.format(curDate), this);
        Intent intent = getIntent();
        String data = intent.getExtras().getString("data");
        JSONObject body;

        int lateCountTotal = 0, nosigncountTotal = 0, outCountTotal = 0, underNum = 0, askForLeave = 0;
        try {
            JSONObject object = new JSONObject(data.toString());
            body = object.optJSONObject("body");
            userList = body.optJSONArray("userList");
            lateCountTotal = body.optInt("lateCountTotal");//迟到
            nosigncountTotal = body.optInt("nosigncountTotal");//未签
            outCountTotal = body.optInt("outCountTotal");//外出
            underNum = userList.length();//下属人数
            askForLeave = body.optInt("leaveCountTotal");//请假
        } catch (JSONException e) {
            e.printStackTrace();
        }
        id_tv_my.setText("我的下属" + underNum + "人");
        id_tv_branch.setText("缺勤" + nosigncountTotal + "  迟/退" + (lateCountTotal) + "  外出" + outCountTotal + "  请假" + askForLeave);
        workDetailAdapter = new WorkDetailAdapter(mContext, userList);
        id_workdetail_listview.setAdapter(workDetailAdapter);
    }

    @Override
    protected void setPameter() {
        menuPage = "com.android.hcframe.internalservice.sign.WorkDetailActivity";
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {

        int i = view.getId();
        if (i == R.id.id_rl_my) {//我的考勤数据
            Intent intent = new Intent(this, MonthCalendarActivity.class);
            startActivity(intent);
        } else if (i == R.id.id_tv_cencle) {//取消
            finish();
        }
    }

    /**
     * @param data     返回的数据
     * @param request  请求的类型
     * @param category 返回的类型
     */
    @Override
    public void notify(Object data, RequestCategory request, ResponseCategory category) {
        if (request != null) {
            switch (request) {
                case WORKDETAIL:
                    if (data != null) {
                        switch (category) {
                            case SUCCESS:
                                HcLog.D(TAG + " ontify SUCCESS callback = ");
                                JSONObject body;

                                int lateCountTotal = 0, nosigncountTotal = 0, outCountTotal = 0, underNum = 0, askForLeave = 0;
                                try {
                                    HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
                                    JSONObject object = new JSONObject(data.toString());
                                    body = object.optJSONObject("body");
                                    userList = body.optJSONArray("userList");
                                    underNum = userList.length();//下属人数
                                    askForLeave = body.optInt("leaveCountTotal");//请假
                                    lateCountTotal = body.optInt("lateCountTotal");//迟到
                                    nosigncountTotal = body.optInt("nosigncountTotal");//未签
                                    outCountTotal = body.optInt("outCountTotal");//外出
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                id_tv_my.setText("我的下属" + underNum + "人");
                                id_tv_branch.setText("缺勤" + nosigncountTotal + "  迟/退" + (lateCountTotal) + "  外出" + outCountTotal + "  请假" + askForLeave);
                                workDetailAdapter = new WorkDetailAdapter(mContext, userList);
                                id_workdetail_listview.setAdapter(workDetailAdapter);
                                workDetailAdapter.notifyDataSetChanged();
                                break;
                            case REQUEST_FAILED:
                                ResponseCodeInfo info = (ResponseCodeInfo) data;
                                if (HcHttpRequest.REQUEST_ACCOUT_EXCLUDED == info.getCode()
                                        || HcHttpRequest.REQUEST_TOKEN_FAILED == info.getCode()
                                        ) {
                                    HcUtil.reLogining(info.getBodyData(), mContext, info.getMsg());
                                } else {
                                    HcUtil.showToast(mContext, info.getMsg());
                                }
                                break;
                            case NETWORK_ERROR:
                                HcUtil.toastNetworkError(mContext);
                                break;
                            case SYSTEM_ERROR:
                                HcUtil.toastSystemError(mContext, data);
                                break;
                            case DATA_ERROR:
                                HcUtil.toastDataError(mContext);
                                break;
                            case SESSION_TIMEOUT:
                                HcUtil.toastTimeOut(mContext);
                                break;
                            default:
                                break;
                        }
                    } else {
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

    }
}