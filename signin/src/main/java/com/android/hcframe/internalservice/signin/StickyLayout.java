/**
The MIT License (MIT)

Copyright (c) 2014 singwhatiwanna
https://github.com/singwhatiwanna
http://blog.csdn.net/singwhatiwanna

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package com.android.hcframe.internalservice.signin;

import com.android.hcframe.HcLog;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;


public class StickyLayout extends LinearLayout {
	
    private static final String TAG = "StickyLayout";

    /**
     * ************************  =======
     *                        *      |
     *     mHeaderHeight      *
     *                        *  mOriginalHeaderHeight
     * ************************
     *     mOffsetHeight      *      |
     * ************************  =======
     */
    
    
    /** header最初的高度,也可以认为是最大高度 单位：px*/
    private int mOriginalHeaderHeight;
    /** 当前的header高度 单位：px */
    private int mHeaderHeight;
    /** 固定高度 */
    private int mOffsetHeight;
    

    private int mStatus = STATUS_EXPANDED;
    public static final int STATUS_EXPANDED = 1;
    public static final int STATUS_COLLAPSED = 2;

    private int mTouchSlop;

    // 分别记录上次滑动的坐标
    private int mLastX = 0;
    private int mLastY = 0;

    // 分别记录上次滑动的坐标(onInterceptTouchEvent)
    private int mLastXIntercept = 0;
    private int mLastYIntercept = 0;

    // 用来控制滑动角度，仅当角度a满足如下条件才进行滑动：tan a = deltaX / deltaY > 2
    private static final int TAN = 2;

    private boolean mIsSticky = true;
    private boolean mDisallowInterceptTouchEventOnHeader = true;

    private onScrollListener mListener;
    
    public StickyLayout(Context context) {
        this(context, null);
    }

    public StickyLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public StickyLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mOriginalHeaderHeight = context.getResources().getDimensionPixelSize(R.dimen.sign_header_height);
        mOffsetHeight = context.getResources().getDimensionPixelSize(R.dimen.sign_offset_height);
        mHeaderHeight = mOriginalHeaderHeight - mOffsetHeight;
        if (mHeaderHeight < 0) mHeaderHeight = 0;
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        HcLog.D(TAG + " touch slop = "+mTouchSlop);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int intercepted = 0;
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN: {
            mLastXIntercept = x;
            mLastYIntercept = y;
            mLastX = x;
            mLastY = y;
            intercepted = 0;
            break;
        }
        case MotionEvent.ACTION_MOVE: {
            int deltaX = x - mLastXIntercept;
            int deltaY = y - mLastYIntercept;
//            HcLog.D(TAG + " onInterceptTouchEvent y = "+y + " deltaY = "+deltaY
//            		+ " header height = "+getHeaderHeight());
            if (mDisallowInterceptTouchEventOnHeader && y <= getHeaderHeight()) {
                intercepted = 0;
            } else if (Math.abs(deltaY) <= Math.abs(deltaX)) {
                intercepted = 0;
            } else if (mStatus == STATUS_EXPANDED && deltaY <= -mTouchSlop) {
                intercepted = 1;
            } else if (mListener != null) {
                if (mListener.onDownTouch() && deltaY >= mTouchSlop) {
                    intercepted = 1;
                }
            }
            break;
        }
        case MotionEvent.ACTION_UP: {
            intercepted = 0;
            mLastXIntercept = mLastYIntercept = 0;
            break;
        }
        default:
            break;
        }
//        HcLog.D(TAG + " onInterceptTouchEvent intercepted = " +intercepted);
        return intercepted != 0 && mIsSticky;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsSticky) {
            return true;
        }
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN: {
            break;
        }
        case MotionEvent.ACTION_MOVE: {
            int deltaX = x - mLastX;
            int deltaY = y - mLastY;

            mHeaderHeight += deltaY;
            setHeaderHeight(mHeaderHeight);
            break;
        }
        case MotionEvent.ACTION_UP: {
            // 这里做了下判断，当松开手的时候，会自动向两边滑动，具体向哪边滑，要看当前所处的位置
            int destHeight = 0;
            if (mHeaderHeight <= (mOriginalHeaderHeight - mOffsetHeight) * 0.5) {
                destHeight = 0;
                mStatus = STATUS_COLLAPSED;
            } else {
                destHeight = mOriginalHeaderHeight - mOffsetHeight;
                mStatus = STATUS_EXPANDED;
            }
            // 慢慢滑向终点
            this.smoothSetHeaderHeight(mHeaderHeight, destHeight, 500);
            break;
        }
        default:
            break;
        }
        mLastX = x;
        mLastY = y;
        return true;
    }

    private void smoothSetHeaderHeight(final int from, final int to, long duration) {
        final int frameCount = (int) (duration / 1000f * 30) + 1;
        final float partation = (to - from) / (float) frameCount;
        new Thread("Thread#smoothSetHeaderHeight") {

            @Override
            public void run() {
                for (int i = 0; i < frameCount; i++) {
                    final int height;
                    if (i == frameCount - 1) {
                        height = to;
                    } else {
                        height = (int) (from + partation * i);
                    }
                    post(new Runnable() {
                        public void run() {
                            setHeaderHeight(height);
                        }
                    });
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                
                post(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (mListener != null) {
							mListener.onCompleted();
						}
					}
				});
            };

        }.start();
    }

    private void setHeaderHeight(int height) {

        if (height <= 0) {
            height = 0;
        } else if (height > mOriginalHeaderHeight - mOffsetHeight) {
            height = mOriginalHeaderHeight - mOffsetHeight;
        }

        if (height == 0) {
            mStatus = STATUS_COLLAPSED;
        } else {
            mStatus = STATUS_EXPANDED;
        }
        mHeaderHeight = height;
        
        if (mListener != null) {
        	mListener.updateHeader(mHeaderHeight + mOffsetHeight, mHeaderHeight < (mOriginalHeaderHeight - mOffsetHeight) * 0.5);
        }
//        if (mHeader != null && mHeader.getLayoutParams() != null) {
//            mHeader.getLayoutParams().height = height;
//            mHeader.requestLayout();
//            mHeaderHeight = height;
//        } 
    }

    public int getHeaderHeight() {
        return mHeaderHeight;
    }

    public void setSticky(boolean isSticky) {
        mIsSticky = isSticky;
    }

    public void requestDisallowInterceptTouchEventOnHeader(boolean disallowIntercept) {
        mDisallowInterceptTouchEventOnHeader = disallowIntercept;
    }

    public static interface onScrollListener {
    	
    	/**
    	 * 更新头的显示高度
    	 * @author jrjin
    	 * @time 2016-1-5 上午11:04:51
    	 * @param height header的实际高度
    	 * @param visibility offset header是否可见
    	 */
    	public void updateHeader(int height, boolean visibility);
    	
    	/**
    	 * 下拉的时候是否分配给其他的view
    	 * @author jrjin
    	 * @time 2016-1-5 上午11:08:43
    	 * @return
    	 */
    	public boolean onDownTouch();
    	
    	public void onCompleted();
    }
    
    public void setOnScrollListener(onScrollListener listener) {
    	mListener = listener;
    }
}