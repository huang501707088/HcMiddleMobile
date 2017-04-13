package com.android.hcframe.settings.sys;

import java.io.File;
import java.util.Observable;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcObserver;
import com.android.hcframe.HcSubject;
import com.android.hcframe.R;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.push.PushSettingsActivity;
import com.android.hcframe.sql.DataCleanManager;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class SysSettingPage extends AbstractPage implements HcObserver {

	private RelativeLayout msg_receiver_settings_rl;

	private RelativeLayout clear_cache_rl;

	private RelativeLayout gesture_pwd_rl;

	private TextView clear_cache_value;

	private boolean isFirst = true;

	protected SysSettingPage(Activity context, ViewGroup group) {
		super(context, group);
	}

	@Override
	public void update(Observable observable, Object data) {

	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.msg_receiver_settings_rl) {
			mContext.startActivity(new Intent(mContext,
					PushSettingsActivity.class));
		} else if (id == R.id.clear_cache_rl) {
			clear_cache_value.setText(String.format(mContext
					.getString(R.string.clear_cach_data), DataCleanManager
					.cleanApplicationData(mContext, true,
							new File(Environment.getExternalStorageDirectory()
									+ "/hc/").getAbsolutePath(), StorageUtils
									.getCacheDirectory(mContext)
									.getAbsolutePath())));
			// 需要重新创建数据库
			
		} else if (id == R.id.gesture_pwd_rl) {
			
		}
	}

	@Override
	public void updateData(HcSubject subject, Object data,
			RequestCategory request, ResponseCategory response) {

	}

	@Override
	public void initialized() {
		if (isFirst) {
			isFirst = !isFirst;
			msg_receiver_settings_rl.setOnClickListener(this);
			clear_cache_rl.setOnClickListener(this);
			gesture_pwd_rl.setOnClickListener(this);
			clear_cache_value.setText(String.format(mContext
					.getString(R.string.clear_cach_data), DataCleanManager
					.cleanApplicationData(mContext, false,
							new File(Environment.getExternalStorageDirectory()
									+ "/hc/").getAbsolutePath(), StorageUtils
									.getCacheDirectory(mContext)
									.getAbsolutePath())));
		}
	}

	@Override
	public void setContentView() {
		if (mView == null) {
			mView = mInflater.inflate(R.layout.sys_settings, null);
			msg_receiver_settings_rl = (RelativeLayout) mView
					.findViewById(R.id.msg_receiver_settings_rl);
			clear_cache_rl = (RelativeLayout) mView
					.findViewById(R.id.clear_cache_rl);
			gesture_pwd_rl = (RelativeLayout) mView
					.findViewById(R.id.gesture_pwd_rl);
			clear_cache_value = (TextView) mView
					.findViewById(R.id.clear_cache_value);

		}
	}
}
