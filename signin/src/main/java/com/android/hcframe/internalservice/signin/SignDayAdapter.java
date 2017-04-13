/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2016-1-5 下午6:35:21
*/
package com.android.hcframe.internalservice.signin;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.hcframe.HcUtil;
import com.android.hcframe.adapter.HcBaseAdapter;

public class SignDayAdapter extends HcBaseAdapter<SignDayInfo> {

	public SignDayAdapter(Context context, List<SignDayInfo> infos) {
		super(context, infos);
		// TODO Auto-generated constructor stub
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder = null;
		SignDayInfo signItem = getItem(position);
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.sign_list_item_layout, null);
			viewHolder.signin_time_tv = (TextView) convertView
					.findViewById(R.id.sign_list_item_signin);
			viewHolder.signout_time_tv = (TextView) convertView
					.findViewById(R.id.sign_list_item_signout);
			viewHolder.sign_date_tv = (TextView) convertView
					.findViewById(R.id.sign_list_item_date);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		if (HcUtil.isEmpty(signItem.mSignInTime)) {
			viewHolder.signin_time_tv.setText(mContext
					.getString(R.string.none_sign_text));
			viewHolder.signin_time_tv.setTextColor(mContext.getResources().getColor(R.color.sign_list_item_text));
		} else {
			viewHolder.signin_time_tv.setText(signItem.mSignInTime.substring(0, signItem.mSignInTime.lastIndexOf(":")));
			if (signItem.mSignInTime.compareToIgnoreCase(signItem.mWorkInTime) > 0) {
				viewHolder.signin_time_tv.setTextColor(Color.RED);
			} else {
				viewHolder.signin_time_tv.setTextColor(mContext.getResources().getColor(R.color.sign_list_item_text));
			}
		}

		if (HcUtil.isEmpty(signItem.mSignOutTime)) {
			viewHolder.signout_time_tv.setText(mContext
					.getString(R.string.none_sign_text));
			viewHolder.signin_time_tv.setTextColor(mContext.getResources().getColor(R.color.sign_list_item_text));
		} else {
			viewHolder.signout_time_tv.setText(signItem.mSignOutTime.substring(0, signItem.mSignOutTime.lastIndexOf(":")));
			if (signItem.mSignOutTime.compareToIgnoreCase(signItem.mWorkOutTime) < 0) {
				viewHolder.signout_time_tv.setTextColor(Color.RED);
			} else {
				viewHolder.signout_time_tv.setTextColor(mContext.getResources().getColor(R.color.sign_list_item_text));
			}
		}
		viewHolder.sign_date_tv.setText(HcUtil.changeDateFormat(
				signItem.mDate + " 00:00:00",
				HcUtil.FORMAT_POLLUTION, HcUtil.FORMAT_MONTH));
		return convertView;
	}

	private final class ViewHolder {

		public TextView signin_time_tv;

		public TextView signout_time_tv;

		public TextView sign_date_tv;
	}
}
