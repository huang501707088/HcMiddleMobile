/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-8-27 下午4:42:47
*/
package com.android.hcframe.doc.data;

import java.text.DecimalFormat;

/**
 * 文件信息，包括主文件和附件
 * 另外在检索的时候用到
 * @see SearchDocInfo 
 * @author jrjin
 * @time 2015-8-27 下午4:42:54
 */
public class DocFileInfo {

	/** 资料文件ID */
	private String mFileId;
	/** 资料文件名 */
	private String mFileName;
	/** 资料文件大小 */
	private int mFileSize;
	/** 资料文件路径(url) */
	private String mFileUrl;
	
	/** 资料标识：0—标题，1—主文件，2—附件 */
	private int mFlag; 
	
	public void setFileId(String fileId) {
		mFileId = fileId;
	}
	
	public void setFileName(String fileName) {
		mFileName = fileName;
	}
	
	public void setFileSize(int size) {
		mFileSize = size;
	}
	
	public void setFileUrl(String url) {
		mFileUrl = url;
	}
	
	public String getFileId() {
		return mFileId;
	}
	
	public String getFileName() {
		return mFileName;
	}
	
	public String getFileUrl() {
		return mFileUrl;
	}
	
	public int getFileSize() {
		return mFileSize;
	}
	
	public void setFlag(int flag) {
		mFlag = flag;
	}
	
	public int getFlag() {
		return mFlag;
	}
	
	public String getFileSizeForUnit() {
		if (mFileSize <= 0) return 0 + " kb";
		if (mFileSize < 1024) return mFileSize + " kb";
		if (mFileSize < 1024 * 1024) return mFileSize / 1024 + " K";
		return new DecimalFormat("0.00").format(mFileSize / (1024 * 1024d)) + " M";
				
	}
}
