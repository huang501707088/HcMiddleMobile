package com.android.hcframe.doc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-3-22 15:15.
 */
public class DocBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent doc = new Intent();
        doc.setClass(context, DocDetailsActivity.class);
        doc.putExtra("data_id", intent.getExtras().getString("data_id"));
        doc.putExtra("data_flag", intent.getExtras().getInt("data_flag"));
        context.startActivity(doc);
    }
}
