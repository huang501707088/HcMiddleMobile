package com.android.hcframe.internalservice.contacts;

import android.app.Activity;
import android.view.ViewGroup;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.menu.MenuPage;

public class ContactMenuPage extends MenuPage {

	@Override
	public AbstractPage createPage(String appId, Activity context,
			ViewGroup parent) {

		return new ContactPage(context, parent, appId, ContactPage.DataType.EMPLOYEE, false);
	}

}
