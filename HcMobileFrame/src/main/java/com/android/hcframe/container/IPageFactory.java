/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-7-22 上午11:03:46
*/
package com.android.hcframe.container;

import android.app.Activity;
import android.view.ViewGroup;

import com.android.hcframe.AbstractPage;

public interface IPageFactory {

	public AbstractPage createPage(Activity context, ViewGroup parent);
}
