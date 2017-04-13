package com.android.hcframe.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.badge.BadgeObserver;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-11-8 13:40.
 */

public class PointTextView extends TextView implements BadgeObserver {

    private static final String TAG = "MenuTextView";

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

    public PointTextView(Context context) {
        this(context, null);
    }

    public PointTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PointTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PointTextView, defStyleAttr, 0);
            mFlag = a.getInt(R.styleable.PointTextView_flag, FLAG_NONE);
            a.recycle();
        }
//        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextAlign(Paint.Align.CENTER);
//        mPaint.setTextSize(10 * HcUtil.getScreenDensity());

    }

    @Override
    public void setCount(int count) {
        if (mCount != count) {
            mCount = count;
            if (mCount <= 0) {
                if (getVisibility() != View.INVISIBLE)
                    setVisibility(View.INVISIBLE);
            } else {
                if (mFlag == FLAG_NONE) {
                    setText("");
                } else {
                    if (mCount > 99) {
                        setText("99+");
                    } else {
                        setText("" + mCount);
                    }
                }

                if (getVisibility() != View.VISIBLE)
                    setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void setFlag(int flag) {
        mFlag = flag;
    }

//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        int width = getWidth();
//        int height = getHeight();
//        if (width <= 0 || height <= 0) return;
//        canvas.drawCircle(width / 2, height / 2, width > height ? height / 2 : width / 2, mPaint);
//    }
}
