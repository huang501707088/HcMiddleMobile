package com.android.hcframe.pcenter;

import android.app.Activity;
import android.view.ViewGroup;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.menu.MenuPage;

public class ModifyBindTwoMenuPage extends MenuPage {

	@Override
	public AbstractPage createPage(String appId, Activity context,
			ViewGroup parent) {
		return new ModifyBindTwoPage(context, parent);
	}

}
