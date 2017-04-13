/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-6-12 下午6:13:40
 */
package com.android.hcframe.sign;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.ebook.Params;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.push.HcAppState;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class DownloadSignPDFActivity extends HcBaseActivity implements IHttpResponse {
    private String url;
    private String filename = null;
    private File file;
    private Handler handler;
    private String mTitle;
    private String msg;
    private String fileId;
    private String pageId;
    private String method;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        HcLog.D("DownloadPDFActivity# " + HcLog.TAG_PDF + " it is onCreate!");
        Intent intent = getIntent();
//        url = intent.getExtras().getString("url");
//
        msg = intent.getExtras().getString("message");
        try {
            JSONObject jsonObject = new JSONObject(msg);
            JSONObject body = jsonObject.optJSONObject("body");
            url = "http://10.80.6.61:8080/terminalServer" + body.optString("url");
            mTitle = "附件";
            fileId = body.optString("fileId");
            pageId = body.optString("pageId");
            method = body.optString("method");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(url)) {
            HcUtil.showToast(this, "文件下载失败！");
            finishActivity();
            return;
        }
        HcLog.D(" DownloadPDFActivity onCreate url = " + url);
        filename = HcUtil.getMD5String(url);
        file = new File(HcApplication.getPdfDir(), filename + ".pdf");
        // 判断本地是否有pdf
        if (file.exists()) {
            startPDFActicity(file);
        } else {
            HcLog.Sysout("5");
            HcDialog.showProgressDialog(this, "正在下载...");
            // 发送pdf下载命令
            if (HcUtil.CHANDED) {
                url = HcUtil.mappedUrl(url);
            }
            HcHttpRequest.getRequest().sendDownPdfCommand(url, this);
        }
        handler = new Handler();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        return false;
    }

    private void startPDFActicity(File file) {
        Uri uri = Uri.fromFile(file);
        final Intent intent = new Intent("android.intent.action.VIEW", uri);
        intent.setClassName(this, "com.android.hcframe.ebook.SignActivity");
        intent.putExtra("copyRight", Params.COPYRIGHT);
        intent.putExtra("isVectorSign", true); // 是否矢量
        intent.putExtra("saveVector", true); // 是否保存矢量
        intent.putExtra("isUseEbenSDK", false);// 是否使用E人E本模式
        Bundle bundle = new Bundle();
//        bundle.putSerializable(Params.FILEINFO, fileColumn);// 文件信息
        bundle.putString("projectId", "");
        bundle.putString("power", Params.EDIT);
        bundle.putString("source", Params.FROM_WEB);
        bundle.putString("approveId", "");
        bundle.putString("fileId", fileId);
        bundle.putString("pageId", pageId);
        bundle.putString("path", file.getAbsolutePath());
        intent.putExtra(Params.FILEINFO, bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        startActivityForResult(intent, -1);
        finish();
    }

    @Override
    public void notify(Object data, RequestCategory request,
                       ResponseCategory category) {
        // TODO Auto-generated method stub
        if (request != null && request == RequestCategory.DOWNLOAD_PDF) {
            if (data != null && data instanceof InputStream) {
                InputStream stream = (InputStream) data;
                String filename = createPdf(this.filename, stream);
                HcDialog.deleteProgressDialog();
                if (!HcUtil.isEmpty(filename)) {
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            // 打开pdf文件
                            startPDFActicity(file);
                        }
                    });

                } else {
                    HcUtil.showToast(this, "文件下载失败！");
                    finishActivity();
                }
            } else {
                HcDialog.deleteProgressDialog();
                HcUtil.showToast(this, "文件下载失败！");
                finishActivity();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private String createPdf(String pdf, InputStream is) {
        if (is != null) {
            OutputStream outputStream = null;
            try {
                String directory = HcApplication.getPdfDir().getAbsolutePath();
                File dir = new File(directory);
                if (!dir.exists()) dir.mkdirs();
                File file = new File(directory, pdf + ".pdf");
                outputStream = new FileOutputStream(file);
                byte[] b = new byte[1024];
                int len;
                while ((len = is.read(b)) > 0) {
                    HcLog.D(" DownloadPDFActivity createPdf len = " + len);
                    outputStream.write(b, 0, len);
                }
                return file.getAbsolutePath();
            } catch (Exception e) {
                // TODO: handle exception
                return null;
            } finally {
                try {
                    if (outputStream != null)
                        outputStream.close();
                    is.close();
                    is = null;
                } catch (Exception e2) {
                    // TODO: handle exception
                }
            }
        }

        return null;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        HcLog.D("DownloadPDFActivity# " + HcLog.TAG_PDF + " it is onResume!");
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        HcLog.D("DownloadPDFActivity# " + HcLog.TAG_PDF + " it is onStop!");
    }

    @Override
    public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
        // TODO Auto-generated method stub

    }

    private void finishActivity() {
        HcAppState.getInstance().removeActivity(this);
        finish();
    }
}
