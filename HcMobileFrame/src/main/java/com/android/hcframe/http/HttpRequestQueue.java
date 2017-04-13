/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-17 上午8:44:12
*/
package com.android.hcframe.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;

public class HttpRequestQueue {

	private static final String TAG = "HttpRequestQueue";
	
	private static HttpRequestQueue QUEUE = new HttpRequestQueue();
	
	private AbstractHcHttpClient mClient;
	
	public static final String BASE_URL = "/terminalServer/szf/";

	public static final String STATUS = "code";
	public static final String BODY = "body";
	public static final String MSG = "msg";
	
	/**
	 * 返回成功
	 */
	public static final int REQUEST_SUCCESS = 0;
	
	private Timer mTimer = new Timer("request_queue");

	/**
	 * 请求等待的超时时间
	 */
	private static final int TIME_OUT = 30 * 1000;
	
	/**
	 * key:请求的URL的MD5格式 value:超时处理的Task
	 */
	private Map<String, TimerTask> mTaskMap = new HashMap<String, TimerTask>();

	/**
	 * @author jrjin
	 * @date 2015-12-5 下午4:59:32 最大请求次数
	 */
	private static final int REQUEST_MAX_COUNT = 3;
	
	public static final String HTTP_REQUEST_GET = "GET";
	
	public static final String HTTP_REQUEST_POST = "POST";
	
	private Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) { // 处理已经放到AbstractHttpRequest#sendMessage里面了
			// TODO Auto-generated method stub
			int what = msg.what;
			Object data = msg.obj;
			switch (what) {
			case 400:
			case 402:
				if (msg.obj == null)
					return;
				if (msg.obj instanceof AbstractHttpRequest) {
					AbstractHttpRequest http = (AbstractHttpRequest) msg.obj;
					/**
					 * @author jrjin
					 * @date 2015-12-5 下午4:33:09 增加了重复请求的次数，减少容错率
					 */
					if (http.mRequestCount < REQUEST_MAX_COUNT) {
						http.mRequestCount = http.mRequestCount + 1;
						mClient.execute(http);
					} else { // 已经超过设定的请求次数了
						cancelTask(http);
						http.onNetworkInterrupt();
					}
				
				}
				break;

			default:
				break;
			}

		}

	};
	
	private HttpRequestQueue() {
		mClient = new DefaultClient();
	}
	
	public static HttpRequestQueue getInstance() {
		return QUEUE;
	}
	
	public void setHttpClient(AbstractHcHttpClient client) {
		mClient = client;
	}
	
	public void cancelTask(AbstractHttpRequest http) {
		TimerTask task = null;
		synchronized (this) {
			http.canelFuture();
			task = mTaskMap.remove(http.mUrl);
			
		}
		if (task != null)
			task.cancel();
		HcLog.D(TAG + " #cancelTask end!");
	}
	
	/**
	 * 
	 * @author jrjin
	 * @time 2015-12-20 下午7:43:10
	 * @param http
	 * @param delay amount of time in milliseconds before execution
	 */
	public void addTask(AbstractHttpRequest http, long delay) {
		if (http == null)
			return;
		RequestTask task = new RequestTask(http);
		synchronized (this) {
			mTaskMap.put(http.mUrl, task);
			mTimer.schedule(task, delay);
		}
	}
	
	private void postView(final Object category, final IHttpResponse respose,
			final ResponseCategory rc, final RequestCategory rq) {
		if (respose != null) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					respose.notify(category, rq, rc);
				}
			});
		}
	}
	
	/**
	 * 
	 * @author jrjin
	 * @time 2015-6-19 上午11:47:18
	 * @param url
	 * @return 要是第一次请求返回url的MD5格式, 否则返回null
	 */
	public String hasInTask(String url) {
		String key = null;
		if (!TextUtils.isEmpty(url)) { // Null不需要请求
			key = HcUtil.getMD5String(url);
			synchronized (this) {
				if (mTaskMap.containsKey(key))
					key = null; // 已经存在则返回Null
			}
		}
		return key;
	}
	
	/**
	 * 用户主动取消请求或者退出当前页面
	 * 
	 * @author jrjin
	 * @time 2015-9-24 下午4:06:12
	 * @param md5Url
	 */
	public void cancelRequest(String md5Url) {
		synchronized (this) {
			TimerTask task = mTaskMap.remove(md5Url);
			if (null != task) {
				AbstractHttpRequest http = ((RequestTask) task).mHttp;
				if (null != http) {
					http.cancelRequest();
					http = null;
				}
				task.cancel();
				task = null;
			}
		}
	}
	
	private class RequestTask extends TimerTask {

		private AbstractHttpRequest mHttp;

		public RequestTask(AbstractHttpRequest http) {
			mHttp = http;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			HcLog.D(TAG + " #RequestTask http = " + mHttp);

			synchronized (HttpRequestQueue.this) {
				if (mHttp == null) return;

				/**
				 * @author jrjin
				 * @date 2016-2-23 下午3:10:08
				 * 更新日志
				 */
				mHttp.updateLog(false);
				/**
				 * @author jrjin
				 * @date 2015-12-5 下午4:36:11 取消返回的处理
				 */
				mHttp.onConnectionTimeout();
				mHttp.cancelRequest();

				mTaskMap.remove(mHttp.mUrl);
				mHttp = null;
			}

			
		}

	}
	
	public AbstractHcHttpClient getHttpClient() {
		return mClient;
	}
	
	public Handler getHttpHandler() {
		return mHandler;
	}

	public static String URLEncode(String str) {
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			HcLog.D("it is in URLEncode e = " + e);
		}
		return str;
	}
}
