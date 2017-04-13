package com.android.hcframe.hcmail.task;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;

import com.android.email.MessageListContext;
import com.android.emailcommon.provider.Account;
import com.android.emailcommon.provider.Mailbox;
import com.android.emailcommon.utility.IntentUtilities;
import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcUtil;
import com.android.hcframe.email.R;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 17-3-23 10:46.
 */

public class EmailActivity extends HcBaseActivity {

    public static final String EXTRA_ACCOUNT_ID = "ACCOUNT_ID";
    public static final String EXTRA_MAILBOX_ID = "MAILBOX_ID";
    public static final String EXTRA_MAILBOX_TYPE = "MAILBOX_TYPE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);
        Intent intent = getIntent();
        long accountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, Account.NO_ACCOUNT);
        long mailboxId = intent.getLongExtra(EXTRA_MAILBOX_ID, Mailbox.NO_MAILBOX);
        if (accountId == Account.NO_ACCOUNT || mailboxId == Mailbox.NO_MAILBOX) {
            finish();
            return;
        }
        int mailboxType = intent.getIntExtra(EXTRA_MAILBOX_TYPE, Mailbox.TYPE_INBOX);
        FragmentManager manager = getFragmentManager();
        EmailFragment fragment = (EmailFragment) manager.findFragmentById(R.id.email_fragment_parent);
        MessageListContext listContext = MessageListContext.forMailbox(accountId, mailboxId);
        if (fragment == null) {
            fragment = EmailFragment.newInstance(listContext, mailboxType);
            HcUtil.addFragmentToActivity(manager, fragment, R.id.email_fragment_parent);
        }

        new EmailPresenter(this, getLoaderManager(), fragment, listContext);
    }

    /**
     * Create an intent to launch and open a mailbox.
     *
     * @param accountId must not be -1.
     * @param mailboxId must not be -1.  Magic mailboxes IDs (such as
     * {@link Mailbox#QUERY_ALL_INBOXES}) don't work.
     */
    public static Intent createOpenMailboxIntent(Activity fromActivity, long accountId,
                                                 long mailboxId, int mailboxType) {
        if (accountId == -1 || mailboxId == -1) {
            throw new IllegalArgumentException();
        }
        Intent i = IntentUtilities.createRestartAppIntent(fromActivity, EmailActivity.class);
        i.putExtra(EXTRA_ACCOUNT_ID, accountId);
        i.putExtra(EXTRA_MAILBOX_ID, mailboxId);
        i.putExtra(EXTRA_MAILBOX_TYPE, mailboxType);
        return i;
    }
}
