/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-28 下午11:36:02
*/
package com.android.hcframe.internalservice.news;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.ad.AdPanelView;
import com.android.hcframe.data.AbstractColumn;
import com.android.hcframe.data.HcNewsData;
import com.android.hcframe.data.NewsColumn;
import com.android.hcframe.data.NewsInfo;
import com.android.hcframe.data.NewsPageData;
import com.android.hcframe.data.HcNewsData.NotifyData;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.ResponseNewsInfo;
import com.android.hcframe.menu.DownloadPDFActivity;
import com.android.hcframe.menu.MenuBaseActivity;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshBase.Mode;
import com.android.hcframe.pull.PullToRefreshBase.OnLastItemVisibleListener;
import com.android.hcframe.pull.PullToRefreshBase.OnRefreshBothListener;
import com.android.hcframe.pull.PullToRefreshListView;
import com.android.hcframe.servicemarket.photoscan.ImageScanActivity;
import com.android.hcframe.view.toast.NoDataView;

public class NewsItemPage extends AbstractPage implements OnItemClickListener {

	private static final String TAG = "NewsItemPage";
	
	private AdPanelView mPanelView;
	
	private PullToRefreshListView mListView;
	
	private NewsColumn mColumn;
	
	private NewsItemAdapter mAdapter;
	/** 页面信息 */
	private NewsPageData mPageData;
	
	/** 新闻栏目下的列表 */
	private List<NewsInfo> mNewsInfos = new ArrayList<NewsInfo>();
	
//	private TextView mEmptyText;
	
	private LinearLayout mAdParent;

	private NoDataView mNoDataView;

