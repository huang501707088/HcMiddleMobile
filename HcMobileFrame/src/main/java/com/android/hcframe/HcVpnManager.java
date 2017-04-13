/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-5-17 上午10:31:08
 */
package com.android.hcframe;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.android.hcframe.sql.SettingHelper;
/*
import com.sangfor.ssl.IVpnDelegate;
import com.sangfor.ssl.SFException;
import com.sangfor.ssl.SangforAuth;
import com.sangfor.ssl.common.VpnCommon;
import com.sangfor.ssl.service.setting.SystemConfiguration;
*/
import android.app.Activity;
import android.content.Intent;
import android.os.Process;

/*  先隐藏VPN功能 */
public final class HcVpnManager /*implements IVpnDelegate*/ {

	private static final String TAG = "HcVpnManager";

	private static final HcVpnManager VPN_MANAGER = new HcVpnManager();

	private static final String VPN_IP = "220.191.211.39";
	private static final int VPN_PORT = 443;
	private String USER = "app2";
	private String PASSWD = "sangfor@2015";

	private InetAddress mAddress = null;

	/*private SangforAuth mSangforAuth;*/

	private VpnCallback mVpnCallback;

	private HcVpnManager() {
		/*mSangforAuth = SangforAuth.getInstance();*/
	}

	public static final HcVpnManager getVpnManager() {
		return VPN_MANAGER;
	}

	public String getVPNUser() {
		return USER;
	}
	
	public void initVpn(Activity context, VpnCallback callback) {
		mVpnCallback = callback;

		if (HcUtil.isEmpty(SettingHelper.getVpnAccount(context))) {
			 USER = HcConfig.getConfig().getVpnAccount();
			 PASSWD = HcConfig.getConfig().getVpnPw();
		} else {
			USER = SettingHelper.getVpnAccount(context);
			PASSWD = SettingHelper.getVpnPwd(context);
		}
		
		/*
		try {
//			mSangforAuth.init(context, this, SangforAuth.AUTH_MODULE_EASYAPP);
			mSangforAuth.init(context, this, SangforAuth.AUTH_MODULE_L3VPN);
			mSangforAuth
					.setLoginParam(AUTH_CONNECT_TIME_OUT, String.valueOf(3));
		} catch (SFException e) {
			// TODO Auto-generated catch block
			HcLog.D(TAG + " initVpn SFException e = " + e);
		}

		initSslVpn();
		*/
	}

