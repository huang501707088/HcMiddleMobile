package com.android.hcframe.netdisc;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.TopBarView;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.HttpRequestQueue;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.netdisc.data.SettingSharedHelper;
import com.android.hcframe.netdisc.netdisccls.MySkydriveFoldItem;
import com.android.hcframe.netdisc.netdisccls.MySkydriveInfoItem;
import com.android.hcframe.netdisc.util.NetdiscUtil;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshListView;
import com.android.hcframe.view.selector.ItemInfo;
import com.android.hcframe.view.toast.NoDataView;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by pc on 2016/8/14.
 * 工作组
 */
public class WorkGroupActivity extends HcBaseActivity implements PullToRefreshBase.OnRefreshBothListener {
    private static final String TAG = "WorkGroupActivity";
    public static final int MAIN = 1;
    public static final int CHOOSE = 2;
    public static final int HOME = 3;
    public static final int NETDISC = 4;
    public static final String TYPE = "type";
    public static final String FILEID = "fileid";
    public static final String FOLDERID = "folderid";
    private TopBarView mTopBarView;
    private PullToRefreshListView mDiscSearchLv;
    int type;
    private Handler mHandler = new Handler();
    private NoDataView mNoDataView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.netdisc_workgroup_list);
        mTopBarView = (TopBarView) findViewById(R.id.details_top_bar);
        type = getIntent().getIntExtra(TYPE, 1);
        if (type == MAIN) {
            mTopBarView.setTitle("共享空间");
        } else if (type == CHOOSE) {
            mTopBarView.setTitle("共享到");
        } else if (type == HOME) {
            mTopBarView.setTitle("共享空间");
        } else if (type == NETDISC) {
            mTopBarView.setTitle("我的共享空间");
        }

        mDiscSearchLv = (PullToRefreshListView) findViewById(R.id.netdisc_search_lv);
        mNoDataView = (NoDataView) findViewById(R.id.share_pager_no_data);
        mDiscSearchLv.setEmptyView(mNoDataView);
        HcDialog.showProgressDialog(WorkGroupActivity.this, "获取数据中");
        initDate();
        mDiscSearchLv.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mDiscSearchLv.setOnRefreshBothListener(WorkGroupActivity.this);
        // 绑定listView的监听器
        mDiscSearchLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View parent, int key,
                                    long arg3) {
                JSONObject jsonObject = (JSONObject) adapter.getAdapter().getItem(key);
                Intent intent = new Intent();
                intent.putExtra("infoid", jsonObject.optString("infoid"));
                intent.putExtra("infoname", jsonObject.optString("infoname"));
                if (jsonObject.optString("userroleType") == null || "".equals(jsonObject.optString("userroleType"))) {
                    intent.putExtra("userroleType", "1");
                } else {
                    intent.putExtra("userroleType", jsonObject.optString("userroleType"));
                }

                intent.setClass(WorkGroupActivity.this, WorkGroupDetailActivity.class);
                startActivity(intent);
            }
        });
    }

    private class ListRequest extends AbstractHttpRequest {

        private static final String TAG = WorkGroupActivity.TAG + "ListRequest";

        public ListRequest() {

        }

        public String getRequestMethod() {
            return "clouddiskM-webapp/api/clouddisk/deletefile";
        }

        @Override
        public String getParameterUrl() {

            return "";
        }

    }

    JSONArray jsonArray;

    private void initDate() {

        ListRequest request = new ListRequest();
        String url = "";
        if (type == HOME) {
            url = NetdiscUtil.BASE_URL + "getGroupShareList";
        } else if (type == NETDISC) {
            url = NetdiscUtil.BASE_URL + "getmyGroupList";
        }

        HttpPost share = new HttpPost(url);
        request.sendRequestCommand(url, share, RequestCategory.NONE, new AbstractHttpResponse() {
            @Override
            public void onSuccess(Object data, RequestCategory request) {
                HcDialog.deleteProgressDialog();

                try {
                    JSONObject jsonObject = new JSONObject(data.toString());

                    if (type == HOME) {
                        jsonArray = jsonObject.optJSONArray("groupShareList");
                    } else if (type == NETDISC) {
                        jsonArray = jsonObject.optJSONArray("mygroupList");
                    }

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mDiscSearchLv.onRefreshComplete();
                            WorkAdapter workAdapter = new WorkAdapter(WorkGroupActivity.this, jsonArray, type);
                            mDiscSearchLv.setAdapter(workAdapter);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public String getTag() {
                return null;
            }

            @Override
            public void onAccountExcluded(String data, String msg, RequestCategory category) {
                HcDialog.deleteProgressDialog();
                mDiscSearchLv.onRefreshComplete();
            }

            @Override
            public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

            }
        }, false);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        HttpRequestQueue.getInstance().cancelRequest("");
        super.onDestroy();
    }

    private class DeleteListRequest extends AbstractHttpRequest {

        private static final String TAG = WorkGroupActivity.TAG + "$MySkydriveRequest";

        public DeleteListRequest() {

        }

        public String getRequestMethod() {
            return "clouddiskM-webapp/api/clouddisk/deletefile";
        }

        @Override
        public String getParameterUrl() {

            return "";
        }

    }


    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
        initDate();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {
        //上拉
    }


    private class ShareResponse extends AbstractHttpResponse {
        int num;
        String name;

        private ShareResponse(int num, String name) {
            this.num = num;
            this.name = name;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + "#################################");
            try {
                JSONObject jsonObject = new JSONObject(data.toString());
                String code = jsonObject.optString("code");
                String link = jsonObject.optString("link");
                Intent intent = new Intent(WorkGroupActivity.this, ShareSuccessActivity.class);
                intent.putExtra("num", num);
                intent.putExtra("code", code);
                intent.putExtra("name", name);
                intent.putExtra("link", link);
                startActivity(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public String getTag() {
            return null;
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {

        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

        }
    }
}