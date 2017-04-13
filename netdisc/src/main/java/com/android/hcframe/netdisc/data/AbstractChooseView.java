package com.android.hcframe.netdisc.data;

import android.app.Activity;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcLog;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.netdisc.MySkydriveActivity;
import com.android.hcframe.netdisc.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-8-1 10:59.
 */
public abstract  class AbstractChooseView extends AbstractPage implements AdapterView.OnItemClickListener {

    private static final String TAG = "AbstractChooseView";

    /** 当前上传的文件夹 */
    private String mFileDir;

    /** 当前上传的文件夹的ID */
    protected String mDirId;

    private Thread mScanThread;

    protected TextView mDir;

    protected TextView mCommit;

    protected AbsListView mAbsView;

    protected HcBaseAdapter mAdapter;

    protected final Uri mScanUrl;

    protected int mSelectCount;

    private Handler mHandler = new Handler();

    public AbstractChooseView(Activity context, ViewGroup group, String data, Uri scanUrl) {
        super(context, group);
        mScanUrl = scanUrl;
        parseJson(data);
    }

    @Override
    public void initialized() {
        if (isFirst) {
            isFirst = !isFirst;
            mDir.setText(mContext.getResources().getString(R.string.netdisc_upload_position) + "  " + mFileDir);
            mCommit.setEnabled(false);
            start();
        }

    }

    private void parseJson(String data) {
        try {
            JSONObject object = new JSONObject(data);
            mFileDir = object.getString(MySkydriveActivity.FILE_NAME);
            mDirId = object.getString(MySkydriveActivity.FILE_ID);
        } catch(JSONException e) {
            HcLog.D(TAG + "#parseJson JSONException e =" + e);
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
