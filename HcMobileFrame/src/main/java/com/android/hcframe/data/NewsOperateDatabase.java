/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2016-1-26 上午9:11:45
*/
package com.android.hcframe.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcLog;
import com.android.hcframe.sql.HcProvider;
import com.android.hcframe.sql.HcDatabase.NewsList;
import com.android.hcframe.sql.SettingHelper;

public final class NewsOperateDatabase {

	private NewsOperateDatabase() {}
	
	/**
	 * 删除对应用户下面的新闻数据
	 * @author jrjin
	 * @time 2016-1-18 上午10:48:11
	 * @param context
	 */
	public static void deleteNewsList(Context context) {
		final ContentResolver cr = context.getContentResolver();
		String where = NewsList.ACCOUNT + "=" + "'"
				+ SettingHelper.getAccount(context) + "'";
		cr.delete(HcProvider.CONTENT_URI_ANNUAL_PROGRAM, where, null);
	}
	
	/**
	 * 获取当前栏目下新闻列表
	 * @author jrjin
	 * @time 2016-1-12 下午1:54:47
	 * @param context
	 * @param columnId 新闻栏目编号
	 * @return
	 */
	public static List<NewsInfo> getNewsInfos(Context context, String columnId, boolean scroll) {
		List<NewsInfo> infos = new ArrayList<NewsInfo>();
		final ContentResolver cr = context.getContentResolver();
		String[] projection = {NewsList.CONTENT_TYPE, NewsList.NEWS_ADDRESS, NewsList.NEWS_CONTENT,
				NewsList.NEWS_CONTENT_URI, NewsList.NEWS_DATA, NewsList.NEWS_ICON_URI,
				NewsList.NEWS_PIC_COUNT, NewsList.NEWS_TITLE, /*NewsList.SCROLL,*/ 
				NewsList.NEWS_ID};
		String selection = NewsList.ACCOUNT + "=" + "'"
						+ SettingHelper.getAccount(context) + "'" + " AND " + 
						NewsList.NEWS_COLUMN_ID + "=" + "'" + columnId +"'" + " AND " +NewsList.SCROLL + "=" + (scroll == true ? 1 : 0);
		Cursor c = cr.query(HcProvider.CONTENT_URI_NEWS_LIST, projection, selection, null, null);
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
			NewsInfo info = null;
			while (!c.isAfterLast()) {
				info = new NewsInfo();
				info.mAddress = c.getString(1);
				info.mContentType = "" + c.getInt(0);
				info.mContentUrl = c.getString(3);
				info.mCount = c.getInt(6);
				info.mDate = c.getString(4);
				info.mId = c.getString(8);
				info.mTitle = c.getString(7);
				info.newsSummary = c.getString(2);
				info.mScroll = scroll;//c.getInt(8) == 1;
				if (c.getInt(0) == 3) {// 新闻图片
					info.mImgs.addAll(Arrays.asList(c.getString(5).split(";")));
				} else {
					info.mIconUrl = c.getString(5);
				}
				infos.add(info);
				c.moveToNext();
			}
		}
		if (c != null)
			c.close();
		return infos;
	}
	
	/**
	 * 添加新闻数据
	 * @author jrjin
	 * @time 2016-1-12 下午2:33:06
	 * @param context
	 * @param infos 年会节目列表
	 * @param
	 */
	public static int insertNewsList(Context context, List<NewsInfo> infos, String columnId, boolean scroll) {
		if (infos == null || infos.size() == 0) return 0;
//		final ContentValues values = new ContentValues();
		final ContentResolver cr = context.getContentResolver();
		String where = NewsList.ACCOUNT + "=" + "'"
				+ SettingHelper.getAccount(context) + "'" + " AND " + 
				NewsList.NEWS_COLUMN_ID + "=" + "'" + columnId +"'" + " AND " + 
				NewsList.SCROLL + "=" + (scroll == true ? 1 : 0);
		cr.delete(HcProvider.CONTENT_URI_NEWS_LIST, where, null);
//		for (NewsInfo info : infos) {
//			insertNewsInfo(info, cr, values, columnId);
//		}
		
		int size = infos.size();
		ContentValues[] values = new ContentValues[size];
		for (int i = 0; i < size; i++) {
			values[i] = new ContentValues();
			setNewsValue(infos.get(i), values[i], columnId);
		}
		
		int number = cr.bulkInsert(HcProvider.CONTENT_URI_NEWS_LIST, values);
		HcLog.D("NewsOperateDatabase#insertNewsList insert size = "+size + " success number = "+number);
		return number;
	}
	
	private static void setNewsValue(NewsInfo info, ContentValues values, String columnId) {
		values.clear();
		values.put(NewsList.ACCOUNT,
				SettingHelper.getAccount(HcApplication.getContext()));
		if (TextUtils.isEmpty(info.mContentType)) {
			values.put(NewsList.CONTENT_TYPE, 0);
			values.put(NewsList.NEWS_ICON_URI, info.mIconUrl);
		} else {
			values.put(NewsList.CONTENT_TYPE, Integer.valueOf(info.mContentType));
			if ("3".equals(info.mContentType) && info.mImgs.size() > 0) {
				StringBuilder builder = new StringBuilder();
				for (String iconUri : info.mImgs) {
					builder.append(iconUri + ";");
				}
				String icons = builder.toString();
				icons = icons.substring(0, icons.length() - 1);
				values.put(NewsList.NEWS_ICON_URI, icons);
			} else {
				values.put(NewsList.NEWS_ICON_URI, info.mIconUrl);
			}
		}
		
		values.put(NewsList.NEWS_ADDRESS, info.mAddress);
		values.put(NewsList.NEWS_COLUMN_ID, columnId);
		values.put(NewsList.NEWS_CONTENT, info.newsSummary);
		values.put(NewsList.NEWS_CONTENT_URI, info.mContentUrl);
		values.put(NewsList.NEWS_DATA, info.mDate);
		values.put(NewsList.NEWS_ID, info.mId);
		values.put(NewsList.NEWS_PIC_COUNT, info.mCount);
		values.put(NewsList.NEWS_TITLE, info.mTitle);
		values.put(NewsList.SCROLL, info.mScroll == true ? 1 : 0);
		
	}
	
	private static Uri insertNewsInfo(NewsInfo info, ContentResolver cr,
			ContentValues values, String columnId) {
		values.clear();
		values.put(NewsList.ACCOUNT,
				SettingHelper.getAccount(HcApplication.getContext()));
		if (TextUtils.isEmpty(info.mContentType)) {
			values.put(NewsList.CONTENT_TYPE, 0);
			values.put(NewsList.NEWS_ICON_URI, info.mIconUrl);
		} else {
			values.put(NewsList.CONTENT_TYPE, Integer.valueOf(info.mContentType));
			if ("3".equals(info.mContentType)) {
				StringBuilder builder = new StringBuilder();
				for (String iconUri : info.mImgs) {
					builder.append(iconUri + ";");
				}
				String icons = builder.toString();
				icons = icons.substring(0, icons.length() - 1);
				values.put(NewsList.NEWS_ICON_URI, icons);
			} else {
				values.put(NewsList.NEWS_ICON_URI, info.mIconUrl);
			}
		}
		
		values.put(NewsList.NEWS_ADDRESS, info.mAddress);
		values.put(NewsList.NEWS_COLUMN_ID, columnId);
		values.put(NewsList.NEWS_CONTENT, info.newsSummary);
		values.put(NewsList.NEWS_CONTENT_URI, info.mContentUrl);
		values.put(NewsList.NEWS_DATA, info.mDate);
		values.put(NewsList.NEWS_ID, info.mId);
		values.put(NewsList.NEWS_PIC_COUNT, info.mCount);
		values.put(NewsList.NEWS_TITLE, info.mTitle);
		values.put(NewsList.SCROLL, info.mScroll == true ? 1 : 0);
		
		return cr.insert(HcProvider.CONTENT_URI_NEWS_LIST, values);
	}
}
