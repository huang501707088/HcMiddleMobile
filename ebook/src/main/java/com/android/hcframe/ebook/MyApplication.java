package com.android.hcframe.ebook;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.widget.Toast;

import com.android.hcframe.ebook.entity.ConfigEntity;

import java.util.List;

public class MyApplication extends Application {
	public static boolean isUseEbenSDK = false;
	public static int signType = ConfigEntity.SUBMIT_PDF;
	public static SharedPreferences sharedPreferences;
	Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			Toast.makeText(getApplicationContext(), "获取初始化数据失败", Toast.LENGTH_SHORT).show();
		};
	};

	@Override
	public void onCreate() {
		super.onCreate();
		sharedPreferences = getSharedPreferences("configs", MODE_PRIVATE);
		getFirstData();
	}

	private void getFirstData() {
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				HcResponse excuetPost = HttpRequest.excuetPost("http://zjhcservice.iask.in:8080/sign/config/getconfig",
//						"configname", "terminaltype,signtype");
//				switch (excuetPost.getCode()) {
//				case HcResponse.SUCCESS:
//					MyApplication.setConfigs(excuetPost.getBody());
//					List<ConfigEntity> list = JSONUtil.toList(excuetPost.getBody(), ConfigEntity.class);
//
//					if (list != null && list.size() > 0) {
//						for (int i = 0; i < list.size(); i++) {
//							String configname = list.get(i).getConfigname();
//							if ("terminaltype".equals(configname)) {
//								if (ConfigEntity.SIGN_TYPE_EBEN == list.get(i).getConfigvalue()) {
//									isUseEbenSDK = true;
//								}
//							} else if ("signtype".equals(configname)) {
//								if (ConfigEntity.SUBMIT_TXT == list.get(i).getConfigvalue()) {
//									signType = ConfigEntity.SUBMIT_TXT;
//								}
//								if (ConfigEntity.SUBMIT_PDF == list.get(i).getConfigvalue()) {
//									signType = ConfigEntity.SUBMIT_PDF;
//								}
//							}
//						}
//					}
//					break;
//
//				default:
//					break;
//				}
//			}
//		}).start();

	}

	public static void setConfigs(String str) {
		Editor edit = sharedPreferences.edit();
		edit.clear();
		edit.putString("getConfigs", str);
		edit.commit();
	}

	public static String getConfigs() {
		return sharedPreferences.getString("getConfigs", null);
	}
}
