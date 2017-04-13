/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2013-11-26 下午1:21:43
*/
package com.android.hcframe.http;

import org.apache.http.client.HttpClient;

public interface IHttpClientFactory {

	HttpClient getHttpClient();
}
