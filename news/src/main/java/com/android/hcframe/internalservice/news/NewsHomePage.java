/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-28 下午4:26:18
*/
package com.android.hcframe.internalservice.news;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.ad.RecyclingPagerAdapter;
import com.android.hcframe.badge.BadgeCache;
import com.android.hcframe.badge.BadgeObserver;
import com.android.hcframe.data.HcNewsData;
import com.android.hcframe.data.NewsColumn;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.menu.DownloadPDFActivity;
import com.android.hcframe.push.HcPushManager;
import com.android.hcframe.push.PushHtmlActivity;
import com.android.hcframe.push.PushInfo;
import com.android.hcframe.servicemarket.photoscan.ImageScanActivity;
import com.android.hcframe.view.tab.HcTabPageIndicator;
import com.android.hcframe.view.tab.HcTabPageIndicator.ViewHolderBase;
import com.android.hcframe.view.tab.HcTabPageIndicator.ViewHolderCreator;

public class NewsHomePage extends AbstractPage {

    private static final String TAG = "NewsHomePage";

    private final String mAppId;

    private ViewPager mPager;

    private HcTabPageIndicator mIndicator;

    private NewsPageAdapter mAdapter;

    private List<NewsColumn> mColumns = new ArrayList<NewsColumn>();

    private Set<NewsItemPage> mPages = new HashSet<NewsItemPage>();

    public NewsHomePage(Activity context, ViewGroup group, String appId) {
        super(context, group);
        // TODO Auto-generated constructor stub
        mAppId = appId;
        HcNewsData.getInstance().addObserver(this);
    }

    @Override
    public void update(Observable observable, Object data) {
        // TODO Auto-generated method stub
        if (observable instanceof HcNewsData && data != null && data == RequestCategory.NEWSCOLUMN) {
            mColumns.clear();
            mColumns.addAll(HcNewsData.getInstance().getNewsColumns(mContext));
            if (mColumns.size() < 2) {
                if (mIndicator.getVisibility() != View.GONE) {
                    mIndicator.setVisibility(View.GONE);
                }
            } else {
                if (mIndicator.getVisibility() != View.VISIBLE) {
                    mIndicator.setVisibility(View.VISIBLE);
                }
            }
            HcLog.D(TAG + "#update columns size = " + mColumns.size());
            mAdapter.notifyDataSetChanged();
            HcLog.D(TAG + "#update mAdapter size = " + mAdapter.getCount());
            mIndicator.notifyDataSetChanged();
            mIndicator.moveToItem(-1);
            mIndicator.moveToItem(0);
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
            mAdapter = new NewsPageAdapter();
//			mPager.setAdapter(mAdapter);
        }
//		mColumns.clear();
//		mColumns.addAll(HcNewsData.getInstance().getNewsColumns(mContext));

//		mAdapter.notifyDataSetChanged();
        // 每次第一次进页面的时候重新去获取栏目列表
//		mIndicator.setViewPager(mPager, 0);
//		HcNewsData.getInstance().refreshColumns();
    }

    @Override
    public void setContentView() {
        // TODO Auto-generated method stub
        if (mView == null) {
            mView = mInflater.inflate(R.layout.news_home_layout, null);

            mIndicator = (HcTabPageIndicator) mView.findViewById(R.id.news_home_indicator);

            mPager = (ViewPager) mView.findViewById(R.id.news_home_pager);

            mIndicator.setVisibilityCount(6);
            mIndicator.setJumpPager(mJumpPager);
            mIndicator.setViewHolderCreator(new ViewHolderCreator() {

                @Override
                public ViewHolderBase createViewHolder() {
                    // TODO Auto-generated method stub
                    return new IndicatorViewHolder();
                }
            });

        }
    }

    private class IndicatorViewHolder extends HcTabPageIndicator.ViewHolderBase {

        private TextView mTitleTextView;
        private View mViewSelected;
//        private MenuTextView menuTextView;
        private final int COLOR_TEXT_SELECTED = Color.parseColor("#51afe6");
        private final int COLOR_TEXT_NORMAL = Color.parseColor("#000000");

        @Override
        public View createView(LayoutInflater layoutInflater, int position) {
            View view = layoutInflater.inflate(
                    R.layout.news_topbar_indicator_layout, null);
            mTitleTextView = (TextView) view
                    .findViewById(R.id.news_topbar_item_title);
            mViewSelected = view
                    .findViewById(R.id.news_topbar_item_selected);
//            menuTextView = (MenuTextView) view.findViewById(R.id.point);

            return view;
        }

