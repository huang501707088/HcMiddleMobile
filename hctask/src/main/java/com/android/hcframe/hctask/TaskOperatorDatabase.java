package com.android.hcframe.hctask;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.android.hcframe.HcLog;
import com.android.hcframe.hctask.state.CancelledState;
import com.android.hcframe.hctask.state.CompletedState;
import com.android.hcframe.hctask.state.EndState;
import com.android.hcframe.hctask.state.ProcessingState;
import com.android.hcframe.hctask.state.ReceivingState;
import com.android.hcframe.hctask.state.TaskState;
import com.android.hcframe.sql.HcDatabase.TaskInfo;
import com.android.hcframe.sql.HcProvider;
import com.android.hcframe.sql.SettingHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-7-27 14:36.
 */
public class TaskOperatorDatabase {

    private static final String TAG = "TaskOperatorDatabase";

    /**
     * 保存任务列表
     * @param context
     * @param tasks
     * @return
     */
    public static int insertTasks(Context context, List<TaskState> tasks) {
        final ContentResolver cr = context.getContentResolver();
        String where = TaskInfo.USER_ID + "=" + "'" + SettingHelper.getUserId(context) + "'";
        cr.delete(HcProvider.CONTENT_URI_TASK, where, null);
        if (tasks.isEmpty()) return 0;
        int size = tasks.size();
        final ContentValues[] values = new ContentValues[size];
        for (int i = 0; i < size; i++) {
            values[i] = new ContentValues();
            setTaskValues(tasks.get(i), values[i], context);
        }

        int number = cr.bulkInsert(HcProvider.CONTENT_URI_TASK, values);
        HcLog.D(TAG + " #insertTasks end insertTasks! insert number = " + number);
        return number;
    }

    /**
     * 更改任务的状态
     * @param task
     */
    public static int updateTask(Context context, TaskState task) {
        final ContentResolver cr = context.getContentResolver();
        final ContentValues values = new ContentValues();
        String where = TaskInfo.USER_ID + "=" + "'" + SettingHelper.getUserId(context) + "'"
                + " AND " + TaskInfo.ID + "=" + task.getId();
        values.put(TaskInfo.STATUS, task.getStatus());
        int number = cr.update(HcProvider.CONTENT_URI_TASK, values, where, null);
        HcLog.D(TAG + " #updateTask end updateTask! update number = " + number);
        return number;
    }

    public static List<TaskState> getTasks(Context context) {
        List<TaskState> tasks = new ArrayList<TaskState>();
        final ContentResolver cr = context.getContentResolver();
        String selection = TaskInfo.USER_ID + "=" + "'"
                + SettingHelper.getUserId(context) + "'";
        String[] projection = {TaskInfo.EXECUTOR, TaskInfo.RELEASE_DATE, TaskInfo.DEADLINE,
            TaskInfo.PUBLISHER_URL, TaskInfo.CONTENT, TaskInfo.PUBLISHER, TaskInfo.EXECUTOR_ID,
            TaskInfo.PUBLISHER_ID, TaskInfo.STATUS, TaskInfo.EXECUTOR_URL, TaskInfo.ID};
        Cursor c = cr.query(HcProvider.CONTENT_URI_TASK, projection, selection, null, null);
        if (c != null && c.getCount() > 0) {
            c.moveToNext();
            TaskState task;
            while (!c.isAfterLast()) {
                int status = c.getInt(8);
                switch (status) {
                    case TaskState.STATUS_CANCELLED:
                        task = new CancelledState();
                        break;
                    case TaskState.STATUS_COMPLETED:
                        task = new CompletedState();
                        break;
                    case TaskState.STATUS_END:
                        task = new EndState();
                        break;
                    case TaskState.STATUS_PROCESSING:
                        task = new ProcessingState();
                        break;
                    case TaskState.STATUS_RECEIVING:
                        task = new ReceivingState();
                        break;

                    default:
                        task = new EndState();
                        break;
                }

                task.setDescription(c.getString(4));
                task.setEndDate(c.getString(2));
                task.setExecutor(c.getString(0));
                task.setExecutorId(c.getString(6));
                task.setExecutorUrl(c.getString(9));
                task.setId(c.getString(10));
                task.setPublishDate(c.getString(1));
                task.setPublisher(c.getString(5));
                task.setPublisherId(c.getString(7));
                task.setPublisherUrl(c.getString(3));

                tasks.add(task);
                c.moveToNext();
            }
        }
        if (c != null)
            c.close();
        return tasks;
    }

    private static void setTaskValues(TaskState task, ContentValues values, Context context) {
        values.clear();
        values.put(TaskInfo.CONTENT, task.getDescription());
        values.put(TaskInfo.DEADLINE, task.getEndDate());
        values.put(TaskInfo.EXECUTOR, task.getExecutor());
        values.put(TaskInfo.EXECUTOR_ID, task.getExecutorId());
        values.put(TaskInfo.EXECUTOR_URL, task.getExecutorUrl());
        values.put(TaskInfo.PUBLISHER, task.getPublisher());
        values.put(TaskInfo.PUBLISHER_ID, task.getPublisherId());
        values.put(TaskInfo.PUBLISHER_URL, task.getPublisherUrl());
        values.put(TaskInfo.RELEASE_DATE, task.getPublishDate());
        values.put(TaskInfo.STATUS, task.getStatus());
        values.put(TaskInfo.USER_ID, SettingHelper.getUserId(context));
        values.put(TaskInfo.ID, task.getId());
    }
}
