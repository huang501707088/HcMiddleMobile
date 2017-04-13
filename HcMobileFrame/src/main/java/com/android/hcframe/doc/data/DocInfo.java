/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-8-27 下午3:10:33
 */
package com.android.hcframe.doc.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 通过栏目获取的资料列表，或者是资料详情
 * 
 * @see SearchDocInfo 通过检索获取的资料列表
 * @author jrjin
 * @time 2015-8-28 上午9:52:03
 */
public class DocInfo /*implements Serializable*/ {

	private static final String TAG = "DocInfo";

	/** 资料编号 */
	private String mId;
	/** 资料标题 */
	private String mTitle;
	/** 资料出处 */
	private String mSource;
	/** 资料发布日期 */
	private String mDate;
	/** 状态：0--有效，1--失效 */
	private int mStatus;
	/** 附件信息列表,第一个为主文件 */
	private List<DocFileInfo> mDocInfos = new ArrayList<DocFileInfo>();
	/** 资料标识：0—标题，1—主文件，2—附件 */
	private int mFlag = FLAG_TITIL;
	/** 标题 */
	public static final int FLAG_TITIL = 0;
	/** 主文件 */
	public static final int FLAG_MAIN = 1;
	/** 附件 */
	public static final int FLAG_SUB = 2;
	/** 栏目编号;要是是通过检索去请求详情的接口，则为资料编号或者文件编号 */
	private String mColumnId = "";

	public void setDataId(String id) {
		mId = id;
	}

	public void setDataTitle(String title) {
		mTitle = title;
	}

	/**
	 * 设置资料发布日期
	 * 
	 * @author jrjin
	 * @time 2015-8-27 下午3:50:18
	 * @param date
	 *            资料发布日期
	 */
	public void setDate(String date) {
		mDate = date;
	}

	/**
	 * 设置资料出处
	 * 
	 * @author jrjin
	 * @time 2015-8-27 下午3:49:56
	 * @param source
	 *            资料出处
	 */
	public void setDataSource(String source) {
		mSource = source;
	}

	public void setStatus(int status) {
		mStatus = status;
	}

	public String getDataId() {
		return mId;
	}

	public String getDataTitle() {
		return mTitle;
	}

	public String getDataSource() {
		return mSource;
	}

	public String getDate() {
		return mDate;
	}

	public int getStatus() {
		return mStatus;
	}

	public void addDocInfo(DocFileInfo info) {
		if (mDocInfos.contains(info))
			return;
		mDocInfos.add(info);
	}

	public List<DocFileInfo> getDocInfos() {
		return mDocInfos;
	}

	public void setFlag(int flag) {
		throw new UnsupportedOperationException(TAG
				+ " setFlag is not supported flag == 0");
	}

	public int getFlag() {
		return mFlag;
	}

	public String getFileId() {
		if (mDocInfos.size() == 0)
			throw new IndexOutOfBoundsException(TAG
					+ " getFileId size is 0, index is 0!");
		return mDocInfos.get(0).getFileId();
	}

	public String getFileName() {
		if (mDocInfos.size() == 0)
			throw new IndexOutOfBoundsException(TAG
					+ " getFileId size is 0, index is 0!");
		return mDocInfos.get(0).getFileName();
	}

	public String getFileUrl() {
		if (mDocInfos.size() == 0)
			throw new IndexOutOfBoundsException(TAG
					+ " getFileId size is 0, index is 0!");
		return mDocInfos.get(0).getFileUrl();
	}

	public int getFileSize() {
		if (mDocInfos.size() == 0)
			throw new IndexOutOfBoundsException(TAG
					+ " getFileId size is 0, index is 0!");
		return mDocInfos.get(0).getFileSize();
	}

	public void setColumnId(String id) {
		mColumnId = id;
	}

	public String getColumnId() {
		return mColumnId;
	}
	
	public String getFileSizeForUnit() {
		if (mDocInfos.size() == 0)
			throw new IndexOutOfBoundsException(TAG
					+ " getFileId size is 0, index is 0!");
		return mDocInfos.get(0).getFileSizeForUnit();
	}
}
