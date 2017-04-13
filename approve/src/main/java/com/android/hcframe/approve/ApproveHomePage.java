package com.android.hcframe.approve;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.android.frame.download.DownloadUtil;
import com.android.frame.download.FileColumn;
import com.android.frame.download.HcDownloadService;
import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcApplication;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.ebook.Params;
import com.android.hcframe.ebook.SignActivity;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.HttpRequestQueue;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshListView;
import com.android.hcframe.sql.SettingHelper;
import com.android.hcframe.view.EditTextAlterDialog;
import com.android.hcframe.view.TwoBtnAlterDialog;
import com.android.hcframe.zxing.decoding.Intents;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Created by pc on 2016/6/22.
 */
public class ApproveHomePage extends AbstractPage implements PullToRefreshBase.OnRefreshBothListener, ServiceConnection, ServiceCallBack.TransferCallback {
    private static final String TAG = "ApproveHomePage";
    private final String mAppId;
    private TopBarView mBarView;
    ApproveAdapter approveAdapter;
    PullToRefreshListView approve_lv;
    public static final int NUM = 10;
    public int page = 1;
    int length;
    private Handler mHandler = new Handler();
    private String mListMD5Url;
    List<JSONObject> projectList = new ArrayList<JSONObject>();
    public static final int COMMON = 1;
    public static final int DOWN = 2;
    public static final int UP = 3;

    protected ApproveHomePage(Activity context, ViewGroup group, String appId) {
        super(context, group);
        mAppId = appId;
    }

    @Override
    public void initialized() {
        bindservice();
        initData(COMMON);
    }

    EditTextAlterDialog edittextDialog;

    @Override
    public void setContentView() {
        if (isFirst) {
            isFirst = !isFirst;
            mView = mInflater.inflate(R.layout.approve_homepage, null);//关联布局文件
            approve_lv = (PullToRefreshListView) mView.findViewById(R.id.approve_lv);
            approve_lv.setMode(PullToRefreshBase.Mode.BOTH);
            approve_lv.setOnRefreshBothListener(ApproveHomePage.this);
            approve_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    JSONObject jsonObject = (JSONObject) parent.getAdapter().getItem(position);
                    downPDF(jsonObject);

                }
            });
