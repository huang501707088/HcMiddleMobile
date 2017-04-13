/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-3 上午11:04:56
*/
package com.android.hcframe.sys;

import android.text.TextUtils;

import com.android.hcframe.HcUtil;

public class SystemMessage {

	/** 消息具体内容,由具体的模块定义,可能是ID,也可能是具体的类名. */
	private String mContentId;
	
	/** 接收到消息的时间 */
	private String mDate;
	
	/** 消息的内容 */
	private String mContent;
	
	/** 消息类型 */
	private int mType;
	
	/** 消息标题 */
	private String mTitle;
	
	/** 消息所属应用ID */
	private String mAppId;
	
	/** 是否已读 */
	private boolean mReaded = false;
	/**
	 * 是否原生应用0:原生;1:html;默认为-1
	 */
	private int mAppType = -1;
	/**
	 * 每个模块首页的url或者className
	 */
	private String mIndexContent;

	/** 系统消息在数据库中的主键ID */
	private int mMessageId;

	private String mAppName;
	
	public void setContentId(String contentId) {
		mContentId = contentId;
	}
	
	public String getContentId() {
		return mContentId;
	}
	
	public void setTitle(String title) {
		mTitle = title;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public void setAppId(String appId) {
		mAppId = appId;
	}
	
	public String getAppId() {
		return mAppId;
	}
	
	public void setDate(String date) {
		mDate = date;
	}
	
	public void setDate() {
		mDate = HcUtil.getDate(HcUtil.FORMAT_YEAR_MONTH_DAY, System.currentTimeMillis());
	}
	
	public String getDate() {
		return mDate;
	}
	
	public void setContent(String content) {
		mContent = content;
	}
	
	public String getContent() {
		return mContent;
	}
	
	public void setType(int type) {
		mType = type;
	}
	
	public void setType(String type) {
		if (TextUtils.isEmpty(type)) return;
		mType = Integer.valueOf(type);
	}
	
	public int getType() {
		return mType;
	}
	
	public void setReaded(boolean read) {
		mReaded = read;
	}
	
	public boolean getReaded() {
		return mReaded;
	}

	public int getAppType() {
		return mAppType;
	}

	public void setAppType(int appType) {
		mAppType = appType;
	}

	public String getIndexContent() {
		return mIndexContent;
	}

	public void setIndexContent(String indexContent) {
		mIndexContent = indexContent;
	}

	public int getMessageId() {
		return mMessageId;
	}

	public void setMessageId(int messageId) {
		mMessageId = messageId;
	}

	public String getAppName() {
		return mAppName;
	}

	public void setAppName(String appName) {
		mAppName = appName;
	}
}