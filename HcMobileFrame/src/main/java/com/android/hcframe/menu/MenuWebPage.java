/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-5-8 下午3:29:00
*/
package com.android.hcframe.menu;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import org.json.JSONObject;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.PluginState;
import android.widget.ProgressBar;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.BaseWebChromeClient;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.monitor.LogManager;
import com.android.hcframe.push.HcAppState;
import com.android.hcframe.push.HcPushManager;
import com.android.hcframe.push.PushInfo;
import com.android.hcframe.sql.SettingHelper;

public class MenuWebPage extends AbstractPage {

	private static final String TAG = "MenuWebPage";
	
	private WebView mWebView;
	private WebSettings mSettings; 
	/**
	 * 需要跳转的URL
	 */
	private String mUrl;

	private MenuBaseActivity mActivity;

	private ProgressBar mProgressBar;
	/** webview是否主停止加载 */
	private boolean onDestory = false;
	
	private CookieManager mCookieManager;
	
	private final String mAppId;

	/** 请求的头部 */
	private Map<String, String> mHeaders = new HashMap<String, String>();

	/** 用户点击标题栏的返回按钮 */
	public static final int ON_RETURN_CODE = 1;
	/** 用户点击系统的返回按钮 */
	public static final int ON_KEY_DOWN_CODE = 2;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 1003:
				HcDialog.deleteProgressDialog();
				break;

