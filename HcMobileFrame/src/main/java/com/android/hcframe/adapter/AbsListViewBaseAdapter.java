/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-6-9 下午12:56:01
*/
package com.android.hcframe.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class AbsListViewBaseAdapter<T> extends BaseAdapter {

	protected final LayoutInflater mInflater;
	protected final List<T> mInfos;
	protected Context mContext;
	protected final ViewHolderFactory<T> mViewHolderFactory;
	
	public AbsListViewBaseAdapter(Context context, List<T> infos, ViewHolderFactory<T>	factory) {
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mInfos = infos;
		mContext = context;
		mViewHolderFactory = factory;
		if (mViewHolderFactory == null) {
			throw new NullPointerException("AbsListViewBaseAdapter ViewHolderFactory is null!");
		}
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
			base = mViewHolderFactory.createViewHolder();
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

	@SuppressWarnings("需要测试")
	public final void releaseAdatper() {
		mInfos.clear();
		notifyDataSetChanged();
		mContext = null;
	}
}
