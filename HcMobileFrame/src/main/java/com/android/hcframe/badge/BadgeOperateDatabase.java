package com.android.hcframe.badge;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.android.hcframe.HcLog;
import com.android.hcframe.sql.HcDatabase;
import com.android.hcframe.sql.HcDatabase.Badge;
import com.android.hcframe.sql.HcProvider;
import com.android.hcframe.sql.SettingHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-4-21 10:34.
 */
public class BadgeOperateDatabase {

    private static final String TAG = "BadgeOperateDatabase";

    public static List<BadgeInfo> getBadges(Context context) {
        List<BadgeInfo> badges = new ArrayList<BadgeInfo>();
        final ContentResolver cr = context.getContentResolver();
        String selection = Badge.ACCOUNT + "=" + "'"
                + SettingHelper.getAccount(context) + "'";
        String[] projection = {Badge.COUNT, Badge.BADGE_TYPE, Badge.VISIBILITY,
            Badge.APP_ID, Badge.MODULE_ID, Badge.TYPE};
        Cursor c = cr.query(HcProvider.CONTENT_URI_BADGE, projection, selection, null, null);
        if (c != null) {
            HcLog.D(TAG + " count = "+c.getCount());
        }
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            BadgeInfo badge;
            while (!c.isAfterLast()) {
                int type = c.getInt(5);
                if (type == BadgeInfo.TYPE_APP) {
                    badge = new AppBadgeInfo();
                } else {
                    badge = new ModuleBadgeInfo();
                }
                badge.setModuleId(c.getString(4));
                badge.setAppId(c.getString(3));
                badge.setCount(c.getInt(0));
                badge.setType(c.getInt(1));
                badge.setVisibility(c.getInt(2) != 0);
                badges.add(badge);
                c.moveToNext();
            }
        }
        if (c != null)
            c.close();
        return badges;
    }

    public static void insertBadges(Context context, BadgeInfo root) {
        List<BadgeInfo> infos = new ArrayList<BadgeInfo>();
        Iterator<BadgeInfo> iterator = root.iterator();
        while (iterator.hasNext()) {
        	infos.add(iterator.next());
        }
        insertBadges(context, infos);
    }

    public static int insertBadges(Context context, List<BadgeInfo> badges) {
        final ContentResolver cr = context.getContentResolver();
        String where = Badge.ACCOUNT + "=" + "'" + SettingHelper.getAccount(context) + "'";
        cr.delete(HcProvider.CONTENT_URI_BADGE, where, null);
        int size = badges.size();
        final ContentValues[] values = new ContentValues[size];
        for (int i = 0; i < size; i++) {
            values[i] = new ContentValues();
            setAppValues(badges.get(i), values[i], context);
        }

        int number = cr.bulkInsert(HcProvider.CONTENT_URI_BADGE, values);
        HcLog.D(TAG + " #insertBadges end insertBadges! insert number = " + number);
        return number;
    }

    private static void setAppValues(BadgeInfo info, ContentValues values, Context context) {
        values.clear();
        values.put(Badge.ACCOUNT, SettingHelper.getAccount(context));
        values.put(Badge.APP_ID, info.getAppId());
        values.put(Badge.BADGE_TYPE, info.getType());
        values.put(Badge.COUNT, info.getCount());
        values.put(Badge.MODULE_ID, info.getModuleId());
        values.put(Badge.TYPE, info instanceof AppBadgeInfo ? BadgeInfo.TYPE_APP : BadgeInfo.TYPE_MODULE);
        values.put(Badge.VISIBILITY, info.getVisibility() == false ? 0 : 1);
    }

    private static int updateBadge(Context context, BadgeInfo info) {
        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        String where = Badge.ACCOUNT + "=" + "'" + SettingHelper.getAccount(context) + "'"
                + " AND " + Badge.APP_ID + "=" + info.getAppId()
                + " AND " + Badge.MODULE_ID + "=" + info.getModuleId();
        values.put(Badge.TYPE, info instanceof AppBadgeInfo ? BadgeInfo.TYPE_APP : BadgeInfo.TYPE_MODULE);
        values.put(Badge.VISIBILITY, info.getVisibility() == false ? 0 : 1);
        values.put(Badge.BADGE_TYPE, info.getType());
        values.put(Badge.COUNT, info.getCount());
        int index = cr.update(HcProvider.CONTENT_URI_BADGE, values, where, null);
        return index;
    }
}
