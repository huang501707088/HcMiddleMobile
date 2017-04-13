/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-12 下午3:39:57
*/
package com.android.hcframe.container;

import com.android.hcframe.container.data.ViewInfo;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

/**
 * 应用视图基类
 * @author jrjin
 * @time 2015-11-12 下午3:42:11
 */
public abstract class AppViewLayout implements AppViewFactory, IHttpResponse {

	private static final String TAG = "AppViewLayout";
	
	/** 应用视图实例界面 */
	protected View mParent;
	
	/** 
	 * 当为单应用视图的时候，则为AppInfo
	 * 当为多应用视图的时候，则为AppViewInfo */
	protected ViewInfo mAppViewInfo;
	
	protected DisplayImageOptions mOptions;
	
	protected int mLayoutId;
	
	protected Context mContext;

	/** 应用容器的ID */
	protected String mParentId;
	
	public AppViewLayout() {
//		mOptions = new DisplayImageOptions.Builder()
//		.imageScaleType(ImageScaleType.EXACTLY)
//		.showImageForEmptyUri(R.drawable.oa_setting_icon)
//		.showImageOnFail(R.drawable.oa_setting_icon).cacheInMemory(true)
//		.cacheOnDisk(true).considerExifParams(true)
//		.bitmapConfig(Bitmap.Config.ARGB_8888).build();
	}
	
	/**
	 * 加载数据
	 * @author jrjin
	 * @time 2015-11-23 上午9:15:15
	 */
	public void onResume() {}
	
	public void onRelease() {
		if (mParent != null) {
			if (mParent instanceof AbsListView) {
				((AbsListView) mParent).removeAllViewsInLayout();			
			} else if (mParent instanceof ViewGroup) {
				((ViewGroup) mParent).removeAllViews();
			}
			
			mParent = null;
		}
		
		mContext = null;
		mAppViewInfo = null;
		mOptions = null;
	}

	@Override
	public void notify(Object data, RequestCategory request,
			ResponseCategory category) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
		// TODO Auto-generated method stub
		
	}

	public void setParentId(String parentId) {
		mParentId = parentId;
	}

	public String getParentId() {
		return mParentId;
	}
}
