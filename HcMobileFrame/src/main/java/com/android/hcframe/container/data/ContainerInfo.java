/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-12 上午9:44:17
*/
package com.android.hcframe.container.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用容器视图数据信息
 * @author jrjin
 * @time 2015-11-12 上午9:45:46
 */
public class ContainerInfo extends ViewInfo {

	private static final String TAG = "ContainerInfo";
	
	/** 应用视图模版列表 AppViewInfo*/
	private List<ViewInfo> mInfos = new ArrayList<ViewInfo>();
	
	@Override
	public void addView(ViewInfo info) {
		// TODO Auto-generated method stub
		if (!mInfos.contains(info) && info instanceof AppViewInfo)
			mInfos.add(info);
	}

	@Override
	public ViewInfo getViewInfo(int index) {
		// TODO Auto-generated method stub
		if (index < 0 || index >= mInfos.size()) return null;
		return mInfos.get(index);
	}

	@Override
	public List<ViewInfo> getViewInfos() {
		// TODO Auto-generated method stub
		return mInfos;
	}

	
}
