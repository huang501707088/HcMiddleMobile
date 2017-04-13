/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-21 下午2:41:37
*/
package com.android.hcframe.update;

import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;

public class UpdateManager implements IHttpResponse {
	
	private boolean mAutomatic = true;
	
	@Override
	public void notify(Object data, RequestCategory request,
			ResponseCategory category) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
		// TODO Auto-generated method stub
		
	}

	
}
