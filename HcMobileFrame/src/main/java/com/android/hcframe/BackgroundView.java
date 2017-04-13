package com.android.hcframe;

import com.android.hcframe.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Shader.TileMode;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class BackgroundView extends LinearLayout {

	private Bitmap mBitmap;
	private int mHeight;
	private int mWidth;
	
	private Shader mShader;
	private Paint mBmPaint;
	
	public BackgroundView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context);
		setWillNotDraw(false);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub		
//		mBmPaint.setShader(mShader);
		canvas.drawPaint(mBmPaint);
	}

	private void init(Context context) {
		Drawable bg = context.getResources().getDrawable(R.drawable.topbar_bg);
		mHeight = bg.getIntrinsicHeight();
		mWidth = bg.getIntrinsicWidth();
		mBitmap = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
		Canvas canvas = new Canvas();
		canvas.setBitmap(mBitmap);
		bg.setBounds(0, 0, mWidth, mHeight);
		bg.draw(canvas);
		mShader = new BitmapShader(mBitmap, TileMode.REPEAT, TileMode.CLAMP);
		mBmPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBmPaint.setShader(mShader);
	}

}
