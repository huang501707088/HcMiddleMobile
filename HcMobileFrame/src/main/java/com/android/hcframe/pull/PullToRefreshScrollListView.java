package com.android.hcframe.pull;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListAdapter;

import com.android.hcframe.HcLog;
import com.android.hcframe.R;

/**
 * Created by pc on 2016/8/26.
 */
public class PullToRefreshScrollListView extends PullToRefreshAdapterViewBase<ScrollListView> {

    private LoadingLayout mHeaderLoadingView;
    private LoadingLayout mFooterLoadingView;

    private FrameLayout mLvFooterLoadingFrame;

    private boolean mListViewExtrasEnabled;
    /** 下拉显示的提示 */
    private String mPullDownLable;
    /** 下拉释放的提示 */
    private String mPullDownReleaseLable;
    /** 上拉显示的提示 */
    private String mPullUpLable;
    /** 上拉释放的提示 */
    private String mPullUpReleaseLable;

    private boolean mResetLable = false;

    public PullToRefreshScrollListView(Context context) {
        super(context);
    }

    public PullToRefreshScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullToRefreshScrollListView(Context context, Mode mode) {
        super(context, mode);
    }

    public PullToRefreshScrollListView(Context context, Mode mode, AnimationStyle style) {
        super(context, mode, style);
    }

    @Override
    public final Orientation getPullToRefreshScrollDirection() {
        return Orientation.VERTICAL;
    }

    @Override
    protected void onRefreshing(final boolean doScroll) {
        HcLog.D("PullToRefreshListView #onRefreshing doScroll = " + doScroll);
        /**
         * If we're not showing the Refreshing view, or the list is empty, the
         * the header/footer views won't show so we use the normal method.
         */
        ListAdapter adapter = mRefreshableView.getAdapter();
        if (!mListViewExtrasEnabled || !getShowViewWhileRefreshing() || null == adapter || adapter.isEmpty()) {
            super.onRefreshing(doScroll);
            return;
        }

        super.onRefreshing(false);

        final LoadingLayout origLoadingView, listViewLoadingView, oppositeListViewLoadingView;
        final int selection, scrollToY;

        switch (getCurrentMode()) {
            case MANUAL_REFRESH_ONLY:
            case PULL_FROM_END:
                origLoadingView = getFooterLayout();
                listViewLoadingView = mFooterLoadingView;
                oppositeListViewLoadingView = mHeaderLoadingView;
                selection = mRefreshableView.getCount() - 1;
                scrollToY = getScrollY() - getFooterSize();
                break;
            case PULL_FROM_START:
            default:
                origLoadingView = getHeaderLayout();
                listViewLoadingView = mHeaderLoadingView;
                oppositeListViewLoadingView = mFooterLoadingView;
                selection = 0;
                scrollToY = getScrollY() + getHeaderSize();
                break;
        }

        // Hide our original Loading View
        origLoadingView.reset();
        origLoadingView.hideAllViews();

        // Make sure the opposite end is hidden too
        oppositeListViewLoadingView.setVisibility(View.GONE);

        // Show the ListView Loading View and set it to refresh.
        listViewLoadingView.setVisibility(View.VISIBLE);
        listViewLoadingView.refreshing();

        if (doScroll) {
            // We need to disable the automatic visibility changes for now
            disableLoadingLayoutVisibilityChanges();

            // We scroll slightly so that the ListView's header/footer is at the
            // same Y position as our normal header/footer
            setHeaderScroll(scrollToY);

            // Make sure the ListView is scrolled to show the loading
            // header/footer
            mRefreshableView.setSelection(selection);

            // Smooth scroll as normal
            smoothScrollTo(0);
        }
    }

    @Override
    protected void onReset() {
        /**
         * If the extras are not enabled, just call up to super and return.
         */
        if (!mListViewExtrasEnabled) {
            super.onReset();
            return;
        }

        final LoadingLayout originalLoadingLayout, listViewLoadingLayout;
        final int scrollToHeight, selection;
        final boolean scrollLvToEdge;

        switch (getCurrentMode()) {
            case MANUAL_REFRESH_ONLY:
            case PULL_FROM_END:
                originalLoadingLayout = getFooterLayout();
                listViewLoadingLayout = mFooterLoadingView;
                selection = mRefreshableView.getCount() - 1;
                scrollToHeight = getFooterSize();
                scrollLvToEdge = Math.abs(mRefreshableView.getLastVisiblePosition() - selection) <= 1;
                break;
            case PULL_FROM_START:
            default:
                originalLoadingLayout = getHeaderLayout();
                listViewLoadingLayout = mHeaderLoadingView;
                scrollToHeight = -getHeaderSize();
                selection = 0;
                scrollLvToEdge = Math.abs(mRefreshableView.getFirstVisiblePosition() - selection) <= 1;
                break;
        }

        // If the ListView header loading layout is showing, then we need to
        // flip so that the original one is showing instead
        if (listViewLoadingLayout.getVisibility() == View.VISIBLE) {

            // Set our Original View to Visible
            originalLoadingLayout.showInvisibleViews();

            // Hide the ListView Header/Footer
            listViewLoadingLayout.setVisibility(View.GONE);

            /**
             * Scroll so the View is at the same Y as the ListView
             * header/footer, but only scroll if: we've pulled to refresh, it's
             * positioned correctly
             */
            if (scrollLvToEdge && getState() != State.MANUAL_REFRESHING) {
                mRefreshableView.setSelection(selection);
                setHeaderScroll(scrollToHeight);
            }
        }

        // Finally, call up to super
        super.onReset();
    }

