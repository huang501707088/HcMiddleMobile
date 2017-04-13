/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-10-10 下午2:25:44
*/
package com.android.hcframe.contacts.data;

import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 部门和员工的基类
 * @author jrjin
 * @time 2015-10-14 上午9:23:49
 */
public abstract class ContactsInfo implements Iterable<ContactsInfo>, Parcelable {

	private static final String TAG = "ContactsInfo";
	
	/** 员工工号/部门编号 */
	private String mId;
	/** 员工姓名/部门名称 */
	private String mName;
	/** 部门编号/上级部门编号 */
	private String mParentId;
	/** 部门名称/上级部门名称 */
	private String mParentName;

	public ContactsInfo() {}
	
	public ContactsInfo(Parcel p) {
		mId = p.readString();
		mName = p.readString();
		mParentId = p.readString();
		mParentName = p.readString();
	}
	
	/**
	 * 设置员工工号/部门编号
	 * @author jrjin
	 * @time 2015-10-14 上午8:42:48
	 * @param id 员工工号/部门编号
	 */
	public final void setId(String id) {
		mId = id;
	}
	
	/**
	 * 获取员工工号/部门编号
	 * @author jrjin
	 * @time 2015-10-14 上午8:43:08
	 * @return 员工工号/部门编号
	 */
	public final String getId() {
		return mId;
	}
	
	/**
	 * 设置员工姓名/部门名称
	 * @author jrjin
	 * @time 2015-10-14 上午8:43:42
	 * @param name 员工姓名/部门名称
	 */
	public /*final*/ void setName(String name) {
		mName = name;
	}
	
	/**
	 * 获取员工姓名/部门名称
	 * @author jrjin
	 * @time 2015-10-14 上午8:44:00
	 * @return 员工姓名/部门名称
	 */
	public final String getName() {
		return mName;
	}
	
	/**
	 * 设置部门编号/上级部门编号
	 * @author jrjin
	 * @time 2015-10-14 上午8:44:10
	 * @param id
	 */
	public final void setParentId(String id) {
		mParentId = id;
	}
	
	/**
	 * 获取部门编号/上级部门编号
	 * @author jrjin
	 * @time 2015-10-14 上午8:44:36
	 * @return 部门编号/上级部门编号
	 */
	public final String getParentId() {
		return mParentId;
	}
	
	/**
	 * 设置部门名称/上级部门名称
	 * @author jrjin
	 * @time 2015-10-14 上午8:44:45
	 * @param name 部门名称/上级部门名称
	 */
	public final void setParentName(String name) {
		mParentName = name;
	}
	
	/**
	 * 获取部门名称/上级部门名称
	 * @author jrjin
	 * @time 2015-10-14 上午8:45:09
	 * @return 部门名称/上级部门名称
	 */
	public final String getParentName() {
		return mParentName;
	}
	
	/**
	 * 设置员工Email
	 * @author jrjin
	 * @time 2015-10-14 上午8:45:26
	 * @param email 员工电子邮箱
	 */
	public void setEmail(String email) {
		throw new UnsupportedOperationException(TAG + " #setEmail email = "+email);
	}
	
	/**
	 * 获取员工Email
	 * @author jrjin
	 * @time 2015-10-14 上午8:45:40
	 * @return 员工Email
	 */
	public String getEmail() {
		throw new UnsupportedOperationException(TAG + " #getEmail!");
	}
	
	/**
	 * 设置员工手机号码
	 * @author jrjin
	 * @time 2015-10-14 上午8:46:12
	 * @param phone 手机号码
	 */
	public void setMobilePhone(String phone) {
		throw new UnsupportedOperationException(TAG + " #setMobilePhone phone = "+phone);
	}
	
	/**
	 * 获取员工手机号码
	 * @author jrjin
	 * @time 2015-10-14 上午8:46:48
	 * @return 员工手机号码
	 */
	public String getMobilePhone() {
		throw new UnsupportedOperationException(TAG + " #getMobilePhone!");
	}
	
	/**
	 * 设置员工备用手机号码
	 * @author jrjin
	 * @time 2015-10-14 上午8:47:11
	 * @param phone 员工备用手机号码
	 */
	public void setStandbyPhone(String phone) {
		throw new UnsupportedOperationException(TAG + " #setStandbyPhone phone = "+phone);
	}
	
	/**
	 * 获取员工备用手机号码
	 * @author jrjin
	 * @time 2015-10-14 上午8:47:37
	 * @return 员工备用手机号码
	 */
	public String getStandbyPhone() {
		throw new UnsupportedOperationException(TAG + " #getStandbyPhone!");
	}
	
