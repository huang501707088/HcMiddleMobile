package com.android.hcframe.approve;

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
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.frame.download.FileColumn;
import com.android.frame.download.HcDownloadService;
import com.android.hcframe.HcApplication;
import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.ebook.Params;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.HttpRequestQueue;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.sql.SettingHelper;

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

/**
 * Created by pc on 2016/8/14.
 * 稿件明细
 */
public class ApproveDetailActivity extends HcBaseActivity implements ServiceConnection, ServiceCallBack.TransferCallback {
    private static final String TAG = "ApproveDetailActivity";
    private TopBarView mTopBarView;
    private ListView mDiscSearchLv;
    private Handler mHandler = new Handler();
    String projectId;
    ApproveDetailAdapter approveAdapter;
    List<JSONObject> projectList = new ArrayList<JSONObject>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.approve_detail);
        mTopBarView = (TopBarView) findViewById(R.id.details_top_bar);
        mTopBarView.setTitle("稿件明细");
        mDiscSearchLv = (ListView) findViewById(R.id.approve_lv);
        HcDialog.showProgressDialog(ApproveDetailActivity.this, "获取数据中");
        initDate();
        bindservice();
        // 绑定listView的监听器
        mDiscSearchLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View parent, int key,
                                    long arg3) {
                JSONObject jsonObject = (JSONObject) adapter.getAdapter().getItem(key);
                downPDF(jsonObject);
//                Intent intent = new Intent();
//                intent.putExtra("infoid", jsonObject.optString("infoid"));
//                intent.putExtra("infoname", jsonObject.optString("infoname"));
//                if (jsonObject.optString("userroleType") == null || "".equals(jsonObject.optString("userroleType"))) {
//                    intent.putExtra("userroleType", "1");
//                } else {
//                    intent.putExtra("userroleType", jsonObject.optString("userroleType"));
//                }
//                intent.setClass(WorkGroupActivity.this, WorkGroupDetailActivity.class);
//                startActivity(intent);
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

    private class ListRequest extends AbstractHttpRequest {

        private static final String TAG = ApproveDetailActivity.TAG + "ListRequest";

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
    public static final String BASE_URL = "/terminalServer/szf/";

    private void initDate() {
        projectId = getIntent().getStringExtra("projectId");
        ListRequest request = new ListRequest();
        String url = HcUtil.getScheme() + BASE_URL + "getApproveDetail";
        HttpPost share = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
        builder.addTextBody("projectId", projectId, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        share.setEntity(builder.build());
        request.sendRequestCommand(url, share, RequestCategory.NONE, new AbstractHttpResponse() {
            @Override
            public void onSuccess(Object data, RequestCategory request) {
                HcDialog.deleteProgressDialog();
                try {
                    JSONObject jsonObject = new JSONObject(data.toString());
                    JSONObject frist = new JSONObject();
                    frist.put("createDate", jsonObject.optString("createDate"));
                    frist.put("projectName", jsonObject.optString("projectName"));
                    frist.put("createUserName", jsonObject.optString("createUserName"));
                    projectList.add(frist);
                    JSONArray jsonArray = jsonObject.optJSONArray("approveList");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        projectList.add((JSONObject) jsonArray.get(i));
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (approveAdapter == null) {
                                // 实例化自定义的MySkydriveAdapter
                                approveAdapter = new ApproveDetailAdapter(ApproveDetailActivity.this, projectList);
                                mDiscSearchLv.setAdapter(approveAdapter);
                            } else {
                                approveAdapter.notifyDataSetChanged();
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
        }, false);
    }

    String power;
    String source;
    String approveId;

    public void downPDF(JSONObject jsonObject) {
//        projectId = jsonObject.optString("projectId");
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
        String fileList = ApproveUtil.getFileIDSP(this);
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
        fileColumn.setName(jsonObject.optString("pdfFileName") + ".pdf");
        fileColumn.setUrl(HcConfig.getConfig().getServerName(HcConfig.Module.CLOUD_DISK) + ":" + HcConfig.getConfig().getServerPort(HcConfig.Module.CLOUD_DISK) + "/clouddiskM-webapp/api/clouddisk/" + "downloadFile");
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
            HcDialog.showProgressDialog(this, "文件下载中...");
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
                ApproveUtil.setFileIDSP(this, fileColumn.getFileid());
                doOpenFile(file, fileColumn);
            } else {
                HcUtil.showToast(this, "下载失败");
            }
        } else {
            if (!"0".equals(fileColumn.getState())) {
                HcDialog.deleteProgressDialog();
                HcUtil.showToast(this, "下载失败");
            }
        }
    }

    private void doOpenFile(File file, FileColumn fileColumn) {
        final Uri uri = Uri.fromFile(file);
        final Intent intent = new Intent("android.intent.action.VIEW", uri);
        intent.setClassName(this, "com.android.hcframe.ebook.SignActivity");
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
        startActivity(intent);
//        mContext.overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        HttpRequestQueue.getInstance().cancelRequest("");
        unbindService(this);
        super.onDestroy();
    }
}