//            View topbar = mContext.findViewById(R.id.menu_top_bar);
//            if (topbar != null) {
//                mBarView = (TopBarView) topbar;
//                mBarView.setMenuBtnVisiable(View.VISIBLE);
//                mBarView.setMenuSrc(R.drawable.approve_add_btn);
//                mBarView.setMenuListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
////                        if (mPopParent.getVisibility() == View.VISIBLE) {
////                            mPopParent.setVisibility(View.GONE);
////                        } else {
////                            mPopParent.setVisibility(View.VISIBLE);
////                        }
//                    }
//                });
//            }
        }
    }

    /**
     * 绑定service
     */
    private void bindservice() {
        Intent intent = new Intent().setClass(mContext, HcDownloadService.class);
        mContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    public static final String BASE_URL = "/terminalServer/szf/";

    private void initData(int place) {
        ListRequest request = new ListRequest();
        String url = HcUtil.getScheme() + BASE_URL + "getApproveList";
//        String url="http://10.80.6.13:8080"+ BASE_URL + "getApproveList";
        HttpPost share = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
        builder.addTextBody("size", NUM + "", ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("currentpage", page + "", ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        share.setEntity(builder.build());
        if (place == COMMON) {
            HcDialog.showProgressDialog(mContext, "获取数据中");
        }
        request.sendRequestCommand(url, share, RequestCategory.NONE, new CloudListResponse(place), false);

//        JSONArray jsonArray = new JSONArray();
//
//        for (int i = 0; i < 20; i++) {
//            JSONObject jsonObject = new JSONObject();
//            jsonArray.put(jsonObject);
//        }
//
//        approveAdapter = new ApproveAdapter(mContext, jsonArray);
//        approve_lv.setAdapter(approveAdapter);


    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
        approve_lv.setMode(PullToRefreshBase.Mode.BOTH);
        page = 1;
        initData(DOWN);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {
        if (length == NUM) {
            page++;
            initData(UP);
        } else {
            approve_lv.onRefreshComplete();
            approve_lv.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        }

    }


    private class ListRequest extends AbstractHttpRequest {

        private static final String TAG = ApproveHomePage.TAG + "ListRequest";

        public ListRequest() {

        }

        public String getRequestMethod() {
            return "terminalServer/szf/getApproveList";
        }

        @Override
        public String getParameterUrl() {

            return "";
        }

    }

    private class CloudListResponse extends AbstractHttpResponse {
        private static final String TAG = ApproveHomePage.TAG + "$MySkydriveResponse";
        int place;

        public CloudListResponse(int place) {
            this.place = place;
        }

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            HcDialog.deleteProgressDialog();

            try {
                JSONObject object = new JSONObject((String) data);
                JSONArray jsonArray = object.optJSONArray("projectList");
                length = jsonArray.length();

                if (place == UP) {
                } else {
                    projectList.clear();
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    projectList.add((JSONObject) jsonArray.get(i));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //清空adapter
                    approve_lv.onRefreshComplete();
                    if (length != NUM) {
                        approve_lv.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                    }
                    if (approveAdapter == null) {
                        // 实例化自定义的MySkydriveAdapter
                        approveAdapter = new ApproveAdapter(mContext, projectList);
                        approve_lv.setAdapter(approveAdapter);
                    } else {
                        approveAdapter.notifyDataSetChanged();
                    }
                }
            });
        }


        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            approve_lv.onRefreshComplete();
            HcDialog.deleteProgressDialog();
            approveAdapter.notifyDataSetChanged();
            HcUtil.reLogining(data, mContext, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
            mListMD5Url = md5Url;
        }

        @Override
        public void onNetworkInterrupt(RequestCategory request) {
            approve_lv.onRefreshComplete();
            super.onNetworkInterrupt(request);
        }

        @Override
        public void onConnectionTimeout(RequestCategory request) {
            approve_lv.onRefreshComplete();
            super.onConnectionTimeout(request);
        }

        @Override
        public void onParseDataError(RequestCategory request) {
            approve_lv.onRefreshComplete();
            super.onParseDataError(request);
        }

        @Override
        public void onRequestFailed(int code, String msg, RequestCategory request) {
            approve_lv.onRefreshComplete();
            super.onRequestFailed(code, msg, request);
        }

        @Override
        public void onResponseFailed(int code, RequestCategory request) {
            approve_lv.onRefreshComplete();
            super.onResponseFailed(code, request);
        }

        @Override
        public void unknown(RequestCategory request) {
            approve_lv.onRefreshComplete();
            super.unknown(request);
        }
    }

    @Override
    public void onResume() {

    }

    @Override
    public void update(Observable observable, Object data) {

    }

    @Override
    public void onDestory() {
        HttpRequestQueue.getInstance().cancelRequest(mListMD5Url);
        mContext.unbindService(this);
        super.onDestory();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
    }

    String projectId;
    String power;
    String source;
    String approveId;

    public void downPDF(JSONObject jsonObject) {
        projectId = jsonObject.optString("projectId");
        String approveUserId = jsonObject.optString("approveUserId");
        String userid = SettingHelper.getUserId(HcApplication.getContext());
        String sts = jsonObject.optString("sts");
        approveId = jsonObject.optString("approveId");
        source = Params.FROM_EDIT;
        if ("0".equals(sts)) {
            if (approveUserId.equals(userid)) {
                power = Params.EDIT;
            } else {
                power = Params.READONLY;
            }
        } else {
            power = Params.READONLY;
        }

        com.android.frame.download.FileColumn fileColumn = new com.android.frame.download.FileColumn();
        String fileID = jsonObject.optString("pdfFile");
        String fileList = ApproveUtil.getFileIDSP(mContext);
        boolean b = false;
        if (fileList != null && !"".equals(fileList)) {
            String[] strings = fileList.split(",");
            for (int i = 0; i < strings.length; i++) {
                if (strings[i].equals(fileID)) {
                    b = true;
                    break;
                }
            }
        }
        fileColumn.setFileid(jsonObject.optString("pdfFile"));
        fileColumn.setName(jsonObject.optString("projectName") + ".pdf");
        fileColumn.setUrl(HcConfig.getConfig().getServerName(HcConfig.Module.CLOUD_DISK) + ":" + HcConfig.getConfig().getServerPort(HcConfig.Module.CLOUD_DISK)+"/clouddiskM-webapp/api/clouddisk/" + "downloadFile");
//        fileColumn.setUrl("http://mobile.zjhcsoft.com:16201/clouddiskM-webapp/api/clouddisk/" + "downloadFile");
        fileColumn.setExt("pdf");
        fileColumn.setFileSize("0");
        fileColumn.setPosition(0);
        fileColumn.setState("0");
        fileColumn.setUpdirid("");
        fileColumn.setUpOrDown(1);
        fileColumn.setLevel(4);
        fileColumn.setSource(HcDownloadService.PDF_SOURCE);
        if (!b) {
//        boolean b = OperateDatabase.insertDownload(fileColumn, HcApplication.getContext());
//        if (b) {
            HcDialog.showProgressDialog(mContext, "文件下载中...");
            inSend = Parcel.obtain();
            inSend.writeSerializable(fileColumn);
            try {
                iBinder.transact(0, inSend, null, IBinder.FLAG_ONEWAY);
                inSend.recycle();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            fileColumn.setPath(HcDownloadService.DWONLOAD_PATH + "/" + fileColumn.getName());
            File file = new File(fileColumn.getPath());
            boolean exists = file.exists();
            if (exists) {
                doOpenFile(file, fileColumn);
            } else {
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

//        }
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

    FileColumn fileColumn;

    @Override
    public void transferCallback(FileColumn fileColumn) {
        this.fileColumn = fileColumn;

        if (fileColumn.getSuccess() == 1) {
            HcDialog.deleteProgressDialog();
//            fileColumn.setPath(fileColumn.getPath()+".pdf");
            File file = new File(fileColumn.getPath());
            boolean b = file.exists();
            if (b) {
                ApproveUtil.setFileIDSP(mContext, fileColumn.getFileid());
                doOpenFile(file, fileColumn);
            }else{
                HcUtil.showToast(mContext, "下载失败");
            }
        } else {
            if (!"0".equals(fileColumn.getState())) {
                HcDialog.deleteProgressDialog();
                HcUtil.showToast(mContext, "下载失败");
            }
        }
    }

    private void doOpenFile(File file, FileColumn fileColumn) {
        final Uri uri = Uri.fromFile(file);
        final Intent intent = new Intent("android.intent.action.VIEW", uri);
        intent.setClassName(mContext, "com.android.hcframe.ebook.SignActivity");
        intent.putExtra("copyRight", Params.COPYRIGHT);
        intent.putExtra("isVectorSign", true); // 是否矢量
        intent.putExtra("saveVector", true); // 是否保存矢量
        intent.putExtra("isUseEbenSDK", false);// 是否使用E人E本模式
        Bundle bundle = new Bundle();
        bundle.putSerializable(Params.FILEINFO, fileColumn);// 文件信息
        bundle.putString("projectId", projectId);
        bundle.putString("power", power);
        bundle.putString("source", source);
        bundle.putString("approveId", approveId);
        intent.putExtra(Params.FILEINFO, bundle);
        mContext.startActivity(intent);
//        mContext.overridePendingTransition(0, 0);
    }
}
