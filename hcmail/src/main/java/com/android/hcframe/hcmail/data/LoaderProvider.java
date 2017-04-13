package com.android.hcframe.hcmail.data;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.android.email.MessageListContext;
import com.android.email.data.ThrottlingCursorLoader;
import com.android.emailcommon.provider.Account;
import com.android.emailcommon.provider.EmailContent;
import com.android.emailcommon.provider.Mailbox;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.hcmail.EmailUtils;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 17-3-28 10:29.
 */

public class LoaderProvider {

    private static final String TAG = "MessagesAdapter";

    @NonNull
    private final Context mContext;

    private static final String[] MESSAGE_PROJECTION = new String[] {
            EmailContent.RECORD_ID, EmailContent.MessageColumns.MAILBOX_KEY, EmailContent.MessageColumns.ACCOUNT_KEY,
            EmailContent.MessageColumns.DISPLAY_NAME, EmailContent.MessageColumns.SUBJECT, EmailContent.MessageColumns.TIMESTAMP,
            EmailContent.MessageColumns.FLAG_READ, EmailContent.MessageColumns.FLAG_FAVORITE, EmailContent.MessageColumns.FLAG_ATTACHMENT,
            EmailContent.MessageColumns.FLAGS, EmailContent.MessageColumns.SNIPPET, EmailContent.MessageColumns.TO_LIST, EmailContent.MessageColumns.OUTBOX_STATUS
    };

    public static final int COLUMN_ID = 0;
    public static final int COLUMN_MAILBOX_KEY = 1;
    public static final int COLUMN_ACCOUNT_KEY = 2;
    public static final int COLUMN_DISPLAY_NAME = 3;
    public static final int COLUMN_SUBJECT = 4;
    public static final int COLUMN_DATE = 5;
    public static final int COLUMN_READ = 6;
    public static final int COLUMN_FAVORITE = 7;
    public static final int COLUMN_ATTACHMENTS = 8;
    public static final int COLUMN_FLAGS = 9;
    public static final int COLUMN_SNIPPET = 10;

    /** Selection to retrieve all messages in "outbox" for one account */
    private static final String PER_ACCOUNT_OUTBOX_SELECTION =
            EmailContent.MessageColumns.ACCOUNT_KEY + "=?" + " AND " + EmailContent.Message.ALL_OUTBOX_SELECTION;

    private static final String PER_ACCOUNT_OUTBOX_SENDING = PER_ACCOUNT_OUTBOX_SELECTION + " AND " + EmailContent.MessageColumns.OUTBOX_STATUS + "=1";

    private static final String PER_ACCOUNT_OUTBOX_FAILED = PER_ACCOUNT_OUTBOX_SELECTION + " AND " + EmailContent.MessageColumns.OUTBOX_STATUS + "=2";

    public LoaderProvider(@NonNull Context context) {
        mContext = HcUtil.checkNotNull(context, "context cannot be null");
    }

    private static class MessagesCursorLoader extends ThrottlingCursorLoader {
        protected final Context mContext;
        private final long mAccountId;
        private final long mMailboxId;

        public MessagesCursorLoader(Context context, MessageListContext listContext) {
            // Initialize with no where clause.  We'll set it later.
            super(context, EmailContent.Message.CONTENT_URI,
                    MESSAGE_PROJECTION, null, null,
                    EmailContent.MessageColumns.TIMESTAMP + " DESC");

            mContext = context;
            mAccountId = listContext.getAccountId();
            mMailboxId = listContext.getMailboxId();
            HcLog.D(EmailUtils.DEBUG, TAG + " $MessagesCursorLoader#MessagesCursorLoader mMailboxId = "+mMailboxId);
        }

        @Override
        public Cursor loadInBackground() {
            // Build the where cause (which can't be done on the UI thread.)
            setSelection(EmailContent.Message.buildMessageListSelection(mContext, mAccountId, mMailboxId));
            // Then do a query to get the cursor
            return loadExtras(super.loadInBackground());
        }

