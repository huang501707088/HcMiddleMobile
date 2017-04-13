/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-4-14 下午2:23:36
 */
package com.android.hcframe;


import com.android.hcframe.HcVpnManager.VpnCallback;
import com.android.hcframe.badge.BadgeCache;
import com.android.hcframe.intro.IntroActivity;
import com.android.hcframe.lock.LockActivity;
import com.android.hcframe.menu.Menu1Activity;
import com.android.hcframe.monitor.LogManager;
import com.android.hcframe.push.HcAppState;
import com.android.hcframe.push.HcPushManager;
import com.android.hcframe.push.HcPushManager.BindDeviceCallback;
import com.android.hcframe.sql.OperateDatabase;
import com.android.hcframe.sql.SettingHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;

public class LoadActivity extends Activity implements VpnCallback {

    private static final String TAG = "LoadActivity";

    public static final String LOCK_TAG = "lock_tag";

    private static final int SUCCESS = 0;
    private static final int FAILED = 1;
    private static final int TIME_OUT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        // PushSettings.enableDebugMode(this, true);
        HcLog.D(TAG + " it is onCreate! tast id ============================= " + getTaskId());
        HcAppState.getInstance().addActivity(this);
        setContentView(R.layout.activity_load);

        /**
         * @author jrjin
         * @date 2016-2-24 上午11:34:10
         */
        LogManager.getInstance().addLogsFromDatabase(this);
        if (SettingHelper.showGuidePage(LoadActivity.this, HcConfig
                .getConfig().getAppVersion(), false)) {
            BadgeCache.getInstance().reCreateBadge(this);
        } else {
            BadgeCache.getInstance().createBadge(this);
        }

        startLoading();
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case SUCCESS:
                    String pw = SettingHelper.getGesturePw(LoadActivity.this);
//				startService(new Intent(LoadActivity.this, LoctionService.class));
                    /**
                     * @author jrjin
                     * @date 2015-12-7 下午9:23:51
                     */
                    HcApplication.startAlertClock(LoadActivity.this);

                    if (TextUtils.isEmpty(pw)) {
                        if (SettingHelper.showGuidePage(LoadActivity.this, HcConfig
                                .getConfig().getAppVersion(), true)) {
                            /**
                             * @author jrjin
                             * @date 2016-2-23 上午9:43:08
                             * 进入应用的日志
                             * 放在HcApplication里面处理了
                             */
//                            if (TextUtils.isEmpty(SettingHelper.getAccount(LoadActivity.this))) {
//                                // 第一次进入应用
//                                HcConfig.getConfig().updatePermisstion(LoadActivity.this, true);
//                            } else {
//                                HcConfig.getConfig().updatePermisstion(LoadActivity.this, false);
//                            }


                            startIntroActivity();
                        } else {

                            /**
                             * @author jrjin
                             * @date 2016-2-23 上午9:43:08
                             * 进入应用的日志
                             */
                            LogManager.getInstance().addLog("", null, LogManager.TYPE_APP, LoadActivity.this);


                            /**
                             * @author jrjin
                             * @date 2016-3-21 11:45
                             * 放在HcApplication里面处理了
                             */
//                            HcConfig.getConfig().updatePermisstion(LoadActivity.this, false);


                            startMainActivity();
                        }
                    } else {
                        startLockActivity();
                    }
                    Intent intent = new Intent(/*this, DownloadService.class*/);
                    intent.setAction(getPackageName() + ".DownloadService");
                    intent.setPackage(getPackageName());
                    startService(intent);
                    finish();
                    break;
                case FAILED:
                    finishApp();
                    break;
                case TIME_OUT:
                    HcUtil.showToast(LoadActivity.this, "手机注册失败，请检查网络！");
                    /**
                     * @author jrjin
                     * @date 2015-12-7 下午2:59:11 放到FAILED分支里面去处理 finishApp();
                     */
                    mHandler.sendEmptyMessageDelayed(FAILED, 1000);
                    break;

