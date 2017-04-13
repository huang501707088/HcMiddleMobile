/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-10-13 下午3:19:42
*/
package com.android.hcframe.contacts.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;

import com.android.hcframe.HcLog;

public class ContanctsInfoTest {

	private static final String TAG = "ComtanctsInfoTest";
	
	private ContactsInfo mInfo; // 根目录
	
	public ContanctsInfoTest() {
		mInfo = new DepartmentInfo();
		mInfo.setId("000");
		mInfo.setName("浙江鸿程");
		
		ContactsInfo info = new DepartmentInfo(); // 一级部门
		info.setId("001");
		info.setName("研发中心");
		mInfo.addContacts(info);
		
		ContactsInfo info2 = new DepartmentInfo(); // 二级部门
		info2.setId("0001");
		info2.setName("研发中心移动终端");
		info.addContacts(info2);
		
		ContactsInfo info3 = new EmployeeInfo(); // 人员
		info3.setId("00001");
		info3.setName("小三");
		info2.addContacts(info3);
		
		info3 = new EmployeeInfo(); // 人员
		info3.setId("00002");
		info3.setName("小四");
		info2.addContacts(info3);
		
		info2 = new DepartmentInfo(); // 二级部门
		info2.setId("0002");
		info2.setName("研发中心大数据");
		info.addContacts(info2);
		
		info3 = new EmployeeInfo(); // 人员
		info3.setId("00003");
		info3.setName("小五");
		info2.addContacts(info3);
		
		info3 = new EmployeeInfo(); // 人员
		info3.setId("00004");
		info3.setName("小六");
		info2.addContacts(info3);
//		
		/***********************************/
		info = new DepartmentInfo(); // 一级部门
		info.setId("002");
		info.setName("企业发展部");
		mInfo.addContacts(info);
		
		info2 = new DepartmentInfo(); // 二级部门
		info2.setId("0003");
		info2.setName("企业发展部QA组");
		info.addContacts(info2);
		
		info3 = new EmployeeInfo(); // 人员
		info3.setId("00005");
		info3.setName("小八");
		info2.addContacts(info3);
		
		info3 = new EmployeeInfo(); // 人员
		info3.setId("00006");
		info3.setName("小九");
		info2.addContacts(info3);
	}
	
	public void print() {		
		CompositeIterator iterator = new CompositeIterator(mInfo.iterator());
		while (iterator.hasNext()) {
			ContactsInfo info = iterator.next();
			HcLog.D(TAG + " id = "+info.getId() + " name = "+info.getName());
		}
	}
	
	public static final String getContacts(Context context) {
		StringBuilder builder = new StringBuilder();
		InputStream is = null;
		BufferedReader bufferedReader = null;
		try {
			is = context.getAssets().open("contacts.json");
			bufferedReader = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				builder.append(line);
			}

		} catch (Exception e) {
			// TODO: handle exception
			HcLog.D(TAG + " error = " + e);
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (Exception e2) {
					// TODO: handle exception
				}

			}
			if (is != null) {
				try {
					is.close();
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}
		}
		HcLog.D(TAG + " json data = " + builder.toString());
		return builder.toString();
	}
	
	public static void print(ContactsInfo contacts) {	
		HcLog.D(TAG + " root id = "+contacts.getId() + " root name = "+contacts.getName());
		CompositeIterator iterator = new CompositeIterator(contacts.iterator());
		while (iterator.hasNext()) {
			ContactsInfo info = iterator.next();
			HcLog.D(TAG + " id = "+info.getId() + " name = "+info.getName());
		}
	}
	
}
