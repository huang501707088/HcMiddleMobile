/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2013-11-26 下午1:21:43
 */
package com.android.hcframe.http;

import java.io.InputStream;
//import java.util.List;
//
//import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
//import org.apache.http.client.protocol.ClientContext;
//import org.apache.http.cookie.Cookie;
//import org.apache.http.impl.client.BasicCookieStore;
//import org.apache.http.protocol.BasicHttpContext;
//import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.monitor.LogManager;
import com.android.hcframe.monitor.OperationLogInfo;
import com.android.hcframe.sql.SettingHelper;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

public abstract class AbstractHttp implements Runnable {

	private static final String TAG = "AbstractHttp";
	private HttpClient mClient;
	private Handler mHandler;
	private HttpResponse mHttpResponse = null;
	private HttpUriRequest mRequest;

	protected RequestCategory mCategory = RequestCategory.NONE;

	protected IHttpResponse mResponse = new NoneResponse();

	/** 请求的次数 默认为3次就报超时 */
	protected int mRequestCount = 1;
	/** 请求Url的MD5格式 */
	protected String mUrl;

	private CookieStore mCookieStore;
	
	protected boolean mShowToast = true;

	/**
	 * 用户是否主动取消请求
	 */
	private boolean mShutdown;

	public AbstractHttp(AbstractHcHttpClient client, HttpUriRequest request,
			Handler handler) {
		mClient = client.getClient();
		mHandler = handler;
		mRequest = request;
		mRequest.setHeader("account",
				SettingHelper.getAccount(HcApplication.getContext()));
		String token = SettingHelper.getToken(HcApplication.getContext());
		token = TextUtils.isEmpty(token) ? "-1" :
				("vcheck".equals(getRequestMethod()) || "registerchannelid".equals(getRequestMethod()) ? "-1" :
				token);
		mRequest.setHeader("token", token);
		mRequest.setHeader("terminalId",
				HcUtil.getIMEI(HcApplication.getContext()));
		mRequest.setHeader("clientId", HcConfig.getConfig().getClientId());
		mRequest.setHeader("userId", SettingHelper.getUserId(HcApplication.getContext()));
	}

	@Override
	public final void run() {
		// TODO Auto-generated method stub
		HcLog.D(TAG + "# it is in run! this = " + this);
		try {
			HcLog.D(TAG + "# it is in run! before execute time = "+HcUtil.getDate(HcUtil.FORMAT_POLLUTION, System.currentTimeMillis()));
			// if (mCategory == RequestCategory.LOGIN) {
			// HttpContext context = new BasicHttpContext();
			// mCookieStore = new BasicCookieStore();
			// context.setAttribute(ClientContext.COOKIE_STORE, mCookieStore);
			// mHttpResponse = mClient.execute(mRequest, context);
			// } else {
			// mHttpResponse = mClient.execute(mRequest);
			// }
			
			/**
			 * @author jrjin
			 * @date 2016-2-23 下午3:09:39
			 * 初始化日志
			 */
			initLog();
			
			mHttpResponse = mClient.execute(mRequest);
			int status = mHttpResponse.getStatusLine().getStatusCode();
			HcLog.D(TAG + "# it is in run! after execute time = " + HcUtil.getDate(HcUtil.FORMAT_POLLUTION, System.currentTimeMillis()) + " status = "+status);
			if (status == HttpStatus.SC_OK) {
				
				/**
				 * @author jrjin
				 * @date 2016-2-23 下午3:10:08
				 * 更新日志
				 */
				updateLog(true);
				/**
				if (mCategory == RequestCategory.LOGIN) {
					// if (mCookieStore != null) {
					// List<Cookie> cookies = mCookieStore.getCookies();
					// for (Cookie cookie : cookies) {
					// HcLog.D(TAG + " cookie name = "+cookie.getName() +
					// " value = "+cookie.getValue());
					// if ("JSESSIONID".equals(cookie.getName())) {
					// SettingHelper.setSessionId(HcApplication.getContext(),
					// cookie.getValue());
					// }
					// }
					// }
					Header[] headers = mHttpResponse.getAllHeaders();
					for (Header header : headers) {
						HcLog.D(TAG + " header name = " + header.getName()
								+ " value = " + header.getValue());
						if ("cookie".equals(header.getName())) {
							SettingHelper.setSessionId(
									HcApplication.getContext(),
									header.getValue());
							break;
						}

					}
				}
				 */

				if (mCategory == RequestCategory.UPDATE_DOWNLOAD
						|| mCategory == RequestCategory.DOWNLOAD_IMAGE
						|| mCategory == RequestCategory.DOWNLOAD_APP
						|| mCategory == RequestCategory.DOWNLOAD_PDF
						|| mCategory == RequestCategory.DOWNLOAD_GIF) {
					InputStream stream = mHttpResponse.getEntity().getContent();
					parseInputStream(stream);
				} else {
					String reStr = EntityUtils.toString(
							mHttpResponse.getEntity(), "UTF-8");
					HcLog.D(" length = " + reStr.length());
					parseJson(reStr);
				}

			} else {
				boolean abort = mRequest.isAborted();
				HcLog.D(TAG + " status code = " + status + " is abort ======= " +abort);
				if (!abort) {
					mRequest.abort();
				}
				sendMessage(status);
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			HcLog.D(TAG + "#ClientProtocolException error = " + e + " time = "+HcUtil.getDate(HcUtil.FORMAT_POLLUTION, System.currentTimeMillis()));
			printError(e);
			sendMessage(402);
		} catch (ParseException e) {
			// TODO: h andle exception
			HcLog.D(TAG + "#ParseException error =" + e + " time = "+HcUtil.getDate(HcUtil.FORMAT_POLLUTION, System.currentTimeMillis()));
			printError(e);
			sendMessage(402);		
		} catch (Exception e) {
			// TODO: handle exception
			HcLog.D(TAG + "other Exception error = " + e + " time = "+HcUtil.getDate(HcUtil.FORMAT_POLLUTION, System.currentTimeMillis()));
			printError(e);
			sendMessage(402);
		}
	}

	private void sendMessage(int what) {
		/**
		 * @author jrjin
		 * @date 2016-2-23 下午3:09:00
		 */
		updateLog(false);
		
		Message msg = mHandler.obtainMessage(what);
		msg.obj = this;
		mHandler.sendMessage(msg);
	}

	/**
	 * 解析数据
	 * 
	 * @author jrjin
	 * @time 2015-10-19 下午4:16:19
	 * @param data
	 */
	public abstract void parseJson(String data);

	public void parseInputStream(InputStream stream) {
	}

	private class NoneResponse implements IHttpResponse {

		@Override
		public void notify(Object data, RequestCategory request,
				ResponseCategory category) {
			// TODO Auto-generated method stub

		}

		@Override
		public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
			// TODO Auto-generated method stub

		}

	}

