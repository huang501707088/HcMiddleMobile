package com.android.hcframe.netdisc;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.hcframe.netdisc.netdisccls.ImplDownFragItem;
import com.android.hcframe.netdisc.netdisccls.ImplFragItem;

/**
 * Created by pc on 2016/6/27.
 */
public class ImplFragAdapterCopy extends BaseAdapter {
    /**
     * 初始进度值
     */
    private int mDownloadProgress = 0;
    /**
     * 没有开始下载
     */
    public final static int STATUS_NOBEGIN = 1;
    /**
     * 等待下载
     */
    public final static int STATUS_WAITING = 2;
    /**
     * 正在下载
     */
    public final static int STATUS_DOWNLOADING = 3;
    /**
     * 暂停
     */
    public final static int STATUS_PAUSED = 4;
    /**
     * 下载完成
     */
    public final static int STATUS_FINISHED = 5;
    private boolean downloading = false;//是否下载中
    private SparseArray<ImplDownFragItem> mImplFragItem = null;
    private Context mContext;
    private LayoutInflater mInflater;
    private DownloadManager downloadManager;
    private ListView listView;

    public ImplFragAdapterCopy(Context context, SparseArray<ImplDownFragItem> implFragItem) {
        super();
        mImplFragItem = implFragItem;
        mContext = context;
        mInflater = LayoutInflater.from(context);
        this.downloadManager = DownloadManager.getInstance();
        this.downloadManager.setHandler(mHandler);
    }

    @Override
    public int getCount() {
        return mImplFragItem.size();
    }

    @Override
    public Object getItem(int position) {
        return mImplFragItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImplFragItemHolder implFragItemHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.netdisc_impl_frag_list_item,
                    parent, false);
            implFragItemHolder = new ImplFragItemHolder();
            implFragItemHolder.implImg = (ImageView) convertView.findViewById(R.id.netdisc_impl_list_img);
            implFragItemHolder.implFileName = (TextView) convertView.findViewById(R.id.netdisc_impl_list_text);
            implFragItemHolder.implFileM = (TextView) convertView.findViewById(R.id.netdisc_impl_list_one_date);
            implFragItemHolder.implFileDownM = (TextView) convertView.findViewById(R.id.netdisc_impl_list_two_date);
            implFragItemHolder.implFileMS = (TextView) convertView.findViewById(R.id.netdisc_impl_down_speed);
            implFragItemHolder.mDownloadPercentView = (DownloadPercentView) convertView.findViewById(R.id.downloadPrecentView);
            convertView.setTag(implFragItemHolder);
        } else {
            implFragItemHolder = (ImplFragItemHolder) convertView.getTag();
        }
        final ImplDownFragItem app = mImplFragItem.get(position);
        implFragItemHolder.implFileName.setText(app.getImplFileName());
        implFragItemHolder.implFileDownM.setText((Integer.valueOf(app.getImplDownFileDownM()) * 100.0f / Integer.valueOf(app.getImplDownFileM())) + "%");
        Drawable drawable = mContext.getResources().getDrawable(R.drawable.netdisc_impl_list_img);
        implFragItemHolder.implImg.setImageDrawable(drawable);
        switch (app.getImplFileState()) {
            case DownloadManager.DOWNLOAD_STATE_NORMAL:
                downloading = false;
                implFragItemHolder.mDownloadPercentView.setStatus(STATUS_NOBEGIN);
                break;
            case DownloadManager.DOWNLOAD_STATE_DOWNLOADING:
                //状态下点击变为下载状态
                downloading = true;
                implFragItemHolder.mDownloadPercentView.setStatus(STATUS_DOWNLOADING);
                break;
            case DownloadManager.DOWNLOAD_STATE_FINISH:
                downloading = false;
                implFragItemHolder.mDownloadPercentView.setStatus(STATUS_FINISHED);
                break;
            case DownloadManager.DOWNLOAD_STATE_WAITING:
                //状态下点击变为下载状态
                downloading = false;
                implFragItemHolder.mDownloadPercentView.setStatus(STATUS_PAUSED);
                break;
        }
        implFragItemHolder.mDownloadPercentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (app.getImplFileState()) {
                    case DownloadManager.DOWNLOAD_STATE_NORMAL:
                        /**
                         * 开始下载
                         * */
                        downloading = true;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                              Message message = Message.obtain();
                              app.setImplFileState(DownloadManager.DOWNLOAD_STATE_DOWNLOADING);
                              app.setImplDownFileDownM("0");
                              app.setImplFileState(STATUS_DOWNLOADING);
                              message.obj = app;
                              mProgressHandler.sendMessage(message);
                            }
                        }).start();
                        break;
                    case DownloadManager.DOWNLOAD_STATE_DOWNLOADING:
                        /**
                         * 暂停下载
                         * */
                        downloading = false;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Message message = Message.obtain();
                                app.setImplFileState(DownloadManager.DOWNLOAD_STATE_WAITING);
                                //***
