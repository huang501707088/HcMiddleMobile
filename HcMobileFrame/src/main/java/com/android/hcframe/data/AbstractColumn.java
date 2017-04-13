/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-8-27 上午11:53:21
*/
package com.android.hcframe.data;

public abstract class AbstractColumn {

	/**
	 * 栏目编号
	 */
	private String mId;

	/**
	 * 栏目类型：0：图片+文字 1：图片 2：文字
	 */
	private int mType;

	/**
	 * 内容类型：0：在线编辑 1：文本导入，2：互联网链接
	 * @deprecated
	 */
	private int mContenttype;

	/**
	 * 栏目名
	 */
	private String mName;

	/**
	 * 上传滚动图片:0:否1是
	 */
	private int isSrolltopic;
	
	public static final int UNSCROLLING = 0;
	public static final int SCROLLING = 1;
	
	public static final int COLUMN_TYPE_IMAGE_AND_TEXT = 0;
	public static final int COLUMN_TYPE_IAMGE = 1;
	public static final int COLUMN_TYPE_TEXT = 2;
	
	public static final int COLUMN_CONTENT_TYPE_EDIT = 0;
	public static final int COLUMN_CONTENT_TYPE_TEXT = 1;
	public static final int COLUMN_CONTENT_TYPE_WWW = 2;

	public String getNewsId() {
		return mId;
	}

	public void setNewsId(String newsId) {
		mId = newsId;
	}

	public int getmType() {
		return mType;
	}

	public void setmType(int mType) {
		this.mType = mType;
	}

	public int getmContenttype() {
		return mContenttype;
	}

	public void setmContenttype(int mContenttype) {
		this.mContenttype = mContenttype;
	}

	public String getmName() {
		return mName;
	}

	public void setmName(String mName) {
		this.mName = mName;
	}

	public int getIsSrolltopic() {
		return isSrolltopic;
	}

	public void setIsSrolltopic(int isSrolltopic) {
		this.isSrolltopic = isSrolltopic;
	}
	
	
}
