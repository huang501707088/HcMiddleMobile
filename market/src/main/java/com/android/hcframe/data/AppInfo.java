/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-5-1 上午10:58:17
*/
package com.android.hcframe.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;

import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;

import java.io.File;

public class AppInfo {

	private static final String TAG = "AppInfo";
	
	protected String mId;
	/** 应用类型：0：原生应用；1：Html5 */
	protected int mType;
	/** 应用图标的Url地址 */
	protected String mIcon;
	
	protected String mName;
	
	protected String mPackage;
	/** 应用分类(0：全部；1：数据统计；2：公众服务；3：稽查督报；4：移动办公) */
	protected int mCategory;
	/** Html应用的主页或者是原生应用的下载包地址 */
	protected String mUrl;
	/** 应用当前版本 */
	protected String mVersion;
	/** 0:未安装；1：已安装；2：更新 */
	protected int mState;
	/** 应用最新的版本 */
	protected String mLatestVersion;
	/** 原生应用的大小 */
	protected int mSize;
	/** 全部列表的排序 */
	protected int mAllOrder;
	/** 类别列表的排序 */
	protected int mCategoryOrder;

	protected boolean hasUsed;
	
	protected String mCategoryName;
	/**  应用在服务端的排序 */
	protected int mServerOrder;
	
	public AppInfo() {}
	
	public AppInfo(AppInfo info) {
		mCategory = info.mCategory;
		mIcon = info.mIcon;
		mId = info.mId;
		mName = info.mName;
		mPackage = info.mPackage;
		mType = info.mType;
		mUrl = info.mUrl;
		mVersion = info.mVersion;
		mLatestVersion = info.mLatestVersion;
		mState = info.mState;
		mSize = info.mSize;
		mAllOrder = info.mAllOrder;
		mCategoryOrder = info.mCategoryOrder;
	}
	
	public void setAppInfo(AppInfo info) {
		mCategory = info.mCategory;
		mIcon = info.mIcon;
		mId = info.mId;
		mName = info.mName;
		mPackage = info.mPackage;
		mType = info.mType;
		mUrl = info.mUrl;
		mVersion = info.mVersion;
		mLatestVersion = info.mLatestVersion;
		mState = info.mState;
		mSize = info.mSize;
		mAllOrder = info.mAllOrder;
		mCategoryOrder = info.mCategoryOrder;
	}
	
	public void setAppId(String id) {
		mId = id;
	}
	
	public String getAppId() {
		return mId;
	}
	
	public void setAppName(String name) {
		mName = name;
	}
	
	public String getAppName() {
		return mName;
	}
	
	public void setAppCategory(int category) {
		mCategory = category;
	}
	
	public int getAppCategory() {
		return mCategory;
	}
	
	public void setAppUrl(String url) {
//		if (HcLog.TOUBIAO) {
//			url = HcLog.touBiao(url);
//		}
		mUrl = url;
	}
	
	public String getAppUrl() {
		if (HcUtil.CHANDED) {
			mUrl = HcUtil.mappedUrl(mUrl);
		}
		return mUrl;
	}
	
	public void setAppPackage(String pkg) {
		mPackage = pkg;
	}
	
	public String getAppPackage() {
		return mPackage;
	}
	
	public void setAppVersion(String version) {
		mVersion = version;
	}
	
	public String getAppVersion() {
		return mVersion;
	}
	/**
	 * 图标的Url 
	 * @author jrjin
	 * @time 2015-5-28 下午4:49:16
	 * @param icon 应用图标的url
	 */
	public void setAppIcon(String icon) {
		mIcon = icon;
	}
	/**
	 * @deprecated
	 * @see #getAppIconUrl()
	 * @author jrjin
	 * @time 2015-5-28 下午4:50:29
	 * @return
	 */
	public Bitmap getAppIcon() {
		if (!TextUtils.isEmpty(mIcon)) {
			byte[] bytes = Base64.decode(mIcon, Base64.DEFAULT);
	        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
	        return bitmap;
		}
		return null;
	}
	
	public String getAppIconUrl() {
		return mIcon;
	}
	/**
	 * @deprecated
	 * @see #getAppIconUrl()
	 * @author jrjin
	 * @time 2015-11-28 下午4:53:11
	 * @return
	 */
	public String getAppBase64Icon() {
		return mIcon;
	}
	
	public void setAppType(int type) {
		mType = type;
	}
	
	public int getAppType() {
		return mType;
	}
	
	public void setAppState(int state) {
		mState = state;
	}
	
	public int getAppState() {
		return mState;
	}
	
//	public void setLatestVersion(String version) {
//		mLatestVersion = version;
//	}
//	
//	public String getLatestVersion() {
//		return mLatestVersion;
//	}	
	
	public void setAppSize(int size) {
		mSize = size;
	}
	
	public int getAppSize() {
		return mSize;
	}
	
	public void setAllOrder(int order) {
		mAllOrder = order;
	}
	
	public int getAllOrder() {
		return mAllOrder;
	}
	
	public void setCategoryOrder(int order) {
		mCategoryOrder = order;
	}
	
	public int getCategoryOrder() {
		return mCategoryOrder;
	}
	
	public void setUsed(int used) {
		hasUsed = used == 0 ? false : true;
	}
	
	public boolean hasUsed() {
		return hasUsed;
	}
	
	public void setCategoryName(String name) {
		mCategoryName = name;
	}

	public String getCategoryName() {
		return mCategoryName;
	}
	
	public void setServerOrder(int order) {
		mServerOrder = order;
	}
	
	public int getServerOrder() {
		return mServerOrder;
	}
	
	/**
	 * 启动应用
	 * @author jrjin
	 * @time 2015-11-28 下午4:33:24
	 */
	public void startApp(Context context) {
		throw new UnsupportedOperationException("AppInfo startApp! it is not supported operation!");
	}
	
	/**
	 * 更新应用
	 * @author jrjin
	 * @time 2015-11-28 下午4:33:32
	 */
	public void updateApp(Context context) {
		throw new UnsupportedOperationException("AppInfo updateApp! it is not supported operation!");
	}
	
	/**
	 * 获取应用图标
	 * @author jrjin
	 * @time 2015-11-28 下午4:47:38
	 * @return 应用的图标
	 */
	public final Bitmap getIcon() {
		Bitmap icon = HcAppData.getInstance().getIcon(mId, mVersion);
		HcLog.D(TAG + " #getIcon icon url = " + mIcon);
		if (icon == null && !TextUtils.isEmpty(mIcon)
				&& !TextUtils.isEmpty(mVersion)) {
			HcAppData.getInstance().downloadBitmap(mId, mIcon, mVersion);
		}
		return icon;
	}
	
	/**
	 * 下载原生应用
	 * @author jrjin
	 * @time 2015-11-28 下午8:34:14
	 * @param context
	 */
	public void downlaodApp(Context context) {
		throw new UnsupportedOperationException("AppInfo downlaodApp! it is not supported operation!");
	}

	public void installApk(File apk, Context context) {
		throw new UnsupportedOperationException("AppInfo installApk! it is not supported operation!");
	}
}
