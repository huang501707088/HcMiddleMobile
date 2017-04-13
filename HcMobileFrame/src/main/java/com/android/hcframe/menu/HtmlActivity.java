/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-5-6 上午12:13:27
 */
package com.android.hcframe.menu;

import org.json.JSONObject;

import com.android.hcframe.BaseWebChromeClient;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.TopBarView;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.monitor.LogManager;
import com.android.hcframe.push.HcAppState;
import com.android.hcframe.sql.SettingHelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.PluginState;
import android.widget.ProgressBar;

import java.util.HashMap;
import java.util.Map;

public class HtmlActivity extends Activity {

	private static final String TAG = "HtmlActivity";

	private WebView mWebView;

	private WebSettings mSettings;

	// private ImageView mReturn;
	//
	// private TextView mTitle;

	private String mUrl;

	private String mAppId;

	private String mName;;

	private int mTopBarLeftIcon;

	private TopBarView mTopBarView;
	/**
	 * 需要重新load的Url
	 */
	private String mBaseUrl;

	private ProgressBar mProgressBar;

	/** webview是否主停止加载 */
	private boolean onDestory = false;
	
//	private CookieManager mCookieManager;

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
		HcLog.D(TAG + " it is onCreate! intent = " + intent);
		if (intent != null && intent.getExtras() != null) {
			mName = intent.getStringExtra("title");
			mUrl = intent.getStringExtra("url");
			mTopBarLeftIcon = intent.getIntExtra("lefticon", -1);
			// if (!TextUtils.isEmpty(mUrl))
			// mUrl = mUrl.trim();
			mBaseUrl = intent.getStringExtra("lasturl");
			mAppId = intent.getStringExtra("mAppId");
			HcLog.D(TAG + " it is onCreate! title = " + mName + " url = "
					+ mUrl + " baseUrl = " + mBaseUrl);
			if (TextUtils.isEmpty(mName) || TextUtils.isEmpty(mUrl)) {
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
		/**
		 * @date 2016-1-7 上午11:43:21
		CookieSyncManager.createInstance(this);
		mCookieManager = CookieManager.getInstance();
		mCookieManager.setAcceptCookie(true);
		mCookieManager.removeSessionCookie();
		*/
		initData();
	}

	@SuppressLint("NewApi")
	private void initViews() {
		mTopBarView = (TopBarView) findViewById(R.id.html_top_bar);
		// mReturn = (ImageView) findViewById(R.id.web_return_btn);
		// mTitle = (TextView) findViewById(R.id.web_app_title);
		mWebView = (WebView) findViewById(R.id.html_webview);

		mProgressBar = (ProgressBar) findViewById(R.id.html_progress);

		mSettings = mWebView.getSettings();
		mSettings.setJavaScriptEnabled(true);
		mSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		mSettings.setSaveFormData(false);
		mSettings.setDatabaseEnabled(true);
		mSettings.setDomStorageEnabled(true);
		//设置webview加载的页面的模式
//		mSettings.setLoadWithOverviewMode(true);
		//设置webview推荐使用的窗口
//		mSettings.setUseWideViewPort(true);
		/**
		LayoutAlgorithm是一个枚举用来控制页面的布局，有三个类型：

		1.NARROW_COLUMNS：可能的话使所有列的宽度不超过屏幕宽度

		2.NORMAL：正常显示不做任何渲染

		3.SINGLE_COLUMN：把所有内容放大webview等宽的一列中

		用SINGLE_COLUMN类型可以设置页面居中显示，页面可以放大缩小，
		但这种方法不怎么好，有时候会让你的页面布局走样而且我测了一下，
		只能显示中间那一块，超出屏幕的部分都不能显示。结合mSettings.setLoadWithOverviewMode(true);
		和mSettings.setUseWideViewPort(true);
		*/
//		mSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		// mSettings.setSavePassword(false);
		// mSettings.setSupportZoom(true); // 设置此属性，仅支持双击缩放，不支持触摸缩放（android4.0）
//		mSettings.setBuiltInZoomControls(true);
//		mSettings.setDisplayZoomControls(false);
		mSettings.setDefaultTextEncodingName("UTF-8");
		mSettings.setAllowFileAccess(true);

//		mSettings.setPluginsEnabled(true); // 5.0以上已经去除这个方法
		mSettings.setPluginState(PluginState.ON);
		mSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		mWebView.setWebViewClient(mWebViewClient);
		mWebView.setWebChromeClient(mWebChromeClient);
		mWebView.clearView();
		// mWebView.setInitialScale(100);
		HcLog.D(TAG + " scale = " + mWebView.getScale());
		mTopBarView.setReturnViewListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				onReturn();
				HcLog.D(TAG + " #initViews it is can go back = "+mWebView.canGoBack());
				if (mWebView.canGoBack()) {
					mWebView.goBack();
				} else {
					onReturn();
				}
			}
		});

	}

	private void initData() {
		mTopBarView.setTitle(mName);
		if (mTopBarLeftIcon > 0) {
			mTopBarView.setBackgroundResource(mTopBarLeftIcon);
		}

		if (HcUtil.CHANDED) {
			mUrl = HcUtil.mappedUrl(mUrl);
		}
		/**
		 * @author jrjin
		 * @date 2016-1-7 上午11:42:16
		HcLog.D(TAG + " cookie = "+SettingHelper.getSessionId(this));
		mCookieManager.setCookie(mUrl, SettingHelper.getSessionId(this));
		CookieSyncManager.getInstance().sync();
		*/
//		mUrl = "http://10.80.7.153:8080/terminalServer/h5e/showPage/xcap/index?hc_account=1645";
		
		/**
		 * @date 2016-1-7 上午11:42:28
		 */
		if (mUrl.contains("?")) {
			mUrl = mUrl + "&hc_account=" + SettingHelper.getAccount(this);
		} else {
			mUrl = mUrl + "?hc_account=" + SettingHelper.getAccount(this);
		}
		mHeaders.put("account", SettingHelper.getAccount(this));
		mHeaders.put("clientId", HcConfig.getConfig().getClientId());
		String token = SettingHelper.getToken(this);
		token = TextUtils.isEmpty(token) ? "-1" : token;
		mHeaders.put("token", token);
		mHeaders.put("terminalId", HcUtil.getIMEI(this));
		mHeaders.put("userId", SettingHelper.getUserId(this));
		mWebView.loadUrl(mUrl, mHeaders);
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
		if (!TextUtils.isEmpty(mBaseUrl)) {
			Intent intent = new Intent();
			intent.putExtra("lasturl", mBaseUrl);
			setResult(RESULT_OK, intent);
		}
		HcAppState.getInstance().removeActivity(this);
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
			onDestory = false;
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
			if (onDestory)
				return;			
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			HcLog.D(TAG + " onPageFinished!");
			if (onDestory)
				return;
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
		private WebChromeClient.CustomViewCallback myCallback = null;

//		private ValueCallback<Uri> mUploadMessage;
//
//		private ValueCallback<Uri[]> mUploadCallbackAboveFive;


		/*
		 * // 配置权限 （在WebChromeClinet中实现）
		 * 
		 * @Override public void onGeolocationPermissionsShowPrompt(String
		 * origin, GeolocationPermissions.Callback callback) {
		 * callback.invoke(origin, true, false);
		 * super.onGeolocationPermissionsShowPrompt(origin, callback); }
		 * 
		 * // 扩充数据库的容量（在WebChromeClinet中实现）
		 * 
		 * @Override public void onExceededDatabaseQuota(String url, String
		 * databaseIdentifier, long currentQuota, long estimatedSize, long
		 * totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
		 * 
		 * quotaUpdater.updateQuota(estimatedSize * 2); }
		 * 
		 * // 扩充缓存的容量
		 * 
		 * @Override public void onReachedMaxAppCacheSize(long spaceNeeded, long
		 * totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
		 * 
		 * quotaUpdater.updateQuota(spaceNeeded * 2); }
		 */

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
			HcLog.D(TAG + " #onJsAlert url = " + url + " message = " + message + " JsResult= "+result);
//			return super.onJsAlert(view, url, message, result);
			/**
			try {
				JSONObject object = new JSONObject(message);
				int code = object.getInt("code");
				String account  = object.getString("hc_account");
				String msg = object.getString("msg");
				if (!TextUtils.isEmpty(account) &&
						account.equals(SettingHelper.getAccount(HtmlActivity.this))) {
					switch (code) {
					case 100:
						HcDialog.showProgressDialog(HtmlActivity.this, msg);
						break;
					case 200:
						mHandler.sendEmptyMessageDelayed(1002, 1200);
						HcDialog.showSuccessDialog(HtmlActivity.this, msg);
						break;
					case 400:
					case 500:
						HcDialog.deleteProgressDialog();
						HcUtil.showToast(HtmlActivity.this, msg);
						break;

					default:
						HcDialog.deleteProgressDialog();
						break;
					}
				}
				
				
			} catch (Exception e) {
				// TODO: handle exception
			}
			 */
			try {
				JSONObject object = new JSONObject(message);
				int code = object.getInt("code");
				String msg = object.getString("msg");
				switch (code) {
					case HcHttpRequest.REQUEST_ACCOUT_EXCLUDED:
					case HcHttpRequest.REQUEST_TOKEN_FAILED:
					case 103:
						if (HcUtil.hasValue(object, "body")) {
							HcUtil.reLogining(object.getJSONObject("body").toString(), HtmlActivity.this, msg);
						}
						break;
					case 100:
						HcDialog.showProgressDialog(HtmlActivity.this, msg);
						break;
					case 200:
						mHandler.sendEmptyMessageDelayed(1002, 1200);
						HcDialog.showSuccessDialog(HtmlActivity.this, msg);
						break;
					case 400:
					case 500:
						HcDialog.deleteProgressDialog();
						HcUtil.showToast(HtmlActivity.this, msg);
						break;
					case 600:
						HcDialog.deleteProgressDialog();
						break;
					case 5555:
						Intent intent = new Intent();
						intent.setClassName(HtmlActivity.this, "com.android.hcframe.sign.DownloadSignPDFActivity");
						intent.putExtra("message", message);
						startActivityForResult(intent, 99);
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

			// super.onProgressChanged(view, newProgress);
		}

		@Override
		public void onReceivedTitle(WebView view, String title) {
			super.onReceivedTitle(view, title);
			HcLog.D(TAG + " #onReceivedTitle titile = "+title);
//			if (mTopBarView != null) {
//				mTopBarView.setTitle(title);
//			}
		}

//		// For Android < 3.0
//		@Override
//		public void openFileChooser(ValueCallback<Uri> uploadMsg) {
//			openFileChooser(uploadMsg, "", "");
//			HcLog.D(TAG + " #openFileChooser!!!!!!!!!!!!!!!!<3.0 uploadMsg = "+uploadMsg);
//		}
//
//		// For Android 3.0+
//		@Override
//		public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
//			openFileChooser(uploadMsg, acceptType, "");
//			HcLog.D(TAG + " #openFileChooser!!!!!!!!!!!!!!!!>3.0 uploadMsg = " + uploadMsg + " acceptType="+acceptType);
//		}
//
//		//For Android 4.1
//		@Override
//		public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
//			HcLog.D(TAG + " #openFileChooser!!!!!!!!!!!!!!!!>4.1 uploadMsg = " + uploadFile + " acceptType="+acceptType + " capture="+capture);
//			mUploadMessage = uploadFile;
//			Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//			i.addCategory(Intent.CATEGORY_OPENABLE);
////			i.setType("image/*");
//			i.setType("*/*");
//			startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
//
//		}
//
//		/**
//		 * 兼容5.0及以上
//		 *
//		 * @param webView
//		 * @param valueCallback
//		 * @param fileChooserParams
//		 * @return
//		 */
//		@Override
//		public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> valueCallback, android.webkit.WebChromeClient.FileChooserParams fileChooserParams) {
//			HcLog.D(TAG + " #onShowFileChooser valueCallback ="+valueCallback + " FileChooserParams ="+fileChooserParams);
//			mUploadCallbackAboveFive = valueCallback;
//			Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//			i.addCategory(Intent.CATEGORY_OPENABLE);
//			i.setType("*/*");
//			startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
//			return true;
//		}
//
//
//		@Override
//		public void onActivityResult(int resultCode, Intent data) {
//			if (Build.VERSION.SDK_INT < 21) { // sdk < 5.0
//				if (null == mUploadMessage) {
//					return;
//				}
//				Uri result = data == null || resultCode != Activity.RESULT_OK ? null
//						: data.getData();
//				if (mUploadMessage != null) {
//					mUploadMessage.onReceiveValue(result);
//					mUploadMessage = null;
//				}
//			} else {
//				if (null == mUploadCallbackAboveFive) {
//					return;
//				}
//				Uri[] results = null;
//				if (resultCode == Activity.RESULT_OK) {
//					if (data != null) {
//						String dataString = data.getDataString();
//						ClipData clipData = data.getClipData();
//						if (clipData != null) {
//							int itemCount = clipData.getItemCount();
//							results = new Uri[itemCount];
//							for (int i = 0; i < itemCount; i++) {
//								ClipData.Item item = clipData.getItemAt(i);
//								results[i] = item.getUri();
//							}
//						}
//						if (dataString != null) {
//							results = new Uri[]{Uri.parse(dataString)};
//						}
//					}
//				}
//				mUploadCallbackAboveFive.onReceiveValue(results);
//				mUploadCallbackAboveFive = null;
//				return;
//			}
//
//		}

	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		HcLog.D(TAG + " it is onDestory start!");
		HcAppState.getInstance().removeActivity(this);
		onDestory = true;
		if (mWebView != null) {
			mWebView.stopLoading();
			mWebView = null;
		}
		super.onDestroy();
		HcLog.D(TAG + " it is onDestory end!");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		HcLog.D(TAG + " #onActivityResult requestCode = "+requestCode + " resultCode = "+resultCode + " data ="+data);
		switch (requestCode) {
			case BaseWebChromeClient.FILECHOOSER_RESULTCODE:
				if (mWebChromeClient != null) {
					((BaseWebChromeClient) mWebChromeClient).onActivityResult(resultCode, data);
				}
				break;
			case 99:
				boolean isSign = data.getBooleanExtra("isSign", false);
				if (isSign) {
					mWebView.loadUrl("javascript: callBack('已签批')");
				} else {
					mWebView.loadUrl("javascript: callBack('无签批')");
				}
				break;
			default:
				break;
		}
	}
}
