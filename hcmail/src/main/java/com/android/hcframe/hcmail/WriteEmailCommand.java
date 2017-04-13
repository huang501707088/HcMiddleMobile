package com.android.hcframe.hcmail;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import com.android.hcframe.CommonActivity;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcLog;
import com.android.hcframe.command.AbstractCommand;

import java.util.Map;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 17-4-10 18:01.
 */

public class WriteEmailCommand extends AbstractCommand {

    public static final int FROM_CONTACTS = 1;

    @Override
    public void execute(Context context) {

    }

    @Override
    public void execute(Context context, Parcelable p) {

    }

    @Override
    public void execute(Context context, Map<String, String> data) {
        HcLog.D(EmailUtils.DEBUG, "WriteEmailCommand execute start!!!!!!!!!!!!!!!");
        HcConfig.AppConfigure configure = HcConfig.getConfig().getAppConfigure(HcConfig.Module.MAIL);
        if (configure == null) {
            return;
        }
        String name = data.get("name");
        String address = data.get("address");
        Intent intent = new Intent(context, CommonActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("address", address);
        intent.putExtra("from", FROM_CONTACTS);
        intent.putExtra("data", configure.mAppId);
        intent.putExtra("title", "写邮件");
        intent.putExtra("className", "com.android.hcmail.HcEmailMenuPage");
        context.startActivity(intent);
    }
}
