package com.android.hcframe.schedule;

import java.util.List;

import android.content.Context;
import android.media.Image;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.hcframe.HcLog;
import com.android.hcframe.sql.SettingHelper;

public class ScheduleInfoExpandAdapter extends BaseExpandableListAdapter {
    private LayoutInflater inflater = null;
    private List<ScheduleDateInfo> dateList;
    private Context context;
    private String mUserId;
    private Handler handler;

    public ScheduleInfoExpandAdapter(Context context, List<ScheduleDateInfo> dateList, String userId) {
        this.dateList = dateList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        mUserId = userId;
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                notifyDataSetChanged();
                super.handleMessage(msg);
            }
        };
    }

    public void refresh() {
        handler.sendMessage(new Message());
    }

    @Override
    public int getGroupCount() {
        // TODO Auto-generated method stub
        return dateList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (dateList.get(groupPosition).getScheduleInfoList() == null) {
            return 0;
        } else {
            return dateList.get(groupPosition).getScheduleInfoList().size();
        }
    }

    @Override
    public ScheduleDateInfo getGroup(int groupPosition) {
        // TODO Auto-generated method stub
        return dateList.get(groupPosition);
    }

    @Override
    public ScheduleInfo getChild(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return dateList.get(groupPosition).getScheduleInfoList().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        // TODO Auto-generated method stub
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        GroupViewHolder holder = new GroupViewHolder();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.schedule_date_info_layout, null);
        }
        holder.groupName = (TextView) convertView.findViewById(R.id.one_status_name);
        holder.groupDate = (TextView) convertView.findViewById(R.id.group_date);
        HcLog.D("groupPosition = " + groupPosition + "," + "dateList.size=" + dateList.size());
        String date = getGroup(groupPosition).getmSheduleDate();
        if (date != null) {
            holder.groupName.setText(ScheduleUtils.stampToDate(date));
        }
        holder.groupDate.setBackgroundColor(context.getResources().getColor(R.color.grey));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                             ViewGroup parent) {
        ChildViewHolder viewHolder = null;
        ScheduleInfo entity = getChild(groupPosition, childPosition);
        if (convertView != null) {
            viewHolder = (ChildViewHolder) convertView.getTag();
        } else {
            viewHolder = new ChildViewHolder();
            convertView = inflater.inflate(R.layout.schedule_info_layout, null);
            viewHolder.scheduleTime = (TextView) convertView.findViewById(R.id.two_status_name);
            viewHolder.scheduleTitle = (TextView) convertView.findViewById(R.id.two_complete_time);
            viewHolder.scheduleImg = (ImageView) convertView.findViewById(R.id.two_complete_img);
            viewHolder.scheduleIcon = (ImageView) convertView.findViewById(R.id.schedule_menu_icon);
        }
//        if (TextUtils.isEmpty(entity.getEndTime())) {
//            viewHolder.scheduleTime.setText(ScheduleUtils.stampToHomeDate(entity.getStartTime()));
//        } else {
        if (entity.getStartTime() != null && entity.getEndTime() != null) {
            if (ScheduleUtils.isToday(Long.parseLong(entity.getStartTime()), Long.parseLong(entity.getEndTime()))) {
                viewHolder.scheduleTime.setText(ScheduleUtils.stampToTime(entity.getStartTime()) + "~" + ScheduleUtils.stampToTime(entity.getEndTime()));
            } else {
                String startTime = entity.getStartTime();
                setTime(groupPosition, viewHolder, entity, ScheduleUtils.stampToHomeDate(startTime));
            }
        }else{
            viewHolder.scheduleTime.setVisibility(View.GONE);
        }
//        }
        if (("0").equals(entity.getCreatFlag())) {
            viewHolder.scheduleImg.setVisibility(View.GONE);
        } else if (("1").equals(entity.getCreatFlag())) {
            viewHolder.scheduleImg.setVisibility(View.VISIBLE);
        }
        viewHolder.scheduleTitle.setText(entity.getTheme());
        //此处判断箭头是否去除
        if (entity.getTheme().equals("没有日程安排") || !mUserId.equals(SettingHelper.getUserId(context))) {
            viewHolder.scheduleIcon.setVisibility(View.GONE);
        } else {
            viewHolder.scheduleIcon.setVisibility(View.VISIBLE);
        }
        convertView.setTag(viewHolder);
        return convertView;
    }

    private void setTime(int groupPosition, ChildViewHolder viewHolder, ScheduleInfo entity, String startTime) {
        String endTime = entity.getEndTime();
        long dateAddOne = Long.parseLong(dateList.get(groupPosition).getmSheduleDate()) + 60 * 60 * 24 * 1000;
        if (Long.parseLong(endTime) > dateAddOne) {
            viewHolder.scheduleTime.setText(startTime + "~" + ScheduleUtils.stampToHomeDate(endTime));
        } else {
            viewHolder.scheduleTime.setText(startTime + "~" + ScheduleUtils.stampToTime(endTime));
        }
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return true;
    }

    private class GroupViewHolder {
        public TextView groupName;
        public TextView groupDate;
    }

    private class ChildViewHolder {
        public TextView scheduleTime;
        public TextView scheduleTitle;
        public ImageView scheduleImg;
        public ImageView scheduleIcon;
    }

}
