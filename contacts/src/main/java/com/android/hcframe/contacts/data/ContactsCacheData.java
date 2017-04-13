/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-10-19 下午3:51:14
 */
package com.android.hcframe.contacts.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcObservable;
import com.android.hcframe.HcUtil;
import com.android.hcframe.ModuleInfo;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.http.ResponseCodeInfo;
import com.android.hcframe.internalservice.contacts.ContactsOperateDatabase;
import com.android.hcframe.sql.SettingHelper;

import org.json.JSONArray;
import org.json.JSONObject;

public class ContactsCacheData extends HcObservable implements IHttpResponse {

	private static final String TAG = "ContactsCacheData";

	private static final ContactsCacheData CONTACTS = new ContactsCacheData();

	private ContactsCacheData() {
	}

	/** 通讯录列表,已经包括全部员工 */
	private List<ContactsInfo> mContacts = new ArrayList<ContactsInfo>();

	/** 部门的根部门，没有上级部门 */
	private ContactsInfo mRootInfo = new DepartmentInfo();

	private static final String MODULE_ID = "phonebook_update_all";

	private Handler mHandler = new Handler();

	public static final ContactsCacheData getInstance() {
		return CONTACTS;
	}

	@Override
	public void notify(Object data, final RequestCategory request,
			final ResponseCategory category) {
		// TODO Auto-generated method stub
		switch (request) {
			case CONTACTS_REQUEST:
			switch (category) {
			case SUCCESS:
				if (data != null && data instanceof List<?>) {
//					HcLog.D(TAG + " notify thread = "+Thread.currentThread());
					// do something
					List<ContactsInfo> infos = (List<ContactsInfo>) data;
					if (!infos.isEmpty()) { // 为空的话不做处理
						mContacts.clear();
						createTree(new ArrayList<ContactsInfo>(infos)); // 这里新建是为了避免在另外一个线程里插入数据库出错，数据插入完再清空
					}

					HcLog.D(TAG + " notify end sort time = "+HcUtil.getDate(HcUtil.FORMAT_HOUR, System.currentTimeMillis()));
				} else if (data != null && data instanceof String) {
					// 这里还在线程里面
					/**
					 * @author jrjin
					 * @data 2016-03-03 14:34
					 */
					try {
						JSONObject object = new JSONObject((String) data);
						final int status = object.getInt("code");
						if (status == 0) {
							/** 全部部门和员工 */
							final List<ContactsInfo> contactsInfos = new ArrayList<ContactsInfo>();

							if (HcUtil.hasValue(object, "body")) {
								object = object.getJSONObject("body");
								if (HcUtil.hasValue(object, "updateTime")) {
									SettingHelper.setModuleTime(
											HcApplication.getContext(), MODULE_ID,
											object.getString("updateTime"), true);
								}
								if (HcUtil.hasValue(object, "list")) {
									JSONArray array = object.getJSONArray("list");
									ContactsInfo info = null;
									int size = array.length();
									HcLog.D(TAG + " parseJson array size = " + size);
									int type = 0;
									for (int i = 0; i < size; i++) {
//								HcLog.D(TAG + " parseJson array start position i = " + i);
										object = array.getJSONObject(i);
										if (HcUtil.hasValue(object, "type")) {
											type = object.getInt("type");
										}

										if (type == 0) { // 人员
											info = new EmployeeInfo();

											if (HcUtil.hasValue(object, "mobilePhone")) {
												info.setMobilePhone(object
														.getString("mobilePhone"));
											}
											if (HcUtil.hasValue(object, "standbyPhone")) {
												info.setStandbyPhone(object
														.getString("standbyPhone"));
											}
											if (HcUtil.hasValue(object, "fixedPhone")) {
												info.setFixedPhone(object
														.getString("fixedPhone"));
											}
											if (HcUtil.hasValue(object, "extensionNumber")) {
												info.setExtensionNumber(object
														.getString("extensionNumber"));
											}
											if (HcUtil.hasValue(object, "virtualNetNumber")) {
												info.setVirtualNetNumber(object
														.getString("virtualNetNumber"));
											}
											if (HcUtil.hasValue(object, "email")) {
												info.setEmail(object.getString("email"));
											}
											if (HcUtil.hasValue(object, "standbyEmail")) {
												info.setStandEmail(object
														.getString("standbyEmail"));
											}

											if (HcUtil.hasValue(object, "hidePhone")) {
												HcLog.D(TAG + " #notify visibility = "+"0".equals(object.getString("hidePhone")));
												info.setPhoneVisibility("0".equals(object.getString("hidePhone")));
											}

											if (HcUtil.hasValue(object, "userId")) {
												info.setUserId(object.getString("userId"));
											}
										} else {
											info = new DepartmentInfo();
										}

										if (HcUtil.hasValue(object, "name")) {
											info.setName(object.getString("name"));
										}
										if (HcUtil.hasValue(object, "id")) {
											info.setId(object.getString("id"));
										}
										if (HcUtil.hasValue(object, "parentName")) {
											info.setParentName(object
													.getString("parentName"));
										}
										if (HcUtil.hasValue(object, "parentId")) {
											info.setParentId(object
													.getString("parentId"));
										}

										// for end!
										contactsInfos.add(info);
//								HcLog.D(TAG + " parseJson array end position i = " + i);
									}
								}
							} else {
								// 没有数据
								;
							}

							// 这里新建是为了避免在另外一个线程里插入数据库出错，数据插入完再清空
							final List<ContactsInfo> temps = new ArrayList<ContactsInfo>(contactsInfos);
							mHandler.post(new Runnable() {
								@Override
								public void run() {

									notifyPage(temps, ResponseCategory.SUCCESS);
								}
							});

							// 添加到数据库,需要测试
							ContactsOperateDatabase.insertContacts(contactsInfos,
									HcApplication.getContext());

						}  else {
							// other code
							final String msg = object.getString("msg");
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									notifyPage(new ResponseCodeInfo(status, msg,""), ResponseCategory.REQUEST_FAILED);
								}
							});

						}
					} catch (Exception e) {
						// TODO: handle exception
						//e.printStackTrace();
						HcLog.D(TAG + " parseJson error = " + e);
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								notifyPage(RequestCategory.CONTACTS_REQUEST, ResponseCategory.DATA_ERROR);
							}
						});

					}

					return;
				}
				HcLog.D(TAG + " notify contacts size = " + mContacts.size());
				/** test */
				// ContanctsInfoTest.print(mRootInfo);
				// mContacts.clear();
				// mRootInfo = new DepartmentInfo();
				// getEmployees();

				break;

			default:
				break;
			}

			notifyObservers(this, data, request, category);
			break;
		case CHECK_MODULE_TIME:
			if (category == ResponseCategory.SUCCESS) {
				if (data != null && data instanceof ModuleInfo) {
					ModuleInfo info = (ModuleInfo) data;
					if (info.getUpdateFlag() == ModuleInfo.FLAG_UPDATE) {
						HcHttpRequest.getRequest().sendContactsRequest(MODULE_ID, this);
					}
					
				}
			}
			break;
			

		default:

			break;
		}
	}

	@Override
	public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
		// TODO Auto-generated method stub

	}

	/**
	 * 获取全部通讯录联系人,按拼音自然排序
	 * 
	 * @author jrjin
	 * @time 2015-10-19 下午4:28:20
	 * @return
	 */
	public List<ContactsInfo> getEmployees() {
		if (mContacts.isEmpty()) {
			// 从数据中获取
			HcLog.D(TAG + " #getEmployees before OperateDatabase start time = "+HcUtil.getDate(HcUtil.FORMAT_HOUR, System.currentTimeMillis()));
			// 以后这里可能可以做优化,都去数据库放到单独的线程里面去处理
			List<ContactsInfo> infos = ContactsOperateDatabase
					.getContacts(HcApplication.getContext());
			createTree(infos);
			HcLog.D(TAG + " getEmployees mContacts size = "+mContacts.size());
			if (mContacts.isEmpty()) {
				HcHttpRequest.getRequest().sendContactsRequest(MODULE_ID, this);
			}
		}
//		ContanctsInfoTest.print(mRootInfo);
		return mContacts;
	}

	/**
	 * 
	 * @author jrjin
	 * @time 2015-10-22 下午4:21:08
	 * @param infos
	 *            全部部门和员工
	 */
	private void createTree(List<ContactsInfo> infos) {
		if (infos.isEmpty())
			return;
		HcLog.D(TAG + " createTree start time = "+HcUtil.getDate(HcUtil.FORMAT_HOUR, System.currentTimeMillis()));
		mContacts.clear();
		Iterator<ContactsInfo> iterator = infos.iterator();
		ContactsInfo info = null;
		Iterator<ContactsInfo> intIterator = null;
		ContactsInfo temp = null;
		while (iterator.hasNext()) {
			info = iterator.next();
//			HcLog.D(TAG + " createTree info = "+info);
			intIterator = infos.iterator();
			while (intIterator.hasNext()) {
				temp = intIterator.next();
				if (temp instanceof DepartmentInfo
						&& temp.getId().equals(info.getParentId())) {
					temp.addContacts(info); // 部门会重复添加，但在addContacts方法里面处理了
					break;
				}
			}

			if (info instanceof EmployeeInfo) {
				mContacts.add(info);
				iterator.remove();
			}
		}

		for (ContactsInfo contactsInfo : infos) {
			if (contactsInfo.getParentId() == null
					|| contactsInfo.getParentId().equals("0")) { // 根目录
				mRootInfo.setId(contactsInfo.getId());
				mRootInfo.setName(contactsInfo.getName());
				mRootInfo.addAllContacts(contactsInfo);
			}
		}
		infos.clear();
		
		/**
		 * test
		 
		for (int i = 0; i < 10; i++) {
			mContacts.addAll(mContacts);
		}
		*/
		// 这里进行排序
		/**
		 * @author jrjin
		 * @date 2015-12-10 下午8:50:19
		 * 增加排序功能
		 */
		Collections.sort(mContacts, new Comparator<ContactsInfo>() {

			@Override
			public int compare(ContactsInfo lhs, ContactsInfo rhs) {
				// TODO Auto-generated method stub
				return lhs.getQuanpin().compareTo(rhs.getQuanpin());
			}
		});
		
		HcLog.D(TAG + " createTree end time = "+HcUtil.getDate(HcUtil.FORMAT_HOUR, System.currentTimeMillis()));
	}

	/**
	 * 获取当前部门下的下级部门和员工
	 * 
	 * @author jrjin
	 * @time 2015-10-19 下午4:35:42
	 * @param id
	 *            部门ID,可以为null
	 * @return 部门的下级部门和员工，id == null，返回一级部门
	 */
	public List<ContactsInfo> getContacts(String id) {
		List<ContactsInfo> infos = new ArrayList<ContactsInfo>();

		getEmployees();
		if (mContacts.isEmpty())
			return infos;

		if (null == id || id.equals(mRootInfo.getId())) {
			infos.addAll(mRootInfo.getAll());
		} else {
			CompositeIterator iterator = new CompositeIterator(
					mRootInfo.iterator());
			ContactsInfo info = null;
			while (iterator.hasNext()) {
				info = iterator.next();
				if (info instanceof DepartmentInfo && id.equals(info.getId())) {
					infos.addAll(info.getAll());
					break;
				}
			}
		}
		return infos;
	}

	/**
	 * @author jrjin
	 * @time 2015-12-10 下午9:06:59
	 * @deprecated
	 */
	public void refreshContacts() {
		HcHttpRequest.getRequest().sendContactsRequest(MODULE_ID, this);
	}
	
	public ContactsInfo getRoot() {
		return mRootInfo;
	}

	/**
	 * 要是数据为空,则直接去获取数据
	 * @author jrjin
	 * @time 2015-11-4 下午2:11:52
	 * @param context
	 */
	public void moduleCheck(Context context) {
		if (!getEmployees().isEmpty()) { // 数据为空的时候直接去获取数据
			HcHttpRequest.getRequest().sendModuleCheckCommand(
					new ModuleInfo(MODULE_ID, SettingHelper.getModuleTime(context,
							MODULE_ID, true)), this);
		}
		
	}
	
	/**
	 * 通讯录是否为空
	 * @author jrjin
	 * @time 2016-1-18 下午1:52:46
	 * @return
	 */
	public boolean isEmpty() {
		return mContacts.isEmpty();
	}

	private void notifyPage(Object data, ResponseCategory category) {
		switch (category) {
			case SUCCESS:
				if (data != null && data instanceof List<?>) {
//					HcLog.D(TAG + " notify thread = "+Thread.currentThread());
					// do something
					List<ContactsInfo> infos = (List<ContactsInfo>) data;
					HcLog.D(TAG + " #notifyPage infos size = "+infos.size());
					if (!infos.isEmpty()) { // 为空的话不做处理
						mContacts.clear();
						createTree(infos);
					}

					HcLog.D(TAG + " #notifyPage end sort time = "+HcUtil.getDate(HcUtil.FORMAT_HOUR, System.currentTimeMillis()));
				}
				notifyObservers(this, data, RequestCategory.CONTACTS_REQUEST, category);
			break;

		default:
			notifyObservers(this, data, RequestCategory.CONTACTS_REQUEST, category);
		break;

		}
	}

	/**
	 * 根据部门ID获取部门数据
	 * @param id
	 * @return
	 */
	public ContactsInfo getDepartmentById(String id) {
		if (TextUtils.isEmpty(id)) return null;
		getEmployees();
		if (mContacts.isEmpty())
			return null;

		CompositeIterator iterator = new CompositeIterator(
				mRootInfo.iterator());
		ContactsInfo info = null;
		while (iterator.hasNext()) {
			info = iterator.next();
			if (info instanceof DepartmentInfo && id.equals(info.getId())) {
				return info;
			}
		}
		return null;
	}

	public void checkContacts(Context context) {
		if (ContactsOperateDatabase.getContactsCount(context) > 0) {
			HcHttpRequest.getRequest().sendModuleCheckCommand(
					new ModuleInfo(MODULE_ID, SettingHelper.getModuleTime(context,
							MODULE_ID, true)), this);
		} else {
			HcHttpRequest.getRequest().sendContactsRequest(MODULE_ID, this);
		}
	}
}
