/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-5-8 下午2:47:15
*/
package com.android.hcframe.market;

import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcObserver;
import com.android.hcframe.HcSubject;
import com.android.hcframe.HcUtil;
import com.android.hcframe.data.AppInfo;
import com.android.hcframe.data.HcAppData;
import com.android.hcframe.data.HcAppReceiver;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.http.ResponseCodeInfo;
import com.android.hcframe.menu.MenuBaseActivity;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshBase.OnRefreshListener;
import com.android.hcframe.pull.PullToRefreshScrollView;

public class MarketViewPort extends AbstractPage implements HcObserver {

    private static final String TAG = "MarketViewPort";
    /**
     * @deprecated
     */
    private MenuBaseActivity mActivity;

    private LinearLayout mParent;

    private Map<Integer, AppCategoryView> mCategory = new TreeMap<Integer, AppCategoryView>();

    private AppObservable mObservable;

    private PullToRefreshScrollView mRefreshScrollView;

    private HcAppReceiver mReceiver;

    public MarketViewPort(Activity context, ViewGroup group) {
        super(context, group);
        // TODO Auto-generated constructor stub
        mActivity = (MenuBaseActivity) context;
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
        /**
         * @date 2015-11-30 下午3:04:53
        notifyDataSetChanged();
         */
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
            mView = mInflater.inflate(R.layout.market_content_port, null);

            mParent = (LinearLayout) mView.findViewById(R.id.market_categoryapp_parent);

            /**
             * @author jrjin
             * @date 2015-11-30 下午2:01:07
             */
            HcAppData.getInstance().addObserver(this);
            /**
             * @author jrjin
             * @date 2015-11-30 下午3:06:10
            mActivity.addObserver(this);
            mActivity.addAdapterObserver(this);
             */

            mRefreshScrollView = (PullToRefreshScrollView) mView.findViewById(R.id.market_content_port_pullscrollview);
            mRefreshScrollView.setScrollingWhileRefreshingEnabled(false);
            mRefreshScrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

                @Override
                public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                    // TODO Auto-generated method stub
                    /**
                     * @author jrjin
                     * @date 2015-11-30 下午2:03:13
                     */
                    HcAppData.getInstance().refreshApps(HcAppData.APP_CATEGORY_ALL, RequestCategory.APP_ALL);
                    /**
                     * @date 2015-11-30 下午3:08:24
                    mActivity.refreshApps(HcAppData.APP_CATEGORY_ALL, RequestCategory.APP_ALL);
                     */
                }
            });

        }
    }

    @Override
    public void updateData(HcSubject subject, Object data,
                           RequestCategory request, ResponseCategory response) {
        // TODO Auto-generated method stub
        if (request != null && request == RequestCategory.APP_ALL) {
            if (mRefreshScrollView != null)
                mRefreshScrollView.onRefreshComplete();
            if (data != null && data instanceof List<?>) {
                List<AppInfo> infos = (List<AppInfo>) data;
                // 解析App的类型
                parseApps(infos);
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

    /**
     * 原生应用更新，安装
     *
     * @author jrjin
     * @time 2015-5-27 下午2:56:01
     * @deprecated
     */
    private void notifyDataSetChanged() {
        for (AppCategoryView view : mCategory.values()) {
            view.notifyDateSetChanged();
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

        // 先匹配view,有就更新里面的数据，没有就开始创建View
        matchViews(category);
    }

    private void matchViews(Map<Integer, AppCategoryInfo> category) {
        AppCategoryView view = null;
        Set<Integer> oldCategories = new TreeSet<Integer>(mCategory.keySet()); // 为了检查是否需要删除已有的View,因为类别可能会有变化
        Set<Integer> categories = category.keySet();
        for (Integer key : categories) {
            view = mCategory.get(key);
            if (view == null) { // 说明该列表没有，需要添加
                view = new AppCategoryView(/**mActivity*/mContext, null, key);
                mObservable.addObserver(view);
                view.changePages();
                mCategory.put(key, view);
                mObservable.notifyApp(category.get(key));
                mParent.addView(view.getContentView());
            } else {
                oldCategories.remove(key);  //
                // 更新数据
                mObservable.notifyApp(category.get(key));
            }
        }

        HcLog.D(TAG + " matchViews oldCategories size = " + oldCategories.size());

        for (Integer integer : oldCategories) { // 删除多余的View，本来可以再利用。
            AppCategoryView categoryView = mCategory.remove(integer);
            mObservable.deleteObserver(categoryView);
            mParent.removeView(categoryView.getContentView());
        }

//		for (AppCategoryView categoryView : mCategory.values()) {
//			HcLog.D(TAG + " matchViews childCount = "+categoryView.getChildCount());
//		}
    }

    public class AppObservable extends Observable {

        public AppObservable() {
        }

        private void notifyApp(AppCategoryInfo info) {
            setChanged();
            notifyObservers(info);
        }
    }

    @Override
    public void release() {
        // TODO Auto-generated method stub
        /**
         mActivity.removeAdapterObserver(this);
         mActivity.removeObserver(this);
         */
        HcAppData.getInstance().removeObserver(this);
        for (AppCategoryView view : mCategory.values()) {
            view.release();
            mObservable.deleteObserver(view);
            view = null;
        }
        mCategory.clear();
        mCategory = null;
        mObservable = null;

        if (mParent != null) {
            mParent.removeAllViews();
            mParent = null;
        }


        mRefreshScrollView = null;

        mView = null;

        mActivity = null;

        mContext = null;
        mGroup = null;
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        /**
         * @author jrjin
         * @date 2015-11-30 下午2:08:50
         */
        HcAppData.getInstance().getAppList(HcAppData.APP_CATEGORY_ALL, RequestCategory.APP_ALL, true);
        /**
         * @date 2015-11-30 下午2:53:37
        mActivity.getApps(HcAppData.APP_CATEGORY_ALL, RequestCategory.APP_ALL, true);
         */
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
