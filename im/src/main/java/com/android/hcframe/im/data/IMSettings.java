package com.android.hcframe.im.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-11-9 09:10.
 */

public final class IMSettings {

    private static final String SETTING_PREFERENCES = "im_settings";

    /**
     * IM模块的appId
     */
    private static final String PREFERENCE_KEY_APPID = "appId";

    private static final String PREFERENCE_KEY_RECEIVER = "receiver";

    public static void setIMAppId(Context context, String appId) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_APPID, appId);
        editor.commit();
    }

    public static String getIMAppId(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String appId = sp.getString(PREFERENCE_KEY_APPID, "");
        return appId;
    }

    public static void setIMReceiverGroup(Context context, String groupId) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String groups = sp.getString(PREFERENCE_KEY_RECEIVER, "");
        if (!groups.contains(groupId)) {
            SharedPreferences.Editor editor = sp.edit();
            if (groups.equals("")) {
                editor.putString(PREFERENCE_KEY_RECEIVER, groupId);
            } else {
                editor.putString(PREFERENCE_KEY_RECEIVER, groups + ";" + groupId);
            }
            editor.commit();
        }

    }

    public static String getIMReceiverGroup(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String groups = sp.getString(PREFERENCE_KEY_RECEIVER, "");
        return groups;
    }

    public static void deleteIMReceiverGroup(Context context, String groupId) {
        String groups = getIMReceiverGroup(context);
        if (TextUtils.isEmpty(groups)) return;
        String[] groupList = groups.split(";");
        StringBuilder builder = new StringBuilder();
        for (String group : groupList) {
        	if (!group.equals(groupId)) {
                builder.append(group + ";");
            }
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }

        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_RECEIVER, builder.toString());
        editor.commit();

    }
}
