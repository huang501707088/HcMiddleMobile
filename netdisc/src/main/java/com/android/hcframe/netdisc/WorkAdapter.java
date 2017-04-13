package com.android.hcframe.netdisc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * Created by pc on 2016/6/22.
 */
public class WorkAdapter extends BaseAdapter {
    private JSONArray mySkydriveItem;
    private Context mContext;
    private LayoutInflater mInflater;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;
    int type;

    public WorkAdapter(Context context, JSONArray jsonArray, int type) {
        mySkydriveItem = jsonArray;
        mContext = context;
        this.type = type;
        mInflater = LayoutInflater.from(context);
        mOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageOnLoading(R.drawable.default_icon)
                .showImageForEmptyUri(R.drawable.default_icon)
                .showImageOnFail(R.drawable.default_icon).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888).build();
        mImageLoader = ImageLoader.getInstance();
    }


    @Override
    public int getCount() {
        return mySkydriveItem.length();
    }

    @Override
    public Object getItem(int position) {
        try {
            return mySkydriveItem.get(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        MySkydriveViewHolder mySkydriveViewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.netdisc_work_item,
                    parent, false);
            mySkydriveViewHolder = new MySkydriveViewHolder();
            mySkydriveViewHolder.netdisc_id_tv_name = (TextView) convertView.findViewById(R.id.netdisc_id_tv_name);
            mySkydriveViewHolder.netdisc_id_tv_person = (TextView) convertView.findViewById(R.id.netdisc_id_tv_person);
            mySkydriveViewHolder.netdisc_id_tv_num = (TextView) convertView.findViewById(R.id.netdisc_id_tv_num);
            mySkydriveViewHolder.netdisc_id_tv_size = (TextView) convertView.findViewById(R.id.netdisc_id_tv_size);
            mySkydriveViewHolder.netdisc_id_pb = (ProgressBar) convertView.findViewById(R.id.netdisc_id_pb);
            mySkydriveViewHolder.netdisc_id_ll_person = (LinearLayout) convertView.findViewById(R.id.netdisc_id_ll_person);
            convertView.setTag(mySkydriveViewHolder);
        } else {
            mySkydriveViewHolder = (MySkydriveViewHolder) convertView.getTag();
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) mySkydriveItem.get(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mySkydriveViewHolder.netdisc_id_tv_name.setText(jsonObject.optString("infoname"));
        if (type == WorkGroupActivity.HOME) {
            mySkydriveViewHolder.netdisc_id_ll_person.setVisibility(View.VISIBLE);
            mySkydriveViewHolder.netdisc_id_tv_person.setText(jsonObject.optString("username"));
            String usespace = jsonObject.optString("usespace");
            String totalspace = jsonObject.optString("totalspace");
            long us = 1;
            long ts = 1;
            if (usespace != null && !"".equals(usespace)&&!"null".equals(usespace)) {
                us = Long.decode(usespace);
            }
            if (totalspace != null && !"".equals(totalspace)&&!"null".equals(totalspace)) {
                ts = Long.decode(totalspace);
            }

            long totle = ts * 1024 * 1024 * 1024;
            int percent = (int) ((us * 100) / totle);

            if (percent > 50) {
                mySkydriveViewHolder.netdisc_id_pb.setSecondaryProgress(percent);
            } else {
                mySkydriveViewHolder.netdisc_id_pb.setProgress(percent);
            }
            mySkydriveViewHolder.netdisc_id_pb.setVisibility(View.VISIBLE);
            mySkydriveViewHolder.netdisc_id_tv_size.setVisibility(View.VISIBLE);
            mySkydriveViewHolder.netdisc_id_tv_size.setText("存储空间:" + getSize(us) + "/" + totalspace + "G");
            mySkydriveViewHolder.netdisc_id_tv_num.setText("文件数:" + jsonObject.optString("filecount") + "个");
        } else if (type == WorkGroupActivity.NETDISC) {
            mySkydriveViewHolder.netdisc_id_pb.setVisibility(View.GONE);
            mySkydriveViewHolder.netdisc_id_ll_person.setVisibility(View.GONE);
            mySkydriveViewHolder.netdisc_id_tv_size.setVisibility(View.GONE);
            mySkydriveViewHolder.netdisc_id_tv_num.setText("创建时间:" + jsonObject.optString("createTime"));
        }

        return convertView;
    }


    public static class MySkydriveViewHolder {
        TextView netdisc_id_tv_name;
        TextView netdisc_id_tv_person;
        TextView netdisc_id_tv_num;
        TextView netdisc_id_tv_size;
        ProgressBar netdisc_id_pb;
        LinearLayout netdisc_id_ll_person;
    }

    private String getSize(long size) {
        DecimalFormat df = new DecimalFormat(".##");
        if (size != 0) {
            if (size > 1024 * 1024) {
                if (size > 1024 * 1024 * 1024) {
                    return df.format(((double) size) / (1024.00 * 1024 * 1024)) + "G";
                } else {
                    return df.format(((double) size) / (1024.00 * 1024)) + "M";
                }

            } else {
                return df.format(((double) size) / (1024.00)) + "K";
            }
        }
        return "0G";
    }
}
