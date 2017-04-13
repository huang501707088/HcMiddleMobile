/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-11-2 上午9:39:05
 */
package com.android.hcframe.push;

import com.android.hcframe.HcConfig;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.TopBarView;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.http.ResponseCodeInfo;
import com.android.hcframe.menu.HcWebChromeClient;
import com.android.hcframe.servicemarket.photoscan.NewsDetailsInfo;
import com.android.hcframe.sql.SettingHelper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.webkit.WebSettings.PluginState;
import android.widget.ProgressBar;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PushHtmlActivity extends Activity implements IHttpResponse {

	private static final String TAG = "PushHtmlActivity";

	private WebView mWebView;

	private WebSettings mSettings;

	private TopBarView mTopBarView;

	private ProgressBar mProgressBar;

	private String mId;

	private NewsDetailsInfo mdetails = null;

	/** 请求的头部 */
	private Map<String, String> mHeaders = new HashMap<String, String>();

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
				case 1002:
					HcDialog.deleteProgressDialog();
					break;

				default:
					break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (intent != null && intent.getExtras() != null) {
			mId = intent.getStringExtra("id");
			if (TextUtils.isEmpty(mId)) {
				finish();
				return;
			}
		} else {
			finish();
			return;
		}
		HcAppState.getInstance().addActivity(this);
		setContentView(R.layout.html5_layout);
		initViews();

		// HcDialog.showProgressDialog(this, "加载数据...");

		// initData("百度", "http://www.baidu.com");

		HcLog.D(TAG + " it is onCreate end!");

		if (mdetails == null) {
			HcHttpRequest.getRequest().sendQueryNewsDetails(mId, this);
			HcDialog.showProgressDialog(this,
					R.string.dialog_title_get_data);
		}

	}

	private void initViews() {
		mTopBarView = (TopBarView) findViewById(R.id.html_top_bar);
		mWebView = (WebView) findViewById(R.id.html_webview);

		mProgressBar = (ProgressBar) findViewById(R.id.html_progress);

		mSettings = mWebView.getSettings();
		mSettings.setJavaScriptEnabled(true);
		mSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		mSettings.setSaveFormData(false);
		mSettings.setDatabaseEnabled(true);
		mSettings.setDomStorageEnabled(true);
		mSettings.setDefaultTextEncodingName("UTF-8");
		mSettings.setAllowFileAccess(true);

		mSettings.setPluginState(PluginState.ON);
		mSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		mWebView.setWebViewClient(mWebViewClient);
		mWebView.setWebChromeClient(mWebChromeClient);
		mWebView.clearView();
		HcLog.D(TAG + " scale = " + mWebView.getScale());
		mTopBarView.setReturnViewListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onReturn();
			}
		});
	}

	private void initData(String title, String url) {
		mTopBarView.setTitle(title);
		mHeaders.put("account", SettingHelper.getAccount(this));
		mHeaders.put("clientId", HcConfig.getConfig().getClientId());
		String token = SettingHelper.getToken(this);
		token = TextUtils.isEmpty(token) ? "-1" : token;
		mHeaders.put("token", token);
		mHeaders.put("terminalId", HcUtil.getIMEI(this));
		mHeaders.put("userId", SettingHelper.getUserId(this));
		mWebView.loadUrl(url, mHeaders);
	}

	// 浏览网页历史记录
	// goBack()和goForward()
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// if (mWebView.canGoBack()) {
			// mWebView.goBack();
			// } else {
			// onReturn();
			// }
			// return true;
			if (event.getRepeatCount() == 0) {
				event.startTracking();
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (event.isTracking() && !event.isCanceled()) {
				if (mWebView.canGoBack()) {
					mWebView.goBack();
				} else {
					onReturn();
				}
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	private void onReturn() {
		// HcAppState.getInstance().removeActivity(this);
		// if (!HcAppState.getInstance().getAppOnStarted()) {
		// Intent intent = new Intent();
		// intent.setClass(this, Menu1Activity.class);
		// startActivity(intent);
		//
		// }
//		HcAppState.getInstance().startMainActivity(this);
		finish();
		overridePendingTransition(0, 0);
		// overridePendingTransition(R.anim.activity_open_enter,
		// R.anim.activity_close_enter);
	}

	private WebViewClient mWebViewClient = new WebViewClient() {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// TODO Auto-generated method stub
			HcLog.D(TAG + "#shouldOverrideUrlLoading url = " + url);
			// view.loadUrl(url);
			// return true;
			boolean override = super.shouldOverrideUrlLoading(view, url);
			HcLog.D(TAG + " #shouldOverrideUrlLoading should override = "
					+ override);
			return override;
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			HcLog.D(TAG + " onPageStarted!");
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			HcLog.D(TAG + " onPageFinished!");
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			// TODO Auto-generated method stub
			HcLog.D(TAG + " onReceivedError errorCode = " + errorCode
					+ " description =" + description + " failingUrl = "
					+ failingUrl);
			super.onReceivedError(view, errorCode, description, failingUrl);
		}

	};

	private WebChromeClient mWebChromeClient = new HcWebChromeClient(this) {

		private View myView = null;
		private CustomViewCallback myCallback = null;


		// Android 使WebView支持HTML5 Video（全屏）播放的方法
		@Override
		public void onShowCustomView(View view, CustomViewCallback callback) {
			if (myCallback != null) {
				myCallback.onCustomViewHidden();
				myCallback = null;
				return;
			}

			ViewGroup parent = (ViewGroup) mWebView.getParent();
			parent.removeView(mWebView);
			parent.addView(view);
			myView = view;
			myCallback = callback;
			mWebChromeClient = this;
		}

		@Override
		public void onHideCustomView() {
			if (myView != null) {
				if (myCallback != null) {
					myCallback.onCustomViewHidden();
					myCallback = null;
				}

				ViewGroup parent = (ViewGroup) myView.getParent();
				parent.removeView(myView);
				parent.addView(mWebView);
				myView = null;
			}
		}

		@Override
		public boolean onJsAlert(WebView view, String url, String message,
				JsResult result) {
			// TODO Auto-generated method stub
			HcLog.D(TAG + " url = " + url + " message = " + message);
			// return super.onJsAlert(view, url, message, result);
			try {
				JSONObject object = new JSONObject(message);
				int code = object.getInt("code");
				String msg = object.getString("msg");
				switch (code) {
					case HcHttpRequest.REQUEST_ACCOUT_EXCLUDED:
					case HcHttpRequest.REQUEST_TOKEN_FAILED:
					case 103:
						if (HcUtil.hasValue(object, "body")) {
							HcUtil.reLogining(object.getJSONObject("body").toString(), PushHtmlActivity.this, msg);
						}
						break;
					case 100:
						HcDialog.showProgressDialog(PushHtmlActivity.this, msg);
						break;
					case 200:
						mHandler.sendEmptyMessageDelayed(1002, 1200);
						HcDialog.showSuccessDialog(PushHtmlActivity.this, msg);
						break;
					case 400:
					case 500:
						HcDialog.deleteProgressDialog();
						HcUtil.showToast(PushHtmlActivity.this, msg);
						break;
					case 600:
						HcDialog.deleteProgressDialog();
						break;
					default:
						break;
				}


			} catch (Exception e) {
				// TODO: handle exception
				return super.onJsAlert(view, url, message, result);
			}
			result.cancel();
			return true;
		}

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			// TODO Auto-generated method stub
			HcLog.D(TAG + " onProgressChanged = " + newProgress);
			if (newProgress < 0 || newProgress == 100) {
				mProgressBar.setVisibility(View.GONE);
			} else {
				if (mProgressBar.getVisibility() != View.VISIBLE)
					mProgressBar.setVisibility(View.VISIBLE);
				mProgressBar.setProgress(newProgress);
			}
		}

	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		HcLog.D(TAG + " it is onDestory start!");
		HcAppState.getInstance().removeActivity(this);
		mWebView.stopLoading();
		mWebView = null;
		super.onDestroy();
		HcLog.D(TAG + " it is onDestory end!");
	}

	@Override
	public void notify(Object data, RequestCategory request,
			ResponseCategory category) {
		// TODO Auto-generated method stub
		// 显示内容
		// initData(title, url);

		HcDialog.deleteProgressDialog();
		if (request == RequestCategory.NEWDETAILS) {
			switch (category) {
			case SESSION_TIMEOUT:
			case NETWORK_ERROR:
				HcUtil.toastTimeOut(this);
				break;
			case DATA_ERROR:
				HcUtil.toastDataError(this);
				break;
			case SYSTEM_ERROR:
				HcUtil.toastSystemError(this, data);
				break;
			case SUCCESS:
				if (data != null && data instanceof NewsDetailsInfo) {
					mdetails = (NewsDetailsInfo) data;
					// 显示内容
					initData(mdetails.getTitle(), mdetails.getItemUrl());
				}
				break;
			case REQUEST_FAILED:
				ResponseCodeInfo info = (ResponseCodeInfo) data;
				/**
				 * @author zhujb
				 * @date 2016-04-13 下午4:19:07
				 */
				if (info.getCode() == HcHttpRequest.REQUEST_ACCOUT_EXCLUDED ||
						info.getCode() == HcHttpRequest.REQUEST_TOKEN_FAILED) {
					HcUtil.reLogining(info.getBodyData(), this, info.getMsg());
				} else {
					HcUtil.showToast(this, info.getMsg());
				}
				break;
			default:
				break;

			}
			
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		HcLog.D(TAG + " it is onResume end!");
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		HcLog.D(TAG + " it is onNewIntent end! intent = " + intent);
		/**
		 * 1.关闭原先的请求 2.显示对话框 3.
		 */
		if (intent != null && intent.getExtras() != null) {
			mId = intent.getStringExtra("id");
			if (TextUtils.isEmpty(mId)) {
				finish();
				return;
			}
		} else {
			finish();
			return;
		}
	}

	@Override
	public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
		// TODO Auto-generated method stub

	}

}
