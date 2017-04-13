/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2013-11-26 下午6:16:55
*/
package com.android.hcframe.http;

/**
 * <p>0:返回成功</p>
 * <p>1:帐号无效</p>
 * <p>2:认证失败</p>
 * <p>3:帐号已存在</p>
 * <p>4:会话超时</p>
 * <p>5:用户未登录</p>
 * <p>7:任务已关闭</p>
 * <p>8:重复赞</p>
 * <p>99:系统错误</p>
 * @author jrjin
 *
 */
public enum ResponseCategory {

	/////////////////
	////有返回结果/////
	SUCCESS,// 返回成功
	ACCOUNT_INVALID, // 帐号无效(帐号不存在或者已经被禁用)
	AUTHENTICATION_FAILED,// 认证失败
	AUCCONT_EXISTS,// 帐号已存在(注册帐号时)
	SESSION_TIMEOUT,// 会话超时
	USER_NOT_LOGIN, // 用户未登录
	NOT_MATCH,// 东西不匹配
	SYSTEM_ERROR, // 系统错误
	DATA_IS_NULL, // 需要获取的数据为空
	SCANLOGIN_INVALID, // 帐号失效（扫描二维码登录）
	/**
	 * 数据错误(Json数据出错)
	 */
	DATA_ERROR,
	/**
	 * 网络中断(连续请求3次失败)
	 */
	NETWORK_ERROR,
	/**
	 * 请求返回其他code码
	 */
	REQUEST_FAILED
}
