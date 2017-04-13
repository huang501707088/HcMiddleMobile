package com.android.hcframe.im.data;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.adapter.ViewHolderBase;

import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-9-8 16:42.
 */
public class ChatMessageAdapter extends HcBaseAdapter<ChatMessageInfo> {

    private final boolean mGroup;

    public ChatMessageAdapter(Context context, List<ChatMessageInfo> infos, boolean group) {
        super(context, infos);
        mGroup = group;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessageInfo data = getItem(position);
        ViewHolderBase<ChatMessageInfo> base;
        if (convertView == null) {
            base = data.createViewHolder(mGroup);
            if (base != null) {
                convertView = base.createView(mInflater);
                convertView.setTag(base);
            }
        } else {
            base = (ViewHolderBase<ChatMessageInfo>) convertView.getTag();
        }
        if (base != null) {
            base.setItemData(position, data);
        }
        return convertView;
    }

    @Override
    public int getViewTypeCount() {

        return 7;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessageInfo info = mInfos.get(position);
        if (info instanceof ChatTextOwnInfo) {
            return 0;
        } else if (info instanceof ChatTextOtherInfo) {
            return 1;
        } else if (info instanceof ChatVoiceOwnInfo) {
            return 2;
        } else if (info instanceof ChatVoiceOtherInfo) {
            return 3;
        } else if (info instanceof ChatImageOwnInfo){
            return 4;
        } else if (info instanceof ChatImageOtherInfo){
            return 5;
        } else {
            return 6;
        }
    }
}
