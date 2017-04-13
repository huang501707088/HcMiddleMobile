/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-5-1 上午10:25:35
*/
package com.android.hcframe.market;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.RelativeLayout;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcDraggableGridViewPager;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcObserver;
import com.android.hcframe.HcSubject;
import com.android.hcframe.HcUtil;
import com.android.hcframe.data.AppInfo;
import com.android.hcframe.data.HcAppData;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.http.ResponseCodeInfo;
import com.android.hcframe.HcDraggableGridViewPager.OnRearrangeListener;
import com.android.hcframe.menu.MenuBaseActivity;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshBase.Mode;
import com.android.hcframe.pull.PullToRefreshBase.OnRefreshListener;
import com.android.hcframe.pull.PullToRefreshDraggableGridView;

public class AppItemView extends AbstractPage implements OnRearrangeListener,
        OnItemLongClickListener, HcObserver, OnItemClickListener {

    private static final String TAG = "AppItemView";

    //	private HcDraggableGridViewPager mGridViewPager;
    private PullToRefreshDraggableGridView mGridViewPager;

    private RelativeLayout mNextPage;

    private AppAdapter mAdapter;

    private List<AppInfo> mAppInfos = new ArrayList<AppInfo>();

    private final int mCategory;

    /**
     * @date 2015-11-30 下午1:50:41
     * @see HcAppData
     * @deprecated
     */
    private MenuBaseActivity mActivity;
    @SuppressWarnings("以后应用超市分类型整改的时候,这个需要变动.")
    private final RequestCategory mRC;

    public AppItemView(Activity context, ViewGroup group, int category, RequestCategory cr) {
        super(context, group);
        mCategory = category;
        /**
         * @date 2015-11-30 下午2:14:53
        mActivity = (MenuBaseActivity) context;
         */
        mRC = cr;
        HcLog.D(TAG + " AppItemView RequestCategory = " + mRC + " mCategory = " + mCategory);
    }

    @Override
    public void update(Observable observable, Object data) {
        // TODO Auto-generated method stub
        /**
         * @date 2015-11-30 下午2:47:13
         * 现在在@HcAppData里面处理
        if (mAdapter != null)
        mAdapter.notifyDataSetChanged();
         */
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
            /**
             * @author jrjin
             * @date 2015-11-30 下午2:01:07
             */
            HcAppData.getInstance().addObserver(this);
            /**
             * @date 2015-11-30 下午2:00:39
             * 替换为HcAppData.getInstance().addObserver(this);
            mActivity.addObserver(this);
            mActivity.addAdapterObserver(this);
             */
//			mGridViewPager.setMode(Mode.BOTH);
            mGridViewPager.setScrollingWhileRefreshingEnabled(false);

            mGridViewPager.setOnRefreshListener(new OnRefreshListener<HcDraggableGridViewPager>() {

                @Override
                public void onRefresh(
                        PullToRefreshBase<HcDraggableGridViewPager> refreshView) {
                    // TODO Auto-generated method stub
                    HcLog.D(TAG + " onRefresh!");
                    /**
                     * @author jrjin
                     * @date 2015-11-30 下午2:03:13
                     */
                    HcAppData.getInstance().refreshApps(mCategory, mRC);
                    /**
                     * @date 2015-11-30 下午2:02:23
                    mActivity.refreshApps(mCategory, mRC);
                     */
                }
            });
        }
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
//		for (AppInfo appInfo : mAppInfos) {
//			HcLog.D(TAG + " #onRearrange name = "+appInfo.getAppName() + " all order = "+appInfo.getAllOrder());
//		}
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   int position, long id) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void updateData(HcSubject subject, Object data,
                           RequestCategory request, ResponseCategory response) {
        // TODO Auto-generated method stub
//		HcLog.D(TAG + " AppItemView updateData = "+request + " this = "+this +" same = "+(mRC == request));
        if (mGridViewPager != null)
            mGridViewPager.onRefreshComplete();
        if (request != null && mRC == request) {
            if (data != null && data instanceof List<?>) {
                mAppInfos.clear();
                mAppInfos.addAll((List<AppInfo>) data);
                mAdapter.notifyDataSetChanged();
//				for (AppInfo appInfo : mAppInfos) {
//					HcLog.D(TAG + " #updateData name = "+appInfo.getAppName() + " all order = "+appInfo.getAllOrder());
//				}
            } else if (data != null && data instanceof ResponseCodeInfo) {
                ResponseCodeInfo info = (ResponseCodeInfo) data;
                /**
                 * @author zhujb
                 * @date 2016-04-13 下午4:19:07
                 */
                if (info.getCode() == HcHttpRequest.REQUEST_ACCOUT_EXCLUDED ||
                        info.getCode() == HcHttpRequest.REQUEST_TOKEN_FAILED) {
                    HcUtil.reLogining(info.getBodyData(), mContext, info.getMsg());
                } else {
                    HcUtil.showToast(mContext, info.getMsg());
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        // TODO Auto-generated method stub
        // 注意 parent is null
        AppInfo info = mAppInfos.get(position);
        HcLog.D(TAG + " onItemClick info =" + info);
//		info.setUsed(1);
//		mAdapter.notifyDataSetChanged();
//		int sqlId = OperateDatabase.updateAppUsed(info, mActivity);
        if (!info.hasUsed()) {
            info.setUsed(1);
            mAdapter.notifyDataSetChanged();
            int sqlId = MarketOperateDatabase.updateAppUsed(info, mContext);
            HcLog.D(TAG + " onItemClick sqlId = " + sqlId);
        }
        /**
         * @author jrjin
         * @date 2015-11-30 下午2:07:33
         */
        info.startApp(mContext);
        /**
         * @date 2015-11-30 下午2:07:14
         * 替换为info.startApp(mContext);
        mActivity.onOpenApp(info);
         */
    }

    /**
     * @author jrjin
     * @time 2015-11-30 下午2:13:21
     * @see UpdateCallback#notifyDataChanged()
     * @deprecated
     */
    public void notifyDateSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        /**
         * @author jrjin
         * @date 2015-11-30 下午2:08:50
         */
        HcAppData.getInstance().getAppList(mCategory, mRC, mCategory == HcAppData.APP_CATEGORY_ALL);
        /**
         * @date 2015-11-30 下午2:08:57
        mActivity.getApps(mCategory, mRC, mCategory == HcAppData.APP_CATEGORY_ALL);
         */
    }

    @Override
    public void release() {
        // TODO Auto-generated method stub
        /**
         * @date 2015-11-30 下午2:09:57
        mActivity.removeAdapterObserver(this);
        mActivity.removeObserver(this);
         */
        HcLog.D(TAG + " it is in release!=============================");
        HcAppData.getInstance().removeUpdateCallback(mAdapter);
        HcAppData.getInstance().removeObserver(this);
        mGridViewPager = null;
        mAdapter = null;
        mAppInfos.clear();
        mAppInfos = null;

        mView = null;
        mActivity = null;

        mContext = null;
        mGroup = null;
    }

}
