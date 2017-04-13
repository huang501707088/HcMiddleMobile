/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-24 下午7:25:40
*/
package com.android.hcframe.container;

import java.util.List;

import android.content.Context;

import com.android.hcframe.R;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.container.data.MultipleAppAdapter03;
import com.android.hcframe.container.data.ViewInfo;

/**
 * 多应用表格模版,一行为3个应用,一页最到3个应用,操作3个需要分页,可以左右滑动
 */
public class MultipleAppGridLayout03 extends MultipleAppGridLayout01 {

	public MultipleAppGridLayout03() {
		super();
		mLayoutId = R.layout.container_multiple_grid_layout03;
		mColumn = 3;
		mRow = 1;
		mPageSize = mColumn * mRow;
		mShowDivider = true;
	}
	
	@Override
	public HcBaseAdapter<?> getAdapter(Context context, List<ViewInfo> infos) {
		// TODO Auto-generated method stub
		return new MultipleAppAdapter03(context, infos);
	}

	
}
