package com.android.hcframe.update;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;


import org.json.JSONObject;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.data.DownlaodAppInfo;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.push.HcAppState;
import com.android.hcframe.sql.DataCleanManager;
import com.nostra13.universalimageloader.utils.StorageUtils;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;

/**
 * 版本更新步骤：
 * <p>1、创建APK和下载好的APK的文件夹:{@link HcApplication#createDefaultFiles()}</p>
 * <p>2、去服务器获取最新版本APK信息</p>
 * <p>3、根据服务器返回的信息flag > 0需要升级</p>
 * <p>4、比较存储在本地的APK文件的版本信息，这里可能是个SharePreferences</p>
 * <p>5、本地的>=服务器的，不需要去服务器下载，否则需要去服务器下载。</p>
 * <p>6、要是本地的>=服务器的，则检查本地的APK文件是否存在</p>
 * <p>7、去服务器下载最新的APK</p>
 * @author jrjin
 *
 */
public class DownloadService extends Service implements IHttpResponse {

	private static final String TAG = "DownloadService";

	private Dialog mDownload;

	private LayoutInflater mInflater;

	private final static int NOTIFY_ID = 0x1001;

	private static final int UPDATE_PROGRESS = 1 << 1;
	
	private static final int DOWNLOAD_COMPLETED = 1 << 2;

	private NotificationManager mManager;
	
