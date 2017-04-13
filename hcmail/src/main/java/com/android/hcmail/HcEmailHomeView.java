package com.android.hcmail;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;

import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.email.Controller;
import com.android.email.MessagingController;
import com.android.email.MessagingExceptionStrings;
import com.android.email.MessagingListener;
import com.android.email.RefreshManager;
import com.android.email.activity.MailboxFinder;
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
import com.android.hcframe.badge.BadgeCache;
import com.android.hcframe.badge.BadgeInfo;
import com.android.hcframe.badge.ModuleBadgeInfo;
import com.android.hcframe.email.R;
import com.android.hcframe.hcmail.EmailUtils;
import com.android.hcframe.hcmail.WriteEmailCommand;
import com.android.hcframe.hcmail.data.LoaderProvider;
import com.android.hcframe.hcmail.task.EmailActivity;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshScrollView;
import com.android.hcframe.sql.OperateDatabase;
import com.android.hcframe.sql.SettingHelper;
import com.android.hcframe.view.PointTextView;

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
    private TextView mName;
    private EditText mPW;
    private ImageView mNext;


//    private LinearLayout mEmailParent;
    private LinearLayout mWriteMail;//写邮件
    private LinearLayout mInbox; // 收件箱
    private LinearLayout mOutbox; // 发件箱
    private LinearLayout mAlreadyOutbox; // 已发送
    private LinearLayout mDrafts; // 草稿箱
    private LinearLayout mTrash; // 回收站
    private LinearLayout mSignUp; // 签名设置

    private TextView mSending; // 发送状态

    private TextView mEmailAddress;

    private TextView mTime;

    private PointTextView mPointView;

    private PullToRefreshScrollView mEmailParent;

    private TextView mSendFailed;

    private String mMailBoxName;

    // NOTE: If you change this value, confirm that the new interval exists in arrays.xml
    private static final int DEFAULT_ACCOUNT_CHECK_INTERVAL = 15;

    private int mMailboxType = Mailbox.TYPE_NONE;

    /**
     * 邮件信箱查找,不仅仅是收件箱
     */
    private MailboxFinder mMailboxFinder;

    private RefreshManager mRefreshManager;
    private final RefreshListener mRefreshListener = new RefreshListener();

    /** 刷新的时候是否需要去获取Mailbox */
    private boolean mRefreshing = false;

    private BadgeInfo mBadgeInfo;

    public static final String MODULE_NAME = "EMAIL";

    /** 是否已经登录 */
    private boolean mSignedIn;

    private MessagingController mMessagingController; // 直接跳过Controller和Result

    private MessagingListener mMessagingListener;

    private static final long PLAY_TIME = 60 * 1000; // 1分钟

    private final TimeHandler mHander;
    /** 1:来源通讯录 */
    private int mFrom = -1; // 其他模块调用的标志
    private String mAccount;
    private String mAddress;

    public HcEmailHomeView(Activity context, ViewGroup group, String appId) {
        super(context, group);
        mAppId = appId;
        mMailBoxName = OperateDatabase.getMailBoxName(context, SettingHelper.getUserId(context));
        if (mMailBoxName == null) {
            mMailBoxName = SettingHelper.getEmail(context);
        }
        SetupData.init(SetupData.FLOW_MODE_NORMAL);
        mRefreshManager = RefreshManager.getInstance(context);
//        mRefreshManager.registerListener(mRefreshListener);
        mMessagingController = MessagingController.getInstance(mContext, Controller.getInstance(mContext));
        mMessagingListener = new SendMessageListener();
        mMessagingController.addListener(mMessagingListener);
        mHander = new TimeHandler();
        Intent intent = context.getIntent();
        if (intent.getExtras() != null) {
            mFrom = intent.getIntExtra("from", -1);
            if (mFrom == WriteEmailCommand.FROM_CONTACTS) {
                mAccount = intent.getStringExtra("name");
                mAddress = intent.getStringExtra("address");
            }
        }
    }

    @Override
    public void initialized() {
        mNext.setEnabled(false);
        resolveAccount();
    }

    @Override
    public void setContentView() {
        if (mView == null) {
            mView = mInflater.inflate(R.layout.hcmail_home_view, null);
            mSetupParent = (LinearLayout) mView.findViewById(R.id.hcmail_home_setup_parent);
            mName = (TextView) mView.findViewById(R.id.hcmail_home_view_name);
            mPW = (EditText) mView.findViewById(R.id.hcmail_home_account_password);
            mNext = (ImageView) mView.findViewById(R.id.hcmail_home_next);

//            mEmailParent = (LinearLayout) mView.findViewById(R.id.hcmail_home_email_parent);

            mInbox = (LinearLayout) mView.findViewById(R.id.hcmail_home_view_receive_btn);
            mWriteMail = (LinearLayout) mView.findViewById(R.id.hcmail_home_write_mail);
            mOutbox = (LinearLayout) mView.findViewById(R.id.hcmail_home_send_parent);
            mAlreadyOutbox = (LinearLayout) mView.findViewById(R.id.hcmail_home_already_send_parent);
            mDrafts = (LinearLayout) mView.findViewById(R.id.hcmail_home_drafts_parent);
            mTrash = (LinearLayout) mView.findViewById(R.id.hcmail_home_delete_parent);
            mSignUp = (LinearLayout) mView.findViewById(R.id.hcmail_home_sign_name_parent);
            mSendFailed = (TextView) mView.findViewById(R.id.hcmail_home_send_status);


            mSending = (TextView) mView.findViewById(R.id.hcemail_home_sending);
            mEmailAddress = (TextView) mView.findViewById(R.id.hcemail_home_email_name);
            mTime = (TextView) mView.findViewById(R.id.hcemail_home_email_time);
            mEmailParent = (PullToRefreshScrollView) mView.findViewById(R.id.hcemail_home_scrollview);
            mPointView = (PointTextView) mView.findViewById(R.id.hcemail_home_badge);

            mNext.setOnClickListener(this);
            mInbox.setOnClickListener(this);
            mWriteMail.setOnClickListener(this);
            mAlreadyOutbox.setOnClickListener(this);
            mOutbox.setOnClickListener(this);
            mDrafts.setOnClickListener(this);
            mTrash.setOnClickListener(this);
            mSignUp.setOnClickListener(this);
            mPW.addTextChangedListener(mWatcher);
            mEmailParent.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            mEmailParent.setScrollingWhileRefreshingEnabled(true);
            mEmailParent.setOnRefreshBothListener(new PullToRefreshBase.OnRefreshBothListener<ScrollView>() {
                @Override
                public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                    long mailboxId = Mailbox.findMailboxOfType(mContext, mAccountId, Mailbox.TYPE_INBOX);
                    if (mailboxId != Mailbox.NO_MAILBOX) {
                        mRefreshManager.refreshMessageList(mAccountId, mailboxId, true);
                    } else {
                        mRefreshing = true;
                        startMailboxLookup(Mailbox.TYPE_INBOX);
                    }
                }

                @Override
                public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                    // do nothing
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.hcmail_home_next) {
            if (TextUtils.isEmpty(mPW.getText())) {
                HcUtil.showToast(mContext, "请输入邮箱密码!");
            } else {
                autoSetup();
            }
        } else if (id == R.id.hcmail_home_view_receive_btn) {
            //收信箱
            startEmailActivity(Mailbox.TYPE_INBOX);
        } else if (id == R.id.hcmail_home_write_mail) {
            //写邮件
            Intent intent = new Intent();
            intent.setClass(mContext, HcmailWriteActivity.class);
            mContext.startActivity(intent);
        } else if (id == R.id.hcmail_home_send_parent) {
            //发信箱
            startEmailActivity(Mailbox.TYPE_OUTBOX);
        } else if (id == R.id.hcmail_home_already_send_parent) {
            //已发送
            startEmailActivity(Mailbox.TYPE_SENT);
        } else if (id == R.id.hcmail_home_drafts_parent) {
            //草稿箱
            startEmailActivity(Mailbox.TYPE_DRAFTS);
        } else if (id == R.id.hcmail_home_delete_parent) {
            //已删除
            startEmailActivity(Mailbox.TYPE_TRASH);
        } else if (id == R.id.hcmail_home_sign_name_parent) {
            //签名设置
            Intent intent = new Intent();
            intent.setClass(mContext, HcmailSignActivity.class);
            mContext.startActivity(intent);
        }
    }

//    private void startEmailActivity(int mailboxType) {
//        long mailboxId = /*Controller.getInstance(mContext).findOrCreateMailboxOfType(
//                mAccountId, mailboxType);//*/Mailbox.findMailboxOfType(mContext, mAccountId, mailboxType);
//        HcLog.D(TAG + " #startEmailActivity mailboxId = " + mailboxId + " accountId = " + mAccountId + " uuid = " + mAccountUuid + " mailboxType = " + mailboxType);
//        if (mailboxId != Mailbox.NO_MAILBOX) {
//            mController = Controller.getInstance(mContext);
//            mControllerResult = new ControllerResultUiThreadWrapper<ControllerResult>(new Handler(),
//                    new ControllerResult());
//            mController.addResultCallback(mControllerResult);
//            if (mAccountId == -1 || mailboxId == -1) {
//                throw new IllegalArgumentException();
//            }
//            Intent intent = new Intent();
//            if (mailboxType == 0) {
//                intent.setClass(mContext, HcmailInboxActivity.class);
//            } else if (mailboxType == 5) {
//                intent.setClass(mContext, HcmailOutboxActivity.class);
//            } else if (mailboxType == 3) {
//                intent.setClass(mContext, HcmailDraftsActivity.class);
//            } else if (mailboxType == 6) {
//                intent.setClass(mContext, HcmailDeleteboxActivity.class);
//            } else if (mailboxType == 4) {
//                intent.setClass(mContext, HcmailSendboxActivity.class);
//            }
//            intent.putExtra(EXTRA_ACCOUNT_ID, mAccountId);
//            intent.putExtra(EXTRA_MAILBOX_ID, mailboxId);
//            mContext.startActivity(intent);
//        } else {
//            startMailboxLookup(mailboxType);
//        }
//    }

    TextWatcher mWatcher = new TextWatcher() {
        private CharSequence ps;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            ps = s;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (ps.length() > 0) {
                mNext.setImageResource(R.drawable.hcmail_next_pressed);
                mNext.setEnabled(true);
            } else if (ps.length() == 0) {
                mNext.setImageResource(R.drawable.hcmail_next_normal);
                mNext.setEnabled(false);
            }
        }
    };

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
            showSetupView();
            mPW.setEnabled(false);
            mName.setText("无关联邮箱帐号，请联系管理员");
            return;
        }
        mName.setText(mMailBoxName);
        mEmailAddress.setText(mMailBoxName);
        final int numAccount = EmailContent.count(mContext, Account.CONTENT_URI);
        HcLog.D(TAG + " #resolveAccount numAccount = " + numAccount);
        if (numAccount == 0) {
            showSetupView();
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
                showSetupView();
            } else {
                mAccountId = resolveAccountId(mContext, mAccountUuid);
                if (mAccountId == Account.NO_ACCOUNT) {
                    showSetupView();
                } else {
                    hideSetupView();
                    if (isFirst) {
                        isFirst = false;
                        long mailboxId = Mailbox.findMailboxOfType(mContext, mAccountId, Mailbox.TYPE_INBOX);
                        if (mailboxId != Mailbox.NO_MAILBOX) {
                            if (mFrom == -1) {
                                mRefreshManager.refreshMessageList(mAccountId, mailboxId, true);
                            } else {
                                //写邮件
                                writeEmailFromContacts();
                            }

                        } else {
                            mRefreshing = true;
                            startMailboxLookup(Mailbox.TYPE_INBOX);
                        }
                    }
//                    mEmailParent.setVisibility(View.VISIBLE);
//                    mSetupParent.setVisibility(View.GONE);
//                    if (Account.isNormalAccount(mAccountId)) {
//                        long mailboxId = Mailbox.findMailboxOfType(mContext, mAccountId, Mailbox.TYPE_INBOX);
//                        HcLog.D(TAG + " #resolveAccount 收件箱mailboxId = "+mailboxId);
//                        if (mailboxId == Mailbox.NO_MAILBOX) {
//                            mAutoFinderBox = true;
//                            startMailboxLookup(Mailbox.TYPE_INBOX);
//                        } else {
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
            if (mRefreshing) {
                mRefreshing = false;
                mEmailParent.onRefreshComplete();
            }
            HcDialog.deleteProgressDialog();
            mMailboxFinder = null;
            if (mFrom != -1) {
                mContext.finish();
            }
        }

        @Override
        public void onAccountNotFound() {
            if (mFrom == -1) {
                mContext.finish();
                return;
            }
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
            if (mFrom == -1) {
                mContext.finish();
                return;
            }
            cleanUp();
            showSetupView();
        }

        @Override
        public void onMailboxFound(long accountId, long mailboxId) {
            int mailboxType = mMailboxType;
            mMailboxType = Mailbox.TYPE_NONE;
            HcLog.D(TAG + " #onMailboxFound accountId = " + accountId + " mailboxId = " + mailboxId + " mailboxType = " + mailboxType);
            HcDialog.deleteProgressDialog();
            mMailboxFinder = null;
            mAccountId = accountId;
            mMailboxId = mailboxId;
            if (mFrom != -1) {
                //写邮件
                writeEmailFromContacts();
                return;
            }
            hideSetupView();
            if (mRefreshing) {
                mRefreshing = false;
                // 开始获取新数据
                mRefreshManager.refreshMessageList(mAccountId, mailboxId, true);
                return;
            }
            switch (mailboxType) {
                case Mailbox.TYPE_OUTBOX:
                    startEmailActivity("发件箱", mailboxType);
                    break;
                case Mailbox.TYPE_DRAFTS:
                    startEmailActivity("草稿箱", Mailbox.TYPE_DRAFTS);
                    break;
                case Mailbox.TYPE_TRASH:
                    startEmailActivity("垃圾箱", Mailbox.TYPE_TRASH);
                    break;
                case Mailbox.TYPE_INBOX:
                    //收信箱
                    startEmailActivity("收件箱", Mailbox.TYPE_INBOX);
                    break;
                case Mailbox.TYPE_SENT:
                    startEmailActivity("已发送", Mailbox.TYPE_SENT);
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
        HcDialog.showProgressDialog(mContext, "正在登录邮箱...");
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
                        return new com.android.hcframe.hcmail.HcEmailHomeView.AutoDiscoverResults(false, serverInfo);
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
                HcDialog.deleteProgressDialog();
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
                                    resolveAccount();
                                }
                            });
                        }
                    });
                }
                break;
            case STATE_CHECK_SHOW_SECURITY:
                HcDialog.deleteProgressDialog();
                HcUtil.showToast(mContext, "邮件服务器需要安全协议!");
                if (mFrom != -1) {
                    mContext.finish();
                }
                break;
            case STATE_CHECK_ERROR:
            case STATE_AUTODISCOVER_AUTH_DIALOG:
                HcDialog.deleteProgressDialog();
                // 1. get rid of progress dialog (if any)

                // 2. launch the error dialog, if needed
                HcUtil.showToast(mContext, "邮箱登录失败!");
                if (mFrom != -1) {
                    mContext.finish();
                }
                break;
            case STATE_AUTODISCOVER_RESULT:
                if (mFrom != -1) {
                    mContext.finish();
                }
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


    private void startEmailActivity(int mailboxType) {
        long mailboxId = Mailbox.TYPE_NONE;
        if (mailboxType == Mailbox.TYPE_OUTBOX) {
            mailboxId = Controller.getInstance(mContext).findOrCreateMailboxOfType(
                    mAccountId, mailboxType);
        } else {
            mailboxId = Mailbox.findMailboxOfType(mContext, mAccountId, mailboxType);
        }
        final Intent i;
        HcLog.D(TAG + " #startEmailActivity mailboxId = " + mailboxId + " accountId = " + mAccountId + " uuid = " + mAccountUuid + " mailboxType = " + mailboxType);
        if (mailboxId != Mailbox.NO_MAILBOX) {
            i = EmailActivity.createOpenMailboxIntent(mContext, mAccountId, mailboxId, mailboxType);
            mContext.startActivity(i);
        } else {
            startMailboxLookup(mailboxType);
        }

    }

    private class RefreshListener implements RefreshManager.Listener {
        @Override
        public void onMessagingError(long accountId, long mailboxId, String message) {
            HcLog.D(EmailUtils.DEBUG, TAG + " $RefreshListener#onMessagingError accountId = "+accountId + " mailboxId = "+mailboxId + " message = "+message);
            mEmailParent.onRefreshComplete();
            HcUtil.showToast(mContext, message);
        }

        @Override
        public void onRefreshStatusChanged(long accountId, long mailboxId) {
            mEmailParent.onRefreshComplete();
            int unReaded = LoaderProvider.getReadedCount(mContext, mAccountId);
            HcLog.D(EmailUtils.DEBUG, TAG + " $RefreshListener#onRefreshStatusChanged accountId = "+accountId + " mailboxId = "+mailboxId + " unReaded = "+unReaded);
            mBadgeInfo.updateCount(unReaded);
            HcEmailSharedHelper.setRefreshTime(mContext, System.currentTimeMillis());
            mHander.pause();
            mHander.resume();
            mTime.setText("上次获取: 刚刚");
        }
    }

    @Override
    public void onDestory() {
        mHander.pause();
//        mRefreshManager.unregisterListener(mRefreshListener);
        mRefreshManager = null;
        mMessagingController.removeListener(mMessagingListener);
        mMessagingController = null;
        super.onDestory();
    }

    @Override
    public void onResume() {
        if (mFrom != -1) {
            return;
        }
        // 增加IM角标
        // 点击进入详情页的时候没有处理角标,这个可能页需要考虑下.
        int count = LoaderProvider.getReadedCount(mContext, mAccountId);
        HcLog.D(EmailUtils.DEBUG, TAG + "#onResume count = "+count);
        if (mBadgeInfo == null) {
            mBadgeInfo = BadgeCache.getInstance().getBadgeInfo(mAppId, mAppId + "&" + MODULE_NAME);
            if (mBadgeInfo == null) {
                mBadgeInfo = new ModuleBadgeInfo();
                mBadgeInfo.setAppId(mAppId);
                mBadgeInfo.setModuleId(mAppId + "&" + MODULE_NAME);
                mBadgeInfo.setType(1);
                mBadgeInfo.setCount(LoaderProvider.getReadedCount(mContext, mAccountId));
                BadgeCache.getInstance().matchBadge(mBadgeInfo, "add");
            } else {
                mBadgeInfo.updateCount(LoaderProvider.getReadedCount(mContext, mAccountId));
            }
        } else {
            mBadgeInfo.updateCount(LoaderProvider.getReadedCount(mContext, mAccountId));
        }
        mPointView.setCount(count);

        // 发送邮件的显示
        if (mSignedIn) {
            showOrhideStatusView();
            setRefreshTime(System.currentTimeMillis());
        }
    }

    private class SendMessageListener extends MessagingListener {

        @Override
        public void sendPendingMessagesCompleted(long accountId) {
            HcLog.D(EmailUtils.DEBUG, TAG + "$SendMessageListener#sendPendingMessagesCompleted accountId = "+accountId);
            mHander.post(new Runnable() {
                @Override
                public void run() {
                    if (LoaderProvider.getOutboxCount(mContext, mAccountId, 1) > 0) {
                        if (mSending.getVisibility() != View.VISIBLE) {
                            mSending.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (mSending.getVisibility() != View.GONE) {
                            mSending.setVisibility(View.GONE);
                        }
                    }
                }
            });

        }

        @Override
        public void sendPendingMessagesFailed(long accountId, long messageId, Exception reason) {
            HcLog.D(EmailUtils.DEBUG, TAG + "$SendMessageListener#sendPendingMessagesFailed accountId = "+accountId + " messageId = "+messageId + " reason = "+reason.toString());
            mHander.post(new Runnable() {
                @Override
                public void run() {
                    if (mSendFailed.getVisibility() != View.VISIBLE) {
                        mSendFailed.setVisibility(View.VISIBLE);
                    }

                    if (LoaderProvider.getOutboxCount(mContext, mAccountId, 1) > 0) {
                        if (mSending.getVisibility() != View.VISIBLE) {
                            mSending.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (mSending.getVisibility() != View.GONE) {
                            mSending.setVisibility(View.GONE);
                        }
                    }
                }
            });

        }

        @Override
        public void sendPendingMessagesStarted(long accountId, long messageId) {
            HcLog.D(EmailUtils.DEBUG, TAG + "$SendMessageListener#sendPendingMessagesStarted accountId = "+accountId + " messageId = "+messageId);
            mHander.post(new Runnable() {
                @Override
                public void run() {
                    if (mSending.getVisibility() != View.VISIBLE) {
                        mSending.setVisibility(View.VISIBLE);
                    }
                }
            });

        }

        @Override
        public void synchronizeMailboxFailed(long accountId, long mailboxId, Exception e) {
            int mailboxType = Mailbox.getMailboxType(mContext, mailboxId);
            HcLog.D(EmailUtils.DEBUG, TAG + "$SendMessageListener#synchronizeMailboxFailed accountId = "+accountId + " mailboxId = "+mailboxId + " error = "+e.toString() + " mailboxType = "+mailboxType);
            if (mailboxType == Mailbox.TYPE_INBOX) {
                mHander.post(new Runnable() {
                    @Override
                    public void run() {
                        mEmailParent.onRefreshComplete();
                    }
                });
            }

        }

        @Override
        public void synchronizeMailboxFinished(long accountId, long mailboxId, int totalMessagesInMailbox, int numNewMessages, ArrayList<Long> addedMessages) {
            int mailboxType = Mailbox.getMailboxType(mContext, mailboxId);
            HcLog.D(EmailUtils.DEBUG, TAG + "$SendMessageListener#synchronizeMailboxFinished accountId = "+accountId + " mailboxId = "+mailboxId + " totalMessagesInMailbox = "+totalMessagesInMailbox + " numNewMessages = "+numNewMessages + " mailboxType = "+mailboxType);
            if (mailboxType == Mailbox.TYPE_INBOX) {
                mHander.post(new Runnable() {
                    @Override
                    public void run() {
                        mEmailParent.onRefreshComplete();
                        int unReaded = LoaderProvider.getReadedCount(mContext, mAccountId);
                        HcLog.D(EmailUtils.DEBUG, TAG + " $SendMessageListener#synchronizeMailboxFinished unReaded = "+unReaded);
                        if (mBadgeInfo != null)
                            mBadgeInfo.updateCount(unReaded);
                        HcEmailSharedHelper.setRefreshTime(mContext, System.currentTimeMillis());
                        mHander.pause();
                        mHander.resume();
                        mTime.setText("上次获取: 刚刚");
                        mPointView.setCount(unReaded);
                    }
                });
            }
        }

        @Override
        public void synchronizeMailboxStarted(long accountId, long mailboxId) {
            HcLog.D(EmailUtils.DEBUG, TAG + "$SendMessageListener#synchronizeMailboxStarted accountId = "+accountId + " mailboxId = "+mailboxId);
        }
    }

    private void showSetupView() {
        mSignedIn = false;
        if (mEmailParent.getVisibility() != View.GONE) {
            mEmailParent.setVisibility(View.GONE);
        }
        if (mSetupParent.getVisibility() != View.VISIBLE) {
            mSetupParent.setVisibility(View.VISIBLE);
        }
    }

    private void hideSetupView() {
        mSignedIn = true;
        if (mSetupParent.getVisibility() != View.GONE) {
            mSetupParent.setVisibility(View.GONE);
        }
        if (mEmailParent.getVisibility() != View.VISIBLE) {
            mEmailParent.setVisibility(View.VISIBLE);
        }

        showOrhideStatusView();
        mHander.resume();
    }

    private void showOrhideStatusView() {
        if (LoaderProvider.getOutboxCount(mContext, mAccountId, 1) > 0) {
            if (mSending.getVisibility() != View.VISIBLE) {
                mSending.setVisibility(View.VISIBLE);
            }
        } else {
            if (mSending.getVisibility() != View.GONE) {
                mSending.setVisibility(View.GONE);
            }
        }

        if (LoaderProvider.getOutboxCount(mContext, mAccountId, 2) > 0) {
            if (mSendFailed.getVisibility() != View.VISIBLE) {
                mSendFailed.setVisibility(View.VISIBLE);
            }
        } else {
            if (mSendFailed.getVisibility() != View.GONE) {
                mSendFailed.setVisibility(View.GONE);
            }
        }
    }

    private class TimeHandler extends Handler {

        void resume() {
            if (!hasMessages(0)) {
                sendEmptyMessageDelayed(0, PLAY_TIME);
            }
        }

        void pause() {
            removeMessages(0);
        }

        @Override
        public void handleMessage(Message msg) {
            setRefreshTime(System.currentTimeMillis());
            sendEmptyMessageDelayed(0, PLAY_TIME);
        }
    }

    private void setRefreshTime(long currentTime) {
        long lastRefreshTime = HcEmailSharedHelper.getRefreshTime(mContext);
        StringBuilder builder = new StringBuilder("上次获取： ");
        if (lastRefreshTime <= 0) {
            builder.append("从未获取");
            mTime.setText(builder.toString());
            return;
        }
        long minute = (currentTime - lastRefreshTime) / (60 * 1000); // 分钟
        long hour = minute / 60;
        long day = hour / 24;
        if (day > 0) {
            builder.append(day + "天");
            minute = minute % 60;
            hour = hour % 24;
            if (hour > 0) {
                builder.append(hour + "小时");
            }
            if (minute > 0) {
                builder.append(minute + "分钟");
            }
            builder.append("前");
        } else if (hour > 0) {
            builder.append(hour + "小时");
            minute = minute % 60;
            if (minute > 0) {
                builder.append(minute + "分钟");
            }
            builder.append("前");
        } else if (minute > 0) {
            builder.append(minute + "分钟前");
        } else {
            builder.append("刚刚");
        }
        mTime.setText(builder.toString());

    }

    private void writeEmailFromContacts() {
        Intent intent = new Intent();
        intent.setClass(mContext, HcmailWriteActivity.class);
        intent.setAction(HcmailWriteActivity.ACTION_CONTACT);
        intent.putExtra("to", mAccount + "<" + mAddress + ">");
        mContext.startActivity(intent);
        mContext.finish();
    }
}