/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-5-29 上午11:22:26
*/
package com.android.hcframe.market;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcObserver;
import com.android.hcframe.HcSubject;
import com.android.hcframe.data.AppInfo;
import com.android.hcframe.data.HcAppData;
import com.android.hcframe.data.HcAppReceiver;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.view.tab.HcTabPageIndicator;
import com.android.hcframe.view.tab.HcTabPageIndicator.ViewHolderBase;
import com.android.hcframe.view.tab.HcTabPageIndicator.ViewHolderCreator;

/**
 * 应用分类横向显示
 * @author jrjin
 * @time 2015-11-30 下午5:17:47
 */
public class MarketViewLandPager extends AbstractPage implements OnPageChangeListener, HcObserver {

	private static final String TAG = "MarketViewLandPager";
	
	private ViewPager mViewPager;
	
	private HcTabPageIndicator mIndicator;
	
	private List<AbstractPage> mPages = new ArrayList<AbstractPage>();
	
	private Map<Integer, AbstractPage> mCategory = new TreeMap<Integer, AbstractPage>();
	
	private int mCurrentPage = 0;
	
	private MarketViewAdapter mAdapter;
	
	private AppObservable mObservable;
	
	private List<AppCategoryInfo> mCategoryInfos = new ArrayList<AppCategoryInfo>();

	private HcAppReceiver mReceiver;

	protected MarketViewLandPager(Activity context, ViewGroup group) {
		super(context, group);
		// TODO Auto-generated constructor stub
		mObservable = new AppObservable();
		mReceiver = new HcAppReceiver();
		IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filter.addDataScheme("package");
		context.registerReceiver(mReceiver, filter);
		mReceiver.setInstallListener(HcAppData.getInstance());
	}

	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialized() {
		// TODO Auto-generated method stub
		
		
		
	}

