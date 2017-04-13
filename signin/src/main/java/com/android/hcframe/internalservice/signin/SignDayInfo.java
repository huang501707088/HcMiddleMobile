/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2016-1-5 下午6:21:36
*/
package com.android.hcframe.internalservice.signin;

public class SignDayInfo {

	/**
	 * 编号
	 */
	public String mId;
	/**
	 * 打卡日期：2015-08-01
	 */
	public String mDate;
	/**
	 * 签到方式：0—自动，1—手动
	 */
	public String mSignInType;
	/**
	 * 签出方式：0—自动，1—手动
	 */
	public String mSignOutType;
	/**
	 * 签到时间：08:28:09
	 */
	public String mSignInTime;
	/**
	 * 签出时间：17:30:03
	 */
	public String mSignOutTime;
	
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
}
