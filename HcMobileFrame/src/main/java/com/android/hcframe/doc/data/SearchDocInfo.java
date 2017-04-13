/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-8-27 下午4:24:19
*/
package com.android.hcframe.doc.data;

public class SearchDocInfo extends DocFileInfo {

	private static final String TAG = "SearchDocInfo";
	
	/** 栏目ID */
	private String mColumnId;
	
	public void setColumnId(String id) {
		mColumnId = id;
	}
	
	public String getColumnId() {
		return mColumnId;
	}
}
