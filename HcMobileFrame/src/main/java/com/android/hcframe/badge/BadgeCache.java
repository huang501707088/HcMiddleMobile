package com.android.hcframe.badge;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import com.android.hcframe.CacheManager;
import com.android.hcframe.HcApplication;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TemporaryCache;
import com.android.hcframe.container.data.ContainerConfig;
import com.android.hcframe.container.data.ViewInfo;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.http.ResponseCodeInfo;
import com.android.hcframe.menu.MenuInfo;
import com.android.hcframe.push.HcAppState;
import com.android.hcframe.sql.SettingHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-4-19 14:38.
 */
public class BadgeCache implements IHttpResponse, TemporaryCache {

    private static final String TAG = "BadgeCache";

    private static final BadgeCache mCache = new BadgeCache();

    private BadgeInfo mRoot;
    /**
     * key: appId + _ + module
     * value: BadgeObserver
     */
    private Map<String, BadgeObserver> mBadges = new HashMap<String, BadgeObserver>();

    private String mMD5Url;

    private BadgeCache() {
        CacheManager.getInstance().addCache(this);
        mRoot = new AppBadgeInfo();
        mRoot.setAppId("-1");
        mRoot.setModuleId(HcConfig.getConfig().getClientId());
        mRoot.setType(BadgeObserver.FLAG_TEXT);
    }

    public static final BadgeCache getInstance() {
        return mCache;
    }

    /**
     * 进入应用的时候创建角标树
     * 同一个客户端不会出现两个相同的应用
     *
     * @param context
     */
    public void createBadge(Context context) {
        if (!TextUtils.isEmpty(mMD5Url)) {
            HcHttpRequest.getRequest().cancelRequest(mMD5Url);
            mMD5Url = null;
        }
        createBadgeFromDatebase(context);
        getBadges(context);
    }

    private void createBadgeFromDatebase(Context context) {
        // 1.先判断mRoot是否有数据
        // 2.有数据(说明有消息推送存在,之前应用没有完全退出),直接去服务端请求角标数据
        // 3.没有数据,先去数据库获取数据,数据库中有数据直接加载
        // 4.数据库中没有数据,读取配置文件
        // 5.去服务端请求角标数据
        if (mRoot.isEmpty()) {
            List<BadgeInfo> badgeInfos = BadgeOperateDatabase.getBadges(context);
            HcLog.D(TAG + " #createBadgeFromDatebase badgeInfos size = " + badgeInfos.size());
            if (badgeInfos.isEmpty())
                parseBadgeJson(context);
            else {
                // 把列表增加到mRoot里面
                Iterator<BadgeInfo> iterator;
                Iterator<BadgeInfo> root;
                BadgeInfo info;
                BadgeInfo rootInfo;
                int size = badgeInfos.size();
                while (size > 0) {
                    iterator = badgeInfos.iterator();
                    while (iterator.hasNext()) {
                        info = iterator.next();
                        if (mRoot.getModuleId().equals(info.getAppId())) { // 一级菜单
                            mRoot.addBadge(info, false);
                            iterator.remove();
                            size--;
                        } else {
                            root = mRoot.iterator();
                            while (root.hasNext()) {
                                rootInfo = root.next();
                                if (rootInfo instanceof AppBadgeInfo && rootInfo.getModuleId().equals(info.getAppId())) {
                                    // 添加角标,方法需要验证
                                    rootInfo.addBadge(info, false);
                                    iterator.remove();
                                    size--;
                                    break;
                                }
                            }

                        }
                    }
                }

                /**
                 *
                 * @jrjin
                 * debug
                 */
                Iterator<BadgeInfo> iterator1 = mRoot.iterator();
                BadgeInfo cache;
                while (iterator1.hasNext()) {
                    cache = iterator1.next();
                    HcLog.D(TAG + "#createBadgeFromDatebase parentId = " + cache.getAppId() + " moudleId = " + cache.getModuleId());
                }
            }
        } else {

        }
    }

