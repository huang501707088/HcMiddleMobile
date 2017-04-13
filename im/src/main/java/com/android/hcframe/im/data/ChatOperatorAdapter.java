package com.android.hcframe.im.data;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.im.R;

import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-10-9 09:36.
 */

public class ChatOperatorAdapter extends HcBaseAdapter<ChatOperatorAdapter.OperatorItem> {


    public ChatOperatorAdapter(Context context, List<OperatorItem> infos) {
        super(context, infos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        OperatorItem item = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.im_chatting_operator_item, parent, false);
            holder = new ViewHolder();
            holder.mIcon = (ImageView) convertView.findViewById(R.id.chat_operator_item_icon);
            holder.mName = (TextView) convertView.findViewById(R.id.chat_operator_item_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mName.setText(item.mName);
        holder.mIcon.setImageResource(item.mResId);
        return convertView;
    }

    private class ViewHolder {
        ImageView mIcon;
        TextView mName;
    }

    public static class OperatorItem {
        String mName;
        int mResId;

        public OperatorItem(String name, int resId) {
            mName = name;
            mResId = resId;
        }
    }
}
