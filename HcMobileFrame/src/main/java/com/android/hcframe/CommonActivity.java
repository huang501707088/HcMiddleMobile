package com.android.hcframe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;

import com.android.hcframe.menu.MenuPageFactory;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-7-28 09:19.
 */

/**
 * 一个通用的Activity
 */
public class CommonActivity extends HcBaseActivity {

    private static final String TAG = "CommonActivity";

    private FrameLayout mParent;

    private TopBarView mTopBarView;

    private MenuPageFactory mPageFactory;

    private String mClassName;

    private String mTitle;

    private View mNetworkError;

    public static final String NETWORK_ACTION = ".network.CONNECTIVITY_CHANGE";

    private String mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HcLog.D(TAG + " it is onCreate! tast id ============================= "+getTaskId() + " this =" +this);
        HcLog.D(TAG + " #onCreate start time = " + HcUtil.getDate(HcUtil.FORMAT_HOUR, System.currentTimeMillis()));
        Intent intent = getIntent();
        if (null != intent && null != intent.getExtras()) {
            Bundle bundle = intent.getExtras();
            mClassName = bundle.getString("className");
            mTitle = bundle.getString("title", "");
            mData = bundle.getString("data", "");
            if (TextUtils.isEmpty(mClassName)) {
                finish();
                return;
            }
        }
        setContentView(R.layout.activity_common);
        mTopBarView = (TopBarView) findViewById(R.id.common_top_bar);
        mParent = (FrameLayout) findViewById(R.id.common_parent);
        mTopBarView.setTitle(mTitle);
        mPageFactory = new MenuPageFactory();
        mPageFactory.initMenu(mClassName);
        mPageFactory.onCreate(mData, this, mParent);
        mNetworkError = findViewById(R.id.common_show_networkerror);
        mNetworkError.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startSetting();
            }
        });
    }

    @Override
    protected void onDestroy() {
        mPageFactory.onDestory();
        mPageFactory = null;

        mParent.removeAllViews();
        mParent = null;

        mTopBarView = null;
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
        mPageFactory.onPause();
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
        mPageFactory.onResume();

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
        mPageFactory.onActivityResult(requestCode, resultCode, data);
    }
}
