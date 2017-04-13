/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-3-13 下午4:12:59
*/
package com.android.hcframe;

import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;

public interface HcSubject {

	/**
	 * 添加一个观察者
	 * @author jrjin
	 * @time 2015-4-13 上午10:11:34
	 * @param o 
	 */
	public void addObserver(HcObserver o);
	/**
	 * 移除一个指定的观察者
	 * @author jrjin
	 * @time 2015-4-13 上午10:12:07
	 * @param o
	 */
	public void removeObserver(HcObserver o);
	/**
	 * 移除全部的观察者
	 * @author jrjin
	 * @time 2015-4-13 上午10:12:37
	 */
	public void removeAll();
	/**
	 * 通知全部的观察者
	 * @author jrjin
	 * @time 2015-4-13 上午10:12:51
	 * @param subject
	 * @param data
	 * @param request
	 * @param response
	 */
	public void notifyObservers(HcSubject subject, Object data, RequestCategory request, ResponseCategory response);
	public void notifyObservers();
	public void notifyObservers(Object data);
	public void notifyObserver(HcObserver o, Object data);
}
