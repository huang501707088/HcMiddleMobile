package com.android.hcframe.internalservice.contacts;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.android.hcframe.HcLog;
import com.android.hcframe.contacts.data.CompositeIterator;
import com.android.hcframe.contacts.data.ContactsInfo;
import com.android.hcframe.contacts.data.DepartmentInfo;
import com.android.hcframe.contacts.data.EmployeeInfo;
import com.android.hcframe.sql.HcDatabase;
import com.android.hcframe.sql.HcProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-3-3 12:02.
 */
public final class ContactsOperateDatabase {

    private static final String TAG = "ContactsOperateDatabase";

    /**
     * 添加一位员工
     * @author jrjin
     * @time 2015-10-20 上午10:45:35
     * @param info 员工信息
     * @param context
     */
    public static Uri insertEmployee(ContactsInfo info, Context context) {
        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        cr.delete(HcProvider.CONTENT_URI_CONTACTS, HcDatabase.Contacts.ID + "=?", new String[] { info.getId() });
        return insertEmployee(info, values, cr);
    }

    private static Uri insertEmployee(ContactsInfo info, ContentValues values, ContentResolver cr) {
        values.clear();
        values.put(HcDatabase.Contacts.ID, info.getId());
        values.put(HcDatabase.Contacts.NAME, info.getName());
        values.put(HcDatabase.Contacts.PARENT_ID, info.getParentId());
        values.put(HcDatabase.Contacts.PARENT_NAME, info.getParentName());
        if (info instanceof EmployeeInfo) {
            values.put(HcDatabase.Contacts.EMAIL, info.getEmail());
            values.put(HcDatabase.Contacts.EXTENSION_NUMBER, info.getExtensionNumber());
            values.put(HcDatabase.Contacts.FIXED_PHONE, info.getFixedPhone());
            values.put(HcDatabase.Contacts.MOBILE_PHONE, info.getMobilePhone());
            values.put(HcDatabase.Contacts.STANDBY_EMAIL, info.getStandEmail());
            values.put(HcDatabase.Contacts.STANDBY_PHONE, info.getStandbyPhone());
            values.put(HcDatabase.Contacts.TYPE, 0);
            values.put(HcDatabase.Contacts.VIRTUAL_NET_NUMBER, info.getVirtualNetNumber());
            values.put(HcDatabase.Contacts.NAME_A, info.getAlphabet());
            values.put(HcDatabase.Contacts.NAME_ICON, info.getNameIcon());
            values.put(HcDatabase.Contacts.NAME_JAINPIN, info.getJianpin());
            values.put(HcDatabase.Contacts.NAME_QUANPIN, info.getQuanpin());
            values.put(HcDatabase.Contacts.VISIBILITY, info.getPhoneVisibility() == true ? 0 : 1);
            values.put(HcDatabase.Contacts.USER_ID, info.getUserId());
        } else {
            values.put(HcDatabase.Contacts.TYPE, 1);
        }

        return cr.insert(HcProvider.CONTENT_URI_CONTACTS, values);
    }

    /**
     * 添加整个通讯录
     * @deprecated
     * @author jrjin
     * @time 2015-10-20 上午10:59:50
     * @param info 通讯录的根目录
     * @param context
     */
    public synchronized static void insertContacts(ContactsInfo info, Context context) {
        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        cr.delete(HcProvider.CONTENT_URI_CONTACTS, null, null);
        insertEmployee(info, values, cr); // 插入根目录
        CompositeIterator iterator = new CompositeIterator(info.iterator());
        while (iterator.hasNext()) {
            insertEmployee(iterator.next(), values, cr);
        }
    }

