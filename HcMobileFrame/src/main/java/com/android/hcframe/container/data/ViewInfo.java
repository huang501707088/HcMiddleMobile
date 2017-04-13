/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-11 下午10:10:59
*/
package com.android.hcframe.container.data;

import java.util.List;

import android.content.Context;

/**
 * 视图信息
 * @author jrjin
 * @time 2015-11-12 上午9:45:30
 */
public abstract class ViewInfo {

	private static final String TAG = "ViewInfo";
	
	/** 应用视图实例类型 单个html应用 */
	public static final int VIEW_TYPE_HTML = 1;
	/** 应用视图实例类型 单个内置原生服务 */
	public static final int VIEW_TYPE_SINGLE = 0;
	/** 应用视图实例类型 多应用实例 */
	public static final int VIEW_TYPE_MULTIPLE = 2;
	
	/** value获取方式 不需要获取 */
	public static final int VALUE_REQUEST_NONE = 0;
	/** value获取方式 本地没有需要获取 */
	public static final int VALUE_REQUEST_ONCE = 1;
	/** value获取方式 每次都要获取 */
	public static final int VALUE_REQUEST_EVERY = 2;
	
	/** 应用容器ID/应用视图ID/元素ID*/
	private String mId;

	/** 文字属性的点击类型 */
	public static final int CLICK_TYPE_TEXT = 0;
	/** 图片属性的点击类型 */
	public static final int CLICK_TYPE_IMAGE = 1;
	/** 点击类型_应用 */
	public static final int CLICK_TYPE_APP = 2;

	/** 应用容器应用的ID,用于角标 */
	private String mContainerId;

	/**
	 * 设置视图的ID
	 * @author jrjin
	 * @time 2015-11-12 上午9:59:32
	 * @param id
	 */
	public final void setViewId(String id) {
		mId = id;
	}
	
	/**
	 * 获取视图的ID
	 * @author jrjin
	 * @time 2015-11-12 上午9:59:48
	 * @return
	 */
	public final String getViewId() {
		return mId;
	}
	
	/**
	 * 添加应用视图到应用容器里面
	 * @author jrjin
	 * @time 2015-11-12 上午10:02:53
	 * @param info
	 */
	public void addView(ViewInfo info) {
		throw new UnsupportedOperationException(TAG + " it is in addView info = "+info);
	}
	
	/**
	 * 获取应用容器中指定位置的应用视图数据
	 * @author jrjin
	 * @time 2015-11-12 上午10:03:22
	 * @param index 在应用容器中的位置
	 * @return 应用容器中指定位置的应用视图数据
	 */
	public ViewInfo getViewInfo(int index) {
		throw new UnsupportedOperationException(TAG + " it is in getViewInfo index = "+index);
	}
	
	/**
	 * 获取应用容器中应用视图数据
	 * @author jrjin
	 * @time 2015-11-12 上午10:04:14
	 * @return 应用容器中应用视图数据
	 */
	public List<ViewInfo> getViewInfos() {
		throw new UnsupportedOperationException(TAG + " it is in getViewInfos!");
	}
	
	/**
	 * 设置应用视图实例ID，由服务的创建，可能用于以后获取应用属性的值
	 * @author jrjin
	 * @time 2015-11-12 上午10:37:11
	 * @param instanceId 应用容器视图实例ID(来自于服务端)
	 */
	public void setViewInstanceId(String instanceId) {
		throw new UnsupportedOperationException(TAG + " it is in setViewInstanceId instanceId = "+instanceId);
	}
	
	/**
	 * 获取应用容器实例ID 
	 * @author jrjin
	 * @time 2015-11-12 上午10:37:55
	 * @return 应用容器实例ID 
	 */
	public String getViewInstanceId() {
		throw new UnsupportedOperationException(TAG + " it is in getViewInstanceId!");
	}
	
	/**
	 * 设置应用视图类型 
	 * @author jrjin
	 * @time 2015-11-12 上午10:38:14
	 * @param type 0：单个html应用；1：单个内置原生服务；2：多应用实例
	 */
	public void setViewType(int type) {
		throw new UnsupportedOperationException(TAG + " it is in setViewType type = "+type);
	}
	
	/**
	 * 获取应用容器视图类型
	 * @author jrjin
	 * @time 2015-11-12 上午10:38:59
	 * @return 应用视图类型 type 0：单个html应用；1：单个内置原生服务；2：多应用实例
	 */
	public int getViewType() {
		throw new UnsupportedOperationException(TAG + " it is in getViewType!");
	}
	
