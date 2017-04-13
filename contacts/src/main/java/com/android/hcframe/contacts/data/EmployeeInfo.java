/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-10-13 上午10:57:45
 */
package com.android.hcframe.contacts.data;

import java.util.Iterator;
import java.util.List;

import com.android.hcframe.HcLog;
import com.android.hcframe.internalservice.contacts.HanziToPinyin;
import com.android.hcframe.internalservice.contacts.HanziToPinyin.Token;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.text.TextUtils;

/**
 * 员工
 * 
 * @author jrjin
 * @time 2015-10-14 上午9:25:33
 */
public class EmployeeInfo extends ContactsInfo {

	private static final String TAG = "EmployeeInfo";

	/** 员工邮箱 */
	private String mEmail;
	/** 手机号码 */
	private String mMobilePhone;
	/** 备用手机号码 */
	private String mStandbyPhone;
	/** 固定号码 */
	private String mFixedPhone;
	/** 备用邮箱 */
	private String mStandbyEmail;
	/** 分机号 */
	private String mExtensionNumber;
	/** 虚拟网号 */
	private String mVirtualNetNumber;
	/** 名字全拼 */
	private String mQuanpin;
	/** 名字简拼 */
	private String mJianpin;
	/** 汉字图标 */
	private String mIcon;
	/** 名字首字母 */
	private String mAlphabet;
	/** 移动电话是否可见 */
	private boolean mVisibility = true;
	/** 用户的唯一的Id */
	private String mUserId;

	public EmployeeInfo() {
		super();
	}

	public EmployeeInfo(Parcel p) {
		super(p);
		mEmail = p.readString();
		mExtensionNumber = p.readString();
		mFixedPhone = p.readString();
		mMobilePhone = p.readString();
		mStandbyEmail = p.readString();
		mStandbyPhone = p.readString();
		mVirtualNetNumber = p.readString();
		mQuanpin = p.readString();
		mJianpin = p.readString();
		mIcon = p.readString();
		mAlphabet = p.readString();
		mVisibility = p.readInt() == 0;
		mUserId = p.readString();
	}

	@Override
	public void setEmail(String email) {
		// TODO Auto-generated method stub
		mEmail = email;
	}

	@Override
	public String getEmail() {
		// TODO Auto-generated method stub
		return mEmail;
	}

	@Override
	public void setMobilePhone(String phone) {
		// TODO Auto-generated method stub
		mMobilePhone = phone;
	}

	@Override
	public String getMobilePhone() {
		// TODO Auto-generated method stub
		return mMobilePhone;
	}

	@Override
	public void setStandbyPhone(String phone) {
		// TODO Auto-generated method stub
		mStandbyPhone = phone;
	}

	@Override
	public String getStandbyPhone() {
		// TODO Auto-generated method stub
		return mStandbyPhone;
	}

	@Override
	public void setFixedPhone(String phone) {
		// TODO Auto-generated method stub
		mFixedPhone = phone;
	}

	@Override
	public String getFixedPhone() {
		// TODO Auto-generated method stub
		return mFixedPhone;
	}

	@Override
	public void setStandEmail(String email) {
		// TODO Auto-generated method stub
		mStandbyEmail = email;
	}

	@Override
	public String getStandEmail() {
		// TODO Auto-generated method stub
		return mStandbyEmail;
	}

	@Override
	public void setExtensionNumber(String number) {
		// TODO Auto-generated method stub
		mExtensionNumber = number;
	}

	@Override
	public String getExtensionNumber() {
		// TODO Auto-generated method stub
		return mExtensionNumber;
	}

	@Override
	public void setVirtualNetNumber(String number) {
		// TODO Auto-generated method stub
		mVirtualNetNumber = number;
	}

	@Override
	public String getVirtualNetNumber() {
		// TODO Auto-generated method stub
		return mVirtualNetNumber;
	}

