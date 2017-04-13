package com.android.hcframe.netdisc.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.android.frame.download.DownloadUtil;
import com.android.hcframe.HcApplication;
import com.android.hcframe.HcLog;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.netdisc.data.UploadFileInfo;
import com.android.hcframe.netdisc.util.NetdiscUtil;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-7-28 15:41.
 */
public class HcNetDiscService extends Service {

    private static final String TAG = "HcNetDiscService";

    /**
     * 1.判断上传列表中是否有超过5个在上传,进入等待状态
     * 2.没有超过5个,开始上传
     * 3.上传完,上传等待状态的下一个文件
     */
    public static final String DOWNLOAD = "com.android.hcframe.netdisc.service.start_download";
    public static final String UPLOAD = "com.android.hcframe.netdisc.service.start_upload";

    /**
     * 需要上传的文件列表,包括正在上传的
     *
     */
    private List<UploadFileInfo> mUploadList = new ArrayList<UploadFileInfo>();

    /**
     * 正在上传的文件列表
     */
    private List<UploadFileInfo> mUploadingList = new ArrayList<UploadFileInfo>();


    private static final int MAX_COUNT = 5;

    private Handler mHandler = new Handler();

    public static final String UPLOAD_KEY = "upload";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        HcLog.D(TAG + " #onStartCommand intent = " + intent);
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        String action = intent.getAction();
        if (action.equals(UPLOAD)) {
            ArrayList<UploadFileInfo> infos = intent.getParcelableArrayListExtra(UPLOAD_KEY);
            if (infos == null || infos.isEmpty()) {
                stopSelf();
                return START_NOT_STICKY;
            }
            if (mUploadList.isEmpty()) {
                // 去数据库获取数据
                HcLog.D(TAG + "#onStartCommand  添加前上传的列表为空, 需要添加的列表长度 infos size = " + infos.size());
                mUploadList.addAll(infos);
                mUploadingList.addAll(infos.size() > MAX_COUNT ? infos.subList(0, MAX_COUNT) : infos);

                HcLog.D(TAG + "#onStartCommand  添加后正在上传的列表长度 mUploadingList size = " + mUploadingList.size());
                for (UploadFileInfo info : mUploadingList) {
                    checkUploadFile(info);
                }

            } else {
                // 判断在下载的有多少
                HcLog.D(TAG + "#onStartCommand  添加前上传的列表不为空, 需要添加的列表长度 infos size = "+infos.size() + " 添加前列表的长度 mUploadList size = "+mUploadList.size());
                mUploadList.addAll(infos);
                int size = mUploadingList.size();
                HcLog.D(TAG + "#onStartCommand  添加前上传的列表不为空, 添加前正在上传的列表的长度 mUploadingList size= "+size);
                if (size < MAX_COUNT) {
                    mUploadingList.addAll(infos.size() > (MAX_COUNT - size) ? infos.subList(0, (MAX_COUNT - size)) : infos);
                }
                int temp = size;
                size = mUploadingList.size();

                HcLog.D(TAG + "#onStartCommand  添加前上传的列表不为空, 添加后正在上传的列表的长度 mUploadingList size= "+size);
                for (int i = temp; i < size; i++) {
                    checkUploadFile(mUploadingList.get(i));
                }

            }
        } else if (action.equals(DOWNLOAD)) {

        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private class CheckUploadFileRequest extends AbstractHttpRequest {

        private static final String URL = "uploadFile";

        @Override
        public String getParameterUrl() {
            return "";
        }

        @Override
        public String getRequestMethod() {
            return "uploadFile";
        }

    }

    private class CheckUploadFileResponse extends AbstractHttpResponse {

        private static final String TAG = HcNetDiscService.TAG + "@CheckUploadFileResponse";

        private UploadFileInfo mInfo;

        public CheckUploadFileResponse(UploadFileInfo info) {
            mInfo = info;
        }

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            try {
                JSONObject object = new JSONObject((String) data);
                boolean completed = "true".equals(object.getString("iffast"));
                int position = object.getInt("position");
                mInfo.setCurrent(position);
                if (!completed) {
                    if (mInfo.getTotle() == 1) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                uploadFile(mInfo, null);
                            }
                        });
                    } else {
                        // 创建文件
                        byte[] b = new byte[1024 * 32];
                        File file = new File(mInfo.getFilePath());
                        FileInputStream in = new FileInputStream(file);
                        final File chunkFile = new File(HcApplication.getAppDownloadPath(), mInfo.getFileName() + "_" +(mInfo.getCurrent() + 1));
                        FileOutputStream os = new FileOutputStream(chunkFile);
                        int offset = mInfo.getCurrent() * DownloadUtil.FILE_CHUNK_SIZE;
                        int len;
                        int write = 0;
                        in.skip(offset);
                        while ((len = in.read(b)) > 0) {
                            os.write(b, 0, len);
                            write += len;
                            if (write + b.length >= DownloadUtil.FILE_CHUNK_SIZE) {
                                len = in.read(b, 0, DownloadUtil.FILE_CHUNK_SIZE - write);
                                os.write(b, 0, len);
                                break;
                            } else if (write == DownloadUtil.FILE_CHUNK_SIZE) {
                                break;
                            }
                        }
                        os.flush();
                        os.close();
                        in.close();
                        HcLog.D(TAG + " #onSuccess file size = " + chunkFile.length());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                uploadFile(mInfo, chunkFile);
                            }
                        });
                    }

                } else {
                    // 更改文件上传状态,上传完成

                }
            } catch(Exception e) {
                HcLog.D(TAG + " #onSuccess Exception e ="+e);
            }
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {

        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

        }

        @Override
        public void onConnectionTimeout(RequestCategory request) {
            super.onConnectionTimeout(request);
        }

        @Override
        public void onNetworkInterrupt(RequestCategory request) {
            super.onNetworkInterrupt(request);
        }

        @Override
        public void onParseDataError(RequestCategory request) {
            super.onParseDataError(request);
        }

        @Override
        public void onRequestCanel(RequestCategory request) {
            super.onRequestCanel(request);
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

    private void checkUploadFile(UploadFileInfo info) {
        String url = NetdiscUtil.BASE_URL + CheckUploadFileRequest.URL;// + "?filekey = "+info.getFileKey();
        CheckUploadFileRequest request = new CheckUploadFileRequest();
        HttpPost upload = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
        builder.addTextBody("filesize", info.getFileSize());
        builder.addTextBody("filekey", info.getFileKey());
        builder.addTextBody("dirid", info.getFileDir());
        builder.addTextBody("status", "check");
        builder.addTextBody("name", info.getFileName(), ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("md5", info.getMd5());
        upload.setEntity(builder.build());
        HcLog.D(TAG + "#checkUploadFile info = "+info.getLogs());
        request.sendRequestCommand(info.getFileKey(), upload, RequestCategory.NONE, new CheckUploadFileResponse(info), false);
    }

    private class PostFileResponse extends AbstractHttpResponse {

        private static final String TAG = HcNetDiscService.TAG + "$PostFileResponse";

        private UploadFileInfo mInfo;

        public PostFileResponse(UploadFileInfo info) {
            mInfo = info;
        }

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            try {
                JSONObject object = new JSONObject((String) data);
                boolean completed = "true".equals(object.getString("finish"));
                if (!completed) {
                    // 创建文件
                    mInfo.setCurrent(mInfo.getCurrent() + 1);
                    byte[] b = new byte[1024 * 32];
                    File file = new File(mInfo.getFilePath());
                    FileInputStream in = new FileInputStream(file);
                    final File chunkFile = new File(HcApplication.getAppDownloadPath(), mInfo.getFileName() + "_" +(mInfo.getCurrent() + 1));
                    FileOutputStream os = new FileOutputStream(chunkFile);
                    int offset = mInfo.getCurrent() * DownloadUtil.FILE_CHUNK_SIZE;
                    int len;
                    int write = 0;
                    in.skip(offset);
                    while ((len = in.read(b)) > 0) {
                        os.write(b, 0, len);
                        write += len;
                        if (write + b.length >= DownloadUtil.FILE_CHUNK_SIZE) {
                            len = in.read(b, 0, DownloadUtil.FILE_CHUNK_SIZE - write);
                            os.write(b, 0, len);
                            break;
                        } else if (write == DownloadUtil.FILE_CHUNK_SIZE) {
                            break;
                        }
                    }
                    os.flush();
                    os.close();
                    in.close();
                    HcLog.D(TAG + " #onSuccess file size = "+chunkFile.length());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            uploadFile(mInfo, chunkFile);
                        }
                    });
                } else { // 上传成功！

                }
            } catch(Exception e) {

            }
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {

        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

        }

        @Override
        public void onConnectionTimeout(RequestCategory request) {
            super.onConnectionTimeout(request);
        }

        @Override
        public void onNetworkInterrupt(RequestCategory request) {
            super.onNetworkInterrupt(request);
        }

        @Override
        public void onParseDataError(RequestCategory request) {
            super.onParseDataError(request);
        }

        @Override
        public void onRequestCanel(RequestCategory request) {
            super.onRequestCanel(request);
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

    private void uploadFile(UploadFileInfo info, File file) {
        String url = NetdiscUtil.BASE_URL + CheckUploadFileRequest.URL;// + "?filekey = "+info.getFileKey();
        CheckUploadFileRequest request = new CheckUploadFileRequest();
        HttpPost upload = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
        builder.addTextBody("status", "chunk");
        builder.addTextBody("md5", info.getMd5());
        builder.addTextBody("filekey", info.getFileKey());
        builder.addTextBody("chunks", info.getTotle() == 1 ? "-1" : info.getTotle() + "");
        builder.addTextBody("chunk", info.getCurrent() + 1 + "");
        builder.addTextBody("dirid", info.getFileDir());
        builder.addTextBody("ext", info.getFileExt());
        builder.addTextBody("filesize", info.getFileSize());
        builder.addTextBody("name", info.getFileName(), ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        if (file == null) {
            try {
                file = new File(info.getFilePath());

            } catch(Exception e) {
                HcLog.D(TAG + "#uploadFile file is not exist!!! file path = "+info.getFilePath());
            }
        }
        if (file != null) {
            builder.addBinaryBody("file", file);
            upload.setEntity(builder.build());
            HcLog.D(TAG + "#checkUploadFile info = "+info.getLogs());
            request.sendRequestCommand(info.getFileKey(), upload, RequestCategory.NONE, new PostFileResponse(info), false, 30);
        }

    }
}
