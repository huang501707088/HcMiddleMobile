package com.android.hcframe.contacts.command;

import android.content.Context;
import android.os.Parcelable;

import com.android.hcframe.command.Command;
import com.android.hcframe.contacts.data.ContactsCacheData;

import java.util.Map;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 17-4-7 15:12.
 */

public class ContactsCommand implements Command {


    @Override
    public void execute(Context context) {
        ContactsCacheData.getInstance().checkContacts(context);
    }

    @Override
    public void execute(Context context, Parcelable p) {
        ContactsCacheData.getInstance().checkContacts(context);
    }

    @Override
    public void execute(Context context, Map<String, String> data) {
        ContactsCacheData.getInstance().checkContacts(context);
    }
}
