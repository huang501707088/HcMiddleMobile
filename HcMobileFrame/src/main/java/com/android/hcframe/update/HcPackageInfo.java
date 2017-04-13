package com.android.hcframe.update;

public class HcPackageInfo {

	/** 0不更新；1需要更新；2需要强制更新 */
	public int flag = 0;
	/** 更新描述 */
	public String desc = "";
	/** 服务器最新版本 */
	public String version = "";
	/** 下载Url */
	public String url = "";
	/** 下载包大小 */
	public int size = 0;
	
}