	/**
	 * 设置员工固定电话号码
	 * @author jrjin
	 * @time 2015-10-14 上午8:48:00
	 * @param phone 员工固定电话号码
	 */
	public void setFixedPhone(String phone) {
		throw new UnsupportedOperationException(TAG + " #setFixedPhone phone = "+phone);
	}
	
	/**
	 * 获取员工固定电话号码
	 * @author jrjin
	 * @time 2015-10-14 上午8:48:19
	 * @return 员工固定电话号码
	 */
	public String getFixedPhone() {
		throw new UnsupportedOperationException(TAG + " #getFixedPhone!");
	}
	
	/**
	 * 设置员工备用邮箱
	 * @author jrjin
	 * @time 2015-10-14 上午8:48:28
	 * @param email 员工备用邮箱
	 */
	public void setStandEmail(String email) {
		throw new UnsupportedOperationException(TAG + " #setStandEmail email = "+email);
	}
	
	/**
	 * 获取员工备用邮箱
	 * @author jrjin
	 * @time 2015-10-14 上午8:49:15
	 * @return 员工备用邮箱
	 */
	public String getStandEmail() {
		throw new UnsupportedOperationException(TAG + " #getStandEmail!");
	}
	
	/**
	 * 设置员工分机号
	 * @author jrjin
	 * @time 2015-10-14 上午8:49:26
	 * @param number 员工分机号
	 */
	public void setExtensionNumber(String number) {
		throw new UnsupportedOperationException(TAG + " #setExtensionNumber number = "+number);
	}
	
	/**
	 * 获取员工分机号
	 * @author jrjin
	 * @time 2015-10-14 上午8:49:53
	 * @return 员工分机号
	 */
	public String getExtensionNumber() {
		throw new UnsupportedOperationException(TAG + " #getExtensionNumber!");
	}
	
	/**
	 * 设置员工虚拟网号
	 * @author jrjin
	 * @time 2015-10-14 上午8:50:01
	 * @param number 员工虚拟网号
	 */
	public void setVirtualNetNumber(String number) {
		throw new UnsupportedOperationException(TAG + " #setVirtualNetNumber number = "+number);
	}
	
	/**
	 * 获取员工虚拟网号
	 * @author jrjin
	 * @time 2015-10-14 上午8:50:19
	 * @return 员工虚拟网号
	 */
	public String getVirtualNetNumber() {
		throw new UnsupportedOperationException(TAG + " #getVirtualNetNumber!");
	}
	
	/**
	 * 向部门中添加下级部门或者部门的员工
	 * @author jrjin
	 * @time 2015-10-14 上午8:50:36
	 * @param info 下级部门/部门员工
	 */
	public void addContacts(ContactsInfo info) {
		throw new UnsupportedOperationException(TAG + " #addContacts ContactsInfo = "+info);
	} 
	
	/**
	 * 根据员工工号获取员工信息或者根据部门编号获取部门
	 * @author jrjin
	 * @time 2015-10-12 下午4:24:37
	 * @param id 员工工号/部门编号
	 * @return 部门/员工
	 */
	public ContactsInfo getContacts(String id) {
		throw new UnsupportedOperationException(TAG + " #getContacts id = "+id);
	}
	
	/**
	 * 根据员工工号删除员工/部门
	 * @author jrjin
	 * @time 2015-10-12 下午4:25:13
	 * @param id 员工工号/部门
	 * @return 删除的员工/部门
	 */
	public ContactsInfo removeContacts(String id) {
		throw new UnsupportedOperationException(TAG + " #removeContacts id = "+id);
	}
	
	/**
	 * 删除指定的员工/部门
	 * @author jrjin
	 * @time 2015-10-12 下午4:27:04
	 * @param info 需要删除的员工/部门
	 * @return 删除成功返回true，否则false
	 */
	public boolean removeContacts(ContactsInfo info) {
		throw new UnsupportedOperationException(TAG + " #removeContacts id = "+info.mId);
	}
	
	/**
	 * 获取本部门的所以员工
	 * @author jrjin
	 * @time 2015-10-14 上午8:56:08
	 * @return 本部门的所以员工
	 */
	public List<ContactsInfo> getEmployees() {
		throw new UnsupportedOperationException(TAG + " #getEmployees! ");
	}
	
	/**
	 * 获取本部门下的所以下级部门
	 * @author jrjin
	 * @time 2015-10-14 上午8:57:28
	 * @return 本部门下的所以下级部门
	 */
	public List<ContactsInfo> getDepartments() {
		throw new UnsupportedOperationException(TAG + " #getDepartments! ");
	}
	
