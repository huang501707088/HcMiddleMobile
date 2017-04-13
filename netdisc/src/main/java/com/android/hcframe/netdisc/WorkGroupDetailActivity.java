package com.android.hcframe.netdisc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.frame.download.FileColumn;
import com.android.frame.download.HcDownloadService;
import com.android.hcframe.CommonActivity;
import com.android.hcframe.HcApplication;
import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.HttpRequestQueue;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.netdisc.netdisccls.MySkydriveFoldItem;
import com.android.hcframe.netdisc.netdisccls.MySkydriveInfoItem;
import com.android.hcframe.netdisc.sql.OperateDatabase;
import com.android.hcframe.netdisc.util.NetdiscUtil;
import com.android.hcframe.netdisc.util.ShareDialog;
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

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by pc on 2016/6/22.
 */
public class WorkGroupDetailActivity extends HcBaseActivity implements View.OnClickListener, WorkGroupDetailAdapter.OnClickCallback, WorkGroupDetailAdapter.OnLinearClickCallback, PullToRefreshBase.OnRefreshBothListener, ServiceConnection, ServiceCallBack.TransferCallback {
    private static final String TAG = "WorkGroupDetailActivity";
    public static final String COPY = "1";
    public static final String MOVE = "2";
    private LinearLayout mTopBarViewLinear;
    private ImageView mTopBarView;
    private LinearLayout mNetdiscSearchShow;
    private LinearLayout mNetdiscSearchListEmpty;
    private LinearLayout mNetdiscSearchParent;
    private ImageView mDocSearchImageClear;
    private PullToRefreshListView mDiscSearchLv;
    private LinearLayout mNetdiscSearchNew;
    private LinearLayout mNetdiscSearchFile;
    private LinearLayout mSearch;
    private LinearLayout mNetdiscSearch;
    private ArrayList<MySkydriveInfoItem> mMySkydriveList;
    private WorkGroupDetailAdapter mMySkydriveAdapter;
    private EditText mSearchContent;
    private TextView mSearchText;
    /**
     * 若有文件在下载状态中则显示出来
     */
    private ImageView mDownImg;
    /**
     * Checkbox操作显示
     */
    private LinearLayout mCheckTop;
    private TextView mCheckTopCancel;
    private TextView mCheckTopCenter;
    private TextView mCheckTopAll;
    private LinearLayout mNetdiscSearchTextFooter;
    private TextView mNetdiscSearchTextDownload;
    private TextView mNetdiscSearchTextShared;
    private TextView mNetdiscSearchTextDelete;
    private TextView mNetdiscSearchTextMore;
    private int checkNum = 0;
    private int fileNum = 0;
    private boolean mIsSelectAll = false;
    private String mUpDirId = null;
    /**
     * 自定义的底部弹出框类
     */
    private SelectPicPopupWindow menuWindow;

    /**
     * 点击更多弹出PopupWindow
     */
    private PopupWindow popupMenu;

    /**
     * 新建文件夹请求
     */
    private NewFileResponse mResponse;
    /**
     * 获取云盘中的List列表
     */
    private CloudListResponse cResponse;
    private Handler mHandler = new Handler();
    List<MySkydriveInfoItem> mDownloadInfoItemList = new ArrayList<MySkydriveInfoItem>();//下载文件列表
    List<MySkydriveInfoItem> mdiaidInfoItemList = new ArrayList<MySkydriveInfoItem>();//文件夹列表
    List<MySkydriveInfoItem> mSelectList = new ArrayList<MySkydriveInfoItem>();//移动，复制，删除，重命名列表
    private Stack<MySkydriveInfoItem> mStack;

    /**
     * popWindows的三个按钮
     */
    private TextView movePopBtn;
    private TextView repeatPopBtn;
    private TextView share_btn;
    /**
     * 回调接口当中的参数
     */
    WorkGroupDetailAdapter.MySkydriveViewHolder mMySkydriveViewHolder;
    MySkydriveInfoItem mySkydriveInfoItem;
    /**
     * 全选状态下右侧按钮的一个bug处理
     */
    private boolean mRightSelectAllBug = false;
    /**
     * 重命名请求
     */
    private ReNameRequest rRequest;
    /**
     * 重命名请求返回
     */
    private ReNameResponse rResponse;
    /**
     * 删除请求
     */
    private DeleteListRequest dRequest;
    /**
     * 删除请求返回
     */
    private DeleteListResponse dResponse;

    /**
     * 文件夹名字
     */
    public static final String FILE_NAME = "dir";
    /**
     * 文件夹ID
     */
    public static final String FILE_ID = "dirId";
    String infoid = "";
    String title;
    TextView topbar_title;
    ImageView topbar_right_btn;
    String userroleType;
    private NoDataView mNoDataView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.netdisc_workgroup_detail_list);
        topbar_right_btn = (ImageView) findViewById(R.id.topbar_right_btn);
        infoid = getIntent().getStringExtra("infoid");
        title = getIntent().getStringExtra("infoname");
        userroleType = getIntent().getStringExtra("userroleType");
        if ("1".equals(userroleType) || "2".equals(userroleType)) {
            topbar_right_btn.setVisibility(View.VISIBLE);
            topbar_right_btn.setOnClickListener(this);
        } else {
            topbar_right_btn.setVisibility(View.INVISIBLE);
        }
        topbar_title = (TextView) findViewById(R.id.topbar_title);
        topbar_title.setText(title);
        mTopBarViewLinear = (LinearLayout) findViewById(R.id.doc_list_top_bar);
        mTopBarView = (ImageView) findViewById(R.id.topbar_back_btn);
        mNetdiscSearchShow = (LinearLayout) findViewById(R.id.netdisc_search_show);
        mNetdiscSearchListEmpty = (LinearLayout) findViewById(R.id.netdisc_search_list_empty);
        mNetdiscSearchParent = (LinearLayout) findViewById(R.id.netdisc_search_parent);
        mDiscSearchLv = (PullToRefreshListView) findViewById(R.id.netdisc_search_lv);
        mNoDataView = (NoDataView) findViewById(R.id.group_pager_no_data);
        mDiscSearchLv.setEmptyView(mNoDataView);
        mNetdiscSearchNew = (LinearLayout) findViewById(R.id.netdisc_search_new);
        mNetdiscSearchFile = (LinearLayout) findViewById(R.id.netdisc_search_file);
        mNetdiscSearch = (LinearLayout) findViewById(R.id.netdisc_search);
        mSearch = (LinearLayout) findViewById(R.id.search);
        mDocSearchImageClear = (ImageView) findViewById(R.id.doc_search_image_clear);
        mSearchContent = (EditText) findViewById(R.id.search_content);
        mSearchText = (TextView) findViewById(R.id.search_text);
        mCheckTop = (LinearLayout) findViewById(R.id.check_top);
        mCheckTopCancel = (TextView) findViewById(R.id.check_top_cancel);
        mCheckTopCenter = (TextView) findViewById(R.id.check_top_center);
        mCheckTopAll = (TextView) findViewById(R.id.check_top_all);
        mNetdiscSearchTextFooter = (LinearLayout) findViewById(R.id.netdisc_search_text_footer);
        mNetdiscSearchTextDownload = (TextView) findViewById(R.id.netdisc_search_text_download);
        mNetdiscSearchTextShared = (TextView) findViewById(R.id.netdisc_search_text_shared);
        mNetdiscSearchTextDelete = (TextView) findViewById(R.id.netdisc_search_text_delete);
        mNetdiscSearchTextMore = (TextView) findViewById(R.id.netdisc_search_text_more);
        mDownImg = (ImageView) findViewById(R.id.down_img);
        mDownImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(WorkGroupDetailActivity.this, TransListActivity.class);
                startActivity(intent);
            }
        });
        initPopup();
        bindservice();
        initDate();
        WorkGroupDetailAdapter.setOnLinearClickCallback(this, true);
        WorkGroupDetailAdapter.setOnClickCallback(this);
