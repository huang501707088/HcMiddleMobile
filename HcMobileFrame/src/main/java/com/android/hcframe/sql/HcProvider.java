/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2013-11-26 下午4:12:07
 */
package com.android.hcframe.sql;

import java.util.Properties;

import com.android.hcframe.HcLog;
import com.android.hcframe.sql.HcDatabase.AnnualProgram;
import com.android.hcframe.sql.HcDatabase.AppMessage;
import com.android.hcframe.sql.HcDatabase.Badge;
import com.android.hcframe.sql.HcDatabase.ChatGroup;
import com.android.hcframe.sql.HcDatabase.ChatMessage;
import com.android.hcframe.sql.HcDatabase.Contacts;
import com.android.hcframe.sql.HcDatabase.DataRecord;
import com.android.hcframe.sql.HcDatabase.DataRecordDetail;
import com.android.hcframe.sql.HcDatabase.DownloadFile;
import com.android.hcframe.sql.HcDatabase.HcAppMarket;
import com.android.hcframe.sql.HcDatabase.HcAppOperLog;
import com.android.hcframe.sql.HcDatabase.HcInstallApp;
import com.android.hcframe.sql.HcDatabase.HcNewsColumen;
import com.android.hcframe.sql.HcDatabase.NewsList;
import com.android.hcframe.sql.HcDatabase.Schedule;
import com.android.hcframe.sql.HcDatabase.SysMessage;
import com.android.hcframe.sql.HcDatabase.TaskInfo;
import com.android.hcframe.sql.HcDatabase.UploadFile;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

public class HcProvider extends ContentProvider {

	/** 测试配置 */
	private static final String AUTHORITY_TEST = "com.zjhcsoft.mobile.hcmiddlemobile";
	/** SVN配置 */
	private static final String AUTHORITY_SVN = "PACKAGE_NAME";

	public static final String DEFAULT_AUTHORITY = AUTHORITY_TEST;

	private static String AUTHORITY = DEFAULT_AUTHORITY + ".HcProvider";
	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	private SQLiteOpenHelper mOpenHelper;

	private static final int APP = 1;
	private static final int APP_ID = 10;

	private static final int SYSTEM_MESSAGE = 2;
	private static final int SYSTEM_MESSAGE_ID = 20;

	private static final int NEWS = 3;
	private static final int NEWS_ID = 30;
	
	private static final int DATA_RECORD = 4;
	private static final int DATA_RECORD_ID = 40;
	
	private static final int DATA_RECORD_DETAIL = 5;
	private static final int DATA_RECORD_DETAIL_ID = 50;
	
	private static final int CONTACTS = 6;
	private static final int CONTACTS_ID = 60;
	
	private static final int ANNUAL_PROGRAM = 7;
	private static final int ANNUAL_PROGRAM_ID = 70;

	private static final int NEWS_LIST = 8;
	private static final int NEWS_LIST_ID = 80;

	private static final int OPER_LOG = 9;
	private static final int OPER_LOG_ID = 90;

	private static final int BADGE = 11;
	private static final int BADGE_ID = 110;

	private static final int UPLOAD = 12;
   	private static final int UPLOAD_ID = 120;

   	private static final int DOWNLOAD = 13;
   	private static final int DOWNLOAD_ID = 130;

	private static final int TASK = 14;
	private static final int TASK_ID = 140;

	private static final int MESSAGE = 15;
	private static final int MESSAGE_ID = 150;

	private static final int CHAT = 16;
	private static final int CHAT_ID = 160;

	private static final int GROUP = 17;
	private static final int GROUP_ID = 170;

	private static final int SCHEDULE = 18;
	private static final int SCHEDULE_ID = 180;
	
	private static final String APP_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/" + HcAppMarket.TABLE_NAME;
	private static final String APP_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/" + HcAppMarket.TABLE_NAME;

	private static final String SYSTEM_MESSAGE_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/" + SysMessage.TABLE_NAME;
	private static final String SYSTEM_MESSAGE_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/" + SysMessage.TABLE_NAME;

	private static final String NEWS_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/" + HcNewsColumen.TABLE_NAME;
	private static final String NEWS_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/" + HcNewsColumen.TABLE_NAME;
	
	private static final String DATA_RECORD_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/" + DataRecord.TABLE_NAME;
	private static final String DATA_RECORD_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/" + DataRecord.TABLE_NAME;
	
	private static final String DATA_RECORD_DETAIL_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/" + DataRecordDetail.TABLE_NAME;
	private static final String DATA_RECORD_DETAIL_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/" + DataRecordDetail.TABLE_NAME;

	private static final String CONTACTS_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/" + Contacts.TABLE_NAME;
	private static final String CONTACTS_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/" + Contacts.TABLE_NAME;
	
	private static final String ANNUAL_PROGRAM_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/" + AnnualProgram.TABLE_NAME;
	private static final String ANNUAL_PROGRAM_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/" + AnnualProgram.TABLE_NAME;
	
	private static final String NEWS_LIST_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/" + NewsList.TABLE_NAME;
	private static final String NEWS_LIST_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/" + NewsList.TABLE_NAME;

	private static final String OPER_LOG_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/" + HcAppOperLog.TABLE_NAME;
	private static final String OPER_LOG_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/" + HcAppOperLog.TABLE_NAME;

	private static final String BADGE_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/" + Badge.TABLE_NAME;
	private static final String BADGE_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/" + Badge.TABLE_NAME;

	private static final String UPLOAD_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/" + UploadFile.TABLE_NAME;
	private static final String UPLOAD_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/" + UploadFile.TABLE_NAME;

	private static final String DOWNLOAD_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/" + DownloadFile.TABLE_NAME;
	private static final String DOWNLOAD_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/" + DownloadFile.TABLE_NAME;

