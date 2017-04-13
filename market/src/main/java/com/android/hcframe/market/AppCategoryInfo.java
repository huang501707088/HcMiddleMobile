/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-5-21 下午2:37:54
*/
package com.android.hcframe.market;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.android.hcframe.data.AppInfo;

public class AppCategoryInfo {

	/**
	 * 应用类型名
	 */
	private String mName;
	
	/**
	 * 应用类型标签
	 */
	private int mTag = -1;
	
	private List<AppInfo> mInfos = new ArrayList<AppInfo>();
	
	
	public void setCategoryName(String name) {
		mName = name;
	}
	
	public String getCategoryName() {
		return mName;
	}
	
	public void setCategoryTag(int tag) {
		mTag = tag;
	}
	
	public int getCategoryTag() {
		return mTag;
	}
	
	public void addAppInfo(AppInfo info) {
		if (!mInfos.contains(info)) {
			mInfos.add(info);
		}
	}
	/**
	 * 删除原先的，添加新的应用信息
	 * @author jrjin
	 * @time 2015-11-28 下午4:28:48
	 * @param infos
	 */
	public void addAppList(Collection<AppInfo> infos) {
		mInfos.clear();
		mInfos.addAll(infos);
	}
	
	public List<AppInfo> getApps() {
		return mInfos;
	}
}