//        MoveTerminalAcitivity.setCleanCheckNumCallback(this);
        mNetdiscSearchTextDownload.setOnClickListener(this);
        mNetdiscSearchShow.setOnClickListener(this);
        mNetdiscSearchTextShared.setOnClickListener(this);
        mNetdiscSearchNew.setOnClickListener(this);
        mNetdiscSearchFile.setOnClickListener(this);
        mCheckTopAll.setOnClickListener(this);
        mCheckTopCancel.setOnClickListener(this);
        mDocSearchImageClear.setOnClickListener(this);
        mNetdiscSearchTextMore.setOnClickListener(this);
        mTopBarView.setOnClickListener(this);
        mNetdiscSearchTextDelete.setOnClickListener(this);
        mNetdiscSearchListEmpty.setVisibility(View.GONE);
        mDiscSearchLv.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mDiscSearchLv.setOnRefreshBothListener(WorkGroupDetailActivity.this);
        // 绑定listView的监听器
        mDiscSearchLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View parent, int key,
                                    long arg3) {

            }
        });
    }

    /**
     * 绑定service
     */
    private void bindservice() {
        Intent intent = new Intent().setClass(this, HcDownloadService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    private void initDate() {
        mStack = new Stack<MySkydriveInfoItem>();
        MySkydriveInfoItem root = new MySkydriveFoldItem();
        root.setNetdiscListType("1");
        root.setNetdiscDirLvl("");
        root.setNetdiscId("");
        root.setNetdiscListDate("");
        root.setNetdiscListFileSize("");
        root.setNetdiscListText(title);
        root.setNetdiscUpdirId(infoid);

        // 为Adapter准备数据
        mMySkydriveList = new ArrayList<MySkydriveInfoItem>();
//        List<MySkydriveInfoItem> MySkydriveInfoList = SettingSharedHelper.getCloudListForUserIdInfo(WorkGroupDetailActivity.this, root);
//        if (MySkydriveInfoList != null)
//            mMySkydriveList.addAll(MySkydriveInfoList);
//        mMySkydriveList.addAll(SettingSharedHelper.getCloudListInfo(MySkydriveActivity.this));
        HcLog.D("MySkydriveActivity #initData list size = " + mMySkydriveList.size());
        root.addAllItems(mMySkydriveList);
        mStack.add(root);

//        if (mMySkydriveAdapter == null) {
//            mMySkydriveAdapter = new WorkGroupDetailAdapter(this, mMySkydriveList);
//        }
//        mDiscSearchLv.setAdapter(mMySkydriveAdapter);
        ListRequest request = new ListRequest();
        String url = NetdiscUtil.BASE_URL + "list";
        HttpPost share = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
        builder.addTextBody("up_id", infoid, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("type", "N", ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("dirType", "S", ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        share.setEntity(builder.build());
        HcDialog.showProgressDialog(WorkGroupDetailActivity.this, "获取数据中");
        if (cResponse == null) {
            cResponse = new CloudListResponse();
        }
        request.sendRequestCommand(url, share, RequestCategory.NONE, cResponse, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<FileColumn> mUploadList = new ArrayList<FileColumn>();//所有上传列表
        List<FileColumn> mDownloadList = new ArrayList<FileColumn>();//等待上传列表
        mUploadList = OperateDatabase.getuploadList(getApplicationContext());
        mDownloadList = OperateDatabase.getDownloadList(getApplicationContext());
        if (mDownloadList.size() > 0 || mUploadList.size() > 0) {
            mDownImg.setVisibility(View.VISIBLE);
        } else {
            mDownImg.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(this);
        HttpRequestQueue.getInstance().cancelRequest(mListMD5Url);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.netdisc_search_show) {
            mNetdiscSearchShow.setVisibility(View.GONE);
            mNetdiscSearch.setVisibility(View.VISIBLE);
            mSearch.setVisibility(View.VISIBLE);
            mSearchContent.isFocused();
        } else if (i == R.id.netdisc_search_new) {
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

                    if ("".equals(netdiscEditStr) || TextUtils.isEmpty(netdiscEditStr)) {
                        String type = "N";
                        NewFileRequest request = new NewFileRequest(HttpRequestQueue.URLEncode("新建文件夹"), mStack.peek().getNetdiscId(), "", type, "S");
                        if (mResponse == null) {
                            mResponse = new NewFileResponse();
                        }
                        HcDialog.showProgressDialog(WorkGroupDetailActivity.this, "创建文件夹中");
//                        request.sendRequestCommand(RequestCategory.NewFile, mResponse, false);
                        String url = NetdiscUtil.BASE_IP + "/clouddiskM-webapp/api/clouddisk/adddir?dirName=" + HttpRequestQueue.URLEncode("新建文件夹") + "&updirId=" + mStack.peek().getNetdiscId() + "&type=" + type + "&dirType=" + "S";
                        request.sendRequestCommand(url, RequestCategory.NewFile, mResponse, false);
                    } else {
                        String type = "N";
                        NewFileRequest request = new NewFileRequest(HttpRequestQueue.URLEncode(netdiscEditStr), mStack.peek().getNetdiscId(), "", type, "S");
                        if (mResponse == null) {
                            mResponse = new NewFileResponse();
                        }
                        HcDialog.showProgressDialog(WorkGroupDetailActivity.this, "创建文件夹中");
//                        request.sendRequestCommand(RequestCategory.NewFile, mResponse, false);
                        String url = NetdiscUtil.BASE_IP + "/clouddiskM-webapp/api/clouddisk/adddir?dirName=" + HttpRequestQueue.URLEncode(netdiscEditStr) + "&updirId=" + mStack.peek().getNetdiscId() + "&type=" + type + "&dirType=" + "S";
                        request.sendRequestCommand(url, RequestCategory.NewFile, mResponse, false);
                    }
                }
            });
        } else if (i == R.id.netdisc_search_file) {
            //点击上传文件按钮
            //弹出底部的popupwindow
            //实例化SelectPicPopupWindow
            menuWindow = new SelectPicPopupWindow(this, itemsOnClick);
            //显示窗口
            menuWindow.showAtLocation(this.findViewById(R.id.my_skydrive_main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
        } else if (i == R.id.search_text) {
            //点击搜索按钮，进行搜索数据
            String searchContent = mSearchContent.getText().toString().trim();
            //搜索接口
        } else if (i == R.id.check_top_cancel) {
            mCheckTop.setVisibility(View.GONE);
            mTopBarViewLinear.setVisibility(View.VISIBLE);
            mNetdiscSearchTextFooter.setVisibility(View.VISIBLE);
            mNetdiscSearchTextFooter.setVisibility(View.GONE);
            mNetdiscSearchNew.setClickable(true);
            mNetdiscSearchFile.setClickable(true);
            mNetdiscSearchTextDownload.setClickable(false);
            mNetdiscSearchTextShared.setClickable(false);
            mNetdiscSearchTextDelete.setClickable(false);
            mNetdiscSearchTextMore.setClickable(false);
            //点击Checkbox取消按钮
            // 遍历mMySkydriveList的长度，将已选的按钮设为未选
            for (int j = 0; j < mMySkydriveList.size(); j++) {
                mMySkydriveList.get(j).setChecked(false);
//                if (mMySkydriveAdapter.getIsSelected().get(j)) {
//                    mMySkydriveAdapter.getIsSelected().put(j, false);
//                    checkNum--;// 数量减1
//                }
            }
            checkNum = 0;
            fileNum = 0;
            // 刷新listview和TextView的显示
            dataChanged();
            //下拉按钮可以使用
            mDiscSearchLv.setOnRefreshBothListener(WorkGroupDetailActivity.this);
            mDiscSearchLv.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            WorkGroupDetailAdapter.setOnLinearClickCallback(this, true);
        } else if (i == R.id.check_top_all) {
            if (!mIsSelectAll) {
                /**
                 * 处理当mMySkydriveList.size()只有一个的情况
                 * */
                if (mMySkydriveList.size() == 1 && "全不选".equals(mCheckTopAll.getText()) && mMySkydriveViewHolder.mNetdiscListCheckbox.isChecked()) {
                    mCheckTopAll.setText("全选");
                    mMySkydriveViewHolder.mNetdiscListCheckbox.toggle();
                    mySkydriveInfoItem.setChecked(false);
                    checkNum--;
                    if (!"1".equals(mySkydriveInfoItem.getNetdiscListType())) {
                        mDownloadInfoItemList.remove(mySkydriveInfoItem);
                        fileNum--;
                    }
                } else {
                    mCheckTopAll.setText("全不选");
                    mIsSelectAll = true;
                    mCheckTop.setVisibility(View.VISIBLE);
                    mNetdiscSearchTextFooter.setVisibility(View.VISIBLE);
                    mNetdiscSearchNew.setClickable(false);
                    mNetdiscSearchFile.setClickable(false);
                    mNetdiscSearchTextDownload.setClickable(true);
                    mNetdiscSearchTextShared.setClickable(true);
                    mNetdiscSearchTextDelete.setClickable(true);
                    mNetdiscSearchTextMore.setClickable(true);
                    //点击Checkbox全选按钮
                    // 遍历list的长度，将MyAdapter中的map值全部设为true
                    fileNum = 0;
                    for (int j = 0; j < mMySkydriveList.size(); j++) {
                        mMySkydriveList.get(j).setChecked(true);
                        if (!"1".equals(mMySkydriveList.get(j).getNetdiscListType())) {
                            fileNum++;
                        }
                    }
                    // 数量设为list的长度
                    checkNum = mMySkydriveList.size();
                    if (fileNum > 0) {
                        mNetdiscSearchTextDownload.setTextColor(Color.WHITE);
                        mNetdiscSearchTextShared.setTextColor(Color.WHITE);
                    }
                }
                // 刷新listview和TextView的显示
                dataChanged();

            } else if (mIsSelectAll) {
                mCheckTopAll.setText("全选");
                mIsSelectAll = false;
                mRightSelectAllBug = true;
                //点击Checkbox取消按钮
                // 遍历mMySkydriveList的长度，将已选的按钮设为未选
                for (int j = 0; j < mMySkydriveList.size(); j++) {
                    mMySkydriveList.get(j).setChecked(false);
                }
                checkNum = 0;
                fileNum = 0;
                mNetdiscSearchTextDownload.setTextColor(Color.parseColor("#4D4D4D"));
                mNetdiscSearchTextShared.setTextColor(Color.parseColor("#4D4D4D"));
                // 刷新listview和TextView的显示
                dataChanged();
            }
        } else if (i == R.id.netdisc_search_text_download) {
            checkNum = 0;
            //点击下载按钮
            if (mDownloadInfoItemList != null && mDownloadInfoItemList.size() > 0) {
//                OperateDatabase.insertDownload(mDownloadInfoItemList, HcApplication.getContext());
                mDownImg.setVisibility(View.VISIBLE);
                //点击下载以后将pop隐藏，显示为最初界面
                mTopBarViewLinear.setVisibility(View.VISIBLE);
                mCheckTop.setVisibility(View.GONE);
                mNetdiscSearchTextFooter.setVisibility(View.VISIBLE);
                mNetdiscSearchTextFooter.setVisibility(View.GONE);
                mNetdiscSearchNew.setClickable(true);
                mNetdiscSearchFile.setClickable(true);
                mNetdiscSearchTextDownload.setClickable(false);
                mNetdiscSearchTextShared.setClickable(false);
                mNetdiscSearchTextDelete.setClickable(false);
                mNetdiscSearchTextMore.setClickable(false);
                for (int j = 0; j < mDownloadInfoItemList.size(); j++) {
                    com.android.frame.download.FileColumn fileColumn = new com.android.frame.download.FileColumn();
                    fileColumn.setFileid(mDownloadInfoItemList.get(j).getNetdiscId());
                    fileColumn.setName(mDownloadInfoItemList.get(j).getNetdiscListText());
                    fileColumn.setUrl(NetdiscUtil.BASE_URL + "downloadFile");
                    fileColumn.setExt(mDownloadInfoItemList.get(j).getNetdiscListType());
                    fileColumn.setFileSize(mDownloadInfoItemList.get(j).getNetdiscListFileSize());
                    fileColumn.setPosition(0);
                    fileColumn.setState("2");
                    fileColumn.setUpdirid(mDownloadInfoItemList.get(j).getNetdiscUpdirId());
                    fileColumn.setUpOrDown(1);
                    fileColumn.setLevel(1);
                    fileColumn.setSource(HcDownloadService.NETDISC_SOURCE);
                    boolean b = OperateDatabase.insertDownload(fileColumn, HcApplication.getContext());
                    if (b) {
                        inSend = Parcel.obtain();
                        inSend.writeSerializable(fileColumn);
                        try {
                            iBinder.transact(0, inSend, null, IBinder.FLAG_ONEWAY);
                            inSend.recycle();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            //清除已经打勾的checkbox
            mDownloadInfoItemList.clear();
            cleanIsCheckBox();
        } else if (i == R.id.netdisc_search_text_shared) {//点击复制按钮

            personPowerDialog(COPY);
        } else if (i == R.id.netdisc_search_text_delete) {
            //点击删除按钮(将勾选的list删除，包括文件，再将list列表给数据库)
            if (mDownloadInfoItemList != null && mDownloadInfoItemList.size() > 0) {
//                OperateDatabase.insertDownload(mDownloadInfoItemList, HcApplication.getContext());
                //点击下载以后将pop隐藏，显示为最初界面
                mTopBarViewLinear.setVisibility(View.VISIBLE);
                mCheckTop.setVisibility(View.GONE);
                mNetdiscSearchTextFooter.setVisibility(View.VISIBLE);
                mNetdiscSearchTextFooter.setVisibility(View.GONE);
                mNetdiscSearchNew.setClickable(true);
                mNetdiscSearchFile.setClickable(true);
                mNetdiscSearchTextDownload.setClickable(false);
                mNetdiscSearchTextShared.setClickable(false);
                mNetdiscSearchTextDelete.setClickable(false);
                mNetdiscSearchTextMore.setClickable(false);
            }
            deleteFiles();
        } else if (i == R.id.netdisc_search_text_more) {
            //点击更多按钮
            showPopup();
        } else if (i == R.id.doc_search_image_clear) {
            //点击搜索栏中的x形按钮
            mNetdiscSearchShow.setVisibility(View.VISIBLE);
            mNetdiscSearch.setVisibility(View.GONE);
            mSearch.setVisibility(View.GONE);
        } else if (i == R.id.topbar_back_btn) {
            /**
             * 首先检查栈中是否有数据，若没有，在进行初始化
             * */
            if (mStack.size() > 1) {
                mMySkydriveList.clear();
                mStack.pop();
                mMySkydriveList.addAll(mStack.peek().getItems());
                topbar_title.setText(mStack.peek().getNetdiscListText());
                mMySkydriveAdapter.notifyDataSetChanged();
            } else {
                //退回到上一个Activity
                finish();
            }
        } else if (i == R.id.topbar_right_btn) {//右侧人员管理按钮
            Intent intent = new Intent();
            intent.setClass(WorkGroupDetailActivity.this, PersonnelActivity.class);
            intent.putExtra("infoid", infoid);
            intent.putExtra("type", userroleType);
            startActivity(intent);

        }

    }

    private void cleanIsCheckBox() {
        checkNum = 0;
        fileNum = 0;
        mSelectList.clear();
        mDiscSearchLv.setAdapter(null);
        if (mMySkydriveList.size() > 0) {
            for (int j = 0; j < mMySkydriveList.size(); j++) {
                mMySkydriveList.get(j).setChecked(false);
            }
            // 实例化自定义的MySkydriveAdapter
            mMySkydriveAdapter = new WorkGroupDetailAdapter(WorkGroupDetailActivity.this, mMySkydriveList);
            // 绑定Adapter
            mDiscSearchLv.setAdapter(mMySkydriveAdapter);
        }
    }

    @Override
    public void onCheckBoxClick(View view, int position, MySkydriveInfoItem item) {
        // 取得ViewHolder对象，这样就省去了通过层层的findViewById去实例化我们需要的cb实例的步骤
        mMySkydriveViewHolder = (WorkGroupDetailAdapter.MySkydriveViewHolder) view.getTag();
        mySkydriveInfoItem = (MySkydriveInfoItem) mMySkydriveAdapter.getItem(position);
        // 调整选定条目
        if (mMySkydriveViewHolder.mNetdiscListCheckbox.isChecked() == true) {
            if (!"1".equals(mySkydriveInfoItem.getNetdiscListType())) {
                mDownloadInfoItemList.add(mySkydriveInfoItem);
                fileNum++;
            } else if ("1".equals(mySkydriveInfoItem.getNetdiscListType())) {
                mdiaidInfoItemList.add(mySkydriveInfoItem);
                fileNum++;
            }
            checkNum++;
            mSelectList.add(mySkydriveInfoItem);
            if (checkNum == mMySkydriveList.size()) {
                mCheckTopAll.setText("全不选");
                mIsSelectAll = true;
            } else if (checkNum > 0 && checkNum < mMySkydriveList.size()) {
                mCheckTopAll.setText("全选");
                mIsSelectAll = false;
            }
        } else {
            if (!"1".equals(mySkydriveInfoItem.getNetdiscListType())) {
                mDownloadInfoItemList.remove(mySkydriveInfoItem);
                fileNum--;
            } else if ("1".equals(mySkydriveInfoItem.getNetdiscListType())) {
                mdiaidInfoItemList.remove(mySkydriveInfoItem);
                fileNum--;
            }
            mSelectList.remove(mySkydriveInfoItem);
            checkNum--;
            if (checkNum == 0) {
                mCheckTopAll.setText("全选");
                mIsSelectAll = false;
                mRightSelectAllBug = false;
            } else if (checkNum > 0 && checkNum < mMySkydriveList.size()) {
                mCheckTopAll.setText("全选");
                mIsSelectAll = false;
            }
        }
        if (checkNum > 0) {
            mTopBarViewLinear.setVisibility(View.VISIBLE);
            mCheckTop.setVisibility(View.VISIBLE);
            mNetdiscSearchTextFooter.setVisibility(View.VISIBLE);
            mNetdiscSearchNew.setClickable(false);
            mNetdiscSearchFile.setClickable(false);
            mNetdiscSearchTextDownload.setClickable(true);
            mNetdiscSearchTextShared.setClickable(true);
            mNetdiscSearchTextDelete.setClickable(true);
            mNetdiscSearchTextMore.setClickable(true);
            //通知adapter，只能点击整条数据选择checkbox
            //下拉按钮失效
            mDiscSearchLv.setOnRefreshBothListener(null);
            mDiscSearchLv.setMode(PullToRefreshBase.Mode.DISABLED);
            if (fileNum > 0) {
                mNetdiscSearchTextDownload.setTextColor(Color.WHITE);
                mNetdiscSearchTextShared.setTextColor(Color.WHITE);
            } else {
                fileNum = 0;
                mNetdiscSearchTextDownload.setTextColor(Color.parseColor("#4D4D4D"));
                mNetdiscSearchTextShared.setTextColor(Color.parseColor("#4D4D4D"));
            }
            WorkGroupDetailAdapter.setOnLinearClickCallback(this, false);
        } else {
            mTopBarViewLinear.setVisibility(View.VISIBLE);
            mCheckTop.setVisibility(View.GONE);
            mNetdiscSearchTextFooter.setVisibility(View.VISIBLE);
            mNetdiscSearchTextFooter.setVisibility(View.GONE);
            mNetdiscSearchNew.setClickable(true);
            mNetdiscSearchFile.setClickable(true);
            mNetdiscSearchTextDownload.setClickable(false);
            mNetdiscSearchTextShared.setClickable(false);
            mNetdiscSearchTextDelete.setClickable(false);
            mNetdiscSearchTextMore.setClickable(false);
            mDiscSearchLv.setOnRefreshBothListener(this);
            mDiscSearchLv.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            WorkGroupDetailAdapter.setOnLinearClickCallback(this, true);
        }

        if (checkNum != 1) {
            repeatPopBtn.setTextColor(Color.parseColor("#4D4D4D"));
        } else {
            repeatPopBtn.setTextColor(Color.WHITE);
        }
        mCheckTopCenter.setText("已选" + checkNum + "个");
    }

    @Override
    public void onItemClick(View view, int position, MySkydriveInfoItem item) {
        //如果这个页面在选择状态下被点击，则关闭选择状态，并将所有的checkbox设置为空
        mStack.push(item);
        mTopBarViewLinear.setVisibility(View.VISIBLE);
        mCheckTop.setVisibility(View.GONE);
        mNetdiscSearchTextFooter.setVisibility(View.VISIBLE);
        mNetdiscSearchTextFooter.setVisibility(View.GONE);
        mNetdiscSearchNew.setClickable(true);
        mNetdiscSearchFile.setClickable(true);
        mNetdiscSearchTextDownload.setClickable(false);
        mNetdiscSearchTextShared.setClickable(false);
        mNetdiscSearchTextDelete.setClickable(false);
        mNetdiscSearchTextMore.setClickable(false);
        ListRequest request = new ListRequest();
        String url = NetdiscUtil.BASE_URL + "list";
        HttpPost share = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
        builder.addTextBody("up_id", item.getNetdiscId(), ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("type", "N", ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("dirType", "S", ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        share.setEntity(builder.build());
        HcDialog.showProgressDialog(WorkGroupDetailActivity.this, "获取数据中");
        request.sendRequestCommand(url, share, RequestCategory.NONE, cResponse, false);
//        CloudListRequest request = new CloudListRequest(mStack.peek().getNetdiscId(), "N", "S");
//        if (cResponse == null) {
//            cResponse = new CloudListResponse();
//        }
//        HcDialog.showProgressDialog(WorkGroupDetailActivity.this, "获取数据中");
////            request.sendRequestCommand(RequestCategory.CloudList, cResponse, false);
//        String url = NetdiscUtil.BASE_IP + "/clouddiskM-webapp/api/clouddisk/list?type=N&up_id=" + mStack.peek().getNetdiscId();
//        request.sendRequestCommand(url, RequestCategory.CloudList, cResponse, false);

    }

    @Override
    public void onItemLinearClick(View view, int position, MySkydriveInfoItem item) {
        //如果这个页面在选择状态下被点击，则关闭选择状态，并将所有的checkbox设置为空
        if (checkNum >= 0) {
            // 取得ViewHolder对象，这样就省去了通过层层的findViewById去实例化我们需要的cb实例的步骤
            mMySkydriveViewHolder = (WorkGroupDetailAdapter.MySkydriveViewHolder) view.getTag();
            mySkydriveInfoItem = (MySkydriveInfoItem) mMySkydriveAdapter.getItem(position);
            if (checkNum == 0 && !mRightSelectAllBug) {
                mCheckTop.setVisibility(View.GONE);
                mTopBarViewLinear.setVisibility(View.VISIBLE);
                mNetdiscSearchTextFooter.setVisibility(View.VISIBLE);
                mNetdiscSearchTextFooter.setVisibility(View.GONE);
                mNetdiscSearchNew.setClickable(true);
                mNetdiscSearchFile.setClickable(true);
                mNetdiscSearchTextDownload.setClickable(false);
                mNetdiscSearchTextShared.setClickable(false);
                mNetdiscSearchTextDelete.setClickable(false);
                mNetdiscSearchTextMore.setClickable(false);
                //点击Checkbox取消按钮
                // 遍历mMySkydriveList的长度，将已选的按钮设为未选
                for (int j = 0; j < mMySkydriveList.size(); j++) {
                    mMySkydriveList.get(j).setChecked(false);
                }
                checkNum = 0;
                fileNum = 0;
                // 刷新listview和TextView的显示
                dataChanged();
                //下拉按钮可以使用
                mDiscSearchLv.setOnRefreshBothListener(WorkGroupDetailActivity.this);
                mDiscSearchLv.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                WorkGroupDetailAdapter.setOnLinearClickCallback(this, true);
            } else {
                if (mMySkydriveViewHolder.mNetdiscListCheckbox.isChecked()) {
                    mMySkydriveViewHolder.mNetdiscListCheckbox.toggle();
                    mySkydriveInfoItem.setChecked(false);
                    checkNum--;
                    mCheckTopAll.setText("全选");
                    if (checkNum < mMySkydriveList.size()) {
                        mIsSelectAll = false;
                    } else {
                        mIsSelectAll = true;
                    }
                    if (checkNum == 0) {
                        mRightSelectAllBug = true;
                    }
                } else if (mMySkydriveViewHolder.mNetdiscListCheckbox.isChecked() == false) {
                    mMySkydriveViewHolder.mNetdiscListCheckbox.toggle();
                    mySkydriveInfoItem.setChecked(true);
                    checkNum++;
                    if (checkNum < mMySkydriveList.size()) {
                        mIsSelectAll = false;
                    } else {
                        mIsSelectAll = true;
                    }
                }
                if (checkNum == mMySkydriveList.size()) {
                    mCheckTopAll.setText("全不选");
                }
            }
        }
        mCheckTopCenter.setText("已选" + checkNum + "个");
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
        ListRequest request = new ListRequest();
        String url = NetdiscUtil.BASE_URL + "list";
        HttpPost share = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
        builder.addTextBody("up_id", infoid, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("type", "N", ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("dirType", "S", ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        share.setEntity(builder.build());
        HcDialog.showProgressDialog(WorkGroupDetailActivity.this, "获取数据中");
        if (cResponse == null) {
            cResponse = new CloudListResponse();
        }
        request.sendRequestCommand(url, share, RequestCategory.NONE, cResponse, false);
//        //下拉
//        String upId = mStack.peek().getNetdiscId();
//        String type = mStack.size() > 1 ? "N" : "R";
//        CloudListRequest request = new CloudListRequest(infoid, "N", "S");
//        if (cResponse == null) {
//            cResponse = new CloudListResponse();
//        }
//        HcDialog.showProgressDialog(WorkGroupDetailActivity.this, "获取数据中");
//        /**
//         * 存储页面状态
//         * */
////        SettingSharedHelper.setTypeRorN(this, type);
//        String url = NetdiscUtil.BASE_IP + "/clouddiskM-webapp/api/clouddisk/list?type=" + type + "&up_id=" + mStack.peek().getNetdiscId();
//        request.sendRequestCommand(url, RequestCategory.CloudList, cResponse, false);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {
        //上拉
    }


    Parcel inSend;
    IBinder iBinder;
    HcDownloadService mService;
    com.android.frame.download.FileColumn mFileColumn;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (name.getShortClassName().endsWith("HcDownloadService")) {
            this.iBinder = service;
            mService = ((HcDownloadService.MyBind) service).getMyService();
            ServiceCallBack.getInstance().getService(mService);
            ServiceCallBack.getInstance().setTransferCallback(this);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    public void transferCallback(FileColumn fileColumn) {
        mFileColumn = fileColumn;
    }

    private class NewFileRequest extends AbstractHttpRequest {
        private static final String TAG = WorkGroupDetailActivity.TAG + "$MySkydriveRequest";
        Map<String, String> httpparams = new HashMap<String, String>();

        public NewFileRequest(String dirName, String updirId, String dirLvl, String type, String dirType) {
            httpparams.put("dirName", dirName);
            httpparams.put("updirId", updirId);
            httpparams.put("dirLvl", "");
            httpparams.put("type", type);
            httpparams.put("dirType", dirType);
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

        private static final String TAG = WorkGroupDetailActivity.TAG + "$MySkydriveResponse";

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            /**
             * 利用Sharepreference存储json数据,要是是跟目录的话,需要存储.
             * */
//            if (mStack.size() == 1) {
//                SettingSharedHelper.saveUserIdCloudListInfo(WorkGroupDetailActivity.this, data.toString());
//            }
            HcDialog.deleteProgressDialog();
            mMySkydriveList.clear();
            String mUpId = null;
            try {
                JSONObject object = new JSONObject((String) data);
                if (HcUtil.hasValue(object, "up_id")) {
                    mUpId = object.getString("up_id");
                    if (mStack.size() == 1) {
                        mStack.peek().setNetdiscId(mUpId);
                    }
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
                    if (mMySkydriveAdapter == null) {
                        // 实例化自定义的MySkydriveAdapter
                        mMySkydriveAdapter = new WorkGroupDetailAdapter(WorkGroupDetailActivity.this, mMySkydriveList);
                        // 绑定Adapter
                        mDiscSearchLv.setAdapter(mMySkydriveAdapter);
                    } else {
                        mMySkydriveAdapter.notifyDataSetChanged();
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
            HcUtil.reLogining(data, WorkGroupDetailActivity.this, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
            HcLog.D(TAG + " #notifyRequestMd5Url md5Url = " + md5Url + " request = " + request);
        }

    }

    private void initPopup() {
        if (popupMenu == null) {
            LayoutInflater lay = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = lay.inflate(R.layout.netdisc_workgroup_popup, null);
            movePopBtn = (TextView) v.findViewById(R.id.move_btn);
            repeatPopBtn = (TextView) v.findViewById(R.id.repeat_name_btn);
            share_btn = (TextView) v.findViewById(R.id.share_btn);
            share_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupMenu.dismiss();
                    shareDialog(WorkGroupDetailActivity.this);
                }
            });
            movePopBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupMenu.dismiss();
                    personPowerDialog(MOVE);
                }
            });
            repeatPopBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupMenu.dismiss();
                    //点击重命名按钮
                    if (mSelectList.size() == 1) {
                        final AlertDialog dialog = new AlertDialog.Builder(WorkGroupDetailActivity.this)
                                .create();
                        dialog.setCancelable(false);
                        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View mDialogView = inflater.inflate(R.layout.netdisc_rename_folder_dialog, null);
                        dialog.setView(mDialogView);
                        dialog.show();
                        dialog.getWindow().setContentView(R.layout.netdisc_rename_folder_dialog);
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
                                if (TextUtils.isEmpty(netdiscEditStr)) {
                                    dialog.dismiss();
                                } else {
                                    String type = null;
                                    if (mSelectList.get(0).getNetdiscListType() != "1") {
                                        type = "D";
                                    } else {
                                        type = "F";
                                    }
                                    //清空checkbox
                                    checkNum = 0;
                                    fileNum = 0;
                                    rRequest = new ReNameRequest(mSelectList.get(0).getNetdiscId(), netdiscEditStr, type, "S");
                                    if (rResponse == null) {
                                        rResponse = new ReNameResponse();
                                    }
                                    HcDialog.showProgressDialog(WorkGroupDetailActivity.this, "文件重命名中");
//                                    rRequest.sendRequestCommand(RequestCategory.NONE, rResponse, false);
                                    String url = NetdiscUtil.BASE_IP + "/clouddiskM-webapp/api/clouddisk/rename?infoid=" + mSelectList.get(0).getNetdiscId() + "&infoname=" + HttpRequestQueue.URLEncode(netdiscEditStr) + "&type=" + type + "&dirType=" + "S";
                                    mSelectList.clear();
                                    rRequest.sendRequestCommand(url, RequestCategory.NONE, rResponse, false);
                                }
                            }
                        });
                    }
                }
            });
            popupMenu = new PopupWindow(v, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
//   popupMenu = new PopupWindow(v, getApplicationContext().getResources().getDisplayMetrics().widthPixels / 3,
//                    getApplicationContext().getResources().getDisplayMetrics().heightPixels / 4, true);
        }
    }

    private void showPopup() {
        //设置整个popupwindow的样式。
        popupMenu.setBackgroundDrawable(new BitmapDrawable());
        //使窗口里面的空间显示其相应的效果，比较点击button时背景颜色改变。
        //如果为false点击相关的空间表面上没有反应，但事件是可以监听到的。
        //listview的话就没有了作用。
        popupMenu.setFocusable(true);
        popupMenu.setOutsideTouchable(true);
        popupMenu.update();
//        popupMenu.showAsDropDown(mNetdiscSearchTextMore);
        popupMenu.showAtLocation(mNetdiscSearchTextFooter, Gravity.RIGHT | Gravity.BOTTOM, 0, HcUtil.dip2px(getApplicationContext(), 50));
    }

    //为弹出窗口实现监听类
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {

        public void onClick(View v) {
            menuWindow.dismiss();
            int i = v.getId();
            if (i == R.id.netdisc_picture_linear) {
                //点击图片按钮
//                Intent intent = new Intent(MySkydriveActivity.this, PictureChooseActivity.class);
                Intent intent = new Intent(WorkGroupDetailActivity.this, CommonActivity.class);
                intent.putExtra("className", "com.android.hcframe.netdisc.image.ImagePage");
                intent.putExtra("title", "图片选择");
                intent.putExtra("data", getJsonData());
                startActivityForResult(intent, 100);
            } else if (i == R.id.netdisc_music_linear) {
//                Intent intent = new Intent(MySkydriveActivity.this, FileChooseActivity.class);
//                intent.putExtra("key", MUSIC);
//                startActivity(intent);
                Intent intent = new Intent(WorkGroupDetailActivity.this, CommonActivity.class);
                intent.putExtra("className", "com.android.hcframe.netdisc.audio.AudioPage");
                intent.putExtra("title", "音乐选择");
                intent.putExtra("data", getJsonData());
                startActivityForResult(intent, 100);
                //点击音乐按钮
            } else if (i == R.id.netdisc_video_linear) {
//                Intent intent = new Intent(MySkydriveActivity.this, FileChooseActivity.class);
//                intent.putExtra("key", MOVIE);
//                startActivity(intent);
                Intent intent = new Intent(WorkGroupDetailActivity.this, CommonActivity.class);
                intent.putExtra("className", "com.android.hcframe.netdisc.video.VideoPage");
                intent.putExtra("title", "视频选择");
                intent.putExtra("data", getJsonData());
                startActivityForResult(intent, 100);
                //点击视频按钮
            } else if (i == R.id.netdisc_file_linear) {
                //点击文件按钮，跳转到文件选择页面
//                Intent intent = new Intent(MySkydriveActivity.this, FileChooseActivity.class);
//                intent.putExtra("key", FILE);
//                startActivity(intent);
                Intent intent = new Intent(WorkGroupDetailActivity.this, CommonActivity.class);
                intent.putExtra("className", "com.android.hcframe.netdisc.file.FilePage");
                intent.putExtra("title", "文件选择");
                intent.putExtra("data", getJsonData());
                startActivityForResult(intent, 100);
            }
        }

    };

    // 刷新listview和TextView的显示

    private void dataChanged() {
        // 通知listView刷新
        mMySkydriveAdapter.notifyDataSetChanged();
        // TextView显示最新的选中数目
        mCheckTopCenter.setText("已选" + checkNum + "个");
    }

    private class CloudListRequest extends AbstractHttpRequest {
        private static final String TAG = WorkGroupDetailActivity.TAG + "$MySkydriveRequest";
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
        private static final String TAG = WorkGroupDetailActivity.TAG + "$MySkydriveResponse";

        public CloudListResponse() {
        }

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            /**
             * 缓存首页数据
             * */
//            if (mStack.size() == 1) {
////                SettingSharedHelper.saveCloudListInfo(MySkydriveActivity.this, data.toString());
//                SettingSharedHelper.saveUserIdCloudListInfo(WorkGroupDetailActivity.this, data.toString());
//            }

            HcDialog.deleteProgressDialog();
            mMySkydriveList.clear();
            String mUpId = null;
            try {
                JSONObject object = new JSONObject((String) data);
                if (HcUtil.hasValue(object, "up_id")) {
                    mUpId = object.getString("up_id");
                    mUpDirId = mUpId;
                    if (mStack.size() == 1) {
                        mStack.peek().setNetdiscId(mUpId);
                    }
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
                }

            } catch (JSONException e) {
                HcLog.D(TAG + " #onSuccess parseJson error =" + e);
            }
            mStack.peek().getItems().clear();
            HcLog.D(TAG + " #onSuccess parseJson stack size = " + mStack.size() + " list size =" + mMySkydriveList.size());
            mStack.peek().addAllItems(mMySkydriveList);
            final String ti = mStack.peek().getNetdiscListText();

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    topbar_title.setText(ti);
                    //清空adapter
                    mDiscSearchLv.onRefreshComplete();
                    if (mMySkydriveAdapter == null) {
                        // 实例化自定义的MySkydriveAdapter
                        mMySkydriveAdapter = new WorkGroupDetailAdapter(WorkGroupDetailActivity.this, mMySkydriveList);
                        // 绑定Adapter
                        mDiscSearchLv.setAdapter(mMySkydriveAdapter);
                    } else {
                        mMySkydriveAdapter.notifyDataSetChanged();
                    }
                }
            });
        }


        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            mDiscSearchLv.onRefreshComplete();
            HcDialog.deleteProgressDialog();
            mMySkydriveList.clear();
            mMySkydriveAdapter.notifyDataSetChanged();
            HcUtil.reLogining(data, WorkGroupDetailActivity.this, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
            mListMD5Url = md5Url;
        }

        @Override
        public void onNetworkInterrupt(RequestCategory request) {
            mDiscSearchLv.onRefreshComplete();
            super.onNetworkInterrupt(request);
        }

        @Override
        public void onConnectionTimeout(RequestCategory request) {
            mDiscSearchLv.onRefreshComplete();
            super.onConnectionTimeout(request);
        }

        @Override
        public void onParseDataError(RequestCategory request) {
            mDiscSearchLv.onRefreshComplete();
            super.onParseDataError(request);
        }

        @Override
        public void onRequestFailed(int code, String msg, RequestCategory request) {
            mDiscSearchLv.onRefreshComplete();
            super.onRequestFailed(code, msg, request);
        }

        @Override
        public void onResponseFailed(int code, RequestCategory request) {
            mDiscSearchLv.onRefreshComplete();
            super.onResponseFailed(code, request);
        }

        @Override
        public void unknown(RequestCategory request) {
            mDiscSearchLv.onRefreshComplete();
            super.unknown(request);
        }
    }

    private class ReNameRequest extends AbstractHttpRequest {
        private static final String TAG = WorkGroupDetailActivity.TAG + "$MySkydriveRequest";
        Map<String, String> httpparams = new HashMap<String, String>();

        public ReNameRequest(String infoId, String infoName, String type, String dirType) {
            httpparams.put("infoid", infoId);
            httpparams.put("infoname", infoName);
            httpparams.put("type", type);
            httpparams.put("dirType", dirType);
        }

        public String getRequestMethod() {
            return "clouddiskM-webapp/api/clouddisk/rename";
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

    private class ReNameResponse extends AbstractHttpResponse {
        private static final String TAG = WorkGroupDetailActivity.TAG + "$MySkydriveResponse";

        public ReNameResponse() {
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            HcDialog.deleteProgressDialog();
//            if (mStack.size() == 1) {
//                SettingSharedHelper.saveUserIdCloudListInfo(WorkGroupDetailActivity.this, data.toString());
//            }
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
                    if (mMySkydriveAdapter == null) {
                        // 实例化自定义的MySkydriveAdapter
                        mMySkydriveAdapter = new WorkGroupDetailAdapter(WorkGroupDetailActivity.this, mMySkydriveList);
                        // 绑定Adapter
                        mDiscSearchLv.setAdapter(mMySkydriveAdapter);
                    } else {
                        mMySkydriveAdapter.notifyDataSetChanged();
                    }

                }
            });
        }

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcDialog.deleteProgressDialog();
            mMySkydriveList.clear();
            mMySkydriveAdapter.notifyDataSetChanged();
            HcUtil.reLogining(data, WorkGroupDetailActivity.this, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

        }
    }

    private class DeleteListRequest extends AbstractHttpRequest {

        private static final String TAG = WorkGroupDetailActivity.TAG + "$MySkydriveRequest";

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

    private class DeleteListResponse extends AbstractHttpResponse {
        private static final String TAG = WorkGroupDetailActivity.TAG + "$MySkydriveResponse";

        public DeleteListResponse() {
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            HcDialog.deleteProgressDialog();
//            if (mStack.size() == 1) {
//                SettingSharedHelper.saveUserIdCloudListInfo(WorkGroupDetailActivity.this, data.toString());
//            }
            mSelectList.clear();
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
                    mDownloadInfoItemList.clear();
                    if (mMySkydriveAdapter == null) {
                        // 实例化自定义的MySkydriveAdapter
                        mMySkydriveAdapter = new WorkGroupDetailAdapter(WorkGroupDetailActivity.this, mMySkydriveList);
                        // 绑定Adapter
                        mDiscSearchLv.setAdapter(mMySkydriveAdapter);
                    } else {
                        mMySkydriveAdapter.notifyDataSetChanged();
                    }

                }
            });
        }

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcDialog.deleteProgressDialog();
            mSelectList.clear();
            mMySkydriveList.clear();
            mDownloadInfoItemList.clear();
            mMySkydriveAdapter.notifyDataSetChanged();
            HcUtil.reLogining(data, WorkGroupDetailActivity.this, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

        }

    }

    private String getJsonData() {
        JSONObject object = new JSONObject();
        try {
            object.accumulate(FILE_NAME, mStack.peek().getNetdiscListText());
            object.accumulate(FILE_ID, mStack.peek().getNetdiscId());
        } catch (JSONException e) {
            HcLog.D(TAG + " #getJsonData JSONException e =" + e);
        }
        HcLog.D(TAG + "#getJsonData data = " + object.toString());
        return object.toString();
    }

    private void deleteFiles() {
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
        HcDialog.showProgressDialog(WorkGroupDetailActivity.this, "删除数据中");
        checkNum = 0;
        fileNum = 0;
        String url = NetdiscUtil.BASE_URL + "deletefile";
        mSelectList.clear();
        HcLog.D(TAG + " #deleteFiles url = " + url);
        if (dResponse == null) {
            dResponse = new DeleteListResponse();
        }
        HttpPost delete = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
        builder.addTextBody("infoid", files, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("dirid", folders, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("dirType", "S", ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        delete.setEntity(builder.build());
        cleanIsCheckBox();
        DeleteListRequest request = new DeleteListRequest();
        request.sendRequestCommand(url, delete, RequestCategory.NONE, dResponse, false);
    }

    private ShareDialog shareDialog;

    /**
     * 分享弹出dialog
     *
     * @param context activity实例
     */
    private void shareDialog(final Context context) {
        if (shareDialog == null) {
//            cleanCache(context);
            shareDialog = ShareDialog.createDialog(context);
            ShareDialog.netdisc_id_pub_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    shareFiles("A");
                    shareDialog.dismiss();
                    shareDialog = null;

                }
            });
            ShareDialog.netdisc_id_pri_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    shareFiles("B");
                    shareDialog.dismiss();
                    shareDialog = null;
                }
            });
            shareDialog.show();
        } else {
            shareDialog.dismiss();
            shareDialog = null;
        }

    }

    private void shareFiles(String type) {
        if (mDownloadInfoItemList.isEmpty() && mdiaidInfoItemList.isEmpty()) return;
        StringBuilder fileBuilder = new StringBuilder();//文件ID
        StringBuilder folderBuilder = new StringBuilder();//文件夹id
        int num = mDownloadInfoItemList.size() + mdiaidInfoItemList.size();
        String name = null;
        for (int i = 0; i < mDownloadInfoItemList.size(); i++) {
            if (name == null || "".equals(name)) {
                name = mDownloadInfoItemList.get(0).getNetdiscListText();
            }
            fileBuilder.append(mDownloadInfoItemList.get(i).getNetdiscId() + ",");
        }
        for (int i = 0; i < mdiaidInfoItemList.size(); i++) {
            if (name == null || "".equals(name)) {
                name = mdiaidInfoItemList.get(0).getNetdiscListText();
            }
            folderBuilder.append(mdiaidInfoItemList.get(i).getNetdiscId() + ",");
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

        DeleteListRequest request = new DeleteListRequest();
        String url = NetdiscUtil.BASE_URL + "sharefile";
        HttpPost share = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
        builder.addTextBody("infoid", files, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("dirid", folders, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("type", type);
        share.setEntity(builder.build());
        request.sendRequestCommand(url, share, RequestCategory.NONE, new ShareResponse(num, name), false);
        //点击以后将pop隐藏，显示为最初界面
        mTopBarViewLinear.setVisibility(View.VISIBLE);
        mCheckTop.setVisibility(View.GONE);
        mNetdiscSearchTextFooter.setVisibility(View.VISIBLE);
        mNetdiscSearchTextFooter.setVisibility(View.GONE);
        mNetdiscSearchNew.setClickable(true);
        mNetdiscSearchFile.setClickable(true);
        mNetdiscSearchTextDownload.setClickable(false);
        mNetdiscSearchTextShared.setClickable(false);
        mNetdiscSearchTextDelete.setClickable(false);
        mNetdiscSearchTextMore.setClickable(false);
        //清除已经打勾的checkbox
        cleanIsCheckBox();
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
                Intent intent = new Intent(WorkGroupDetailActivity.this, ShareSuccessActivity.class);
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

    private class ListRequest extends AbstractHttpRequest {

        private static final String TAG = WorkGroupDetailActivity.TAG + "ListRequest";

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

    private Dialog moveDialog;
    private LinearLayout netdisc_id_tv_netdisc, netdisc_id_tv_myshare, netdisc_id_tv_share;

    /**
     * 弹出dialog
     */
    private void personPowerDialog(final String type) {
        if (moveDialog == null) {
            moveDialog = new Dialog(WorkGroupDetailActivity.this, R.style.CustomAlterDialog);
            LayoutInflater inflater = LayoutInflater.from(WorkGroupDetailActivity.this);
            View view = inflater.inflate(R.layout.netdisc_alter_dialog_move, null);
            moveDialog.setContentView(view);
            Window win = moveDialog.getWindow();
            win.getAttributes().gravity = Gravity.CENTER;
            netdisc_id_tv_netdisc = (LinearLayout) moveDialog.findViewById(R.id.netdisc_id_ll_netdisc);
            netdisc_id_tv_myshare = (LinearLayout) moveDialog.findViewById(R.id.netdisc_id_ll_myshare);
            netdisc_id_tv_share = (LinearLayout) moveDialog.findViewById(R.id.netdisc_id_ll_share);
            moveDialog.findViewById(R.id.netdisc_id_iv_close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    moveDialog.dismiss();
                    moveDialog = null;
                }
            });
            netdisc_id_tv_netdisc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    move(type, MoveTerminalAcitivity.NETDISC);
                }
            });
            netdisc_id_tv_myshare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    move(type, MoveTerminalAcitivity.MYSHARE);
                }
            });
            netdisc_id_tv_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    move(type, MoveTerminalAcitivity.SHARE);
                }
            });

            moveDialog.show();
        } else {
            moveDialog.dismiss();
            moveDialog = null;
        }

    }

    public void move(String type, int from) {
        moveDialog.dismiss();
        moveDialog = null;
        //点击以后将pop隐藏，显示为最初界面
        mTopBarViewLinear.setVisibility(View.VISIBLE);
        mCheckTop.setVisibility(View.GONE);
        mNetdiscSearchTextFooter.setVisibility(View.VISIBLE);
        mNetdiscSearchTextFooter.setVisibility(View.GONE);
        mNetdiscSearchNew.setClickable(true);
        mNetdiscSearchFile.setClickable(true);
        mNetdiscSearchTextDownload.setClickable(false);
        mNetdiscSearchTextShared.setClickable(false);
        mNetdiscSearchTextDelete.setClickable(false);
        mNetdiscSearchTextMore.setClickable(false);
        Intent intent = new Intent(WorkGroupDetailActivity.this, MoveTerminalAcitivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("mMySkydriveList", mMySkydriveList);
        bundle.putSerializable("mSelectList", (Serializable) mSelectList);
        if (type.equals(COPY)) {
            //将mStack对象传递过去
            bundle.putSerializable("mStack", mStack);
            bundle.putString("copy", "copy");
        } else {
            //将mStack对象传递过去
            bundle.putSerializable("mStack", mStack);
            intent.putExtra("user", mStack);
        }
//        mSelectList.clear();
        intent.putExtra(MoveTerminalAcitivity.FROM, from);
        intent.putExtras(bundle);
        WorkGroupDetailActivity.this.startActivityForResult(intent, 99);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            mDownImg.setVisibility(View.VISIBLE);
        } else if (requestCode == 99) {
            mDiscSearchLv.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            //清除已经打勾的checkbox
            mDownloadInfoItemList.clear();
            cleanIsCheckBox();
        }
    }
}