/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-4-27 上午10:54:11
 */
package com.android.hcframe.sql;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.data.NewsColumn;
import com.android.hcframe.doc.data.DocColumn;
import com.android.hcframe.doc.data.DocFileInfo;
import com.android.hcframe.doc.data.DocHistoricalRecord;
import com.android.hcframe.doc.data.DocInfo;
import com.android.hcframe.sql.HcDatabase.DataRecord;
import com.android.hcframe.sql.HcDatabase.DataRecordDetail;
import com.android.hcframe.sql.HcDatabase.HcNewsColumen;
import com.android.hcframe.sql.HcDatabase.SysMessage;

import com.android.hcframe.sys.SystemMessage;
import com.android.hcframe.view.selector.DepInfo;
import com.android.hcframe.view.selector.ItemInfo;
import com.android.hcframe.view.selector.StaffInfo;

public final class OperateDatabase {

	private static final String TAG = "OperateDatabase";

	private static final boolean USED = true;

	private static boolean usedAccount = USED;

	private OperateDatabase() {
	}



	/**
	 * 添加新闻栏目
	 * @author jrjin
	 * @time 2016-1-28 上午11:53:26
	 * @param newss
	 * @param context
	 */
	public static void insertNewss(List<NewsColumn> newss, Context context) {
		final ContentValues values = new ContentValues();
		final ContentResolver cr = context.getContentResolver();
		String where = usedAccount == false ? HcNewsColumen.COLUMN_TYPE + "=0"
				: HcNewsColumen.COLUMN_TYPE + "=0" + " AND "
						+ HcNewsColumen.NEWS_USER + "=" + "'"
						+ SettingHelper.getAccount(context) + "'";
		cr.delete(HcProvider.CONTENT_URI_NEWS, where, null);
		for (NewsColumn nc : newss) {
			insertNews(nc, values, cr, context);
		}

	}

	private static Uri insertNews(NewsColumn info, ContentValues values,
			ContentResolver cr, Context context) {
		values.clear();
		values.put(HcNewsColumen.NEWSID, info.getNewsId());
		values.put(HcNewsColumen.CONTENTTYPE, info.getmContenttype());
		values.put(HcNewsColumen.NAME, info.getmName());
		values.put(HcNewsColumen.TYPE, info.getmType());
		values.put(HcNewsColumen.ISSCROLLTOPIC, info.getIsSrolltopic());
		values.put(HcNewsColumen.NEWS_USER, SettingHelper.getAccount(context));
		values.put(HcNewsColumen.COLUMN_TYPE, 0);
		return cr.insert(HcProvider.CONTENT_URI_NEWS, values);

	}

	public static List<NewsColumn> queryNews(Context context) {
		List<NewsColumn> infos = new ArrayList<NewsColumn>();
		final ContentResolver cr = context.getContentResolver();
		String selection = usedAccount == false ? HcNewsColumen.COLUMN_TYPE
				+ "=0" : HcNewsColumen.COLUMN_TYPE + "=0" + " AND "
				+ HcNewsColumen.NEWS_USER + "=" + "'"
				+ SettingHelper.getAccount(context) + "'";
		Cursor cursor = cr.query(HcProvider.CONTENT_URI_NEWS, null, selection,
				null, null);
		NewsColumn newsColumen;
		while (cursor.moveToNext()) {
			newsColumen = new NewsColumn();

			newsColumen.setNewsId(cursor.getString(cursor
					.getColumnIndex(HcNewsColumen.NEWSID)));
			newsColumen.setmName(cursor.getString(cursor
					.getColumnIndex(HcNewsColumen.NAME)));
			newsColumen.setmType(cursor.getInt(cursor
					.getColumnIndex(HcNewsColumen.TYPE)));
			newsColumen.setIsSrolltopic(cursor.getInt(cursor
					.getColumnIndex(HcNewsColumen.ISSCROLLTOPIC)));
			newsColumen.setmContenttype(cursor.getInt(cursor
					.getColumnIndex(HcNewsColumen.CONTENTTYPE)));

			infos.add(newsColumen);
		}
		cursor.close();
		return infos;
	}

