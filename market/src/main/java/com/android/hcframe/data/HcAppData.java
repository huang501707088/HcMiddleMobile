/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-5-1 下午3:09:24
*/
package com.android.hcframe.data;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;

import com.android.hcframe.CacheManager;
import com.android.hcframe.HcApplication;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcObservable;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TemporaryCache;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.market.AppCategoryView;
import com.android.hcframe.market.MarketOperateDatabase;
import com.android.hcframe.market.UpdateCallback;
import com.android.hcframe.sql.SettingHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class HcAppData extends HcObservable implements IHttpResponse,
		AppInstall, TemporaryCache {

	private static final String TAG = "HcAppData";
	
	private static final HcAppData mAppData = new HcAppData();
	
	private HcHttpRequest mRequest;
	
	private List<AppInfo> mAppInfos = new ArrayList<AppInfo>();
	
	public static final int APP_CATEGORY_ALL = 0;
	public static final int APP_CATEGORY_DATA = 1;
	public static final int APP_CATEGORY_SERVICE = 2;
	public static final int APP_CATEGORY_SUPERVICE = 3;
	public static final int APP_CATEGORY_OA = 4;
	/** key:appId;value:icon*/
	private Map<String, Bitmap> mIconMap = new WeakHashMap<String, Bitmap>();
	
	private List<String> mDownloadList = new ArrayList<String>();
	
	private Handler mHandler = new Handler();
	
	private boolean mSorted = false;
	
	/** 应用超市适配器的回调接口，可能有多个应用超市界面 */
	private List<UpdateCallback> mCallbacks = new ArrayList<UpdateCallback>();

	private HcAppData() {

		mRequest = HcHttpRequest.getRequest();
		CacheManager.getInstance().addCache(this);
	}
	
	public static final HcAppData getInstance() {
		return mAppData;
	}

	/**
	 * 全部列表需要重新排序
	 * @author jrjin
	 * @time 2015-5-29 下午4:11:18
	 */
	public void setSorted() {
		mSorted = true;
		Collections.sort(mAppInfos, new Comparator<AppInfo>() {

			@Override
			public int compare(AppInfo lhs, AppInfo rhs) {
				// TODO Auto-generated method stub	
				return lhs.getAllOrder() < rhs.getAllOrder() ? -1 : (lhs.getCategoryOrder() == rhs.getCategoryOrder() ? 0 : 1);
			}
			
		});
	}
	
	/**
	 * <p>1、先在临时缓存中查找</p>
	 * <p>2、再到数据库中查找</p>
	 * <p>3、最后到服务端去获取</p>
	 * @author jrjin
	 * @time 2015-5-1 下午3:16:41
	 * @param category
	 * @param update 是否一定要去服务端获取数据,比如第一次刚进应用的时候
	 */
	public void getAppList(int category, RequestCategory cr, boolean update) {
		HcLog.D(TAG + " getAppList category = "+category + " RequestCategory = "+cr + " update = "+update);
		List<AppInfo> mInfos = new ArrayList<AppInfo>();
		if (category == APP_CATEGORY_ALL) {
			mInfos.addAll(mAppInfos);
		} else {
			filterAppInfo(mInfos, category);
		}
//		HcLog.D(TAG + " getAppList size = "+mInfos.size() + " category = "+category);
		if (mInfos.size() == 0) {
			mAppInfos.clear();
			mAppInfos.addAll(MarketOperateDatabase.getAllApps(HcApplication.getContext()));
			if (category == APP_CATEGORY_ALL) {
				mInfos.addAll(mAppInfos);
			} else {
				filterAppInfo(mInfos, category);
			}
			
		} 
		
		if (mInfos.size() > 0) {
			notifyObservers(this, mInfos, cr, null);
		}
		
		if (update) {
			mRequest.sendAppListCommand("" + 0, cr, this);
		}
			
	}
	
	public void refreshApps(int category, RequestCategory cr) {
		mRequest.sendAppListCommand("" + 0, cr, this);
	}
	
	/**
	 * 
	 * @author jrjin
	 * @time 2015-5-28 下午3:03:54
	 * @param infos 需要排序的应用列表
	 * @param category 需要排序的应用类型
	 */
	private void filterAppInfo(List<AppInfo> infos, int category) {
		for (AppInfo info : mAppInfos) {
//			HcLog.D(TAG + " filterAppInfo info category = "+info.getAppCategory() + " category = "+category);
			if (info.getAppCategory() == category) {
				
				infos.add(info);
//				Collections.sort(infos, new Comparator<AppInfo>() {
//
//					@Override
//					public int compare(AppInfo lhs, AppInfo rhs) {
//						// TODO Auto-generated method stub	
//						return lhs.getCategoryOrder() < rhs.getCategoryOrder() ? -1 : (lhs.getCategoryOrder() == rhs.getCategoryOrder() ? 0 : 1);
//					}
//					
//				});
			}
				
			
		}
		
		Collections.sort(infos, new Comparator<AppInfo>() {

			@Override
			public int compare(AppInfo lhs, AppInfo rhs) {
				// TODO Auto-generated method stub	
				return lhs.getCategoryOrder() < rhs.getCategoryOrder() ? -1 : (lhs.getCategoryOrder() == rhs.getCategoryOrder() ? 0 : 1);
			}
			
		});
	}
	
	@Override
	public void notify(Object data, RequestCategory request,
			ResponseCategory category) {
		// TODO Auto-generated method stub
//		HcLog.D(TAG + " #notify data = "+data);
		if (request != null && request == RequestCategory.APP_ALL && category != null) {
			switch (category) {
			case SUCCESS:
				if (data != null && data instanceof List<?>) { // 更改为从本类里面调用,请查看parseDate() 2016-03-15
					int size = mAppInfos.size();
					HcLog.D(TAG + " #notify before add app size = "+size);
					if (size == 0) {
						mAppInfos.addAll(MarketOperateDatabase.getAllApps(HcApplication.getContext()));
					}
					size = mAppInfos.size();
					
					if (size == 0) { // 说明数据库中没有数据						
						mAppInfos.addAll((List<AppInfo>) data);
						MarketOperateDatabase.insertAppsOnDestory(mAppInfos, HcApplication.getContext());
					} else {
						/** 新的App列表 */
						List<AppInfo> newAppInfos = new ArrayList<AppInfo>((List<AppInfo>) data);
						/** 当前的App列表 */
						List<AppInfo> oldAppInfos = new ArrayList<AppInfo>(mAppInfos);

						
						Iterator<AppInfo> newIterator = newAppInfos.iterator();
						Iterator<AppInfo> oldIterator = oldAppInfos.iterator();
						AppInfo oldInfo;
						AppInfo newInfo;
						/** 外层先遍历新的APP，这样可以先插入新增的 */
						boolean newApp = true;
						while (newIterator.hasNext()) {
							newApp = true;
							newInfo = newIterator.next();
							oldIterator = oldAppInfos.iterator();
							while (oldIterator.hasNext()) {
								oldInfo = oldIterator.next();
								if (oldInfo.getAppId().equals(newInfo.getAppId())) {
									// 说明不是新增的应用
									newApp = false;
									if (!oldInfo.getAppVersion().equals(newInfo.getAppVersion())) {
										// 版本不一样，有更新
										if (oldInfo.getAppType() == 0 && newInfo.getAppType() == 0) {
											// 说明应用的类型没有发生变化,只需更改安装的状态
											if (oldInfo.getAppState() != HcUtil.APP_NORMAL) {
												oldInfo.setAppState(HcUtil.APP_UPDATE);
											}
										} else { // 类型发生了变化
											oldInfo.setAppType(newInfo.getAppType());
											oldInfo.setAppState(HcUtil.APP_NORMAL);
										}
										
										oldInfo.setAppName(newInfo.getAppName());
										oldInfo.setAppPackage(newInfo.getAppPackage());
										oldInfo.setAppSize(newInfo.getAppSize());
										oldInfo.setAppUrl(newInfo.getAppUrl());
										oldInfo.setAppVersion(newInfo.getAppVersion());
										oldInfo.setCategoryName(newInfo.getCategoryName());
										oldInfo.setAppIcon(newInfo.getAppIconUrl()); // 更换图标地址
										
										// 版本不一样，要是服务端排序有变动，才更改本地的排序，否则本地的排序不变
										int oldOrder = oldInfo.getServerOrder();
										int newOrder = newInfo.getServerOrder();
										
										if (oldOrder != newOrder) {
											oldInfo.setServerOrder(newOrder);
											// 因为先遍历服务端的列表，所以不会出现(newOrder >= mAppInfos.size())的情况
											// 顺序发生了变化
											// 这里需要更改，需要增加时间字段
//											mAppInfos.remove(oldInfo);
//											mAppInfos.add(newOrder, oldInfo);
										}
										
									} else {
										// 版本一样，不需要变动本地的排序，但是需要更改服务端的排序
										oldInfo.setServerOrder(newInfo.getServerOrder());
									}
									oldIterator.remove();
									break;
								}
								
							}
							
//							HcLog.D(TAG + " notify newApp = "+newApp);
							if (newApp) { // 插入新的APP
								// 先设置类别的排序
								int count = 0;
								for (AppInfo appInfo : mAppInfos) {
									if (appInfo.getAppCategory() == newInfo.getAppCategory()) {
										count ++;
									}
								}
								newInfo.setCategoryOrder(count);
								HcLog.D(TAG + " notify newApp server order = "+newInfo.getServerOrder());
								mAppInfos.add(newInfo.getServerOrder(), newInfo);
							}
						}
						
						HcLog.D(TAG + " #notify old size = "+oldAppInfos.size() + " app size = "+mAppInfos.size());
						// 删除多余的数据
						mAppInfos.removeAll(oldAppInfos);
						HcLog.D(TAG + " #notify after remove size = "+mAppInfos.size());
						oldAppInfos.clear();
						newAppInfos.clear();
						
						// 重新设置列表的顺序
						for (int i = 0, n = mAppInfos.size(); i < n; i++) {
							mAppInfos.get(i).setAllOrder(i);
						}
						
						if (mAppInfos.size() == 0) { // 说明当前用户服务端没有数据了，需要删除数据库中的数据
							MarketOperateDatabase.deleteApps(HcApplication.getContext());
						}
					}
					
					switch (request) {
					case APP_ALL:
						notifyObservers(this, mAppInfos, request, category);
						break;
//					case APP_DATA:
//						filterAppInfo(infos, APP_CATEGORY_DATA);
//						break;
//					case APP_OA:
//						filterAppInfo(infos, APP_CATEGORY_OA);
//						break;
//					case APP_SERVICE:
//						filterAppInfo(infos, APP_CATEGORY_SERVICE);
//						break;
//					case APP_SUPERVICE:
//						filterAppInfo(infos, APP_CATEGORY_SUPERVICE);
//						break;

					default:
						break;
					}
				} else if (data != null && data instanceof String) {
					/**
					 * @author jinjr
					 * @date 2016-03-15 14:35
					 * 解析放到这里来做,本来是在HcHttpRequest里面的
					 * 这里还在线程里面
					 */
				    parseData((String) data, request);

				}

				break;
				
			case SESSION_TIMEOUT:
			case NETWORK_ERROR:
//				setApps(request, category);
				HcUtil.toastTimeOut(HcApplication.getContext());
				notifyObservers(this, null, request, category);
				break;
			case DATA_ERROR:
				HcUtil.toastDataError(HcApplication.getContext());
				break;
			case SYSTEM_ERROR:
				HcLog.D(TAG + " #nofity response = DATA_ERROR");
//				HcUtil.showToast(HcApplication.getContext(), "请先登录！");
				HcUtil.toastSystemError(HcApplication.getContext(), data);
				notifyObservers(this, null, request, category);
				break;
			case REQUEST_FAILED:
				notifyObservers(this, data, request, category);
				break;
				
			default:
				break;
			}
		} else if (request != null && request == RequestCategory.DOWNLOAD_IMAGE) {
			if (data != null) { // 注意这里还在线程里
				if (data instanceof DownlaodAppInfo) {
					final DownlaodAppInfo info = (DownlaodAppInfo) data;
					InputStream stream = info.stream;
//					HcLog.D(TAG + " thread = "+Thread.currentThread());
					String filePath = createIcon(info.appId, info.appVersion, stream);
					HcLog.D(TAG + " filePath = "+filePath);
					if (stream != null) {
						try {
							stream.close();
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
					if (filePath != null) {
						BitmapFactory.Options options = new BitmapFactory.Options();
						final Bitmap bmp = BitmapFactory.decodeFile(filePath, options);
						mDownloadList.remove(info.appId + "_" + info.appVersion);
						HcLog.D(TAG + " stream = "+info.stream + " Bitmap = "+bmp);
						if (bmp != null) {
							mIconMap.put(info.appId + "_" + info.appVersion, bmp);
							/**
							 * @author jrjin
							 * @date 2015-11-30 下午2:20:38
							 */
							mHandler.post(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									for (UpdateCallback callback : mCallbacks) {
										callback.setBitmap(info.appId, bmp);
									}
								}
							});
							/**
							 * @date 2015-11-30 下午2:20:50
							if (mCallback != null) {
								mHandler.post(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										mCallback.onComplete(info.appId, bmp);
									}
								});
							}
							*/
						} else {
							// 删除图片，说明下载的时候没有下载完整需要重新下载
							File file = new File(filePath);
							file.delete();
						}
						
					}
//					BitmapFactory.Options options = new BitmapFactory.Options();
//					final Bitmap bmp = BitmapFactory.decodeStream(stream, null, options);
//					if (stream != null) {
//						try {
//							stream.close();
//						} catch (Exception e) {
//							// TODO: handle exception
//						}
//					}
//					HcLog.D(TAG + " stream = "+info.stream + " Bitmap = "+bmp);
//					mDownloadList.remove(info.appId + "_" + info.appVersion);
//					if (bmp != null) {
//						mIconMap.put(info.appId + "_" + info.appVersion, bmp);
//						if (mCallback != null) {
//							mHandler.post(new Runnable() {
//								
//								@Override
//								public void run() {
//									// TODO Auto-generated method stub
//									mCallback.onComplete(info.appId, bmp);
//								}
//							});
//						}
//					}

				} else if (data instanceof String) {
					mDownloadList.remove((String) data);
				}
			}
		} else if (request == RequestCategory.DOWNLOAD_APP && data != null
				&& data instanceof DownlaodAppInfo) {
			/**
			 * 这里注意，还是异步的，即不是在主线程里面 开始创建apk文件
			 */
			createApk((DownlaodAppInfo) data);
		} else if (request == RequestCategory.DOWNLOAD_APP && category != null && category == ResponseCategory.SESSION_TIMEOUT) {
//			HcUtil.showToast(HcApplication.getContext(), "下载失败!");//统一在onProgress里面处理
			if (data instanceof String) { // 为AppId
				onProgress((String) data, -1,Integer.MAX_VALUE);
			}
		}
	}
	
	private String createIcon(String appId, String version, InputStream is) {
		if (is != null) {
			OutputStream outputStream = null;
			try {
				String directory = HcApplication.getImagePhotoPath();
				File dir = new File(directory);
	            if (!dir.exists()) dir.mkdirs();
	            File file = new File(directory, appId + "_" + version + ".png");
	            outputStream = new FileOutputStream(file);
	            byte[] b = new byte[1024];
    			int len;
    			while ((len = is.read(b)) > 0) {
    				outputStream.write(b, 0, len);
    			}
    			return file.getAbsolutePath();		
			} catch (Exception e) {
				// TODO: handle exception
				return null;
			} finally {
				try {
					if (outputStream != null) 
						outputStream.close();
					is.close();
					is = null;
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}
		}
		
		return null;
	}
	
	private String getBase64(Resources res, int id) {
		return getBase64(BitmapFactory.decodeResource(res, id));
	}
	
	private String getBase64(Bitmap b) {
		ByteArrayOutputStream out = null;
		try {
			out = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.PNG, 100, out);
 
            out.flush();
            out.close();
 
            byte[] imgBytes = out.toByteArray();
            return Base64.encodeToString(imgBytes, Base64.DEFAULT);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			try {
                out.flush();
                out.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
               
            }
		}
		return null;
	}
	
	public Bitmap getIcon(String appId, String version) {
		Bitmap icon = null;
		icon = mIconMap.get(appId + "_" + version);		
		if (icon == null) {
			try {
				String directory = HcApplication.getImagePhotoPath();
				File dir = new File(directory);
	            if (!dir.exists()) dir.mkdirs();
	            File file = new File(directory, appId + "_" + version + ".png");
	            if (file.exists()) {
	            	 BitmapFactory.Options options = new BitmapFactory.Options();
	 	            icon = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
	            }
	           
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		if (icon != null) {
			mIconMap.put(appId + "_" + version, icon);
		}
		return icon;
	}
	
	public void downloadBitmap(String appid, String iconUrl, String version) {
		if (mDownloadList.contains(appid + "_" + version)) return;
		HcLog.D(TAG + " downloadBitmap iconUrl = "+iconUrl);
		if (!TextUtils.isEmpty(iconUrl)) {
//			mDownloadList.add(appid + "_" +version);//已经在HcHttpReques#sendDownloadImage方法中处理相同的请求
			mRequest.sendDownloadImage(appid, iconUrl, version, this);
		}
		
	}
	
	public List<AppInfo> getAppInfos() {
		return mAppInfos;
	}
	
	/**
	 * 用户退出登录的时候，需要调用
	 * @author jrjin
	 * @time 2015-6-2 下午5:18:31
	 */
	public void releaseData() {
		MarketOperateDatabase.insertAppsOnDestory(mAppInfos, HcApplication.getContext());
		mAppInfos.clear();
		notifyObservers(this, mAppInfos, RequestCategory.APP_ALL, ResponseCategory.SUCCESS);
		HcLog.D(TAG + " #releaseData end releaseData!");
	}

	@Override
	public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
		// TODO Auto-generated method stub
		
	}
	
	public void addCallbacks(UpdateCallback callback) {
		if (!mCallbacks.contains(callback)) {
			mCallbacks.add(callback);
		}
	}

	public void onProgress(String appId, int current, int max) {
		for (UpdateCallback callback : mCallbacks) {
			callback.onProgress(appId, current, max);
		}
	} 
	
	public void removeUpdateCallback(UpdateCallback callback) {
		mCallbacks.remove(callback);
	}
	
	public void removeAllUpdateCallbacks() {
		mCallbacks.clear();
	}
	
	public void setBitmap(String appId, Bitmap icon) {
		for (UpdateCallback callback : mCallbacks) {
			callback.setBitmap(appId, icon);
		}
	}
	
	private int mCurrent = 0;
	
	/**
	 * 注意不是在主线程里面
	 * @author jrjin
	 * @time 2015-5-4 下午3:43:51
	 * @param info
	 */
	private void createApk(final DownlaodAppInfo info) {
		try {
			final AppInfo appInfo = getAppInfo(info.appId);
			File file = new File(HcApplication.getAppDownloadPath() + "/" + "apk_" + appInfo.getAppId() + "_" + appInfo.getAppVersion() + "_zjhc.apk");
			HcLog.D("apk file exists = "+file.exists());
			if (!file.exists())
				file.createNewFile();
			else {
				file.delete();
				file.createNewFile();
			}
			FileOutputStream os = new FileOutputStream(file);
			InputStream is = info.stream;
			byte[] b = new byte[1024];
			int len;
			int downloaded = 0;
			while ((len = is.read(b)) > 0) {
				os.write(b, 0, len);
				downloaded += len;
//				HcLog.D(TAG + " download size = " + downloaded);
				mCurrent = downloaded / 1024;
				mHandler.post(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						onProgress(info.appId, mCurrent, Integer.valueOf(appInfo.getAppSize()));
					}
				});

			}
			HcLog.D(TAG + " download lenght = " + downloaded);
			os.flush();
			is.close();
			os.close();
			/** 文件下载完 */
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					onProgress(info.appId, -1, -1);
				}
			});
