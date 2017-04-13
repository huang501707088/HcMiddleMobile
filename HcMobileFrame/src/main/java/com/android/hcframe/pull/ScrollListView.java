package com.android.hcframe.pull;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

import com.android.hcframe.HcUtil;

/**
 * Created by pc on 2016/8/26.
 */
public class ScrollListView extends ListView {

    public ScrollListView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public ScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public ScrollListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
//        int topBarHeight = getContext().getResources().getDimensionPixelSize(R.dimen.top_bar_height);
//        int titleBarHeight = getContext().getResources().getDimensionPixelSize(R.dimen.market_top_bar_height);
//        int menuHeight = getContext().getResources().getDimensionPixelSize(R.dimen.menu_height);
//        int height = HcUtil.getScreenHeight() - topBarHeight - titleBarHeight - menuHeight;
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2/* height */,

                MeasureSpec.AT_MOST);

        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
