package com.android.hcframe.hcmail;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Parcelable;

import com.android.email.Controller;
import com.android.email.RefreshManager;
import com.android.email.activity.MessageCompose;
import com.android.email.service.AttachmentDownloadService;
import com.android.email.service.MailService;
import com.android.emailcommon.TempDirectory;
import com.android.emailcommon.provider.Account;
import com.android.emailcommon.utility.EmailAsyncTask;
import com.android.hcframe.command.Command;

import java.util.Map;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 17-2-20 14:00.
 */

public class EmailCommand implements Command {

    public EmailCommand() {}

    @Override
    public void execute(Context context) {
        initEmail(context);
    }

    @Override
    public void execute(Context context, Parcelable p) {
        initEmail(context);
    }

    @Override
    public void execute(Context context, Map<String, String> data) {
        initEmail(context);
    }

    /**
     *
     * @param context Application context
     */
    private void initEmail(Context context) {
        TempDirectory.setTempDirectory(context);
        // Tie MailRefreshManager to the Controller.
        RefreshManager.getInstance(context);
        // Reset all accounts to default visible window
        Controller.getInstance(context).resetVisibleLimits();

        // Make sure all required services are running when the app is started (can prevent
        // issues after an adb sync/install)
        setServicesEnabledAsync(context);
    }

    /**
     * Asynchronous version of {@link #setServicesEnabledSync(Context)}.  Use when calling from
     * UI thread (or lifecycle entry points.)
     *
     * @param context
     */
    public static void setServicesEnabledAsync(final Context context) {
        EmailAsyncTask.runAsyncParallel(new Runnable() {
            @Override
            public void run() {
                setServicesEnabledSync(context);
            }
        });
    }

    /**
     * Called throughout the application when the number of accounts has changed. This method
     * enables or disables the Compose activity, the boot receiver and the service based on
     * whether any accounts are configured.
     *
     * Blocking call - do not call from UI/lifecycle threads.
     *
     * @param context
     * @return true if there are any accounts configured.
     */
    public static boolean setServicesEnabledSync(Context context) {
        Cursor c = null;
        try {
            c = context.getContentResolver().query(
                    Account.CONTENT_URI,
                    Account.ID_PROJECTION,
                    null, null, null);
            boolean enable = c != null && c.getCount() > 0;
            setServicesEnabled(context, enable);
            return enable;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private static void setServicesEnabled(Context context, boolean enabled) {
        PackageManager pm = context.getPackageManager();
        if (!enabled && pm.getComponentEnabledSetting(
                new ComponentName(context, MailService.class)) ==
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            /*
             * If no accounts now exist but the service is still enabled we're about to disable it
             * so we'll reschedule to kill off any existing alarms.
             */
            MailService.actionReschedule(context);
        }
        pm.setComponentEnabledSetting(
                new ComponentName(context, MessageCompose.class),
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(
                new ComponentName(context, MailService.class),
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(
                new ComponentName(context, AttachmentDownloadService.class),
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        if (enabled && pm.getComponentEnabledSetting(
                new ComponentName(context, MailService.class)) ==
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            /*
             * And now if accounts do exist then we've just enabled the service and we want to
             * schedule alarms for the new accounts.
             */
            MailService.actionReschedule(context);
        }

        // Start/stop the various services depending on whether there are any accounts
        startOrStopService(enabled, context, new Intent(context, AttachmentDownloadService.class));

    }

    /**
     * Starts or stops the service as necessary.
     * @param enabled If {@code true}, the service will be started. Otherwise, it will be stopped.
     * @param context The context to manage the service with.
     * @param intent The intent of the service to be managed.
     */
    private static void startOrStopService(boolean enabled, Context context, Intent intent) {
        if (enabled) {
            context.startService(intent);
        } else {
            context.stopService(intent);
        }
    }
}
