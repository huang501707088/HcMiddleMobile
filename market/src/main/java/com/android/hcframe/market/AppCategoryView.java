/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-5-21 上午11:31:38
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcDraggableGridViewPager;
import com.android.hcframe.HcLog;
import com.android.hcframe.data.AppInfo;
import com.android.hcframe.data.HcAppData;
import com.android.hcframe.HcDraggableGridViewPager.OnRearrangeListener;
import com.android.hcframe.market.MarketViewPort.AppObservable;
import com.android.hcframe.menu.MenuBaseActivity;

/**
 * 应用超市不分页显示
 * @author jrjin
 * @time 2015-11-30 下午1:45:19
 */
public class AppCategoryView extends AbstractPage implements OnRearrangeListener,
	OnItemLongClickListener, OnItemClickListener {

	private static final String TAG = "AppCategoryView";

	/**
	 * @deprecated
	 */
	private MenuBaseActivity mActivity;

	private final int mCategory ;

	private TextView mCategoryName;

	private HcDraggableGridViewPager mGridViewPager;

	private AppAdapter mAdapter;

	private List<AppInfo> mAppInfos = new ArrayList<AppInfo>();

	private View mDivider;

	public AppCategoryView(Activity context, ViewGroup group, int category) {
		super(context, group);
		// TODO Auto-generated constructor stub
		/**
		 * @date 2015-11-30 下午3:39:41
		mActivity = (MenuBaseActivity) context;
		*/
		mCategory = category;
	}

	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub
//		HcLog.D(TAG + " update observable = "+observable + " data = "+data);
		if (observable instanceof AppObservable && data != null &&
				data instanceof AppCategoryInfo) {
			AppCategoryInfo info = (AppCategoryInfo) data;
//			HcLog.D(TAG + " update category = "+mCategory + " info category = "+info.getCategoryTag());
			if (mCategory == info.getCategoryTag()) {
				HcLog.D(TAG + " update category name = "+info.getCategoryName());
				mCategoryName.setText(info.getCategoryName());
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
				mGridViewPager.setHeight(mAppInfos.size());
				mAdapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

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
			mActivity.addCallbacks(mAdapter);
			*/
		}
	}

	@Override
	public void setContentView() {
		// TODO Auto-generated method stub
		if (isFirst) {
			isFirst = !isFirst;
			mView = mInflater.inflate(R.layout.item_market_no_page, null);

			mCategoryName = (TextView) mView.findViewById(R.id.market_category_name);

			mGridViewPager = (HcDraggableGridViewPager) mView.findViewById(R.id.market_gridview_no_page);

			mDivider = mView.findViewById(R.id.market_category_divider);

			mGridViewPager.setOnItemClickListener(this);
			mGridViewPager.setOnItemLongClickListener(this);
			mGridViewPager.setOnRearrangeListener(this);

			if (mCategory == HcAppData.APP_CATEGORY_ALL)
				mDivider.setVisibility(View.GONE);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		AppInfo info = mAppInfos.get(position);
		HcLog.D(TAG + " onItemClick info =" +info);
		if (!info.hasUsed()) {
			info.setUsed(1);
//			mAdapter.notifyDataSetChanged();
			/**
			 * @author jrjin
			 * @date 2015-11-30 下午3:22:47
			 */
			HcAppData.getInstance().notifyObservers();
			/**
			 * @date 2015-11-30 下午3:24:42
			mActivity.notifyObservers();
			*/
			MarketOperateDatabase.updateAppUsed(info, /**mActivity*/mContext);
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
			HcLog.D(TAG + " onRearrange mCategory = "+mCategory);
			for (int i = 0; i < size; i++) {
				mAppInfos.get(i).setCategoryOrder(i);
			}
		}

		HcAppData.getInstance().setSorted();
	}

	/**
	 * @deprecated
	 * @author jrjin
	 * @time 2015-11-30 下午3:25:46
	 */
	public void notifyDateSetChanged() {
		mAdapter.notifyDataSetChanged();
	}

	public int getChildCount() {
		return mGridViewPager.getChildCount();
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		HcAppData.getInstance().removeUpdateCallback(mAdapter);
		mCategoryName = null;
		mGridViewPager = null;
		mAdapter = null;
		mAppInfos.clear();
		mAppInfos = null;
		mDivider = null;
		mView = null;
		mActivity = null;
	}

}
