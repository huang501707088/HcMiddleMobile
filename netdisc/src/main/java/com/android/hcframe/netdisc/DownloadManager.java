package com.android.hcframe.netdisc;

import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;

import com.android.hcframe.netdisc.netdisccls.DownFragItem;
import com.android.hcframe.netdisc.netdisccls.ImplFragItem;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhujb on 2016/7/4.
 */
public class DownloadManager {
    /**
     * 下载状态：正常，暂停，下载中，已下载，排队中
     */
    public static final int DOWNLOAD_STATE_NORMAL = 0x00;
    public static final int DOWNLOAD_STATE_PAUSE = 0x01;
    public static final int DOWNLOAD_STATE_DOWNLOADING = 0x02;
    public static final int DOWNLOAD_STATE_FINISH = 0x03;
    public static final int DOWNLOAD_STATE_WAITING = 0x04;

    /**
     * SparseArray是android中替代Hashmap的类,可以提高效率
     */
    private SparseArray<ImplFragItem> downloadFiles = new SparseArray<ImplFragItem>();
    /**
     * 用来管理所有下载任务
     */
    private ArrayList<DownloadTask> taskList = new ArrayList<DownloadTask>();
    private Handler mHandler;
    private final static Object syncObj = new Object();
    private static DownloadManager instance;
    private ExecutorService executorService;

    public DownloadManager() {
        super();
        /**
         * 最多只能同时下载3个任务，其余的任务排队等待
         */
        executorService = Executors.newFixedThreadPool(3);
    }

    /**
     * 构建DownloadManager实例
     */
    public static DownloadManager getInstance() {
        if (null == instance) {
            synchronized (syncObj) {
                instance = new DownloadManager();
            }
            return instance;
        }
        return instance;
    }

    /**
     * set一个Handler,开启handler可一个更新listview中的item
     */
    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    /**
     * 开始下载，创建一个下载线程
     */
    public void startDownload(ImplFragItem file) {
        downloadFiles.put(file.implFileId, file);
        DownloadTask task = new DownloadTask(file.implFileId);
        taskList.add(task);
        executorService.submit(task);
    }

    /**
     * 停止所有的下载线程
     */
    public void stopAllDownloadTask() {
        while (taskList.size() != 0) {
            DownloadTask task = taskList.remove(0);
            /**
             * 可以在这里做其他的处理
             */
            task.stopTask();
        }
        /**
         * 会停止正在进行的任务和拒绝接受新的任务
         * */
        executorService.shutdownNow();
    }

    /**
     * 下载任务
     */
    class DownloadTask implements Runnable {
        private boolean isWorking = false;
        private int downloadId;

        public DownloadTask(int id) {
            this.isWorking = true;
            this.downloadId = id;
        }

        /**
         * 停止任务
         */
        public void stopTask() {
            this.isWorking = false;
        }

        /**
         * 更新listview中对应的item
         */
        public void update(ImplFragItem downloadFile) {
            Message msg = mHandler.obtainMessage();
            if (Integer.valueOf(downloadFile.getImplFileM()) == Integer.valueOf(downloadFile.getImplFileDownM()))
                downloadFile.implFileState = DOWNLOAD_STATE_FINISH;
            msg.obj = downloadFile;
            msg.sendToTarget();
        }

        @Override
        public void run() {
            // 更新下载文件的状态
            ImplFragItem downloadFile = downloadFiles.get(downloadId);
            downloadFile.implFileState = DOWNLOAD_STATE_DOWNLOADING;
            while (isWorking) {
                /**
                 * 检测是否下载完成
                 * */
                if (downloadFile.implFileState != DOWNLOAD_STATE_DOWNLOADING) {
                    downloadFiles.remove(downloadFile.implFileId);
                    taskList.remove(this);
                    isWorking = false;
                    break;
                }
                /**
                 * 下载文件，并一秒动态更新一次数据
                 * */
                if (Integer.valueOf(downloadFile.getImplFileM()) <= Integer.valueOf(downloadFile.getImplFileDownM())) {
                    this.update(downloadFile);
                }
                /**
                 * 下载文件，并一秒动态更新一次数据
                 * */
                if (Integer.valueOf(downloadFile.getImplFileM()) < Integer.valueOf(downloadFile.getImplFileDownM())) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        downloadFile.implFileState = DOWNLOAD_STATE_PAUSE;
                        this.update(downloadFile);
                        downloadFiles.remove(downloadId);
                        isWorking = false;
                        break;
                    }
                    /**
                     * 下载数据,并更新listview中的数据
                     * */
                    int implFileDownM = Integer.valueOf(downloadFile.getImplFileDownM());
                    implFileDownM++;
                    downloadFile.setImplFileDownM(String.valueOf(implFileDownM));
                }
            }
        }
    }
}
