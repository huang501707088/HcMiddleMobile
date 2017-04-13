package com.android.hcframe;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.android.hcframe.command.CommandControl;
import com.android.hcframe.container.data.ContainerConfig;
import com.android.hcframe.menu.MenuInfo;
import com.android.hcframe.sql.SettingHelper;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;
//import com.squareup.leakcanary.LeakCanary;
//import com.squareup.leakcanary.watcher.RefWatcher;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.DisplayMetrics;

public class HcApplication extends Application {

	private static final String TAG = "HcApplication";

	private static Context mContext;
	private static String FILE_DIR = "zjhc";

	private static String mFileName = null;

	private HcConfig mConfig;

	private static File privateDir;	
	
//	private RefWatcher mWatcher;
	
//	private HcAppReceiver mReceiver;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
//		mContext = this;
//		mWatcher = LeakCanary.install(this);
		HcLog.D("HcApplication is create!!!!!! device Id = "+HcUtil.getIMEI(this) + " uid = "+Process.myUid() + " pid = "+Process.myPid());
		if (HcLog.DEBUG && Build.VERSION.SDK_INT >=
				Build.VERSION_CODES.GINGERBREAD) {
			StrictMode.setThreadPolicy(new
			StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
			StrictMode.setVmPolicy(new
			StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
		}
		
		Thread.setDefaultUncaughtExceptionHandler(new CrashExceptionHandler());
		initImageLoader(this);
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		HcLog.D(TAG + " den = " + metrics.density + " width = "
				+ metrics.widthPixels + " height = " + metrics.heightPixels);
		HcUtil.setScreenDensity(metrics.density);
		HcUtil.setScreenWidth(metrics.widthPixels);
		HcUtil.setScreenHeight(metrics.heightPixels);

		getResources().updateConfiguration(new Configuration(), metrics);
		
		/** 移动到attachBaseContext里面处理
		mConfig = HcConfig.getConfig();
		mConfig.parseConfig(this);
		 */
		/**
		 * 这里本来放在LoadingActivity里面处理,但是发现要是应用没有打开时，消息推送过来,模块的权限列表都没有去读取；
		 *
		 */
		if (SettingHelper.showGuidePage(this, HcConfig
				.getConfig().getAppVersion(), false)) {
			/**
			 * @author jrjin
			 * @date 2016-2-23 上午9:43:08
			 * 进入应用的日志
			 */
			if (TextUtils.isEmpty(SettingHelper.getAccount(this))) {
				// 第一次进入应用
				HcConfig.getConfig().updatePermisstion(this, true);
			} else {
				HcConfig.getConfig().updatePermisstion(this, false);
			}

		} else {
			HcConfig.getConfig().updatePermisstion(this, false);
		}

		/**
		 *@author jinjr
		 *@date 17-4-7 下午3:26
		 */
		addCommands();

		ContainerConfig.getInstance().parseConfig(this);
		HcUtil.isIntranet(this);
		/*
		mReceiver = new HcAppReceiver();
		IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filter.addDataScheme("package");
		registerReceiver(mReceiver, filter);
		mReceiver.setInstallListener(HcAppData.getInstance());
		*/
		createDefaultFiles();

		/**
		 *@author jinjr
		 *@date 16-12-13 下午2:08
		 *
		 *已经过时
		if (SettingHelper.getAutoSignin(this)) {
			HcUtil.startAutoSignAlarm(this);
		} else {
			HcUtil.stopAutoSignAlarm(this);
		}
		*/

		/**
		 *@author jinjr
		 *@date 17-4-7 下午3:26
		 */
		doCommands();

	}

	public static Context getContext() {
		return mContext;
	}

	public int getMenuSize() {
		return mConfig.getFirstMenuSize();
	}

	public List<MenuInfo> getMenus() {
		return mConfig.getFirstMenus();
	}

	public MenuInfo getCurrentMenuInfo(int index) {
		return mConfig.getCurrentMenu(index);
	}

	/**
	 * @deprecated
	 * @see #createDefaultFiles()
	 * @author jrjin
	 * @time 2015-10-19 上午9:16:04
	 */
	private static void createFileDir() {
		Properties properties = new Properties();
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			properties.load(loader.getResourceAsStream("com/android/hcframe/config/config.properties"));
			FILE_DIR = properties.getProperty("APK_FILE_DIR", "yxh");
		} catch (Exception e) {
			// TODO: handle exception
			FILE_DIR = "zjhc";
		}
	}
	
