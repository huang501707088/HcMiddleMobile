package com.android.hcframe.hcmail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

//import com.android.email.activity.EmailActivity;
import com.android.email.activity.MailboxFinder;
import com.android.email.activity.MessageCompose;
import com.android.email.activity.setup.SetupData;
import com.android.email.mail.Sender;
import com.android.email.mail.Store;
import com.android.emailcommon.mail.MessagingException;
import com.android.emailcommon.provider.Account;
import com.android.emailcommon.provider.EmailContent;
import com.android.emailcommon.provider.HostAuth;
import com.android.emailcommon.provider.Mailbox;
import com.android.emailcommon.provider.Policy;
import com.android.emailcommon.service.EmailServiceProxy;
import com.android.emailcommon.utility.Utility;
import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.email.R;
import com.android.hcframe.hcmail.task.EmailActivity;
import com.android.hcframe.sql.OperateDatabase;
import com.android.hcframe.sql.SettingHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 17-1-13 15:45.
 */

public class HcEmailHomeView extends AbstractPage {

    private static final String TAG = "HcEmailHomeView";

    private final String mAppId;

    private long mAccountId;
    private long mMailboxId; // 收件箱的boxId
    private String mAccountUuid;

    private LinearLayout mSetupParent;
    private EditText mPW;
    private TextView mNext;


    private LinearLayout mEmailParent;
    private LinearLayout mSend; // 发送邮件
    private LinearLayout mInbox; // 收件箱
    private LinearLayout mOutbox; // 发件箱
    private LinearLayout mDrafts; // 草稿箱
    private LinearLayout mTrash; // 回收站

    private String mMailBoxName;

    // NOTE: If you change this value, confirm that the new interval exists in arrays.xml
    private static final int DEFAULT_ACCOUNT_CHECK_INTERVAL = 15;

    private int mMailboxType = Mailbox.TYPE_NONE;

    /**
     * 邮件信箱查找,不仅仅是收件箱
     */
    private MailboxFinder mMailboxFinder;


    public HcEmailHomeView(Activity context, ViewGroup group, String appId) {
        super(context, group);
        mAppId = appId;
        mMailBoxName = OperateDatabase.getMailBoxName(context, SettingHelper.getUserId(context));
        if (mMailBoxName == null) {
            mMailBoxName = SettingHelper.getEmail(context);
        }
        SetupData.init(SetupData.FLOW_MODE_NORMAL);
    }

    @Override
    public void initialized() {
        resolveAccount();

    }

    @Override
    public void setContentView() {
        if (mView == null) {

            mView = mInflater.inflate(R.layout.email_home_view, null);
            mSetupParent = (LinearLayout) mView.findViewById(R.id.email_home_setup_parent);
            mPW = (EditText) mView.findViewById(R.id.email_home_account_password);
            mNext = (TextView) mView.findViewById(R.id.email_home_next);

            mEmailParent = (LinearLayout) mView.findViewById(R.id.email_home_email_parent);
            mSend = (LinearLayout) mView.findViewById(R.id.email_home_send_parent);
            mInbox = (LinearLayout) mView.findViewById(R.id.email_home_inbox_parent);
            mOutbox = (LinearLayout) mView.findViewById(R.id.email_home_outbox_parent);
            mDrafts = (LinearLayout) mView.findViewById(R.id.email_home_drafts_parent);
            mTrash = (LinearLayout) mView.findViewById(R.id.email_home_trash_parent);

            mNext.setOnClickListener(this);
            mSend.setOnClickListener(this);
            mInbox.setOnClickListener(this);
            mOutbox.setOnClickListener(this);
            mDrafts.setOnClickListener(this);
            mTrash.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.email_home_next) {
            if (TextUtils.isEmpty(mPW.getText())) {
                HcUtil.showToast(mContext, "请输入邮箱密码!");
            } else {
                autoSetup();
            }
        } else if (id == R.id.email_home_send_parent) {
            MessageCompose.actionCompose(mContext, mAccountId);
        } else if (id == R.id.email_home_inbox_parent) {
            startEmailActivity(Mailbox.TYPE_INBOX);
        } else if (id == R.id.email_home_outbox_parent) {
            startEmailActivity(Mailbox.TYPE_SENT);
        } else if (id == R.id.email_home_drafts_parent) {
            startEmailActivity(Mailbox.TYPE_DRAFTS);
        } else if (id == R.id.email_home_trash_parent) {
            startEmailActivity(Mailbox.TYPE_TRASH);
        }
    }

