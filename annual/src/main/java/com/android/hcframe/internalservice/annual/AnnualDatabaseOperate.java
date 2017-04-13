/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2016-1-12 上午11:37:46
*/
package com.android.hcframe.internalservice.annual;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcLog;
import com.android.hcframe.sql.HcDatabase.AnnualProgram;
import com.android.hcframe.sql.HcProvider;
import com.android.hcframe.sql.SettingHelper;

import java.util.ArrayList;
import java.util.List;

public final class AnnualDatabaseOperate {

	private static final String TAG = "AnnualDatabaseOperate";
	
	private AnnualDatabaseOperate() {}
	
	/**
	 * 获取年会节目列表
	 * @author jrjin
	 * @time 2016-1-12 下午1:54:47
	 * @param context
	 * @param annualId
	 * @return
	 */
	public static List<AnnualProgramInfo> getAnnualProgramInfos(Context context, String annualId) {
		List<AnnualProgramInfo> infos = new ArrayList<AnnualProgramInfo>();
		final ContentResolver cr = context.getContentResolver();
		String[] projection = {AnnualProgram.CONTENT, AnnualProgram.PROGRAM_ID, AnnualProgram.SCORE,
				AnnualProgram.TITLE, AnnualProgram.TYPE};
		String selection = AnnualProgram.ACCOUNT + "=" + "'"
						+ SettingHelper.getAccount(context) + "'" + " AND " + 
						AnnualProgram.ANNUAL_ID + "=" + "'" + annualId +"'";
		Cursor c = cr.query(HcProvider.CONTENT_URI_ANNUAL_PROGRAM, projection, selection, null, null);
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
			AnnualProgramInfo info = null;
			while (!c.isAfterLast()) {
				info = new AnnualProgramInfo();
				info.setProgramContent(c.getString(0));
				info.setProgramId(c.getString(1));
				info.setProgramScore(c.getInt(2));
				info.setProgramTitle(c.getString(3));
				info.setProgramType(c.getInt(4));
				info.setShowContent(false);
				info.setAnnualId(annualId);
				infos.add(info);
				c.moveToNext();
			}
		}
		if (c != null)
			c.close();
		return infos;
	}
	
	/**
	 * 添加年会节目
	 * @author jrjin
	 * @time 2016-1-12 下午2:33:06
	 * @param context
	 * @param infos 年会节目列表
	 * @param annualId
	 */
	public static void insertAnnualProagrams(Context context, List<AnnualProgramInfo> infos, String annualId) {
		if (infos == null || infos.size() == 0) return;
		final ContentValues values = new ContentValues();
		final ContentResolver cr = context.getContentResolver();
		String where = AnnualProgram.ACCOUNT + "=" + "'"
				+ SettingHelper.getAccount(context) + "'" + " AND " + 
				AnnualProgram.ANNUAL_ID + "=" + "'" + annualId +"'";
		cr.delete(HcProvider.CONTENT_URI_ANNUAL_PROGRAM, where, null);
		for (AnnualProgramInfo info : infos) {
			insertAnnualProagrams(info, cr, values);
		}
	}
	
	private static Uri insertAnnualProagrams(AnnualProgramInfo info, ContentResolver cr,
			ContentValues values) {
		values.clear();
		values.put(AnnualProgram.ANNUAL_ID, info.getAnnualId());
		values.put(AnnualProgram.CONTENT, info.getProgramContent());
		values.put(AnnualProgram.PROGRAM_ID, info.getProgramId());
		values.put(AnnualProgram.SCORE, info.getProgramScore());
		values.put(AnnualProgram.TITLE, info.getProgramTitle());
		values.put(AnnualProgram.TYPE, info.getProgramType());
		values.put(AnnualProgram.ACCOUNT,
				SettingHelper.getAccount(HcApplication.getContext()));
		return cr.insert(HcProvider.CONTENT_URI_ANNUAL_PROGRAM, values);
	}
	
	/**
	 * 评分的时候保存评分
	 * @author jrjin
	 * @time 2016-1-12 下午2:31:01
	 * @param info
	 * @param context
	 */
	public static int updateAnnualProgram(AnnualProgramInfo info, Context context) {
		final ContentValues values = new ContentValues();
		final ContentResolver cr = context.getContentResolver();
		String where = AnnualProgram.ACCOUNT + "=" + "'"
				+ SettingHelper.getAccount(context) + "'" + " AND " + 
				AnnualProgram.PROGRAM_ID + "=" + "'" + info.getProgramId() +"'";
		values.put(AnnualProgram.SCORE, info.getProgramScore());
		int num = cr.update(HcProvider.CONTENT_URI_ANNUAL_PROGRAM, values, where, null);
		HcLog.D(TAG + " updateAnnualProgram update num = "+num);
		return num;
	}
	
	/**
	 * 删除对应annualID的数据
	 * @author jrjin
	 * @time 2016-1-18 上午10:48:11
	 * @param context
	 * @param annualId
	 */
	public static void deleteAnnualProagrams(Context context, String annualId) {
		final ContentResolver cr = context.getContentResolver();
		String where = /*AnnualProgram.ACCOUNT + "=" + "'"
				+ SettingHelper.getAccount(context) + "'" + " AND " + */
				AnnualProgram.ANNUAL_ID + "=" + "'" + annualId +"'";
		cr.delete(HcProvider.CONTENT_URI_ANNUAL_PROGRAM, where, null);
	}
}
