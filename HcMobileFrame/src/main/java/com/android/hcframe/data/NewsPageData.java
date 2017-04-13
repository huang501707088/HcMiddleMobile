/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-7-16 下午4:21:22
*/
package com.android.hcframe.data;

import java.util.ArrayList;
import java.util.List;

import com.android.hcframe.pull.PullToRefreshBase.Mode;

public class NewsPageData {

	/** 当前页面页数 */
	public int mPageNumber = 1;
	/** 页面列表数据 */
	public List<NewsInfo> mInfos = new ArrayList<NewsInfo>();
	/** 页面可操作模式 */
	public Mode mMode = Mode.PULL_FROM_START;
	/** 获取数据类型 */
	public int mGetType = HcNewsData.GET_DATA_REFRESH; 
	/** 栏目编号,只有在数据返回的时候用 
	 * @date 2016-1-27 下午2:52:10 这里之后必须不能为空,因为存数据库的时候需要用到*/
	public String mId;
}