    private void startEmailActivity(int mailboxType) {
        long mailboxId = /*Controller.getInstance(mContext).findOrCreateMailboxOfType(
                mAccountId, mailboxType);//*/Mailbox.findMailboxOfType(mContext, mAccountId, mailboxType);
        final Intent i;
        HcLog.D(TAG + " #startEmailActivity mailboxId = " + mailboxId + " accountId = " + mAccountId + " uuid = " + mAccountUuid + " mailboxType = " + mailboxType);
        if (mailboxId != Mailbox.NO_MAILBOX) {
            i = EmailActivity.createOpenMailboxIntent(mContext, mAccountId, mailboxId, mailboxType);
            mContext.startActivity(i);
        } else {
            startMailboxLookup(mailboxType);
        }

    }

    private void startEmailActivity(String mailbox, int mailboxType) {
        final Intent i;
        HcLog.D(TAG + " #startEmailActivity current mailboxId = " + mMailboxId + " accountId = " + mAccountId + " uuid = " + mAccountUuid + " 邮箱类型 = " + mailbox);
        if (mMailboxId != Mailbox.NO_MAILBOX) {
            i = EmailActivity.createOpenMailboxIntent(mContext, mAccountId, mMailboxId, mailboxType);
            mContext.startActivity(i);
        } else {
            HcLog.D(TAG + " #startEmailActivity error mMailboxId == Mailbox.NO_MAILBOX!");
        }

    }

    @Override
    public void update(Observable observable, Object data) {

    }

    /**
     * Determine which account to use according to the number of accounts already set up,
     * {@link #mAccountId} and {@link #mAccountUuid}.
     * <p>
     * <pre>
     * 1. If there's no account configured, start account setup.
     * 2. Otherwise detemine which account to open with {@link #resolveAccountId} and
     *   2a. If the account doesn't have inbox yet, start inbox finder.
     *   2b. Otherwise open the main activity.
     * </pre>
     */
    private void resolveAccount() {
        if (TextUtils.isEmpty(mMailBoxName)) {
            mEmailParent.setVisibility(View.GONE);
            mSetupParent.setVisibility(View.VISIBLE);
            return;
        }
        final int numAccount = EmailContent.count(mContext, Account.CONTENT_URI);
        HcLog.D(TAG + " #resolveAccount numAccount = " + numAccount);
        if (numAccount == 0) {
            mEmailParent.setVisibility(View.GONE);
            mSetupParent.setVisibility(View.VISIBLE);
        } else {
            List<Account> accounts = getPopImapAccountList(mContext);
            boolean find = false;
            for (Account account : accounts) {
                if (mMailBoxName.equals(account.getEmailAddress())) {
                    mAccountUuid = account.getUuid();
                    find = true;
                    break;
                }
            }
            if (!find) {
                mEmailParent.setVisibility(View.GONE);
                mSetupParent.setVisibility(View.VISIBLE);
            } else {
                mAccountId = resolveAccountId(mContext, mAccountUuid);
                if (mAccountId == Account.NO_ACCOUNT) {
                    mEmailParent.setVisibility(View.GONE);
                    mSetupParent.setVisibility(View.VISIBLE);
                } else {
                    mEmailParent.setVisibility(View.VISIBLE);
                    mSetupParent.setVisibility(View.GONE);
//                    if (Account.isNormalAccount(mAccountId)) {
//                        long mailboxId = Mailbox.findMailboxOfType(mContext, mAccountId, Mailbox.TYPE_INBOX);
//                        HcLog.D(TAG + " #resolveAccount 收件箱mailboxId = "+mailboxId);
//                        if (mailboxId == Mailbox.NO_MAILBOX)
//                            startInboxLookup(Mailbox.TYPE_INBOX);
//                        else {
//                            mMailboxId = mailboxId;
//                            mEmailParent.setVisibility(View.VISIBLE);
//                            mSetupParent.setVisibility(View.GONE);
//                        }
//                    } else {
//                        mEmailParent.setVisibility(View.VISIBLE);
//                        mSetupParent.setVisibility(View.GONE);
//
//                    }
                }

            }


        }
    }

