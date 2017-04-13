/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-4-30 上午10:56:23
*/
package com.android.hcframe;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Adapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Scroller;

import java.util.ArrayList;

/**
 * 1、Item可以拖动
 * 2、上下分页
 * @author jrjin
 * @time 2015-4-30 上午10:59:18
 */
public class HcDraggableGridViewPager extends ViewGroup {

	private static final String TAG = "HcDraggableGridViewPager";
	
	private static final boolean USE_CACHE = false;
	
	private static final boolean USE_PAGE = false;
	
	// layout	
	/** 列数 */
	private static final int DEFAULT_COL_COUNT = 5;
	/** 行数 */
	private static final int DEFAULT_ROW_COUNT = 2;
	/** 子{@code View}之间的间距 */
	private static final int DEFAULT_GRID_GAP = 0; // gap between grids (dips)

	private static final int MAX_SETTLE_DURATION = 600; // ms
	/** 滑动的最短距离 */
	private static final int MIN_DISTANCE_FOR_FLING = 25; // dips
	/** 滑动的最小速度 */
	private static final int MIN_FLING_VELOCITY = 400; // dips
	
	private static final int CLOSE_ENOUGH = 2; // dp
	
	private static final int INVALID_POINTER = -1;

	/** 初始状态 */
	public static final int SCROLL_STATE_IDLE = 0;
	/** 拖动状态 */
	public static final int SCROLL_STATE_DRAGGING = 1;
	public static final int SCROLL_STATE_SETTLING = 2;

	private static final long LONG_CLICK_DURATION = 500; // ms
	private static final long ANIMATION_DURATION = 150; // ms

	private static final int EDGE_TOP = 0;
	private static final int EDGE_BOTTOM = 1;

	private static final long EDGE_HOLD_DURATION = 1200; // ms

	private int mColCount = DEFAULT_COL_COUNT;
	private int mRowCount = DEFAULT_ROW_COUNT;
	private int mPageSize = mColCount * mRowCount;
	private int mGridGap;

	private int mPageCount;
	private int mGridWidth;
	private int mGridHeight;
	/** 往下拉或者往上拉出的最大空隙，拉到最顶部或者最底部还可以拉动的距离 */
	private int mMaxOverScrollSize;
	private int mEdgeSize;

	// internal paddings
	private int mPaddingLeft;
	private int mPaddingTop;
	private int mPaddingRight;
	private int mPaddingButtom;

	/** Index of currently displayed page */
	private int mCurItem; // Index of currently displayed page.
	
	private Adapter mAdapter;

	private Scroller mScroller;

	private boolean mScrollingCacheEnabled;

	private boolean mIsBeingDragged;
	private boolean mIsUnableToDrag;
	private int mTouchSlop;

	private float mLastMotionX;
	private float mLastMotionY;
	private float mInitialMotionX;
	private float mInitialMotionY;
	private int mActivePointerId = INVALID_POINTER;

	private VelocityTracker mVelocityTracker;
	private int mMinimumVelocity;
	private int mMaximumVelocity;
	private int mFlingDistance;
	private int mCloseEnough;

	// click & long click
	private int mLastPosition = -1;
	private long mLastDownTime = Long.MAX_VALUE;

	// rearrange
	private int mLastDragged = -1;
	private int mLastTarget = -1;

	// edge holding
	private int mLastEdge = -1;
	private long mLastEdgeTime = Long.MAX_VALUE;

	private ArrayList<Integer> newPositions = new ArrayList<Integer>();

	private boolean mCalledSuper;
	
	private int mScrollState = SCROLL_STATE_IDLE;
	
	private boolean mUsePages = USE_PAGE;
	
	private OnPageChangeListener mOnPageChangeListener;
	private OnItemClickListener mOnItemClickListener;
	private OnItemLongClickListener mOnItemLongClickListener;
	private OnRearrangeListener mOnRearrangeListener;
	
	/**
	 * 定义动画效果，可以是动画的变化率，可以使存在的动画效果accelerated(加速)，decelerated(减速),repeated(重复),bounced(弹跳)等
	 */
	private static final Interpolator sInterpolator = new Interpolator() {
		public float getInterpolation(float t) {
			t -= 1.0f;
			return t * t * t * t * t + 1.0f;
		}
	};
	