    private void parseBadgeJson(Context context) {
        String badgeList = HcConfig.getConfig().getBadges();
        HcLog.D(TAG + " #parseBadgeJson badgeList = " + badgeList);
        if (TextUtils.isEmpty(badgeList)) return;
        List<String> appList = new ArrayList<String>(Arrays.asList(badgeList.split(";")));
        // 读取有角标功能的应用或者模块
        // 读取一级菜单
        List<MenuInfo> menus = new ArrayList<MenuInfo>(HcConfig.getConfig().getFirstMenus());
        HcLog.D(TAG + " #parseBadgeJson appList size = " + appList.size() + " menus size = " + menus.size());
        // 创建角标树
        Iterator<String> iterator;
        String appId;

        AppBadgeInfo menuAppInfo = null; // 一级菜单的角标,为了下面要是一级菜单是应用容器的话,避免重复添加

        for (MenuInfo menuInfo : menus) {
            iterator = appList.iterator();
            menuAppInfo = null; // 每次循环这里都要重制,因为上一个有角标功能且不是应用容器,下一个是应用容器,则会复用上面一个.
            while (iterator.hasNext()) {
                appId = iterator.next();
                HcLog.D(TAG + "#parseBadgeJson appId = " + appId + " menuId = " + menuInfo.getAppId());
                if (menuInfo.getAppId().equals(appId)) {
                    // 说明一级菜单有角标功能
                    AppBadgeInfo app = new AppBadgeInfo();
                    app.setAppId(HcConfig.getConfig().getClientId());
                    app.setModuleId(appId);
                    mRoot.addBadge(app, false);
                    menuAppInfo = app;
                    iterator.remove();
                    break;
                }
            }

            // 判断是否是应用容器
            if ("com.android.hcframe.container.ContainerMenuPage".equals(menuInfo.getClassName())) {
                // 是一级菜单的话不用再添加了,因为前面已经添加了。
                AppBadgeInfo app;
                if (menuAppInfo != null) {
                    app = menuAppInfo;
                    menuAppInfo = null;
                } else {
                    app = new AppBadgeInfo();
                    app.setAppId(HcConfig.getConfig().getClientId());
                    app.setModuleId(menuInfo.getAppId());
                    mRoot.addBadge(app, false);
                }

                // 添加应用容器里面的数据
                addContainerBadge(context, app, appList, menuInfo.getAppId());
            }
        }

        /**
         * @jrjin
         * debug
         */
        Iterator<BadgeInfo> iterator1 = mRoot.iterator();
        BadgeInfo cache;
        while (iterator1.hasNext()) {
            cache = iterator1.next();
            HcLog.D(TAG + "#parseBadgeJson parentId = " + cache.getAppId() + " moudleId = " + cache.getModuleId());
        }
    }

    private void addContainerBadge(Context context, BadgeInfo parent, List<String> appList, String containerId) {
        if (appList.isEmpty()) return;
        ViewInfo containerInfo = ContainerConfig.getInstance().getContainerInfo(context, containerId);
        if (containerInfo != null) {
            List<ViewInfo> infos = containerInfo.getViewInfos();
            for (ViewInfo viewInfo : infos) { // 每个模版,可能是单应用,可能是多应用,也可能是分隔线.
                List<ViewInfo> apps = viewInfo.getViewInfos(); // 每个模版中的应用列表
                Iterator<String> iterator;
                AppBadgeInfo appBadeInfo = null; // 应用容器中的应用容器的角标,为了下面要是应用容器的话,避免重复添加
                for (ViewInfo app : apps) {
                    iterator = appList.iterator();
                    String entry;
                    while (iterator.hasNext()) {
                        entry = iterator.next();
                        HcLog.D(TAG + " #addContainerBadge container appId = " + app.getAppId() + " badge appId = " + entry);
                        if (app.getAppId().equals(entry)) {
                            AppBadgeInfo appBadge = new AppBadgeInfo();
                            appBadge.setAppId(parent.getModuleId());
                            appBadge.setModuleId(entry);
                            parent.addBadge(appBadge, false);
                            appBadeInfo = appBadge;
                            iterator.remove();
                            break;
                        }
                    }

                    // 判断是否是应用容器
                    if ("com.android.hcframe.container.ContainerMenuPage".equals(app.getViewAction())) {
                        AppBadgeInfo appBadge;
                        if (appBadeInfo != null) {
                            appBadge = appBadeInfo;
                            appBadeInfo = null;
                        } else {
                            appBadge = new AppBadgeInfo();
                            appBadge.setAppId(parent.getModuleId());
                            appBadge.setModuleId(app.getAppId());
                            parent.addBadge(appBadge, false);
                        }


                        // 添加应用容器里面的数据
                        addContainerBadge(context, appBadge, appList, app.getAppId());
                    }
                }
            }
        } else {
            HcLog.D(TAG + " #addContainerBadge viewInfo is null! error!!!!!!");
        }
    }

