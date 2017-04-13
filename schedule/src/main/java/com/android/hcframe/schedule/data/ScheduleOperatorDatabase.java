package com.android.hcframe.schedule.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.android.hcframe.HcLog;
import com.android.hcframe.schedule.ScheduleDateInfo;
import com.android.hcframe.schedule.ScheduleInfo;
import com.android.hcframe.schedule.ScheduleUtils;
import com.android.hcframe.sql.HcDatabase.Schedule;
import com.android.hcframe.sql.HcProvider;
import com.android.hcframe.sql.SettingHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by zhujiabin on 2016/11/14.
 */

public class ScheduleOperatorDatabase {

    private static final String TAG = "SheduleOperatorDatabase";

    /**
     * 把发布的消息保存到数据库中
     *
     * @param context
     * @param infos   需要保存的数据
     * @return
     */
    public static int insertScheduleInfos(Context context, List<ScheduleInfo> infos) {
        String userId = SettingHelper.getUserId(context);
        String where = Schedule.USER_ID + "=" + "'" + userId + "'";
        final ContentResolver cr = context.getContentResolver();
        cr.delete(HcProvider.CONTENT_URI_SCHEDULE, where, null);

        int size = infos.size();
        if (size > 0) {
            ContentValues[] values = new ContentValues[size];
            for (int i = 0; i < size; i++) {
                values[i] = new ContentValues();
                setSheduleValues(values[i], infos.get(i), userId);
            }
            int num = cr.bulkInsert(HcProvider.CONTENT_URI_SCHEDULE, values);
            return num;
        }

        return 0;
    }

    private static void setSheduleValues(ContentValues values, ScheduleInfo info, String userId) {
        values.clear();
        values.put(Schedule.USER_ID, userId);
        values.put(Schedule.SCHEDULE_ID, info.getId());
        values.put(Schedule.SCHEDULE_STARTTIME, info.getStartTime());
        values.put(Schedule.SCHEDULE_ENDTIME, info.getEndTime());
        values.put(Schedule.SCHEDULE_TASKTYPE, info.getTaskType());
        values.put(Schedule.SCHEDULE_TASKMEMBERS, info.getTaskMembers());
        values.put(Schedule.SCHEDULE_THEME, info.getTheme());
        values.put(Schedule.SCHEDULE_CREATOR, info.getCreator());
        values.put(Schedule.SCHEDULE_CREATFLAG, info.getCreatFlag());
        values.put(Schedule.SCHEDULE_ADDITION, info.getAddition());
        values.put(Schedule.SCHEDULE_DATE, info.getDate());
    }

    /**
     * 保存一条数据到数据库中
     *
     * @param context
     * @param info    需要保存的数据
     * @return
     */
    public static Uri insertScheduleInfo(Context context, ScheduleInfo info) {
        String userId = SettingHelper.getUserId(context);
        String where = Schedule.USER_ID + "=" + "'" + userId + "'" +
                " AND " + Schedule.SCHEDULE_ID + "=" + "'" + info.getId() + "'" +
                " AND " + Schedule.SCHEDULE_DATE + "=" + "'" + info.getDate() + "'";
        final ContentResolver cr = context.getContentResolver();
        cr.delete(HcProvider.CONTENT_URI_SCHEDULE, where, null);
        ContentValues values = new ContentValues();
        setSheduleValues(values, info, userId);
        return cr.insert(HcProvider.CONTENT_URI_SCHEDULE, values);
    }

    /**
     * 保存一条“没有相关日程”的数据到数据库中
     *
     * @param context
     * @param info    需要保存的数据
     * @return
     */
    public static Uri insertNoScheduleInfo(Context context, ScheduleInfo info) {
        String userId = SettingHelper.getUserId(context);
        String where = Schedule.USER_ID + "=" + "'" + userId + "'" +
                " AND " + Schedule.SCHEDULE_THEME + "=" + "'" + info.getTheme() + "'" +
                " AND " + Schedule.SCHEDULE_DATE + "=" + "'" + info.getDate() + "'";
        final ContentResolver cr = context.getContentResolver();
        cr.delete(HcProvider.CONTENT_URI_SCHEDULE, where, null);
        ContentValues values = new ContentValues();
        setSheduleValues(values, info, userId);
        return cr.insert(HcProvider.CONTENT_URI_SCHEDULE, values);
    }

    /**
     * 删除一条消息数据
     *
     * @param context
     * @param info
     * @return
     */
    public static int deleteScheduleInfos(Context context, ScheduleInfo info) {
        String userId = SettingHelper.getUserId(context);
        String where = Schedule.USER_ID + "=" + "'" + userId + "'" +
                " AND " + Schedule.SCHEDULE_DATE + "=" + "'" + info.getDate() + "'";
        final ContentResolver cr = context.getContentResolver();
        return cr.delete(HcProvider.CONTENT_URI_SCHEDULE, where, null);
    }

