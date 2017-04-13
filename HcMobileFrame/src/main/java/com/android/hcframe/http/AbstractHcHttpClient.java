/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2013-11-26 下午1:21:43
*/
package com.android.hcframe.http;


import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

public abstract class AbstractHcHttpClient implements IHttpClientFactory {

	private ExecutorService mService;
	private HttpClient mClient;
	
	public AbstractHcHttpClient() {
		mService = Executors.newCachedThreadPool();
		mClient = getHttpClient(); // 为了确保唯一的client，所以在构造方法里调用.
	}
	
	@Override
	public HttpClient getHttpClient() {
		// TODO Auto-generated method stub
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, /*5000*/10 * 1000);
		HttpConnectionParams.setSoTimeout(httpParams, /*5000*/10 * 1000); 
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		registry.register(new Scheme("https",SSLSocketFactory.getSocketFactory(), 443));
		ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(httpParams, registry);
		return new DefaultHttpClient(cm, httpParams);
	}

	public final void execute(Runnable r) {
		mService.execute(r);
	}
	
	public final HttpClient getClient() {
		return mClient;
	}

	/**
	 * Submits a value-returning task for execution and returns a
	 * Future representing the pending results of the task. The
	 * Future's {@code get} method will return the task's result upon
	 * successful completion.
	 *
	 * <p>
	 * If you would like to immediately block waiting
	 * for a task, you can use constructions of the form
	 * {@code result = exec.submit(aCallable).get();}
	 *
	 * <p>Note: The {@link Executors} class includes a set of methods
	 * that can convert some other common closure-like objects,
	 * for example, {@link java.security.PrivilegedAction} to
	 * {@link Callable} form so they can be submitted.
	 *
	 * @param task the task to submit
	 * @return a Future representing pending completion of the task
	 * @throws RejectedExecutionException if the task cannot be
	 *         scheduled for execution
	 * @throws NullPointerException if the task is null
	 */
	public final <T> Future<T> submit(Callable<T> task) {
		return mService.submit(task);
	}

	/**
	 * Submits a Runnable task for execution and returns a Future
	 * representing that task. The Future's {@code get} method will
	 * return the given result upon successful completion.
	 *
	 * @param task the task to submit
	 * @param result the result to return
	 * @return a Future representing pending completion of the task
	 * @throws RejectedExecutionException if the task cannot be
	 *         scheduled for execution
	 * @throws NullPointerException if the task is null
	 */
	public final <T> Future<T> submit(Runnable task, T result) {
		return mService.submit(task, result);
	}

	/**
	 * Submits a Runnable task for execution and returns a Future
	 * representing that task. The Future's {@code get} method will
	 * return {@code null} upon <em>successful</em> completion.
	 *
	 * @param task the task to submit
	 * @return a Future representing pending completion of the task
	 * @throws RejectedExecutionException if the task cannot be
	 *         scheduled for execution
	 * @throws NullPointerException if the task is null
	 */
	public final Future<?> submit(Runnable task) {
		return mService.submit(task);
	}
}