	/**
	 * @deprecated
	 * @see #createDefaultFiles()
	 * @author jrjin
	 * @time 2015-10-19 上午9:16:50
	 */
	private static void createImageFile() {
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		String filePath;
		if (sdCardExist) {
			filePath = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/" + FILE_DIR;
		} else {
			filePath = Environment.getDataDirectory().getAbsolutePath()
					+ "/" + FILE_DIR;
		}
		mFileName = filePath;

		File file = new File(filePath);
		if (!file.exists()) {
			file.mkdir();
		}
		File download = new File(filePath + "/download");
		if (!download.exists()) {
			download.mkdir();
		}
		File photo = new File(filePath + "/photo");
		if (!photo.exists()) {
			photo.mkdir();
		}

		privateDir = new File(getContext().getFilesDir(), "download");
		if (!privateDir.exists()) {
			privateDir.mkdir();
		}
	}

	public static final File getPdfDir() {
		return privateDir;
	}

	public static final String getAppDownloadPath() {
		return mFileName + "/download";
	}

	public static final String getImagePhotoPath() {
		return mFileName + "/photo";
	}

	private void deleteFiles() {
		File download = new File(getAppDownloadPath());
		if (download.exists() && download.isDirectory()) {
			File[] images = download.listFiles();
			for (File file : images) {
				file.delete();
			}
		}
	}

	private static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you
		// may tune some of them,
		// or you can create default configuration by
		// ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.diskCacheSize(100 * 1024 * 1024)
				// 100 Mb
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs() // Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}

	public static void startAlertClock(Context context) {
		AlarmManager am = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(mContext.getPackageName() + ".ServiceBroadcast");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
			/** 在android3.1以后的版本中， 
			 * 如果程序被强制停止后应用状态会被标记为STOPPED，
			 * 此时应用无法收到其他应用的广播，要等到应用再开启一次，
			 * 将STOPPED去掉以后才可以 */
			intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		}
		intent.setPackage(context.getPackageName());
		PendingIntent sender = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 30 * 1000, sender);
	}
	