    /**
     * Determine which account to open with the given account ID and UUID.
     *
     * @return ID of the account to use.
     */
    static long resolveAccountId(Context context, String inputUuid) {
        final long accountId;

        if (!TextUtils.isEmpty(inputUuid)) {
            // If a UUID is specified, try to use it.
            // If the UUID is invalid, accountId will be NO_ACCOUNT.
            accountId = Account.getAccountIdFromUuid(context, inputUuid);

        } else {
            accountId = Account.NO_ACCOUNT;
        }
        return accountId;
    }

    private List<Account> getPopImapAccountList(Context context) {
        List<Account> providerAccounts = new ArrayList<Account>();
        Cursor c = context.getContentResolver().query(Account.CONTENT_URI, Account.ID_PROJECTION,
                null, null, null);
        try {
            while (c.moveToNext()) {
                long accountId = c.getLong(Account.CONTENT_ID_COLUMN);
                String protocol = Account.getProtocol(context, accountId);
                if ((protocol != null) && ("pop3".equals(protocol) || "imap".equals(protocol))) {
                    Account account = Account.restoreAccountWithId(context, accountId);
                    if (account != null) {
                        providerAccounts.add(account);
                    }
                }
            }
        } finally {
            c.close();
        }
        return providerAccounts;
    }

    private final MailboxFinder.Callback mMailboxFinderCallback = new MailboxFinder.Callback() {
        // This MUST be called from callback methods.
        private void cleanUp() {
            HcDialog.deleteProgressDialog();
            mMailboxFinder = null;
        }

        @Override
        public void onAccountNotFound() {
            cleanUp();
            // Account removed?  Clear the IDs and restart the task.  Which will result in either
            // a) show account setup if there's really no accounts  or b) open the default account.

            mAccountId = Account.NO_ACCOUNT;
            mMailboxId = Mailbox.NO_MAILBOX;
            mAccountUuid = null;

            // Restart the account resolution.
            resolveAccount();
        }

        @Override
        public void onMailboxNotFound(long accountId) {
            // Just do the same thing as "account not found".
            onAccountNotFound();
        }

        @Override
        public void onAccountSecurityHold(long accountId) {
            cleanUp();
            mEmailParent.setVisibility(View.GONE);
            mSetupParent.setVisibility(View.VISIBLE);
        }

        @Override
        public void onMailboxFound(long accountId, long mailboxId) {
            int mailboxType = mMailboxType;
            mMailboxType = Mailbox.TYPE_NONE;
            HcLog.D(TAG + " #onMailboxFound accountId = " + accountId + " mailboxId = " + mailboxId + " mailboxType = " + mailboxType);
            cleanUp();
            mAccountId = accountId;
            mSetupParent.setVisibility(View.GONE);
            mEmailParent.setVisibility(View.VISIBLE);
            mMailboxId = mailboxId;
            switch (mailboxType) {
                case Mailbox.TYPE_OUTBOX:
                    startEmailActivity("发件箱", mailboxType);
                    break;
                case Mailbox.TYPE_DRAFTS:
                    startEmailActivity("草稿箱", mailboxType);
                    break;
                case Mailbox.TYPE_TRASH:
                    startEmailActivity("垃圾箱", mailboxType);
                    break;
                case Mailbox.TYPE_INBOX:
                    startEmailActivity("收件箱", mailboxType);
                    break;
                case Mailbox.TYPE_SENT:
                    startEmailActivity("已发送", mailboxType);
                    break;

                default:

                    break;
            }
        }
    };