	private final DataSetObserver mDataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			dataSetChanged();
		}

		@Override
		public void onInvalidated() {
			dataSetChanged();
		}
	};
	
	private final Runnable mEndScrollRunnable = new Runnable() {
		public void run() {
			setScrollState(SCROLL_STATE_IDLE);
		}
	};
	
	private Paint mDividerPaint;
	
	private Paint mBG;
	
	public HcDraggableGridViewPager(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public HcDraggableGridViewPager(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public HcDraggableGridViewPager(Context context, AttributeSet attrs,
									int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initDraggableGridViewPager();
	}

	private void initDraggableGridViewPager() {
		setWillNotDraw(false);
		setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
		setFocusable(true);
		setChildrenDrawingOrderEnabled(true);

		final Context context = getContext();
		final ViewConfiguration configuration = ViewConfiguration.get(context);
		final float density = context.getResources().getDisplayMetrics().density;

		mGridGap = (int) (DEFAULT_GRID_GAP * density);

		// internal paddings
		mPaddingLeft = getPaddingLeft();
		mPaddingTop = getPaddingTop();
		mPaddingRight = getPaddingRight();
		mPaddingButtom = getPaddingBottom();
		super.setPadding(0, 0, 0, 0);

		mScroller = new Scroller(context, sInterpolator);
		mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
		mMinimumVelocity = (int) (MIN_FLING_VELOCITY * density);
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

		mFlingDistance = (int) (MIN_DISTANCE_FOR_FLING * density);
		mCloseEnough = (int) (CLOSE_ENOUGH * density);
		
		mDividerPaint = new Paint();
		mDividerPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mDividerPaint.setColor(context.getResources().getColor(R.color.divider_bg));
		
		mBG = new Paint();
		mBG.setFlags(Paint.ANTI_ALIAS_FLAG);
		mBG.setColor(Color.WHITE);
		
		mColCount = context.getResources().getInteger(R.integer.col_count);
		mRowCount = context.getResources().getInteger(R.integer.row_count);
		mPageSize = mColCount * mRowCount;
		mUsePages = context.getResources().getBoolean(R.bool.market_use_page);
	}
	
	@Override
	protected void onDetachedFromWindow() {
		removeCallbacks(mEndScrollRunnable);
//		if (mAdapter != null) {
//			mAdapter.unregisterDataSetObserver(mDataSetObserver);
//		}
		super.onDetachedFromWindow();
	}
	
	public int getColCount() {
		return mColCount;
	}
	
	public void setColCount(int colCount) {
		if (colCount < 1) {
			colCount = 1;
		}
		mColCount = colCount;
		mPageSize = mColCount * mRowCount;
		requestLayout();
	}
	
	public int getRowCount() {
		return mRowCount;
	}

	public void setRowCount(int rowCount) {
		if (mUsePages) {
			if (rowCount < 1) {
				rowCount = 1;
			}
			mRowCount = rowCount;
			mPageSize = mColCount * mRowCount;
			requestLayout();
		} else {
			throw new UnsupportedOperationException(" the row can not set!");
		}
		
	}

	public int getGridGap() {
		return mGridGap;
	}

	public void setGridGap(int gridGap) {
		if (gridGap < 0) {
			gridGap = 0;
		}
		mGridGap = gridGap;
		requestLayout();
	}

	public int getPageCount() {
		return mPageCount;//(getChildCount() + mPageSize - 1) / mPageSize;
	}
	
	public int getGridHeight() {
		return mGridHeight;
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		final int childCount = getChildCount();
		mPageCount = mUsePages == true ? (childCount + mPageSize - 1) / mPageSize : 1;
		mGridWidth = (getWidth() - mPaddingLeft - mPaddingRight - (mColCount - 1) * mGridGap /*- mColCount + 1*/) / mColCount;
		mGridHeight = mUsePages == true ? (getHeight() - mPaddingTop - mPaddingButtom - (mRowCount - 1) * mGridGap /*- mRowCount + 1*/) / mRowCount : mGridWidth;
		mMaxOverScrollSize = 0;//mGridHeight / 2;
		mEdgeSize = mGridHeight / 2;
		newPositions.clear();
		if (!mUsePages) {
			mPageSize = mColCount * ((childCount + mColCount - 1) / mColCount);
		}
		for (int i = 0; i < childCount; i++) {
			final View child = getChildAt(i);
			final Rect rect = getRectByPosition(i);
			child.measure(MeasureSpec.makeMeasureSpec(rect.width(), MeasureSpec.EXACTLY),
					MeasureSpec.makeMeasureSpec(rect.height(), MeasureSpec.EXACTLY));
//			HcLog.D("child.layout position=" + i + ", rect=" + rect);
			child.layout(rect.left, rect.top, rect.right, rect.bottom);
			newPositions.add(-1);
		}
		if (mCurItem > 0 && mCurItem < mPageCount) {
			final int curItem = mCurItem;
			mCurItem = 0;
			setCurrentItem(curItem);
		}
	}

	/**
	 * Callback interface for responding to changing state of the selected page.
	 */
	public interface OnPageChangeListener {

		/**
		 * This method will be invoked when the current page is scrolled, either as part of a programmatically initiated
		 * smooth scroll or a user initiated touch scroll.
		 * 
		 * @param position
		 *            Position index of the first page currently being displayed. Page position+1 will be visible if
		 *            positionOffset is nonzero.
		 * @param positionOffset
		 *            Value from [0, 1) indicating the offset from the page at position.
		 * @param positionOffsetPixels
		 *            Value in pixels indicating the offset from position.
		 */
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

		/**
		 * This method will be invoked when a new page becomes selected. Animation is not necessarily complete.
		 * 
		 * @param position
		 *            Position index of the new selected page.
		 */
		public void onPageSelected(int position);

		/**
		 * Called when the scroll state changes. Useful for discovering when the user begins dragging, when the pager is
		 * automatically settling to the current page, or when it is fully stopped/idle.
		 * 
		 * @param state
		 *            The new scroll state.
		 * @see DraggableGridViewPager#SCROLL_STATE_IDLE
		 * @see DraggableGridViewPager#SCROLL_STATE_DRAGGING
		 * @see DraggableGridViewPager#SCROLL_STATE_SETTLING
		 */
		public void onPageScrollStateChanged(int state);
	}

	/**
	 * Simple implementation of the {@link OnPageChangeListener} interface with stub implementations of each method.
	 * Extend this if you do not intend to override every method of {@link OnPageChangeListener}.
	 */
	public static class SimpleOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			// This space for rent
		}

		@Override
		public void onPageSelected(int position) {
			// This space for rent
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			// This space for rent
		}
	}

	public interface OnRearrangeListener {
		public abstract void onRearrange(int oldIndex, int newIndex);
	}
	/**
	 * 
	 * @author jrjin
	 * @time 2015-4-30 下午4:30:51
	 * @param position 在{@code ViewGroup}中的位子
	 * @return
	 */
	private Rect getRectByPosition(int position) {
		final int page = position / mPageSize; // page从0开始 
		final int col = (position % mPageSize) % mColCount;
		final int row = (position % mPageSize) / mColCount;
		final int left = mPaddingLeft + col * (mGridWidth + mGridGap);
		final int top = getHeight() * page + mPaddingTop + row * (mGridHeight + mGridGap);
		return new Rect(left, top, left + mGridWidth, top + mGridHeight);
	}
	/**
	 * 
	 * @author jrjin
	 * @time 2015-4-30 下午4:42:01
	 * @param item 当前页	
	 */
	public void setCurrentItem(int item) {
		setCurrentItemInternal(item, false, false);
	}

	/**
	 * 
	 * @author jrjin
	 * @time 2015-4-30 下午4:42:29
	 * @param item 当前页
	 * @param smoothScroll  是否平滑
	 */
	public void setCurrentItem(int item, boolean smoothScroll) {
		setCurrentItemInternal(item, smoothScroll, false);
	}

	/**
	 * 
	 * @author jrjin
	 * @time 2015-4-30 下午4:43:07
	 * @param item
	 * @param smoothScroll
	 * @param always
	 */
	void setCurrentItemInternal(int item, boolean smoothScroll, boolean always) {
		setCurrentItemInternal(item, smoothScroll, always, 0);
	}

	void setCurrentItemInternal(int item, boolean smoothScroll, boolean always, int velocity) {
		if (mPageCount <= 0) {
			setScrollingCacheEnabled(false);
			return;
		}
		if (!always && mCurItem == item) {
			setScrollingCacheEnabled(false);
			return;
		}

		if (item < 0) {
			item = 0;
		} else if (item >= mPageCount) {
			item = mPageCount - 1;
		}
		final boolean dispatchSelected = mCurItem != item;
		mCurItem = item;
//		HcLog.D(TAG + " setCurrentItemInternal item = "+item + " smoothScroll = "+smoothScroll
//				+ " velocity = "+velocity + " dispatchSelected = "+dispatchSelected);
		scrollToItem(item, smoothScroll, velocity, dispatchSelected);
	}

	/**
	 * 
	 * @author jrjin
	 * @time 2015-4-30 下午4:44:56
	 * @param item 当前页面
	 * @param smoothScroll
	 * @param velocity
	 * @param dispatchSelected
	 */
	private void scrollToItem(int item, boolean smoothScroll, int velocity, boolean dispatchSelected) {
		final int destX = getWidth() * item;
		final int destY = getHeight() * item;
		if (smoothScroll) {
//			smoothScrollTo(destX, 0, velocity);
			smoothScrollTo(0, destY, velocity);
			if (dispatchSelected && mOnPageChangeListener != null) {
				mOnPageChangeListener.onPageSelected(item);
			}
		} else {
			if (dispatchSelected && mOnPageChangeListener != null) {
				mOnPageChangeListener.onPageSelected(item);
			}
			completeScroll(false);
//			scrollTo(destX, 0);
			scrollTo(0, destY);
			pageScrolled(/*destX*/destY);
		}
	}
	
	private void setScrollingCacheEnabled(boolean enabled) {
		if (mScrollingCacheEnabled != enabled) {
			mScrollingCacheEnabled = enabled;
			if (USE_CACHE) {
				final int size = getChildCount();
				for (int i = 0; i < size; ++i) {
					final View child = getChildAt(i);
					if (child.getVisibility() != GONE) {
						child.setDrawingCacheEnabled(enabled);
					}
				}
			}
		}
	}
	
	void smoothScrollTo(int x, int y, int velocity) {
		if (getChildCount() == 0) {
			// Nothing to do.
			setScrollingCacheEnabled(false);
			return;
		}
		int sx = getScrollX();
		int sy = getScrollY();
		int dx = x - sx;
		int dy = y - sy;
		if (dx == 0 && dy == 0) {
			completeScroll(false);
			setScrollState(SCROLL_STATE_IDLE);
			return;
		}

		setScrollingCacheEnabled(true);
		setScrollState(SCROLL_STATE_SETTLING);

//		final int width = getWidth();
		final int height = getHeight();
//		final int halfWidth = width / 2;
		final int halfHeight = height / 2;
		final float distanceRatio = Math.min(1f, 1.0f * Math.abs(dy) / height/*width*/);
//		final float distance = halfWidth + halfWidth *
//				distanceInfluenceForSnapDuration(distanceRatio);
		final float distance = halfHeight + halfHeight *
				distanceInfluenceForSnapDuration(distanceRatio);
		int duration = 0;
		velocity = Math.abs(velocity);
		if (velocity > 0) {
			duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
		} else {
			final float pageDelta = (float) Math.abs(dy) / height/*width*/;
			duration = (int) ((pageDelta + 1) * 100);
		}
		duration = Math.min(duration, MAX_SETTLE_DURATION);

		mScroller.startScroll(sx, sy, dx, dy, duration);
		ViewCompat.postInvalidateOnAnimation(this);
	}
	
	private void completeScroll(boolean postEvents) {
		if (mScrollState == SCROLL_STATE_SETTLING) {
			// Done with scroll, no longer want to cache view drawing.
			setScrollingCacheEnabled(false);
			mScroller.abortAnimation();
			int oldX = getScrollX();
			int oldY = getScrollY();
			int x = mScroller.getCurrX();
			int y = mScroller.getCurrY();
			if (oldX != x || oldY != y) {
				scrollTo(x, y);
			}
			if (postEvents) {
				ViewCompat.postOnAnimation(this, mEndScrollRunnable);
			} else {
				mEndScrollRunnable.run();
			}
		}
	}
	
	private boolean pageScrolled(int ypos) {
		if (mPageCount <= 0) {
			mCalledSuper = false;
			onPageScrolled(0, 0, 0);
			if (!mCalledSuper) {
				throw new IllegalStateException("onPageScrolled did not call superclass implementation");
			}
			return false;
		}
//		final int width = getWidth();
		final int height = getHeight();
		final int currentPage = ypos / height;//xpos / width;
		final int offsetPixels = ypos - currentPage * height;//xpos - currentPage * width;
		final float pageOffset = (float) offsetPixels / (float) height;//(float) offsetPixels / (float) width;

		mCalledSuper = false;
		onPageScrolled(currentPage, pageOffset, offsetPixels);
		if (!mCalledSuper) {
			throw new IllegalStateException("onPageScrolled did not call superclass implementation");
		}
		return true;
	}
	
	/**
	 * This method will be invoked when the current page is scrolled, either as part of a programmatically initiated
	 * smooth scroll or a user initiated touch scroll. If you override this method you must call through to the
	 * superclass implementation (e.g. super.onPageScrolled(position, offset, offsetPixels)) before onPageScrolled
	 * returns.
	 * 
	 * @param position
	 *            Position index of the first page currently being displayed. Page position+1 will be visible if
	 *            positionOffset is nonzero.
	 * @param offset
	 *            Value from [0, 1) indicating the offset from the page at position.
	 * @param offsetPixels
	 *            Value in pixels indicating the offset from position.
	 */
	protected void onPageScrolled(int position, float offset, int offsetPixels) {
		if (mOnPageChangeListener != null) {
			mOnPageChangeListener.onPageScrolled(position, offset, offsetPixels);
		}
		mCalledSuper = true;
	}
	
	private void setScrollState(int newState) {
		if (mScrollState == newState) {
			return;
		}
		mScrollState = newState;
		if (mOnPageChangeListener != null) {
			mOnPageChangeListener.onPageScrollStateChanged(newState);
		}
	}
	
	float distanceInfluenceForSnapDuration(float f) {
		f -= 0.5f; // center the values about 0.
		f *= 0.3f * Math.PI / 2.0f;
		return (float) Math.sin(f);
	}
	
	public int getCurrentItem() {
		return mCurItem;
	}
	
	public void setOnPageChangeListener(OnPageChangeListener listener) {
		mOnPageChangeListener = listener;
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		mOnItemClickListener = listener;
	}

	public void setOnItemLongClickListener(OnItemLongClickListener listener) {
		mOnItemLongClickListener = listener;
	}

	public void setOnRearrangeListener(OnRearrangeListener listener) {
		mOnRearrangeListener = listener;
	}
	
	void smoothScrollTo(int x, int y) {
		smoothScrollTo(x, y, 0);
	}
	
	@Override
	public void computeScroll() {
		if (!mScroller.isFinished() && mScroller.computeScrollOffset()) {
			int oldX = getScrollX();
			int oldY = getScrollY();
			int x = mScroller.getCurrX();
			int y = mScroller.getCurrY();

			if (oldX != x || oldY != y) {
				scrollTo(x, y);
				if (!pageScrolled(y)) {
					mScroller.abortAnimation();
//					scrollTo(0, y);
					scrollTo(x, 0);
				}
			}

			// Keep on drawing until the animation has finished.
			ViewCompat.postInvalidateOnAnimation(this);
			return;
		}

		// Done with scroll, clean up state.
		completeScroll(true);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
//		HcLog.D(TAG + " #onInterceptTouchEvent action = "+ev.getAction());
		/*
		 * This method JUST determines whether we want to intercept the motion. If we return true, onMotionEvent will be
		 * called and we do the actual scrolling there.
		 */

		final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;

		// Always take care of the touch gesture being complete.
		if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
			// Release the drag.
//			HcLog.D("Intercept done!");
			mIsBeingDragged = false;
			mIsUnableToDrag = false;
			mActivePointerId = INVALID_POINTER;
			if (mVelocityTracker != null) {
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}
			return false;
		}

		// Nothing more to do here if we have decided whether or not we
		// are dragging.
		if (action != MotionEvent.ACTION_DOWN) {
			if (mIsBeingDragged || mLastDragged >= 0) {
//				HcLog.D("Intercept returning true!");
				return true;
			}
			if (mIsUnableToDrag) {
//				HcLog.D("Intercept returning false!");
				return false;
			}
		}

		switch (action) {
		case MotionEvent.ACTION_MOVE: {
			/*
			 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check whether the user has moved
			 * far enough from his original down touch.
			 */

			/*
			 * Locally do absolute value. mLastMotionY is set to the y value of the down event.
			 */
			final int activePointerId = mActivePointerId;
			if (activePointerId == INVALID_POINTER) {
				// If we don't have a valid id, the touch down wasn't on content.
				break;
			}

			final int pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);
			final float x = MotionEventCompat.getX(ev, pointerIndex);
//			final float dx = x - mLastMotionX;
			final float dx = x - mInitialMotionX;
			final float xDiff = Math.abs(dx);
			final float y = MotionEventCompat.getY(ev, pointerIndex);
			final float dy = y - mLastMotionY;
//			final float yDiff = Math.abs(y - mInitialMotionY);
			final float yDiff = Math.abs(dy);
//			HcLog.D("***Moved to " + x + "," + y + " diff=" + xDiff + "," + yDiff);

			if (/*xDiff*/yDiff > mTouchSlop && /*xDiff*/yDiff * 0.5f > /*yDiff*/xDiff) {
//				HcLog.D("***Starting drag!");
				mIsBeingDragged = true;
				requestParentDisallowInterceptTouchEvent(true);
				setScrollState(SCROLL_STATE_DRAGGING);
//				mLastMotionX = dx > 0 ? mInitialMotionX + mTouchSlop :
//						mInitialMotionX - mTouchSlop;
//				mLastMotionY = y;
				mLastMotionX = x;
				mLastMotionY = dy > 0 ? mInitialMotionY + mTouchSlop :
				mInitialMotionY - mTouchSlop;;
				setScrollingCacheEnabled(true);
			} else if (/*yDiff*/xDiff > mTouchSlop) {
				// The finger has moved enough in the vertical
				// direction to be counted as a drag... abort
				// any attempt to drag horizontally, to work correctly
				// with children that have scrolling containers.
//				HcLog.D("***Unable to drag!");
				mIsUnableToDrag = true;
			}
			if (mIsBeingDragged) {
				// Scroll to follow the motion event
				if (performDrag(y)) {
					ViewCompat.postInvalidateOnAnimation(this);
				}
			}
			break;
		}

		case MotionEvent.ACTION_DOWN: {
			/*
			 * Remember location of down touch. ACTION_DOWN always refers to pointer index 0.
			 */
			mLastMotionX = mInitialMotionX = ev.getX();
			mLastMotionY = mInitialMotionY = ev.getY();
			mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
			mIsUnableToDrag = false;

			mScroller.computeScrollOffset();
			if (mScrollState == SCROLL_STATE_SETTLING &&
					Math.abs(mScroller.getFinalY() - mScroller.getCurrY()) > mCloseEnough) {
				// Let the user 'catch' the pager as it animates.
				mScroller.abortAnimation();
				mIsBeingDragged = true;
				requestParentDisallowInterceptTouchEvent(true);
				setScrollState(SCROLL_STATE_DRAGGING);
			} else {
				completeScroll(false);
				mIsBeingDragged = false;
			}

//			HcLog.D("***Down at " + mLastMotionX + "," + mLastMotionY
//					+ " mIsBeingDragged=" + mIsBeingDragged
//					+ " mIsUnableToDrag=" + mIsUnableToDrag);
			mLastDragged = -1;
			break;
		}

		case MotionEventCompat.ACTION_POINTER_UP:
			onSecondaryPointerUp(ev);
			break;
		}

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);

		/*
		 * The only time we want to intercept motion events is if we are in the drag mode.
		 */
		return mIsBeingDragged;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
