package com.android.hcframe.hcmail;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.emailcommon.provider.EmailContent;
import com.android.emailcommon.provider.Mailbox;
import com.android.hcframe.HcLog;
import com.android.hcframe.email.R;

import java.util.HashSet;
import java.util.Set;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 17-3-28 10:08.
 */

public class MessagesAdapter extends CursorAdapter {

    private static final String TAG = "MessagesAdapter";

    private static final int KEY_HOLDER_VIEW = 1 << 25;

    public static final int KEY_MESSAGE_ID = KEY_HOLDER_VIEW << 1;

    public static final int KEY_MAIBOX_ID = KEY_MESSAGE_ID << 1;

    public static final int KEY_ACCOUNT_ID = KEY_MAIBOX_ID << 1;

    /**
     * Set of seleced message IDs.
     */
    private final HashSet<Long> mSelectedSet = new HashSet<Long>();

    /** 是否是编辑模式 */
    private boolean mEditable;

    /**
     * @deprecated
     * @see com.android.hcframe.hcmail.data.LoaderProvider#MESSAGE_PROJECTION
     */
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
    public static final int COLUMN_TO_LIST = 11;
    public static final int COLUMN_STATUS = 12;

    private LayoutInflater mInflater;

    private final int mMailboxType;

    public MessagesAdapter(Context context, int mailboxType) {
        super(context.getApplicationContext(), null, 0);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMailboxType = mailboxType;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
//        HcLog.D(EmailUtils.DEBUG, TAG + " #bindView start !!!!!!!!!!!!!!!!!!!");
        ViewHolder holder = (ViewHolder) view.getTag(KEY_HOLDER_VIEW);
        boolean isRead = cursor.getInt(COLUMN_READ) != 0;
        long messageId = cursor.getLong(COLUMN_ID);
        long mailboxId = cursor.getLong(COLUMN_MAILBOX_KEY);
        long accountId = cursor.getLong(COLUMN_ACCOUNT_KEY);
        String subject = cursor.getString(COLUMN_SUBJECT);
        long date = cursor.getLong(COLUMN_DATE);
        String sender = cursor.getString(COLUMN_DISPLAY_NAME);
        String toList = cursor.getString(COLUMN_TO_LIST);
        int status = cursor.getInt(COLUMN_STATUS);
        view.setTag(KEY_MAIBOX_ID, mailboxId);
        view.setTag(KEY_MESSAGE_ID, messageId);
        view.setTag(KEY_ACCOUNT_ID, accountId);
        if (mMailboxType == Mailbox.TYPE_INBOX || mMailboxType == Mailbox.TYPE_TRASH) {
            if (isRead) {
                holder.mSrc.setImageResource(R.drawable.hcmail_inbox_opened);
            } else {
                holder.mSrc.setImageResource(R.drawable.hcmail_inbox_closed);
            }
        } else if (mMailboxType == Mailbox.TYPE_OUTBOX) {
            switch (status) {
                case 2:
                    holder.mSrc.setImageResource(R.drawable.hcmail_inbox_exception);
                    holder.mSender.setText("发送失败...");
                    break;

                default:
                    holder.mSrc.setImageResource(R.drawable.hcmail_inbox_opened);
                    holder.mSender.setText("发送中");
                    break;
            }
        }

        holder.mTitle.setText(TextUtils.isEmpty(subject) ? "无主题" : subject);

        holder.mDate.setText(DateUtils.getRelativeTimeSpanString(context, date).toString());

        if (mEditable) {
            holder.mBox.setVisibility(View.VISIBLE);
//            HcLog.D(EmailUtils.DEBUG, TAG + " #bindView messageId = "+messageId + " mSelectedSet = "+mSelectedSet.toString());
            if (mSelectedSet.contains(messageId)) {
                holder.mBox.setChecked(true);
            } else {
                holder.mBox.setChecked(false);
            }
        } else {
            holder.mBox.setVisibility(View.GONE);
        }

        if (mMailboxType == Mailbox.TYPE_OUTBOX) return;

        if (mMailboxType == Mailbox.TYPE_DRAFTS || mMailboxType == Mailbox.TYPE_SENT) {
            holder.mSender.setText(TextUtils.isEmpty(toList) ? "未填写" : "收件人: " + toList);
        } else {
            holder.mSender.setText(TextUtils.isEmpty(sender) ? "未填写" : "发件人: " + sender);
        }

//        HcLog.D(EmailUtils.DEBUG, TAG + " #bindView end !!!!!!!!!!!!!!!!!!! messageId = "+messageId);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        View contentView = mInflater.inflate(R.layout.email_message_list_item, parent, false);
        holder.mSrc = (ImageView) contentView.findViewById(R.id.email_message_item_src);
        holder.mTitle = (TextView) contentView.findViewById(R.id.email_message_item_title);
        holder.mSender = (TextView) contentView.findViewById(R.id.email_message_item_send);
        holder.mDate = (TextView) contentView.findViewById(R.id.email_message_item_date);
        holder.mBox = (CheckBox) contentView.findViewById(R.id.email_message_item_checkbox);
        contentView.setTag(KEY_HOLDER_VIEW, holder);
//        HcLog.D(EmailUtils.DEBUG, TAG + " #newView contentView = "+contentView);
        return contentView;
    }

    private static class ViewHolder {
        ImageView mSrc;
        TextView mTitle;
        TextView mSender;
        TextView mDate;
        CheckBox mBox;
    }

    public Set<Long> getSelectedSet() {
        return mSelectedSet;
    }

    public void clear() {
        mSelectedSet.clear();
    }

    public void setEditable(boolean editable) {
        mEditable = editable;
        notifyDataSetChanged();
    }

    public boolean getEditable() {
        return mEditable;
    }

    public void updateSelected(long messageId) {
        if (mSelectedSet.contains(messageId)) {
            mSelectedSet.remove(messageId);
        } else {
            mSelectedSet.add(messageId);
        }
        notifyDataSetChanged();
    }

    public boolean allSelected() {
        return mSelectedSet.size() >= getCount();
    }

    public int getSelectedSize() {
        return mSelectedSet.size();
    }

    public void updateAllSelected() {
        if (allSelected()) {
            mSelectedSet.clear();
        } else {
            Cursor cursor = getCursor();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
            	mSelectedSet.add(cursor.getLong(COLUMN_ID));
                cursor.moveToNext();
            }
        }
        notifyDataSetChanged();
    }

    public long[] getAllMessageIds() {
        long[] messageIds = new long[getCount()];
        Cursor cursor = getCursor();
        cursor.moveToFirst();
        int count = -1;
        while (!cursor.isAfterLast()) {
            messageIds[++count] = cursor.getLong(COLUMN_ID);
            cursor.moveToNext();
        }
        return messageIds;
    }
}
