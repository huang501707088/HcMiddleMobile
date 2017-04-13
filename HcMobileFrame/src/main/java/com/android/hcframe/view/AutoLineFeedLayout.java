package com.android.hcframe.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义自动换行LinearLayout LayoutParams使用android.widget.LinearLayout.LayoutParams
 *
 * @author?CB @version?创建时间：2014-5-29?下午1:43:28
 */
@SuppressLint("DrawAllocation")
public class AutoLineFeedLayout extends RadioGroup {
    private List<Integer> listX;
    private List<Integer> listY;
    private final static String TAG = "AutoLineFeedLayout";

    public AutoLineFeedLayout(Context context) {
        super(context);
    }

    public AutoLineFeedLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoLineFeedLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
    }

    /**
     * 控制子控件的换行
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();

        int width = getWidth();
        Log.i(TAG, "父控件的宽度：" + width);

        for (int j = 0; j < count; j++) {
            final View childView = getChildAt(j);
            // 获取子控件Child的宽高
            int w = childView.getMeasuredWidth();
            int h = childView.getMeasuredHeight();

            int x = listX.get(j);
            int y = listY.get(j);

            // 布局子控件
            childView.layout(x, y, x + w, y + h);

        }
    }

    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        // Default size if no limits are specified.
        int result = 500;
        if (specMode == MeasureSpec.AT_MOST) {
            // Calculate the ideal size of your control
            // within this maximum size.
            // If your control fills the available space
            // return the outer bound.
            result = specSize;
        } else if (specMode == MeasureSpec.EXACTLY) {
            // If your control can fit within these bounds return that value.
            result = specSize;
        }
        return result;
    }

    /**
     * 计算控件及子控件所占区域
     */
    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 创建测量参数
        int cellWidthSpec = MeasureSpec.makeMeasureSpec(10, MeasureSpec.UNSPECIFIED);
        int cellHeightSpec = MeasureSpec.makeMeasureSpec(10, MeasureSpec.UNSPECIFIED);
        // 记录ViewGroup中Child的总个数
        int count = getChildCount();
        Log.v("a3a3a3", count + "");
        int measuredWidth = measureWidth(widthMeasureSpec);// 计算父控件的宽度
        int left = 0;// 用来记录子空间左上角的横坐标
        int top = 0;// 用来记录子空间左上角的纵坐标
        int maxLineHeight = 0;// 用来记录当前行的最大高度

        listX = new ArrayList<Integer>();
        listY = new ArrayList<Integer>();
        int h = 0;
        for (int i = 0; i < count; i++) {
            View childView = getChildAt(i);

            // 设置子空间Child的宽高
            childView.measure(cellWidthSpec, cellHeightSpec);
            // 获取子控件Child的宽高
            int w = childView.getMeasuredWidth() + 20;
            h = childView.getMeasuredHeight() + 20;

            android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) childView
                    .getLayoutParams();

            // 获取当行控件最高高度
            if (maxLineHeight < h + params.topMargin + params.bottomMargin) {
                maxLineHeight = h + params.topMargin + params.bottomMargin;
            }
            // 判断是否需要换行
            if (left + w > measuredWidth) {
                left = 0;
                top += h;
                // top += maxLineHeight;
                maxLineHeight = 0;
            }
            // 计算并设置显示的坐标
            left += params.leftMargin;
            listX.add(left);
            listY.add(top + params.topMargin);
            // 计算下一个子控件的顶点横坐标
            left += w + params.rightMargin;

        }
        // 加上最后一行的高度
        top += h;
        // top+=maxLineHeight;

        Log.i(TAG, "(left,top):" + left + "," + top + "," + measuredWidth + "," + top);

        // 设置容器控件所占区域大小
        // 注意setMeasuredDimension和resolveSize的用法
        setMeasuredDimension(resolveSize(measuredWidth, widthMeasureSpec),
                resolveSize(top, heightMeasureSpec));
    }

    /**
     * 为控件添加边框
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        // 获取布局控件宽高
        int width = getWidth();
        int height = getHeight();
        // 创建画笔
        Paint mPaint = new Paint();
        // 设置画笔的各个属性
        // mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        // mPaint.setStrokeWidth(10);
        // mPaint.setAntiAlias(true);
        // 创建矩形框
        Rect mRect = new Rect(0, 0, 0, 0);
        // 绘制边框
        canvas.drawRect(mRect, mPaint);
        // 最后必须调用父类的方法
        super.dispatchDraw(canvas);
    }

}
