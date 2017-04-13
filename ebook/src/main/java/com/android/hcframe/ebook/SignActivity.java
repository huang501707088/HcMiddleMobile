package com.android.hcframe.ebook;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.frame.download.DownloadUtil;
import com.android.frame.download.FileColumn;
import com.android.frame.download.HcDownloadService;
import com.android.hcframe.HcApplication;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.ebook.entity.ConfigEntity;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.HttpRequestQueue;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.view.EditTextAlterDialog;
import com.android.hcframe.view.TwoBtnAlterDialog;
import com.kinggrid.iapppdf.ui.viewer.IAppPDFActivity;
import com.kinggrid.pdfservice.Annotation;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class SignActivity extends IAppPDFActivity implements OnClickListener, ServiceConnection, ServiceCallBack.TransferCallback {
    private static final String TAG = "SignActivity";
    private FrameLayout frameLayout;
    private String ACTION_SAVE = "com.kinggrid.iapppdf.broadcast.savesignfinish";// 保存完毕
    final static int TYPE_ANNOT_STAMP = 1;
    TopBarView topBarView;
    LinearLayout approve_id_ll_send, approve_id_ll_sign, approve_id_ll_approve, approve_id_ll_save;
    RelativeLayout approve_id_rl_bottom;
    String projectId;
    String power;//0只读，1可编辑
    String source;//0来源于详情,1来源于列表
    String approveId;
    String pageId;
    String fileId;
    String filePath;
    EditTextAlterDialog edittextDialog;
    boolean isSign = false;
    private Handler mHandler = new Handler();
    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case Params.MSG_OK:
                    finshActivity();
//                    SignActivity.this.finish();
                    break;
                case Params.MSG_ERROR:
                    Toast.makeText(SignActivity.this, "提交失败", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
        }

        ;
    };

    BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.contains(ACTION_SAVE)) {
                List<Annotation> annotList = new ArrayList<Annotation>();
                annotList = getAnnotationList(TYPE_ANNOT_STAMP);
                if (annotList.size() != 0) {
                    writeStringToTxt(annotationToJson(annotList));
                }
                if (Params.FROM_WEB.equals(source)) {
                    webSubmit();
                } else {
                    sendFile();
                }

//                submitFile("id", fileColumn.getFileid(), "signType", String.valueOf(MyApplication.signType));
            }

        }
    };
    private FileColumn fileColumn;
    private boolean isUseEbenSDK;

    @Override
    protected void onCreateImpl(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreateImpl(savedInstanceState);
        setContentView(R.layout.activity_sign);
        bindservice();
        topBarView = (TopBarView) findViewById(R.id.details_top_bar);
        approve_id_ll_send = (LinearLayout) findViewById(R.id.approve_id_ll_send);
        approve_id_ll_sign = (LinearLayout) findViewById(R.id.approve_id_ll_sign);
        approve_id_ll_approve = (LinearLayout) findViewById(R.id.approve_id_ll_approve);
        approve_id_ll_save = (LinearLayout) findViewById(R.id.approve_id_ll_save);
        approve_id_rl_bottom = (RelativeLayout) findViewById(R.id.approve_id_rl_bottom);
//        titleTxt = (TextView) findViewById(R.id.title);
//        signBtn = (TextView) findViewById(R.id.signBtn);
//        submitBtn = (TextView) findViewById(R.id.submitBtn);
        approve_id_ll_send.setOnClickListener(this);
        approve_id_ll_sign.setOnClickListener(this);
        approve_id_ll_approve.setOnClickListener(this);
        approve_id_ll_save.setOnClickListener(this);
        topBarView.setReturnViewListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isDocumentModified()) {
                    savePDFReadSettings();
//                    finish();
                    finshActivity();
                } else {
                    if (isUseEbenSDK) {
                        saveAllSignAndDocument();
                    } else {
                        saveDocument();
//                        finish();
                        finshActivity();
                    }
                }
            }
        });
