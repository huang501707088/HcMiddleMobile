/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2013-11-26 下午1:21:43
 */
package com.android.hcframe.sql;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.push.PushModuleItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class SettingHelper {

    private static final String SETTING_PREFERENCES = "hcframe_setting";

    /**
     * 用户帐号
     */
    private static final String PREFERENCE_KEY_ACCOUNT = "account";
    /**
     * 用户名,相当于昵称
     */
    private static final String PREFERENCE_KEY_NAME = "name";
    /**
     * 用户绑定手机号
     */
    private static final String PREFERENCE_KEY_MOBILE = "mobile_phone";
    /**
     * 用户头像Url
     */
    private static final String PREFERENCE_KEY_ICON = "user_icon";

    /**
     * http header 中的数据 登录成功后的用户ID
     */
    private static final String PREFERENCE_KEY_SESSION = "sessionid";

    /**
     * 在同个手机登录过的用户名
     */
    private static final String PREFERENCE_KEY_USERS = "users";
    /**
     * 手势密码
     */
    private static final String PREFERENCE_KEY_GESTURE_PW = "gesture_pw";
    /**
     * 已经下载的APP信息
     */
    private static final String PREFERENCE_KEY_DOWNLOAD_APPS = "download_apps";

    /**
     * 当前登录返回token值
     */
    private static final String PREFERENCE_KEY_TOKEN = "sessiontoken";

    private static final String PREFERENCE_KEY_VPN_ACCOUNT = "vpnaccount";

    private static final String PREFERENCE_KEY_VPN_PWD = "vpnpwd";

    /**
     * 自动签到
     */
    private static final String PREFERENCE_KEY_AUTO_SIGNIN = "auto_signin";

    private static final String PREFERENCE_KEY_SEARCH_DOC = "search_doc";
    /**
     * 模块去服务端检测的更新时间
     */
    private static final String PREFERENCE_KEY_CHECK_TIME = "check_time";

    /**
     * 保存推送的CHANNEL_ID
     */
    private static final String PREFERENCE_KEY_CHANNEL_ID = "channel_id";

    /**
     * 保存当前推送绑定的版本，用于版本升级的时候,重新绑定推送用户
     */
    private static final String PREFERENCE_KEY_BIND_VERSION = "bind_version";

    /**
     * 保存哪些版本已经使用过引导页
     */
    private static final String PREFERENCE_KEY_HELP_VERSION = "bind_version";

    /**
     * 保存修改推送模块选项
     */
    private static final String PREFERENCE_KEY_PUSH_SUB_VERSION = "push_sub_modules";

    /**
     * 判断百度推送是否绑定设备成功
     */
    private static final String PREFERENCE_KEY_BAIDU_BIND_DEVICE = "bind_device";

    /**
     * 注册用户时的验证码倒计时
     */
    private static final String PREFERENCE_KEY_REGISTER_CODE = "register_code";

    /**
     * 签到考勤的信息 json格式,
     * 包括经纬度、上下班时间和范围
     */
    private static final String PREFERENCE_KEY_SIGN_INFO = "sign_info";

    /**
     * 打卡时间
     */
    private static final String PREFERENCE_KEY_TODAY_TIME_SIGNIN = "signInTime";
    private static final String PREFERENCE_KEY_TODAY_TIME_SIGNOUT = "signOutTime";


    /**
     * 年会信息
     */
    private static final String PREFERENCE_KEY_ANNUAL_INFO = "annual_info";

    /**
     * 是否自动登录
     */
    private static final String PREFERENCE_KEY_LOGIN_AUTO = "login_auto";

    /**
     * 用户操作权限列表
     */
    private static final String PREFERENCE_KEY_OPERATE_PERMISSION = "operate_permission";
    /**
     * 角标去服务端检测的更新时间
     */
    private static final String PREFERENCE_KEY_BADGE_TIME = "badge_time";

    /**
     * 保存IM的登录密码
     */
    private static final String PREFERENCE_KEY_IM_PW = "password";

    /**
     * 保存软件盘高度
     */
    private static final String PREFERENCE_KEY_KEYBOARD = "keyboard";

    private static final String PREFERENCE_KEY_DEX2_SHA1 = "dex2-SHA1-Digest";

    /**
     *@author jinjr
     *@date 17-3-16 上午9:16
     * 保存用户邮箱 */
    private static final String PREFERENCE_KEY_EMAIL = "email";

    private SettingHelper() {}

    /**
     * 001;1.0.0&002;2.0.0
     *
     * @param context
     * @param app
     * @author jrjin
     * @time 2015-5-4 下午2:11:06
     */
    public static void setDownloadAppInfo(Context context, String app) {
        String apps = getDownloadAppInfo(context);
        if (TextUtils.isEmpty(apps))
            apps = app;
        else {
            StringBuilder builder = new StringBuilder(apps);
            builder.append("&");
            builder.append(app);
            apps = builder.toString();
        }
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_DOWNLOAD_APPS, apps);
        editor.commit();
    }

    public static String getDownloadAppInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String info = sp.getString(PREFERENCE_KEY_DOWNLOAD_APPS, "");
        return info;
    }

    public static void setGesturePw(Context context, String pw) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_GESTURE_PW, pw);
        editor.commit();
    }

    public static String getGesturePw(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String pw = sp.getString(PREFERENCE_KEY_GESTURE_PW, "");
        return pw;
    }

    public static void setLoginUsers(Context context, String user) {
        String users = getLoginUsers(context);
        if (TextUtils.isEmpty(users))
            users = user;
        else {
            String[] userList = users.split("&");
            StringBuilder builder = new StringBuilder(user + "&");
            for (String string : userList) {
                if (!string.equals(user)) { // 去除当前的用户
                    builder.append(string + "&");
                }
            }


//			if (users.contains(user + "&")) {
//				users = users.replaceAll(user + "&", "");
//			} else if (users.contains(user)) { // 说明再最后，或者只有一个
//				users = users.replaceAll(user, "");
//			}
//			StringBuilder builder = new StringBuilder(/* users */);
//			// builder.append("&");
//			builder.append(user);
//
//			if (!TextUtils.isEmpty(users)) {
//				builder.append("&");
//				builder.append(users);
//			}

            users = builder.toString();
            users = users.substring(0, users.length() - 1);

        }
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_USERS, users);
        editor.commit();
    }

    public static String getLoginUsers(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String users = sp.getString(PREFERENCE_KEY_USERS, "");
        return users;
    }

    public static String deleteUser(Context context, String user) {
        String users = getLoginUsers(context);
        if (TextUtils.isEmpty(users)) {
            return null;
        } else {
            String[] userList = users.split("&");
            StringBuilder builder = new StringBuilder();
            for (String string : userList) {
                if (!string.equals(user)) { // 去除当前的用户
                    builder.append(string + "&");
                }
            }

            users = builder.toString();
//			if (users.contains(user + "&")) {
//				users = users.replaceAll(user + "&", "");
//			} else if (users.contains(user)) { // 说明再最后，或者只有一个
//				users = users.replaceAll(user, "");
//			}
        }
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_USERS, users);
        editor.commit();

        return users;
    }

    /**
     * 设置用户登录后的唯一的UserId
     * @param context
     * @param userId
     */
    public static void setUserId(Context context, String userId) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_SESSION, userId);
        editor.commit();
    }

    public static String getUserId(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String id = sp.getString(PREFERENCE_KEY_SESSION, "");
        return id;
    }

    public static void setAccount(Context context, String account) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_ACCOUNT, account);
        editor.commit();
    }

    public static void setName(Context context, String name) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_NAME, name);
        editor.commit();
    }

    public static void setMobile(Context context, String mobile_phone) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_MOBILE, mobile_phone);
        editor.commit();
    }

    public static void setIcon(Context context, String icon) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_ICON, icon);
        editor.commit();
    }

    public static String getAccount(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String account = sp.getString(PREFERENCE_KEY_ACCOUNT, "");
        return account;
    }

    public static String getName(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String name = sp.getString(PREFERENCE_KEY_NAME, "");
        return name;
    }

    public static String getMobile(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String mobile = sp.getString(PREFERENCE_KEY_MOBILE, "");
        return mobile;
    }

    public static String getIcon(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String icon = sp.getString(PREFERENCE_KEY_ICON, "");
        return icon;
    }

    /**
     * 记录登录成功时的token值，在HTTP请求的头中会用到。
     *
     * @param context
     * @param token
     */
    public static void setToken(Context context, String token) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_TOKEN, token);
        editor.commit();
    }

    /**
     * 获取当前登录帐号的token值
     *
     * @param context
     * @return
     */
    public static String getToken(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String token = sp.getString(PREFERENCE_KEY_TOKEN, "");
        return token;
    }

    public static void setVpnAccount(Context context, String vpnaccount) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_VPN_ACCOUNT, vpnaccount);
        editor.commit();
    }

    public static void setVpnPwd(Context context, String vpnpwd) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_VPN_PWD, vpnpwd);
        editor.commit();
    }

    public static String getVpnAccount(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String account = sp.getString(PREFERENCE_KEY_VPN_ACCOUNT, "");
        return account;
    }

    public static String getVpnPwd(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String account = sp.getString(PREFERENCE_KEY_VPN_PWD, "");
        return account;
    }

    /**
     * 设置是否自动签到
     *
     * @param context
     * @param autoSignin
     * @author jrjin
     * @time 2016-1-4 下午3:14:20
     */
    public static void setAutoSignin(Context context, boolean autoSignin) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(PREFERENCE_KEY_AUTO_SIGNIN, autoSignin);
        editor.commit();
    }

    /**
     * 获取是否为自动签到
     *
     * @param context
     * @return
     * @author jrjin
     * @time 2016-1-4 下午3:14:24
     */
    public static boolean getAutoSignin(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        boolean account = sp.getBoolean(PREFERENCE_KEY_AUTO_SIGNIN, false);
        return account;
    }

    /**
     * 存储搜索过的key，用';'隔开
     *
     * @param context
     * @param key
     * @author jrjin
     * @time 2015-8-31 上午11:06:07
     */
    public static void setSearchKey(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_SEARCH_DOC, key);
        editor.commit();
    }

    public static String getSearchKey(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String key = sp.getString(PREFERENCE_KEY_SEARCH_DOC, "");
        return key;
    }

    /**
     * 保存模块的更新时间,需要根据帐号
     * <p>
     * </p>
     * moduleId;time&moduleId2;time2
     * account:moduleId__time;moduleId__time&account:moduleId__time;moduleId__time
     *
     * @param context
     * @param moduleId   模块ID
     * @param updateTime 模块最新更新时间
     * @author jrjin
     * @time 2015-10-22 下午4:05:40
     * @deprecated 替换为 {@link #setModuleTime(Context, String, String, boolean)}
     */
    public static void setModuleTime(Context context, String moduleId,
                                     String updateTime) {
        HcLog.D("SettingHelper#setModuleTime moduleId=" + moduleId + " updateTime=" + updateTime);
        if (TextUtils.isEmpty(updateTime) || TextUtils.isEmpty(moduleId))
            return;
        String times = getModuleTimes(context);
        if (TextUtils.isEmpty(times)) {
            times = moduleId + ";" + updateTime;
        } else {
            StringBuilder builder = new StringBuilder();
            String[] modules = times.split("&");
            String[] time = null;
            boolean added = false;// 判断是否已经添加
            for (String module : modules) {
                time = module.split(";");
                if (time[0].equals(moduleId)) { // 更新原来的
                    builder.append(moduleId + ";" + updateTime + "&");
                    added = true;
                } else {
                    // 直接添加
                    builder.append(module + "&");
                }
            }
            if (!added) {
                builder.append(moduleId + ";" + updateTime + "&");
            }

//			HcLog.D("SettingHelper#setModuleTime before changed times = "+times);
            // 去除最后一个"&"
            times = builder.toString();
//			HcLog.D("SettingHelper#setModuleTime after changed times = "+times);
            times = times.substring(0, times.length() - 1);
//			HcLog.D("SettingHelper#setModuleTime before changed2 times = "+times);
        }

        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_CHECK_TIME, times);
        editor.commit();
    }

    /**
     * 获取对应模块的更新时间
     *
     * @param context
     * @param moduleId 模块的ID
     * @return 模块更新时间
     * @author jrjin
     * @time 2015-10-22 下午3:45:13
     * @deprecated 替换为 {@link #getModuleTime(Context, String, boolean)}
     */
    public static String getModuleTime(Context context, String moduleId) {
        if (TextUtils.isEmpty(moduleId))
            return "";
        String time = getModuleTimes(context);
        HcLog.D("SettingHelper#getModuleTime time = " + time);
        boolean hasTime = false;
        if (!TextUtils.isEmpty(time)) {
            String[] modules = time.split("&");
            String[] times = null;
            for (String module : modules) {
                times = module.split(";");
                if (times[0].equals(moduleId)) {
                    time = times[1];
                    hasTime = true;
                    break;
                }
            }
        }
        if (!hasTime) {
            time = "";
        }
        HcLog.D("SetingHelper getModuleTime moduleId = " + moduleId
                + " updateTime = " + time);
        return time;
    }

    /**
     * 获取全部模块及其更新时间
     * <p>
     * </p>
     * moduleId;time&moduleId2;time2更改为下面的
     * <p></p>
     * account:moduleId__time;moduleId__time&account:moduleId__time;moduleId__time
     *
     * @param context
     * @return 全部模块及其更新时间
     * @author jrjin
     * @time 2015-10-22 下午3:41:19
     */
    private static String getModuleTimes(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String times = sp.getString(PREFERENCE_KEY_CHECK_TIME, "");
        return times;
    }

    /**
     * 保存角标的更新时间,需要根据帐号
     * <p>
     * </p>
     * account;time&account2;time2
     *
     * @param context
     * @param userId     用户ID
     * @param updateTime 角标最新更新时间
     * @author czx
     * @time 2016-4-22 下午4:05:40
     */
    public static void setBadgeTime(Context context, String userId,
                                    String updateTime) {
        HcLog.D("SettingHelper#setBadgeTime moduleId=" + userId + " updateTime=" + updateTime);
        if (TextUtils.isEmpty(updateTime))
            return;
        if (TextUtils.isEmpty(userId)) {
            userId = "everyone";
        }
        String times = getBadgeTimes(context);
        if (TextUtils.isEmpty(times)) {
            times = userId + ";" + updateTime;
        } else {
            StringBuilder builder = new StringBuilder();
            String[] modules = times.split("&");
            String[] time = null;
            boolean added = false;// 判断是否已经添加
            for (String module : modules) {
                time = module.split(";");
                if (time[0].equals(userId)) { // 更新原来的
                    builder.append(userId + ";" + updateTime + "&");
                    added = true;
                } else {
                    // 直接添加
                    builder.append(module + "&");
                }
            }
            if (!added) {
                builder.append(userId + ";" + updateTime + "&");
            }

//			HcLog.D("SettingHelper#setModuleTime before changed times = "+times);
            // 去除最后一个"&"
            times = builder.toString();
//			HcLog.D("SettingHelper#setModuleTime after changed times = "+times);
            times = times.substring(0, times.length() - 1);
//			HcLog.D("SettingHelper#setModuleTime before changed2 times = "+times);
        }
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_BADGE_TIME, times);
        editor.commit();
    }

    /**
     * 获取对应模块的更新时间
     *
     * @param context
     * @param userId  用户的ID
     * @return 获取角标更新时间
     * @author czx
     * @time 2016-4-22 下午3:45:13
     */
    public static String getBadgeTime(Context context, String userId) {
        if (TextUtils.isEmpty(userId))
            userId = "everyone";
        String time = getBadgeTimes(context);
        HcLog.D("SettingHelper#getBadgeTime time = " + time);
        boolean hasTime = false;
        if (!TextUtils.isEmpty(time)) {
            String[] modules = time.split("&");
            String[] times = null;
            for (String module : modules) {
                times = module.split(";");
                if (times[0].equals(userId)) {
                    time = times[1];
                    hasTime = true;
                    break;
                }
            }
        }
        if (!hasTime) {
            time = System.currentTimeMillis() + "";
        }
        HcLog.D("SetingHelper getBadgeTime userId = " + userId
                + " updateTime = " + time);
        return time;
    }

    /**
     * 获取全部模块及其更新时间
     * <p>
     * </p>
     * account;time&account2;time2更改为下面的
     *
     * @param context
     * @return 全部模块及其更新时间
     * @author jrjin
     * @time 2015-10-22 下午3:41:19
     */
    private static String getBadgeTimes(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String times = sp.getString(PREFERENCE_KEY_BADGE_TIME, "");
        return times;
    }

    /**
     * 保存从百度推送服务器获取到的ChannelId
     *
     * @param context
     * @param channelId
     * @author jrjin
     * @time 2015-11-2 下午3:15:12
     */
    public static void setChannelId(Context context, String channelId) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_CHANNEL_ID, channelId);
        editor.commit();
    }

    /**
     * 获取从百度推送服务器获取到的ChannelId
     *
     * @param context
     * @return ChannelId
     * @author jrjin
     * @time 2015-11-2 下午3:15:54
     */
    public static String getChannelId(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String channelId = sp.getString(PREFERENCE_KEY_CHANNEL_ID, "");
        return channelId;
    }

    /**
     * 获取当前绑定的版本，用于版本升级的时候,重新绑定推送用户
     *
     * @param context
     * @return ChannelId
     * @author jrjin
     * @time 2015-11-2 下午3:15:54
     */
    public static String getBindAppVersion(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String version = sp.getString(PREFERENCE_KEY_BIND_VERSION, "");
        return version;
    }

    /**
     * 保存当前绑定的版本，用于版本升级的时候,重新绑定推送用户
     *
     * @param context
     * @param version
     * @author jrjin
     * @time 2015-11-2 下午3:15:12
     */
    public static void setBindAppVersion(Context context, String version) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_BIND_VERSION, version);
        editor.commit();
    }

    /**
     * 设置推送消息,不需要根据用户去存储
     * @param context
     * @param result
     */
    public static void setPushSubSettings(Context context, String result) {
        String userId = getUserId(context);
        if (TextUtils.isEmpty(userId)) return;
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_PUSH_SUB_VERSION, result);
        editor.commit();
    }

    /**
     * 获取推送消息的设置信息,不需要根据用户去存储
     * @param context
     * @return
     */
    public static String getPushSubSettings(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String version = sp.getString(PREFERENCE_KEY_PUSH_SUB_VERSION, "");
        return version;
    }


    /**
     * 检测当前版本是否需要出现引导页,是否需要去服务端注册设备
     * <p>
     * 各版本直接用分号隔开
     * </p>
     *
     * @param context
     * @param version 当前的版本
     * @param save    是否需要保存当前版本；去服务端注册不需要，检测引导页的时候需要保存
     * @return true:显示引导页; false:不需要
     * @author jrjin
     * @time 2015-11-6 下午12:23:41
     */
    public static boolean showGuidePage(Context context, String version,
                                        boolean save) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String versions = sp.getString(PREFERENCE_KEY_HELP_VERSION, "");
        boolean show = true;
        if (TextUtils.isEmpty(version)) {
            versions = version;
        } else {
            String[] v = versions.split(";");
            for (String string : v) {
                if (string.equals(version)) {
                    show = false;
                    break;
                }
            }
            if (show && save) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(PREFERENCE_KEY_HELP_VERSION, versions + ";"
                        + version);
                editor.commit();
            }
        }

        if (show) { // 没有注册,重新绑定
            setBaiduBindDevice(context, false);
        }

        return show;
    }

    /**
     * 设置百度ChannelId是否绑定设备成功
     *
     * @param context
     * @param binded
     * @author jrjin
     * @time 2015-12-17 上午10:02:11
     */
    public static void setBaiduBindDevice(Context context, boolean binded) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(PREFERENCE_KEY_BAIDU_BIND_DEVICE, binded);
        editor.commit();
    }

    public static boolean getBaiduBindDevice(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        boolean bind = sp.getBoolean(PREFERENCE_KEY_BAIDU_BIND_DEVICE, false);
        return bind;
    }

    /**
     * 保存注册时的验证码的倒计时
     *
     * @param context
     * @param time    倒计时剩余时间;当前时间
     * @author jrjin
     * @time 2015-12-17 下午1:54:37
     */
    public static void setRegisterCode(Context context, String time) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_REGISTER_CODE, time);
        editor.commit();
    }

    public static String getRegisterCode(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String register = sp.getString(PREFERENCE_KEY_REGISTER_CODE, "");
        return register;
    }

    /**
     * 设置当前用户的签到信息
     * account&info;account&info
     *
     * @param context
     * @param sign
     * @author jrjin
     * @time 2016-1-5 下午2:27:47
     */
    public static void setSignInfo(Context context, String sign) {
        String account = getAccount(context);
        if (TextUtils.isEmpty(account)) return;
        String signs = getSigninfos(context);
        if (TextUtils.isEmpty(signs)) {
            signs = account + "&" + sign;
        } else {
            StringBuilder builder = new StringBuilder();
            String[] modules = signs.split(";");
            String[] time = null;
            boolean added = false;
            for (String module : modules) {
                time = module.split("&");
                if (time[0].equals(account)) {
                    builder.append(account + "&" + sign + ";");
                    added = true;
                } else {
                    // 直接添加
                    builder.append(module + ";");
                }
            }
            if (!added) {
                builder.append(account + "&" + sign + ";");
            }
            // 去除最后一个";"
            signs = builder.toString();
            signs = signs.substring(0, signs.length() - 1);
        }

        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_SIGN_INFO, signs);
        editor.commit();
    }

    private static String getSigninfos(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String signs = sp.getString(PREFERENCE_KEY_SIGN_INFO, "");
        return signs;
    }

    /**
     * 获取签到的配置信息
     *
     * @param context
     * @return
     * @author jrjin
     * @time 2016-1-5 下午2:32:50
     */
    public static String getSignInfo(Context context) {
        String account = getAccount(context);
        if (TextUtils.isEmpty(account)) return "";
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String signs = sp.getString(PREFERENCE_KEY_SIGN_INFO, "");
        String[] signList = signs.split(";");
        String[] infos;
        for (String string : signList) {
            infos = string.split("&");
            if (account.equals(infos[0]) && infos.length > 1) {
                return infos[1];
            }
        }
        return "";
    }

    /**
     * 获取年会信息；
     * <p>格式: account1&&annualInfo;account2&annualInfo</p>
     *
     * @param context
     * @param annualInfo 最新年会信息,为json格式
     * @author jrjin
     * @time 2016-1-11 下午2:53:04
     */
    public static void setAnnualInfo(Context context, String annualInfo) {
        String account = getAccount(context);
        if (TextUtils.isEmpty(account)) return;
        String annualInfos = getAnnualInfos(context);
        if (TextUtils.isEmpty(annualInfos)) {
            annualInfos = account + "&&" + annualInfo;
        } else {
            StringBuilder builder = new StringBuilder();
            String[] annuals = annualInfos.split(";");
            String[] annual = null;
            boolean added = false;
            for (String info : annuals) {
                annual = info.split("&&");
                if (annual[0].equals(account)) {
                    builder.append(account + "&&" + annualInfo + ";");
                    added = true;
                } else {
                    // 直接添加
                    builder.append(info + ";");
                }
            }
            if (!added) {
                builder.append(account + "&&" + annualInfo + ";");
            }
            // 去除最后一个";"
            annualInfos = builder.toString();
            annualInfos = annualInfos.substring(0, annualInfos.length() - 1);
        }


        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_ANNUAL_INFO, annualInfos);
        editor.commit();
    }

    public static String getAnnualInfo(Context context) {
        String account = getAccount(context);
        if (TextUtils.isEmpty(account)) return "";

        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String annualInfo = sp.getString(PREFERENCE_KEY_ANNUAL_INFO, "");
        String[] annualList = annualInfo.split(";");
        String[] infos;
        for (String string : annualList) {
            infos = string.split("&&");
            if (account.equals(infos[0])) {
                return infos[1];
            }
        }
        return "";

    }

    private static String getAnnualInfos(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String annuals = sp.getString(PREFERENCE_KEY_ANNUAL_INFO, "");
        return annuals;
    }

    public static void deleteAnnualInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_ANNUAL_INFO, "");
        editor.commit();
    }

    /**
     * 获取相应key的全部信息
     *
     * @param context
     * @param key     需要查找的信息的key
     * @return
     * @author jrjin
     * @time 2016-1-15 上午9:51:36
     */
    private static String getInfos(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String info = sp.getString(key, "");
        return info;
    }

    /**
     * 查找对应searchKey的内容，格式为：
     * <p>searchKey1+searchToken+内容+token</p>
     *
     * @param context
     * @param key         在SharedPreferences中的存储的key
     * @param searchKey   查找的内容对应的key
     * @param token       内容之间的分隔符 可以为null
     * @param searchToken searchKey和内容之间的分隔符 可以为null
     * @return searchKey的内容, 不存在返回 ""
     * @author jrjin
     * @time 2016-1-15 上午9:54:51
     */
    public static String getInfo(Context context, String key, String searchKey, String token, String searchToken) {
        if (TextUtils.isEmpty(searchKey)) return "";

        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String infos = sp.getString(key, "");
        HcLog.D("SettingHelper#getInfo infos = " + infos + " token = " + token + " searchToken = " + searchToken);
        if (TextUtils.isEmpty(infos)) return "";
        String[] infoList = infos.split(TextUtils.isEmpty(token) ? ";" : token);
        String[] infoToken;
        searchToken = TextUtils.isEmpty(searchToken) ? "&" : searchToken;
        for (String string : infoList) {
            infoToken = string.split(searchToken);
            if (searchKey.equals(infoToken[0])) {
                if (infoToken.length < 2) return "";
                return infoToken[1];
            }
        }
        return "";
    }

    /**
     * 保存数据到SharedPreferences,格式为：
     * <p>searchKey1+searchToken+content1+token+searchKey2+searchToken+content2</p>
     *
     * @param context
     * @param key         在SharedPreferences中的存储的key
     * @param searchKey   content对应的key
     * @param content     需要保存的内容
     * @param token       content之间的分隔符 可以为null
     * @param searchToken searchKey和content之间的分隔符 可以为null
     * @return 当前Key的信息
     * @author jrjin
     * @time 2016-1-15 上午10:07:13
     */
    public static String setInfo(Context context, String key, String searchKey, String content, String token, String searchToken) {
        if (TextUtils.isEmpty(searchKey)) return "";
        String infos = getInfos(context, key);
        token = TextUtils.isEmpty(token) ? ";" : token;
        searchToken = TextUtils.isEmpty(searchToken) ? "&" : searchToken;
        if (TextUtils.isEmpty(infos)) {
            infos = searchKey + searchToken + content;
        } else {
            StringBuilder builder = new StringBuilder();
            String[] infoList = infos.split(token);
            String[] infoToken = null;
            boolean added = false;
            for (String info : infoList) {
                infoToken = info.split(searchToken);
                if (infoToken[0].equals(searchKey)) {
                    builder.append(searchKey + searchToken + content + token);
                    added = true;
                } else {
                    // 直接添加
                    builder.append(info + token);
                }
            }
            if (!added) {
                builder.append(searchKey + searchToken + content + token);
            }
            // 去除最后一个";"
            infos = builder.toString();
            infos = infos.substring(0, infos.length() - /*1*/token.length());
        }


        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, infos);
        editor.commit();
        return infos;
    }

    public static void setLoginAuto(Context context, boolean auto) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(PREFERENCE_KEY_LOGIN_AUTO, auto);
        editor.commit();
    }

    public static boolean getLoginAuto(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        boolean auto = sp.getBoolean(PREFERENCE_KEY_LOGIN_AUTO, true);
        return auto;
    }

    public static void clearData(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
    }

    /**
     * 保存当前用户的权限,格式：用户1:appId1;appId2&用户2:appId1;appId2
     *
     * @param context
     * @param permisstion 当前用户可以操作应用的权限列表 appId1;appId2;appId3
     * @author jinjr
     * @date 16-3-18 上午11:32
     */
    public static void setOperatePermisstion(Context context, String permisstion) {
        String key = getAccount(context);
        key = TextUtils.isEmpty(key) ? "anyone" : key;
        setInfo(context, PREFERENCE_KEY_OPERATE_PERMISSION,
                key, permisstion, "&", ":");
    }

    /**
     * 获取当前用户的权限列表
     *
     * @param context
     * @return 用户权限列表, 格式:appId1;appId2;appId3
     */
    public static String getOperatePermisstion(Context context) {
        String key = getAccount(context);
        key = TextUtils.isEmpty(key) ? "anyone" : key;
        return getInfo(context, PREFERENCE_KEY_OPERATE_PERMISSION, key, "&", ":");
    }

    /**
     * 获取对应模块的更新时间
     * <p></p>
     * 格式：  account:moduleId__time;moduleId__time&account:moduleId__time;moduleId__time
     *
     * @param context
     * @param moduleId 模块的ID
     * @param anyone   是否要区分用户
     * @return 模块更新时间
     * @author jrjin
     * @time 2015-10-22 下午3:45:13
     */
    public static String getModuleTime(Context context, String moduleId, boolean anyone) {
        if (TextUtils.isEmpty(moduleId))
            return "";
        String time = getModuleTimes(context);
        HcLog.D("SettingHelper#getModuleTime time = " + time);
        String account = getAccount(context);
        account = (TextUtils.isEmpty(account) || anyone == true) ? "anyone" : account;
        time = getValue(account, time, "&", ":");
        HcLog.D("SettingHelper#getModuleTime after by account time = " + time);
        time = getValue(moduleId, time, ";", "__");
        HcLog.D("SetingHelper getModuleTime moduleId = " + moduleId
                + " updateTime = " + time);
        return time;
    }

    /**
     * 保存模块的更新时间,需要根据帐号
     * <p></p>
     * account:moduleId__time;moduleId__time&account:moduleId__time;moduleId__time
     *
     * @param context
     * @param moduleId   模块ID
     * @param updateTime 模块最新更新时间
     * @author jrjin
     * @time 2015-10-22 下午4:05:40
     */
    public static void setModuleTime(Context context, String moduleId,
                                     String updateTime, boolean anyone) {
        HcLog.D("SettingHelper#setModuleTime moduleId=" + moduleId + " updateTime=" + updateTime);
        if (TextUtils.isEmpty(updateTime) || TextUtils.isEmpty(moduleId))
            return;
        String times = getModuleTimes(context);
        String account = getAccount(context);
        account = (TextUtils.isEmpty(account) || anyone == true) ? "anyone" : account;

        if (TextUtils.isEmpty(times)) {
            times = account + ":" + moduleId + "__" + updateTime;
        } else {
            StringBuilder builder = new StringBuilder();
            String[] modules = times.split("&");
            String[] time = null;
            boolean added = false;// 判断是否已经添加
            for (String module : modules) {
                time = module.split(":"); // 先根据用户
                if (time[0].equals(account)) { // 更新原来的
                    // 再来替换moduleId
//					String content = setValue(moduleId, time[1], updateTime, "__", ";");
                    String content = setValue(moduleId, time[1], updateTime, ";", "__");
                    HcLog.D("SettingHelper#setModuleTime after set content = " + content);
                    builder.append(account + ":" + content + "&");
                    added = true;
                } else {
                    // 直接添加
                    builder.append(module + "&");
                }
            }
            if (!added) {
                builder.append(account + ":" + moduleId + "__" + updateTime + "&");
            }

//			HcLog.D("SettingHelper#setModuleTime before changed times = "+times);
            // 去除最后一个"&"
            times = builder.toString();
//			HcLog.D("SettingHelper#setModuleTime after changed times = "+times);
            times = times.substring(0, times.length() - 1);
//			HcLog.D("SettingHelper#setModuleTime before changed2 times = "+times);
        }

        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_CHECK_TIME, times);
        editor.commit();
    }


    /**
     * 根据指定的key获取value
     * <p>searchKey1+searchToken+content1+token+searchKey2+searchToken+content2</p>
     *
     * @param searchKey   查找的内容对应的key
     * @param content     指定查找的范围
     * @param token       content之间的分隔符 不能为null和""
     * @param searchToken serchKey和content之间的分隔符 不能为null和""
     * @return 指定key的value
     */
    private static String getValue(String searchKey, String content, String token, String searchToken) {
        if (TextUtils.isEmpty(searchKey) || TextUtils.isEmpty(content) ||
                TextUtils.isEmpty(token) || TextUtils.isEmpty(searchToken)) return "";

        String infos = content;
        if (TextUtils.isEmpty(infos)) return "";
        String[] infoList = infos.split(token);
        String[] infoToken;
        for (String string : infoList) {
            infoToken = string.split(searchToken);
            if (searchKey.equals(infoToken[0])) {
                if (infoToken.length < 2) return "";
                return infoToken[1];
            }
        }
        return "";
    }


    /**
     * 根据指定的key设置value
     * <p>searchKey1+searchToken+content1+token+searchKey2+searchToken+content2</p>
     *
     * @param searchKey   查找的内容对应的key
     * @param oldContent  原先的内容
     * @param content     对应key的内容
     * @param token       content之间的分隔符 不能为null和""
     * @param searchToken serchKey和content之间的分隔符 不能为null和""
     * @return 指定key的value
     */
    private static String setValue(String searchKey, String oldContent, String content, String token, String searchToken) {
        if (TextUtils.isEmpty(searchKey) || TextUtils.isEmpty(content) ||
                TextUtils.isEmpty(token) || TextUtils.isEmpty(searchToken)) return "";

        String infos = oldContent;
        if (TextUtils.isEmpty(infos)) {
            infos = searchKey + searchToken + content;
        } else {
            StringBuilder builder = new StringBuilder();
            String[] infoList = infos.split(token);
            String[] infoToken = null;
            boolean added = false;
            for (String info : infoList) {
                infoToken = info.split(searchToken);
                if (infoToken[0].equals(searchKey)) {
                    builder.append(searchKey + searchToken + content + token);
                    added = true;
                } else {
                    // 直接添加
                    builder.append(info + token);
                }
            }
            if (!added) {
                builder.append(searchKey + searchToken + content + token);
            }
            // 去除最后一个";"
            infos = builder.toString();
            infos = infos.substring(0, infos.length() - /*1*/token.length());
        }
        return infos;
    }

    /**
     * 设置当天签到时间,格式为：日期_用户1=时间;用户2=时间2
     * @param context
     * @param time
     */
    public static void setSigninTime(Context context, String time) {
        setTime(context, time, PREFERENCE_KEY_TODAY_TIME_SIGNIN);
    }

    public static void setSignoutTime(Context context, String time) {
        setTime(context, time, PREFERENCE_KEY_TODAY_TIME_SIGNOUT);
    }

    /**
     * 格式为：日期_用户1=时间;用户2=时间2
     * @param context
     * @param time
     * @param key
     */
    private static void setTime(Context context, String time, String key) {
        HcLog.D("SettingHelper#setTime time = "+time);
        String account = getAccount(context);
        if (TextUtils.isEmpty(account)) return;
        String date = HcUtil.getDate(HcUtil.FORMAT_SIGN_YEAR_MONTH_DAY, System.currentTimeMillis());
        String info = getInfo(context, key, date, "&", "_");
        HcLog.D("SettingHelper#setTime today date = "+date + " info = "+info + " account = "+account);
        time = TextUtils.isEmpty(time) ? "-1" : time;

        if (TextUtils.isEmpty(info)) { // 说明没有任何记录
            setInfo(context, key, date, account + "=" + time, "&", "_");
        } else {
            String value = setValue(account, info, time, ";", "=");
            HcLog.D("SettingHelper#setTime value = "+value);

            SharedPreferences sp = context.getSharedPreferences(
                    SETTING_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(key, date + "_" + value);
            editor.commit();

//            setInfo(context, key, date, value, "&", "_");
        }
    }

    /**
     * 格式为：日期_用户1=时间;用户2=时间2
     * @param context
     * @param key
     * @return
     */
    private static String getTime(Context context, String key) {
        String account = getAccount(context);
        if (TextUtils.isEmpty(account)) return "";
        String date = HcUtil.getDate(HcUtil.FORMAT_SIGN_YEAR_MONTH_DAY, System.currentTimeMillis());
        String info = getInfo(context, key, date, "&", "_");
        HcLog.D("SettingHelper#getTime today date = " + date + " info = " + info);
        if (TextUtils.isEmpty(info)) {
            return "";
        } else {
            String value = getValue(account, info, ";", "=");
            HcLog.D("SettingHelper#getTime value = " + value + " info = " + info);
            return value.equals("-1") ? "" : value;
        }
    }

    /**
     * 设置当天签到时间,格式为：日期_用户1=时间;用户2=时间2
     * @param context
     */
    public static String getSigninTime(Context context) {
        return getTime(context, PREFERENCE_KEY_TODAY_TIME_SIGNIN);
    }

    public static String getSignoutTime(Context context) {
        return getTime(context, PREFERENCE_KEY_TODAY_TIME_SIGNOUT);
    }

    /**
     * 根据一定的规则,在为preferenceKey的SharedPreferences中存储value,value可能会替换已有的值
     * <p></p>
     * 存储的规则 key:subKey_value;subkey_value & key:subKey_value;subKey_value
     * <p></p>
     * 实例：用户： 模块_更新时间；模块_更新时间 & 用户：模块_更新时间；模块_更新时间
     * @param context
     * @param preferenceKey 存储在SharedPreferences中的key
     * @param key 外层的key
     * @param subKey 里面的key
     * @param value 需要设置的值
     * @param group 外层是否多组 true 多组“&”有效
     */
    public static void setValue(Context context, String preferenceKey, String key, String subKey, String value, boolean group) {
        HcLog.D("SettingHelper#setValue key = "+key + " subKey = "+subKey + " value = "+value);
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(subKey)) return;
        String info = getInfo(context, preferenceKey, key, "&", ":");
        HcLog.D("SettingHelper#setValue info = "+info);
        value = TextUtils.isEmpty(value) ? "-1" : value; // 这里需要测试

        if (TextUtils.isEmpty(info)) { // 说明没有任何记录
            setInfo(context, preferenceKey, key, subKey + "_" + value, "&", ":");
        } else {
            String subValue = setValue(subKey, info, value, ";", "_");
            HcLog.D("SettingHelper#setValue subValue = " + subValue);

            if (group) {
                setInfo(context, preferenceKey, key, subValue, "&", ":");
            } else {
                SharedPreferences sp = context.getSharedPreferences(
                        SETTING_PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(preferenceKey, key + ":" + value);
                editor.commit();
            }

        }
    }

    /**
     * 根据key和subKey获取存储在SharedPreferences中的preferenceKey的值
     * <p></p>
     * 存储的规则 key:subKey_value;subkey_value & key:subKey_value;subKey_value
     * <p></p>
     * 实例：用户： 模块_更新时间；模块_更新时间 & 用户：模块_更新时间；模块_更新时间
     * @param context
     * @param preferenceKey 存储在SharedPreferences中的key
     * @param key 外层的key
     * @param subKey 里面的key
     * @return 根据key和subKey获取的值
     */
    public static String getValue(Context context, String preferenceKey, String key, String subKey) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(subKey)) return "";
        String info = getInfo(context, preferenceKey, key, "&", ":");
        HcLog.D("SettingHelper#getValue  info = " + info);
        if (TextUtils.isEmpty(info)) {
            return "";
        } else {
            String value = getValue(subKey, info, ";", "_");
            HcLog.D("SettingHelper#getValue value = " + value + " info = " + info);
            return value.equals("-1") ? "" : value;
        }
    }

    public static String getIMPW(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String pw = sp.getString(PREFERENCE_KEY_IM_PW, "");
        return pw;
    }

    public static void setIMPW(Context context, String pw) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_IM_PW, pw);
        editor.commit();
    }

    public static int getKeyboradHeight(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        int keyboard = sp.getInt(PREFERENCE_KEY_KEYBOARD, -1);
        return keyboard;
    }

    public static void setKeyboradHeight(Context context, int height) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(PREFERENCE_KEY_KEYBOARD, height);
        editor.commit();
    }

    /**
     * 保存5.0以下已经opt的信息
     * @param context
     * @param version 当前版本
     * @param dexSHA1 当前的签名文件
     */
    public static void setClassDex(Context context, String version, String dexSHA1) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_DEX2_SHA1, version + "_" + dexSHA1);
        editor.commit();
    }

    /**
     * 获取5.0以下已经opt的信息
     * @param context
     * @return version_dexSHA1
     */
    public static String getClassDex(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_MULTI_PROCESS);
        return sp.getString(PREFERENCE_KEY_DEX2_SHA1, "");
    }

    public static void setEmail(Context context, String email) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_EMAIL, email);
        editor.commit();
    }

    public static String getEmail(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String email = sp.getString(PREFERENCE_KEY_EMAIL, "");
        return email;
    }
}