    /**
     * Sets the account sync, delete, and other misc flags not captured in {@code HostAuth}
     * information for the specified account based on the protocol type.
     */
    static void setFlagsForProtocol(Account account, String protocol) {
        if (HostAuth.SCHEME_IMAP.equals(protocol)) {
            // Delete policy must be set explicitly, because IMAP does not provide a UI selection
            // for it.
            account.setDeletePolicy(Account.DELETE_POLICY_ON_DELETE);
            account.mFlags |= Account.FLAGS_SUPPORTS_SEARCH;
        }

        if (HostAuth.SCHEME_EAS.equals(protocol)) {
            account.setDeletePolicy(Account.DELETE_POLICY_ON_DELETE);
            account.setSyncInterval(Account.CHECK_INTERVAL_PUSH);
        } else {
            account.setSyncInterval(DEFAULT_ACCOUNT_CHECK_INTERVAL);
        }
    }

    private void autoSetup() {
        HcConfig.ServerInfo incoming = HcConfig.getConfig().getServerInfo(HcConfig.Module.MAIL_INCOMING);
        HcConfig.ServerInfo outgoing = HcConfig.getConfig().getServerInfo(HcConfig.Module.MAIL_OUTGOING);
        if (incoming == null || outgoing == null) {
            HcUtil.showToast(mContext, "邮件服务器未配置!");
            return;
        }

        String incomingAddress = incoming.mExtranetServerURL;
        String outgoingAddress = outgoing.mExtranetServerURL;
        if (TextUtils.isEmpty(incomingAddress) || TextUtils.isEmpty(outgoingAddress)) {
            HcUtil.showToast(mContext, "邮件服务器地址未配置!");
            return;
        }

        String email = mMailBoxName;
        String password = mPW.getText().toString();

        Account account = SetupData.getAccount();
        // 收件服务器配置
        String protocol = TextUtils.isEmpty(incoming.mServerType) ? HostAuth.SCHEME_POP3 :
                incoming.mServerType.equals(HostAuth.SCHEME_IMAP) ? HostAuth.SCHEME_IMAP : HostAuth.SCHEME_POP3;
        String port = TextUtils.isEmpty(incoming.mPort) ? "143" : incoming.mPort;
        HostAuth recvAuth = account.getOrCreateHostAuthRecv(mContext);
        recvAuth.setLogin(email, password);
        recvAuth.setConnection(protocol, incomingAddress, Integer.valueOf(port), getSecurity(incoming.mSecurity));

        // 设置发送服务器
        String sendPort = TextUtils.isEmpty(outgoing.mPort) ? "25" : outgoing.mPort;
        HostAuth sendAuth = account.getOrCreateHostAuthSend(mContext);
        sendAuth.setLogin(email, password);
        sendAuth.setConnection(HostAuth.SCHEME_SMTP, outgoingAddress, Integer.valueOf(sendPort), getSecurity(outgoing.mSecurity));
        // 设置用户
        String name = SettingHelper.getName(mContext);
        account.setSenderName(name);
        account.setEmailAddress(email);
        account.setDisplayName(name);
        account.setDefaultAccount(true);
        SetupData.setDefault(true);
        setFlagsForProtocol(account, protocol);

        SetupData.setAllowAutodiscover(true);


        SetupData.setCheckSettingsMode(SetupData.CHECK_INCOMING | SetupData.CHECK_OUTGOING);

//        new DuplicateCheckTask(account.mId, recvAuth.mAddress, recvAuth.mLogin, SetupData.CHECK_INCOMING)
//                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new AccountCheckTask(SetupData.CHECK_INCOMING, account)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private int getSecurity(String security) {
        if (TextUtils.isEmpty(security)) return HostAuth.FLAG_NONE;
        if ("SSL/TLS".equals(security)) {
            return HostAuth.FLAG_SSL;
        } else if ("ALL_SSL/TLS".equals(security)) {
            return HostAuth.FLAG_SSL | HostAuth.FLAG_TRUST_ALL;
        } else if ("STARTTLS".equals(security)) {
            return HostAuth.FLAG_TLS | HostAuth.FLAG_TRUST_ALL;
        } else if ("ALL_STARTTLS".equals(security)) {
            return HostAuth.FLAG_TLS | HostAuth.FLAG_TRUST_ALL;
        } else {
            return HostAuth.FLAG_SSL;
        }
    }

