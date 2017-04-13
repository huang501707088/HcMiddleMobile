/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-28 下午1:30:26
*/
package com.android.hcframe.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class HcBaseAdapter2<T> extends BaseAdapter {

	protected final LayoutInflater mInflater;
	protected final List<T> mInfos;
	protected Context mContext;
	
	public HcBaseAdapter2(Context context, List<T> infos) {
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		T data = getItem(position);
		ViewHolderBase<T> base;
		if (convertView == null) {
			base = createViewHolder();
			if (base != null) {
				convertView = base.createView(mInflater);
//				base.setItemData(position, data);
				convertView.setTag(base);
			} 
		} else {
			base = (ViewHolderBase<T>) convertView.getTag();
		}
		if (base != null) {
			base.setItemData(position, data);
		}
		return convertView;
	}

	/**
	 * 创建View Holder
	 * @author jrjin
	 * @time 2015-6-9 下午12:10:00
	 * @return View Holder
	 */
	public abstract ViewHolderBase<T> createViewHolder();
	
	@SuppressWarnings("需要测试")
	public final void releaseAdatper() {
		mInfos.clear();
		notifyDataSetChanged();
		mContext = null;
	}
}
