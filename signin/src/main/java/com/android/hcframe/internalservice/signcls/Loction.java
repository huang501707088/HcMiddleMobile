package com.android.hcframe.internalservice.signcls;

public class Loction {

	private String mAccount;

	/**
	 * 签到/签出方式：0—自动，1—手动
	 */
	private String mSignType;

	/**
	 * 签到/签出：0—签到，1—签出
	 */
	private String mSignFlag;
	/**
	 * 经度
	 */
	private String mAddressLongitude;
	/**
	 * 维度
	 */
	private String mAddressLatitude;
	/**
	 * 签到类型：0-本地考勤，1，外勤
	 */
	private String mType;
	/**
	 * 签到地址
	 */
	private String mAddress;

	public Loction() {

	}

	public String getmAccount() {
		return mAccount;
	}

	public void setmAccount(String mAccount) {
		this.mAccount = mAccount;
	}

	public String getmSignType() {
		return mSignType;
	}

	public void setmSignType(String mSignType) {
		this.mSignType = mSignType;
	}

	public String getmSignFlag() {
		return mSignFlag;
	}

	public void setmSignFlag(String mSignFlag) {
		this.mSignFlag = mSignFlag;
	}

	public String getmAddressLongitude() {
		return mAddressLongitude;
	}

	public void setmAddressLongitude(String mAddressLongitude) {
		this.mAddressLongitude = mAddressLongitude;
	}

	public String getmAddressLatitude() {
		return mAddressLatitude;
	}

	public void setmAddressLatitude(String mAddressLatitude) {
		this.mAddressLatitude = mAddressLatitude;
	}

	public String getmType() {
		return mType;
	}

	public void setmType(String mType) {
		this.mType = mType;
	}

	public String getmAddress() {
		return mAddress;
	}

	public void setmAddress(String mAddress) {
		this.mAddress = mAddress;
	}
}