    @Override
    protected LoadingLayoutProxy createLoadingLayoutProxy(final boolean includeStart, final boolean includeEnd) {
        LoadingLayoutProxy proxy = super.createLoadingLayoutProxy(includeStart, includeEnd);
        HcLog.D("PullToRefreshListView #createLoadingLayoutProxy mListViewExtrasEnabled============="+mListViewExtrasEnabled);
        if (mListViewExtrasEnabled) {
            final Mode mode = getMode();

            if (includeStart && mode.showHeaderLoadingLayout()) {
                proxy.addLayout(mHeaderLoadingView);
            }
            if (includeEnd && mode.showFooterLoadingLayout()) {
                proxy.addLayout(mFooterLoadingView);
            }
        }

        return proxy;
    }

    protected ScrollListView createListView(Context context, AttributeSet attrs) {
        final ScrollListView lv;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            lv = new InternalListViewSDK9(context, attrs);
        } else {
            lv = new InternalListView(context, attrs);
        }
        return lv;
    }

    @Override
    protected ScrollListView createRefreshableView(Context context, AttributeSet attrs) {
        ScrollListView lv = createListView(context, attrs);

        // Set it to this so it can be used in ListActivity/ListFragment
        lv.setId(android.R.id.list);
        return lv;
    }

    @Override
    protected void handleStyledAttributes(TypedArray a) {
        super.handleStyledAttributes(a);

        mListViewExtrasEnabled = a.getBoolean(R.styleable.PullToRefresh_ptrListViewExtrasEnabled, true);
        HcLog.D("PullToRefreshListView #handleStyledAttributes mListViewExtrasEnabled============="+mListViewExtrasEnabled);
        if (mListViewExtrasEnabled) {
            final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);

            // Create Loading Views ready for use later
            FrameLayout frame = new FrameLayout(getContext());
            mHeaderLoadingView = createLoadingLayout(getContext(), Mode.PULL_FROM_START, a);
            mHeaderLoadingView.setVisibility(View.GONE);
            frame.addView(mHeaderLoadingView, lp);
            mRefreshableView.addHeaderView(frame, null, false);

            mLvFooterLoadingFrame = new FrameLayout(getContext());
            mFooterLoadingView = createLoadingLayout(getContext(), Mode.PULL_FROM_END, a);
            mFooterLoadingView.setVisibility(View.GONE);
            mLvFooterLoadingFrame.addView(mFooterLoadingView, lp);

            /**
             * If the value for Scrolling While Refreshing hasn't been
             * explicitly set via XML, enable Scrolling While Refreshing.
             */
            if (!a.hasValue(R.styleable.PullToRefresh_ptrScrollingWhileRefreshingEnabled)) {
                setScrollingWhileRefreshingEnabled(true);
            }
        }
    }

    @TargetApi(9)
    final class InternalListViewSDK9 extends InternalListView {

        public InternalListViewSDK9(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX,
                                       int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {

            final boolean returnValue = super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX,
                    scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);

            // Does all of the hard work...
            OverscrollHelper.overScrollBy(PullToRefreshScrollListView.this, deltaX, scrollX, deltaY, scrollY, isTouchEvent);

            return returnValue;
        }
    }

    protected class InternalListView extends ScrollListView implements EmptyViewMethodAccessor {

        private boolean mAddedLvFooter = false;

        public InternalListView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            /**
             * This is a bit hacky, but Samsung's ListView has got a bug in it
             * when using Header/Footer Views and the list is empty. This masks
             * the issue so that it doesn't cause an FC. See Issue #66.
             */
            try {
                super.dispatchDraw(canvas);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            /**
             * This is a bit hacky, but Samsung's ListView has got a bug in it
             * when using Header/Footer Views and the list is empty. This masks
             * the issue so that it doesn't cause an FC. See Issue #66.
             */
            try {
                return super.dispatchTouchEvent(ev);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public void setAdapter(ListAdapter adapter) {
            // Add the Footer View at the last possible moment
            if (null != mLvFooterLoadingFrame && !mAddedLvFooter) {
                addFooterView(mLvFooterLoadingFrame, null, false);
                mAddedLvFooter = true;
            }

            super.setAdapter(adapter);
        }

        @Override
        public void setEmptyView(View emptyView) {
            PullToRefreshScrollListView.this.setEmptyView(emptyView);
        }

        @Override
        public void setEmptyViewInternal(View emptyView) {
            super.setEmptyView(emptyView);
        }

    }

    /**
     * 增加ListView的头
     * 注意：{@link #handleStyledAttributes} 也有设置,要是有问题需要到这里更改
     * @author jrjin
     * @time 2015-12-29 下午1:34:24
     * @param v
     * @param data
     * @param isSelectable
     * 添加header时调用的addHeaderView方法必须放在listview.setadapter前面，意思很明 确就是如果想给listview添加头部则必须在给其绑定adapter前添加，否则会报错。原因是当我们在调用setAdapter方法时会 android会判断当前listview是否已经添加header，如果已经添加则会生成一个新的tempadapter，这个新的 tempadapter包含我们设置的adapter所有内容以及listview的header和footer。所以当我们在给listview添加了 header后在程序中调用listview.getadapter时返回的是tempadapter而不是我们通过setadapter传进去的 adapter。如果没有设置adapter则tempadapter与我们自己的adapter是一样的。 listview.getadapter().getcount()方法返回值会比我们预期的要大，原因是添加了header
     * <p></p>
     * 接着上面的tempadapter说，我们自定义adapter里面的getitem方法里面返回的position是不包括header的，是我们自定义adapter中数据position编号从0开始，也就是说与我们传进去的list的位置是一样的
     */
    public void addHeaderView(View v, Object data, boolean isSelectable) {
        mRefreshableView.addHeaderView(v, data, isSelectable);
    }

    public int getFirstVisiblePosition() {
        return mRefreshableView.getFirstVisiblePosition();
    }

    @Override
    protected void onPullToRefresh() {
        // TODO Auto-generated method stub
        if (mResetLable) {
            switch (getCurrentMode()) {
                case MANUAL_REFRESH_ONLY:
                case PULL_FROM_END:
                    if (!TextUtils.isEmpty(mPullUpLable)) {
                        getFooterLayout().setPullLabel(mPullUpLable);
                    }
                    break;
                case PULL_FROM_START:
                default:
                    if (!TextUtils.isEmpty(mPullDownLable)) {
                        getHeaderLayout().setPullLabel(mPullDownLable);
                    }
                    break;
            }
        }

        super.onPullToRefresh();
    }

    @Override
    protected void onReleaseToRefresh() {
        // TODO Auto-generated method stub
        if (mResetLable) {
            switch (getCurrentMode()) {
                case MANUAL_REFRESH_ONLY:
                case PULL_FROM_END:
                    if (!TextUtils.isEmpty(mPullUpReleaseLable)) {
                        getFooterLayout().setReleaseLabel(mPullUpReleaseLable);
                    }
                    break;
                case PULL_FROM_START:
                default:
                    if (!TextUtils.isEmpty(mPullDownReleaseLable)) {
                        getHeaderLayout().setReleaseLabel(mPullDownReleaseLable);
                    }
                    break;
            }
        }
        super.onReleaseToRefresh();
    }

    /**
     * 设置是否重新设置提示
     * @author jrjin
     * @time 2016-1-6 上午10:07:53
     * @param reset
     */
    public void setResetLable(boolean reset) {
        mResetLable = reset;
    }

    /**
     * 设置下拉提示
     * @author jrjin
     * @time 2016-1-6 上午10:10:02
     * @param lable
     */
    public void setPullDownLable(String lable) {
        mPullDownLable = lable;
    }
    /**
     * 设置下拉释放的提示
     * @author jrjin
     * @time 2016-1-6 上午10:10:22
     * @param lable
     */
    public void setPullDownReleaseLable(String lable) {
        mPullDownReleaseLable = lable;
    }
    /**
     * 设置上拉提示
     * @author jrjin
     * @time 2016-1-6 上午10:10:38
     * @param lable
     */
    public void setPullUpLable(String lable) {
        mPullUpLable = lable;
    }
    /**
     * 设置上拉释放提示
     * @author jrjin
     * @time 2016-1-6 上午10:10:47
     * @param lable
     */
    public void setPullUpReleaseLable(String lable) {
        mPullUpReleaseLable = lable;
    }

}
