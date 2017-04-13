/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-8-30 下午2:53:59
 */
package com.android.hcframe.doc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcObserver;
import com.android.hcframe.HcSubject;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.doc.data.DocCacheData;
import com.android.hcframe.doc.data.DocColumn;
import com.android.hcframe.doc.data.DocColumnAdapter;
import com.android.hcframe.doc.data.DocDataListAdapter;
import com.android.hcframe.doc.data.DocDataSearchListAdapter;
import com.android.hcframe.doc.data.DocHistoricalRecord;
import com.android.hcframe.doc.data.DocInfo;
import com.android.hcframe.doc.data.DocKeyAdapter;
import com.android.hcframe.doc.data.DocKeyAdapter.onSearchKeyClick;
import com.android.hcframe.doc.data.SearchDocInfo;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.http.ResponseCodeInfo;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshBase.Mode;
import com.android.hcframe.pull.PullToRefreshBase.OnLastItemVisibleListener;
import com.android.hcframe.pull.PullToRefreshBase.OnRefreshBothListener;
import com.android.hcframe.pull.PullToRefreshListView;
import com.android.hcframe.push.HcAppState;
import com.android.hcframe.sql.OperateDatabase;
import com.android.hcframe.sql.SettingHelper;

import java.util.ArrayList;
import java.util.List;

