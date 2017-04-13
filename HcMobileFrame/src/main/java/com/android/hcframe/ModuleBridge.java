package com.android.hcframe;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-12-9 09:46.
 */

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;

import com.android.hcframe.sql.HcDatabase;
import com.android.hcframe.sql.HcProvider;


/**
 * 模块之间的桥接类
 */
public final class ModuleBridge {

    private static final String TAG = "ModuleBridge";

    /**
     * 查看员工详情
     * @param context
     * @param emp 需要查看的员工信息
     */
    public static void startContactDetailsActivity(Activity context, Parcelable emp) {
        startContactDetailsActivity(context, emp, false, -1);
    }

    /**
     * 查看员工详情
     * @param context
     * @param emp 需要查看的员工信息
     * @param IMModule 是否是IM模块.true:是
     * @param requestCode activity回调的requestCode
     */
    public static void startContactDetailsActivity(Activity context, Parcelable emp, boolean IMModule, int requestCode) {
        Intent intent = new Intent();
        intent.setClassName(context, "com.android.hcframe.internalservice.contacts.ContactDetailsAct");
        intent.putExtra("emp", emp);
        intent.putExtra("im", IMModule);
        if (IMModule) {
            context.startActivityForResult(intent, requestCode);
        } else {
            context.startActivity(intent);
        }

//        mContext.overridePendingTransition(0, 0);
    }

    /**
     * 查看员工详情
     * @param context
     * @param userId 需要查看的员工的userId
     * @return true：能找到员工; false：查找不到员工
     */
    public static boolean startContactDetailsActivity(Activity context, String userId) {
        return startContactDetailsActivity(context, userId, false, -1);
    }

    /**
     * 查看员工详情
     * @param context
     * @param userId 需要查看的员工的userId
     * @param IMModule 是否是IM模块.true:是
     * @param requestCode activity回调的requestCode
     * @return true：能找到员工; false：查找不到员工
     */
    public static boolean startContactDetailsActivity(Activity context, String userId, boolean IMModule, int requestCode) {
        if (exist(context, userId)) {
            Intent intent = new Intent();
            intent.setClassName(context, "com.android.hcframe.internalservice.contacts.ContactDetailsAct");
            intent.putExtra("userId", userId);
            intent.putExtra("im", IMModule);
            if (IMModule) {
                context.startActivityForResult(intent, requestCode);
            } else {
                context.startActivity(intent);
            }
            return true;
        }

        return false;

    }

    private static boolean exist(Context context, String userId) {
        final ContentResolver cr = context.getContentResolver();
        String where = HcDatabase.Contacts.USER_ID + "=" + "'" + userId + "'";
        Cursor c = cr.query(HcProvider.CONTENT_URI_CONTACTS, null, where, null, null);
        boolean exist = false;
        if (c != null && c.getCount() > 0) {
            exist = true;
        }
        if (c != null)
            c.close();
        return exist;
    }

    /**
     * 从IM私聊界面跳转到创建新任务界面
     * @param context
     * @param userId 任务执行人
     * @param content 任务内容
     */
    public static void startTaskActivity(Context context, String userId, String content) {
        startTaskActivity(context, userId, content, userId == null ? null : getNameByUserId(context, userId));
    }

    /**
     * 从IM私聊界面跳转到创建新任务界面
     * @param context
     * @param userId 任务执行人
     * @param content 任务内容
     */
    public static void startTaskActivity(Context context, String userId, String content, String name) {
        Intent intent = new Intent();
        intent.setClassName(context, "com.android.hcframe.hctask.PublicTaskActivity");
        intent.putExtra("userId", userId);
        intent.putExtra("content", content);
        intent.putExtra("name", name);
        context.startActivity(intent);
    }

    public static String getNameByUserId(Context context, String userId) {
        String name = "";
        String[] projection = {HcDatabase.Contacts.NAME};
        String where = HcDatabase.Contacts.USER_ID + "=" + "'" + userId + "'";
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(HcProvider.CONTENT_URI_CONTACTS, projection, where, null, null);
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            name = c.getString(0);
        }
        if (c != null)
            c.close();
        return name;
    }
}
