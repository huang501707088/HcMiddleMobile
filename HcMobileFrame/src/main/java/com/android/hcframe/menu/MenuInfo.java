/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-4-27 下午2:07:25
*/
package com.android.hcframe.menu;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;

public class MenuInfo {

	private static final String TAG = "MenuInfo";
	
	/** 应用ID */
	private String mId;
	/** 应用名字即菜单标题 */
	private String mName;
	/** 应用默认图标 */
	private String mNormalIcon;
	/**应用选中时的图标 */
	private String mSelectIcon;
	/** 应用内容地址原生应用可为空 */
	private String mUrl;
	/** 是否显示菜单图标 */
	@Deprecated
	private boolean isShowIcon = true;
	/** 是否显示菜单标题 */
	@Deprecated
	private boolean isShowName = true;
	/** 是否为原生应用 */
	private boolean isCloud = false;
	/** 显示页面的Class,用于反射 */
	private String mClassName;
	
	private Drawable mNormal;
	
	private Drawable mSelect;
	
	private static final String RESOURCES_TYPE = "drawable";
	
	private StateListDrawable mListDrawable;
	
	public MenuInfo() {}
	
	public MenuInfo(MenuInfo info) {
		mId = info.mId;
		mName = info.mName;
		mSelectIcon = info.mSelectIcon;
		mNormalIcon = info.mNormalIcon;
		mUrl = info.mUrl;
		isShowIcon = info.isShowIcon;
		isShowName = info.isShowName;
		isCloud = info.isCloud;
		mClassName = info.mClassName;
	}
	
	public void setAppId(String id) {
		mId = id;
	} 
	
	public void setAppName(String name) {
		mName = name;
	}
	
	public void setAppNormalIcon(String icon) {
//		HcLog.D(TAG + " setAppNormalIcon icon = "+icon + " drawable = "+mNormal);
		mNormalIcon = icon;
		mNormal = HcApplication.getContext().getResources().getDrawable(
				HcApplication.getContext().getResources().getIdentifier(getIconName(mNormalIcon), RESOURCES_TYPE, /*HcUtil.PACKAGE_NAME*/HcApplication.getContext().getPackageName()));
//		HcLog.D(TAG + " setAppNormalIcon icon = "+icon + " drawable = "+mNormal);
	}
	
	public void setAppSelectIcon(String icon) {
		mSelectIcon = icon;
		mSelect = HcApplication.getContext().getResources().getDrawable(
				HcApplication.getContext().getResources().getIdentifier(getIconName(mSelectIcon), RESOURCES_TYPE, /*HcUtil.PACKAGE_NAME*/HcApplication.getContext().getPackageName()));
	}
	
	public void setAppIndexUrl(String url) {
		mUrl = url;
	}
	/**
	 * @deprecated 
	 * @see {@link HcConfig#getMenuIconVisibility()}
	 * @author jrjin
	 * @time 2015-7-9 下午4:16:27
	 * @param visibillity
	 */
	@Deprecated
	public void setIconVisibility(boolean visibillity) {
		isShowIcon = visibillity;
	}
	/**
	 * @deprecated 
	 * @see {@link HcConfig#getMenuTitleVisibility()}
	 * @author jrjin
	 * @time 2015-7-9 下午4:16:27
	 * @param visibillity
	 */
	@Deprecated
	public void setNameVisibility(boolean visibillity) {
		isShowName = visibillity;
	}
	
	public void setClouded(boolean cloud) {
		isCloud = cloud;
	}
	
	public String getAppId() {
		return mId;
	}
	
	public String getAppName() {
		return mName;
	}
	
	public String getNormalAppIcon() {
		return mNormalIcon;
	}
	
	public String getSelectAppIcon() {
		return mSelectIcon;
	}
	
	public String getAppUrl() {
		return mUrl;
	}
	/**
	 * @deprecated instead of {@link HcConfig#getMenuIconVisibility()}
	 * @author jrjin
	 * @time 2015-7-9 下午4:07:50
	 * @return
	 */
	@Deprecated
	public boolean getIconVisibility() {
		return isShowIcon;
	}
	/**
	 * @deprecated instead of {@link HcConfig#getMenuTitleVisibility()}
	 * @author jrjin
	 * @time 2015-7-9 下午4:09:06
	 * @return
	 */
	@Deprecated
	public boolean getNameVisibility() {
		return isShowName;
	}
	
	public boolean getClouded() {
		return isCloud;
	}
	
	public void setClassName(String className) {
		mClassName = className;
	}
	
	public String getClassName() {
		return mClassName;
	}
	
	public StateListDrawable getMenuIcon() {
		if (null == mListDrawable) {
			mListDrawable = new StateListDrawable();
			mListDrawable.addState(HcUtil.SELECTED_STATE_SET, mSelect);
			mListDrawable.addState(HcUtil.PRESSED_STATE_SET, mSelect);
			mListDrawable.addState(HcUtil.FOCUSED_WINDOW_FOCUSED_STATE_SET, mSelect);
			mListDrawable.addState(HcUtil.FOCUSED_WINDOW_UNFOCUSED_STATE_SET, mNormal);
			mListDrawable.addState(HcUtil.EMPTY_STATE_SET, mNormal);
		}
		return mListDrawable;
	}
	
	private String getIconName(String icon) {
		String name = icon;
		for (Category category : Category.values()) {
			if (name.endsWith(category.mCategory)) {
				name = name.replace(category.mCategory, "");
			}
			break;
		}
		return name;
	}
	
	private enum Category {
		PNG(".png"), PNG_9("9.png"), JPG(".jpg"), JPEG(".jpeg");
		
		private String mCategory;
		
		Category(String category) {
			mCategory = category;
		}
	}
}