	private static final String TASK_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/" + TaskInfo.TABLE_NAME;
	private static final String TASK_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/" + TaskInfo.TABLE_NAME;

	private static final String MESSAGE_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/" + AppMessage.TABLE_NAME;
	private static final String MESSAGE_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/" + AppMessage.TABLE_NAME;

	private static final String CHAT_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/" + ChatMessage.TABLE_NAME;
	private static final String CHAT_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/" + ChatMessage.TABLE_NAME;

	private static final String GROUP_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/" + ChatGroup.TABLE_NAME;
	private static final String GROUP_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/" + ChatGroup.TABLE_NAME;

	private static final String SCHEDULE_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/" + Schedule.TABLE_NAME;
	private static final String SCHEDULE_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/" + Schedule.TABLE_NAME;

	static {
		/**
		 * @author jinjr
		 * @date 17-4-11 下午1:55
		 * unused...
		Properties properties = new Properties();
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			properties.load(loader.getResourceAsStream("com/android/hcframe/config/config.properties"));
			AUTHORITY = properties.getProperty("PROVIDER_AUTHORITY", DEFAULT_AUTHORITY);
		} catch (Exception e) {
			// TODO: handle exception
			AUTHORITY = DEFAULT_AUTHORITY;
		}
		*/
		HcLog.D("HcProvider static AUTHORITY = "+AUTHORITY);
		
		sURIMatcher.addURI(AUTHORITY, HcAppMarket.TABLE_NAME, APP);
		sURIMatcher.addURI(AUTHORITY, HcAppMarket.TABLE_NAME + "/#", APP_ID);

		sURIMatcher.addURI(AUTHORITY, SysMessage.TABLE_NAME, SYSTEM_MESSAGE);
		sURIMatcher.addURI(AUTHORITY, SysMessage.TABLE_NAME + "/#",
				SYSTEM_MESSAGE_ID);

		sURIMatcher.addURI(AUTHORITY, HcNewsColumen.TABLE_NAME, NEWS);
		sURIMatcher.addURI(AUTHORITY, HcNewsColumen.TABLE_NAME + "/#", NEWS_ID);
		
		sURIMatcher.addURI(AUTHORITY, DataRecord.TABLE_NAME, DATA_RECORD);
		sURIMatcher.addURI(AUTHORITY, DataRecord.TABLE_NAME + "/#", DATA_RECORD_ID);
		
		sURIMatcher.addURI(AUTHORITY, DataRecordDetail.TABLE_NAME, DATA_RECORD_DETAIL);
		sURIMatcher.addURI(AUTHORITY, DataRecordDetail.TABLE_NAME + "/#", DATA_RECORD_DETAIL_ID);
		
		sURIMatcher.addURI(AUTHORITY, Contacts.TABLE_NAME, CONTACTS);
		sURIMatcher.addURI(AUTHORITY, Contacts.TABLE_NAME + "/#", CONTACTS_ID);
		
		sURIMatcher.addURI(AUTHORITY, AnnualProgram.TABLE_NAME, ANNUAL_PROGRAM);
		sURIMatcher.addURI(AUTHORITY, AnnualProgram.TABLE_NAME + "/#", ANNUAL_PROGRAM_ID);
	
		sURIMatcher.addURI(AUTHORITY, NewsList.TABLE_NAME, NEWS_LIST);
		sURIMatcher.addURI(AUTHORITY, NewsList.TABLE_NAME + "/#", NEWS_LIST_ID);

		sURIMatcher.addURI(AUTHORITY, HcAppOperLog.TABLE_NAME, OPER_LOG);
		sURIMatcher.addURI(AUTHORITY, HcAppOperLog.TABLE_NAME + "/#", OPER_LOG_ID);

		sURIMatcher.addURI(AUTHORITY, Badge.TABLE_NAME, BADGE);
		sURIMatcher.addURI(AUTHORITY, Badge.TABLE_NAME + "/#", BADGE_ID);

		sURIMatcher.addURI(AUTHORITY, UploadFile.TABLE_NAME, UPLOAD);
		sURIMatcher.addURI(AUTHORITY, UploadFile.TABLE_NAME + "/#", UPLOAD_ID);

		sURIMatcher.addURI(AUTHORITY, DownloadFile.TABLE_NAME, DOWNLOAD);
		sURIMatcher.addURI(AUTHORITY, DownloadFile.TABLE_NAME + "/#", DOWNLOAD_ID);

		sURIMatcher.addURI(AUTHORITY, TaskInfo.TABLE_NAME, TASK);
		sURIMatcher.addURI(AUTHORITY, TaskInfo.TABLE_NAME + "/#", TASK_ID);

		sURIMatcher.addURI(AUTHORITY, AppMessage.TABLE_NAME, MESSAGE);
		sURIMatcher.addURI(AUTHORITY, AppMessage.TABLE_NAME + "/#", MESSAGE_ID);

		sURIMatcher.addURI(AUTHORITY, ChatMessage.TABLE_NAME, CHAT);
		sURIMatcher.addURI(AUTHORITY, ChatMessage.TABLE_NAME + "/#", CHAT_ID);

		sURIMatcher.addURI(AUTHORITY, ChatGroup.TABLE_NAME, GROUP);
		sURIMatcher.addURI(AUTHORITY, ChatGroup.TABLE_NAME + "/#", GROUP_ID);

		sURIMatcher.addURI(AUTHORITY, Schedule.TABLE_NAME, SCHEDULE);
		sURIMatcher.addURI(AUTHORITY, Schedule.TABLE_NAME + "/#", SCHEDULE_ID);
	}

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	public static final Uri CONTENT_URI_APP = Uri.parse("content://"
			+ AUTHORITY + "/" + HcAppMarket.TABLE_NAME);
	public static final Uri CONTENT_URI_INSTALL_APP = Uri.parse("content://"
			+ AUTHORITY + "/" + HcInstallApp.TABLE_NAME);
	public static final Uri CONTENT_URI_NEWS = Uri.parse("content://"
			+ AUTHORITY + "/" + HcNewsColumen.TABLE_NAME);
	
