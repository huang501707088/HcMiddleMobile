package com.android.hcframe.internalservice.linhai;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.internalservice.sign.SignListByDayActivity;
import com.android.hcframe.internalservice.signin.R;

import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-7-1 16:11.
 */
public class LHSignDayAdapter extends HcBaseAdapter<LHSignDayInfo> {

    public LHSignDayAdapter(Context context, List<LHSignDayInfo> infos) {
        super(context, infos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final LHSignDayInfo info = getItem(position);
        ViewHolder mHolder;
        if (convertView == null) {
            mHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.linhai_day_item_layout, parent, false);
            mHolder.mName = (TextView) convertView.findViewById(R.id.linhai_day_item_name);
            mHolder.mStatus = (TextView) convertView.findViewById(R.id.linhai_day_item_status);
            mHolder.mGo = (ImageView) convertView.findViewById(R.id.linhai_day_item_go);
            mHolder.mAmTime = (TextView) convertView.findViewById(R.id.linhai_day_item_am_time);
            mHolder.mPmTime = (TextView) convertView.findViewById(R.id.linhai_day_item_pm_time);
            mHolder.mAmAddress = (TextView) convertView.findViewById(R.id.linhai_day_item_am_address);
            mHolder.mPmAddress = (TextView) convertView.findViewById(R.id.linhai_day_item_pm_address);
            mHolder.mAmImage = (ImageView) convertView.findViewById(R.id.linhai_day_item_am_image);
            mHolder.mPmImage = (ImageView) convertView.findViewById(R.id.linhai_day_item_pm_image);

            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }
        mHolder.mName.setText(info.getName());
        // 根据状态显示
        if (LHSignDayListActivity.SIGN_TYPE_LATE.equals(info.getType())) {
            // 异常
            if (mHolder.mStatus.getVisibility() != View.VISIBLE)
                mHolder.mStatus.setVisibility(View.VISIBLE);
            mHolder.mStatus.setBackgroundResource(R.drawable.late_img);
        } else if (LHSignDayListActivity.SIGN_TYPE_FIELD.equals(info.getType())) {
            // 外勤
            if (mHolder.mStatus.getVisibility() != View.VISIBLE)
                mHolder.mStatus.setVisibility(View.VISIBLE);
            mHolder.mStatus.setBackgroundResource(R.drawable.sign_img);
        } else if (LHSignDayListActivity.SIGN_TYPE_LEAVE.equals(info.getType())) {
            // 请假
            if (mHolder.mStatus.getVisibility() != View.VISIBLE)
                mHolder.mStatus.setVisibility(View.VISIBLE);
            mHolder.mStatus.setBackgroundResource(R.drawable.linhai_sign_status_leave);
        } else if (LHSignDayListActivity.SIGN_TYPE_UNSIGN.equals(info.getType())) {
            // 未考勤
            if (mHolder.mStatus.getVisibility() != View.VISIBLE)
                mHolder.mStatus.setVisibility(View.VISIBLE);
            mHolder.mStatus.setBackgroundResource(R.drawable.not_img);
        } else {
            // 正常
            if (mHolder.mStatus.getVisibility() != View.GONE)
                mHolder.mStatus.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(info.getSigninTime())) {
            mHolder.mAmTime.setText("无记录");
        } else {
            mHolder.mAmTime.setText(getTime(info.getSigninTime()));
        }
        if (TextUtils.isEmpty(info.getSignoutTime())) {
            mHolder.mPmTime.setText("无记录");
        } else {
            mHolder.mPmTime.setText(getTime(info.getSignoutTime()));
        }
        if (TextUtils.isEmpty(info.getSigninAddress())) {
            if (mHolder.mAmAddress.getVisibility() != View.INVISIBLE)
                mHolder.mAmAddress.setVisibility(View.INVISIBLE);
        } else {
            if (mHolder.mAmAddress.getVisibility() != View.VISIBLE)
                mHolder.mAmAddress.setVisibility(View.VISIBLE);
            mHolder.mAmAddress.setText(info.getSigninAddress());
        }
        if (TextUtils.isEmpty(info.getSignoutAddress())) {
            if (mHolder.mPmAddress.getVisibility() != View.INVISIBLE)
                mHolder.mPmAddress.setVisibility(View.INVISIBLE);
        } else {
            if (mHolder.mPmAddress.getVisibility() != View.VISIBLE)
                mHolder.mPmAddress.setVisibility(View.VISIBLE);
            mHolder.mPmAddress.setText(info.getSignoutAddress());
        }

        mHolder.mAmImage.setVisibility(info.isShowSigninIcon() ? View.VISIBLE : View.INVISIBLE);
        mHolder.mPmImage.setVisibility(info.isShowSignoutIcon() ? View.VISIBLE : View.INVISIBLE);

        mHolder.mGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SignListByDayActivity.class);
                intent.putExtra("account", info.getUserId());
                intent.putExtra("signDay", info.getDate());
                mContext.startActivity(intent);
            }
        });
        if (LHSignDayListActivity.SIGN_TYPE_UNSIGN.equals(info.getType()) ||
                LHSignDayListActivity.SIGN_TYPE_LEAVE.equals(info.getType())) {
            if (mHolder.mGo.getVisibility() == View.VISIBLE) {
                mHolder.mGo.setVisibility(View.GONE);
            }
        } else {
            if (mHolder.mGo.getVisibility() != View.VISIBLE) {
                mHolder.mGo.setVisibility(View.VISIBLE);
            }
        }

        return convertView;
    }

    private class ViewHolder {

        TextView mName;
        TextView mStatus;
        ImageView mGo;
        TextView mAmTime;
        TextView mPmTime;
        TextView mAmAddress;
        TextView mPmAddress;
        ImageView mAmImage;
        ImageView mPmImage;
    }

    /**
     * 把时间格式为HH:mm:ss转化为HH:mm
     * @param time
     * @return HH:mm格式的时间
     */
    private String getTime(String time) {
        String[] times = time.split(":");
        int size = times.length;
        if (size >= 3) {
            return times[0] + ":" +times[1];
        } else {
            return time;
        }
    }
}
