package com.android.hcframe.netdisc;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.TopBarView;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.netdisc.util.NetdiscUtil;
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

/**
 * Created by pc on 2016/8/9
 */
public class RecycleActivity extends HcBaseActivity implements View.OnClickListener {
    private static final String TAG = "RecycleActivity";
    private TopBarView mTopBarView;
    private ListView mFileSearchLv;
    private RecycleAdapter mRecycleAdapter;
    private int checkNum = 0;
    private ProgressDialog mProgressDialog;
    ArrayList<JSONObject> ArrayList = new ArrayList<JSONObject>();//
    JSONArray mRecycleInfo = new JSONArray();
    private Handler mHandler = new Handler();
    LinearLayout check_top, netdisc_search_text_footer;
    TextView check_top_cancel, check_top_center, check_top_all,
            netdisc_search_text_reduction, netdisc_search_text_delete;
    boolean mIsSelectAll;
    private NoDataView mNoDataView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.netdisc_recycle_layout);
        initView();
        initData();
        mFileSearchLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View parent, int key,
                                    long arg3) {
                // 取得ViewHolder对象，这样就省去了通过层层的findViewById去实例化我们需要的cb实例的步骤
                RecycleAdapter.MySkydriveViewHolder mMySkydriveViewHolder = (RecycleAdapter.MySkydriveViewHolder) parent.getTag();
                // 改变CheckBox的状态
                mMySkydriveViewHolder.mNetdiscListCheckbox.toggle();
                JSONObject jsonObject = (JSONObject) adapter.getItemAtPosition(key);
                // 将CheckBox的选中状况记录下来
                mRecycleAdapter.getIsSelected().put(key, mMySkydriveViewHolder.mNetdiscListCheckbox.isChecked());
                // 调整选定条目
                if (mMySkydriveViewHolder.mNetdiscListCheckbox.isChecked() == true) {
                    ArrayList.add(jsonObject);
                    checkNum++;
                } else {
                    ArrayList.remove(jsonObject);
                    checkNum--;
                }
                if (checkNum == mRecycleInfo.length()) {
                    check_top_all.setText("全不选");
                    mIsSelectAll = true;
                } else if (checkNum > 0 && checkNum < mRecycleInfo.length()) {
                    check_top_all.setText("全选");
                    mIsSelectAll = false;
                }
                check_top_center.setText("已选" + checkNum + "个");
                if (checkNum > 0) {
                    mTopBarView.setVisibility(View.GONE);
                    check_top.setVisibility(View.VISIBLE);
                    netdisc_search_text_footer.setVisibility(View.VISIBLE);
                } else {
                    //按钮颜色为灰色
                    mTopBarView.setVisibility(View.VISIBLE);
                    check_top.setVisibility(View.GONE);
                    netdisc_search_text_footer.setVisibility(View.GONE);
                }
            }
        });

    }

    private void initView() {
        mFileSearchLv = (ListView) findViewById(R.id.netdisc_file_lv);
        mNoDataView = (NoDataView) findViewById(R.id.recycle_pager_no_data);
        mFileSearchLv.setEmptyView(mNoDataView);
        mTopBarView = (TopBarView) findViewById(R.id.details_top_bar);
        check_top = (LinearLayout) findViewById(R.id.check_top);
        check_top_cancel = (TextView) findViewById(R.id.check_top_cancel);
        check_top_center = (TextView) findViewById(R.id.check_top_center);
        check_top_all = (TextView) findViewById(R.id.check_top_all);
        netdisc_search_text_footer = (LinearLayout) findViewById(R.id.netdisc_search_text_footer);
        netdisc_search_text_reduction = (TextView) findViewById(R.id.netdisc_search_text_reduction);
        netdisc_search_text_delete = (TextView) findViewById(R.id.netdisc_search_text_delete);
        mTopBarView.setTitle("回收站");
        check_top_cancel.setOnClickListener(this);
        check_top_all.setOnClickListener(this);
        netdisc_search_text_reduction.setOnClickListener(this);
        netdisc_search_text_delete.setOnClickListener(this);
    }

    private void initData() {
        HcDialog.showProgressDialog(RecycleActivity.this, "获取数据中");
        RecycleRequest request = new RecycleRequest();
        String url = NetdiscUtil.BASE_URL + "getRecycleList";
        HttpPost share = new HttpPost(url);
        request.sendRequestCommand(url, share, RequestCategory.NONE, new RecycleResponse(), false);
        // 实例化自定义的MyAdapter
//        mRecycleAdapter = new RecycleAdapter(RecycleActivity.this, mRecycleInfo);
//        // 绑定Adapter
//        mFileSearchLv.setAdapter(mRecycleAdapter);

    }

    private class RecycleRequest extends AbstractHttpRequest {

        @Override
        public String getRequestMethod() {
            return null;
        }

        @Override
        public String getParameterUrl() {
            return null;
        }
    }

    private class RecycleResponse extends AbstractHttpResponse {

        private RecycleResponse() {
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + "#################################");
            HcDialog.deleteProgressDialog();
            try {
                JSONObject jsonObject = new JSONObject(data.toString());
                mRecycleInfo = jsonObject.optJSONArray("recyList");

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //清空adapter
                        if (mRecycleAdapter == null) {
                            // 实例化自定义的MySkydriveAdapter
                            mRecycleAdapter = new RecycleAdapter(RecycleActivity.this, mRecycleInfo);
                            // 绑定Adapter
                            mFileSearchLv.setAdapter(mRecycleAdapter);
                        } else {
                            mRecycleAdapter.notifyDataSetChanged();
                        }
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
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

        }
    }

    @Override
    protected void onDestroy() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.check_top_cancel) {//取消
            dataChanged();
        } else if (i == R.id.check_top_all) {//全选
            allSelect();
        } else if (i == R.id.netdisc_search_text_reduction) {//还原
            reduction();
        } else if (i == R.id.netdisc_search_text_delete) {//彻底删除
            dalete();
        }

    }

    private void dalete() {
        HcDialog.showProgressDialog(RecycleActivity.this, "文件删除中");
        RecycleRequest request = new RecycleRequest();
        String url = NetdiscUtil.BASE_URL + "deleteRecycle";
        StringBuilder fileBuilder = new StringBuilder();//文件ID
        StringBuilder folderBuilder = new StringBuilder();//文件夹id
        for (int i = 0; i < ArrayList.size(); i++) {
            JSONObject json = ArrayList.get(i);
            if ("1".equals(json.optString("extType"))) {
                fileBuilder.append(json.optString("infoid") + ",");
            } else {
                folderBuilder.append(json.optString("infoid") + ",");
            }
        }
        String files = fileBuilder.toString();
        String folders = folderBuilder.toString();
        if (!TextUtils.isEmpty(files)) {
            files = files.substring(0, files.length() - 1);
        } else {
            files = "";
        }

        if (!TextUtils.isEmpty(folders)) {
            folders = folders.substring(0, folders.length() - 1);
        } else {
            folders = "";
        }
        HttpPost share = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
        builder.addTextBody("infoid", folders, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("dirid", files, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        share.setEntity(builder.build());
        request.sendRequestCommand(url, share, RequestCategory.NONE, new AbstractHttpResponse() {
            @Override
            public void onSuccess(Object data, RequestCategory request) {
                HcLog.D(TAG + "###########################");
                HcDialog.deleteProgressDialog();

                try {
                    JSONObject jsonObject = new JSONObject(data.toString());
                    mRecycleInfo = jsonObject.optJSONArray("recyList");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //清空adapter
                            // 实例化自定义的MySkydriveAdapter
                            mRecycleAdapter = new RecycleAdapter(RecycleActivity.this, mRecycleInfo);
                            // 绑定Adapter
                            mFileSearchLv.setAdapter(mRecycleAdapter);
                            dataChanged();
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
            }

            @Override
            public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

            }
        }, false);
    }

    private void reduction() {
        HcDialog.showProgressDialog(RecycleActivity.this, "文件还原中");
        RecycleRequest request = new RecycleRequest();
        StringBuilder fileBuilder = new StringBuilder();//文件ID
        StringBuilder folderBuilder = new StringBuilder();//文件夹id
        for (int i = 0; i < ArrayList.size(); i++) {
            JSONObject json = ArrayList.get(i);
            if ("1".equals(json.optString("extType"))) {
                fileBuilder.append(json.optString("infoid") + ",");
            } else {
                folderBuilder.append(json.optString("infoid") + ",");
            }
        }
        String files = fileBuilder.toString();
        String folders = folderBuilder.toString();
        if (!TextUtils.isEmpty(files)) {
            files = files.substring(0, files.length() - 1);
        } else {
            files = "";
        }

        if (!TextUtils.isEmpty(folders)) {
            folders = folders.substring(0, folders.length() - 1);
        } else {
            folders = "";
        }
        String url = NetdiscUtil.BASE_URL + "restore";
        HttpPost share = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
        builder.addTextBody("infoid", folders, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("dirid", files, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        share.setEntity(builder.build());
        request.sendRequestCommand(url, share, RequestCategory.NONE, new AbstractHttpResponse() {
            @Override
            public void onSuccess(Object data, RequestCategory request) {
                HcLog.D(TAG + "###########################");
                HcDialog.deleteProgressDialog();

                try {
                    JSONObject jsonObject = new JSONObject(data.toString());
                    mRecycleInfo = jsonObject.optJSONArray("recyList");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //清空adapter
                            // 实例化自定义的MySkydriveAdapter
                            mRecycleAdapter = new RecycleAdapter(RecycleActivity.this, mRecycleInfo);
                            // 绑定Adapter
                            mFileSearchLv.setAdapter(mRecycleAdapter);
                            dataChanged();
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
            }

            @Override
            public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

            }
        }, false);
    }

    private void allSelect() {
        if (mIsSelectAll) {//全部选完了
            check_top_all.setText("全选");
            mIsSelectAll = false;
//            dataChanged();
            ArrayList.clear();
            for (int j = 0; j < mRecycleInfo.length(); j++) {
                mRecycleAdapter.getIsSelected().put(j, false);
//                if (mMySkydriveAdapter.getIsSelected().get(j)) {
//                    mMySkydriveAdapter.getIsSelected().put(j, false);
//                    checkNum--;// 数量减1
//                }
            }
            checkNum = 0;
            // 通知listView刷新
            mRecycleAdapter.notifyDataSetChanged();
            // TextView显示最新的选中数目
            check_top_center.setText("已选" + checkNum + "个");
        } else {//没有全部选完
            check_top_all.setText("全不选");
            mIsSelectAll = true;
            ArrayList.clear();
            for (int j = 0; j < mRecycleInfo.length(); j++) {
                try {
                    ArrayList.add((JSONObject) mRecycleInfo.get(j));
                    mRecycleAdapter.getIsSelected().put(j, true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            checkNum = mRecycleInfo.length();
            // 通知listView刷新
            mRecycleAdapter.notifyDataSetChanged();
            // TextView显示最新的选中数目
            check_top_center.setText("已选" + mRecycleInfo.length() + "个");
        }
    }


    private void dataChanged() {
        ArrayList.clear();
        mTopBarView.setVisibility(View.VISIBLE);
        check_top.setVisibility(View.GONE);
        netdisc_search_text_footer.setVisibility(View.GONE);
        for (int j = 0; j < mRecycleInfo.length(); j++) {
            mRecycleAdapter.getIsSelected().put(j, false);
//                if (mMySkydriveAdapter.getIsSelected().get(j)) {
//                    mMySkydriveAdapter.getIsSelected().put(j, false);
//                    checkNum--;// 数量减1
//                }
        }
        checkNum = 0;
        // 通知listView刷新
        mRecycleAdapter.notifyDataSetChanged();
        // TextView显示最新的选中数目
        check_top_center.setText("已选" + checkNum + "个");
    }
}
