package com.android.hcframe.ebook;

import com.android.frame.download.FileColumn;
import com.android.frame.download.HcDownloadService;
import com.android.hcframe.HcLog;

/**
 * Created by ncll on 2016/10/13.
 */
public class ServiceCallBack implements HcDownloadService.ServiceCallback {
    private static ServiceCallBack serviceCallBack = null;

    //静态工厂方法
    public static ServiceCallBack getInstance() {
        if (serviceCallBack == null) {
            serviceCallBack = new ServiceCallBack();
        }
        return serviceCallBack;
    }

    HcDownloadService mService;

    public void getService(HcDownloadService service) {
        mService = service;
        mService.setServiceCallback(this, HcDownloadService.PDF_SOURCE);
    }

    @Override
    public void serviceCallback(FileColumn fileColumn) {//接收Service传回的数据，并对数据库进行操作，并关联界面发送状态
        if (fileColumn.getSuccess() == 1) {//如果返回成功，直接操作数据库删除该文件
            HcLog.D("ServiceCallBack###########fileColumn" + fileColumn.getName() + "." + fileColumn.getExt());
            if (fileColumn.getUpOrDown() == 0) {//上传
//                OperateDatabase.deleteUploadInfo(fileColumn, HcApplication.getContext());
            } else if (fileColumn.getUpOrDown() == 1) {//下载
//                OperateDatabase.deleteDownloadInfo(fileColumn, HcApplication.getContext());
            }
        }
        mTransferCallback.transferCallback(fileColumn);
    }

    public interface TransferCallback {
        void transferCallback(FileColumn fileColumn);
    }

    private TransferCallback mTransferCallback;

    public void setTransferCallback(TransferCallback transferCallback) {
        mTransferCallback = transferCallback;
    }

}