	public static final Uri CONTENT_URI_DATA_RECORD = Uri.parse("content://"
			+ AUTHORITY + "/" + DataRecord.TABLE_NAME);
	
	public static final Uri CONTENT_URI_DATA_RECORD_DETAIL = Uri.parse("content://"
			+ AUTHORITY + "/" + DataRecordDetail.TABLE_NAME);
	
	public static final Uri CONTENT_URI_CONTACTS = Uri.parse("content://"
			+ AUTHORITY + "/" + Contacts.TABLE_NAME);
	
	public static final Uri CONTENT_URI_SYSTEM_MASSAGE = Uri.parse("content://"
			+ AUTHORITY + "/" + SysMessage.TABLE_NAME);
	
	public static final Uri CONTENT_URI_ANNUAL_PROGRAM = Uri.parse("content://"
			+ AUTHORITY + "/" + AnnualProgram.TABLE_NAME);
	
	public static final Uri CONTENT_URI_NEWS_LIST = Uri.parse("content://"
			+ AUTHORITY + "/" + NewsList.TABLE_NAME);

	public static final Uri CONTENT_URI_LOG = Uri.parse("content://"
			+ AUTHORITY + "/" + HcAppOperLog.TABLE_NAME);

	public static final Uri CONTENT_URI_BADGE = Uri.parse("content://"
			+ AUTHORITY + "/" + Badge.TABLE_NAME);

	public static final Uri CONTENT_URI_DOWNLOAD_FILE = Uri.parse("content://"
			+ AUTHORITY + "/" + DownloadFile.TABLE_NAME);

	public static final Uri CONTENT_URI_UPLOAD_FILE = Uri.parse("content://"
			+ AUTHORITY + "/" + UploadFile.TABLE_NAME);

	public static final Uri CONTENT_URI_TASK = Uri.parse("content://"
			+ AUTHORITY + "/" + TaskInfo.TABLE_NAME);

	public static final Uri CONTENT_URI_MESSAGE = Uri.parse("content://"
			+ AUTHORITY + "/" + AppMessage.TABLE_NAME);

	public static final Uri CONTENT_URI_CHAT = Uri.parse("content://"
			+ AUTHORITY + "/" + ChatMessage.TABLE_NAME);

	public static final Uri CONTENT_URI_GROUP = Uri.parse("content://"
			+ AUTHORITY + "/" + ChatGroup.TABLE_NAME);

	public static final Uri CONTENT_URI_SCHEDULE = Uri.parse("content://"
			+ AUTHORITY + "/" + Schedule.TABLE_NAME);

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		mOpenHelper = new HcMarketDatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
		int match = sURIMatcher.match(uri);
		switch (match) {
		case APP:
			qBuilder.setTables(HcAppMarket.TABLE_NAME);
			break;

		case APP_ID:
			qBuilder.setTables(HcAppMarket.TABLE_NAME);
			qBuilder.appendWhere("_id=" + uri.getLastPathSegment());
			break;
		case SYSTEM_MESSAGE:
			qBuilder.setTables(SysMessage.TABLE_NAME);
			break;

		case SYSTEM_MESSAGE_ID:
			qBuilder.setTables(SysMessage.TABLE_NAME);
			qBuilder.appendWhere("_id=" + uri.getLastPathSegment());
			break;
		case NEWS:
			qBuilder.setTables(HcNewsColumen.TABLE_NAME);
			break;
		case NEWS_ID:
			qBuilder.setTables(HcNewsColumen.TABLE_NAME);
			qBuilder.appendWhere("_id=" + uri.getLastPathSegment());
			break;
		case DATA_RECORD:
			qBuilder.setTables(DataRecord.TABLE_NAME);
			break;

		case DATA_RECORD_ID:
			qBuilder.setTables(DataRecord.TABLE_NAME);
			qBuilder.appendWhere("_id=" + uri.getLastPathSegment());
			break;
		case DATA_RECORD_DETAIL:
			qBuilder.setTables(DataRecordDetail.TABLE_NAME);
			break;

		case DATA_RECORD_DETAIL_ID:
			qBuilder.setTables(DataRecordDetail.TABLE_NAME);
			qBuilder.appendWhere("_id=" + uri.getLastPathSegment());
			break;	
		case CONTACTS:
			qBuilder.setTables(Contacts.TABLE_NAME);
			break;
		case CONTACTS_ID:
			qBuilder.setTables(Contacts.TABLE_NAME);
			qBuilder.appendWhere("_id=" + uri.getLastPathSegment());
			break;
		case ANNUAL_PROGRAM:
			qBuilder.setTables(AnnualProgram.TABLE_NAME);
			break;
		case ANNUAL_PROGRAM_ID:
			qBuilder.setTables(AnnualProgram.TABLE_NAME);
			qBuilder.appendWhere("_id=" + uri.getLastPathSegment());
			break;
			
		case NEWS_LIST:
			qBuilder.setTables(NewsList.TABLE_NAME);
			break;
		case NEWS_LIST_ID:
			qBuilder.setTables(NewsList.TABLE_NAME);
			qBuilder.appendWhere("_id=" + uri.getLastPathSegment());
			break;
		case OPER_LOG:
			qBuilder.setTables(HcAppOperLog.TABLE_NAME);
			break;
		case OPER_LOG_ID:
			qBuilder.setTables(HcAppOperLog.TABLE_NAME);
			qBuilder.appendWhere("_id=" + uri.getLastPathSegment());
			break;
		case BADGE:
			qBuilder.setTables(Badge.TABLE_NAME);
			break;
		case BADGE_ID:
			qBuilder.setTables(Badge.TABLE_NAME);
			qBuilder.appendWhere("_id=" + uri.getLastPathSegment());
			break;
		case UPLOAD:
		    qBuilder.setTables(UploadFile.TABLE_NAME);
		    break;
		case UPLOAD_ID:
		    qBuilder.setTables(UploadFile.TABLE_NAME);
		    qBuilder.appendWhere("_id=" + uri.getLastPathSegment());
		    break;
		case DOWNLOAD:
		    qBuilder.setTables(DownloadFile.TABLE_NAME);
		    break;
		case DOWNLOAD_ID:
		    qBuilder.setTables(DownloadFile.TABLE_NAME);
		    qBuilder.appendWhere("_id=" + uri.getLastPathSegment());
		    break;
		case TASK:
			qBuilder.setTables(TaskInfo.TABLE_NAME);
			break;
		case TASK_ID:
			qBuilder.setTables(TaskInfo.TABLE_NAME);
			qBuilder.appendWhere("_id=" + uri.getLastPathSegment());
			break;
		case MESSAGE:
			qBuilder.setTables(AppMessage.TABLE_NAME);
			break;
		case MESSAGE_ID:
			qBuilder.setTables(AppMessage.TABLE_NAME);
			qBuilder.appendWhere("_id=" + uri.getLastPathSegment());
			break;
		case CHAT:
			qBuilder.setTables(ChatMessage.TABLE_NAME);
			break;
		case CHAT_ID:
			qBuilder.setTables(ChatMessage.TABLE_NAME);
			qBuilder.appendWhere("_id=" + uri.getLastPathSegment());
			break;
		case GROUP:
			qBuilder.setTables(ChatGroup.TABLE_NAME);
			break;
		case GROUP_ID:
			qBuilder.setTables(ChatGroup.TABLE_NAME);
			qBuilder.appendWhere("_id=" + uri.getLastPathSegment());
			break;
		case SCHEDULE:
			qBuilder.setTables(Schedule.TABLE_NAME);
			break;
		case SCHEDULE_ID:
			qBuilder.setTables(Schedule.TABLE_NAME);
			qBuilder.appendWhere("_id=" + uri.getLastPathSegment());
			break;
		default:
			break;
		}

