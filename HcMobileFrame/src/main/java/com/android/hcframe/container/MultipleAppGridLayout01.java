/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-24 下午3:16:13
*/
package com.android.hcframe.container;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.android.hcframe.DotView;
import com.android.hcframe.DraggableGridViewPager;
import com.android.hcframe.DraggableGridViewPager.OnPageChangeListener;
import com.android.hcframe.R;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.container.data.MultipleAppAdapter01;
import com.android.hcframe.container.data.ViewInfo;

/**
 * 多应用表格模版,一行4个应用,一页最到8个应用,操作8个需要分页,可以左右滑动
 */
public class MultipleAppGridLayout01 extends AbstractMultipleAppLayout implements OnPageChangeListener {

	protected int mColumn;
	
	protected int mRow;
	
	protected int mPageSize;
	
	private DotView mDotView;
	
	protected boolean mShowDivider;
	
	public MultipleAppGridLayout01() {
		super();
		mLayoutId = R.layout.container_multiple_grid_layout03;
		mColumn = 4;
		mRow = 2;
		mPageSize = mColumn * mRow;
		mShowDivider = false;
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
				DotView dotView = (DotView) layout.findViewById(R.id.container_grid03_dot_parent);
				if (size > mPageSize) {// 说明不止一页
//					int width = context.getResources().getDimensionPixelSize(R.dimen.container_grid_dot_width);
//					dotView.setDotWidth(width);
					dotView.setTotalItems((size + mPageSize - 1) / mPageSize);
				}
				mDotView = dotView;
				DraggableGridViewPager gridview = (DraggableGridViewPager) layout.findViewById(R.id.container_grid03_gridview);
				if (mShowDivider)
					gridview.setShowDivider(mShowDivider);
				int itemHeight = context.getResources().getDimensionPixelSize(R.dimen.container_multiple_grid03_item_height);
				int dotHeight = context.getResources().getDimensionPixelSize(R.dimen.container_grid_dot);
				LayoutParams params = layout.getLayoutParams();
				params.height = mRow * itemHeight;
				gridview.setLayoutParams(params);
				
				params = parent.getLayoutParams();
				params.height = mRow * itemHeight + dotHeight;
				layout.setLayoutParams(params);
				
				gridview.setRowCount(mRow);
				gridview.setColCount(mColumn);
				gridview.setAdapter(mAdapter);
				gridview.setOnItemClickListener(this);
				gridview.setOnPageChangeListener(this);
				
			}
			mParent = layout;
			
			return layout;
		}
		return null;
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int position) {
		// TODO Auto-generated method stub
		if (mDotView != null) {
			mDotView.setCurrentItem(position);
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRelease() {
		// TODO Auto-generated method stub
		mDotView = null;
		super.onRelease();
	}

	@Override
	public HcBaseAdapter<?> getAdapter(Context context, List<ViewInfo> infos) {
		// TODO Auto-generated method stub
		return new MultipleAppAdapter01(context, infos);
	}
	
	
}
