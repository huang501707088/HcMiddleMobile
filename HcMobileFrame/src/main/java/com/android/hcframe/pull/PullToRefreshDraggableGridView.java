/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-5-7 下午3:43:04
*/
package com.android.hcframe.pull;

import com.android.hcframe.HcLog;
import com.android.hcframe.HcDraggableGridViewPager;
import com.android.hcframe.HcDraggableGridViewPager.OnPageChangeListener;
import com.android.hcframe.HcDraggableGridViewPager.OnRearrangeListener;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Adapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class PullToRefreshDraggableGridView extends PullToRefreshBase<HcDraggableGridViewPager> {

	public PullToRefreshDraggableGridView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public PullToRefreshDraggableGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public PullToRefreshDraggableGridView(Context context, PullToRefreshBase.Mode mode) {
		super(context, mode);
		// TODO Auto-generated constructor stub
	}

	public PullToRefreshDraggableGridView(Context context, PullToRefreshBase.Mode mode, PullToRefreshBase.AnimationStyle animStyle) {
		super(context, mode, animStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	public PullToRefreshBase.Orientation getPullToRefreshScrollDirection() {
		// TODO Auto-generated method stub
		return Orientation.VERTICAL;
	}

	@Override
	protected HcDraggableGridViewPager createRefreshableView(Context context,
			AttributeSet attrs) {
		// TODO Auto-generated method stub
		return new HcDraggableGridViewPager(context, attrs);
	}

	@Override
	protected boolean isReadyForPullEnd() {
		// TODO Auto-generated method stub
//		int scroll = mRefreshableView.getPageCount() * 
//				mRefreshableView.getRowCount() * mRefreshableView.getGridHeight();
//		HcLog.D("PullToRefreshDraggableGridView isReadyForPullEnd! scrollY =" + mRefreshableView.getScrollY() + " min scroll ="+scroll);		
//		return mRefreshableView.getScrollY() >= scroll;
		return mRefreshableView.getCurrentItem() == mRefreshableView.getPageCount() - 1;
	}

	@Override
	protected boolean isReadyForPullStart() {
		// TODO Auto-generated method stub
		HcLog.D("PullToRefreshDraggableGridView isReadyForPullStart! scrollY =" + mRefreshableView.getScrollY());
		return mRefreshableView.getScrollY() == 0;
	}

	public void setAdapter(Adapter adapter) {
		mRefreshableView.setAdapter(adapter);
	}
	
	public void setOnPageChangeListener(OnPageChangeListener listener) {
		mRefreshableView.setOnPageChangeListener(listener);
	}
	
	public void setOnItemClickListener(OnItemClickListener listener) {
		mRefreshableView.setOnItemClickListener(listener);
	}
	
	public void setOnItemLongClickListener(OnItemLongClickListener listener) {
		mRefreshableView.setOnItemLongClickListener(listener);
	}
	
	public void setOnRearrangeListener(OnRearrangeListener listener) {
		mRefreshableView.setOnRearrangeListener(listener);
	}
}
