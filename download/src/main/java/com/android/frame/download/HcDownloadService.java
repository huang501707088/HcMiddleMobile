package com.android.frame.download;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.util.Log;
import android.widget.Toast;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.sql.HcDatabase;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by ncll on 2016/10/9.
 */
public class HcDownloadService extends Service implements DownloadTask.DownloadCallback {
    private static final String TAG = "HcDownloadService";
    public static final int NETDISC_SOURCE = 1;
    public static final int PDF_SOURCE = 2;
    public static final int IM_SOURCE = 4;
    //上传下载到本地的路径
    public static final String DWONLOAD_PATH = HcApplication.getAppDownloadPath() + "/netdisc/downloads";
    public static final String UPLOAD_PATH = HcApplication.getAppDownloadPath() + "/netdisc/upload";
    ArrayList<FileColumn> up_fileList_wait = new ArrayList<FileColumn>();//上传等待列表
    ArrayList<FileColumn> up_fileList_ing = new ArrayList<FileColumn>();//上传进行列表
    ArrayList<FileColumn> up_fileList_stop = new ArrayList<FileColumn>();//暂停列表
    ArrayList<FileColumn> down_fileList_wait = new ArrayList<FileColumn>();//下载等待列表
    ArrayList<FileColumn> down_fileList_ing = new ArrayList<FileColumn>();//下载进行列表
    public static final int THREAD_MAX_SIZE = 5; //最大下载上传个数
    // 下载任务的集合
    Map<Integer, DownloadTask> mTasks = new LinkedHashMap<Integer, DownloadTask>();
    InitThread mInitThread;
    public static final int MSG_INIT = 0;
    public static final int MSG_UP = 1;
    private Map<Integer, ServiceCallback> map;

