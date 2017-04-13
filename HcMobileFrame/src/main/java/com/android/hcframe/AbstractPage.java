/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2013-11-26 下午1:26:06
*/
package com.android.hcframe;

import java.util.Observer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

/**
 * 此类可以看作是每个页面或者某块区域的抽象，主要目的是把界面元素从Activity中剥离出，
 * 使代码更加清晰，易读；主要负责显示。
 */
public abstract class AbstractPage implements Observer,OnClickListener {

	private static final String TAG = "AbstractPage";
	
	/**
	 * 1、Group可以为空，此时当前页面没有切换页面的功能
	 * 2、Group不为空，通过ViewGroup的添加删除功能达到切换页面的功能，使一个Activity管理
	 * 好几个页面
	 */
	protected ViewGroup mGroup;
	/**
	 * 当前需要呈现的View
	 */
	protected View mView;

	protected Activity mContext;
	/**
	 * 主要用于{@code mView}的加载
	 */
	protected final LayoutInflater mInflater;
	/**
	 * 可以为空
	 * 当前页面的前一页，主要用于返回事件中
	 */
	private AbstractPage mPreviousPage;
	
	/**
	 * 当mGroup为空时，用来判断加载的条件，只需第一次加载。
	 * 可以用mView是否为空代替
	 */
	protected boolean isFirst = true; 
	
	
	protected AbstractPage(Activity context,ViewGroup group) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContext = context;
		mGroup = group;
	}
	
	public void setPreviousPage(AbstractPage previousPage) {
		mPreviousPage = previousPage;
	}
	
	public AbstractPage getPreView() {
		return mPreviousPage;
	}
	
	/**
	 * 对页面数据进行初始化
	 * 
	 */
	public abstract void initialized();
	
	/**
	 * 对页面元素的初始化，包括监听事件的设置
	 * 注意：每个页面元素只需初始化一次，可以通过mView是否为空来判断
	 */
	public abstract void setContentView();
	
	/**
	 * @deprecated changed to {@link #onPause()} and {@link #onDestory()}
	 * 撤销当前页面的某些操作，比如停止计时器的计时
	 * 可以在{@code Activity#onPause}或者{@code Activity#onDestroy}方法中调用
	 */
	public void revoked() {
		
	}
	
	private void removeView() {
		HcLog.D(TAG + " it is in removeView view group = "+mGroup);
		if (mGroup != null) {
			mGroup.removeAllViews();
		}

	}
	
	private void addView() {
		if (mGroup != null && mView != null) {
			mGroup.addView(mView);
		}
	
	}
	
	/**
	 * 通过此方法实现页面的切换
	 */
	public void changePages() {
		setParameters();
		setContentView();
		removeView();
		addView();
		initialized();	
	}

	public View getContentView() {
//		HcLog.D(TAG + " getContentView = "+mView);
		return mView;
	}
	
	/**
	 * 为了可扩展设置页面里的内容
	 * @author jrjin
	 * @time 2015-5-25 上午11:40:35
	 * @param objects
	 */
	public void changePages(Object ... objects) {
		changePages();
	}
	
	/**
	 * 设置内容，如果使用，必须在{@code #changePages()}调用之前
	 * @author jrjin
	 * @time 2015-5-25 下午2:28:17
	 * @param parameters
	 */
	public void setParameters(Object ... parameters) {
//		throw new UnsupportedOperationException(" 需要继承！");
	}
	/**
	 * 设置初始化前的一些内容，本来可以通过构造函数来实现，但为了统一。
	 * @author jrjin
	 * @time 2015-5-26 上午9:23:44
	 */
	public void setParameters() {}
	
	/**
	 * 要是当前的Activity每次在onResume时需要更新数据
	 * <p>需要在{@link Activity#onResume}方法中调用此方法</P>
	 * @author jrjin
	 * @time 2015-6-4 下午3:21:39
	 */
	public void onResume() {}
	/**
	 * {@link Activity#onPause()}
	 * @author jrjin
	 * @time 2015-6-4 下午3:21:48
	 */
	public void onPause() {}
	/**
	 * {@link Activity#onDestroy()}
	 * @author jrjin
	 * @time 2015-6-4 下午3:22:47
	 */
	public void onDestory() {
		release();
	}
	
	/**
	 * 释放内存
	 * @author jrjin
	 * @time 2015-6-4 下午3:57:48
	 */
	public void release() {}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {}
}
