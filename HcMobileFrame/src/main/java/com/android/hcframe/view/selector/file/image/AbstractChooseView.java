package com.android.hcframe.view.selector.file.image;

import android.app.Activity;
import android.net.Uri;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcLog;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.R;


/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-8-1 10:59.
 */
public abstract  class AbstractChooseView extends AbstractPage implements AdapterView.OnItemClickListener {

    private static final String TAG = "AbstractChooseView";

    private Thread mScanThread;

    protected TextView mSend; // 发送按钮

    protected AbsListView mAbsView;

    protected HcBaseAdapter mAdapter;

    protected final Uri mScanUrl;

    protected int mSelectCount;

    private Handler mHandler = new Handler();

    public AbstractChooseView(Activity context, ViewGroup group, String data, Uri scanUrl) {
        super(context, group);
        mScanUrl = scanUrl;
        mSend = (TextView) context.findViewById(R.id.topbar_right);
        mSend.setText("发送");

    }

    @Override
    public void initialized() {
        if (isFirst) {
            isFirst = !isFirst;
            start();
        }

    }

    private void stop() {
        if (mScanThread != null) {
            try {
                Thread t = mScanThread;
                t.join();
                mScanThread = null;
            } catch (InterruptedException ex) {
                // so now what?
            }
        }
    }

    @Override
    public void onDestory() {
        stop();
        mAbsView.setAdapter(null);
        if (mAdapter != null)
            mAdapter.releaseAdatper();
        mContext = null;
    }

    private void start() {
        if (mScanThread != null) {
            return;
        }
        Thread t = new Thread(new WorkerThread());
        t.setName("image-scan");
        mScanThread = t;
        t.start();
    }

    private class WorkerThread implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            scanFiles();
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    initAdapter();
                    mAbsView.setAdapter(mAdapter);
//                    HcLog.D(TAG + " $WorkerThread#run mAdapter list size = " + mAdapter.getCount());
                    stop();
                }
            });
        }

    }

    public abstract void scanFiles();

    public abstract void initAdapter();

    /**
     * 获取文件的扩展名
     * @param filePath
     * @return
     */
    protected String getExtByPath(String filePath) {
        int position = filePath.lastIndexOf('.');
        String ext = filePath.substring(position + 1, filePath.length());
        HcLog.D(TAG + " #getExtByPath filePath ="+filePath + " position = "+position + " ext = "+ext);
        return ext;
    }
}
