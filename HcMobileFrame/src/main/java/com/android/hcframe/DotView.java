package com.android.hcframe;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

public class DotView extends ViewGroup{
	
	private static final String TAG = "DotView";
	/**
	 * 总的页数
	 */
    private int mTotalItems;
    /**
     * 当前的页数
     */
    private int mCurrentItem;
    /**
     * dot的资源
     */
    private int mDotDrawableId;
    
    private int mDotWidth;
    
    private int mGravity = GRAVITY_CENTER;
    
    public static final int GRAVITY_LEFT = 0;
    public static final int GRAVITY_CENTER = 1;
    public static final int GRAVITY_RIGHT = 2;
    
    public DotView(Context context) {
        this(context, null);
    }
    
    public DotView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public DotView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initPager();
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.DotView, defStyle, 0);
		mDotDrawableId = a.getResourceId(R.styleable.DotView_dot_id, R.drawable.pager_dots);
		a.recycle();
		mDotWidth = getResources().getDrawable(mDotDrawableId).getIntrinsicWidth();
	}
    
    private void initPager(){
        setFocusable(false);
        setWillNotDraw(false);
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(mTotalItems <= 0) return;
        createLayout();
    }
    /**
     * 更新点的变化
     * @author jrjin
     * @time 2015-12-11 上午10:34:41
     */
    private void updateLayout() {
        for(int i = 0; i < mTotalItems; i++) {
            final ImageView img = (ImageView) getChildAt(i);
            TransitionDrawable tmp = (TransitionDrawable)img.getDrawable();
            if(i == mCurrentItem) {
                tmp.startTransition(50);
            } else {
                tmp.resetTransition();
            }
        }
    }
    
    private void createLayout() {
        detachAllViewsFromParent();
        int dotWidth = mDotWidth;
        HcLog.D("DotView createLayout dotWidth = "+dotWidth + " width = "+getWidth());
        int separation = dotWidth;
        int marginLeft;
        switch (mGravity) {
		case GRAVITY_LEFT:
			marginLeft = (int) (10 * HcUtil.getScreenDensity());
			break;
		case GRAVITY_RIGHT:
			marginLeft = getWidth() - (mTotalItems * dotWidth + ((mTotalItems - 1) * separation)) - (int) (10 * HcUtil.getScreenDensity());
			break;
		default:
			marginLeft = getWidth() / 2 - ((mTotalItems * dotWidth / 2) + (((mTotalItems - 1) * separation) / 2));
			break;
		}
//        int marginLeft = getWidth() / 2 - ((mTotalItems * dotWidth / 2) + (((mTotalItems - 1) * separation) / 2));
        int marginTop = getHeight() / 2 - dotWidth / 2;
        ViewGroup.LayoutParams p;
        p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        for(int i = 0; i < mTotalItems; i++) {
        	ImageView dot = new ImageView(getContext());
            TransitionDrawable td;
            td = (TransitionDrawable) getResources().getDrawable(mDotDrawableId);
            td.setCrossFadeEnabled(true);
            dot.setImageDrawable(td);
            dot.setLayoutParams(p);
            int childHeightSpec = getChildMeasureSpec(
                    MeasureSpec.makeMeasureSpec(dotWidth, MeasureSpec.UNSPECIFIED), 0, p.height);
            int childWidthSpec = getChildMeasureSpec(
                    MeasureSpec.makeMeasureSpec(dotWidth, MeasureSpec.EXACTLY), 0, p.width);
            dot.measure(childWidthSpec, childHeightSpec);
            
            int left = marginLeft + i * (dotWidth + separation);
            
            
            dot.layout(left, marginTop, left + dotWidth, marginTop + dotWidth);
            addViewInLayout(dot, getChildCount(), p, true);
            if(i == mCurrentItem){
                TransitionDrawable tmp = (TransitionDrawable) dot.getDrawable();
                tmp.startTransition(200);
            }
        }
        postInvalidate();
    }
    
    public int getTotalItems() {
        return mTotalItems;
    }

    public void setTotalItems(int totalItems) {
        if(totalItems != mTotalItems){
            mTotalItems = totalItems;
            createLayout();
        }
    }

    public int getCurrentItem() {
        return mCurrentItem;
    }

    public void setCurrentItem(int currentItem) {
        if(currentItem != mCurrentItem){
            mCurrentItem = currentItem;
            updateLayout();
        }
    }

    public void setDotWidth(int width) {
    	if (mDotWidth != width) {
    		mDotWidth = width;
        	createLayout();
    	}
    }
    
    public void setDotDrawableId(int resId) {
    	if (mDotDrawableId != resId) {
    		mDotDrawableId = resId;
        	createLayout();
    	}
    }
    
    public void setGravity(int gravity) {
    	if (mGravity != gravity) {
    		mGravity = gravity;
    		createLayout();
    	}
    }
}
