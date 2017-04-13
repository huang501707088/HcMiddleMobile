package com.android.hcframe.push;

public class PushSubModuleItem extends PushItem {

	/**
	 * 存储的时候,0表示不推送,1表示推送
	 */
	private boolean mPushed;

	@Override
	public boolean isPushed() {
		return mPushed;
	}

	@Override
	public void setPushed(boolean pushed) {
		mPushed = pushed;
	}
}
