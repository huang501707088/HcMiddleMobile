package com.android.hcframe.approve;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcUtil;
import com.android.hcframe.sql.SettingHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by pc on 2016/6/22.
 */
public class ApproveDetailAdapter extends BaseAdapter {
    private List<JSONObject> mySkydriveItem;
    private Context mContext;
    private LayoutInflater mInflater;

    public ApproveDetailAdapter(Context context, List<JSONObject> jsonArray) {
        mySkydriveItem = jsonArray;
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return mySkydriveItem.size();
    }

    @Override
    public Object getItem(int position) {
        return mySkydriveItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        MySkydriveViewHolder mySkydriveViewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.approve_detail_item,
                    parent, false);
            mySkydriveViewHolder = new MySkydriveViewHolder();
            mySkydriveViewHolder.approve_id_rl_first = (RelativeLayout) convertView.findViewById(R.id.approve_id_rl_first);
            mySkydriveViewHolder.approve_id_ll_item = (LinearLayout) convertView.findViewById(R.id.approve_id_ll_item);
            mySkydriveViewHolder.approve_id_tv_title = (TextView) convertView.findViewById(R.id.approve_id_tv_title);
            mySkydriveViewHolder.approve_id_tv_time = (TextView) convertView.findViewById(R.id.approve_id_tv_time);
            mySkydriveViewHolder.approve_id_tv_name = (TextView) convertView.findViewById(R.id.approve_id_tv_name);
            mySkydriveViewHolder.approve_id_tv_status = (TextView) convertView.findViewById(R.id.approve_id_tv_status);
            mySkydriveViewHolder.approve_id_tv_remark = (TextView) convertView.findViewById(R.id.approve_id_tv_remark);
            convertView.setTag(mySkydriveViewHolder);
        } else {
            mySkydriveViewHolder = (MySkydriveViewHolder) convertView.getTag();
        }
        JSONObject jsonObject = mySkydriveItem.get(position);
        if (position == 0) {
            mySkydriveViewHolder.approve_id_rl_first.setVisibility(View.VISIBLE);
            mySkydriveViewHolder.approve_id_ll_item.setVisibility(View.GONE);
            mySkydriveViewHolder.approve_id_tv_title.setText(jsonObject.optString("projectName"));
            mySkydriveViewHolder.approve_id_tv_time.setText(HcUtil.getDate(HcUtil.FORMAT_POLLUTION_NEW, Long.parseLong(jsonObject.optString("createDate"))) + "由" + jsonObject.optString("createUserName") + "创建");
        } else {
            mySkydriveViewHolder.approve_id_rl_first.setVisibility(View.GONE);
            mySkydriveViewHolder.approve_id_ll_item.setVisibility(View.VISIBLE);
            mySkydriveViewHolder.approve_id_tv_name.setText(jsonObject.optString("versioncode"));
            String sts = jsonObject.optString("sts");
            if ("0".equals(sts)) {
                mySkydriveViewHolder.approve_id_tv_status.setText("待批阅");
                mySkydriveViewHolder.approve_id_tv_status.setTextColor(Color.parseColor("#FF5013"));
            } else {
                mySkydriveViewHolder.approve_id_tv_status.setText("已批阅");
                mySkydriveViewHolder.approve_id_tv_status.setTextColor(Color.parseColor("#999999"));
            }
            mySkydriveViewHolder.approve_id_tv_remark.setText(jsonObject.optString("approveMemo"));
        }
        return convertView;
    }


    public static class MySkydriveViewHolder {
        TextView approve_id_tv_title;
        TextView approve_id_tv_time;
        TextView approve_id_tv_name;
        TextView approve_id_tv_status;
        TextView approve_id_tv_remark;
        LinearLayout approve_id_ll_item;
        RelativeLayout approve_id_rl_first;
    }

}
