package com.android.hcframe.ebook.entity;

public class ConfigEntity {
	public final static int SIGN_TYPE_COMMON = 1;// 普通模式
	public final static int SIGN_TYPE_EBEN = 0;// E人E本模式
	public final static int SUBMIT_PDF = 0;// 提交pdf文件
	public final static int SUBMIT_TXT = 1;// 提交签字内容
	
	private String id;
	private String configname;
	private int configvalue;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getConfigname() {
		return configname;
	}

	public void setConfigname(String configname) {
		this.configname = configname;
	}

	public int getConfigvalue() {
		return configvalue;
	}

	public void setConfigvalue(int configvalue) {
		this.configvalue = configvalue;
	}
}
