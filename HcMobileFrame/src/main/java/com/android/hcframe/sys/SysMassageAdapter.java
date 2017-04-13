/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-3 下午1:59:08
*/
package com.android.hcframe.sys;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.adapter.HcBaseAdapter;

public class SysMassageAdapter extends HcBaseAdapter<SystemMessage> {

	public SysMassageAdapter(Context context, List<SystemMessage> infos) {
		super(context, infos);
		// TODO Auto-generated constructor stub
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		SystemMessage message = getItem(position);
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.system_message_item_layout, parent, false);
			holder.mContent = (TextView) convertView.findViewById(R.id.sys_message_item_content);
			holder.mDate = (TextView) convertView.findViewById(R.id.sys_message_item_date);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.mContent.setText(message.getContent());
//		holder.mDate.setText(message.getDate());
		holder.mDate.setText(HcUtil.getDate(HcUtil.FORMAT_YEAR_MONTH_DAY, Long.valueOf(message.getDate())));
//		if (message.getReaded()) { // 已读
//			holder.mContent.setTextColor(mContext.getResources().getColor(R.color.system_message_readed));
//		} else {
//			holder.mContent.setTextColor(mContext.getResources().getColor(R.color.system_message_unread));
//		}
		
		return convertView;
	}

	private class ViewHolder {
		
		TextView mContent;
		
		TextView mDate;
	}
}
