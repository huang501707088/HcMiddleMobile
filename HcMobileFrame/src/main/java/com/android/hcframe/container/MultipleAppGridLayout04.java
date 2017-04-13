/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2016-2-15 下午1:52:47
*/
package com.android.hcframe.container;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.android.hcframe.DraggableGridViewPager;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.container.data.MultipleAppAdapter04;
import com.android.hcframe.container.data.ViewInfo;

/**
 * 多应用表格模版,为类似支付宝的九宫阁显示样式,一行4个应用,不分页显示,有多少应用就显示多少应用
 */
public class MultipleAppGridLayout04 extends AbstractMultipleAppLayout {

	private final int mColumn;
	
	private int mRow;
	
	private final boolean mShowDivider;
	
	public MultipleAppGridLayout04() {
		mLayoutId = R.layout.container_multiple_grid_layout04;
		mColumn = 4;
		mShowDivider = true;
	}
	
	@Override
	public View createAppView(Context context, ViewGroup parent, ViewInfo info) {
		// TODO Auto-generated method stub
		mContext = context;
		if (mLayoutId != 0) {
			ViewGroup layout = (ViewGroup) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
					inflate(mLayoutId, parent, false);
			List<ViewInfo> apps = info.getViewInfos();
			int size = apps.size();
			if (!apps.isEmpty()) {
				mAppViewInfo = info;
				if (mAdapter == null) {
					mAdapter = getAdapter(context, apps);
				}
				
				mRow = (size + mColumn - 1) / mColumn;
				
				DraggableGridViewPager gridview = (DraggableGridViewPager) layout;
				if (mShowDivider)
					gridview.setShowDividers(mShowDivider, mShowDivider);
				int itemHeight = HcUtil.getScreenWidth() / mColumn;
				
				LayoutParams params = layout.getLayoutParams();
				params.height = mRow * itemHeight;
				gridview.setLayoutParams(params);
				
//				params = parent.getLayoutParams();
//				params.height = mRow * itemHeight;
//				layout.setLayoutParams(params);
				
				gridview.setRowCount(mRow);
				gridview.setColCount(mColumn);
				gridview.setAdapter(mAdapter);
				gridview.setOnItemClickListener(this);
				
			}
			mParent = layout;
			
			return layout;
		}
		return null;
	}

	@Override
	public HcBaseAdapter<?> getAdapter(Context context, List<ViewInfo> infos) {
		// TODO Auto-generated method stub
		return new MultipleAppAdapter04(context, infos);
	}

}
