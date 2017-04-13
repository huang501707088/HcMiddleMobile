package com.android.hcframe.im;

import android.content.Context;
import android.text.SpannableString;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcUtil;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.im.IMUtil;
import com.android.hcframe.im.R;
import com.android.hcframe.im.data.AppMessageInfo;
import com.android.hcframe.im.data.ChatGroupMessageInfo;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-9-9 19:20.
 */
public class IMGroupMenuAdapter extends HcBaseAdapter<ChatGroupMessageInfo> {

    public IMGroupMenuAdapter(Context context, List<ChatGroupMessageInfo> infos) {
        super(context, infos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatGroupMessageInfo info = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.im_group_item_layout, parent, false);
            holder.mNumOfPeople = (TextView) convertView.findViewById(R.id.im_group_item_num_of_people);
            holder.mTitle = (TextView) convertView.findViewById(R.id.im_group_item_title);
            holder.mIcon = (ImageView) convertView.findViewById(R.id.im_group_item_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mTitle.setText(info.getTitle());
        holder.mNumOfPeople.setText(info.getCount() + "人");
        return convertView;
    }

    private class ViewHolder {
        ImageView mIcon;
        TextView mTitle;
        TextView mNumOfPeople;
    }
}
