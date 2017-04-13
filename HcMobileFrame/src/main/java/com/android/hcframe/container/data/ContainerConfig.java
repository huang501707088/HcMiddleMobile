/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-11 下午3:55:26
*/
package com.android.hcframe.container.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.android.hcframe.HcLog;

import android.content.Context;

public class ContainerConfig {

	private static final String TAG = "ContainerConfig";
	
	private static final ContainerConfig CONTAINER = new ContainerConfig();
	
	/** 应用容器数据信息 */
	private List<ViewInfo> mViewInfos = new ArrayList<ViewInfo>();
	
	/**
	 * 应用视图模版
	 * key：视图模版ID
	 * value: 视图模版布局类名
	 * */
	private Map<String, String> mLayouts = new HashMap<String, String>();
	
	private ContainerConfig() {}
	
	public static ContainerConfig getInstance() {
		return CONTAINER;
	}
	
	public void parseConfig(Context context) {
		String layouts = readConfig(context, "layout.json");
		parseLayout(layouts);
	}
	
	/**
	 * 判断对应的key是否存在value
	 * 
	 * @param object
	 * @param tag
	 * @return true:有数据；false：没有数据
	 */
	private boolean hasValue(JSONObject object, String tag) {
		boolean exist = false;
		if (object != null && object.has(tag)) {
			try {
				Object object2 = object.get(tag);
				// LOG.D(TAG + " object2 = "+object2 + " tag = "+tag);
				if (object2 != null && !object2.equals("")
						&& !object.isNull(tag)) {
					exist = true;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}

		}
		return exist;
	}
	
	/**
	 * 读取json格式的文件，转化成json格式的字符串
	 * @author jrjin
	 * @time 2015-11-12 上午11:34:59
	 * @param context
	 * @param name 需要读取的文件名
	 * @return json格式的字符串
	 */
	private String readConfig(Context context, String name) {
		StringBuilder builder = new StringBuilder();
		InputStream is = null;
		BufferedReader bufferedReader = null;
		try {
			is = context.getAssets().open(name);
			bufferedReader = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				builder.append(line);
			}

		} catch (Exception e) {
			// TODO: handle exception
			HcLog.D(TAG + " error = " + e);
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (Exception e2) {
					// TODO: handle exception
				}

			}
			if (is != null) {
				try {
					is.close();
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}
		}
		HcLog.D(TAG + " parseConfig json data = " + builder.toString());
		return builder.toString();
	}
	
