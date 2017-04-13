package com.android.hcmail;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.os.Bundle;
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
 * Created by zhujiabin on 2017/3/17.
 */

public class HcmailOutboxActivity extends HcBaseActivity implements View.OnClickListener, HcmailOutboxAdapter.OnClickCallback, HcmailOutboxAdapter.OnItemCallback, LoaderManager.LoaderCallbacks<Cursor> {

    private TopBarView mMailBoxTopBar;
    private PullToRefreshListView mMailBoxList;
    private HcmailOutboxAdapter mAdapter;
    private LinearLayout mMailBoxTop;
    private LinearLayout mText;
    private LinearLayout mWrite;
    private LinearLayout mOutboxTop;
    private TextView mCancel;
    private TextView mCenter;
    private TextView mAll;
    private static List<HcmailOutbox> mOutBoxList;
    private List<HcmailOutbox> mOutBoxSelectList;
    private int checkNum = 0;
    HcmailOutboxAdapter.HcmailOutboxViewHolder BoxViewHolder;
    private HcmailOutbox mHcmailOutbox;

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

    // Controller access
    private Controller mController;

    static final String[] MESSAGE_PROJECTION = new String[]{
            EmailContent.RECORD_ID, EmailContent.MessageColumns.MAILBOX_KEY, EmailContent.MessageColumns.ACCOUNT_KEY,
            EmailContent.MessageColumns.DISPLAY_NAME, EmailContent.MessageColumns.SUBJECT, EmailContent.MessageColumns.TIMESTAMP,
            EmailContent.MessageColumns.FLAG_READ, EmailContent.MessageColumns.FLAG_FAVORITE, EmailContent.MessageColumns.FLAG_ATTACHMENT,
            EmailContent.MessageColumns.FLAGS, EmailContent.MessageColumns.SNIPPET
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hcmail_outbox_list);
        initView();
        initData();
        initEvent();
    }

    private void initView() {
        mMailBoxTopBar = (TopBarView) findViewById(R.id.hcmail_outbox_top_bar);
        mOutboxTop = (LinearLayout) findViewById(R.id.hcmail_outbox_top);
        mMailBoxList = (PullToRefreshListView) findViewById(R.id.hcmail_outbox_list);
        mMailBoxTop = (LinearLayout) findViewById(R.id.hcmail_outbox_top);
        mCancel = (TextView) findViewById(R.id.hcmail_outbox_cancel);
        mCenter = (TextView) findViewById(R.id.hcmail_outbox_center);
        mAll = (TextView) findViewById(R.id.hcmail_outbox_all);
        mWrite = (LinearLayout) findViewById(R.id.hcmail_outbox_write_footer);
        mText = (LinearLayout) findViewById(R.id.hcmail_outbox_text_footer);
    }

    private void initData() {
        mOutBoxList = new ArrayList<>();
        mOutBoxSelectList = new ArrayList<>();
        mMailBoxTopBar.setTitle("已发送");
        mMailBoxTopBar.setMenuBtnVisiable(View.VISIBLE);
        mMailBoxTopBar.setMenuSrc(R.drawable.hcmail_transpate_delete);
        mMailBoxTopBar.setMenuListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //点击删除按钮
                mMailBoxTopBar.setVisibility(View.GONE);
                mMailBoxTop.setVisibility(View.VISIBLE);
                mText.setVisibility(View.VISIBLE);
                mWrite.setVisibility(View.GONE);
                mWrite.setClickable(false);
                mText.setClickable(true);
                mAdapter = new HcmailOutboxAdapter(HcmailOutboxActivity.this, mOutBoxList, true);
                mMailBoxList.setAdapter(mAdapter);
            }
        });
