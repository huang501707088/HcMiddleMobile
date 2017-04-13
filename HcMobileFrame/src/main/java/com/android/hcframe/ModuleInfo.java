/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-10-22 下午5:03:44
*/
package com.android.hcframe;

public class ModuleInfo {

	/** 模块编号 */
	private String mModuleId;
	/** 模块最后更新时间 */
	private String mUpdateTime;
	/** 0:有更新；1:无更新 */
	private int mFlag = FLAG_NEWEST;
	/** 需要更新 */
	public static final int FLAG_UPDATE = 0;
	/** 无需更新 */
	public static final int FLAG_NEWEST = 1;
	
	public ModuleInfo() {}
	
	public ModuleInfo(String moduleId, String updateTime) {
		mModuleId = moduleId;
		mUpdateTime = updateTime;
	}
	
	public ModuleInfo(String moduleId, int updateFlag) {
		mModuleId = moduleId;
		mFlag = updateFlag;
	}
	
	public ModuleInfo(String moduleId, String updateTime, int updateFlag) {
		mModuleId = moduleId;
		mUpdateTime = updateTime;
		mFlag = updateFlag;
	}
	
	public void setModuleId(String moduleId) {
		mModuleId = moduleId;
	}
	
	public String getModuleId() {
		return mModuleId;
	}
	
	public void setUpdateTime(String updateTime) {
		mUpdateTime = updateTime;
	}
	
	public String getUpdateTime() {
		return mUpdateTime;
	}
	
	public void setUpdateFlag(int flag) {
		mFlag = flag;
	}
	
	public int getUpdateFlag() {
		return mFlag;
	}
}
