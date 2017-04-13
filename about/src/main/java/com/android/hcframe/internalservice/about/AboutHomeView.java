/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-14 下午2:00:12
*/
package com.android.hcframe.internalservice.about;

import java.util.Observable;

import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.intro.IntroActivity;
import com.android.hcframe.menu.HtmlActivity;
import com.android.hcframe.update.DownloadService;

public class AboutHomeView extends AbstractPage implements IHttpResponse, ServiceConnection {

	private static final String TAG = "AboutHomeView";
	
	private TextView mAppVersion;
	
	private LinearLayout mAppUpdate;
	
	private LinearLayout mShowInfo;
	
	private LinearLayout mWelcome;
	
	private LinearLayout mQRCode;
	
	private LinearLayout mFeedback;
	
	private TextView mShowVersion;
	
	private TextView mCopyright;
	
	/** 版本更新的数据 */
	private String mUpdateData;
	
	/** 是否检测版本弹出对话框,只有一种可能,就是点击版本检测,去服务端检测,返回直接弹出对话框 */
	private boolean mShowDialog = false;
	
	private DownloadService mDownloadService;
	
	private boolean mLatest = true;
	
	protected AboutHomeView(Activity context, ViewGroup group) {
		super(context, group);
		// TODO Auto-generated constructor stub
		Intent intent = new Intent();
		intent.setAction(mContext.getPackageName() + ".DownloadService");
		intent.setPackage(mContext.getPackageName());
		mContext.bindService(intent, this, Context.BIND_AUTO_CREATE);

	}

	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		if (id == R.id.about_content_feedback_parent) {
			startFeedbackActivity();
		} else if (id == R.id.about_content_appinfo_parent) {
			startFeatures();
		} else if (id == R.id.about_content_welcome_parent) {
			startIntroActivity();
		} else if (id == R.id.about_content_erweima_parent) {
			startQRCodeActivity();
		} else if (id == R.id.about_content_version_parent) {
			HcLog.D(TAG + "#onClick check version updateDate = "+mUpdateData + " lastest = "+mLatest + " service = "+mDownloadService);
			if (!TextUtils.isEmpty(mUpdateData)) {
				if (!mLatest) { // 调用升级
					if (mDownloadService != null) {
						mDownloadService.updateVersion(mUpdateData);
					}
				} 
			} else {
				if (!HcUtil.isNetWorkError(mContext)) {
					HcDialog.showProgressDialog(mContext, "版本检测中");
					mShowDialog = true;
					HcHttpRequest.getRequest().sendCheckAppVersionCommand(HcConfig.getConfig()
							.getAppVersion(), 0 + "", HcUtil
							.getIMEI(mContext), this);
				}
			}
		}
	}
	
	private void startFeedbackActivity() {
		Intent intent = new Intent(mContext, FeedbackActivity.class);
		mContext.startActivity(intent);
		mContext.overridePendingTransition(0, 0);
	}

	private void startFeatures() {
		Intent intent = new Intent(mContext, HtmlActivity.class);
		intent.putExtra("title", "功能介绍");
		intent.putExtra("mAppId", "");
		intent.putExtra("url", HcUtil.getScheme() + HcHttpRequest.BASE_URL + "getAppMemo");
		mContext.startActivity(intent);
		mContext.overridePendingTransition(0, 0);
	}
	
	private void startIntroActivity() {
		Intent intent = new Intent(mContext, IntroActivity.class);
		intent.putExtra("finishOnly", true);
		mContext.startActivity(intent);
		mContext.overridePendingTransition(0, 0);
	}
	
	private void startQRCodeActivity() {
		Intent intent = new Intent(mContext, QRCodeActivity.class);
		mContext.startActivity(intent);
		mContext.overridePendingTransition(0, 0);
	}
	
	@Override
	public void initialized() {
		// TODO Auto-generated method stub
		mCopyright.setText(HcConfig.getConfig().getCopyright());
		mAppVersion.setText(mContext.getPackageManager().getApplicationLabel(mContext.getApplicationInfo()) + " V" + HcConfig.getConfig().getAppVersion() + " " + HcConfig.getConfig().getVersionName());
	}

	@Override
	public void setContentView() {
		// TODO Auto-generated method stub
		if (mView == null) {
			mView = mInflater.inflate(R.layout.about_content_layout, null);
			
			mAppVersion = (TextView) mView.findViewById(R.id.about_content_appversion);
			
			mAppUpdate = (LinearLayout) mView.findViewById(R.id.about_content_version_parent);
			
			mShowInfo = (LinearLayout) mView.findViewById(R.id.about_content_appinfo_parent);
			
			mWelcome = (LinearLayout) mView.findViewById(R.id.about_content_welcome_parent);
			
			mQRCode = (LinearLayout) mView.findViewById(R.id.about_content_erweima_parent);
			
			mFeedback = (LinearLayout) mView.findViewById(R.id.about_content_feedback_parent);
			
			mShowVersion = (TextView) mView.findViewById(R.id.about_content_version_show);
			
			mCopyright = (TextView) mView.findViewById(R.id.about_content_copyright);
			
			mAppUpdate.setOnClickListener(this);
			mShowInfo.setOnClickListener(this);
			mWelcome.setOnClickListener(this);
			mQRCode.setOnClickListener(this);
			mFeedback.setOnClickListener(this);
		}
	}

	@Override
	public void onDestory() {
		// TODO Auto-generated method stub
		
		if (mDownloadService != null) {
			mDownloadService = null;
		}
		
		mContext.unbindService(this);
		
		mContext = null;
		mView = null;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		//版本检测
		if (HcUtil.isNetWorkAvailable(mContext) && TextUtils.isEmpty(mUpdateData)) {
			HcHttpRequest.getRequest().sendCheckAppVersionCommand(HcConfig.getConfig()
					.getAppVersion(), 0 + "", HcUtil
					.getIMEI(mContext), this);
		}
		
	}

	@Override
	public void notify(Object data, RequestCategory request,
			ResponseCategory category) {
		// TODO Auto-generated method stub
		if (request != null) {
			switch (request) {
			case CHECKAV:
				HcDialog.deleteProgressDialog();
				if (data != null && data instanceof String) {
					switch (category) {
					case SUCCESS: // body里面的数据了
						try {
							mUpdateData = (String) data;
							JSONObject object = new JSONObject(mUpdateData);
							if (HcUtil.hasValue(object, "status")) {
								if (mShowDialog) {
									mShowDialog = !mShowDialog;
									// 这里调用DownloadService去下载文件
									if (mDownloadService != null) {
										mDownloadService.updateVersion(mUpdateData);
									}
								}
								return;
							}
							
							
							int flag = object.getInt("flag");
							if (flag == 0) {
								mLatest = true;
								mShowVersion.setVisibility(View.VISIBLE);
								mShowVersion.setText("已是最新版");
							} else {
								mLatest = false;
								mShowVersion.setVisibility(View.VISIBLE);
								mShowVersion.setText("发现新版本");
							}
							if (mShowDialog) {
								mShowDialog = !mShowDialog;
								// 这里调用DownloadService去下载文件
								if (mDownloadService != null) {
									mDownloadService.updateVersion(mUpdateData);
								}
							}
							
						} catch (Exception e) {
							// TODO: handle exception
							mShowVersion.setVisibility(View.GONE);
						}
						if (mShowDialog) {
							mShowDialog = !mShowDialog;
						}
						break;

					default:
						// 不用更新
						if (mShowDialog) {
							mShowDialog = !mShowDialog;
						}
						mShowVersion.setVisibility(View.GONE);
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

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// TODO Auto-generated method stub
		HcLog.D(TAG + " onServiceConnected ComponentName = "+name + " service = "+service);
		if (mDownloadService == null) {
			mDownloadService = ((DownloadService.LocalDownloadBinder) service).getLocalService();
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub
		
	}

}