	private void parseLayout(String data) {
		try {
			JSONObject object = new JSONObject(data);
			if (hasValue(object, "layouts")) {
				JSONArray array = object.getJSONArray("layouts");
				int size = array.length();
				String id;
				String name;
				for (int i = 0; i < size; i++) {
					id = null;
					name = null;
					object = array.getJSONObject(i);
					if (hasValue(object, "id")) {
						id = object.getString("id");
					}
					if (hasValue(object, "className")) {
						name = object.getString("className");
					}
					if (id != null && name != null) {
						mLayouts.put(id, name);
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			HcLog.D(TAG + " parseLayout error = "+e);
		}
	}
	
	/**
	 * 
	 * @author jrjin
	 * @time 2015-11-16 下午3:28:58
	 * @param data
	 * @deprecated
	 */
	private void parseContainer(String data) {
		try {
			JSONObject object = new JSONObject(data);			
			if (hasValue(object, "appContainers")) {
				JSONArray containers = object.getJSONArray("appContainers");
				int size = containers.length();
				ViewInfo containerInfo;
				for (int i = 0; i < size; i++) {
					containerInfo = new ContainerInfo();
					object = containers.getJSONObject(i);
					if (hasValue(object, "containerId")) {
						containerInfo.setViewId(object.getString("containerId"));
					} else { // 出错
						containerInfo.setViewId("-1");
					}
					
					/**  解析视图   */
					if (hasValue(object, "viewList")) {
						JSONArray layouts = object.getJSONArray("viewList");
						int layoutSize = layouts.length();
						ViewInfo layoutInfo;
						for (int j = 0; j < layoutSize; j++) {
							layoutInfo = new AppViewInfo();
							object = layouts.getJSONObject(j);
							if (hasValue(object, "viewId")) { // 实例ID
								layoutInfo.setViewInstanceId(object.getString("viewId"));
							}
							if (hasValue(object, "templateId")) { // 视图ID 
								layoutInfo.setViewId(object.getString("templateId"));
							}
							if (hasValue(object, "viewType")) {
								layoutInfo.setViewType(Integer.valueOf(object.getInt("viewType")));
							}
							if (hasValue(object, "viewAction")) {
								layoutInfo.setViewAction(object.getString("viewAction"));
							}
							
							/** 解析视图元素 */
							if (hasValue(object, "elements")) {
								JSONArray elements = object.getJSONArray("elements");
								int eleSize = elements.length();
								ViewInfo eleInfo;
								for (int k = 0; k < eleSize; k++) {
									object = elements.getJSONObject(k);
									eleInfo = new ElementInfo();
									if (hasValue(object, "elementId")) {
										eleInfo.setViewId(object.getString("elementId"));									
									}
									if (hasValue(object, "value")) {
										eleInfo.setElementValue(object.getString("value"));
									}
									if (hasValue(object, "action")) {
										eleInfo.setViewAction(object.getString("action"));
									}
									if (hasValue(object, "attrId")) {
										eleInfo.setAttrId(object.getString("attrId"));
									}
									if (hasValue(object, "dynamic")) {
										eleInfo.setRequestType(Integer.valueOf(object.getInt("dynamic")));
									}
									
									// 一个元素解析完毕，添加到视图中
									layoutInfo.addView(eleInfo);
								}
							}
							
							// 一个视图解析完毕，添加到容器中
							containerInfo.addView(layoutInfo);
						}
					}
					
					// 一个容器解析结束，添加到列表中
					mViewInfos.add(containerInfo);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			HcLog.D(TAG + " parseContainer error = "+e);
		}
	}
	
	private class Layout {
		/** 应用视图模版ID */
		String mId;
		/** 应用视图模版布局类名 */
		String mName;
	}
	
	/**
	 * 获取应用视图布局类名
	 * @author jrjin
	 * @time 2015-11-12 下午1:39:54
	 * @param layoutId 应用视图布局ID
	 * @return 应用视图布局类名
	 */
	public String getLayoutName(String layoutId) {
		return mLayouts.get(layoutId);
	}
	
	/**
	 * 获取应用容器信息
	 * @author jrjin
	 * @time 2015-11-16 下午3:08:21
	 * @param context
	 * @param containerId 应用容器ID
	 * @return 制定的应用容器信息 ContainerInfo
	 * @see ContainerInfo
	 */
	public ViewInfo getContainerInfo(Context context, String containerId) {
		String containerString = readConfig(context, containerId + ".json");
		HcLog.D(TAG + " getContainerInfo json data = "+containerString);
		try {
			JSONObject object = new JSONObject(containerString).getJSONObject("appContainer");
			ViewInfo containerInfo = new ContainerInfo();
			containerInfo.setContainerId(containerId);
			if (hasValue(object, "containerId")) {
				containerInfo.setViewId(object.getString("containerId"));
			} else { // 出错
				containerInfo.setViewId(containerId);
			}
			/**  解析视图   */
			if (hasValue(object, "viewList")) {
				JSONArray layouts = object.getJSONArray("viewList");
				int layoutSize = layouts.length();
				ViewInfo layoutInfo;
				for (int i = 0; i < layoutSize; i++) {
					layoutInfo = new AppViewInfo();
					layoutInfo.setContainerId(containerId);
					object = layouts.getJSONObject(i);
					if (hasValue(object, "viewId")) { // 实例ID
						layoutInfo.setViewInstanceId(object.getString("viewId"));
					}
					if (hasValue(object, "templateId")) { // 视图ID 
						layoutInfo.setViewId(object.getString("templateId"));
					}
					
					/** 解析APP列表 */
					if (hasValue(object, "appList")) {
						JSONArray apps = object.getJSONArray("appList");
						int appSize = apps.length();
						ViewInfo appInfo;
						for (int j = 0; j < appSize; j++) {
							object = apps.getJSONObject(j);
							appInfo = new AppInfo();
							appInfo.setContainerId(containerId);
							if (hasValue(object, "appType")) {
								appInfo.setViewType(Integer.valueOf(object.getInt("appType")));
							}
							if (hasValue(object, "androidAppAction")) {
								appInfo.setViewAction(object.getString("androidAppAction"));
							}
							if (hasValue(object, "appId")) {
								appInfo.setAppId(object.getString("appId"));
							}
							if (hasValue(object, "appName")) {
								appInfo.setAppName(object.getString("appName"));
							}
							
							/** 解析视图元素 */
							if (hasValue(object, "elements")) {
								JSONArray elements = object.getJSONArray("elements");
								int eleSize = elements.length();
//								HcLog.D(TAG + " getContainerInfo element size = "+eleSize);
								ViewInfo eleInfo;
								for (int k = 0; k < eleSize; k++) {
									object = elements.getJSONObject(k);
									eleInfo = new ElementInfo();
									eleInfo.setContainerId(containerId);
									if (hasValue(object, "elementId")) {
										eleInfo.setViewId(object.getString("elementId"));									
									}
									if (hasValue(object, "value")) {
										eleInfo.setElementValue(object.getString("value"));
									}
									if (hasValue(object, "androidAction")) {
										eleInfo.setViewAction(object.getString("androidAction"));
									}
									if (hasValue(object, "attrId")) {
										eleInfo.setAttrId(object.getString("attrId"));
									}
									if (hasValue(object, "dynamic")) {
										eleInfo.setRequestType(Integer.valueOf(object.getInt("dynamic")));
									}
									
									// 一个元素解析完毕，添加到视图中
									appInfo.addView(eleInfo);
								}
							}
//							HcLog.D(TAG + " getContainerInfo element size = " +appInfo.getViewInfos().size());
							// 一个App解析完成
							layoutInfo.addView(appInfo);
						}
					}
					
					// 一个视图解析完毕，添加到容器中
					containerInfo.addView(layoutInfo);
				}

			}
			return containerInfo;
		} catch (Exception e) {
			// TODO: handle exception
			HcLog.D(TAG + " parseContainer error = "+e);
		}
		return null;
	}
}
