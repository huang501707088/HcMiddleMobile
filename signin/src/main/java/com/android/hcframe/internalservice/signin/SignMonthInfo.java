/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2016-1-5 下午6:21:51
*/
package com.android.hcframe.internalservice.signin;

import java.util.ArrayList;
import java.util.List;

public class SignMonthInfo {

	/**
	 * 当前年月
	 */
	public String mMonth;
	/**
	 * 标准上班时间
	 */
	public String mWorkInTime;
	/**
	 * 标准下班时间
	 */
	public String mWorkOutTime;
	/**
	 * 迟到次数
	 */
	public int mLateAmount;
	/**
	 * 早退次数
	 */
	public int mLeavEearlyAmount;
	/**
	 * 缺勤次数
	 */
	public int mAbsenteeismAmount;
	
	public List<SignDayInfo> mDayInfos = new ArrayList<SignDayInfo>();
	
}
