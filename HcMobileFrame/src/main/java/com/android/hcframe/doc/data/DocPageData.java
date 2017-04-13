/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-8-31 上午9:23:25
*/
package com.android.hcframe.doc.data;

import java.util.ArrayList;
import java.util.List;

import com.android.hcframe.pull.PullToRefreshBase.Mode;

/**
 * 资料数据的页面信息
 * @author jrjin
 * @time 2015-8-31 上午10:46:20
 */
public class DocPageData {

public int mPageNumber = 1;
	
	public List<DocInfo> mInfos = new ArrayList<DocInfo>();
	
	public Mode mMode = Mode.PULL_FROM_START;
}
