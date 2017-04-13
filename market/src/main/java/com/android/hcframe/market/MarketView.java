/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-4-29 下午3:16:08
*/
package com.android.hcframe.market;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.sax.StartElementListener;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TabWidget;
import android.widget.TextView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.data.HcAppData;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.menu.MenuBaseActivity;

/**
 * @deprecated
 * @author jrjin
 * @time 2015-11-30 下午4:11:56
 */
public class MarketView extends AbstractPage implements OnPageChangeListener {

	private static final String TAG = "MarketView";
	
	private ImageView mSetting;
	
	private View mIndicator;
	
	private ViewPager mViewPager;
	
	private TabWidget mTabWidget;
	
	private int mIndicatorWidth = -1;
	
//	private Display mDisplay;
//	
//	private DisplayMetrics mMetrics;
	
	private List<AbstractPage> mPages = new ArrayList<AbstractPage>();
	
	private int mCurrentPage = 0;
	
	private MarketViewAdapter mAdapter;
	
	private AppItemView mAllAppsView;
	
	private AppItemView mDataView;
	
	private AppItemView mServiceView;
	
	private AppItemView mSurpversionView;
	
	private AppItemView mOAView;
	
	private MenuBaseActivity mActivity;
	
	public MarketView(Activity context, ViewGroup group) {
		super(context, group);
		// TODO Auto-generated constructor stub
		mActivity = (MenuBaseActivity) context;
//		mDisplay = context.getWindowManager().getDefaultDisplay();
//		mMetrics = new DisplayMetrics();
//		mDisplay.getMetrics(mMetrics);
//		HcLog.D(TAG + " width = "+mDisplay.getWidth() + " height = "+mDisplay.getHeight() + " density = "+mMetrics.density);
		
		mIndicatorWidth = HcUtil.getScreenWidth() / 5;//(int) (mDisplay.getWidth() /*- 40 * mMetrics.density - context.getResources().getDrawable(R.drawable.setting_btn_normal).getIntrinsicWidth()*/) / 5;
		HcLog.D(TAG + " mIndicatorWidth = "+mIndicatorWidth);
		mAllAppsView = new AppItemView(context, null, HcAppData.APP_CATEGORY_ALL, RequestCategory.APP_ALL);
		mDataView = new AppItemView(context, null, HcAppData.APP_CATEGORY_DATA, RequestCategory.APP_DATA);
		mServiceView = new AppItemView(context, null, HcAppData.APP_CATEGORY_SERVICE, RequestCategory.APP_SERVICE);
		mSurpversionView = new AppItemView(context, null, HcAppData.APP_CATEGORY_SUPERVICE, RequestCategory.APP_SUPERVICE);
		mOAView = new AppItemView(context, null, HcAppData.APP_CATEGORY_OA, RequestCategory.APP_OA);
	}

	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub
		if (mAdapter != null) 
			mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialized() {
		// TODO Auto-generated method stub
		if (mAdapter == null) {
			mAdapter = new MarketViewAdapter();
			mPages.add(mAllAppsView);
			mPages.add(mDataView);
			mPages.add(mServiceView);
			mPages.add(mSurpversionView);
			mPages.add(mOAView);
			mViewPager.setOnPageChangeListener(this);
			mViewPager.setAdapter(mAdapter);
			mPages.get(mCurrentPage).changePages();
			mViewPager.setCurrentItem(mCurrentPage);
		}
	}

	@Override
	public void setContentView() {
		// TODO Auto-generated method stub
		if (isFirst) {
			isFirst = !isFirst;
			mView = mInflater.inflate(R.layout.market_content, null);
			mIndicator = mView.findViewById(R.id.market_indicator);
			mSetting = (ImageView) mView.findViewById(R.id.market_setting_btn);
			mSetting.setOnClickListener(new OnClickListener() {
				
				public void onClick(View arg0) {
					;
				}
			});
			mViewPager = (ViewPager) mView.findViewById(R.id.market_pager);
			mTabWidget = (TabWidget) mView.findViewById(R.id.market_category_bar);
			if (mIndicator.getMeasuredWidth() != mIndicatorWidth) {
				mIndicator.getLayoutParams().width = mIndicatorWidth;
				mIndicator.requestLayout();
            }
			int count = mTabWidget.getChildCount();
			for(int i = 0; i < count; i++) {
				View view = mTabWidget.getChildAt(i);
				view.setTag(i);
				view.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						int index = (Integer)v.getTag();
						if (mCurrentPage == index) return;
						updateScrollingIndicatorPosition(mCurrentPage, index);
						mCurrentPage = index;
						mPages.get(mCurrentPage).changePages();
						mViewPager.setCurrentItem(mCurrentPage);
//						mActivity.getApps(mCurrentPage, getCategory(mCurrentPage), true);
					}
				});
			}
			/**
			 * @date 2015-11-30 下午4:12:49
			mActivity.addObserver(mAllAppsView);
			mActivity.addObserver(mDataView);
			mActivity.addObserver(mOAView);
			mActivity.addObserver(mServiceView);
			mActivity.addObserver(mSurpversionView);
			mActivity.addAdapterObserver(mAllAppsView);
			*/
		}
	}

	private class MarketViewAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mPages.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			// TODO Auto-generated method stub
			return view == object;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			// TODO Auto-generated method stub
			HcLog.D(TAG + " instantiateItem ViewGroup = "+container + " position = "+position);
			View view = mPages.get(position).getContentView();
			if (view == null)
				mPages.get(position).changePages();
			((ViewPager) container).addView(mPages.get(position).getContentView());
			return mPages.get(position).getContentView();
			
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
			if (position < mPages.size()) {
				((ViewPager) container).removeView(mPages.get(position).getContentView());
			}
		}

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@SuppressLint("NewApi")
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
//		HcLog.D(TAG + " index = " + arg0 + " offset = "+arg2 + " baifenbi =" + arg1);
		if (arg1 > 0) {
			mIndicator.setTranslationX((arg0 + arg1) * mIndicatorWidth);
		}
	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		updateScrollingIndicatorPosition(mCurrentPage, arg0);
		mCurrentPage = arg0;
		updateBar(mCurrentPage);
	}
	
	private void updateBar(int index) {
		mTabWidget.setCurrentTab(index);
		mPages.get(mCurrentPage).changePages();
	}
	
	@SuppressLint("NewApi")
	private void updateScrollingIndicatorPosition(int oldIndex, int newIndex) {
//		int translationX = newIndex - oldIndex > 0 : mIndicatorWidth * newIndex ? 
		mIndicator.setTranslationX(newIndex * mIndicatorWidth);
	}
	
}
