package com.android.hcframe.monitor;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.android.hcframe.HcLog;
import com.android.hcframe.sql.HcDatabase.HcAppOperLog;
import com.android.hcframe.sql.HcProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-2-19 15:29.
 */
public class LogOperatorDatabase {

    /**
     *获取未上传的日志
     * @param context
     * @return 未上传的日志
     */
    public static List<OperationLogInfo> getOperationLogs(Context context) {
        List<OperationLogInfo> infos = new ArrayList<OperationLogInfo>();
        final ContentResolver cr = context.getContentResolver();
        String[] projection = {HcAppOperLog.MODULE_ID, HcAppOperLog.ACCOUNT, HcAppOperLog.APP_ID,
            HcAppOperLog.END, HcAppOperLog.IMEI, HcAppOperLog.START, HcAppOperLog.TYPE,
            HcAppOperLog.VERSION, HcAppOperLog.NAME, HcAppOperLog.RESULT};
        Cursor c = cr.query(HcProvider.CONTENT_URI_LOG, projection, null, null, null);
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            OperationLogInfo info;
            while (!c.isAfterLast()) {
                info = new OperationLogInfo();
                info.setModuleId(c.getString(0));
                info.setAccount(c.getString(1));
                info.setEndTime(c.getString(3));
                info.setImei(c.getString(4));
                info.setStartTime(c.getString(5));
                info.setType(c.getInt(6));
                info.setVersion(c.getString(7));
                info.setName(c.getString(8));
                info.setResult(c.getInt(9));
                infos.add(info);
                c.moveToNext();
            }
        }
        if (c != null)
            c.close();
        return infos;
    }


    /**
     * 保存未上传的日志到数据库中
     * @param context
     * @param logs 日志列表
     * @return
     */
    public static int insertLogs(Context context, List<OperationLogInfo> logs) {
        if (logs == null || logs.size() == 0) return 0;
        final ContentResolver cr = context.getContentResolver();
        cr.delete(HcProvider.CONTENT_URI_LOG, null, null);
        int size = logs.size();
        ContentValues[] values = new ContentValues[size];
        for (int i = 0; i < size; i++) {
            values[i] = new ContentValues();
            setLogValue(logs.get(i), values[i]);
        }
        int number = cr.bulkInsert(HcProvider.CONTENT_URI_LOG, values);
        HcLog.D("LogOperateDatabase#insertLogs insert size = " + size + " success number = " + number);
        return number;
    }

    private static void setLogValue(OperationLogInfo log, ContentValues values) {
        values.clear();
        values.put(HcAppOperLog.ACCOUNT, log.getAccount());
        values.put(HcAppOperLog.APP_ID, log.getAppId());
        values.put(HcAppOperLog.END, log.getEndTime());
        values.put(HcAppOperLog.IMEI, log.getImei());
        values.put(HcAppOperLog.MODULE_ID, log.getModuleId());
        values.put(HcAppOperLog.START, log.getStartTime());
        values.put(HcAppOperLog.TYPE, log.getType());
        values.put(HcAppOperLog.VERSION, log.getVersion());
        values.put(HcAppOperLog.NAME, log.getName());
        values.put(HcAppOperLog.RESULT, log.getResult());
    }
}
