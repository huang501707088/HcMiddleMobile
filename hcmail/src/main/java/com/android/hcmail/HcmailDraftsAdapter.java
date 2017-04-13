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

public class HcmailDraftsAdapter extends HcBaseAdapter<HcmailDrafts> {
    private Boolean mCheckFlag;

    public interface OnClickCallback {
        void onCheckBoxClick(View view, int position, HcmailDrafts item);
    }

    private static OnClickCallback mCallback;

    public static void setOnClickCallback(OnClickCallback callback) {
        mCallback = callback;
    }

    public interface OnItemClickCallback {
        void onItemClick(View view, int position, HcmailDrafts item);
    }

    private static OnItemClickCallback iCallback;

    public static void setItemClickCallback(OnItemClickCallback callback) {
        iCallback = callback;
    }

    public HcmailDraftsAdapter(Context context, List<HcmailDrafts> infos, Boolean checkFlag) {
        super(context, infos);
        mCheckFlag = checkFlag;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final HcmailDrafts item = (HcmailDrafts) getItem(position);
        HcmailDraftsViewHolder hcmailDraftsViewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.hcmail_drafts_list_item,
                    parent, false);
            hcmailDraftsViewHolder = new HcmailDraftsViewHolder();
            hcmailDraftsViewHolder.img = (ImageView) convertView
                    .findViewById(R.id.hcmail_box_list_img);
            hcmailDraftsViewHolder.title = (TextView) convertView
                    .findViewById(R.id.hcmail_box_list_title);
            hcmailDraftsViewHolder.name = (TextView) convertView
                    .findViewById(R.id.hcmail_box_list_from_name);
            hcmailDraftsViewHolder.date = (TextView) convertView
                    .findViewById(R.id.hcmail_box_list_date);
            hcmailDraftsViewHolder.checkbox = (CheckBox) convertView
                    .findViewById(R.id.hcmail_drafts_list_checkbox);
            hcmailDraftsViewHolder.draftsContent = (LinearLayout) convertView
                    .findViewById(R.id.drafts_content);
            if (mCheckFlag) {
                hcmailDraftsViewHolder.checkbox.setVisibility(View.VISIBLE);
                //设置所有的checkbox为空状态
            } else {
                hcmailDraftsViewHolder.checkbox.setVisibility(View.GONE);
            }
            convertView.setTag(hcmailDraftsViewHolder);
        } else {
            hcmailDraftsViewHolder = (HcmailDraftsViewHolder) convertView.getTag();
        }
        hcmailDraftsViewHolder.img.setImageResource(R.drawable.hcmail_inbox_opened);
        hcmailDraftsViewHolder.title.setText(item.getBoxTitle());
        hcmailDraftsViewHolder.name.setText(item.getBoxName());
        hcmailDraftsViewHolder.date.setText(item.getBoxDate());
        final View finalConvertView = convertView;
        if (mCheckFlag) {
            hcmailDraftsViewHolder.checkbox.setChecked(item.isChecked());
            final HcmailDraftsViewHolder finalHcmailDraftsViewHolder = hcmailDraftsViewHolder;
            hcmailDraftsViewHolder.draftsContent.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (finalHcmailDraftsViewHolder.checkbox.isChecked()) {
                        finalHcmailDraftsViewHolder.checkbox.setChecked(false);
                    } else {
                        finalHcmailDraftsViewHolder.checkbox.setChecked(true);
                    }
                    if (mCallback != null) {
                        mCallback.onCheckBoxClick(finalConvertView, position, item);
                    }

                }
            });
            hcmailDraftsViewHolder.checkbox.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mCallback != null) {
                        mCallback.onCheckBoxClick(finalConvertView, position, item);
                    }
                }
            });

        } else {
            hcmailDraftsViewHolder.draftsContent.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (iCallback != null) {
                        iCallback.onItemClick(finalConvertView, position, item);
                    }
                }
            });
        }
        return convertView;
    }

    public static class HcmailDraftsViewHolder {
        private LinearLayout draftsContent;
        public TextView date;
        public TextView title;
        public TextView name;
        public ImageView img;
        public CheckBox checkbox;
    }
}
