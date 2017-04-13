/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-5-5 下午2:40:17
 */
package com.android.hcframe.sql;

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
import com.android.hcframe.sql.HcDatabase.HcNewsColumen;
import com.android.hcframe.sql.HcDatabase.NewsList;
import com.android.hcframe.sql.HcDatabase.Schedule;
import com.android.hcframe.sql.HcDatabase.SysMessage;
import com.android.hcframe.sql.HcDatabase.TaskInfo;
import com.android.hcframe.sql.HcDatabase.UploadFile;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * DATABASE_VERSION = 6为初始化版本,之前的版本未记录,之后的版本变更都会记录
 * <p>======================================</p>
 * <p>DATABASE_VERSION = 7 变更说明：</p>
 * <p>1.新增{@link NewsList#TABLE_NAME}表</p>
 * <p>2.修改表{@link HcAppMarket#APP_ACCOUNT} = account</p>
 * <p>3.修改表{@link SysMessage#ACCOUNT} = account</p>
 * <p>4.修改表{@link AnnualProgram#ACCOUNT} = account</p>
 * <p>4.修改表{@link HcNewsColumen#NEWS_USER} = account</p>
 * <p>======================================</p>
 * <p>DATABASE_VERSION = 8 变更说明：</p>
 * <p>1.新增{@link HcAppOperLog#TABLE_NAME}表</p>
 * <p>============================================</p>
 * <p>DATABASE_VERSION = 9 变更说明：</p>
 * <p>1.新增{@link Badge#TABLE_NAME}表</p>
 * <p>======================================</p>
 * <p>DATABASE_VERSION = 10 变更说明：</p>
 * <p>1.在通讯录{@link Contacts#TABLE_NAME}表里新增{@link Contacts#VISIBILITY}字段</p>
 * <p>======================================</p>
 * <p>DATABASE_VERSION = 11 变更说明：</p>
 * <p>1.在通讯录{@link Contacts#TABLE_NAME}表里新增{@link Contacts#USER_ID}字段</p>
 * <p>======================================</p>
 * <p>DATABASE_VERSION = 12 变更说明：</p>
 * <p>1.新增{@link DownloadFile#TABLE_NAME}表</p>
 * <p>2.新增{@link UploadFile#TABLE_NAME}表</p>
 * <p>======================================</p>
 * <p>DATABASE_VERSION = 13 变更说明：</p>
 * <p>1.新增{@link TaskInfo#TABLE_NAME}表</p>
 * <p>======================================</p>
 * <p>DATABASE_VERSION = 14 变更说明：</p>
 * <p>1.新增{@link AppMessage#TABLE_NAME}表</p>
 * <p>2.新增{@link ChatMessage#TABLE_NAME}表</p>
 * <p>======================================</p>
 * <p>DATABASE_VERSION = 15 变更说明：</p>
 * <p>1.新增{@link ChatMessage#CHAT_FILE_NAME}字段</p>
 * <p>2.新增{@link ChatMessage#CHAT_VOICE_DURATION}字段</p>
 * <p>3.新增{@link ChatMessage#CHAT_VOICE_READED}字段</p>
 * <p>======================================</p>
 * <p>DATABASE_VERSION = 16 变更说明：</p>
 * <p>1.新增{@link ChatMessage#CHAT_SEND_STATE}字段</p>
 * <p>======================================</p>
 * <p>DATABASE_VERSION = 17 变更说明：</p>
 * <p>1.新增{@link ChatGroup#TABLE_NAME}表</p>
 * <p>======================================</p>
 * <p>DATABASE_VERSION = 18 变更说明：</p>
 * <p>1.新增{@link DownloadFile#URL}字段</p>
 * <p>2.新增{@link UploadFile#URL}字段</p>
 * <p>======================================</p>
 * <p>DATABASE_VERSION = 19 变更说明：</p>
 * <p>1.新增{@link AppMessage#MESSAGE_COUNT}字段</p>
 * <p>======================================</p>
 * <p>DATABASE_VERSION = 20 变更说明：</p>
 * <p>1.新增{@link SysMessage#APP_TYPE}字段</p>
 * <p>2.新增{@link SysMessage#INDEX_CONTENT}字段</p>
 * <p>3.新增{@link SysMessage#APP_NAME}字段</p>
 * <p>======================================</p>
 * <p>DATABASE_VERSION = 21 变更说明：</p>
 * <p>1.新增{@link Schedule#TABLE_NAME}表</p>
 * <p>======================================</p>
 * <p>DATABASE_VERSION = 22 变更说明：</p>
 * <p>1.新增{@link ChatMessage#CHAT_RECEIVER}字段</p>
 * <p>======================================</p>
 * <p>数据库的一些简单操作</p>
 * <p>在已有表中添加字段：db.execSQL("ALTER TABLE app_market " +
                        "ADD COLUMN app_type INTEGER NOT NULL DEFAULT -1;");</p>
 * <p>在已有表中重命名字段不支持,只能先更改表名然后新建表最后拷贝数据</p>
 * <p>在已有表中删除字段不支持</p>
 * <p>重命名已有的表：db.execSQL("ALTER TABLE app_market RENAME TO app_market_temp")</p>
 * <p>导入数据：db.execSQL("INSERT INTO app_market SELECT app_id, "", ProductId FROM app_market_temp")或者
 * db.execSQL("INSERT INTO app_market() SELECT app_id, "", ProductId FROM app_market_temp")  
 * 注意 双引号""是用来补充原来不存在的数据的</p>
 * 
 * <p>============================================</p>
 * @author jrjin
 * @time 2016-1-26 上午9:45:55
 * 
 */
public class HcMarketDatabaseHelper extends SQLiteOpenHelper {

	private static final String TAG = "HcMarketDatabaseHelper";

	private static final String DATABASE_NAME = "hcmarket.db";
	private static final int DATABASE_VERSION_HC = 22;
	private static final int DATABASE_VERSION_OA = 22;
	private static final int DATABASE_VERSION = DATABASE_VERSION_HC;
	private Context mContext;

	public HcMarketDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		HcLog.D(TAG + " it is onCreate!");
		db.execSQL("CREATE TABLE " + HcAppMarket.TABLE_NAME + "("
				+ HcAppMarket._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ HcAppMarket.APP_ID + " TEXT," + HcAppMarket.APP_NAME
				+ " TEXT," + HcAppMarket.APP_VERSION + " TEXT,"
				+ HcAppMarket.APP_CATEGORY + " INTEGER," + HcAppMarket.APP_ICON
				+ " TEXT,"
				/* + HcAppMarket.APP_LATEST_VERSION + " TEXT," */
				+ HcAppMarket.APP_ORDER_ALL + " INTEGER,"
				+ HcAppMarket.APP_ORDER_CATEGORY + " INTEGER,"
				+ HcAppMarket.APP_ORDER_SERVER + " INTEGER,"
				+ HcAppMarket.APP_PACKAGE + " TEXT," + HcAppMarket.APP_SIZE
				+ " INTEGER," + HcAppMarket.APP_STATE + " INTEGER,"
				+ HcAppMarket.APP_URL + " TEXT," + HcAppMarket.APP_TYPE
				+ " INTEGER," + HcAppMarket.APP_USED + " INTEGER,"
				+ HcAppMarket.APP_ACCOUNT + " TEXT,"
				+ HcAppMarket.APP_CATEGORY_NAME + " TEXT" + ");");
		
		db.execSQL("CREATE TABLE " + HcNewsColumen.TABLE_NAME + "("
				+ HcNewsColumen._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ HcNewsColumen.NEWSID + " TEXT," + HcNewsColumen.TYPE
				+ " INTEGER DEFAULT -1," + HcNewsColumen.CONTENTTYPE
				+ " INTEGER DEFAULT -1," + HcNewsColumen.ISSCROLLTOPIC
				+ " INTEGER DEFAULT -1," + HcNewsColumen.NEWS_USER + " TEXT,"
				+ HcNewsColumen.NAME + " TEXT," + HcNewsColumen.COLUMN_TYPE
				+ " INTEGER" + ");");
		
		db.execSQL("CREATE TABLE " + DataRecord.TABLE_NAME + "("
				+ DataRecord._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ DataRecord.DATA_FLAG + " INTEGER," + DataRecord.DATA_NAME
				+ " TEXT," + DataRecord.DATA_READ_TIME + " INTEGER,"
				+ DataRecord.DATA_SIZE + " INTEGER," + DataRecord.DATA_ID
				+ " TEXT," + DataRecord.DATA_DATE + " TEXT,"
				+ DataRecord.ACCOUNT + " TEXT" + ");");
		
		db.execSQL("CREATE TABLE " + DataRecordDetail.TABLE_NAME + "("
				+ DataRecordDetail._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ DataRecordDetail.DATA_FLAG + " INTEGER,"
				+ DataRecordDetail.DATA_NAME + " TEXT,"
				+ DataRecordDetail.DATA_ID + " TEXT,"
				+ DataRecordDetail.DATA_SOURCE + " TEXT,"
				+ DataRecordDetail.DATA_TIME + " TEXT,"
				+ DataRecordDetail.FILE_ID + " TEXT,"
				+ DataRecordDetail.FILE_NAME + " TEXT,"
				+ DataRecordDetail.FILE_SIZE + " INTEGER,"
				+ DataRecordDetail.FILE_URL + " TEXT,"
				+ DataRecordDetail.ACCOUNT + " TEXT" + ");");
		
		db.execSQL("CREATE TABLE " + Contacts.TABLE_NAME + "(" + Contacts._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + Contacts.TYPE
				+ " INTEGER," + Contacts.NAME + " TEXT," + Contacts.ID
				+ " TEXT," + Contacts.PARENT_ID + " TEXT,"
				+ Contacts.PARENT_NAME + " TEXT," + Contacts.EMAIL + " TEXT,"
				+ Contacts.EXTENSION_NUMBER + " TEXT," + Contacts.FIXED_PHONE
				+ " TEXT," + Contacts.MOBILE_PHONE + " TEXT,"
				+ Contacts.VIRTUAL_NET_NUMBER + " TEXT,"
				+ Contacts.NAME_A + " TEXT,"
				+ Contacts.NAME_JAINPIN + " TEXT,"
				+ Contacts.NAME_QUANPIN + " TEXT,"
				+ Contacts.NAME_ICON + " TEXT,"
				+ Contacts.VISIBILITY + " INTEGER,"
				+ Contacts.STANDBY_PHONE + " TEXT,"
				+ Contacts.USER_ID + " TEXT,"
				+ Contacts.STANDBY_EMAIL
				+ " TEXT" + ");");
		
		db.execSQL("CREATE TABLE " + SysMessage.TABLE_NAME + "(" 
				+ SysMessage._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ SysMessage.TYPE + " INTEGER," 
				+ SysMessage.CONTENT_ID + " TEXT," 
				+ SysMessage.APP_ID + " TEXT," 
				+ SysMessage.CONTENT + " TEXT,"
				+ SysMessage.DATE + " TEXT," 
				+ SysMessage.READ + " INTEGER,"
				+ SysMessage.APP_TYPE + " INTEGER DEFAULT -1,"
				+ SysMessage.INDEX_CONTENT + " TEXT,"
				+ SysMessage.APP_NAME + " TEXT,"
				+ SysMessage.ACCOUNT + " TEXT," 
				+ SysMessage.TITLE + " TEXT" 
				+ ");");
		
		db.execSQL("CREATE TABLE " + AnnualProgram.TABLE_NAME + "(" 
				+ AnnualProgram._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ AnnualProgram.TYPE + " INTEGER," 
				+ AnnualProgram.SCORE + " INTEGER,"
				+ AnnualProgram.CONTENT + " TEXT,"
				+ AnnualProgram.PROGRAM_ID + " TEXT," 
				+ AnnualProgram.ACCOUNT + " TEXT," 
				+ AnnualProgram.TITLE + " TEXT," 
				+ AnnualProgram.ANNUAL_ID + " TEXT"
				+ ");");
		
		db.execSQL("CREATE TABLE " + NewsList.TABLE_NAME + "(" 
				+ NewsList._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ NewsList.CONTENT_TYPE + " INTEGER," 
				+ NewsList.ACCOUNT + " TEXT," 
				+ NewsList.NEWS_ADDRESS + " TEXT," 
				+ NewsList.NEWS_COLUMN_ID + " TEXT,"
				+ NewsList.NEWS_CONTENT + " TEXT," 
				+ NewsList.NEWS_CONTENT_URI + " TEXT,"
				+ NewsList.NEWS_DATA + " TEXT," 
				+ NewsList.NEWS_ICON_URI + " TEXT,"
				+ NewsList.NEWS_ID + " TEXT,"
				+ NewsList.NEWS_TITLE + " TEXT,"
				+ NewsList.NEWS_PIC_COUNT + " INTEGER,"
				+ NewsList.SCROLL + " INTEGER DEFAULT 0"
				+ ");");
		db.execSQL("CREATE TABLE " + HcAppOperLog.TABLE_NAME + "("
				+ HcAppOperLog._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ HcAppOperLog.TYPE + " INTEGER,"
				+ HcAppOperLog.APP_ID + " TEXT,"
				+ HcAppOperLog.END + " TEXT,"
				+ HcAppOperLog.IMEI + " TEXT,"
				+ HcAppOperLog.ACCOUNT + " TEXT,"
				+ HcAppOperLog.START + " TEXT,"
				+ HcAppOperLog.MODULE_ID + " TEXT,"
				+ HcAppOperLog.RESULT + " INTEGER DEFAULT -1,"
				+ HcAppOperLog.NAME + " TEXT,"
				+ HcAppOperLog.VERSION + " TEXT"
				+ ");");
		db.execSQL("CREATE TABLE " + Badge.TABLE_NAME + "("
				+ Badge._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Badge.TYPE + " INTEGER,"
				+ Badge.APP_ID + " TEXT,"
				+ Badge.MODULE_ID + " TEXT,"
				+ Badge.VISIBILITY + " INTEGER,"
				+ Badge.ACCOUNT + " TEXT,"
				+ Badge.BADGE_TYPE + " INTEGER,"
				+ Badge.COUNT + " INTEGER DEFAULT 0"
				+ ");");
		db.execSQL("CREATE TABLE " + UploadFile.TABLE_NAME + "("
				+ UploadFile._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ UploadFile.ACCOUNT + " TEXT,"
				+ UploadFile.FILEKEY + " TEXT,"
				+ UploadFile.MD5 + " TEXT,"
				+ UploadFile.PATH + " TEXT,"
				+ UploadFile.ALL_SLICE + " INTEGER,"
				+ UploadFile.SLICE + " INTEGER,"
				+ UploadFile.NAME + " TEXT,"
				+ UploadFile.EXT + " TEXT,"
				+ UploadFile.TYPE + " TEXT,"
				+ UploadFile.POSITION + " TEXT,"
				+ UploadFile.FILESIZE + " TEXT,"
				+ UploadFile.DIRID + " TEXT,"
				+ UploadFile.URL + " TEXT,"
				+ UploadFile.STATE + " TEXT"
				+ ");");
		db.execSQL("CREATE TABLE " + DownloadFile.TABLE_NAME + "("
				+ DownloadFile._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ DownloadFile.FILEId + " TEXT,"
				+ DownloadFile.ACCOUNT + " TEXT,"
				+ DownloadFile.PATH + " TEXT,"
				+ DownloadFile.FILESIZE + " TEXT,"
				+ DownloadFile.POSITION + " TEXT,"
				+ DownloadFile.NAME + " TEXT,"
				+ DownloadFile.EXT + " TEXT,"
				+ DownloadFile.UPDIRId + " TEXT,"
				+ DownloadFile.URL + " TEXT,"
				+ DownloadFile.STATE + " TEXT"
				+ ");");
		db.execSQL("CREATE TABLE " + TaskInfo.TABLE_NAME + "("
				+ TaskInfo._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ TaskInfo.STATUS + " INTEGER,"
				+ TaskInfo.CONTENT + " TEXT,"
				+ TaskInfo.DEADLINE + " TEXT,"
				+ TaskInfo.EXECUTOR + " TEXT,"
				+ TaskInfo.EXECUTOR_ID + " TEXT,"
				+ TaskInfo.EXECUTOR_URL + " TEXT,"
				+ TaskInfo.PUBLISHER + " TEXT,"
				+ TaskInfo.PUBLISHER_ID + " TEXT,"
				+ TaskInfo.PUBLISHER_URL + " TEXT,"
				+ TaskInfo.RELEASE_DATE + " TEXT,"
				+ TaskInfo.USER_ID + " TEXT,"
				+ TaskInfo.ID + " TEXT"
				+ ");");
		db.execSQL("CREATE TABLE " + AppMessage.TABLE_NAME + "("
				+ AppMessage._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ AppMessage.MESSAGE_CONTENT + " TEXT,"
				+ AppMessage.MESSAGE_DATE + " TEXT,"
				+ AppMessage.MESSAGE_ID + " TEXT,"
				+ AppMessage.MESSAGE_TITLE + " TEXT,"
				+ AppMessage.MESSAGE_ICON + " TEXT,"
				+ AppMessage.MESSAGE_TYPE + " INTEGER,"
				+ AppMessage.MESSAGE_COUNT + " INTEGER,"
				+ AppMessage.USER_ID + " TEXT"
				+ ");");
		db.execSQL("CREATE TABLE " + ChatMessage.TABLE_NAME + "("
				+ ChatMessage._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ ChatMessage.CHAT_CONTENT + " TEXT,"
				+ ChatMessage.CHAT_DATE + " TEXT,"
				+ ChatMessage.Chat_ID + " TEXT,"
				+ ChatMessage.CHAT_NAME + " TEXT,"
				+ ChatMessage.USER_ID + " TEXT,"
				+ ChatMessage.CHAT_MESSAGE_ID + " TEXT,"
				+ ChatMessage.CHAT_TYPE + " INTEGER,"
				+ ChatMessage.CHAT_SHOW_TIME + " INTEGER,"
				+ ChatMessage.CHAT_OWN + " INTEGER,"
				+ ChatMessage.CHAT_VOICE_DURATION + " INTEGER,"
				+ ChatMessage.CHAT_FILE_NAME + " TEXT,"
				+ ChatMessage.CHAT_RECEIVER + " TEXT,"
				+ ChatMessage.CHAT_VOICE_READED + " INTEGER,"
				+ ChatMessage.CHAT_SEND_STATE + " INTEGER DEFAULT 0"
				+ ");");
		db.execSQL("CREATE TABLE " + ChatGroup.TABLE_NAME + "("
				+ ChatGroup._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ ChatGroup.GROUP_ICON + " TEXT,"
				+ ChatGroup.GROUP_ID + " TEXT,"
				+ ChatGroup.GROUP_MEMBERS + " TEXT,"
				+ ChatGroup.GROUP_NAME + " TEXT,"
				+ ChatGroup.GROUP_COUNT + " INTEGER,"
				+ ChatGroup.GROUP_NOTICED + " INTEGER,"
				+ ChatGroup.USER_ID + " TEXT"
				+ ");");
		db.execSQL("CREATE TABLE " + Schedule.TABLE_NAME + "("
				+ Schedule._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Schedule.USER_ID + " TEXT,"
				+ Schedule.SCHEDULE_ID + " TEXT,"
				+ Schedule.SCHEDULE_STARTTIME + " TEXT,"
				+ Schedule.SCHEDULE_ENDTIME + " TEXT,"
				+ Schedule.SCHEDULE_TASKTYPE + " TEXT,"
				+ Schedule.SCHEDULE_TASKMEMBERS + " TEXT,"
				+ Schedule.SCHEDULE_THEME + " TEXT,"
				+ Schedule.SCHEDULE_CREATOR + " TEXT,"
				+ Schedule.SCHEDULE_CREATFLAG + " TEXT,"
				+ Schedule.SCHEDULE_ADDITION + " TEXT,"
				+ Schedule.SCHEDULE_DATE + " TEXT"
				+ ");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		HcLog.D(TAG + " oldVersion = " + oldVersion + " newVersion = "
				+ newVersion);
		int version = oldVersion;
		if (oldVersion < 6) {
			db.execSQL("DROP TABLE IF EXISTS " + HcAppMarket.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + HcNewsColumen.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + DataRecord.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + DataRecordDetail.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Contacts.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + SysMessage.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + AnnualProgram.TABLE_NAME);
			onCreate(db);
			version = 6;
		}
		
		if (version < 7) {
			db.beginTransaction();
			try {
				db.execSQL("DROP TABLE IF EXISTS " + NewsList.TABLE_NAME);
				db.execSQL("DROP TABLE IF EXISTS " + HcAppMarket.TABLE_NAME);
				db.execSQL("DROP TABLE IF EXISTS " + SysMessage.TABLE_NAME);
				db.execSQL("DROP TABLE IF EXISTS " + AnnualProgram.TABLE_NAME);
				db.execSQL("DROP TABLE IF EXISTS " + HcNewsColumen.TABLE_NAME);
				createDatabase7(db);
				db.setTransactionSuccessful();
			} catch (SQLException e) {
				// TODO: handle exception
				HcLog.D(TAG + " SQLException error ============================== "+e.getMessage());
			} finally {
				db.endTransaction();
			}
			version = 7;
		}
		if (version < 8) {
			db.execSQL("DROP TABLE IF EXISTS " + HcAppOperLog.TABLE_NAME);
			createDatabase8(db);
			version = 8;
		}

		if (version < 9) {
			db.execSQL("DROP TABLE IF EXISTS " + Badge.TABLE_NAME);
			createDatabase9(db);
			version = 9;
		}

		if (version < 10) {
			db.execSQL("ALTER TABLE " + Contacts.TABLE_NAME +
					" ADD COLUMN " + Contacts.VISIBILITY + " INTEGER;");
			version = 10;
		}
		if (version < 11) {
			db.execSQL("ALTER TABLE " + Contacts.TABLE_NAME +
					" ADD COLUMN " + Contacts.USER_ID + " TEXT;");
			version = 11;
		}

		if (version < 12) {
			createDatabase12(db);
			version = 12;
		}

		if (version < 13) {
			createDatabase13(db);
			version = 13;
		}

		if (version < 14) {
			createDatabase14(db);
			version = 14;
		}

		if (version < 15) {
			db.execSQL("ALTER TABLE " + ChatMessage.TABLE_NAME +
					" ADD COLUMN " + ChatMessage.CHAT_VOICE_DURATION + " INTEGER;");
			db.execSQL("ALTER TABLE " + ChatMessage.TABLE_NAME +
					" ADD COLUMN " + ChatMessage.CHAT_FILE_NAME + " TEXT;");
			db.execSQL("ALTER TABLE " + ChatMessage.TABLE_NAME +
					" ADD COLUMN " + ChatMessage.CHAT_VOICE_READED + " INTEGER;");
			version = 15;
		}

		if (version < 16) {
			db.execSQL("ALTER TABLE " + ChatMessage.TABLE_NAME +
					" ADD COLUMN " + ChatMessage.CHAT_SEND_STATE + " INTEGER DEFAULT 0;");
			version = 16;
		}

		if (version < 17) {
			createDatabase17(db);
			version = 17;
		}

		if (version < 18) {
			db.execSQL("ALTER TABLE " + UploadFile.TABLE_NAME +
					" ADD COLUMN " + UploadFile.URL + " TEXT;");
			db.execSQL("ALTER TABLE " + DownloadFile.TABLE_NAME +
					" ADD COLUMN " + DownloadFile.URL + " TEXT;");
			version = 18;
		}

		if (version < 19) {
			db.execSQL("ALTER TABLE " + AppMessage.TABLE_NAME +
					" ADD COLUMN " + AppMessage.MESSAGE_COUNT + " INTEGER DEFAULT 0;");
			version = 19;
		}

		if (version < 20) {
			db.execSQL("ALTER TABLE " + SysMessage.TABLE_NAME +
					" ADD COLUMN " + SysMessage.APP_TYPE + " INTEGER DEFAULT -1;");
			db.execSQL("ALTER TABLE " + SysMessage.TABLE_NAME +
					" ADD COLUMN " + SysMessage.INDEX_CONTENT + " TEXT;");
			db.execSQL("ALTER TABLE " + SysMessage.TABLE_NAME +
					" ADD COLUMN " + SysMessage.APP_NAME + " TEXT;");
			version = 20;
		}

		if (version < 21) {
			createDatabase21(db);
			version = 21;
		}

		if (version < 22) {
			db.execSQL("ALTER TABLE " + ChatMessage.TABLE_NAME +
					" ADD COLUMN " + ChatMessage.CHAT_RECEIVER + " TEXT;");
			version = 22;
		}

		if (version != DATABASE_VERSION) { // 一般不会出现
			db.execSQL("DROP TABLE IF EXISTS " + HcAppMarket.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + HcNewsColumen.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + DataRecord.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + DataRecordDetail.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Contacts.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + SysMessage.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + AnnualProgram.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + NewsList.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + HcAppOperLog.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Badge.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + UploadFile.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + DownloadFile.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + TaskInfo.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + AppMessage.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + ChatMessage.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + ChatGroup.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Schedule.TABLE_NAME);
			onCreate(db);
		}
		
	}

	private void createNewsListTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + NewsList.TABLE_NAME + "(" 
				+ NewsList._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ NewsList.CONTENT_TYPE + " INTEGER," 
				+ NewsList.ACCOUNT + " TEXT," 
				+ NewsList.NEWS_ADDRESS + " TEXT," 
				+ NewsList.NEWS_COLUMN_ID + " TEXT,"
				+ NewsList.NEWS_CONTENT + " TEXT," 
				+ NewsList.NEWS_CONTENT_URI + " TEXT,"
				+ NewsList.NEWS_DATA + " TEXT," 
				+ NewsList.NEWS_ICON_URI + " TEXT,"
				+ NewsList.NEWS_ID + " TEXT,"
				+ NewsList.NEWS_TITLE + " TEXT,"
				+ NewsList.NEWS_PIC_COUNT + " INTEGER,"
				+ NewsList.SCROLL + " INTEGER DEFAULT 0"
				+ ");");
	}
	
	private void createDatabase7(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + HcAppMarket.TABLE_NAME + "("
				+ HcAppMarket._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ HcAppMarket.APP_ID + " TEXT," + HcAppMarket.APP_NAME
				+ " TEXT," + HcAppMarket.APP_VERSION + " TEXT,"
				+ HcAppMarket.APP_CATEGORY + " INTEGER," + HcAppMarket.APP_ICON
				+ " TEXT,"
				/* + HcAppMarket.APP_LATEST_VERSION + " TEXT," */
				+ HcAppMarket.APP_ORDER_ALL + " INTEGER,"
				+ HcAppMarket.APP_ORDER_CATEGORY + " INTEGER,"
				+ HcAppMarket.APP_ORDER_SERVER + " INTEGER,"
				+ HcAppMarket.APP_PACKAGE + " TEXT," + HcAppMarket.APP_SIZE
				+ " INTEGER," + HcAppMarket.APP_STATE + " INTEGER,"
				+ HcAppMarket.APP_URL + " TEXT," + HcAppMarket.APP_TYPE
				+ " INTEGER," + HcAppMarket.APP_USED + " INTEGER,"
				+ HcAppMarket.APP_ACCOUNT + " TEXT,"
				+ HcAppMarket.APP_CATEGORY_NAME + " TEXT" + ");");
		
		db.execSQL("CREATE TABLE " + SysMessage.TABLE_NAME + "(" 
				+ SysMessage._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ SysMessage.TYPE + " INTEGER," 
				+ SysMessage.CONTENT_ID + " TEXT," 
				+ SysMessage.APP_ID + " TEXT," 
				+ SysMessage.CONTENT + " TEXT,"
				+ SysMessage.DATE + " TEXT," 
				+ SysMessage.READ + " INTEGER,"
				+ SysMessage.ACCOUNT + " TEXT," 
				+ SysMessage.TITLE + " TEXT" 
				+ ");");
		
		db.execSQL("CREATE TABLE " + AnnualProgram.TABLE_NAME + "(" 
				+ AnnualProgram._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ AnnualProgram.TYPE + " INTEGER," 
				+ AnnualProgram.SCORE + " INTEGER,"
				+ AnnualProgram.CONTENT + " TEXT,"
				+ AnnualProgram.PROGRAM_ID + " TEXT," 
				+ AnnualProgram.ACCOUNT + " TEXT," 
				+ AnnualProgram.TITLE + " TEXT," 
				+ AnnualProgram.ANNUAL_ID + " TEXT"
				+ ");");
		
		db.execSQL("CREATE TABLE " + NewsList.TABLE_NAME + "(" 
				+ NewsList._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ NewsList.CONTENT_TYPE + " INTEGER," 
				+ NewsList.ACCOUNT + " TEXT," 
				+ NewsList.NEWS_ADDRESS + " TEXT," 
				+ NewsList.NEWS_COLUMN_ID + " TEXT,"
				+ NewsList.NEWS_CONTENT + " TEXT," 
				+ NewsList.NEWS_CONTENT_URI + " TEXT,"
				+ NewsList.NEWS_DATA + " TEXT," 
				+ NewsList.NEWS_ICON_URI + " TEXT,"
				+ NewsList.NEWS_ID + " TEXT,"
				+ NewsList.NEWS_TITLE + " TEXT,"
				+ NewsList.NEWS_PIC_COUNT + " INTEGER,"
				+ NewsList.SCROLL + " INTEGER DEFAULT 0"
				+ ");");
		
		db.execSQL("CREATE TABLE " + HcNewsColumen.TABLE_NAME + "("
				+ HcNewsColumen._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ HcNewsColumen.NEWSID + " TEXT," + HcNewsColumen.TYPE
				+ " INTEGER DEFAULT -1," + HcNewsColumen.CONTENTTYPE
				+ " INTEGER DEFAULT -1," + HcNewsColumen.ISSCROLLTOPIC
				+ " INTEGER DEFAULT -1," + HcNewsColumen.NEWS_USER + " TEXT,"
				+ HcNewsColumen.NAME + " TEXT," + HcNewsColumen.COLUMN_TYPE
				+ " INTEGER" + ");");
	}
	
	private void createDatabase8(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + HcAppOperLog.TABLE_NAME + "(" 
				+ HcAppOperLog._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ HcAppOperLog.TYPE + " INTEGER," 
				+ HcAppOperLog.APP_ID + " TEXT,"
				+ HcAppOperLog.END + " TEXT,"
				+ HcAppOperLog.IMEI + " TEXT," 
				+ HcAppOperLog.ACCOUNT + " TEXT," 
				+ HcAppOperLog.START + " TEXT,"
				+ HcAppOperLog.MODULE_ID + " TEXT,"
				+ HcAppOperLog.RESULT + " INTEGER DEFAULT -1,"
				+ HcAppOperLog.NAME + " TEXT,"
				+ HcAppOperLog.VERSION + " TEXT"
				+ ");");
	}

	private void createDatabase9(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + Badge.TABLE_NAME + "("
				+ Badge._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Badge.TYPE + " INTEGER,"
				+ Badge.APP_ID + " TEXT,"
				+ Badge.MODULE_ID + " TEXT,"
				+ Badge.VISIBILITY + " INTEGER,"
				+ Badge.ACCOUNT + " TEXT,"
				+ Badge.BADGE_TYPE + " INTEGER,"
				+ Badge.COUNT + " INTEGER DEFAULT 0"
				+ ");");
	}

	private void createDatabase12(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + UploadFile.TABLE_NAME + "("
				+ UploadFile._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ UploadFile.ACCOUNT + " TEXT,"
				+ UploadFile.FILEKEY + " TEXT,"
				+ UploadFile.MD5 + " TEXT,"
				+ UploadFile.PATH + " TEXT,"
				+ UploadFile.ALL_SLICE + " INTEGER,"
				+ UploadFile.SLICE + " INTEGER,"
				+ UploadFile.NAME + " TEXT,"
				+ UploadFile.EXT + " TEXT,"
				+ UploadFile.TYPE + " TEXT,"
				+ UploadFile.POSITION + " TEXT,"
				+ UploadFile.FILESIZE + " TEXT,"
				+ UploadFile.DIRID + " TEXT,"
				+ UploadFile.STATE + " TEXT"
				+ ");");

		db.execSQL("CREATE TABLE " + DownloadFile.TABLE_NAME + "("
				+ DownloadFile._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ DownloadFile.FILEId + " TEXT,"
				+ DownloadFile.ACCOUNT + " TEXT,"
				+ DownloadFile.PATH + " TEXT,"
				+ DownloadFile.FILESIZE + " TEXT,"
				+ DownloadFile.POSITION + " TEXT,"
				+ DownloadFile.NAME + " TEXT,"
				+ DownloadFile.EXT + " TEXT,"
				+ DownloadFile.UPDIRId + " TEXT,"
				+ DownloadFile.STATE + " TEXT"
				+ ");");
	}

	private void createDatabase13(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TaskInfo.TABLE_NAME + "("
				+ TaskInfo._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ TaskInfo.STATUS + " INTEGER,"
				+ TaskInfo.CONTENT + " TEXT,"
				+ TaskInfo.DEADLINE + " TEXT,"
				+ TaskInfo.EXECUTOR + " TEXT,"
				+ TaskInfo.EXECUTOR_ID + " TEXT,"
				+ TaskInfo.EXECUTOR_URL + " TEXT,"
				+ TaskInfo.PUBLISHER + " TEXT,"
				+ TaskInfo.PUBLISHER_ID + " TEXT,"
				+ TaskInfo.PUBLISHER_URL + " TEXT,"
				+ TaskInfo.RELEASE_DATE + " TEXT,"
				+ TaskInfo.USER_ID + " TEXT,"
				+ TaskInfo.ID + " TEXT"
				+ ");");
	}

	private void createDatabase14(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + AppMessage.TABLE_NAME + "("
				+ AppMessage._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ AppMessage.MESSAGE_CONTENT + " TEXT,"
				+ AppMessage.MESSAGE_DATE + " TEXT,"
				+ AppMessage.MESSAGE_ID + " TEXT,"
				+ AppMessage.MESSAGE_TITLE + " TEXT,"
				+ AppMessage.MESSAGE_ICON + " TEXT,"
				+ AppMessage.MESSAGE_TYPE + " INTEGER,"
				+ AppMessage.USER_ID + " TEXT"
				+ ");");
		db.execSQL("CREATE TABLE " + ChatMessage.TABLE_NAME + "("
				+ ChatMessage._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ ChatMessage.CHAT_CONTENT + " TEXT,"
				+ ChatMessage.CHAT_DATE + " TEXT,"
				+ ChatMessage.Chat_ID + " TEXT,"
				+ ChatMessage.CHAT_NAME + " TEXT,"
				+ ChatMessage.USER_ID + " TEXT,"
				+ ChatMessage.CHAT_MESSAGE_ID + " TEXT,"
				+ ChatMessage.CHAT_TYPE + " INTEGER,"
				+ ChatMessage.CHAT_SHOW_TIME + " INTEGER,"
				+ ChatMessage.CHAT_OWN + " INTEGER"
				+ ");");
	}

	private void createDatabase17(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + ChatGroup.TABLE_NAME + "("
				+ ChatGroup._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ ChatGroup.GROUP_ICON + " TEXT,"
				+ ChatGroup.GROUP_ID + " TEXT,"
				+ ChatGroup.GROUP_MEMBERS + " TEXT,"
				+ ChatGroup.GROUP_NAME + " TEXT,"
				+ ChatGroup.GROUP_COUNT + " INTEGER,"
				+ ChatGroup.GROUP_NOTICED + " INTEGER,"
				+ ChatGroup.USER_ID + " TEXT"
				+ ");");
	}

	private void createDatabase21(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + Schedule.TABLE_NAME + "("
				+ Schedule._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Schedule.USER_ID + " TEXT,"
				+ Schedule.SCHEDULE_ID + " TEXT,"
				+ Schedule.SCHEDULE_STARTTIME + " TEXT,"
				+ Schedule.SCHEDULE_ENDTIME + " TEXT,"
				+ Schedule.SCHEDULE_TASKTYPE + " TEXT,"
				+ Schedule.SCHEDULE_TASKMEMBERS + " TEXT,"
				+ Schedule.SCHEDULE_THEME + " TEXT,"
				+ Schedule.SCHEDULE_CREATOR + " TEXT,"
				+ Schedule.SCHEDULE_CREATFLAG + " TEXT,"
				+ Schedule.SCHEDULE_ADDITION + " TEXT,"
				+ Schedule.SCHEDULE_DATE + " TEXT"
				+ ");");
	}
}
