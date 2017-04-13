package com.android.hcframe.servicemarket.photoscan;

import java.util.ArrayList;
import java.util.List;

public class NewsDetailsInfo {

	private String contentType;

	private String id;

	private String title;

	private String itemUrl;

	private String date;

	private List<PicInfo> pics = new ArrayList<PicInfo>();

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

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

	public String getItemUrl() {
		return itemUrl;
	}

	public void setItemUrl(String itemUrl) {
		this.itemUrl = itemUrl;
	}

	public List<PicInfo> getPics() {
		return pics;
	}

	public void setPics(List<PicInfo> pics) {
		this.pics = pics;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
}
