package com.android.hcframe.approve;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.hcframe.HcApplication;
import com.android.hcframe.sql.SettingHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by pc on 2016/6/22.
 */
public class ApproveAdapter extends BaseAdapter {
    private List<JSONObject> mySkydriveItem;
    private Context mContext;
    private LayoutInflater mInflater;

    public ApproveAdapter(Context context, List<JSONObject> jsonArray) {
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
            convertView = mInflater.inflate(R.layout.approve_item,
                    parent, false);
            mySkydriveViewHolder = new MySkydriveViewHolder();
            mySkydriveViewHolder.approve_id_tv_title = (TextView) convertView.findViewById(R.id.approve_id_tv_title);
            mySkydriveViewHolder.approve_id_tv_name = (TextView) convertView.findViewById(R.id.approve_id_tv_name);
            mySkydriveViewHolder.approve_id_tv_status = (TextView) convertView.findViewById(R.id.approve_id_tv_status);
            mySkydriveViewHolder.approve_id_tv_remark = (TextView) convertView.findViewById(R.id.approve_id_tv_remark);
            mySkydriveViewHolder.approve_id_iv_mine = (ImageView) convertView.findViewById(R.id.approve_id_iv_mine);
            convertView.setTag(mySkydriveViewHolder);
        } else {
            mySkydriveViewHolder = (MySkydriveViewHolder) convertView.getTag();
        }
        JSONObject jsonObject = null;
        jsonObject = (JSONObject) mySkydriveItem.get(position);
        mySkydriveViewHolder.approve_id_tv_title.setText(jsonObject.optString("projectName"));
        mySkydriveViewHolder.approve_id_tv_name.setText(jsonObject.optString("versioncode"));
        String sts = jsonObject.optString("sts");
        if ("0".equals(sts)) {
            mySkydriveViewHolder.approve_id_tv_status.setText("未批阅");
            mySkydriveViewHolder.approve_id_tv_status.setTextColor(Color.parseColor("#FF5013"));
            String userid = SettingHelper.getUserId(HcApplication.getContext());
            String approveUserId = jsonObject.optString("approveUserId");
            if (approveUserId.equals(userid)) {
                mySkydriveViewHolder.approve_id_iv_mine.setVisibility(View.VISIBLE);
            } else {
                mySkydriveViewHolder.approve_id_iv_mine.setVisibility(View.INVISIBLE);
            }
        } else {
            mySkydriveViewHolder.approve_id_tv_status.setText("已批阅");
            mySkydriveViewHolder.approve_id_tv_status.setTextColor(Color.parseColor("#999999"));
            mySkydriveViewHolder.approve_id_iv_mine.setVisibility(View.INVISIBLE);
        }
        mySkydriveViewHolder.approve_id_tv_remark.setText(jsonObject.optString("approveMemo"));
        return convertView;
    }


    public static class MySkydriveViewHolder {
        TextView approve_id_tv_title;
        TextView approve_id_tv_name;
        TextView approve_id_tv_status;
        TextView approve_id_tv_remark;
        ImageView approve_id_iv_mine;
    }

}