//        init();
        mController = Controller.getInstance(this);
        //初始化数据
        final Intent intent = getIntent();
        viewContext = MessageListContext.forIntent(this, intent);
        //此处每次返回都会开启***
        getLoaderManager().initLoader(0, null, this);
    }

    private void init() {
        HcmailOutbox outbox = new HcmailOutbox();
        outbox.setBoxImg("");
        outbox.setBoxId("1");
        outbox.setBoxTitle("朱氏邮件标题");
        outbox.setBoxName("朱家斌");
        outbox.setBoxDate("2017年3月16日");
        mOutBoxList.add(outbox);
        HcmailOutbox outbox1 = new HcmailOutbox();
        outbox1.setBoxImg("");
        outbox1.setBoxId("2");
        outbox1.setBoxTitle("牛氏邮件标题");
        outbox1.setBoxName("牛家斌");
        outbox1.setBoxDate("2017年3月6日");
        mOutBoxList.add(outbox1);
        HcmailOutbox outbox2 = new HcmailOutbox();
        outbox2.setBoxImg("");
        outbox2.setBoxId("2");
        outbox2.setBoxTitle("牛氏邮件标题");
        outbox2.setBoxName("牛家斌");
        outbox2.setBoxDate("2017年2月28日");
        mOutBoxList.add(outbox2);
        HcmailOutbox outbox3 = new HcmailOutbox();
        outbox3.setBoxImg("");
        outbox3.setBoxId("3");
        outbox3.setBoxTitle("天氏邮件标题");
        outbox3.setBoxName("天家斌");
        outbox3.setBoxDate("2017年3月10日");
        mOutBoxList.add(outbox3);
        HcmailOutbox outbox4 = new HcmailOutbox();
        outbox4.setBoxImg("");
        outbox4.setBoxId("4");
        outbox4.setBoxTitle("地氏邮件标题");
        outbox4.setBoxName("地家斌");
        outbox4.setBoxDate("2017年3月9日");
        mOutBoxList.add(outbox4);
        mAdapter = new HcmailOutboxAdapter(this, mOutBoxList, false);
        mMailBoxList.setAdapter(mAdapter);
    }

    private void initEvent() {
        mCancel.setOnClickListener(this);
        mAll.setOnClickListener(this);
        mText.setOnClickListener(this);
        mWrite.setOnClickListener(this);
        HcmailOutboxAdapter.setOnClickCallback(this);
        HcmailOutboxAdapter.setOnItemCallback(this);
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
        if (i == R.id.hcmail_outbox_cancel) {
            mMailBoxTopBar.setVisibility(View.VISIBLE);
            mMailBoxTop.setVisibility(View.GONE);
            mText.setVisibility(View.GONE);
            mWrite.setVisibility(View.VISIBLE);
            mWrite.setClickable(true);
            mText.setClickable(false);
            checkNum = 0;
            mAdapter = new HcmailOutboxAdapter(HcmailOutboxActivity.this, mOutBoxList, false);
            mMailBoxList.setAdapter(mAdapter);
        } else if (i == R.id.hcmail_outbox_all) {
            if (mAll.getText().equals("全选")) {
                checkNum = 0;
                mAll.setText("全不选");
                for (int j = 0; j < mOutBoxList.size(); j++) {
                    mOutBoxList.get(j).setChecked(true);
                    checkNum++;
                }
                mOutBoxSelectList.addAll(mOutBoxList);
                dataChanged();
            } else if (mAll.getText().equals("全不选")) {
                mAll.setText("全选");
                checkNum = 0;
                for (int j = 0; j < mOutBoxList.size(); j++) {
                    mOutBoxList.get(j).setChecked(false);
                }
                mOutBoxSelectList.clear();
                dataChanged();
            }
        } else if (i == R.id.hcmail_outbox_text_footer) {
            //删除邮件
            HcLog.D(" mOutBoxSelectList.size() = " + mOutBoxSelectList.size());
            final Set set = new HashSet();
            for (int j = 0; j < mOutBoxSelectList.size(); j++) {
                set.add(Long.valueOf(mOutBoxSelectList.get(j).getBoxId()));
                HcLog.D("mOutBoxSelectList.get(j).getBoxId()= " + mOutBoxSelectList.get(j).getBoxId());
            }
            deleteMessages(set);
        } else if (i == R.id.hcmail_outbox_write_footer) {
            //写邮件
            Intent intent = new Intent();
            intent.setClass(this, HcmailWriteActivity.class);
            startActivity(intent);
        }
    }

    private void deleteMessages(Set<Long> selectedSet) {
        mMailBoxTopBar.setVisibility(View.VISIBLE);
        mOutboxTop.setVisibility(View.GONE);
        mText.setVisibility(View.GONE);
        mWrite.setVisibility(View.VISIBLE);
        mWrite.setClickable(true);
        mText.setClickable(false);
        checkNum = 0;
        mOutBoxSelectList.clear();
        mCenter.setText("收件箱(" + checkNum + ")");
        final long[] messageIds = Utility.toPrimitiveLongArray(selectedSet);
        mController.deleteMessages(messageIds);
        mOutBoxList.removeAll(mOutBoxSelectList);
        mOutBoxSelectList.clear();
        mAdapter.notifyDataSetChanged();
        HcUtil.showToast(this, R.string.hcmail_delete);
        selectedSet.clear();
    }

    // 刷新listview和TextView的显示
    private void dataChanged() {
        // 通知listView刷新
        mAdapter.notifyDataSetChanged();
        // TextView显示最新的选中数目
        mCenter.setText("已发送(" + checkNum + ")");
    }

    @Override
    public void onCheckBoxClick(View view, int position, HcmailOutbox item) {
        mWrite.setClickable(false);
        mText.setClickable(true);
        BoxViewHolder = (HcmailOutboxAdapter.HcmailOutboxViewHolder) view.getTag();
        mHcmailOutbox = mAdapter.getItem(position);

        // 调整选定条目
        if (BoxViewHolder.checkbox.isChecked() == true) {
            checkNum++;
            mOutBoxSelectList.add(mHcmailOutbox);
            if (checkNum == mOutBoxList.size()) {
                mCenter.setText("全不选");
                mOutBoxSelectList.addAll(mOutBoxList);
            } else if (checkNum > 0 && checkNum < mOutBoxList.size()) {
                mCenter.setText("全选");
            }
            mCenter.setText("已发送(" + checkNum + ")");
        } else if (BoxViewHolder.checkbox.isChecked() == false) {
            checkNum--;
            mOutBoxSelectList.remove(mHcmailOutbox);
            mCenter.setText("已发送(" + checkNum + ")");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mController.updateMailbox(viewContext.mAccountId, viewContext.getMailboxId(), false);
        return new MessagesCursorLoader(HcmailOutboxActivity.this, viewContext);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        classifyOutmMailBoxList();
    }

    private void classifyOutmMailBoxList() {
        HcLog.D("mOutBoxList.size() = " + mOutBoxList.size());
        mAdapter = new HcmailOutboxAdapter(HcmailOutboxActivity.this, mOutBoxList, false);
        mMailBoxList.setAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onItemClick(View view, int position, HcmailOutbox item) {
        //写邮件
        Intent intent = new Intent();
        intent.setClass(this, HcmailWriteActivity.class);
        intent.setAction(HcmailWriteActivity.ACTION_OUT_BOX);
        intent.putExtra("HcmailOutbox",item);
        startActivity(intent);
    }

    private static class MessagesCursorLoader extends ThrottlingCursorLoader {
        protected Context mContext;
        private long mAccountId;
        private long mMailboxId;

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
                mOutBoxList.clear();
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    int mHasInvite = EmailContent.Message.FLAG_INCOMING_MEETING_INVITE;
                    int mHasBeenRepliedTo = EmailContent.Message.FLAG_REPLIED_TO;
                    int mHasBeenForwarded = EmailContent.Message.FLAG_FORWARDED;
                    while (!cursor.isAfterLast()) {
                        HcmailOutbox outbox = new HcmailOutbox();
                        long mMessageId = cursor.getLong(COLUMN_ID);
                        HcLog.D("mMessageId = " + mMessageId);
                        outbox.setBoxId(String.valueOf(mMessageId));
                        long mMailboxId = cursor.getLong(COLUMN_MAILBOX_KEY);
                        final long accountId = cursor.getLong(COLUMN_ACCOUNT_KEY);
                        boolean isRead = (cursor.getInt(COLUMN_READ) != 0);
                        if (isRead) {
                            outbox.setBoxImg("已读");
                        } else {
                            outbox.setBoxImg("未读");
                        }
                        int mIsFavorite = cursor.getInt(COLUMN_FAVORITE);
                        int flags = cursor.getInt(COLUMN_FLAGS);
                        int mHasAttachment = cursor.getInt(COLUMN_ATTACHMENTS);
                        long mDate = cursor.getLong(COLUMN_DATE);
                        String date = HcmailUtils.getDateToString(mDate);
                        outbox.setBoxDate(date);
                        String mSender = cursor.getString(COLUMN_DISPLAY_NAME);
                        outbox.setBoxName(mSender);
                        String mSubject = cursor.getString(COLUMN_SUBJECT);
                        outbox.setBoxTitle(mSubject);
                        String mSnippet = cursor.getString(COLUMN_SNIPPET);
                        mOutBoxList.add(outbox);
                        cursor.moveToNext();
                    }
                }
            }
        }
    }
}
