/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-12 下午4:22:58
*/
package com.android.hcframe.container;

import com.android.hcframe.container.data.ElementInfo;
import com.android.hcframe.container.data.ViewInfo;

public interface ViewElement {

	/**
	 * 获取元素的自定义ID
	 * @author jrjin
	 * @time 2015-11-20 上午11:04:17
	 * @return 元素的自定义ID，是与服务的约定好的，并且在xml文件里配置进去
	 */
	public String getElementId();
	
	/**
	 * 给视图元素设置值
	 * @author jrjin
	 * @time 2015-11-20 上午11:05:15
	 * @param value 元素的值
	 * @param type 元素请求类型
	 * @param attrId 元素所对应的属性的ID
	 */
	public void setValue(String value, int type, String attrId);
	/**
	 * 给视图元素设置值
	 * @author jrjin
	 * @time 2015-11-20 上午11:00:53
	 * @param info AppInfo || {@link ElementInfo}
	 */
	public void setValue(ViewInfo info);

}
