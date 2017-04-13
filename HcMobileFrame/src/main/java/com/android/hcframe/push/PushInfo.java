/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-10-30 下午4:21:38
 */
package com.android.hcframe.push;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcLog;
import com.android.hcframe.badge.AppBadgeInfo;
import com.android.hcframe.badge.BadgeInfo;
import com.android.hcframe.container.ContainerActivity;
import com.android.hcframe.container.data.ContainerConfig;
import com.android.hcframe.container.data.ViewInfo;
import com.android.hcframe.menu.Menu1Activity;
import com.android.hcframe.menu.Menu2Activity;
import com.android.hcframe.menu.Menu3Activity;
import com.android.hcframe.menu.Menu4Activity;
import com.android.hcframe.menu.Menu5Activity;
import com.android.hcframe.menu.MenuInfo;
import com.android.hcframe.sql.DataCleanManager;
import com.android.hcframe.sql.OperateDatabase;
import com.android.hcframe.sys.SystemMessage;
import com.nostra13.universalimageloader.utils.StorageUtils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;

public class PushInfo {

    /**
     * 应用ID,主要用于查找应用的具体位置,应用的类型和应用的主页面
     */
    private String mAppId;

    /**
     * 消息的内容的类型,可以为空,每个模块自己定义
     */
    private String mType;

    /**
     * 消息的具体内容,可以为空,每个模块自己定义
     */
    private String mContent;
    /**
     * "clean":清楚缓存
     */
    private String mStatus;

    /***
     * 当前消息所对应的用户
     */
    private String mAccount;

    /**
     * 应用的名字
     */
    private String mAppName;

    /**
     * 在线编辑
     */
    public static final int TYPE_ONLINE = 0;
    /**
     * 文本导入
     */
    public static final int TYPE_PDF = 1;
    /**
     * 互联网链接
     */
    public static final int TYPE_URL = 2;
    /**
     * 图片新闻
     */
    public static final int TYPE_IMAGE = 3;
    /**
     * 进入应用
     */
    public static final int TYPE_APP = 4;
    /**
     * 视频新闻
     */
    public static final int TYPE_VIDEO = 5;

    /**
     * 存储应用容器中的应用的信息,最外层的放在第一个
     * 比如一级菜单为应用容器且要打开里面的应用(此应用也可能是应用容器)
     */
    private List<MenuInfo> mAppInfos = new ArrayList<MenuInfo>();

    public PushInfo(String data) {
        if (TextUtils.isEmpty(data))
            return;
        try {
            JSONObject object = new JSONObject(data);
            if (hasValue(object, "status")) {
                mStatus = object.getString("status");
                if ("clean".equals(mStatus)) {
                    //清除缓存
                    DataCleanManager.cleanApplicationData(HcApplication.getContext(), true,
                            new File(Environment.getExternalStorageDirectory()
                                    + "/hc/").getAbsolutePath(), StorageUtils
                                    .getCacheDirectory(HcApplication.getContext())
                                    .getAbsolutePath());
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                    return;
                }
            }

            if (hasValue(object, "appid")) {
                mAppId = object.getString("appid");
            }
            if (hasValue(object, "type")) {
                mType = object.getString("type");
            }
            if (hasValue(object, "content")) {
                mContent = object.getString("content");
            }
            if (hasValue(object, "account")) {
                mAccount = object.getString("account");
            }

        } catch (Exception e) {
            // TODO: handle exception
            HcLog.D("PushInfo Exception e = " + e);
        }
    }

    public void setAppId(String appId) {
        mAppId = appId;
    }

