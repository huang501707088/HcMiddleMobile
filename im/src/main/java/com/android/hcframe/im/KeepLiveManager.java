package com.android.hcframe.im;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-9-14 08:54.
 */
public final class KeepLiveManager {

    private static final String TAG = "KeepLiveManager";

    private static KeepLiveManager mManager = new KeepLiveManager();

    private static final int JOB_ID = 1;

    private KeepLiveManager() {}

    public static KeepLiveManager getInstance() {
        return mManager;
    }

    /**
     * 启动JobService,SDK > 21才能被调用
     * @param context
     */
    public void startJobService(Context context) {

        if (Build.VERSION.SDK_INT >= 21) {
            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo.Builder jb = new JobInfo.Builder(JOB_ID, new ComponentName(context, ChatService.class))
                    .setPeriodic(10)
                    .setPersisted(true);
            scheduler.schedule(jb.build());
        }

    }
}