                default:
                    break;
            }

        }

    };

    private void startLockActivity() {
        Intent intent = new Intent(this, LockActivity.class);
        intent.putExtra(LOCK_TAG, LockActivity.TAG_UNLOCK);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, Menu1Activity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void startIntroActivity() {
        Intent intent = new Intent(this, IntroActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    /**
     * 第一次启动去认证l3vpn的时候才会被调用，除非把vpn彻底退出,再次启动的时候才会去调用
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
//		mVpnManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        // mHandler.sendEmptyMessageDelayed(0, 1000);
    }

    @Override
    public void setStatus(int status) {
        // TODO Auto-generated method stub
        HcLog.D(TAG + " setStatus status = " + status);
        switch (status) {
            case HcUtil.VPN_AUTH_FAILED:
                HcUtil.showToast(this, "VPN认证失败，请检查网络！");
                mHandler.removeMessages(TIME_OUT);
                mHandler.sendEmptyMessageDelayed(FAILED, 1000);

                break;
            case HcUtil.VPN_AUTH_LOGOUT:

                break;
            case HcUtil.VPN_AUTH_SUCCESS:

                break;
            case HcUtil.VPN_L3VPN_SUCCESS:
                mHandler.removeMessages(TIME_OUT);

                startLoading();

                break;
            case HcUtil.VPN_INIT_FAILED:
                HcUtil.showToast(this, "网络不通，请检查网络！");
                mHandler.removeMessages(TIME_OUT);
                mHandler.sendEmptyMessageDelayed(FAILED, 1000);
                break;
            case HcUtil.VPN_INIT_SUCCESS:

                break;
            case HcUtil.VPN_L3VPN_FAILED:
                HcUtil.showToast(this, "L3VPN认证失败，请检查网络！");
                mHandler.removeMessages(TIME_OUT);
                mHandler.sendEmptyMessage(FAILED);
                break;
            case HcUtil.VPN_STATUS_OFFLINE:

                break;

            default:
                break;
        }
    }

    public void finishApp() {
        finish();
        System.exit(0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);

    }

    /**
     * 1.判断是否需要去服务器注册 注意与服务端注册与推送无关
     * 2.1.没有注册,先去服务端注册设备
     * 2.2.注册失败，直接退出应用
     * 2.3.注册成功，看3.1
     * 3.1.已经注册，直接4;并且是否需要百度推送而且是否没有绑定
     * 3.2.没有绑定，是否有ChannelId
     * 3.3.有,直接绑定,记录绑定成功的记录
     * 3.4.没有,去百度获取ChannelId,然后再3.3
     * 判断是否需要去百度推送服务器注册
     * 4、版本检测
     *
     * @author jrjin
     * @time 2015-12-4 下午4:52:03
     */
    private void startLoading() {
        // 判断是否有去自己的服务器注册过
        if (HcPushManager.getInstance().registered(this)) {
            startIMModule();
            // 已经注册,直接进入版本检测阶段
            HcLog.D(TAG + " startLoading 已经注册,直接进入版本检测阶段!");
//			mHandler.sendEmptyMessageDelayed(DVSTATE, 1000);
            /**
             * czx
             * 2016.4.22
             */
            //放在onCreate方法里面
//            BadgeCache.getInstance().createBadge(this);
            HcLog.D(TAG + " startLoading 已经注册,直接进入应用,并且进行版本检测!");
            mHandler.sendEmptyMessageDelayed(SUCCESS, 2000);
            /**
             *@author jinjr
             *@date 16-4-15 上午9:57
             * 放在Success里面处理,为了不使用悬浮框的样式作为版本升级的对话框
             */
//			Intent intent = new Intent(/*this, DownloadService.class*/);
//			intent.setAction(getPackageName() + ".DownloadService");
//			intent.setPackage(getPackageName());
//			startService(intent);

            /**
             * @date 2015-12-17 上午10:09:09
             */
            // 判断是否需要百度推送
            if (HcPushManager.getInstance().pushed() && !HcPushManager.getInstance().deviceBinded(this)) {
                // channelId绑定设备
                if (HcPushManager.getInstance().channelIdExist(this)) {
                    HcLog.D(TAG + " startLoading channel已经存在,直接去服务端注册绑定设备!");
                    // 去服务端注册
                    HcPushManager.getInstance().registerDevice(this, true);
                } /*else {
                    // 去获取Channel
					HcLog.D(TAG + " startLoading channel不存在,去百度推送服务器获取ChannelId!");
					HcPushManager.getInstance().getChannelId(this);
				}*/

            }
            /**
             * @author jrjin
             * @date 2015-12-31 下午3:05:40
             * 不管怎么样子,都去百度那边注册,为了增加消息的到达率
             */
            HcPushManager.getInstance().getChannelId(this);

        } else {
            // 先去服务端注册设备,注册成功再去注册百度推送绑定设备
            HcLog.D(TAG + " startLoading 去服务端注册设备!");
            if (HcUtil.isNetWorkAvailable(this)) {
                HcPushManager.getInstance().setBindDeviceCallback(mCallback);
                HcPushManager.getInstance().registerDevice(this, false);
            } else {
                HcUtil.showToast(this, "网络不可用,请先打开网络以便设备注册!");
                mHandler.removeMessages(TIME_OUT);
                mHandler.sendEmptyMessageDelayed(FAILED, 1000);
            }

        }
    }

    /**
     * @author jinjr
     * @date 16-4-15 上午9:57
     */

    private BindDeviceCallback mCallback = new BindDeviceCallback() {

        @Override
        public void onResult(int status) {
            // TODO Auto-generated method stub
            if (status == HcPushManager.STATUS_OK) {
                startIMModule();
                HcLog.D("LoadActivity $Callback#onResult is OK 设备注册成功！");
                HcPushManager.getInstance().setBindDeviceCallback(null);
                /**
                 * czx
                 * 2016.4.22
                 */
                //放在onCreate方法里面
//                BadgeCache.getInstance().createBadge(LoadActivity.this);
                mHandler.removeMessages(TIME_OUT);
                mHandler.sendEmptyMessageDelayed(SUCCESS, 2000);
                /**
                 *@author jinjr
                 *@date 16-4-15 上午9:57
                 * 放在Success里面处理,为了不使用悬浮框的样式作为版本升级的对话框
                 */
//				Intent intent = new Intent(/*LoadActivity.this, DownloadService.class*/);
//				intent.setAction(getPackageName() + ".DownloadService");
//				intent.setPackage(getPackageName());
//				startService(intent);

                // 判断是否需要百度推送
                if (HcPushManager.getInstance().pushed()) {
                    // channelId绑定设备
                    if (HcPushManager.getInstance().channelIdExist(LoadActivity.this)) {
                        HcLog.D(TAG + " $Callback#onResult channel已经存在,直接去服务端注册绑定设备!");
                        // 去服务端注册
                        HcPushManager.getInstance().registerDevice(LoadActivity.this, true);
                    } else {
                        // 去获取Channel
                        HcLog.D(TAG + " $Callback#onResult channel不存在,去百度推送服务器获取ChannelId!");
                        HcPushManager.getInstance().getChannelId(LoadActivity.this);
                    }

                }
            } else {
                // 退出应用
                HcPushManager.getInstance().setBindDeviceCallback(null);
                HcUtil.showToast(LoadActivity.this, "服务端绑定设备失败,将退出应用！");
                mHandler.removeMessages(TIME_OUT);
                mHandler.sendEmptyMessageDelayed(FAILED, 1000);
            }
        }
    };

    @Override
    protected void onDestroy() {
        HcAppState.getInstance().removeActivity(this);
        super.onDestroy();
    }

    private void startIMModule() {
        if (Build.VERSION.SDK_INT >= 21) {
            Intent intent = new Intent(HcUtil.IM_APP_STARTED_ACTION);
            intent.setPackage(getPackageName());
            sendBroadcast(intent);
        }
    }
}