    /**
     * 检测用户是否已经登录过...
     */
    private class DuplicateCheckTask extends AsyncTask<Void, Void, Account> {

        private final long mAccountId;
        private final String mCheckHost;
        private final String mCheckLogin;
        private final int mCheckSettingsMode;

        public DuplicateCheckTask(long accountId, String checkHost, String checkLogin,
                                  int checkSettingsMode) {
            mAccountId = accountId;
            mCheckHost = checkHost;
            mCheckLogin = checkLogin;
            mCheckSettingsMode = checkSettingsMode;
        }

        @Override
        protected Account doInBackground(Void... params) {
            Account account = Utility.findExistingAccount(mContext, mAccountId,
                    mCheckHost, mCheckLogin);
            return account;
        }

        @Override
        protected void onPostExecute(Account duplicateAccount) {

            new AccountCheckTask(mCheckSettingsMode, SetupData.getAccount())
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }


    // State
    private final static int STATE_START = 0;
    private final static int STATE_CHECK_AUTODISCOVER = 1;
    private final static int STATE_CHECK_INCOMING = 2;
    private final static int STATE_CHECK_OUTGOING = 3;
    private final static int STATE_CHECK_OK = 4;                    // terminal
    private final static int STATE_CHECK_SHOW_SECURITY = 5;         // terminal
    private final static int STATE_CHECK_ERROR = 6;                 // terminal
    private final static int STATE_AUTODISCOVER_AUTH_DIALOG = 7;    // terminal
    private final static int STATE_AUTODISCOVER_RESULT = 8;         // terminal
    private int mState = STATE_START;
    private int mOldState = STATE_START;

    // Support for UI

    private MessagingException mProgressException;

    /**
     * This AsyncTask does the actual account checking
     * <p>
     * TODO: It would be better to remove the UI complete from here (the exception->string
     * conversions).
     */
    private class AccountCheckTask extends AsyncTask<Void, Integer, MessagingException> {

        final int mMode;
        final Account mAccount;
        final String mStoreHost;
        final String mCheckEmail;
        final String mCheckPassword;

        /**
         * Create task and parameterize it
         *
         * @param mode         bits request operations
         * @param checkAccount account holding values to be checked
         */
        public AccountCheckTask(int mode, Account checkAccount) {
            mMode = mode;
            mAccount = checkAccount;
            mStoreHost = checkAccount.mHostAuthRecv.mAddress;
            mCheckEmail = checkAccount.mEmailAddress;
            mCheckPassword = checkAccount.mHostAuthRecv.mPassword;
        }

