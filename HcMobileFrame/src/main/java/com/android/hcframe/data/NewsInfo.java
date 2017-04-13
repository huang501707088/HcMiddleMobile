/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-6-10 下午3:26:37
*/
package com.android.hcframe.data;

import java.util.ArrayList;
import java.util.List;

public class NewsInfo {

	public String mId;
	
	public String mTitle;
	
	public String mIconUrl;
	
	public String mAddress;
	
	public String mDate;
	
	public String mContentUrl;
	/**
	 * 新闻内容类型：0在线编辑 1文本导入，2互联网链接，3图片新闻
	 */
	public String mContentType;
	
	public ArrayList<String> mImgs=new ArrayList<String>();
	
	public String newsSummary;
	/** 图片新闻图片张数 */
	public int mCount;
	
	public boolean mScroll = false;
	
}
