package com.android.hcframe.view.gallery;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-11-30 10:18.
 */

public class RecyclerViewDivider extends RecyclerView.ItemDecoration {

    private Drawable mDivider;
    private int mDividerHeight = 1;//分割线高度，默认为1px
    /**
     * 列表的方向
     * @see android.support.v7.widget.LinearLayoutManager#HORIZONTAL
     * @see android.support.v7.widget.LinearLayoutManager#VERTICAL
     */
    private int mOrientation;
    /**
     * 是否画最后一条分割线
     * */
    private boolean mDrawLast;

    private static final int[] ATTRS = new int[]{android.R.attr.listDivider}; //我们通过获取系统属性中的listDivider来添加，在系统中的AppTheme中设置


    /**
     * 默认分割线：高度为1dip，颜色为灰色
     *
     * @param context
     * @param orientation 列表方向
     * @param drawLast 是否显示最后一条分割线
     */
    public RecyclerViewDivider(Context context, int orientation, boolean drawLast) {
        setOrientation(orientation);
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();
        mDividerHeight = (int) (mDivider.getIntrinsicHeight() * HcUtil.getScreenDensity());
        HcLog.D("RecyclerViewDivider mDividerHeight = "+mDividerHeight);
        mDrawLast = drawLast;
    }

    /**
     * 自定义分割线
     *
     * @param context
     * @param orientation 列表方向
     * @param drawableId  分割线图片
     * @param drawLast 是否显示最后一条分割线
     */
    public RecyclerViewDivider(Context context, int orientation, int drawableId, boolean drawLast) {
        setOrientation(orientation);
        mDivider = ContextCompat.getDrawable(context, drawableId);
        mDividerHeight = mDivider.getIntrinsicHeight();
        HcLog.D("RecyclerViewDivider mDividerHeight = "+mDividerHeight);
        mDrawLast = drawLast;
    }


    //画横线, 这里的parent其实是显示在屏幕显示的这部分
    public void drawHorizontalLine(Canvas c, RecyclerView parent, RecyclerView.State state){
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();
        final int childCount = mDrawLast ? parent.getChildCount() : parent.getChildCount() - 1;
        for (int i = 0; i < childCount; i++){
            final View child = parent.getChildAt(i);

            //获得child的布局信息
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)child.getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + mDividerHeight;
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    //画竖线
    public void drawVerticalLine(Canvas c, RecyclerView parent, RecyclerView.State state){
        int top = parent.getPaddingTop();
        int bottom = parent.getHeight() - parent.getPaddingBottom();
        final int childCount = mDrawLast ? parent.getChildCount() : parent.getChildCount() - 1;
        for (int i = 0; i < childCount; i++){
            final View child = parent.getChildAt(i);

            //获得child的布局信息
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)child.getLayoutParams();
            final int left = child.getRight() + params.rightMargin;
            final int right = left + mDividerHeight;
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if(mOrientation == LinearLayoutManager.HORIZONTAL){
            //画竖线，就是往右偏移一个分割线的宽度
            outRect.set(0, 0, mDividerHeight, 0);
        }else {
            //画横线，就是往下偏移一个分割线的高度
            outRect.set(0, 0, 0, mDividerHeight);

        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == LinearLayoutManager.HORIZONTAL){
            drawVerticalLine(c, parent, state);
        }else {
            drawHorizontalLine(c, parent, state);
        }
    }

    //设置屏幕的方向
    private void setOrientation(int orientation){
        if (orientation != LinearLayoutManager.HORIZONTAL && orientation != LinearLayoutManager.VERTICAL) {
            throw new IllegalArgumentException("invalid orientation error");        }
        mOrientation = orientation;
    }
}
