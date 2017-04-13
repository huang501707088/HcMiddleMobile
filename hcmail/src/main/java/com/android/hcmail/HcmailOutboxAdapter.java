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

public class HcmailOutboxAdapter extends HcBaseAdapter<HcmailOutbox> {
    private Boolean mCheckFlag;

    public interface OnClickCallback {
        void onCheckBoxClick(View view, int position, HcmailOutbox item);
    }

    private static OnClickCallback mCallback;

    public static void setOnClickCallback(OnClickCallback callback) {
        mCallback = callback;
    }

    public interface OnItemCallback {
        void onItemClick(View view, int position, HcmailOutbox item);
    }

    private static OnItemCallback cCallback;

    public static void setOnItemCallback(OnItemCallback callback) {
        cCallback = callback;
    }

    public HcmailOutboxAdapter(Context context, List<HcmailOutbox> infos, Boolean checkFlag) {
        super(context, infos);
        mCheckFlag = checkFlag;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final HcmailOutbox item = (HcmailOutbox) getItem(position);
        HcmailOutboxViewHolder hcmailOutboxViewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.hcmail_outbox_list_item,
                    parent, false);
            hcmailOutboxViewHolder = new HcmailOutboxViewHolder();
            hcmailOutboxViewHolder.img = (ImageView) convertView
                    .findViewById(R.id.hcmail_box_list_img);
            hcmailOutboxViewHolder.title = (TextView) convertView
                    .findViewById(R.id.hcmail_box_list_title);
            hcmailOutboxViewHolder.name = (TextView) convertView
                    .findViewById(R.id.hcmail_box_list_from_name);
            hcmailOutboxViewHolder.date = (TextView) convertView
                    .findViewById(R.id.hcmail_box_list_date);
            hcmailOutboxViewHolder.checkbox = (CheckBox) convertView
                    .findViewById(R.id.hcmail_outbox_list_checkbox);
            hcmailOutboxViewHolder.outboxContent = (LinearLayout) convertView
                    .findViewById(R.id.outbox_content);
            if (mCheckFlag) {
                hcmailOutboxViewHolder.checkbox.setVisibility(View.VISIBLE);
                //设置所有的checkbox为空状态
            } else {
                hcmailOutboxViewHolder.checkbox.setVisibility(View.GONE);
            }
            convertView.setTag(hcmailOutboxViewHolder);
        } else {
            hcmailOutboxViewHolder = (HcmailOutboxViewHolder) convertView.getTag();
        }
        hcmailOutboxViewHolder.img.setImageResource(R.drawable.hcmail_inbox_opened);
        hcmailOutboxViewHolder.title.setText(item.getBoxTitle());
        hcmailOutboxViewHolder.name.setText(item.getBoxName());
        hcmailOutboxViewHolder.date.setText(item.getBoxDate());
        final View finalConvertView = convertView;
        if (mCheckFlag) {
            hcmailOutboxViewHolder.checkbox.setChecked(item.isChecked());
            final HcmailOutboxViewHolder finalHcmailOutboxViewHolder = hcmailOutboxViewHolder;
            hcmailOutboxViewHolder.outboxContent.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (finalHcmailOutboxViewHolder.checkbox.isChecked()) {
                        finalHcmailOutboxViewHolder.checkbox.setChecked(false);
                    } else {
                        finalHcmailOutboxViewHolder.checkbox.setChecked(true);
                    }
                    if (mCallback != null) {
                        mCallback.onCheckBoxClick(finalConvertView, position, item);
                    }

                }
            });
            hcmailOutboxViewHolder.checkbox.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mCallback != null) {
                        mCallback.onCheckBoxClick(finalConvertView, position, item);
                    }
                }
            });
        } else {
            hcmailOutboxViewHolder.outboxContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //回调接口，从新装一次
                    if (cCallback != null) {
                        cCallback.onItemClick(finalConvertView, position, item);
                    }
                }
            });
        }
        return convertView;
    }

    public static class HcmailOutboxViewHolder {
        private LinearLayout outboxContent;
        public TextView date;
        public TextView title;
        public TextView name;
        public ImageView img;
        public CheckBox checkbox;
    }
}