	@Override
	public Iterator<ContactsInfo> iterator() {
		// TODO Auto-generated method stub
		return new NullIterator();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		super.writeToParcel(dest, flags);
		dest.writeString(mEmail);
		dest.writeString(mExtensionNumber);
		dest.writeString(mFixedPhone);
		dest.writeString(mMobilePhone);
		dest.writeString(mStandbyEmail);
		dest.writeString(mStandbyPhone);
		dest.writeString(mVirtualNetNumber);
		dest.writeString(mQuanpin);
		dest.writeString(mJianpin);
		dest.writeString(mIcon);
		dest.writeString(mAlphabet);
		dest.writeInt(mVisibility == true ? 0 : 1);
		dest.writeString(TextUtils.isEmpty(mUserId) ? "" : mUserId);
	}

	public static final Creator<EmployeeInfo> CREATOR = new Creator<EmployeeInfo>() {

		@Override
		public EmployeeInfo createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new EmployeeInfo(source);
		}

		@Override
		public EmployeeInfo[] newArray(int size) {
			// TODO Auto-generated method stub
			return new EmployeeInfo[size];
		}
	};

	@Override
	public void sendMessage(Activity context, String content) {
		// TODO Auto-generated method stub
		Uri smsToUri = Uri.parse("smsto:" + mMobilePhone);
		Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
		intent.putExtra("sms_body", content);
		context.startActivity(intent);
	}

	@Override
	public void saveContacts(Activity context) {
//		Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);// 把电话保存到现有联系人
		Intent it = new Intent(Intent.ACTION_INSERT, Uri.withAppendedPath(
				Uri.parse("content://com.android.contacts"), "contacts"));
		it.setType("vnd.android.cursor.dir/person");
		it.putExtra(android.provider.ContactsContract.Intents.Insert.NAME,
				getName());
		it.putExtra(android.provider.ContactsContract.Intents.Insert.EMAIL,
				getEmail());
		it.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE,
				getMobilePhone());
		it.putExtra(
				android.provider.ContactsContract.Intents.Insert.SECONDARY_EMAIL,
				getStandEmail());
		it.putExtra(
				android.provider.ContactsContract.Intents.Insert.SECONDARY_PHONE,
				getStandbyPhone());
		it.putExtra(
				android.provider.ContactsContract.Intents.Insert.TERTIARY_PHONE,
				getFixedPhone());

		context.startActivity(it);
	}

	@Override
	public String getQuanpin() {
		// TODO Auto-generated method stub
		return mQuanpin;
	}

	@Override
	public String getJianpin() {
		// TODO Auto-generated method stub
		return mJianpin;
	}

	@Override
	public String getNameIcon() {
		// TODO Auto-generated method stub
		return mIcon;
	}

	@Override
	public String getAlphabet() {
		// TODO Auto-generated method stub
		return mAlphabet;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		super.setName(name);
//		HcLog.D(TAG + " #setName = "+name);
		mJianpin = "";
		mQuanpin = "";
		if (!TextUtils.isEmpty(name)) {
			int length = name.length();
//			HcLog.D(TAG + " #setName = "+name + " length = "+length);
			mIcon = length > 2 ? name.substring(length - 2, length) : name;
			List<Token> tokens = HanziToPinyin.getInstance().get(name);
//			HcLog.D(TAG + " #setName = "+name + " token size = "+tokens.size() );
			for (Token token : tokens) {
				mQuanpin += token.target;
				mJianpin += token.target.substring(0, 1);
			}
			if (mJianpin.length() > 0)
				mAlphabet = mJianpin.substring(0, 1);
//			HcLog.D(TAG + " #setName mJianpin = "+mJianpin + " mQuanpin = "+mQuanpin + " mAlphabet = "+mAlphabet);
			tokens.clear();
		}
	}

	@Override
	public void setPhoneVisibility(boolean visibility) {
		mVisibility = visibility;
	}

	@Override
	public boolean getPhoneVisibility() {
		return mVisibility;
	}

	@Override
	public String getUserId() {
		return mUserId;
	}

	@Override
	public void setUserId(String userId) {
		mUserId = userId;
	}
}
