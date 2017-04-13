package com.android.hcframe.im.data;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.hcframe.HcLog;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.im.R;

import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-9-27 09:58.
 */

public class ChatSmileyAdapter extends HcBaseAdapter<String> {

    public ChatSmileyAdapter(Context context, List<String> infos) {
        super(context, infos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String name = getItem(position);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.im_chatting_smiley_item, parent, false);
        }
        ImageView icon = (ImageView) convertView;
        int resId = mContext.getResources().getIdentifier(name, "drawable", mContext.getPackageName());
        HcLog.D("ChatSmileyAdapter #getView resId = "+resId + " name = "+name);
        icon.setImageResource(resId);
        return icon;
    }
}
