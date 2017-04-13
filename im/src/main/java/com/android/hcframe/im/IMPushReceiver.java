package com.android.hcframe.im;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.android.hcframe.HcLog;
import com.android.hcframe.badge.BadgeCache;
import com.android.hcframe.badge.BadgeInfo;
import com.android.hcframe.im.data.AppMessageInfo;
import com.android.hcframe.im.data.ChatOperatorDatabase;
import com.android.hcframe.im.data.IMSettings;
import com.android.hcframe.push.HcPushManager;
import com.android.hcframe.push.PushInfo;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-11-22 08:52.
 */

public class IMPushReceiver extends BroadcastReceiver {

    private static final String TAG = "IMPushReceiver";

    public static final String ACTION_SYSTEM_MESSAGE = "com.android.hcframe.im.system_message";

    public static final String ACTION_CHAT_MESSAGE = "com.android.hcframe.im.chat_message";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        HcLog.D(TAG + " #onReveive action = " +action);
        if (ACTION_SYSTEM_MESSAGE.equals(action)) {
            String content = intent.getStringExtra("content");
            PushInfo info = new PushInfo(content);
            HcPushManager.getInstance().setPushInfo(info);
            // 清空对应的角标
            AppMessageInfo app = ChatOperatorDatabase.getAppMessageInfo(context, info.getAppId());
            if (app != null) {
                int count = app.getCount();
                app.setCount(0);
                ChatOperatorDatabase.updateAppMessage(context, app);
                app = ChatOperatorDatabase.getAppMessageInfo(context, AppMessageInfo.SYSTEM_MESSAGE_ID);

                String imAppId = IMSettings.getIMAppId(context);
                if (TextUtils.isEmpty(imAppId)) {
                    HcLog.D(TAG + " #onReceive 出错了, 找不到IM模块的appId!");
                } else {
                    BadgeInfo badgeInfo = BadgeCache.getInstance().getBadgeInfo(imAppId, imAppId + "_IM");
                    if (badgeInfo != null) {
                        badgeInfo.removeCount(count);
                    } else {
                        HcLog.D(TAG + " #onReceive 出错了, 找不到IM模块对应的角标!");
                    }
                }

                if (app != null) {
                    count = app.getCount() - count;
                    if (count < 0)
                        count = 0;
                    app.setCount(count);
                    ChatOperatorDatabase.updateAppMessage(context, app);
                } else {
                    HcLog.D(TAG + " #onReceive 出错了, 找不到系统消息记录! appId = "+AppMessageInfo.SYSTEM_MESSAGE_ID);
                }

            } else {
                HcLog.D(TAG + " #onReceive 出错了, 找不到对应应用的消息! appId = "+info.getAppId());
            }

            info.startActivity(context);
        } else if (ACTION_CHAT_MESSAGE.equals(action)) {
            AppMessageInfo messageInfo = intent.getParcelableExtra("emp");
            String appId = intent.getStringExtra("appId");
            if (messageInfo != null && !TextUtils.isEmpty(appId)) {
                PushInfo info = new PushInfo(null);
                info.setAppId(appId);
                info.setContent(messageInfo.getId());
                HcPushManager.getInstance().setPushInfo(info);
                info.startActivity(context);
            }


        }
    }
}
