package com.android.hcframe.im;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.android.hcframe.HcLog;
import com.android.hcframe.sql.SettingHelper;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-10-11 14:33.
 */

public class IMService extends Service {

    private static final String TAG = "IMService";

    private static final String XMPP_ACTION = "com.android.hcframe.login";

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            HcLog.D(TAG + " #onReceive action = "+action);
            if (action.equals(XMPP_ACTION)) {
                // 登录
                IMManager.getInstance().login(SettingHelper.getUserId(getApplicationContext()),
                        SettingHelper.getIMPW(getApplicationContext()), "APP");
            } else if (action.equals("com.android.hcframe.logout")) {
                IMManager.getInstance().logOut();
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        HcLog.D(TAG + " it is onStartCommand! intent = "+intent + " flags = "+flags + " startId = "+startId);
        if (IMUtil.imEnabled())
            IMManager.getInstance().createXMPPSocket(/*IMUtil.IM_SERVER, IMUtil.IM_PORT*/);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        HcLog.D(TAG + " it is onDestroy!!!!!");
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        HcLog.D(TAG + " it is onCreate!!!!!");
        IntentFilter filter = new IntentFilter();
        filter.addAction(XMPP_ACTION);
        filter.addAction("com.android.hcframe.logout");
        registerReceiver(mReceiver, filter);

        super.onCreate();
    }
}
