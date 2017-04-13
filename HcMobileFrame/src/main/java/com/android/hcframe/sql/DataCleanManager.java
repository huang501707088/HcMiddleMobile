package com.android.hcframe.sql;

/*  * 文 件 名:  DataCleanManager.java  * 描    述:  主要功能有清除内/外缓存，清除数据库，清除sharedPreference，清除files和清除自定义目录  */

import java.io.File;
import java.text.DecimalFormat;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Environment;

/** * 本应用数据清除管理器 */
public class DataCleanManager {
	/** * 清除本应用内部缓存(/data/data/com.xxx.xxx/cache) * * @param context */
	public static long cleanInternalCache(Context context, boolean isDelete) {
		return deleteFilesByDirectory(context.getCacheDir(), isDelete);
	}

	/** * 清除本应用所有数据库(/data/data/com.xxx.xxx/databases) * * @param context */
	public static long cleanDatabases(Context context, boolean isDelete) {
		return deleteFilesByDirectory(
				new File("/data/data/" + context.getPackageName()
						+ "/databases"), isDelete);
	}

	/**
	 * * 清除本应用SharedPreference(/data/data/com.xxx.xxx/shared_prefs) * * @param
	 * context
	 */
	public static long cleanSharedPreference(Context context, boolean isDelete) {
		return deleteFilesByDirectory(
				new File("/data/data/" + context.getPackageName()
						+ "/shared_prefs"), isDelete);
	}

	/** * 按名字清除本应用数据库 * * @param context * @param dbName */
	public static long cleanDatabaseByName(Context context, String dbName,
			boolean isDelete) {
		return deleteFilesByDirectory(
				new File("/data/data/" + context.getPackageName()
						+ "/databases/" + dbName), isDelete);
	}

	/** * 清除/data/data/com.xxx.xxx/files下的内容 * * @param context */
	public static long cleanFiles(Context context, boolean isDelete) {
		return deleteFilesByDirectory(context.getFilesDir(), isDelete);
	}

	/**
	 * * 清除外部cache下的内容(/mnt/sdcard/android/data/com.xxx.xxx/cache) * * @param
	 * context
	 */
	public static long cleanExternalCache(Context context, boolean isDelete) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return deleteFilesByDirectory(context.getExternalCacheDir(),
					isDelete);
		}
		return 0;
	}

	/** * 清除自定义路径下的文件，使用需小心，请不要误删。而且只支持目录下的文件删除 * * @param filePath */
	public static long cleanCustomCache(String filePath, boolean isDelete) {
		return deleteFilesByDirectory(new File(filePath), isDelete);
	}

	/** * 清除本应用所有的数据 * * @param context * @param filepath */
	public static String cleanApplicationData(Context context,
			boolean isDelete, String... filepath) {
		long totalSize = 0;
		totalSize += cleanInternalCache(context, isDelete);
		totalSize += cleanExternalCache(context, isDelete);
//		totalSize += cleanDatabases(context, isDelete);
		totalSize += cleanSharedPreference(context, isDelete);
		totalSize += cleanFiles(context, isDelete);
		for (String filePath : filepath) {
			totalSize += cleanCustomCache(filePath, isDelete);
		}
		if (isDelete) {
//			SettingHelper.clearData(context);
			OperateDatabase.clearDatabaseByAccount(context);		
		}
		return formetFileSize(totalSize);
	}

	private static long deleteFilesByDirectory(File directory, boolean isDelete) {
		long fileS = 0;
		if (directory != null && directory.exists()) {
			if (directory.isDirectory()) {
				for (File item : directory.listFiles()) {
					if (item.isFile()) {
						if (isDelete) {
							item.delete();
						}
						fileS = fileS + item.length();
					} else {
						fileS = fileS + deleteFilesByDirectory(item, isDelete);
					}
				}
			} else {
				fileS = fileS + directory.length();
				if (isDelete) {
					directory.delete();
				}
			}
		}
		return fileS;
	}

	private static String formetFileSize(long fileS) {
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "K";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "M";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "G";
		}
		return fileSizeString;
	}
}