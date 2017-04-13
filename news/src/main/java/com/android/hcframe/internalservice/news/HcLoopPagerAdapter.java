/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-28 下午1:32:53
*/
package com.android.hcframe.internalservice.news;

import java.util.List;

import com.android.hcframe.ad.LoopPagerAdapter;
import com.android.hcframe.adapter.ViewHolderFactory;
import com.android.hcframe.data.NewsInfo;

public class HcLoopPagerAdapter extends LoopPagerAdapter<NewsInfo> {

	public HcLoopPagerAdapter(List<NewsInfo> data, ViewHolderFactory<NewsInfo> factory) {
		super(data, true, factory);
		// TODO Auto-generated constructor stub
	}

}
