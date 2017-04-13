package com.android.hcmail;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.os.Bundle;

import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.email.Controller;
import com.android.email.MessageListContext;
import com.android.email.data.ThrottlingCursorLoader;
import com.android.emailcommon.provider.Account;
import com.android.emailcommon.provider.EmailContent;
import com.android.emailcommon.provider.Mailbox;
import com.android.emailcommon.utility.Utility;
import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.email.R;
import com.android.hcframe.pull.PullToRefreshListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zhujiabin on 2017/3/16.
 */

public class HcmailInboxActivity extends HcBaseActivity implements View.OnClickListener, HcmailInboxAdapter.OnClickCallback, HcmailInboxAdapter.OnItemClickCallback, LoaderManager.LoaderCallbacks<Cursor> {

    private TopBarView mInboxTopBar;
    private static PullToRefreshListView mInboxList;
    private static HcmailInboxAdapter mAdapter;
    private LinearLayout mInboxTop;
    private LinearLayout mWrite;
    private LinearLayout mText;
    private TextView mCancel;
    private TextView mCenter;
    private TextView mAll;
    private static List<HcmailInbox> mMailBoxList;
    private List<HcmailInbox> mMailBoxSelectList;
    private int checkNum = 0;
    HcmailInboxAdapter.HcmailInboxViewHolder mInboxViewHolder;
    private HcmailInbox mHcmailInbox;

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

    public MessageListContext viewContext;

    static final String[] MESSAGE_PROJECTION = new String[]{
            EmailContent.RECORD_ID, EmailContent.MessageColumns.MAILBOX_KEY, EmailContent.MessageColumns.ACCOUNT_KEY,
            EmailContent.MessageColumns.DISPLAY_NAME, EmailContent.MessageColumns.SUBJECT, EmailContent.MessageColumns.TIMESTAMP,
            EmailContent.MessageColumns.FLAG_READ, EmailContent.MessageColumns.FLAG_FAVORITE, EmailContent.MessageColumns.FLAG_ATTACHMENT,
            EmailContent.MessageColumns.FLAGS, EmailContent.MessageColumns.SNIPPET
    };

