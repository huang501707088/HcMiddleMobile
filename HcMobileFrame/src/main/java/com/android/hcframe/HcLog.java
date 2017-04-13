package com.android.hcframe;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import com.nostra13.universalimageloader.utils.StorageUtils;

import android.content.Context;
import android.util.Log;

public final class HcLog {

	private static final String TAG = "HcMobileFrame";
	
	public static final String TAG_PDF = "pdf_test";
	
	public static final boolean DEBUG = true;
	
	private static final String mPackageName = HcUtil.PACKAGE_NAME;
	
	private HcLog() {}
	
	public static void D(String debug) {
		if (DEBUG) {
//			if (null == mPackageName) {
//				mPackageName = HcApplication.getContext().getPackageName();
//			}
			Log.d(TAG, mPackageName + " " + debug);

		}
	}
	
	public static void E(String error)
	{
		if (DEBUG) {
//			if (null == mPackageName) {
//				mPackageName = HcApplication.getContext().getPackageName();
//			}
			Log.e(TAG, mPackageName + " " + error);
		}
	}
	
	public static void Debug(String tag, String debug) {
		if (DEBUG) {
//			if (null == mPackageName) {
//				mPackageName = HcApplication.getContext().getPackageName();
//			}
			Log.d(TAG + "#" +tag, mPackageName + " " + debug);
		}
	}
	
	public static void Sysout(String msg)
	{
		if(DEBUG)
			System.out.println("-->"+msg);
	}
	
	public static void writeDebug(String debug) {
		if (DEBUG) {
			File directory = getCacheDirectory(HcApplication.getContext());
			File file = new File(directory, HcUtil.getDate("yyyy-MM-dd", System.currentTimeMillis()) + ".txt");
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (Exception e) {
					// TODO: handle exception
					HcLog.D(" #writeDebug error ==================================== "+e);
				}
			}
			FileWriter writer = null;
			BufferedWriter bWriter = null;
			try {
				writer = new FileWriter(file, true);
				bWriter = new BufferedWriter(writer);
				bWriter.write(debug);
				bWriter.newLine();
				bWriter.write("=======================================");
				bWriter.newLine();
//				writer.write("\n");
//				
//				writer.write("\n");
				HcLog.D(" #writeDebug write end debug = "+debug);
//				HcLog.E(" #writeError ="+debug);
				
			} catch (Exception e) {
				// TODO: handle exception
				HcLog.D(" #writeDebug write error = "+e);
			} finally {
				if (null != bWriter) {
					try {
						bWriter.close();
					} catch (Exception e2) {
						// TODO: handle exception
						// do nothing
					}
				}
				if (null != writer) {
					try {
						writer.close();
					} catch (Exception e2) {
						// TODO: handle exception
						// do nothing
					}
				}
			}
			 
		}
	}
	
	private static File getCacheDirectory(Context context) {
		File cacheDir = StorageUtils.getCacheDirectory(context);
		File individualCacheDir = new File(cacheDir, "log");
		if (!individualCacheDir.exists()) {
			if (!individualCacheDir.mkdir()) {
				individualCacheDir = cacheDir;
			}
		}
		return individualCacheDir;
	}

	public static void error(String tag, Exception e) {
		if (DEBUG) {
			StringBuilder builder = new StringBuilder(tag + " error=");
			Throwable cause = e.getCause();
			if (cause != null) {
				StackTraceElement[] elements = cause.getStackTrace();
				for (StackTraceElement stackTraceElement : elements) {
					builder.append("causeBy:" + stackTraceElement.toString());
					builder.append("\n");
				}
				D(builder.toString());
			}
		}
	}

	public static void D(boolean debug, String message) {
		if (debug) {
//			if (null == mPackageName) {
//				mPackageName = HcApplication.getContext().getPackageName();
//			}
			Log.d(TAG, mPackageName + " " + message);

		}
	}
}