    public String getAppId() {
        return mAppId;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getType() {
        return mType;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public String getContent() {
        return mContent;
    }

    /**
     * 判断对应的key是否存在value
     *
     * @param object
     * @param tag
     * @return true:有数据；false：没有数据
     */
    private boolean hasValue(JSONObject object, String tag) {
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

    public void startActivity2(Context context) {
        HcLog.D("PushInfo startActivity type = " + mType + " appId = " + mAppId);
        if (!TextUtils.isEmpty(mType)) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // 进入应用
            List<MenuInfo> infos = new ArrayList<MenuInfo>(HcConfig.getConfig()
                    .getFirstMenus());
            boolean isMenu = false;
            int index = 0;
            for (MenuInfo menuInfo : infos) {
                if (menuInfo.getAppId().equals(mAppId)) {
                    isMenu = true;
                    break;
                }
                index++;
            }
            if (isMenu) {
                // 关闭所以的Activity,进入相应的菜单
                switch (index) {
                    case 0:
                        intent.setClass(context, Menu1Activity.class);
                        break;
                    case 1:
                        intent.setClass(context, Menu2Activity.class);
                        break;
                    case 2:
                        intent.setClass(context, Menu3Activity.class);
                        break;
                    case 3:
                        intent.setClass(context, Menu4Activity.class);
                        break;
                    case 4:
                        intent.setClass(context, Menu5Activity.class);
                        break;
                    default:
                        intent.setClass(context, Menu1Activity.class);
                        break;
                }
                HcAppState.getInstance().finishAllActivities();
            } else { // 进入其他Activity，暂不考虑,直接先进入应用
                intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED); // 和从Launcher进入一样的效果
                /**
                 * @author jrjin
                 * @date 2015-12-4 上午10:14:05 增加这句的话，要是应用已经打开，会重新再次打开一个新的应用
                 *       intent.setClass(context, Menu1Activity.class);
                 */
            }
            infos.clear();
            context.startActivity(intent);
        }
    }

    public void startActivitys(Context context) {
        boolean isAppRunning = false;
        String MY_PKG_NAME = "com.android.hcframe";
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
        for (ActivityManager.RunningTaskInfo info : list) {
            if (info.topActivity.getPackageName().equals(MY_PKG_NAME) && info.baseActivity.getPackageName().equals(MY_PKG_NAME)) {
                isAppRunning = true;
                break;
            }
        }
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //isAppRunning为true的时候是程序在运行
        if (isAppRunning) {
            // 进入应用
            List<MenuInfo> infos = new ArrayList<MenuInfo>(HcConfig.getConfig()
                    .getFirstMenus());
            boolean isMenu = false;
            int index = 0;
            for (MenuInfo menuInfo : infos) {
                if (menuInfo.getAppId().equals(mAppId)) {
                    isMenu = true;
                    break;
                }
                index++;
            }
            if (isMenu) {
                // 关闭所以的Activity,进入相应的菜单
                switch (index) {
                    case 0:
                        intent.setClass(context, Menu1Activity.class);
                        break;
                    case 1:
                        intent.setClass(context, Menu2Activity.class);
                        break;
                    case 2:
                        intent.setClass(context, Menu3Activity.class);
                        break;
                    case 3:
                        intent.setClass(context, Menu4Activity.class);
                        break;
                    case 4:
                        intent.setClass(context, Menu5Activity.class);
                        break;
                    default:
                        intent.setClass(context, Menu1Activity.class);
                        break;
                }
                HcAppState.getInstance().finishAllActivities();
            } else {
                //根据返回值判断网页还是类
                if (true) {
                    intent.putExtra("id", "");
                    intent.setClass(context, PushHtmlActivity.class);
                } else {
                    intent.putExtra("mMenuPage", "");
                    intent.putExtra("title", "");
                    intent.setClass(context, PushBaseActivity.class);
                }

            }
            infos.clear();
            context.startActivity(intent);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED); // 和从Launcher进入一样的效果
            /**
             * @author jrjin
             * @date 2015-12-4 上午10:14:05 增加这句的话，要是应用已经打开，会重新再次打开一个新的应用
             *       intent.setClass(context, Menu1Activity.class);
             */
            intent.setClass(context, Menu2Activity.class);
            context.startActivity(intent);
        }

    }


