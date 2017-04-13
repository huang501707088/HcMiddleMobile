package com.android.hcframe.netdisc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.HttpRequestQueue;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.netdisc.data.SettingSharedHelper;
import com.android.hcframe.netdisc.netdisccls.MySkydriveFoldItem;
import com.android.hcframe.netdisc.netdisccls.MySkydriveInfoItem;
import com.android.hcframe.netdisc.util.NetdiscUtil;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshListView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by pc on 2016/6/28.  美工给的图有误，我认为此处的文件夹或文件后面应该带checkbox，因为这样才能保证移动到该文件夹中的文件可以再次移动和进行其他操作
 */
public class MoveTerminalAcitivity extends HcBaseActivity implements View.OnClickListener {
    private static final String TAG = "MoveTerminalAcitivity";
    public static final int NETDISC = 1;
    public static final int MYSHARE = 2;
    public static final int SHARE = 3;
    public static final String FROM = "from";
    private PullToRefreshListView mMoveTerminalLv;
    private LinearLayout mMoveNewFile;
    private LinearLayout mMoveTo, netdisc_search_list_footer;
    private TextView mMoveText;
    private TextView mTopbarTitle;
    private TextView mMyFileText;
    private ImageView mTopBarBack;
    private MoveTerminalAdapter mMoveTerminalAdapter;
    /**
     * 网盘中的listview
     */
    private Handler mHandler = new Handler();
    private List<MySkydriveInfoItem> mMySkydriveList = new ArrayList<MySkydriveInfoItem>();
    List<MySkydriveInfoItem> mSelectList = new ArrayList<MySkydriveInfoItem>();//移动列表
    private Stack<MySkydriveInfoItem> mStack;
    private String title = "";
    String type = "";
    int from;
    private List<MySkydriveInfoItem> mStackList;
    String copy = null;
    /**
     * 新建文件夹请求
     */
    private NewFileResponse mResponse;
    /**
     * 移动请求
     */
    private CopyListResponse cResponse;
    /**
     * 复制请求
     */
    private MoveListResponse vResponse;
    /**
     * 获取云盘中的List列表
     */
    private CloudListResponse lResponse;
    private NoDataView mNoDataView;
//    /**
//     * 提醒MySkydriveActivity已经移动，复制完成，清空checkNum等参数
//     */
//    public interface OnCleanCheckNumCallback {
//        void onCleanCheckNumClick();
//    }
//
//    private static OnCleanCheckNumCallback cCallback;
//
//    public static void setCleanCheckNumCallback(OnCleanCheckNumCallback callback) {
//        cCallback = callback;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.netdisc_move_terminal);
        mTopBarBack = (ImageView) findViewById(R.id.topbar_back_btn);
//        mMoveTerminalLv = (PullToRefreshListView) findViewById(R.id.move_lv);
        mMoveNewFile = (LinearLayout) findViewById(R.id.move_new_file);
        mMoveTo = (LinearLayout) findViewById(R.id.move_to);
        mMoveTerminalLv = (PullToRefreshListView) findViewById(R.id.move_lv);
        mNoDataView = (NoDataView) findViewById(R.id.move_pager_no_data);
        mMoveTerminalLv.setEmptyView(mNoDataView);
        mMoveText = (TextView) findViewById(R.id.move_text);
        mTopbarTitle = (TextView) findViewById(R.id.topbar_title);
        mMyFileText = (TextView) findViewById(R.id.my_file_text);
        netdisc_search_list_footer = (LinearLayout) findViewById(R.id.netdisc_search_list_footer);
        initData();
        mMoveTerminalLv.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mTopBarBack.setOnClickListener(this);
        mMoveNewFile.setOnClickListener(this);
        mMoveTo.setOnClickListener(this);
        // 绑定listView的监听器
        mMoveTerminalLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position,
                                    long arg3) {
                MySkydriveInfoItem mySkydriveFoldItem = (MySkydriveInfoItem) adapter.getAdapter().getItem(position);
                if (mySkydriveFoldItem instanceof MySkydriveFoldItem) {
                    mStack.push(mySkydriveFoldItem);
                    setTitleList();
                    //点击文件夹，进入文件夹里面，是同一个activity，只是显示的文件有所不同
                    CloudListRequest request = new CloudListRequest(mStack.peek().getNetdiscId(), "N", type);
                    if (lResponse == null) {
                        lResponse = new CloudListResponse();
                    }
                    HcDialog.showProgressDialog(MoveTerminalAcitivity.this, "获取数据中");
//            request.sendRequestCommand(RequestCategory.CloudList, lResponse, false);
                    String url = NetdiscUtil.BASE_IP + "/clouddiskM-webapp/api/clouddisk/list?type=N&up_id=" + mStack.peek().getNetdiscId() + "&dirType=" + type;
                    request.sendRequestCommand(url, RequestCategory.CloudList, lResponse, false);


                } else {
                }

            }
        });
    }

    private void initData() {
        //取出上一层文件夹的名字
        Bundle bundle = getIntent().getExtras();
//        mMySkydriveList = (ArrayList<MySkydriveInfoItem>) bundle.getSerializable("mMySkydriveList");
        mSelectList = (List<MySkydriveInfoItem>) bundle.getSerializable("mSelectList");
//        if (bundle.getSerializable("mStack") != null) {
//            mStackList = (ArrayList<MySkydriveInfoItem>) bundle.getSerializable("mStack");
//        }
//        mStack = new Stack<MySkydriveInfoItem>();
//        mStack.addAll(mStackList);
        copy = bundle.getString("copy");
        String url = "";
        if (!TextUtils.isEmpty(copy)) {
            if (!"copy".equals(copy)) {
                mTopbarTitle.setText("选择共享位置");
                mMyFileText.setText("立即共享");
            } else {
                mTopbarTitle.setText("选择复制位置");
                mMyFileText.setText("立即复制");
            }

        }
        from = getIntent().getIntExtra(FROM, 1);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式

        if (from == NETDISC) {
            url = NetdiscUtil.BASE_URL + "list";
            title = "我的网盘";
            type = "U";
            builder.addTextBody("up_id", "", ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
            builder.addTextBody("type", "R", ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
            builder.addTextBody("dirType", "U", ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
            netdisc_search_list_footer.setVisibility(View.VISIBLE);
        } else if (from == MYSHARE) {
            url = NetdiscUtil.BASE_URL + "getmyGroupList";
            title = "我的共享空间";
            type = "S";
            netdisc_search_list_footer.setVisibility(View.GONE);
        } else if (from == SHARE) {
            url = NetdiscUtil.BASE_URL + "getGroupShareList";
            title = "共享空间";
            type = "S";
            netdisc_search_list_footer.setVisibility(View.GONE);
        }
        mMoveText.setText(title);
        // 实例化自定义的MySkydriveAdapter
//        mMoveTerminalAdapter = new MoveTerminalAdapter(this, mMySkydriveList);
//        // 绑定Adapter
//        mMoveTerminalLv.setAdapter(mMoveTerminalAdapter);
        mStack = new Stack<MySkydriveInfoItem>();
        MySkydriveInfoItem root = new MySkydriveFoldItem();
        root.setNetdiscListType("1");
        root.setNetdiscDirLvl("");
        root.setNetdiscId("");
        root.setNetdiscListDate("");
        root.setNetdiscListFileSize("");
        root.setNetdiscListText(title);
        root.setNetdiscUpdirId("");

        // 为Adapter准备数据
        mMySkydriveList = new ArrayList<MySkydriveInfoItem>();
//        List<MySkydriveInfoItem> MySkydriveInfoList = SettingSharedHelper.getCloudListForUserIdInfo(WorkGroupDetailActivity.this, root);
//        if (MySkydriveInfoList != null)
//            mMySkydriveList.addAll(MySkydriveInfoList);
//        mMySkydriveList.addAll(SettingSharedHelper.getCloudListInfo(MySkydriveActivity.this));
        HcLog.D("MySkydriveActivity #initData list size = " + mMySkydriveList.size());
        root.addAllItems(mMySkydriveList);
        mStack.add(root);

//        if (mMoveTerminalAdapter == null) {
//            mMoveTerminalAdapter = new MoveTerminalAdapter(this, mMySkydriveList);
//        }
//        mMoveTerminalLv.setAdapter(mMoveTerminalAdapter);
        HcDialog.showProgressDialog(MoveTerminalAcitivity.this, "获取数据中");
        ListRequest request = new ListRequest();
        HttpPost share = new HttpPost(url);
        share.setEntity(builder.build());
        request.sendRequestCommand(url, share, RequestCategory.NONE, new AbstractHttpResponse() {
            @Override
            public void onSuccess(Object data, RequestCategory request) {
                HcDialog.deleteProgressDialog();
                mMySkydriveList.clear();
                try {
                    JSONObject jsonObject = new JSONObject(data.toString());
                    JSONArray jsonArray = null;
                    if (from == SHARE) {
                        jsonArray = jsonObject.optJSONArray("groupShareList");
                    } else if (from == MYSHARE) {
                        jsonArray = jsonObject.optJSONArray("mygroupList");
                    } else if (from == NETDISC) {
                        jsonArray = jsonObject.optJSONArray("dirfiles");
                    }
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = (JSONObject) jsonArray.get(i);
                        MySkydriveInfoItem mySkydriveInfoItem;
                        String ext = object.optString("ext");
                        if (TextUtils.isEmpty(ext)) {
                            ext = "1";
                        }
                        if ("1".equals(ext)) {
                            mySkydriveInfoItem = new MySkydriveFoldItem();
                        } else {
                            mySkydriveInfoItem = new MySkydriveInfoItem();
                        }
                        mySkydriveInfoItem.setNetdiscId(object.optString("infoid"));
                        mySkydriveInfoItem.setNetdiscListText(object.optString("infoname"));
                        mySkydriveInfoItem.setNetdiscId(object.optString("infoid"));
                        mySkydriveInfoItem.setNetdiscListType(ext);
                        mMySkydriveList.add(mySkydriveInfoItem);
                    }
                    mStack.peek().addAllItems(mMySkydriveList);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mMoveTerminalAdapter = new MoveTerminalAdapter(MoveTerminalAcitivity.this, mMySkydriveList);
                            // 绑定Adapter
                            mMoveTerminalLv.setAdapter(mMoveTerminalAdapter);
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

    private class ListRequest extends AbstractHttpRequest {

        private static final String TAG = MoveTerminalAcitivity.TAG + "ListRequest";

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.move_new_file) {
            //点击新建文件夹按钮，弹出新建文件夹Dialog
            final AlertDialog dialog = new AlertDialog.Builder(this)
                    .create();
            dialog.setCancelable(false);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View mDialogView = inflater.inflate(R.layout.netdisc_new_folder_dialog, null);
            dialog.setView(mDialogView);
            dialog.show();
            dialog.getWindow().setContentView(R.layout.netdisc_new_folder_dialog);
            final EditText netdiscEdit = (EditText) dialog.getWindow()
                    .findViewById(R.id.netdisc_edit);
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
                    String netdiscEditStr = netdiscEdit.getText().toString().trim();
                    String typeNR = "";
                    if ("U".equals(type)) {
                        typeNR = mStack.size() > 1 ? "N" : "R";
                    } else {
                        typeNR = "N";
                    }
                    if ("".equals(netdiscEditStr) || TextUtils.isEmpty(netdiscEditStr)) {
                        NewFileRequest request = new NewFileRequest("新建文件夹", "", "", "R");
                        if (mResponse == null) {
                            mResponse = new NewFileResponse();
                        }
                        HcDialog.showProgressDialog(MoveTerminalAcitivity.this, "创建文件夹中");
//                        request.sendRequestCommand(RequestCategory.NewFile, mResponse, false);
                        String url = NetdiscUtil.BASE_IP + "/clouddiskM-webapp/api/clouddisk/adddir?dirName=" + HttpRequestQueue.URLEncode("新建文件夹") + "&updirId=" + mStack.peek().getNetdiscId() + "&type=" + typeNR + "&dirType=" + type;
                        request.sendRequestCommand(url, RequestCategory.NewFile, mResponse, false);
                    } else {
                        NewFileRequest request = new NewFileRequest(netdiscEditStr, "", "", "R");
                        if (mResponse == null) {
                            mResponse = new NewFileResponse();
                        }
                        HcDialog.showProgressDialog(MoveTerminalAcitivity.this, "创建文件夹中");
//                        request.sendRequestCommand(RequestCategory.NewFile, mResponse, false);
                        System.out.print("mStack.size()" + mStack.size() + " " + mStack.peek().getNetdiscId() + " " + "type=" + type);
                        String url = NetdiscUtil.BASE_IP + "/clouddiskM-webapp/api/clouddisk/adddir?dirName=" + HttpRequestQueue.URLEncode(netdiscEditStr) + "&updirId=" + mStack.peek().getNetdiscId() + "&type=" + typeNR + "&dirType=" + type;
                        request.sendRequestCommand(url, RequestCategory.NewFile, mResponse, false);
                    }
                }
            });
        } else if (i == R.id.move_to) {
            if (TextUtils.isEmpty(copy)) {
                //点击立即移动按钮
                moveFiles();
            } else {
                copyFiles();
            }
        } else if (i == R.id.topbar_back_btn) {
            /**
             * 首先检查栈中是否有数据，若没有，在进行初始化
             * */
            if (mStack.size() > 1) {
                mStack.pop();
                setTitleList();
                mMySkydriveList.clear();
                mMySkydriveList.addAll(mStack.peek().getItems());
                mMoveTerminalAdapter.notifyDataSetChanged();
            } else {
                //退回到上一个Activity
                finish();
            }
        }
    }

    private class NewFileRequest extends AbstractHttpRequest {
        private static final String TAG = MoveTerminalAcitivity.TAG + "$MoveTerminalRequest";
        Map<String, String> httpparams = new HashMap<String, String>();

        public NewFileRequest(String dirName, String updirId, String dirLvl, String type) {
            httpparams.put("dirName", dirName);
            httpparams.put("updirId", updirId);
            httpparams.put("dirLvl", "");
            httpparams.put("type", type);
        }

        @Override
        public String getRequestMethod() {
            return "clouddiskM-webapp/api/clouddisk/adddir";
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

    private class NewFileResponse extends AbstractHttpResponse {

        private static final String TAG = MoveTerminalAcitivity.TAG + "$MoveTerminalResponse";

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            /**
             * 利用Sharepreference存储json数据,要是是跟目录的话,需要存储.
             * */

            HcDialog.deleteProgressDialog();
            if (mStack.size() == 1) {
                SettingSharedHelper.saveUserIdCloudListInfo(MoveTerminalAcitivity.this, data.toString());
            }
            mMySkydriveList.clear();
            String mUpId = null;
            try {
                JSONObject object = new JSONObject((String) data);
                if (HcUtil.hasValue(object, "up_id")) {
                    mUpId = object.getString("up_id");

                }
                JSONArray jsonBodyArray = object.getJSONArray("dirfiles");
                int jsonArrayNum = jsonBodyArray.length();
                if (jsonArrayNum > 0) {
                    for (int i = 0; i < jsonArrayNum; i++) {
                        MySkydriveInfoItem mMySkydriveItem = null;

                        JSONObject mySkydriveFileObj = jsonBodyArray.getJSONObject(i);
                        String mExt = null;
                        if (HcUtil.hasValue(mySkydriveFileObj, "ext")) {
                            mExt = mySkydriveFileObj.getString("ext");
                        }
                        if ("1".equals(mExt)) {
                            mMySkydriveItem = new MySkydriveFoldItem();
                        } else {
                            mMySkydriveItem = new MySkydriveInfoItem();
                        }
                        mMySkydriveItem.setNetdiscUpdirId(mUpId);
                        mMySkydriveItem.setNetdiscListType(mExt);

                        if (HcUtil.hasValue(mySkydriveFileObj, "infoid")) {
                            String mInfoid = mySkydriveFileObj.getString("infoid");
                            mMySkydriveItem.setNetdiscId(mInfoid);
                        }
                        if (HcUtil.hasValue(mySkydriveFileObj, "infoname")) {
                            String mInfoname = mySkydriveFileObj.getString("infoname");
                            mMySkydriveItem.setNetdiscListText(mInfoname);
                        }

                        if (HcUtil.hasValue(mySkydriveFileObj, "createtime")) {
                            String mCreatetime = mySkydriveFileObj.getString("createtime");
                            mMySkydriveItem.setNetdiscListDate(mCreatetime);
                        }

                        if (HcUtil.hasValue(mySkydriveFileObj, "lvl")) {
                            String mLvl = mySkydriveFileObj.getString("lvl");
                            mMySkydriveItem.setNetdiscDirLvl(mLvl);
                        }
                        if (HcUtil.hasValue(mySkydriveFileObj, "filesize")) {
                            String mFilesize = mySkydriveFileObj.getString("filesize");
                            mMySkydriveItem.setNetdiscListFileSize(mFilesize);
                        }
                        mMySkydriveList.add(mMySkydriveItem);
                        mStack.peek().addItem(mMySkydriveItem);

                    }
                }
            } catch (JSONException e) {
                HcLog.D(TAG + " #onSuccess parseJson error =" + e);
            }
            mStack.peek().getItems().clear();
            mStack.peek().addAllItems(mMySkydriveList);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mMoveTerminalAdapter == null) {
                        // 实例化自定义的MySkydriveAdapter
                        mMoveTerminalAdapter = new MoveTerminalAdapter(MoveTerminalAcitivity.this, mMySkydriveList);
                        // 绑定Adapter
                        mMoveTerminalLv.setAdapter(mMoveTerminalAdapter);
                    } else {
                        mMoveTerminalAdapter.notifyDataSetChanged();
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
            HcUtil.reLogining(data, MoveTerminalAcitivity.this, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
            HcLog.D(TAG + " #notifyRequestMd5Url md5Url = " + md5Url + " request = " + request);
        }

    }

    private class MoveListRequest extends AbstractHttpRequest {
        private static final String TAG = MoveTerminalAcitivity.TAG + "$MoveTerminalRequest";

        public MoveListRequest() {

        }

        @Override
        public String getRequestMethod() {
            return "clouddiskM-webapp/api/clouddisk/removefile";
        }

        @Override
        public String getParameterUrl() {
            return "";
        }
    }

    private class MoveListResponse extends AbstractHttpResponse {
        private static final String TAG = MoveTerminalAcitivity.TAG + "$MySkydriveResponse";

        public MoveListResponse() {
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            HcDialog.deleteProgressDialog();
            if (mStack.size() == 1) {
                SettingSharedHelper.saveUserIdCloudListInfo(MoveTerminalAcitivity.this, data.toString());
            }
            mMySkydriveList.clear();
            String mUpId = null;
            try {
                JSONObject object = new JSONObject((String) data);
                if (HcUtil.hasValue(object, "up_id")) {
                    mUpId = object.getString("up_id");

                }
                JSONArray jsonBodyArray = object.getJSONArray("dirfiles");
                int jsonArrayNum = jsonBodyArray.length();
                if (jsonArrayNum > 0) {
                    for (int i = 0; i < jsonArrayNum; i++) {
                        MySkydriveInfoItem mMySkydriveItem = null;

                        JSONObject mySkydriveFileObj = jsonBodyArray.getJSONObject(i);
                        String mExt = null;
                        if (HcUtil.hasValue(mySkydriveFileObj, "ext")) {
                            mExt = mySkydriveFileObj.getString("ext");
                        }
                        if ("1".equals(mExt)) {
                            mMySkydriveItem = new MySkydriveFoldItem();
                        } else {
                            mMySkydriveItem = new MySkydriveInfoItem();
                        }
                        mMySkydriveItem.setNetdiscUpdirId(mUpId);
                        mMySkydriveItem.setNetdiscListType(mExt);

                        if (HcUtil.hasValue(mySkydriveFileObj, "infoid")) {
                            String mInfoid = mySkydriveFileObj.getString("infoid");
                            mMySkydriveItem.setNetdiscId(mInfoid);
                        }
                        if (HcUtil.hasValue(mySkydriveFileObj, "infoname")) {
                            String mInfoname = mySkydriveFileObj.getString("infoname");
                            mMySkydriveItem.setNetdiscListText(mInfoname);
                        }

                        if (HcUtil.hasValue(mySkydriveFileObj, "createtime")) {
                            String mCreatetime = mySkydriveFileObj.getString("createtime");
                            mMySkydriveItem.setNetdiscListDate(mCreatetime);
                        }

                        if (HcUtil.hasValue(mySkydriveFileObj, "lvl")) {
                            String mLvl = mySkydriveFileObj.getString("lvl");
                            mMySkydriveItem.setNetdiscDirLvl(mLvl);
                        }
                        if (HcUtil.hasValue(mySkydriveFileObj, "filesize")) {
                            String mFilesize = mySkydriveFileObj.getString("filesize");
                            mMySkydriveItem.setNetdiscListFileSize(mFilesize);
                        }
                        mMySkydriveList.add(mMySkydriveItem);
                        mStack.peek().addItem(mMySkydriveItem);

                    }
                }
            } catch (JSONException e) {
                HcLog.D(TAG + " #onSuccess parseJson error =" + e);
            }
            mStack.peek().getItems().clear();
            mStack.peek().addAllItems(mMySkydriveList);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mMoveTerminalAdapter == null) {
                        // 实例化自定义的MySkydriveAdapter
                        mMoveTerminalAdapter = new MoveTerminalAdapter(MoveTerminalAcitivity.this, mMySkydriveList);
                        // 绑定Adapter
                        mMoveTerminalLv.setAdapter(mMoveTerminalAdapter);
                    } else {
                        mMoveTerminalAdapter.notifyDataSetChanged();
                    }

                }
            });
            MoveTerminalAcitivity.this.finish();
        }

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcDialog.deleteProgressDialog();
            mMySkydriveList.clear();
            mMoveTerminalAdapter.notifyDataSetChanged();
            HcUtil.reLogining(data, MoveTerminalAcitivity.this, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

        }
    }

    private class CopyListRequest extends AbstractHttpRequest {
        private static final String TAG = MoveTerminalAcitivity.TAG + "$MoveTerminalRequest";

        public CopyListRequest() {

        }

        @Override
        public String getRequestMethod() {
            return "clouddiskM-webapp/api/clouddisk/copyfile";
        }

        @Override
        public String getParameterUrl() {
            return "";
        }
    }

    private class CopyListResponse extends AbstractHttpResponse {
        private static final String TAG = MoveTerminalAcitivity.TAG + "$MySkydriveResponse";

        public CopyListResponse() {
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            HcDialog.deleteProgressDialog();
            if (mStack.size() == 1) {
                SettingSharedHelper.saveUserIdCloudListInfo(MoveTerminalAcitivity.this, data.toString());
            }
            mMySkydriveList.clear();
            String mUpId = null;
            try {
                JSONObject object = new JSONObject((String) data);
                if (HcUtil.hasValue(object, "up_id")) {
                    mUpId = object.getString("up_id");

                }
                JSONArray jsonBodyArray = object.getJSONArray("dirfiles");
                int jsonArrayNum = jsonBodyArray.length();
                if (jsonArrayNum > 0) {
                    for (int i = 0; i < jsonArrayNum; i++) {
                        MySkydriveInfoItem mMySkydriveItem = null;

                        JSONObject mySkydriveFileObj = jsonBodyArray.getJSONObject(i);
                        String mExt = null;
                        if (HcUtil.hasValue(mySkydriveFileObj, "ext")) {
                            mExt = mySkydriveFileObj.getString("ext");
                        }
                        if ("1".equals(mExt)) {
                            mMySkydriveItem = new MySkydriveFoldItem();
                        } else {
                            mMySkydriveItem = new MySkydriveInfoItem();
                        }
                        mMySkydriveItem.setNetdiscUpdirId(mUpId);
                        mMySkydriveItem.setNetdiscListType(mExt);

                        if (HcUtil.hasValue(mySkydriveFileObj, "infoid")) {
                            String mInfoid = mySkydriveFileObj.getString("infoid");
                            mMySkydriveItem.setNetdiscId(mInfoid);
                        }
                        if (HcUtil.hasValue(mySkydriveFileObj, "infoname")) {
                            String mInfoname = mySkydriveFileObj.getString("infoname");
                            mMySkydriveItem.setNetdiscListText(mInfoname);
                        }

                        if (HcUtil.hasValue(mySkydriveFileObj, "createtime")) {
                            String mCreatetime = mySkydriveFileObj.getString("createtime");
                            mMySkydriveItem.setNetdiscListDate(mCreatetime);
                        }

                        if (HcUtil.hasValue(mySkydriveFileObj, "lvl")) {
                            String mLvl = mySkydriveFileObj.getString("lvl");
                            mMySkydriveItem.setNetdiscDirLvl(mLvl);
                        }
                        if (HcUtil.hasValue(mySkydriveFileObj, "filesize")) {
                            String mFilesize = mySkydriveFileObj.getString("filesize");
                            mMySkydriveItem.setNetdiscListFileSize(mFilesize);
                        }
                        mMySkydriveList.add(mMySkydriveItem);
                        mStack.peek().addItem(mMySkydriveItem);

                    }
                }
            } catch (JSONException e) {
                HcLog.D(TAG + " #onSuccess parseJson error =" + e);
            }
            mStack.peek().getItems().clear();
            mStack.peek().addAllItems(mMySkydriveList);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mMoveTerminalAdapter == null) {
                        // 实例化自定义的MySkydriveAdapter
                        mMoveTerminalAdapter = new MoveTerminalAdapter(MoveTerminalAcitivity.this, mMySkydriveList);
                        // 绑定Adapter
                        mMoveTerminalLv.setAdapter(mMoveTerminalAdapter);
                    } else {
                        mMoveTerminalAdapter.notifyDataSetChanged();
                    }

                }
            });
            MoveTerminalAcitivity.this.finish();
        }

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcDialog.deleteProgressDialog();
            mMySkydriveList.clear();
            mMoveTerminalAdapter.notifyDataSetChanged();
            HcUtil.reLogining(data, MoveTerminalAcitivity.this, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

        }
    }

    private class CloudListRequest extends AbstractHttpRequest {
        private static final String TAG = MoveTerminalAcitivity.TAG + "$MySkydriveRequest";
        Map<String, String> httpparams = new HashMap<String, String>();

        public CloudListRequest(String upId, String type, String dirType) {
            httpparams.put("up_id", upId);
            httpparams.put("type", type);
            httpparams.put("dirType", dirType);
        }

        public String getRequestMethod() {
            return "clouddiskM-webapp/api/clouddisk/list";
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

    private String mListMD5Url;

    private class CloudListResponse extends AbstractHttpResponse {
        private static final String TAG = MoveTerminalAcitivity.TAG + "$MoveTerminalResponse";

        public CloudListResponse() {
        }

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            HcDialog.deleteProgressDialog();
            mMySkydriveList.clear();
            String mUpId = null;
            try {
                JSONObject object = new JSONObject((String) data);
                if (HcUtil.hasValue(object, "up_id")) {
                    mUpId = object.getString("up_id");
                }

                JSONArray jsonBodyArray = object.getJSONArray("dirfiles");
                int jsonArrayLenth = jsonBodyArray.length();
                for (int i = 0; i < jsonArrayLenth; i++) {
                    MySkydriveInfoItem mMySkydriveItem = null;

                    String mExt = null;
                    JSONObject mySkydriveFileObj = jsonBodyArray.getJSONObject(i);

                    if (HcUtil.hasValue(mySkydriveFileObj, "ext")) {
                        mExt = mySkydriveFileObj.getString("ext");
                    }
                    if ("1".equals(mExt)) {
                        mMySkydriveItem = new MySkydriveFoldItem();
                    } else {
                        mMySkydriveItem = new MySkydriveInfoItem();
                    }
                    mMySkydriveItem.setNetdiscUpdirId(mUpId);
                    mMySkydriveItem.setNetdiscListType(mExt);

                    if (HcUtil.hasValue(mySkydriveFileObj, "infoid")) {
                        String mInfoid = mySkydriveFileObj.getString("infoid");
                        mMySkydriveItem.setNetdiscId(mInfoid);
                    }
                    if (HcUtil.hasValue(mySkydriveFileObj, "infoname")) {
                        String mInfoname = mySkydriveFileObj.getString("infoname");
                        mMySkydriveItem.setNetdiscListText(mInfoname);
                    }

//                    if (!"".equals(mUpId)) {
//                        mMySkydriveItem.setNetdiscUpdirId(mUpId);
//                    }
                    if (HcUtil.hasValue(mySkydriveFileObj, "createtime")) {
                        String mCreatetime = mySkydriveFileObj.getString("createtime");
                        mMySkydriveItem.setNetdiscListDate(mCreatetime);
                    }
                    if (HcUtil.hasValue(mySkydriveFileObj, "type")) {
                        String mType = mySkydriveFileObj.getString("type");
                        mMySkydriveItem.setNetdiscListType(mType);
                    }
                    if (HcUtil.hasValue(mySkydriveFileObj, "lvl")) {
                        String mLvl = mySkydriveFileObj.getString("lvl");
                        mMySkydriveItem.setNetdiscDirLvl(mLvl);
                    }
                    if (HcUtil.hasValue(mySkydriveFileObj, "filesize")) {
                        String mFileSize = mySkydriveFileObj.getString("filesize");
                        mMySkydriveItem.setNetdiscListFileSize(mFileSize);
                    }

                    mMySkydriveList.add(mMySkydriveItem);
                    mStack.peek().addItem(mMySkydriveItem);
                }

            } catch (JSONException e) {
                HcLog.D(TAG + " #onSuccess parseJson error =" + e);
            }
            mStack.peek().getItems().clear();
            HcLog.D(TAG + " #onSuccess parseJson stack size = " + mStack.size() + " list size =" + mMySkydriveList.size());
            mStack.peek().addAllItems(mMySkydriveList);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mStack != null && mStack.size() > 1) {
                        netdisc_search_list_footer.setVisibility(View.VISIBLE);
                    }
                    //清空adapter
                    if (mMoveTerminalAdapter == null) {
                        // 实例化自定义的MySkydriveAdapter
                        mMoveTerminalAdapter = new MoveTerminalAdapter(MoveTerminalAcitivity.this, mMySkydriveList);
                        // 绑定Adapter
                        mMoveTerminalLv.setAdapter(mMoveTerminalAdapter);
                    } else {
                        mMoveTerminalAdapter.notifyDataSetChanged();
                    }
                }
            });
        }


        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcDialog.deleteProgressDialog();
            mMySkydriveList.clear();
            mMoveTerminalAdapter.notifyDataSetChanged();
            HcUtil.reLogining(data, MoveTerminalAcitivity.this, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
            mListMD5Url = md5Url;
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
    }

    public void setTitleList() {
        List<MySkydriveInfoItem> mStackList = new ArrayList<MySkydriveInfoItem>();
        mStackList.addAll(mStack);
        String mMySkydriveTitle = mStackList.get(0).getNetdiscListText();
        for (int i = 1; i < mStackList.size(); i++) {
            mMySkydriveTitle = mStackList.get(i).getNetdiscListText() + "&" + mMySkydriveTitle;
        }
        String[] mMySkydriveTitles = mMySkydriveTitle.split("&");
        String mTitle = "";
        for (int i = 0; i < mMySkydriveTitles.length; i++) {
            if (!"我的云盘".equals(mMySkydriveTitles[mMySkydriveTitles.length - 1 - i])) {
                mTitle += mMySkydriveTitles[mMySkydriveTitles.length - 1 - i] + ">";
            } else {
                continue;
            }
        }
        if (!TextUtils.isEmpty(mTitle)) {
            mTitle = mTitle.substring(0, mTitle.length() - 1);
        }
        mMoveText.setText(mTitle);
    }

    private void copyFiles() {
        if (mSelectList.isEmpty()) {
            finish();
            return;
        }
        StringBuilder fileBuilder = new StringBuilder();
        StringBuilder folderBuilder = new StringBuilder();
        for (MySkydriveInfoItem item : mSelectList) {
            if (item instanceof MySkydriveFoldItem) {
                folderBuilder.append(item.getNetdiscId() + ",");
            } else {
                fileBuilder.append(item.getNetdiscId() + ",");
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
        String url = null;
        if (!"copy".equals(copy)) {
            HcDialog.showProgressDialog(MoveTerminalAcitivity.this, "共享数据中");
//            mTopbarTitle.setText("选择共享位置");
//            mMyFileText.setText("立即共享");
            url = NetdiscUtil.BASE_URL + "saveGroupfile";
        } else {
            HcDialog.showProgressDialog(MoveTerminalAcitivity.this, "复制数据中");
//            mTopbarTitle.setText("选择复制位置");
//            mMyFileText.setText("立即复制");
            url = NetdiscUtil.BASE_URL + "copyfile";
        }

        HcLog.D(TAG + " #deleteFiles url = " + url);
        if (cResponse == null) {
            cResponse = new CopyListResponse();
        }
        HttpPost delete = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
        builder.addTextBody("infoid", files, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("dirid", folders, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        if (!"copy".equals(copy)) {
            builder.addTextBody("groupid", mStack.peek().getNetdiscId(), ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        } else {
            builder.addTextBody("newdir_id", mStack.peek().getNetdiscId(), ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        }
        builder.addTextBody("dirType", type, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        String typeNR = "";
        if ("U".equals(type)) {
            typeNR = mStack.size() > 1 ? "N" : "R";
        } else {
            typeNR = "N";
        }
        builder.addTextBody("type", typeNR, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        delete.setEntity(builder.build());
        CopyListRequest cRequest = new CopyListRequest();
        cRequest.sendRequestCommand(url, delete, RequestCategory.NONE, cResponse, false);
    }

    private void moveFiles() {
        if (mSelectList.isEmpty()) return;
        StringBuilder fileBuilder = new StringBuilder();
        StringBuilder folderBuilder = new StringBuilder();
        for (MySkydriveInfoItem item : mSelectList) {
            if (item instanceof MySkydriveFoldItem) {
                folderBuilder.append(item.getNetdiscId() + ",");
            } else {
                fileBuilder.append(item.getNetdiscId() + ",");
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
        HcDialog.showProgressDialog(MoveTerminalAcitivity.this, "提交数据中");
        String url = NetdiscUtil.BASE_URL + "removefile";
        HcLog.D(TAG + " #removefile url = " + url);
        if (cResponse == null) {
            cResponse = new CopyListResponse();
        }
        HttpPost delete = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
        builder.addTextBody("infoid", files, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("dirid", folders, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("newdir_id", mStack.peek().getNetdiscId(), ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("dirType", type, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        String typeNR = "";
        if ("U".equals(type)) {
            typeNR = mStack.size() > 1 ? "N" : "R";
        } else {
            typeNR = "N";
        }
        builder.addTextBody("type", typeNR, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));

        delete.setEntity(builder.build());
        MoveListRequest vRequest = new MoveListRequest();
        if (vResponse == null) {
            vResponse = new MoveListResponse();
        }
        vRequest.sendRequestCommand(url, delete, RequestCategory.NONE, vResponse, false);
    }
}