	@Override
	public void setContentView() {
		// TODO Auto-generated method stub
		if (isFirst) {
			isFirst = !isFirst;
			mView = mInflater.inflate(R.layout.market_content_land, null);
			mIndicator = (HcTabPageIndicator) mView.findViewById(R.id.market_land_pager_indicator);
			mViewPager = (ViewPager) mView.findViewById(R.id.market_land_pager);
			mIndicator.setViewHolderCreator(new ViewHolderCreator() {
				
				@Override
				public ViewHolderBase createViewHolder() {
					// TODO Auto-generated method stub
					return new MarketViewHolder();
				}
			});
			mViewPager.setAdapter(new MarketViewAdapter());
			mIndicator.setViewPager(mViewPager, -1);
			/**
			 * @author jrjin
			 * @date 2015-11-30 下午2:01:07
			 */
			HcAppData.getInstance().addObserver(this);
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
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int position) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateData(HcSubject subject, Object data,
			RequestCategory request, ResponseCategory response) {
		// TODO Auto-generated method stub
		if (request != null && request == RequestCategory.APP_ALL) {
			if (data != null && data instanceof List<?>) {
				List<AppInfo> infos = (List<AppInfo>) data;
				parseApps(infos);
			}
		}
	}
	
	private void parseApps(List<AppInfo> infos) {
		Map<Integer, AppCategoryInfo> category = new TreeMap<Integer, AppCategoryInfo>();
		AppCategoryInfo cInfo = null;
		cInfo = new AppCategoryInfo();
		cInfo.setCategoryTag(HcAppData.APP_CATEGORY_ALL);
		cInfo.setCategoryName("全部应用");
		cInfo.addAppList(infos);
		category.put(HcAppData.APP_CATEGORY_ALL, cInfo);
		for (AppInfo info : infos) {
			cInfo = category.get(info.getAppCategory());
			if (null == cInfo) { // 说明以前这个类型没有
				cInfo = new AppCategoryInfo();
				cInfo.setCategoryName(info.getCategoryName());
				cInfo.setCategoryTag(info.getAppCategory());
				category.put(info.getAppCategory(), cInfo);
			}
			cInfo.addAppInfo(info);		
		} // 分类结束
		
		mCategoryInfos.clear();
		mCategoryInfos.addAll(category.values());
		
		// 设置Indicator,放到matchViews这里去设置
		
		
		// 先匹配view,有就更新里面的数据，没有就开始创建View
		matchViews(category);
	}
	
	private void matchViews(Map<Integer, AppCategoryInfo> category) {
		AbstractPage view = null;
		Set<Integer> oldCategories = new TreeSet<Integer>(mCategory.keySet()); // 为了检查是否需要删除已有的View,因为类别可能会有变化
		Set<Integer> categories = category.keySet();
		for (Integer key : categories) {
			view = mCategory.get(key);
			if (view == null) { // 说明该列表没有，需要添加
				view = new AppItemLandView(mContext, null, key);
				mObservable.addObserver(view);
				view.changePages();
				mCategory.put(key, view);
				mObservable.notifyApp(category.get(key));
				mPages.add(view);
			} else {
				oldCategories.remove(key);  // 
				// 更新数据
				mObservable.notifyApp(category.get(key));
			}
		}
		
		HcLog.D(TAG + " matchViews oldCategories size = "+oldCategories.size());
		
		for (Integer integer : oldCategories) { // 删除多余的View，本来可以再利用。
			AbstractPage categoryView = mCategory.remove(integer);
			mObservable.deleteObserver(categoryView);
		}
		mViewPager.getAdapter().notifyDataSetChanged();	
        if (mIndicator != null) {
        	mIndicator.notifyDataSetChanged();
        	mIndicator.moveToItem(-1); // 这里为了第一次不显示taobar
        	mIndicator.moveToItem(0);
        }
	}
	
	public class AppObservable extends Observable {
		
		public AppObservable() {}
		
		private void notifyApp(AppCategoryInfo info) {
			setChanged();
			notifyObservers(info);
		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		HcAppData.getInstance().getAppList(HcAppData.APP_CATEGORY_ALL, RequestCategory.APP_ALL, true);
	}

	private class MarketViewHolder extends HcTabPageIndicator.ViewHolderBase {

        private TextView mTitleTextView;
        private View mViewSelected;
        private final int COLOR_TEXT_SELECTED = Color.parseColor("#5aefe6");
        private final int COLOR_TEXT_NORMAL = Color.parseColor("#000000");

        @Override
        public View createView(LayoutInflater layoutInflater, int position) {
            View view = layoutInflater.inflate(R.layout.market_topbar_indicator_layout, null);
            mTitleTextView = (TextView) view.findViewById(R.id.market_topbar_item_title);
            mViewSelected = view.findViewById(R.id.market_topbar_item_selected);
            return view;
        }

        @Override
        public void updateView(int position, boolean isCurrent) {
            HcLog.D(TAG + " updateView position = "+position + " isCurrent = "+isCurrent);
        	mTitleTextView.setText(mCategoryInfos.get(position).getCategoryName());
            if (isCurrent) {
                mTitleTextView.setTextColor(COLOR_TEXT_SELECTED);
                mViewSelected.setVisibility(View.VISIBLE);
            } else {
                mTitleTextView.setTextColor(COLOR_TEXT_NORMAL);
                mViewSelected.setVisibility(View.INVISIBLE);
            }
        }
    }

	@Override
	public void release() {
		// TODO Auto-generated method stub
		HcAppData.getInstance().removeObserver(this);
		for (AbstractPage view : mCategory.values()) {
			view.release();
			mObservable.deleteObserver(view);
			view = null;
		}
		mCategory.clear();
		mCategory = null;
		mObservable = null;
		
		mPages.clear();
		mCategoryInfos.clear();
		
		mView = null;
		
		
		mContext = null;
		mGroup = null;
	}

	@Override
	public void onDestory() {
		if (mReceiver != null) {
			mReceiver.setInstallListener(null);
			mContext.unregisterReceiver(mReceiver);
			mReceiver = null;
		}
		super.onDestory();
	}
}
