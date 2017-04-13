/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-4-29 上午9:25:00
 */
package com.android.hcframe.menu;

import com.android.hcframe.CacheManager;
import com.android.hcframe.HcApplication;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.TopBarView;
import com.android.hcframe.badge.BadgeCache;
import com.android.hcframe.container.ContainerActivity;
import com.android.hcframe.login.LoginActivity;
import com.android.hcframe.monitor.LogManager;
import com.android.hcframe.push.HcAppState;
import com.android.hcframe.sql.SettingHelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

public class MenuBaseActivity extends Activity {

	private static final String TAG = "MenuBaseActivity";

	protected int mHighMenuId = R.id.menu1;

	protected FrameLayout mParent;

	private MenuInfo mMenuInfo;

	protected TopBarView mTopBarView;

	private MenuPageFactory mPageFactory;

	/**
	 * 判断是否作为Menu呈现，还是作为从应用容器里面的应用启动呈现
	 */
	private boolean mIsMenuActivity = true;

	private String mClassName;

	protected String mAppId;

	private String mAppName;
	
	private View mNetworkError;
	
	public static final String NETWORK_ACTION = ".network.CONNECTIVITY_CHANGE";

	/** 用于显示无权限的提示 */
	private View mPermisstion;

	/** 从应用容器里面启动的h5应用的url */
	private String mUrl;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		HcLog.D(TAG + " it is onCreate! tast id ============================= "+getTaskId() + " this =" +this);
		HcLog.D(TAG + " #onCreate start time = "+HcUtil.getDate(HcUtil.FORMAT_HOUR, System.currentTimeMillis()));
		Intent intent = getIntent();
		if (null != intent && null != intent.getExtras()) {
			Bundle bundle = intent.getExtras();
			mIsMenuActivity = bundle.getBoolean("menu", true);
			if (!mIsMenuActivity) { // 说明从应用容器那边启动,包括url的应用
				mClassName = bundle.getString("className");
				mAppId = bundle.getString("appId");
				mAppName = bundle.getString("appName", "");
				mUrl = bundle.getString("url", "");
				if (TextUtils.isEmpty(mClassName) || TextUtils.isEmpty(mAppId)) {
					finish();
					return;
				}
			}
		}
		getWindow().setFormat(PixelFormat.RGBA_8888);
		HcAppState.getInstance().addActivity(this);
		HcAppState.getInstance().setAppOnStarted();