    public void startActivity(Context context) {
        HcLog.D("PushInfo startActivity type = " + mType + " appId = " + mAppId + " content = "+mContent + " account = "+mAccount);
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // 查找应用的位置
        List<MenuInfo> infos = new ArrayList<MenuInfo>(HcConfig.getConfig().getFirstMenus());
        boolean find = false;
        int index = -1;
        for (MenuInfo menuInfo : infos) {
            index++;
            if (menuInfo.getAppId().equals(mAppId)) {
                mAppName = menuInfo.getAppName();
                find = true;
                break;
            } else { // 判断应用是否是应用容器,这个要放到后面判断吗？
                if (!menuInfo.getClouded() && "com.android.hcframe.container.ContainerMenuPage".equals(menuInfo.getClassName())) { // 双重保护
                    boolean success = addContainerApp(context, mAppId, menuInfo.getAppId());
                    if (success) {
                        find = true;
                        break;
                    }
                }
            }

        }
        if (!find) { // 未找到,跳转到第一个应用.
            index = 0;
        }

        switch (index) {
            case 0:
                intent.setClass(context, Menu1Activity.class);
                break;
            case 1:
                intent.setClass(context, Menu2Activity.class);
                break;
            case 2:
                intent.setClass(context, Menu3Activity.class);
                break;
            case 3:
                intent.setClass(context, Menu4Activity.class);
                break;
            case 4:
                intent.setClass(context, Menu5Activity.class);
                break;
            default: // 这里应该是不可能的
                intent.setClass(context, Menu1Activity.class);
                break;
        }
        HcLog.D("PushInfo # app size = "+mAppInfos.size());
        HcAppState.getInstance().finishAllActivities();
        infos.clear();
        context.startActivity(intent);

    }

    /**
     *
     * @param context
     * @param appId 消息推送应用的ID
     * @param containerId 应用容器的ID
     * @return
     */
    private boolean addContainerApp(Context context, String appId, String containerId) {

        ViewInfo containerInfo = ContainerConfig.getInstance().getContainerInfo(context, containerId);
        if (containerInfo != null) {
            List<ViewInfo> infos = containerInfo.getViewInfos();
            for (ViewInfo viewInfo : infos) { // 每个模版,可能是单应用,可能是多应用,也可能是分隔线.
                List<ViewInfo> apps = viewInfo.getViewInfos(); // 每个模版中的应用列表

                for (ViewInfo app : apps) {
                    if (app.getAppId().equals(appId)) { // 说明是要找的应用
                        mAppName = app.getAppName();
                        MenuInfo info = new MenuInfo();
                        info.setAppId(app.getAppId());
                        info.setAppName(app.getAppName());
                        boolean cloud = app.getViewType() == ViewInfo.VIEW_TYPE_HTML;
                        info.setClouded(cloud);
                        if (cloud) {
                            info.setAppIndexUrl(app.getViewAction());
                        } else {
                            info.setClassName(app.getViewAction());
                        }
                        mAppInfos.add(info);
                        return true;
                    } else { // 判断是否是应用容器
                        if (app.getViewType() == ViewInfo.VIEW_TYPE_SINGLE &&
                                "com.android.hcframe.container.ContainerMenuPage".equals(app.getViewAction())) {

                            boolean success = addContainerApp(context, appId, app.getAppId());
                            if (success) {
                                mAppName = app.getAppName();
                                MenuInfo info = new MenuInfo();
                                info.setAppId(app.getAppId());
                                info.setAppName(app.getAppName());
                                info.setClouded(false);
                                info.setClassName("com.android.hcframe.container.ContainerMenuPage");
                                mAppInfos.add(0, info);
                            }
                            return success;
                        }
                    }
                }
            }
        } else {
            HcLog.D("PushInfo #addContainerApp viewInfo is null! error!!!!!!");
        }

        return false;
    }

    public MenuInfo getAppInfo() {
        if (mAppInfos.isEmpty()) return null;
        return mAppInfos.remove(0);
    }

    public String getAppName() {
        return mAppName;
    }

