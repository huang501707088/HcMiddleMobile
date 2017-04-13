package com.android.hcframe.view.scroll;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.android.hcframe.HcLog;
import com.android.hcframe.R;

public class ScrollLayout extends ViewGroup {

    private static final String TAG = "ScrollLayout";

    private VelocityTracker mVelocityTracker;// 用于判断甩动手势

    private Scroller mScroller;// 滑动控制

    private int mCurScreen;

    private static final int DEFAULT_SCREEN = 1;

    private float mLastMotionX; // 手指移动的时候，或者手指离开屏幕的时候记录下的手指的横坐标

    private float mLastMotionY; // 手指移动的时候，或者手指离开屏幕的时候记录下的手指的纵坐标

    private int mTouchSlop;// 手指移动的最小距离的判断标准

    // 在viewpapper中就是依赖于这个值来判断用户
    // 手指滑动的距离是否达到界面滑动的标准
    private static final int SNAP_VELOCITY = 600;// 默认的滚动速度,之后用于和手指滑动产生的速度比较,获取屏幕滚动的速度

    private static final int TOUCH_STATE_REST = 0;//表示触摸状态为空闲,即没有触摸或者手指离开了

    private static final int TOUCH_STATE_SCROLLING = 1;//表示手指正在移动

    private int mTouchState = TOUCH_STATE_REST;// 当前手指的事件状态

    private OnViewChangeListener mOnViewChangeListener;

    /** 是否开始滑动 */
    private boolean mScrolled;

    public ScrollLayout(Context context) {
        this(context, null);
    }