	/**
	 * 开始初始化VPN，该初始化为异步接口，后续动作通过回调函数通知结果
	 * 
	 * @return 成功返回true，失败返回false，一般情况下返回true
	 */
	private boolean initSslVpn() {
		/*
		SangforAuth sfAuth = SangforAuth.getInstance();

		mAddress = null;

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mAddress = InetAddress.getByName(VPN_IP);
					HcLog.D(TAG + "ip Addr is : " + mAddress.getHostAddress());
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					HcLog.D(HcVpnManager.TAG
							+ " initSslVpn UnknownHostException e = " + e);
				}
			}
		});
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (mAddress == null || mAddress.getHostAddress() == null) {
			HcLog.D(TAG + " vpn host error!");
			return false;
		}
		long host = VpnCommon.ipToLong(mAddress.getHostAddress());
		int port = VPN_PORT;

		if (sfAuth.vpnInit(host, port) == false) {
			HcLog.D(TAG + "vpn init fail, errno is " + sfAuth.vpnGeterr());
			return false;
		}
		*/
		return true;

	}
	/*
	@Override
	public void reloginCallback(int status, int result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void vpnCallback(int vpnResult, int authType) {
		// TODO Auto-generated method stub
		switch (vpnResult) {
		case IVpnDelegate.RESULT_VPN_INIT_FAIL:

			// 初始化vpn失败
			HcLog.D(" RESULT_VPN_INIT_FAIL, error is = "
					+ mSangforAuth.vpnGeterr());
			HcUtil.showToast(HcApplication.getContext(), "VPN初始化失败！");
			if (mVpnCallback != null) {
				mVpnCallback.setStatus(HcUtil.VPN_INIT_FAILED);
			}
			break;

		case IVpnDelegate.RESULT_VPN_INIT_SUCCESS:

			// 初始化vpn成功，接下来就需要开始认证工作了
			HcLog.D(TAG + "RESULT_VPN_INIT_SUCCESS, current vpn status is "
					+ mSangforAuth.vpnQueryStatus());
			HcUtil.showToast(HcApplication.getContext(), " VPN初始化成功！");
			// 初始化成功，进行认证操作
			doVpnLogin(IVpnDelegate.AUTH_TYPE_PASSWORD);

			break;

		case IVpnDelegate.RESULT_VPN_AUTH_FAIL:

			// 认证失败，有可能是传入参数有误，具体信息可通过sfAuth.vpnGeterr()获取
			String errString = mSangforAuth.vpnGeterr();
			HcLog.D(TAG + "RESULT_VPN_AUTH_FAIL, error is " + errString);
			HcUtil.showToast(HcApplication.getContext(), "VPN认证失败！");
			if (mVpnCallback != null) {
				mVpnCallback.setStatus(HcUtil.VPN_AUTH_FAILED);
			}
			break;

		case IVpnDelegate.RESULT_VPN_AUTH_SUCCESS:

			// 认证成功，认证成功有两种情况，一种是认证通过，可以使用sslvpn功能了，另一种是前一个认证（如：用户名密码认证）通过，
			// 但需要继续认证（如：需要继续证书认证）
			if (authType == IVpnDelegate.AUTH_TYPE_NONE) {
				HcLog.D(TAG + "welcom to sangfor sslvpn!");
				HcUtil.showToast(HcApplication.getContext(), "VPN认证成功！");
//				if (mVpnCallback != null) {
//					mVpnCallback.setStatus(HcUtil.VPN_L3VPN_SUCCESS);
//				}
				// 若为L3vpn流程，认证成功后开启自动开启l3vpn服务
				if (SangforAuth.getInstance().getModuleUsed() == SangforAuth.AUTH_MODULE_EASYAPP) {
					// EasyApp流程，认证流程结束，可访问资源。

				}

			} else {
				HcLog.D(TAG
						+ "auth success, and need next auth, next auth type is "
						+ authType);

				if (authType == IVpnDelegate.AUTH_TYPE_SMS) {
					// 输入短信验证码

				} else {
					doVpnLogin(authType);
				}
			}
			break;
		case IVpnDelegate.RESULT_VPN_AUTH_CANCEL:
			HcLog.D(TAG + " RESULT_VPN_AUTH_CANCEL");
			break;
		case IVpnDelegate.RESULT_VPN_AUTH_LOGOUT:

			// 主动注销（自己主动调用logout接口）或者被动注销（通过控制台把用户踢掉）均会调用该接口
			HcLog.D(TAG + " RESULT_VPN_AUTH_LOGOUT");
			HcUtil.showToast(HcApplication.getContext(), "VPN退出成功！");
			if (mVpnCallback != null) {
				mVpnCallback.setStatus(HcUtil.VPN_AUTH_LOGOUT);
			}
			// System.exit(0);
			LogoutActivity.finishTask();
			Process.killProcess(Process.myPid());
			break;
		case IVpnDelegate.RESULT_VPN_L3VPN_FAIL:

			// L3vpn启动失败，有可能是没有l3vpn资源，具体信息可通过sfAuth.vpnGeterr()获取
			HcLog.D(TAG + " RESULT_VPN_L3VPN_FAIL, error is "
					+ mSangforAuth.vpnGeterr());
			HcUtil.showToast(HcApplication.getContext(), "L3VPN启动失败！");
			if (mVpnCallback != null) {
				mVpnCallback.setStatus(HcUtil.VPN_L3VPN_FAILED);
			}
			break;
		case IVpnDelegate.RESULT_VPN_L3VPN_SUCCESS:

			// L3vpn启动成功
			HcLog.D(TAG + " RESULT_VPN_L3VPN_SUCCESS ===== "
					+ SystemConfiguration.getInstance().getSessionId());
			HcUtil.showToast(HcApplication.getContext(), "L3VPN启动成功!");
			// L3vpn流程，认证流程结束，可访问资源。
			if (mVpnCallback != null) {
				mVpnCallback.setStatus(HcUtil.VPN_L3VPN_SUCCESS);
			}
			break;
		case IVpnDelegate.VPN_STATUS_ONLINE:

			// 与设备连接建立
			HcLog.D(TAG + " IVpnDelegate.VPN_STATUS_ONLINE online");
//			HcUtil.showToast(HcApplication.getContext(), "与设备连接建立!");
			break;
		case IVpnDelegate.VPN_STATUS_OFFLINE:

			// 与设备连接断开
			HcLog.D(TAG + " IVpnDelegate.VPN_STATUS_OFFLINE offline");
//			HcUtil.showToast(HcApplication.getContext(), "与设备断开连接！");
			break;
		default:

			// 其它情况，不会发生，如果到该分支说明代码逻辑有误
			HcLog.D(TAG + "default result, vpn result is " + vpnResult);
			break;
		}
	}

	@Override
	public void vpnRndCodeCallback(byte[] data) {
		// TODO Auto-generated method stub

	}
	*/

