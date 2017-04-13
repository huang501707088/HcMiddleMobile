/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-4-29 上午9:25:00
 */
package com.android.hcframe.push;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.android.hcframe.AbsActiviy;
import com.android.hcframe.R;
import com.android.hcframe.TopBarView;
import com.android.hcframe.monitor.LogManager;

public class PushBaseActivity extends AbsActiviy {

    private static final String TAG = "PushBaseActivity";

    protected FrameLayout mParent;

    protected TopBarView mTopBarView;
    protected String mAppId;
    String mMenuPage = "";
    String title = "";

    @Override
    protected void onInitView() {
        setContentView(R.layout.activity_base_center);
        // ((HcApplication)getApplication()).getRefWatcher().watch(this);
        mTopBarView = (TopBarView) findViewById(R.id.menu_top_bar);
        mParent = (FrameLayout) findViewById(R.id.center_parent);
        mTopBarView.setReturnViewListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                HcAppState.getInstance().removeActivity(PushBaseActivity.this);
                LogManager.getInstance().updateLog(PushBaseActivity.this, false);
                finish();
            }
        });
        getWindow().setFormat(PixelFormat.RGBA_8888);
        HcAppState.getInstance().addActivity(this);
        HcAppState.getInstance().setAppOnStarted();
    }

    @Override
    protected void onInitData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mMenuPage = bundle.getString("mMenuPage");
        title = bundle.getString("title");
        mTopBarView.setTitle(title);
    }

    @Override
    protected void setPameter() {
        menuPage = mMenuPage;
    }

}
