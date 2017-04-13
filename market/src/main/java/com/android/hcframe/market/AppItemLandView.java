/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-6-1 上午10:48:18
*/
package com.android.hcframe.market;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcDraggableGridViewPager;
import com.android.hcframe.HcLog;
import com.android.hcframe.data.AppInfo;
import com.android.hcframe.data.HcAppData;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.HcDraggableGridViewPager.OnRearrangeListener;
import com.android.hcframe.market.MarketViewLandPager.AppObservable;
import com.android.hcframe.menu.MenuBaseActivity;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshBase.Mode;
import com.android.hcframe.pull.PullToRefreshDraggableGridView;
import com.android.hcframe.pull.PullToRefreshBase.OnRefreshListener;
import com.android.hcframe.sql.OperateDatabase;

/**
 * 未完善
 * @author jrjin
 * @time 2015-11-30 下午3:53:20
 */
public class AppItemLandView extends AbstractPage implements OnRearrangeListener,
	OnItemLongClickListener, OnItemClickListener {

	private static final String TAG = "AppItemLandView";
	
	private PullToRefreshDraggableGridView mGridViewPager;
	
	private RelativeLayout mNextPage;
	
	private AppAdapter mAdapter;
	
	private List<AppInfo> mAppInfos = new ArrayList<AppInfo>();
	
	private final int mCategory ;
	/**
	 * @deprecated
	 */
	private MenuBaseActivity mActivity;
	
	public AppItemLandView(Activity context, ViewGroup group, int category) {
		super(context, group);
		// TODO Auto-generated constructor stub
		mCategory = category;
		/**
		 * @date 2015-11-30 下午3:34:22
		mActivity = (MenuBaseActivity) context;
		*/
	}

	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub
		if (mGridViewPager != null)
			mGridViewPager.onRefreshComplete();
		if (observable instanceof AppObservable && data != null && 
				data instanceof AppCategoryInfo) {
			AppCategoryInfo info = (AppCategoryInfo) data;
//			HcLog.D(TAG + " update category = "+mCategory + " info category = "+info.getCategoryTag());
			if (mCategory == info.getCategoryTag()) {
				HcLog.D(TAG + " update category name = "+info.getCategoryName());
				
				mAppInfos.clear();				
				mAppInfos.addAll(info.getApps());
				if (mCategory != HcAppData.APP_CATEGORY_ALL) {
					Collections.sort(mAppInfos, new Comparator<AppInfo>() {

						@Override
						public int compare(AppInfo lhs, AppInfo rhs) {
							// TODO Auto-generated method stub	
							return lhs.getCategoryOrder() < rhs.getCategoryOrder() ? -1 : (lhs.getCategoryOrder() == rhs.getCategoryOrder() ? 0 : 1);
						}
						
					});
				}
				// 变更HcDraggableGridViewPager的高度
				HcLog.D(TAG + " update info size = "+mAppInfos.size());
				
				mAdapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		AppInfo info = mAppInfos.get(position);
		HcLog.D(TAG + " onItemClick info =" +info);
		if (!info.hasUsed()) {
			info.setUsed(1);
			mAdapter.notifyDataSetChanged();
//			mActivity.notifyObservers();
			MarketOperateDatabase.updateAppUsed(info, mContext /**mActivity*/);
		}	
		/**
		 * @author jrjin
		 * @date 2015-11-30 下午2:07:33
		 */
		info.startApp(mContext);
		/**
		mActivity.onOpenApp(info);
		*/
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void onRearrange(int oldIndex, int newIndex) {
		// TODO Auto-generated method stub
		AppInfo info = mAppInfos.get(oldIndex);
		mAppInfos.remove(info);
		mAppInfos.add(newIndex, info);		
		mAdapter.notifyDataSetChanged();
		
		int size = mAppInfos.size();
		if (mCategory == HcAppData.APP_CATEGORY_ALL) {
			for (int i = 0; i < size; i++) {
				mAppInfos.get(i).setAllOrder(i);
			}
		} else {
			for (int i = 0; i < size; i++) {
				mAppInfos.get(i).setCategoryOrder(i);
			}
		}
		HcAppData.getInstance().setSorted();
	}

	@Override
	public void initialized() {
		// TODO Auto-generated method stub
		if (mAdapter == null) {
			mAdapter = new AppAdapter(mContext, mAppInfos);
			mGridViewPager.setAdapter(mAdapter);
			/**
			 * @date 2015-11-30 下午1:58:51
			 */
			HcAppData.getInstance().addCallbacks(mAdapter);
			/**
			 * @date 2015-11-30 下午1:59:02
			 * 替换为HcAppData中的addCallbacks()
			mActivity.addCallbacks(mAdapter);
			*/
		} 
	}

	@Override
	public void setContentView() {
		// TODO Auto-generated method stub
		if (isFirst) {
			isFirst = !isFirst;
			mView = mInflater.inflate(R.layout.market_viewpage_content, null);
			
			mGridViewPager = (PullToRefreshDraggableGridView) mView.findViewById(R.id.market_gridview_pager);
			
			mNextPage = (RelativeLayout) mView.findViewById(R.id.market_next_page_btn);
			
			mGridViewPager.setOnItemClickListener(this);
			mGridViewPager.setOnItemLongClickListener(this);
			mGridViewPager.setOnRearrangeListener(this);
			
			if (mCategory != HcAppData.APP_CATEGORY_ALL)
				mGridViewPager.setMode(Mode.DISABLED);
			
			mGridViewPager.setScrollingWhileRefreshingEnabled(false);
			
			mGridViewPager.setOnRefreshListener(new OnRefreshListener<HcDraggableGridViewPager>() {

				@Override
				public void onRefresh(
						PullToRefreshBase<HcDraggableGridViewPager> refreshView) {
					// TODO Auto-generated method stub
					HcLog.D(TAG + " onRefresh!");
					/**
					 * @author jrjin
					 * @date 2015-11-30 下午3:55:57
					 */
					HcAppData.getInstance().refreshApps(HcAppData.APP_CATEGORY_ALL, RequestCategory.APP_ALL);
					/**
					mActivity.refreshApps(HcAppData.APP_CATEGORY_ALL, RequestCategory.APP_ALL);
					*/
				}
			});
		}
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		HcAppData.getInstance().removeUpdateCallback(mAdapter);
		mGridViewPager = null;
		mAdapter = null;
		mAppInfos.clear();
		mAppInfos = null;
		mView = null;
		mActivity = null;
	}

}