package com.android.hcmail;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.email.Controller;
import com.android.email.MessageListContext;
import com.android.email.data.ThrottlingCursorLoader;
import com.android.emailcommon.provider.Account;
import com.android.emailcommon.provider.EmailContent;
import com.android.emailcommon.provider.Mailbox;
import com.android.emailcommon.utility.Utility;
import com.android.hcframe.CommonActivity;
import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.email.R;
import com.android.hcframe.http.HttpRequestQueue;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.pull.PullToRefreshListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zhujiabin on 2017/3/17.
 */

public class HcmailDeleteboxActivity extends HcBaseActivity implements View.OnClickListener, HcmailDeleteboxAdapter.OnClickCallback, HcmailDeleteboxAdapter.OnChangeCallback, LoaderManager.LoaderCallbacks<Cursor> {

    private TopBarView mDeleteboxTopBar;
    private PullToRefreshListView mDeleteboxList;
    private HcmailDeleteboxAdapter mAdapter;
    private LinearLayout mDeleteboxTop;
    private LinearLayout mText;
    private LinearLayout mDeleImg;
    private LinearLayout mMoveImg;
    private LinearLayout mDeleteAll;
    private TextView mCancel;
    private TextView mCenter;
    private TextView mAll;
    private static List<HcmailDeletebox> mMailBoxList;
    private List<HcmailDeletebox> mDeleteSelectList;
    private int checkNum = 0;
    HcmailDeleteboxAdapter.HcmailDeleteboxViewHolder mDeleteboxViewHolder;
    private HcmailDeletebox mHcmailDeletebox;

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

