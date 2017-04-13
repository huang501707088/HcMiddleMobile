/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-6-3 下午2:14:00
 */
package com.android.hcframe;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.hcframe.menu.MenuInfo;
import com.android.hcframe.sql.SettingHelper;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils;

public final class HcConfig {

	private static final String TAG = "HcConfig";

	private static final HcConfig CONFIG = new HcConfig();

	private List<MenuInfo> mMenus = new ArrayList<MenuInfo>();

	private List<HashMap<String, String>> introbgs = new ArrayList<HashMap<String, String>>();

	private String ProdMemo;

	private String mVersionName;

	private MenuStyle mMenuStyle;

	private VpnInfo mVpnInfo;

	private ColorStateList mTextColorStateList;

	private ColorStateList mItemColorStateList;

	private StateListDrawable mItemBackgrounDrawable;

	private ColorDrawable mNormalColorDrawable;

	private ColorDrawable mSelctColorDrawable;
	/** 服务端打包发布的应用版本，可能并不是manifest里面的应用版本 */
	private String mVersion;

	private ServerInfo mServerInfo;

	/** 是否启用百度推送 */
	private boolean mPushed = true;

	/** 软件版权 */
	private String mCopyright;

	/** 用户模块配置信息 */
	private AccountInfo mAccountInfo;

	/**
	 * 用户权限列表的AppId
	 */
	private List<String> mAppIds;

	/** 打包到配置文件里的权限列表 */
	private String mPermisstion;

	/** 应用程序的ID,打包的时候写入到配置文件中 */
	private String mClientId;

	/** 角标应用列表数据 */
	private String mBadges;

	private ContactsInfo mContactsInfo;

	/** 存储一些模块的独立的IP和port
	 *  key:模块的name,配置文件中定义 */
	private Map<Module, ServerInfo> mServers = new HashMap<Module, ServerInfo>();

	/** 存储打包到本地的应用模块 */
	private List<AppConfigure> mAppConfigures = new ArrayList<AppConfigure>();

	private HcConfig() {
		mMenuStyle = new MenuStyle();
		mVpnInfo = new VpnInfo();
		mServerInfo = new ServerInfo();
		mAccountInfo = new AccountInfo();
		mAppIds = new ArrayList<String>();
		mContactsInfo = new ContactsInfo();
	}

	public static HcConfig getConfig() {
		return CONFIG;
	}

