/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-29 上午11:20:30
*/
package com.android.hcframe.internalservice.news;

import com.android.hcframe.HcUtil;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * 
 * @author jrjin
 * @time 2015-12-29 上午11:25:53
 * 问题一 ： 嵌套在 ScrollView的 ListVew数据显示不全，我遇到的是最多只显示两条已有的数据。

	解决办法：重写 ListVew或者 GridView，网上还有很多若干解决办法，但是都不好用或者很复杂。

	只重写该方法，达到使ListView适应ScrollView的效果  

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

	int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,

	MeasureSpec.AT_MOST);

	super.onMeasure(widthMeasureSpec, expandSpec);

	}

	问题二 、打开套有 ListVew的 ScrollView的页面布局 默认 起始位置不是最顶部。

	解决办法有两种都挺好用：

	一是把套在里面的Gridview 或者 ListVew 不让获取焦点即可。

	gridview.setFocusable(false); listview.setFocusable(false);

	注意：在xml布局里面设置android：focusable=“false”不生效

	方法二：网上还查到说可以设置myScrollView.smoothScrollTo(0,0);
 @deprecated 未被使用,因为适配器里面的布局就不会复用了.
 */
public class ScrollListView extends ListView {

	public ScrollListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public ScrollListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public ScrollListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		int topBarHeight = getContext().getResources().getDimensionPixelSize(R.dimen.top_bar_height);
		int titleBarHeight = getContext().getResources().getDimensionPixelSize(R.dimen.market_top_bar_height);
		int menuHeight = getContext().getResources().getDimensionPixelSize(R.dimen.menu_height);
		int height = HcUtil.getScreenHeight() - topBarHeight - titleBarHeight - menuHeight;
		int expandSpec = MeasureSpec.makeMeasureSpec(/*Integer.MAX_VALUE >> 2*/height,

		MeasureSpec.AT_MOST);

		super.onMeasure(widthMeasureSpec, expandSpec);
	}

}
