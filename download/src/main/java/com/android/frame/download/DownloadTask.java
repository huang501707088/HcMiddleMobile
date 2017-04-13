package com.android.frame.download;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcUtil;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ncll on 2016/7/12.
 */
public class DownloadTask {

    FileColumn fileInfo = null;
    Context context = null;
    //    ThreadDAO dao = null;
    private long mfinished = 0;
    //本次下载大小
    private long downloadSize = 0;
    public boolean isPause = false;
    //	private int mThreadCount = 1;// 线程数量
    private List<DownloadThread> mThreadList = null;// 线程集合
    public static ExecutorService sExecutorService = Executors
            .newCachedThreadPool();// 线程池

    public DownloadTask(FileColumn fileInfo, Context context) {
        this.fileInfo = fileInfo;
        this.context = context;
//        dao = new ThreadDAOImpl(context);
    }

    public void download() {
        mThreadList = new ArrayList<DownloadThread>();
        // 启动多个线程进行下载
        DownloadThread thread = new DownloadThread(fileInfo);
        // thread.start();
        DownloadTask.sExecutorService.execute(thread);
        // 添加线程到集合中
        mThreadList.add(thread);
    }

    /**
     * 判断是否所有线程都执行完毕，用synchronized，是synchronized是在多个程序调用时，只有一个可以调用，
     * 其他的只能等调用完成才能继续调用
     */
    private synchronized void checkAllThreadFinished() {
        boolean allFinished = true;
        // 遍历线程集合，判断线程是否都执行完毕
        for (DownloadThread thread : mThreadList) {
            if (thread.isFinished) {
                allFinished = false;
                break;
            }
        }
        if (!allFinished) {
            // 删除线程信息------------------------------------------------
//            dao.deleteThread(fileInfo.getUrl());
            // 发送广播通知UI下载任务结束
            if ("0".equals(fileInfo.getState())) {//下载状态为0是正在下载，只有正在下载的文件在这里结束才说明是下载完成
                fileInfo.setSuccess(1);
            }
            mDownloadCallback.downloadCallBack(fileInfo);
        }
    }

    static DownloadCallback mDownloadCallback;

    //
    public interface DownloadCallback {
        void downloadCallBack(FileColumn fileColumn);
    }

    public static void setDownloadCallback(DownloadCallback downloadCallback) {
        mDownloadCallback = downloadCallback;
    }

    /**
     * 下载线程
     *
     * @author czx
     */
    class DownloadThread extends Thread {
        private FileColumn threadInfo = null;
        public boolean isFinished = false;// 线程是否执行完毕

        public DownloadThread(FileColumn threadInfo) {
            this.threadInfo = threadInfo;
        }

        public void run() {

            RandomAccessFile raf = null;
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            try {
                URL url = new URL(threadInfo.getUrl() + "?RANGE=" + threadInfo.getPosition()
                        + "&infoid=" + threadInfo.getFileid()
                        + "&type=F");
//                java.net.URL url = new URL("http://10.80.6.124:8080/terminalServer//file/getfile?fileid=5&type=common&clientId=13&date=1478506781376");

                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("GET");
                // 设置下载位置
//                connection.setRequestProperty("Range", "bytes=" + start + "-"
//                        + threadInfo.getEnd());// 用了Range服务器认为是部分下载
                // 设置文件写入位置
                File file = new File(HcDownloadService.DWONLOAD_PATH,
                        fileInfo.getName());
                fileInfo.setPath(HcDownloadService.DWONLOAD_PATH + "/" +
                        fileInfo.getName());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(threadInfo.getPosition());// 在读写的时候跳过设置好的字节数，从下一个字节数开始读写
//                Intent intent = new Intent(HcDownloadService.ACTION_UPDATE);
                // 开始下载
                mfinished = threadInfo.getPosition();
                downloadSize = 0;
                if (connection.getResponseCode() == HttpStatus.SC_OK) {
                    // 读取数据
                    inputStream = connection.getInputStream();
                    byte[] buffer = new byte[1024 * 4];
                    int len = -1;
                    long time = System.currentTimeMillis();
                    long fristTime = System.currentTimeMillis();
                    /**下载的 平均速度*/
                    int downloadSpeed = 0;
                    int useTime = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        // 写入文件
                        raf.write(buffer, 0, len);
                        // 累加整个文件的完成进度
                        mfinished += len;
                        downloadSize += len;
                        // 累加每个线程的完成进度
                        threadInfo.setPosition(threadInfo.getPosition() + len);
                        // 吧下载进度发送广播给Activity
                        if (System.currentTimeMillis() - time > 1000) {
                            time = System.currentTimeMillis();
                            long size = Long.parseLong(fileInfo.getFileSize());
                            useTime = (int) ((System.currentTimeMillis() - fristTime) / 1000);
                            downloadSpeed = (int) ((downloadSize / useTime) / 1024);
                            fileInfo.setState("0");
                            fileInfo.setSpeed(downloadSpeed);
                            fileInfo.setPosition(mfinished);
                            mDownloadCallback.downloadCallBack(fileInfo);
                            boolean opened = HcUtil.getSharePreference(HcApplication.getContext());//true开
                            if (opened && !isWifiConnected(HcApplication.getContext()) && fileInfo.getSource() == 1) {
                                isPause = true;
                                Handler handler = new Handler(Looper.getMainLooper());
                                System.out.println("service started");
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "请在WIFI下下载文件", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                        // 再下载过程中暂停，把下载进度保存------------------------------------------
                        if (isPause) {
                            threadInfo.setState("1");
                            if (threadInfo.getSource() == 1) {
                                threadInfo.setSpeed(0);
                                mDownloadCallback.downloadCallBack(fileInfo);
                            }
                            return;
                        }
                    }
                }
                // 标示线程执行完毕
                isFinished = true;
                // 检查下载任务是否执行完毕
                checkAllThreadFinished();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                threadInfo.setState("1");
                if (threadInfo.getSource() == 1) {
                    threadInfo.setSpeed(0);
                    mDownloadCallback.downloadCallBack(fileInfo);
                }
                e.printStackTrace();
            } finally {
                try {
                    connection.disconnect();
                    raf.close();
                    inputStream.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    public boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            return true;
        }

        return false;
    }
}
