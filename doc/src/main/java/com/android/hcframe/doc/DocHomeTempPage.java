/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-9-1 上午11:15:52
 */
package com.android.hcframe.doc;

import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcObserver;
import com.android.hcframe.HcSubject;
import com.android.hcframe.HcUtil;
import com.android.hcframe.doc.data.DocCacheData;
import com.android.hcframe.doc.data.DocColumn;
import com.android.hcframe.doc.data.DocColumnAdapter;
import com.android.hcframe.doc.data.DocDataSearchListAdapter;
import com.android.hcframe.doc.data.DocHistoricalRecord;
import com.android.hcframe.doc.data.DocHistoryRecordAdapter;
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
import com.android.hcframe.push.HcPushManager;
import com.android.hcframe.push.PushInfo;
import com.android.hcframe.sql.SettingHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class DocHomeTempPage extends AbstractPage implements
        OnItemClickListener, TextWatcher, HcObserver, OnFocusChangeListener {

    private static final String TAG = "DocHomeTempPage";

    /**
     * Search
     */
    private LinearLayout mSearchParent;

    private ImageView mColumnBtn;

    private ImageView mSearchBtn;

    private EditText mSearchText;

    private LinearLayout doc_search_show_lly;

    private ImageView doc_search_image_clear;

    private LinearLayout search_lly;

    private LinearLayout doc_search_lly;

    /**
     * data list panel
     */
    private LinearLayout mListParent;

    private PullToRefreshListView mListView;

    private LinearLayout mEmpty;

    private TextView data_list_title;

    private TextView doc_home_list_empty_text;

    /**
     * columns panel
     */
    private LinearLayout mColumnParent;

    private RelativeLayout mColumnHomeBtn;

    private GridView mGridView;

    private DocCacheData mCacheData;

    /**
     * 栏目列表
     */
    private List<DocColumn> mColumns;

    private DocColumnAdapter mColumnAdapter;

    private DocHistoryRecordAdapter mHistoryRecordAdapter;

    private DocDataSearchListAdapter mDataSearchListAdapter;

    private DocKeyAdapter mKeyAdapter;

    private List<SearchDocInfo> mSearchInfos = new ArrayList<SearchDocInfo>();

    private static final int GET_DATA_REFRESH = 0;

    private static final int GET_DATA_MORE = 1;

    /**
     * 搜索页
     */
    private int mCurrentSearchPage = 1;
    /**
     * 搜索页刷新类型
     */
    private int mRefreshSearchType = GET_DATA_REFRESH;

    /**
     * 检索时，匹配到的Keys
     */
    private List<String> mKeyList = new ArrayList<String>();

    private String mKey;

    private ShowMode mShowMode = ShowMode.DATA_HISTORY_LIST;

    private static final String ALL_COLUMN = "-1";

    private List<DocHistoricalRecord> mRecords = new ArrayList<DocHistoricalRecord>();

    public static final int COLUMN_REQUEST_CODE = 1 << 2;

    private final String mAppId;

    protected DocHomeTempPage(Activity context, ViewGroup group, String appId) {
        super(context, group);
        // TODO Auto-generated constructor stub
        mAppId = appId;
    }

    @Override
    public void update(Observable observable, Object data) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int id = v.getId();
        if (id == R.id.doc_column_selector_image) {
            mSearchParent.setFocusable(true);
            mSearchParent.setFocusableInTouchMode(true);
            mSearchParent.requestFocus();
            mSearchText.setText("");
            showColumns();
            createColumns();
            showSearch();
        } else if (id == R.id.doc_search_image) {
            String key = mSearchText.getText().toString();
            if (!TextUtils.isEmpty(key)) {
                mListView.setMode(Mode.PULL_FROM_START);
                HcDialog.showProgressDialog(mContext, "正在检索数据");
                mSearchParent.setFocusable(true);
                mSearchParent.setFocusableInTouchMode(true);
                mSearchParent.requestFocus();
                mCacheData.addSearchKey(key);
                mShowMode = ShowMode.ALL_COLUMN_DATA_SEARCH_LIST;
                mCurrentSearchPage = 1;
                mRefreshSearchType = GET_DATA_REFRESH;
                mKey = key;
                mCacheData.searchData(ALL_COLUMN, key, mCurrentSearchPage,
                        HcUtil.NEWS_COUNT);
            }
        } else if (id == R.id.doc_column_home_btn) {
            mListView.setMode(Mode.DISABLED);
            mRecords.clear();
            mRecords.addAll(mCacheData.getHistoricalRecords());
            mShowMode = ShowMode.DATA_HISTORY_LIST;
            mListView.setAdapter(mHistoryRecordAdapter);
            data_list_title
                    .setText(mContext.getString(R.string.history_record));
            hindColumns();
        } else if (id == R.id.doc_search_image_clear) {
            mSearchText.setText("");
        } else if (id == R.id.doc_search_show_lly) {
            hideSearch();
        }

    }

    @Override
    public void initialized() {
        // TODO Auto-generated method stub
        if (mCacheData == null) {
            mCacheData = DocCacheData.getInstance();
            mCacheData.addObserver(this);
        }
        mColumnBtn.setOnClickListener(this);
        mSearchBtn.setOnClickListener(this);
        mColumnHomeBtn.setOnClickListener(this);
        mSearchText.addTextChangedListener(this);
        mSearchText.setOnFocusChangeListener(this);
        mGridView.setOnItemClickListener(this);
        mListView.setOnItemClickListener(this);
        doc_search_show_lly.setOnClickListener(this);
        doc_search_image_clear.setOnClickListener(this);
        mListView.setScrollingWhileRefreshingEnabled(false);
        mListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {
                // TODO Auto-generated method stub
                if (mListView.getMode() == Mode.PULL_FROM_START) {
                    HcUtil.showToast(mContext, "没有更多数据！");
                }
            }
        });

        mListView
                .setOnRefreshBothListener(new OnRefreshBothListener<ListView>() {

                    @Override
                    public void onPullDownToRefresh(
                            PullToRefreshBase<ListView> refreshView) {
                        // TODO Auto-generated method stub

                        mCurrentSearchPage = 1;
                        mRefreshSearchType = GET_DATA_REFRESH;
                        mCacheData.searchData(ALL_COLUMN, mKey != null ? mKey
                                : "", mCurrentSearchPage, HcUtil.NEWS_COUNT);

                    }

                    @Override
                    public void onPullUpToRefresh(
                            PullToRefreshBase<ListView> refreshView) {
                        // TODO Auto-generated method stub

                        mCurrentSearchPage++;
                        mRefreshSearchType = GET_DATA_MORE;
                        mCacheData.searchData(ALL_COLUMN, mKey != null ? mKey
                                : "", mCurrentSearchPage, HcUtil.NEWS_COUNT);

                    }
                });
        mListView.setMode(Mode.DISABLED);
        mRecords.clear();
        mRecords.addAll(mCacheData.getHistoricalRecords());
        mShowMode = ShowMode.DATA_HISTORY_LIST;
        if (mHistoryRecordAdapter == null) {
            mHistoryRecordAdapter = new DocHistoryRecordAdapter(mContext,
                    mRecords);
            mListView.setAdapter(mHistoryRecordAdapter);
        }
    }

    @Override
    public void setContentView() {
        // TODO Auto-generated method stub
        if (isFirst) {
            isFirst = !isFirst;
            mView = mInflater.inflate(R.layout.doc_home_page_layout, null);

            mSearchParent = (LinearLayout) mView
                    .findViewById(R.id.doc_column_search_parent);
            doc_search_show_lly = (LinearLayout) mView
                    .findViewById(R.id.doc_search_show_lly);
            doc_search_image_clear = (ImageView) mView
                    .findViewById(R.id.doc_search_image_clear);
            search_lly = (LinearLayout) mView.findViewById(R.id.search_lly);
            doc_search_lly = (LinearLayout) mView
                    .findViewById(R.id.doc_search_lly);
            mColumnBtn = (ImageView) mView
                    .findViewById(R.id.doc_column_selector_image);
            mSearchBtn = (ImageView) mView.findViewById(R.id.doc_search_image);
            mSearchText = (EditText) mView.findViewById(R.id.search_content_et);

            mListParent = (LinearLayout) mView
                    .findViewById(R.id.doc_home_data_list_parent);
            data_list_title = (TextView) mView
                    .findViewById(R.id.data_list_title);
            mListView = (PullToRefreshListView) mView
                    .findViewById(R.id.doc_home_data_list);
            mEmpty = (LinearLayout) mView
                    .findViewById(R.id.doc_home_list_empty_view);

            doc_home_list_empty_text = (TextView) mView
                    .findViewById(R.id.doc_home_list_empty_text);

            mColumnParent = (LinearLayout) mView
                    .findViewById(R.id.doc_home_columns_parent);
            mColumnHomeBtn = (RelativeLayout) mView
                    .findViewById(R.id.doc_column_home_btn);
            mGridView = (GridView) mView.findViewById(R.id.doc_home_columns_gv);

            mListView.setEmptyView(mEmpty);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        // TODO Auto-generated method stub
        HcLog.D(TAG + " onFocusChange v = " + v + " hasFocus = " + hasFocus);
        if (v == mSearchText && hasFocus) {
            hindColumns();
            data_list_title.setText(R.string.search_record);
            doc_home_list_empty_text.setText(R.string.search_none);
            mShowMode = ShowMode.DATA_KEY_LIST;
            if (TextUtils.isEmpty(mSearchText.getText().toString())) {
                mKeyList.clear();
                mKeyList.addAll(mCacheData.getSearchKeys(null));
            }

            if (mKeyAdapter == null) {
                mKeyAdapter = new DocKeyAdapter(mContext, mKeyList);
                mKeyAdapter.setSearchKeyClickListener(new onSearchKeyClick() {

                    @Override
                    public void setKey(String key) {
                        // TODO Auto-generated method stub
                        mSearchText.setText(key);
                        // 设置光标的位置
                        mSearchText.setSelection(key.length());

                        String searchKey = mSearchText.getText().toString();
                        if (!TextUtils.isEmpty(searchKey)) {
                            HcDialog.showProgressDialog(mContext, "正在检索数据");
                            mSearchParent.setFocusable(true);
                            mSearchParent.setFocusableInTouchMode(true);
                            mSearchParent.requestFocus();
                            mCacheData.addSearchKey(searchKey);
                            mShowMode = ShowMode.ALL_COLUMN_DATA_SEARCH_LIST;
                            mCurrentSearchPage = 1;
                            mRefreshSearchType = GET_DATA_REFRESH;
                            mKey = searchKey;
                            mCacheData.searchData(ALL_COLUMN, searchKey,
                                    mCurrentSearchPage, HcUtil.NEWS_COUNT);
                        }
                    }
                });
            }
            mListView.setAdapter(mKeyAdapter);
            mListView.setMode(Mode.DISABLED);
        }
    }

    @Override
    public void updateData(HcSubject subject, Object data,
                           RequestCategory request, ResponseCategory response) {
        // TODO Auto-generated method stub
        HcDialog.deleteProgressDialog();
        mListView.onRefreshComplete();
        HcLog.D(TAG + " updateData request = " + request + " response = "
                + response);
        switch (request) {
            case SEARCH_ALL_DATA:
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
                                if (mShowMode == ShowMode.ALL_COLUMN_DATA_SEARCH_LIST) {
                                    mListView.setMode(Mode.PULL_FROM_START);
                                }
                            } else if (size < HcUtil.NEWS_COUNT) {
                                if (mShowMode == ShowMode.ALL_COLUMN_DATA_SEARCH_LIST) {
                                    mListView.setMode(Mode.PULL_FROM_START);
                                }
                            } else {
                                if (mShowMode == ShowMode.ALL_COLUMN_DATA_SEARCH_LIST) {
                                    mListView.setMode(Mode.BOTH);
                                }
                            }

                            updateDocSearchOrders(infos);

                        }
                        break;
                    case DATA_ERROR:
                        HcUtil.toastDataError(mContext);
                        break;
                    case SESSION_TIMEOUT:
                    case NETWORK_ERROR:
                        HcUtil.toastTimeOut(mContext);
                        break;
                    case ACCOUNT_INVALID:
                        HcUtil.showToast(mContext, "请先登录！");
                        break;
                    case DATA_IS_NULL:
                        HcUtil.showToast(mContext, "没有查到数据");
                        break;
                    /**
                     * czx
                     * 2016.4.14
                     */
                    case REQUEST_FAILED:
                        ResponseCodeInfo info = (ResponseCodeInfo) data;
                        if (HcHttpRequest.REQUEST_TOKEN_FAILED == info.getCode() ||
                                HcHttpRequest.REQUEST_ACCOUT_EXCLUDED == info.getCode()) {
                            HcUtil.reLogining(info.getBodyData(), mContext, info.getMsg());
                        } else {
                            HcUtil.showToast(mContext, info.getMsg());
                        }
                        break;
                    case SYSTEM_ERROR:
                        HcUtil.toastSystemError(mContext, data);
                        break;
                    default:
                        break;
                }
                break;
            case DATA_COLUMN:
                switch (response) {
                    case SUCCESS:
                        if (mColumnAdapter == null) {
                            mColumnAdapter = new DocColumnAdapter(mContext, mColumns,
                                    ALL_COLUMN, mAppId);
                            mGridView.setAdapter(mColumnAdapter);
                        } else {
                            mColumnAdapter.notifyDataSetChanged();
                        }

                        break;
                    case DATA_ERROR:
                        HcUtil.toastDataError(mContext);
                        break;
                    case SESSION_TIMEOUT:
                    case NETWORK_ERROR:
                        HcUtil.toastTimeOut(mContext);
                        break;
                    case ACCOUNT_INVALID:
                        HcUtil.showToast(mContext, "请先登录！");
                        break;
                    /**
                     * czx
                     * 2016.4.14
                     */
                    case REQUEST_FAILED:
                        ResponseCodeInfo info = (ResponseCodeInfo) data;
                        if (HcHttpRequest.REQUEST_TOKEN_FAILED == info.getCode() ||
                                HcHttpRequest.REQUEST_ACCOUT_EXCLUDED == info.getCode()) {
                            HcUtil.reLogining(info.getBodyData(), mContext, info.getMsg());
                        } else {
                            HcUtil.showToast(mContext, info.getMsg());
                        }
                        break;
                    case SYSTEM_ERROR:
                        HcUtil.toastSystemError(mContext, data);
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
        String key = s.toString();
        HcLog.D(TAG + " afterTextChanged key = " + key);
        // if (TextUtils.isEmpty(key)) key = "";
        mKeyList.clear();
        mKeyList.addAll(mCacheData.getSearchKeys(key));
        HcLog.D(TAG + " afterTextChanged key = " + key + " key list size ="
                + mKeyList.size());
        if (mKeyAdapter != null) {
            mKeyAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        // TODO Auto-generated method stub
        if (parent instanceof GridView) {
            mColumnAdapter.setSelectPos(position);
            startColumnActivity((DocColumn) parent.getItemAtPosition(position));

        } else if (parent instanceof ListView) {
            Object item = parent.getItemAtPosition(position);
            if (item instanceof DocHistoricalRecord) {
                DocHistoricalRecord record = (DocHistoricalRecord) item;
                record.setReadTime("" + System.currentTimeMillis());
                record.setUsername(HcUtil.isEmpty(SettingHelper
                        .getAccount(mContext)) ? "---" : SettingHelper
                        .getAccount(mContext));
                mCacheData.addDocHistoricalRecord(record, record.getFileId());
                startDetailActivity(record.getFileId(), DocInfo.FLAG_TITIL);
            } else if (item instanceof SearchDocInfo) {
                SearchDocInfo info = (SearchDocInfo) item;
                DocHistoricalRecord record = new DocHistoricalRecord();
                record.setReadTime("" + System.currentTimeMillis());
                record.setFileId(info.getFileId());
                record.setFileName(info.getFileName());
                record.setFileSize(info.getFileSize());
                record.setFlag(info.getFlag());
                record.setUsername(HcUtil.isEmpty(SettingHelper
                        .getAccount(mContext)) ? "---" : SettingHelper
                        .getAccount(mContext));
                mCacheData.addDocHistoricalRecord(record, info.getFileId());
                startDetailActivity(info.getFileId(), info.getFlag());
            }
        }
    }

    private void startColumnActivity(DocColumn column) {
        Intent intent = new Intent(mContext, DocColumnActivity.class);
        intent.putExtra("column_title", column.getmName());
        intent.putExtra("column_id", column.getNewsId());
        intent.putExtra("appId", mAppId);
        mContext.startActivityForResult(intent, COLUMN_REQUEST_CODE);
        mContext.overridePendingTransition(0, 0);
    }

    public enum ShowMode {
        ALL_COLUMN_DATA_SEARCH_LIST, DATA_KEY_LIST, DATA_HISTORY_LIST
    }

    @Override
    public void onDestory() {
        // TODO Auto-generated method stub
        if (mCacheData != null)
            mCacheData.removeObserver(this);
        super.onDestory();

        mGroup.removeAllViews();
        mView = null;
        mGroup = null;
        mContext = null;
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        PushInfo info = HcPushManager.getInstance().getPushInfo();
        if (info != null) {
            HcPushManager.getInstance().setPushInfo(null);
            if (PushInfo.TYPE_PDF == Integer.valueOf(info.getType()) &&
                    mAppId.equals(info.getAppId())) {
                Intent intent = new Intent();
                intent.setClass(mContext, DocDetailsActivity.class);
                intent.putExtra("data_id", info.getContent());
                intent.putExtra("data_flag", DocInfo.FLAG_TITIL);
                mContext.startActivity(intent);
            }

        }

        super.onResume();
    }

    private void createColumns() {
        if (mColumns == null) { // 第一次点击的时候
            mColumns = mCacheData.getDocColumns(mContext);
            if (!mColumns.isEmpty()) {
                if (mColumnAdapter == null) {
                    mColumnAdapter = new DocColumnAdapter(mContext, mColumns,
                            ALL_COLUMN, mAppId);
                    mGridView.setAdapter(mColumnAdapter);
                }
            } else {
                // 弹出对话框
                HcDialog.showProgressDialog(mContext, R.string.dialog_title_get_data);

            }
        } else {
            if (mColumnAdapter == null) {
                mColumnAdapter = new DocColumnAdapter(mContext, mColumns,
                        ALL_COLUMN, mAppId);
                mGridView.setAdapter(mColumnAdapter);
            }

        }
    }

    private void startDetailActivity(String id, int flag) {
        Intent intent = new Intent(mContext, DocDetailsActivity.class);
        intent.putExtra("data_id", id);
        intent.putExtra("data_flag", flag);
        mContext.startActivity(intent);
        mContext.overridePendingTransition(0, 0);
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

    private void updateDocSearchOrders(List<SearchDocInfo> info) {
        HcLog.D(TAG + " updateDocSearchOrders info = " + info);
        if (mRefreshSearchType == GET_DATA_REFRESH) {
            mSearchInfos.clear();
        }
        mSearchInfos.addAll(info);
        if (mShowMode == ShowMode.ALL_COLUMN_DATA_SEARCH_LIST) {
            if (mDataSearchListAdapter == null) {
                mDataSearchListAdapter = new DocDataSearchListAdapter(mContext,
                        mSearchInfos);
            }
            mDataSearchListAdapter.setKey(mSearchText.getText().toString()
                    .trim());
            mListView.setAdapter(mDataSearchListAdapter);
            mDataSearchListAdapter.notifyDataSetChanged();
        }
        data_list_title.setText(String.format(
                mContext.getString(R.string.sum_result), info.size()));
        info.clear();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        HcLog.D(TAG + " onActivityResult requestCode = " + requestCode
                + " resultCode = " + resultCode + " intent = " + data);
        if (requestCode == COLUMN_REQUEST_CODE
                && resultCode == Activity.RESULT_OK) {
            hindColumns();
            data_list_title.setText(R.string.history_record);
            mShowMode = ShowMode.DATA_HISTORY_LIST;
            mRecords.clear();
            mRecords.addAll(mCacheData.getHistoricalRecords());
            mListView.setAdapter(mHistoryRecordAdapter);
            doc_home_list_empty_text.setText(R.string.history_none);
            showSearch();
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

}
