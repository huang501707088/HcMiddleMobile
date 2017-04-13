/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-9-23 下午2:15:40
*/
package com.android.hcframe.push;

import java.io.File;
import java.util.Observable;

import org.json.JSONObject;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.sql.DataCleanManager;
import com.android.hcframe.sql.SettingHelper;
import com.android.hcframe.sys.SysMassageActivity;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.nostra13.universalimageloader.utils.StorageUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

public class HcPushManager extends Observable implements IHttpResponse {

	private static final String TAG = "HcPushManager";
	
	private static final HcPushManager PUSH_MANAGER = new HcPushManager();
	
	/** 绑定设备成功 */
	public static final int STATUS_OK = 0;
	/** 绑定设备失败 */
	public static final int STATUS_FAIL = 1;
	
	private BindDeviceCallback mCallback;
	
	private static final int FAILED = 1;
	
	/** 点击时的PushInfo */
	private PushInfo mPushInfo;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case FAILED:
				System.exit(0);
				break;

			default:
				break;
			}
		}

	};
	
	private HcPushManager() {}
	
	public static final HcPushManager getInstance() {
		return PUSH_MANAGER;
	}
	
	private String getMetaValue(Context context, String metaKey) {
		Bundle metaData = null;
        String apiKey = null;
        if (context == null || metaKey == null) {
            return null;
        }
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
                apiKey = metaData.getString(metaKey);
            }
        } catch (NameNotFoundException e) {

        }
        HcLog.D("HcPushManager getMetaValue apiKey = "+apiKey);
        return apiKey;
	}
	
	/**
	 * 获取推送通道唯一的ID
	 * @author jrjin
	 * @time 2015-9-23 下午2:26:03
	 * @param context
	 */
	public void getChannelId(Context context) {
		/**
		 * @author jrjin
		 * @date 2015-12-7 下午2:10:38
		 * 这里的判断放在channelIdExist(Context context)方法中处理
		if (TextUtils.isEmpty(SettingHelper.getChannelId(context)));
		*/
		PushManager.startWork(context, PushConstants.LOGIN_TYPE_API_KEY, getMetaValue(context, "api_key"));
	}		

	@Override
	public void notify(Object data, RequestCategory request,
			ResponseCategory category) {
		// TODO Auto-generated method stub
		if (request != null) {
			switch (request) {
			case BINDCHAN:
				if (data != null) {
					switch (category) {
					case SUCCESS:
						HcLog.D(TAG + " ontify SUCCESS callback = "+mCallback);
						if (mCallback != null) {
							mCallback.onResult(STATUS_OK);
						} else { // 说明是百度去绑定设备
							SettingHelper.setBaiduBindDevice(HcApplication.getContext(), true);
						}
						break;

					default:
						if (mCallback != null) {
							mCallback.onResult(STATUS_FAIL);
						}
						break;
					}
				} else {
					if (mCallback != null) {
						mCallback.onResult(STATUS_FAIL);
					}
				}
				break;
			case CHECKAV:
				if (data != null && data instanceof String) {
					switch (category) {
					case SUCCESS:
						try {
							JSONObject object = new JSONObject((String) data).getJSONObject("body");
							if (HcUtil.hasValue(object, "status")) {
								String status = object.getString("status");
								if ("1".equals(status)) { // 新设备需要与管理员说明开通设备
									final AlertDialog.Builder builder = new AlertDialog.Builder(
											HcApplication.getContext());
									builder.setCancelable(false);
									builder.setTitle("新注册用户");
									builder.setMessage("新注册用户需要联系管理员将设备设置为启用状态。");
									builder.setPositiveButton("确定",
											new OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													mHandler.sendEmptyMessageDelayed(FAILED, 200);
												}
											});
									builder.create().show();
								} else if ("3".equals(status)) { // 清除缓存
									// 清除缓存在
									DataCleanManager.cleanApplicationData(
											HcApplication.getContext(),
											true,
											new File(Environment
													.getExternalStorageDirectory()
													+ "/hc/").getAbsolutePath(),
											StorageUtils.getCacheDirectory(HcApplication.getContext())
													.getAbsolutePath());
									HcHttpRequest.getRequest().sendUpdateTerStsCommand(HcUtil
											.getIMEI(HcApplication.getContext()), this);
									mHandler.sendEmptyMessageDelayed(FAILED, 1000);
								}
							}
						} catch (Exception e) {
							// TODO: handle exception
						}
						break;

					default:
						break;
					}
				}
				break;

			default:
				break;
			}
		}
	}

	@Override
	public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * 通知{@link SysMassageActivity#update(Observable, Object)}更新数据
	 * @author jrjin
	 * @time 2015-12-4 上午9:23:08
	 */
	public void notifyUpdateMessage() {
		setChanged();
		notifyObservers();
	}
	
	/**
	 * 是否需要推动功能
	 * @author jrjin
	 * @time 2015-12-7 下午1:59:38
	 * @return
	 */
	public boolean pushed() {
		return HcConfig.getConfig().baiDuPushed();
	}
	
	/**
	 * 是否已经去服务端注册设备
	 * @author jrjin
	 * @time 2015-12-7 下午2:00:47
	 * @return true:已经注册
	 */
	public boolean registered(Context context) {
		return !SettingHelper.showGuidePage(context, HcConfig.getConfig().getAppVersion(), false);
	}
	
	/**
	 * 百度推送的ChannelId是否已经存在
	 * @author jrjin
	 * @time 2015-12-7 下午2:09:07
	 * @param context
	 * @return
	 */
	public boolean channelIdExist(Context context) {
		if (TextUtils.isEmpty(SettingHelper.getChannelId(context)))
			return false;
		return true;
	}
	
	/**
	 * 
	 * @author jrjin
	 * @time 2015-12-7 下午2:44:06
	 * @param context
	 * @param push
	 */
	public void registerDevice(Context context, boolean push) {
		if (push) {
			HcHttpRequest.getRequest().sendBindChannel(HcUtil.getIMEI(context), SettingHelper.getChannelId(context), 
					HcConfig.getConfig().getAppVersion(), "" + 0, this);
		} else {
			HcHttpRequest.getRequest().sendBindChannel(HcUtil.getIMEI(context), "", 
					HcConfig.getConfig().getAppVersion(), "" + 0, this);
		}
		
	}
	
	public static interface BindDeviceCallback {
		/**
		 * 绑定设备的结果
		 * @author jrjin
		 * @time 2015-12-7 下午2:29:42
		 * @param status {@link HcPushManager#STATUS_OK}, {@link HcPushManager#STATUS_FAIL}
		 */
		public void onResult(int status);
	}
	
	public void setBindDeviceCallback(BindDeviceCallback callback) {
		mCallback = callback;
	}
	
	/**
	 * @deprecated
	 * @author jrjin
	 * @time 2015-12-17 上午10:43:17
	 */
	public void registerFailed() {
//		if (mCallback != null) {
//			mCallback.onResult(STATUS_FAIL);
//		}
		;
	}
	
	public boolean deviceBinded(Context context) {
		return SettingHelper.getBaiduBindDevice(context);
	}
	
	public void sendCheckDevice(Context context) {
		HcHttpRequest.getRequest().sendCheckAppVersionCommand(HcConfig.getConfig()
				.getAppVersion(), 0 + "", HcUtil.getIMEI(context), this);
	}
	
	public void setPushInfo(PushInfo info) {
		mPushInfo = info;
	}
	
	public PushInfo getPushInfo() {
		return mPushInfo;
	}
}
