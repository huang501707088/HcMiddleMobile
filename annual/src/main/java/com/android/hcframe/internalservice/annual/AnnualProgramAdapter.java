/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2016-1-12 下午2:36:05
*/
package com.android.hcframe.internalservice.annual;

import android.content.Context;

import com.android.hcframe.adapter.AbsListViewBaseAdapter;
import com.android.hcframe.adapter.ViewHolderFactory;

import java.util.List;

public class AnnualProgramAdapter extends AbsListViewBaseAdapter<AnnualProgramInfo> {

	public AnnualProgramAdapter(Context context, List<AnnualProgramInfo> infos,
			ViewHolderFactory<AnnualProgramInfo> factory) {
		super(context, infos, factory);
		// TODO Auto-generated constructor stub
	}

}
