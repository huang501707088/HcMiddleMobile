package com.android.hcframe.im.data;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.hcframe.HcUtil;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.im.IMUtil;
import com.android.hcframe.im.R;
import com.android.hcframe.view.PointTextView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-11-23 14:40.
 */

public class IMSystemMessageAdapter extends HcBaseAdapter<AppMessageInfo> {

    public IMSystemMessageAdapter(Context context, List<AppMessageInfo> infos) {
        super(context, infos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppMessageInfo info = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.im_system_message_item_layout, parent, false);
            holder.mContent = (TextView) convertView.findViewById(R.id.im_system_message_item_content);
            holder.mDate = (TextView) convertView.findViewById(R.id.im_system_message_item_date);
            holder.mTitle = (TextView) convertView.findViewById(R.id.im_system_message_item_title);
            holder.mIcon = (ImageView) convertView.findViewById(R.id.im_system_message_item_icon);
            holder.mPoint = (PointTextView) convertView.findViewById(R.id.im_system_message_item_badge);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mTitle.setText(info.getTitle());
        holder.mContent.setText(info.getContent());
//        SpannableString spannable = IMUtil.getExpressionString(HcApplication.getContext(), info.getContent(), 18);
//        holder.mContent.setText(spannable);
        holder.mDate.setText(IMUtil.getMessageDate(mContext, Long.valueOf(info.getDate())));
        ImageLoader.getInstance().displayImage(info.getIconUri(), holder.mIcon, HcUtil.getImageOptions());

        holder.mPoint.setCount(info.getCount());

        return convertView;
    }

    private class ViewHolder {
        ImageView mIcon;
        TextView mTitle;
        TextView mContent;
        TextView mDate;
        PointTextView mPoint;
    }
}
