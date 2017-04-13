/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-10-26 上午10:34:51
*/
package com.android.hcframe.menu;

import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.badge.BadgeObserver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

public class MenuTextView extends TextView implements BadgeObserver {

	private static final String TAG = "MenuTextView";
	
	protected Paint mPaint;
	
	/** 不显示数字的点的半径 */
	private static final int POINT_RADIUS = 5;
	/** 显示数字的点的半径 */
	private static final int POINT_RADIUS_TEXT = 8;
	/** 数据的条数 不显示数字的时候可以随便任意的数字只要>0*/
	protected int mCount = 0;
	
	public static final int FLAG_TEXT = 1;
	
	public static final int FLAG_NONE = 2;
	/** 显示数字:1;不显示数字：2*/
	protected int mFlag = FLAG_NONE;
	/** 点的半径 */
	protected float mRadius;
	/** 数字内容，超过99，显示99+ */
	protected String mTextCount;
	/** 数字显示的Y值 */
	private float mTextY;
	/** draw栏目的角标的时候,不需要再画菜单的标题 */
	protected boolean mRedraw = true;

	private int mImageWidth;

	/** 角标圆点的y坐标 */
	private float mCy;
	
	public MenuTextView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public MenuTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public MenuTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init(context);
	}

	private void init(Context context) {
		mPaint = new Paint();
		mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mPaint.setTextAlign(Paint.Align.CENTER);
		mPaint.setTextSize(10 * HcUtil.getScreenDensity());
		mRadius = (mFlag == FLAG_NONE ? POINT_RADIUS : POINT_RADIUS_TEXT) * HcUtil.getScreenDensity();
		mImageWidth = context.getResources().getDrawable(R.drawable.menu_fifth_normal).getIntrinsicWidth();
		float menuHeight = context.getResources().getDimension(R.dimen.menu_height);
		float f = (menuHeight - mImageWidth - 10 * HcUtil.getScreenDensity()) / 2;
		mCy = f < 10 * HcUtil.getScreenDensity() ? 10 * HcUtil.getScreenDensity() : f;
		HcLog.D(TAG + " #init image width ===============" + mImageWidth + " menu height = "+menuHeight + " cy = "+mCy);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		if (!mRedraw || mCount <= 0) return; // 不需要显示
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		if (width <= 0 || height <= 0) return;
		mPaint.setColor(Color.RED);

		float cx = width - (width - mImageWidth) / 2;
		canvas.drawCircle(cx, mCy, mRadius, mPaint);
		if (mFlag == FLAG_TEXT) {
			mPaint.setColor(Color.WHITE);
			Rect bounds = new Rect();
			mPaint.getTextBounds(mTextCount, 0, mTextCount.length(), bounds);
			HcLog.D(TAG + " #onDraw bound w = "+bounds.width() + " bound h = "+bounds.height());
			canvas.drawText(mTextCount, cx, mCy + (int) (bounds.height() / 24d * 11), mPaint);
		}
		
	}

	/**
	 * 设置推送时未读的数据条数
	 * @author jrjin
	 * @time 2015-10-26 下午3:36:23
	 * @param count 推送未读的数据量 <=0时，不显示点
	 */
	@Override
	public void setCount(int count) {
		mCount = count;
		Rect bounds = new Rect();
		if (mCount <= 0) {
			mTextCount = null;
			mTextY = mRadius;
		} else if (mCount >= 100) {
			mTextCount = "99+";
			mPaint.getTextBounds(mTextCount, 0, mTextCount.length(), bounds);
		} else {
			mTextCount = mCount + "";
			mPaint.getTextBounds(mTextCount, 0, mTextCount.length(), bounds);
		}
		mTextY = mRadius + bounds.height() / 2;
//		invalidate(); // 不会每次都绘画
		postInvalidateDelayed(200);
	}
	
	/**
	 * 设置显示的点的类型
	 * @author jrjin
	 * @time 2015-10-26 下午3:43:26
	 * @param flag 显示数字:0;不显示数字：1
	 */
	@Override
	public void setFlag(int flag) {
		mFlag = flag;
		mRadius = (mFlag == FLAG_NONE ? POINT_RADIUS : POINT_RADIUS_TEXT) * HcUtil.getScreenDensity();
		postInvalidateDelayed(200);
	}
}
