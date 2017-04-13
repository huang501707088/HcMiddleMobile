/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-23 上午10:07:52
*/
package com.android.hcframe.container;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.android.hcframe.HcConfig;
import com.android.hcframe.HcLog;
import com.android.hcframe.badge.BadgeCache;
import com.android.hcframe.badge.BadgeObserver;
import com.android.hcframe.container.data.ViewInfo;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public abstract class AbstractSingleAppLayout extends AppViewLayout {


	@Override
	public View createAppView(final Context context, ViewGroup parent, ViewInfo info) {
		// TODO Auto-generated method stub
		if (mLayoutId != 0) {
			View layout = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
					inflate(mLayoutId, parent, false);
			
			List<ViewInfo> apps = info.getViewInfos();
			HcLog.D(" AbstractSingleAppLayout createAppView app size = "+apps.size());
			if (!apps.isEmpty()) {
				final ViewInfo app = apps.get(0); // 其实这里最多就一个
				mAppViewInfo = app;

				layout.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						BadgeCache.getInstance().operateBadge(app.getContainerId() + "_" + app.getAppId());
						app.onClick(context, ViewInfo.CLICK_TYPE_APP);
					}
				});
				
				/** 这里放在onResume里面，不管静态还是动态，都重心设置。
				ViewGroup group = (ViewGroup) layout;
				int count = group.getChildCount();
				View child;
				for (int i = 0; i < count; i++) {
					child = group.getChildAt(i);
					if (child instanceof ViewElement) {
						((ViewElement) child).setValue(app);
					}
				}
				*/
			}
			mParent = layout;
			return layout;
		}
		return null;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
//		HcLog.D("AbstractSingleAppLayout  onResume appViewInfo info = "+mAppViewInfo);
		if (null != mAppViewInfo && mParent instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) mParent;
			setValue(group);
			/**
			 * @date 2015-12-2 上午10:50:02
			int count = group.getChildCount();
			HcLog.D("AbstractSingleAppLayout  onResume child size = "+count);
			View child;
			for (int i = 0; i < count; i++) {
				child = group.getChildAt(i);
				if (child instanceof ViewElement) {
					((ViewElement) child).setValue(mAppViewInfo);
				} 
			}
			*/
		}
	}

	/**
	 * 设置View的属性值
	 * @author jrjin
	 * @time 2015-12-2 上午10:48:36
	 * @param group
	 */
	private void setValue(ViewGroup group) {
		int count = group.getChildCount();
		HcLog.D("AbstractSingleAppLayout  setValue child size = "+count);
		View child;
		for (int i = 0; i < count; i++) {
			child = group.getChildAt(i);
			if (child instanceof ViewElement) {
				((ViewElement) child).setValue(mAppViewInfo);
			} else if (child instanceof ViewGroup) {
				setValue((ViewGroup) child); 
			}
		}
	}

}