//		HcLog.D(TAG + " #onTouchEvent action = "+ev.getAction() + " edge flags = "+ev.getEdgeFlags());
		if (ev.getAction() == MotionEvent.ACTION_DOWN && ev.getEdgeFlags() != 0) {
			// Don't handle edge touches immediately -- they may actually belong to one of our
			// descendants.
			return false;
		}
		/** 为了没有子View的时候也可以刷新，所以需要注释掉
		if (mPageCount <= 0) {
			// Nothing to present or scroll; nothing to touch.
			return false;
		}*/

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);

		final int action = ev.getAction();
		boolean needsInvalidate = false;

		switch (action & MotionEventCompat.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
			mScroller.abortAnimation();
			// Remember where the motion event started
			mLastMotionX = mInitialMotionX = ev.getX();
			mLastMotionY = mInitialMotionY = ev.getY();
			mActivePointerId = MotionEventCompat.getPointerId(ev, 0);

//			HcLog.D("Down at " + mLastMotionX + "," + mLastMotionY
//					+ " mIsBeingDragged=" + mIsBeingDragged
//					+ " mIsUnableToDrag=" + mIsUnableToDrag + " mScrollState "+mScrollState);

			if (!mIsBeingDragged && mScrollState == SCROLL_STATE_IDLE) {
				mLastPosition = getPositionByXY((int) mLastMotionX, (int) mLastMotionY);
			} else {
				mLastPosition = -1;
			}
			if (mLastPosition >= 0) {
				mLastDownTime = System.currentTimeMillis();
			} else {
				mLastDownTime = Long.MAX_VALUE;
			}
//			HcLog.D("Down at mLastPosition=" + mLastPosition);
			mLastDragged = -1;
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
			final float x = MotionEventCompat.getX(ev, pointerIndex);
			final float y = MotionEventCompat.getY(ev, pointerIndex);
//			HcLog.D(" MotionEvent.ACTION_MOVE mLastDragged = " + mLastDragged);
			if (mLastDragged >= 0) {
				// change draw location of dragged visual
				final View v = getChildAt(mLastDragged);
				HcLog.D(TAG + " ACTION_MOVE  child count = "+getChildCount() + " mLastDragged = "+mLastDragged + " v = "+v);
				final int l = getScrollX() + (int) x - v.getWidth() / 2;
				final int t = getScrollY() + (int) y - v.getHeight() / 2;
				v.layout(l, t, l + v.getWidth(), t + v.getHeight());
//				HcLog.D(" mScrollState = " + mScrollState);
				// check for new target hover
				if (mScrollState == SCROLL_STATE_IDLE) {
					final int target = getTargetByXY((int) x, (int) y);
					if (target != -1 && mLastTarget != target) {
						animateGap(target);
						mLastTarget = target;
//						HcLog.D("Moved to mLastTarget=" + mLastTarget);
					}
					// edge holding
					final int edge = getEdgeByXY((int) x, (int) y);
					if (mLastEdge == -1) {
						if (edge != mLastEdge) {
							mLastEdge = edge;
							mLastEdgeTime = System.currentTimeMillis();
						}
					} else {
						if (edge != mLastEdge) {
							mLastEdge = -1;
						} else {
							if ((System.currentTimeMillis() - mLastEdgeTime) >= EDGE_HOLD_DURATION) {
								performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
								triggerSwipe(edge);
								mLastEdge = -1;
							}
						}
					}
				}
			} else if (!mIsBeingDragged) {
				final float xDiff = Math.abs(x - mLastMotionX);
				final float yDiff = Math.abs(y - mLastMotionY);
//				HcLog.D("Moved to " + x + "," + y + " diff=" + xDiff + "," + yDiff);

				if (/*xDiff*/yDiff > mTouchSlop && /*xDiff > yDiff*/yDiff > xDiff) {
//					HcLog.D("Starting drag!");
					mIsBeingDragged = true;
					requestParentDisallowInterceptTouchEvent(true);
//					mLastMotionX = x - mInitialMotionX > 0 ? mInitialMotionX + mTouchSlop :
//							mInitialMotionX - mTouchSlop;
//					mLastMotionY = y;
					mLastMotionX = x;
					mLastMotionY = y - mInitialMotionY > 0 ? mInitialMotionY + mTouchSlop :
					mInitialMotionY - mTouchSlop;
					setScrollState(SCROLL_STATE_DRAGGING);
					setScrollingCacheEnabled(true);
				}
			}
			// Not else! Note that mIsBeingDragged can be set above.
			if (mIsBeingDragged) {
				// Scroll to follow the motion event
				needsInvalidate |= performDrag(y);
			} else if (mLastPosition >= 0) {
				final int currentPosition = getPositionByXY((int) x, (int) y);
//				HcLog.D("Moved to currentPosition=" + currentPosition + " mLastPosition = "+mLastPosition);
				if (currentPosition == mLastPosition) {
					if ((System.currentTimeMillis() - mLastDownTime) >= LONG_CLICK_DURATION) {
						if (onItemLongClick(currentPosition)) {
							performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
							mLastDragged = mLastPosition;
							requestParentDisallowInterceptTouchEvent(true);
							mLastTarget = -1;
							animateDragged();
							mLastPosition = -1;
						}
						mLastDownTime = Long.MAX_VALUE;
					}
				} else {
					mLastPosition = -1;
				}
			}
			break;
		}
		case MotionEvent.ACTION_UP: {
//			HcLog.D("Touch up!!! mIsBeingDragged ="+mIsBeingDragged + " mLastPosition ="+mLastPosition);
			final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
			final float x = MotionEventCompat.getX(ev, pointerIndex);
			final float y = MotionEventCompat.getY(ev, pointerIndex);

			if (mLastDragged >= 0) {
				rearrange();
			} else if (mIsBeingDragged) {
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
				int initialVelocity = (int) VelocityTrackerCompat.getYVelocity(velocityTracker, mActivePointerId);
//				final int width = getWidth();
				final int height = getHeight();
//				final int scrollX = getScrollX();
				final int scrollY = getScrollY();
				final int currentPage = scrollY / height;//scrollX / width;
				final int offsetPixels = scrollY - currentPage * height;//scrollX - currentPage * width;
				final float pageOffset = (float) offsetPixels / (float) height/*width*/;
				final int totalDelta = (int) (y - mInitialMotionY);//(int) (x - mInitialMotionX);
				int nextPage = determineTargetPage(currentPage, pageOffset, initialVelocity, totalDelta);
				setCurrentItemInternal(nextPage, true, true, initialVelocity);

				mActivePointerId = INVALID_POINTER;
				endDrag();
			} else if (mLastPosition >= 0) {
				final int currentPosition = getPositionByXY((int) x, (int) y);
//				HcLog.D("Touch up!!! currentPosition=" + currentPosition);
				if (currentPosition == mLastPosition) {
					onItemClick(currentPosition);
				}
			}
			break;
		}
		case MotionEvent.ACTION_CANCEL:
