package com.android.hcframe.push;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.TopBarView;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.HttpRequestQueue;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.sql.SettingHelper;

public class PushSettingsActivity extends HcBaseActivity implements PushSettingsAdapter.OnCheckCallback {

	private static final String TAG = "PushSettingsActivity";

	private List<PushItem> mPushItems = new ArrayList<PushItem>();

	private PushSettingsAdapter mAdapter;

	private String mChannelId;

	private TopBarView mTopbar;

	private ListView mListView;

	private Handler mHandler = new Handler();

	private String mJsonData;

	private String mListMd5Url;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_push_settings);
		mTopbar = (TopBarView) findViewById(R.id.push_settings_top_bar);
		mListView = (ListView) findViewById(R.id.push_settings_listview);

		mTopbar.setTitle(getString(R.string.push_settings));
		mChannelId = SettingHelper.getChannelId(this);

		// 这里不用管版本的问题,反正会去服务端获取,只需要根据当前的用户获取数据

		String jsonData = SettingHelper.getPushSubSettings(this);
		if (!TextUtils.isEmpty(jsonData)) {
			parseJson(jsonData);
		} else {
			HcDialog.showProgressDialog(this, "正在获取数据...");
		}
		mAdapter = new PushSettingsAdapter(this, mPushItems);
		mAdapter.setOnCheckCallback(this);
		mListView.setAdapter(mAdapter);
		// 去服务端获取数据
		PushListRequest request = new PushListRequest();
		PushListRespone respone = new PushListRespone();
		if (HcUtil.isNetWorkError(this)) {
			HcDialog.deleteProgressDialog();

		} else {
			request.sendRequestCommand(RequestCategory.PushModuleList, respone, false);
		}

	}


	private void parseJson(String json) {
		mJsonData = json;
		mPushItems.clear();
		try {
			JSONObject object = new JSONObject(json);
			JSONArray array = object.getJSONArray("push_appList");
			int itemSize = array.length();
			PushModuleItem item;
			PushSubModuleItem subItem;
			for (int i = 0; i < itemSize; i++) {
				item = new PushModuleItem();
				object = array.getJSONObject(i);
				if (HcUtil.hasValue(object, "app_id")) {
					item.setId(object.getString("app_id"));
				}
				if (HcUtil.hasValue(object, "app_name")) {
					item.setName(object.getString("app_name"));
				}
				JSONArray pushList = object.getJSONArray("pushList");
				int subSize = pushList.length();
				for (int j = 0; j < subSize; j++) {
					subItem = new PushSubModuleItem();
					object = pushList.getJSONObject(j);
					if (HcUtil.hasValue(object, "is_push")) {
						subItem.setPushed(object.getInt("is_push") == 1);
					}
					if (HcUtil.hasValue(object, "item_id")) {
						subItem.setId(object.getString("item_id"));
					}
					if (HcUtil.hasValue(object, "item_name")) {
						subItem.setName(object.getString("item_name"));
					}
					item.addItem(subItem);
				}
				mPushItems.add(item);
				mPushItems.addAll(item.getItems());
			}
		} catch (Exception e) {
			HcLog.D(TAG + " #parseJson data = "+json);
		}
	}

	@Override
	public void onCheckChanged(PushItem item, ToggleButton view) {
		if (HcUtil.isNetWorkError(this)) {
			view.setChecked(item.isPushed());
		} else {
			HcDialog.showProgressDialog(this, "正在提交更改...");
			PushItemRequest request = new PushItemRequest(item);
			PushItemRespone respone = new PushItemRespone(item, view);
			request.sendRequestCommand(RequestCategory.UpdatePushSettings, respone, false);
		}
	}

	private class PushListRequest extends AbstractHttpRequest {

		@Override
		public String getParameterUrl() {
			return "versioncode=" + HcConfig.getConfig().getAppVersion() + "&channelID=" + mChannelId
					+ "&pType=0";
		}

		@Override
		public String getRequestMethod() {
			return "clientPushList";
		}
	}

	private class PushListRespone extends AbstractHttpResponse {

		private static final String TAG = PushSettingsActivity.TAG + "$PushListRespone";

		@Override
		public String getTag() {
			return TAG;
		}

		@Override
		public void onSuccess(final Object data, RequestCategory request) {
			HcLog.D(TAG + " onSuccess  data = "+data);
			HcDialog.deleteProgressDialog();
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mListMd5Url = null;
					parseJson((String) data);
					mAdapter.notifyDataSetChanged();
					SettingHelper.setPushSubSettings(PushSettingsActivity.this, (String) data);
				}
			});
		}

		@Override
		public void onAccountExcluded(String data, String msg, RequestCategory category) {
			mListMd5Url = null;
			HcUtil.reLogining(data, PushSettingsActivity.this, msg);
		}

		@Override
		public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
			mListMd5Url = md5Url;
		}
	}

	private class PushItemRequest extends AbstractHttpRequest {

		private PushItem mItem;

		public PushItemRequest(PushItem item) {
			mItem = item;
		}

		@Override
		public String getParameterUrl() {
			return "channelId=" + mChannelId + "&item_id=" + mItem.getId() + "&Is_push="
					+ (mItem.isPushed() ? 0 : 1); // 这里的item数据还没有变化,所以要0-->1; 1-->0
		}

		@Override
		public String getRequestMethod() {
			return "saveClientPush";
		}
	}

	private class PushItemRespone extends AbstractHttpResponse {

		private static final String TAG = PushSettingsActivity.TAG + "$PushListRespone";

		private PushItem mItem;

		private ToggleButton mView;

		public PushItemRespone(PushItem item, ToggleButton view) {
			mItem = item;
			mView = view;
		}

		@Override
		public String getTag() {
			return TAG;
		}

		@Override
		public void onSuccess(final Object data, RequestCategory request) {
			HcLog.D(TAG + " onSuccess  data = "+data);
			HcDialog.deleteProgressDialog();
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					// 替换数据
					update(mItem);
					// 更改状态
					mItem.setPushed(!mItem.isPushed());

				}
			});
		}

		@Override
		public void onAccountExcluded(String data, String msg, RequestCategory category) {
			mView.setChecked(mItem.isPushed());
			HcUtil.reLogining(data, PushSettingsActivity.this, msg);
		}

		@Override
		public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

		}

		@Override
		public void onConnectionTimeout(RequestCategory request) {
			super.onConnectionTimeout(request);
			mView.setChecked(mItem.isPushed());
		}

		@Override
		public void onNetworkInterrupt(RequestCategory request) {
			super.onNetworkInterrupt(request);
			mView.setChecked(mItem.isPushed());
		}

		@Override
		public void onParseDataError(RequestCategory request) {
			super.onParseDataError(request);
			mView.setChecked(mItem.isPushed());
		}

		@Override
		public void onRequestCanel(RequestCategory request) {
			super.onRequestCanel(request);
			mView.setChecked(mItem.isPushed());
		}

		@Override
		public void onRequestFailed(int code, String msg, RequestCategory request) {
			super.onRequestFailed(code, msg, request);
			mView.setChecked(mItem.isPushed());
		}

		@Override
		public void onResponseFailed(int code, RequestCategory request) {
			super.onResponseFailed(code, request);
			mView.setChecked(mItem.isPushed());
		}

		@Override
		public void unknown(RequestCategory request) {
			super.unknown(request);
			mView.setChecked(mItem.isPushed());
		}
	}

	@Override
	protected void onDestroy() {
		if (!TextUtils.isEmpty(mListMd5Url)) {
			HttpRequestQueue.getInstance().cancelRequest(mListMd5Url);
			mListMd5Url = null;
		}

		super.onDestroy();
	}

	private void update(PushItem item) {
		String data = null;
		StringBuilder builder = new StringBuilder();

		String old = getItemString(builder, "is_push", item.isPushed() ? "1" : "0", "item_id", item.getId());
		String newItem;
		if (mJsonData.contains(old)) {
			newItem = getItemString(builder, "is_push", item.isPushed() ? "0" : "1", "item_id", item.getId());
			data = mJsonData.replace(old, newItem);
		} else {
			old = getItemString(builder, "item_id", item.getId(), "is_push", item.isPushed() ? "1" : "0");
			if (mJsonData.contains(old)) {
				newItem = getItemString(builder, "item_id", item.getId(), "is_push", item.isPushed() ? "0" : "1");
				data = mJsonData.replace(old, newItem);
			} else {
				old = getItemString(builder, "is_push", item.isPushed() ? "1" : "0", "item_name", item.getName(), "item_id", item.getId());

				if (mJsonData.contains(old)) {
					newItem = getItemString(builder, "is_push", item.isPushed() ? "0" : "1", "item_name", item.getName(), "item_id", item.getId());
					data = mJsonData.replace(old, newItem);
				} else {
					old = getItemString(builder, "item_id", item.getId(), "item_name", item.getName(), "is_push", item.isPushed() ? "1" : "0");
					if (mJsonData.contains(old)) {
						newItem = getItemString(builder, "item_id", item.getId(), "item_name", item.getName(), "is_push", item.isPushed() ? "0" : "1");
						data = mJsonData.replace(old, newItem);
					} else {
						HcLog.D(TAG + " #update failed name = "+item.getName() + " id = "+item.getId() + " pushed = "+item.isPushed());
					}
				}
			}
		}

		if (data != null) {
			SettingHelper.setPushSubSettings(this, data);
		}
	}

	private String getItemString(StringBuilder builder, String... value) {
		int size = value.length;
		for (int i = 0; i < size; i++) {
			builder.append("\"");
			builder.append(value[i]);
			if (i % 2 == 0)
				builder.append("\":");
			else {
				builder.append("\",");
			}
		}
		builder.deleteCharAt(builder.length() - 1);
		String data = builder.toString();
		HcLog.D(TAG + "#getItemString data = "+data);
		builder.delete(0, builder.length());
		return data;
	}
}
