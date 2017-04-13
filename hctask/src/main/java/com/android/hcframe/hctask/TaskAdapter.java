package com.android.hcframe.hctask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.hcframe.HcLog;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.hctask.state.TaskState;
import com.android.hcframe.sql.SettingHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-8-3 14:57.
 */
public class TaskAdapter extends HcBaseAdapter<TaskState> {

    private DisplayImageOptions mHeaderOption;

    private final TaskOperator mOperator;

    public TaskAdapter(Context context, List<TaskState> infos, TaskOperator operator) {
        super(context, infos);
        mHeaderOption = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageOnLoading(R.drawable.task_default_head)
                .showImageForEmptyUri(R.drawable.task_default_head)
                .showImageOnFail(R.drawable.task_default_head)
                .cacheInMemory(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888).build();
        mOperator = operator;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TaskState task = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.task_item_layout, parent, false);
            holder.mBtn1 = (TextView) convertView.findViewById(R.id.task_item_btn1);
            holder.mBtn2 = (TextView) convertView.findViewById(R.id.task_item_btn2);
            holder.mContent = (TextView) convertView.findViewById(R.id.task_item_content);
            holder.mDivider = convertView.findViewById(R.id.task_item_divider);
            holder.mEndDate = (TextView) convertView.findViewById(R.id.task_item_date);
            holder.mIcon = (ImageView) convertView.findViewById(R.id.task_item_icon);
            holder.mPublisher = (TextView) convertView.findViewById(R.id.task_item_publisher);
            holder.mStatus = (ImageView) convertView.findViewById(R.id.task_item_status);
            holder.mTimeout = (TextView) convertView.findViewById(R.id.task_item_timeout);
//            holder.mTimeout = (ImageView) convertView.findViewById(R.id.task_item_img);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mDivider.setBackgroundColor(Color.parseColor(task.getDividerColor()));
        if (task.getStatus() == 0) {
            holder.mStatus.setImageResource(R.drawable.task_receiving_icon);
        } else if (task.getStatus() == 1) {
            holder.mStatus.setImageResource(R.drawable.task_proceiving_icon);
        } else if (task.getStatus() == 2) {
            holder.mStatus.setImageResource(R.drawable.task_completed_icon);
        }
        //如果是自己发布的就显示执行人，如果是别人发布的就显示发布人，通过userId判断
        if (SettingHelper.getUserId(mContext).equals(task.getPublisherId())) {
            holder.mPublisher.setText("执行人： " + task.getExecutor());
        } else {
            holder.mPublisher.setText("发布人： " + task.getPublisher());
        }
        ImageLoader.getInstance().displayImage(task.getPublisherUrl(), holder.mIcon, mHeaderOption);
        holder.mEndDate.setText("截止日期： " + task.getEndDate());
        holder.mContent.setText(task.getDescription());
        task.setTextView(mContext, mOperator, holder.mBtn1, holder.mBtn2);
        if (isTimeout(task.getEndDate())) {
            if (holder.mTimeout.getVisibility() != View.VISIBLE) {
                holder.mTimeout.setVisibility(View.VISIBLE);
            }
        } else {
            if (holder.mTimeout.getVisibility() != View.GONE) {
                holder.mTimeout.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    private class ViewHolder {
        View mDivider;
        ImageView mStatus;
        ImageView mIcon;
        TextView mPublisher;
        TextView mEndDate;
        TextView mContent;
        TextView mBtn1;
        TextView mBtn2;
        TextView mTimeout;
    }

    private boolean isTimeout(String date) {
        long current = System.currentTimeMillis();
        long end = getDateMills(date);
        return current > end;
    }

    private long getDateMills(String date) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date d = f.parse(date);
            return d.getTime() + 24 * 60 * 60 * 1000;
        } catch (Exception e) {
            HcLog.D("TaskAdapter #getDateMills Exception e =" + e + " date = " + date);
        }
        return 0;
    }
}