	/**
	 * 处理认证，通过传入认证类型（需要的话可以改变该接口传入一个hashmap的参数用户传入认证参数）.
	 * 也可以一次性把认证参数设入，这样就如果认证参数全满足的话就可以一次性认证通过，可见下面屏蔽代码
	 * 
	 * @param authType
	 *            认证类型
	 *
	 */
	/* @throws SFException */
	private void doVpnLogin(int authType) {
		HcLog.D(TAG + " doVpnLogin authType " + authType);
		/*
		boolean ret = false;
		switch (authType) {
		case IVpnDelegate.AUTH_TYPE_CERTIFICATE:
			mSangforAuth.setLoginParam(IVpnDelegate.CERT_PASSWORD, PASSWD);
			mSangforAuth.setLoginParam(IVpnDelegate.CERT_P12_FILE_NAME, USER);
			ret = mSangforAuth.vpnLogin(IVpnDelegate.AUTH_TYPE_CERTIFICATE);
			break;
		case IVpnDelegate.AUTH_TYPE_PASSWORD:
			mSangforAuth.setLoginParam(IVpnDelegate.PASSWORD_AUTH_USERNAME,
					USER);
			mSangforAuth.setLoginParam(IVpnDelegate.PASSWORD_AUTH_PASSWORD,
					PASSWD);
			// mSangforAuth.setLoginParam(IVpnDelegate.SET_RND_CODE_STR,
			// rndcode);
			ret = mSangforAuth.vpnLogin(IVpnDelegate.AUTH_TYPE_PASSWORD);
			break;
		case IVpnDelegate.AUTH_TYPE_SMS:
			ret = mSangforAuth.vpnLogin(IVpnDelegate.AUTH_TYPE_SMS);
			break;
		case IVpnDelegate.AUTH_TYPE_SMS1:
			ret = mSangforAuth.vpnLogin(IVpnDelegate.AUTH_TYPE_SMS1);
			break;
		default:
			HcLog.D(TAG + " default authType " + authType);
			break;
		}

		if (ret == true) {
			HcLog.D(TAG + " success to call login method");
		} else {
			HcLog.D(TAG + " fail to call login method");
		}
		*/
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		/*switch (requestCode) {
		case IVpnDelegate.REQUEST_L3VPNSERVICE:
			if (resultCode == Activity.RESULT_OK) {
				HcLog.D(TAG + " onActivityResult resultCode == RESULT_OK");
				SangforAuth.getInstance().vpnL3vpnStart();
			}
		}*/
	}

	public boolean vpnLogout() {
		/*if (HcConfig.getConfig().vpnEnable())
			return mSangforAuth.vpnLogout();
		else {
			return false;
		}*/
		return false;
	}

	public interface VpnCallback {

		public void setStatus(int status);
	}
}
