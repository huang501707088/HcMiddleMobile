/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2013-11-26 下午1:21:43
*/
package com.android.hcframe.http;

public interface IHttpResponse {

	/**
	 * 
	 * @author jrjin
	 * @time 2014-9-21 下午10:48:35
	 * @param data 返回的数据
	 * @param request 请求的类型
	 * @param category 返回的类型
	 */
	void notify(Object data,RequestCategory request, ResponseCategory category);
	/**
	 * 请求前保存url，以便主动取消请求或者关闭页面的时候取消请求
	 * 图片请求可能不适用
	 * @author jrjin
	 * @time 2015-9-24 下午3:49:13
	 * @param request
	 * @param md5Url
	 */
	void notifyRequestMd5Url(RequestCategory request, String md5Url);
}