	/**
	 * 获取本部门下的所以下级部门和员工
	 * @author jrjin
	 * @time 2015-10-14 上午8:58:37
	 * @return 本部门下的所以下级部门和员工
	 */
	public List<ContactsInfo> getAll() {
		throw new UnsupportedOperationException(TAG + " #getAll! ");
	}
	
	/**
	 * 获取部门下的所以员工数目(包括下级部门的员工)
	 * @author jrjin
	 * @time 2015-10-14 上午8:59:57
	 * @return 部门下的员工数
	 */
	public int getEmployeeCount() {
		throw new UnsupportedOperationException(TAG + " #getEmployeeCount! ");
	}
	
	/**
	 * 重置一些变量
	 * @author jrjin
	 * @time 2015-10-14 上午9:05:35
	 */
	public void changed() {
		
	}
	
	/**
	 * 发短信
	 * <p></p>
	 * 注意：到时参数自己补充
	 * @author jrjin
	 * @time 2015-10-14 上午9:36:21
	 * @param context 启动的activity
	 * @param content 需要发送的短信内容
	 */
	public void sendMessage(Activity context, String content) {
		throw new UnsupportedOperationException(TAG + " #sendMessage! ");
	}
	
	/**
	 * 列表点击事件
	 * <p></p>
	 * 注意：到时参数自己补充
	 * @author jrjin
	 * @time 2015-10-14 上午9:37:48
	 */
	public void onItemClick() {
		throw new UnsupportedOperationException(TAG + " #onItemClick! ");
	}
	
	/**
	 * 获取列表显示的资源布局的ID
	 * @author jrjin
	 * @time 2015-10-14 上午9:38:25
	 * @return 列表显示的资源布局的ID
	 */
	public int getLayoutId() {
		return 0;
	}
	
	/**
	 * 打电话
	 * <p></p>
	 * 注意：到时参数自己补充
	 * @author jrjin
	 * @time 2015-10-14 上午9:39:39
	 */
	public void onCall() {
		throw new UnsupportedOperationException(TAG + " #onCall! ");
	}
	/**
	 * 复制部门的员工和下级部门,部门原先的数据被清空.
	 * @author jrjin
	 * @time 2015-10-20 上午9:20:18
	 * @param info 部门
	 */
	public void addAllContacts(ContactsInfo info) {
		throw new UnsupportedOperationException(TAG + " #addAllContacts ContactsInfo = "+info);
	}

	@Override
	public Iterator<ContactsInfo> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(mId);
		dest.writeString(mName);
		dest.writeString(mParentId);
		dest.writeString(mParentName);
	}
	
	public static final Creator<ContactsInfo> CREATOR = new Creator<ContactsInfo>() {

		@Override
		public ContactsInfo createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new ContactsInfo(source) {
			};
		}

		@Override
		public ContactsInfo[] newArray(int size) {
			// TODO Auto-generated method stub
			return new ContactsInfo[size];
		}
	};
	
	/**
	 * 保存名片
	 * @author jrjin
	 * @time 2015-10-23 下午3:21:28
	 * @param context
	 */
	public void saveContacts(Activity context) {
		throw new UnsupportedOperationException(TAG + " #saveContacts! ");
	}
	
	public String getQuanpin() {
		throw new UnsupportedOperationException(TAG + " #getQuanpin! ");
	}
	
	public String getJianpin() {
		throw new UnsupportedOperationException(TAG + " #getJianpin! ");
	}
	
	public String getNameIcon() {
		throw new UnsupportedOperationException(TAG + " #getNameIcon! ");
	}
	
	/**
	 * 获取名字的第一个字的字母
	 * @author jrjin
	 * @time 2015-12-10 上午10:10:27
	 * @return
	 */
	public String getAlphabet() {
		throw new UnsupportedOperationException(TAG + " #getAlphabet! ");
	}

	public void setPhoneVisibility(boolean visibility) {
		throw new UnsupportedOperationException(TAG + " #setPhoneVisibility! visibility ="+visibility);
	}

	public boolean getPhoneVisibility() {
		throw new UnsupportedOperationException(TAG + " #getPhoneVisibility!");
	}

	public void setUserId(String userId) {
		throw new UnsupportedOperationException(TAG + " #setUserId! userId = "+userId);
	}

	public String getUserId() {
		throw new UnsupportedOperationException(TAG + " #getUserId!");
	}
}
