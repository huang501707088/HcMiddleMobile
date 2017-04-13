/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2016-1-19 下午8:14:27
*/
package com.android.hcframe.http;

public class ResponseNewsInfo extends ResponseCodeInfo {

    private String mId;
	
    public ResponseNewsInfo(int code, String msg, String id) {
        super(code, msg, id);
        // TODO Auto-generated constructor stub
        mId = id;
    }

    public String getNewsId() {
        return mId;
    }
}
