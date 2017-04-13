package com.android.hcframe.view.scroll;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-9-10 10:32.
 */
public class HcScrollView extends ScrollView {

    private OnScrollChangedListener mListener;

    public HcScrollView(Context context) {
        super(context);
    }

    public HcScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HcScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mListener != null) {
            mListener.onScrollChanged(l, t, oldl, oldt);
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
    }

    public static interface OnScrollChangedListener {

        void onScrollChanged(int x, int y, int oldX, int oldY);
    }

    public void setOnScrollChangedListener(OnScrollChangedListener listener) {
        mListener = listener;
    }
}
