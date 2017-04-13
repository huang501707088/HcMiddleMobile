package com.android.hcframe.pcenter;

import com.android.hcframe.HcObservable;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;

public class PCenterManager extends HcObservable implements IHttpResponse {

	private HcHttpRequest mRequest;

	public PCenterManager() {
		mRequest = HcHttpRequest.getRequest();
	}

	@Override
	public void notify(Object data, RequestCategory request,
			ResponseCategory category) {
		notifyObservers(this, data, request, category);
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

	public void sendNicknameCommand(String account, String realName) {
		mRequest.sendNicknameCommand(account, realName, this);
	}

	public void sendBindPhoneCommand(String account, String oldmobile,
			String newmobile, String oldcode, String newcode) {
		mRequest.sendBindPhoneCommand(account, oldmobile, newmobile, oldcode,
				newcode, this);
	}
}