//	public RefWatcher getRefWatcher() {
//		return mWatcher;
//	}
	
	/**
	 * 创建一些默认的缓存文件夹
	 * <p></p>
	 * 位置：SD card
	 * <i>("/Android/data/[app_package_name]/cache")</i>
	 * @author jrjin
	 * @time 2015-10-19 上午9:07:03
	 */
	private void createDefaultFiles() {
		File cacheDir = StorageUtils.getCacheDirectory(this);
		mFileName = cacheDir.getAbsolutePath();
		HcLog.D(TAG + " createDefaultFiles FileName = "+mFileName);
		File individualCacheDir = new File(cacheDir, "download");
		if (!individualCacheDir.exists()) {
			individualCacheDir.mkdir();
		}
		
		individualCacheDir = new File(cacheDir, "photo");
		if (!individualCacheDir.exists()) {
			individualCacheDir.mkdir();
		}
		
		individualCacheDir = new File(cacheDir, "log");
		if (!individualCacheDir.exists()) {
			individualCacheDir.mkdir();
		}
		
		privateDir = new File(getFilesDir(), "download");
		if (!privateDir.exists()) {
			privateDir.mkdir();
		}
	}
	
	/**
	 * 注意二维码的链接不能变更,如果变更,这里需要更改.
	 * @author jrjin
	 * @time 2015-12-18 上午11:54:50
	 * @return
	 */
	public String getQRCodePath() {
		File code = new File(getImagePhotoPath(), "app_url.png");
		if (code.exists()) {
			return code.getAbsolutePath();
		} else {
			Bitmap icon = null;
			FileOutputStream os = null; 
			try {
				code.createNewFile();
				icon = BitmapFactory.decodeResource(getResources(), R.drawable.app_url);
				if (icon != null) {
					os = new FileOutputStream(code);
					icon.compress(Bitmap.CompressFormat.PNG, 100, os);
				}
				return code.getAbsolutePath();
				
			} catch (Exception e) {
				// TODO: handle exception
				return "";
			} finally {
				if (os != null) {
					try {
						os.close();
					} catch (Exception e2) {
						// TODO: handle exception
					}
					os = null;
				}
				if (icon != null) {
					icon.recycle();
					icon = null;
				}
			}
		}
	}


	/************* <5.0 dexopt *********************/

	@Override
	protected void attachBaseContext(Context base) {
		super .attachBaseContext(base);
		mContext = this;
		mConfig = HcConfig.getConfig();
		mConfig.parseConfig(this);
		HcLog.D(TAG + " #attachBaseContext App attachBaseContext ");
		if (!quickStart() && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {//>=5.0的系统默认对dex进行oat优化
			if (needWait(base)){
				waitForDexopt(base);
			}
			MultiDex.install (this);
		} else {
			return;
		}
	}


	/**
	 * Get classes.dex file signature
	 * @param context
	 * @return
	 */
	private String get2thDexSHA1(Context context) {
		ApplicationInfo ai = context.getApplicationInfo();
		String source = ai.sourceDir;
		try {
			JarFile jar = new JarFile(source);
			Manifest mf = jar.getManifest();
			Map<String, Attributes> map = mf.getEntries();
			Attributes a = map.get("classes2.dex");
			return a.getValue("SHA1-Digest");
		} catch (Exception e) {
			HcLog.D(TAG + " #get2thDexSHA1 没有进行分包处理,找不到第二个dex包.");
//			e.printStackTrace();
		}
		return "" ;
	}

	// optDex finish
	public void installFinish(Context context){
		SettingHelper.setClassDex(context, HcConfig.getConfig().getAppVersion(), get2thDexSHA1(context));
	}

	private static String getCurProcessName(Context context) {
		try {
			int pid = android.os.Process.myPid();
			ActivityManager manager = (ActivityManager) context
					.getSystemService(Context. ACTIVITY_SERVICE);
			for (ActivityManager.RunningAppProcessInfo appProcess : manager
					.getRunningAppProcesses()) {
				if (appProcess.pid == pid) {
					return appProcess. processName;
				}
			}
		} catch (Exception e) {
			// ignore
		}
		return "" ;
	}

	private void waitForDexopt(Context base) {
		Intent intent = new Intent(base, LoadResActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		base.startActivity(intent);
		long startWait = System.currentTimeMillis ();
		long waitTime = 10 * 1000 ;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1 ) {
			waitTime = 20 * 1000 ;//实测发现某些场景下有些2.3版本有可能10s都不能完成optdex
		}
		while (needWait(base)) {
			try {
				long nowWait = System.currentTimeMillis() - startWait;
				HcLog.D(TAG + "#waitForDexopt wait ms :" + nowWait);
				if (nowWait >= waitTime) {
					return;
				}
				Thread.sleep(200 );
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean quickStart() {
		if (getCurProcessName(this).contains(":mini")) {
			HcLog.D(TAG + " #quickStart :mini start!");
			return true;
		}
		return false ;
	}
	//neead wait for dexopt ?
	private boolean needWait(Context context){
		String flag = get2thDexSHA1(context);
		if (TextUtils.isEmpty(flag)) return false; // 没有分包
		String version = HcConfig.getConfig().getAppVersion();
		String classDex = SettingHelper.getClassDex(context);
		HcLog.D(TAG +  "#needWait dex2-sha1 "+flag + " version = "+version + " last data = "+classDex);
		return !classDex.equals(flag + "_" + version);

	}

	private void addCommands() {
		/** add commands */
		CommandControl.getInstance().setCommand(CommandControl.FLAG_INIT_CONTACTS, "com.android.hcframe.contacts.command.ContactsCommand");
		if (HcConfig.getConfig().assertModule(HcConfig.Module.MAIL)) {
			CommandControl.getInstance().setCommand(CommandControl.FLAG_INIT_EMAIL, "com.android.hcframe.hcmail.EmailCommand");
			CommandControl.getInstance().setCommand(CommandControl.FLAG_CONTACTS_WRITE_EMAIL, "com.android.hcframe.hcmail.WriteEmailCommand");
		}


	}

	private void doCommands() {
		if (!TextUtils.isEmpty(SettingHelper.getUserId(this)))
			CommandControl.getInstance().sendCommand(this, CommandControl.FLAG_INIT_CONTACTS);
		if (HcConfig.getConfig().assertModule(HcConfig.Module.MAIL)) {
			CommandControl.getInstance().sendCommand(this, CommandControl.FLAG_INIT_EMAIL);
		}


	}
}
