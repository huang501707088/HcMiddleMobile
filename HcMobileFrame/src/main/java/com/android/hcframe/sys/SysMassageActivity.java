/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-3 下午1:57:08
*/
package com.android.hcframe.sys;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.R;
import com.android.hcframe.TopBarView;
import com.android.hcframe.doc.data.DocInfo;
import com.android.hcframe.push.HcPushManager;
import com.android.hcframe.push.PushHtmlActivity;
import com.android.hcframe.push.PushInfo;
import com.android.hcframe.servicemarket.photoscan.ImageScanActivity;
import com.android.hcframe.sql.OperateDatabase;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class SysMassageActivity extends HcBaseActivity implements OnItemClickListener,
	Observer {

	private static final String TAG = "SysMassageActivity";

	private ListView mListView;
	
	private List<SystemMessage> mMessages = new ArrayList<SystemMessage>();
	
	private SysMassageAdapter mAdapter;
	
	private TopBarView mTopBarView;

	private static final String ACTION_IMAGE = "com.android.hcframe.start_image";

	private static final String ACTION_DOC = "com.android.hcframe.start_doc";

	private String mAppId = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (intent.getExtras() != null) {
			mAppId = intent.getStringExtra("appId");
		}
		if (TextUtils.isEmpty(mAppId)) {
			finish();
			return;
		}
		setContentView(R.layout.activity_system_massage);
		mTopBarView = (TopBarView) findViewById(R.id.system_message_top_bar);
		mListView = (ListView) findViewById(R.id.system_message_list);
		
		mAdapter = new SysMassageAdapter(this, mMessages);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mTopBarView.setTitle("系统消息");
		HcPushManager.getInstance().addObserver(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		SystemMessage message = (SystemMessage) parent.getItemAtPosition(position);
		PushInfo info = new PushInfo(null);
		info.setAppId(message.getAppId());
		info.setContent(message.getContentId());
		info.setType("" + message.getType());
		HcPushManager.getInstance().setPushInfo(info);
		info.startActivityFromIM(this, message.getMessageId());
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		refreshMessage();
	}

	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub
		if (observable != null && observable instanceof HcPushManager) {
			refreshMessage();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		HcPushManager.getInstance().deleteObserver(this);
		super.onDestroy();
	}
	
	private void refreshMessage() {
		mMessages.clear();
		mMessages.addAll(OperateDatabase.getSystemMessages(this, mAppId));
		mAdapter.notifyDataSetChanged();
	}
	
	/**
	 * 进入相应的内容界面，
	 * 这里要是是清除缓存的，先忽略掉
	 * @author jrjin
	 * @time 2015-12-4 下午1:44:26
	 * @param message
	 */
	private void startContentActivity(SystemMessage message) {
		Intent intent = new Intent();
		String appId = message.getAppId();
		int type = message.getType();
		if (TextUtils.isEmpty(appId)) return;
		if (appId.equals("240")) { // 新闻频道
			switch (type) {
			case PushInfo.TYPE_URL:
			case PushInfo.TYPE_VIDEO:
			case PushInfo.TYPE_ONLINE:
				intent.setClass(this, PushHtmlActivity.class);
				intent.putExtra("id", message.getContentId());				
				break;
			case PushInfo.TYPE_IMAGE:
//				intent.setPackage(getPackageName());
//				intent.setAction(ACTION_IMAGE);
//				intent.putExtra("newsId", message.getContentId());
//				sendBroadcast(intent);
				intent.setClass(this, ImageScanActivity.class);
				intent.putExtra(ImageScanActivity.EXTRA_IMAGE_ID, message.getContentId());
				break;
			case PushInfo.TYPE_PDF:
				intent.setPackage(getPackageName());
				intent.setAction(ACTION_DOC);
				intent.putExtra("data_id", message.getContentId());
				intent.putExtra("data_flag", DocInfo.FLAG_TITIL);
				sendBroadcast(intent);

//				intent.setClass(this, DocDetailsActivity.class);
//				intent.putExtra("data_id", message.getContentId());
//				intent.putExtra("data_flag", DocInfo.FLAG_TITIL);
				return;

			default:
				return;
			}
		} else if (appId.equals("241")) {
			switch (type) {
			case PushInfo.TYPE_PDF:
				intent.setPackage(getPackageName());
				intent.setAction(ACTION_DOC);
				intent.putExtra("data_id", message.getContentId());
				intent.putExtra("data_flag", DocInfo.FLAG_TITIL);
				sendBroadcast(intent);

//				intent.setClass(this, DocDetailsActivity.class);
//				intent.putExtra("data_id", message.getContentId());
//				intent.putExtra("data_flag", DocInfo.FLAG_TITIL);
				return;

			default:
				return;
			}
		} else if (appId.equals("242")) {
			switch (type) {
			case PushInfo.TYPE_APP: // 这里处理起来比较复杂
				;
				break;

			default:
				return;
			}
		} else {
			return;
		}
		startActivity(intent);
		overridePendingTransition(0, 0);
	}
}
