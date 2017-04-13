/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-20 下午2:40:29
*/
package com.android.hcframe.http;

import android.text.TextUtils;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;

/**
 * 服务端返回的消息处理,基本都在主线程中进行，除了下载数据
 * @author jrjin
 * @time 2015-12-20 下午2:44:20
 */
public abstract class AbstractHttpResponse implements IHttpResponse {

	private static final String TAG = "AbstractHttpResponse$";

	public AbstractHttpResponse() {}

	/**
	 * 请求返回成功,这里还在线程里面
	 * @param data
	 * @param request
	 */
	public abstract void onSuccess(Object data, RequestCategory request);

	@Override
	public final void notify(Object data, RequestCategory request,
			ResponseCategory category) {
		// TODO Auto-generated method stub
		HcLog.D(TAG + getTag() + "#notify request = " + request + " data = " + data);
		onSuccess(data, request);
	}

	/**
	 * 请求数据的时候，网络中断
	 * @author jrjin
	 * @time 2015-12-18 上午8:54:58
	 */
	public void onNetworkInterrupt(RequestCategory request) {
		HcLog.D(TAG + getTag() + "#onNetworkInterrupt request = "+request);
		HcDialog.deleteProgressDialog();
		HcUtil.toastNetworkError(HcApplication.getContext());
	}
	/**
	 * 请求数据超时,已经在主线程里面
	 * @author jrjin
	 * @time 2015-12-18 上午8:57:41
	 */
	public void onConnectionTimeout(RequestCategory request) {
		HcLog.D(TAG + getTag() + "#onConnectionTimeout request = "+request);
		HcDialog.deleteProgressDialog();
		HcUtil.toastTimeOut(HcApplication.getContext());
	}
	
	/**
	 * 服务端返回的数据解析失败,已经在主线程里面
	 * @author jrjin
	 * @time 2015-12-18 上午8:58:34
	 */
	public void onParseDataError(RequestCategory request) {
		HcLog.D(TAG + getTag() + "#onParseDataError request = "+request);
		HcDialog.deleteProgressDialog();
		HcUtil.toastDataError(HcApplication.getContext());
	}
	
	/**
	 * 请求服务器返回的数据的code!=0,用户操作失败,已经在主线程里面
	 * @author jrjin
	 * @time 2015-12-18 上午8:59:47
	 * @param code 服务端返回的状态码
	 * @param msg 服务端返回的对应的状态码信息
	 */
	public void onRequestFailed(int code, final String msg, RequestCategory request) {
		HcLog.D(TAG + getTag() + "#onRequestFailed request = " + request);
		HcDialog.deleteProgressDialog();
		if (!TextUtils.isEmpty(msg)) {
			HcUtil.showToast(HcApplication.getContext(), msg);
		}
	}
	/**
	 * HttpResponse返回的错误码,已经在主线程里面
	 * @author jrjin
	 * @time 2015-12-18 上午9:03:43
	 * @param code HttpResponse.getStatusLine().getStatusCode()
	 */
	public void onResponseFailed(int code, RequestCategory request) {
		HcLog.D(TAG + getTag() + "#onResponseFailed request = "+request);
		HcDialog.deleteProgressDialog();
		HcUtil.toastSystemError(HcApplication.getContext(), code);
	}
	/**
	 * 未知的错误,已经在主线程里面
	 * @author jrjin
	 * @time 2015-12-20 下午1:42:36
	 */
	public void unknown(RequestCategory request) {
		HcLog.D(TAG + getTag() + "#unknown request = " + request);
		HcDialog.deleteProgressDialog();
	}

	/** log的输出标签 */
	public abstract String getTag();

	/**
	 * 重复的请求被取消
	 * @see HttpRequestQueue#hasInTask(String)
	 * @see AbstractHttpRequest#sendRequestCommand(RequestCategory, AbstractHttpResponse, boolean)
	 * */
	public void onRequestCanel(RequestCategory request) {
		HcLog.D(TAG + getTag() + "#onRequestCanel request = " + request);
		HcDialog.deleteProgressDialog();
	}

	/**
	 * 帐号超时
	 * @param data appIds的json格式
	 * @param msg
	 */
	public abstract void onAccountExcluded(String data, String msg, RequestCategory category);
}