public class DocColumnActivity extends HcBaseActivity implements OnItemClickListener,
		OnClickListener, TextWatcher, HcObserver, OnFocusChangeListener {

	private static final String TAG = "DocColumnActivity";
	/** top bar */
	private TopBarView mTopBarView;

	/** Search */
	private LinearLayout mSearchParent;

	private ImageView mColumnBtn;

	private ImageView mSearchBtn;

	private EditText mSearchText;

	private LinearLayout doc_search_show_lly;

	private ImageView doc_search_image_clear;

	private LinearLayout search_lly;

	private LinearLayout doc_search_lly;

	/** data list panel */
	private LinearLayout mListParent;

	private PullToRefreshListView mListView;

	private LinearLayout mEmpty;

	private TextView mEmptyText;

	/** columns panel */
	private LinearLayout mColumnParent;

	private RelativeLayout mColumnHomeBtn;

	private GridView mGridView;

	private DocCacheData mCacheData;

	/** 当前栏目的编号 */
	private String mCurrentColumnId;
	/** 上次栏目的编号 */
	private String mPreColumnId;
	/** 当前栏目的标题 */
	private String mCurrentColumnTitle;
	/** 栏目列表 */
	private List<DocColumn> mColumns;
	/** 当前栏目的资料列表 */
	private List<DocInfo> mDocInfos;

	private DocColumnAdapter mColumnAdapter;

	private DocDataListAdapter mDataListAdapter;

	private DocDataSearchListAdapter mDataSearchListAdapter;

	private DocKeyAdapter mKeyAdapter;

	private List<SearchDocInfo> mSearchInfos = new ArrayList<SearchDocInfo>();

	/**
	 * 当前栏目数据页面
	 */
	private int mCurrentPage = 1;

	private static final int GET_DATA_REFRESH = 0;

	private static final int GET_DATA_MORE = 1;
	/**
	 * 当前栏目刷新类型
	 */
	private int mRefreshType = GET_DATA_REFRESH;

	private int mCurrentSearchPage = 1;
	private int mRefreshSearchType = GET_DATA_REFRESH;

	private ShowMode mShowMode = ShowMode.COLUMN_DATA_LIST;
	/** 检索时，匹配到的Keys */
	private List<String> mKeyList = new ArrayList<String>();

	private String mKey;

	private String mAppId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// mCurrentColumnId = "00010001";
		// mCurrentColumnTitle = "测试";
		Intent intent = getIntent();
		if (intent == null || intent.getExtras() == null) {
			finishActivity();
			return;
		}
		mCurrentColumnId = intent.getStringExtra("column_id");
		if (TextUtils.isEmpty(mCurrentColumnId)) {
			finishActivity();
			return;
		}
		mPreColumnId = mCurrentColumnId;

		mCurrentColumnTitle = intent.getStringExtra("column_title");
		if (TextUtils.isEmpty(mCurrentColumnTitle)) {
			finishActivity();
			return;
		}

		mAppId = intent.getStringExtra("appId");
		if (TextUtils.isEmpty(mAppId)) {
			finishActivity();
			return;
		}

		mCacheData = DocCacheData.getInstance();
		mCacheData.addObserver(this);
		setContentView(R.layout.activity_doc_column_data);
		initViews();
		initData();
		setListeners();
	}

	private void initViews() {
		mTopBarView = (TopBarView) findViewById(R.id.doc_list_top_bar);

		mSearchParent = (LinearLayout) findViewById(R.id.doc_column_search_parent);
		mColumnBtn = (ImageView) findViewById(R.id.doc_column_selector_image);
		mSearchBtn = (ImageView) findViewById(R.id.doc_search_image);
		mSearchText = (EditText) findViewById(R.id.search_content_et);

		mListParent = (LinearLayout) findViewById(R.id.doc_column_data_list_parent);
		mListView = (PullToRefreshListView) findViewById(R.id.doc_column_data_list);
		mEmpty = (LinearLayout) findViewById(R.id.doc_data_list_empty_view);

		mColumnParent = (LinearLayout) findViewById(R.id.doc_columns_parent);
		mColumnHomeBtn = (RelativeLayout) findViewById(R.id.doc_column_home_btn);
		mGridView = (GridView) findViewById(R.id.doc_columns_gv);

		doc_search_show_lly = (LinearLayout) findViewById(R.id.doc_search_show_lly);
		doc_search_image_clear = (ImageView) findViewById(R.id.doc_search_image_clear);
		search_lly = (LinearLayout) findViewById(R.id.search_lly);
		doc_search_lly = (LinearLayout) findViewById(R.id.doc_search_lly);

		mListView.setEmptyView(mEmpty);

		// mSearchText.setFocusable(false);
	}

	private void setListeners() {
		mColumnBtn.setOnClickListener(this);
		mSearchBtn.setOnClickListener(this);
		mColumnHomeBtn.setOnClickListener(this);
		doc_search_image_clear.setOnClickListener(this);
		doc_search_show_lly.setOnClickListener(this);
		mSearchText.addTextChangedListener(this);
		mSearchText.setOnFocusChangeListener(this);
		mGridView.setOnItemClickListener(this);
		mListView.setOnItemClickListener(this);
		mListView.setScrollingWhileRefreshingEnabled(false);
		mListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

			@Override
			public void onLastItemVisible() {
				// TODO Auto-generated method stub
				if (mListView.getMode() == Mode.PULL_FROM_START) {
					HcUtil.showToast(DocColumnActivity.this, "没有更多数据！");
				}
			}
		});

		mListView
				.setOnRefreshBothListener(new OnRefreshBothListener<ListView>() {

					@Override
					public void onPullDownToRefresh(
							PullToRefreshBase<ListView> refreshView) {
						// TODO Auto-generated method stub
						switch (mShowMode) {
						case COLUMN_DATA_LIST:
							mCurrentPage = 1;
							mRefreshType = GET_DATA_REFRESH;
							mCacheData.getDataList(mCurrentColumnId,
									mCurrentPage, HcUtil.NEWS_COUNT);
							break;
						case COLUMN_DATA_SEARCH_LIST:
							mCurrentSearchPage = 1;
							mRefreshSearchType = GET_DATA_REFRESH;
							mCacheData.searchData(mCurrentColumnId,
									mKey != null ? mKey : "",
									mCurrentSearchPage, HcUtil.NEWS_COUNT);
							break;

						default:
							break;
						}
					}

					@Override
					public void onPullUpToRefresh(
							PullToRefreshBase<ListView> refreshView) {
						// TODO Auto-generated method stub
						switch (mShowMode) {
						case COLUMN_DATA_LIST:
							mCurrentPage++;
							mRefreshType = GET_DATA_MORE;
							mCacheData.getDataList(mCurrentColumnId,
									mCurrentPage, HcUtil.NEWS_COUNT);
							break;
						case COLUMN_DATA_SEARCH_LIST:
							mCurrentSearchPage++;
							mRefreshSearchType = GET_DATA_MORE;
							mCacheData.searchData(mCurrentColumnId,
									mKey != null ? mKey : "",
									mCurrentSearchPage, HcUtil.NEWS_COUNT);
							break;

						default:
							break;
						}
					}
				});
	}

	private void initData() {
		if (mDocInfos == null) {
			mDocInfos = mCacheData.getDocInfos(mCurrentColumnId, true);
			if (mDocInfos.isEmpty()) {
				HcDialog.showProgressDialog(this, R.string.dialog_title_get_data);
			}
			if (mDataListAdapter == null) {
				mDataListAdapter = new DocDataListAdapter(this, mDocInfos);
				mListView.setAdapter(mDataListAdapter);
			}
		}
		mCurrentPage = mCacheData.getCurrentPageNumber(mCurrentColumnId);
		mListView.setMode(mCacheData.getCurrentMode(mCurrentColumnId));
		mTopBarView.setTitle(mCurrentColumnTitle);
	}

	private void createColumns() {
		if (mColumns == null) { // 第一次点击的时候
			mColumns = mCacheData.getDocColumns(this);
			if (!mColumns.isEmpty()) { // 这里其实肯定不为空了
				if (mColumnAdapter == null) {
					mColumnAdapter = new DocColumnAdapter(this, mColumns,
							mCurrentColumnId, mAppId);
					mGridView.setAdapter(mColumnAdapter);
				}
			}
		} else {
			if (mColumnAdapter == null) {
				mColumnAdapter = new DocColumnAdapter(this, mColumns,
						mCurrentColumnId, mAppId);
				mGridView.setAdapter(mColumnAdapter);
			}

			if (!mCurrentColumnId.equals(mPreColumnId))
				mColumnAdapter.setColumnId(mCurrentColumnId);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		if (parent instanceof GridView) {
			mShowMode = ShowMode.COLUMN_DATA_LIST;
			DocColumn column = (DocColumn) parent.getItemAtPosition(position);
			mPreColumnId = mCurrentColumnId;
			mCurrentColumnId = column.getNewsId();
			mCurrentColumnTitle = column.getmName();
			// 刷新
			hindColumns();
			if (!mCurrentColumnId.equals(mPreColumnId)) {				
				mDocInfos = mCacheData.getDocInfos(mCurrentColumnId, false);
				if (mDocInfos.isEmpty()) {
					HcDialog.showProgressDialog(this, R.string.dialog_title_get_data);
				}
				mDataListAdapter = new DocDataListAdapter(this, mDocInfos);
				mTopBarView.setTitle(mCurrentColumnTitle);
				mListView.setAdapter(mDataListAdapter);
			}
			mCurrentPage = mCacheData.getCurrentPageNumber(mCurrentColumnId);
			mListView.setMode(mCacheData.getCurrentMode(mCurrentColumnId));

		} else if (parent instanceof ListView) {
			Object item = parent.getItemAtPosition(position);
			if (item instanceof DocInfo) {
				DocInfo info = (DocInfo) item;
				// mCacheData.getDataDetail(info.getDataId(),
				// DocInfo.FLAG_TITIL);
				DocHistoricalRecord record = new DocHistoricalRecord();
				record.setReadTime("" + System.currentTimeMillis());
				record.setFileId(info.getDataId());
				record.setFileName(info.getDataTitle());
				record.setFileSize(info.getFileSize());
				record.setUsername(HcUtil.isEmpty(SettingHelper
						.getAccount(this)) ? "---" : SettingHelper
						.getAccount(this));
				// record.setFileUrl(info.getFileUrl());
				record.setFlag(DocInfo.FLAG_TITIL);
				record.setmDate(info.getDate());
				mCacheData.addDocHistoricalRecord(record, info.getDataId());
				mCacheData.addDocDetail(info);
				OperateDatabase.insertDataRecordDetail(this, info);
				startDetailActivity(info.getDataId(), DocInfo.FLAG_TITIL);
			} else if (item instanceof SearchDocInfo) {
				SearchDocInfo info = (SearchDocInfo) item;
				// mCacheData.getDataDetail(info.getFileId(), info.getFlag());
				DocHistoricalRecord record = new DocHistoricalRecord();
				record.setReadTime("" + System.currentTimeMillis());
				record.setFileId(info.getFileId());
				record.setFileName(info.getFileName());
				record.setFileSize(info.getFileSize());
				record.setUsername(HcUtil.isEmpty(SettingHelper
						.getAccount(this)) ? "---" : SettingHelper
						.getAccount(this));

				// record.setFileUrl(info.getFileUrl());
				record.setFlag(info.getFlag());
				mCacheData.addDocHistoricalRecord(record, info.getFileId());
				startDetailActivity(info.getFileId(), info.getFlag());
			}
		}
	}

	private void startDetailActivity(String id, int flag) {
		Intent intent = new Intent(this, DocDetailsActivity.class);
		intent.putExtra("data_id", id);
		intent.putExtra("data_flag", flag);
		startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		if (id == R.id.doc_column_selector_image) {
			mSearchParent.setFocusable(true);
			mSearchParent.setFocusableInTouchMode(true);
			mSearchParent.requestFocus();
			showColumns();
			createColumns();
			showSearch();
		} else if (id == R.id.doc_search_image) {
			String key = mSearchText.getText().toString();
			if (!TextUtils.isEmpty(key)) {
				HcDialog.showProgressDialog(this, "正在检索数据");
				mSearchParent.setFocusable(true);
				mSearchParent.setFocusableInTouchMode(true);
				mSearchParent.requestFocus();
				mCacheData.addSearchKey(key);
				mShowMode = ShowMode.COLUMN_DATA_SEARCH_LIST;
				mCurrentSearchPage = 1;
				mRefreshSearchType = GET_DATA_REFRESH;
				mKey = key;
				mCacheData.searchData(mCurrentColumnId, key,
						mCurrentSearchPage, HcUtil.NEWS_COUNT);
			}
		} else if (id == R.id.doc_column_home_btn) {
			setResult(Activity.RESULT_OK);
			finishActivity();
			overridePendingTransition(0, 0);
		} else if (id == R.id.doc_search_image_clear) {
			mSearchText.setText("");
		} else if (id == R.id.doc_search_show_lly) {
			hideSearch();
		}
		/*
		switch (id) {
		case R.id.doc_column_selector_image:
			mSearchParent.setFocusable(true);
			mSearchParent.setFocusableInTouchMode(true);
			mSearchParent.requestFocus();
			showColumns();
			createColumns();
			showSearch();
			break;
		case R.id.doc_search_image:
			String key = mSearchText.getText().toString();
			if (!TextUtils.isEmpty(key)) {
				HcDialog.showProgressDialog(this, "正在检索数据...");
				mSearchParent.setFocusable(true);
				mSearchParent.setFocusableInTouchMode(true);
				mSearchParent.requestFocus();
				mCacheData.addSearchKey(key);
				mShowMode = ShowMode.COLUMN_DATA_SEARCH_LIST;
				mCurrentSearchPage = 1;
				mRefreshSearchType = GET_DATA_REFRESH;
				mKey = key;
				mCacheData.searchData(mCurrentColumnId, key,
						mCurrentSearchPage, HcUtil.NEWS_COUNT);
			}
			break;
		case R.id.doc_column_home_btn:
			setResult(Activity.RESULT_OK);
			finishActivity();
			overridePendingTransition(0, 0);
			break;
		case R.id.doc_search_image_clear:
			mSearchText.setText("");
			break;
		case R.id.doc_search_show_lly:
			hideSearch();
			break;
		default:
			break;
		}
		*/
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		// do something
		if (null == s)
			return;
		String key = s.toString();
		HcLog.D(TAG + " afterTextChanged key = " + key);
		// if (TextUtils.isEmpty(key)) key = "";
		mKeyList.clear();
		mKeyList.addAll(mCacheData.getSearchKeys(key));
		HcLog.D(TAG + " afterTextChanged key = " + key + " key list size ="
				+ mKeyList.size());
		if (mKeyAdapter == null) {
			; // 以后考虑是否不要重新创建对象
		} else {
			mKeyAdapter.notifyDataSetChanged();
		}

	}

	@Override
	public void updateData(HcSubject subject, Object data,
			RequestCategory request, ResponseCategory response) {
		// TODO Auto-generated method stub
		HcLog.D(TAG + " updateData request = " + request + " response = "
				+ response);
		HcDialog.deleteProgressDialog();
		mListView.onRefreshComplete();
		switch (request) {
		case SEARCH_DATA:
			switch (response) {
			case SUCCESS:
				if (data != null && data instanceof List<?>) {
					List<SearchDocInfo> infos = (List<SearchDocInfo>) data;
					int size = infos.size();
					if (size == 0) {
						if (mRefreshSearchType == GET_DATA_REFRESH) {
							// 说明刷新没有数据
						} else {
							mCurrentSearchPage--;
						}
						if (mShowMode == ShowMode.COLUMN_DATA_SEARCH_LIST) {
							mListView.setMode(Mode.PULL_FROM_START);
						}
					} else if (size < HcUtil.NEWS_COUNT) {
						if (mShowMode == ShowMode.COLUMN_DATA_SEARCH_LIST) {
							mListView.setMode(Mode.PULL_FROM_START);
						}
					} else {
						if (mShowMode == ShowMode.COLUMN_DATA_SEARCH_LIST) {
							mListView.setMode(Mode.BOTH);
						}
					}

					updateDocSearchOrders(infos);
				}
				break;
			case DATA_ERROR:
				HcUtil.toastDataError(this);
				break;
			case SESSION_TIMEOUT:
				HcUtil.toastTimeOut(this);
				break;
			case ACCOUNT_INVALID:
				HcUtil.showToast(this, "请先登录！");
				break;
			case DATA_IS_NULL:
				HcUtil.showToast(this, "没有查到数据");
				break;
			/**
			 * czx
			 * 2016.4.14
			 */
			case REQUEST_FAILED:
				ResponseCodeInfo info = (ResponseCodeInfo) data;
				if (HcHttpRequest.REQUEST_TOKEN_FAILED == info.getCode() ||
						HcHttpRequest.REQUEST_ACCOUT_EXCLUDED == info.getCode()) {
					HcUtil.reLogining(info.getBodyData(), this, info.getMsg());
				} else {
					HcUtil.showToast(this, info.getMsg());
				}
				break;
			case SYSTEM_ERROR:
				HcUtil.toastSystemError(this, data);
				break;
			default:
				break;
			}
			break;
		case DATA_LIST:
			switch (response) {
			case SUCCESS:
				if (data != null && data instanceof List<?>) {
					List<DocInfo> infos = (List<DocInfo>) data;
					int size = infos.size();
					Mode mode = Mode.PULL_FROM_START;
					if (size == 0) {
						if (mRefreshType == GET_DATA_REFRESH) {
							// 说明刷新没有数据
						} else {
							mCurrentPage--;
						}
						if (mShowMode == ShowMode.COLUMN_DATA_LIST) {
							mListView.setMode(Mode.PULL_FROM_START);
						}
					} else if (size < HcUtil.NEWS_COUNT) {
						if (mShowMode == ShowMode.COLUMN_DATA_LIST) {
							mListView.setMode(Mode.PULL_FROM_START);
						}
					} else {
						if (mShowMode == ShowMode.COLUMN_DATA_LIST) {
							mListView.setMode(Mode.BOTH);
						}
						mode = Mode.BOTH;
					}
					updateDocOrders(infos, mode);
				}
				break;
			case DATA_ERROR:
				HcUtil.toastDataError(this);
				break;
			case SESSION_TIMEOUT:
				HcUtil.toastTimeOut(this);
				break;
			case ACCOUNT_INVALID:
				HcUtil.showToast(this, "请先登录！");
				break;
			/**
			 * czx
			 * 2016.4.14
			 */
			case REQUEST_FAILED:
				ResponseCodeInfo info = (ResponseCodeInfo) data;
				if (HcHttpRequest.REQUEST_TOKEN_FAILED == info.getCode() ||
						HcHttpRequest.REQUEST_ACCOUT_EXCLUDED == info.getCode()) {
					HcUtil.reLogining(info.getBodyData(), this, info.getMsg());
				} else {
					HcUtil.showToast(this, info.getMsg());
				}
				break;
			case SYSTEM_ERROR:
				HcUtil.toastSystemError(this, data);
				break;
			default:
				break;
			}
			break;

		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (mCacheData != null)
			mCacheData.removeObserver(this);
		super.onDestroy();
	}

	private void showColumns() {
		if (mListParent.getVisibility() != View.GONE)
			mListParent.setVisibility(View.GONE);
		if (mColumnParent.getVisibility() != View.VISIBLE)
			mColumnParent.setVisibility(View.VISIBLE);
	}

	private void hindColumns() {
		if (mColumnParent.getVisibility() != View.GONE)
			mColumnParent.setVisibility(View.GONE);
		if (mListParent.getVisibility() != View.VISIBLE)
			mListParent.setVisibility(View.VISIBLE);
	}

	public enum ShowMode {
		COLUMN_DATA_LIST, COLUMN_DATA_SEARCH_LIST, COLUMN_DATA_KEY_LIST
	}

	private void updateDocOrders(List<DocInfo> info, Mode mode) {
		HcLog.D(TAG + " updateOrders info = " + info);
		if (mRefreshType == GET_DATA_REFRESH) {
			mDocInfos.clear();
		}
		mDocInfos.addAll(info);
		if (mShowMode == ShowMode.COLUMN_DATA_LIST) {
			mListView.setAdapter(mDataListAdapter);
			mDataListAdapter.notifyDataSetChanged();
		}

		info.clear();
		mCacheData.updateNewsPageNumber(mCurrentColumnId, mCurrentPage, mode);
	}

	private void updateDocSearchOrders(List<SearchDocInfo> info) {
		HcLog.D(TAG + " updateDocSearchOrders info = " + info);
		if (mRefreshSearchType == GET_DATA_REFRESH) {
			mSearchInfos.clear();
		}
		mSearchInfos.addAll(info);
		if (mShowMode == ShowMode.COLUMN_DATA_SEARCH_LIST) {
			if (mDataSearchListAdapter == null) {
				mDataSearchListAdapter = new DocDataSearchListAdapter(this,
						mSearchInfos);
			}
			mDataSearchListAdapter.setKey(mSearchText.getText().toString()
					.trim());
			mListView.setAdapter(mDataSearchListAdapter);
			mDataSearchListAdapter.notifyDataSetChanged();
		}

		info.clear();
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		// TODO Auto-generated method stub
		HcLog.D(TAG + " onFocusChange v = " + v + " hasFocus = " + hasFocus);
		if (v == mSearchText && hasFocus) {
			hindColumns();
			mShowMode = ShowMode.COLUMN_DATA_KEY_LIST;
			if (TextUtils.isEmpty(mSearchText.getText().toString())) {
				mKeyList.clear();
				mKeyList.addAll(mCacheData.getSearchKeys(null));
			}

			if (mKeyAdapter == null) {
				mKeyAdapter = new DocKeyAdapter(this, mKeyList);
				mKeyAdapter.setSearchKeyClickListener(new onSearchKeyClick() {

					@Override
					public void setKey(String key) {
						// TODO Auto-generated method stub
						mSearchText.setText(key);
						// 设置光标的位置
						mSearchText.setSelection(key.length());

						String searchKey = mSearchText.getText().toString();
						if (!TextUtils.isEmpty(searchKey)) {
							HcDialog.showProgressDialog(DocColumnActivity.this,
									"正在检索数据");
							mSearchParent.setFocusable(true);
							mSearchParent.setFocusableInTouchMode(true);
							mSearchParent.requestFocus();
							mCacheData.addSearchKey(searchKey);
							mShowMode = ShowMode.COLUMN_DATA_SEARCH_LIST;
							mCurrentSearchPage = 1;
							mRefreshSearchType = GET_DATA_REFRESH;
							mKey = searchKey;
							mCacheData.searchData(mCurrentColumnId, searchKey,
									mCurrentSearchPage, HcUtil.NEWS_COUNT);
						}
					}
				});
			}
			mListView.setAdapter(mKeyAdapter);
			mListView.setMode(Mode.DISABLED);
		} else {
			;
			// mShowMode = ShowMode.COLUMN_DATA_LIST;
			// mCurrentPage = mCacheData.getCurrentPageNumber(mCurrentColumnId);
			// mListView.setMode(mCacheData.getCurrentMode(mCurrentColumnId));
			// mListView.setAdapter(mDataListAdapter);
			// mDataListAdapter.notifyDataSetChanged();
		}
	}

	private void hideSearch() {
		if (doc_search_show_lly.getVisibility() != View.GONE) {
			doc_search_show_lly.setVisibility(View.GONE);
		}
		if (doc_search_lly.getVisibility() != View.VISIBLE) {
			doc_search_lly.setVisibility(View.VISIBLE);
		}
		if (search_lly.getVisibility() != View.VISIBLE) {
			search_lly.setVisibility(View.VISIBLE);
		}
		mSearchText.requestFocus();
	}

	private void showSearch() {
		if (doc_search_show_lly.getVisibility() != View.VISIBLE) {
			doc_search_show_lly.setVisibility(View.VISIBLE);
		}
		if (doc_search_lly.getVisibility() != View.GONE) {
			doc_search_lly.setVisibility(View.GONE);
		}
		if (search_lly.getVisibility() != View.GONE) {
			search_lly.setVisibility(View.GONE);
		}
	}

	private void finishActivity() {
		HcAppState.getInstance().removeActivity(this);
		finish();
	}
}