//        findViewById(R.id.remarksBtn).setOnClickListener(this);
        Bundle bundle = getIntent().getExtras().getBundle(Params.FILEINFO);
        fileColumn = (FileColumn) bundle.getSerializable(Params.FILEINFO);
        projectId = bundle.getString("projectId");
        power = bundle.getString("power");
        source = bundle.getString("source");
        approveId = bundle.getString("approveId");
        if ("0".equals(power)) {
            approve_id_rl_bottom.setVisibility(View.GONE);
        }
        if ("1".equals(source)) {
            topBarView.setMenuBtnVisiable(View.VISIBLE);
            topBarView.setMenuSrc(R.drawable.approve_icon_detail);
            topBarView.setMenuListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra("projectId", projectId);
                    intent.setClassName(SignActivity.this, "com.android.hcframe.approve.ApproveDetailActivity");
                    startActivity(intent);
                    finish();
                }
            });
        }
        if (Params.FROM_WEB.equals(source)) {
            topBarView.setTitle("附件");
            fileId = bundle.getString("fileId");
            pageId = bundle.getString("pageId");
            filePath = bundle.getString("path");
        } else {
            topBarView.setTitle(fileColumn.getName());
        }

        isUseEbenSDK = getIntent().getBooleanExtra("isUseEbenSDK", false);

        frameLayout = (FrameLayout) findViewById(R.id.book_frame);
        initPDFView(frameLayout);

        IntentFilter save_filter = new IntentFilter(ACTION_SAVE);
        registerReceiver(receiver, save_filter);
        //普通模式签批模式下按钮的监听
        super.setOnKinggridSignListener(new OnKinggridSignListener() {
            @Override
            public void onSaveSign() {
                HcLog.D("###################################################onSaveSign");
                saveDocument();//保存文档接口。注意和eben模式接口不一样
            }

            @Override
            public void onCloseSign() {

            }

            @Override
            public void onClearSign() {

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

    @Override
    public void setOnKinggridSignListener(OnKinggridSignListener onKinggridSignListener) {
        // TODO Auto-generated method stub
        super.setOnKinggridSignListener(onKinggridSignListener);

    }

    @Override
    protected void onDestroyImpl(boolean finishing) {
        // TODO Auto-generated method stub
        super.onDestroyImpl(finishing);
        HttpRequestQueue.getInstance().cancelRequest(mListMD5Url);
        unregisterReceiver(receiver);
    }

    @Override
    protected void onPauseImpl(boolean finishing) {
        // TODO Auto-generated method stub
        super.onPauseImpl(finishing);
    }

    @Override
    protected void onPostCreateImpl(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onPostCreateImpl(savedInstanceState);
    }

    @Override
    protected void onResumeImpl() {
        // TODO Auto-generated method stub
        super.onResumeImpl();
    }

    @Override
    protected void onStartImpl() {
        // TODO Auto-generated method stub
        super.onStartImpl();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.approve_id_ll_approve) {
            isSign = true;
            openTextAnnotation();//文本备注接口

        } else if (i == R.id.approve_id_ll_sign) {
            isSign = true;
            if (isUseEbenSDK) {
                setEbenCanHandwrite(true);//打开Eben签批模式（为了和普通模式统一操作可以在初始化的调用setEbenCanHandwrite(false)关闭默认打开的签批图层）
            } else {
                openHandwriteAnnotation();//普通模式打开签批图层
            }

        } else if (i == R.id.approve_id_ll_send) {
            if (isUseEbenSDK) {
                saveAllSignAndDocument();//eben模式保存签批内容接口
            } else {
                saveDocument();
                List<Annotation> annotList = new ArrayList<Annotation>();
                annotList = getAnnotationList(TYPE_ANNOT_STAMP);
                if (annotList.size() != 0) {
                    writeStringToTxt(annotationToJson(annotList));
                }
                if (Params.FROM_WEB.equals(source)) {
                    webSubmit();
                } else {
                    sendFile();
                }
//                submitFile("id", fileColumn.getFileid(), "signType", String.valueOf(MyApplication.signType));
            }


        } else if (i == R.id.approve_id_ll_save) {
            if (isUseEbenSDK) {
                saveAllSignAndDocument();
            } else {
                saveDocument();
//                finish();
                finshActivity();
            }
        }

    }

//    /**
//     * 提交签批内容或者文件
//     *
//     * @param param
//     */
//    private void submitFile(final String... param) {
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                // TODO Auto-generated method stub
//                Map<String, File> files = new HashMap<String, File>();
//                File f = null;
//                //根据文件合成配置信息判断提交哪中文件
//                if (MyApplication.signType == ConfigEntity.SUBMIT_TXT) {
//                    f = new File(Environment.getExternalStorageDirectory() + "/test1.txt");
//                } else if (MyApplication.signType == ConfigEntity.SUBMIT_PDF) {
//                    f = new File(fileColumn.getPath());
//                }
//                files.put("file", f);
//
//                Map<String, Object> params = new HashMap<String, Object>();
//                for (int i = 0; i < param.length; ) {
//                    params.put(param[i], param[i + 1]);
//                    i = i + 2;
//                }
//
//                HcResponse postFile = HttpRequest.postFile(files,
//                        HttpRequest.BaseUrl + "article/submitarticle", params);
//                switch (postFile.getCode()) {
//                    case HcResponse.SUCCESS:
//                        handler.sendEmptyMessage(Params.MSG_OK);
//                        break;
//
//                    default:
//                        handler.sendEmptyMessage(Params.MSG_ERROR);
//
//                        break;
//                }
//            }
//        }).start();
//    }

    /**
     * 将签批的内容提取写到txt文件中
     *
     * @param fileString
     */
    @SuppressWarnings("deprecation")
    private void writeStringToTxt(String fileString) {
        if (MyApplication.signType == 1) {
            File file = new File(Environment.getExternalStorageDirectory() + "/test1.txt");
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(Environment.getExternalStorageDirectory() + "/test1.txt");
                outputStream.write(fileString.getBytes());
                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将数组转换为JSON格式的数据。
     *
     * @return JSON格式的数据
     */
    public String annotationToJson(List<Annotation> annotList) {
        try {
            JSONArray array = new JSONArray();
            JSONObject object = new JSONObject();
            int length = annotList.size();
            for (int i = 0; i < length; i++) {
                Annotation annot = annotList.get(i);

                if (annot.getAnnoContent().startsWith("q")) {
                    String authorId = annot.getAuthorId();
                    String authorName = annot.getAuthorName();
                    String pageNo = annot.getPageNo();
                    String X = annot.getX();
                    String Y = annot.getY();
                    String width = annot.getWidth();
                    String height = annot.getHeight();
                    String styleName = annot.getStyleName();
                    String createTime = annot.getCreateTime();
                    String annotContent = annot.getAnnoContent();
                    String unType = annot.getUnType();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("authorId", authorId);
                    jsonObject.put("authorName", authorName);
                    jsonObject.put("unType", unType);
                    jsonObject.put("styleId", "99");
                    jsonObject.put("styleName", "test");
                    // jsonObject.put("styleId", "12");
                    // jsonObject.put("styleName", styleName);
                    jsonObject.put("PageNo", pageNo);
                    jsonObject.put("X", X);
                    jsonObject.put("Y", Y);
                    jsonObject.put("Width", width);
                    jsonObject.put("Height", height);
                    jsonObject.put("createTime", createTime);
                    jsonObject.put("annotContent", annotContent);
                    // jsonObject.put("annotSignature", "");
                    array.put(jsonObject);
                } else {
                    String authorId = annot.getAuthorId();
                    String authorName = annot.getAuthorName();
                    String pageNo = annot.getPageNo();
                    String X = annot.getX();
                    String Y = annot.getY();
                    String width = annot.getWidth();
                    String height = annot.getHeight();
                    String styleName = annot.getStyleName();
                    String createTime = annot.getCreateTime();
                    String annotContent = annot.getAnnoContent();
                    String unType = annot.getUnType();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("authorId", authorId);
                    jsonObject.put("authorName", authorName);
                    jsonObject.put("unType", unType);
                    jsonObject.put("styleId", "99");
                    // jsonObject.put("styleId", "12");
                    jsonObject.put("styleName", styleName);
                    jsonObject.put("PageNo", pageNo);
                    jsonObject.put("X", X);
                    jsonObject.put("Y", Y);
                    jsonObject.put("Width", width);
                    jsonObject.put("Height", height);
                    jsonObject.put("createTime", createTime);
                    jsonObject.put("annotContent", annotContent);
                    jsonObject.put("annotSignature", bytesToHexString(getBytesFromFile(new File(annotContent))));
                    array.put(jsonObject);
                }
            }
            // object.put("annots", array);
            Log.d("Kevin", "array.toString() : " + array.toString());
            return array.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    private byte[] getBytesFromFile(File f) {
        if (f == null) {
            return null;
        }
        try {
            FileInputStream stream = new FileInputStream(f);
            ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = stream.read(b)) != -1)
                out.write(b, 0, n);
            stream.close();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
        }
        return null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (!isDocumentModified()) {
                savePDFReadSettings();
//                finish();
                finshActivity();
            } else {
                if (isUseEbenSDK) {
                    saveAllSignAndDocument();
                } else {
                    saveDocument();
//                    finish();
                    finshActivity();
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void sendFile() {
        String path = null;
        if (MyApplication.signType == ConfigEntity.SUBMIT_TXT) {
            path = Environment.getExternalStorageDirectory() + "/test1.txt";
        } else if (MyApplication.signType == ConfigEntity.SUBMIT_PDF) {
            path = fileColumn.getPath();
        }
        FileColumn fileInfo = getFileInfo(path, EbookUtil.NETDISK_DIRECTORY);
        if (fileInfo != null) {
            HcDialog.showProgressDialog(this, "文件上传中...");
            inSend = Parcel.obtain();
            inSend.writeSerializable(fileInfo);
            try {
                iBinder.transact(0, inSend, null, IBinder.FLAG_ONEWAY);
                inSend.recycle();
            } catch (RemoteException e) {
                e.printStackTrace();

            }
        } else {
            HcUtil.showToast(this, "上传失败");
        }

    }

    /**
     * 获取文件信息，并存入数据库
     *
     * @param path
     * @param updirId
     */
    public static FileColumn getFileInfo(String path, String updirId) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        if (file.length() == 0) {
            return null;
        }
        long length = file.length();
        String fileName = file.getName();
        int position = fileName.lastIndexOf(".");
        String name = fileName.substring(0, position);
        String ext = fileName.substring(position + 1, fileName.length());
        String MD5 = file != null ? EbookUtil.getFileMD5(file, length) : "";
        if (name != null && !"".equals(name)) {
            FileColumn fileColumn = new FileColumn();
            fileColumn.setName(name);
            fileColumn.setExt(ext);
            String fileid = HcUtil.getMD5String(name + ext + file.length() + path);
            fileColumn.setFileid(fileid);
            fileColumn.setPath(path);
//            String md5;
//            md5 = NetdiscUtil.getFileMD5(file, length);
            fileColumn.setMd5(MD5);
            fileColumn.setPosition(0);
            fileColumn.setState("0");
            fileColumn.setFileSize(length + "");
            fileColumn.setUpdirid(updirId);
            fileColumn.setUpOrDown(0);
            fileColumn.setUrl(HcConfig.getConfig().getServerName(HcConfig.Module.CLOUD_DISK) + ":" + HcConfig.getConfig().getServerPort(HcConfig.Module.CLOUD_DISK) + "/clouddiskM-webapp/api/clouddisk/");
            fileColumn.setLevel(1);
            fileColumn.setSource(HcDownloadService.PDF_SOURCE);
            //如果文件大小小于5M直接上传不需要分片，如果大于5M将文件分片
            if (DownloadUtil.FILE_CHUNK_SIZE >= length) {
                fileColumn.setAll_slice(-1);
                fileColumn.setSlice(1);
                fileColumn.setType("0");
            } else {
//                path = path + "/HcMobile/upload/" + HcUtil.getDateFile();
//                exist(path);
                int file_num = (int) (length / DownloadUtil.FILE_CHUNK_SIZE);
                fileColumn.setAll_slice(file_num);
                fileColumn.setSlice(1);
                fileColumn.setType("1");
            }
            return fileColumn;

        } else {
            return null;
        }
    }

    private void showDialog(final String url) {
        List<String> list = new ArrayList<String>();
        list.add("修改后需再批阅");
        list.add("修改后无需批阅");
        list.add("通过，无需修改");
        edittextDialog = EditTextAlterDialog.createDialog(SignActivity.this, list);
        edittextDialog.btn_ok.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String str = edittextDialog.ebook_id_et.getText().toString();
                if (str == null || "".equals(str)) {
                    str = "无备注信息";
                }
                submit(str, url);
                edittextDialog.dismiss();
            }
        });
        edittextDialog.btn_cancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                edittextDialog.dismiss();
            }
        });
        edittextDialog.show();
    }

    public static final String BASE_URL = "/terminalServer/szf/";
    private String mListMD5Url;

    private void submit(String str, String pdfFile) {
        ListRequest request = new ListRequest();
        String url = HcUtil.getScheme() + BASE_URL + "updateApprove";
        HttpPost share = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
        builder.addTextBody("approveId", approveId, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("approveMemo", str, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("pdfFile", pdfFile, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        share.setEntity(builder.build());
        HcDialog.showProgressDialog(SignActivity.this, "获取数据中");
        request.sendRequestCommand(url, share, RequestCategory.NONE, new CloudListResponse(), false);


    }

    public void webSubmit() {
        File newFile = null;
        if (MyApplication.signType == ConfigEntity.SUBMIT_TXT) {
            String path = Environment.getExternalStorageDirectory() + "/test1.txt";
            newFile = new File(path);
        } else if (MyApplication.signType == ConfigEntity.SUBMIT_PDF) {
            newFile = new File(filePath);
//            path = fileColumn.getPath();
        }
        ListRequest request = new ListRequest();
//        String url = HcUtil.getScheme() + BASE_URL + "uploadapprovalfile";
        String url = "http://10.80.6.61:8080" + BASE_URL + "uploadapprovalfile";
        HttpPost share = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
        builder.addTextBody("fileId", fileId, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("pageId", pageId, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));

        if (!newFile.exists()) {
            return;
        }
        builder.addBinaryBody("file", newFile);
        share.setEntity(builder.build());
        HcDialog.showProgressDialog(SignActivity.this, "获取数据中");
        request.sendRequestCommand(url, share, RequestCategory.NONE, new CloudListResponse(), false, 30);


    }

    private class ListRequest extends AbstractHttpRequest {

        private static final String TAG = SignActivity.TAG + "ListRequest";

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
        private static final String TAG = SignActivity.TAG + "$MySkydriveResponse";

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            HcDialog.deleteProgressDialog();
//            HcUtil.showToast(SignActivity.this, "提交成功");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    finshActivity();
                }
            });
        }


        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcDialog.deleteProgressDialog();
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
            mListMD5Url = md5Url;
        }

        @Override
        public void onConnectionTimeout(RequestCategory request) {
            super.onConnectionTimeout(request);
            HcDialog.deleteProgressDialog();
        }

        @Override
        public void onParseDataError(RequestCategory request) {
            super.onParseDataError(request);
            HcDialog.deleteProgressDialog();
        }

        @Override
        public void onRequestFailed(int code, String msg, RequestCategory request) {
            super.onRequestFailed(code, msg, request);
            HcDialog.deleteProgressDialog();
        }

        @Override
        public void unknown(RequestCategory request) {
            super.unknown(request);
            HcDialog.deleteProgressDialog();
        }

        @Override
        public void onResponseFailed(int code, RequestCategory request) {
            super.onResponseFailed(code, request);
            HcDialog.deleteProgressDialog();
        }

        @Override
        public void onRequestCanel(RequestCategory request) {
            super.onRequestCanel(request);
            HcDialog.deleteProgressDialog();
        }
    }

    private void finshActivity() {
        Intent mIntent = new Intent();
        mIntent.putExtra("isSign", isSign);
        // 设置结果，并进行传送
        SignActivity.this.setResult(Activity.RESULT_OK, mIntent);
        SignActivity.this.finish();
    }

    Parcel inSend;
    IBinder iBinder;
    HcDownloadService mService;

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
        if (fileColumn.getSuccess() == 1) {
            HcDialog.deleteProgressDialog();
//            fileColumn.setPath(fileColumn.getPath()+".pdf");
            showDialog(fileColumn.getUrl());
        } else {
            if (!"0".equals(fileColumn.getState())) {
                HcDialog.deleteProgressDialog();
                HcUtil.showToast(this, "上传失败");
            }
        }
    }

}
