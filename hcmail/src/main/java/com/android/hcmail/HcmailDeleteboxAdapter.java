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

public class HcmailDeleteboxAdapter extends HcBaseAdapter<HcmailDeletebox> {
    private Boolean mCheckFlag;

    public interface OnClickCallback {
        void onCheckBoxClick(View view, int position, HcmailDeletebox item);
    }

    private static OnClickCallback mCallback;

    public static void setOnClickCallback(OnClickCallback callback) {
        mCallback = callback;
    }

    public interface OnChangeCallback {
        void onChangeBoxClick(View view, int position, HcmailDeletebox item);
    }

    private static OnChangeCallback cCallback;

    public static void setOnChangeCallback(OnChangeCallback callback) {
        cCallback = callback;
    }

    public HcmailDeleteboxAdapter(Context context, List<HcmailDeletebox> infos, Boolean checkFlag) {
        super(context, infos);
        mCheckFlag = checkFlag;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final HcmailDeletebox item = (HcmailDeletebox) getItem(position);
        HcmailDeleteboxViewHolder hcmailDeleteboxViewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.hcmail_deletebox_list_item,
                    parent, false);
            hcmailDeleteboxViewHolder = new HcmailDeleteboxViewHolder();
            hcmailDeleteboxViewHolder.img = (ImageView) convertView
                    .findViewById(R.id.hcmail_box_list_img);
            hcmailDeleteboxViewHolder.title = (TextView) convertView
                    .findViewById(R.id.hcmail_box_list_title);
            hcmailDeleteboxViewHolder.name = (TextView) convertView
                    .findViewById(R.id.hcmail_box_list_from_name);
            hcmailDeleteboxViewHolder.date = (TextView) convertView
                    .findViewById(R.id.hcmail_box_list_date);
            hcmailDeleteboxViewHolder.checkbox = (CheckBox) convertView
                    .findViewById(R.id.hcmail_deletebox_list_checkbox);
            hcmailDeleteboxViewHolder.deleteboxContent = (LinearLayout) convertView
                    .findViewById(R.id.deletebox_content);
            if (mCheckFlag) {
                hcmailDeleteboxViewHolder.checkbox.setVisibility(View.VISIBLE);
                //设置所有的checkbox为空状态
            } else {
                hcmailDeleteboxViewHolder.checkbox.setVisibility(View.GONE);
            }
            convertView.setTag(hcmailDeleteboxViewHolder);
        } else {
            hcmailDeleteboxViewHolder = (HcmailDeleteboxViewHolder) convertView.getTag();
        }
        hcmailDeleteboxViewHolder.img.setImageResource(R.drawable.hcmail_inbox_opened);
        hcmailDeleteboxViewHolder.title.setText(item.getBoxTitle());
        hcmailDeleteboxViewHolder.name.setText(item.getBoxName());
        hcmailDeleteboxViewHolder.date.setText(item.getBoxDate());
        final HcmailDeleteboxViewHolder finalHcmailDeleteboxViewHolder = hcmailDeleteboxViewHolder;
        final View finalConvertView = convertView;
        if (mCheckFlag) {
            hcmailDeleteboxViewHolder.checkbox.setChecked(item.isChecked());
            hcmailDeleteboxViewHolder.deleteboxContent.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (finalHcmailDeleteboxViewHolder.checkbox.isChecked()) {
                        finalHcmailDeleteboxViewHolder.checkbox.setChecked(false);
                    } else {
                        finalHcmailDeleteboxViewHolder.checkbox.setChecked(true);
                    }
                    if (mCallback != null) {
                        mCallback.onCheckBoxClick(finalConvertView, position, item);
                    }

                }
            });
            hcmailDeleteboxViewHolder.checkbox.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mCallback != null) {
                        mCallback.onCheckBoxClick(finalConvertView, position, item);
                    }
                }
            });

        } else {
            hcmailDeleteboxViewHolder.deleteboxContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!finalHcmailDeleteboxViewHolder.checkbox.isChecked()) {
                        finalHcmailDeleteboxViewHolder.checkbox.setChecked(true);
                    }
                    //回调接口，从新装一次
                    if (cCallback != null) {
                        cCallback.onChangeBoxClick(finalConvertView, position, item);
                    }
                }
            });

        }
        return convertView;
    }

    public static class HcmailDeleteboxViewHolder {
        private LinearLayout deleteboxContent;
        public TextView date;
        public TextView title;
        public TextView name;
        public ImageView img;
        public CheckBox checkbox;
    }
}