			default:
				break;
			}
		}
		
	};

	/** 最后加载的Url */
	private String mLastUrl;

	private boolean mReLoginSuccess;

	public MenuWebPage(Activity context, ViewGroup group, String appId) {
		super(context, group);
		// TODO Auto-generated constructor stub
		mAppId = appId;
		mActivity = (MenuBaseActivity) context;
		/**
		 * @date 2016-1-7 上午11:45:11
		CookieSyncManager.createInstance(mContext);
		mCookieManager = CookieManager.getInstance();
		mCookieManager.setAcceptCookie(true);
		mCookieManager.removeSessionCookie();
		*/
	}

	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialized() {
		// TODO Auto-generated method stub
		HcLog.D(TAG + " initialized webview = " + mWebView);
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
//		HcLog.D(TAG + " #onResume mPreUrl = "+mPreUrl);

		// 增加消息推送的功能
		PushInfo info = HcPushManager.getInstance().getPushInfo();
		if (info != null) {
			HcPushManager.getInstance().setPushInfo(null);
			if (mAppId.equals(info.getAppId()) && !mUrl.equals(info.getContent())) {
				Intent intent = new Intent();
				intent.setClass(mContext, HtmlActivity.class);
				intent.putExtra("url", info.getContent());
				intent.putExtra("title", info.getAppName());
				mContext.startActivity(intent);
				mContext.overridePendingTransition(0, 0);
			}
		}

		if (HcUtil.CHANDED) {
			mUrl = HcUtil.mappedUrl(mUrl);
		}
		if (isFirst) {
			isFirst = !isFirst;
			
			/**
			 * @date 2016-1-7 上午11:44:50
			HcLog.D(TAG + " cookie = "+SettingHelper.getSessionId(mContext));
			mCookieManager.setCookie(mUrl, SettingHelper.getSessionId(mContext));
			CookieSyncManager.getInstance().sync();
			*/
			if (mUrl.contains("?")) {
				mUrl = mUrl + "&hc_account=" + SettingHelper.getAccount(mContext);
			} else {
				mUrl = mUrl + "?hc_account=" + SettingHelper.getAccount(mContext);
			}
			String account = SettingHelper.getAccount(mContext);
			String clientId = HcConfig.getConfig().getClientId();
			String terminalId = HcUtil.getIMEI(mContext);
			String userId = SettingHelper.getUserId(mContext);
			mHeaders.put("account", account);
			mHeaders.put("clientId", clientId);
			String token = SettingHelper.getToken(mContext);
			token = TextUtils.isEmpty(token) ? "-1" : token;
			mHeaders.put("token", token);
			mHeaders.put("terminalId", terminalId);
			mHeaders.put("userId", userId);
			HcLog.D(TAG + " #onResume accout = "+account + " clientId = "+clientId + " token = "+token
				+ " IMEI = "+terminalId + " userId = "+userId);
			mWebView.loadUrl(mUrl, mHeaders);
		} else {
			if (mReLoginSuccess) {
				mReLoginSuccess = !mReLoginSuccess;
				String account = SettingHelper.getAccount(mContext);
				String clientId = HcConfig.getConfig().getClientId();
				String terminalId = HcUtil.getIMEI(mContext);
				String userId = SettingHelper.getUserId(mContext);
				mHeaders.put("account", account);
				mHeaders.put("clientId", clientId);
				String token = SettingHelper.getToken(mContext);
				token = TextUtils.isEmpty(token) ? "-1" : token;
				mHeaders.put("token", token);
				mHeaders.put("terminalId", terminalId);
				mHeaders.put("userId", userId);
				HcLog.D(TAG + " #onResume accout = "+account + " clientId = "+clientId + " token = "+token
						+ " IMEI = "+terminalId + " userId = "+userId);
				String url;
				if (TextUtils.isEmpty(mLastUrl)) {
					url = mUrl;
				} else {
					url = mLastUrl;
				}
				if (url.contains("?")) {
					url = url + "&hc_account=" + SettingHelper.getAccount(mContext);
				} else {
					url = url + "?hc_account=" + SettingHelper.getAccount(mContext);
				}
				mWebView.loadUrl(url, mHeaders);
				mLastUrl = null;
			}
		}
	}

	@Override
	public void setContentView() {
		// TODO Auto-generated method stub
		if (mView == null) {
			mView = mInflater.inflate(R.layout.menu_web_layout, null);
			mWebView = (WebView) mView.findViewById(R.id.menu_webview);
			mProgressBar = (ProgressBar) mView.findViewById(R.id.menu_webview_progress);
			
			mSettings = mWebView.getSettings();
			mSettings.setJavaScriptEnabled(true);
			mSettings.setJavaScriptCanOpenWindowsAutomatically(true);
			mSettings.setSaveFormData(false);
			mSettings.setSavePassword(false);
//			mSettings.setSupportZoom(false);
			mSettings.setDefaultTextEncodingName("UTF-8");
			
			mSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
			
//			mSettings.setBuiltInZoomControls(true);
//			mSettings.setDisplayZoomControls(false);
//			mSettings.setUseWideViewPort(true);
//			mSettings.setLoadWithOverviewMode(true);
//			mSettings.setPluginsEnabled(true); // 5.0以上已经去除这个方法
			mSettings.setPluginState(PluginState.ON);
			
			mWebView.setWebViewClient(mWebViewClient);
			mWebView.setWebChromeClient(mWebChromeClient);
		    mWebView.clearView();
		}
	}
	
	private WebViewClient mWebViewClient = new WebViewClient() {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// TODO Auto-generated method stub
			HcLog.D(TAG + " #shouldOverrideUrlLoading url = "+url + " load time = "+HcUtil.getDate(HcUtil.FORMAT_POLLUTION, System.currentTimeMillis()));

			onDestory = false;

			boolean override = super.shouldOverrideUrlLoading(view, url);
			HcLog.D(TAG + " #shouldOverrideUrlLoading should override = "
					+ override);
			if (override) {
				mLastUrl = url;
			}
			return override;
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// TODO Auto-generated method stub
			super.onPageStarted(view, url, favicon);
			HcLog.D(TAG + " onPageStarted url = " + url + " start time = " + HcUtil.getDate(HcUtil.FORMAT_POLLUTION, System.currentTimeMillis()));
			if (onDestory) return;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			// TODO Auto-generated method stub
			super.onPageFinished(view, url);
			HcLog.D(TAG + " onPageFinished url = "+url + " end time = "+HcUtil.getDate(HcUtil.FORMAT_POLLUTION, System.currentTimeMillis()));
			if (onDestory) return;
			
		}
		
		
		
	};


	private WebChromeClient mWebChromeClient = new HcWebChromeClient(mContext) {


		@Override
		public boolean onJsAlert(WebView view, String url, String message,
				JsResult result) {
			// TODO Auto-generated method stub
			HcLog.D(TAG + " #onJsAlert url = "+url + " message = "+message);
//			return super.onJsAlert(view, url, message, result);
			/**
			try {
				JSONObject object = new JSONObject(message);
				int code = object.getInt("code");
				String account  = object.getString("hc_account");
				String msg = object.getString("msg");
				if (!TextUtils.isEmpty(account) && 
						account.equals(SettingHelper.getAccount(mContext))) {
					switch (code) {
					case 100:
						HcDialog.showProgressDialog(mContext, msg);
						break;
					case 200:
						mHandler.sendEmptyMessageDelayed(1003, 1200);
						HcDialog.showSuccessDialog(mContext, msg);
						break;
					case 400:
					case 500:
						HcDialog.deleteProgressDialog();
						HcUtil.showToast(mContext, msg);
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
							HcUtil.reLogining(object.getJSONObject("body").toString(), mContext, msg);
						}
				        break;
					case 100:
						HcDialog.showProgressDialog(mContext, msg);
						break;
					case 200:
						mHandler.sendEmptyMessageDelayed(1003, 1200);
						HcDialog.showSuccessDialog(mContext, msg);
						break;
					case 400:
					case 500:
						HcDialog.deleteProgressDialog();
						HcUtil.showToast(mContext, msg);
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
			HcLog.D(TAG + " #onProgressChanged newProgress = "+newProgress);
			if (newProgress < 0 || newProgress == 100) {
				mProgressBar.setVisibility(View.GONE);
			} else {
				if (mProgressBar.getVisibility() != View.VISIBLE)
					mProgressBar.setVisibility(View.VISIBLE);
				mProgressBar.setProgress(newProgress);
			}
//			super.onProgressChanged(view, newProgress);
		}

		@Override
		public void onReceivedTitle(WebView view, String title) {
			super.onReceivedTitle(view, title);
			HcLog.D(TAG + " #onReceivedTitle titile = "+title);
			if (mActivity != null) {
				mActivity.setTitle(title);
			}
		}

		@Override
		public void onCloseWindow(WebView window) {
			super.onCloseWindow(window);
			HcLog.D(TAG + " #onCloseWindow!!!!!!!!!!!!!!!! window ="+window);
		}

		@Override
		public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
			HcLog.D(TAG + " #onCreateWindow isDialog =" + isDialog + " isUserGesture =" + isUserGesture + " resultMsg=" + resultMsg);
			return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);

		}

		@Override
		public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
			HcLog.D(TAG + " #onReceivedTitle onJsConfirm url =" + url + " result ="+result);
			return super.onJsConfirm(view, url, message, result);

		}

		@Override
		public void onShowCustomView(View view, CustomViewCallback callback) {
			super.onShowCustomView(view, callback);
			HcLog.D(TAG + " #onShowCustomView callback = " + callback);
		}

		@Override
		public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
			super.onShowCustomView(view, requestedOrientation, callback);
			HcLog.D(TAG + " #onShowCustomView requestedOrientation = " + requestedOrientation + " callback=" + callback);
		}


	};

	@Override
	public void setParameters() {
		// TODO Auto-generated method stub
		mUrl = mActivity.getMenuUrl();
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		mWebView = null;
		mProgressBar = null;
		mView = null;
		mActivity = null;
	}

	@Override
	public void onDestory() {
		// TODO Auto-generated method stub
		onDestory = true;
		if (mWebView != null) {
			mWebView.setWebChromeClient(null);
			mWebView.stopLoading();
		}

		super.onDestory();
		mContext = null;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		HcLog.D(TAG + " #onActivityResult requestCode = "+requestCode + " webview = "+mWebView + " activity = "+mActivity);
		switch (requestCode) {
		    case ON_KEY_DOWN_CODE:
				if (mWebView != null && mWebView.canGoBack()) {
					mWebView.goBack();
				} else {
					HcAppState.getInstance().removeActivity(mActivity);
					LogManager.getInstance().updateLog(mActivity, false);
					mActivity.finish();
				}
		        break;
			case ON_RETURN_CODE:
				if (mWebView != null && mWebView.canGoBack()) {
					mWebView.goBack();
				} else {
					HcAppState.getInstance().removeActivity(mActivity);
					LogManager.getInstance().updateLog(mActivity, false);
					mActivity.finish();
				}
				break;
			case BaseWebChromeClient.FILECHOOSER_RESULTCODE:
				if (mWebChromeClient != null) {
					((BaseWebChromeClient) mWebChromeClient).onActivityResult(resultCode, data);
				}
				break;
			case HcUtil.REQUEST_CODE_LOGIN:
				if (resultCode == HcUtil.LOGIN_SUCCESS) { // 登录失败在MenuBaseActivity#onActivityResult里面处理
					mReLoginSuccess = true;
				}
				break;

		    default:
		        break;
		}
	}
}