//			HcLog.D("Touch cancel!!!");
			if (mLastDragged >= 0) {
				rearrange();
			} else if (mIsBeingDragged) {
				scrollToItem(mCurItem, true, 0, false);
				mActivePointerId = INVALID_POINTER;
				endDrag();
			}
			break;
		case MotionEventCompat.ACTION_POINTER_DOWN: {
			final int index = MotionEventCompat.getActionIndex(ev);
//			final float x = MotionEventCompat.getX(ev, index);
			final float y = MotionEventCompat.getY(ev, index);
			mLastMotionY = y;
			mActivePointerId = MotionEventCompat.getPointerId(ev, index);
			break;
		}
		case MotionEventCompat.ACTION_POINTER_UP:
			onSecondaryPointerUp(ev);
			mLastMotionY = MotionEventCompat.getY(ev,
					MotionEventCompat.findPointerIndex(ev, mActivePointerId));
			break;
		}
		if (needsInvalidate) {
			ViewCompat.postInvalidateOnAnimation(this);
		}
		return true;
	}
	
	private void requestParentDisallowInterceptTouchEvent(boolean disallowIntercept) {
		final ViewParent parent = getParent();
		if (parent != null) {
			parent.requestDisallowInterceptTouchEvent(disallowIntercept);
		}
	}
	
	private boolean performDrag(float y) {
		boolean needsInvalidate = false;

		final float deltaY = mLastMotionY - y;
		mLastMotionY = y;

		float oldScrollY = getScrollY();
		float scrollY = oldScrollY + deltaY;
//		final int width = getWidth();
		final int height = getHeight();
		float leftBound = height * 0;
		float rightBound = height * (mPageCount - 1);

		if (scrollY < leftBound) {
			final float over = Math.min(leftBound - scrollY, mMaxOverScrollSize);
			scrollY = leftBound - over;
		} else if (scrollY > rightBound) {
			final float over = Math.min(scrollY - rightBound, mMaxOverScrollSize);
			scrollY = rightBound + over;
		}
		// Don't lose the rounded component
		mLastMotionY += scrollY - (int) scrollY;
//		scrollTo((int) scrollX, getScrollY());
		scrollTo(getScrollX(), (int) scrollY);
		pageScrolled((int) scrollY);

		return needsInvalidate;
	}
	
	private void onSecondaryPointerUp(MotionEvent ev) {
		final int pointerIndex = MotionEventCompat.getActionIndex(ev);
		final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
		if (pointerId == mActivePointerId) {
			// This was our active pointer going up. Choose a new
			// active pointer and adjust accordingly.
			final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
			mLastMotionY = MotionEventCompat.getY(ev, newPointerIndex);
			mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
			if (mVelocityTracker != null) {
				mVelocityTracker.clear();
			}
		}
	}
	
	private int getPositionByXY(int x, int y) {
		final int col = (x - mPaddingLeft) / (mGridWidth + mGridGap);
		final int row = (y - mPaddingTop) / (mGridHeight + mGridGap);
		if (x < mPaddingLeft || x >= (mPaddingLeft + col * (mGridWidth + mGridGap) + mGridWidth) ||
				y < mPaddingTop || y >= (mPaddingTop + row * (mGridHeight + mGridGap) + mGridHeight) ||
				col < 0 || col >= mColCount ||
				row < 0 || row >= mRowCount) {
			// touch in padding
			return -1;
		}
		final int position = mCurItem * mPageSize + row * mColCount + col;
		if (position < 0 || position >= getChildCount()) {
			// empty item
			return -1;
		}
//		HcLog.D(TAG + " getPositionByXY x = "+x + " y = "+y + " curItem = "+mCurItem);
		return position;
	}
	
	private int getTargetByXY(int x, int y) {
		final int position = getPositionByXY(x, y);
		if (position < 0) {
			return -1;
		}
		final Rect r = getRectByPosition(position);
		final int page = position / mPageSize;
		r.inset(r.width() / 4, r.height() / 4);
//		r.offset(-getWidth() * page, 0);
		r.offset(0, -getHeight() * page);
		if (!r.contains(x, y)) {
			return -1;
		}
		return position;
	}
	
	private void animateGap(int target) {
		for (int i = 0; i < getChildCount(); i++) {
			View v = getChildAt(i);
			if (i == mLastDragged) {
				continue;
			}

			int newPos = i;
			if (mLastDragged < target && i >= mLastDragged + 1 && i <= target) {
				newPos--;
			} else if (target < mLastDragged && i >= target && i < mLastDragged) {
				newPos++;
			}

			int oldPos = i;
			if (newPositions.get(i) != -1) {
				oldPos = newPositions.get(i);
			}

			if (oldPos == newPos) {
				continue;
			}

			// animate
//			HcLog.D("animateGap from=" + oldPos + ", to=" + newPos);
			final Rect oldRect = getRectByPosition(oldPos);
			final Rect newRect = getRectByPosition(newPos);
			oldRect.offset(-v.getLeft(), -v.getTop());
			newRect.offset(-v.getLeft(), -v.getTop());

			TranslateAnimation translate = new TranslateAnimation(
					oldRect.left, newRect.left,
					oldRect.top, newRect.top);
			translate.setDuration(ANIMATION_DURATION);
			translate.setFillEnabled(true);
			translate.setFillAfter(true);
			v.clearAnimation();
			v.startAnimation(translate);

			newPositions.set(i, newPos);
		}
	}
	
	private int getEdgeByXY(int x, int y) {
		if (y < mEdgeSize) {
			return EDGE_TOP;
		} else if (y >= (getHeight() - mEdgeSize)) {
			return EDGE_BOTTOM;
		}
		return -1;
	}
	
	private void triggerSwipe(int edge) {
		if (edge == EDGE_TOP && mCurItem > 0) {
			setCurrentItem(mCurItem - 1, true);
		} else if (edge == EDGE_BOTTOM && mCurItem < mPageCount - 1) {
			setCurrentItem(mCurItem + 1, true);
		}
	}
	
	private void onItemClick(int position) {
		HcLog.D("onItemClick position=" + position);
		if (mOnItemClickListener != null) {
			mOnItemClickListener.onItemClick(null, getChildAt(position), position, position / mColCount);
		}
	}

	private boolean onItemLongClick(int position) {
		HcLog.D("onItemLongClick position=" + position);
		if (mOnItemLongClickListener != null) {
			return mOnItemLongClickListener.onItemLongClick(null, getChildAt(position), position, position / mColCount);
		}
		return false;
	}
	
	private void animateDragged() {
		if (mLastDragged >= 0) {
			final View v = getChildAt(mLastDragged);
			v.setBackgroundColor(getResources().getColor(R.color.drag_item_bg));
			final Rect r = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
			r.inset(-r.width() / 20, -r.height() / 20);
			v.measure(MeasureSpec.makeMeasureSpec(r.width(), MeasureSpec.EXACTLY),
					MeasureSpec.makeMeasureSpec(r.height(), MeasureSpec.EXACTLY));
			v.layout(r.left, r.top, r.right, r.bottom);

			AnimationSet animSet = new AnimationSet(true);
			ScaleAnimation scale = new ScaleAnimation(0.9091f, 1, 0.9091f, 1, v.getWidth() / 2, v.getHeight() / 2);
			scale.setDuration(ANIMATION_DURATION);
			AlphaAnimation alpha = new AlphaAnimation(1, .5f);
			alpha.setDuration(ANIMATION_DURATION);

			animSet.addAnimation(scale);
			animSet.addAnimation(alpha);
			animSet.setFillEnabled(true);
			animSet.setFillAfter(true);

			v.clearAnimation();
			v.startAnimation(animSet);
		}
	}
	
	private void rearrange() {
		HcLog.D(TAG + " rearrange mLastDragged = "+mLastDragged);
		if (mLastDragged >= 0) {
			int count = getChildCount();
			if (mLastDragged < count)
				getChildAt(mLastDragged).setBackgroundColor(Color.TRANSPARENT);
			for (int i = 0; i < count; i++) {
				getChildAt(i).clearAnimation();
			}
			if (mLastTarget >= 0 && mLastDragged != mLastTarget) {
				final View child = getChildAt(mLastDragged);
				removeViewAt(mLastDragged);
				addView(child, mLastTarget);
				if (mOnRearrangeListener != null) {
					mOnRearrangeListener.onRearrange(mLastDragged, mLastTarget);
				}
			}
			mLastDragged = -1;
			mLastTarget = -1;
			requestLayout();
			invalidate();
		}
	}
	
	private int determineTargetPage(int currentPage, float pageOffset, int velocity, int deltaY) {
		int targetPage;
		if (Math.abs(deltaY) > mFlingDistance && Math.abs(velocity) > mMinimumVelocity) {
			targetPage = velocity > 0 ? currentPage : currentPage + 1;
		} else {
			final float truncator = currentPage >= mCurItem ? 0.4f : 0.6f;
			targetPage = (int) (currentPage + pageOffset + truncator);
		}
		return targetPage;
	}
	
	private void endDrag() {
		mIsBeingDragged = false;
		mIsUnableToDrag = false;

		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}
	
	private void dataSetChanged() {
		int childCount = getChildCount();
		int adapterCount = mAdapter.getCount();
//		HcLog.D(TAG  + " dataSetChanged childCount = "+childCount + " adapterCount = "+adapterCount);
		for (int i = 0; i < getChildCount() && i < mAdapter.getCount(); i++) {
			final View child = getChildAt(i);
			final View newChild = mAdapter.getView(i, child, this);
//			HcLog.D(TAG + " child = "+child + " newChild = "+newChild);
			if (newChild != child) {
				removeViewAt(i);
				addView(newChild, i);
			}
		}
		for (int i = getChildCount(); i < mAdapter.getCount(); i++) {
			final View child = mAdapter.getView(i, null, this);
			addView(child);
		}
//		HcLog.D(TAG  + " dataSetChanged childCount = "+getChildCount() + " adapterCount = "+adapterCount);
		while (getChildCount() > mAdapter.getCount()) {
			removeViewAt(getChildCount() - 1);
		}
	}

	public void setAdapter(Adapter adapter) {
		if (mAdapter != null) {
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
			removeAllViews();
			mCurItem = 0;
			scrollTo(0, 0);
		}
		mAdapter = adapter;
		if (mAdapter != null) {
			mAdapter.registerDataSetObserver(mDataSetObserver);
			for (int i = 0; i < mAdapter.getCount(); i++) {
				final View child = mAdapter.getView(i, null, this);
				HcLog.D(TAG + " setAdapter child = "+child);
				addView(child);
			}
		}
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		int count = getChildCount();
		int row = (count + mColCount - 1) / mColCount;		
		if (count > 0) {
			canvas.drawRect(new Rect(0, 0, getWidth(), mGridHeight * row), mBG);
		}
		
		super.onDraw(canvas);
		if (count > 0) {
			for (int i = 1; i < mColCount; i++) {
				canvas.drawLine(i * mGridWidth /*+ (i - 1)*/, 0, i * mGridWidth /*+ (i - 1)*/, row * mGridHeight, mDividerPaint);	
			}
			
//			for (int i = 1; i <= row; i++) {
//				canvas.drawLine(0, i * mGridHeight /*+ (i - 1)*/, getWidth(), i * mGridHeight /*+ (i - 1)*/, mDividerPaint);
//			}
			
			for (int i = 1; i <= row; i++) {
				canvas.drawLine(0, i * mGridHeight, getWidth(), i * mGridHeight, mDividerPaint);
			}
			
			
//			for (int i = 0; i < mPageCount; i++) {
//				canvas.drawLine(0, i * getHeight() + mGridHeight, 
//						getWidth(), i * getHeight() + mGridHeight, mDividerPaint);
//			}
		}
	}

	/**
	 * 设置GridView的高度，只使用不分页显示时调用
	 * 调用{@link #setHeight(int)}的时候还没调用
	 * {@link #onLayout(boolean, int, int, int, int)}
	 * <p></p>
	 * 导致mGridHeight = 0;所以mGridHeight = 0时需要先设置mGridHeight的值
	 * 注意：在调用这个方法的时候GridHeight = 0时，默认GridView的宽度为整个屏幕的宽度HcUtil.getScreenWidth()
	 * @see AppCategoryView#update(java.util.Observable, Object)
	 * @author jrjin
	 * @time 2015-11-27 下午2:59:04
	 * @param count
	 */
	public void setHeight(int count) {// 第一次调用这个的时候还没有调用onLayout，所以mGridHeight = 0;
		mRowCount = (count + mColCount - 1) / mColCount; // 需要更改当前的行数，不然不分页显示的化，超过一页的后面那些不能被点击.看getPositionByXY
		if (mGridWidth <= 0 || mGridHeight <= 0) {
			mGridHeight = mGridWidth = (HcUtil.getScreenWidth() - mPaddingLeft - mPaddingRight - (mColCount - 1) * mGridGap /*- mColCount + 1*/) / mColCount;
		}
		LayoutParams params = getLayoutParams();
		params.height = mGridHeight * ((count + mColCount - 1) / mColCount);		
		setLayoutParams(params);
	}
}
