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
 * Created by zhujiabin on 2017/3/29.
 */

public class HcmailSendboxActivity extends HcBaseActivity implements View.OnClickListener,HcmailSendboxAdapter.OnClickCallback, HcmailSendboxAdapter.OnItemCallback, LoaderManager.LoaderCallbacks<Cursor>{

    private TopBarView mMailBoxTopBar;
    private PullToRefreshListView mMailBoxList;
    private HcmailSendboxAdapter mAdapter;
    private LinearLayout mMailBoxTop;
    private LinearLayout mText;
    private LinearLayout mWrite;
    private LinearLayout mSendboxTop;
    private TextView mCancel;
    private TextView mCenter;
    private TextView mAll;
    private static List<HcmailSendbox> mSendBoxList;
    private List<HcmailSendbox> mSendBoxSelectList;
    private int checkNum = 0;
    HcmailSendboxAdapter.HcmailSendboxViewHolder BoxViewHolder;
    private HcmailSendbox mHcmailSendbox;

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
        setContentView(R.layout.hcmail_sendbox_list);
        initView();
        initData();
        initEvent();
    }

    private void initView() {
        mMailBoxTopBar = (TopBarView) findViewById(R.id.hcmail_sendbox_top_bar);
        mSendboxTop = (LinearLayout) findViewById(R.id.hcmail_sendbox_top);
        mMailBoxList = (PullToRefreshListView) findViewById(R.id.hcmail_sendbox_list);
        mMailBoxTop = (LinearLayout) findViewById(R.id.hcmail_sendbox_top);
        mCancel = (TextView) findViewById(R.id.hcmail_sendbox_cancel);
        mCenter = (TextView) findViewById(R.id.hcmail_sendbox_center);
        mAll = (TextView) findViewById(R.id.hcmail_sendbox_all);
        mWrite = (LinearLayout) findViewById(R.id.hcmail_sendbox_write_footer);
        mText = (LinearLayout) findViewById(R.id.hcmail_sendbox_text_footer);
    }

    private void initData() {
        mSendBoxList = new ArrayList<>();
        mSendBoxSelectList = new ArrayList<>();
        mMailBoxTopBar.setTitle("发件箱");
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
                mAdapter = new HcmailSendboxAdapter(HcmailSendboxActivity.this, mSendBoxList, true);
                mMailBoxList.setAdapter(mAdapter);
            }
        });
