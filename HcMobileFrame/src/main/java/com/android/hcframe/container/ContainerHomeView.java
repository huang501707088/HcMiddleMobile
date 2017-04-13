/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-12 上午9:26:45
*/
package com.android.hcframe.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.badge.BadgeCache;
import com.android.hcframe.container.data.ContainerConfig;
import com.android.hcframe.container.data.ViewInfo;
import com.android.hcframe.menu.MenuInfo;
import com.android.hcframe.push.HcPushManager;
import com.android.hcframe.push.PushInfo;

public class ContainerHomeView extends AbstractPage {

	private static final String TAG = "ContainerHomeView";
	
	private final String mAppId;
	
	private LinearLayout mParent;
	
	/** 容器信息 */
	private ViewInfo mContainerInfo;
	
	private List<AppViewLayout> mLayouts = new ArrayList<AppViewLayout>();
	
	private static final int ADD_VIEW = 1;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case ADD_VIEW:
				addViews(mParent);
				for (AppViewLayout layout : mLayouts) {
					layout.onResume();
				}
				HcDialog.deleteProgressDialog();
				break;

			default:
				break;
			}
		}
		
		
	};
	
	protected ContainerHomeView(Activity context, ViewGroup group, String appId) {
		super(context, group);
		// TODO Auto-generated constructor stub
		mAppId = appId;
		mContainerInfo = ContainerConfig.getInstance().getContainerInfo(context, appId);
	}

	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialized() {
		// TODO Auto-generated method stub
		// 消息推送的跳转
		PushInfo info = HcPushManager.getInstance().getPushInfo();
		if (info != null) {
			MenuInfo menuInfo = info.getAppInfo();
			if (menuInfo != null) {
				if (menuInfo.getClouded() == true) {
					startHtmlActivity(menuInfo);
				} else {
					startNativeActivity(menuInfo);
				}
			} else { // 说明出错了
				HcPushManager.getInstance().setPushInfo(null);
			}
		}
	}

	@Override
	public void setContentView() {
		// TODO Auto-generated method stub
		if (mView == null) {
			mView = mInflater.inflate(R.layout.container_home_layout, null);
			mParent = (LinearLayout) mView.findViewById(R.id.container_home_parent);
//			addViews(mParent);
		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		// 获取动态数据，要是有登录界面的话，可能需要切换界面
		HcLog.D(TAG + " #onResume ============================start time = "+HcUtil.getDate(HcUtil.FORMAT_HOUR, System.currentTimeMillis()));
//		if (isFirst) {
//			isFirst = !isFirst;
//			addViews(mParent);
//		}
//		for (AppViewLayout layout : mLayouts) {
//			layout.onResume();
//		}
		
		if (isFirst) {
			isFirst = !isFirst;
			HcDialog.showProgressDialog(mContext, R.string.dialog_title_load_data);
			mHandler.sendEmptyMessageDelayed(ADD_VIEW, 30);
		} else {
			for (AppViewLayout layout : mLayouts) {
				layout.onResume();
			}
		}
		
		HcLog.D(TAG + " #onResume ========================end time = "+HcUtil.getDate(HcUtil.FORMAT_HOUR, System.currentTimeMillis()));
	}

	private void addViews(LinearLayout group) {
		HcLog.D(TAG + " #addViews =====================start time = "+HcUtil.getDate(HcUtil.FORMAT_HOUR, System.currentTimeMillis()));
		if (mContainerInfo == null) return;
		List<ViewInfo> infos = mContainerInfo.getViewInfos(); // 视图列表
		if (infos.isEmpty()) return;
		AppViewLayout layout;
		String className;
		String viewId;
		for (ViewInfo viewInfo : infos) {
			viewId = viewInfo.getViewId();
			HcLog.D(TAG + " addViews view id = "+viewId);
//			if (viewId.equals("multiple_grid_layout02") || viewId.equals("multiple_grid_layout01")
//					|| viewId.equals("multiple_grid_layout03") || viewId.equals("multiple_list_layout01"))
//				continue;
			className = ContainerConfig.getInstance().getLayoutName(viewId);
			layout = getAppView(className);
			if (layout != null) {
				View child = layout.createAppView(mContext, group, viewInfo);
				if (child != null) {
					mLayouts.add(layout);
					group.addView(child);
				}
				
			}
		}
		HcLog.D(TAG + " #addViews =================================end time = "+HcUtil.getDate(HcUtil.FORMAT_HOUR, System.currentTimeMillis()));
	}
	
	private AppViewLayout getAppView(String className) {
		try {
			Class<?> cl = Class.forName(className);
			AppViewLayout layout = (AppViewLayout) cl.newInstance();
			layout.setParentId(mAppId);
			return layout;
		} catch (ClassNotFoundException e) {
			// TODO: handle exception
			HcLog.D(TAG + " getAppView ClassNotFoundException e = "+e + " className = "+className);
		} catch (Exception e) {
			// TODO: handle exception
			HcLog.D(TAG + " getAppView Exception e = "+e);
		}
		return null;
	}

	@Override
	public void onDestory() {
		// TODO Auto-generated method stub
		// 删除角标的监听
		BadgeCache.getInstance().removeAllBadgeObserver(mAppId);
		super.onDestory();
		for (AppViewLayout layout : mLayouts) {
			layout.onRelease();
		}
		mLayouts.clear();
		mLayouts = null;
		mContainerInfo = null;
		mContext = null;
	}

	private void startHtmlActivity(MenuInfo info) {

		Intent intent = new Intent();
		intent.setClass(mContext, ContainerActivity.class);
		intent.putExtra("appId", info.getAppId());
		intent.putExtra("appName", info.getAppName());
		intent.putExtra("url", info.getAppUrl());
		intent.putExtra("className", "com.android.hcframe.menu.WebMenuPage");
		intent.putExtra("menu", false);
		mContext.startActivity(intent);
		mContext.overridePendingTransition(0, 0);
	}

	private void startNativeActivity(MenuInfo info) {
		Intent intent = new Intent(mContext, ContainerActivity.class);
		intent.putExtra("appId", info.getAppId());
		intent.putExtra("className", info.getClassName());
		intent.putExtra("appName", info.getAppName());
		intent.putExtra("menu", false);
		mContext.startActivity(intent);
		mContext.overridePendingTransition(0, 0);
	}
}
