package com.android.hcframe.ebook.entity;

import java.io.Serializable;

public class FileEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id;

	private String title;

	private String oldfileid;

	private String newfileid;

	private String signState;

	private String createdtime;

	private String signtime;

	private String uniqueid;

	private String userid;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getOldFileid() {
		return oldfileid;
	}

	public void setOldFileid(String oldFileid) {
		this.oldfileid = oldFileid;
	}

	public String getNewFileid() {
		return newfileid;
	}

	public void setNewFileid(String newFileid) {
		this.newfileid = newFileid;
	}

	public String getSignState() {
		return signState;
	}

	public void setSignState(String signState) {
		this.signState = signState;
	}

	public String getCreatedtime() {
		return createdtime;
	}

	public void setCreatedtime(String createdtime) {
		this.createdtime = createdtime;
	}

	public String getSigntime() {
		return signtime;
	}

	public void setSigntime(String signtime) {
		this.signtime = signtime;
	}

	public String getUniqueid() {
		return uniqueid;
	}

	public void setUniqueid(String uniqueid) {
		this.uniqueid = uniqueid;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

}