		Cursor resultCursor = qBuilder.query(mOpenHelper.getWritableDatabase(),
				projection, selection, selectionArgs, null, null, sortOrder);
		resultCursor.setNotificationUri(getContext().getContentResolver(), uri);

		return resultCursor;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		int match = sURIMatcher.match(uri);
		switch (match) {
		case APP:
			return APP_CONTENT_TYPE;

		case APP_ID:
			return APP_CONTENT_ITEM_TYPE;
		case SYSTEM_MESSAGE:
			return SYSTEM_MESSAGE_CONTENT_TYPE;

		case SYSTEM_MESSAGE_ID:
			return SYSTEM_MESSAGE_CONTENT_ITEM_TYPE;
		case NEWS:
			return NEWS_CONTENT_TYPE;
		case NEWS_ID:
			return NEWS_CONTENT_ITEM_TYPE;
		case DATA_RECORD:
			return DATA_RECORD_CONTENT_TYPE;
		case DATA_RECORD_ID:
			return DATA_RECORD_CONTENT_ITEM_TYPE;
		case DATA_RECORD_DETAIL:
			return DATA_RECORD_DETAIL_CONTENT_TYPE;
		case DATA_RECORD_DETAIL_ID:
			return DATA_RECORD_DETAIL_CONTENT_ITEM_TYPE;
		case CONTACTS:
			return CONTACTS_CONTENT_TYPE;
		case CONTACTS_ID:
			return CONTACTS_CONTENT_ITEM_TYPE;
		case ANNUAL_PROGRAM:
			return ANNUAL_PROGRAM_CONTENT_TYPE;
		case ANNUAL_PROGRAM_ID:
			return ANNUAL_PROGRAM_CONTENT_ITEM_TYPE;
		case NEWS_LIST:
			return NEWS_LIST_CONTENT_TYPE;
		case NEWS_LIST_ID:
			return NEWS_LIST_CONTENT_ITEM_TYPE;
		case OPER_LOG:
			return OPER_LOG_CONTENT_TYPE;
		case OPER_LOG_ID:
			return OPER_LOG_CONTENT_ITEM_TYPE;
		case BADGE:
			return BADGE_CONTENT_TYPE;
		case BADGE_ID:
			return BADGE_CONTENT_ITEM_TYPE;
		case UPLOAD:
		    return UPLOAD_CONTENT_TYPE;
		case UPLOAD_ID:
		    return UPLOAD_CONTENT_ITEM_TYPE;
		case DOWNLOAD:
		    return DOWNLOAD_CONTENT_TYPE;
		case DOWNLOAD_ID:
		    return DOWNLOAD_CONTENT_ITEM_TYPE;
		case TASK:
			return TASK_CONTENT_TYPE;
		case TASK_ID:
			return TASK_CONTENT_ITEM_TYPE;
		case MESSAGE:
			return MESSAGE_CONTENT_TYPE;
		case MESSAGE_ID:
			return MESSAGE_CONTENT_ITEM_TYPE;
		case CHAT:
			return CHAT_CONTENT_TYPE;
		case CHAT_ID:
			return CHAT_CONTENT_ITEM_TYPE;
		case GROUP:
			return GROUP_CONTENT_TYPE;
		case GROUP_ID:
			return GROUP_CONTENT_ITEM_TYPE;
		case SCHEDULE:
			return SCHEDULE_CONTENT_TYPE;
		case SCHEDULE_ID:
			return SCHEDULE_CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		int match = sURIMatcher.match(uri);
		if (match != APP && match != SYSTEM_MESSAGE && match!=NEWS
				&& match != DATA_RECORD && match != DATA_RECORD_DETAIL
				&& match != CONTACTS && match != ANNUAL_PROGRAM
				&& match != NEWS_LIST && match != OPER_LOG
				&& match != BADGE && match != UPLOAD
				&& match != DOWNLOAD && match != TASK
				&& match != MESSAGE && match != CHAT
				&& match != GROUP && match != SCHEDULE) {
			throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
		}
		SQLiteDatabase sqlDB = mOpenHelper.getWritableDatabase();

		long newID = -1;

		switch (match) {
		case APP:
			newID = sqlDB.insert(HcAppMarket.TABLE_NAME, null, values);
			break;
		case SYSTEM_MESSAGE:
			newID = sqlDB.insert(SysMessage.TABLE_NAME, null, values);
			break;
		case NEWS:
			newID = sqlDB.insert(HcNewsColumen.TABLE_NAME, null, values);
			break;
		case DATA_RECORD:
			newID = sqlDB.insert(DataRecord.TABLE_NAME, null, values);
			break;
		case DATA_RECORD_DETAIL:
			newID = sqlDB.insert(DataRecordDetail.TABLE_NAME, null, values);
			break;
		case CONTACTS:
			newID = sqlDB.insert(Contacts.TABLE_NAME, null, values);
			break;
		case ANNUAL_PROGRAM:
			newID = sqlDB.insert(AnnualProgram.TABLE_NAME, null, values);
			break;
		case NEWS_LIST:
			newID = sqlDB.insert(NewsList.TABLE_NAME, null, values);
			break;
		case OPER_LOG:
			newID = sqlDB.insert(HcAppOperLog.TABLE_NAME, null, values);
			break;
		case BADGE:
			newID = sqlDB.insert(Badge.TABLE_NAME, null, values);
			break;
		case UPLOAD:
		    newID = sqlDB.insert(UploadFile.TABLE_NAME, null, values);
		    break;
		case DOWNLOAD:
		    newID = sqlDB.insert(DownloadFile.TABLE_NAME, null, values);
		    break;
		case TASK:
			newID = sqlDB.insert(TaskInfo.TABLE_NAME, null, values);
			break;
		case MESSAGE:
			newID = sqlDB.insert(AppMessage.TABLE_NAME, null, values);
			break;
		case CHAT:
			newID = sqlDB.insert(ChatMessage.TABLE_NAME, null, values);
			break;
		case GROUP:
			newID = sqlDB.insert(ChatGroup.TABLE_NAME, null, values);
			break;
		case SCHEDULE:
			newID = sqlDB.insert(Schedule.TABLE_NAME, null, values);
			break;
		default:
			break;
		}

		if (newID > 0) {
			Uri newUri = ContentUris.withAppendedId(uri, newID);
			getContext().getContentResolver().notifyChange(newUri, null);
			return newUri;
		}

		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		int match = sURIMatcher.match(uri);

		SQLiteDatabase sqlDB = mOpenHelper.getWritableDatabase();
		int rowsAffected = 0;
		switch (match) {
		case APP:
			rowsAffected = sqlDB.delete(HcAppMarket.TABLE_NAME, selection,
					selectionArgs);
			break;

		case APP_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsAffected = sqlDB.delete(HcAppMarket.TABLE_NAME,
						BaseColumns._ID + "=" + id, null);
			} else {
				rowsAffected = sqlDB.delete(HcAppMarket.TABLE_NAME, selection
						+ " and " + BaseColumns._ID + "=" + id, selectionArgs);
			}

			break;
		case SYSTEM_MESSAGE:
			rowsAffected = sqlDB.delete(SysMessage.TABLE_NAME, selection,
					selectionArgs);
			break;

		case SYSTEM_MESSAGE_ID:
			String install_id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsAffected = sqlDB.delete(SysMessage.TABLE_NAME,
						SysMessage._ID + "=" + install_id, null);
			} else {
				rowsAffected = sqlDB.delete(SysMessage.TABLE_NAME, selection
						+ " and " + SysMessage._ID + "=" + install_id,
						selectionArgs);
			}