    /**
     * 更新一条消息数据
     *
     * @param context
     * @param info
     * @return
     */
    public static int updateScheduleInfo(Context context, ScheduleInfo info) {
        String userId = SettingHelper.getUserId(context);
        String where = Schedule.USER_ID + "=" + "'" + userId + "'" +
                " AND " + Schedule.SCHEDULE_ID + "=" + "'" + info.getId() + "'";
        final ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(Schedule.SCHEDULE_STARTTIME, info.getStartTime());
        values.put(Schedule.SCHEDULE_ENDTIME, info.getEndTime());
        values.put(Schedule.SCHEDULE_TASKTYPE, info.getTaskType());
        values.put(Schedule.SCHEDULE_TASKMEMBERS, info.getTaskMembers());
        values.put(Schedule.SCHEDULE_THEME, info.getTheme());
        values.put(Schedule.SCHEDULE_CREATOR, info.getCreator());
        values.put(Schedule.SCHEDULE_CREATFLAG, info.getCreatFlag());
        values.put(Schedule.SCHEDULE_ADDITION, info.getAddition());
        return cr.update(HcProvider.CONTENT_URI_SCHEDULE, values, where, null);
    }

    /**
     * 获取表消息数据
     *
     * @param context
     * @return
     */
    public static List<ScheduleInfo> getScheduleInfos(Context context) {
        List<ScheduleInfo> infos = new ArrayList<ScheduleInfo>();
        String userId = SettingHelper.getUserId(context);
        String[] projection = {Schedule.SCHEDULE_ID, Schedule.SCHEDULE_STARTTIME, Schedule.SCHEDULE_ENDTIME, Schedule.SCHEDULE_TASKTYPE,
                Schedule.SCHEDULE_TASKMEMBERS, Schedule.SCHEDULE_THEME, Schedule.SCHEDULE_CREATOR,
                Schedule.SCHEDULE_CREATFLAG, Schedule.SCHEDULE_ADDITION, Schedule.SCHEDULE_DATE};
        String where = Schedule.USER_ID + "=" + "'" + userId + "'";
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(HcProvider.CONTENT_URI_SCHEDULE, projection, where, null, null);
        ScheduleInfo info;
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                info = new ScheduleInfo();
                info.setId(c.getString(0));
                info.setStartTime(c.getString(1));
                info.setEndTime(c.getString(2));
                info.setTaskType(c.getString(3));
                info.setTaskMembers(c.getString(4));
                info.setTheme(c.getString(5));
                info.setCreator(c.getString(6));
                info.setCreatFlag(c.getString(7));
                info.setAddition(c.getString(8));
                info.setDate(c.getString(9));
                infos.add(info);
                c.moveToNext();
            }
        }

        if (c != null)
            c.close();
        return infos;
    }

    public static Map<String, ScheduleDateInfo> getScheduleDateInfos(Context context) {
        Map<String, ScheduleDateInfo> schedule = new TreeMap<String, ScheduleDateInfo>();
        String userId = SettingHelper.getUserId(context);
        String[] projection = {Schedule.SCHEDULE_ID, Schedule.SCHEDULE_STARTTIME, Schedule.SCHEDULE_ENDTIME, Schedule.SCHEDULE_TASKTYPE,
                Schedule.SCHEDULE_TASKMEMBERS, Schedule.SCHEDULE_THEME, Schedule.SCHEDULE_CREATOR,
                Schedule.SCHEDULE_CREATFLAG, Schedule.SCHEDULE_ADDITION, Schedule.SCHEDULE_DATE};
        String where = Schedule.USER_ID + "=" + "'" + userId + "'";
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(HcProvider.CONTENT_URI_SCHEDULE, projection, where, null, null);
        ScheduleInfo info;
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                info = new ScheduleInfo();
                info.setId(c.getString(0));
                info.setStartTime(c.getString(1));
                info.setEndTime(c.getString(2));
                info.setTaskType(c.getString(3));
                info.setTaskMembers(c.getString(4));
                info.setTheme(c.getString(5));
                info.setCreator(c.getString(6));
                info.setCreatFlag(c.getString(7));
                info.setAddition(c.getString(8));
                info.setDate(c.getString(9));
                addSchedule(context, schedule, info);
                c.moveToNext();
            }
        }

        if (c != null)
            c.close();

        return schedule;
    }

    private static void addSchedule(Context context, Map<String, ScheduleDateInfo> cache, ScheduleInfo info) {
        long currentTime = System.currentTimeMillis();
        //3天之后的时间，过滤掉3天后的日程
        long futureTime = System.currentTimeMillis() + 60 * 60 * 24 * 1000 * 3;
        String time = ScheduleUtils.stampToDate(String.valueOf(currentTime));
        String fTime = ScheduleUtils.stampToDate(String.valueOf(futureTime));
        ScheduleDateInfo dateInfo = cache.get(info.getDate());
        try {
            if (Long.parseLong(info.getDate()) >= Long.parseLong(ScheduleUtils.dateToStamp(time))&&Long.parseLong(info.getDate())<Long.parseLong(ScheduleUtils.dateToStamp(fTime))) {
                HcLog.D("ScheduleOperatorDabase#addSchedule schedule date = " + info.getDate());
                if (dateInfo == null) {
                    dateInfo = new ScheduleDateInfo();
                    dateInfo.setmSheduleDate(info.getDate());
                    dateInfo.addScheduleInfo(info);
                    cache.put(info.getDate(), dateInfo);
                } else {
                    dateInfo.addScheduleInfo(info);
                }
            } else {
                deleteScheduleInfos(context, info);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
