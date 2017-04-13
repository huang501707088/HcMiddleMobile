package com.android.hcframe.hctask;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.container.ContainerCircleImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.List;

/**
 * Created by pc on 2016/8/5.
 */
public class TaskDetailAdapter extends BaseAdapter {

    private List<TaskDetailInfo.DiscussInfo> mDiscussInfoItem;
    private Context mContext;
    private LayoutInflater mInflater;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;

    public TaskDetailAdapter(Context context, List<TaskDetailInfo.DiscussInfo> list) {
        mDiscussInfoItem = list;
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageOnLoading(R.drawable.task_default_head)
                .showImageForEmptyUri(R.drawable.task_default_head)
                .showImageOnFail(R.drawable.task_default_head).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888).build();
        mImageLoader = ImageLoader.getInstance();
    }


    @Override
    public int getCount() {
        return mDiscussInfoItem.size();
    }

    @Override
    public Object getItem(int position) {
        return mDiscussInfoItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final TaskDetailInfo.DiscussInfo item = (TaskDetailInfo.DiscussInfo) getItem(position);
        TaskDetailsViewHolder mTaskDetailsViewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.task_details_item_layout,
                    parent, false);
            mTaskDetailsViewHolder = new TaskDetailsViewHolder();
            mTaskDetailsViewHolder.mUrl = (ContainerCircleImageView) convertView
                    .findViewById(R.id.task_details_item_icon);
            mTaskDetailsViewHolder.mName = (TextView) convertView.findViewById(R.id.task_details_item_name);
            mTaskDetailsViewHolder.mContent = (TextView) convertView.findViewById(R.id.task_details_item_data);
            mTaskDetailsViewHolder.mDate = (TextView) convertView.findViewById(R.id.task_details_date);
            convertView.setTag(mTaskDetailsViewHolder);
        } else {
            mTaskDetailsViewHolder = (TaskDetailsViewHolder) convertView.getTag();
        }
        if (!"".equals(item.getmUrl()) && item.getmUrl() != null) {
            mImageLoader.displayImage(item.getmUrl(), mTaskDetailsViewHolder.mUrl, mOptions);
        }
        mTaskDetailsViewHolder.mName.setText(item.getmName());
        mTaskDetailsViewHolder.mContent.setText(item.getmContent());
        /**
         * 处理日期
         * */
        String date = item.getmDate();
        String[] dates = date.split("\\s+");
        StringBuilder month = new StringBuilder();
        String[] months = dates[0].split("-");
        String dMonth = month.append(months[0]).append("月").append(months[1]).append("日").toString();
        StringBuilder time = new StringBuilder();
        String[] times = dates[1].split(":");
        String dTime = time.append(times[0]).append(":").append(times[1]).toString();
        mTaskDetailsViewHolder.mDate.setText(dMonth + " " + dTime);
        return convertView;
    }


    public static class TaskDetailsViewHolder {
        TextView mName;
        TextView mContent;
        TextView mDate;
        ContainerCircleImageView mUrl;
    }


}