    @Override
    public void onCreate() {
        DownloadTask.setDownloadCallback(this);
        HcLog.D("HcDownloadService onCreate");
        map = new HashMap<Integer, ServiceCallback>();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        HcLog.D("HcDownloadService onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void downloadCallBack(FileColumn fileColumn) {
        handler.obtainMessage(MSG_UP, fileColumn).sendToTarget();
        if ("1".equals(fileColumn.getState()) || fileColumn.getSuccess() == 1) {
            Iterator<FileColumn> iterator = down_fileList_ing.iterator();
            while (iterator.hasNext()) {
                FileColumn fileColumn1 = iterator.next();
                if (fileColumn.getFileid().equals(fileColumn1.getFileid())) {//如果存正在上传的文件，否则是等待文件
                    iterator.remove();
                    break;
                }
            }
            if (down_fileList_ing.size() < THREAD_MAX_SIZE) {
                Iterator<FileColumn> itera = down_fileList_wait.iterator();
                while (itera.hasNext()) {
                    FileColumn fileColumn1 = itera.next();
                    itera.remove();
                    startDownload(fileColumn1);
                    break;
                }
            }
        }
    }

    public interface ServiceCallback {
        void serviceCallback(FileColumn fileColumn);
    }


    public void setServiceCallback(ServiceCallback callback, int source) {
        HcLog.D("setServiceCallback####map.size()" + map.size());
        map.put(source, callback);
        Iterator<Map.Entry<Integer, ServiceCallback>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, ServiceCallback> entry = it.next();
            HcLog.D("setServiceCallback###key= " + entry.getKey() + " and value= " + entry.getValue());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBind;
    }

    private MyBind mBind = new MyBind();

    public class MyBind extends Binder {
        public HcDownloadService getMyService() {
            return HcDownloadService.this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
            FileColumn fileColumn = (FileColumn) data.readSerializable();
            int upOrDown = fileColumn.getUpOrDown();
            if (upOrDown == 0) {//上传
                upload(fileColumn);
            } else if (upOrDown == 1) {//下载
                download(fileColumn);
            }
            return false;
        }
    }

    /**
     * 上传入口
     *
     * @param fileColumn
     */
    public void upload(FileColumn fileColumn) {
        String state = fileColumn.getState();//（0：正在上传，1：暂停，2等待，3失败）
        if ("0".equals(state) || "2".equals(state)) {
            startUpload(fileColumn);
        } else if ("1".equals(state) || "3".equals(state)) {
            stopUpload(fileColumn);
        }
    }

    /**
     * 停止上传
     *
     * @param fileColumn
     */
    private void stopUpload(FileColumn fileColumn) {
        Iterator<FileColumn> iterator = up_fileList_ing.iterator();
        boolean uploading = false;
        while (iterator.hasNext()) {
            FileColumn fileColumn1 = iterator.next();
            if (fileColumn.getUpdirid().equals(fileColumn1.getUpdirid())
                    && fileColumn.getFileid().equals(fileColumn1.getFileid())) {//如果存正在上传的文件，否则是等待文件
                uploading = true;
                break;
            }
        }
        if (uploading) {
            up_fileList_stop.add(fileColumn);
        } else {
            Iterator<FileColumn> iter = up_fileList_wait.iterator();
            while (iterator.hasNext()) {
                FileColumn fileColumn1 = iter.next();
                if (fileColumn.getUpdirid().equals(fileColumn1.getUpdirid())
                        && fileColumn.getFileid().equals(fileColumn1.getFileid())) {//等待队列直接删除即可
                    iter.remove();
                    break;
                }
            }
        }
    }

    /**
     * 开始上传
     *
     * @param fileColumn
     */
    private void startUpload(FileColumn fileColumn) {
        int level = fileColumn.getLevel();
        switch (level) {
            case 1:
                if (up_fileList_ing.size() >= THREAD_MAX_SIZE) {
                    up_fileList_wait.add(fileColumn);
                } else {
                    up_fileList_ing.add(fileColumn);
                    httpRequestCheck(fileColumn);
                }
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                up_fileList_ing.add(fileColumn);
                httpRequestCheck(fileColumn);
                break;
            default:
                break;
        }
    }

    /**
     * 下载入口
     *
     * @param fileColumn
     */
    private void download(FileColumn fileColumn) {
        String state = fileColumn.getState();//下载（0：下载，1，暂停，2等待,3失败）
        if ("0".equals(state) || "2".equals(state)) {
            startDownload(fileColumn);
        } else if ("1".equals(state) || "3".equals(state)) {
            stopDownload(fileColumn);
        }
    }

    /**
     * 开始下载
     *
     * @param fileColumn
     */
    private void startDownload(FileColumn fileColumn) {
        int level = fileColumn.getLevel();
        switch (level) {
            case 1:
                if (down_fileList_ing.size() >= THREAD_MAX_SIZE) {
                    down_fileList_wait.add(fileColumn);
                } else {
                    down_fileList_ing.add(fileColumn);
                    mInitThread = new InitThread(fileColumn);
                    DownloadTask.sExecutorService.execute(mInitThread);
                }
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                down_fileList_ing.add(fileColumn);
                mInitThread = new InitThread(fileColumn);
                DownloadTask.sExecutorService.execute(mInitThread);
                break;
            default:
                break;
        }
    }

    /**
     * 停止下载
     *
     * @param fileColumn
     */
    private void stopDownload(FileColumn fileColumn) {
        DownloadTask task = mTasks.get(Integer.parseInt(fileColumn.getFileid()));
        if (task != null) {//下载线程有这个文件，那么就是正在下载中的文件否则是等待中的文件
            // 停止下载任务
            task.isPause = true;
            Iterator<FileColumn> iterator = down_fileList_ing.iterator();
            while (iterator.hasNext()) {
                FileColumn fileColumn1 = iterator.next();
                if (fileColumn1.getFileid().equals(fileColumn.getFileid())) {
                    iterator.remove();

                }
            }

        } else {
            Iterator<FileColumn> itera = down_fileList_wait.iterator();
            while (itera.hasNext()) {
                FileColumn fileColumn1 = itera.next();
                if (fileColumn.getFileid().equals(fileColumn1.getFileid())) {
                    itera.remove();
                }
            }
        }
    }

    private void httpRequestCheck(final FileColumn uploadColumn) {
        HcLog.D(TAG + "第一次检测上传文件httpRequestCheck~~~~~~~" + uploadColumn.getName());
//        String url = NetdiscUtil.BASE_URL + CheckUploadFileRequest.URL;
        CheckUploadFileRequest request = new CheckUploadFileRequest();
        HttpPost upload = new HttpPost(uploadColumn.getUrl() + CheckUploadFileRequest.URL);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
        builder.addTextBody("filesize", uploadColumn.getFileSize());
        builder.addTextBody("filekey", uploadColumn.getFileid());
        builder.addTextBody("dirid", uploadColumn.getUpdirid() == null ? "R" : uploadColumn.getUpdirid());
        builder.addTextBody("status", "check");
        builder.addTextBody("name", uploadColumn.getName(), ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("md5", uploadColumn.getMd5());
        upload.setEntity(builder.build());
        HcLog.D(TAG + "#checkUploadFile info = " + uploadColumn.getPath());
        boolean opened = HcUtil.getSharePreference(HcApplication.getContext());//true开
        if (opened && !isWifiConnected(HcApplication.getContext()) && uploadColumn.getSource() == HcDownloadService.NETDISC_SOURCE) {
            Handler handler = new Handler(Looper.getMainLooper());
            System.out.println("service started");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "请在WIFI下上传文件", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            request.sendRequestCommand(uploadColumn.getUrl() + CheckUploadFileRequest.URL + "?name=" + uploadColumn.getFileid(),
                    upload, RequestCategory.NONE,
                    new CheckUploadFileResponse(uploadColumn),
                    false);
        }

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

        private static final String TAG = HcDownloadService.TAG + "@CheckUploadFileResponse";

        private FileColumn mInfo;

        public CheckUploadFileResponse(FileColumn info) {
            mInfo = info;
        }

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + "#onSuccess  检测成功 file name = " +mInfo.getName() + " data = "  + data.toString());
            if (data != null) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(data.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String isfast = jsonObject.optString("iffast");
                if ("false".equals(isfast)) {
                    if (mInfo.getSource() == HcDownloadService.NETDISC_SOURCE) {
                        if (mInfo.getAll_slice() == -1) {
                            mInfo.setState("0");
                            mInfo.setSpeed((int) (50 * Math.random() + 50));
                            handler.obtainMessage(MSG_UP, mInfo).sendToTarget();
                        } else {
                            mInfo.setState("0");
                            mInfo.setSpeed((int) (50 * Math.random() + 50));
                            handler.obtainMessage(MSG_UP, mInfo).sendToTarget();
                        }
                    }

                    String position = jsonObject.optString("position");
                    int slice = Integer.decode(position) + 1;
                    mInfo.setSlice(slice);
                    sendFile(mInfo);
                } else {
                    up_fileList_ing.remove(mInfo);
                    if (mInfo.getSource() == HcDownloadService.NETDISC_SOURCE) {//网盘秒传成功后发送广播
                        mInfo.setSuccess(1);
                        handler.obtainMessage(MSG_UP, mInfo).sendToTarget();
                    } else/* if (mInfo.getSource() == HcDownloadService.IM_SOURCE)*/ {//其他功能秒传成功后发送广播通知
                        mInfo.setSuccess(1);
                        String infoid = jsonObject.optString("infoid");
                        mInfo.setUrl(infoid);
                        handler.obtainMessage(MSG_UP, mInfo).sendToTarget();
                    }
                    uploadQueue();
                }
            }
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            uploadFail(mInfo);
            HcLog.D(TAG + "#onAccountExcluded file name = " + mInfo.getName() + " data = " + data);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

        }

        @Override
        public void onConnectionTimeout(RequestCategory request) {
            uploadFail(mInfo);
            HcLog.D(TAG + "#onConnectionTimeout file name = " + mInfo.getName());
//            super.onConnectionTimeout(request);
        }

        @Override
        public void onNetworkInterrupt(RequestCategory request) {
            uploadFail(mInfo);
            HcLog.D(TAG + "#onNetworkInterrupt file name = " + mInfo.getName());
//            super.onNetworkInterrupt(request);
        }

        @Override
        public void onParseDataError(RequestCategory request) {
            uploadFail(mInfo);
            HcLog.D(TAG + "#onParseDataError file name = " + mInfo.getName());
//            super.onParseDataError(request);
        }

        @Override
        public void onRequestCanel(RequestCategory request) {
            uploadFail(mInfo);
            HcLog.D(TAG + "#onRequestCanel file name = " + mInfo.getName());
//            super.onRequestCanel(request);
        }

        @Override
        public void onRequestFailed(int code, String msg, RequestCategory request) {
            uploadFail(mInfo);
            HcLog.D(TAG + "#onRequestFailed file name = " + mInfo.getName() + " code = " + code + " msg = " + msg);
//            super.onRequestFailed(code, msg, request);
        }

        @Override
        public void onResponseFailed(int code, RequestCategory request) {
            uploadFail(mInfo);
            HcLog.D(TAG + "#onResponseFailed file name = " + mInfo.getName() + " code = " + code);
//            super.onResponseFailed(code, request);
        }

        @Override
        public void unknown(RequestCategory request) {
            uploadFail(mInfo);
            HcLog.D(TAG + "#unknown#######onSuccess file name = " + mInfo.getName());
//            super.unknown(request);
        }
    }

