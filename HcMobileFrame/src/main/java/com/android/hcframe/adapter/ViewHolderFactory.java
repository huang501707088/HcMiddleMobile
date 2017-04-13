/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-6-9 下午12:02:47
*/
package com.android.hcframe.adapter;
/**
 * View Holder的工厂
 * @author jrjin
 * @time 2015-6-9 下午12:09:12
 * @param <T>
 */
public interface ViewHolderFactory<T> {
	/**
	 * 创建View Holder
	 * @author jrjin
	 * @time 2015-6-9 下午12:10:00
	 * @return View Holder
	 */
	public ViewHolderBase<T> createViewHolder();
}
