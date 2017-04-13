package com.android.hcframe.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;

import com.android.hcframe.menu.MenuTextView;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-5-11 10:02.
 */
public class ColumnTextView extends MenuTextView {

    public ColumnTextView(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    public ColumnTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }

    public ColumnTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        mRedraw = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        if (mCount <= 0) return; // 不需要显示
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if (width <= 0 || height <= 0) return;
        mPaint.setColor(Color.RED);
        /**
         * 修改角标显示位置
         * czx
         * 2016.4.29
         */
        float cx = width*3/4;
        float cy= height/4;
        canvas.drawCircle(cx, cy, mRadius, mPaint);
        if (mFlag == FLAG_TEXT) {
            mPaint.setColor(Color.WHITE);
            canvas.drawText(mTextCount, cx, cy+mRadius/3, mPaint);
        }

    }
}
