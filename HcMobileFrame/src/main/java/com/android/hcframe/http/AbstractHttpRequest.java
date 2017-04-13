/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-16 下午11:59:27
*/
package com.android.hcframe.http;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.os.Handler;
import android.text.TextUtils;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.monitor.LogManager;
import com.android.hcframe.monitor.OperationLogInfo;
import com.android.hcframe.sql.SettingHelper;

/**
 * 
 * @author jrjin
 * @time 2015-12-17 上午12:01:57
 */
public abstract class AbstractHttpRequest implements Runnable {

	private static final String TAG = "AbstractHttpRequest";
	private final HttpClient mClient;
	/**
	 * 返回请求的消息处理
	 */
	private final Handler mHandler;
	private HttpResponse mHttpResponse = null;
	private HttpUriRequest mRequest;

	protected RequestCategory mCategory = RequestCategory.NONE;

	protected AbstractHttpResponse mResponse = new NoneResponse();

	/** 请求的次数 默认为3次就报超时 */
	protected int mRequestCount = 1;
	/** 请求Url的MD5格式 */
	protected String mUrl;
	
	private final AbstractHcHttpClient mExecutorService;
	
	private final String mRequestType;
	/** 最大请求次数 */
	private static final int REQUEST_MAX_COUNT = 3;
	/** 请求次数 */
	private final int mRequestMaxCount;

	/** 是否需要读取数据流 */
	private boolean mReadStream = false;

	private Future<?> mFuture;

	private boolean mShutdown;
	
	public AbstractHttpRequest() {
		this(REQUEST_MAX_COUNT, null);
	}
	
	/**
	 * 
	 * @param requestMaxCount 去服务端请求的次数，默认为3次
	 */
	public AbstractHttpRequest(int requestMaxCount) {
		this(requestMaxCount, null);
	}
	
	/**
	 * 
	 * @param requestType {@link HttpRequestQueue#HTTP_REQUEST_GET} and {@link HttpRequestQueue#HTTP_REQUEST_POST}
	 */
	public AbstractHttpRequest(String requestType) {
		this(REQUEST_MAX_COUNT, requestType);
	}
	
	/**
	 * 
	 * @param handler 主线程的消息通知队列
	 */
	public AbstractHttpRequest(Handler handler) {
		this(handler, null);
	}
	
	/**
	 * 
	 * @param handler 主线程的消息通知队列
	 * @param requestType {@link HttpRequestQueue#HTTP_REQUEST_GET} and {@link HttpRequestQueue#HTTP_REQUEST_POST}
	 */
	public AbstractHttpRequest(Handler handler, String requestType) {
		this(null, requestType, handler, 0);
	}
	
	/**
	 * 
	 * @param requestMaxCount 去服务端请求的次数，默认为3次
	 * @param requestType {@link HttpRequestQueue#HTTP_REQUEST_GET} and {@link HttpRequestQueue#HTTP_REQUEST_POST}
	 */
	public AbstractHttpRequest(int requestMaxCount, String requestType) {
		this(null, requestType, null, requestMaxCount);
	}

	/**
	 * 
	 * @param client HttpClient
	 * @param handler Handler
	 * @param requestType {@link HttpRequestQueue#HTTP_REQUEST_GET} and {@link HttpRequestQueue#HTTP_REQUEST_POST}
	 */
	public AbstractHttpRequest(AbstractHcHttpClient client, Handler handler, String requestType) {
		this(client, requestType, handler, REQUEST_MAX_COUNT);
	}

	/**
	 * 
	 * @param client HttpClient
	 * @param requestType {@link HttpRequestQueue#HTTP_REQUEST_GET} and {@link HttpRequestQueue#HTTP_REQUEST_POST}
	 * @param handler 主线程的消息通知队列
	 * @param requestMaxCount 去服务端请求的次数，默认为3次
	 */
	public AbstractHttpRequest(AbstractHcHttpClient client, String requestType,
			Handler handler, int requestMaxCount) {
		if (client == null) {
			client = HttpRequestQueue.getInstance().getHttpClient();
		}
		mExecutorService = client;
		mClient = client.getClient();
		if (handler == null) {
			handler = HttpRequestQueue.getInstance().getHttpHandler();
		}
		mHandler = handler;
		if (TextUtils.isEmpty(requestType)) {
			mRequestType = HttpRequestQueue.HTTP_REQUEST_GET;
		} else {
			mRequestType = requestType;
		}
		
		if (requestMaxCount < 1 || requestMaxCount > 3) {
			requestMaxCount = REQUEST_MAX_COUNT;
		}
		mRequestMaxCount = requestMaxCount;
		
	}