			break;
		case NEWS:
			rowsAffected = sqlDB.delete(HcNewsColumen.TABLE_NAME, selection,
					selectionArgs);
			break;

		case NEWS_ID:
			String news_id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsAffected = sqlDB.delete(HcNewsColumen.TABLE_NAME,
						BaseColumns._ID + "=" + news_id, null);
			} else {
				rowsAffected = sqlDB.delete(HcNewsColumen.TABLE_NAME, selection
						+ " and " + BaseColumns._ID + "=" + news_id,
						selectionArgs);
			}

			break;
		case DATA_RECORD:
			rowsAffected = sqlDB.delete(DataRecord.TABLE_NAME, selection,
					selectionArgs);
			break;

		case DATA_RECORD_ID:
			String dataId = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsAffected = sqlDB.delete(DataRecord.TABLE_NAME,
						BaseColumns._ID + "=" + dataId, null);
			} else {
				rowsAffected = sqlDB.delete(DataRecord.TABLE_NAME, selection
						+ " and " + BaseColumns._ID + "=" + dataId, selectionArgs);
			}

			break;
		case DATA_RECORD_DETAIL:
			rowsAffected = sqlDB.delete(DataRecord.TABLE_NAME, selection,
					selectionArgs);
			break;

		case DATA_RECORD_DETAIL_ID:
			String dataDetailId = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsAffected = sqlDB.delete(DataRecordDetail.TABLE_NAME,
						BaseColumns._ID + "=" + dataDetailId, null);
			} else {
				rowsAffected = sqlDB.delete(DataRecordDetail.TABLE_NAME, selection
						+ " and " + BaseColumns._ID + "=" + dataDetailId, selectionArgs);
			}

			break;
		case CONTACTS:
			rowsAffected = sqlDB.delete(Contacts.TABLE_NAME, selection,
					selectionArgs);
			break;

		case CONTACTS_ID:
			String contacts_id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsAffected = sqlDB.delete(Contacts.TABLE_NAME,
						BaseColumns._ID + "=" + contacts_id, null);
			} else {
				rowsAffected = sqlDB.delete(Contacts.TABLE_NAME, selection
						+ " and " + BaseColumns._ID + "=" + contacts_id,
						selectionArgs);
			}

			break;
		case ANNUAL_PROGRAM:
			rowsAffected = sqlDB.delete(AnnualProgram.TABLE_NAME, selection,
					selectionArgs);
			break;

		case ANNUAL_PROGRAM_ID:
			String program_id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsAffected = sqlDB.delete(AnnualProgram.TABLE_NAME,
						BaseColumns._ID + "=" + program_id, null);
			} else {
				rowsAffected = sqlDB.delete(AnnualProgram.TABLE_NAME, selection
						+ " and " + BaseColumns._ID + "=" + program_id,
						selectionArgs);
			}

			break;
			
		case NEWS_LIST:
			rowsAffected = sqlDB.delete(NewsList.TABLE_NAME, selection,
					selectionArgs);
			break;

		case NEWS_LIST_ID:
			String news_list_id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsAffected = sqlDB.delete(NewsList.TABLE_NAME,
						BaseColumns._ID + "=" + news_list_id, null);
			} else {
				rowsAffected = sqlDB.delete(NewsList.TABLE_NAME, selection
						+ " and " + BaseColumns._ID + "=" + news_list_id,
						selectionArgs);
			}

			break;
		case OPER_LOG:
			rowsAffected = sqlDB.delete(HcAppOperLog.TABLE_NAME, selection,
					selectionArgs);
			break;

		case OPER_LOG_ID:
			String log_id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsAffected = sqlDB.delete(HcAppOperLog.TABLE_NAME,
						BaseColumns._ID + "=" + log_id, null);
			} else {
				rowsAffected = sqlDB.delete(HcAppOperLog.TABLE_NAME, selection
								+ " and " + BaseColumns._ID + "=" + log_id,
						selectionArgs);
			}

			break;
		case BADGE:
			rowsAffected = sqlDB.delete(Badge.TABLE_NAME, selection,
					selectionArgs);
			break;

		case BADGE_ID:
			String badge_id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsAffected = sqlDB.delete(Badge.TABLE_NAME,
						BaseColumns._ID + "=" + badge_id, null);
			} else {
				rowsAffected = sqlDB.delete(Badge.TABLE_NAME, selection
								+ " and " + BaseColumns._ID + "=" + badge_id,
						selectionArgs);
			}

			break;
		case UPLOAD:
		    rowsAffected = sqlDB.delete(UploadFile.TABLE_NAME, selection,
		            selectionArgs);
		    break;

		case UPLOAD_ID:
		    String upload_id = uri.getLastPathSegment();
		    if (TextUtils.isEmpty(selection)) {
		        rowsAffected = sqlDB.delete(UploadFile.TABLE_NAME,
		                BaseColumns._ID + "=" + upload_id, null);
		    } else {
		        rowsAffected = sqlDB.delete(UploadFile.TABLE_NAME, selection
		                        + " and " + BaseColumns._ID + "=" + upload_id,
		                selectionArgs);
		    }

		    break;
		case DOWNLOAD:
		    rowsAffected = sqlDB.delete(DownloadFile.TABLE_NAME, selection,
		            selectionArgs);
		    break;

		case DOWNLOAD_ID:
		    String download_id = uri.getLastPathSegment();
		    if (TextUtils.isEmpty(selection)) {
		        rowsAffected = sqlDB.delete(DownloadFile.TABLE_NAME,
		                BaseColumns._ID + "=" + download_id, null);
		    } else {
		        rowsAffected = sqlDB.delete(DownloadFile.TABLE_NAME, selection
		                        + " and " + BaseColumns._ID + "=" + download_id,
		                selectionArgs);
		    }

		    break;
		case TASK:
			rowsAffected = sqlDB.delete(TaskInfo.TABLE_NAME, selection,
					selectionArgs);
			break;

		case TASK_ID:
			String task_id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsAffected = sqlDB.delete(TaskInfo.TABLE_NAME,
						BaseColumns._ID + "=" + task_id, null);
			} else {
				rowsAffected = sqlDB.delete(TaskInfo.TABLE_NAME, selection
								+ " and " + BaseColumns._ID + "=" + task_id,
						selectionArgs);
			}

			break;
		case MESSAGE:
			rowsAffected = sqlDB.delete(AppMessage.TABLE_NAME, selection,
					selectionArgs);
			break;

		case MESSAGE_ID:
			String message_id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsAffected = sqlDB.delete(AppMessage.TABLE_NAME,
						BaseColumns._ID + "=" + message_id, null);
			} else {
				rowsAffected = sqlDB.delete(AppMessage.TABLE_NAME, selection
								+ " and " + BaseColumns._ID + "=" + message_id,
						selectionArgs);
			}

			break;
		case CHAT:
			rowsAffected = sqlDB.delete(ChatMessage.TABLE_NAME, selection,
					selectionArgs);
			break;

		case CHAT_ID:
			String chat_id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsAffected = sqlDB.delete(ChatMessage.TABLE_NAME,
						BaseColumns._ID + "=" + chat_id, null);
			} else {
				rowsAffected = sqlDB.delete(ChatMessage.TABLE_NAME, selection
								+ " and " + BaseColumns._ID + "=" + chat_id,
						selectionArgs);
			}

			break;
		case GROUP:
			rowsAffected = sqlDB.delete(ChatGroup.TABLE_NAME, selection,
					selectionArgs);
			break;

		case GROUP_ID:
			String group_id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsAffected = sqlDB.delete(ChatGroup.TABLE_NAME,
						BaseColumns._ID + "=" + group_id, null);
			} else {
				rowsAffected = sqlDB.delete(ChatGroup.TABLE_NAME, selection
								+ " and " + BaseColumns._ID + "=" + group_id,
						selectionArgs);
			}

			break;
		case SCHEDULE:
			rowsAffected = sqlDB.delete(Schedule.TABLE_NAME, selection,
					selectionArgs);
			break;

		case SCHEDULE_ID:
			String schedule_id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsAffected = sqlDB.delete(Schedule.TABLE_NAME,
						BaseColumns._ID + "=" + schedule_id, null);
			} else {
				rowsAffected = sqlDB.delete(Schedule.TABLE_NAME, selection
								+ " and " + BaseColumns._ID + "=" + schedule_id,
						selectionArgs);
			}

			break;
		default:
			throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsAffected;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		SQLiteDatabase sqlDB = mOpenHelper.getWritableDatabase();
		int match = sURIMatcher.match(uri);
		int rowAffected;
		switch (match) {
		case APP:
			rowAffected = sqlDB.update(HcAppMarket.TABLE_NAME, values,
					selection, selectionArgs);
			break;

		case APP_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowAffected = sqlDB.update(HcAppMarket.TABLE_NAME, values,
						HcAppMarket._ID + "=" + id, null);
			} else {
				rowAffected = sqlDB.update(HcAppMarket.TABLE_NAME, values,
						selection + " and " + HcAppMarket._ID + "=" + id,
						selectionArgs);
			}

			break;
		case SYSTEM_MESSAGE:
			rowAffected = sqlDB.update(SysMessage.TABLE_NAME, values,
					selection, selectionArgs);
			break;

		case SYSTEM_MESSAGE_ID:
			String install_id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowAffected = sqlDB.update(SysMessage.TABLE_NAME, values,
						SysMessage._ID + "=" + install_id, null);
			} else {
				rowAffected = sqlDB.update(SysMessage.TABLE_NAME, values,
						selection + " and " + SysMessage._ID + "="
								+ install_id, selectionArgs);
			}

			break;
		case NEWS:
			rowAffected = sqlDB.update(HcNewsColumen.TABLE_NAME, values,
					selection, selectionArgs);
			break;

		case NEWS_ID:
			String news_id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowAffected = sqlDB.update(HcNewsColumen.TABLE_NAME, values,
						HcNewsColumen._ID + "=" + news_id, null);
			} else {
				rowAffected = sqlDB.update(HcNewsColumen.TABLE_NAME, values,
						selection + " and " + HcNewsColumen._ID + "=" + news_id,
						selectionArgs);
			}

			break;
		case DATA_RECORD:
			rowAffected = sqlDB.update(DataRecord.TABLE_NAME, values,
					selection, selectionArgs);
			break;

		case DATA_RECORD_ID:
			String dataId = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowAffected = sqlDB.update(DataRecord.TABLE_NAME, values,
						DataRecord._ID + "=" + dataId, null);
			} else {
				rowAffected = sqlDB.update(DataRecord.TABLE_NAME, values,
						selection + " and " + DataRecord._ID + "=" + dataId,
						selectionArgs);
			}

			break;
		case DATA_RECORD_DETAIL:
			rowAffected = sqlDB.update(DataRecord.TABLE_NAME, values,
					selection, selectionArgs);
			break;

		case DATA_RECORD_DETAIL_ID:
			String dataDetailId = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowAffected = sqlDB.update(DataRecordDetail.TABLE_NAME, values,
						DataRecordDetail._ID + "=" + dataDetailId, null);
			} else {
				rowAffected = sqlDB.update(DataRecordDetail.TABLE_NAME, values,
						selection + " and " + DataRecordDetail._ID + "=" + dataDetailId,
						selectionArgs);
			}

			break;
		case CONTACTS:
			rowAffected = sqlDB.update(Contacts.TABLE_NAME, values,
					selection, selectionArgs);
			break;

		case CONTACTS_ID:
			String contacts_id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowAffected = sqlDB.update(Contacts.TABLE_NAME, values,
						Contacts._ID + "=" + contacts_id, null);
			} else {
				rowAffected = sqlDB.update(Contacts.TABLE_NAME, values,
						selection + " and " + Contacts._ID + "=" + contacts_id,
						selectionArgs);
			}

			break;
		case ANNUAL_PROGRAM:
			rowAffected = sqlDB.update(AnnualProgram.TABLE_NAME, values,
					selection, selectionArgs);
			break;

		case ANNUAL_PROGRAM_ID:
			String program_id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowAffected = sqlDB.update(AnnualProgram.TABLE_NAME, values,
						BaseColumns._ID + "=" + program_id, null);
			} else {
				rowAffected = sqlDB.update(AnnualProgram.TABLE_NAME, values,
						selection + " and " + BaseColumns._ID + "=" + program_id,
						selectionArgs);
			}

			break;
			
		case NEWS_LIST:
			rowAffected = sqlDB.update(NewsList.TABLE_NAME, values,
					selection, selectionArgs);
			break;

		case NEWS_LIST_ID:
			String news_list_id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowAffected = sqlDB.update(NewsList.TABLE_NAME, values,
						BaseColumns._ID + "=" + news_list_id, null);
			} else {
				rowAffected = sqlDB.update(NewsList.TABLE_NAME, values,
						selection + " and " + BaseColumns._ID + "=" + news_list_id,
						selectionArgs);
			}

			break;
		case OPER_LOG:
			rowAffected = sqlDB.update(HcAppOperLog.TABLE_NAME, values,
					selection, selectionArgs);
			break;

		case OPER_LOG_ID:
			String log_id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowAffected = sqlDB.update(HcAppOperLog.TABLE_NAME, values,
						BaseColumns._ID + "=" + log_id, null);
			} else {
				rowAffected = sqlDB.update(HcAppOperLog.TABLE_NAME, values,
						selection + " and " + BaseColumns._ID + "=" + log_id,
						selectionArgs);
			}

			break;
		case BADGE:
			rowAffected = sqlDB.update(Badge.TABLE_NAME, values,
					selection, selectionArgs);
			break;

		case BADGE_ID:
			String badge_id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowAffected = sqlDB.update(Badge.TABLE_NAME, values,
						BaseColumns._ID + "=" + badge_id, null);
			} else {
				rowAffected = sqlDB.update(Badge.TABLE_NAME, values,
						selection + " and " + BaseColumns._ID + "=" + badge_id,
						selectionArgs);
			}

			break;
		case UPLOAD:
		    rowAffected = sqlDB.update(UploadFile.TABLE_NAME, values,
		            selection, selectionArgs);
		    break;

		case UPLOAD_ID:
		    String upload_id = uri.getLastPathSegment();
		    if (TextUtils.isEmpty(selection)) {
		        rowAffected = sqlDB.update(UploadFile.TABLE_NAME, values,
		                BaseColumns._ID + "=" + upload_id, null);
		    } else {
		        rowAffected = sqlDB.update(UploadFile.TABLE_NAME, values,
		                selection + " and " + BaseColumns._ID + "=" + upload_id,
		                selectionArgs);
		    }

		    break;
		case DOWNLOAD:
		    rowAffected = sqlDB.update(DownloadFile.TABLE_NAME, values,
		            selection, selectionArgs);
		    break;

		case DOWNLOAD_ID:
		    String download_id = uri.getLastPathSegment();
		    if (TextUtils.isEmpty(selection)) {
		        rowAffected = sqlDB.update(DownloadFile.TABLE_NAME, values,
		                BaseColumns._ID + "=" + download_id, null);
		    } else {
		        rowAffected = sqlDB.update(DownloadFile.TABLE_NAME, values,
		                selection + " and " + BaseColumns._ID + "=" + download_id,
		                selectionArgs);
		    }

		    break;
		case TASK:
			rowAffected = sqlDB.update(TaskInfo.TABLE_NAME, values,
					selection, selectionArgs);
			break;

		case TASK_ID:
			String task_id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowAffected = sqlDB.update(TaskInfo.TABLE_NAME, values,
						BaseColumns._ID + "=" + task_id, null);
			} else {
				rowAffected = sqlDB.update(TaskInfo.TABLE_NAME, values,
						selection + " and " + BaseColumns._ID + "=" + task_id,
						selectionArgs);
			}

			break;
		case MESSAGE:
			rowAffected = sqlDB.update(AppMessage.TABLE_NAME, values,
					selection, selectionArgs);
			break;

		case MESSAGE_ID:
			String message_id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowAffected = sqlDB.update(AppMessage.TABLE_NAME, values,
						BaseColumns._ID + "=" + message_id, null);
			} else {
				rowAffected = sqlDB.update(AppMessage.TABLE_NAME, values,
						selection + " and " + BaseColumns._ID + "=" + message_id,
						selectionArgs);
			}

			break;
		case CHAT:
			rowAffected = sqlDB.update(ChatMessage.TABLE_NAME, values,
					selection, selectionArgs);
			break;

		case CHAT_ID:
			String chat_id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowAffected = sqlDB.update(ChatMessage.TABLE_NAME, values,
						BaseColumns._ID + "=" + chat_id, null);
			} else {
				rowAffected = sqlDB.update(ChatMessage.TABLE_NAME, values,
						selection + " and " + BaseColumns._ID + "=" + chat_id,
						selectionArgs);
			}

			break;
		case GROUP:
			rowAffected = sqlDB.update(ChatGroup.TABLE_NAME, values,
					selection, selectionArgs);
			break;

		case GROUP_ID:
			String group_id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowAffected = sqlDB.update(ChatGroup.TABLE_NAME, values,
						BaseColumns._ID + "=" + group_id, null);
			} else {
				rowAffected = sqlDB.update(ChatGroup.TABLE_NAME, values,
						selection + " and " + BaseColumns._ID + "=" + group_id,
						selectionArgs);
			}

			break;
		case SCHEDULE:
			rowAffected = sqlDB.update(Schedule.TABLE_NAME, values,
					selection, selectionArgs);
			break;

		case SCHEDULE_ID:
			String schedule_id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowAffected = sqlDB.update(Schedule.TABLE_NAME, values,
						BaseColumns._ID + "=" + schedule_id, null);
			} else {
				rowAffected = sqlDB.update(Schedule.TABLE_NAME, values,
						selection + " and " + BaseColumns._ID + "=" + schedule_id,
						selectionArgs);
			}

			break;
		default:
			throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return rowAffected;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			int numValues = values.length;
            for (int i = 0; i < numValues; i++) {
            	insert(uri, values[i]);
            }
            db.setTransactionSuccessful();
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			db.endTransaction();
		}
		return values.length;
//		return super.bulkInsert(uri, values);
	}

	
}
