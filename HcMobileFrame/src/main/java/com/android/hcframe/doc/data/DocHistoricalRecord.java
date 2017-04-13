/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-8-30 上午11:26:26
 */
package com.android.hcframe.doc.data;

public class DocHistoricalRecord extends DocFileInfo {

	private static final String TAG = "DocHistoricalRecord";

	private String mReadTime;

	private String username;

	private String mDate;

	public String getmDate() {
		return mDate;
	}

	public void setmDate(String mDate) {
		this.mDate = mDate;
	}

	public void setReadTime(String time) {
		mReadTime = time;
	}

	public String getReadTime() {
		return mReadTime;
	}

	@Override
	public void setFileUrl(String url) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException(TAG
				+ " #setFileUrl is not supported! url = " + url);
	}

	@Override
	public String getFileUrl() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException(TAG
				+ "#getFileUrl is not supported!");
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
