/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-10-30 下午3:53:52
*/
package com.android.hcframe.push;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.android.hcframe.HcLog;
import com.android.hcframe.menu.Menu1Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class HcAppState {

	private static final String TAG = "HcAppState";
	
	private static final HcAppState APP_STATE = new HcAppState();
	/** 记录App是否已经启动：1.当MainActivity1启动过说明已经启动过了 */
	private boolean mOnStarted = false;
	
	private List<Activity> mActivities = new ArrayList<Activity>();

	private HcAppState() {
	}
	
	public static HcAppState getInstance() {
		return APP_STATE;
	}
	
	/**
	 * 获取应用是否已经启动，注意只用于消息推送
	 * @author jrjin
	 * @time 2015-10-30 下午4:05:41
	 * @return true:已经启动
	 */
	private boolean getAppOnStarted() {
		HcLog.D(TAG + " getAppOnStarted OnStarted = "+mOnStarted);
		return mOnStarted;
	}
	
	/**
	 * 设置应用已经启动
	 * <p>在{@link Menu1Activity#onCreate}</p>里设置
	 * @author jrjin
	 * @time 2015-10-30 下午4:05:37
	 * @param onStart
	 */
	public void setAppOnStarted() {
		mOnStarted = true;
	}
	/**
	 * 添加启动的Activity
	 * @author jrjin
	 * @time 2015-11-2 下午3:53:21
	 * @param activity
	 */
	public void addActivity(Activity activity) {
		if (mActivities.contains(activity)) return;
		mActivities.add(activity);
	}
	
	/**
	 * 删除需要finish的Activity
	 * @author jrjin
	 * @time 2015-11-2 下午3:52:57
	 * @param activity
	 */
	public void removeActivity(Activity activity) {
		mActivities.remove(activity);
	}
	
	/**
	 * @author jrjin
	 * @time 2015-10-30 下午4:17:50
	 */
	@SuppressWarnings("需要测试")
	public void finishAllActivities() {
		Iterator<Activity> iterator = mActivities.iterator();
		HcLog.D(TAG + " finishAllActivities activity size = " + mActivities.size());
		Activity a;
		while (iterator.hasNext()) {
			a = iterator.next();
			iterator.remove();
			a.finish();
			a = null;
		}
	}

	public Activity getTopActivity() {
		Activity current = null;
		if (!mActivities.isEmpty()) {
			current = mActivities.get(mActivities.size() -1);
		}
		HcLog.D(TAG + "#getTopActivity current = "+current);
		return current;
	}


	/**
	 * 进入应用的主页面,如果应用的主页面不存在，
	 * 启动主页的Activity；否则直接退出当前的Activity
	 * @author jrjin
	 * @time 2015-11-2 下午1:54:44
	 * @param context
	 */
	public void startMainActivity(Activity context) {
		mActivities.remove(context);
		if (!mOnStarted) {
			Intent intent = new Intent();
			intent.setClass(context, Menu1Activity.class);
			context.startActivity(intent);
		}
		context.finish();
		context.overridePendingTransition(0, 0);
	}

}
