/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-10-10 下午3:20:40
*/
package com.android.hcframe.contacts.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.android.hcframe.HcLog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.text.TextUtils;

/**
 * 部门
 * @author jrjin
 * @time 2015-10-14 上午9:25:17
 */
public class DepartmentInfo extends ContactsInfo {

	private static final String TAG = "DepartmentInfo";

	/** 部门下的部门及员工列表 */
	private List<ContactsInfo> mInfos = new ArrayList<ContactsInfo>();
	
	private int mCount = 0;
	
	public DepartmentInfo() {
		super();
	}
	
	/**
	 * @author jrjin
	 * @date 2015-10-21 下午1:59:03
	 * @param p
	 */
	@SuppressWarnings("未经检测的,ContactsInfo对象。")
	public DepartmentInfo(Parcel p) {
		super(p);
		final ClassLoader loader = getClass().getClassLoader();
		mCount = p.readInt();
		mInfos.clear();
		mInfos.addAll(Arrays.asList((ContactsInfo[])p.readParcelableArray(loader)));
	}
	
	@Override
	public void addContacts(ContactsInfo info) {
		// TODO Auto-generated method stub
		boolean repeat = false;
		if (info instanceof DepartmentInfo) {
			for (ContactsInfo contactsInfo : mInfos) {
				if (contactsInfo.getId().equals(info.getId())) {
					repeat = true;
					break;
				}
			}
		}
		if (!mInfos.contains(info) && !repeat)
			mInfos.add(info);
	}

	@Override
	public ContactsInfo getContacts(String id) {
		// TODO Auto-generated method stub
		Iterator<ContactsInfo> iterator = mInfos.iterator();
		ContactsInfo info;
		while (iterator.hasNext()) {
			info = iterator.next();
			if (info.getId().equals(id)) {
				return info;
			}
		}
		return null;
	}

	@Override
	public ContactsInfo removeContacts(String id) {
		// TODO Auto-generated method stub
		Iterator<ContactsInfo> iterator = mInfos.iterator();
		ContactsInfo info;
		while (iterator.hasNext()) {
			info = iterator.next();
			if (info.getId().equals(id)) {
				iterator.remove();
				return info;
			}
		}
		return null;
	}

	@Override
	public boolean removeContacts(ContactsInfo info) {
		// TODO Auto-generated method stub
		return mInfos.remove(info);
	}

	@Override
	public Iterator<ContactsInfo> iterator() {
		// TODO Auto-generated method stub
		return mInfos.iterator();
	}

	@Override
	public List<ContactsInfo> getEmployees() {
		// TODO Auto-generated method stub
		List<ContactsInfo> employees = new ArrayList<ContactsInfo>();
		for (ContactsInfo info : mInfos) {
			if (info instanceof EmployeeInfo)
				employees.add(info);
		}
		return employees;
	}

	@Override
	public List<ContactsInfo> getDepartments() {
		// TODO Auto-generated method stub
		List<ContactsInfo> departments = new ArrayList<ContactsInfo>();
		for (ContactsInfo info : mInfos) {
			if (info instanceof DepartmentInfo)
				departments.add(info);
		}
		return departments;
	}

	@Override
	public List<ContactsInfo> getAll() {
		// TODO Auto-generated method stub
		return mInfos;
	}

	@Override
	public int getEmployeeCount() {
		// TODO Auto-generated method stub
		if (mCount == 0) {
			CompositeIterator iterator = new CompositeIterator(iterator());
			while (iterator.hasNext()) {
				if (iterator.next() instanceof EmployeeInfo)
					mCount ++;
			}
		}
		return mCount;
	}

	@Override
	public void changed() {
		// TODO Auto-generated method stub
		mCount = 0;
	}

	@Override
	public void addAllContacts(ContactsInfo info) {
		// TODO Auto-generated method stub
		mInfos.clear();
		mInfos.addAll(info.getAll());
	}
	
	@SuppressWarnings("未经检测的,ContactsInfo对象.")
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		super.writeToParcel(dest, flags);
		dest.writeInt(mCount);
		dest.writeParcelableArray((ContactsInfo[])mInfos.toArray(), flags);
	}

	public static final Creator<DepartmentInfo> CREATOR = new Creator<DepartmentInfo>() {

		@Override
		public DepartmentInfo createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new DepartmentInfo(source);
		}

		@Override
		public DepartmentInfo[] newArray(int size) {
			// TODO Auto-generated method stub
			return new DepartmentInfo[size];
		}
	};

	@Override
	public void sendMessage(Activity context, String content) {
		// TODO Auto-generated method stub
		/** 短信群发方式 */
		// tel1;tel2;tel3		
		String tels = "";
		StringBuilder builder = new StringBuilder();
		
		CompositeIterator iterator = new CompositeIterator(iterator());
		ContactsInfo info = null;
		while (iterator.hasNext()) {
			info = iterator.next();
			if (info instanceof EmployeeInfo && !TextUtils.isEmpty(info.getMobilePhone())) {
				builder.append(info.getMobilePhone() + ";");
			}
				
		}
		tels = builder.toString();
		if (TextUtils.isEmpty(tels) || tels.length() <= 2) return;
		HcLog.D(TAG + " sendMessage before sub tels = "+tels);
		tels = tels.substring(0, tels.length() - 1);
		Uri smsToUri = Uri.parse("smsto:" + tels);
		HcLog.D(TAG + " sendMessage after sub tels = "+tels);
		Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
		intent.putExtra("sms_body", content);
		context.startActivity(intent);
	}
	
	
}