	public void parseConfig(Context context) {
		StringBuilder builder = new StringBuilder();
		InputStream is = null;
		BufferedReader bufferedReader = null;
		try {
			is = context.getAssets().open("configure.json");
			bufferedReader = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				builder.append(line);
			}

		} catch (Exception e) {
			// TODO: handle exception
			HcLog.D(TAG + " error = " + e);
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (Exception e2) {
					// TODO: handle exception
				}

			}
			if (is != null) {
				try {
					is.close();
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}
		}
		HcLog.D(TAG + " json data = " + builder.toString());
		try {
			JSONObject config = new JSONObject(builder.toString());

			/** intro */
			if (hasValue(config, "IntroPage")) {
				JSONObject intro = config.getJSONObject("IntroPage");
				if (hasValue(intro, "IntroImages")) {
					JSONArray introImgs = intro.getJSONArray("IntroImages");
					for (int i = 0; i < introImgs.length(); i++) {
						JSONObject introImg = introImgs.getJSONObject(i);
						HashMap<String, String> introImgMap = new HashMap<String, String>();
						if (hasValue(introImg, "imageName")) {
							introImgMap.put("imageName",
									introImg.getString("imageName"));
						}
						if (hasValue(introImg, "title")) {
							introImgMap.put("title",
									introImg.getString("title"));
						}
						if (hasValue(introImg, "detailText")) {
							introImgMap.put("detailText",
									introImg.getString("detailText"));
						}
						introbgs.add(introImgMap);
					}
				}
			}

			/** app id list */
			if (hasValue(config, "appIds")) {
//				mAppIds.addAll(Arrays.asList(config.getString("appIds").split(";")));
				mPermisstion = config.getString("appIds");
			}

			if (mPermisstion == null) {
				mPermisstion = "";
			}

			/** 角标应用列表 */
			if (hasValue(config, "badgeIds")) {
				mBadges = config.getString("badgeIds");
			}

			/** VPN Info */
			/** not used
			JSONObject vpn = config.getJSONObject("VPN");
			if (hasValue(vpn, "isUsed")) {
				mVpnInfo.mVpnEnable = vpn.getString("isUsed").equals("true");
			}

			if (hasValue(vpn, "account")) {
				mVpnInfo.mVpnAccount = vpn.getString("account");
			}
			if (hasValue(vpn, "pw")) {
				mVpnInfo.mVpnPw = vpn.getString("pw");
			}*/

			JSONObject vpn;
			/** 个人中心 */
			if (hasValue(config, "personCenter")) {
				vpn = config.getJSONObject("personCenter");
				if (hasValue(vpn, "CanRegister")) {
					mAccountInfo.mCanRegister = vpn.getString("CanRegister").equals("true");
				}
				if (hasValue(vpn, "regetPwd")) {
					mAccountInfo.mCanForgetPw = vpn.getString("regetPwd").equals("true");
				}
				if (hasValue(vpn, "updatePwd")) {
					mAccountInfo.mCanModifyPw = vpn.getString("updatePwd").equals("true");
				}
				if (hasValue(vpn, "updateMobile")) {
					mAccountInfo.mCanBindPhone = vpn.getString("updateMobile").equals("true");
				}
			}

			/** 通讯录 */
			if (hasValue(config, "contacts")) {
				vpn = config.getJSONObject("contacts");
				if (hasValue(vpn, "secrecy")) {
					mContactsInfo.mSecrecy = "true".equals(vpn.getString("secrecy"));
				}
			}

			/** IM */
			parseModule(config, "im", Module.IM);

			/** 云盘 */
			parseModule(config, "clouddisk", Module.CLOUD_DISK);

			/** 邮箱 */
			parseMailModule(config);

			/** 应用模块 */
			if (hasValue(config, "nativeAppConfigure")) {
				JSONArray array = config.getJSONArray("nativeAppConfigure");
				int length = array.length();
				for (int i = 0; i < length; i++) {
					parseAppConfigure(array.getJSONObject(i));
				}
			}

			/** 推送 */
			if (hasValue(config, "Push")) {
				vpn = config.getJSONObject("Push");
				if (hasValue(vpn, "isUsed")) {
					mPushed = "true".equals(vpn.getString("isUsed"));
				}
			}

			/** Version Info */
			vpn = config.getJSONObject("buildConfigure")
					.getJSONObject("common");
			if (hasValue(vpn, "Name")) {
				mVersionName = vpn.getString("Name");
			}
			if (hasValue(vpn, "Version")) {
				mVersion = vpn.getString("Version");
			}
			if (hasValue(vpn, "ProdMemo")) {
				ProdMemo = vpn.getString("ProdMemo");
			}
			if (hasValue(vpn, "ProdMemo")) {
				mCopyright = vpn.getString("ProdMemo");
			}
//			if (hasValue(vpn, "CanRegister")) {
//				mCanRegister = vpn.getString("CanRegister").equals("true");
//			}

			if (hasValue(vpn, "clientId")) {
				mClientId = vpn.getString("clientId");
				HcLog.D("HcConfig #client id ============================= "+mClientId);
			}

			/** Server Info */
			vpn = config.getJSONObject("buildConfigure").getJSONObject(
					"Publish");
			if (hasValue(vpn, "extranetServerURL")) {
				mServerInfo.mExtranetServerURL = vpn
						.getString("extranetServerURL");
			}
			if (hasValue(vpn, "intranetServerURL")) {
				mServerInfo.mIntranetServerURL = vpn
						.getString("intranetServerURL");
			}
			if (hasValue(vpn, "mapped")) {
				mServerInfo.hasMapped = vpn.getString("mapped").equals("true");
			}
			if (hasValue(vpn, "serverPort")) {
				mServerInfo.mPort = vpn.getString("serverPort");
			}

			/** Menu Info */
			JSONObject object = config.getJSONObject("MenuPattern");

			if (hasValue(object, "level1")) {
				JSONObject menu = object.getJSONObject("level1");
				boolean isIconVisibility = true;
				boolean isNameVisibility = true;
				if (hasValue(menu, "isShowImage")) {
					isIconVisibility = menu.getString("isShowImage").equals(
							"true");
					mMenuStyle.isShowIcon = isIconVisibility;
				}
				if (hasValue(menu, "isShowTitle")) {
					isNameVisibility = menu.getString("isShowTitle").equals(
							"true");
					mMenuStyle.isShowTitle = isNameVisibility;
				}
				if (hasValue(menu, "normalTextColor")) {
					mMenuStyle.mTextNormalColor = Long.decode(menu
							.getString("normalTextColor"));
				}
				if (hasValue(menu, "selectTextColor")) {
					mMenuStyle.mTextSelectColor = Long.decode(menu
							.getString("selectTextColor"));
				}
				if (hasValue(menu, "backgroundColor")) {
					mMenuStyle.mMenuBarBackgroundColor = Long.decode(menu
							.getString("backgroundColor"));
				}
				if (hasValue(menu, "normalItemColor")) {
					mMenuStyle.mMenuItemNormalColor = Long.decode(menu
							.getString("normalItemColor"));
				}
				if (hasValue(menu, "selectItemColor")) {
					mMenuStyle.mMenuItemSelectColor = Long.decode(menu
							.getString("selectItemColor"));
				}

				if (hasValue(menu, "menu")) {
					JSONArray array = menu.getJSONArray("menu");
					int size = array.length();
					JSONObject item = null;
					MenuInfo info = null;
					HcLog.D(TAG + " array size = " + size);
					for (int i = 0; i < size; i++) {
						item = array.getJSONObject(i);
						info = new MenuInfo();
						info.setIconVisibility(isIconVisibility);
						info.setNameVisibility(isNameVisibility);
						if (hasValue(item, "id")) {
							info.setAppId(item.getString("id"));
						}
						if (hasValue(item, "title")) {
							info.setAppName(item.getString("title"));
						}
						if (hasValue(item, "url")) {
							info.setAppIndexUrl(item.getString("url"));
						}
						if (hasValue(item, "image")) {
							info.setAppNormalIcon(item.getString("image"));
						}
						if (hasValue(item, "selimage")) {
							info.setAppSelectIcon(item.getString("selimage"));
						}
						if (hasValue(item, "isCloud")) {
							info.setClouded(item.getString("isCloud").equals(
									"true"));
						}
						if (hasValue(item, "className")) {
							info.setClassName(item.getString("className"));
						}
						mMenus.add(info);
					}
				}
			}
		} catch (JSONException e) {
			// TODO: handle exception
			HcLog.D(TAG + " json error = " + e);
		}