    // Controller access
    private Controller mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hcmail_inbox_list);
        initView();
        initData();
        initEvent();
    }


    private void initView() {
        mInboxTopBar = (TopBarView) findViewById(R.id.hcmail_inbox_top_bar);
        mInboxTop = (LinearLayout) findViewById(R.id.hcmail_inbox_top);
        mInboxList = (PullToRefreshListView) findViewById(R.id.hcmail_inbox_list);
        mCancel = (TextView) findViewById(R.id.hcmail_inbox_cancel);
        mCenter = (TextView) findViewById(R.id.hcmail_inbox_center);
        mAll = (TextView) findViewById(R.id.hcmail_inbox_all);
        mWrite = (LinearLayout) findViewById(R.id.hcmail_inbox_write_footer);
        mText = (LinearLayout) findViewById(R.id.hcmail_inbox_text_footer);
    }

    private void initData() {
        mController = Controller.getInstance(this);
        //初始化数据
        final Intent intent = getIntent();
        viewContext = MessageListContext.forIntent(this, intent);
        //此处每次返回都会开启***
        getLoaderManager().initLoader(0, null, this);
        mMailBoxList = new ArrayList<>();
        mMailBoxSelectList = new ArrayList<>();
        mInboxTopBar.setTitle("收件箱");
        mInboxTopBar.setMenuBtnVisiable(View.VISIBLE);
        mInboxTopBar.setMenuSrc(R.drawable.hcmail_transpate_delete);
        mInboxTopBar.setMenuListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //点击删除按钮
                mInboxTopBar.setVisibility(View.GONE);
                mInboxTop.setVisibility(View.VISIBLE);
                mText.setVisibility(View.VISIBLE);
                mWrite.setVisibility(View.GONE);
                HcLog.D("mMailBoxList.size() = "+mMailBoxList.size());
                HcLog.D("mMailBoxSelectList.size() = "+mMailBoxSelectList.size());
                mAdapter = new HcmailInboxAdapter(HcmailInboxActivity.this, mMailBoxList, true);
                mInboxList.setAdapter(mAdapter);
            }
        });
    }

    private void initEvent() {
        mCancel.setOnClickListener(this);
        mAll.setOnClickListener(this);
        mText.setOnClickListener(this);
        mWrite.setOnClickListener(this);
        HcmailInboxAdapter.setOnClickCallback(this);
        HcmailInboxAdapter.setOnItemClickCallback(this);
    }

    private void classifyInboxList() {
        HcLog.D("mMailBoxList.size() = " + mMailBoxList.size());
        mAdapter = new HcmailInboxAdapter(HcmailInboxActivity.this, mMailBoxList, false);
        mInboxList.setAdapter(mAdapter);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getLoaderManager().destroyLoader(0);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.hcmail_inbox_cancel) {
            mWrite.setClickable(true);
            mText.setClickable(false);
            mInboxTopBar.setVisibility(View.VISIBLE);
            mInboxTop.setVisibility(View.GONE);
            mText.setVisibility(View.GONE);
            mWrite.setVisibility(View.VISIBLE);
            mAdapter = new HcmailInboxAdapter(HcmailInboxActivity.this, mMailBoxList, false);
            mInboxList.setAdapter(mAdapter);
        } else if (i == R.id.hcmail_inbox_all) {
            if (mAll.getText().equals("全选")) {
                checkNum = 0;
                mAll.setText("全不选");
                for (int j = 0; j < mMailBoxList.size(); j++) {
                    mMailBoxList.get(j).setmChecked(true);
                    checkNum++;
                }
                mMailBoxSelectList.addAll(mMailBoxList);
                dataChanged();
            } else if (mAll.getText().equals("全不选")) {
                mAll.setText("全选");
                checkNum = 0;
                for (int j = 0; j < mMailBoxList.size(); j++) {
                    mMailBoxList.get(j).setmChecked(false);
                }
                mMailBoxSelectList.clear();
                dataChanged();
            }
        } else if (i == R.id.hcmail_inbox_write_footer) {
            //写邮件
            Intent intent = new Intent();
            intent.setClass(this, HcmailWriteActivity.class);
            startActivity(intent);
        } else if (i == R.id.hcmail_inbox_text_footer) {
            //删除邮件
            HcLog.D(" mMailBoxSelectList.size() = " + mMailBoxSelectList.size());
            final Set set = new HashSet();
            for (int j = 0; j < mMailBoxSelectList.size(); j++) {
                set.add(Long.valueOf(mMailBoxSelectList.get(j).getmInboxId()));
                HcLog.D("imMailBoxSelectList.get(j).getmInboxId()= " + mMailBoxSelectList.get(j).getmInboxId());
            }
            deleteMessages(set);
        }
    }

    // 刷新listview和TextView的显示
    private void dataChanged() {
        // 通知listView刷新
        mAdapter.notifyDataSetChanged();
        // TextView显示最新的选中数目
        mCenter.setText("收件箱(" + checkNum + ")");
    }

    @Override
    public void onCheckBoxClick(View view, int position, HcmailInbox item) {
        mWrite.setClickable(false);
        mText.setClickable(true);
        mInboxViewHolder = (HcmailInboxAdapter.HcmailInboxViewHolder) view.getTag();
        mHcmailInbox = mAdapter.getItem(position);
        HcLog.D("checkNum = "+checkNum);
        // 调整选定条目
        if (mInboxViewHolder.checkbox.isChecked() == true) {
            checkNum++;
            mMailBoxSelectList.add(mHcmailInbox);
            HcLog.D("mMailBoxSelectList ss  = " + mMailBoxSelectList.size());
            if (checkNum == (mMailBoxList.size())) {
                mCenter.setText("全不选");
                mMailBoxSelectList.addAll(mMailBoxList);
            } else if (checkNum > 0 && checkNum < (mMailBoxList.size())) {
                mCenter.setText("全选");
            }
            mCenter.setText("收件箱(" + checkNum + ")");
        } else if (mInboxViewHolder.checkbox.isChecked() == false) {
            checkNum--;
            mMailBoxSelectList.remove(mMailBoxList);
            mCenter.setText("收件箱(" + checkNum + ")");
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mController.updateMailbox(viewContext.mAccountId, viewContext.getMailboxId(), false);
        return new MessagesCursorLoader(HcmailInboxActivity.this, viewContext);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        classifyInboxList();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onItemClick(View view, int position, HcmailInbox item) {
        mHcmailInbox = mAdapter.getItem(position);
        HcLog.D("Id = " + mHcmailInbox.getmInboxId() + "Title = " + mHcmailInbox.getmInboxTitle());
        Intent intent = new Intent();
        intent.setClass(this, HcmailViewActivity.class);
        intent.putExtra("inbox", mHcmailInbox);
        startActivity(intent);
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
            mAccountId = listContext.mAccountId;
            mMailboxId = listContext.getMailboxId();
            HcLog.D("mAccountId=" + mAccountId + "mMailboxId =" + mMailboxId + "EmailContent.Message.CONTENT_URI = " + EmailContent.Message.CONTENT_URI + " EmailContent.MessageColumns.TIMESTAMP = " + EmailContent.MessageColumns.TIMESTAMP);
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
                        isEasAccount = account.isEasAccount(mContext);
                        isRefreshable = Mailbox.isRefreshable(mContext, mMailboxId);
                    } else { // Account removed?
                        mailbox = null;
                    }
                }
            }
            final int countAccounts = EmailContent.count(mContext, Account.CONTENT_URI);
            CallMessageCursor callLogsCursor = new CallMessageCursor(baseCursor);
            return callLogsCursor;
        }

        public class CallMessageCursor extends CursorWrapper {

            /**
             * Creates a cursor wrapper.
             *
             * @param cursor The underlying cursor to wrap.
             */
            public CallMessageCursor(Cursor cursor) {
                super(cursor);
                HcLog.D("cursor = " + cursor);
                mMailBoxList.clear();
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    int mHasInvite = EmailContent.Message.FLAG_INCOMING_MEETING_INVITE;
                    int mHasBeenRepliedTo = EmailContent.Message.FLAG_REPLIED_TO;
                    int mHasBeenForwarded = EmailContent.Message.FLAG_FORWARDED;
                    while (!cursor.isAfterLast()) {
                        HcmailInbox inbox = new HcmailInbox();
                        long mMessageId = cursor.getLong(COLUMN_ID);
                        HcLog.D("mMessageId = " + mMessageId);
                        inbox.setmInboxId(String.valueOf(mMessageId));
                        long mMailboxId = cursor.getLong(COLUMN_MAILBOX_KEY);
                        final long accountId = cursor.getLong(COLUMN_ACCOUNT_KEY);
                        boolean isRead = (cursor.getInt(COLUMN_READ) != 0);
                        if (isRead) {
                            inbox.setmInboxImg("已读");
                        } else {
                            inbox.setmInboxImg("未读");
                        }
                        int mIsFavorite = cursor.getInt(COLUMN_FAVORITE);
                        int flags = cursor.getInt(COLUMN_FLAGS);
                        int mHasAttachment = cursor.getInt(COLUMN_ATTACHMENTS);
                        long mDate = cursor.getLong(COLUMN_DATE);
                        String date = HcmailUtils.getDateToString(mDate);
                        inbox.setmInboxDate(date);
                        String mSender = cursor.getString(COLUMN_DISPLAY_NAME);
                        inbox.setmInboxName(mSender);
                        String mSubject = cursor.getString(COLUMN_SUBJECT);
                        inbox.setmInboxTitle(mSubject);
                        String mSnippet = cursor.getString(COLUMN_SNIPPET);
                        mMailBoxList.add(inbox);
                        cursor.moveToNext();
                    }
                }
            }
        }
    }

    private void deleteMessages(Set<Long> selectedSet) {
        mInboxTopBar.setVisibility(View.VISIBLE);
        mInboxTop.setVisibility(View.GONE);
        mText.setVisibility(View.GONE);
        mWrite.setVisibility(View.VISIBLE);
        mWrite.setClickable(true);
        mText.setClickable(false);
        checkNum = 0;
        mCenter.setText("收件箱(" + checkNum + ")");
        final long[] messageIds = Utility.toPrimitiveLongArray(selectedSet);
        mController.deleteMessages(messageIds);
        mMailBoxList.removeAll( mMailBoxSelectList);
        mMailBoxSelectList.clear();
        mAdapter.notifyDataSetChanged();
        HcUtil.showToast(this, R.string.hcmail_delete);
        selectedSet.clear();
    }

}