        @Override
        protected MessagingException doInBackground(Void... params) {

            try {
                if ((mMode & SetupData.CHECK_AUTODISCOVER) != 0) {
                    if (isCancelled()) return null;
                    publishProgress(STATE_CHECK_AUTODISCOVER);
                    HcLog.D(TAG + "$AccountCheckTask#doInBackground Begin auto-discover for " + mCheckEmail);
                    Store store = Store.getInstance(mAccount, mContext);
                    Bundle result = store.autoDiscover(mContext, mCheckEmail, mCheckPassword);
                    // Result will be one of:
                    //  null: remote exception - proceed to manual setup
                    //  MessagingException.AUTHENTICATION_FAILED: username/password rejected
                    //  Other error: proceed to manual setup
                    //  No error: return autodiscover results
                    if (result == null) {
                        return new AutoDiscoverResults(false, null);
                    }
                    int errorCode =
                            result.getInt(EmailServiceProxy.AUTO_DISCOVER_BUNDLE_ERROR_CODE);
                    if (errorCode == MessagingException.AUTODISCOVER_AUTHENTICATION_FAILED) {
                        return new AutoDiscoverResults(true, null);
                    } else if (errorCode != MessagingException.NO_ERROR) {
                        return new AutoDiscoverResults(false, null);
                    } else {
                        HostAuth serverInfo = (HostAuth)
                                result.getParcelable(EmailServiceProxy.AUTO_DISCOVER_BUNDLE_HOST_AUTH);
                        return new AutoDiscoverResults(false, serverInfo);
                    }
                }

                // Check Incoming Settings
                if ((mMode & SetupData.CHECK_INCOMING) != 0) {
                    if (isCancelled()) return null;
                    HcLog.D(TAG + "$AccountCheckTask#doInBackground Begin check of incoming email settings");
                    publishProgress(STATE_CHECK_INCOMING);
                    Store store = Store.getInstance(mAccount, mContext);
                    Bundle bundle = store.checkSettings();
                    int resultCode = MessagingException.UNSPECIFIED_EXCEPTION;
                    if (bundle != null) {
                        resultCode = bundle.getInt(
                                EmailServiceProxy.VALIDATE_BUNDLE_RESULT_CODE);
                    }
                    if (resultCode == MessagingException.SECURITY_POLICIES_REQUIRED) {
                        SetupData.setPolicy((Policy) bundle.getParcelable(
                                EmailServiceProxy.VALIDATE_BUNDLE_POLICY_SET));
                        return new MessagingException(resultCode, mStoreHost);
                    } else if (resultCode == MessagingException.SECURITY_POLICIES_UNSUPPORTED) {
                        String[] data = bundle.getStringArray(
                                EmailServiceProxy.VALIDATE_BUNDLE_UNSUPPORTED_POLICIES);
                        return new MessagingException(resultCode, mStoreHost, data);
                    } else if (resultCode != MessagingException.NO_ERROR) {
                        String errorMessage =
                                bundle.getString(EmailServiceProxy.VALIDATE_BUNDLE_ERROR_MESSAGE);
                        return new MessagingException(resultCode, errorMessage);
                    }
                }

                // Check Outgoing Settings
                if ((mMode & SetupData.CHECK_OUTGOING) != 0) {
                    if (isCancelled()) return null;
                    HcLog.D(TAG + "$AccountCheckTask#doInBackground Begin check of outgoing email settings");
                    publishProgress(STATE_CHECK_OUTGOING);
                    Sender sender = Sender.getInstance(mContext, mAccount);
                    sender.close();
                    sender.open();
                    sender.close();
                }

                // If we reached the end, we completed the check(s) successfully
                return null;
            } catch (final MessagingException me) {
                // Some of the legacy account checkers return errors by throwing MessagingException,
                // which we catch and return here.
                return me;
            }
        }


        /**
         * Progress reports (runs in UI thread).  This should be used for real progress only
         * (not for errors).
         */
        @Override
        protected void onProgressUpdate(Integer... progress) {
            if (isCancelled()) return;
            reportProgress(progress[0], null);
        }

        /**
         * Result handler (runs in UI thread).
         * <p>
         * AutoDiscover authentication errors are handled a bit differently than the
         * other errors;  If encountered, we display the error dialog, but we return with
         * a different callback used only for AutoDiscover.
         *
         * @param result null for a successful check;  exception for various errors
         */
        @Override
        protected void onPostExecute(MessagingException result) {
            if (isCancelled()) return;
            if (result == null) {
                reportProgress(STATE_CHECK_OK, null);
            } else {
                int progressState = STATE_CHECK_ERROR;
                int exceptionType = result.getExceptionType();

                switch (exceptionType) {
                    // NOTE: AutoDiscover reports have their own reporting state, handle differently
                    // from the other exception types
                    case MessagingException.AUTODISCOVER_AUTHENTICATION_FAILED:
                        progressState = STATE_AUTODISCOVER_AUTH_DIALOG;
                        break;
                    case MessagingException.AUTODISCOVER_AUTHENTICATION_RESULT:
                        progressState = STATE_AUTODISCOVER_RESULT;
                        break;
                    // NOTE: Security policies required has its own report state, handle it a bit
                    // differently from the other exception types.
                    case MessagingException.SECURITY_POLICIES_REQUIRED:
                        progressState = STATE_CHECK_SHOW_SECURITY;
                        break;
                }
                reportProgress(progressState, result);
            }
        }
    }