	/**
	 * 获取历史记录，按时间最近读取排序
	 * 
	 * @author jrjin
	 * @time 2015-8-30 上午11:38:04
	 * @param context
	 * @return 历史已读资料列表
	 */
	public static List<DocHistoricalRecord> getHistoricalRecords(Context context) {
		List<DocHistoricalRecord> records = new ArrayList<DocHistoricalRecord>();
		final ContentResolver cr = context.getContentResolver();
		String selection = usedAccount == false ? null : DataRecord.ACCOUNT
				+ "="
				+ "'---'"
				+ (HcUtil.isEmpty(SettingHelper.getAccount(context)) ? ""
						: (" or " + DataRecord.ACCOUNT + "='" + SettingHelper
								.getAccount(context)) + "'");
		Cursor c = cr.query(HcProvider.CONTENT_URI_DATA_RECORD, null,
                selection, null, DataRecord.DATA_READ_TIME + " DESC");
		if (c != null && c.getCount() > 0) {
			DocHistoricalRecord record;
			c.moveToFirst();
			List<String> fileIds = new ArrayList<String>();
			while (!c.isAfterLast()) {
				record = new DocHistoricalRecord();
				record.setFileId(c.getString(c
						.getColumnIndex(DataRecord.DATA_ID)));
				record.setFileName(c.getString(c
						.getColumnIndex(DataRecord.DATA_NAME)));
				record.setmDate(c.getString(c
						.getColumnIndex(DataRecord.DATA_DATE)));
				record.setFileSize(c.getInt(c
						.getColumnIndex(DataRecord.DATA_SIZE)));
				record.setFlag(c.getInt(c.getColumnIndex(DataRecord.DATA_FLAG)));
				record.setReadTime(""
						+ c.getInt(c.getColumnIndex(DataRecord.DATA_READ_TIME)));
				if (!fileIds.contains(record.getFileId())) {
					records.add(record);
					fileIds.add(record.getFileId());
				}
				c.moveToNext();
			}
		}
		if (c != null)
			c.close();
		return records;
	}

