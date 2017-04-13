package com.android.hcframe.im;

import com.android.hcframe.im.data.AppMessageInfo;
import com.android.hcframe.im.data.ChatMessageInfo;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-10-18 11:35.
 */

public interface OnReceiverCallback {

    public void onReceiver(ChatMessageInfo chatInfo, AppMessageInfo appInfo);

    public class DefaultCallback implements OnReceiverCallback {

        @Override
        public void onReceiver(ChatMessageInfo chatInfo, AppMessageInfo appInfo) {

        }
    }
}
