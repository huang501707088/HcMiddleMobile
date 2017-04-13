package com.android.hcframe.push;

import java.util.ArrayList;
import java.util.List;

public class PushModuleItem extends PushItem {

	private List<PushItem> mItems = new ArrayList<PushItem>();

	@Override
	public List<PushItem> getItems() {
		return mItems;
	}

	@Override
	public void addItem(PushItem item) {
		if (mItems.contains(item)) return;
		mItems.add(item);
	}
}
