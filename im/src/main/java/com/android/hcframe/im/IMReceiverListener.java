package com.android.hcframe.im;

import android.text.TextUtils;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcLog;
import com.android.hcframe.badge.BadgeCache;
import com.android.hcframe.badge.BadgeInfo;
import com.android.hcframe.badge.ModuleBadgeInfo;
import com.android.hcframe.container.data.ContainerConfig;
import com.android.hcframe.container.data.ViewInfo;
import com.android.hcframe.im.data.AppMessageInfo;
import com.android.hcframe.im.data.ChatMessageInfo;
import com.android.hcframe.im.data.ChatOperatorDatabase;
import com.android.hcframe.im.data.IMSettings;
import com.android.hcframe.menu.MenuInfo;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-10-11 10:21.
 */

/**
 * 一个全局的接收器
 */
public class IMReceiverListener implements OnChatReceiveListener, OnReceiverCallback {

    private static final String TAG = "IMReceiverListener";

    private static IMReceiverListener mListener = new IMReceiverListener();

    private String mImAppId;

    public static IMReceiverListener getInstance() {
        return mListener;
    }

    private IMReceiverListener() {
    }

    @Override
    public void onReceive(Chat chat, Message message) {
        HcLog.D(TAG + "#onReceiver 在全局的接收器 chat = "+chat + " message = "+message);
        // 注意这里还在子线程里
        switch (message.getType()) {
            case error:
                HcLog.D(TAG + " 信息发送失败!");
                break;
            case chat:
            case groupchat:
            case normal:
                IMUtil.parseMessage(HcApplication.getContext(), message, this);
                break;

            default:
                break;
        }
    }

    @Override
    public void onReceiver(ChatMessageInfo chatInfo, AppMessageInfo appInfo) {

        if (mImAppId == null) {
            mImAppId = IMUtil.getIMAppId(HcApplication.getContext());
            if (!TextUtils.isEmpty(mImAppId)) {
                setBadge();
            } else {
                HcLog.D(TAG + " #onReceiver 出错了,IM模块的appId获取不到！");
            }

        } else {
            setBadge();
        }
    }

    private int getCounts() {
        List<AppMessageInfo> infos = ChatOperatorDatabase.getAppMessages(HcApplication.getContext());
        int count = 0;
        for (AppMessageInfo info : infos) {
            count += info.getCount();
        }
        return count;
    }

    private void setBadge() {
        BadgeInfo badgeInfo = BadgeCache.getInstance().getBadgeInfo(mImAppId, mImAppId + "_IM");
        if (badgeInfo != null) {
            badgeInfo.addCount(1);
        } else {
            badgeInfo = new ModuleBadgeInfo();
            badgeInfo.setAppId(mImAppId);
            badgeInfo.setModuleId(mImAppId + "_IM");
            badgeInfo.setType(1);
            badgeInfo.setCount(getCounts());
            BadgeCache.getInstance().matchBadge(badgeInfo, "add");

        }
    }
}
