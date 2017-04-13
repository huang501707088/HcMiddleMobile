/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2013-12-10 下午4:32:38
*/
package com.android.hcframe;

import com.android.hcframe.push.HcAppState;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Process;
import android.view.KeyEvent;

public class HcBaseActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		HcAppState.getInstance().addActivity(this);
	}
	
	private long mExitTime = 0;

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
//		switch (keyCode) {
//		case KeyEvent.KEYCODE_BACK:
//			if (keyCode == KeyEvent.KEYCODE_BACK
//					&& event.getAction() == KeyEvent.ACTION_DOWN) {
//				if ((System.currentTimeMillis() - mExitTime) > 2000) {
//					HcUtil.showToast(getApplicationContext(), getResources().getString(R.string.application_exit));
//					HcLog.D("it is onKeyDown! first down......");
//					mExitTime = System.currentTimeMillis();
//				} else {
//					HcAppState.getInstance().removeActivity(this);
//					finish();
//					System.exit(0);
//					HcLog.D(" pid = "+Process.myPid());
////					Process.killProcess(Process.myPid());
//				}
//				return true;
//			}
//			break;
//
//		default:
//			break;
//		}
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (keyCode == KeyEvent.KEYCODE_BACK
			&& event.getAction() == KeyEvent.ACTION_DOWN) {
				HcAppState.getInstance().removeActivity(this);
			}
			break;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		HcAppState.getInstance().removeActivity(this);
		super.onDestroy();
	}
}
