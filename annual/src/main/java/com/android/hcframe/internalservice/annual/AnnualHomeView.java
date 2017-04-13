/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2016-1-11 下午1:38:37
*/
package com.android.hcframe.internalservice.annual;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.ModuleInfo;
import com.android.hcframe.container.ContainerActivity;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.login.LoginActivity;
import com.android.hcframe.menu.MenuBaseActivity;
import com.android.hcframe.push.HcAppState;
import com.android.hcframe.sql.SettingHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.json.JSONObject;

import java.util.Observable;

public class AnnualHomeView extends AbstractPage {

	private static final String TAG = "AnnualHomeView";
	
	private final String mAppId;
	
	private ImageView mIcon;
	
	private TextView mIntroBtn;
	private TextView mProgramBtn;
	private TextView mShakeBtn;
	
	private DisplayImageOptions mOptions;
	
	private String mAnnualInfo;
	
	public static final String MODULE_ID = "annual_all_update";
	
	private Result mResult = Result.NONE;
	
	protected AnnualHomeView(Activity context, ViewGroup group, String appId) {
		super(context, group);
		// TODO Auto-generated constructor stub
		mAppId = appId;
		mOptions = new DisplayImageOptions.Builder()
		.imageScaleType(ImageScaleType.EXACTLY)
		/*.showImageOnLoading(R.drawable.annual_home_logo)
		.showImageForEmptyUri(R.drawable.annual_home_logo)
		.showImageOnFail(R.drawable.annual_home_logo)*/
		.cacheInMemory(true).cacheOnDisk(true)
		.considerExifParams(true)
		.bitmapConfig(Bitmap.Config.ARGB_8888).build();
	}

	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		if (id == R.id.annual_home_intro_btn) {
			if (TextUtils.isEmpty(mAnnualInfo)) {
				if (HcUtil.isNetWorkAvailable(mContext)) {
					mResult = Result.INTRO;
					HcDialog.showProgressDialog(mContext, "获取年会信息");
					HcHttpRequest.getRequest().sendAnnualInfoCommand(new AnnualRespone());
				}
			} else {
				startIntroActivity();
			}
			
		} else if (id == R.id.annual_home_program_btn) {
			if (TextUtils.isEmpty(mAnnualInfo)) {
				if (HcUtil.isNetWorkAvailable(mContext)) {
					mResult = Result.PROGRAM;
					HcDialog.showProgressDialog(mContext, "正在获取年会信息！");
					HcHttpRequest.getRequest().sendAnnualInfoCommand(new AnnualRespone());
				}
			} else {
				startProgramActivity();
			}
			
		} else if (id == R.id.annual_home_shake_btn) {
			if (TextUtils.isEmpty(mAnnualInfo)) {
				if (HcUtil.isNetWorkAvailable(mContext)) {
					mResult = Result.SHAKE;
					HcDialog.showProgressDialog(mContext, "正在获取年会信息！");
					HcHttpRequest.getRequest().sendAnnualInfoCommand(new AnnualRespone());
				}
			} else {
				startShakeActivity();
			}
			
		}
	}

	@Override
	public void initialized() {
		// TODO Auto-generated method stub
		String account = SettingHelper.getAccount(mContext);
		if (!TextUtils.isEmpty(account)) {
			mAnnualInfo = SettingHelper.getAnnualInfo(mContext);
			if (TextUtils.isEmpty(mAnnualInfo)) {
				// 去服务端获取数据
				HcDialog.showProgressDialog(mContext, "获取年会信息");
				HcHttpRequest.getRequest().sendAnnualInfoCommand(new AnnualRespone());
			} else {
				// 去检测数据是否有更新
				ModuleInfo info = new ModuleInfo();
				info.setModuleId(MODULE_ID);
				info.setUpdateTime(SettingHelper.getModuleTime(mContext, MODULE_ID, false));
				HcHttpRequest.getRequest().sendModuleCheckCommand(info, new CheckResponse());
			}
		}
		
		
	}

	@Override
	public void setContentView() {
		// TODO Auto-generated method stub
		if (mView == null) {
			mView = mInflater.inflate(R.layout.annual_home_layout, null);
			
			mIcon = (ImageView) mView.findViewById(R.id.annual_home_icon);
			mIntroBtn = (TextView) mView.findViewById(R.id.annual_home_intro_btn);
			mProgramBtn = (TextView) mView.findViewById(R.id.annual_home_program_btn);
			mShakeBtn = (TextView) mView.findViewById(R.id.annual_home_shake_btn);
			
			mIntroBtn.setOnClickListener(this);
			mProgramBtn.setOnClickListener(this);
			mShakeBtn.setOnClickListener(this);
			
			LayoutParams params = (LayoutParams) mIcon.getLayoutParams();
			params.height = (int) (HcUtil.getScreenWidth() - 20 * HcUtil.getScreenDensity()) / 2;
		}
	}

	private void startIntroActivity() {
		Intent intent = new Intent(mContext, AnnualIntroActivity.class);
		mContext.startActivity(intent);
	}
	
	private void startProgramActivity() {
		Intent intent = new Intent(mContext, AnnualProgramActivity.class);
		mContext.startActivity(intent);
	}
	
	private void startShakeActivity() {
		Intent intent = new Intent(mContext, AnnualShakeActivity.class);
		mContext.startActivity(intent);
	}

	@Override
	public void onDestory() {
		// TODO Auto-generated method stub
		mOptions = null;
		mContext = null;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		if (HcUtil.isEmpty(SettingHelper.getToken(mContext))) {
			Intent login = new Intent(mContext, LoginActivity.class);
			login.putExtra("loginout", false);
			mContext.startActivityForResult(login, HcUtil.REQUEST_CODE_LOGIN);
			return;
		} else {
			
			if (onResult) {
				onResult = !onResult;
				mAnnualInfo = SettingHelper.getAnnualInfo(mContext);
				if (TextUtils.isEmpty(mAnnualInfo)) {
					// 去服务端获取数据
					HcDialog.showProgressDialog(mContext, R.string.dialog_title_get_data);
					HcHttpRequest.getRequest().sendAnnualInfoCommand(new AnnualRespone());
				} else {
					// 去检测数据更新
					ModuleInfo info = new ModuleInfo();
					info.setModuleId(MODULE_ID);
					info.setUpdateTime(SettingHelper.getModuleTime(mContext, MODULE_ID, false));
					HcHttpRequest.getRequest().sendModuleCheckCommand(info, new CheckResponse());
				}
			}
			
			Drawable src = mIcon.getDrawable();
			HcLog.D(TAG + " #onResume src = " +src);
			if (src == null) {
//				ImageLoader.getInstance().displayImage("", mIcon, mOptions);
				if (!TextUtils.isEmpty(mAnnualInfo)) {
					// 加载图片
					String uri = parseJson(mAnnualInfo);
					if (!TextUtils.isEmpty(uri)) {
						ImageLoader.getInstance().displayImage(uri, mIcon, mOptions);
					}
				}
			}
		}
	}

	private boolean onResult = false;
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		// 失败跳转界面
		// 成功onresum处理
		if (requestCode == HcUtil.REQUEST_CODE_LOGIN) {
			if (resultCode == HcUtil.LOGIN_SUCCESS)
				onResult = true;// 在onresume里处理
//			else { // 这里在MenuBaseActivity#onActivityResult（）里面统一处理了；更改时间2016-05-13 16：00
//				if (mContext instanceof ContainerActivity) { // 通讯录从应用容器里面进入
//					HcAppState.getInstance().removeActivity(mContext);
//					mContext.finish();
//				} else if (mContext instanceof MenuBaseActivity) {
//					HcUtil.startPreActivity(mContext);//登录取消之后跳转到之前的tab
//				} else {
//					HcAppState.getInstance().removeActivity(mContext);
//					mContext.finish();
//				}
//
//			}
		}
	}

	private String parseJson(String data) {
		try {
			JSONObject object = new JSONObject(data);
			if (HcUtil.hasValue(object, "background_pic")) {
				return object.getString("background_pic");
			}
		} catch (Exception e) {
			// TODO: handle exception
			HcLog.D(TAG + " #parseJson data = "+data + " e = "+e);
		}
		return null;
	}
	
	private class AnnualRespone implements IHttpResponse {

		@Override
		public void notify(Object data, RequestCategory request,
				ResponseCategory category) {
			// TODO Auto-generated method stub
			HcDialog.deleteProgressDialog();
			switch (category) {
			case NETWORK_ERROR:
			case SESSION_TIMEOUT:
				mResult = Result.NONE;
				HcUtil.toastNetworkError(mContext);
				break;
			case SYSTEM_ERROR:
				mResult = Result.NONE;
				HcUtil.toastSystemError(mContext, data);
				break;
			case SUCCESS:
				if (data != null && data instanceof String) {
					HcLog.D(TAG + "$AnnualRespone #notify parse SUCCESS data = "+data);
					try {
						JSONObject object = new JSONObject((String) data);
						int code = object.getInt("code");
						if (code == 0) {
							// 设置更新时间、图片、信息
							object = object.getJSONObject("body");
							if (HcUtil.hasValue(object, "updateTime")) {
								SettingHelper.setModuleTime(mContext, MODULE_ID, object.getString("updateTime"), false);
							}
							if (HcUtil.hasValue(object, "background_pic")) {
								ImageLoader.getInstance().displayImage(object.getString("background_pic"), mIcon, mOptions);
							}
							
							mAnnualInfo = object.toString();
							HcLog.D(TAG + " mAnnualInfo = "+mAnnualInfo);
							SettingHelper.setAnnualInfo(mContext, /*object.toString()*/mAnnualInfo);
							switch (mResult) {
							case INTRO:
								startIntroActivity();
								break;
							case PROGRAM:
								startProgramActivity();
								break;
							case SHAKE:
								startShakeActivity();
								break;

							default:
								break;
							}
							mResult = Result.NONE;
						} else {
							mResult = Result.NONE;
							String msg = object.getString("msg");
							if (code == HcHttpRequest.REQUEST_ACCOUT_EXCLUDED ||
									code == HcHttpRequest.REQUEST_TOKEN_FAILED) {
								// 调用公共模块
								// 公共模块需要处理:1.清除缓存，包括帐号信息
								// 2.解析出现在的权限列表appIds
								// 3.保存appIds并且更新appIds
								// 4.弹出登录的对话框
								if (HcUtil.hasValue(object, "body")) {
									HcUtil.reLogining(object.getJSONObject("body").toString(), mContext, msg);
								}
							} else {
								HcUtil.showToast(mContext, msg);
							}

						}
					} catch (Exception e) {
						// TODO: handle exception
						mResult = Result.NONE;
						HcLog.D(TAG + "$AnnualRespone #notify parse Error e = "+e);
						HcUtil.toastDataError(mContext);
					}
					
				}
				// error
				break;

			default:
				mResult = Result.NONE;
				break;
			}
			 
		}

		@Override
		public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class CheckResponse implements IHttpResponse {

		@Override
		public void notify(Object data, RequestCategory request,
				ResponseCategory category) {
			// TODO Auto-generated method stub
			if (data != null && data instanceof ModuleInfo) {
				ModuleInfo info = (ModuleInfo) data;
				if (info.getUpdateFlag() == ModuleInfo.FLAG_UPDATE) {
					// 之前的数据要删除吗？
					// 删除数据库
					AnnualDatabaseOperate.deleteAnnualProagrams(mContext, parseAnnualId(mAnnualInfo));
					// 删除SharePreference
					SettingHelper.deleteAnnualInfo(mContext);
					
					HcHttpRequest.getRequest().sendAnnualInfoCommand(new AnnualRespone());
				}
			}
		}

		@Override
		public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
			// TODO Auto-generated method stub
			HcLog.D(TAG + "$CheckResponse#notifyRequestMd5Url md5Url = "+md5Url);
		}
		
	}
	
	public enum Result {
		NONE,INTRO,PROGRAM,SHAKE
	}
	
	private String parseAnnualId(String data) {
		try {
			JSONObject object = new JSONObject(data);
			if (HcUtil.hasValue(object, "annual_id")) {
				return object.getString("annual_id");
			}
		} catch (Exception e) {
			// TODO: handle exception
			HcLog.D(TAG + " #parseAnnualId data = "+data + " e = "+e);
		}
		return null;
	}
}
