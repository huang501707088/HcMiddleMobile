package com.android.hcmail;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by zhujiabin on 2017/3/31.
 */

public class HcEmailSharedHelper {
    private final static String HCEMAIL = "HcEMAIL";

    /** 收件箱的最后一次刷新时间 */
    private static final String PREFERENCES_KEY_REFRESH_TIME = "refresh_time";

    /**
     * 判断是否第一次进入该页面
     */
    // 存储sharedpreferences
    public static void setSign(Context context, String sign) {
        SharedPreferences mSign = context.getSharedPreferences(HCEMAIL, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSign.edit();
        editor.putString("sign", sign);
        editor.commit();
    }

    // 获得sharedpreferences的数据
    public static String getSign(Context context) {
        SharedPreferences mSign = context.getSharedPreferences(HCEMAIL, Context.MODE_PRIVATE);
        String sign = mSign.getString("sign", "");//默认返回true
        return sign;
    }

    public static void setRefreshTime(Context context, long time) {
        SharedPreferences sp = context.getSharedPreferences(HCEMAIL, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(PREFERENCES_KEY_REFRESH_TIME, time);
        editor.commit();

    }

    public static long getRefreshTime(Context context) {
        SharedPreferences sp = context.getSharedPreferences(HCEMAIL, Context.MODE_PRIVATE);
        long time = sp.getLong(PREFERENCES_KEY_REFRESH_TIME, 0);
        return time;
    }
}
