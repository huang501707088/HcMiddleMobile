/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-8-27 下午3:08:01
 */
package com.android.hcframe.doc.data;

import android.content.Context;
import android.text.TextUtils;

import com.android.hcframe.CacheManager;
import com.android.hcframe.HcApplication;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcObservable;
import com.android.hcframe.HcUtil;
import com.android.hcframe.ModuleInfo;
import com.android.hcframe.TemporaryCache;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.pull.PullToRefreshBase.Mode;
import com.android.hcframe.service.HcService;
import com.android.hcframe.sql.OperateDatabase;
import com.android.hcframe.sql.SettingHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 1、栏目缓存
 * <p>
 * a.先获取临时缓存
 * </p>
 * <p>
 * b.没有数据再从数据库中获取
 * </p>
 * <p>
 * c.没有数据再去服务端获取
 * </p>
 * <p>
 * d.根据返回的栏目数据，更新临时缓存和数据库
 * </p>
 * <p>
 * e.更新栏目的资料缓存{@code DocCacheData#mDocMap}
 * </p>
 * <p>
 * f.通知界面更新栏目，直接解析数据或者再来获取一次都可以.
 * </p>
 * <p>
 * </p>
 * 2、栏目资料缓存
 * <p>
 * a.先获取临时缓存
 * </p>
 * <p>
 * b.没有数据再从数据库中获取
 * </p>
 * <p>
 * c.没有数据再去服务端获取
 * </p>
 * <p>
 * d.有数据而且是第一次进入页面，需要去服务器获取数据
 * </p>
 * <p>
 * e.根据返回的栏目资料数据，通知页面，更新临时缓存，这个动作在页面完成，因为临时 缓存中list和页面中的是公用的
 * </p>
 * 
 * @author jrjin
 * @time 2015-8-28 下午3:31:25
 */
public class DocCacheData extends HcObservable implements IHttpResponse,
		TemporaryCache {

	private static final String TAG = "DocCacheData";

	private static final DocCacheData DOC_DATA = new DocCacheData();

	/** 资料中心栏目缓存 */
	private List<DocColumn> mDocColumns = new ArrayList<DocColumn>();
	/** key:栏目的ID; value:栏目下的资料 */
	// private Map<String, List<DocInfo>> mDocMap = new HashMap<String,
	// List<DocInfo>>();
	private Map<String, DocPageData> mDocMap = new HashMap<String, DocPageData>();
	/** 历史检索记录 */
	private List<String> mSearchKey = new ArrayList<String>();
	/** 历史资料详情记录 */
	private List<DocInfo> mDocInfos = new ArrayList<DocInfo>();
	/** 历史资料记录 */
	private List<DocHistoricalRecord> mRecords = new ArrayList<DocHistoricalRecord>();
	/** 历史资料ID */
	private List<String> mRecordId = new ArrayList<String>();

	private static final String MODULE_ID = "datacenter_update_column";
	
	private DocCacheData() {
		CacheManager.getInstance().addCache(this);
	}

	public static final DocCacheData getInstance() {
		return DOC_DATA;
	}

	/**
	 * 更新栏目，重新设置
	 * 
	 * @author jrjin
	 * @time 2015-7-16 上午11:45:31
	 * @param columns
	 * @return
	 */
	public List<DocColumn> reSetDocColumns(List<DocColumn> columns,
			Context context) {
		mDocColumns.clear();
		mDocColumns.addAll(columns);
		// 更新数据库
		OperateDatabase.insertDocColumns(mDocColumns, context);
		createDocCache(columns);

		return mDocColumns;
	}

	/**
	 * 获取栏目 1、先读取临时缓存 2、再读取数据库
	 * 
	 * @author jrjin
	 * @time 2015-7-16 上午11:45:16
	 * @return
	 */
	public List<DocColumn> getDocColumns(Context context) {
		if (mDocColumns.size() == 0) {
			// 去数据库获取
			mDocColumns.addAll(OperateDatabase.getDocColumns(HcApplication
					.getContext()));
		}
		/**
		 * @author jrjin
		 * @date 2016-1-19 下午5:39:59
		 * 这个也可以放在@DocHomeTempPage#createColumns方法中处理*/
		refreshDocColumns();
		
		
//		if (mDocColumns.size() == 0) {
//			// 去服务端获取
//			getDataColumn();
//		} else {
//			// 去检测栏目数据
//			HcHttpRequest.getRequest().sendModuleCheckCommand(
//					new ModuleInfo(MODULE_ID, SettingHelper.getModuleTime(context,
//							MODULE_ID)), this);
//		}
		return mDocColumns;
	}

	/**
	 * 根据栏目创建缓存； 注意：原先有的，后来删除的栏目缓存还在
	 * 
	 * @author jrjin
	 * @time 2015-7-16 上午10:06:34
	 * @param columns
	 * @return
	 */
	public int createDocCache(List<DocColumn> columns) {
		DocPageData data = null;
		for (DocColumn docColumn : columns) {
			data = mDocMap.get(docColumn.getNewsId());
			if (null == data) {
				mDocMap.put(docColumn.getNewsId(), new DocPageData());
			}
		}
		return mDocMap.size();
	}

	@Override
	public void notify(Object data, RequestCategory request,
			ResponseCategory category) {
		// TODO Auto-generated method stub
		switch (request) {
		case DATA_COLUMN:
			switch (category) {
			case SUCCESS:
				if (data != null && data instanceof List<?>) {
					reSetDocColumns((List<DocColumn>) data,
							HcApplication.getContext());
					notifyObservers(this, data, request, category);
				}
				break;
			case SESSION_TIMEOUT:
			case NETWORK_ERROR:
				notifyObservers(this, data, request, category);
				break;
			case DATA_ERROR:
			case SYSTEM_ERROR:
				notifyObservers(this, data, request, category);
				break;
			case ACCOUNT_INVALID:
				notifyObservers(this, data, request, category);
				break;
			default:
				notifyObservers(this, data, request, category);
				break;
			}
			break;
		case DATA_LIST:
		case SEARCH_ALL_DATA:
		case SEARCH_DATA:
		case SEARCH_DATA_DETAIL:
			notifyObservers(this, data, request, category);
			break;
		
		case CHECK_MODULE_TIME:
			if (category == ResponseCategory.SUCCESS) {
				if (data != null && data instanceof ModuleInfo) {
					ModuleInfo info = (ModuleInfo) data;
					if (info.getUpdateFlag() == ModuleInfo.FLAG_UPDATE) {
						getDataColumn();
					}
					
				}
			}
			break;
			
		default:
			break;
		}
	}

	private void getDataColumn() {
		HcHttpRequest.getRequest().sendDataColumnCommand(this, MODULE_ID);
	}

	/**
	 * 获取栏目的数据
	 * @author jrjin
	 * @time 2015-9-1 下午3:39:51
	 * @param id
	 * @param page
	 * @param size
	 */
	public void getDataList(String id, int page, int size) {
		HcHttpRequest.getRequest().sendDataListCommand(id, page, /* size */
				HcUtil.NEWS_COUNT, this);
	}

	/**
	 * 检索全部栏目或者单个栏目的数据
	 * @author jrjin
	 * @time 2015-9-1 下午3:40:04
	 * @param id
	 * @param key
	 * @param page
	 * @param size
	 */
	public void searchData(String id, String key, int page, int size) {
		HcHttpRequest.getRequest().sendSearchDataListCommand(id, page, /* size */
				HcUtil.NEWS_COUNT, key, this);
	}

	/**
	 * 获取资料的详情
	 * @author jrjin
	 * @time 2015-9-1 下午3:40:37
	 * @param id
	 * @param flag
	 */
	public void getDataDetail(String id, int flag) {
		HcHttpRequest.getRequest().sendSearchDataDetail(id, flag, this);
	}

	/**
	 * 获取历史查询匹配的内容
	 * 
	 * @author jrjin
	 * @time 2015-8-28 下午3:20:45
	 * @return
	 */
	public List<String> getSearchKeys(String searchKey) {
		if (mSearchKey.isEmpty()) {
			// 去配置文件或者数据库查询，建议去存储到配置文件里
			String key = SettingHelper.getSearchKey(HcApplication.getContext());
			if (!TextUtils.isEmpty(key)) {
				mSearchKey.addAll(Arrays.asList(key.split(";")));
			}
		}
		List<String> keys = new ArrayList<String>();
		if (TextUtils.isEmpty(searchKey)) {
			keys.addAll(mSearchKey);
		} else {
			for (String string : mSearchKey) {
				if (string.contains(searchKey))
					keys.add(string);
			}
		}
		
		return keys;
	}

	/**
	 * 添加key,最多的记录为20
	 * 
	 * @author jrjin
	 * @time 2015-8-31 下午1:52:24
	 * @param searchKey
	 */
	public void addSearchKey(String searchKey) {
		if (TextUtils.isEmpty(searchKey))
			return;
		Iterator<String> iterator = mSearchKey.iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			if (key.equals(searchKey)) {
				iterator.remove();
				break;
			}

		}
		mSearchKey.add(0, searchKey);
		if (mSearchKey.size() > 20) {
			mSearchKey.remove(mSearchKey.size() - 1);
		}
		updateKeys();
	}

	/**
	 * 更新key
	 * @author jrjin
	 * @time 2015-9-1 下午3:39:08
	 */
	private void updateKeys() {
		if (mSearchKey.isEmpty()) {
			SettingHelper.setSearchKey(HcApplication.getContext(),
					"");
			return;
		}

		StringBuilder builder = new StringBuilder();
		for (String string : mSearchKey) {
			builder.append(string + ";");
		}
		/** 取出最后一个分号';' */
		SettingHelper.setSearchKey(HcApplication.getContext(),
				builder.substring(0, builder.length() - 1));
	}

	/**
	 * 删除搜索的key
	 * @see DocKeyAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 * @author jrjin
	 * @time 2015-9-1 下午3:38:06
	 * @param key 需要删除的key
	 */
	public void deleteKey(String key) {
		mSearchKey.remove(key);
		updateKeys();
	}

	/**
	 * 获取当前栏目的列表
	 * 
	 * @author jrjin
	 * @time 2015-8-28 下午4:10:19
	 * @param columnId
	 *            栏目编码
	 * @param update
	 *            有缓存是否需要去服务端获取数据;true:需要去获取不管缓存有没有;false:有缓存了就不用去获取了
	 * @return 当前栏目的列表
	 */
	public List<DocInfo> getDocInfos(String columnId, boolean update) {
		DocPageData data = mDocMap.get(columnId);
		if (null == data) {
			data = new DocPageData();
			mDocMap.put(columnId, data);
		}

		List<DocInfo> infos = data.mInfos;

		if (infos.isEmpty()) {
			// 去数据库中获取
			
		}
		if (infos.isEmpty() || update) {
			// 去服务端获取数据
			getDataList(columnId, 1, HcUtil.NEWS_COUNT);
		}

		return infos;
	}

	/**
	 * 获取资料历史记录，并保存资料编码
	 * @author jrjin
	 * @time 2015-9-1 下午3:36:57
	 * @return 资料历史记录
	 */
	public List<DocHistoricalRecord> getHistoricalRecords() {
		if (mRecords.isEmpty()) {
			mRecords.addAll(OperateDatabase.getHistoricalRecords(HcApplication
					.getContext()));
		}
		for (DocHistoricalRecord record : mRecords) {			
			mRecordId.add(record.getFileId()); // 这里可能有重复，不影响使用，以后再考虑优化。
		}
		HcLog.D(TAG + " getHistoricalRecords record size = "+mRecords.size());
		return mRecords;
	}

	public DocPageData getPageData(String columnId) {
		return mDocMap.get(columnId);
	}

	/**
	 * 更新缓存的页面信息
	 * 
	 * @author jrjin
	 * @time 2015-8-31 上午10:46:02
	 * @param columnId
	 * @param number
	 * @param mode
	 */
	public void updateNewsPageNumber(String columnId, int number, Mode mode) {
		DocPageData data = mDocMap.get(columnId);
		if (null != data) {
			data.mPageNumber = number;
			data.mMode = mode;
		}
	}

	/**
	 * 获取栏目的页面的页数
	 * 
	 * @author jrjin
	 * @time 2015-8-31 上午10:40:22
	 * @param columnId
	 *            栏目编号
	 * @return 当前栏目的数据的页数
	 */
	public int getCurrentPageNumber(String columnId) {
		DocPageData data = mDocMap.get(columnId);
		if (null != data) {
			return data.mPageNumber;
		}
		return -1;
	}

	/**
	 * 获取当前的刷新模式
	 * 
	 * @author jrjin
	 * @time 2015-8-31 上午10:39:28
	 * @param columnId
	 *            栏目的编号
	 * @return 上次栏目的页面刷新模式
	 */
	public Mode getCurrentMode(String columnId) {
		DocPageData data = mDocMap.get(columnId);
		if (null != data) {
			return data.mMode;
		}
		return Mode.PULL_FROM_START;
	}

	/**
	 * 切换用户时，清空缓存。
	 * 
	 * @author jrjin
	 * @time 2015-7-16 上午10:11:05
	 */
	public void clearDocCache() {
		OperateDatabase.insertDataRecords(HcApplication.getContext(), mRecords);
		mDocColumns.clear();
		Iterator<DocPageData> iterator = mDocMap.values().iterator();
		while (iterator.hasNext()) {
			iterator.next().mInfos.clear();
		}
		mDocMap.clear();

		mDocInfos.clear();
		mRecords.clear();
		mRecordId.clear();

	}

	public void addDocDetail(DocInfo info) {
		if (info == null)
			return;
		if (!mDocInfos.contains(info))
			mDocInfos.add(info);
	}

	/**
	 * 进入资料详情页面调用
	 * @author jrjin
	 * @time 2015-9-1 下午3:33:55
	 * @param DataId 资料编码/文件编码
	 * @param flag 资料标识：0—标题，1—主文件，2—附件
	 * @return 资料详情，可能为null
	 */
	public DocInfo getDocDetail(String DataId, int flag) {
		switch (flag) {
		case DocInfo.FLAG_TITIL:
			for (DocInfo docInfo : mDocInfos) {
				if (docInfo.getDataId().equals(DataId)) {
					return docInfo;
				}
			}
			break;
		case DocInfo.FLAG_MAIN:
			for (DocInfo docInfo : mDocInfos) {
				if (docInfo.getFileId().equals(DataId)) {
					return docInfo;
				}
			}
			break;
		case DocInfo.FLAG_SUB:
			for (DocInfo docInfo : mDocInfos) {
				for (DocFileInfo fileInfo : docInfo.getDocInfos()) {
					if (fileInfo.getFileId().equals(DataId)) {
						return docInfo;
					}
				}
			}
			break;

		default:
			break;
		}

		return null;
	}

	/**
	 * 添加已阅读的资料记录 
	 * @author jrjin
	 * @time 2015-9-1 下午3:31:59
	 * @param record 当前需要打开的资料
	 * @param dataId 资料编码/文件编码
	 */
	public void addDocHistoricalRecord(DocHistoricalRecord record, String dataId) {
		if (record == null)
			return;
		for (String id : mRecordId) {
			HcLog.D(TAG + " addDocHistoricalRecord history id = "+id + " current id = "+dataId);
			if (id.equals(dataId)) { // 重新排序
				Iterator<DocHistoricalRecord> iterator = mRecords.iterator();
				String currentId;
				while (iterator.hasNext()) {
					currentId = iterator.next().getFileId();
					HcLog.D(TAG + " addDocHistoricalRecord record id = "+currentId);
					if (id.equals(currentId)) { // 到时需要检测会不会有问题
						iterator.remove();
						mRecords.add(0,record);
						break;
					}
				}
				return;
			}
				
				
		}
		mRecordId.add(dataId);
		mRecords.add(record);

		if (mRecords.size() > 50) {
			mRecords.remove(mRecords.size() - 1);
		}
	}

	/**
	 * 定时刷新
	 * @see HcService
	 * @author jrjin
	 * @time 2015-9-1 下午2:07:10
	 */
	public void refreshDocColumns() {
		if (mDocColumns.size() == 0) 
			getDataColumn();
		else {
			HcHttpRequest.getRequest().sendModuleCheckCommand(
					new ModuleInfo(MODULE_ID, SettingHelper.getModuleTime(HcApplication.getContext(),
							MODULE_ID, false)), this);
		}
	}

	@Override
	public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearCache(boolean exit) {
		clearDocCache();
	}
}
