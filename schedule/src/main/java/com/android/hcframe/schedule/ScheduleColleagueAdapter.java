package com.android.hcframe.schedule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.hcframe.HcLog;
import com.android.hcframe.ModuleBridge;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.schedule.data.ScheduleColleagueInfo;

import java.util.List;

/**
 * Created by zhujiabin on 2016/11/22.
 */

public class ScheduleColleagueAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<List<ScheduleDetailsInfo>> mInfos;

    public ScheduleColleagueAdapter(Context context, List<List<ScheduleDetailsInfo>> infos) {
        mContext = context;
        mInfos = infos;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return mInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.schedule_colleague_adapter, parent, false);
            holder.mColleague = (LinearLayout) convertView.findViewById(R.id.schedule_col_linear);
            holder.mName = (TextView) convertView.findViewById(R.id.schedule_colleague_txt);
            holder.mTimes = (LinearLayout) convertView.findViewById(R.id.schedule_colleague_time);
            holder.mThemes = (TextView) convertView.findViewById(R.id.schedule_colleague_theme);
            holder.mIcon = (ImageView) convertView.findViewById(R.id.schedule_colleague_tele_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final List<ScheduleDetailsInfo> infos = (List<ScheduleDetailsInfo>) getItem(position);
        StringBuilder s = new StringBuilder();
        holder.mTimes.removeAllViews();
        int none = 0;
        int size = infos.size();
//        HcLog.D("ScheduleColleagueAdapter #getView infos size =" + size);
        for (int j = 0; j < size; j++) {
            String startTime = null, endTime = null;
            TextView tv = new TextView(mContext);
            StringBuilder sb = new StringBuilder();
            String start = infos.get(j).getStartTime();
            String end = infos.get(j).getEndTime();
            boolean isToday = false;
            if (start != null && end != null) {
                isToday = ScheduleUtils.isToday(Long.parseLong(start), Long.parseLong(end));
            }
            if (isToday) {
                if (start != null && end != null) {
                    startTime = ScheduleUtils.stampToTime(start);
                    endTime = ScheduleUtils.stampToTime(end);
                    sb.append(startTime);
                    sb.append("~");
                    sb.append(endTime);
                }
                if (start != null && end == null) {
                    startTime = ScheduleUtils.stampToTime(start);
                    sb.append(startTime);
                }
                if (start == null && end == null) {
                    none++;
                }
                tv.setSingleLine();
                tv.setTextColor(Color.parseColor("#999999"));
                tv.setTextSize(12);
                tv.setText(sb.toString());
            } else {
                if (start != null && end != null) {
                    startTime = ScheduleUtils.stampToTimes(start);
                    endTime = ScheduleUtils.stampToTimes(end);
                    sb.append(startTime);
                    sb.append("~");
                    sb.append(endTime);
                }
                if (start != null && end == null) {
                    startTime = ScheduleUtils.stampToTimes(start);
                    sb.append(startTime);
                }
                if (start == null && end == null) {
                    none++;
                }
                tv.setSingleLine();
                tv.setTextColor(Color.parseColor("#999999"));
                tv.setTextSize(12);
                tv.setText(sb.toString());
            }
            holder.mTimes.addView(tv);
            s.append(infos.get(j).getTheme() + "\n");
            holder.mName.setText(infos.get(j).getmName());
            String theme = s.toString().substring(0, s.toString().length() - 1);
            if (!theme.equals("null")) {
                holder.mThemes.setText(theme);
            } else {
                holder.mThemes.setText("");
            }
        }
        if (none == infos.size()) {
            holder.mTimes.removeAllViews();
            TextView times = new TextView(mContext);
            times.setSingleLine();
            times.setTextColor(Color.parseColor("#999999"));
            times.setTextSize(14);
            times.setText("没有日程安排");
            holder.mTimes.addView(times);
        }
        holder.mIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HcLog.D("Creator = " + infos.get(0).getmUserId());
                String creatorId = infos.get(0).getmUserId();
                //跳转到通讯录
                ModuleBridge.startContactDetailsActivity((Activity) mContext, creatorId);
            }
        });
        holder.mColleague.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ScheduleColleagueInfoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("scheduleName", mInfos.get(position).get(0).getmName());
                bundle.putString("scheduleId", mInfos.get(position).get(0).getmUserId());
                intent.putExtras(bundle);
                mContext.startActivity(intent);
            }
        });
        return convertView;
    }

    public static class ViewHolder {
        private LinearLayout mColleague;
        private TextView mName;
        private LinearLayout mTimes;
        private TextView mThemes;
        private ImageView mIcon;
    }

    public void updateData(ScheduleColleagueInfo info) {
        mInfos.clear();
        mInfos.addAll(info.getScheduleColleagueInfos().values());
        notifyDataSetChanged();
        HcLog.D("ScheduleColleagueAdapter#updateData info size = " + mInfos.size());
    }

    public void clear() {
        mInfos.clear();
    }
}