    /**
     * The worker (AsyncTask) will call this (in the UI thread) to report progress.  If we are
     * attached to an activity, update the progress immediately;  If not, simply hold the
     * progress for later.
     *
     * @param newState The new progress state being reported
     */
    private void reportProgress(int newState, MessagingException ex) {
        mOldState = mState;
        mState = newState;
        mProgressException = ex;
        HcLog.D(TAG + " #reportProgress newState = " + newState);
        switch (newState) {
            case STATE_CHECK_OK:

                // immediately terminate, clean up, and report back
                // 1. get rid of progress dialog (if any)

                // 2. exit self

                // 3. report OK back to target fragment or activity
//                getCallbackTarget().onCheckSettingsComplete(CHECK_SETTINGS_OK);
                if (mOldState == STATE_CHECK_INCOMING) {
                    new AccountCheckTask(SetupData.CHECK_OUTGOING, SetupData.getAccount())
                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                } else if (mOldState == STATE_CHECK_OUTGOING) {
                    final Account account = SetupData.getAccount();
                    int newFlags = account.getFlags() &
                            ~(Account.FLAGS_NOTIFY_NEW_MAIL | Account.FLAGS_BACKGROUND_ATTACHMENTS);
                    newFlags |= Account.FLAGS_BACKGROUND_ATTACHMENTS;
                    account.setFlags(newFlags);
                    Utility.runAsync(new Runnable() {
                        @Override
                        public void run() {
                            EmailUtils.commitSettings(mContext, account);
                            EmailUtils.setServicesEnabledSync(mContext);
                            Account.restoreAccountWithId(mContext, account.mId);
                            SetupData.init(SetupData.FLOW_MODE_RETURN_TO_MESSAGE_LIST, account); // 这个需要测试
                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//                                    mSetupParent.setVisibility(View.GONE);
//                                    mEmailParent.setVisibility(View.VISIBLE);
                                    resolveAccount();
                                }
                            });
                        }
                    });
                }
                break;
            case STATE_CHECK_SHOW_SECURITY:
                HcUtil.showToast(mContext, "邮件服务器需要安全协议!");
                break;
            case STATE_CHECK_ERROR:
            case STATE_AUTODISCOVER_AUTH_DIALOG:
                // 1. get rid of progress dialog (if any)

                // 2. launch the error dialog, if needed
                HcUtil.showToast(mContext, "邮箱登录失败!");
                break;
            case STATE_AUTODISCOVER_RESULT:

                // 1. get rid of progress dialog (if any)

                // 2. exit self

                // 3. report back to target fragment or activity

                break;
            default:
                // Display a normal progress message

                break;
        }
    }

    /**
     * Start inbox lookup.  This MSUT be called on the UI thread.
     */
    private void startMailboxLookup(int mailboxType) {
        HcLog.D(TAG + "#startMailboxLookup mailbox not found.  Starting mailbox finder... mailboxType = " + mailboxType);
        stopMailboxLookup(); // Stop if already running -- it shouldn't be but just in case.
        mMailboxFinder = new MailboxFinder(mContext, mAccountId, mailboxType,
                mMailboxFinderCallback);
        mMailboxType = mailboxType;
        mMailboxFinder.startLookup();
        HcDialog.showProgressDialog(mContext, "正在查找邮箱...");
    }

    /**
     * Stop inbox lookup.  This MSUT be called on the UI thread.
     */
    private void stopMailboxLookup() {
        if (mMailboxFinder != null) {
            mMailboxFinder.cancel();
            mMailboxFinder = null;
        }
    }

    /**
     * This exception class is used to report autodiscover results via the reporting mechanism.
     */
    public static class AutoDiscoverResults extends MessagingException {
        public final HostAuth mHostAuth;

        /**
         * @param authenticationError true if auth failure, false for result (or no response)
         * @param hostAuth            null for "no autodiscover", non-null for server info to return
         */
        public AutoDiscoverResults(boolean authenticationError, HostAuth hostAuth) {
            super(null);
            if (authenticationError) {
                mExceptionType = AUTODISCOVER_AUTHENTICATION_FAILED;
            } else {
                mExceptionType = AUTODISCOVER_AUTHENTICATION_RESULT;
            }
            mHostAuth = hostAuth;
        }
    }
}