//			int totle = appInfo.getAppSize();
			if (downloaded == 0) { // 
				HcUtil.showToast(HcApplication.getContext(), "网络错误,下载失败！");
				return;
			}

			SettingHelper.setDownloadAppInfo(HcApplication.getContext(), appInfo.getAppId() + ";" + appInfo.getAppVersion());
			/** 这里需要测试,Application能否启动Activity */
			appInfo.installApk(file, HcApplication.getContext());

		} catch (Exception e) {
			// TODO: handle exception
			HcLog.D(TAG + " download error e = " + e);
			e.printStackTrace();
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					onProgress(info.appId, -1, Integer.MAX_VALUE);
				}
			});
		}
	}
	
	private AppInfo getAppInfo(String appId) {
		for (AppInfo info : mAppInfos) {
			if (info.getAppId().equals(appId)) {
				return info;
			}
		}
		return null;
	}

	@Override
	public void onInstallCompleted(String pkg, String action) {
		// TODO Auto-generated method stub
		HcLog.D(TAG + " onInstallCompleted pkg = " + pkg + " action = " + action);
		List<AppInfo> infos = mAppInfos;
		for (AppInfo appInfo : infos) {
			if (pkg.equals(appInfo.getAppPackage())) {
				if (action.equals(Intent.ACTION_PACKAGE_ADDED)
						|| action.equals(Intent.ACTION_PACKAGE_CHANGED)) {
					appInfo.setAppState(HcUtil.APP_INSTALL);
					MarketOperateDatabase.updateAppInfo(appInfo, HcApplication.getContext());
					// 更新界面
					for (UpdateCallback callback : mCallbacks) {
						callback.notifyDataChanged();
					}
				}
			}
		}
	}
	
	/**
	 * 通知适配器更新数据，
	 * 在{@link AppCategoryView#onItemClick(android.widget.AdapterView, android.view.View, int, long)}
	 */
	public void notifyObservers() {
		for (UpdateCallback callback : mCallbacks) {
			callback.notifyDataChanged();
		}
	}

	@Override
	public void clearCache(boolean exit) {
		releaseData();
	}


	private void parseData(String data, final RequestCategory request) {

		try {
			JSONObject object = new JSONObject(data);
			JSONArray array = object.getJSONArray("body");
			final List<AppInfo> infos = new ArrayList<AppInfo>();
			JSONObject app;
			AppInfo info = null;
			for (int i = 0, n = array.length(); i < n; i++) {
				app = array.getJSONObject(i);
				info = null;
				if (HcUtil.hasValue(app, "type")) {
					String type = app.getString("type");
					if (!TextUtils.isEmpty(type)
							&& type.equals("0")) {
						info = new NativeAppInfo();
						info.setAppType(0);
					}

				}
				if (info == null) {
					info = new Html5AppInfo();
					info.setAppType(1);
				}
				// HcLog.D(TAG + " info = " + info);
				if (HcUtil.hasValue(app, "id")) {
					info.setAppId(app.getString("id"));
				}
				if (HcUtil.hasValue(app, "name")) {
					info.setAppName(app.getString("name"));
				}
				if (HcUtil.hasValue(app, "icon")) {
					info.setAppIcon(app.getString("icon"));
				}
				if (HcUtil.hasValue(app, "version")) {
					info.setAppVersion(app.getString("version"));
				}
				if (HcUtil.hasValue(app, "url")) {
					info.setAppUrl(app.getString("url"));
				}
				if (HcUtil.hasValue(app, "package_name")) {
					info.setAppPackage(app
							.getString("package_name"));
				}
				if (HcUtil.hasValue(app, "category")) {
					// HcLog.D(TAG +
					// " category = "+app.getString("category"));
					info.setAppCategory(Integer.valueOf(app
							.getString("category")));
					// HcLog.D(TAG + " category = "
					// + app.getString("category"));
				}
				if (HcUtil.hasValue(app, "appsize")) {
					info.setAppSize(Integer.valueOf(app
							.getString("appsize")));
				}
				if (HcUtil.hasValue(app, "category_name")) {
					info.setCategoryName(app
							.getString("category_name"));
				}

				info.setAllOrder(i);
				info.setServerOrder(i);
				infos.add(info);
			}

			// 确认有多少种应用类型
			Set<Integer> category = new HashSet<Integer>();
			for (AppInfo appInfo : infos) {
				category.add(appInfo.getAppCategory());
			}

			for (Integer integer : category) {
				filterApps(infos, integer);
			}

			mHandler.post(new Runnable() {
				@Override
				public void run() {
					HcAppData.this.notify(infos, request, ResponseCategory.SUCCESS);
				}
			});
		} catch (JSONException e) {

			mHandler.post(new Runnable() {
				@Override
				public void run() {
					notifyObservers(HcAppData.this, null, request, ResponseCategory.DATA_ERROR);
				}
			});
		}
	}

	private void filterApps(List<AppInfo> infos, int category) {
		List<AppInfo> appInfos = new ArrayList<AppInfo>();
		for (AppInfo info : infos) {
			if (category == info.getAppCategory()) {
				appInfos.add(info);
			}
			// HcLog.D(TAG +
			// " filterApps info category = "+info.getAppCategory() +
			// " category = "+category);
		}
		for (int i = 0, n = appInfos.size(); i < n; i++) {
			appInfos.get(i).setCategoryOrder(i);
		}

	}
}
