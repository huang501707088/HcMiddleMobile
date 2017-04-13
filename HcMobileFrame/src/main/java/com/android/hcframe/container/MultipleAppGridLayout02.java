/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-23 下午2:26:45
*/
package com.android.hcframe.container;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridView;

import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.container.data.MultipleAppAdapter02;
import com.android.hcframe.container.data.ViewInfo;

/**
 * 多应用表格模版,一行2个应用,不分页显示,有多少应用就显示多少应用
 */
public class MultipleAppGridLayout02 extends AbstractMultipleAppLayout {
	
	private static final int COLUMNS = 2;
	
	public MultipleAppGridLayout02() {
		super();
		mLayoutId = R.layout.container_multiple_grid_layout02;
	}
	
	@Override
	public View createAppView(Context context, ViewGroup parent, ViewInfo info) {
		// TODO Auto-generated method stub
		mContext = context;
		if (mLayoutId != 0) {
			GridView layout = (GridView) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
					inflate(mLayoutId, parent, false);
			List<ViewInfo> apps = info.getViewInfos();
			if (!apps.isEmpty()) {
				if (mAdapter == null) {
					mAdapter = new MultipleAppAdapter02(context, apps);
				}
				int size = apps.size();
				int itemHeight = context.getResources().getDimensionPixelSize(R.dimen.container_multiple_grid02_item_height);
				LayoutParams params = parent.getLayoutParams();
				params.height = ((size + COLUMNS -1) / COLUMNS) * itemHeight ;
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
