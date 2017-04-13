/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-3-13 下午3:25:34
 */
package com.android.hcframe.login;

import com.android.hcframe.HcObservable;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;

public class LoginManager extends HcObservable implements IHttpResponse {

	private HcHttpRequest mRequest;

	public enum CodeType {
		NONE, REGISTER, RETRIEVE
	};

	public LoginManager() {
		mRequest = HcHttpRequest.getRequest();
	}

	@Override
	public void notify(Object data, RequestCategory request,
			ResponseCategory category) {
		// TODO Auto-generated method stub
		notifyObservers(this, data, request, category);
	}

	public void login(String name, String pw, String deviceid) {
		mRequest.sendLoginCommand(name, pw, deviceid, this);
	}

	@Override
	public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

	}

	public void getCode(String account, String mobile, String type) {
		mRequest.sendGetCodeCommand(account, mobile, type, this);
	}

	public void checkCode(String account, String mobile, String type,
			String code) {
		mRequest.sendCheckCodeCommand(account, mobile, type, code, this);
	}

	public void sendRegisterCommand(String account, String mobile,
			String password, String code) {
		mRequest.sendRegisterCommand(account, mobile, password, code, this);
	}

	public void sendRegetPwdCommand(String account, String mobile,
			String newpwd, String code) {
		mRequest.sendRegetPwdCommand(account, mobile, newpwd, code, this);
	}

}