	/**
	 * 设置视图或者元素点击事件
	 * @author jrjin
	 * @time 2015-11-12 上午10:39:40
	 * @param action 
	 */
	public void setViewAction(String action) {
		throw new UnsupportedOperationException(TAG + " it is in setViewAction action =" +action);
	}
	
	/**
	 * 获取视图或者元素点击事件
	 * @author jrjin
	 * @time 2015-11-12 上午10:40:24
	 * @return 视图或者元素点击事件 viewType = 0：应用主页url； viewType = 1：应用className
	 */
	public String getViewAction() {
		throw new UnsupportedOperationException(TAG + " it is in getViewAction!");
	}
	
	/**
	 * 设置应用视图元素的值
	 * @author jrjin
	 * @time 2015-11-12 上午11:03:24
	 * @param value
	 */
	public void setElementValue(String value) {
		throw new UnsupportedOperationException(TAG + " it is in setElementValue value = "+value);
	}
	
	/**
	 * 获取应用视图元素的值
	 * @author jrjin
	 * @time 2015-11-12 上午11:03:45
	 * @return 应用视图元素的值
	 */
	public String getElementValue() {
		throw new UnsupportedOperationException(TAG + " it is in getElementValue!");
	}
	
	/**
	 * 设置应用视图值的请求方式
	 * @author jrjin
	 * @time 2015-11-12 上午11:04:15
	 * @param request 应用视图值的请求方式
	 * @see #VALUE_REQUEST_ONCE
	 * @see #VALUE_REQUEST_NONE
	 * @see #VALUE_REQUEST_EVERY
	 */
	public void setRequestType(int request) {
		throw new UnsupportedOperationException(TAG + " it is in setRequestType reques = "+request);
	}
	
	/**
	 * 获取应用视图值的请求方式
	 * @author jrjin
	 * @time 2015-11-12 上午11:05:17
	 * @return 0:不需要获取,直接显示value；1：本地没有需要获取；2：每次都要获取
	 */
	public int getRequestType() {
		throw new UnsupportedOperationException(TAG + " it is in getRequestType!");
	}
	
	/**
	 * 设置应用属性的ID
	 * @author jrjin
	 * @time 2015-11-12 上午11:07:11
	 * @param attrId
	 */
	public void setAttrId(String attrId) {
		throw new UnsupportedOperationException(TAG + " it is in setAttrId attrId = "+attrId);
	}
	
	/**
	 * 获取应用属性的ID
	 * @author jrjin
	 * @time 2015-11-12 上午11:07:26
	 * @return 应用属性的ID
	 */
	public String getAttrId() {
		throw new UnsupportedOperationException(TAG + " it is in getAttrId!");
	}
	
	/**
	 * 应用点击事件或者元素点击事件
	 * <p></p>
	 * {@link AppInfo},{@link ElementInfo}
	 * @author jrjin
	 * @time 2015-11-20 上午9:04:39
	 * @param context Activity
	 * @param type 点击控件的类型,主要用于属性点击事件
	 */
	public void onClick(Context context, int type) {}
	
	/**
	 * 设置应用ID
	 * @author jrjin
	 * @time 2015-11-20 上午9:55:29
	 * @param appId
	 * @see AppInfo
	 */
	public void setAppId(String appId) {
		throw new UnsupportedOperationException(TAG + " it is in setAppId! appId = "+appId);
	}
	
	/**
	 * 获取应用ID
	 * @author jrjin
	 * @time 2015-11-20 上午9:55:32
	 * @return
	 * @see AppInfo
	 */
	public String getAppId() {
		throw new UnsupportedOperationException(TAG + " it is in getAppId!");
	}
	
	/**
	 * 设置应用名称
	 * @author jrjin
	 * @time 2015-11-20 上午9:55:36
	 * @param appName
	 * @see AppInfo
	 */
	public void setAppName(String appName) {
		throw new UnsupportedOperationException(TAG + " it is in setAppName! appName = "+appName);
	}
	
	/**
	 * 获取应用名称
	 * @author jrjin
	 * @time 2015-11-20 上午9:55:40
	 * @return 
	 * @see AppInfo
	 */
	public String getAppName() {
		throw new UnsupportedOperationException(TAG + " it is in getAppName!");
	}

	public final void setContainerId(String containerId) {
		mContainerId = containerId;
	}

	public final String getContainerId() {
		return mContainerId;
	}
}
