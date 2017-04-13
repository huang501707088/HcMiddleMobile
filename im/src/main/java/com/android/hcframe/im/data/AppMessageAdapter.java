package com.android.hcframe.im.data;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.hcframe.HcApplication;
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
 * Created by jrjin on 16-9-9 19:20.
 */
public class AppMessageAdapter extends HcBaseAdapter<AppMessageInfo> {

    public AppMessageAdapter(Context context, List<AppMessageInfo> infos) {
        super(context, infos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppMessageInfo info = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.im_home_item_layout, parent, false);
            holder.mContent = (TextView) convertView.findViewById(R.id.im_home_item_content);
            holder.mDate = (TextView) convertView.findViewById(R.id.im_home_item_date);
            holder.mTitle = (TextView) convertView.findViewById(R.id.im_home_item_title);
            holder.mIcon = (ImageView) convertView.findViewById(R.id.im_home_item_icon);
            holder.mPoint = (PointTextView) convertView.findViewById(R.id.im_home_item_badge);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mTitle.setText(info.getTitle());
        SpannableString spannable = null;
        if (info.getType() == 2 && IMSettings.getIMReceiverGroup(mContext).contains(info.getId())) {
            String s = "[有人@我] ";
            spannable = IMUtil.getExpressionString(HcApplication.getContext(), s + info.getContent(), 18);
            spannable.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            spannable = IMUtil.getExpressionString(HcApplication.getContext(), info.getContent(), 18);
        }

        holder.mContent.setText(spannable);
        holder.mDate.setText(IMUtil.getMessageDate(mContext, Long.valueOf(info.getDate())));

//        holder.mIcon.setImageResource(R.drawable.im_chat_group_icon);
        if (info.getType() == 2) { // 群组
            ImageLoader.getInstance().displayImage(info.getIconUri(), holder.mIcon, HcUtil.getAccountImageOptions());
        } else if (info.getType() == 3) { // 单聊
            ImageLoader.getInstance().displayImage(HcUtil.getHeaderUri(info.getIconUri()), holder.mIcon, HcUtil.getAccountImageOptions());
        } else { // 系统消息
            ImageLoader.getInstance().displayImage(info.getIconUri(), holder.mIcon, HcUtil.getImageOptions());
        }

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
