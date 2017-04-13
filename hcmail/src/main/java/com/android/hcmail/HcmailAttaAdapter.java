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

public class HcmailAttaAdapter extends HcBaseAdapter<HcmailAtta> {

    private boolean mFlag;

    public interface OnDeleCallback {
        void onDeleClick(View view, int position, HcmailAtta item);
    }

    private static OnDeleCallback mCallback;

    public static void setOnDeleCallback(OnDeleCallback callback) {
        mCallback = callback;
    }

    public HcmailAttaAdapter(Context context, List<HcmailAtta> infos, boolean flag) {
        super(context, infos);
        mFlag = flag;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final HcmailAtta item = getItem(position);
        HcmailAttaHolder hcmailAttaHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.hcmail_atta_list_item,
                    parent, false);
            hcmailAttaHolder = new HcmailAttaHolder();
            hcmailAttaHolder.imgDelet = (ImageView) convertView
                    .findViewById(R.id.add_attachment_delet);
            hcmailAttaHolder.img = (ImageView) convertView
                    .findViewById(R.id.add_attachment);
            hcmailAttaHolder.name = (TextView) convertView
                    .findViewById(R.id.hcmail_atta_name);
            convertView.setTag(hcmailAttaHolder);
        } else {
            hcmailAttaHolder = (HcmailAttaHolder) convertView.getTag();
        }
        if (mFlag) {
            hcmailAttaHolder.img.setVisibility(View.GONE);
            hcmailAttaHolder.imgDelet.setVisibility(View.VISIBLE);
            final View finalConvertView = convertView;
            hcmailAttaHolder.imgDelet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCallback != null) {
                        mCallback.onDeleClick(finalConvertView, position, item);
                    }
                }
            });
        }else{
            hcmailAttaHolder.img.setVisibility(View.VISIBLE);
            hcmailAttaHolder.imgDelet.setVisibility(View.GONE);
        }
        hcmailAttaHolder.name.setText(item.getAttaName());
        return convertView;
    }

    public static class HcmailAttaHolder {
        public TextView name;
        public ImageView img;
        public ImageView imgDelet;
    }
}
