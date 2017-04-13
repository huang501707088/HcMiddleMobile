/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-6-10 下午8:37:06
*/
package com.android.hcframe.internalservice.news;

import com.android.hcframe.HcUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.GridView;

public class HcGridView extends GridView {

	private int mColCount;
	
	private Paint mDividerPaint;
	
	private int mGridWidth;
	
	private int mGridHeight;
	
	public HcGridView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public HcGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public HcGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init(context);
	}

	private void init(Context context) {
		mColCount = context.getResources().getInteger(R.integer.col_count);
		mDividerPaint = new Paint();
		mDividerPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mDividerPaint.setColor(context.getResources().getColor(R.color.divider_bg));
		mGridHeight = HcUtil.getScreenHeight();
		mGridWidth = HcUtil.getScreenWidth();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		int count = getChildCount();
		int row = (count + mColCount - 1) / mColCount;
		if (count > 0) {
			for (int i = 1; i < mColCount; i++) {
				canvas.drawLine(i * mGridWidth, 0, i * mGridWidth, row * mGridHeight, mDividerPaint);	
			}
			
			for (int i = 1; i <= row; i++) {
				canvas.drawLine(0, i * mGridHeight, getWidth(), i * mGridHeight, mDividerPaint);
			}

		}
	}

}
