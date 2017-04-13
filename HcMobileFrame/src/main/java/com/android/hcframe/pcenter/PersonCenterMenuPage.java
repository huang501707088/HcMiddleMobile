package com.android.hcframe.pcenter;

import android.app.Activity;
import android.view.ViewGroup;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.menu.MenuPage;

public class PersonCenterMenuPage extends MenuPage {

	@Override
	public AbstractPage createPage(String appId, Activity context,
			ViewGroup parent) {
		return new PersonCenterPage(context, parent);
	}

}
