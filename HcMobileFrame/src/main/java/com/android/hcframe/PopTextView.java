package com.android.hcframe;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
/**
 * 可以一直有跑马灯效果
 * @author jrjin
 * @time 2016-1-13 下午1:50:05
 */
public class PopTextView extends TextView {

	public PopTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isFocused() {
		// TODO Auto-generated method stub
		return true;
	}

	
}
