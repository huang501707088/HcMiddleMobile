package com.android.hcframe.push;

import com.android.hcframe.HcObservable;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;

public class PushManager extends HcObservable implements IHttpResponse {

	private HcHttpRequest mRequest;

	public PushManager() {
		mRequest = HcHttpRequest.getRequest();
	}

	@Override
	public void notify(Object data, RequestCategory request,
			ResponseCategory category) {
		// TODO Auto-generated method stub
		notifyObservers(this, data, request, category);
	}

	@Override
	public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

	}

	public void sendBindChannel(String imei, String channelID,
			String versioncode, String ptype) {
		mRequest.sendBindChannel(imei, channelID, versioncode, ptype, this);
	}

	public void sendPushModuleList(String versioncode, String channelId,
			String ptype) {
		mRequest.sendPushModuleList(versioncode, channelId, ptype, this);
	}

	public void sendUpdatePushSettings(String channelId, String App_id,
			String Is_push) {
		mRequest.sendUpdatePushSettings(channelId, App_id, Is_push, this);
	}
}
