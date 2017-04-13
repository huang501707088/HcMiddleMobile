/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2016-1-19 下午6:16:39
*/
package com.android.hcframe.http;

public class ResponseCodeInfo {

	private int mCode;
	
	private String mMsg;

	private String mData;
	
	public ResponseCodeInfo(int code, String msg, String bodyData) {
		mCode = code;
		mMsg = msg;
		mData = bodyData;
	}



	public int getCode() {
		return mCode;
	}
	
	public String getMsg() {
		return mMsg;
	}

	public String getBodyData() {
		return mData;
	}
}