    /**
     * 添加角标监听
     *
     * @param key      appId + _ + moduleId
     * @param observer
     */
    public void addBadgeObserver(String key, BadgeObserver observer) {
        mBadges.put(key, observer);
        HcLog.D(TAG + " #addBadgeObserver mBadges size = " + mBadges.size());
        //去除树中的值
        /**
         * czx
         * 2016.4.29
         */
        String[] str = key.split("_");
        if (str != null && str.length == 2) {

            Iterator<BadgeInfo> iterator = mRoot.iterator();
            BadgeInfo info;
            while (iterator.hasNext()) {
                info = iterator.next();
                if (info.getAppId().equals(str[0]) && info.getModuleId().equals(str[1])) {
                    observer.setCount(info.getCount());
                    observer.setFlag(info.getType());
                    return;
                }
            }

        }


    }

    /**
     * 删除角标监听
     *
     * @param key appId + _ + moduleId
     */
    public void removeBadgeObserver(String key) {
        mBadges.remove(key);
    }

    /**
     * 删除角标监听
     *
     * @param observer
     */
    public void removeBadgeObserver(BadgeObserver observer) {
        String key = getKey(observer);
        if (key != null) {
            mBadges.remove(key);
        }
    }

    /**
     *
     * @param observer
     * @return appId + _ + moduleId
     */
    private String getKey(BadgeObserver observer) {
        Set<Map.Entry<String, BadgeObserver>> entries = mBadges.entrySet();
        String key = null;
        for (Map.Entry<String, BadgeObserver> badge : entries) {
            if (badge.getValue() == observer) {
                key = badge.getKey();
                break;
            }
        }
        return key;
    }

    /**
     * 对一级菜单的操作
     * @param observer
     */
    public void operateBadge(BadgeObserver observer) {
        String key = getKey(observer);
        if (!TextUtils.isEmpty(key)) {
            String[] strings = key.split("_");
            if (strings != null && strings.length > 1) {
                List<BadgeInfo> badgeInfoList = mRoot.getBadges();
                int size = badgeInfoList.size();
                if (size > 0) {
                    BadgeInfo badgeInfo;
                    for (int i = 0; i < size; i++) {
                        badgeInfo = badgeInfoList.get(i);
                        if (strings[0].equals(badgeInfo.getAppId()) && strings[1].equals(badgeInfo.getModuleId())) {
                            badgeInfo.onClick(observer);
                            break;
                        }
                    }
                }
            }


        }
    }

    public BadgeObserver getBadgeObserver(String key) {
        return mBadges.get(key);
    }