	@Override
	public final void run() {
		// TODO Auto-generated method stub
		HcLog.D(TAG + "# it is in run! this = " + this);
		try {
			HcLog.D(TAG + "# it is in run! before execute time = "+HcUtil.getDate(HcUtil.FORMAT_POLLUTION, System.currentTimeMillis()));

			/**
			 * @author jrjin
			 * @date 2016-2-23 下午3:09:39
			 * 初始化日志
			 */
			initLog();

			mHttpResponse = mClient.execute(mRequest);
			HcLog.D(TAG + "# it is in run! after execute time = " + HcUtil.getDate(HcUtil.FORMAT_POLLUTION, System.currentTimeMillis()));
			int code = mHttpResponse.getStatusLine().getStatusCode();
			HttpRequestQueue.getInstance().cancelTask(this);
			if (code == HttpStatus.SC_OK) {

				/**
				 * @author jrjin
				 * @date 2016-2-23 下午3:10:08
				 * 更新日志
				 */
				updateLog(true);

				if (/*mCategory == RequestCategory.UPDATE_DOWNLOAD
						|| mCategory == RequestCategory.DOWNLOAD_IMAGE
						|| mCategory == RequestCategory.DOWNLOAD_APP
						|| mCategory == RequestCategory.DOWNLOAD_PDF
						|| mCategory == RequestCategory.DOWNLOAD_GIF*/
						mReadStream) {
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
				HcLog.D(TAG + " status code = " + code + " is abort ===== "+abort);
				if (!abort) {
					mRequest.abort();
				}
//				HcLog.D(TAG + " status code = " + code + " after abort is abort ===== "+mRequest.isAborted());
				/**
				 * @author jrjin
				 * @date 2016-2-23 下午3:10:08
				 * 更新日志
				 */
				updateLog(false);

				onResponseFailed(code);
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			HcLog.D(TAG + "#ClientProtocolException error = " + e + " time = "+HcUtil.getDate(HcUtil.FORMAT_POLLUTION, System.currentTimeMillis()));
			printError(e);
			sendMessage();
		} catch (ParseException e) {
			// TODO: handle exception
			HcLog.D(TAG + "#ParseException error =" + e + " time = "+HcUtil.getDate(HcUtil.FORMAT_POLLUTION, System.currentTimeMillis()));
			printError(e);
			sendMessage();		
		} catch (Exception e) {
			// TODO: handle exception
			HcLog.D(TAG + "other Exception error = " + e + " time = "+HcUtil.getDate(HcUtil.FORMAT_POLLUTION, System.currentTimeMillis()));
			printError(e);
			sendMessage();
		}
	}

	private void sendMessage() {
		/**
		 * @author jrjin
		 * @date 2016-2-23 下午3:10:08
		 * 更新日志
		 */
		updateLog(false);

		if (mHandler != null) {
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (mResponse == null || mResponse instanceof NoneResponse || mShutdown) return;
					if (mRequestCount < mRequestMaxCount) {
						mRequestCount = mRequestCount + 1;
						if (mFuture != null) {
							mFuture = null;
							mExecutorService.submit(AbstractHttpRequest.this);
						} else {
							mExecutorService.execute(AbstractHttpRequest.this);
						}

					} else { // 已经超过设定的请求次数了
						HttpRequestQueue.getInstance().cancelTask(AbstractHttpRequest.this);
						onNetworkInterrupt();
					}
				}
			});
			
			
//			Message msg = mHandler.obtainMessage(400);
//			msg.obj = this;
//			mHandler.sendMessage(msg);
		}
	}

	/**
	 * 解析数据,最好不要去重载
	 * 
	 * @author jrjin
	 * @time 2015-10-19 下午4:16:19
	 * @param data
	 */
	public void parseJson(String data) {
		HcLog.D(TAG + " #parseJson data = " + data);
		try {
			JSONObject object = new JSONObject(data);
			int status = object.getInt(HttpRequestQueue.STATUS);
			if (status == HttpRequestQueue.REQUEST_SUCCESS) {
				if (hasValue(object, HttpRequestQueue.BODY)) {
					object = object.getJSONObject(HttpRequestQueue.BODY);
					onSuccess(object.toString());
				} else {
					onSuccess("{}");
				}

			} else if (status == HcHttpRequest.REQUEST_ACCOUT_EXCLUDED ||
					status == HcHttpRequest.REQUEST_TOKEN_FAILED) {
				String msg = "";
				if (hasValue(object, HttpRequestQueue.MSG)) {
					msg = object.getString(HttpRequestQueue.MSG);
				}
				object = object.getJSONObject(HttpRequestQueue.BODY);
				onAccountExcluded(object.toString(), msg);
			} else {
				String msg = "";
				if (hasValue(object, HttpRequestQueue.MSG)) {
					msg = object.getString(HttpRequestQueue.MSG);
				}
				onRequestFailed(status, msg);
			}
		} catch (Exception e) {
			// TODO: handle exception
			HcLog.D(TAG + " #parseJson error e = "+e);
			onParseDataError();
		}
	}

	public void parseInputStream(InputStream stream){
		;
	}

	private class NoneResponse extends AbstractHttpResponse {

		@Override
		public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
			// TODO Auto-generated method stub
			;
		}

		@Override
		public void onSuccess(Object data, RequestCategory request) {
			// TODO Auto-generated method stub
			;
		}

		@Override
		public void onConnectionTimeout(RequestCategory request) {
			;
		}

		@Override
		public void onNetworkInterrupt(RequestCategory request) {
			;
		}

		@Override
		public void onParseDataError(RequestCategory request) {
			;
		}

		@Override
		public void onRequestFailed(int code, String msg, RequestCategory request) {
			;
		}

		@Override
		public void onResponseFailed(int code, RequestCategory request) {
			;
		}

		@Override
		public void unknown(RequestCategory request) {
			;
		}

		@Override
		public String getTag() {
			return "NoneResponse";
		}

		@Override
		public void onAccountExcluded(String data, String msg, RequestCategory request) {

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
		if (mFuture != null) {
			boolean canel = mFuture.cancel(true);
			HcLog.D(TAG + " #cancelRequest canel = "+canel);
			mFuture = null;
		}
		mShutdown = true;
		// 在子线程中中断请求
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
		HcLog.D(TAG + " <<<<< end printError!>>>>> time = " + HcUtil.getDate(HcUtil.FORMAT_POLLUTION, System.currentTimeMillis()));
	}
	
	/**
	 * 获取请求的根Url
	 * @author jrjin
	 * @time 2015-12-16 下午11:43:47
	 * @return
	 */
	private String getBaseUrl() {
		return HcUtil.getScheme() + "/terminalServer/szf/";
	}
	/**
	 * 获取请求的方法,该方法需要重载
	 * @author jrjin
	 * @time 2015-12-16 下午11:44:22
	 * @return
	 */
	public abstract String getRequestMethod();
	
	/**
	 * 一个模版方法,对服务器的请求
	 * @author jrjin
	 * @time 2015-12-17 上午12:06:17
	 * @param request 请求类型
	 * @param response 请求回调
	 * @param readStream 是否需要解析数据流
	 * @see #parseInputStream(InputStream)
	 * @see #parseJson(String)
	 */
	public final void sendRequestCommand(RequestCategory request, AbstractHttpResponse response, boolean readStream) {
		/**
		 * 1.一个完整的Url
		 * 2.
		 */
		String url = getRequestUrl();
//		HcLog.D(TAG + " #sendRequestCommand url = "+url);
		sendRequestCommand(url, request, response, readStream);
//		if (!TextUtils.isEmpty(url)) {
//			String key = HttpRequestQueue.getInstance().hasInTask(url);
//			if (key == null)
//				return;
//			mUrl = key;
//			setRequest(url, mRequestType);
//			setParameters();
//			HttpRequestQueue.getInstance().addTask(this, mRequestMaxCount * 10 * 1000 + 3 * 1000);
//			mExecutorService.execute(this);
//		}
	}

	/**
	 * 一个模版方法,对服务器的请求
	 * @author jrjin
	 * @time 2015-12-17 上午12:06:17
	 * @param request 请求类型
	 * @param response 请求回调
	 * @param readStream 是否需要解析数据流
	 * @see #parseInputStream(InputStream)
	 * @see #parseJson(String)
	 */
	public final void sendRequestCommand(Map<String, String> parameters, RequestCategory request,
										 AbstractHttpResponse response, boolean readStream) {
		/**
		 * 1.一个完整的Url
		 * 2.
		 */
		String method = getRequestMethod();
		if (TextUtils.isEmpty(method)) {
			method = "?";
		}
		if (!method.endsWith("?")) {
			method = method + "?";
		}
		String url = getBaseUrl() + method + getParameters(parameters);
		HcLog.D(TAG + " #sendRequestCommand url = "+url);
		sendRequestCommand(url, request, response, readStream);

	}

	/**
	 * 一个模版方法,对服务器的请求
	 * @author jrjin
	 * @time 2015-12-17 上午12:06:17
	 * @param url 需要请求的url,完整的Url ： baseUrl + methodUrl + parameterUrl
	 * @param request 请求类型
	 * @param response 请求回调
	 * @param readStream 是否需要解析数据流
	 * @see #parseInputStream(InputStream)
	 * @see #parseJson(String)
	 */
	public final void sendRequestCommand(String url, RequestCategory request, AbstractHttpResponse response, boolean readStream) {
		mReadStream = readStream;
		/**
		 * 1.一个完整的Url
		 * 2.
		 */
		HcLog.D(TAG + " #sendRequestCommand url = " + url);
		if (!TextUtils.isEmpty(url)) {
			String key = HttpRequestQueue.getInstance().hasInTask(url);
			if (key == null) {
				if (mResponse != null) {
					mResponse.onRequestCanel(request);
				}
				return;
			}
			mUrl = key;
			setRequest(url, mRequestType);
			setParameters(request, response);
			HttpRequestQueue.getInstance().addTask(this, mRequestMaxCount * 10 * 1000 + 3 * 1000);
			if (mResponse != null) {
				mResponse.notifyRequestMd5Url(mCategory, key);
			}
			mExecutorService.execute(this);
		}
	}
	
	/**
	 * 判断对应的key是否存在value
	 * 
	 * @param object
	 * @param tag
	 * @return true:有数据；false：没有数据
	 */
	private boolean hasValue(JSONObject object, String tag) {
		boolean exist = false;
		if (object != null && object.has(tag)) {
			try {
				Object object2 = object.get(tag);
				// LOG.D(TAG + " object2 = "+object2 + " tag = "+tag);
				if (object2 != null && !object2.equals("")
						&& !object.isNull(tag)) {
					exist = true;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}

		}
		return exist;
	}
	
	private static String URLEncode(String str) {
		try {
			return java.net.URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			HcLog.D("it is in URLEncode e = " + e);
		}
		return str;
	}
	
	/**
	 * 请求的绝对URL:
	 * getBaseUrl + getRequestMethod + getParameterUrl
	 * @author jrjin
	 * @time 2015-12-17 上午9:00:45
	 * @return 请求的url
	 */
	private String getRequestUrl() {
		String method = getRequestMethod();
		if (TextUtils.isEmpty(method)) {
			method = "?";
		}
		String parameter = getParameterUrl();
		if (TextUtils.isEmpty(parameter))
			return getBaseUrl() + method;
		if (!method.endsWith("?") && !parameter.startsWith("?")) {
			method = method + "?";
		}
		return getBaseUrl() + method + parameter;
	}

	/**
	 * 获取的请求的参数的url
	 * @author jinjr
	 * @date 16-3-23 下午2:09
	 * @return 请求的属性url
	 */
	public abstract String getParameterUrl();

	/**
	 * 设置{@link HttpUriRequest}
	 * @author jrjin
	 * @time 2015-12-17 下午6:59:12
	 * @param url 请求的url
	 * @param type 请求类型
	 * @see HttpRequestQueue#HTTP_REQUEST_GET
	 * @see HttpRequestQueue#HTTP_REQUEST_POST
	 */
	private void setRequest(String url, String type) {
		if (type.equals(HttpRequestQueue.HTTP_REQUEST_GET)) {
			mRequest = new HttpGet(url);			
		} else {
			mRequest = new HttpPost(url);
		}
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
	
	/**
	 * 设置一些必要的参数
	 * {@link #mCategory}
	 * {@link #mResponse}
	 * @author jrjin
	 * @time 2015-12-17 下午7:11:15
	 */
	public void setParameters(RequestCategory request, AbstractHttpResponse response) {
		mCategory = request;
		mResponse = response;
	}
	
	/**
	 * 服务端数据返回成功,需要解析body里面的数据,这里还在主线程中.
	 * @author jrjin
	 * @time 2015-12-18 上午8:51:47
	 * @param dataBody body中的数据
	 */
	public void onSuccess(String dataBody) {
		if (mResponse != null) {
			mResponse.onSuccess(dataBody, mCategory);
		}
	}
	
	/**
	 * 请求数据的时候，网络中断
	 * @author jrjin
	 * @time 2015-12-18 上午8:54:58
	 */
	public  void onNetworkInterrupt() {
		HcLog.D(TAG + getTag() + " it is in onNetworkInterrupt!");
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mResponse != null) {
					mResponse.onNetworkInterrupt(mCategory);
				}
			}
		});
	}
	/**
	 * 请求数据超时
	 * @author jrjin
	 * @time 2015-12-18 上午8:57:41
	 */
	public void onConnectionTimeout() {
		HcLog.D(TAG + getTag() + " it is in onConnectionTimeout!");
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mResponse != null) {
					mResponse.onConnectionTimeout(mCategory);
				}
			}
		});
		
	}
	
	/**
	 * 服务端返回的数据解析失败
	 * @author jrjin
	 * @time 2015-12-18 上午8:58:34
	 */
	public void onParseDataError() {
		HcLog.D(TAG + getTag() + " it is in onParseDataError!");
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mResponse != null) {
					mResponse.onParseDataError(mCategory);
				}
			}
		});
	}
	
	/**
	 * 请求服务器返回的数据的code!=0,用户操作失败
	 * @author jrjin
	 * @time 2015-12-18 上午8:59:47
	 * @param code 服务端返回的状态码
	 * @param msg 服务端返回的对应的状态码信息
	 */
	public void onRequestFailed(final int code, final String msg) {
		HcLog.D(TAG + getTag() + " it is in onRequestFailed! code = "+code + " msg = "+msg);
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mResponse != null) {
					mResponse.onRequestFailed(code, msg, mCategory);
				}
			}
		});

	}
	/**
	 * HttpResponse返回的错误码
	 * @author jrjin
	 * @time 2015-12-18 上午9:03:43
	 * @param code HttpResponse.getStatusLine().getStatusCode()
	 */
	public void onResponseFailed(final int code) {
		HcLog.D(TAG + getTag() + " it is in onResponseFailed! code = "+code);
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mResponse != null) {
					mResponse.onResponseFailed(code, mCategory);
				}
			}
		});
	}
	/**
	 * 未知的错误
	 * @author jrjin
	 * @time 2015-12-20 下午1:42:36
	 */
	public void unknown() {
		HcLog.D(TAG + getTag() + " it is in unKnown!");
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mResponse != null) {
					mResponse.unknown(mCategory);
				}
			}
		});
	}
	
	public String getTag() {

		return this + "#" + getRequestMethod();
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

	private String getParameters(Map<String, String> parameters) {
		if (parameters == null || parameters.size() == 0) return "";
		StringBuilder builder = new StringBuilder();
		Set<Map.Entry<String, String>> entrySet = parameters.entrySet();
		for (Map.Entry<String, String> entry : entrySet) {
			builder.append(entry.getKey() + "=" + URLEncode(entry.getValue() + "&"));
		}
		String parameter =  builder.toString();
		parameter = parameter.substring(0, parameter.length() - 1);
		return parameter;
	}

	/**
	 * 设置{@link HttpUriRequest}
	 * @param request
	 * @author jrjin
	 * @time 2015-12-17 下午6:59:12
	 */
	private void setRequest(HttpUriRequest request) {
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

	/**
	 * 一个模版方法,对服务器的请求,针对post请求
	 * @author jrjin
	 * @time 2015-12-17 上午12:06:17
	 * @param url 需要请求的url,完整的Url ： baseUrl + methodUrl + parameterUrl
	 * @param request 请求类型
	 * @param response 请求回调
	 * @param readStream 是否需要解析数据流
	 * @see #parseInputStream(InputStream)
	 * @see #parseJson(String)
	 */
	public final void sendRequestCommand(String url, HttpUriRequest requestUri, RequestCategory request, AbstractHttpResponse response, boolean readStream) {

		sendRequestCommand(url, requestUri, request, response, readStream , 10);
	}

	/**
	 * 帐号超时
	 * @param data appIds的json格式
	 * @param msg
	 */
	public void onAccountExcluded(final String data, final String msg) {
		HcLog.D(TAG + getTag() + " it is in onAccountExcluded! data = "+data + " msg = "+msg);
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mResponse != null) {
					mResponse.onAccountExcluded(data, msg, mCategory);
				}
			}
		});
	}

	/**
	 * 一个模版方法,对服务器的请求
	 * @author jrjin
	 * @time 2015-12-17 上午12:06:17
	 * @param request 请求类型
	 * @param response 请求回调
	 * @param readStream 是否需要解析数据流
	 * @return a Future representing pending completion of the task
	 * @see #parseInputStream(InputStream)
	 * @see #parseJson(String)
	 */
	public final Future<?> submitRequestTask(RequestCategory request, AbstractHttpResponse response, boolean readStream) {

		String url = getRequestUrl();
		return submitRequestTask(url, request, response, readStream);

	}

	/**
	 * 一个模版方法,对服务器的请求
	 * @author jrjin
	 * @time 2015-12-17 上午12:06:17
	 * @param request 请求类型
	 * @param response 请求回调
	 * @param readStream 是否需要解析数据流
	 * @return a Future representing pending completion of the task
	 * @see #parseInputStream(InputStream)
	 * @see #parseJson(String)
	 */
	public final Future<?> submitRequestTask(Map<String, String> parameters, RequestCategory request,
										 AbstractHttpResponse response, boolean readStream) {
		/**
		 * 1.一个完整的Url
		 * 2.
		 */
		String method = getRequestMethod();
		if (TextUtils.isEmpty(method)) {
			method = "?";
		}
		if (!method.endsWith("?")) {
			method = method + "?";
		}
		String url = getBaseUrl() + method + getParameters(parameters);
		HcLog.D(TAG + " #sendRequestCommand url = " + url);
		return submitRequestTask(url, request, response, readStream);

	}

	/**
	 * 一个模版方法,对服务器的请求
	 * @author jrjin
	 * @time 2015-12-17 上午12:06:17
	 * @param url 需要请求的url,完整的Url ： baseUrl + methodUrl + parameterUrl
	 * @param request 请求类型
	 * @param response 请求回调
	 * @param readStream 是否需要解析数据流
	 * @return a Future representing pending completion of the task
	 * @see #parseInputStream(InputStream)
	 * @see #parseJson(String)
	 */
	public final Future<?> submitRequestTask(String url, RequestCategory request, AbstractHttpResponse response, boolean readStream) {
		mReadStream = readStream;
		/**
		 * 1.一个完整的Url
		 * 2.
		 */
		HcLog.D(TAG + " #sendRequestCommand url = " + url);
		if (!TextUtils.isEmpty(url)) {
			String key = HttpRequestQueue.getInstance().hasInTask(url);
			if (key == null) {
				if (mResponse != null) {
					mResponse.onRequestCanel(request);
				}
				return null;
			}
			mUrl = key;
			setRequest(url, mRequestType);
			setParameters(request, response);
			HttpRequestQueue.getInstance().addTask(this, mRequestMaxCount * 10 * 1000 + 3 * 1000);
			if (mResponse != null) {
				mResponse.notifyRequestMd5Url(mCategory, key);
			}
			mFuture = mExecutorService.submit(this);
			return mFuture;
		}
		return null;
	}

	/**
	 * 一个模版方法,对服务器的请求,针对post请求
	 * @author jrjin
	 * @time 2015-12-17 上午12:06:17
	 * @param url 需要请求的url,完整的Url ： baseUrl + methodUrl + parameterUrl
	 * @param request 请求类型
	 * @param response 请求回调
	 * @param readStream 是否需要解析数据流
	 * @return a Future representing pending completion of the task
	 * @see #parseInputStream(InputStream)
	 * @see #parseJson(String)
	 */
	public final Future<?> submitRequestTask(String url, HttpUriRequest requestUri, RequestCategory request, AbstractHttpResponse response, boolean readStream) {
		mReadStream = readStream;
		/**
		 * 1.一个完整的Url
		 * 2.
		 */
		HcLog.D(TAG + " #sendRequestCommand requestUri = " + requestUri);
		if (!TextUtils.isEmpty(url)) {
			String key = HttpRequestQueue.getInstance().hasInTask(url);
			if (key == null) {
				if (mResponse != null) {
					mResponse.onRequestCanel(request);
				}
				return null;
			}

			mUrl = key;
			setRequest(requestUri);
			setParameters(request, response);
			HttpRequestQueue.getInstance().addTask(this, mRequestMaxCount * 10 * 1000 + 3 * 1000);
			if (mResponse != null) {
				mResponse.notifyRequestMd5Url(mCategory, key);
			}
			mFuture = mExecutorService.submit(this);
			return mFuture;
		}
		return null;
	}

	public void canelFuture() {
		mFuture = null;
	}

	/**
	 * 一个模版方法,对服务器的请求
	 * @author jrjin
	 * @time 2015-12-17 上午12:06:17
	 * @param request 请求类型
	 * @param response 请求回调
	 * @param readStream 是否需要解析数据流
	 * @param result the result to return,不能为空
	 * @return a Future representing pending completion of the task
	 * @see #parseInputStream(InputStream)
	 * @see #parseJson(String)
	 */
	public final <T> Future<T> submitRequestTask(RequestCategory request, AbstractHttpResponse response, boolean readStream, T result) {

		String url = getRequestUrl();
		return submitRequestTask(url, request, response, readStream, result);

	}

	/**
	 * 一个模版方法,对服务器的请求
	 * @author jrjin
	 * @time 2015-12-17 上午12:06:17
	 * @param request 请求类型
	 * @param response 请求回调
	 * @param readStream 是否需要解析数据流
	 * @param result the result to return,不能为空
	 * @return a Future representing pending completion of the task
	 * @see #parseInputStream(InputStream)
	 * @see #parseJson(String)
	 */
	public final <T> Future<T> submitRequestTask(Map<String, String> parameters, RequestCategory request,
											 AbstractHttpResponse response, boolean readStream, T result) {
		/**
		 * 1.一个完整的Url
		 * 2.
		 */
		String method = getRequestMethod();
		if (TextUtils.isEmpty(method)) {
			method = "?";
		}
		if (!method.endsWith("?")) {
			method = method + "?";
		}
		String url = getBaseUrl() + method + getParameters(parameters);
		HcLog.D(TAG + " #sendRequestCommand url = "+url);
		return submitRequestTask(url, request, response, readStream, result);

	}

	/**
	 * 一个模版方法,对服务器的请求
	 * @author jrjin
	 * @time 2015-12-17 上午12:06:17
	 * @param url 需要请求的url,完整的Url ： baseUrl + methodUrl + parameterUrl
	 * @param request 请求类型
	 * @param response 请求回调
	 * @param readStream 是否需要解析数据流
	 * @param result the result to return,不能为空
	 * @return a Future representing pending completion of the task
	 * @see #parseInputStream(InputStream)
	 * @see #parseJson(String)
	 */
	public final <T> Future<T> submitRequestTask(String url, RequestCategory request, AbstractHttpResponse response, boolean readStream, T result) {
		mReadStream = readStream;
		/**
		 * 1.一个完整的Url
		 * 2.
		 */
		HcLog.D(TAG + " #submitRequestTask url = " + url);
		if (!TextUtils.isEmpty(url)) {
			String key = HttpRequestQueue.getInstance().hasInTask(url);
			if (key == null) {
				if (mResponse != null) {
					mResponse.onRequestCanel(request);
				}
				return null;
			}
			mUrl = key;
			setRequest(url, mRequestType);
			setParameters(request, response);
			HttpRequestQueue.getInstance().addTask(this, mRequestMaxCount * 10 * 1000 + 3 * 1000);
			if (mResponse != null) {
				mResponse.notifyRequestMd5Url(mCategory, key);
			}
			mFuture = mExecutorService.submit(this, result);
			return (Future<T>) mFuture;
		}
		return null;
	}

	/**
	 * 一个模版方法,对服务器的请求,针对post请求
	 * @author jrjin
	 * @time 2015-12-17 上午12:06:17
	 * @param url 需要请求的url,完整的Url ： baseUrl + methodUrl + parameterUrl
	 * @param request 请求类型
	 * @param response 请求回调
	 * @param readStream 是否需要解析数据流
	 * @param result the result to return,不能为空
	 * @return a Future representing pending completion of the task
	 * @see #parseInputStream(InputStream)
	 * @see #parseJson(String)
	 */
	public final <T> Future<T> submitRequestTask(String url, HttpUriRequest requestUri, RequestCategory request, AbstractHttpResponse response, boolean readStream, T result) {

		return submitRequestTask(url, requestUri, request, response, readStream, result, 10);
	}

	/**
	 * 一个模版方法,对服务器的请求,针对post请求
	 * @author jrjin
	 * @time 2015-12-17 上午12:06:17
	 * @param url 需要请求的url,完整的Url ： baseUrl + methodUrl + parameterUrl
	 * @param request 请求类型
	 * @param response 请求回调
	 * @param readStream 是否需要解析数据流
	 * @param timeOut 超时时间,为秒,大于10s
	 * @see #parseInputStream(InputStream)
	 * @see #parseJson(String)
	 */
	public final void sendRequestCommand(String url, HttpUriRequest requestUri, RequestCategory request, AbstractHttpResponse response, boolean readStream, int timeOut) {
		mReadStream = readStream;
		/**
		 * 1.一个完整的Url
		 * 2.
		 */
		HcLog.D(TAG + " #sendRequestCommand requestUri = " + requestUri + " url = "+url);
		if (!TextUtils.isEmpty(url)) {
			String key = HttpRequestQueue.getInstance().hasInTask(url);
			if (key == null) {
				if (mResponse != null) {
					mResponse.onRequestCanel(request);
				}
				return;
			}

			mUrl = key;
			setRequest(requestUri);
			setParameters(request, response);
			HttpRequestQueue.getInstance().addTask(this, mRequestMaxCount * timeOut * 1000 + 3 * 1000);
			if (mResponse != null) {
				mResponse.notifyRequestMd5Url(mCategory, key);
			}
			mExecutorService.execute(this);
		}
	}

	/**
	 * 一个模版方法,对服务器的请求,针对post请求
	 * @author jrjin
	 * @time 2015-12-17 上午12:06:17
	 * @param url 需要请求的url,完整的Url ： baseUrl + methodUrl + parameterUrl
	 * @param request 请求类型
	 * @param response 请求回调
	 * @param readStream 是否需要解析数据流
	 * @param result the result to return,不能为空
	 * @param timeOut 超时时间,为秒,大于10s
	 * @return a Future representing pending completion of the task
	 * @see #parseInputStream(InputStream)
	 * @see #parseJson(String)
	 */
	public final <T> Future<T> submitRequestTask(String url, HttpUriRequest requestUri, RequestCategory request, AbstractHttpResponse response, boolean readStream, T result, int timeOut) {
		mReadStream = readStream;
		/**
		 * 1.一个完整的Url
		 * 2.
		 */
		HcLog.D(TAG + " #submitRequestTask requestUri = " + requestUri);
		if (!TextUtils.isEmpty(url)) {
			String key = HttpRequestQueue.getInstance().hasInTask(url);
			if (key == null) {
				if (mResponse != null) {
					mResponse.onRequestCanel(request);
				}
				return null;
			}

			mUrl = key;
			setRequest(requestUri);
			setParameters(request, response);
			HttpRequestQueue.getInstance().addTask(this, mRequestMaxCount * timeOut * 1000 + 3 * 1000);
			if (mResponse != null) {
				mResponse.notifyRequestMd5Url(mCategory, key);
			}
			mFuture = mExecutorService.submit(this, result);
			return (Future<T>) mFuture;
		}
		return null;
	}
}