    public ScrollLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScrollLayout, defStyle, 0);
        mCurScreen = a.getInt(R.styleable.ScrollLayout_default_screen, DEFAULT_SCREEN);
        a.recycle();
    }

    private void init(Context context) {
        mScroller = new Scroller(context);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop(); // 使用系统默认的值
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 为每一个孩子设置它们的位置
        HcLog.D(TAG + " #onLayout changed!!!!!!!!!!!!");
        int childLeft = 0;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View childView = getChildAt(i);
            if (childView.getVisibility() != View.GONE) {
                // 此处获取到的宽度就是在onMeasure中设置的值
                final int childWidth = childView.getMeasuredWidth();
                // 为每一个子View布局
                childView.layout(childLeft, 0, childLeft + childWidth, childView.getMeasuredHeight());
                childLeft = childLeft + childWidth;
            }
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 在onlayout之前执行，获取View申请的大小，把它们保存下来，方面后面使用
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException(
                    "ScrollLayout width only can run at EXACTLY mode!");
        }

        final int heightModed = MeasureSpec.getMode(heightMeasureSpec);
        if (heightModed != MeasureSpec.EXACTLY) {
            throw new IllegalStateException(
                    "ScrollLayout height only can run at EXACTLY mode!");
        }

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
        if (mCurScreen <= count - 1) {
            scrollTo(mCurScreen * width, 0);
            HcLog.D(TAG + " #onMeasure getScrollX = "+getScrollX());
        }

    }

    /**
     * 让界面跟着手指移动到手指移动的地点
     */
    public void snapToDestination() {
        final int screenWidth = getWidth();// 子view的宽度，此例中为他适配的父view的宽度
        final int destScreen = (getScrollX() + screenWidth / 2) / screenWidth;
        snapToScreen(destScreen);
    }

    /**
     * 滚动到指定screen
     */
    public void snapToScreen(int whichScreen) {
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));// 获取要滚动到的目标screen
        HcLog.D(TAG + " #snapToScreen scrollX = "+getScrollX() + " screen = "+whichScreen);
        if (getScrollX() != (whichScreen * getWidth())) {
            final int delta = whichScreen * getWidth() - getScrollX();// 获取屏幕移到目的view还需要移动多少距离
            mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta) * 2);// 使用Scroller辅助滚动，让滚动变得更平滑
            mCurScreen = whichScreen;
            mScrolled = true;
            invalidate();// 重绘界面
            if (mOnViewChangeListener != null) {
                mOnViewChangeListener.OnViewChange(mCurScreen);
            }
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {//computeScrollOffset  方法会一直返回false，但当动画执行完成后会返回返加true.
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        } else {
//            HcLog.D(TAG + " #computeScroll scroll finish current scrollX = "+getScrollX());
            if (mScrolled) {
                mScrolled = false;
                if (mOnViewChangeListener != null) {
                    mOnViewChangeListener.onComputeScroll(mCurScreen);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        final int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();
//        HcLog.D(TAG + " #onTouchEvent getScrollX = "+getScrollX());
        switch (action) {
            case MotionEvent.ACTION_DOWN://1,终止滚动2,获取最后一次事件的x值

                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mLastMotionX = x;
                //---------------New Code----------------------
                mLastMotionY = y;
                //---------------------------------------------
                break;
            case MotionEvent.ACTION_MOVE://1,获取最后一次事件的x值2,滚动到指定位置

                int deltaX = (int) (mLastMotionX - x);

                //---------------New Code----------------------
                int deltaY = (int) (mLastMotionY - y);
                if(Math.abs(deltaX) < 200 && Math.abs(deltaY) > 10)
                    break;
                mLastMotionY = y;
                //-------------------------------------
//                HcLog.D(TAG + " #onTouchEvent ACTION_MOVE deltaX ="+deltaX + " getScrollX = "+getScrollX());
                if (isCanMove(deltaX)) {
//                    HcLog.D(TAG + " #onTouchEvent ACTION_MOVE deltaX ="+deltaX);
                    mLastMotionX = x;
                    scrollBy(deltaX, 0);
                }

                break;
            case MotionEvent.ACTION_UP://1,计算手指移动的速度并得出我们需要的速度2,选择不同情况下滚动到哪个screen

                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000);
                int velocityX = (int) velocityTracker.getXVelocity();
                if (velocityX > SNAP_VELOCITY && mCurScreen > 0) {
                    snapToScreen(mCurScreen - 1);
                } else if (velocityX < -SNAP_VELOCITY
                        && mCurScreen < getChildCount() - 1) {
                    snapToScreen(mCurScreen + 1);
                } else {
                    snapToDestination();
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }

                mTouchState = TOUCH_STATE_REST;
                break;
            case MotionEvent.ACTION_CANCEL://1,设置触摸事件状态为空闲
                mTouchState = TOUCH_STATE_REST;
                break;
            default:
                break;
        }
        return true;
    }

    private boolean isCanMove(int deltaX) {
        if (getScrollX() <= 0 && deltaX < 0) {
            return false;
        }
        if (getScrollX() >= (getChildCount() - 1) * getWidth() && deltaX > 0) {
            return false;
        }
        return true;
    }

    public void setOnViewChangeListener(OnViewChangeListener listener) {
        mOnViewChangeListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        final int action = ev.getAction();
        // 如果
        if ((action == MotionEvent.ACTION_MOVE)
                && mTouchState != TOUCH_STATE_REST) {
            return true;
        }

        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:// 判断滚动是否停止
                mLastMotionX = x;
                mLastMotionY = y;
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
                        : TOUCH_STATE_SCROLLING;
                break;
            case MotionEvent.ACTION_MOVE:// 判断是否达成滚动条件
                final int xDiff = (int) Math.abs(mLastMotionX - x);
                if (xDiff > mTouchSlop) {
                    mTouchState = TOUCH_STATE_SCROLLING;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:// 把状态调整为空闲
                mTouchState = TOUCH_STATE_REST;
                break;

        }
        // 如果屏幕没有在滚动那么就不消耗这个touch事件
        return mTouchState != TOUCH_STATE_REST;
    }

    public interface OnViewChangeListener {
        /**
         * 当前切换的页面,此时页面并未切换完成
          * @param position 当前需要显示的页面索引
         */
        public void OnViewChange(int position);

        /**
         * 页面切换完成
         * @param position 当前页面的索引
         */
        public void onComputeScroll(int position);
    }

    /**
     * 设置当前的需要显示页面的索引值
     * @param position 页面的索引值
     */
    public void setCurrentScreen(int position) {
        mCurScreen = position;
    }
}
