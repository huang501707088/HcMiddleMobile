/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-28 下午12:25:01
*/
package com.android.hcframe.ad;

import com.android.hcframe.HcLog;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

public class LoopViewPager extends ViewPager {

	private static final String TAG = "LoopViewPager";
	
	private boolean mCanLoop = true;
	
	private LoopPagerAdapter mAdapter;
	
	private OnPageChangeListener mOuterPageChangeListener;
	
	private ViewPagerScroller mScroller;
	
	public LoopViewPager(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}

	public LoopViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	public void setLoopAdapter(LoopPagerAdapter adapter) {
        mAdapter = adapter;
        mCanLoop = adapter.canLoop();
        setAdapter(mAdapter);
        setCurrentItem(0, false);
    }
	
	@Override
    public int getCurrentItem() {
        return mAdapter != null ? mAdapter.toRealPosition(super.getCurrentItem()) : 0;
    }

    /**
     *
     * @param item 要选择的页面索引,如果设置成轮播,则实际的索引为item + 1
     * @param smoothScroll 是否平滑移动到指定的页面
     */
    public void setCurrentItem(int item, boolean smoothScroll) {
        int realItem = 0;
        try {
            realItem = mAdapter.toInnerPosition(item);
        } catch (NullPointerException e){
        	HcLog.D(TAG + " #setCurrentItem NullPointerException postion = "+item);
        }
        super.setCurrentItem(realItem, smoothScroll);
    }

    @Override
    public void setCurrentItem(int item) {
        if (getCurrentItem() != item) {
            setCurrentItem(item, true);
        }
    }
    
    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mOuterPageChangeListener = listener;
    }
    
    private void init() {
        super.setOnPageChangeListener(onPageChangeListener);
    }
    
    private OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {
        private float mPreviousOffset = -1;
        private float mPreviousPosition = -1;

        @Override
        public void onPageSelected(int position) {
            int realPosition = mAdapter.toRealPosition(position);
            if (mPreviousPosition != realPosition) {
                mPreviousPosition = realPosition;
                if (mOuterPageChangeListener != null) {
                    mOuterPageChangeListener.onPageSelected(realPosition);
                }
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {
            int realPosition = position;
            if (mAdapter != null) {
                realPosition = mAdapter.toRealPosition(position);
                if (mCanLoop && positionOffset == 0
                        && mPreviousOffset == 0
                        && (position == 0 || position == mAdapter.getCount() - 1)) {
                    setCurrentItem(realPosition, false);
                }
                
                mPreviousOffset = positionOffset;
                if (mOuterPageChangeListener != null) {
                    if (realPosition != mAdapter.getRealCount() - 1) {
                        mOuterPageChangeListener.onPageScrolled(realPosition,
                                positionOffset, positionOffsetPixels);
                    } else {
                        if (positionOffset > .5) {
                            mOuterPageChangeListener.onPageScrolled(0, 0, 0);
                        } else {
                            mOuterPageChangeListener.onPageScrolled(realPosition,
                                    0, 0);
                        }
                    }
                }
            }

            
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (mAdapter != null) {
                int position = LoopViewPager.super.getCurrentItem();
                int realPosition = mAdapter.toRealPosition(position);

                if (mCanLoop && state == ViewPager.SCROLL_STATE_IDLE
                        && (position == 0 || position == mAdapter.getCount() - 1)) {
                  
                	//如果是0或者最后一个View，为了无限循环,滚动结束会预先跳到相反的View，如0跳最后，最后跳0
                    //为了看起来界面没有变化则改变滚动速度，让肉眼看不到是滚动了的。
                    mScroller.setZero(true);
                    setCurrentItem(realPosition, true);//如果为false，就不会刷新视图，也就出现第一次加载的时候往前滚，会有空白View。
                    mScroller.setZero(false);
                }
            }
            if (mOuterPageChangeListener != null) {
                mOuterPageChangeListener.onPageScrollStateChanged(state);
            }
        }
    };
    
    public boolean isCanLoop() {
        return mCanLoop;
    }
    
    public void setScroller(ViewPagerScroller scroller) {
        mScroller = scroller;
    }
}
