package com.android.hcframe.view.circle;

import com.android.hcframe.HcUtil;
import com.android.hcframe.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;


@SuppressWarnings("WeakerAccess")
public abstract class ShaderHelper {
    private final static int ALPHA_MAX = 255;

    protected int mViewWidth;
    protected int mViewHeight;

    /** 边框的颜色 */
    protected int mBorderColor = Color.BLACK;
    /** 边框的宽度 */
    protected int mBorderWidth = 0;
    /** 边框的透明度 */
    protected float mBorderAlpha = 1f;
    /** 是否是正方形 */
    protected boolean square = false;

    /** 边框画笔 */
    protected final Paint mBorderPaint;
    /** 图片画笔 */
    protected final Paint mImagePaint;
    protected BitmapShader mShader;
    /** 需要显示的Drawable */
    protected Drawable mDrawable;
    protected final Matrix mMatrix = new Matrix();

    protected int mPadding;
    
    public ShaderHelper() {
        mBorderPaint = new Paint();
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);

        mImagePaint = new Paint();
        mImagePaint.setAntiAlias(true);
        
        mPadding = (int) (5 * HcUtil.getScreenDensity());
    }

    /**
     * 画边框及Drawable
     * @author jrjin
     * @time 2015-11-24 上午11:07:05
     * @param canvas
     * @param imagePaint
     * @param borderPaint
     */
    public abstract void draw(Canvas canvas, Paint imagePaint, Paint borderPaint);
    /**
     * 重置数据
     * @author jrjin
     * @time 2015-11-24 上午11:07:12
     */
    public abstract void reset();
    @SuppressWarnings("UnusedParameters")
    public abstract void calculate(int bitmapWidth, int bitmapHeight, float width, float height, float scale, float translateX, float translateY);


    @SuppressWarnings("SameParameterValue")
    protected final int dpToPx(DisplayMetrics displayMetrics, int dp) {
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public void init(Context context, AttributeSet attrs, int defStyle) {
        if(attrs != null){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShaderImageView, defStyle, 0);
            mBorderColor = typedArray.getColor(R.styleable.ShaderImageView_siBorderColor, mBorderColor);
            mBorderWidth = typedArray.getDimensionPixelSize(R.styleable.ShaderImageView_siBorderWidth, mBorderWidth);
            mBorderAlpha = typedArray.getFloat(R.styleable.ShaderImageView_siBorderAlpha, mBorderAlpha);
            square = typedArray.getBoolean(R.styleable.ShaderImageView_siSquare, square);
            typedArray.recycle();
        }

        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setAlpha(Float.valueOf(mBorderAlpha * ALPHA_MAX).intValue());
        mBorderPaint.setStrokeWidth(mBorderWidth);
    }

    public boolean onDrawShader(Canvas canvas) {
        if (mShader == null) {
            createShader();
        }
        if (mShader != null && mViewWidth > 0 && mViewHeight > 0) {
            draw(canvas, mImagePaint, mBorderPaint);
            return true;
        }

        return false;
    }

    public void onSizeChanged(int width, int height) {
        if(mViewWidth == width && mViewHeight == height) return;
//        mViewWidth = width;
//        mViewHeight = height;
        if (isSquare()) {
        	mViewWidth = width;
        	mViewHeight = height;
        } else {
        	mViewWidth = width - mPadding * 2;
        	mViewHeight = height - mPadding * 2;
		}
        if(isSquare()) {
            mViewWidth = mViewHeight = Math.min(width, height);
        }
        if(mShader != null) {
            calculateDrawableSizes();
        }
    }

    public Bitmap calculateDrawableSizes() {
        Bitmap bitmap = getBitmap();
        if(bitmap != null) {
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();

            if(bitmapWidth > 0 && bitmapHeight > 0) {
                float width = Math.round(mViewWidth - 2f * mBorderWidth);
                float height = Math.round(mViewHeight - 2f * mBorderWidth);

                float scale;
                float translateX = 0;
                float translateY = 0;

                if (bitmapWidth * height > width * bitmapHeight) {
                    scale = height / bitmapHeight;
                    translateX = Math.round((width/scale - bitmapWidth) / 2f);
                } else {
                    scale = width / (float) bitmapWidth;
                    translateY = Math.round((height/scale - bitmapHeight) / 2f);
                }

                mMatrix.setScale(scale, scale);
                mMatrix.preTranslate(translateX, translateY);
                mMatrix.postTranslate(mBorderWidth, mBorderWidth);

                calculate(bitmapWidth, bitmapHeight, width, height, scale, translateX, translateY);

                return bitmap;
            }
        }

        reset();
        return null;
    }

    public final void onImageDrawableReset(Drawable drawable) {
        mDrawable = drawable;
        mShader = null;
        mImagePaint.setShader(null);
    }

    protected void createShader() {
        Bitmap bitmap = calculateDrawableSizes();
        if(bitmap != null && bitmap.getWidth() > 0 && bitmap.getHeight() > 0) {
            mShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            mImagePaint.setShader(mShader);
        }
    }

    protected Bitmap getBitmap() {
        Bitmap bitmap = null;
        if(mDrawable != null) {
            if(mDrawable instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) mDrawable).getBitmap();
            }
        }

        return bitmap;
    }

    public final int getBorderColor() {
        return mBorderColor;
    }

    public final void setBorderColor(final int borderColor) {
        mBorderColor = borderColor;
        if(mBorderPaint != null) {
            mBorderPaint.setColor(borderColor);
        }
    }

    public final int getBorderWidth() {
        return mBorderWidth;
    }

    public final void setBorderWidth(final int borderWidth) {
        mBorderWidth = borderWidth;
        if(mBorderPaint != null) {
            mBorderPaint.setStrokeWidth(borderWidth);
        }
    }

    public final float getBorderAlpha() {
        return mBorderAlpha;
    }

    public final void setBorderAlpha(final float borderAlpha) {
        mBorderAlpha = borderAlpha;
        if(mBorderPaint != null) {
            mBorderPaint.setAlpha(Float.valueOf(borderAlpha * ALPHA_MAX).intValue());
        }
    }

    public final void setSquare(final boolean square) {
        this.square = square;
    }

    public final boolean isSquare() {
        return square;
    }
}