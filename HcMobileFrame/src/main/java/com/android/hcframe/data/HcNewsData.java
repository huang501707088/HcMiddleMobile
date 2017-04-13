/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-7-16 上午11:21:34
 */
package com.android.hcframe.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;

import android.content.Context;

import com.android.hcframe.CacheManager;
import com.android.hcframe.HcApplication;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.ModuleInfo;
import com.android.hcframe.TemporaryCache;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.pull.PullToRefreshBase.Mode;
import com.android.hcframe.sql.OperateDatabase;
import com.android.hcframe.sql.SettingHelper;

public final class HcNewsData extends Observable implements IHttpResponse,
		TemporaryCache {

	private static final String TAG = "HcNewsData";

	private static final HcNewsData NEWS = new HcNewsData();

	/**
	 * key:新闻栏目ID value:新闻页信息包括当前页和列表
	 * */
	private Map<String, NewsPageData> mNewsCache = new HashMap<String, NewsPageData>();
	/**
	 * 新闻栏目缓存
	 */
	private List<NewsColumn> mNewsColumns = new ArrayList<NewsColumn>();

	/**
	 * key:新闻栏目ID value:滚动图片列表
	 */
	private Map<String, List<NewsInfo>> mScrollNewsCache = new HashMap<String, List<NewsInfo>>();
	/** 刷新 */
	public static final int GET_DATA_REFRESH = 0;
	/** 获取更多 */
	public static final int GET_DATA_MORE = 1;

	private static final String MODULE_ID = "news_update_catalog";

	private HcNewsData() {
		CacheManager.getInstance().addCache(this);
	}

	public static final HcNewsData getInstance() {
		return NEWS;
	}

	/**
	 * 获取栏目的列表 注意：页面中的列表和这里的公用同一套数据
	 * @deprecated
	 * @author jrjin
	 * @time 2015-7-16 上午9:49:23
	 * @param newsId
	 * @return 返回栏目的列表
	 */
	public List<NewsInfo> getNewsInfos(String newsId) {
		NewsPageData page = mNewsCache.get(newsId);
		if (null == page) {
			page = new NewsPageData();
			page.mId = newsId;
			mNewsCache.put(newsId, page);
		}
		return page.mInfos;
	}

	/**
	 * 根据栏目创建缓存； 注意：原先有的，后来删除的栏目缓存还在
	 *
	 * @author jrjin
	 * @time 2015-7-16 上午10:06:34
	 * @param columns
	 * @return
	 */
	public int createNewsCache(List<NewsColumn> columns) {
		NewsPageData page = null;
		for (NewsColumn newsColumn : columns) {
			page = mNewsCache.get(newsColumn.getNewsId());
			if (null == page) {
				NewsPageData data = new NewsPageData();
				data.mId = newsColumn.getNewsId();
				mNewsCache.put(newsColumn.getNewsId(), /*new NewsPageData()*/data);
			}
		}
		return mNewsCache.size();
	}

	/**
	 * 切换用户时，清空缓存。
	 *
	 * @author jrjin
	 * @time 2015-7-16 上午10:11:05
	 */
	public void clearNewsCache() {
		// 清除新闻栏目缓存
		mNewsColumns.clear();

		// 清除新闻列表缓存
		Iterator<NewsPageData> iterator = mNewsCache.values().iterator();
		NewsPageData data;
		while (iterator.hasNext()) {
			data = iterator.next();
			if (data.mInfos.size() <= HcUtil.NEWS_COUNT)
				NewsOperateDatabase.insertNewsList(HcApplication.getContext(), data.mInfos, data.mId, false);
			else {
				NewsOperateDatabase.insertNewsList(HcApplication.getContext(), data.mInfos.subList(0, HcUtil.NEWS_COUNT - 1), data.mId, false);
			}
			data.mInfos.clear();
		}
		mNewsCache.clear();

		// 清除滚动列表缓存
		Iterator<Entry<String, List<NewsInfo>>> iterator2 = mScrollNewsCache.entrySet().iterator();
		Entry<String, List<NewsInfo>> entry;
		while (iterator2.hasNext()) {
			entry = iterator2.next();
			NewsOperateDatabase.insertNewsList(HcApplication.getContext(), entry.getValue(), entry.getKey(), true);
			entry.getValue().clear();
		}
//		Iterator<List<NewsInfo>> iterator2 = mScrollNewsCache.values()
//				.iterator();
//		while (iterator2.hasNext()) {
//			iterator2.next().clear();
//		}
		mScrollNewsCache.clear();
	}

	/**
	 * 获取栏目的滚动图片列表 注意：页面中的列表和这里的公用同一套数据
	 *
	 * @author jrjin
	 * @time 2015-7-16 上午9:49:23
	 * @param newsId
	 * @return 返回栏目的列表
	 */
	public List<NewsInfo> getScrollNewsInfos(String newsId) {
		List<NewsInfo> infos = mScrollNewsCache.get(newsId);
		if (null == infos) {
			infos = new ArrayList<NewsInfo>();
			mScrollNewsCache.put(newsId, infos);
		}
		if (infos.size() == 0) {
			infos.addAll(NewsOperateDatabase.getNewsInfos(HcApplication.getContext(), newsId, true));
		}
		HcLog.D(TAG + " #getScrollNewsInfos info size = "+infos.size());
		return infos;
	}

	/**
	 * 获取栏目
	 *
	 * @author jrjin
	 * @time 2015-7-16 上午11:45:16
	 * @return
	 */
	public List<NewsColumn> getNewsColumns(Context mActivity) {
		if (mNewsColumns.size() == 0) {
			// 去数据库获取
			mNewsColumns.addAll(OperateDatabase.queryNews(mActivity));
		}
		HcLog.D(TAG + " #getNewsColumns column size = "+mNewsColumns.size());
		return mNewsColumns;
	}

	/**
	 * 根据栏目创建滚动图片缓存； 注意：原先有的，后来删除的栏目缓存还在
	 *
	 * @author jrjin
	 * @time 2015-7-16 上午10:06:34
	 * @param columns
	 * @return
	 */
	public int createScrollNewsCache(List<NewsColumn> columns) {
		List<NewsInfo> infos = null;
		for (NewsColumn newsColumn : columns) {
			if (newsColumn.getIsSrolltopic() == NewsColumn.SCROLLING) {
				infos = mScrollNewsCache.get(newsColumn.getNewsId());
				if (null == infos) {
					mScrollNewsCache.put(newsColumn.getNewsId(),
							new ArrayList<NewsInfo>());
				}
			}

		}
		return mScrollNewsCache.size();
	}

	/**
	 * 更新栏目，重新设置
	 *
	 * @author jrjin
	 * @time 2015-7-16 上午11:45:31
	 * @param columns
	 * @return
	 */
	public List<NewsColumn> reSetNewsColumns(List<NewsColumn> columns,Context context) {
		HcLog.D(TAG + " #reSetNewsColumns column size = "+columns.size());
		mNewsColumns.clear();
		mNewsColumns.addAll(columns);
		HcLog.D(TAG + " #reSetNewsColumns after add column size = "+mNewsColumns.size());
		// 更新数据库
		OperateDatabase.insertNewss(mNewsColumns, context);
		createScrollNewsCache(columns);
		createNewsCache(columns);

		return mNewsColumns;
	}

	public void updateNewsPageNumber(String newsId, int number, Mode mode) {
		NewsPageData data = mNewsCache.get(newsId);
		if (null != data) {
			data.mPageNumber = number;
			data.mMode = mode;
		}
	}

	public int getCurrentPageNumber(String newsId) {
		NewsPageData data = mNewsCache.get(newsId);
		if (null != data) {
			return data.mPageNumber;
		}
		return -1;
	}

	public Mode getCurrentMode(String newsId) {
		NewsPageData data = mNewsCache.get(newsId);
		if (null != data) {
			return data.mMode;
		}
		return Mode.PULL_FROM_START;
	}

	/**
	 *
	 * @author jrjin
	 * @time 2015-12-29 上午8:56:34
	 * @param newsId
	 * @return
	 */
	public NewsPageData getNewsPageData(String newsId) {
		NewsPageData data = mNewsCache.get(newsId);
		if (data == null) {
			data = new NewsPageData();
			data.mId = newsId;
			mNewsCache.put(newsId, data);
		}

		if (data.mInfos.size() == 0) {
			data.mInfos.addAll(NewsOperateDatabase.getNewsInfos(HcApplication.getContext(), newsId,false));
		}
		HcLog.D(TAG + " #getNewsPageData data size = "+data.mInfos.size());
		return data;
	}

	@Override
	public void notify(Object data, RequestCategory request,
			ResponseCategory category) {
		// TODO Auto-generated method stub
		switch (request) {
		case NEWS_LIST:
			switch (category) {
			case SUCCESS:
				NewsPageData pageData = (NewsPageData) data;
				HcLog.D(TAG + " notify pageData size = "+pageData.mInfos.size());
				NewsPageData columnPage = mNewsCache.get(pageData.mId);
				if (pageData.mGetType == GET_DATA_REFRESH) { // 刷新
					if (columnPage == null) { // 一般不会出现
						columnPage = new NewsPageData();
						columnPage.mId = pageData.mId;
						mNewsCache.put(pageData.mId, columnPage);
					} else {
						columnPage.mInfos.clear();
					}
					columnPage.mInfos.addAll(pageData.mInfos);
					columnPage.mPageNumber = 1;
					if (columnPage.mInfos.size() < HcUtil.NEWS_COUNT) {// 说明没有一页,不能获取更多
						columnPage.mMode = Mode.PULL_FROM_START;
					} else {
						columnPage.mMode = Mode.BOTH;
					}
				} else {
					int size = pageData.mInfos.size();
					if (size == 0) {// 说明没有更多的数据了
						; // 不需要做任何事情
					} else if (size < HcUtil.NEWS_COUNT) {
						// 说明是最后页面
						columnPage.mInfos.addAll(pageData.mInfos);
						HcLog.D(TAG + " #notify page number before add number = "+columnPage.mPageNumber + " size = "+size);
						columnPage.mPageNumber ++;
						HcLog.D(TAG + " #notify page number after add number = "+columnPage.mPageNumber + " size = "+size);
						columnPage.mMode = Mode.PULL_FROM_START;
					} else {
						columnPage.mInfos.addAll(pageData.mInfos);
						HcLog.D(TAG + " #notify page number before add number = "+columnPage.mPageNumber + " size = "+size);
						columnPage.mPageNumber ++;
						HcLog.D(TAG + " #notify page number after add number = "+columnPage.mPageNumber + " size = "+size);
						columnPage.mMode = Mode.BOTH;
					}
				}
				notifyNews(category, pageData.mId, request);
				break;
			case SYSTEM_ERROR:
//				HcUtil.toastSystemError(HcApplication.getContext());
				notifyNews(category, (String) data, request);
				break;
			case DATA_ERROR:
//				HcUtil.toastDataError(HcApplication.getContext());
				notifyNews(category, (String) data, request);
				break;
			case SESSION_TIMEOUT:
//				HcUtil.toastTimeOut(HcApplication.getContext());
				notifyNews(category, (String) data, request);
				break;
			case NETWORK_ERROR:
//				HcUtil.toastNetworkError(HcApplication.getContext());
				notifyNews(category, (String) data, request);
				break;
			case REQUEST_FAILED:
				setChanged();
				notifyObservers(data);
				break;

			default:
				break;
			}

			break;
		case NEWSCOLUMN:
			switch (category) {
			case SUCCESS:
				if (data != null && data instanceof List<?>) {
					reSetNewsColumns((List<NewsColumn>) data, HcApplication.getContext());
					setChanged();
					notifyObservers(RequestCategory.NEWSCOLUMN);
				}
				break;

			default:
				break;
			}
			break;
		case NEWSSCROLL:
			switch (category) {
			case SUCCESS:
				NewsPageData pageData = (NewsPageData) data;
				List<NewsInfo> infos = mScrollNewsCache.get(pageData.mId);
				if (infos == null) {
					infos = pageData.mInfos;
					mScrollNewsCache.put(pageData.mId, infos);
				} else {
					infos.clear();
					infos.addAll(pageData.mInfos);
				}
				notifyNews(category, pageData.mId, request);
				break;

			default:
				break;
			}

			break;
		case CHECK_MODULE_TIME:
			if (category == ResponseCategory.SUCCESS) {
				if (data != null && data instanceof ModuleInfo) {
					ModuleInfo info = (ModuleInfo) data;
					if (info.getUpdateFlag() == ModuleInfo.FLAG_UPDATE) {
						HcHttpRequest.getRequest().sendNewsColumnCommand(this, MODULE_ID);
					}

				}
			}
			break;

		default:
			break;
		}

	}

	public void refreshColumns() {
		if (mNewsColumns.size() == 0) {
			HcHttpRequest.getRequest().sendNewsColumnCommand(this, MODULE_ID);
		} else {
			// 检测数据
			HcHttpRequest.getRequest().sendModuleCheckCommand(
					new ModuleInfo(MODULE_ID, SettingHelper.getModuleTime(HcApplication.getContext(),
							MODULE_ID, false)), this);
		}

	}

	@Override
	public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
		// TODO Auto-generated method stub

	}

	private void notifyNews(ResponseCategory type, String id, RequestCategory request) {
		setChanged();
		notifyObservers(new NotifyData(type, id, request));
	}

	/**
	 * 获取新闻数据
	 * @author jrjin
	 * @time 2015-12-29 上午9:30:37
	 * @param type {@link #GET_DATA_REFRESH} and {@link #GET_DATA_MORE}
	 * @param data 当前页面信息
	 */
	public void getNewsList(int type, NewsPageData data, String id) {
		int page = type == GET_DATA_REFRESH ? 1 : data.mPageNumber + 1;
		HcHttpRequest.getRequest().sendNewsListCommand(id, HcUtil.NEWS_COUNT, page, this, type);
	}

	public static class NotifyData {
		public ResponseCategory mType;
		/** 栏目编号 */
		public String mId;

		public RequestCategory mRequest;

		NotifyData(ResponseCategory type, String id, RequestCategory request) {
			mType = type;
			mId = id;
			mRequest = request;
		}
	}
	/**
	 * 获取滚动图片
	 * @author jrjin
	 * @time 2015-12-29 下午3:03:19
	 * @param id 栏目编号
	 */
	public void getScrollList(String id) {
		HcHttpRequest.getRequest().sendNewsScrollListCommand(id, this);
	}

	@Override
	public void clearCache(boolean exit) {
		clearNewsCache();
	}
}
