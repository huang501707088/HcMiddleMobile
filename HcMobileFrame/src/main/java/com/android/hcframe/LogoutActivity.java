/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-5-17 下午3:30:06
 */
package com.android.hcframe;

import java.util.Timer;
import java.util.TimerTask;

import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.push.HcAppState;
import com.android.hcframe.sql.SettingHelper;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

public class LogoutActivity extends Activity {

	private static Timer mTimer = new Timer("finish");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		HcAppState.getInstance().addActivity(this);
		mTimer.schedule(new FinishTask(), 5 * 1000);

//		// 解除绑定用户
//		HcHttpRequest.getRequest().sendBindChannel(
//				HcUtil.getIMEI(this),
//				SettingHelper.getChannelId(this),
//				HcConfig.getConfig().getAppVersion(), 0 + "",
//				new IHttpResponse() {
//
//					@Override
//					public void notifyRequestMd5Url(RequestCategory request,
//							String md5Url) {
//					}
//
//					@Override
//					public void notify(Object data, RequestCategory request,
//							ResponseCategory category) {
//						
//					}
//				});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	private class FinishTask extends TimerTask {

		public FinishTask() {

		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			HcLog.D(" LogoutActivity#FinishTask");
			HcAppState.getInstance().removeActivity(LogoutActivity.this);
			finish();
			System.exit(0);
		}

	}

	public static void finishTask() {
		mTimer.cancel();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		HcLog.D(" LogoutActivity it is onPause!");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		HcLog.D(" LogoutActivity it is onDestroy!");
	}

}
