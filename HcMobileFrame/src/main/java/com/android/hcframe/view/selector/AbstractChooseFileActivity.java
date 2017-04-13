package com.android.hcframe.view.selector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.view.selector.file.HcFileChooseHomeView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-11-28 10:13.
 */

public abstract class AbstractChooseFileActivity extends HcBaseActivity implements HcFileChooseHomeView.OnOperatorListener {

    private static final String TAG = "AbstractChooseActivity";

    private FrameLayout mParent;

    private View mNetworkError;

    public static final String NETWORK_ACTION = ".network.CONNECTIVITY_CHANGE";

    private HcFileChooseHomeView mChooseView;

    protected ChooseObserver mObserver;

    public static final String SELECT = "select";

    protected String mAppId;

    protected String mAppName;

    protected boolean mSelected;

    private Thread mScanThread;

    private Handler mHandler = new Handler();

    private List<ItemInfo> mInfos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HcLog.D(TAG + " it is onCreate! tast id ============================= "+getTaskId() + " this =" +this);
        HcLog.D(TAG + " #onCreate start time = " + HcUtil.getDate(HcUtil.FORMAT_HOUR, System.currentTimeMillis()));
        Intent intent = getIntent();
        if (null != intent && null != intent.getExtras()) {
            Bundle bundle = intent.getExtras();
            mAppId = bundle.getString("appId", "");
            mAppName = bundle.getString("appName", "");
            mSelected = bundle.getBoolean(SELECT, false);
        }
        setContentView(R.layout.activity_choose_file);
        mParent = (FrameLayout) findViewById(R.id.file_choose_parent);
        mNetworkError = findViewById(R.id.file_choose_show_networkerror);
        mNetworkError.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startSetting();
            }
        });
        String title = getTopbarTitle();
        if (title == null) {
            title = "文件选择";
        }
        mChooseView = new HcFileChooseHomeView(this, mParent, mSelected, this, title, null);
        mObserver = new ChooseObserver();
        mObserver.addObserver(mChooseView);
        mChooseView.changePages();
    }

    @Override
    protected void onDestroy() {
        stop();
        mParent.removeAllViews();
        mParent = null;
        super.onDestroy();
    }

    private void startSetting() {
        Intent intent = new Intent();
        //判断手机系统的版本  即API大于10 就是3.0或以上版本
        if (android.os.Build.VERSION.SDK_INT > 10) {
            intent.setAction(android.provider.Settings.ACTION_SETTINGS);
        } else {
            intent.setClassName("com.android.settings","com.android.settings.WirelessSettings");
//			ComponentName component = new ComponentName("com.android.settings","com.android.settings.WirelessSettings");
//            intent.setComponent(component);
            intent.setAction("android.intent.action.VIEW");
        }
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        unregisterReceiver(mReceiver);
        super.onPause();
        HcLog.D(TAG + " #onPause end time = " + HcUtil.getDate(HcUtil.FORMAT_HOUR, System.currentTimeMillis()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        HcLog.D(TAG + " it is onResume! this = " + this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(getPackageName() + NETWORK_ACTION);
        registerReceiver(mReceiver, filter);
        networkChanged();

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            HcLog.D(TAG + " action = "+intent.getAction() + " network available = "+HcUtil.isNetWorkAvailable(context));
            if (action.equals(context.getPackageName() + NETWORK_ACTION)) {
                networkChanged();
            }
        }
    };

    private void networkChanged() {
        if (HcUtil.isNetWorkAvailable(this)) {
            if (mNetworkError.getVisibility() == View.VISIBLE)
                mNetworkError.setVisibility(View.GONE);
        } else {
            if (mNetworkError.getVisibility() != View.VISIBLE)
                mNetworkError.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        HcLog.D(TAG + " requestCode = " + requestCode + " resultCode = "
                + resultCode + " intent = " + data);
    }

    private class ChooseObserver extends Observable {
        public void notifyData(List<ItemInfo> data) {
            setChanged();
            notifyObservers(data);
        }
    }

    /**
     * 获取标题栏的标题
     * @return
     */
    protected abstract String getTopbarTitle();

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

    private void start(ItemInfo info) {
        if (mScanThread != null) {
            return;
        }
        synchronized (mInfos) {
            mInfos.clear();
        }

        Thread t = new Thread(new WorkerThread(info.getItemId()));
        t.setName("file-scan");
        mScanThread = t;
        t.start();
    }

    private class WorkerThread implements Runnable {

        private String mFileName;

        public WorkerThread(String fileName) {
            mFileName = fileName;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            scanFiles(mFileName);
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    if (mObserver != null) {
                        synchronized (mInfos) {
                            mObserver.notifyData(mInfos);
                            mInfos.clear();
                        }

                    }

                    stop();
                }
            });
        }

    }

    protected abstract void scanFiles(String fileName);

    @Override
    public void onCanelRefreshRequest(ItemInfo info) {
        stop();
    }

    @Override
    public void onParentItemClick(ItemInfo info) {
        start(info);
    }

    @Override
    public void onRefresh(ItemInfo info) {
        start(info);
    }

    protected void addItemInfo(ItemInfo info) {
        synchronized (mInfos) {
            mInfos.add(info);
        }

    }

    protected void addAllItemInfos(Collection<ItemInfo> infos) {
        synchronized (mInfos) {
            mInfos.addAll(infos);
        }

    }
}
