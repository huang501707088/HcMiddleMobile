package com.android.hcframe.modifypwd;

import com.android.hcframe.HcObservable;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;

public class ModifyManager extends HcObservable implements IHttpResponse {
	private HcHttpRequest mRequest;

	public ModifyManager() {
		mRequest = HcHttpRequest.getRequest();
	}

	@Override
	public void notify(Object data, RequestCategory request,
			ResponseCategory category) {
		notifyObservers(this, data, request, category);
	}

	public void modifypwd(String account,String oldpwd,String newpwd)
	{
		mRequest.sendModifyCommand(account, oldpwd, newpwd, this);
	}

	@Override
	public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
		
	}
}
