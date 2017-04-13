/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2013-11-26 下午1:26:06
*/
package com.android.hcframe.adapter;


import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

public abstract class HcBaseAdapter<T> extends BaseAdapter {

	protected final LayoutInflater mInflater;
	protected final List<T> mInfos;
	protected Context mContext;
	
	public HcBaseAdapter(Context context, List<T> infos) {
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mInfos = infos;
		mContext = context;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mInfos.size();
	}

	@Override
	public T getItem(int position) {
		// TODO Auto-generated method stub
		return mInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@SuppressWarnings("需要测试")
	public final void releaseAdatper() {
		mInfos.clear();
		notifyDataSetChanged();
		mContext = null;
	}
}