        @Override
        public void updateView(int position, boolean isCurrent) {
            NewsColumn ni = mColumns.get(position);
            mTitleTextView.setText(ni.getmName());
            BadgeCache.getInstance().addBadgeObserver(mAppId + "_" +
                            ni.getNewsId(), (BadgeObserver) mTitleTextView
            );

            if (isCurrent) {
                mTitleTextView.setTextColor(COLOR_TEXT_SELECTED);
                mViewSelected.setVisibility(View.VISIBLE);

            } else {
                mTitleTextView.setTextColor(COLOR_TEXT_NORMAL);
                mViewSelected.setVisibility(View.INVISIBLE);
            }
        }

//        @Override
//        public void JumpPager(int position) {
//            NewsColumn ni = mColumns.get(position);
//            BadgeCache.getInstance().removeAllBadgeObserver(ni.getNewsId());
//        }
    }


    private class NewsPageAdapter extends RecyclingPagerAdapter {

        private static final String TAG = NewsHomePage.TAG + "$NewsPageAdapter";

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            // TODO Auto-generated method stub
            HcLog.D(TAG + " #getView position = " + position + " convertView = " + convertView /*+ " ViewGroup = "+container*/);
            NewsItemPage itemPage;
            if (convertView == null) {
                itemPage = new NewsItemPage(mContext, null);
                itemPage.changePages();
                convertView = itemPage.getContentView();
                convertView.setTag(itemPage);
                mPages.add(itemPage);
            } else {
                itemPage = (NewsItemPage) convertView.getTag();
            }
            itemPage.setNewsColumm(mColumns.get(position));
            itemPage.onResume();
            return convertView;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            if (mColumns != null) return mColumns.size();
            return 0;
        }

    }


    @Override
    public void onDestory() {
        // TODO Auto-generated method stub
        HcNewsData.getInstance().deleteObservers();
        mColumns.clear();
//		mAdapter.notifyDataSetChanged();
        if (mIndicator != null)
            mIndicator.notifyDataSetChanged();
        HcLog.D(TAG + " #onDestory page size = " + mPages.size());
        for (NewsItemPage page : mPages) {
            page.onDestory();
        }
        mPages.clear();
        mContext = null;
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        PushInfo info = HcPushManager.getInstance().getPushInfo();
        if (info != null) {
            HcPushManager.getInstance().setPushInfo(null);
            if (mAppId.equals(info.getAppId())) {
                Intent intent = new Intent();
                int type = Integer.valueOf(info.getType());
                switch (type) {
                    case PushInfo.TYPE_URL:
                    case PushInfo.TYPE_ONLINE:
                    case PushInfo.TYPE_VIDEO:
                        intent.setClass(mContext, PushHtmlActivity.class);
                        intent.putExtra("id", info.getContent());
//                        intent.setClass(mContext, HtmlActivity.class);
//                        intent.putExtra("url", info.getContent());
//                        intent.putExtra("title", info.getAppName());
                        break;
                    case PushInfo.TYPE_IMAGE:
                        intent.setClass(mContext, ImageScanActivity.class);
                        intent.putExtra(ImageScanActivity.EXTRA_IMAGE_ID,
                                info.getContent());
                        break;
                    case PushInfo.TYPE_PDF:
                        intent.setClass(mContext, DownloadPDFActivity.class);
                        intent.putExtra("url", info.getContent());
                        intent.putExtra("title", "资料详情");
//                        intent.setPackage(mContext.getPackageName());
//                        intent.setAction("com.android.hcframe.start_doc");
//                        intent.putExtra("data_id", info.getContent());
//                        intent.putExtra("data_flag", DocInfo.FLAG_TITIL);
//                        mContext.sendBroadcast(intent);
                        break;

                    default:
                        return;
                }

                mContext.startActivity(intent);
                mContext.overridePendingTransition(0, 0);
            }
        }
        if (isFirst) {
            isFirst = !isFirst;
            HcLog.D(TAG + " #onResume start time = " + HcUtil.getDate(HcUtil.FORMAT_HOUR, System.currentTimeMillis()));
            mColumns.clear();
            mColumns.addAll(HcNewsData.getInstance().getNewsColumns(mContext));
            if (mColumns.size() < 2) {
                if (mIndicator.getVisibility() != View.GONE) {
                    mIndicator.setVisibility(View.GONE);
                }
            } else {
                if (mIndicator.getVisibility() != View.VISIBLE) {
                    mIndicator.setVisibility(View.VISIBLE);
                }
            }
            mPager.setAdapter(mAdapter);
            mIndicator.setViewPager(mPager, 0);
            HcNewsData.getInstance().refreshColumns();
            HcLog.D(TAG + " #onResume end time = " + HcUtil.getDate(HcUtil.FORMAT_HOUR, System.currentTimeMillis()));
        }

    }

    private HcTabPageIndicator.JumpPager mJumpPager = new HcTabPageIndicator.JumpPager() {
        @Override
        public void jumpPager(int position) {
            NewsColumn ni = mColumns.get(position);
            BadgeCache.getInstance().operateBadge(mAppId + "_" +
                    ni.getNewsId());
//            BadgeCache.getInstance().removeAllBadgeObserver(ni.getNewsId());
        }
    };

}