    /**
     * 添加角标接口返回值
     *
     * @param data     返回的数据
     * @param request  请求的类型
     * @param category 返回的类型
     */
    @Override
    public void notify(Object data, RequestCategory request, ResponseCategory category) {
        if (request != null) {
            switch (request) {
                case CORNER:
                    mMD5Url = null;
                    if (data != null) {
                        switch (category) {
                            case SUCCESS:
                                HcLog.D(TAG + " ontify SUCCESS callback data = " + data);
                                try {
//                                    String s = "{\"corner\":[{\"app_id\":\"240\",\"operate_type\":\"add\",\"item_id\":\"1307ce3652b74867bf25d3e8763b1182\",\"remind_count\":\"1\",\"remind_type\":\"1\"},{\"app_id\":\"257\",\"operate_type\":\"add\",\"item_id\":\"258\",\"remind_count\":\"1\",\"remind_type\":\"1\"},{\"app_id\":\"265\",\"operate_type\":\"add\",\"item_id\":\"255\",\"remind_count\":\"1\",\"remind_type\":\"1\"},{\"app_id\":\"265\",\"operate_type\":\"add\",\"item_id\":\"268\",\"remind_count\":\"1\",\"remind_type\":\"1\"},{\"app_id\":\"242\",\"operate_type\":\"add\",\"item_id\":\"258\",\"remind_count\":\"1\",\"remind_type\":\"1\"},{\"app_id\":\"242\",\"operate_type\":\"add\",\"item_id\":\"241\",\"remind_count\":\"1\",\"remind_type\":\"1\"},{\"app_id\":\"242\",\"operate_type\":\"add\",\"item_id\":\"1\",\"remind_count\":\"1\",\"remind_type\":\"1\"},{\"app_id\":\"242\",\"operate_type\":\"add\",\"item_id\":\"240\",\"remind_count\":\"1\",\"remind_type\":\"1\"},{\"app_id\":\"242\",\"operate_type\":\"add\",\"item_id\":\"242\",\"remind_count\":\"1\",\"remind_type\":\"1\"},{\"app_id\":\"265\",\"operate_type\":\"add\",\"item_id\":\"268\",\"remind_count\":\"1\",\"remind_type\":\"1\"}],\"nowtime\":\"1462436863576\"}";
                                    JSONObject jsonObject = new JSONObject((String) data);
//                                    JSONObject jsonObject = new JSONObject(s);
                                    String updateTime = jsonObject.optString("nowtime");
                                    SettingHelper.setBadgeTime(HcApplication.getContext(), SettingHelper.getAccount(HcApplication.getContext()), updateTime);
                                    JSONArray corners = jsonObject.optJSONArray("corner");
                                    JSONObject corner;
                                    BadgeInfo badgeInfo;
                                    for (int i = 0, n = corners.length(); i < n; i++) {
                                        corner = (JSONObject) corners.get(i);
                                        if (HcUtil.hasValue(corner, "column_type")) {
                                            int type = corner.getInt("column_type");
                                            if (type == BadgeInfo.TYPE_APP) {
                                                badgeInfo = new AppBadgeInfo();
                                            } else {
                                                badgeInfo = new ModuleBadgeInfo();
                                            }
                                            badgeInfo.setAppId(corner.optString("app_id"));
                                            badgeInfo.setCount(Integer.valueOf(corner.optString("remind_count")));
                                            badgeInfo.setModuleId(corner.optString("item_id"));
                                            badgeInfo.setType(Integer.valueOf(corner.optString("remind_type")));
                                            matchBadge(badgeInfo, corner.optString("operate_type"));

                                        }

                                    }


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    HcLog.D(TAG + " #notify parser json error! e = " + e);
                                }
                                break;
                            case REQUEST_FAILED:
                                ResponseCodeInfo responseCodeInfo = (ResponseCodeInfo) data;
                                HcLog.D(TAG + " #notify parser json ! body = " + responseCodeInfo.getBodyData());
                                if (responseCodeInfo.getCode() == HcHttpRequest.REQUEST_ACCOUT_EXCLUDED ||
                                        responseCodeInfo.getCode() == HcHttpRequest.REQUEST_TOKEN_FAILED)
                                    HcUtil.cleanBadgeCacheInfo(responseCodeInfo.getBodyData(), HcApplication.getContext());
                                break;
                            case DATA_ERROR:
                                break;
                            default:

                                break;
                        }
                    } else {
                    }
                    break;
                default:
                    break;
            }
        }


    }