	protected NewsItemPage(Activity context, ViewGroup group) {
		super(context, group);
		// TODO Auto-generated constructor stub
		HcNewsData.getInstance().addObserver(this);
	}

	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub
		if (observable instanceof HcNewsData && data != null && data instanceof NotifyData) {
			mListView.onRefreshComplete();
			NotifyData notifyData = (NotifyData) data;
			if (notifyData.mId.equals(mColumn.getNewsId())) {
				switch (notifyData.mRequest) {
				case NEWS_LIST:
					switch (notifyData.mType) {
					case SUCCESS: // mEmptyText需要显示处理
						updateNewsData(false);
//						mEmptyText.setText("暂无数据！");
						break;
					case NETWORK_ERROR:
						HcUtil.toastNetworkError(mContext);
//						mEmptyText.setText("网络不给力！");
						break;
					case SYSTEM_ERROR:
						HcUtil.toastSystemError(mContext, data);
//						mEmptyText.setText("系统错误！");
						break;
					case DATA_ERROR:
						HcUtil.toastDataError(mContext);
//						mEmptyText.setText("数据错误！");
						break;
					case SESSION_TIMEOUT:
						HcUtil.toastTimeOut(mContext);
//						mEmptyText.setText("网络不给力！");
						break;
					default:
						break;
					}
					break;
				case NEWSSCROLL:
					switch (notifyData.mType) {
					case SUCCESS:
						updateScrollData(false);
						break;

					default:
						break;
					}
					break;

				default:
					break;
				}
				
				
			}
			
		} else if (observable instanceof HcNewsData && data != null && data instanceof ResponseNewsInfo) {
			mListView.onRefreshComplete();
			ResponseNewsInfo info = (ResponseNewsInfo) data;
			if (info.getCode() == HcHttpRequest.REQUEST_ACCOUT_EXCLUDED ||
					info.getCode() == HcHttpRequest.REQUEST_TOKEN_FAILED) {
				HcUtil.reLogining(info.getBodyData(),mContext, info.getMsg());
			}else {
			if (info.getNewsId().equals(mColumn.getNewsId()))
				HcUtil.showToast(mContext, info.getMsg());
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
		
	}

	@Override
	public void setContentView() {
		// TODO Auto-generated method stub
		if (mView == null) {
			mView = mInflater.inflate(R.layout.news_pager_layout, null);

			mNoDataView = (NoDataView) mView.findViewById(R.id.news_pager_no_data);

			mListView = (PullToRefreshListView) mView.findViewById(R.id.news_pager_listview);
			
			mListView.setOnItemClickListener(this);
			
//			mEmptyText = (TextView) mView.findViewById(R.id.listview_empty_text);
			
			mListView.setScrollingWhileRefreshingEnabled(false);
			
			mListView.setOnRefreshBothListener(new OnRefreshBothListener<ListView>() {

				@Override
				public void onPullDownToRefresh(
						PullToRefreshBase<ListView> refreshView) {
					// TODO Auto-generated method stub
					if (HcUtil.isNetWorkAvailable(mContext)) {
//						mEmptyText.setText("正在获取数据...");
						HcNewsData.getInstance().getNewsList(HcNewsData.GET_DATA_REFRESH, mPageData, mColumn.getNewsId());
					} else {
//						mEmptyText.setText("网络不给力！");
						mListView.onRefreshComplete();
						HcUtil.toastNetworkError(mContext);
					}
					
				}

				@Override
				public void onPullUpToRefresh(
						PullToRefreshBase<ListView> refreshView) {
					// TODO Auto-generated method stub
					if (HcUtil.isNetWorkAvailable(mContext)) {
						HcNewsData.getInstance().getNewsList(HcNewsData.GET_DATA_MORE, mPageData, mColumn.getNewsId());
					} else {
						mListView.onRefreshComplete();
						HcUtil.toastNetworkError(mContext);
					}
					
				}
			});
			
//			mListView.setEmptyView(mView.findViewById(R.id.listview_empty_view));
			mListView.setEmptyView(mNoDataView);
			mListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

				@Override
				public void onLastItemVisible() {
					// TODO Auto-generated method stub
					if (mListView.getMode() == Mode.PULL_FROM_START) {
						HcUtil.showToast(mContext, "没有更多数据！");
					}
				}
			});
			
			View panelView = mInflater.inflate(R.layout.news_adpanel_layout, null);
			
			mAdParent = (LinearLayout) panelView.findViewById(R.id.news_adpanelview_parent);
			mPanelView = (AdPanelView) panelView.findViewById(R.id.news_adpanelview);
			
			mPanelView.getLayoutParams().height = HcUtil.getScreenWidth() / 5 * 2;
		}
	}

	/**
	 * 设置当前页面的栏目信息
	 * 1.判断是否需要显示栏目滚动图片
	 * 2.设置页面属性(包括刷新模式、列表信息和当前页码)
	 * 3.更新适配器数据
	 * 4.页面数据获取放到{@link #onResume()}里面完成
	 * @author jrjin
	 * @time 2015-12-29 上午9:04:48
	 * @param column
	 */
	public void setNewsColumm(NewsColumn column) {
		mColumn = column;
		HcLog.D(TAG + " #setNewsColumm scroll = "+column.getIsSrolltopic() + " adView = "+mPanelView);
		updateNewsData(true);
		
		if (mColumn.getIsSrolltopic() == AbstractColumn.UNSCROLLING) {
			if (mPanelView != null) {
				mPanelView.setVisibility(View.GONE);
				mPanelView.pause();
			}
		} else {
			if (mPanelView != null) {
				mPanelView.setVisibility(View.VISIBLE);
				
			}
			updateScrollData(true);
		}
		
	}

	/**
	 * 
	 * @author jrjin
	 * @time 2015-12-29 上午10:49:30
	 * @param refresh 是否需要去刷新
	 */
	private void updateNewsData(boolean refresh) {
		mPageData = HcNewsData.getInstance().getNewsPageData(mColumn.getNewsId());
		mListView.setMode(mPageData.mMode);
		mNewsInfos.clear();
		mNewsInfos.addAll(mPageData.mInfos);
		if (mAdapter == null) {
			mAdapter = new NewsItemAdapter(mContext, mNewsInfos);
			mListView.addHeaderView(mAdParent, null, false);
			mListView.setAdapter(mAdapter);
		} else {
			mAdapter.notifyDataSetChanged();
		}
		
		if (mPageData.mInfos.size() == 0 && refresh) {
			if (HcUtil.isNetWorkAvailable(mContext)) {
//				mEmptyText.setText("正在获取数据...");
				HcNewsData.getInstance().getNewsList(HcNewsData.GET_DATA_REFRESH, mPageData, mColumn.getNewsId());
			} else {
//				mEmptyText.setText("网络不给力！");
				HcUtil.toastNetworkError(mContext);
			}
			
		}

	}
	
	private void updateScrollData(boolean refresh) {
		if (mPanelView != null) {
			List<NewsInfo> infos = HcNewsData.getInstance().getScrollNewsInfos(mColumn.getNewsId());
			if (infos.size() == 0 && refresh) {
				HcNewsData.getInstance().getScrollList(mColumn.getNewsId());
				return;
			}
			mPanelView.createLayout(infos);
		}
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		NewsInfo info = (NewsInfo) parent.getItemAtPosition(position);
		if (TextUtils.isEmpty(info.mContentType)) return;
		int type = Integer.valueOf(info.mContentType);
		switch (type) {
		case 0:
		case 2:
			if (mContext instanceof MenuBaseActivity) {
				((MenuBaseActivity) mContext)
						.startHtmlActivity(info.mContentUrl);
			}
			break;
		case 1:
			startPDFActivity(info.mContentUrl, info.mTitle);
			break;
		case 3:
			startImageScanActivity(0, info.mImgs, info.mId);
			break;

		default:
			break;
		}
		
	}

	private void startImageScanActivity(int position, ArrayList<String> iamgeUrls,
			String newsId) {
		Intent intent = new Intent(mContext, ImageScanActivity.class);
		// 图片url,为了演示这里使用常量，一般从数据库中或网络中获取
		intent.putExtra(ImageScanActivity.EXTRA_IMAGE_URLS, iamgeUrls);
		intent.putExtra(ImageScanActivity.EXTRA_IMAGE_INDEX, position);
		intent.putExtra(ImageScanActivity.EXTRA_IMAGE_ID, newsId);
		mContext.startActivity(intent);
	}
	
	private void startPDFActivity(String url, String title) {
		Intent intent = new Intent(mContext, DownloadPDFActivity.class);
		intent.putExtra("url", url);
		intent.putExtra("title", title);
		mContext.startActivity(intent);
		mContext.overridePendingTransition(0, 0);
	}

	@Override
	public void onDestory() {
		// TODO Auto-generated method stub
		HcNewsData.getInstance().deleteObserver(this);
		if (mPanelView != null)
			mPanelView.pause();
		super.onDestory();
		mContext = null;
	}
	
	
}