	/**
	 * 用户主动取消请求或者退出当前页面
	 * 
	 * @author jrjin
	 * @time 2015-9-24 下午2:57:12
	 */
	public synchronized void cancelRequest() {
		mResponse = null;
		mResponse = new NoneResponse();
		mShutdown = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				mRequest.abort();
			}
		}).start();
	}
	
	private void printError(Exception ex) {
		StringBuilder builder = new StringBuilder("<<<<< start printError!>>>>>");
		builder.append("\n");
		StackTraceElement[] elements = ex.getStackTrace();
		for (StackTraceElement stackTraceElement : elements) {
			builder.append(stackTraceElement.toString());
			builder.append("\n");
		}
		Throwable cause = ex.getCause();
		if (cause != null) {
			elements = cause.getStackTrace();
			for (StackTraceElement stackTraceElement : elements) {
				builder.append("causeBy:" + stackTraceElement.toString());
				builder.append("\n");
			}
		}
		HcLog.D(TAG + " <<<<< end printError!>>>>> time = "+HcUtil.getDate(HcUtil.FORMAT_POLLUTION, System.currentTimeMillis()));
	}
	
	/**
	 * 获取请求的根Url
	 * @author jrjin
	 * @time 2015-12-16 下午11:43:47
	 * @return
	 */
	private String getBaseUrl() {
		return "/terminalServer/szf/";
	}
	/**
	 * 获取请求的方法,该方法需要重载
	 * @author jrjin
	 * @time 2015-12-16 下午11:44:22
	 * @return
	 */
	public String getRequestMethod() {
		return "";
	}
	
	/**
	 * 接口请求日志
	 */
	private OperationLogInfo mInfo;
	
	private void initLog() {
		mInfo = new OperationLogInfo();
		mInfo.setImei(HcUtil.getIMEI(HcApplication.getContext()));
		mInfo.setName(getRequestMethod());
		mInfo.setStartTime("" + System.currentTimeMillis());
//		mInfo.setStartTime(HcUtil.getDate(HcUtil.FORMAT_LOG_DATE, System.currentTimeMillis()));
		mInfo.setType(LogManager.TYPE_SERVER);
		mInfo.setVersion(HcConfig.getConfig().getAppVersion());
		mInfo.setModuleId("");
	}
	
	/**
	 * 更新日志,线程不安全
	 * @author jrjin
	 * @time 2016-2-23 下午3:15:29
	 * @param success
	 */
	@SuppressWarnings("线程不安全,需要测试")
	public final void updateLog(boolean success) {
		if (mInfo != null) {
			mInfo.setEndTime("" + System.currentTimeMillis());
//			mInfo.setEndTime(HcUtil.getDate(HcUtil.FORMAT_LOG_DATE, System.currentTimeMillis()));
			mInfo.setResult(success == true ? 0 : 1);
			LogManager manager = LogManager.getInstance();
			HcLog.D(TAG + " #updateLog====================manager = "+manager + " http = "+this);
			LogManager.getInstance().addServerLog(mInfo);
			mInfo = null;
		}
	}

	public boolean shutDown() {
		return mResponse == null || mResponse instanceof NoneResponse || mShutdown;
	}
}
