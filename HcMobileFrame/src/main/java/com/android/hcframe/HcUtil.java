/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2013-12-12 下午2:10:37
 */
package com.android.hcframe;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StatFs;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

//import com.android.hcframe.internalservice.signin.LoctionService;
import com.android.hcframe.badge.AppBadgeInfo;
import com.android.hcframe.badge.BadgeCache;
import com.android.hcframe.badge.BadgeObserver;
import com.android.hcframe.internalservice.signin.SignCache;
import com.android.hcframe.login.LoginActivity;
import com.android.hcframe.menu.DownloadPDFActivity;
import com.android.hcframe.menu.Menu1Activity;
import com.android.hcframe.menu.Menu2Activity;
import com.android.hcframe.menu.Menu3Activity;
import com.android.hcframe.menu.Menu4Activity;
import com.android.hcframe.menu.Menu5Activity;
import com.android.hcframe.menu.MenuWebPage;
import com.android.hcframe.monitor.LogManager;
import com.android.hcframe.push.HcAppState;
import com.android.hcframe.sql.SettingHelper;
import com.android.hcframe.view.TwoBtnAlterDialog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class HcUtil {

    private static final String TAG = "HcUtil";
    /**
     * 网盘片段大小
     */
    public static final int FILE_CHUNK_SIZE = 1 * 1024 * 1024; // 5M
    public static final String FORMAT_YEAR = "yyyy/MM/dd";
    public static final String FORMAT_MONTH = "MM月dd日";
    public static final String FORMAT_HOUR = "HH:mm:ss";
    public static final String FORMAT_DATE = "yyyyMMddHH";
    public static final String FORMAT_DAY = "MM/dd";
    public static final String FORMAT_CITY_UPDATE = "yyyy/MM/dd HH:mm";
    public static final String FORMAT_MONITOR = "yyyy/MM/dd HH:mm:ss";
    public static final String FORMAT_CITY_HOUR = "HH:mm";
    public static final String FORMAT_POLLUTION = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_POLLUTION_NEW = "yyyy-MM-dd";
    public static final String FORMAT_YEAR_MONTH = "yyyy-MM";
    public static final String FORMAT_YEAR_MONTH_DAY = "yyyy.MM.dd";

    public static final String FORMAT_POLLUTION_S = FORMAT_POLLUTION + ".S";

    public static final String FORMAT_SIGN_YEAR_MONTH_DAY = "yyyy年MM月dd日";


    public static final String FORMAT_LOG_DATE = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * 确保推送启动在引导页之后
     *
     * @author jrjin
     * @date 2015-12-7 下午2:57:35
     * @see LoadActivity#
     * @deprecated
     */
    public static boolean isUpdate = false;

    /**
     * @deprecated
     */
    public static final String MARKET_APP_ID = "001";
    public static final String OA_APP_ID = "004";

    public static final String MARKET_APP_CLASSNAME = "com.android.hcframe.market.MarketMenuPage";

    public static final int APP_NORMAL = 0;
    public static final int APP_INSTALL = 1;
    public static final int APP_UPDATE = 2;

    public static final int VPN_INIT_SUCCESS = 0;
    public static final int VPN_INIT_FAILED = 1;
    public static final int VPN_AUTH_FAILED = 2;
    public static final int VPN_AUTH_SUCCESS = 3;
    public static final int VPN_STATUS_ONLINE = 4;
    public static final int VPN_STATUS_OFFLINE = 5;
    public static final int VPN_AUTH_LOGOUT = 6;
    public static final int VPN_L3VPN_SUCCESS = 7;
    public static final int VPN_L3VPN_FAILED = 8;

    public static final int REQUEST_CODE_HTML = 1;

    public static final int REQUEST_CODE_LOGIN = 100;

    public static int LOGIN_SUCCESS = 100;

    public static String CLIKC_ACTION = "com.android.hcframe.click";

    public static final int NEWS_COUNT = 20;

    /**
     * 默认状态
     */
    public static final int[] EMPTY_STATE_SET = {};
    /**
     * 获取到焦点时的状态
     */
    public static final int[] FOCUSED_STATE_SET = {android.R.attr.state_focused};
    /**
     * 选中时的状态
     */
    public static final int[] SELECTED_STATE_SET = {android.R.attr.state_selected};
    /**
     * 按下去时的状态
     */
    public static final int[] PRESSED_STATE_SET = {android.R.attr.state_pressed};
    /**
     * Indicates the view's window has focus.
     */
    public static final int[] FOCUSED_WINDOW_FOCUSED_STATE_SET = {
            android.R.attr.state_focused, android.R.attr.state_window_focused};
    public static final int[] FOCUSED_WINDOW_UNFOCUSED_STATE_SET = {
            android.R.attr.state_focused, -android.R.attr.state_window_focused};
    public static final String PACKAGE_NAME = "com.android.hcframe";

    public static final String START_SERVICE = "com.android.hcframe.HcService";

    public static final String SERVICE_CLASS = "com.android.hcframe.service.HcService";

    /**
     * 启动拍照
     */
    public static final int REQUEST_CODE_FROM_CAMERA = 3;
    /**
     * 启动相册
     */
    public static final int REQUEST_CODE_FROM_ALBUM = 4;
    /**
     * 启动裁剪
     */
    public static final int REQUEST_CODE_FROM_CROP = 5;
    /**
     * 启动照片选择
     */
    public static final int REQUEST_CODE_HEAD_PORTRAIT = 6;
    /**
     * MapTunActivity传回数值
     */
    public static final int REQUEST_CODE_FOR_TEXT = 7;

    /** 应用启动通知IM模块 */
    public static final String IM_APP_STARTED_ACTION = "com.android.hcframe.im.app_start";

    public static String changeDate(String date) {
        SimpleDateFormat f = new SimpleDateFormat(FORMAT_POLLUTION);
        try {
            Date d = f.parse(date);
            SimpleDateFormat sd = new SimpleDateFormat(FORMAT_POLLUTION_NEW);
            return sd.format(d);
        } catch (Exception e) {
        }

        return date;
    }

    public static String changeDateFormat(String date, String format1,
                                          String format2) {
        SimpleDateFormat f = new SimpleDateFormat(format1);
        try {
            Date d = f.parse(date);
            SimpleDateFormat sd = new SimpleDateFormat(format2);
            return sd.format(d);
        } catch (Exception e) {
        }

        return date;
    }

    public static long getDateMills(String date) {
        SimpleDateFormat f = new SimpleDateFormat(FORMAT_POLLUTION);
        try {
            Date d = f.parse(date);
            return d.getTime();
        } catch (Exception e) {
        }
        return 0;
    }

    public static final Date getDate(String date, String format) {
        Date d = null;
        SimpleDateFormat f = new SimpleDateFormat(format);
        try {
            d = f.parse(date);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return d;
    }

    public static String getDate(String f, long longTime) {
        SimpleDateFormat format = new SimpleDateFormat(f);
        Date d = null;
        d = new Date(longTime);
        String strMsgTime = format.format(d);
        return strMsgTime;
    }

    public static String getNowDate(String f) {
        SimpleDateFormat format = new SimpleDateFormat(f);
        Date d = new Date();
        return format.format(d);
    }

    /**
     * 网络中断
     *
     * @param context
     * @author jrjin
     * @time 2015-4-13 上午10:46:45
     */
    public static void toastNetworkError(Context context) {
        if (null == context)
            return;
        showToast(
                context,
                context.getResources().getString(
                        R.string.toast_request_network_error));
    }

    /**
     * 与服务器连接超时
     *
     * @param context
     * @author jrjin
     * @time 2015-4-13 上午10:46:45
     */
    public static void toastTimeOut(Context context) {
        if (null == context)
            return;
        showToast(
                context,
                context.getResources().getString(
                        R.string.toast_connect_time_out));
    }

    /**
     * 服务器数据返回错误
     *
     * @param context
     * @author jrjin
     * @time 2015-4-13 上午10:47:20
     */
    public static void toastDataError(Context context) {
        if (null == context)
            return;
        showToast(context,
                context.getResources().getString(R.string.toast_data_error));
    }

    /**
     * 系统错误,服务请求返回400，500&
     *
     * @param context
     * @author jrjin
     * @time 2015-12-20 下午7:50:49
     */
    public static void toastSystemError(Context context) {
        if (null == context)
            return;
        showToast(
                context,
                context.getResources().getString(
                        R.string.toast_system_error));
    }

    private static Toast mToast;

    public static void showToast(Context context, String content) {
        if (null == context)
            return;
        if (mToast == null) {
            Toast toast = Toast.makeText(context, content, Toast.LENGTH_SHORT);
            mToast = toast;
        } else {
            try {
                mToast.setText(content);
                mToast.setDuration(Toast.LENGTH_SHORT);
            } catch (Exception e) {
                // TODO: handle exception
                if (mToast != null) {
                    mToast.cancel();
                    mToast = null;
                }
                Toast toast = Toast.makeText(context, content,
                        Toast.LENGTH_SHORT);
                mToast = toast;
            }

        }
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.show();
    }

    private static Resources mResources;

    public static void showToast(Context context, int resId) {
        if (null == context)
            return;
        if (mResources == null)
            mResources = context.getResources();
        if (mToast == null) {
            Toast toast = Toast.makeText(context, mResources.getString(resId),
                    Toast.LENGTH_SHORT);
            mToast = toast;
        } else {
            try {
                mToast.setText(mResources.getString(resId));
                mToast.setDuration(Toast.LENGTH_SHORT);
            } catch (Exception e) {
                // TODO: handle exception
                if (mToast != null) {
                    mToast.cancel();
                    mToast = null;
                }
                Toast toast = Toast.makeText(context,
                        mResources.getString(resId), Toast.LENGTH_SHORT);
                mToast = toast;
            }

        }
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.show();
    }

    private static int sActiveTabIndex = -1;

    private static int mPreActiveTab = -1;

    /**
     * 带有ButtonBar的Activity加载View的时候调用此方法
     *
     * @param a         当前的Activity
     * @param highlight 当前button的ID
     */
    public static boolean updateButtonBar(Activity a, int highlight) {
        final TabWidget ll = (TabWidget) a.findViewById(R.id.menubar);
        // ll.setBackgroundColor(HcConfig.getConfig().getMenuBarBackgroundColor());
        int size = ((HcApplication) a.getApplication()).getMenuSize();
        int count = ll.getChildCount();
        if (size > count)
            throw new IndexOutOfBoundsException("menu is more than tab widget!");
        if (size <= 1) {
            ll.setVisibility(View.GONE);
            return false;
        }
        for (int i = size; i < count; i++) {
            ll.getChildAt(i).setVisibility(View.GONE);
        }

        String appId = null;

        for (int i = 0; i < size; i++) {
            TextView v = (TextView) ll.getChildAt(i);
            /**
             *@author jinjr
             *@date 16-4-21 下午2:03
             */
            appId = HcConfig.getConfig().getCurrentMenu(i).getAppId();

            BadgeCache.getInstance().addAppBadgeObserver(appId, HcConfig.getConfig().getClientId() + "_" +
                            appId, (BadgeObserver) v
            );

            if (HcConfig.getConfig().getMenuTitleVisibility()) {
                v.setText(((HcApplication) a.getApplication())
                        .getCurrentMenuInfo(i).getAppName());
                // v.setTextColor(HcConfig.getConfig().getMenuTitileColor());
            } else {
                v.setCompoundDrawablePadding(0);
                v.setText("");
                v.setTextSize(0.1f);
                // v.setPadding(0, (int)(17 * mDensity), 0, (int)(16 *
                // mnsity));
            }
            if (HcConfig.getConfig().getMenuIconVisibility()) {
                ;
                // v.setCompoundDrawablesWithIntrinsicBounds(null,
                // HcConfig.getConfig().getCurrentMenu(i).getMenuIcon(), null,
                // null);
            } else {
                v.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
                        null);
            }
        }
        boolean withtabs = false;
        for (int i = count - 1; i >= 0; i--) {

            View v = ll.getChildAt(i);
            boolean isActive = (v.getId() == highlight);
            if (isActive) {
                // v.setBackgroundColor(HcConfig.getConfig().getMenuItemSelectColor());
                ll.setCurrentTab(i);
                sActiveTabIndex = i;
            } else {
                // v.setBackgroundColor(HcConfig.getConfig().getMenuItemNormalColor());
            }
            v.setTag(i);
            v.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        for (int i = 0; i < ll.getTabCount(); i++) {
                            if (ll.getChildTabViewAt(i) == v) {
                                ll.setCurrentTab(i);
                                processTabClick((Activity) ll.getContext(), v,
                                        ll.getChildAt(sActiveTabIndex).getId());
                                break;
                            }
                        }
                    }
                }
            });

            v.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    // if (v.getId() != ll.getChildAt(sActiveTabIndex).getId())
                    // {
                    // ll.getChildAt(sActiveTabIndex).setBackgroundColor(
                    // HcConfig.getConfig().getMenuItemNormalColor());
                    // }
                    processTabClick((Activity) ll.getContext(), v, ll
                            .getChildAt(sActiveTabIndex).getId());
                }
            });
        }
        return withtabs;
    }

    /**
     * @param a
     * @param v       当前点击的View
     * @param current 当前选中的,要是切换成功则变为上一个
     * @author jrjin
     * @time 2015-11-3 下午2:36:51
     */
    private static void processTabClick(Activity a, View v, int current) {
        int id = v.getId();
        if (id == current) {
            return;
        }

        /**
         *@author jinjr
         *@date 16-4-21 下午2:03
         * 操作,更新数据
         */
        BadgeCache.getInstance().operateBadge((BadgeObserver) v);

        final TabWidget ll = (TabWidget) a.findViewById(R.id.menubar);
        /** 记录当前的，跳转成功后变为上一个 */
        mPreActiveTab = current;
        activateTab(a, id);
        if (id != -1) {
            // v.setBackgroundColor(HcConfig.getConfig().getMenuItemSelectColor());
            ll.setCurrentTab((Integer) v.getTag());
        }
    }

    private static void activateTab(Activity a, int id) {
        Intent intent = new Intent();
        if (id == R.id.menu1) {
            intent.setClass(a, Menu1Activity.class);
        } else if (id == R.id.menu2) {
            intent.setClass(a, Menu2Activity.class);
        } else if (id == R.id.menu3) {
            intent.setClass(a, Menu3Activity.class);
        } else if (id == R.id.menu4) {
            intent.setClass(a, Menu4Activity.class);
        } else if (id == R.id.menu5) {
            intent.setClass(a, Menu5Activity.class);
        } else {
            return;
        }
        /*
         * switch (id) { case R.id.menu1: intent.setClass(a,
		 * Menu1Activity.class); break; case R.id.menu2: intent.setClass(a,
		 * Menu2Activity.class); break; case R.id.menu3: intent.setClass(a,
		 * Menu3Activity.class); break; case R.id.menu4: intent.setClass(a,
		 * Menu4Activity.class); break; case R.id.menu5: intent.setClass(a,
		 * Menu5Activity.class); break;
		 * 
		 * default: return; }
		 */
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        HcAppState.getInstance().removeActivity(a);

        /**
         * @author jrjin
         * @date 2016-2-23 下午4:43:35
         * 更新退出日志
         */
        LogManager.getInstance().updateLog(a, false);

        /**
         * @date 2015-12-30 下午2:48:23
         * 这里放在@NewsHomePage的onDestory里面处理
        HcNewsData.getInstance().deleteObservers();// 清空观察者
         */
        a.startActivity(intent);
        a.finish();
        a.overridePendingTransition(0, 0);
    }

    /**
     * 点击导航栏进入应用,需要用户权限，跳转到登录，登录失败跳转回原先的Activity
     *
     * @param a
     * @author jrjin
     * @time 2015-11-3 下午2:51:49
     */
    public static void startPreActivity(Activity a) {
        activateTab(a, mPreActiveTab);
    }

    public static boolean isNetWorkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo info = cm.getActiveNetworkInfo();

        return info != null && info.isConnected();
    }

    public static boolean isNetWorkError(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo info = cm.getActiveNetworkInfo();
        boolean error = !(info != null && info.isConnected());
        if (error) {
            showToast(
                    context,
                    context.getResources().getString(
                            R.string.toast_network_error));
        }
        return error;
    }

    private static char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static MessageDigest messagedigest = null;

    static {
        try {
            messagedigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nsaex) {

            nsaex.printStackTrace();
        }
    }

    public static String getMD5String(String s) {
        return getMD5String(s.getBytes());
    }

    private static String getMD5String(byte[] bytes) {
        messagedigest.update(bytes);
        return bufferToHex(messagedigest.digest());
    }

    private static String bufferToHex(byte bytes[]) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    private static String bufferToHex(byte bytes[], int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString().toUpperCase();
    }

    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = hexDigits[(bt & 0xf0) >> 4];// 取字节中高 4 位的数字转换, >>>
        // 为逻辑右移，将符号位一起右移,此处未发现两种符号有何不同
        char c1 = hexDigits[bt & 0xf];// 取字节中低 4 位的数字转换
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }

    private static String[] mWeeks = null;
    private static Calendar mCalendar;

    public static String getDayOfWeek(Context context, long time) {
        if (mWeeks == null) {
            mWeeks = context.getResources().getStringArray(R.array.main_weeks);
        }

        if (mCalendar == null) {
            mCalendar = Calendar.getInstance();
        }
        mCalendar.setTimeInMillis(time);
        return mWeeks[mCalendar.get(Calendar.DAY_OF_WEEK) - 1];
    }

    public static final int START_TAG = 1;

    private static final String SECRET = "abcdef";

    private static final String SIGN = "&sign=";

    /**
     * 注意此Map为TreeMap
     *
     * @param parameters
     * @return
     * @author jrjin
     * @time 2014-10-14 下午4:26:48
     */
    public static String getSignKey(Map<String, String> parameters) {
        StringBuilder builder = new StringBuilder();
        builder.append(SECRET);
        if (parameters != null) {
            int size = parameters.size();
            if (size > 0) {
                List<String> keys = new ArrayList<String>(parameters.keySet());
                List<String> values = new ArrayList<String>(parameters.values());
                for (int i = 0; i < size; i++) {
                    builder.append(keys.get(i));
                    builder.append(values.get(i));
                }
            }
        }
        builder.append(SECRET);
        HcLog.D("HcUtil getSignKey = " + builder.toString());
        return SIGN + getMD5String(builder.toString());
    }

    public static boolean isEmpty(String str) {
        if ("".equals(str) || str == null)
            return true;
        return false;
    }

    private static String mDeviceId = null;

    public static String getIMEI(Context context) {
        if (null == mDeviceId) {
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            mDeviceId = tm.getDeviceId();
        }
        HcLog.D(" getIMEI device id = " + mDeviceId);
        return mDeviceId;
    }

    private static int mWidth = -1;

    private static int mHeight = -1;

    private static float mDensity = 2.0f;

    public static void setScreenWidth(int width) {
        mWidth = width;
    }

    public static void setScreenHeight(int height) {
        mHeight = height;
    }

    public static int getScreenWidth() {
        return mWidth;
    }

    public static int getScreenHeight() {
        return mHeight;
    }

    public static void setScreenDensity(float density) {
        mDensity = density;
    }

    public static float getScreenDensity() {
        return mDensity;
    }

    /**
     * @author jrjin
     * @date 2016-1-28 上午10:28:14
     * <p/>
     * private static boolean canRelease = false;
     * <p/>
     * public static void setReleaseable(boolean release) {
     * canRelease = release;
     * }
     * <p/>
     * public static boolean getRealeaseable() {
     * return canRelease;
     * }
     */
    public static boolean hasValue(JSONObject object, String tag) {
        boolean exist = false;
        if (object != null && object.has(tag)) {
            try {
                Object object2 = object.get(tag);
                // LOG.D(TAG + " object2 = "+object2 + " tag = "+tag);
                if (object2 != null && !object2.equals("")
                        && !object.isNull(tag)) {
                    exist = true;
                }
            } catch (Exception e) {
                // TODO: handle exception
            }

        }
        return exist;
    }

    public static boolean isTimeZoneNow(int startHour, int startMinute,
                                        int startSecond, int endHour, int endMinute, int endSecond) {
        Calendar now = Calendar.getInstance();

        Calendar min = Calendar.getInstance();
        min.set(Calendar.HOUR_OF_DAY, startHour);
        min.set(Calendar.MINUTE, startMinute);

        Calendar max = Calendar.getInstance();
        max.set(Calendar.HOUR_OF_DAY, endHour);
        max.set(Calendar.MINUTE, endMinute);

        if (now.getTimeInMillis() > min.getTimeInMillis()
                && now.getTimeInMillis() < max.getTimeInMillis()) {
            return true;
        }
        return false;
    }

    public static boolean isTimeZonePoint(int startHour, int startMinute,
                                          int startSecond, int endHour, int endMinute, int endSecond,
                                          String pointTime) {

        if (pointTime == null)
            return false;

        String pointDate = "2015-11-11" + " " + pointTime;

        Date point = getDate(pointDate, FORMAT_POLLUTION);

        Calendar min = Calendar.getInstance();
        min.set(2015, 11, 11, startHour, startMinute, startSecond);

        Calendar max = Calendar.getInstance();
        max.set(2015, 11, 11, endHour, endMinute, endSecond);

        if (point.getTime() > min.getTimeInMillis()
                && point.getTime() < max.getTimeInMillis()) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否是内网
     */
    private static boolean mIntraneted = false;

    /**
     * 判断是否使用内网
     *
     * @param context
     * @return
     * @author jrjin
     * @time 2015-8-6 下午2:57:47
     */
    public static boolean isIntranet(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (null != info && info.isAvailable()) {
            String name = info.getTypeName();
            int type = info.getType();
            HcLog.D("HcUtil#isIntranet network name = " + name + " type = "
                    + type);
            switch (type) {
                case ConnectivityManager.TYPE_WIFI:
                    // PING 内网看能不能通
                    try {
                        String ip = HcConfig.getConfig().getIntranetServerURL();
                        if (ip.startsWith("http://")) {
                            ip = ip.replace("http://", "");
                        } else if (ip.startsWith("https://")) {
                            ip = ip.replace("https://", "");
                        }
                        HcLog.D("HcUtil# isIntranet intranet = " + ip);
                        Process p = Runtime.getRuntime().exec(
                                "ping -c 1 -w 1 "
                                        + ip);

                        // InputStream input = p.getInputStream();
                        //
                        // BufferedReader in = new BufferedReader(new
                        // InputStreamReader(input));
                        //
                        // StringBuffer stringBuffer = new StringBuffer();
                        //
                        // String content = "";
                        //
                        // while ((content = in.readLine()) != null) {
                        //
                        // stringBuffer.append(content);
                        // HcLog.D("HcUtil# isIntranet content = "+content);
                        //
                        // }

                        int status = p.waitFor(); // 这里有可能会等待很久
                        HcLog.D("HcUtil# isIntranet status = " + status);
                        if (status == 0) {
                            mIntraneted = true;
                            return mIntraneted;
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                        HcLog.D("HcUtil# isIntranet error = " + e);
                    }

                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    break;
                case ConnectivityManager.TYPE_ETHERNET:
                    break;

                default:
                    break;
            }

        }
        mIntraneted = false;
        return mIntraneted;
    }

    /**
     * 是否为外网
     *
     * @return
     * @author jrjin
     * @time 2015-8-10 上午9:51:55
     */
    public static boolean intraneted() {
        return mIntraneted;
    }

    /**
     * 访问url的前段部分
     *
     * @return
     * @author jrjin
     * @time 2015-8-10 上午10:40:50
     */
    public static String getScheme() {
        String port = TextUtils.isEmpty(HcConfig.getConfig().getServerPort()) ? ""
                : ":" + HcConfig.getConfig().getServerPort();
        return HcConfig.getConfig().ipMapped() ? (mIntraneted == true ?
         /*"http://" +*/ HcConfig.getConfig().getIntranetServerURL() + port
                : /*"http://" +*/ HcConfig.getConfig().getExtranetServerURL()
                + port)
                : /*"http://" +*/ HcConfig.getConfig().getExtranetServerURL()
                + port;
    }

    public static final boolean CHANDED = true;

    /**
     * 现在资源的url返回的都是映射地址(外网地址)，所以当在内网时，需要更改 掉外网地址，变成内网地址。要是url返回的是内网地址，则相反
     * <P>
     * </p>
     * 修改内网地址 used in {@link MenuWebPage#onResume()}
     * {@link DownloadPDFActivity#onCreate(Bundle)}
     *
     * @param url
     * @return
     * @author jrjin
     * @time 2015-8-10 上午11:23:17
     */
    public static final String mappedUrl(String url) {
        if (TextUtils.isEmpty(url))
            return "";
        if (INT_TO_EXT)
            return intToExt(url);
        else {
            return extToInt(url);
        }
    }

    /**
     * 当前资源url服务器返回的地址，是内网地址true，否则false
     */
    private static final boolean INT_TO_EXT = false;

    /**
     * 内网转外网
     *
     * @param url
     * @return
     * @author jrjin
     * @time 2015-9-28 上午10:27:02
     */
    private static final String intToExt(String url) {
        if (HcConfig.getConfig().ipMapped() && !mIntraneted
                && url.contains(HcConfig.getConfig().getIntranetServerURL())) {
            url = url.replace(HcConfig.getConfig().getIntranetServerURL(),
                    HcConfig.getConfig().getExtranetServerURL());
        }
        return url;
    }

    /**
     * 外网转内网
     *
     * @param url
     * @return
     * @author jrjin
     * @time 2015-9-28 上午10:27:20
     */
    private static final String extToInt(String url) {
        if (HcConfig.getConfig().ipMapped() && mIntraneted
                && url.contains(HcConfig.getConfig().getExtranetServerURL())) {
            url = url.replace(HcConfig.getConfig().getExtranetServerURL(),
                    HcConfig.getConfig().getIntranetServerURL());
        }
        return url;
    }

    public static boolean isGPS(Context context) {
        LocationManager lm = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static void openGPS(Activity context) {
        if (!isGPS(context)) {
            // Intent gpsIntent = new Intent();
            // gpsIntent.setClassName("com.android.settings",
            // "com.android.settings.widget.SettingsAppWidgetProvider");
            // gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
            // gpsIntent.setData(Uri.parse("custom:3"));
            // try {
            // PendingIntent.getBroadcast(context, 0, gpsIntent, 0).send();
            // } catch (CanceledException e) {
            // e.printStackTrace();
            // }
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            context.startActivityForResult(intent, 0);
        }

    }

    public static void hidDay(DatePicker mDatePicker) {
        Field[] datePickerfFields = mDatePicker.getClass().getDeclaredFields();
        for (Field datePickerField : datePickerfFields) {
            if ("mDaySpinner".equals(datePickerField.getName())) {
                datePickerField.setAccessible(true);
                Object dayPicker = new Object();
                try {
                    dayPicker = datePickerField.get(mDatePicker);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
                // datePicker.getCalendarView().setVisibility(View.GONE);
                ((View) dayPicker).setVisibility(View.GONE);
            }
        }
    }

    public static void highlight(TextView tv, String key) {
        int index = -1;
        String str = tv.getText().toString().trim();
        index = str.indexOf(key);
        if (index != -1) {
            SpannableStringBuilder style = new SpannableStringBuilder(str);
            style.setSpan(new ForegroundColorSpan(Color.RED), index, index
                    + key.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.setText(style);
        }
    }

    public static int getPackageVersion(Context context) {
        int result = -1;
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
            if (info != null) {
                result = info.versionCode;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String getApplicationName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            return (String) packageManager.getApplicationLabel(applicationInfo);
        } catch (NameNotFoundException e) {
            HcLog.D("HcUtil#getApplicationName PackageManager.NameNotFoundException!");
        }

        return "";

    }

    /**
     * 发送短信
     *
     * @param smsBody
     */
    public static void sendSMS(Context context, String smsBody, String tel) {
        Uri smsToUri = Uri.parse("smsto:" + tel);
        Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        intent.putExtra("sms_body", smsBody);
        context.startActivity(intent);
    }

    public static void dial(Context context, String tel) {
        Intent intent = new Intent("android.intent.action.CALL",
                Uri.parse("tel:" + tel));
        context.startActivity(intent);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static float getTextViewLength(TextView textView, String text) {
        TextPaint paint = textView.getPaint();
        float textLength = paint.measureText(text);
        return textLength;
    }

    private static DisplayImageOptions mOptions;

    public static DisplayImageOptions getImageOptions() {
        if (mOptions == null) {
            mOptions = new DisplayImageOptions.Builder()
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .showImageOnLoading(R.drawable.app_icon_data)
                    .showImageForEmptyUri(R.drawable.app_icon_data)
                    .showImageOnFail(R.drawable.app_icon_data)
                    .cacheInMemory(true).cacheOnDisk(true)
                    .considerExifParams(true)
                    .bitmapConfig(Bitmap.Config.ARGB_8888).build();
        }

        return mOptions;
    }

    private static DisplayImageOptions mAccountOptions;

    public static DisplayImageOptions getAccountImageOptions() {
        if (mAccountOptions == null) {
            mAccountOptions = new DisplayImageOptions.Builder()
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .showImageOnLoading(R.drawable.default_photo)
                    .showImageForEmptyUri(R.drawable.default_photo)
                    .showImageOnFail(R.drawable.default_photo)
                    .cacheInMemory(true).cacheOnDisk(false)
                    .considerExifParams(true)
                    .bitmapConfig(Bitmap.Config.ARGB_8888).build();
        }

        return mAccountOptions;
    }

    private static Random mRandom = new Random();

    public static int getShapColor() {
        int r = mRandom.nextInt(256);
        int g = mRandom.nextInt(256);
        int b = mRandom.nextInt(256);
        return Color.rgb(r, g, b);
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null) {
            return ni.getState() == NetworkInfo.State.CONNECTED;
        }
        return false;
    }

    public static String getGetParams(List<NameValuePair> pairs)
            throws Exception {
        String urlParams = "";
        for (NameValuePair nv : pairs) {
            if (urlParams.equals("")) {
                urlParams += "?";
            } else {
                urlParams += "&";
            }
            String pname = nv.getName();
            String pvalue = nv.getValue();
            if (pname != null && pname.length() > 0) {
                if (pvalue != null && pvalue.length() > 0) {
                    urlParams += (pname + "=" + URLEncoder.encode(pvalue,
                            "UTF-8"));
                } else {
                    urlParams += (pname + "=");
                }
            }
        }
        return urlParams;
    }

    public static List<NameValuePair> mapToList(Map<String, String> params) {
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                pairs.add(new BasicNameValuePair(entry.getKey(), entry
                        .getValue()));
            }
        }
        return pairs;
    }


    public static void closeSilently(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (Throwable t) {
            // do nothing
        }
    }

    public static void closeSilently(ParcelFileDescriptor c) {
        if (c == null) return;
        try {
            c.close();
        } catch (Throwable t) {
            // do nothing
        }
    }

    public static int dp2px(Context context, int dp) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()));
    }

    public static int SIGNIN_REQ = 0;

    public static int SIGNOUT_REQ = 1;

    private static int INTERVAL = 1000 * 60 * 60 * 24;

    public static void stopAutoSignAlarm(Context context) {
        Intent intent = new Intent(/*context, LoctionService.class*/context.getPackageName() + ".LoctionService");
        intent.putExtra("signFlag", SIGNIN_REQ);
        PendingIntent senderSignIn = PendingIntent.getService(context,
                SIGNIN_REQ, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        am.cancel(senderSignIn);

        intent = new Intent(/*context, LoctionService.class*/context.getPackageName() + ".LoctionService");
        intent.setPackage(context.getPackageName());
        intent.putExtra("signFlag", SIGNOUT_REQ);
        PendingIntent senderSignOut = PendingIntent.getService(context,
                SIGNOUT_REQ, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        am.cancel(senderSignOut);
    }

    public static void startAutoSignAlarm(Context context) {
        if (SignCache.getInstance().configExist(context)) {
            String workInTime = SignCache.getInstance().getWorkInTime();
            String workOutTime = SignCache.getInstance().getWorkOutTime();
            if (!TextUtils.isEmpty(workInTime) && !TextUtils.isEmpty(workOutTime)) {
                Intent intent = new Intent(/*context, LoctionService.class*/context.getPackageName() + ".LoctionService");
                intent.setPackage(context.getPackageName());
                intent.putExtra("signFlag", SIGNIN_REQ);
                PendingIntent senderSignIn = PendingIntent.getService(context,
                        SIGNIN_REQ, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager am = (AlarmManager) context
                        .getSystemService(Context.ALARM_SERVICE);

                am.setRepeating(AlarmManager.RTC_WAKEUP, getDateMills(getDate(FORMAT_POLLUTION_NEW, System.currentTimeMillis()) + " " + workInTime) - 5 * 60 * 1000,
                        INTERVAL, senderSignIn);

                intent = new Intent(/*context, LoctionService.class*/context.getPackageName() + ".LoctionService");
                intent.setPackage(context.getPackageName());
                intent.putExtra("signFlag", SIGNOUT_REQ);
                PendingIntent senderSignOut = PendingIntent.getService(context,
                        SIGNOUT_REQ, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                am.setRepeating(AlarmManager.RTC_WAKEUP, getDateMills(getDate(FORMAT_POLLUTION_NEW, System.currentTimeMillis()) + " " + workOutTime),
                        INTERVAL, senderSignOut);
            }
        }

    }


    public static void startAutoSignInAlarm(Context context) {
        if (SignCache.getInstance().configExist(context)) {
            String workInTime = SignCache.getInstance().getWorkInTime();

            if (!TextUtils.isEmpty(workInTime)) {
                Intent intent = new Intent(/*context, LoctionService.class*/context.getPackageName() + ".LoctionService");
                intent.setPackage(context.getPackageName());
                intent.putExtra("signFlag", SIGNIN_REQ);
                PendingIntent senderSignIn = PendingIntent.getService(context,
                        SIGNIN_REQ, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager am = (AlarmManager) context
                        .getSystemService(Context.ALARM_SERVICE);

                am.setRepeating(AlarmManager.RTC_WAKEUP, getDateMills(getDate(FORMAT_POLLUTION_NEW, System.currentTimeMillis()) + " " + workInTime),
                        INTERVAL, senderSignIn);

            }
        }

    }


    public static void startAutoSignOutAlarm(Context context) {
        if (SignCache.getInstance().configExist(context)) {
            String workOutTime = SignCache.getInstance().getWorkOutTime();

            if (!TextUtils.isEmpty(workOutTime)) {
                Intent intent = new Intent(/*context, LoctionService.class*/context.getPackageName() + ".LoctionService");
                intent.setPackage(context.getPackageName());
                intent.putExtra("signFlag", SIGNIN_REQ);
                PendingIntent senderSignOut = PendingIntent.getService(context,
                        SIGNOUT_REQ, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager am = (AlarmManager) context
                        .getSystemService(Context.ALARM_SERVICE);

                am.setRepeating(AlarmManager.RTC_WAKEUP, getDateMills(getDate(FORMAT_POLLUTION_NEW, System.currentTimeMillis()) + " " + workOutTime),
                        INTERVAL, senderSignOut);

            }
        }

    }

    private static TwoBtnAlterDialog alterDialog;

    /**
     * 弹出重新登录dialog
     *
     * @param context activity实例
     * @param msg     提示消息体
     */
    private static void twoBtnAlterDialog(final Context context, String msg) {
        if (alterDialog == null) {
//            cleanCache(context);
            alterDialog = TwoBtnAlterDialog.createDialog(context, msg);
            TwoBtnAlterDialog.btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startLoginActivity(context);
                    alterDialog.dismiss();
                    alterDialog = null;

                }
            });
            TwoBtnAlterDialog.btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alterDialog.dismiss();
                    alterDialog = null;
                }
            });
            alterDialog.show();
        } else {
            alterDialog.dismiss();
            alterDialog = null;
        }

    }

    /**
     * 跳转登录界面
     *
     * @param context
     */
    private static void startLoginActivity(Context context) {
        Intent login = new Intent(context, LoginActivity.class);
        login.putExtra("loginout", true);

        ((Activity) context).startActivityForResult(login, HcUtil.REQUEST_CODE_LOGIN);
        ((Activity) context).overridePendingTransition(0, 0);
    }

    /**
     * 清楚缓存
     *
     * @param context
     */
    private static void cleanCache(Context context) {
        CacheManager.getInstance().clearCaches(false);

//				SettingHelper.setSessionId(mContext, "");
        SettingHelper.setToken(context, "");
        SettingHelper.setAccount(context, "");
        SettingHelper.setIcon(context, "");
        SettingHelper.setMobile(context, "");
        SettingHelper.setName(context, "");
//        BadgeCache.getInstance().createBadge(context);
        SettingHelper.setUserId(context, "");
    }

    /**
     * 1.清除缓存，包括帐号信息
     * 2.解析出现在的权限列表appIds
     * 3.保存appIds并且更新appIds
     * 4.弹出登录的对话框
     *
     * @param data    json格式的数据,在body体里面
     * @param context
     */
    public static void reLogining(String data, Context context, String msg) {
        cleanCache(context);
        String appIds = "";
        try {
            JSONObject object = new JSONObject(data);
            if (hasValue(object, "appIds")) {
                appIds = object.getString("appIds");
            }
        } catch (Exception e) {

        }
        SettingHelper.setOperatePermisstion(context, appIds);
        HcConfig.getConfig().updatePermisstion(context, false);
        twoBtnAlterDialog(context, msg);
        // 连接IM,这里需要判断,通过广播告知IM模块
        Intent intent = new Intent("com.android.hcframe.logout");
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent);
    }

    /**
     * 1.清除缓存，包括帐号信息
     * 2.解析出现在的权限列表appIds
     * 3.保存appIds并且更新appIds
     *
     * @param data    json格式的数据,在body体里面
     * @param context
     */
    public static void cleanBadgeCacheInfo(String data, Context context) {
        cleanCache(context);
        String appIds = "";
        try {
            JSONObject object = new JSONObject(data);
            if (hasValue(object, "appIds")) {
                appIds = object.getString("appIds");
            }
        } catch (Exception e) {

        }
        SettingHelper.setOperatePermisstion(context, appIds);
        HcConfig.getConfig().updatePermisstion(context, false);
    }

    /**
     * 根据路径获得突破并压缩返回bitmap用于显示
     *
     * @param filePath
     * @return
     */
    public static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 720, 1280);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 计算图片的缩放值
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        HcLog.D("HcUtil #calculateInSampleSize = " + inSampleSize);
        return inSampleSize;
    }

    /**
     * 根据路径获得突破并压缩返回bitmap用于显示
     *
     * @param filePath
     * @return
     */
    public static Bitmap getSmallBitmap(String filePath, int width, int height) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, width, height);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 获取sd卡的根目录，如果不存在就获取项目的根目录
     */
    public static String getSDPatha() {
        File sdDir = null;
        String path = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED); //判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
            path = sdDir.toString();
        } else {
            //@return /data/data/com.xxx.xxx/files
            path = HcApplication.getContext().getFilesDir().getAbsolutePath();

        }
        return path;
    }

    /**
     * 根据文件返回文件MD5值
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static String getFileMD5String(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        FileChannel ch = in.getChannel();
        MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0,
                file.length());
        messagedigest.update(byteBuffer);
        return bufferToHex(messagedigest.digest());
    }

    /**
     * 获取单个文件的MD5值！
     *
     * @param file
     * @return
     */

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

    /**
     * 获取当前日期
     *
     * @return 当前日期
     */
    public static String getDateFile() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String time = formatter.format(curDate);
        return time;
    }

    public static String getHeaderUri(String userId) {
        return HcUtil.getScheme() + "/terminalServer/szf/getEmpIcon?user_id=" + userId +
                "&clientId=" + HcConfig.getConfig().getClientId() + "&token=-1";
    }

    public static boolean canStorage(Context context) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            showToast(context, R.string.insert_sd_card);
        } else {
            File file = Environment.getExternalStorageDirectory();
            StatFs fs = new StatFs(file.getAbsolutePath());
            // keep one free block
            if (Build.VERSION.SDK_INT >= 18) {
                if (fs.getAvailableBlocksLong() > 1) {
                    return true;
                } else {
                    showToast(context, R.string.storage_is_full);
                }
            } else {
                if (fs.getAvailableBlocks() > 1) {
                    return true;
                } else {
                    showToast(context, R.string.storage_is_full);
                }
            }
        }
        return false;
    }

    /**
     * 缓存wifi开关按钮的状态
     */
    // 存储sharedpreferences
    public static void setSharedPreference(Context context, boolean switchFlag) {
        SharedPreferences mSwitchShared = context.getSharedPreferences("Netdisc", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSwitchShared.edit();
        editor.putBoolean("switchFlag", switchFlag);
        editor.commit();// 提交修改
    }

    /**
     * 获取wifi开关按钮的状态
     *
     * @param context
     * @return
     */
    // 获得sharedpreferences的数据
    public static Boolean getSharePreference(Context context) {
        SharedPreferences mSwitchShared = context.getSharedPreferences("Netdisc", Context.MODE_PRIVATE);
        Boolean switchFlag = mSwitchShared.getBoolean("switchFlag", true);//默认返回true
        return switchFlag;
    }

    /**
     * 获取部门或者群组的默认图标
     * @return
     */
    public static int getDepResId() {
        return R.drawable.frame_dep_icon;
    }

    /*
     * Compute the sample size as a function of minSideLength
     * and maxNumOfPixels.
     * minSideLength is used to specify that minimal width or height of a
     * bitmap.
     * maxNumOfPixels is used to specify the maximal size in pixels that is
     * tolerable in terms of memory usage.
     *
     * The function returns a sample size based on the constraints.
     * Both size and minSideLength can be passed in as IImage.UNCONSTRAINED,
     * which indicates no care of the corresponding constraint.
     * The functions prefers returning a sample size that
     * generates a smaller bitmap, unless minSideLength = IImage.UNCONSTRAINED.
     *
     * Also, the function rounds up the sample size to a power of 2 or multiple
     * of 8 because BitmapFactory only honors sample size this way.
     * For example, BitmapFactory downsamples an image by 2 even though the
     * request is 3. So we round up the sample size to avoid OOM.
     */
    public static int computeSampleSize(BitmapFactory.Options options,
                                        int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static final int UNCONSTRAINED = -1;

    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 :
                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == UNCONSTRAINED) ? 128 :
                (int) Math.min(Math.floor(w / minSideLength),
                        Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == UNCONSTRAINED) &&
                (minSideLength == UNCONSTRAINED)) {
            return 1;
        } else if (minSideLength == UNCONSTRAINED) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    /**
     * 根据路径获得突破并压缩返回bitmap用于显示
     * 图片占内存大小: width * height * 4 * 屏幕密度
     0,  // Unknown
     1,  // Alpha_8
     2,  // RGB_565
     2,  // ARGB_4444
     4,  // RGBA_8888 一个像素占用 4byte;
     4,  // BGRA_8888
     1,  // kIndex_8
     * @param filePath
     * @return
     */
    public static Bitmap makeBitmap(String filePath, int minSideLength, int maxNumOfPixels) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = computeSampleSize(options, minSideLength, maxNumOfPixels);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }

    public static int hashCode(Object... objects) {
        return Arrays.hashCode(objects);
    }

    public static <T> T checkNotNull(T reference) {
        if(reference == null) {
            throw new NullPointerException();
        } else {
            return reference;
        }
    }

    public static boolean equal(Object a, Object b) {
        return a == b || a != null && a.equals(b);
    }

    public static void checkState(boolean expression) {
        if(!expression) {
            throw new IllegalStateException();
        }
    }

    public static void checkArgument(boolean expression, Object errorMessage) {
        if(!expression) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }

    public static void checkArgument(boolean expression) {
        if(!expression) {
            throw new IllegalArgumentException();
        }
    }

    public static void checkState(boolean expression, Object errorMessage) {
        if(!expression) {
            throw new IllegalStateException(String.valueOf(errorMessage));
        }
    }

    public static <E> ArrayList<E> newArrayList(E... elements) {
        checkNotNull(elements);
        int capacity = computeArrayListCapacity(elements.length);
        ArrayList list = new ArrayList(capacity);
        Collections.addAll(list, elements);
        return list;
    }

    static int computeArrayListCapacity(int arraySize) {
        checkNonnegative(arraySize, "arraySize");
        return saturatedCast(5L + (long)arraySize + (long)(arraySize / 10));
    }

    static int checkNonnegative(int value, String name) {
        if(value < 0) {
            String var2 = String.valueOf(String.valueOf(name));
            throw new IllegalArgumentException((new StringBuilder(40 + var2.length())).append(var2).append(" cannot be negative but was: ").append(value).toString());
        } else {
            return value;
        }
    }

    public static int saturatedCast(long value) {
        return value > 2147483647L?2147483647:(value < -2147483648L?-2147483648:(int)value);
    }

    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap();
    }

    /**
     * 系统错误,服务请求返回400，500&
     *
     * @param context
     * @param code 错误码
     * @author jrjin
     * @time 2017-03-08 下午7:50:49
     */
    public static void toastSystemError(Context context, int code) {
        if (null == context)
            return;
        showToast(
                context,
                context.getResources().getString(
                        R.string.toast_system_error) + code);
    }

    /**
     * 系统错误,服务请求返回400，500&
     *
     * @param context
     * @param code 错误码
     * @author jrjin
     * @time 2017-03-08 下午7:50:49
     */
    public static void toastSystemError(Context context, Object code) {
        if (null == context)
            return;
        if (code != null && code instanceof Integer) {
            int c = (Integer) code;
            toastSystemError(context, c);
        } else {
            toastSystemError(context);
        }
    }

    /**
     * The {@code fragment} is added to the container view with id {@code frameId}. The operation is
     * performed by the {@code fragmentManager}.
     *
     */
    public static void addFragmentToActivity (@NonNull FragmentManager fragmentManager,
                                              @NonNull Fragment fragment, int frameId) {
        checkNotNull(fragmentManager);
        checkNotNull(fragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(frameId, fragment);
        transaction.commit();
    }

    public static <T> T checkNotNull(T reference, String description) {
        if(reference == null) {
            throw new NullPointerException(description);
        } else {
            return reference;
        }
    }
}
