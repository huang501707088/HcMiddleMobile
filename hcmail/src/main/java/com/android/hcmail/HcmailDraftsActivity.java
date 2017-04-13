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

public class HcmailDraftsActivity extends HcBaseActivity implements View.OnClickListener, HcmailDraftsAdapter.OnClickCallback, HcmailDraftsAdapter.OnItemClickCallback, LoaderManager.LoaderCallbacks<Cursor> {

    private TopBarView BoxTopBar;
    private LinearLayout mDraftsTop;
    private PullToRefreshListView mDraftsList;
    private HcmailDraftsAdapter mAdapter;
    private LinearLayout BoxTop;
    private LinearLayout mText;
    private LinearLayout mWrite;
    private TextView mCancel;
    private TextView mCenter;
    private TextView mAll;
    private static List<HcmailDrafts> mMailDraftsList;
    private List<HcmailDrafts> mBoxSelectList;
    private int checkNum = 0;
    HcmailDraftsAdapter.HcmailDraftsViewHolder BoxViewHolder;
    private HcmailDrafts mHcmailDrafts;

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
        setContentView(R.layout.hcmail_drafts_list);
        initView();
        initData();
        initEvent();
    }

    private void initView() {
        BoxTopBar = (TopBarView) findViewById(R.id.hcmail_drafts_top_bar);
        mDraftsList = (PullToRefreshListView) findViewById(R.id.hcmail_drafts_list);
        BoxTop = (LinearLayout) findViewById(R.id.hcmail_drafts_top);
        mCancel = (TextView) findViewById(R.id.hcmail_drafts_cancel);
        mCenter = (TextView) findViewById(R.id.hcmail_drafts_center);
        mAll = (TextView) findViewById(R.id.hcmail_drafts_all);
        mWrite = (LinearLayout) findViewById(R.id.hcmail_drafts_write_footer);
        mText = (LinearLayout) findViewById(R.id.hcmail_drafts_text_footer);
    }

    private void initData() {
        mMailDraftsList = new ArrayList<>();
        mBoxSelectList = new ArrayList<>();
        BoxTopBar.setTitle("草稿箱");
        BoxTopBar.setMenuBtnVisiable(View.VISIBLE);
        BoxTopBar.setMenuSrc(R.drawable.hcmail_transpate_delete);
        BoxTopBar.setMenuListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //点击删除按钮
                BoxTopBar.setVisibility(View.GONE);
                BoxTop.setVisibility(View.VISIBLE);
                mWrite.setVisibility(View.GONE);
                mText.setVisibility(View.VISIBLE);
                mWrite.setClickable(false);
                mText.setClickable(true);
                mAdapter = new HcmailDraftsAdapter(HcmailDraftsActivity.this, mMailDraftsList, true);
                mDraftsList.setAdapter(mAdapter);
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
        HcmailDrafts drafts = new HcmailDrafts();
        drafts.setBoxImg("");
        drafts.setBoxId("1");
        drafts.setBoxTitle("朱氏邮件标题");
        drafts.setBoxName("朱家斌");
        drafts.setBoxDate("2017年3月16日");
        mMailDraftsList.add(drafts);
        HcmailDrafts drafts1 = new HcmailDrafts();
        drafts1.setBoxImg("");
        drafts1.setBoxId("2");
        drafts1.setBoxTitle("牛氏邮件标题");
        drafts1.setBoxName("牛家斌");
        drafts1.setBoxDate("2017年3月6日");
        mMailDraftsList.add(drafts1);
        HcmailDrafts drafts2 = new HcmailDrafts();
        drafts2.setBoxImg("");
        drafts2.setBoxId("2");
        drafts2.setBoxTitle("牛氏邮件标题");
        drafts2.setBoxName("牛家斌");
        drafts2.setBoxDate("2017年2月28日");
        mMailDraftsList.add(drafts2);
        HcmailDrafts drafts3 = new HcmailDrafts();
        drafts3.setBoxImg("");
        drafts3.setBoxId("3");
        drafts3.setBoxTitle("天氏邮件标题");
        drafts3.setBoxName("天家斌");
        drafts3.setBoxDate("2017年3月10日");
        mMailDraftsList.add(drafts3);
        HcmailDrafts drafts4 = new HcmailDrafts();
        drafts4.setBoxImg("");
        drafts4.setBoxId("4");
        drafts4.setBoxTitle("地氏邮件标题");
        drafts4.setBoxName("地家斌");
        drafts4.setBoxDate("2017年3月9日");
        mMailDraftsList.add(drafts4);
        mAdapter = new HcmailDraftsAdapter(this, mMailDraftsList, false);
        mDraftsList.setAdapter(mAdapter);
    }

    private void initEvent() {
        mCancel.setOnClickListener(this);
        mAll.setOnClickListener(this);
        mWrite.setOnClickListener(this);
        mText.setOnClickListener(this);
        HcmailDraftsAdapter.setOnClickCallback(this);
        HcmailDraftsAdapter.setItemClickCallback(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.hcmail_drafts_cancel) {
            BoxTopBar.setVisibility(View.VISIBLE);
            BoxTop.setVisibility(View.GONE);
            mText.setVisibility(View.GONE);
            mWrite.setVisibility(View.VISIBLE);
            mWrite.setClickable(true);
            mText.setClickable(false);
            checkNum = 0;
            mAdapter = new HcmailDraftsAdapter(HcmailDraftsActivity.this, mMailDraftsList, false);
            mDraftsList.setAdapter(mAdapter);
        } else if (i == R.id.hcmail_drafts_all) {
            if (mAll.getText().equals("全选")) {
                checkNum = 0;
                mAll.setText("全不选");
                for (int j = 0; j < mMailDraftsList.size(); j++) {
                    mMailDraftsList.get(j).setChecked(true);
                    checkNum++;
                }
                mBoxSelectList.addAll(mMailDraftsList);
                dataChanged();
            } else if (mAll.getText().equals("全不选")) {
                mAll.setText("全选");
                checkNum = 0;
                for (int j = 0; j < mMailDraftsList.size(); j++) {
                    mMailDraftsList.get(j).setChecked(false);
                }
                mBoxSelectList.clear();
            }
            dataChanged();
        } else if (i == R.id.hcmail_drafts_text_footer) {
            //删除邮件
            HcLog.D(" mBoxSelectList.size() = " + mBoxSelectList.size());
            final Set set = new HashSet();
            for (int j = 0; j < mBoxSelectList.size(); j++) {
                set.add(Long.valueOf(mBoxSelectList.get(j).getBoxId()));
                HcLog.D("mBoxSelectList.get(j).getmInboxId()= " + mBoxSelectList.get(j).getBoxId());
            }
            deleteMessages(set);
        }else if(i == R.id.hcmail_drafts_write_footer){
            //写邮件
            Intent intent = new Intent();
            intent.setClass(this, HcmailWriteActivity.class);
            startActivity(intent);
        }
    }

    private void deleteMessages(Set<Long> selectedSet) {
        BoxTopBar.setVisibility(View.VISIBLE);
        BoxTop.setVisibility(View.GONE);
        mText.setVisibility(View.GONE);
        mWrite.setVisibility(View.VISIBLE);
        mWrite.setClickable(true);
        mText.setClickable(false);
        checkNum = 0;
        mCenter.setText("收件箱(" + checkNum + ")");
        final long[] messageIds = Utility.toPrimitiveLongArray(selectedSet);
        mController.deleteMessages(messageIds);
        mMailDraftsList.removeAll(mBoxSelectList);
        mBoxSelectList.clear();
        mAdapter.notifyDataSetChanged();
        HcUtil.showToast(this, R.string.hcmail_delete);
        selectedSet.clear();
    }

    // 刷新listview和TextView的显示
    private void dataChanged() {
        // 通知listView刷新
        mAdapter.notifyDataSetChanged();
        // TextView显示最新的选中数目
        mCenter.setText("草稿箱(" + checkNum + ")");
    }

    @Override
    public void onCheckBoxClick(View view, int position, HcmailDrafts item) {
        mWrite.setClickable(false);
        mText.setClickable(true);
        BoxViewHolder = (HcmailDraftsAdapter.HcmailDraftsViewHolder) view.getTag();
        mHcmailDrafts = mAdapter.getItem(position);

        // 调整选定条目
        if (BoxViewHolder.checkbox.isChecked() == true) {
            checkNum++;
            mBoxSelectList.add(mHcmailDrafts);
            if (checkNum == mMailDraftsList.size()) {
                mCenter.setText("全不选");
                mBoxSelectList.addAll(mMailDraftsList);
            } else if (checkNum > 0 && checkNum < mMailDraftsList.size()) {
                mCenter.setText("全选");
            }
            mCenter.setText("草稿箱(" + checkNum + ")");
        } else if (BoxViewHolder.checkbox.isChecked() == false) {
            checkNum--;
            mBoxSelectList.remove(mHcmailDrafts);
            mCenter.setText("草稿箱(" + checkNum + ")");
        }
    }

    @Override
    public void onItemClick(View view, int position, HcmailDrafts item) {
        //跳转到写邮件页面
        Intent intent = new Intent();
        intent.setClass(this, HcmailWriteActivity.class);
        intent.setAction(HcmailWriteActivity.ACTION_DRAFTS_BOX);
        intent.putExtra("HcmailDraftsbox",item);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mController.updateMailbox(viewContext.mAccountId, viewContext.getMailboxId(), false);
        return new MessagesCursorLoader(HcmailDraftsActivity.this, viewContext);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        classifyDraftsmMailDraftsList();
    }

    private void classifyDraftsmMailDraftsList() {
        mAdapter = new HcmailDraftsAdapter(HcmailDraftsActivity.this, mMailDraftsList, false);
        mDraftsList.setAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

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
            MessagesCursorLoader.CallMessageCursor callLogsCursor = new MessagesCursorLoader.CallMessageCursor(baseCursor);
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
                mMailDraftsList.clear();
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    int mHasInvite = EmailContent.Message.FLAG_INCOMING_MEETING_INVITE;
                    int mHasBeenRepliedTo = EmailContent.Message.FLAG_REPLIED_TO;
                    int mHasBeenForwarded = EmailContent.Message.FLAG_FORWARDED;
                    while (!cursor.isAfterLast()) {
                        HcmailDrafts drafts = new HcmailDrafts();
                        long mMessageId = cursor.getLong(COLUMN_ID);
                        HcLog.D("mMessageId = " + mMessageId);
                        drafts.setBoxId(String.valueOf(mMessageId));
                        long mMailboxId = cursor.getLong(COLUMN_MAILBOX_KEY);
                        final long accountId = cursor.getLong(COLUMN_ACCOUNT_KEY);
                        boolean isRead = (cursor.getInt(COLUMN_READ) != 0);
                        if (isRead) {
                            drafts.setBoxImg("已读");
                        } else {
                            drafts.setBoxImg("未读");
                        }
                        int mIsFavorite = cursor.getInt(COLUMN_FAVORITE);
                        int flags = cursor.getInt(COLUMN_FLAGS);
                        int mHasAttachment = cursor.getInt(COLUMN_ATTACHMENTS);
                        long mDate = cursor.getLong(COLUMN_DATE);
                        String date = HcmailUtils.getDateToString(mDate);
                        drafts.setBoxDate(date);
                        String mSender = cursor.getString(COLUMN_DISPLAY_NAME);
                        drafts.setBoxName(mSender);
                        String mSubject = cursor.getString(COLUMN_SUBJECT);
                        drafts.setBoxTitle(mSubject);
                        String mSnippet = cursor.getString(COLUMN_SNIPPET);
                        mMailDraftsList.add(drafts);
                        cursor.moveToNext();
                    }
                }
            }
        }
    }
}
