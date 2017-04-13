/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-24 下午1:35:35
*/
package com.android.hcframe.container;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;

import com.android.hcframe.R;
import com.android.hcframe.container.data.MultipleListAppAdapter01;
import com.android.hcframe.container.data.ViewInfo;

/**
 * 多应用列表模版
 */
public class MultipleAppListLayout01 extends AbstractMultipleAppLayout {

	private static final String TAG = "MultipleAppListLayout01";
	
	public MultipleAppListLayout01() {
		super();
		mLayoutId = R.layout.container_multiple_list_layout;
	}
	
	@Override
	public View createAppView(Context context, ViewGroup parent, ViewInfo info) {
		// TODO Auto-generated method stub
		mContext = context;
		if (mLayoutId != 0) {
			ListView layout = (ListView) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
					inflate(mLayoutId, parent, false);
			List<ViewInfo> apps = info.getViewInfos();
			if (!apps.isEmpty()) {
				if (mAdapter == null) {
					mAdapter = new MultipleListAppAdapter01(context, apps);
				}
				int size = apps.size();
				int itemHeight = context.getResources().getDimensionPixelSize(R.dimen.container_multiple_list_item_height);
				LayoutParams params = parent.getLayoutParams();
				params.height = size * itemHeight ;
				layout.setLayoutParams(params);
				layout.setAdapter(mAdapter);			
			}
			mParent = layout;
			layout.setOnItemClickListener(this);
			return layout;
		}
		return null;
	}

}