	private String mFileName;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(final Message msg) {
			// TODO Auto-generated method stub
			HcLog.D(TAG + " msg what " + msg.what);
			if (msg.what == UPDATE_PROGRESS && mTotle != -1) {
				if (mUpdateFlag != 2) {
					mManager.notify(
							NOTIFY_ID,
							getNotification(getApplicationContext(), mTotle,
									msg.arg1));
				} else {
					mHandler.post(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							mViewHolder.mProgressBar.setMax(mTotle);
							mViewHolder.mProgressBar.setProgress(msg.arg1);
//							mViewHolder.mTotle.setText(msg.arg1 + "/" + mTotle + " k");
							mViewHolder.mTotle.setText(intToDouble(msg.arg1) + "/" + mTotleSize + " m");
						}
					});
					
				}
			} else if (msg.what == DOWNLOAD_COMPLETED) {
				if (mUpdateFlag != 2) {
					mManager.cancel(NOTIFY_ID);
					notification = null;
					mBuilder = null;
				} else {
					if (mUpdateDialog != null) {
						mUpdateDialog.cancel();
						mUpdateDialog = null;
					}
					System.exit(0);
				}
				mUpdateFlag = 0;
				stopSelf();
			}
		}
	};

	/** 文件总大小 */
	private int mTotle = -1; // 单位为k
	
	/** 0：空闲状态;1：普通下载;2：强制下载 */
	private int mUpdateFlag = 0;
	
	private boolean mAutomatic = true;
	
	public static final String AUTOMATIC = "automatic";
	
	private final IBinder mBinder = new LocalDownloadBinder();
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		HcLog.D(TAG + " it is onCreate!");
		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		HcLog.D(TAG + " onStartCommand intent = " + intent);
		if (intent == null) {
			stopSelf();
			return START_NOT_STICKY;
		}
		if (intent.getExtras() != null) {
			mAutomatic = intent.getExtras().getBoolean(AUTOMATIC, true);			
		}
		// 这里需要加一个判断,是否需要去请求版本更新
		if (mUpdateFlag == 0)
			HcHttpRequest.getRequest().sendCheckAppVersionCommand(HcConfig.getConfig().getAppVersion(), "" + 0, HcUtil.getIMEI(getApplicationContext()), this);

		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		HcLog.D(TAG + "# it is in onDestroy!");
		super.onDestroy();

	}


	private void showDialog(final HcPackageInfo info, final boolean hasDownloaded) {
		HcLog.D(TAG + " #showDialog it is come showdialog! hasDownloaded = " + hasDownloaded);
		if (mDownload != null) {
			mDownload.cancel();
			mDownload = null;
		}

		Context context = HcAppState.getInstance().getTopActivity();

		mDownload = new Dialog(context == null ? getApplicationContext() : context, R.style.myDialogTheme);
		mDownload.setCancelable(false);
		View view = mInflater.inflate(R.layout.download_check_dialog_layout, null);
		HcLog.D(TAG + " #showDialog it is before show dialog! view = " + view);
		mDownload.setContentView(view);
		if (context == null)
			mDownload.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		mDownload.show();
		HcLog.D(TAG + " #showDialog it is after show dialog!");
		TextView title = (TextView) view.findViewById(R.id.download_title);
		title.setText(getResources().getString(R.string.update_download_dialog_version, info.version));
		TextView content = (TextView) view.findViewById(R.id.download_content);
		content.setText(info.desc);
		content.setMovementMethod(ScrollingMovementMethod.getInstance());
		TextView ok = (TextView) view.findViewById(R.id.download_ok_btn);
		
		TextView canel = (TextView) view
				.findViewById(R.id.download_canel_btn);
		
		if (info.flag == 2) {
			canel.setVisibility(View.GONE);
		} else {
			canel.setVisibility(View.VISIBLE);
		}
		
		ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!hasDownloaded) {
					if (info.flag == 2) {
						showDownloadDialog();
					} 
					HcHttpRequest.getRequest().downloadApp("", info.url, DownloadService.this);
				} else {
					File apkFile = new File(HcApplication.getAppDownloadPath(), mFileName);
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setDataAndType(Uri.fromFile(apkFile),
							"application/vnd.android.package-archive");
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getApplicationContext().startActivity(i);
					if (mUpdateFlag == 2) {
						System.exit(0);
					}
				}
				if (mDownload != null) {
					mDownload.cancel();
					mDownload = null;
				}
			}
		});
		canel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if (mDownload != null) {
					mDownload.cancel();
					mDownload = null;
				}

				mUpdateFlag = 0;
				stopSelf();
			}
		});

	}

	private static final int MB_SIZE = 1024 * 100; // 0.1Mb

	private void createApk(InputStream is) {
		File file = null;
		FileOutputStream os = null;
		try {
			
			file = new File(HcApplication.getAppDownloadPath(), mFileName);
			HcLog.D("apk file exists = "+file.exists());
			if (!file.exists())
				file.createNewFile();
			else {
				file.delete();
				file.createNewFile();
			}
			os = new FileOutputStream(file);
			int size = is.available(); // 在阻塞的情况的始终返回0，本地文件可以获取大小。
			HcLog.D("size = " + size);
			byte[] b = new byte[1024 * 32];
			int len;
			int downloaded = 0;
			int offset = 0;
			while ((len = is.read(b)) > 0) {
				os.write(b, 0, len);
				HcLog.D(TAG + " read size = " + len);
				downloaded += len;
				offset += len;
				HcLog.D(TAG + " download size = " + downloaded + " offset = " + offset);
				if (offset >= MB_SIZE) {
					offset = 0;
					Message message = new Message();
					message.what = UPDATE_PROGRESS;
//				message.arg1 = downloaded / 1024;
					message.arg1 = downloaded;
					mHandler.handleMessage(message);

					try { // 减少状态栏更新太频繁,这里是临时更改,下次要更改成百分比的形式.
						Thread.sleep(50);
					} catch(Exception e) {

					}
				}


			}
			HcLog.D(TAG + " download lenght = " + downloaded);
			os.flush();
			is.close();
			os.close();
			/** 文件下载完 */
			if (mUpdateFlag != 2) {
				Message message = new Message();
				message.what = DOWNLOAD_COMPLETED;
				mHandler.handleMessage(message);

				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setDataAndType(Uri.fromFile(file),
						"application/vnd.android.package-archive");
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplicationContext().startActivity(i);
			} else {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setDataAndType(Uri.fromFile(file),
						"application/vnd.android.package-archive");
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplicationContext().startActivity(i);
				
				Message message = new Message();
				message.what = DOWNLOAD_COMPLETED;
				mHandler.handleMessage(message);
			}
			

		} catch (Exception e) {
			// TODO: handle exception
			HcLog.D(TAG + " download error e = " + e);
			if (file != null && file.exists())
				file.delete();
			Message message = new Message();
			message.what = DOWNLOAD_COMPLETED;
			mHandler.handleMessage(message);
		} finally {
			if (os != null) {
				try {
					os.flush();
					os.close();
					os = null;
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}
			
			if (is != null) {
				try {
					is.close();
					is = null;
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}
			
			stopSelf();
		}

	}
	private Notification notification;

	private PendingIntent mIntent;

	private RemoteViews mRemoteViews;

	private Notification.Builder mBuilder; // SDK >= 11  3.0以后
	
	private Notification getNotification(Context context, int totle, int current) {

		HcLog.D(TAG + " #getNotification notification = "+notification + " builder = "+mBuilder);

		if (Build.VERSION.SDK_INT >= 21) { // 5.0以后
			if (mBuilder == null) {
//				Intent intent = new Intent();
//				mIntent = PendingIntent.getBroadcast(context, 0, intent,
//						PendingIntent.FLAG_UPDATE_CURRENT);
				mBuilder = new Notification.Builder(context)
						.setSmallIcon(R.drawable.app_icon)
//						.setContentIntent(mIntent)
						.setTicker("下载更新!")
				        .setContentTitle("下载APK");

			}

//			mBuilder.setContentText(intToDouble(current)
//					+ "/" + mTotleSize + " M"); // 显示在进度条的左下方
			mBuilder.setContentInfo(intToDouble(current)
					+ "/" + mTotleSize + " M"); // 显示在进度条的右下方
			mBuilder.setProgress(totle, current, false);
			return mBuilder.build();
		} else {
			if (notification == null) {
				notification = new Notification();
				Intent intent = new Intent();
//			intent.setPackage(getPackageName());
				mIntent = PendingIntent.getBroadcast(context, 0, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				notification.icon = R.drawable.app_icon;
				notification.contentIntent = mIntent;
			}
			mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.download_remote_layout);
//		Intent intent = new Intent();
			// intent.setClass(context, Menu1Activity.class);
			// Intent intent = new Intent(Intent.ACTION_MAIN);
			// intent.addCategory(Intent.CATEGORY_LAUNCHER);
			// intent.setComponent(((Activity) context).getComponentName());
			// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
			// Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

//		PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent,
//				PendingIntent.FLAG_UPDATE_CURRENT);
//		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.download_remote_layout);
//		remoteViews.setProgressBar(R.id.download_remote_progress, totle / 1024,
//				current / 1024, false);
//		remoteViews.setTextViewText(R.id.download_remote_text, "" + current
//				+ "/" + totle + " k");
//		notification.icon = R.drawable.app_icon;
//		notification.contentIntent = pIntent;
//		notification.contentView = remoteViews;

			mRemoteViews.setProgressBar(R.id.download_remote_progress, totle,
					current, false);
			mRemoteViews.setTextViewText(R.id.download_remote_text, intToDouble(current)
					+ "/" + mTotleSize + " M");


			notification.contentView = mRemoteViews;
		}




		return notification;
	}

	
	private Dialog mUpdateDialog;
	
	private void showDownloadDialog() {
		if (mDownload != null) {
			mDownload.cancel();
			mDownload = null;
		}

		Context context = HcAppState.getInstance().getTopActivity();

		HcLog.D("it is in showDownloadDialog! flag = "+mUpdateFlag);
		mUpdateDialog = new Dialog(context == null ? getApplicationContext() : context, R.style.myDialogTheme);
		mUpdateDialog.setCancelable(false);
		View view = mInflater.inflate(R.layout.download_remote_layout, null);
		mUpdateDialog.setContentView(view);
		if (context == null)
			mUpdateDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT); // 窗口可以获得焦点响应操作

		// mUpdateDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY); // system_overlay窗口显示的时候焦点在后面的Activity上，仍旧可以操作后面的Activity
		mUpdateDialog.show();
		// 只能在show后面设置值,比如设置窗口的大小
		// WindowManager.LayoutParams lp = d.getWindow().getAttributes();
		// WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		// Display display = wm.getDefaultDisplay();
		// lp.gravity = 0x11, // CENTER
		// mUpdateDialog.getWindow().setAttributes(lp);

		mViewHolder = new ViewHolder();
		mViewHolder.mTotle = (TextView) view.findViewById(R.id.download_remote_text);
		mViewHolder.mProgressBar = (ProgressBar) view.findViewById(R.id.download_remote_progress);
		
	}
	
	private ViewHolder mViewHolder;
	
	private class ViewHolder {
		TextView mTotle;
		ProgressBar mProgressBar;
	}

	@Override
	public void notify(Object data, RequestCategory request,
			ResponseCategory category) {
		// TODO Auto-generated method stub
		HcLog.D(TAG + " notify data = "+data + " request = "+request);
		if (request != null) {
			switch (request) {
			case CHECKAV: // 已经在主线程
				switch (category) {
				case SUCCESS:
					updateVersion((String) data);
					break;
				case NETWORK_ERROR:
					stopSelf();
					break;
				case SYSTEM_ERROR:
					stopSelf();
					break;
				case DATA_ERROR:
					stopSelf();
					break;

				default:
					stopSelf();
					break;
				}
				break;
			case DOWNLOAD_APP:
				if (data != null && data instanceof DownlaodAppInfo) {
					/**
					 * 这里注意，还是异步的，即不是在主线程里面 开始创建apk文件
					 */
					createApk(((DownlaodAppInfo)data).stream);
				} else {
					mUpdateFlag = 0;
				}
				break;
			case UPDATEAS:
				// 退出应用
				System.exit(0);
				break;

			default:
				stopSelf();
				break;
			}
		}
	}


	@Override
	public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 一个绑定本地的远程服务
	 * @author jrjin
	 * @time 2015-12-22 上午11:55:18
	 */
    public class LocalDownloadBinder extends Binder {
		
		public DownloadService getLocalService() {
			return DownloadService.this;
		}
	}
    
    /**
     * 版本检测
     * @author jrjin
     * @time 2015-12-22 下午1:46:22
     */
    public void sendCheckAppVersion() {
    	if (mUpdateFlag == 0) { // 说明空闲状态
    		if (HcUtil.isNetWorkAvailable(this)) {
    			HcHttpRequest.getRequest().sendCheckAppVersionCommand(HcConfig.getConfig().getAppVersion(), "" + 0, HcUtil.getIMEI(getApplicationContext()), this);
    		}
    	} 
    }
    
    
    public enum UpdateState {
    	/** 网络不可用 */
    	NETWORK_UNAVAILABLE,
    	/** 版本检测失败 */
    	CHECK_FAILED,
    	/** 需要更新 */
    	UPDATE,
    	/** 已经最新 */
    	LATEST
    }
    
    private void showRegisterDialog() {
    	if (mDownload != null) {
    		mDownload.cancel();
    		mDownload = null;
    	}
		Context context = HcAppState.getInstance().getTopActivity();

    	mDownload = new Dialog(context == null ? getApplicationContext() : context, R.style.myDialogTheme);
		mDownload.setCancelable(false);
		View view = mInflater.inflate(R.layout.download_check_dialog_layout, null);
		HcLog.D(TAG + " # it is before show dialog! view = "+view);
		mDownload.setContentView(view);
		if (context == null)
			mDownload.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		mDownload.show();
		TextView title = (TextView) view.findViewById(R.id.download_title);
    	title.setText("新注册用户");
    	TextView content = (TextView) view.findViewById(R.id.download_content);
		content.setText("请联系管理员将设备设置为启用状态!");
		content.setMovementMethod(ScrollingMovementMethod.getInstance());
		TextView ok = (TextView) view.findViewById(R.id.download_ok_btn);
		
		TextView canel = (TextView) view
				.findViewById(R.id.download_canel_btn);
		canel.setVisibility(View.GONE);
		ok.setText("退出应用");
		ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if (mDownload != null) {
					mDownload.cancel();
					mDownload = null;
				}
				System.exit(0);
			}
		});
    }
    
    
    public void updateVersion(String data) {
    	HcLog.D(TAG + " #updateVersion data ="+data + " mUpdateFlag = "+mUpdateFlag);
    	if (mUpdateFlag != 0) return;
    	try {
			JSONObject object = new JSONObject((String) data);
			if (HcUtil.hasValue(object, "status")) {
				String status = object.getString("status");
				if ("1".equals(status)) {
					showRegisterDialog();
				} else if ("3".equals(status)) {
					DataCleanManager.cleanApplicationData(
							getApplicationContext(),
							true,
							new File(Environment
									.getExternalStorageDirectory()
									+ "/hc/").getAbsolutePath(),
							StorageUtils.getCacheDirectory(this)
									.getAbsolutePath());
					
					HcHttpRequest.getRequest().sendUpdateTerStsCommand(HcUtil.getIMEI(getApplicationContext()), DownloadService.this);
					// 退出应用
//					System.exit(0); // 放在notify中处理
				}
			} else { // 版本检测
				if (HcUtil.hasValue(object, "flag")) {
					HcPackageInfo info = new HcPackageInfo();
					info.flag = object.getInt("flag");
//					info.flag = 2;
					mUpdateFlag = info.flag;
					if (info.flag > 0) { // 需要升级
						if (HcUtil.hasValue(object, "desc")) {
							info.desc = object.getString("desc");
						}
						if (HcUtil.hasValue(object, "version")) {
							info.version = object.getString("version");
						}
						if (HcUtil.hasValue(object, "url")) {
							info.url = object.getString("url");
						}
						if (HcUtil.hasValue(object, "size")) { // 单位从k转变到byte
							info.size = Integer.valueOf(object.getString("size"));
						}
//						info.url = info.url.replace("localhost:8080", "10.80.18.251:8002");
						mTotle = info.size;
						mTotleSize = intToDouble(mTotle);
						mFileName = info.version + "_" + info.size + ".apk";
						File file = new File(HcApplication.getAppDownloadPath(), mFileName);
//						boolean download = file.exists() && (file.length() / 1024 >= info.size - 5);
//						HcLog.D(TAG + " file size = "+file.length() / 1024 + " info download size = " + info.size);
						boolean download = file.exists() && (file.length() >= info.size - 5);
						HcLog.D(TAG + " file size = "+file.length() + " info download size = " + info.size);
						showDialog(info, download);
						
					} 
				}
				
			}
		} catch (Exception e) {
			// TODO: handle exception
			HcLog.D(TAG + " notify error = "+e);
			
		}
    }

	private String mTotleSize;

	private String intToDouble(int data) {
		double d = data / (1024 * 1024d);
		BigDecimal decimal = new BigDecimal("" + d);
		String value = decimal.setScale(2,BigDecimal.ROUND_HALF_UP).toString();
		HcLog.D(TAG + " #intToDouble size = "+data + " byte" + "  size ="+value + " Mb");
		return value;
	}
}