    private void httpRequestTask(final FileColumn uploadColumn, final File file) {
        HcLog.D(TAG + "#httpRequestTask 准备上传文件 file name = " + uploadColumn.getName());
//        String url = NetdiscUtil.BASE_URL + CheckUploadFileRequest.URL;// + "?filekey = "+info.getFileKey();
        CheckUploadFileRequest request = new CheckUploadFileRequest();
        HttpPost upload = new HttpPost(uploadColumn.getUrl() + CheckUploadFileRequest.URL);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
//        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addTextBody("filesize", uploadColumn.getFileSize());
        builder.addTextBody("filekey", uploadColumn.getFileid());
        builder.addTextBody("dirid", uploadColumn.getUpdirid());
        builder.addTextBody("status", "chunk");
        builder.addTextBody("chunks", uploadColumn.getAll_slice() + "");
        builder.addTextBody("chunk", uploadColumn.getSlice() + "");
        builder.addTextBody("name", uploadColumn.getName(), ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("ext", uploadColumn.getExt());
        builder.addTextBody("md5", uploadColumn.getMd5());
        builder.addBinaryBody("file", file, ContentType.create(HTTP.OCTET_STREAM_TYPE, HTTP.UTF_8), uploadColumn.getName());
        upload.setEntity(builder.build());
        HcLog.D(TAG + "#httpRequestTask info = " + uploadColumn.getPath());
        boolean opened = HcUtil.getSharePreference(HcApplication.getContext());//true开
        if (opened && !isWifiConnected(HcApplication.getContext()) && uploadColumn.getSource() == HcDownloadService.NETDISC_SOURCE) {
            Handler handler = new Handler(Looper.getMainLooper());
            System.out.println("service started");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "请在WIFI下上传文件", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            request.sendRequestCommand(uploadColumn.getUrl()
                            + "?name="
                            + uploadColumn.getFileid(),
                    upload,
                    RequestCategory.NONE,
                    new PostFileResponse(uploadColumn), false, 30);
        }
    }

    private class PostFileResponse extends AbstractHttpResponse {

        private static final String TAG = HcDownloadService.TAG + "$PostFileResponse";

        private FileColumn uploadColumn;

        public PostFileResponse(FileColumn info) {
            uploadColumn = info;
        }

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + "#onSuccess 上传文件成功 file name = " + uploadColumn.getName() + " data = " + data.toString());
            if (data != null) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(data.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String finish = jsonObject.optString("finish");
                if ("false".equals(finish)) {
                    boolean stop = false;
                    if (up_fileList_stop != null && up_fileList_stop.size() > 0) {
                        for (int i = 0; i < up_fileList_stop.size(); i++) {
                            if (up_fileList_stop.get(i).getFileid().equals(uploadColumn.getFileid())
                                    && up_fileList_stop.get(i).getUpdirid().equals(uploadColumn.getUpdirid())) {//暂停
                                up_fileList_stop.remove(i);
                                stop = true;
                                break;
                            }
                        }
                    }
                    if (stop) {
                        uploadColumn.setState("1");
                        long p = uploadColumn.getSlice() * DownloadUtil.FILE_CHUNK_SIZE;
                        uploadColumn.setPosition(p);
                        if (uploadColumn.getSource() == HcDownloadService.NETDISC_SOURCE) {

                            uploadColumn.setSpeed(0);
                            handler.obtainMessage(MSG_UP, uploadColumn).sendToTarget();
                        }
                        up_fileList_ing.remove(uploadColumn);
                        uploadQueue();
                    } else {
                        int slice = uploadColumn.getSlice();
                        uploadColumn.setSlice(slice + 1);
                        uploadColumn.setSpeed((int) (50 * Math.random() + 50));
                        handler.obtainMessage(MSG_UP, uploadColumn).sendToTarget();
                        sendFile(uploadColumn);
                    }

                } else {
                    up_fileList_ing.remove(uploadColumn);
                    if (uploadColumn.getSource() == HcDownloadService.NETDISC_SOURCE) {//网盘秒传成功后发送广播
                        uploadColumn.setSuccess(1);
                        handler.obtainMessage(MSG_UP, uploadColumn).sendToTarget();
                    } else/* if (uploadColumn.getSource() == HcDownloadService.IM_SOURCE)*/ {//其他功能秒传成功后发送广播通知
                        uploadColumn.setSuccess(1);
                        String infoid = jsonObject.optString("infoid");
                        uploadColumn.setUrl(infoid);
                        handler.obtainMessage(MSG_UP, uploadColumn).sendToTarget();
                    }
                    uploadQueue();
                }
            }
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            uploadFail(uploadColumn);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

        }

