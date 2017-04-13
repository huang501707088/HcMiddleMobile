/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-12 上午9:44:40
*/
package com.android.hcframe.container.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用视图的数据信息
 * @author jrjin
 * @time 2015-11-12 上午9:44:54
 */
public class AppViewInfo extends ViewInfo {

	private static final String TAG = "AppViewInfo";
	
	/**
	 * 应用视图实例ID，由服务的创建，可能用于以后获取应用属性的值
	 */
	private String mViewInstanceId;
	
	/** 应用视图实例类型 
	 * 1：单个html应用；
	 * 0：单个内置原生服务；
	 * 2：多应用实例 
	 * @deprecated
	 * @see AppInfo
	 */
	private int mViewType;
	
	/** 进入应用 
	 * viewType = 1：应用主页url
	 * viewType = 0：应用className
	 * @deprecated
	 * @see AppInfo
	 */
	private String mViewAction;
	
	/** 模版中的应用列表，比如是多应用模版 AppInfo*/
	private List<ViewInfo> mInfos = new ArrayList<ViewInfo>();
	
	@Override
	public void addView(ViewInfo info) {
		// TODO Auto-generated method stub
		if (!mInfos.contains(info) && info instanceof AppInfo)
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

	@Override
	public void setViewInstanceId(String instanceId) {
		// TODO Auto-generated method stub
		mViewInstanceId = instanceId;
	}

	@Override
	public String getViewInstanceId() {
		// TODO Auto-generated method stub
		return mViewInstanceId;
	}

	@Override
	public void setViewType(int type) {
		// TODO Auto-generated method stub
		mViewType = type;
	}

	@Override
	public int getViewType() {
		// TODO Auto-generated method stub
		return mViewType;
	}

	@Override
	public void setViewAction(String action) {
		// TODO Auto-generated method stub
		mViewAction = action;
	}

	@Override
	public String getViewAction() {
		// TODO Auto-generated method stub
		return mViewAction;
	}
		
}