    /**
     * @author jinjr
     * @date 16-11-24 上午11:43
     * @param context
     * @param messageId
     */
    public void startActivityFromIM(Context context, int messageId) {
        HcLog.D("PushInfo #startActivityFromIM type = " + mType + " appId = " + mAppId + " content = "+mContent + " account = "+mAccount);
        SystemMessage message = OperateDatabase.getSystemMessage(context, messageId);
        int appType = message.getAppType();
        HcLog.D("PushInfo #startActivityFromIM appType = "+appType + " messageId = "+messageId);
        if (appType != -1) {
            mAppName = message.getAppName();
            switch (appType) {
                case 1:
                    startHtmlActivity(context, message.getIndexContent());
                    break;
                case 0:
                    startNativeActivity(context, message.getIndexContent());
                    break;

                default:
                    break;
            }
        } else {
            // 查找应用的位置
            List<MenuInfo> infos = new ArrayList<MenuInfo>(HcConfig.getConfig().getFirstMenus());
            String action;
            boolean cloud;
            for (MenuInfo menuInfo : infos) {
                if (menuInfo.getAppId().equals(mAppId)) {
                    mAppName = menuInfo.getAppName();
                    cloud = menuInfo.getClouded();
                    action = cloud ? menuInfo.getAppUrl() : menuInfo.getClassName();
                    OperateDatabase.updateSysMessage(context, messageId, cloud ? 1 : 0, action, mAppName);
                    if (cloud) {
                        startHtmlActivity(context, action);
                    } else {
                        startNativeActivity(context, action);
                    }
                    break;
                } else { // 判断应用是否是应用容器,这个要放到后面判断吗？
                    if (!menuInfo.getClouded() && "com.android.hcframe.container.ContainerMenuPage".equals(menuInfo.getClassName())) { // 双重保护
                        boolean success = findContainerApp(context, mAppId, menuInfo.getAppId(), messageId);
                        if (success) {
                            break;
                        }
                    }
                }

            }
        }

    }

    private void startHtmlActivity(Context context, String action) {

        Intent intent = new Intent();
        intent.setClass(context, ContainerActivity.class);
        intent.putExtra("appId", mAppId);
        intent.putExtra("appName", mAppName);
        intent.putExtra("url", action);
        intent.putExtra("className", "com.android.hcframe.menu.WebMenuPage");
        intent.putExtra("menu", false);
        context.startActivity(intent);
    }

    private void startNativeActivity(Context context, String action) {
        Intent intent = new Intent(context, ContainerActivity.class);
        intent.putExtra("appId", mAppId);
        intent.putExtra("className", action);
        intent.putExtra("appName", mAppName);
        intent.putExtra("menu", false);
        context.startActivity(intent);
    }

    /**
     *
     * @param context
     * @param appId 消息推送应用的ID
     * @param containerId 应用容器的ID
     * @return
     */
    private boolean findContainerApp(Context context, String appId, String containerId, int messageId) {

        ViewInfo containerInfo = ContainerConfig.getInstance().getContainerInfo(context, containerId);
        if (containerInfo != null) {
            List<ViewInfo> infos = containerInfo.getViewInfos();
            for (ViewInfo viewInfo : infos) { // 每个模版,可能是单应用,可能是多应用,也可能是分隔线.
                List<ViewInfo> apps = viewInfo.getViewInfos(); // 每个模版中的应用列表

                for (ViewInfo app : apps) {
                    if (app.getAppId().equals(appId)) { // 说明是要找的应用
                        mAppName = app.getAppName();
                        boolean cloud = app.getViewType() == ViewInfo.VIEW_TYPE_HTML;
                        OperateDatabase.updateSysMessage(context, messageId, cloud ? 1 : 0, app.getViewAction(), mAppName);
                        if (cloud) {
                            startHtmlActivity(context, app.getViewAction());
                        } else {
                            startNativeActivity(context, app.getViewAction());
                        }

                        return true;
                    } else { // 判断是否是应用容器
                        if (app.getViewType() == ViewInfo.VIEW_TYPE_SINGLE &&
                                "com.android.hcframe.container.ContainerMenuPage".equals(app.getViewAction())) {

                            boolean success = addContainerApp(context, appId, app.getAppId());

                            return success;
                        }
                    }
                }
            }
        } else {
            HcLog.D("PushInfo #findContainerApp viewInfo is null! error!!!!!!");
        }

        return false;
    }
}
