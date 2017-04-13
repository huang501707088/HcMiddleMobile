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

public class HcmailSendboxAdapter extends HcBaseAdapter<HcmailSendbox> {
    private Boolean mCheckFlag;

    public interface OnClickCallback {
        void onCheckBoxClick(View view, int position, HcmailSendbox item);
    }

    private static OnClickCallback mCallback;

    public static void setOnClickCallback(OnClickCallback callback) {
        mCallback = callback;
    }

    public interface OnItemCallback {
        void onItemClick(View view, int position, HcmailSendbox item);
    }

    private static OnItemCallback cCallback;

    public static void setOnItemCallback(OnItemCallback callback) {
        cCallback = callback;
    }

    public HcmailSendboxAdapter(Context context, List<HcmailSendbox> infos, Boolean checkFlag) {
        super(context, infos);
        mCheckFlag = checkFlag;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final HcmailSendbox item = (HcmailSendbox) getItem(position);
        HcmailSendboxViewHolder hcmailSendboxViewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.hcmail_sendbox_list_item,
                    parent, false);
            hcmailSendboxViewHolder = new HcmailSendboxViewHolder();
            hcmailSendboxViewHolder.img = (ImageView) convertView
                    .findViewById(R.id.hcmail_box_list_img);
            hcmailSendboxViewHolder.title = (TextView) convertView
                    .findViewById(R.id.hcmail_box_list_title);
            hcmailSendboxViewHolder.name = (TextView) convertView
                    .findViewById(R.id.hcmail_box_list_from_name);
            hcmailSendboxViewHolder.date = (TextView) convertView
                    .findViewById(R.id.hcmail_box_list_date);
            hcmailSendboxViewHolder.checkbox = (CheckBox) convertView
                    .findViewById(R.id.hcmail_sendbox_list_checkbox);
            hcmailSendboxViewHolder.sendboxContent = (LinearLayout) convertView
                    .findViewById(R.id.sendbox_content);
            if (mCheckFlag) {
                hcmailSendboxViewHolder.checkbox.setVisibility(View.VISIBLE);
                //设置所有的checkbox为空状态
            } else {
                hcmailSendboxViewHolder.checkbox.setVisibility(View.GONE);
            }
            convertView.setTag(hcmailSendboxViewHolder);
        } else {
            hcmailSendboxViewHolder = (HcmailSendboxViewHolder) convertView.getTag();
        }
        if ("发送失败".equals(item.getBoxImg())) {
            hcmailSendboxViewHolder.img.setImageResource(R.drawable.hcmail_inbox_opened);
        } else if ("发送中...".equals(item.getBoxImg())) {
            hcmailSendboxViewHolder.img.setImageResource(R.drawable.hcmail_inbox_exception);
        }
        hcmailSendboxViewHolder.title.setText(item.getBoxTitle());
        hcmailSendboxViewHolder.name.setText(item.getBoxName());
        hcmailSendboxViewHolder.date.setText(item.getBoxDate());
        final View finalConvertView = convertView;
        if (mCheckFlag) {
            hcmailSendboxViewHolder.checkbox.setChecked(item.isChecked());
            final HcmailSendboxViewHolder finalHcmailSendboxViewHolder = hcmailSendboxViewHolder;
            hcmailSendboxViewHolder.sendboxContent.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (finalHcmailSendboxViewHolder.checkbox.isChecked()) {
                        finalHcmailSendboxViewHolder.checkbox.setChecked(false);
                    } else {
                        finalHcmailSendboxViewHolder.checkbox.setChecked(true);
                    }
                    if (mCallback != null) {
                        mCallback.onCheckBoxClick(finalConvertView, position, item);
                    }

                }
            });
            hcmailSendboxViewHolder.checkbox.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mCallback != null) {
                        mCallback.onCheckBoxClick(finalConvertView, position, item);
                    }
                }
            });
        } else {
            hcmailSendboxViewHolder.sendboxContent.setOnClickListener(new View.OnClickListener() {
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

    public static class HcmailSendboxViewHolder {
        private LinearLayout sendboxContent;
        public TextView date;
        public TextView title;
        public TextView name;
        public ImageView img;
        public CheckBox checkbox;
    }
}