    @Override
    public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
        mMD5Url = md5Url;
    }


    /**
     * 匹配需要进行添加或者修改的角标
     *
     * @param info
     * @param type 角标更新的方式
     */
    public void matchBadge(BadgeInfo info, String type) {
        HcLog.D(TAG + " matchBadge!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1!!!!!!!!!!!!!!!!!!!1 info appId = "+info.getAppId() + " moduleId = "+info.getModuleId());
        Iterator<BadgeInfo> iterator = mRoot.iterator();
        BadgeInfo parentBadgeInfo = null; // 上级角标
        BadgeInfo badgeInfo = null; // 匹配到的角标
        while (iterator.hasNext()) {
            badgeInfo = iterator.next();
            // 情况1：先匹配到了角标对象的parentId
            // 第一步：先找到角标对象的parentId
            if (badgeInfo.getAppId().equals(info.getAppId())) {
                if (parentBadgeInfo == null)
                    parentBadgeInfo = badgeInfo.getParentBadge();

                // 第二步： 再查找当前的角标对象的模块ID
                if (badgeInfo.getModuleId().equals(info.getModuleId())) {
                    // 匹配到了需要找的角标,更新角标数字

                    if ("add".equals(type)) {
                        HcLog.D(TAG + "#matchBadge 在应用ID="+badgeInfo.getModuleId() +"新增角标数量="+info.getCount() +" 新增前角标数="+badgeInfo.getCount());
                        badgeInfo.updateType(info.getType());
                        badgeInfo.addCount(info.getCount());
                        HcLog.D(TAG + "#matchBadge 在应用ID=" + badgeInfo.getModuleId() +" 新增后角标数=" + badgeInfo.getCount());
                    } else if ("cover".equals(type)) {
                        HcLog.D(TAG + "#matchBadge 在应用ID="+badgeInfo.getModuleId() +"覆盖角标数量="+info.getCount() +" 覆盖前角标数="+badgeInfo.getCount());
                        badgeInfo.updateType(info.getType());
                        badgeInfo.updateCount(info.getCount());
                        HcLog.D(TAG + "#matchBadge 在应用ID=" + badgeInfo.getModuleId() + " 覆盖后角标数=" + badgeInfo.getCount());
                    }

                    return;
                }


            } /* 情况2：先匹配到了上级角标对象 */
            else if (badgeInfo.getModuleId().equals(info.getAppId())) {
                // 这里还是判断badgeInfo里面的列表有没有匹配
                List<BadgeInfo> modules = badgeInfo.getBadges();
                for (BadgeInfo module : modules) {
                    // 第一步：匹配角标对象的模块ID
                    if (module.getModuleId().equals(info.getModuleId())) {
                        if ("add".equals(type)) {
                            HcLog.D(TAG + "#matchBadge 先找到上级应用,在应用ID="+badgeInfo.getModuleId() +"新增角标数量="+info.getCount() +" 新增前角标数="+module.getCount());
                            module.updateType(info.getType());
                            module.addCount(info.getCount());
                            HcLog.D(TAG + "#matchBadge 先找到上级应用,在应用ID=" + badgeInfo.getModuleId() +" 新增后角标数=" + badgeInfo.getCount());
                        } else if ("cover".equals(type)) {
                            HcLog.D(TAG + "#matchBadge 先找到上级应用,在应用ID="+badgeInfo.getModuleId() +"覆盖角标数量="+info.getCount() +" 覆盖前角标数="+module.getCount());
                            module.updateType(info.getType());
                            module.updateCount(info.getCount());
                            HcLog.D(TAG + "#matchBadge 先找到上级应用,在应用ID=" + badgeInfo.getModuleId() + " 覆盖后角标数=" + module.getCount());
                        }

                        return;
                    }
                }
                // 第二步：判断上级角标对象(badgeInfo)是否为应用容器
                // 是应用容器,直接不添加,因为应用容器下的应用肯定是在badgeInfo.getBadges()里面了,
                // 说明应用容器下面新增了应用,而App未更新;或者说服务端的数据出错了.
                // 不是应用容器,则需要判断info是否为模块角标,应用角标不添加(说明数据出错了)
                if (isContainer(badgeInfo.getModuleId() + ".json")) {
                    HcLog.D(TAG + "#matchBadge 上级角标对象是应用容器,不需要添加,可能获取到的角标数据出错了！！！ 上级应用ID="+badgeInfo.getModuleId() +"新增应用模块ID="+info.getModuleId());
                } else {
                    if (info instanceof ModuleBadgeInfo) {
                        badgeInfo.addBadge(info, true);
                        HcLog.D(TAG + "#matchBadge 在应用ID="+badgeInfo.getModuleId() +"新增应用模块ID="+info.getModuleId());
                    } else {
                        HcLog.D(TAG + "#matchBadge 上级角标不是应用容器,但是需要添加的角标对象为应用角标类型,不需要添加,获取到的角标数据出错了！！！ 上级应用ID="+badgeInfo.getModuleId() +"新增应用模块ID="+info.getModuleId());
                    }
                }

                return;
            }
        }

        if (parentBadgeInfo != null) {
            if (isContainer(parentBadgeInfo.getModuleId() + ".json")) {
                HcLog.D(TAG + "#matchBadge parentBadgeInfo 上级角标对象是应用容器,不需要添加,可能获取到的角标数据出错了！！！ 上级应用ID="+parentBadgeInfo.getModuleId() +"新增应用模块ID="+info.getModuleId());
            } else {
                if (info instanceof ModuleBadgeInfo) {
                    parentBadgeInfo.addBadge(info, true);
                    HcLog.D(TAG + "#matchBadge parentBadgeInfo在应用ID="+parentBadgeInfo.getModuleId() +"新增应用模块ID="+info.getModuleId());
                } else {
                    HcLog.D(TAG + "#matchBadge parentBadgeInfo上级角标不是应用容器,但是需要添加的角标对象为应用角标类型,不需要添加,获取到的角标数据出错了！！！ 上级应用ID="+parentBadgeInfo.getModuleId() +"新增应用模块ID="+info.getModuleId());
                }
            }
//            parentBadgeInfo.addBadge(info, true);
//            HcLog.D(TAG + "#matchBadge parentBadgeInfo 在应用ID="+info.getAppId() +"新增应用模块ID="+info.getModuleId());
        } else { // 说明是断层的,没有上级角标,按道理不会出现这种情况
            HcLog.D(TAG + "#matchBadge not find badge error!");
        }
    }

    /**
     *  增加角标监听的对象,这里需要注意,IM里面的子模块没有调用这个.
     *  <p>IM的设计是IM应用里面只有一个模块,而这个模块不需要显示,所有的操作都只影响这个模块</p>
     *  <p>IM模块的Id为appId_IM,如果需要使用会出错,因为key值为appId_appId_IM,到时需要吧模块id更改成appId@IM</p>
     * @param appId 应用Id
     * @param key appId + _ + moduleId
     * @param observer
     */
    public void addAppBadgeObserver(String appId, String key, BadgeObserver observer) {
        String badgeList = HcConfig.getConfig().getBadges();
        if (TextUtils.isEmpty(badgeList)) return;
        List<String> appList = new ArrayList<String>(Arrays.asList(badgeList.split(";")));
        HcLog.D(TAG + " #addAppBadgeObserver key = "+key + " appId = "+appId);
        for (String id : appList) {
            if (id.equals(appId)) {
                addBadgeObserver(key, observer);
                break;
            }
        }
    }

    /**
     * 操作角标
     *
     * @param key appId + _ + moduleId
     */
    public void operateBadge(String key) {
        HcLog.D(TAG + "#   removeAllBadgeObserver  key=" + key);
        if (!TextUtils.isEmpty(key)) {
            String[] strings = key.split("_");
            Iterator<BadgeInfo> iterator = mRoot.iterator();
            BadgeInfo badgeInfo = null; // 匹配到的角标
            while (iterator.hasNext()) {
                badgeInfo = iterator.next();
                if (badgeInfo != null && strings != null && strings.length > 1) {
                    if (strings[0].equals(badgeInfo.getAppId()) && strings[1].equals(badgeInfo.getModuleId())) {
                        badgeInfo.onClick(getBadgeObserver(key));
                        return;
                    }
                }
            }
        }
    }

    /**
     * 删除角标的监听器
     *
     * @param key appId
     */
    public void removeAllBadgeObserver(String key) {
        HcLog.D(TAG + "#   removeAllBadgeObserver  key=" + key);
        if (!TextUtils.isEmpty(key)) {
            Iterator<Map.Entry<String, BadgeObserver>> it = mBadges.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, BadgeObserver> entry = it.next();
                String id = entry.getKey().toString();
                String value = entry.getValue().toString();
                String[] id_ml = id.split("_");
                if (id_ml != null && id_ml.length > 1) {
                    if (id_ml[0].equals(key)) {
                        it.remove();
                    }
                }

            }
        }
    }

    @Override
    public void clearCache(boolean exit) {
        if (exit) {
            /**
             * 去掉角标监听
             */
            mBadges.clear();

        }
        clearBadgeCache(exit);
    }

    /**
     * 清楚角标缓存数据
     */
    private void clearBadgeCache(boolean exit) {
        if (mRoot != null) { // 这里注意,以后换成推送的时候,退出不需要清除角标数据
            BadgeOperateDatabase.insertBadges(HcApplication.getContext(), mRoot);
            if (!exit) // 退出应用,角标的数据还要保持,因为有IM.切换用户的时候才清空.
                mRoot.clearBadge();
        }
        if (!exit) { // 注意：这里用户都还没有变化,所以读数据库获取的数据还是原来的数据
            // 更改成去服务端获取角标数据
            ;
//            createBadgeFromDatebase(HcApplication.getContext());
        }
    }

    private void getBadges(Context context) {
        String time = SettingHelper.getBadgeTime(context, SettingHelper.getAccount(context));
        HcLog.D(TAG + " #getBadge time = " + time);
        if (TextUtils.isEmpty(time)) {
            time = System.currentTimeMillis() + "";
        }
        HcHttpRequest.getRequest().sendGetCornerCommand(time, this);
    }

    public void checkBadges(Context context, String appId) {
        String badgeList = HcConfig.getConfig().getBadges();
        HcLog.D(TAG + " #checkBadges badgeList = " + badgeList + " appId = "+appId);
        if (TextUtils.isEmpty(badgeList)) return;
        List<String> appList = new ArrayList<String>(Arrays.asList(badgeList.split(";")));
        for (String id : appList) {
        	if (id.equals(appId)) {
                getBadges(context);
                return;
            }
        }
    }

    /**
     * 获取节点上的角标数据
     * @param appId
     * @param moduleId
     * @return
     */
    public BadgeInfo getBadgeInfo(String appId, String moduleId) {
        Iterator<BadgeInfo> iterator = mRoot.iterator();
        BadgeInfo info = null;
        while (iterator.hasNext()) {
            info = iterator.next();
            if (info.getAppId().equals(appId) && info.getModuleId().equals(moduleId)) {
                return info;
            }
        }

        return null;
    }

    /**
     * 第一次启动应用或者版本更新时调用
     * 同一个客户端不会出现两个相同的应用
     *
     * @param context
     */
    public void reCreateBadge(Context context) {
        if (!TextUtils.isEmpty(mMD5Url)) {
            HcHttpRequest.getRequest().cancelRequest(mMD5Url);
            mMD5Url = null;
        }
        parseBadgeJson(context);
        List<BadgeInfo> badgeInfos = BadgeOperateDatabase.getBadges(context);
        HcLog.D(TAG + " #reCreateBadge badgeInfos size = " + badgeInfos.size());
        if (badgeInfos.size() > 0) {
            Iterator<BadgeInfo> olds = badgeInfos.iterator();
            Iterator<BadgeInfo> news = mRoot.iterator();
            BadgeInfo oldBadge;
            BadgeInfo newBadge;
            // 第一步： 先匹配模块的角标
            while (olds.hasNext()) {
                oldBadge = olds.next();
                if (oldBadge instanceof ModuleBadgeInfo) {
                    while (news.hasNext()) {
                    	newBadge = news.next();
                        if (newBadge instanceof ModuleBadgeInfo) {
                            HcLog.D(TAG + "#reCreateBadge 匹配到子模块 新版本角标上级应用ID_模块ID = "+newBadge.getAppId() + "_" +newBadge.getModuleId()
                                + " 老版本角标上级应用ID_模块ID = "+oldBadge.getModuleId() + "_"+oldBadge.getModuleId());
                            if (newBadge.getAppId().equals(oldBadge.getAppId()) &&
                                newBadge.getModuleId().equals(oldBadge.getModuleId())) {
                                newBadge.updateCount(oldBadge.getCount());
                            }
                        }
                    }
                    olds.remove();
                }
            }


            // 第二步,匹配没有子应用的应用角标,即应用容器
            olds = badgeInfos.iterator();
            while (olds.hasNext()) {
                oldBadge = olds.next();
                if (oldBadge instanceof AppBadgeInfo && oldBadge.getBadges().isEmpty()) {
                    while (news.hasNext()) {
                        newBadge = news.next();
                        if (newBadge instanceof AppBadgeInfo && newBadge.getBadges().isEmpty()) {// 这里最好增加一个判断是否是应用容器
                            HcLog.D(TAG + "#reCreateBadge 匹配到应用 新版本角标上级应用ID_应用ID = "+newBadge.getAppId() + "_" +newBadge.getModuleId()
                                    + " 老版本角标上级应用ID_应用ID = "+oldBadge.getAppId() + "_"+oldBadge.getModuleId());
                            // 只需要比较应用本身的id,而不需要比较父应用的id,因为应用有可能被移动了,应用容器里面应用之间的移动
                            if(newBadge.getModuleId().equals(oldBadge.getModuleId())) {
                                newBadge.updateCount(oldBadge.getCount());
                            }
                        }
                    }
                    olds.remove();
                }
            }

            badgeInfos.clear();
        }
        getBadges(context);
    }

    /**
     *  判断是否为应用容器
     * @param fileName 应用容器的配置文件名
     * @return
     */
    private boolean isContainer(String fileName) {
        try {
            String[] files = HcApplication.getContext().getAssets().list("");
            for (String file : files) {
//            	HcLog.D(TAG + " #isContainer file = "+file);
                if (file.equals(fileName)) {
                    HcLog.D(TAG + " #isContainer 找到了 "+fileName + " 是应用容器");
                    return true;
                }
            }
            return false;
        } catch(Exception e) {
            HcLog.D(TAG + " #isContainer e ="+e);
            return false;
        }
    }
}