		HcLog.D(TAG + " menu size = " + mMenus.size());
	}

	/**
	 * 判断对应的key是否存在value
	 * 
	 * @param object
	 * @param tag
	 * @return true:有数据；false：没有数据
	 */
	private boolean hasValue(JSONObject object, String tag) {
		boolean exist = false;
		if (object != null && object.has(tag)) {
			try {
				Object object2 = object.get(tag);
				// LOG.D(TAG + " object2 = "+object2 + " tag = "+tag);
				if (object2 != null && !object2.equals("")
						&& !object.isNull(tag)) {
					exist = true;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}

		}
		return exist;
	}

	public String getVpnAccount() {
		return mVpnInfo.mVpnAccount;
	}

	public String getVpnPw() {
		return mVpnInfo.mVpnPw;
	}

	/**
	 * 获取一级菜单
	 * 
	 * @author jrjin
	 * @time 2015-11-2 下午2:01:42
	 * @return 一级菜单
	 */
	public List<MenuInfo> getFirstMenus() {
		return mMenus;
	}

	/**
	 * 获取一级菜单的大小
	 * 
	 * @author jrjin
	 * @time 2015-11-2 下午2:02:05
	 * @return 一级菜单的大小
	 */
	public int getFirstMenuSize() {
		return mMenus.size();
	}

	public MenuInfo getCurrentMenu(int index) {
		return mMenus.get(index);
	}

	public String getVersionName() {
		return mVersionName;
	}

	public boolean vpnEnable() {
		return false;// mVpnInfo.mVpnEnable;
	}

	/**
	 * 菜单的标题是否可见，菜单的图标和标题必须有一个可见
	 * 
	 * @author jrjin
	 * @time 2015-7-9 下午4:05:21
	 * @return true:可见;false:不可见
	 */
	public boolean getMenuTitleVisibility() {
		if (menuAssert())
			return true;
		return mMenuStyle.isShowTitle;
	}

	/**
	 * 菜单的图标是否可见，菜单的图标和标题必须有一个可见
	 * 
	 * @author jrjin
	 * @time 2015-7-9 下午4:05:21
	 * @return true:可见;false:不可见
	 */
	public boolean getMenuIconVisibility() {
		if (menuAssert())
			return true;
		return mMenuStyle.isShowIcon;
	}

	private class MenuStyle {
		/** 是否显示菜单标题 */
		boolean isShowTitle = true;
		/** 是否显示菜单图标 */
		boolean isShowIcon = true;

		long mTextNormalColor;
		long mTextSelectColor;
		long mMenuItemNormalColor;
		long mMenuItemSelectColor;
		long mMenuBarBackgroundColor;
	}

	/**
	 * 菜单的图标和标题必须有一个可见
	 * 
	 * @author jrjin
	 * @time 2015-7-9 下午4:05:21
	 * @return
	 */
	private boolean menuAssert() {
		return mMenuStyle.isShowIcon == mMenuStyle.isShowTitle;
	}

	private class VpnInfo {

		String mVpnAccount;
		String mVpnPw;
		/** Vpn是否可用 */
		boolean mVpnEnable = true;
	}

	public int getMenuBarBackgroundColor() {
		return (int) mMenuStyle.mMenuBarBackgroundColor;
	}

	public ColorStateList getMenuTitileColor() {
		HcLog.D(TAG + " getMenuTitileColor normal = "
				+ mMenuStyle.mTextNormalColor + " select = "
				+ mMenuStyle.mTextSelectColor);
		if (null == mTextColorStateList) {
			int[][] states = { HcUtil.PRESSED_STATE_SET,
					HcUtil.FOCUSED_STATE_SET, HcUtil.SELECTED_STATE_SET,
					HcUtil.EMPTY_STATE_SET };
			int[] colors = { (int) mMenuStyle.mTextSelectColor,
					(int) mMenuStyle.mTextSelectColor,
					(int) mMenuStyle.mTextSelectColor,
					(int) mMenuStyle.mTextNormalColor };
			mTextColorStateList = new ColorStateList(states, colors);
		}
		return mTextColorStateList;
	}

	public ColorStateList getMenuItemBackgroundColor() {
		if (null == mTextColorStateList) {
			int[][] states = { HcUtil.PRESSED_STATE_SET,
					HcUtil.FOCUSED_STATE_SET, HcUtil.SELECTED_STATE_SET,
					HcUtil.EMPTY_STATE_SET };
			int[] colors = { (int) mMenuStyle.mMenuItemSelectColor,
					(int) mMenuStyle.mMenuItemSelectColor,
					(int) mMenuStyle.mMenuItemSelectColor,
					(int) mMenuStyle.mMenuItemNormalColor };
			mItemColorStateList = new ColorStateList(states, colors);
		}
		return mItemColorStateList;
	}

	public StateListDrawable getMenuItemBackgroundDrawable() {
		if (null == mItemBackgrounDrawable) {
			if (null == mNormalColorDrawable) {
				mNormalColorDrawable = new ColorDrawable(
						(int) mMenuStyle.mMenuItemNormalColor);
			}
			if (null == mSelctColorDrawable) {
				mSelctColorDrawable = new ColorDrawable(
						(int) mMenuStyle.mMenuItemSelectColor);
			}
			mItemBackgrounDrawable = new StateListDrawable();
			mItemBackgrounDrawable.addState(HcUtil.SELECTED_STATE_SET,
					mSelctColorDrawable);
			mItemBackgrounDrawable.addState(HcUtil.PRESSED_STATE_SET,
					mSelctColorDrawable);
			// mItemBackgrounDrawable.addState(HcUtil.FOCUSED_WINDOW_FOCUSED_STATE_SET,
			// mSelctColorDrawable);
			// mItemBackgrounDrawable.addState(HcUtil.FOCUSED_WINDOW_UNFOCUSED_STATE_SET,
			// mNormalColorDrawable);
			mItemBackgrounDrawable.addState(HcUtil.EMPTY_STATE_SET,
					mNormalColorDrawable);
		}
		return mItemBackgrounDrawable;
	}

	public int getMenuItemNormalColor() {
		return (int) mMenuStyle.mMenuItemNormalColor;
	}

	public int getMenuItemSelectColor() {
		return (int) mMenuStyle.mMenuItemSelectColor;
	}

	public String getAppVersion() {
		return mVersion;
	}

	public static class ServerInfo {
		/** 是否需要映射 */
		public boolean hasMapped = false;
		/** 外网访问地址 */
		public String mExtranetServerURL;
		/** 内网访问地址 */
		public String mIntranetServerURL;
		/** 服务器端口 */
		public String mPort;
		/** 服务器类型, 用于邮箱模块 */
		public String mServerType;
		/** 服务器安全协议, 用于邮箱模块 */
		public String mSecurity;
	}

	public boolean ipMapped() {
//		return false;
		return mServerInfo.hasMapped;
	}

	public String getExtranetServerURL() {
		return mServerInfo.mExtranetServerURL;
	}

	public String getIntranetServerURL() {
		return mServerInfo.mIntranetServerURL;
	}

	public String getServerPort() {
		return mServerInfo.mPort;
	}

	public List<HashMap<String, String>> getIntrobgs() {
		return introbgs;
	}

	public boolean baiDuPushed() {
		return mPushed;
	}

	public String getProdMemo() {
		return ProdMemo;
	}

	public String getCopyright() {
		return mCopyright;
	}
	/**
	 * @author jrjin
	 * @time 2016-1-22 下午2:27:48
	 * @return 是否显示注册功能
	 * 
	 */
	public boolean canRegister() {
		return mAccountInfo.mCanRegister;
	}
	
	private class AccountInfo {
		
		private boolean mCanRegister = true;
		
		private boolean mCanModifyPw = true;
		
		private boolean mCanBindPhone = true;
		
		private boolean mCanForgetPw = true;
	}
	/**
	 * 是否有修改秘密的功能
	 * @author jrjin
	 * @time 2016-1-22 下午2:30:16
	 * @return
	 */
	public boolean canModifyPw() {
		return mAccountInfo.mCanModifyPw;
	}
	/**
	 * 是否有绑定手机号的功能
	 * @author jrjin
	 * @time 2016-1-22 下午2:30:31
	 * @return
	 */
	public boolean canBindPhone() {
		return mAccountInfo.mCanBindPhone;
	}
	/**
	 * 是否有忘记密码的功能
	 * @author jrjin
	 * @time 2016-1-22 下午2:30:46
	 * @return
	 */
	public boolean canForgetPw() {
		return mAccountInfo.mCanForgetPw;
	}

	/**
	 * 进入模块之前,要先判断是否有权限
	 * @param appId 应用的Id
	 * @return
	 */
	public boolean assertOperate(String appId) {
		HcLog.D(TAG + "#assertOperate appId = "+appId + " list = "+mAppIds.toString());
		return mAppIds.contains(appId);
	}

	/**
	 * 更新权限列表
	 * @param context
	 * @param first 当前版本是否第一次进入
	 */
	public void updatePermisstion(Context context, boolean first) {
		mAppIds.clear();
		if (first) {
			SettingHelper.setOperatePermisstion(context, mPermisstion);
			HcLog.D(TAG + " #updatePermisstion mPermisstion = " + mPermisstion);
			if (!TextUtils.isEmpty(mPermisstion))
				mAppIds.addAll(Arrays.asList(mPermisstion.split(";")));
		} else {
			String appIds = SettingHelper.getOperatePermisstion(context);
			HcLog.D(TAG + " #updatePermisstion appIds = "+appIds);
			if (!TextUtils.isEmpty(appIds))
				mAppIds.addAll(Arrays.asList(appIds.split(";")));
		}

		HcLog.D(TAG + " #updatePermisstion app size = "+mAppIds.size());
	}

	public String getClientId() {
		return mClientId;
	}

	public String getBadges() {
		return mBadges;
	}

	/**
	 * 通讯录模块的配置信息
	 */
	private class ContactsInfo {

		/** 是否保密 */
		boolean mSecrecy = false;
	}

	public boolean hasContactsSecrecy() {
		return mContactsInfo.mSecrecy;
	}

	/**
	 * 获取服务器的名字
	 * @param module 模块在配置文件中的名字,事先定义好
	 * @return
     */
	public String getServerName(Module module) {
		String name = "";
		ServerInfo info = mServers.get(module);
		if (info != null)
			name = info.mExtranetServerURL;
		return name;
	}
	/**
	 * 获取服务器的端口号
	 * @param module 模块在配置文件中的模块,事先定义好
	 * @return
	 */
	public int getServerPort(Module module) {
		int port = 8080;
		ServerInfo info = mServers.get(module);
		if (info != null)
			port = Integer.valueOf(info.mPort);
		return port;
	}

	public enum Module {
		/** 关于模块 */
		ABOUT("com.android.hcframe.internalservice.about.AboutMenuPage"),
		/** 年会模块 */
		ANNUAL("com.android.hcframe.internalservice.annual.AnnualMenuPage"),
		/** 手写签批 */
		APPROVE("com.android.hcframe.approve.ApprovePage"),
		/** 通讯录 */
		CONTACTS("com.android.hcframe.internalservice.contacts.ContactMenuPage"),
		/** 资料中心 */
		DOC("com.android.hcframe.doc.DocMenuPage"),
		/** 邮件系统 */
		MAIL("com.android.hcmail.HcEmailMenuPage"),
		/** 邮箱收件服务器,为了获取服务器配置信息 */
		MAIL_INCOMING(""),
		/** 邮箱发件服务器,为了获取服务器配置信息 */
		MAIL_OUTGOING(""),
		/** 任务模块*/
		TASK("com.android.hcframe.hctask.TaskMenuPage"),
		/** 即时通讯模块 */
		IM("com.android.hcframe.im.IMHomeMenuPage"),
		/** 应用超市模块 */
		MARKET("com.android.hcframe.market.MarketMenuPage"),
		/** 网盘模块 */
		CLOUD_DISK("com.android.hcframe.netdisc.NetdiscPage"),
		/** 新闻模块 */
		NEWS("com.android.hcframe.internalservice.news.NewsMenuPage"),
		/** 日程模块 */
		SCHEDULE("com.android.hcframe.schedule.SheduleMenuPage"),
		/** 签到考勤模块 */
		SIGN_IN("com.android.hcframe.internalservice.sign.SignMenuPage");

		private String mClassName;

		private Module(String className) {

			mClassName = className;
		}

		public String getClassName() {
			return mClassName;
		}

		public boolean isMatch(String className) {
			return mClassName.equals(className);
		}
	}

	private void parseModule(JSONObject config, String key, Module module) {
		try {
			if (hasValue(config, key)) {
				JSONObject moduleInfo = config.getJSONObject(key);
				String name = null;
				String port = null;
				if (hasValue(moduleInfo, "serverName")) {
					name = moduleInfo.getString("serverName");
				}
				if (hasValue(moduleInfo, "serverPort")) {
					port = moduleInfo.getString("serverPort");
				}
				if (name == null)
					name = "";
				if (port == null)
					port = "8080";
				ServerInfo server = new ServerInfo();
				server.mExtranetServerURL = name;
				server.mPort = port;
				mServers.put(module, server);
			} else {
				HcLog.D(TAG + "#parseModule by key = "+key + " has no value!");
			}
		} catch(Exception e) {
			HcLog.D(TAG + "#parseModule e = "+e);
		}

	}

	public static class AppConfigure {

		/** 应用模块ID */
		public String mAppId;
		/** 应用模块名称 */
		public String mAppName;
		/** 应用模块主页类名 */
		public String mClassName;
		/** 服务器地址 用于IM和网盘模块 */
		public String mServerName;
		/** 服务器端口 用于IM和网盘模块 */
		public String mPort;
		/** 模块是否保密 用于通讯录模块 */
		public boolean mSecrecy;
	}

	private void parseAppConfigure(JSONObject json) {
		try {
			if (hasValue(json, "className")) {
				AppConfigure configure = new AppConfigure();
				configure.mClassName = json.getString("className");
				if (hasValue(json, "appId")) {
					configure.mAppId = json.getString("appId");
				}
				if (hasValue(json, "appName")) {
					configure.mAppName = json.getString("appName");
				}
				if (hasValue(json, "serverName")) {
					configure.mServerName = json.getString("serverName");
				}
				if (hasValue(json, "serverPort")) {
					configure.mPort = json.getString("serverPort");
				}
				if (hasValue(json, "")) {
					configure.mSecrecy = "true".equals(json.getString("secrecy"));
				}

				mAppConfigures.add(configure);
			}
		} catch(Exception e) {
			HcLog.D(TAG + " #parseAppConfigure e = "+e);
		}

	}

	/**
	 * 判断该模块是否已经打包到APP里面
	 * @param module 需要判断的模块
	 * @return
     */
	public boolean assertModule(Module module) {
		for (AppConfigure configure : mAppConfigures) {
			if (module.isMatch(configure.mClassName))
				return true;
		}

		return false;
	}

	/**
	 * 获取应用模块的配置信息
	 * @param module
	 * @return null没有该模块
     */
	public AppConfigure getAppConfigure(Module module) {
		for (AppConfigure configure : mAppConfigures) {
			if (module.isMatch(configure.mClassName))
				return configure;
		}

		return null;
	}

	private void parseMailModule(JSONObject config) {
		try {
			if (hasValue(config, "email")) {
				JSONObject moduleInfo = config.getJSONObject("email");
				ServerInfo server;
				if (hasValue(moduleInfo, "incoming")) {
					JSONObject incoming = moduleInfo.getJSONObject("incoming");
					server = new ServerInfo();
					if (hasValue(incoming, "type")) {
						server.mServerType = incoming.getString("type");
					}
					if (hasValue(incoming, "serverName")) {
						server.mExtranetServerURL = incoming.getString("serverName");
					}
					if (hasValue(incoming, "serverPort")) {
						server.mPort = incoming.getString("serverPort");
					}
					if (hasValue(incoming, "security")) {
						server.mSecurity = incoming.getString("security");
					}
					mServers.put(Module.MAIL_INCOMING, server);
				}
				if (hasValue(moduleInfo, "outgoing")) {
					JSONObject outgoing = moduleInfo.getJSONObject("outgoing");
					server = new ServerInfo();
					if (hasValue(outgoing, "serverName")) {
						server.mExtranetServerURL = outgoing.getString("serverName");
					}
					if (hasValue(outgoing, "serverPort")) {
						server.mPort = outgoing.getString("serverPort");
					}
					if (hasValue(outgoing, "security")) {
						server.mSecurity = outgoing.getString("security");
					}
					mServers.put(Module.MAIL_OUTGOING, server);
				}

			} else {
				HcLog.D(TAG + "#parseMailModule email has no value!");
			}
		} catch(Exception e) {
			HcLog.D(TAG + "#parseMailModule e = "+e);
		}

	}

	public ServerInfo getServerInfo(Module module) {
		return mServers.get(module);
	}
}
