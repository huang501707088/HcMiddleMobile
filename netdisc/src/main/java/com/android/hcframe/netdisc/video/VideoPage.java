package com.android.hcframe.netdisc.video;

import android.app.Activity;
import android.provider.MediaStore;
import android.view.ViewGroup;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.menu.MenuPage;
import com.android.hcframe.netdisc.audio.AudioChooseView;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-8-1 17:31.
 */
public class VideoPage extends MenuPage {

    @Override
    public AbstractPage createPage(String appId, Activity context, ViewGroup parent) {
        return new AudioChooseView(context, parent, appId, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
    }
}
