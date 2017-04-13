package com.android.hcframe.netdisc.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.android.hcframe.HcApplication;
import com.android.hcframe.netdisc.R;

/**
 * Created by ncll on 2017/3/9.
 */

public class DiscInfoRingView extends View {

    private Paint paint;
    private Context context;
    /*圆弧线宽*/
    private float circleBorderWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
    /*内边距*/
    private float circlePadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
    /*字体大小*/
    private float textSize1 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics());
    /*字体大小*/
    private float textSize2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, getResources().getDisplayMetrics());
    /*绘制圆周的画笔*/
    private Paint backCirclePaint;
    /*绘制圆周白色分割线的画笔*/
    private Paint linePaint;
    /*绘制文字的画笔*/
    private Paint textPaint1;
    /*绘制文字的画笔*/
    private Paint textPaint2;
    /*百分比*/
    private float percentfree = 0;
    /*百分比*/
    private float percentshare = 0;
    /*渐变圆周颜色数组*/
    private int[] gradientColorArray = new int[]{Color.GREEN, Color.parseColor("#fe751a"), Color.parseColor("#13be23"), Color.GREEN};
    private Paint gradientCirclePaint;
    String used="";
    String total="";
    public DiscInfoRingView(Context context) {
        super(context);
        init();
    }

//    public DiscInfoRingView(Context context, int freeSpace, int shareSpace) {
//        this(context);
//        percentfree = freeSpace;
//        percentshare = shareSpace;
//        init();
//    }

    public DiscInfoRingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DiscInfoRingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //绘制背景色
        backCirclePaint = new Paint();
        backCirclePaint.setStyle(Paint.Style.STROKE);
        backCirclePaint.setAntiAlias(true);
        backCirclePaint.setColor(Color.LTGRAY);
        backCirclePaint.setStrokeWidth(circleBorderWidth);
//        backCirclePaint.setMaskFilter(new BlurMaskFilter(20, BlurMaskFilter.Blur.OUTER));
//绘制变色圆环
        gradientCirclePaint = new Paint();
        gradientCirclePaint.setStyle(Paint.Style.STROKE);
        gradientCirclePaint.setAntiAlias(true);
        gradientCirclePaint.setColor(Color.LTGRAY);
        gradientCirclePaint.setStrokeWidth(circleBorderWidth);
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(Color.LTGRAY);
        paint.setStrokeWidth(circleBorderWidth);
//        linePaint = new Paint();
//        linePaint.setColor(Color.WHITE);
//        linePaint.setStrokeWidth(5);
//绘制文字
        textPaint1 = new Paint();
        textPaint1.setAntiAlias(true);
        textPaint1.setTextSize(textSize1);
        textPaint1.setColor(HcApplication.getContext().getResources().getColor(R.color.netdisc_99_gray));
        textPaint2 = new Paint();
        textPaint2.setAntiAlias(true);
        textPaint2.setTextSize(textSize2);
        textPaint2.setColor(HcApplication.getContext().getResources().getColor(R.color.netdisc_33_black));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measureHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(Math.min(measureWidth, measureHeight), Math.min(measureWidth, measureHeight));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //1.绘制灰色背景圆环
        canvas.drawArc(
                new RectF(circlePadding * 2, circlePadding * 2,
                        getMeasuredWidth() - circlePadding * 2, getMeasuredHeight() - circlePadding * 2), -90, 360, false, backCirclePaint);
        //2.绘制颜色渐变圆环
        LinearGradient linearGradient = new LinearGradient(circlePadding, circlePadding,
                getMeasuredWidth() - circlePadding,
                getMeasuredHeight() - circlePadding,
                gradientColorArray, null, Shader.TileMode.MIRROR);
//        gradientCirclePaint.setShader(linearGradient);
//  gradientCirclePaint.setColor(Color.BLUE);
        gradientCirclePaint.setColor(HcApplication.getContext().getResources().getColor(R.color.netdisc_51_blue));
//        gradientCirclePaint.setShadowLayer(10, 10, 10, Color.RED);
        canvas.drawArc(
                new RectF(circlePadding * 2, circlePadding * 2,
                        getMeasuredWidth() - circlePadding * 2, getMeasuredHeight() - circlePadding * 2), -90, (float) (percentfree / 100.0) * 360, false, gradientCirclePaint);
        paint.setColor(HcApplication.getContext().getResources().getColor(R.color.netdisc_ff4_red));
//        paint.setColor(Color.RED);
        canvas.drawArc(
                new RectF(circlePadding * 2, circlePadding * 2,
                        getMeasuredWidth() - circlePadding * 2, getMeasuredHeight() - circlePadding * 2), -90, (float) (percentshare / 100.0) * 360, false, paint);

        //半径
        float radius = (getMeasuredWidth() - circlePadding * 3) / 2;
        //X轴中点坐标
        int centerX = getMeasuredWidth() / 2;

        //3.绘制100份线段，切分空心圆弧
//        for (float i = 0; i < 360; i += 3.6) {
//            double rad = i * Math.PI / 180;
//            float startX = (float) (centerX + (radius - circleBorderWidth) * Math.sin(rad));
//            float startY = (float) (centerX + (radius - circleBorderWidth) * Math.cos(rad));
//
//            float stopX = (float) (centerX + radius * Math.sin(rad) + 1);
//            float stopY = (float) (centerX + radius * Math.cos(rad) + 1);
//
//            canvas.drawLine(startX, startY, stopX, stopY, linePaint);
//        }

        //4.绘制文字
        float textWidth = textPaint1.measureText("已用/总量");
        int textHeight = (int) (Math.ceil(textPaint1.getFontMetrics().descent - textPaint1.getFontMetrics().ascent) + 2);
//        canvas.drawText(percent + "%", centerX - textWidth / 2, centerX + textHeight / 4, textPaint);
        canvas.drawText("已用/总量", centerX - textWidth / 2, centerX - textHeight / 2, textPaint1);
        float textWidth2 = textPaint2.measureText(used + "/"+total);
        int textHeight2 = (int) (Math.ceil(textPaint2.getFontMetrics().descent - textPaint2.getFontMetrics().ascent) + 2);
//        canvas.drawText(percent + "%", centerX - textWidth / 2, centerX + textHeight / 4, textPaint);
        canvas.drawText(used + "/"+total, centerX - textWidth2 / 2, centerX + textHeight2 / 2, textPaint2);
    }

    public void setInfo(long shapespace, long userspace, long totalspace) {
        long userToealSpace = shapespace + userspace;
        double useTotal;
        if (userToealSpace < 1024) {//小于1kb
            if (userspace > 0) {
                useTotal =(double) (userToealSpace);
                used = useTotal + "字节";
            } else {
                used = "1000.0G";
            }
        } else if (userToealSpace < 1024 * 1024 && userToealSpace >= 1024) {//大于1kb小于1Mb
            useTotal = userToealSpace / 1024.00;
            used = useTotal + "KB";
        } else if (userToealSpace >= 1024 * 1024 && userToealSpace < 1024 * 1024 * 1024) {//大于1MB小于1GB
            useTotal = userToealSpace / (1024.00 * 1024);
            used = useTotal + "M";
        } else {
            useTotal = userToealSpace / (1024.00 * 1024 * 1024);
            used = useTotal + "G";
        }
        double totalSpace = (double)(totalspace);
        total = totalSpace + "G";
        percentfree =  (userToealSpace*100)/(totalspace*1024*1024*1024);
        percentshare = (shapespace*100)/(totalspace*1024*1024*1024);
        postInvalidate();//刷新
    }
}