    /**
     * 自定义的底部弹出框类
     */
    private DeleteSelectPicPopupWindow menuWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hcmail_deletebox_list);
        initView();
        initData();
        initEvent();
    }

    private void initView() {
        mDeleteboxTopBar = (TopBarView) findViewById(R.id.hcmail_deletebox_top_bar);
        mDeleteboxList = (PullToRefreshListView) findViewById(R.id.hcmail_deletebox_list);
        mDeleteboxTop = (LinearLayout) findViewById(R.id.hcmail_deletebox_top);
        mDeleteboxList = (PullToRefreshListView) findViewById(R.id.hcmail_deletebox_list);
        mDeleImg = (LinearLayout) findViewById(R.id.hcmail_deletebox_dele_img_linear);
        mMoveImg = (LinearLayout) findViewById(R.id.hcmail_deletebox_move_img_linear);
        mCancel = (TextView) findViewById(R.id.hcmail_deletebox_cancel);
        mCenter = (TextView) findViewById(R.id.hcmail_deletebox_center);
        mAll = (TextView) findViewById(R.id.hcmail_deletebox_all);
        mText = (LinearLayout) findViewById(R.id.hcmail_deletebox_text_footer);
        mDeleteAll = (LinearLayout) findViewById(R.id.hcmail_delete_box_footer);

    }

    private void initData() {
        mMailBoxList = new ArrayList<>();
        mDeleteSelectList = new ArrayList<>();
        mDeleteboxTopBar.setTitle("已删除");
//        init();
        mController = Controller.getInstance(this);
        //初始化数据
        final Intent intent = getIntent();
        viewContext = MessageListContext.forIntent(this, intent);
        //此处每次返回都会开启***
        getLoaderManager().initLoader(0, null, this);
    }

    private void init() {
        HcmailDeletebox deletebox = new HcmailDeletebox();
        deletebox.setBoxImg("");
        deletebox.setBoxId("1");
        deletebox.setBoxTitle("朱氏邮件标题");
        deletebox.setBoxName("朱家斌");
        deletebox.setBoxDate("2017年3月16日");
        mMailBoxList.add(deletebox);
        HcmailDeletebox deletebox1 = new HcmailDeletebox();
        deletebox1.setBoxImg("");
        deletebox.setBoxId("2");
        deletebox1.setBoxTitle("牛氏邮件标题");
        deletebox1.setBoxName("牛家斌");
        deletebox1.setBoxDate("2017年3月6日");
        mMailBoxList.add(deletebox1);
        HcmailDeletebox deletebox2 = new HcmailDeletebox();
        deletebox2.setBoxImg("");
        deletebox2.setBoxId("2");
        deletebox2.setBoxTitle("牛氏邮件标题");
        deletebox2.setBoxName("牛家斌");
        deletebox2.setBoxDate("2017年2月28日");
        mMailBoxList.add(deletebox2);
        HcmailDeletebox deletebox3 = new HcmailDeletebox();
        deletebox3.setBoxImg("");
        deletebox3.setBoxId("3");
        deletebox3.setBoxTitle("天氏邮件标题");
        deletebox3.setBoxName("天家斌");
        deletebox3.setBoxDate("2017年3月10日");
        mMailBoxList.add(deletebox3);
        HcmailDeletebox deletebox4 = new HcmailDeletebox();
        deletebox4.setBoxImg("");
        deletebox4.setBoxId("4");
        deletebox4.setBoxTitle("地氏邮件标题");
        deletebox4.setBoxName("地家斌");
        deletebox4.setBoxDate("2017年3月9日");
        mMailBoxList.add(deletebox4);
        mAdapter = new HcmailDeleteboxAdapter(this, mMailBoxList, false);
        mDeleteboxList.setAdapter(mAdapter);
    }

    private void initEvent() {
        mCancel.setOnClickListener(this);
        mAll.setOnClickListener(this);
        mText.setOnClickListener(this);
        mDeleImg.setOnClickListener(this);
        mDeleteboxList.setOnClickListener(this);
        mDeleteAll.setOnClickListener(this);
        mMoveImg.setOnClickListener(this);
        HcmailDeleteboxAdapter.setOnClickCallback(this);
        HcmailDeleteboxAdapter.setOnChangeCallback(this);
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
        if (i == R.id.hcmail_deletebox_cancel) {
            mDeleteAll.setClickable(true);
            mText.setClickable(false);
            mDeleteboxTopBar.setVisibility(View.VISIBLE);
            mDeleteboxTop.setVisibility(View.GONE);
            mText.setVisibility(View.GONE);
            mDeleteAll.setVisibility(View.VISIBLE);
            for (int j = 0; j < mMailBoxList.size(); j++) {
                mMailBoxList.get(j).setChecked(false);
            }
            checkNum = 0;
            mAdapter = new HcmailDeleteboxAdapter(HcmailDeleteboxActivity.this, mMailBoxList, false);
            mDeleteboxList.setAdapter(mAdapter);
        } else if (i == R.id.hcmail_deletebox_all) {
            if (mAll.getText().equals("全选")) {
                checkNum = 0;
                mAll.setText("全不选");
                for (int j = 0; j < mMailBoxList.size(); j++) {
                    mMailBoxList.get(j).setChecked(true);
                    checkNum++;
                }
                mDeleteSelectList.addAll(mMailBoxList);
                dataChanged();
            } else if (mAll.getText().equals("全不选")) {
                mAll.setText("全选");
                checkNum = 0;
                for (int j = 0; j < mMailBoxList.size(); j++) {
                    mMailBoxList.get(j).setChecked(false);
                }
                mDeleteSelectList.clear();
                dataChanged();
            }
        } else if (i == R.id.hcmail_delete_box_footer) {
            //清空邮件
            final AlertDialog dialog = new AlertDialog.Builder(this)
                    .create();
            dialog.setCancelable(false);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View mDialogView = inflater.inflate(R.layout.hcmail_delete_box_dialog, null);
            dialog.setView(mDialogView);
            dialog.show();
            dialog.getWindow().setContentView(R.layout.hcmail_delete_box_dialog);
            Button unagree_dialog = (Button) dialog.getWindow()
                    .findViewById(R.id.unagree_dialog);
            Button agree_dialog = (Button) dialog.getWindow().findViewById(
                    R.id.agree_dialog);
            unagree_dialog.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            agree_dialog.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    //点击全部删除
                    final Set set = new HashSet();
                    for (int j = 0; j < mMailBoxList.size(); j++) {
                        set.add(Long.valueOf(mMailBoxList.get(j).getBoxId()));
                        HcLog.D("mDeleteSelectList.get(j).getmInboxId()= " + mMailBoxList.get(j).getBoxId());
                    }
                    if (set.size() > 0) {
                        deleteMessages(set);
                    }
                }
            });
        } else if (i == R.id.hcmail_deletebox_dele_img_linear) {
            //点击删除按钮
            HcLog.D("mDeleteSelectList.size() = " + mDeleteSelectList.size());
            final Set set = new HashSet();
            for (int j = 0; j < mDeleteSelectList.size(); j++) {
                set.add(Long.valueOf(mDeleteSelectList.get(j).getBoxId()));
                HcLog.D("mDeleteSelectList.get(j).getmInboxId()= " + mDeleteSelectList.get(j).getBoxId());
            }
            deleteMessages(set);
        } else if (i == R.id.hcmail_deletebox_move_img_linear) {
            //点击移动到按钮
            menuWindow = new DeleteSelectPicPopupWindow(this, itemsOnClick);
            //显示窗口
            menuWindow.showAtLocation(this.findViewById(R.id.delete_box_main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
        }
    }

    //为弹出窗口实现监听类
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {

        public void onClick(View v) {
            menuWindow.dismiss();
            int i = v.getId();
            if (i == R.id.hcmail_send_box) {
                //转移到发件箱
            } else if (i == R.id.hcmail_receive_box) {
                //转移到收件箱
            } else if (i == R.id.hcmail_draft_box) {
                //转移到草稿箱
            }
        }
    };

    // 刷新listview和TextView的显示
    private void dataChanged() {
        // 通知listView刷新
        mAdapter.notifyDataSetChanged();
        // TextView显示最新的选中数目
        mCenter.setText("已删除(" + checkNum + ")");
    }

    @Override
    public void onCheckBoxClick(View view, int position, HcmailDeletebox item) {
        mDeleteboxViewHolder = (HcmailDeleteboxAdapter.HcmailDeleteboxViewHolder) view.getTag();
        mHcmailDeletebox = mAdapter.getItem(position);

        // 调整选定条目
        if (mDeleteboxViewHolder.checkbox.isChecked() == true) {
            checkNum++;
            mDeleteSelectList.add(mHcmailDeletebox);
            if (checkNum == mMailBoxList.size()) {
                mCenter.setText("全不选");
                mDeleteSelectList.addAll(mMailBoxList);
            } else if (checkNum > 0 && checkNum < mMailBoxList.size()) {
                mCenter.setText("全选");
            }
            mCenter.setText("已删除(" + checkNum + ")");
        } else if (mDeleteboxViewHolder.checkbox.isChecked() == false) {
            checkNum--;
            mDeleteSelectList.remove(mHcmailDeletebox);
            mCenter.setText("已删除(" + checkNum + ")");
        }
    }

    @Override
    public void onChangeBoxClick(View view, int position, HcmailDeletebox item) {
        mDeleteAll.setClickable(false);
        mText.setClickable(true);
        mDeleteboxTopBar.setVisibility(View.GONE);
        mDeleteboxTop.setVisibility(View.VISIBLE);
        mText.setVisibility(View.VISIBLE);
        mDeleteAll.setVisibility(View.GONE);
        mHcmailDeletebox = mAdapter.getItem(position);
        mHcmailDeletebox.setChecked(true);
        mDeleteSelectList.add(mHcmailDeletebox);
        mAdapter = new HcmailDeleteboxAdapter(HcmailDeleteboxActivity.this, mMailBoxList, true);
        mDeleteboxList.setAdapter(mAdapter);
        checkNum++;
        mCenter.setText("已删除(" + checkNum + ")");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mController.updateMailbox(viewContext.mAccountId, viewContext.getMailboxId(), false);
        return new MessagesCursorLoader(HcmailDeleteboxActivity.this, viewContext);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        classifyDeletemMailDraftsList();
    }

    private void classifyDeletemMailDraftsList() {
        mAdapter = new HcmailDeleteboxAdapter(HcmailDeleteboxActivity.this, mMailBoxList, false);
        mDeleteboxList.setAdapter(mAdapter);
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
                mMailBoxList.clear();
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    int mHasInvite = EmailContent.Message.FLAG_INCOMING_MEETING_INVITE;
                    int mHasBeenRepliedTo = EmailContent.Message.FLAG_REPLIED_TO;
                    int mHasBeenForwarded = EmailContent.Message.FLAG_FORWARDED;
                    while (!cursor.isAfterLast()) {
                        HcmailDeletebox delete = new HcmailDeletebox();
                        long mMessageId = cursor.getLong(COLUMN_ID);
                        HcLog.D("mMessageId = " + mMessageId);
                        delete.setBoxId(String.valueOf(mMessageId));
                        long mMailboxId = cursor.getLong(COLUMN_MAILBOX_KEY);
                        final long accountId = cursor.getLong(COLUMN_ACCOUNT_KEY);
                        boolean isRead = (cursor.getInt(COLUMN_READ) != 0);
                        if (isRead) {
                            delete.setBoxImg("已读");
                        } else {
                            delete.setBoxImg("未读");
                        }
                        int mIsFavorite = cursor.getInt(COLUMN_FAVORITE);
                        int flags = cursor.getInt(COLUMN_FLAGS);
                        int mHasAttachment = cursor.getInt(COLUMN_ATTACHMENTS);
                        long mDate = cursor.getLong(COLUMN_DATE);
                        String date = HcmailUtils.getDateToString(mDate);
                        delete.setBoxDate(date);
                        String mSender = cursor.getString(COLUMN_DISPLAY_NAME);
                        delete.setBoxName(mSender);
                        String mSubject = cursor.getString(COLUMN_SUBJECT);
                        delete.setBoxTitle(mSubject);
                        String mSnippet = cursor.getString(COLUMN_SNIPPET);
                        mMailBoxList.add(delete);
                        cursor.moveToNext();
                    }
                }
            }
        }
    }

    private void deleteMessages(Set<Long> selectedSet) {
        mDeleteboxTopBar.setVisibility(View.VISIBLE);
        mDeleteboxTop.setVisibility(View.GONE);
        mText.setVisibility(View.GONE);
        mDeleteAll.setVisibility(View.VISIBLE);
        mDeleteAll.setClickable(true);
        mText.setClickable(false);
        checkNum = 0;
        mCenter.setText("收件箱(" + checkNum + ")");
        final long[] messageIds = Utility.toPrimitiveLongArray(selectedSet);
        mController.deleteMessages(messageIds);
        mMailBoxList.removeAll(mDeleteSelectList);
        mDeleteSelectList.clear();
        mAdapter.notifyDataSetChanged();
        HcUtil.showToast(this, R.string.hcmail_delete);
        selectedSet.clear();
    }
}
