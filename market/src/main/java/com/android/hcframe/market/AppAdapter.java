/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-5-1 上午11:21:31
*/
package com.android.hcframe.market;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.data.AppInfo;
import com.android.hcframe.data.NativeAppInfo;

public class AppAdapter extends HcBaseAdapter<AppInfo> implements UpdateCallback {

	/**
	 * 记录原生应用的对应的ViewHolder，方便更新的时候显示
	 * key：应用ID
	 */
	private Map<String, ViewHolder> mAppsMap = new HashMap<String, ViewHolder>();
	
	public AppAdapter(Context context, List<AppInfo> infos) {
		super(context, infos);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final AppInfo info = getItem(position);
		ViewHolder mHolder;
		if (convertView == null) {
			mHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.item_market_app, parent, false);
			mHolder.mIcon = (ImageView) convertView.findViewById(R.id.market_app_icon);
			mHolder.mInstall = (ImageView) convertView.findViewById(R.id.market_new_app);
			mHolder.mName = (TextView) convertView.findViewById(R.id.market_app_name);
			mHolder.mParent = (RelativeLayout) convertView.findViewById(R.id.market_item_parent);
			mHolder.mUpdate = (TextView) convertView.findViewById(R.id.market_update_app_btn);
			mHolder.mProgressBar = (ProgressBar) convertView.findViewById(R.id.market_download_progress);
			convertView.setTag(mHolder);
		} else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		mAppsMap.put(info.getAppId(), mHolder);

		mHolder.mParent.setBackgroundColor(Color.TRANSPARENT);
		mHolder.mProgressBar.setVisibility(View.GONE);
		mHolder.mName.setText(info.getAppName());
		Bitmap icon = null;
		/**
		 * @date 2015-11-30 下午1:37:31
		 * @author jrjin
		 */
		icon = info.getIcon();
		/**
		 * @date 2015-11-30 下午1:38:11 替换为info.getIcon();
		if (mContext instanceof MenuBaseActivity) {
			icon = ((MenuBaseActivity) mContext).getIcon(info.getAppId(), info.getAppIconUrl(), info.getAppVersion());
		}
		*/
		if (icon != null) {
			mHolder.mIcon.setImageBitmap(icon);
		} else {
			mHolder.mIcon.setImageResource(R.drawable.app_icon_data);
		}

		if (!info.hasUsed()) {
			mHolder.mInstall.setVisibility(View.VISIBLE);

		} else {
			mHolder.mInstall.setVisibility(View.GONE);
		}
		if (info instanceof NativeAppInfo && info.getAppState() == HcUtil.APP_UPDATE) {
			HcLog.D("AppAdapter  nativeApp can be update!");
			mHolder.mUpdate.setVisibility(View.VISIBLE);
			mHolder.mUpdate.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
					/**
					 * @author jrjin
					 * @date 2015-11-30 下午1:39:40
					 */
					info.updateApp(mContext);
					/**
					 * @date 2015-11-30 下午1:39:11 替换为info.updateApp(mContext);
					if (mContext instanceof MenuBaseActivity) {
						((MenuBaseActivity) mContext).updateApp(info);
					}
					*/
				}
			});
		} else {
			mHolder.mUpdate.setVisibility(View.GONE);
		}
		
		return convertView;
	}

	private class ViewHolder {
		private RelativeLayout mParent;
		private ImageView mIcon;
		private TextView mName;
		private TextView mUpdate;
		private ImageView mInstall;
		private ProgressBar mProgressBar;
	}

	@Override
	public void onProgress(String appId, int current, int max) {
		// TODO Auto-generated method stub
		ViewHolder holder = mAppsMap.get(appId);
		if (holder != null) {
			if (max == -1) { // 下载完成
				holder.mProgressBar.setVisibility(View.GONE);
				
			} else if (max == Integer.MAX_VALUE) { // 下载失败
				holder.mProgressBar.setVisibility(View.GONE);
				HcUtil.showToast(mContext, "APP下载失败！");
				for (AppInfo info : mInfos) {
					if (info.getAppId().equals(appId)) {
						if (info.getAppState() == HcUtil.APP_UPDATE) {
							holder.mUpdate.setVisibility(View.VISIBLE);
						}
						break;
					}
				}
			} else {
				holder.mUpdate.setVisibility(View.GONE);
				holder.mProgressBar.setVisibility(View.VISIBLE);
				holder.mProgressBar.setMax(max);
				holder.mProgressBar.setProgress(current);
			}
			
		}
	}

	@Override
	public void setBitmap(String appId, Bitmap icon) {
		// TODO Auto-generated method stub
		if (icon == null) return;
		ViewHolder holder = mAppsMap.get(appId);
		HcLog.D(" setBitmap appId = "+appId + " icon = "+icon);
		if (holder != null) {
			holder.mIcon.setImageBitmap(icon);
		}
	}

	@Override
	public void notifyDataChanged() {
		// TODO Auto-generated method stub
		notifyDataSetChanged();
	}
}