    /**
     * 返回整个通讯录
     * @author jrjin
     * @time 2015-10-20 上午11:46:31
     * @param context
     * @return
     */
    public synchronized static List<ContactsInfo> getContacts(Context context) {
        List<ContactsInfo> infos = new ArrayList<ContactsInfo>();
        final ContentResolver cr = context.getContentResolver();
        String[] projection = {HcDatabase.Contacts.ID, HcDatabase.Contacts.NAME, HcDatabase.Contacts.PARENT_ID, HcDatabase.Contacts.PARENT_NAME,
                HcDatabase.Contacts.EMAIL, HcDatabase.Contacts.EXTENSION_NUMBER, HcDatabase.Contacts.FIXED_PHONE, HcDatabase.Contacts.MOBILE_PHONE,
                HcDatabase.Contacts.STANDBY_EMAIL, HcDatabase.Contacts.STANDBY_PHONE, HcDatabase.Contacts.VIRTUAL_NET_NUMBER,
                HcDatabase.Contacts.TYPE, HcDatabase.Contacts.VISIBILITY, HcDatabase.Contacts.USER_ID};
        Cursor c = cr.query(HcProvider.CONTENT_URI_CONTACTS, projection, null, null, null);
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            int type = 0;
            ContactsInfo info = null;
            while (!c.isAfterLast()) {
                type = c.getInt(11);
                if (type == 0) {
                    info = new EmployeeInfo();
                    info.setEmail(c.getString(4));
                    info.setExtensionNumber(c.getString(5));
                    info.setFixedPhone(c.getString(6));
                    info.setMobilePhone(c.getString(7));
                    info.setStandEmail(c.getString(8));
                    info.setStandbyPhone(c.getString(9));
                    info.setVirtualNetNumber(c.getString(10));
                    info.setPhoneVisibility(c.getInt(12) == 0);
                    info.setUserId(c.getString(13));
                } else {
                    info = new DepartmentInfo();
                }
                info.setId(c.getString(0));
                info.setName(c.getString(1));
                info.setParentId(c.getString(2));
                info.setParentName(c.getString(3));
                infos.add(info);
                c.moveToNext();
            }
        }
        if (c != null) c.close();
        HcLog.D(TAG + " getContacts contacts size = " + infos.size());
        return infos;
    }

    /**
     * 添加整个通讯录
     * @author jrjin
     * @time 2015-10-22 下午3:29:39
     * @param contacts 部门和部门列表
     * @param context
     */
    public synchronized static int insertContacts(List<ContactsInfo> contacts, Context context) {
        HcLog.D(TAG + " #insertContacts contacts size = "+contacts.size());
        if (contacts.isEmpty()) return 0;
//		final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        cr.delete(HcProvider.CONTENT_URI_CONTACTS, null, null);
        /**
         * @date 2016-1-28 下午12:18:53
         * 替换为下面的事物处理机制
        for (ContactsInfo contactsInfo : contacts) {
        insertEmployee(contactsInfo, values, cr);
        }
         */
        int size = contacts.size();
        ContentValues[] values = new ContentValues[size];
        for (int i = 0; i < size; i++) {
            values[i] = new ContentValues();
            setEmployeeValue(contacts.get(i), values[i]);
        }
        int number = cr.bulkInsert(HcProvider.CONTENT_URI_CONTACTS, values);
        contacts.clear();
        HcLog.D(TAG + " #insertContacts contacts insert number = "+number);
        return number;
    }

    private static void setEmployeeValue(ContactsInfo info, ContentValues values) {
        values.clear();
        values.put(HcDatabase.Contacts.ID, info.getId());
        values.put(HcDatabase.Contacts.NAME, info.getName());
        values.put(HcDatabase.Contacts.PARENT_ID, info.getParentId());
        values.put(HcDatabase.Contacts.PARENT_NAME, info.getParentName());
        if (info instanceof EmployeeInfo) {
            values.put(HcDatabase.Contacts.EMAIL, info.getEmail());
            values.put(HcDatabase.Contacts.EXTENSION_NUMBER, info.getExtensionNumber());
            values.put(HcDatabase.Contacts.FIXED_PHONE, info.getFixedPhone());
            values.put(HcDatabase.Contacts.MOBILE_PHONE, info.getMobilePhone());
            values.put(HcDatabase.Contacts.STANDBY_EMAIL, info.getStandEmail());
            values.put(HcDatabase.Contacts.STANDBY_PHONE, info.getStandbyPhone());
            values.put(HcDatabase.Contacts.TYPE, 0);
            values.put(HcDatabase.Contacts.VIRTUAL_NET_NUMBER, info.getVirtualNetNumber());
            values.put(HcDatabase.Contacts.NAME_A, info.getAlphabet());
            values.put(HcDatabase.Contacts.NAME_ICON, info.getNameIcon());
            values.put(HcDatabase.Contacts.NAME_JAINPIN, info.getJianpin());
            values.put(HcDatabase.Contacts.NAME_QUANPIN, info.getQuanpin());
            values.put(HcDatabase.Contacts.VISIBILITY, info.getPhoneVisibility() == true ? 0 : 1);
            values.put(HcDatabase.Contacts.USER_ID, info.getUserId());
        } else {
            values.put(HcDatabase.Contacts.TYPE, 1);
        }
    }

    public static ContactsInfo getContact(Context context, String userId) {
        final ContentResolver cr = context.getContentResolver();
        String where = HcDatabase.Contacts.USER_ID + "=" + "'" + userId + "'";
        String[] projection = {HcDatabase.Contacts.ID, HcDatabase.Contacts.NAME, HcDatabase.Contacts.PARENT_ID, HcDatabase.Contacts.PARENT_NAME,
                HcDatabase.Contacts.EMAIL, HcDatabase.Contacts.EXTENSION_NUMBER, HcDatabase.Contacts.FIXED_PHONE, HcDatabase.Contacts.MOBILE_PHONE,
                HcDatabase.Contacts.STANDBY_EMAIL, HcDatabase.Contacts.STANDBY_PHONE, HcDatabase.Contacts.VIRTUAL_NET_NUMBER,
                HcDatabase.Contacts.TYPE, HcDatabase.Contacts.VISIBILITY, HcDatabase.Contacts.USER_ID};
        Cursor c = cr.query(HcProvider.CONTENT_URI_CONTACTS, projection, where, null, null);
        ContactsInfo info = null;
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            int type = 0;
            type = c.getInt(11);
            if (type == 0) {
                info = new EmployeeInfo();
                info.setEmail(c.getString(4));
                info.setExtensionNumber(c.getString(5));
                info.setFixedPhone(c.getString(6));
                info.setMobilePhone(c.getString(7));
                info.setStandEmail(c.getString(8));
                info.setStandbyPhone(c.getString(9));
                info.setVirtualNetNumber(c.getString(10));
                info.setPhoneVisibility(c.getInt(12) == 0);
                info.setUserId(c.getString(13));
            } else {
                info = new DepartmentInfo();
            }
            info.setId(c.getString(0));
            info.setName(c.getString(1));
            info.setParentId(c.getString(2));
            info.setParentName(c.getString(3));
            c.moveToNext();
        }
        if (c != null)
            c.close();
        return info;
    }

    public static int getContactsCount(Context context) {
        int count = 0;
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(HcProvider.CONTENT_URI_CONTACTS, new String[] {HcDatabase.Contacts.ID}, null, null, null);
        if (c != null) {
            count = c.getCount();
            c.close();
        }
        return count;
    }
}