        @Override
        public void onConnectionTimeout(RequestCategory request) {
            uploadFail(uploadColumn);
//            super.onConnectionTimeout(request);
        }

        @Override
        public void onNetworkInterrupt(RequestCategory request) {
            uploadFail(uploadColumn);
//            super.onNetworkInterrupt(request);
        }

        @Override
        public void onParseDataError(RequestCategory request) {
            uploadFail(uploadColumn);
//            super.onParseDataError(request);
        }

        @Override
        public void onRequestCanel(RequestCategory request) {
            uploadFail(uploadColumn);
//            super.onRequestCanel(request);
        }

        @Override
        public void onRequestFailed(int code, String msg, RequestCategory request) {
            HcLog.D(TAG + " #onRequestFailed code = "+code);
            uploadFail(uploadColumn);
//            super.onRequestFailed(code, msg, request);

        }

        @Override
        public void onResponseFailed(int code, RequestCategory request) {
            HcLog.D(TAG + " #onRequestFailed code = "+code);
            uploadFail(uploadColumn);
//            super.onResponseFailed(code, request);

        }

        @Override
        public void unknown(RequestCategory request) {
            uploadFail(uploadColumn);
//            super.unknown(request);
        }

    }

    /**
     * 上传失败
     *
     * @param fileColumn
     */
    public void uploadFail(FileColumn fileColumn) {
        fileColumn.setState("3");
        if (fileColumn.getAll_slice() == -1) {
            fileColumn.setPosition(Long.decode(fileColumn.getFileSize()) / 2);
        } else {
            long p = fileColumn.getSlice() * DownloadUtil.FILE_CHUNK_SIZE;
            fileColumn.setPosition(p);
        }
        up_fileList_ing.remove(fileColumn);
//        if (fileColumn.getSource() == HcDownloadService.NETDISC_SOURCE) {
//            handler.obtainMessage(MSG_UP, fileColumn).sendToTarget();
//        }
        handler.obtainMessage(MSG_UP, fileColumn).sendToTarget();
        uploadQueue();
    }

    /**
     * 上传队列,将等待队列的文件放入进行队列中
     */
    private synchronized void uploadQueue() {
        up_fileList_wait = sortList(up_fileList_wait);
        if (up_fileList_ing.size() < THREAD_MAX_SIZE) {
            Iterator<FileColumn> itera = up_fileList_wait.iterator();
            while (itera.hasNext()) {
                FileColumn fileColumn1 = itera.next();
                itera.remove();
                startUpload(fileColumn1);
                break;
            }
        }
    }

    /**
     * 文件发送
     *
     * @param uploadColumn
     */
    private synchronized void sendFile(FileColumn uploadColumn) {
        if (uploadColumn != null) {
            InputStream inputStream = null;
            File file = null;
            //type为0不需要分片
            if ("0".equals(uploadColumn.getType())) {
                file = new File(uploadColumn.getPath());
            } else if ("1".equals(uploadColumn.getType())) {//type为1需要分片
                try {
                    long p = (uploadColumn.getSlice() - 1) * DownloadUtil.FILE_CHUNK_SIZE;
                    uploadColumn.setPosition(p);
                    FileAccessI fileAccessI = new FileAccessI(uploadColumn.getPath(), uploadColumn.getPosition());
                    inputStream = fileAccessI.read();
                    long position = fileAccessI.getPosition();
                    position = uploadColumn.getPosition() + position;
                    uploadColumn.setPosition(position);
                    file = inputstreamtofile(inputStream, UPLOAD_PATH);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            httpRequestTask(uploadColumn, file);
        }
    }

    public File inputstreamtofile(InputStream ins, String path) {
        exist(path);
        File file = new File(path + "/1");
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[1024 * 5];
            while ((bytesRead = ins.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 判断文件夹是否存在
     *
     * @param path 文件夹路劲
     * @return
     */
    public void exist(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * WIFI是否连接
     *
     * @param context
     * @return
     */
    public boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            return true;
        }

        return false;
    }

    /**
     * List正序
     *
     * @param origin
     * @return
     */
    public static ArrayList<FileColumn> sortList(ArrayList<FileColumn> origin) {
        if (origin == null || origin.size() == 0) {
            return origin;
        }
        Collections.sort(origin, new Comparator<FileColumn>() {
            @Override
            public int compare(FileColumn lhs, FileColumn rhs) {
                int p1 = 0;
                int p2 = 0;

                if (lhs != null) {
                    p1 = lhs.getLevel();
                }

                if (rhs != null) {
                    p2 = lhs.getLevel();
                }
                // LogUtil.i("p1: " + p1 + " p2: " + p2);
                if (p1 > p2) {
                    return 1; // p1 排在p2前面
                } else {
                    return -1; // p1排在p2后面
                }
            }

        });
        return origin;
    }
//-------------------------------------下载------------------------------------------

    /**
     * 初始化的子线程
     */
    class InitThread extends Thread {
        private FileColumn ifileInfo = null;

        public InitThread(FileColumn fileInfo) {
            ifileInfo = fileInfo;
        }

        public void run() {
            HttpURLConnection connection = null;
            RandomAccessFile raf = null;// 输出流
            try {

                // 链接网络文件
                java.net.URL url = new URL(ifileInfo.getUrl() + "?RANGE=" + ifileInfo.getPosition()
                        + "&infoid=" + ifileInfo.getFileid()
                        + "&type=F");
//                java.net.URL url = new URL("http://10.80.6.124:8080/terminalServer//file/getfile?fileid=5&type=common&clientId=13&date=1478506781376");
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Accept-Encoding", "identity");
                int lenght = -1;
                int c = 0;
                if (connection.getResponseCode() == HttpStatus.SC_OK) {// 判断是否链接成功
                    // 本地创建文件，并设置文件长度
                    File dir = new File(DWONLOAD_PATH);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File file = new File(dir, ifileInfo.getName());
                    raf = new RandomAccessFile(file, "rwd");// mode代表文件权限，r--Read---读取权限，w--Write---写入权限，d--Delete---删除权限
                    raf.setLength(Long.decode(ifileInfo.getFileSize()));
                    handler.obtainMessage(MSG_INIT, ifileInfo).sendToTarget();
                    // 获得文件长度
                } else {
                    ifileInfo.setState("3");
//                    if (ifileInfo.getSource() == HcDownloadService.NETDISC_SOURCE) {
                        handler.obtainMessage(MSG_UP, ifileInfo).sendToTarget();
//                    }

                }
            } catch (Exception e) {
                ifileInfo.setState("3");
//                if (ifileInfo.getSource() == HcDownloadService.NETDISC_SOURCE) {
                    handler.obtainMessage(MSG_UP, ifileInfo).sendToTarget();
//                }
                e.printStackTrace();
            } finally {
                try {
                    connection.disconnect();
                    raf.close();
                } catch (Exception e) {
                    ifileInfo.setState("3");
//                    if (ifileInfo.getSource() == HcDownloadService.NETDISC_SOURCE) {
                        handler.obtainMessage(MSG_UP, ifileInfo).sendToTarget();
//                    }
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    Handler handler = new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case MSG_INIT:
                    FileColumn fileInfo = (FileColumn) message.obj;
                    Log.i("LOGCAT", "Init:" + fileInfo.toString());
                    DownloadTask task = new DownloadTask(fileInfo,
                            HcDownloadService.this);
                    task.download();
                    mTasks.put(Integer.parseInt(fileInfo.getFileid()), task);
                    // 把下载任务添加到集合中
                    break;
                case MSG_UP:
                    FileColumn fileColumn = (FileColumn) message.obj;
                    HcLog.D("HcDownloadService####MSG_UP  fileColumn " + fileColumn.getSource());
                    map.get(fileColumn.getSource()).serviceCallback(fileColumn);
//                    mCallback.serviceCallback(fileColumn);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        HcLog.D("HcDownloadService OnDestroy");
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        HcLog.D("HcDownloadService onUnbind");
        return super.onUnbind(intent);
    }
}