        private Cursor loadExtras(Cursor baseCursor) {
            boolean found = false;
            Account account = null;
            Mailbox mailbox = null;
            boolean isEasAccount = false;
            boolean isRefreshable = false;

            if (mMailboxId < 0) {
                // Magic mailbox.
                found = true;
            } else {
                mailbox = Mailbox.restoreMailboxWithId(mContext, mMailboxId);
                if (mailbox != null) {
                    account = Account.restoreAccountWithId(mContext, mailbox.mAccountKey);
                    if (account != null) {
                        found = true;
                        isEasAccount = account.isEasAccount(mContext) ;
                        isRefreshable = Mailbox.isRefreshable(mContext, mMailboxId);
                    } else { // Account removed?
                        mailbox = null;
                    }
                }
            }
            final int countAccounts = EmailContent.count(mContext, Account.CONTENT_URI);
            HcLog.D(EmailUtils.DEBUG, TAG + "$MessagesCursorLoader#loadExtras countAccounts = "+countAccounts + " found="+found + " account = "+account + " mailbox = "+mailbox);
            return wrapCursor(baseCursor, found, account, mailbox, isEasAccount,
                    isRefreshable, countAccounts);
        }

        /**
         * Wraps a basic cursor containing raw messages with information about the context of
         * the list that's being loaded, such as the account and the mailbox the messages
         * are for.
         * Subclasses may extend this to wrap with additional data.
         */
        protected Cursor wrapCursor(Cursor cursor,
                                    boolean found, Account account, Mailbox mailbox, boolean isEasAccount,
                                    boolean isRefreshable, int countTotalAccounts) {
            return new MessagesCursor(cursor, found, account, mailbox, isEasAccount,
                    isRefreshable, countTotalAccounts);
        }
    }

    /**
     * The actual return type from the loader.
     */
    public static class MessagesCursor extends CursorWrapper {
        /**  Whether the mailbox is found. */
        public final boolean mIsFound;
        /** {@link Account} that owns the mailbox.  Null for combined mailboxes. */
        public final Account mAccount;
        /** {@link Mailbox} for the loaded mailbox. Null for combined mailboxes. */
        public final Mailbox mMailbox;
        /** {@code true} if the account is an EAS account */
        public final boolean mIsEasAccount;
        /** {@code true} if the loaded mailbox can be refreshed. */
        public final boolean mIsRefreshable;
        /** the number of accounts currently configured. */
        public final int mCountTotalAccounts;

        private MessagesCursor(Cursor cursor,
                               boolean found, Account account, Mailbox mailbox, boolean isEasAccount,
                               boolean isRefreshable, int countTotalAccounts) {
            super(cursor);
            mIsFound = found;
            mAccount = account;
            mMailbox = mailbox;
            mIsEasAccount = isEasAccount;
            mIsRefreshable = isRefreshable;
            mCountTotalAccounts = countTotalAccounts;
        }
    }

    /**
     * Creates the loader for {@link com.android.hcframe.hcmail.task.EmailPresenter}.
     *
     * @return always of {@link MessagesCursor}.
     */
    public Loader<Cursor> createLoader(MessageListContext listContext) {

        return new MessagesCursorLoader(mContext, listContext);
    }

    public static int getReadedCount(Context context, long accountId) {
        Cursor c = context.getContentResolver().query(EmailContent.Message.CONTENT_URI, new String[] {EmailContent.RECORD_ID},
                EmailContent.Message.PER_ACCOUNT_UNREAD_SELECTION, new String[] {String.valueOf(accountId)}, null);
        if (c != null) {
            int count = c.getCount();
            c.close();
            return count;
        }
        return 0;
    }

    public Loader<Cursor> createTaskLoader(Uri uri, String[] projection, String selection,
                                           String[] selectionArgs, String sortOrder) {
        return new CursorLoader(mContext, uri, projection, selection, selectionArgs, sortOrder);
    }

    public Loader<Cursor> createTaskLoader(Uri uri, String[] projection, String selection) {
        return new CursorLoader(mContext, uri, projection, selection, null, null);
    }

    /**
     * 获取发件箱中指定状态的邮件数量.
     * @param context
     * @param accountId
     * @return
     */
    public static int getOutboxCount(Context context, long accountId, int status) {
        String selection = null;
        switch (status) {
            case 1: // 发送完成
                selection = PER_ACCOUNT_OUTBOX_SENDING;
                break;
            case 2: // 发送失败
                selection = PER_ACCOUNT_OUTBOX_FAILED;
                break;

            default:
                selection = PER_ACCOUNT_OUTBOX_SELECTION;
                break;
        }
        return EmailContent.count(context, EmailContent.Message.CONTENT_URI, selection, new String[] {String.valueOf(accountId)});
    }

}
