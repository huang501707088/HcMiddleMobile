package com.android.hcmail;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.email.R;

import java.util.List;

/**
 * Created by zhujiabin on 2017/3/14.
 */

public class HcmailInboxAdapter extends HcBaseAdapter<HcmailInbox> {
    private Boolean mCheckFlag;

    public interface OnItemClickCallback {
        void onItemClick(View view, int position, HcmailInbox item);
    }

    private static OnItemClickCallback lCallback;

    public static void setOnItemClickCallback(OnItemClickCallback callback) {
        lCallback = callback;
    }
    public interface OnClickCallback {
        void onCheckBoxClick(View view, int position, HcmailInbox item);
    }

    private static OnClickCallback mCallback;

    public static void setOnClickCallback(OnClickCallback callback) {
        mCallback = callback;
    }

    public HcmailInboxAdapter(Context context, List<HcmailInbox> infos, Boolean checkFlag) {
        super(context, infos);
        mCheckFlag = checkFlag;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final HcmailInbox item = (HcmailInbox) getItem(position);
        HcmailInboxViewHolder hcmailInboxViewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.hcmail_inbox_list_item,
                    parent, false);
            hcmailInboxViewHolder = new HcmailInboxViewHolder();
            hcmailInboxViewHolder.img = (ImageView) convertView
                    .findViewById(R.id.hcmail_box_list_img);
            hcmailInboxViewHolder.title = (TextView) convertView
                    .findViewById(R.id.hcmail_box_list_title);
            hcmailInboxViewHolder.name = (TextView) convertView
                    .findViewById(R.id.hcmail_box_list_from_name);
            hcmailInboxViewHolder.date = (TextView) convertView
                    .findViewById(R.id.hcmail_box_list_date);
            hcmailInboxViewHolder.checkbox = (CheckBox) convertView
                    .findViewById(R.id.hcmail_inbox_list_checkbox);
            hcmailInboxViewHolder.inboxContent = (LinearLayout) convertView
                    .findViewById(R.id.inbox_content);
            if (mCheckFlag) {
                hcmailInboxViewHolder.checkbox.setVisibility(View.VISIBLE);
                //设置所有的checkbox为空状态
            } else {
                hcmailInboxViewHolder.checkbox.setVisibility(View.GONE);
            }
            convertView.setTag(hcmailInboxViewHolder);
        } else {
            hcmailInboxViewHolder = (HcmailInboxViewHolder) convertView.getTag();
        }
        if(item.getmInboxImg().equals("未读")){
            hcmailInboxViewHolder.img.setImageResource(R.drawable.hcmail_inbox_closed);
        }else{
            hcmailInboxViewHolder.img.setImageResource(R.drawable.hcmail_inbox_opened);
        }

        hcmailInboxViewHolder.title.setText(item.getmInboxTitle());
        hcmailInboxViewHolder.name.setText(item.getmInboxName());
        hcmailInboxViewHolder.date.setText(item.getmInboxDate());
        final View finalConvertView = convertView;
        if (mCheckFlag) {
            hcmailInboxViewHolder.checkbox.setChecked(item.ismChecked());
            final HcmailInboxViewHolder finalHcmailInboxViewHolder = hcmailInboxViewHolder;
            hcmailInboxViewHolder.inboxContent.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (finalHcmailInboxViewHolder.checkbox.isChecked()) {
                        finalHcmailInboxViewHolder.checkbox.setChecked(false);
                    } else {
                        finalHcmailInboxViewHolder.checkbox.setChecked(true);
                    }
                    if (mCallback != null) {
                        mCallback.onCheckBoxClick(finalConvertView, position, item);
                    }

                }
            });
            hcmailInboxViewHolder.checkbox.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mCallback != null) {
                        mCallback.onCheckBoxClick(finalConvertView, position, item);
                    }
                }
            });

        }else{
            hcmailInboxViewHolder.inboxContent.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (lCallback != null) {
                        lCallback.onItemClick(finalConvertView, position, item);
                    }
                }
            });
        }
        return convertView;
    }

    public static class HcmailInboxViewHolder {
        private LinearLayout inboxContent;
        public TextView date;
        public TextView title;
        public TextView name;
        public ImageView img;
        public CheckBox checkbox;
    }
}
