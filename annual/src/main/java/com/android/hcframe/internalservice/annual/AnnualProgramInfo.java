/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2016-1-12 上午11:41:26
*/
package com.android.hcframe.internalservice.annual;

public class AnnualProgramInfo {

	/** 节目标题 */
	private String mTitle;
	/** 节目内容 */
	private String mContent;
	/** 节目评分 */
	private int mScore;
	/** 是否显示节目内容 */
	private boolean mShowContent;
	/** 节目类型 
	 * 1、歌曲声乐
	 * 2、舞蹈表演
	 * 3、情景相声
	 */
	private int mType;
	/** 节目ID */
	private String mId;
	/** 年会标识 */
	private String mAnnualId;
	
	/** 歌曲声乐 */
	public static final int TYPE_MUSIC = 1;
	/** 舞蹈表演 */
	public static final int TYPE_DANCING = 2;
	/** 情景相声 */
	public static final int TYPE_SKIT = 3;
	
	public AnnualProgramInfo() {}
	
	public AnnualProgramInfo(AnnualProgramInfo info) {
		mTitle = info.mTitle;
		mContent = info.mContent;
		mAnnualId = info.mAnnualId;
		mId = info.mId;
		mScore = info.mScore;
		mType = info.mType;
		mShowContent = info.mShowContent;
	}
	
	/**
	 * 
	 * @author jrjin
	 * @time 2016-1-12 下午12:16:42
	 * @param id 节目ID
	 */
	public void setProgramId(String id) {
		mId = id;
	}
	
	/**
	 * 获取节目ID
	 * @author jrjin
	 * @time 2016-1-12 下午12:16:52
	 * @return
	 */
	public String getProgramId() {
		return mId;
	}
	/**
	 * 设置节目标题
	 * @author jrjin
	 * @time 2016-1-12 下午12:17:01
	 * @param title 节目标题
	 */
	public void setProgramTitle(String title) {
		mTitle = title;
	}
	/**
	 * 获取节目标题
	 * @author jrjin
	 * @time 2016-1-12 下午12:17:16
	 * @return 节目标题
	 */
	public String getProgramTitle() {
		return mTitle;
	}
	/**
	 * 设置节目内容说明
	 * @author jrjin
	 * @time 2016-1-12 下午12:17:33
	 * @param content 节目说明
	 */
	public void setProgramContent(String content) {
		mContent = content;
	}
	/**
	 * 获取节目内容
	 * @author jrjin
	 * @time 2016-1-12 下午12:17:55
	 * @return
	 */
	public String getProgramContent() {
		return mContent;
	}
	/**
	 * 设置节目类型
	 * @author jrjin
	 * @time 2016-1-12 下午12:18:07
	 * @param type 1、歌曲声乐；2、舞蹈表演；3、情景相声
	 * @see #TYPE_DANCING
	 * @see #TYPE_MUSIC
	 * @see #TYPE_SKIT
	 */
	public void setProgramType(int type) {
		mType = type;
	}
	
	/**
	 * 获取节目类型
	 * @author jrjin
	 * @time 2016-1-12 下午12:19:17
	 * @return 1、歌曲声乐；2、舞蹈表演；3、情景相声
	 * @see #TYPE_DANCING
	 * @see #TYPE_MUSIC
	 * @see #TYPE_SKIT
	 */
	public int getProgramType() {
		return mType;
	}
	/**
	 * 设置评分,默认为0，即未打分
	 * @author jrjin
	 * @time 2016-1-12 下午12:21:44
	 * @param score
	 */
	public void setProgramScore(int score) {
		mScore = score;
	}
	/**
	 * 获取节目评分
	 * @author jrjin
	 * @time 2016-1-12 下午12:22:17
	 * @return 0-5；0：未打分;
	 */
	public int getProgramScore() {
		return mScore;
	}
	/**
	 * 设置是否显示内容
	 * @author jrjin
	 * @time 2016-1-12 下午12:22:53
	 * @param show
	 */
	public void setShowContent(boolean show) {
		mShowContent = show;
	}
	
	/**
	 * 获取是否显示内容
	 * @author jrjin
	 * @time 2016-1-12 下午12:23:10
	 * @return
	 */
	public boolean getShowContent() {
		return mShowContent; 
	}
	
	public void setAnnualId(String annualId) {
		mAnnualId = annualId;
	}
	
	public String getAnnualId() {
		return mAnnualId;
	}
}
