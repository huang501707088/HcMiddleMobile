/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2013-11-26 下午1:21:43
*/
package com.android.hcframe.http;

/**
 * 为枚举类型，是向服务器请求的数据类型。
 * <p>1:没有类型，默认情况</p>
 * <p>2:获取应用信息</p>
 * <p>3:下载安装包</p>
 * <p>4:下载图片</p>
 * <p>4:下载html5应用</p>
 */

public enum RequestCategory {

	NONE,
	GET_APPS,
	UPDATE_DOWNLOAD,	
	DOWNLOAD_IMAGE,
	DOWNLOAD_APP,
	DOWNLOAD_PDF,
	DOWNLOAD_GIF,
	NEWSCOLUMN,
	SIGN,
	SIGNITEM,
	SIGNADDR,
	SIGNMENU,
	SIGNLISTBYDAY,
	SIGNLISTBYMONTH,
	LOGIN,
	MONITOR,
	MODIFY,
	GETCODE,
	CHECKCODE,
	REGISTER,
	REGETPWD,
	NICKNAME,
	BINDP,
	CHECKAV,
	UPDATEAS,
	SENDMSG,
	SENDREPORT,
	HELPC,
	LOGOUT,
	UPDATE_VERSION,
	/** 全部应用超市应用 */
	APP_ALL,
	/** 统计应用 */
	APP_DATA,
	APP_SERVICE,
	APP_SUPERVICE,
	APP_OA,
	/** 服务超市应用 */
	APP_SERVER,
	NEWS,
	NEWSSCROLL,
	NEWDETAILS,
	BINDCHAN,
	PushModuleList,
	UpdatePushSettings,
	
	/** 资料中心栏目 */
	DATA_COLUMN,
	/** 栏目数据 */
	DATA_LIST,
	/** 栏目搜索 */
	SEARCH_DATA,
	/** 数据详情 */
	SEARCH_DATA_DETAIL,
	/** 搜索全部栏目的数据 */
	SEARCH_ALL_DATA,
	/** 获取通讯录联系人 */
	CONTACTS_REQUEST,
	/** 检测模块更新时间 */
	CHECK_MODULE_TIME,
	/** 上传头像 */
	POST_IMAGE,
	/** 上传头像 */
	POST_LIST_IMAGE,
	/** 新闻列表 */
	NEWS_LIST,
	/** 年会配置信息 */
	ANNUAL_CONFIG,
	/** 年会节目列表 */
	ANNUAL_PROGRAM,
	/** 提交节目打分 */
	ANNUAL_SCORE,
	/** 提交摇一摇获取奖券码 */
	ANNUAL_SHAKE,
	/** 获取摇一摇状态 */
	ANNUAL_SHAKE_STATUS,
	/** 上传日志 */
	POST_LOGS,
	/** 角标 */
	CORNER,
	/** 扫码 */
	SCAN,
	/** 签到 */
	WORKDETAIL,
	/**新建文件夹*/
	NewFile,
	/**获取云盘中的List列表*/
	CloudList
}
