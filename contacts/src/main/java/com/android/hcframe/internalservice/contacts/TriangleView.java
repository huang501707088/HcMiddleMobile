package com.android.hcframe.internalservice.contacts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-11-1 11:54.
 */

public class TriangleView extends View {

    private Paint mPaint;

    private Path mPath;

    public TriangleView(Context context) {
        super(context);
        init();
    }

    public TriangleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TriangleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (width > 0 && height > 0) {

            mPath.reset();
            mPath.moveTo(0, height);
            mPath.lineTo(width / 2, 0);
            mPath.lineTo(width, height);
            canvas.drawPath(mPath, mPaint);
        }

    }

    private void init() {
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.parseColor("#b3000000"));
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }
}
