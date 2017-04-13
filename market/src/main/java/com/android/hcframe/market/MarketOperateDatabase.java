package com.android.hcframe.market;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.data.AppInfo;
import com.android.hcframe.data.Html5AppInfo;
import com.android.hcframe.data.NativeAppInfo;
import com.android.hcframe.sql.HcDatabase;
import com.android.hcframe.sql.HcProvider;
import com.android.hcframe.sql.SettingHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-3-11 14:50.
 */
public final class MarketOperateDatabase {

    private static final String TAG = "MarketOperateDatabase";

    private static final boolean USED = true;

    private static boolean usedAccount = USED;

    /**
     * 获取全部应用,按{@code HcAppMarket.APP_ORDER_ALL}排序
     *
     * @author jrjin
     * @time 2015-5-4 上午10:52:26
     * @param context
     * @return
     */
    public static List<AppInfo> getAllApps(Context context) {
        List<AppInfo> infos = new ArrayList<AppInfo>();
        final ContentResolver cr = context.getContentResolver();
        String selection = usedAccount == false ? null
                : HcDatabase.HcAppMarket.APP_ACCOUNT + "=" + "'"
                + SettingHelper.getAccount(context) + "'";
        String[] selectionArg = usedAccount == false ? null
                : new String[] { "'" + SettingHelper.getAccount(context) + "'" };
        HcLog.D(TAG + " #getAllApps account = "
                + SettingHelper.getAccount(context));
        Cursor c = cr.query(HcProvider.CONTENT_URI_APP, null, selection, null,
                HcDatabase.HcAppMarket.APP_ORDER_ALL);
        AppInfo info;
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                int type = c.getInt(c.getColumnIndex(HcDatabase.HcAppMarket.APP_TYPE));
                if (type == 0) {
                    info = new NativeAppInfo();
                } else {
                    info = new Html5AppInfo();
                }
                info.setAppType(type);
                info.setAppCategory(c.getInt(c
                        .getColumnIndex(HcDatabase.HcAppMarket.APP_CATEGORY)));
                info.setAppIcon(c.getString(c
                        .getColumnIndex(HcDatabase.HcAppMarket.APP_ICON)));
                info.setAppCategory(c.getInt(c
                        .getColumnIndex(HcDatabase.HcAppMarket.APP_CATEGORY)));
                info.setAppIcon(c.getString(c
                        .getColumnIndex(HcDatabase.HcAppMarket.APP_ICON)));
                info.setAppId(c.getString(c.getColumnIndex(HcDatabase.HcAppMarket.APP_ID)));
                info.setAppName(c.getString(c
                        .getColumnIndex(HcDatabase.HcAppMarket.APP_NAME)));
                info.setAppPackage(c.getString(c
                        .getColumnIndex(HcDatabase.HcAppMarket.APP_PACKAGE)));
                info.setAppUrl(c.getString(c
                        .getColumnIndex(HcDatabase.HcAppMarket.APP_URL)));
                info.setAppVersion(c.getString(c
                        .getColumnIndex(HcDatabase.HcAppMarket.APP_VERSION)));
                // info.setLatestVersion(c.getString(c.getColumnIndex(HcAppMarket.APP_LATEST_VERSION)));
                info.setAppState(c.getInt(c
                        .getColumnIndex(HcDatabase.HcAppMarket.APP_STATE)));
                info.setAppSize(c.getInt(c.getColumnIndex(HcDatabase.HcAppMarket.APP_SIZE)));
                info.setAllOrder(c.getInt(c
                        .getColumnIndex(HcDatabase.HcAppMarket.APP_ORDER_ALL)));
                info.setCategoryOrder(c.getInt(c
                        .getColumnIndex(HcDatabase.HcAppMarket.APP_ORDER_CATEGORY)));
                info.setUsed(c.getInt(c.getColumnIndex(HcDatabase.HcAppMarket.APP_USED)));
                info.setCategoryName(c.getString(c
                        .getColumnIndex(HcDatabase.HcAppMarket.APP_CATEGORY_NAME)));
                info.setServerOrder(c.getInt(c
                        .getColumnIndex(HcDatabase.HcAppMarket.APP_ORDER_SERVER)));
                HcLog.D(TAG
                        + " #getAllApps account = "
                        + c.getString(c.getColumnIndex(HcDatabase.HcAppMarket.APP_ACCOUNT)));
                infos.add(info);
                c.moveToNext();
            }

        }
        if (c != null)
            c.close();
        HcLog.writeDebug("getAllApps end where = "
                + selection
                + " ; app count = "
                + getApps(context)
                + " time = "
                + HcUtil.getDate(HcUtil.FORMAT_POLLUTION,
                System.currentTimeMillis()));
        HcLog.D(TAG + " #getAllApps size = " + infos.size());
        return infos;
    }

    private static Uri insertApp(AppInfo info, ContentValues values,
                                 ContentResolver cr) {
        values.clear();
        values.put(HcDatabase.HcAppMarket.APP_CATEGORY, info.getAppCategory());
        values.put(HcDatabase.HcAppMarket.APP_ICON, info.getAppBase64Icon());
        values.put(HcDatabase.HcAppMarket.APP_ID, info.getAppId());
        values.put(HcDatabase.HcAppMarket.APP_NAME, info.getAppName());
        values.put(HcDatabase.HcAppMarket.APP_PACKAGE, info.getAppPackage());
        values.put(HcDatabase.HcAppMarket.APP_TYPE, info.getAppType());
        values.put(HcDatabase.HcAppMarket.APP_URL, info.getAppUrl());
        values.put(HcDatabase.HcAppMarket.APP_VERSION, info.getAppVersion());
        // values.put(HcAppMarket.APP_LATEST_VERSION, info.getLatestVersion());
        values.put(HcDatabase.HcAppMarket.APP_STATE, info.getAppState());
        values.put(HcDatabase.HcAppMarket.APP_ORDER_ALL, info.getAllOrder());
        values.put(HcDatabase.HcAppMarket.APP_ORDER_CATEGORY, info.getCategoryOrder());
        values.put(HcDatabase.HcAppMarket.APP_SIZE, info.getAppSize());
        values.put(HcDatabase.HcAppMarket.APP_USED, (info.hasUsed() == false ? 0 : 1));
        HcLog.D(TAG + " #insertApp account = "
                + SettingHelper.getAccount(HcApplication.getContext()));
        values.put(HcDatabase.HcAppMarket.APP_ACCOUNT,
                SettingHelper.getAccount(HcApplication.getContext()));
        values.put(HcDatabase.HcAppMarket.APP_CATEGORY_NAME, info.getCategoryName());
        values.put(HcDatabase.HcAppMarket.APP_ORDER_SERVER, info.getServerOrder());
        return cr.insert(HcProvider.CONTENT_URI_APP, values);
    }

    private static void setAppValues(AppInfo info, ContentValues values) {
        values.clear();
        values.put(HcDatabase.HcAppMarket.APP_CATEGORY, info.getAppCategory());
        values.put(HcDatabase.HcAppMarket.APP_ICON, info.getAppBase64Icon());
        values.put(HcDatabase.HcAppMarket.APP_ID, info.getAppId());
        values.put(HcDatabase.HcAppMarket.APP_NAME, info.getAppName());
        values.put(HcDatabase.HcAppMarket.APP_PACKAGE, info.getAppPackage());
        values.put(HcDatabase.HcAppMarket.APP_TYPE, info.getAppType());
        values.put(HcDatabase.HcAppMarket.APP_URL, info.getAppUrl());
        values.put(HcDatabase.HcAppMarket.APP_VERSION, info.getAppVersion());
        // values.put(HcAppMarket.APP_LATEST_VERSION, info.getLatestVersion());
        values.put(HcDatabase.HcAppMarket.APP_STATE, info.getAppState());
        values.put(HcDatabase.HcAppMarket.APP_ORDER_ALL, info.getAllOrder());
        values.put(HcDatabase.HcAppMarket.APP_ORDER_CATEGORY, info.getCategoryOrder());
        values.put(HcDatabase.HcAppMarket.APP_SIZE, info.getAppSize());
        values.put(HcDatabase.HcAppMarket.APP_USED, (info.hasUsed() == false ? 0 : 1));
        HcLog.D(TAG + " #insertApp account = "
                + SettingHelper.getAccount(HcApplication.getContext()));
        values.put(HcDatabase.HcAppMarket.APP_ACCOUNT,
                SettingHelper.getAccount(HcApplication.getContext()));
        values.put(HcDatabase.HcAppMarket.APP_CATEGORY_NAME, info.getCategoryName());
        values.put(HcDatabase.HcAppMarket.APP_ORDER_SERVER, info.getServerOrder());
    }

    /**
     * 退出应用的时候添加列表
     *
     * @author jrjin
     * @time 2015-5-6 上午12:37:47
     * @param infos
     * @param context
     */
    public static int insertAppsOnDestory(List<AppInfo> infos, Context context) {
        if (infos == null || infos.size() == 0)
            return 0; // 数据空就不用操作数据库了
//		final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        String where = usedAccount == false ? null : HcDatabase.HcAppMarket.APP_ACCOUNT
                + "=" + "'" + SettingHelper.getAccount(context) + "'";
        HcLog.writeDebug("insertAppsOnDestory before delete apps where = "
                + where
                + " ; app count = "
                + getApps(context)
                + " time = "
                + HcUtil.getDate(HcUtil.FORMAT_POLLUTION,
                System.currentTimeMillis()));
        cr.delete(HcProvider.CONTENT_URI_APP, where, null);
        /**
         * @date 2016-1-28 上午11:31:06
         * 替换为下面的事物处理
        for (AppInfo info : infos) {
        insertApp(info, values, cr);
        }
         */
        int size = infos.size();
        final ContentValues[] values = new ContentValues[size];
        for (int i = 0; i < size; i++) {
            values[i] = new ContentValues();
            setAppValues(infos.get(i), values[i]);
        }

        int number = cr.bulkInsert(HcProvider.CONTENT_URI_APP, values);

        HcLog.writeDebug("insertAppsOnDestory after delete apps where = "
                + where
                + " ; app count = "
                + getApps(context)
                + " time = "
                + HcUtil.getDate(HcUtil.FORMAT_POLLUTION,
                System.currentTimeMillis()));
        HcLog.D(TAG + " #insertAppsOnDestory end insertAppsOnDestory! insert number = "+number);
        return number;
    }

    /**
     * 删除当前用户的应用，因为当前用户没有数据
     *
     * @author jrjin
     * @time 2015-6-19 上午9:00:44
     * @param context
     * @return
     */
    public static int deleteApps(Context context) {
        final ContentResolver cr = context.getContentResolver();
        String where = usedAccount == false ? null : HcDatabase.HcAppMarket.APP_ACCOUNT
                + "=" + "'" + SettingHelper.getAccount(context) + "'";
        HcLog.writeDebug("deleteApps before delete apps where = "
                + where
                + " ; app count = "
                + getApps(context)
                + " time = "
                + HcUtil.getDate(HcUtil.FORMAT_POLLUTION,
                System.currentTimeMillis()));
        return cr.delete(HcProvider.CONTENT_URI_APP, where, null);
    }

    private static int getApps(Context context) {
        int count = 0;
        final ContentResolver cr = context.getContentResolver();
        String selection = usedAccount == false ? null
                : HcDatabase.HcAppMarket.APP_ACCOUNT + "=" + "'"
                + SettingHelper.getAccount(context) + "'";
        Cursor c = cr.query(HcProvider.CONTENT_URI_APP, null, selection, null,
                null);
        if (c != null) {
            count = c.getCount();
            c.close();
        }
        return count;
    }

    /**
     * @deprecated 从服务端返回应用的时候添加全部应用信息 这里需要进行比较
     *
     * @author jrjin
     * @time 2015-5-4 上午10:55:17
     * @param infos
     *            从服务端返回的数据列表，已经设置了排序
     * @param context
     */
    @Deprecated
    public static void insertAppsFormServer(List<AppInfo> infos, Context context) {
        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        List<AppInfo> appInfos = getAllApps(context);
        HcLog.D(TAG + " insertAppsFormServer old size = " + appInfos.size()
                + " new size =" + infos.size());
        if (appInfos.size() == 0) {
            for (AppInfo info : infos) {
                HcLog.D(TAG + " insertAppsFormServer all order = "
                        + info.getAllOrder());
                insertApp(info, values, cr);
            }
        } else {
            Iterator<AppInfo> oldIterator = appInfos.iterator();
            Iterator<AppInfo> newIterator = infos.iterator();
            AppInfo oldInfo;
            AppInfo newInfo;
            /** 存储新列表和老列表都有的在老列表中的App */
            List<AppInfo> same = new ArrayList<AppInfo>();
            /** 存储心列表的APP ID，主要是用来判断新增加的APP的顺序 */
            List<String> appIds = new ArrayList<String>();

            while (newIterator.hasNext()) {
                appIds.add(newIterator.next().getAppId());
            }

            while (oldIterator.hasNext()) {
                oldInfo = oldIterator.next();
                newIterator = infos.iterator();
                // HcLog.D(TAG + " old appid = "+oldInfo.getAppId() +
                // " new apps size = "+infos.size());
                while (newIterator.hasNext()) {
                    newInfo = newIterator.next();
                    // HcLog.D(TAG + " old appid = "+oldInfo.getAppId() +
                    // " new appid = "+newInfo.getAppId());
                    if (oldInfo.getAppId().equals(newInfo.getAppId())) {
                        if (!oldInfo.getAppVersion().equals(
                                newInfo.getAppVersion())) {
                            // 说明说要更新数据
                            HcLog.D(TAG + " old version = "
                                    + oldInfo.getAppVersion()
                                    + " new version = "
                                    + newInfo.getAppVersion());
                            // if (oldInfo.getAppType() != newInfo.getAppType())
                            // throw new
                            // IllegalAccessError(" insertApps app type must be same!");
                            if (oldInfo.getAppType() == 0
                                    && newInfo.getAppType() == 0) {
                                // 原生应用
                                if (oldInfo.getAppState() != HcUtil.APP_NORMAL) {
                                    newInfo.setAppState(HcUtil.APP_UPDATE);
                                }
                            } else {
                                newInfo.setAppState(HcUtil.APP_NORMAL);
                            }
                            newInfo.setAllOrder(oldInfo.getAllOrder());
                            newInfo.setCategoryOrder(oldInfo.getCategoryOrder()); // 类型不能变更
                            newInfo.setUsed(oldInfo.hasUsed() == false ? 0 : 1);
                            // HcLog.D(TAG +
                            // " insertAppsFormServer before update");
                            // 用newInfo更新数据库
                            updateAppInfo(newInfo, context);
                        }

                        newIterator.remove();
                        // oldIterator.remove(); // 这里会出错
                        same.add(oldInfo); // 更改成现在这样
                    }
                }
            }
            HcLog.D(TAG + " same size = " + same.size());
            for (AppInfo appInfo : same) {
                AppInfo delete;
                oldIterator = appInfos.iterator();
                while (oldIterator.hasNext()) {
                    delete = oldIterator.next();
                    if (appInfo.getAppId().equals(delete.getAppId()))
                        oldIterator.remove();
                }
            }

            int deleteCount = 0;
            /** 删除多余的数据 */
            oldIterator = appInfos.iterator();
            while (oldIterator.hasNext()) {
                deleteAppInfo(oldIterator.next(), context);
                deleteCount++;
            }
            HcLog.D(TAG + " insertAppsFormServer delete size = " + deleteCount);

            /** 检索出需要插入前面的App */
            same.clear(); // 避免过多的对象，这里就不新建List，复用原先的。
            matchNewApp(appIds, infos, same);

            /** 添加新的数据,需要重新设置 全部列表和类别列表的顺序 */
            int count = 0;
            Cursor c = cr.query(HcProvider.CONTENT_URI_APP, null, null, null,
                    null);
            if (c != null) {
                count = c.getColumnCount();
                c.close();
            }
            AppInfo insert;
            int categoryOrder = -1;
            int newCount = 0;
            newIterator = infos.iterator();
            while (newIterator.hasNext()) {
                insert = newIterator.next();
                insert.setAllOrder(count);
                categoryOrder = selectAppOrder(cr, insert.getAppCategory());
                insert.setCategoryOrder(++categoryOrder); // 这里包括返回-1的时候，顺序为0
                insertApp(insert, values, cr);
                count++;
                newCount++;
            }
            HcLog.D(TAG + " insertAppsFormServer insert end size = " + newCount);

            int insertStartCount = same.size();
            if (insertStartCount > 0) {
                appInfos.clear();
                appInfos.addAll(getAllApps(context));
                for (int i = insertStartCount - 1; i >= 0; i--) {
                    filterAppInfo(appInfos, same.get(i));
                }
                // 重新排序
                for (int i = 0, n = appInfos.size(); i < n; i++) {
                    appInfos.get(i).setAllOrder(i);
                }

                // 更新数据库
                insertAppsOnDestory(appInfos, context);

            }
            same.clear();
            appInfos.clear();
        }

    }

    /**
     * 重新排序类别
     *
     * @author jrjin
     * @time 2015-5-27 上午10:25:54
     * @param appInfos
     * @param insert
     */
    private static void filterAppInfo(List<AppInfo> appInfos, AppInfo insert) {
        List<AppInfo> infos = new ArrayList<AppInfo>();
        for (AppInfo info : appInfos) {
            // HcLog.D(TAG +
            // " filterAppInfo info category = "+info.getAppCategory() +
            // " category = "+category);
            if (info.getAppCategory() == insert.getAppCategory()) {

                infos.add(info);

            }
        }

        Collections.sort(infos, new Comparator<AppInfo>() {

            @Override
            public int compare(AppInfo lhs, AppInfo rhs) {
                // TODO Auto-generated method stub
                return lhs.getCategoryOrder() < rhs.getCategoryOrder() ? -1
                        : (lhs.getCategoryOrder() == rhs.getCategoryOrder() ? 0
                        : 1);
            }

        });
        int size = infos.size();
        if (size > 0) { // 需要重新排序
            infos.add(0, insert);
            for (int i = 0; i < size; i++) {
                infos.get(i).setCategoryOrder(i);
            }
        } // 要是没有匹配的类别，不需要再排序，因为已经在HcHttpRequest#MarketAppList#filterApps(List<AppInfo>
        // infos, int category)
        /** 类别的顺序已经更改了，可以添加到全部列表里了 */
        appInfos.add(0, insert);
        infos.clear();
    }

    /**
     * 匹配插入在前面的Apps
     *
     * @author jrjin
     * @time 2015-5-27 上午9:42:19
     * @param appIds
     *            全部应用app id
     * @param newApps
     *            需要插入的新的App
     * @param insertApps
     *            存储需要插入前面的App
     */
    private static void matchNewApp(List<String> appIds, List<AppInfo> newApps,
                                    List<AppInfo> insertApps) {
        if (appIds.size() == 0 || newApps.size() == 0)
            return;
        if (appIds.get(0).equals(newApps.get(0).getAppId())) {
            appIds.remove(0);
            insertApps.add(newApps.remove(0));
            matchNewApp(appIds, newApps, insertApps);
        }
    }

    private static int selectAppOrder(ContentResolver cr, int category) {
        String columns[] = { HcDatabase.HcAppMarket.APP_ORDER_CATEGORY };
        String selection = usedAccount == false ? HcDatabase.HcAppMarket.APP_CATEGORY
                + "=" + category : HcDatabase.HcAppMarket.APP_CATEGORY + "=" + category
                + " AND " + HcDatabase.HcAppMarket.APP_ACCOUNT + "=" + "'"
                + SettingHelper.getAccount(HcApplication.getContext()) + "'";
        ;
        Cursor c = cr.query(HcProvider.CONTENT_URI_APP, columns, selection,
                null, HcDatabase.HcAppMarket.APP_ORDER_CATEGORY + " DESC");
        HcLog.D(TAG + " count = " + c.getCount());
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            int order = c.getInt(0);
            c.close();
            return order;
        }

        if (c != null)
            c.close();
        return -1;
    }

    /**
     * @deprecated not used
     * @author jrjin
     * @time 2015-5-12 上午10:22:16
     * @param infos
     * @return
     */
    private static List<AppInfo> filterNativeApps(List<AppInfo> infos) {
        List<AppInfo> appInfos = new ArrayList<AppInfo>();
        for (AppInfo info : infos) {
            if (info.getAppType() == 0)
                appInfos.add(info);
        }
        return appInfos;
    }

    /**
     * @deprecated
     * See {@link MarketOperateDatabase#getAllApps(Context)}
     *             <p>
     *             获取已安装的原生应用
     *             </p>
     * @author jrjin
     * @time 2015-5-4 上午10:50:42
     * @param context
     * @return
     */
    public static List<AppInfo> getInstallApps(Context context) {
        final ContentResolver cr = context.getContentResolver();
        List<AppInfo> infos = new ArrayList<AppInfo>();
        Cursor c = cr.query(HcProvider.CONTENT_URI_APP, null,
                HcDatabase.HcAppMarket.APP_STATE + "!=0", null, null);
        AppInfo info;
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                info = new NativeAppInfo();
                info.setAppCategory(c.getInt(c
                        .getColumnIndex(HcDatabase.HcAppMarket.APP_CATEGORY)));
                info.setAppIcon(c.getString(c
                        .getColumnIndex(HcDatabase.HcAppMarket.APP_ICON)));
                info.setAppId(c.getString(c.getColumnIndex(HcDatabase.HcAppMarket.APP_ID)));
                info.setAppName(c.getString(c
                        .getColumnIndex(HcDatabase.HcAppMarket.APP_NAME)));
                info.setAppPackage(c.getString(c
                        .getColumnIndex(HcDatabase.HcAppMarket.APP_PACKAGE)));
                info.setAppUrl(c.getString(c
                        .getColumnIndex(HcDatabase.HcAppMarket.APP_URL)));
                info.setAppVersion(c.getString(c
                        .getColumnIndex(HcDatabase.HcAppMarket.APP_VERSION)));
                info.setAppType(0);
                // info.setLatestVersion(c.getString(c.getColumnIndex(HcAppMarket.APP_LATEST_VERSION)));
                info.setAppState(c.getInt(c
                        .getColumnIndex(HcDatabase.HcAppMarket.APP_STATE)));
                infos.add(info);
                c.moveToNext();
            }
        }
        if (c != null)
            c.close();
        return infos;
    }

    /**
     * 更新应用信息
     *
     * @author jrjin
     * @time 2015-5-4 上午11:07:56
     * @param info
     * @param context
     * @return
     */
    public static int updateAppInfo(AppInfo info, Context context) {
        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        String where = usedAccount == false ? HcDatabase.HcAppMarket.APP_ID + "=?"
                : HcDatabase.HcAppMarket.APP_ID + "=?" + " AND " + HcDatabase.HcAppMarket.APP_ACCOUNT
                + "=" + "'" + SettingHelper.getAccount(context) + "'";
        String[] selection = usedAccount == false ? new String[] { info
                .getAppId() } : new String[] { info.getAppId() /*
																 * , "'" +
																 * SettingHelper
																 * .getAccount(
																 * context) +
																 * "'"
																 */};
        values.put(HcDatabase.HcAppMarket.APP_ICON, info.getAppIconUrl());
        values.put(HcDatabase.HcAppMarket.APP_CATEGORY, info.getAppCategory());
        values.put(HcDatabase.HcAppMarket.APP_STATE, info.getAppState());
        values.put(HcDatabase.HcAppMarket.APP_VERSION, info.getAppVersion());
        values.put(HcDatabase.HcAppMarket.APP_NAME, info.getAppName());
        values.put(HcDatabase.HcAppMarket.APP_ORDER_ALL, info.getAllOrder());
        values.put(HcDatabase.HcAppMarket.APP_ORDER_CATEGORY, info.getCategoryOrder());
        values.put(HcDatabase.HcAppMarket.APP_PACKAGE, info.getAppPackage());
        values.put(HcDatabase.HcAppMarket.APP_SIZE, info.getAppSize());
        values.put(HcDatabase.HcAppMarket.APP_TYPE, info.getAppType());
        // HcLog.D(TAG + " updateAppInfo url = "+info.getAppUrl());
        values.put(HcDatabase.HcAppMarket.APP_URL, info.getAppUrl());
        values.put(HcDatabase.HcAppMarket.APP_USED, info.hasUsed() == false ? 0 : 1);
        values.put(HcDatabase.HcAppMarket.APP_CATEGORY_NAME, info.getCategoryName());
        values.put(HcDatabase.HcAppMarket.APP_ORDER_SERVER, info.getServerOrder());
        int row = cr.update(HcProvider.CONTENT_URI_APP, values, where,
                selection);
        HcLog.D(TAG + " #updateAppInfo row = " + row);
        return row;

    }

    public static int deleteAppInfo(AppInfo info, Context context) {
        final ContentResolver cr = context.getContentResolver();
        String where = usedAccount == false ? HcDatabase.HcAppMarket.APP_ID + "=?"
                : HcDatabase.HcAppMarket.APP_ID + "=?" + " AND " + HcDatabase.HcAppMarket.APP_ACCOUNT
                + "=" + "'" + SettingHelper.getAccount(context) + "'";
        String[] selection = usedAccount == false ? new String[] { info
                .getAppId() } : new String[] { info.getAppId() /*
																 * , "'" +
																 * SettingHelper
																 * .getAccount(
																 * context) +
																 * "'"
																 */};
        HcLog.D(TAG + " deleteAppInfo app id = " + info.getAppId());
        return cr.delete(HcProvider.CONTENT_URI_APP, where, selection);
    }

    public static int updateAppUsed(AppInfo info, Context context) {
        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        String where = usedAccount == false ? HcDatabase.HcAppMarket.APP_ID + "=?"
                : HcDatabase.HcAppMarket.APP_ID + "=?" + " AND " + HcDatabase.HcAppMarket.APP_ACCOUNT
                + "=" + "'" + SettingHelper.getAccount(context) + "'";
        String[] selection = usedAccount == false ? new String[] { info
                .getAppId() } : new String[] { info.getAppId() /*
																 * , "'" +
																 * SettingHelper
																 * .getAccount(
																 * context) +
																 * "'"
																 */};
        values.put(HcDatabase.HcAppMarket.APP_USED, info.hasUsed() == false ? 0 : 1);
        return cr.update(HcProvider.CONTENT_URI_APP, values, where, selection);
    }

    /**
     * @deprecated
     * @author jrjin
     * @time 2015-6-18 下午5:12:10
     * @param context
     * @return
     */
    @Deprecated
    public static List<AppInfo> getServerApps(Context context) {
        List<AppInfo> infos = new ArrayList<AppInfo>();
        final ContentResolver cr = context.getContentResolver();
        String selection = usedAccount == false ? null
                : HcDatabase.HcInstallApp.APP_ACCOUNT + "="
                + SettingHelper.getAccount(context);
        Cursor c = cr.query(HcProvider.CONTENT_URI_INSTALL_APP, null,
                selection, null, null);
        AppInfo info;
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            int order = 0;
            while (!c.isAfterLast()) {
                info = new AppInfo();
                info.setAppId(c.getString(c.getColumnIndex(HcDatabase.HcInstallApp.APP_ID)));
                info.setAllOrder(order);

                infos.add(info);
                order++;
                c.moveToNext();
            }

        }
        if (c != null)
            c.close();
        return infos;
    }

    /**
     * @deprecated 存储服务端应用列表的顺序
     * @author jrjin
     * @time 2015-5-28 下午3:22:13
     * @param infos
     * @param context
     */
    @Deprecated
    public static void insertServerApps(List<AppInfo> infos, Context context) {
        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        String where = usedAccount == false ? null : HcDatabase.HcAppMarket.APP_ACCOUNT
                + "=" + "'" + SettingHelper.getAccount(context) + "'";
        cr.delete(HcProvider.CONTENT_URI_INSTALL_APP, where, null);
        for (AppInfo info : infos) {
            insertServerApp(info, values, cr);
        }
    }

    @Deprecated
    private static Uri insertServerApp(AppInfo info, ContentValues values,
                                       ContentResolver cr) {
        values.clear();
        values.put(HcDatabase.HcInstallApp.APP_ID, info.getAppId());
        // values.put(HcInstallApp.APP_NAME, info.getAppName());
        // values.put(HcInstallApp.APP_PACKAGE, info.getAppPackage());
        // values.put(HcInstallApp.APP_VERSION, info.getAppVersion());
        values.put(HcDatabase.HcInstallApp.APP_ACCOUNT,
                SettingHelper.getAccount(HcApplication.getContext()));
        return cr.insert(HcProvider.CONTENT_URI_INSTALL_APP, values);
    }
}
