package com.android.frame.download;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.text.TextUtils;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;

import java.io.File;
import java.util.HashMap;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-12-1 11:35.
 */

public class ServiceUtils {

    private static final String TAG = "ServiceUtils";

    public static HcDownloadService mService = null;

    private static HashMap<Context, ServiceBinder> mConnectionMap = new HashMap<Context, ServiceBinder>();

    public static class ServiceToken {
        ContextWrapper mWrappedContext;
        ServiceToken(ContextWrapper context) {
            mWrappedContext = context;
        }
    }

    public static ServiceToken bindToService(Activity context) {
        return bindToService(context, null);
    }

    public static ServiceToken bindToService(Activity context, ServiceConnection callback) {
        Activity realActivity = context.getParent();
        if (realActivity == null) {
            realActivity = context;
        }
        ContextWrapper cw = new ContextWrapper(realActivity);
        Intent service = new Intent(cw, HcDownloadService.class);
        service.setPackage(cw.getPackageName());
        cw.startService(service);
        ServiceBinder sb = new ServiceBinder(callback);
        service = new Intent(cw, HcDownloadService.class); // 这里可以测试下用同一个Intent对象
        service.setPackage(cw.getPackageName());
        if (cw.bindService(service/*(new Intent()).setClass(cw, HcDownloadService.class)*/, sb, 0)) {
            mConnectionMap.put(cw, sb);
            return new ServiceToken(cw);
        }
        HcLog.D(TAG + "Failed to bind to service");
        return null;
    }

    public static void unbindFromService(ServiceToken token) {
        if (token == null) {
            HcLog.D(TAG + "Trying to unbind with null token");
            return;
        }
        ContextWrapper cw = token.mWrappedContext;
        ServiceBinder sb = mConnectionMap.remove(cw);
        if (sb == null) {
            HcLog.D(TAG + "Trying to unbind for unknown Context");
            return;
        }
        cw.unbindService(sb);
        if (mConnectionMap.isEmpty()) {
            // presumably there is nobody interested in the service at this point,
            // so don't hang on to the ServiceConnection
            mService = null;
        }
    }


    private static class ServiceBinder implements ServiceConnection {
        ServiceConnection mCallback;
        ServiceBinder(ServiceConnection callback) {
            mCallback = callback;
        }

        public void onServiceConnected(ComponentName className, android.os.IBinder service) {
            mService = ((HcDownloadService.MyBind) service).getMyService();
            if (mCallback != null) {
                mCallback.onServiceConnected(className, service);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(className);
            }
            mService = null;
        }
    }

    /**
     *
     * @param callback
     * @param source 回调的来源
     */
    public static void addServiceCallback(HcDownloadService.ServiceCallback callback, int source) {
        if (mService != null) {
            mService.setServiceCallback(callback, source);
        }
    }

    /**
     * 创建上传文件信息
     *
     * @param path 上传文件的本地路径
     * @param updir 保存到云盘上的根目录
     * @param md5 文件的md5值
     * @param source 文件的模块资源
     */
    public static FileColumn createFile(String path, String updir, String md5, int source) {
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
        if (!TextUtils.isEmpty(name)) {
            FileColumn fileColumn = new FileColumn();
            fileColumn.setName(name);
            fileColumn.setExt(ext);
            String fileid = HcUtil.getMD5String(name + ext + file.length() + path);
            fileColumn.setFileid(fileid);
            fileColumn.setPath(path);
            fileColumn.setMd5(md5);
            fileColumn.setPosition(0);
            fileColumn.setState("2");
            fileColumn.setFileSize(length + "");
            fileColumn.setUpdirid(updir);
            fileColumn.setUpOrDown(0);
            fileColumn.setUrl(DownloadUtil.BASE_URL);
            fileColumn.setLevel(4);
            fileColumn.setSource(source);
            //如果文件大小小于5M直接上传不需要分片，如果大于5M将文件分片
            if (DownloadUtil.FILE_CHUNK_SIZE >= length) {
                fileColumn.setAll_slice(-1);
                fileColumn.setSlice(1);
                fileColumn.setType("0");
            } else {
                int file_num = (int) ((length + DownloadUtil.FILE_CHUNK_SIZE - 1) / DownloadUtil.FILE_CHUNK_SIZE);
                fileColumn.setAll_slice(file_num);
                fileColumn.setSlice(1);
                fileColumn.setType("1");
            }
            return fileColumn;

        } else {
            return null;
        }
    }

    /**
     * 上传文件
     * @param file
     */
    public static void uploadFile(FileColumn file) {
        if (mService != null) {
            mService.upload(file);
        }
    }
}
