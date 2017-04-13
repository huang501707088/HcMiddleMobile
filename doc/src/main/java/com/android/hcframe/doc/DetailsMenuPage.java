package com.android.hcframe.doc;

import android.app.Activity;
import android.view.ViewGroup;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.menu.MenuPage;

public class DetailsMenuPage extends MenuPage{

	@Override
	public AbstractPage createPage(String appId, Activity context,
			ViewGroup parent) {
		return new DetailsPage(context, parent);
	}

}