//        init();
        mController = Controller.getInstance(this);
        //初始化数据
        final Intent intent = getIntent();
        viewContext = MessageListContext.forIntent(this, intent);
        //此处每次返回都会开启
        getLoaderManager().initLoader(0, null, this);
    }

    private void init() {
        HcmailSendbox sendbox = new HcmailSendbox();
        sendbox.setBoxImg("");
        sendbox.setBoxId("1");
        sendbox.setBoxTitle("朱氏邮件标题");
        sendbox.setBoxName("朱家斌");
        sendbox.setBoxDate("2017年3月16日");
        mSendBoxList.add(sendbox);
        HcmailSendbox sendbox1 = new HcmailSendbox();
        sendbox1.setBoxImg("");
        sendbox1.setBoxId("2");
        sendbox1.setBoxTitle("牛氏邮件标题");
        sendbox1.setBoxName("牛家斌");
        sendbox1.setBoxDate("2017年3月6日");
        mSendBoxList.add(sendbox1);
        HcmailSendbox sendbox2 = new HcmailSendbox();
        sendbox2.setBoxImg("");
        sendbox2.setBoxId("2");
        sendbox2.setBoxTitle("牛氏邮件标题");
        sendbox2.setBoxName("牛家斌");
        sendbox2.setBoxDate("2017年2月28日");
        mSendBoxList.add(sendbox2);
        HcmailSendbox sendbox3 = new HcmailSendbox();
        sendbox3.setBoxImg("");
        sendbox3.setBoxId("3");
        sendbox3.setBoxTitle("天氏邮件标题");
        sendbox3.setBoxName("天家斌");
        sendbox3.setBoxDate("2017年3月10日");
        mSendBoxList.add(sendbox3);
        HcmailSendbox sendbox4 = new HcmailSendbox();
        sendbox4.setBoxImg("");
        sendbox4.setBoxId("4");
        sendbox4.setBoxTitle("地氏邮件标题");
        sendbox4.setBoxName("地家斌");
        sendbox4.setBoxDate("2017年3月9日");
        mSendBoxList.add(sendbox4);
        mAdapter = new HcmailSendboxAdapter(this, mSendBoxList, false);
        mMailBoxList.setAdapter(mAdapter);
    }

    private void initEvent() {
        mCancel.setOnClickListener(this);
        mAll.setOnClickListener(this);
        mText.setOnClickListener(this);
        mWrite.setOnClickListener(this);
        HcmailSendboxAdapter.setOnClickCallback(this);
        HcmailSendboxAdapter.setOnItemCallback(this);
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
        if (i == R.id.hcmail_sendbox_cancel) {
            mMailBoxTopBar.setVisibility(View.VISIBLE);
            mMailBoxTop.setVisibility(View.GONE);
            mText.setVisibility(View.GONE);
            mWrite.setVisibility(View.VISIBLE);
            mWrite.setClickable(true);
            mText.setClickable(false);
            checkNum = 0;
            mAdapter = new HcmailSendboxAdapter(HcmailSendboxActivity.this, mSendBoxList, false);
            mMailBoxList.setAdapter(mAdapter);
        } else if (i == R.id.hcmail_sendbox_all) {
            if (mAll.getText().equals("全选")) {
                checkNum = 0;
                mAll.setText("全不选");
                for (int j = 0; j < mSendBoxList.size(); j++) {
                    mSendBoxList.get(j).setChecked(true);
                    checkNum++;
                }
                mSendBoxSelectList.addAll(mSendBoxList);
                dataChanged();
            } else if (mAll.getText().equals("全不选")) {
                mAll.setText("全选");
                checkNum = 0;
                for (int j = 0; j < mSendBoxList.size(); j++) {
                    mSendBoxList.get(j).setChecked(false);
                }
                mSendBoxSelectList.clear();
                dataChanged();
            }
        } else if (i == R.id.hcmail_sendbox_text_footer) {
            //删除邮件
            HcLog.D(" mSendBoxSelectList.size() = " + mSendBoxSelectList.size());
            final Set set = new HashSet();
            for (int j = 0; j < mSendBoxSelectList.size(); j++) {
                set.add(Long.valueOf(mSendBoxSelectList.get(j).getBoxId()));
                HcLog.D("mSendBoxSelectList.get(j).getBoxId()= " + mSendBoxSelectList.get(j).getBoxId());
            }
            deleteMessages(set);
        } else if (i == R.id.hcmail_sendbox_write_footer) {
            //写邮件
            Intent intent = new Intent();
            intent.setClass(this, HcmailWriteActivity.class);
            startActivity(intent);
        }
    }

    private void deleteMessages(Set<Long> selectedSet) {
        mMailBoxTopBar.setVisibility(View.VISIBLE);
        mSendboxTop.setVisibility(View.GONE);
        mText.setVisibility(View.GONE);
        mWrite.setVisibility(View.VISIBLE);
        mWrite.setClickable(true);
        mText.setClickable(false);
        checkNum = 0;
        mSendBoxSelectList.clear();
        mCenter.setText("收件箱(" + checkNum + ")");
        final long[] messageIds = Utility.toPrimitiveLongArray(selectedSet);
        mController.deleteMessages(messageIds);
        mSendBoxList.removeAll(mSendBoxSelectList);
        mSendBoxSelectList.clear();
        mAdapter.notifyDataSetChanged();
        HcUtil.showToast(this, R.string.hcmail_delete);
        selectedSet.clear();
    }

    // 刷新listview和TextView的显示
    private void dataChanged() {
        // 通知listView刷新
        mAdapter.notifyDataSetChanged();
        // TextView显示最新的选中数目
        mCenter.setText("发件箱(" + checkNum + ")");
    }

    @Override
    public void onCheckBoxClick(View view, int position, HcmailSendbox item) {
        mWrite.setClickable(false);
        mText.setClickable(true);
        BoxViewHolder = (HcmailSendboxAdapter.HcmailSendboxViewHolder) view.getTag();
        mHcmailSendbox = mAdapter.getItem(position);

        // 调整选定条目
        if (BoxViewHolder.checkbox.isChecked() == true) {
            checkNum++;
            mSendBoxSelectList.add(mHcmailSendbox);
            if (checkNum == mSendBoxList.size()) {
                mCenter.setText("全不选");
                mSendBoxSelectList.addAll(mSendBoxList);
            } else if (checkNum > 0 && checkNum < mSendBoxList.size()) {
                mCenter.setText("全选");
            }
            mCenter.setText("发件箱(" + checkNum + ")");
        } else if (BoxViewHolder.checkbox.isChecked() == false) {
            checkNum--;
            mSendBoxSelectList.remove(mHcmailSendbox);
            mCenter.setText("发件箱(" + checkNum + ")");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mController.updateMailbox(viewContext.mAccountId, viewContext.getMailboxId(), false);
        return new HcmailSendboxActivity.MessagesCursorLoader(HcmailSendboxActivity.this, viewContext);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        classifyOutmMailBoxList();
    }

    private void classifyOutmMailBoxList() {
        HcLog.D("mSendBoxList.size() = " + mSendBoxList.size());
        mAdapter = new HcmailSendboxAdapter(HcmailSendboxActivity.this, mSendBoxList, false);
        mMailBoxList.setAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onItemClick(View view, int position, HcmailSendbox item) {
        //写邮件
        Intent intent = new Intent();
        intent.setClass(this, HcmailWriteActivity.class);
        intent.setAction(HcmailWriteActivity.ACTION_SEND_BOX);
        intent.putExtra("HcmailSendbox",item);
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
            HcmailSendboxActivity.MessagesCursorLoader.CallMessageCursor callLogsCursor = new HcmailSendboxActivity.MessagesCursorLoader.CallMessageCursor(baseCursor);
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
                mSendBoxList.clear();
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    int mHasInvite = EmailContent.Message.FLAG_INCOMING_MEETING_INVITE;
                    int mHasBeenRepliedTo = EmailContent.Message.FLAG_REPLIED_TO;
                    int mHasBeenForwarded = EmailContent.Message.FLAG_FORWARDED;
                    while (!cursor.isAfterLast()) {
                        HcmailSendbox sendbox = new HcmailSendbox();
                        long mMessageId = cursor.getLong(COLUMN_ID);
                        HcLog.D("mMessageId = " + mMessageId);
                        sendbox.setBoxId(String.valueOf(mMessageId));
                        long mMailboxId = cursor.getLong(COLUMN_MAILBOX_KEY);
                        final long accountId = cursor.getLong(COLUMN_ACCOUNT_KEY);
                        boolean isRead = (cursor.getInt(COLUMN_READ) != 0);
//                        if (isRead) {
//                            sendbox.setBoxImg("已读");
//                        } else {
//                            sendbox.setBoxImg("未读");
//                        }
                        int mIsFavorite = cursor.getInt(COLUMN_FAVORITE);
                        int flags = cursor.getInt(COLUMN_FLAGS);
                        int mHasAttachment = cursor.getInt(COLUMN_ATTACHMENTS);
                        long mDate = cursor.getLong(COLUMN_DATE);
                        String date = HcmailUtils.getDateToString(mDate);
                        sendbox.setBoxDate(date);
                        String mSender = cursor.getString(COLUMN_DISPLAY_NAME);
                        sendbox.setBoxName(mSender);
                        String mSubject = cursor.getString(COLUMN_SUBJECT);
                        sendbox.setBoxTitle(mSubject);
                        String mSnippet = cursor.getString(COLUMN_SNIPPET);
                        mSendBoxList.add(sendbox);
                        cursor.moveToNext();
                    }
                }
            }
        }
    }
}