	/**
	 * 获取资料详情，要是类型不是0，则要先查询出资料编号
	 * 
	 * @author jrjin
	 * @time 2015-8-30 上午11:58:17
	 * @param context
	 * @param id
	 *            资料编号或者文件编号
	 * @param flag
	 *            资料显示类型：0—标题，1—主文件，2—附件
	 * @return 返回资料编号对应的详情
	 */
	public static DocInfo getHistoricalRecordDetail(Context context, String id,
			int flag) {
		DocInfo info = new DocInfo();
		final ContentResolver cr = context.getContentResolver();
		Cursor c;
		if (flag == DocInfo.FLAG_TITIL) {
			String where = usedAccount == false ? DataRecordDetail.DATA_ID
					+ "=?" : DataRecordDetail.DATA_ID + "=?" + " AND "
					+ DataRecordDetail.ACCOUNT + "=" + "'"
					+ SettingHelper.getAccount(context) + "'";
			c = cr.query(HcProvider.CONTENT_URI_DATA_RECORD_DETAIL, null,
					where, new String[] { id }, null);
			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				info.setDataId(c.getString(c
						.getColumnIndex(DataRecordDetail.DATA_ID)));
				info.setDataSource(c.getString(c
						.getColumnIndex(DataRecordDetail.DATA_SOURCE)));
				info.setDataTitle(c.getString(c
						.getColumnIndex(DataRecordDetail.DATA_NAME)));
				info.setDate(c.getString(c
						.getColumnIndex(DataRecordDetail.DATA_TIME)));
				info.setFlag(DocInfo.FLAG_TITIL);
				DocFileInfo fileInfo;
				while (!c.isAfterLast()) {
					fileInfo = new DocFileInfo();
					fileInfo.setFileId(c.getString(c
							.getColumnIndex(DataRecordDetail.FILE_ID)));
					fileInfo.setFileName(c.getString(c
							.getColumnIndex(DataRecordDetail.FILE_NAME)));
					fileInfo.setFileSize(c.getInt(c
							.getColumnIndex(DataRecordDetail.FILE_SIZE)));
					fileInfo.setFileUrl(c.getString(c
							.getColumnIndex(DataRecordDetail.FILE_URL)));
					fileInfo.setFlag(c.getInt(c
							.getColumnIndex(DataRecordDetail.DATA_FLAG)));
					info.addDocInfo(fileInfo);
					c.moveToNext();
				}
			}
		} else {
			String columns[] = { DataRecordDetail.DATA_ID };
			String where = usedAccount == false ? DataRecordDetail.FILE_ID
					+ "=?" : DataRecordDetail.FILE_ID + "=?" + " AND "
					+ DataRecordDetail.ACCOUNT + "=" + "'"
					+ SettingHelper.getAccount(context) + "'";
			c = cr.query(HcProvider.CONTENT_URI_DATA_RECORD_DETAIL, columns,
					where, new String[] { id }, null);
			String dataId = null;
			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				dataId = c.getString(0);
			}
			if (c != null)
				c.close();
			if (dataId != null) {
				where = usedAccount == false ? DataRecordDetail.DATA_ID + "=?"
						: DataRecordDetail.DATA_ID + "=?" + " AND "
								+ DataRecordDetail.ACCOUNT + "=" + "'"
								+ SettingHelper.getAccount(context) + "'";
				c = cr.query(HcProvider.CONTENT_URI_DATA_RECORD_DETAIL, null,
						where, new String[] { dataId }, null);
				if (c != null && c.getCount() > 0) {
					c.moveToFirst();
					info.setDataId(c.getString(c
							.getColumnIndex(DataRecordDetail.DATA_ID)));
					info.setDataSource(c.getString(c
							.getColumnIndex(DataRecordDetail.DATA_SOURCE)));
					info.setDataTitle(c.getString(c
							.getColumnIndex(DataRecordDetail.DATA_NAME)));
					info.setDate(c.getString(c
							.getColumnIndex(DataRecordDetail.DATA_TIME)));
					info.setFlag(DocInfo.FLAG_TITIL);
					DocFileInfo fileInfo;
					while (!c.isAfterLast()) {
						fileInfo = new DocFileInfo();
						fileInfo.setFileId(c.getString(c
								.getColumnIndex(DataRecordDetail.FILE_ID)));
						fileInfo.setFileName(c.getString(c
								.getColumnIndex(DataRecordDetail.FILE_NAME)));
						fileInfo.setFileSize(c.getInt(c
								.getColumnIndex(DataRecordDetail.FILE_SIZE)));
						fileInfo.setFileUrl(c.getString(c
								.getColumnIndex(DataRecordDetail.FILE_URL)));
						fileInfo.setFlag(c.getInt(c
								.getColumnIndex(DataRecordDetail.DATA_FLAG)));
						info.addDocInfo(fileInfo);
						c.moveToNext();
					}
				}
			}
		}
		if (c != null)
			c.close();
		return info;
	}

	/**
	 * 添加已读数据的记录
	 * 
	 * @author jrjin
	 * @time 2015-8-30 下午2:01:20
	 * @param context
	 * @param records
	 */
	public static int insertDataRecords(Context context,
			List<DocHistoricalRecord> records) {
		if (records == null || records.size() == 0)
			return 0; // 数据空就不用操作数据库了
//		final ContentValues values = new ContentValues();
		final ContentResolver cr = context.getContentResolver();
		List<String> accounts = new ArrayList<String>();
		for (int i = 0; i < records.size(); i++) {
			if (!accounts.contains(records.get(i).getUsername())) {
				accounts.add(records.get(i).getUsername());
				String where = usedAccount == false ? null : DataRecord.ACCOUNT
						+ "=" + "'" + records.get(i).getUsername() + "'";
				cr.delete(HcProvider.CONTENT_URI_DATA_RECORD, where, null);
			}
		}

		/**
		 * @date 2016-1-28 上午11:54:34
		 * 替换为
		for (DocHistoricalRecord record : records) {
			insertDataRecord(record, values, cr);
		}
		*/
		int size = records.size();
		ContentValues[] values = new ContentValues[size];
		for (int i = 0; i < size; i++) {
			values[i] = new ContentValues();
			setDataRecordValue(records.get(i), values[i]);
		}
		int number = cr.bulkInsert(HcProvider.CONTENT_URI_DATA_RECORD, values);
		HcLog.D(TAG + " #insertDataRecords insert number = " + number);
		return number;
	}

	private static void setDataRecordValue(DocHistoricalRecord record,
			ContentValues values) {
		HcLog.D(TAG + " insertDataRecord data id = " + record.getFileId()
				+ " data flag = " + record.getFlag());
		values.clear();
		values.put(DataRecord.ACCOUNT, record.getUsername());
		values.put(DataRecord.DATA_FLAG, record.getFlag());
		values.put(DataRecord.DATA_ID, record.getFileId());
		values.put(DataRecord.DATA_NAME, record.getFileName());
		values.put(DataRecord.DATA_READ_TIME,
				Long.valueOf(record.getReadTime()));
		values.put(DataRecord.DATA_SIZE, record.getFileSize());
		
		values.put(DataRecord.DATA_DATE, record.getmDate());
	}
	
	private static Uri insertDataRecord(DocHistoricalRecord record,
			ContentValues values, ContentResolver cr) {
		HcLog.D(TAG + " insertDataRecord data id = " + record.getFileId()
				+ " data flag = " + record.getFlag());
		values.clear();
		values.put(DataRecord.ACCOUNT, record.getUsername());
		values.put(DataRecord.DATA_FLAG, record.getFlag());
		values.put(DataRecord.DATA_ID, record.getFileId());
		values.put(DataRecord.DATA_NAME, record.getFileName());
		values.put(DataRecord.DATA_READ_TIME,
				Long.valueOf(record.getReadTime()));
		values.put(DataRecord.DATA_SIZE, record.getFileSize());
		
		values.put(DataRecord.DATA_DATE, record.getmDate());
		return cr.insert(HcProvider.CONTENT_URI_DATA_RECORD, values);
	}

	/**
	 * 添加已阅读的资料详情
	 * 
	 * @author jrjin
	 * @time 2015-8-30 下午2:12:57
	 * @param context
	 * @param info
	 *            已阅读的资料详情
	 */
	public static void insertDataRecordDetail(Context context, DocInfo info) {
		if (info == null)
			return; // 数据空就不用操作数据库了
		final ContentValues values = new ContentValues();
		final ContentResolver cr = context.getContentResolver();
		String where = usedAccount == false ? DataRecordDetail.DATA_ID + "=?"
				: DataRecordDetail.DATA_ID + "=?" + " AND "
						+ DataRecordDetail.ACCOUNT + "=" + "'"
						+ SettingHelper.getAccount(context) + "'";
		Cursor c = cr.query(HcProvider.CONTENT_URI_DATA_RECORD_DETAIL, null,
				where, new String[] { info.getDataId() }, null);
		if (c != null && c.getCount() > 0) {
			c.close();
			return;
		}
		if (c != null)
			c.close();
		// cr.delete(HcProvider.CONTENT_URI_DATA_RECORD_DETAIL, where, new
		// String[] { info.getDataId() });
		for (DocFileInfo fileInfo : info.getDocInfos()) {
			values.clear();
			values.put(DataRecordDetail.ACCOUNT,
					SettingHelper.getAccount(context));
			values.put(DataRecordDetail.DATA_FLAG, fileInfo.getFlag());
			values.put(DataRecordDetail.DATA_ID, info.getDataId());
			values.put(DataRecordDetail.DATA_NAME, info.getDataTitle());
			values.put(DataRecordDetail.DATA_SOURCE, info.getDataSource());
			values.put(DataRecordDetail.DATA_TIME, info.getDate());
			values.put(DataRecordDetail.FILE_ID, fileInfo.getFileId());
			values.put(DataRecordDetail.FILE_NAME, fileInfo.getFileName());
			values.put(DataRecordDetail.FILE_SIZE, fileInfo.getFileSize());
			values.put(DataRecordDetail.FILE_URL, fileInfo.getFileUrl());
			cr.insert(HcProvider.CONTENT_URI_DATA_RECORD_DETAIL, values);
		}
	}

	/**
	 * 添加资料中心栏目数据
	 * 
	 * @author jrjin
	 * @time 2015-8-31 下午8:04:24
	 * @param columns
	 * @param context
	 */
	public static void insertDocColumns(List<DocColumn> columns, Context context) {
		if (columns == null || columns.isEmpty())
			return;
		final ContentValues values = new ContentValues();
		final ContentResolver cr = context.getContentResolver();
		String where = usedAccount == false ? HcNewsColumen.COLUMN_TYPE + "=1"
				: HcNewsColumen.COLUMN_TYPE + "=1" + " AND "
						+ HcNewsColumen.NEWS_USER + "=" + "'"
						+ SettingHelper.getAccount(context) + "'";
		cr.delete(HcProvider.CONTENT_URI_NEWS, where, null);
		for (DocColumn column : columns) {
			insertDocColumn(column, cr, values);
		}
	}

	private static Uri insertDocColumn(DocColumn column, ContentResolver cr,
			ContentValues values) {
		values.clear();
		values.put(HcNewsColumen.NEWSID, column.getNewsId());
		values.put(HcNewsColumen.NAME, column.getmName());
		values.put(HcNewsColumen.NEWS_USER,
				SettingHelper.getAccount(HcApplication.getContext()));
		values.put(HcNewsColumen.COLUMN_TYPE, 1);
		return cr.insert(HcProvider.CONTENT_URI_NEWS, values);
	}

	public static List<DocColumn> getDocColumns(Context context) {
		List<DocColumn> columns = new ArrayList<DocColumn>();
		final ContentResolver cr = context.getContentResolver();
		String selection = usedAccount == false ? HcNewsColumen.COLUMN_TYPE
				+ "=1" : HcNewsColumen.COLUMN_TYPE + "=1" + " AND "
				+ HcNewsColumen.NEWS_USER + "=" + "'"
				+ SettingHelper.getAccount(context) + "'";
		Cursor cursor = cr.query(HcProvider.CONTENT_URI_NEWS, null, selection,
				null, null);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			DocColumn column;
			while (!cursor.isAfterLast()) {
				column = new DocColumn();
				column.setNewsId(cursor.getString(cursor
						.getColumnIndex(HcNewsColumen.NEWSID)));
				column.setmName(cursor.getString(cursor
						.getColumnIndex(HcNewsColumen.NAME)));
				columns.add(column);
				cursor.moveToNext();
			}
		}
		if (cursor != null)
			cursor.close();
		return columns;
	}

	
	/**
	 * 获取当前用户的系统消息
	 * @author jrjin
	 * @time 2015-12-3 下午4:28:48
	 * @param context
	 * @param count 返回的条数
	 * @return 当前用户的系统消息
	 */
	public static List<SystemMessage> getSystemMessages(Context context, int count) {
		List<SystemMessage> messages = new ArrayList<SystemMessage>();
		final ContentResolver cr = context.getContentResolver();
		String[] projection = {SysMessage.APP_ID, SysMessage.CONTENT, SysMessage.CONTENT_ID,
					SysMessage.DATE, SysMessage.READ, SysMessage.TITLE, SysMessage.TYPE, SysMessage.APP_TYPE,
					SysMessage.INDEX_CONTENT, SysMessage._ID, SysMessage.APP_NAME};
		String selection = usedAccount == false ? /*"LIMIT " + count*/null
				: SysMessage.ACCOUNT + "=" + "'"
						+ SettingHelper.getAccount(context) + "'"/* + " LIMIT " + count*/;
		Cursor c = cr.query(HcProvider.CONTENT_URI_SYSTEM_MASSAGE, projection, selection, null, SysMessage._ID + " DESC " + "LIMIT " + count);
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
			SystemMessage message = null;
			while (!c.isAfterLast()) {
				message = new SystemMessage();
				message.setAppId(c.getString(0));
				message.setContent(c.getString(1));
				message.setContentId(c.getString(2));
				message.setDate(c.getString(3));
				message.setReaded(c.getInt(4) == 0);
				message.setTitle(c.getString(5));
				message.setType(c.getInt(6));
				message.setAppType(c.getInt(7));
				message.setIndexContent(c.getString(8));
				message.setMessageId(c.getInt(9));
				message.setAppName(c.getString(10));
				messages.add(message);
				c.moveToNext();
			}
		}
		if (c != null)
			c.close();
		return messages;
	}
	
	/**
	 * 插入新的系统消息
	 * @author jrjin
	 * @time 2015-12-3 下午4:34:57
	 * @param message
	 * @param context
	 * @return
	 */
	public static Uri insertSysMessage(SystemMessage message, Context context) {
		final ContentValues values = new ContentValues();
		final ContentResolver cr = context.getContentResolver();
		values.put(SysMessage.ACCOUNT, SettingHelper.getAccount(context));
		values.put(SysMessage.APP_ID, message.getAppId());
		values.put(SysMessage.CONTENT, message.getContent());
		values.put(SysMessage.CONTENT_ID, message.getContentId());
		values.put(SysMessage.DATE, message.getDate());
		values.put(SysMessage.READ, message.getReaded() ? 0 : 1);
		values.put(SysMessage.TITLE, message.getTitle());
		values.put(SysMessage.TYPE, message.getType());
		values.put(SysMessage.APP_TYPE, message.getAppType());
		values.put(SysMessage.INDEX_CONTENT, message.getIndexContent());
		values.put(SysMessage.APP_NAME, message.getAppName());
		return cr.insert(HcProvider.CONTENT_URI_SYSTEM_MASSAGE, values);
	}
	
	/**
	 * 更新到已读状态
	 * @author jrjin
	 * @time 2015-12-3 下午4:54:40
	 * @param message
	 * @param context
	 * @return
	 * @deprecated
	 */
	public static int updateSysMessage(SystemMessage message, Context context) {
		final ContentValues values = new ContentValues();
		final ContentResolver cr = context.getContentResolver();
		String where = usedAccount == false ? SysMessage.APP_ID + "=" +message.getAppId() :
				SysMessage.APP_ID + "=" +message.getAppId()
				+ " AND " +SysMessage.ACCOUNT + "=" + "'"
						+ SettingHelper.getAccount(context) + "'";
		HcLog.D(TAG + " updateSysMessage start! where = " + where);
		values.put(SysMessage.READ, 0);
		values.put(SysMessage.INDEX_CONTENT, message.getIndexContent());
		values.put(SysMessage.APP_TYPE, message.getAppType());
		values.put(SysMessage.APP_NAME, message.getAppName());
		int num = cr.update(HcProvider.CONTENT_URI_SYSTEM_MASSAGE, values, where, null);
		HcLog.D(TAG + " updateSysMessage update num = " + num);
		return num;
	}
	
	/**
	 * 清空数据库,这里没有根据用户
	 * @author jrjin
	 * @time 2016-1-26 上午9:15:46
	 * @param context
	 */
	public static void clearDatabase(Context context) {
		final ContentResolver cr = context.getContentResolver();
		cr.delete(HcProvider.CONTENT_URI_ANNUAL_PROGRAM, null, null);
		cr.delete(HcProvider.CONTENT_URI_APP, null, null);
		cr.delete(HcProvider.CONTENT_URI_CONTACTS, null, null);
		cr.delete(HcProvider.CONTENT_URI_DATA_RECORD, null, null);
		cr.delete(HcProvider.CONTENT_URI_DATA_RECORD_DETAIL, null, null);
		cr.delete(HcProvider.CONTENT_URI_NEWS, null, null);
		cr.delete(HcProvider.CONTENT_URI_SYSTEM_MASSAGE, null, null);
		cr.delete(HcProvider.CONTENT_URI_NEWS_LIST, null, null);
	}
	
	/**
	 * 清空相应用户下的数据库
	 * @author jrjin
	 * @time 2016-1-26 上午9:17:27
	 * @param context
	 */
	public static void clearDatabaseByAccount(Context context) {
		final ContentResolver cr = context.getContentResolver();
		String where = "account=" + "'" + SettingHelper.getAccount(context) + "'";
		cr.delete(HcProvider.CONTENT_URI_ANNUAL_PROGRAM, where, null);
		cr.delete(HcProvider.CONTENT_URI_APP, where, null);
		cr.delete(HcProvider.CONTENT_URI_CONTACTS, null, null);
		cr.delete(HcProvider.CONTENT_URI_DATA_RECORD, where, null);
		cr.delete(HcProvider.CONTENT_URI_DATA_RECORD_DETAIL, where, null);
		cr.delete(HcProvider.CONTENT_URI_NEWS, where, null);
		cr.delete(HcProvider.CONTENT_URI_SYSTEM_MASSAGE, where, null);
		cr.delete(HcProvider.CONTENT_URI_NEWS_LIST, where, null);
	}

	/**
	 * 获取当前用户对应应用的系统消息
	 * @author jrjin
	 * @time 2015-12-3 下午4:28:48
	 * @param context
	 * @param appId 应用id
	 * @return 当前用户的系统消息
	 */
	public static List<SystemMessage> getSystemMessages(Context context, String appId) {
		List<SystemMessage> messages = new ArrayList<SystemMessage>();
		final ContentResolver cr = context.getContentResolver();
		String[] projection = {SysMessage.APP_ID, SysMessage.CONTENT, SysMessage.CONTENT_ID,
				SysMessage.DATE, SysMessage.READ, SysMessage.TITLE, SysMessage.TYPE, SysMessage.APP_TYPE,
				SysMessage.INDEX_CONTENT, SysMessage._ID, SysMessage.APP_NAME};
		String selection = usedAccount == false ? SysMessage.APP_ID + "=" + "'" + appId + "'"
				: SysMessage.APP_ID + "=" + "'" + appId + "'" + " AND " + SysMessage.ACCOUNT + "=" + "'"
				+ SettingHelper.getAccount(context) + "'";
		Cursor c = cr.query(HcProvider.CONTENT_URI_SYSTEM_MASSAGE, projection, selection, null, SysMessage._ID + " DESC ");
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
			SystemMessage message = null;
			while (!c.isAfterLast()) {
				message = new SystemMessage();
				message.setAppId(c.getString(0));
				message.setContent(c.getString(1));
				message.setContentId(c.getString(2));
				message.setDate(c.getString(3));
				message.setReaded(c.getInt(4) == 0);
				message.setTitle(c.getString(5));
				message.setType(c.getInt(6));
				message.setAppType(c.getInt(7));
				message.setIndexContent(c.getString(8));
				message.setMessageId(c.getInt(9));
				message.setAppName(c.getString(10));
				messages.add(message);
				c.moveToNext();
			}
		}
		if (c != null)
			c.close();
		HcLog.D(TAG + " #getSystemMessages size = "+messages.size() + " appId = "+appId);
		return messages;
	}

	/**
	 * 更新系统消息
	 * @author jrjin
	 * @time 2015-12-3 下午4:54:40
	 * @param context
	 * @param messageId 系统消息在数据库中主键的ID
	 * @param appType 应用的类型
	 * @param indexContent 应用模块首页
	 * @return
	 */
	public static int updateSysMessage(Context context, int messageId, int appType, String indexContent, String appName) {
		final ContentValues values = new ContentValues();
		final ContentResolver cr = context.getContentResolver();
		String where = usedAccount == false ? SysMessage._ID + "=" +messageId :
				SysMessage._ID + "=" +messageId
						+ " AND " +SysMessage.ACCOUNT + "=" + "'"
						+ SettingHelper.getAccount(context) + "'";
		HcLog.D(TAG + " updateSysMessage start! where = " + where);
		values.put(SysMessage.READ, 0);
		values.put(SysMessage.INDEX_CONTENT, indexContent);
		values.put(SysMessage.APP_TYPE, appType);
		values.put(SysMessage.APP_NAME, appName);
		int num = cr.update(HcProvider.CONTENT_URI_SYSTEM_MASSAGE, values, where, null);
		HcLog.D(TAG + " updateSysMessage update num = " + num);
		return num;
	}

	/**
	 * 获取当前用户对应应用的系统消息
	 * @author jrjin
	 * @time 2015-12-3 下午4:28:48
	 * @param context
	 * @param messageId 消息的ID
	 * @return 当前用户的系统消息
	 */
	public static SystemMessage getSystemMessage(Context context, int messageId) {
		final ContentResolver cr = context.getContentResolver();
		String[] projection = {SysMessage.APP_ID, SysMessage.CONTENT, SysMessage.CONTENT_ID,
				SysMessage.DATE, SysMessage.READ, SysMessage.TITLE, SysMessage.TYPE, SysMessage.APP_TYPE,
				SysMessage.INDEX_CONTENT, SysMessage._ID, SysMessage.APP_NAME};
		String selection = SysMessage._ID + "=" + messageId;
		Cursor c = cr.query(HcProvider.CONTENT_URI_SYSTEM_MASSAGE, projection, selection, null, null);
		SystemMessage message = null;
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
			message = new SystemMessage();
			message.setAppId(c.getString(0));
			message.setContent(c.getString(1));
			message.setContentId(c.getString(2));
			message.setDate(c.getString(3));
			message.setReaded(c.getInt(4) == 0);
			message.setTitle(c.getString(5));
			message.setType(c.getInt(6));
			message.setAppType(c.getInt(7));
			message.setIndexContent(c.getString(8));
			message.setMessageId(c.getInt(9));
			message.setAppName(c.getString(10));
		}
		if (c != null)
			c.close();
		return message;
	}

	public static void deleteSystemMessage(Context context) {
		final ContentResolver cr = context.getContentResolver();
		String where = usedAccount == false ? null : SysMessage.ACCOUNT + "=" + "'"
						+ SettingHelper.getAccount(context) + "'";
		cr.delete(HcProvider.CONTENT_URI_SYSTEM_MASSAGE, where, null);
	}

	public static void deleteSystemMessage(Context context, String module) {
		final ContentResolver cr = context.getContentResolver();
		String where = usedAccount == false ? SysMessage.APP_ID + "=" +module : SysMessage.ACCOUNT + "=" + "'"
				+ SettingHelper.getAccount(context) + "'" + " AND " + SysMessage.APP_ID + "=" +module;
		cr.delete(HcProvider.CONTENT_URI_SYSTEM_MASSAGE, where, null);
	}

	public static String getMailBoxName(Context context, String userId) {
		String[] projection = {HcDatabase.Contacts.EMAIL};
		String selection = HcDatabase.Contacts.USER_ID + "=" + "'" + userId + "'";
		Cursor c = context.getContentResolver().query(HcProvider.CONTENT_URI_CONTACTS, projection,
				selection, null, null);
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
			String name = c.getString(0);
			c.close();
			return name;
		}
		if (c != null) {
			c.close();
		}
		return null;
	}

	/**
	 * 获取全部人员的数据
	 * @param context
	 * @param columnName 数据库的字段名,附带的数据可以HcDatabase.Contacts.ID,HcDatabase.Contacts.EMAIL,HcDatabase.Contacts.MOBILE_PHONE
	 * @return
     */
	public static List<ItemInfo> getContacts(Context context, String columnName) {
		List<ItemInfo> infos = new ArrayList<ItemInfo>();
		final ContentResolver cr = context.getContentResolver();
		String[] projection = {HcDatabase.Contacts.ID, HcDatabase.Contacts.NAME,
				HcDatabase.Contacts.EMAIL, HcDatabase.Contacts.USER_ID, HcDatabase.Contacts.MOBILE_PHONE};
		Cursor c = cr.query(HcProvider.CONTENT_URI_CONTACTS, projection, HcDatabase.Contacts.TYPE + "=0", null, null);
		if (c != null) {
			if (c.getCount() > 0) {
				int index = 0;
				if (HcDatabase.Contacts.EMAIL.equals(columnName)) {
					index = 2;
				} else if (HcDatabase.Contacts.MOBILE_PHONE.equals(columnName)) {
					index = 4;
				}
				ItemInfo info;
				c.moveToFirst();
				while (!c.isAfterLast()) {
					info = new StaffInfo();
					info.setItemId(c.getString(index));
					info.setUserId(c.getString(3));
					info.setItemValue(c.getString(1));
					info.setIconUrl(HcUtil.getHeaderUri(c.getString(3)));
					infos.add(info);
					c.moveToNext();
				}
			}
			c.close();
		}
		return infos;
	}

	/**
	 * 获取全部人员的数据
	 * @param context
	 * @return
     */
	public static List<ItemInfo> getContacts(Context context) {
		return getContacts(context, HcDatabase.Contacts.ID);
	}

	/**
	 * 获取当前部门下的子部门和人员
	 * @param context
	 * @param depId 部门ID
	 * @param columnName 数据库的字段名,附带的数据可以HcDatabase.Contacts.ID,HcDatabase.Contacts.EMAIL,HcDatabase.Contacts.MOBILE_PHONE
     * @return
     */
	public static List<ItemInfo> getDepartment(Context context, String depId, String columnName) {
		if (TextUtils.isEmpty(depId)) {
			depId = "0";
		}
		List<ItemInfo> infos = new ArrayList<ItemInfo>();
		final ContentResolver cr = context.getContentResolver();
		String[] projection = {HcDatabase.Contacts.ID, HcDatabase.Contacts.NAME,
				HcDatabase.Contacts.EMAIL, HcDatabase.Contacts.USER_ID, HcDatabase.Contacts.MOBILE_PHONE,
				HcDatabase.Contacts.TYPE};
		Cursor c = cr.query(HcProvider.CONTENT_URI_CONTACTS, projection, HcDatabase.Contacts.PARENT_ID + "=" + "'" + depId + "'", null, null);
		if (c != null) {
			if (c.getCount() > 0) {
				int type = 0;
				int index = 0;
				if (HcDatabase.Contacts.EMAIL.equals(columnName)) {
					index = 2;
				} else if (HcDatabase.Contacts.MOBILE_PHONE.equals(columnName)) {
					index = 4;
				}
				ItemInfo info;
				c.moveToFirst();
				while (!c.isAfterLast()) {
					type = c.getInt(5);
					if (type == 0) {
						info = new StaffInfo();
						info.setItemId(c.getString(index));
						info.setUserId(c.getString(3));
						info.setIconUrl(HcUtil.getHeaderUri(c.getString(3)));
					} else {
						info = new DepInfo();
						info.setItemId(c.getString(0));
						info.setIconUrl("drawable://" + HcUtil.getDepResId());
					}
					info.setItemValue(c.getString(1));
					infos.add(info);
					c.moveToNext();
				}
			}
			c.close();
		}
		return infos;
	}

	/**
	 * 获取当前部门下的子部门和人员
	 * @param context
	 * @param depId 部门ID
     * @return
     */
	public static List<ItemInfo> getDepartment(Context context, String depId) {
		return getDepartment(context, depId, HcDatabase.Contacts.ID);
	}
}
