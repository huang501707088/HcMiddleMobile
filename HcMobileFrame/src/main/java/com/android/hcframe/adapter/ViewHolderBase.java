/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-6-9 上午10:49:51
*/
package com.android.hcframe.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

/**
 * 在{@link BaseAdapter#getView(int, View, android.view.ViewGroup)}中使用，
 * 使{@link AbsListView}的可以复用Item.
 * @author jrjin
 * @time 2015-6-9 上午10:55:01
 * @param <T>
 */
public interface ViewHolderBase<T> {

	/**
	 * 创建{@link AbsListView}的子View
	 * @author jrjin
	 * @time 2015-6-9 上午10:59:04
	 * @param inflater
	 * @return contentView
	 */
	public View createView(LayoutInflater inflater);
	/**
	 * 设置{@link AbsListView}的子View内容
	 * @author jrjin
	 * @time 2015-6-9 上午11:00:10
	 * @param position 位置
	 * @param data 子View需要设置的数据
	 */
	public void setItemData(int position, T data);
}
