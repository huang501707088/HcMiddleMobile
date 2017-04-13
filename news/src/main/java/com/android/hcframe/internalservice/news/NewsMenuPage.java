package com.android.hcframe.internalservice.news;

import android.app.Activity;
import android.view.ViewGroup;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.menu.MenuPage;

public class NewsMenuPage extends MenuPage{

	@Override
	public AbstractPage createPage(String appId, Activity context,
			ViewGroup parent) {
		return new NewsHomePage(context, parent, appId);
	}

}
