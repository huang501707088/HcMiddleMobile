/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-3-13 下午4:12:11
*/
package com.android.hcframe;

import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;

public interface HcObserver {

	public void updateData(HcSubject subject, Object data, RequestCategory request, ResponseCategory response);
}
