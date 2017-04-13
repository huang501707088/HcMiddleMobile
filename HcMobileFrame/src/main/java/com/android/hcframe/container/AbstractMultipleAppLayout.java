/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-23 下午2:55:52
*/
package com.android.hcframe.container;

import java.util.List;

import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.badge.BadgeCache;
import com.android.hcframe.container.data.ViewInfo;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public abstract class AbstractMultipleAppLayout extends AppViewLayout implements OnItemClickListener {

	protected HcBaseAdapter<?> mAdapter;
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		ViewInfo appInfo;
		if (parent != null) {
			appInfo = (ViewInfo) parent.getItemAtPosition(position);
			BadgeCache.getInstance().operateBadge(appInfo.getContainerId() + "_" +appInfo.getAppId());
			if (mContext != null)
				appInfo.onClick(mContext, ViewInfo.CLICK_TYPE_APP);
		} else {
			if (mAppViewInfo != null && mContext != null) {
				appInfo = mAppViewInfo.getViewInfo(position);
				BadgeCache.getInstance().operateBadge(appInfo.getContainerId() + "_" +appInfo.getAppId());
				appInfo.onClick(mContext, ViewInfo.CLICK_TYPE_APP);
			}
		}
		
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}

	public HcBaseAdapter<?> getAdapter(Context context, List<ViewInfo> infos) {
		return null;
	}

	@Override
	public void onRelease() {
		// TODO Auto-generated method stub
		if (mAdapter != null) {
			mAdapter.releaseAdatper();
			mAdapter = null;
		}
		super.onRelease();
	}

}
