/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-23 上午11:26:48
*/
package com.android.hcframe;

import com.android.hcframe.badge.BadgeObserver;
import com.android.hcframe.view.circle.ShaderHelper;
import com.android.hcframe.view.circle.ShaderImageView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class PointImageView extends ShaderImageView implements BadgeObserver {

	private Paint mPaint;
	
	/** 不显示数字的点的半径 */
	private static final int POINT_RADIUS = 5;
	/** 显示数字的点的半径 */
	private static final int POINT_RADIUS_TEXT = 8;
	/** 数据的条数 不显示数字的时候可以随便任意的数字只要>0*/
	private int mCount = 0;
	
	public static final int FLAG_TEXT = 1;
	
	public static final int FLAG_NONE = 2;
	/** 显示数字:1;不显示数字：2*/
	private int mFlag = FLAG_NONE;
	/** 点的半径 */
	private float mRadius;
	/** 数字内容，超过99，显示99+ */
	private String mTextCount;
	/** 数字显示的Y值 */
	private float mTextY;
	
	public PointImageView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public PointImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public PointImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init();
	}

	@Override
	public void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		if (mCount <= 0) return; // 不需要显示
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		if (width <= 0 || height <= 0) return;
		mPaint.setColor(Color.RED);
		int tempWidth = width >= height ? height : width;
		float cx = (width -tempWidth) / 2 + tempWidth - mRadius;
		canvas.drawCircle(cx, mRadius, mRadius, mPaint);
		if (mFlag == FLAG_TEXT) {
			mPaint.setColor(Color.WHITE);
			canvas.drawText(mTextCount, cx, mTextY, mPaint);
		}
	}

	private void init() {
		mPaint = new Paint();
		mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mPaint.setTextAlign(Paint.Align.CENTER);
		mPaint.setTextSize(10 * HcUtil.getScreenDensity());
		mRadius = (mFlag == FLAG_NONE ? POINT_RADIUS : POINT_RADIUS_TEXT) * HcUtil.getScreenDensity();
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
		
		postInvalidateDelayed(200);
	}

	@Override
	protected ShaderHelper createImageViewHelper() {
		// TODO Auto-generated method stub
		return null;
	}
}
