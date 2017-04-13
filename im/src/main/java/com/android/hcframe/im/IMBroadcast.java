package com.android.hcframe.im;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;

import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-10-11 10:14.
 */

public class IMBroadcast extends BroadcastReceiver {

    private boolean mNetWorkAvailabled = false;

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        HcLog.D("IMBroadcast#onReceive action = "+action);
        if (!IMUtil.imEnabled()) return;
        if (HcUtil.IM_APP_STARTED_ACTION.equals(action)) {
            //启动服务
            IMManager.getInstance().addChatListener(IMReceiverListener.getInstance());
            if (Build.VERSION.SDK_INT >= 21) {
                KeepLiveManager.getInstance().startJobService(context);
            }
        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            if (!mNetWorkAvailabled && HcUtil.isNetWorkAvailable(context)) {
                mNetWorkAvailabled = true;
                // 说明从无网络---->有网络
                IMManager.getInstance().createXMPPSocket();
            } else {
                // 说明从有网络---->无网络
                mNetWorkAvailabled = false;
                IMManager.getInstance().logOut();
            }
        }
    }
}
