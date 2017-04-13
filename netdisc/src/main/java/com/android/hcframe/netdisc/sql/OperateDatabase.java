/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-4-27 上午10:54:11
 */
package com.android.hcframe.netdisc.sql;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.android.hcframe.HcLog;
import com.android.hcframe.sql.HcDatabase;
import com.android.hcframe.sql.HcDatabase.DownloadFile;
import com.android.hcframe.sql.HcDatabase.SysMessage;
import com.android.hcframe.sql.HcDatabase.UploadFile;
import com.android.hcframe.sql.HcProvider;
import com.android.hcframe.sql.SettingHelper;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public final class OperateDatabase {

    private static final String TAG = "OperateDatabase";

    private OperateDatabase() {
    }

    /**
     * 添加上传文件
     *
     * @param uploadColumn
     * @param context
     * @author jrjin
     * @time 2016-1-28 上午11:53:26
     */
    public static synchronized boolean insertUpload(com.android.frame.download.FileColumn uploadColumn, Context context) {
        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        String where = UploadFile.FILEKEY + "=" + "'"
                + uploadColumn.getFileid() + "'"
                + " AND " + SysMessage.ACCOUNT + "=" + "'"
                + SettingHelper.getAccount(context) + "'"
                + " AND " + UploadFile.DIRID + "=" + "'"
                + uploadColumn.getUpdirid() + "'";
        Cursor cursor = cr.query(HcProvider.CONTENT_URI_UPLOAD_FILE, null, where, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            return false;
        } else {
            values.clear();
            values.put(UploadFile.ACCOUNT, SettingHelper.getAccount(context));
            values.put(UploadFile.FILEKEY, uploadColumn.getFileid());
            values.put(UploadFile.MD5, uploadColumn.getMd5());
            values.put(UploadFile.PATH, uploadColumn.getPath());
            values.put(UploadFile.ALL_SLICE, uploadColumn.getAll_slice());
            values.put(UploadFile.SLICE, uploadColumn.getSlice());
            values.put(UploadFile.NAME, uploadColumn.getName());
            values.put(UploadFile.EXT, uploadColumn.getExt());
            values.put(UploadFile.TYPE, uploadColumn.getType());
            values.put(UploadFile.POSITION, uploadColumn.getPosition());
            values.put(UploadFile.STATE, uploadColumn.getState());
            values.put(UploadFile.FILESIZE, uploadColumn.getFileSize());
            values.put(UploadFile.DIRID, uploadColumn.getUpdirid());
            values.put(UploadFile.URL, uploadColumn.getUrl());
            cursor.close();
            cr.insert(HcProvider.CONTENT_URI_UPLOAD_FILE, values);
            return true;
        }
    }


    /**
     * 获取上传列表
     *
     * @param context
     * @author jrjin
     * @time 2016-1-28 上午11:53:26
     */
    public static synchronized List<com.android.frame.download.FileColumn> getuploadList(Context context) {
        HcLog.D("getuploadList###########fileColumn");
        final ContentResolver cr = context.getContentResolver();
        List<com.android.frame.download.FileColumn> downloadList = new ArrayList<com.android.frame.download.FileColumn>();
        String where = SysMessage.ACCOUNT + "=" + "'"
                + SettingHelper.getAccount(context) + "'";
        Cursor cursor = cr.query(HcProvider.CONTENT_URI_UPLOAD_FILE, null, where, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            try {
                downloadList = getUploadInfoList(cursor);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        return downloadList;
    }

    public static synchronized List<com.android.frame.download.FileColumn> getUploadInfoList(Cursor cursor) throws JSONException {
        int count = cursor.getColumnCount();
        String[] ColumnName = cursor.getColumnNames();

        List<com.android.frame.download.FileColumn> downloadColumns = new ArrayList<com.android.frame.download.FileColumn>();
        while (cursor.moveToNext()) {
            com.android.frame.download.FileColumn download = new com.android.frame.download.FileColumn();
            for (int i = 1; i < count; i++) {
                if (UploadFile.FILEKEY.equals(ColumnName[i])) {
                    download.setFileid(cursor.getString(i));
                } else if (UploadFile.NAME.equals(ColumnName[i])) {
                    download.setName(cursor.getString(i));
                } else if (UploadFile.EXT.equals(ColumnName[i])) {
                    download.setExt(cursor.getString(i));
                } else if (UploadFile.MD5.equals(ColumnName[i])) {
                    download.setMd5(cursor.getString(i));
                } else if (UploadFile.STATE.equals(ColumnName[i])) {
                    download.setState(cursor.getString(i));
                } else if (UploadFile.POSITION.equals(ColumnName[i])) {
                    download.setPosition(Long.parseLong(cursor.getString(i)));
                } else if (UploadFile.FILESIZE.equals(ColumnName[i])) {
                    download.setFileSize(cursor.getString(i));
                } else if (UploadFile.PATH.equals(ColumnName[i])) {
                    download.setPath(cursor.getString(i));
                } else if (UploadFile.TYPE.equals(ColumnName[i])) {
                    download.setType(cursor.getString(i));
                } else if (UploadFile.ALL_SLICE.equals(ColumnName[i])) {
                    download.setAll_slice(Integer.decode(cursor.getString(i)));
                } else if (UploadFile.SLICE.equals(ColumnName[i])) {
                    download.setSlice(Integer.decode(cursor.getString(i)));
                } else if (UploadFile.DIRID.equals(ColumnName[i])) {
                    download.setUpdirid(cursor.getString(i));
                } else if (UploadFile.URL.equals(ColumnName[i])) {
                    download.setUrl(cursor.getString(i));
                }
            }
            download.setUpOrDown(0);
            download.setLevel(1);
            download.setSource(1);
            downloadColumns.add(download);
        }
        return downloadColumns;
    }

    /**
     * 更新上传数据库
     *
     * @param downloadColumn
     * @param context
     * @return
     * @author jrjin
     * @time 2015-12-3 下午4:54:40
     */
    public static synchronized int updateUploadInfo(com.android.frame.download.FileColumn downloadColumn, Context context) {
        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        String where = UploadFile.FILEKEY + "=" + "'" + downloadColumn.getFileid() + "'"
                + " AND " + SysMessage.ACCOUNT + "=" + "'"
                + SettingHelper.getAccount(context) + "'";
        HcLog.D(TAG + " updateDownloadInfo start! where = " + where);
        values.put(UploadFile.POSITION, downloadColumn.getPosition());
        values.put(UploadFile.STATE, downloadColumn.getState());
        int num = cr.update(HcProvider.CONTENT_URI_UPLOAD_FILE, values, where, null);
        return num;
    }

    /**
     * 删除上传完成的信息
     *
     * @param downloadColumn
     * @param context
     */
    public static synchronized void deleteUploadInfo(com.android.frame.download.FileColumn downloadColumn, Context context) {
        HcLog.D("deleteUploadInfo###########fileColumn" + downloadColumn.getName() + "." + downloadColumn.getExt());
        final ContentResolver cr = context.getContentResolver();
        String where = UploadFile.FILEKEY + "=" + "'" + downloadColumn.getFileid() + "'"
                + " AND " + SysMessage.ACCOUNT + "=" + "'"
                + SettingHelper.getAccount(context) + "'";
        cr.delete(HcProvider.CONTENT_URI_UPLOAD_FILE, where, null);
    }

    /**
     * 添加下载文件
     *
     * @param fileColumn
     * @param context
     * @author jrjin
     * @time 2016-1-28 上午11:53:26
     */
    public static synchronized boolean insertDownload(com.android.frame.download.FileColumn fileColumn, Context context) {
        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        String where = DownloadFile.FILEId + "=" + "'"
                + fileColumn.getFileid() + "'"
                + " AND " + SysMessage.ACCOUNT + "=" + "'"
                + SettingHelper.getAccount(context) + "'";
        Cursor cursor = cr.query(HcProvider.CONTENT_URI_DOWNLOAD_FILE, null, where, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            return false;
        } else {
            values.clear();
            values.put(DownloadFile.ACCOUNT, SettingHelper.getAccount(context));
            values.put(DownloadFile.FILEId, fileColumn.getFileid());
            values.put(DownloadFile.NAME, fileColumn.getName());
            values.put(DownloadFile.URL, fileColumn.getUrl());
            values.put(DownloadFile.EXT, fileColumn.getExt());
            values.put(DownloadFile.FILESIZE, fileColumn.getFileSize());
            values.put(DownloadFile.POSITION, fileColumn.getPosition());
            values.put(DownloadFile.STATE, fileColumn.getState());
            values.put(DownloadFile.UPDIRId, fileColumn.getUpdirid());
            cr.insert(HcProvider.CONTENT_URI_DOWNLOAD_FILE, values);
            cursor.close();
            return true;
        }
    }

//    /**
//     * 查询下载文件
//     *
//     * @param downloadID
//     * @param context
//     * @author jrjin
//     * @time 2016-1-28 上午11:53:26
//     */
//    public static synchronized List<FileColumn> queryDownload(List<String> downloadID, Context context) {
//        final ContentValues values = new ContentValues();
//        final ContentResolver cr = context.getContentResolver();
//        List<FileColumn> downloadList = new ArrayList<FileColumn>();
//        for (int i = 0; i < downloadID.size(); i++) {
//            String where = DownloadFile.FILEId + "=" + "'"
//                    + downloadID.get(i) + "'"
//                    + " AND " + SysMessage.ACCOUNT + "=" + "'"
//                    + SettingHelper.getAccount(context) + "'";
//            Cursor cursor = cr.query(HcProvider.CONTENT_URI_DOWNLOAD_FILE, null, where, null, null);
//            if (cursor != null && cursor.getCount() > 0) {
////                try {
////                    List<FileColumn> downloadColumnList = getDownloadInfoList(cursor);
////                    for (int j = 0; j < downloadColumnList.size(); j++) {
////                        downloadList.add(downloadColumnList.get(j));
////                    }
////                } catch (JSONException e) {
////                    e.printStackTrace();
////                }
//
//            }
//            cursor.close();
//        }
//        return downloadList;
//    }

    public static synchronized List<com.android.frame.download.FileColumn> getDownloadInfoList(Cursor cursor) throws JSONException {
        HcLog.D(TAG + " #getDownloadInfoList ");
        int count = cursor.getColumnCount();
        String[] ColumnName = cursor.getColumnNames();
        List<com.android.frame.download.FileColumn> downloadColumns = new ArrayList<com.android.frame.download.FileColumn>();
        while (cursor.moveToNext()) {
            com.android.frame.download.FileColumn download = new com.android.frame.download.FileColumn();
            for (int i = 1; i < count; i++) {
                if (DownloadFile.FILEId.equals(ColumnName[i])) {
                    download.setFileid(cursor.getString(i));
                } else if (DownloadFile.NAME.equals(ColumnName[i])) {
                    download.setName(cursor.getString(i));
                } else if (DownloadFile.EXT.equals(ColumnName[i])) {
                    download.setExt(cursor.getString(i));
                } else if (DownloadFile.UPDIRId.equals(ColumnName[i])) {
                    download.setUpdirid(cursor.getString(i));
                } else if (DownloadFile.STATE.equals(ColumnName[i])) {
                    download.setState(cursor.getString(i));
                } else if (DownloadFile.POSITION.equals(ColumnName[i])) {
                    download.setPosition(Long.parseLong(cursor.getString(i)));
                } else if (DownloadFile.FILESIZE.equals(ColumnName[i])) {
                    download.setFileSize(cursor.getString(i));
                } else if (DownloadFile.URL.equals(ColumnName[i])) {
                    download.setUrl(cursor.getString(i));
                }
            }
            download.setUpOrDown(1);
            download.setLevel(1);
            download.setSource(1);
            downloadColumns.add(download);
        }
        cursor.close();
        return downloadColumns;
    }

    /**
     * 获取下载列表
     *
     * @param context
     * @author jrjin
     * @time 2016-1-28 上午11:53:26
     */
    public static synchronized List<com.android.frame.download.FileColumn> getDownloadList(Context context) {
        HcLog.D(TAG + " #getDownloadList ");
        final ContentResolver cr = context.getContentResolver();
        String where = SysMessage.ACCOUNT + "=" + "'"
                + SettingHelper.getAccount(context) + "'";
        List<com.android.frame.download.FileColumn> downloadList = new ArrayList<com.android.frame.download.FileColumn>();
        Cursor cursor = cr.query(HcProvider.CONTENT_URI_DOWNLOAD_FILE, null, where, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            try {
                downloadList = getDownloadInfoList(cursor);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        return downloadList;
    }

    /**
     * 更新到已读状态
     *
     * @param downloadColumn
     * @param context
     * @return
     * @author jrjin
     * @time 2015-12-3 下午4:54:40
     */
    public static synchronized int updateDownloadInfo(com.android.frame.download.FileColumn downloadColumn, Context context) {
        HcLog.D(TAG + " #updateDownloadInfo id = " + downloadColumn.getFileid());
        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        String where = DownloadFile.FILEId + "=" + "'" + downloadColumn.getFileid() + "'"
                + " AND " + SysMessage.ACCOUNT + "=" + "'"
                + SettingHelper.getAccount(context) + "'";
        HcLog.D(TAG + " updateDownloadInfo start! where = " + where);
        values.put(DownloadFile.POSITION, downloadColumn.getPosition());
        values.put(DownloadFile.STATE, downloadColumn.getState());
        int num = cr.update(HcProvider.CONTENT_URI_DOWNLOAD_FILE, values, where, null);
        return num;
    }

    /**
     * 删除下载完成的信息
     *
     * @param fileColumn
     * @param context
     */
    public static synchronized void deleteDownloadInfo(com.android.frame.download.FileColumn fileColumn, Context context) {
        HcLog.D(TAG + " #deleteDownloadInfo id = " + fileColumn.getFileid());
        final ContentResolver cr = context.getContentResolver();
        String where = DownloadFile.FILEId + "=" + "'" + fileColumn.getFileid() + "'"
                + " AND " + SysMessage.ACCOUNT + "=" + "'"
                + SettingHelper.getAccount(context) + "'";
        cr.delete(HcProvider.CONTENT_URI_DOWNLOAD_FILE, where, null);
    }

    /**
     * 重置上传数据库，将所有上传数据都改为暂停
     *
     * @param context
     * @return
     * @author jrjin
     * @time 2015-12-3 下午4:54:40
     */
    public static synchronized int updateUploadStop(Context context) {
        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        HcLog.D(TAG + " updateUploadStop start! ");
        values.put(HcDatabase.UploadFile.STATE, "1");
        int num = cr.update(HcProvider.CONTENT_URI_UPLOAD_FILE, values, null, null);
        return num;
    }

    /**
     * 重置下载数据库，将所有上传数据都改为暂停
     *
     * @param context
     * @return
     * @author jrjin
     * @time 2015-12-3 下午4:54:40
     */
    public static int updateDownloadStop(Context context) {
        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        HcLog.D(TAG + " updateDownloadStop start! ");
        values.put(HcDatabase.DownloadFile.STATE, "1");
        int num = cr.update(HcProvider.CONTENT_URI_DOWNLOAD_FILE, values, null, null);
        return num;
    }
}
