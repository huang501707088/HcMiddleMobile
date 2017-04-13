package com.android.hcframe.hcmail;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;

import com.android.email.activity.MessageCompose;
import com.android.email.provider.AccountBackupRestore;
import com.android.email.service.AttachmentDownloadService;
import com.android.email.service.MailService;
import com.android.emailcommon.provider.Account;
import com.android.emailcommon.provider.EmailContent;
import com.android.emailcommon.utility.EmailAsyncTask;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 17-2-20 14:02.
 */

/**
 * 邮件箱说明：
 *     邮件服务器utf-7编辑  <------>   正常编码
 * <p>1. Sent Sent</p>
 * <p>2. Trash Trash</p>
 * <p>3. &T797Og- 便笺</p>
 * <p>4. &U9FO9nux- 发件箱</p>
 * <p>5. &V4NXPpCuTvY- 垃圾邮件</p>
 * <p>6. &gFR8+066- 联系人</p>
 * <p>7. &TvtSoQ- 任务</p>
 * <p>8. &ZeVThg- 日历</p>
 * <p>9. INBOX INBOX</p>
 * <p>10. &XfJT0ZABkK5O9g- 已发送邮件</p>
 * <p>11. &XfJSIJZkkK5O9g- 已删除邮件</p>
 * <p>12. &g0l6Pw- 草稿</p>
 * <p>13. &ZeVf1w- 日志</p>
 */
public class EmailUtils {

    public static final boolean DEBUG = true;

    /**
     * Specifies how many messages will be shown in a folder by default. This number is set
     * on each new folder and can be incremented with "Load more messages..." by the
     * VISIBLE_LIMIT_INCREMENT
     */
    public static final int VISIBLE_LIMIT_DEFAULT = 25;

    /**
     * Number of additional messages to load when a user selects "Load more messages..."
     */
    public static final int VISIBLE_LIMIT_INCREMENT = 25;

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

//        pm.setComponentEnabledSetting(new ComponentName(context, WidgetConfiguration.class),
//                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
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
     * Commits the UI-related settings of an account to the provider.  This is static so that it
     * can be used by the various account activities.  If the account has never been saved, this
     * method saves it; otherwise, it just saves the settings.
     * @param context the context of the caller
     * @param account the account whose settings will be committed
     */
    public static void commitSettings(Context context, Account account) {
        if (!account.isSaved()) {
            account.save(context);
        } else {
            ContentValues cv = getAccountContentValues(account);
            account.update(context, cv);
        }
        // Update the backup (side copy) of the accounts
        AccountBackupRestore.backup(context);
    }

    /**
     * Returns a set of content values to commit account changes (not including the foreign keys
     * for the two host auth's and policy) to the database.  Does not actually commit anything.
     */
    public static ContentValues getAccountContentValues(Account account) {
        ContentValues cv = new ContentValues();
        cv.put(EmailContent.AccountColumns.IS_DEFAULT, account.mIsDefault);
        cv.put(EmailContent.AccountColumns.DISPLAY_NAME, account.getDisplayName());
        cv.put(EmailContent.AccountColumns.SENDER_NAME, account.getSenderName());
        cv.put(EmailContent.AccountColumns.SIGNATURE, account.getSignature());
        cv.put(EmailContent.AccountColumns.SYNC_INTERVAL, account.mSyncInterval);
        cv.put(EmailContent.AccountColumns.RINGTONE_URI, account.mRingtoneUri);
        cv.put(EmailContent.AccountColumns.FLAGS, account.mFlags);
        cv.put(EmailContent.AccountColumns.SYNC_LOOKBACK, account.mSyncLookback);
        cv.put(EmailContent.AccountColumns.SECURITY_SYNC_KEY, account.mSecuritySyncKey);
        return cv;
    }
}
