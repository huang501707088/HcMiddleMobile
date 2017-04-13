package com.android.hcframe.netdisc.audio;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.hcframe.HcUtil;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.netdisc.R;

import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-8-1 13:55.
 */
public class AudioAdapter extends HcBaseAdapter<NetdiscAudioInfo> {

    public AudioAdapter(Context context, List<NetdiscAudioInfo> infos) {
        super(context, infos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NetdiscAudioInfo info = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.netdisc_audio_item_layout, parent, false);
            holder.mDate = (TextView) convertView.findViewById(R.id.audio_item_file_date);
            holder.mName = (TextView) convertView.findViewById(R.id.audio_item_file_name);
            holder.mSelected = (CheckBox) convertView.findViewById(R.id.audio_item_checkbox);
            holder.mIcon = (ImageView) convertView.findViewById(R.id.audio_item_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mIcon.setImageResource(info.getResId());
//        if (info.isAudio()) {
//            holder.mIcon.setImageResource(R.drawable.netdisc_music_file);
//        } else {
//            holder.mIcon.setImageResource(R.drawable.netdisc_video_file);
//        }
        holder.mName.setText(info.getDisplayName());
        holder.mDate.setText(HcUtil.getDate("yyyy-MM-dd  HH:mm", info.getDate()));
        holder.mSelected.setChecked(info.isSelected());
        return convertView;
    }

    private class ViewHolder {
        TextView mName;
        TextView mDate;
        CheckBox mSelected;
        ImageView mIcon;
    }
}
