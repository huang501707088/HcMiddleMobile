/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-4-10 下午3:17:58
 */
package com.android.hcframe.adapter;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcLog;
import com.android.hcframe.R;
import com.android.hcframe.login.LoginActivity;
import com.android.hcframe.sql.SettingHelper;

public class LoginUserAdapter extends HcBaseAdapter<String> {

	public LoginUserAdapter(Context context, List<String> infos) {
		super(context, infos);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final String name = getItem(position);
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_login_user, parent,
					false);
			viewHolder = new ViewHolder();
			viewHolder.login_delete_user = (LinearLayout) convertView
					.findViewById(R.id.login_delete_user);
			viewHolder.login_record_user_tv = (TextView) convertView
					.findViewById(R.id.login_record_user_tv);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.login_record_user_tv.setText(name);
		viewHolder.login_delete_user.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				String users = SettingHelper.deleteUser(HcApplication.getContext(), name);
				HcLog.D("LoginUserAdapter onClick name = "+name + " user = "+users);
				String[] acounts = null;
				if (!TextUtils.isEmpty(users) ) {
					acounts = users.split("&");
				}
				mInfos.clear();
				if (acounts != null && acounts.length > 0) {
					mInfos.addAll(Arrays.asList(acounts));
				}
				notifyDataSetChanged();
				if (mInfos.size() == 0) {
					if (mContext instanceof LoginActivity) {
						((LoginActivity) mContext).hindListView();
					}
				}
			}
		});
		return convertView;
	}

	private class ViewHolder {

		TextView login_record_user_tv;

		LinearLayout login_delete_user;
	}
}
