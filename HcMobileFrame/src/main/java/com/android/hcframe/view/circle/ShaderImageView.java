package com.android.hcframe.view.circle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

@SuppressWarnings("WeakerAccess")
public abstract class ShaderImageView extends ImageView {

    private final static String TAG = "ShaderImageView";
    
    private ShaderHelper mPathHelper;

    public ShaderImageView(Context context) {
        super(context);
        setup(context, null, 0);
    }

    public ShaderImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs, 0);
    }

    public ShaderImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup(context, attrs, defStyle);
    }

    private void setup(Context context, AttributeSet attrs, int defStyle) {
        if (getPathHelper() != null)
        	mPathHelper.init(context, attrs, defStyle);
    }

    protected ShaderHelper getPathHelper() {
        if(mPathHelper == null) {
        	mPathHelper = createImageViewHelper();
        }
        return mPathHelper;
    }

    protected abstract ShaderHelper createImageViewHelper();

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getPathHelper() != null) {
        	if(mPathHelper.isSquare()) {
                super.onMeasure(widthMeasureSpec, widthMeasureSpec);
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        } else {
        	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
    	
    	
    }

    //Required by path helper
    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        if (getPathHelper() != null) {
        	mPathHelper.onImageDrawableReset(getDrawable());
        }
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (getPathHelper() != null) {
        	getPathHelper().onImageDrawableReset(getDrawable());
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        if (getPathHelper() != null)
        	getPathHelper().onImageDrawableReset(getDrawable());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (getPathHelper() != null)
        	getPathHelper().onSizeChanged(w, h);
    }

    @Override
    public void onDraw(Canvas canvas) {
    	if (getPathHelper() != null) {
    		if(!getPathHelper().onDrawShader(canvas)) {
                super.onDraw(canvas);
            }
    	} else {
			super.onDraw(canvas);
		}
        
    }

    public void setBorderColor(final int borderColor) {
    	if (getPathHelper() != null) {
    		getPathHelper().setBorderColor(borderColor);
            invalidate();
    	}
        
    }

    public int getBorderWidth() {
    	if (getPathHelper() != null)
    		return getPathHelper().getBorderWidth();
    	return 0;
    }

    public void setBorderWidth(final int borderWidth) {
    	if (getPathHelper() != null) {
    		getPathHelper().setBorderWidth(borderWidth);
    		invalidate();
    	}
        
    }

    public float getBorderAlpha() {
    	if (getPathHelper() != null)
    		return getPathHelper().getBorderAlpha();
    	return 0.0f;
    }

    public void setBorderAlpha(final float borderAlpha) {
    	if (getPathHelper() != null) {
    		getPathHelper().setBorderAlpha(borderAlpha);
    		invalidate();
    	}
        
    }

    public void setSquare(final boolean square) {
    	if (getPathHelper() != null) {
    		getPathHelper().setSquare(square);
    		invalidate();
    	} 
    }
}