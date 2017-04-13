package com.android.hcframe.view.circle;

import android.content.Context;
import android.util.AttributeSet;

public class CircularImageView extends ShaderImageView {

    private CircleShader mShader;

    public CircularImageView(Context context) {
        super(context);
    }

    public CircularImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircularImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public ShaderHelper createImageViewHelper() {
    	mShader = new CircleShader();
        return mShader;
    }

    public float getBorderRadius() {
        if(mShader != null) {
            return mShader.getBorderRadius();
        }
        return 0;
    }

    public void setBorderRadius(final float borderRadius) {
        if(mShader != null) {
        	mShader.setBorderRadius(borderRadius);
            invalidate();
        }
    }
}
