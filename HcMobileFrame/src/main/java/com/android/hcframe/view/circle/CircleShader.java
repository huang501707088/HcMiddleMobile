package com.android.hcframe.view.circle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

public class CircleShader extends ShaderHelper {
    private float mCenter;
    private float mBitmapCenterX;
    private float mBitmapCenterY;
    private float mBorderRadius;
    private int mBitmapRadius;

    public CircleShader() {
    }

    @Override
    public void init(Context context, AttributeSet attrs, int defStyle) {
        super.init(context, attrs, defStyle);
        square = true;
    }

    @Override
    public void draw(Canvas canvas, Paint imagePaint, Paint borderPaint) {
        canvas.drawCircle(mCenter, mCenter, mBorderRadius, borderPaint);
        canvas.save();
        canvas.concat(mMatrix);
        canvas.drawCircle(mBitmapCenterX, mBitmapCenterY, mBitmapRadius, imagePaint);
        canvas.restore();
    }

    @Override
    public void onSizeChanged(int width, int height) {
        super.onSizeChanged(width, height);
        mCenter = Math.round(mViewWidth / 2f);
        mBorderRadius = Math.round((mViewWidth - mBorderWidth) / 2f);
    }

    @Override
    public void calculate(int bitmapWidth, int bitmapHeight,
                          float width, float height,
                          float scale,
                          float translateX, float translateY) {
        mBitmapCenterX = Math.round(bitmapWidth / 2f);
        mBitmapCenterY = Math.round(bitmapHeight / 2f);
        mBitmapRadius = Math.round(width / scale / 2f + 0.5f);
    }

    @Override
    public void reset() {
        mBitmapRadius = 0;
        mBitmapCenterX = 0;
        mBitmapCenterY = 0;
    }

    public final float getBorderRadius() {
        return mBorderRadius;
    }

    public final void setBorderRadius(final float borderRadius) {
        mBorderRadius = borderRadius;
    }
}