package com.android.hcframe.schedule;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.hcframe.HcUtil;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.List;

/**
 * Created by zhujiabin on 2016/11/28.
 */

public class ScheduleDetailAdapter extends HcBaseAdapter<ScheduleDetailAddition> {
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;

    public ScheduleDetailAdapter(Context context, List<ScheduleDetailAddition> infos) {
        super(context, infos);
        /**
         * 用来加载网络图片
         * */
        mOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageOnLoading(R.drawable.schedule_detail_addtion_img)
                .showImageForEmptyUri(R.drawable.schedule_detail_addtion_img)
                .showImageOnFail(R.drawable.schedule_detail_addtion_img).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888).build();
        mImageLoader = ImageLoader.getInstance();

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ScheduleDetailAddition info = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.schedule_detail_addition, parent, false);
            holder.mAdditionName = (TextView) convertView.findViewById(R.id.schedule_detail_addtion_txt);
            holder.isAdditionVisible = (ImageView) convertView.findViewById(R.id.schedule_detail_addtion_img);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mAdditionName.setText(info.getmAdditionName());
        ImageLoader.getInstance().displayImage(info.getIsAdditionVisible(), holder.isAdditionVisible, mOptions);
        return convertView;

    }

    public class ViewHolder {
        private TextView mAdditionName;
        private ImageView isAdditionVisible;
    }
}