		setContentView(R.layout.activity_base_menu);
		// ((HcApplication)getApplication()).getRefWatcher().watch(this);
		mTopBarView = (TopBarView) findViewById(R.id.menu_top_bar);
		mParent = (FrameLayout) findViewById(R.id.menu_parent);
		mPermisstion = findViewById(R.id.menu_permisstion_parent);
		if (mIsMenuActivity) {
			HcUtil.updateButtonBar(this, mHighMenuId);
			mMenuInfo = ((HcApplication) getApplication())
					.getCurrentMenuInfo(checkIndexById(mHighMenuId));
			mTopBarView.setTitle(mMenuInfo.getAppName());
			mAppName = mMenuInfo.getAppName();
			if (HcConfig.getConfig().getFirstMenuSize() <= 1) {
				mTopBarView.setVisibility(View.GONE);
			}

			mPageFactory = new MenuPageFactory();
			if (mMenuInfo.getClouded())
				mPageFactory.initMenu("com.android.hcframe.menu.WebMenuPage");
			else {
				mPageFactory.initMenu(mMenuInfo.getClassName());
			}
			mAppId = mMenuInfo.getAppId();
			mPageFactory.onCreate(mAppId, this, mParent);

		} else {
			mTopBarView.setReturnBtnVisiable(View.VISIBLE);
			mTopBarView.setTitle(mAppName);
			findViewById(R.id.menubar_parent).setVisibility(View.GONE);


			
			mPageFactory = new MenuPageFactory();
			mPageFactory.initMenu(mClassName); // com.android.hcframe.menu.WebMenuPage 这个不支持
			mPageFactory.onCreate(mAppId, this, mParent);

			/**
			 * @author jrjin
			 * @date 2016-2-23 下午4:36:27
			 * 重新写返回按钮的事件
			 */
			mTopBarView.setReturnViewListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
//					if ("com.android.hcframe.menu.WebMenuPage".equals(mClassName)) {
//						mPageFactory.onActivityResult(MenuWebPage.ON_RETURN_CODE, 1, null);
//					} else {
//						HcAppState.getInstance().removeActivity(MenuBaseActivity.this);
//						LogManager.getInstance().updateLog(MenuBaseActivity.this, false);
//						finish();
//					}
					HcAppState.getInstance().removeActivity(MenuBaseActivity.this);
					LogManager.getInstance().updateLog(MenuBaseActivity.this, false);
					finish();

				}
			});
			if ("com.android.hcframe.menu.WebMenuPage".equals(mClassName)) {
				mTopBarView.setReturnBtnIcon(R.drawable.center_close);
			}

		}

		mTopBarView.setMenuListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				HcLog.D(TAG + " TopBarView on right btn click! v = " + v);

			}
		});
		
		mNetworkError = findViewById(R.id.menu_show_networkerror);
		mNetworkError.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startSetting();
			}
		});
		
		/**
		 * @author jrjin
		 * @date 2016-2-23 下午4:29:46
		 */
		LogManager.getInstance().addLog(mAppName, mAppId, LogManager.TYPE_MODULE, this);
	}

	private void startSetting() {
		Intent intent = new Intent();
		//判断手机系统的版本  即API大于10 就是3.0或以上版本 
		if (android.os.Build.VERSION.SDK_INT > 10) {
			intent.setAction(android.provider.Settings.ACTION_SETTINGS);
		} else {
			intent.setClassName("com.android.settings","com.android.settings.WirelessSettings");
//			ComponentName component = new ComponentName("com.android.settings","com.android.settings.WirelessSettings");
//            intent.setComponent(component);
            intent.setAction("android.intent.action.VIEW");
		}
		startActivity(intent);
	}
	
	private int checkIndexById(int id) {
		int index = -1;
		if (id == R.id.menu1) {
			index = 0;
		} else if (id == R.id.menu2) {
			index = 1;
		} else if (id == R.id.menu3) {
			index = 2;
		} else if (id == R.id.menu4) {
			index = 3;
		} else if (id == R.id.menu5) {
			index = 4;
		}
		/*
		 * switch (id) { case R.id.menu1: index = 0; break; case R.id.menu2:
		 * index = 1; break; case R.id.menu3: index = 2; break; case R.id.menu4:
		 * index = 3; break; case R.id.menu5: index = 4; break;
		 * 
		 * default: break; }
		 */
		return index;
	}

	private long mExitTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (!mIsMenuActivity) { // 应用容器
			switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {

					if ("com.android.hcframe.menu.WebMenuPage".equals(mClassName)) {
						mPageFactory.onActivityResult(MenuWebPage.ON_KEY_DOWN_CODE, 1, null);
						return true;
					} else {
						/**
						 * @author jrjin
						 * @date 2016-2-23 下午1:43:54
						 * 退出应用模块
						 */
						LogManager.getInstance().updateLog(this, false);

						HcAppState.getInstance().removeActivity(this);
					}

				}
				break;

			default:
				break;
			}
			return super.onKeyDown(keyCode, event);
		}
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (keyCode == KeyEvent.KEYCODE_BACK
					&& event.getAction() == KeyEvent.ACTION_DOWN) {
				if ((System.currentTimeMillis() - mExitTime) > 2000) {
					HcUtil.showToast(getApplicationContext(), getResources()
							.getString(R.string.application_exit));
					HcLog.D(TAG + "it is onKeyDown! first down......");
					mExitTime = System.currentTimeMillis();
				} else {
					
					/**
					 * @author jrjin
					 * @date 2016-2-23 上午10:16:20
					 * 退出应用的日志
					 */
					LogManager.getInstance().updateLog(this, true);


					CacheManager.getInstance().clearCaches(true);

					/**
					 * @date 2016-1-28 下午12:33:43
					 * 放到DocCacheData.getInstance().clearDocCache()处理
					OperateDatabase.insertDataRecords(this, DocCacheData
							.getInstance().getHistoricalRecords());
					*/
					/**
					 * @author jrjin
					 * @date 2016-1-28 上午10:31:13
					List<AppInfo> mAppInfos = HcAppData.getInstance()
							.getAppInfos();
					for (AppInfo appInfo : mAppInfos) {
						HcLog.D(TAG + " onKeyDown appInfo id = "
								+ appInfo.getAppId() + " app name ="
								+ appInfo.getAppName() + " order = "
								+ appInfo.getAllOrder()
								+ " category order = "
								+ appInfo.getCategoryOrder());
					}
					OperateDatabase.insertAppsOnDestory(mAppInfos, this);
					mAppInfos.clear();
					*/

					boolean loginAuto = SettingHelper.getLoginAuto(this);
					if (!loginAuto) {
						SettingHelper.setUserId(this, "");
						SettingHelper.setToken(this, "");
						SettingHelper.setAccount(this, "");
						SettingHelper.setIcon(this, "");
						SettingHelper.setMobile(this, "");
						SettingHelper.setName(this, "");
						/**
						 *@author jinjr
						 *@date 17-3-16 上午9:16
						 */
						SettingHelper.setEmail(this, "");

						// 如果有IM模块,需要退出登录...
					}
					/**
					 *@author jinjr
					 *@date 16-9-18 下午2:46
					 * 直接换成下面的HcAppState.getInstance().finishAllActivities();
					 * 角标的存储需要测试
					HcAppState.getInstance().removeActivity(this);
					finish();
					System.exit(0);
					 */
					HcAppState.getInstance().finishAllActivities();

					HcLog.D(" pid = " + Process.myPid());

				}
				return true;
			}
			break;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}


	public void startHtmlActivity(String title, String url) {
		Intent intent = new Intent(this, HtmlActivity.class);
		intent.putExtra("title", title);
		intent.putExtra("url", url);
		intent.putExtra("mAppId", mMenuInfo != null ? mMenuInfo.getAppId()
				: mAppId);
		startActivity(intent);
		overridePendingTransition(0, 0);
		// overridePendingTransition(R.anim.wallpaper_intra_open_enter,
		// R.anim.wallpaper_intra_open_exit);
	}

	@Override
	protected void onResume() {
		super.onResume();
		HcLog.D(TAG + " it is onResume! this = " + this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(getPackageName() + NETWORK_ACTION);
		registerReceiver(mReceiver, filter);
		networkChanged();

		/**
		 * 增加权限控制
		 * @author jinjr
		 * @date 16-3-21 下午2:23
		 *
		 */
		if (HcConfig.getConfig().assertOperate(mAppId)) {
			if (mParent.getVisibility() != View.VISIBLE) {
				mParent.setVisibility(View.VISIBLE);
			}
			if (mPermisstion.getVisibility() != View.GONE) {
				mPermisstion.setVisibility(View.GONE);
			}
			mPageFactory.onResume();
		} else {
			if (mParent.getVisibility() != View.GONE) {
				mParent.setVisibility(View.GONE);
			}
			if (mPermisstion.getVisibility() != View.VISIBLE) {
				mPermisstion.setVisibility(View.VISIBLE);
			}

			if (TextUtils.isEmpty(SettingHelper.getAccount(this))) {
				// 用户需要登录
				startLoginActivity();
			}
		}


	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		unregisterReceiver(mReceiver);
		mPageFactory.onPause();
		super.onPause();
		HcLog.D(TAG + " it is onPause! this = " + this);
		HcLog.D(TAG + " #onPause end time = "+HcUtil.getDate(HcUtil.FORMAT_HOUR, System.currentTimeMillis()));
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		HcAppState.getInstance().removeActivity(this);
		BadgeCache.getInstance().checkBadges(this, mAppId);
		if (mPageFactory != null) {
			mPageFactory.onDestory();
			mPageFactory = null;
		}

		if (mParent != null) {
			mParent.removeAllViews();
			mParent = null;
		}


		mTopBarView = null;

		super.onDestroy();
		HcLog.D(TAG + " it is onDestroy! this = " + this);

		
	}

	public void startPDFActivity(String url, String title) {
		Intent intent = new Intent(this, DownloadPDFActivity.class);
		intent.putExtra("url", url);
		intent.putExtra("title", title);
		startActivity(intent);
		overridePendingTransition(0, 0);
	}

	public void startHtmlActivity(String url) {
		Intent intent = new Intent(this, HtmlActivity.class);
		intent.putExtra("title", mMenuInfo != null ? mMenuInfo.getAppName()
				: mAppName);
		intent.putExtra("url", url);
		intent.putExtra("mAppId", mMenuInfo != null ? mMenuInfo.getAppId()
				: mAppId);
		startActivity(intent);
		overridePendingTransition(0, 0);
		// overridePendingTransition(R.anim.wallpaper_intra_open_enter,
		// R.anim.wallpaper_intra_open_exit);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		HcLog.D(TAG + " requestCode = " + requestCode + " resultCode = "
				+ resultCode + " intent = " + data);
		if (requestCode == HcUtil.REQUEST_CODE_LOGIN) {
			// 失败跳转界面
			// 成功onresum处理
			if (resultCode == HcUtil.LOGIN_SUCCESS)
			/**
			 * @jrjin
			 * @2016-05-13 16:00
			 * 说明,本来这句不需要调用,但MenuWebPage里面需要处理;
			 * AnnualHomeView里面也需要处理,需要测试,使用的时候需要注意.
			 */
				mPageFactory.onActivityResult(requestCode, resultCode, data);// 在onresume里处理
			else {
				if (this instanceof ContainerActivity) { // 类似通讯录模块从应用容器里面进入
					HcAppState.getInstance().removeActivity(this);
					finish();
				} else if (this instanceof MenuBaseActivity) {
					HcUtil.startPreActivity(this);//登录取消之后跳转到之前的tab
					// 要是没有前面的tab？ 那就还是调用onResume方法,类似一个死循环.
				} else {
					HcAppState.getInstance().removeActivity(this);
					finish();
				}

			}
		} else {
			mPageFactory.onActivityResult(requestCode, resultCode, data);
		}
	}

	public String getMenuUrl() {
		return mMenuInfo != null ? mMenuInfo.getAppUrl() :
				TextUtils.isEmpty(mUrl) ? "" : mUrl;
	}

	public String getMenuId() {
		return mMenuInfo != null ? mMenuInfo.getAppId() : "";
	}

	public String getMenuAppName() {
		return mMenuInfo != null ? mMenuInfo.getAppName() : "";
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		HcLog.D(TAG + " it is onStop! this = " + this);
		HcLog.D(TAG + " #onStop end time = "+HcUtil.getDate(HcUtil.FORMAT_HOUR, System.currentTimeMillis()));
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			HcLog.D(TAG + " action = "+intent.getAction() + " network available = "+HcUtil.isNetWorkAvailable(context));
			if (action.equals(context.getPackageName() + NETWORK_ACTION)) {
				networkChanged();
			}
		}
	};
	
	private void networkChanged() {
		if (HcUtil.isNetWorkAvailable(this)) {
			if (mNetworkError.getVisibility() == View.VISIBLE)
				mNetworkError.setVisibility(View.GONE);
		} else {
			if (mNetworkError.getVisibility() != View.VISIBLE)
				mNetworkError.setVisibility(View.VISIBLE);
		}
	}

	private void startLoginActivity() {
		Intent login = new Intent(this, LoginActivity.class);
		login.putExtra("loginout", false);
		startActivityForResult(login, HcUtil.REQUEST_CODE_LOGIN);
		overridePendingTransition(0,0);
	}

	public void setTitle(String title) {
		if (!mIsMenuActivity) {
			if (mTopBarView != null) {
				mTopBarView.setTitle(TextUtils.isEmpty(title) ? mAppName : title);
			}
		}
	}
}