//                                app.setImplDownFileDownM("0");
                                app.setImplFileState(STATUS_PAUSED);
                                message.obj = app;
                                mProgressHandler.sendMessage(message);
                            }
                        }).start();
                        break;
                    case DownloadManager.DOWNLOAD_STATE_FINISH:
                        /**
                         * 弹出对话框，询问该用户是否重新下载
                         * */
                        downloading = false;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Message message = Message.obtain();
                                app.setImplFileState(DownloadManager.DOWNLOAD_STATE_FINISH);
                                //***
//                                app.setImplDownFileDownM("0");
                                app.setImplFileState(STATUS_FINISHED);
                                message.obj = app;
                                mProgressHandler.sendMessage(message);
                            }
                        }).start();
                        break;
                    case DownloadManager.DOWNLOAD_STATE_WAITING:
                        /**
                         * 继续下载
                         * */
                        downloading = true;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Message message = Message.obtain();
                                app.setImplFileState(DownloadManager.DOWNLOAD_STATE_DOWNLOADING);
                                //***
//                                app.setImplDownFileDownM("0");
                                app.setImplFileState(STATUS_DOWNLOADING);
                                message.obj = app;
                                mProgressHandler.sendMessage(message);
                            }
                        }).start();
                        break;
                }
            }
        });
        return convertView;
    }
    private Handler mProgressHandler = new Handler() {

        public void handleMessage(Message msg) {
            ImplDownFragItem downFile = (ImplDownFragItem) msg.obj;
            /**
             * notifyDataSetChanged会执行getView函数，更新所有可视item的数据
             * notifyDataSetChanged();
             * 只更新指定item的数据，提高了性能
             */
            updateView(downFile.getImplDownFileId());
        }
    };
    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            ImplFragItem downloadFile = (ImplFragItem) msg.obj;
            ImplDownFragItem appFile = mImplFragItem.get(downloadFile.implFileId);
            appFile.setImplDownFileDownM(downloadFile.getImplFileDownM());
            appFile.setImplFileState(downloadFile.getImplFileState());

            /**
             * notifyDataSetChanged会执行getView函数，更新所有可视item的数据
             * notifyDataSetChanged();
             * 只更新指定item的数据，提高了性能
             */
            updateView(appFile.getImplDownFileId());
        }
    };

    // 更新指定item的数据
    private void updateView(int index) {
        int visiblePos = listView.getFirstVisiblePosition();
        int offset = index - visiblePos;
        /**
         *  只有在可见区域才更新
         * */
        if (offset < 0) return;

        View view = listView.getChildAt(offset);
        final ImplDownFragItem app = mImplFragItem.get(index);
        final ImplFragItemHolder holder = (ImplFragItemHolder) view.getTag();

        holder.implFileName.setText(app.getImplFileName());
        holder.implFileDownM.setText((Integer.valueOf(app.getImplDownFileDownM()) * 100.0f / Integer.valueOf(app.getImplDownFileM())) + "%");
        Drawable drawable = mContext.getResources().getDrawable(R.drawable.netdisc_impl_list_img);
        holder.implImg.setImageDrawable(drawable);

        switch (app.getImplFileState()) {
            /**
             * 下载
             * */
            case DownloadManager.DOWNLOAD_STATE_NORMAL:
                //改变百分比按钮的样式
                downloading = false;
                holder.mDownloadPercentView.setStatus(STATUS_NOBEGIN);
                break;
            /**
             * 下载中
             * */
            case DownloadManager.DOWNLOAD_STATE_DOWNLOADING:
                //改变百分比按钮的样式
                downloading = true;
                // 线程控制进度
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (downloading) {
                            //当进度为100时通过handler把状态转为下载完成，并且退出线程
                            if (mDownloadProgress == 100) {
                                holder.mDownloadPercentView.setStatus(STATUS_FINISHED);
                                return;
                            }
                            mDownloadProgress = (int) (Integer.valueOf(app.getImplDownFileDownM()) * 100.0f / Integer.valueOf(app.getImplDownFileM()));
                            //handler更新进度
                            holder.mDownloadPercentView.setProgress(mDownloadProgress);
                        }
                    }
                });
                break;
            /**
             * 已下载
             * */
            case DownloadManager.DOWNLOAD_STATE_FINISH:
                //改变百分比按钮的样式
                downloading = false;
                holder.mDownloadPercentView.setStatus(STATUS_FINISHED);
                break;
            /**
             * 排队中
             * */
            case DownloadManager.DOWNLOAD_STATE_WAITING:
                //改变百分比按钮的样式
                downloading = false;
                holder.mDownloadPercentView.setStatus(STATUS_PAUSED);
                break;
        }
    }

    public class ImplFragItemHolder {
        ImageView implImg;
        TextView implFileName;
        TextView implFileM;
        TextView implFileDownM;
        TextView implFileMS;
        DownloadPercentView mDownloadPercentView;
    }
}
