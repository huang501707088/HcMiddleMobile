/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-6-19 上午10:09:54
*/
package com.android.hcframe;

import java.lang.Thread.UncaughtExceptionHandler;

import com.android.hcframe.monitor.LogManager;

public class CrashExceptionHandler implements UncaughtExceptionHandler {

	private static final String TAG = "CrashExceptionHandler";
	
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		// TODO Auto-generated method stub
		HcLog.D(TAG + " #uncaughtException thread = "+thread + " Throwable ex= "+ex);
		StringBuilder builder = new StringBuilder("<<<<< start write crash! time = "+HcUtil.getDate(HcUtil.FORMAT_POLLUTION, System.currentTimeMillis()));
		builder.append("\n");
		builder.append("thread name = " +thread.getName());
		builder.append("\n");
		builder.append(ex.toString());
		builder.append("\n");
		StackTraceElement[] elements = ex.getStackTrace();
		for (StackTraceElement stackTraceElement : elements) {
			builder.append(stackTraceElement.toString());
			builder.append("\n");
		}
		Throwable cause = ex.getCause();
		if (cause != null) {
			elements = cause.getStackTrace();
			for (StackTraceElement stackTraceElement : elements) {
				builder.append("causeBy:" + stackTraceElement.toString());
				builder.append("\n");
			}
		}
		HcLog.writeDebug(builder.toString());
		HcLog.D(TAG + " #uncaughtException end! time = "+HcUtil.getDate(HcUtil.FORMAT_POLLUTION, System.currentTimeMillis()));
		
		/**
		 * @author jrjin
		 * @date 2016-2-24 上午11:06:21
		 */
		LogManager.getInstance().updateLog(HcApplication.getContext(), true);
	}

}
