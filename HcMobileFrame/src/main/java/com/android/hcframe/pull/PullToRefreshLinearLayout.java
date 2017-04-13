/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-29 下午12:19:57
*/
package com.android.hcframe.pull;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.LinearLayout;

/**
 * 还是有问题,先暂时不用
 * @author jrjin
 * @time 2015-12-29 下午12:56:44
 */
public class PullToRefreshLinearLayout extends PullToRefreshBase<LinearLayout> {
	
	public PullToRefreshLinearLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public PullToRefreshLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public PullToRefreshLinearLayout(Context context,
			com.android.hcframe.pull.PullToRefreshBase.Mode mode) {
		super(context, mode);
		// TODO Auto-generated constructor stub
	}

	public PullToRefreshLinearLayout(Context context,
			com.android.hcframe.pull.PullToRefreshBase.Mode mode,
			com.android.hcframe.pull.PullToRefreshBase.AnimationStyle animStyle) {
		super(context, mode, animStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	public com.android.hcframe.pull.PullToRefreshBase.Orientation getPullToRefreshScrollDirection() {
		// TODO Auto-generated method stub
		return Orientation.VERTICAL;
	}

	@Override
	protected LinearLayout createRefreshableView(Context context,
			AttributeSet attrs) {
		// TODO Auto-generated method stub
		return new LinearLayout(context, attrs);
	}

	@Override
	protected boolean isReadyForPullEnd() {
		// TODO Auto-generated method stub
		return isLastItemVisible();
	}

	@Override
	protected boolean isReadyForPullStart() {
		// TODO Auto-generated method stub
		return isFirstItemVisible();
	}

	private boolean isFirstItemVisible() {
		
		AbsListView mListView = null;
		
		int childCount = mRefreshableView.getChildCount();
		
		for (int i = 0; i < childCount; i++) {
			if (mRefreshableView.getChildAt(i) instanceof AbsListView) {
				mListView = (AbsListView) mRefreshableView.getChildAt(i);
				break;
			}
		}
		
		if (mListView != null) {
			final Adapter adapter = mListView.getAdapter();

			if (null == adapter || adapter.isEmpty()) {
				if (DEBUG) {
					Log.d(LOG_TAG, "isFirstItemVisible. Empty View.");
				}
				return true;

			} else {

				/**
				 * This check should really just be:
				 * mRefreshableView.getFirstVisiblePosition() == 0, but PtRListView
				 * internally use a HeaderView which messes the positions up. For
				 * now we'll just add one to account for it and rely on the inner
				 * condition which checks getTop().
				 */
				if (mListView.getFirstVisiblePosition() <= 1) {
					final View firstVisibleChild = mRefreshableView.getChildAt(0);
					if (firstVisibleChild != null) {
						return firstVisibleChild.getTop() >= mRefreshableView.getTop();
					}
				}
			}
		}
		
		
		

		return false;
	}
	
	private boolean isLastItemVisible() {
		
		AbsListView mListView = null;
		
		int childCount = mRefreshableView.getChildCount();
		
		for (int i = 0; i < childCount; i++) {
			if (mRefreshableView.getChildAt(i) instanceof AbsListView) {
				mListView = (AbsListView) mRefreshableView.getChildAt(i);
				break;
			}
		}
		
		if (mListView != null) {
			final Adapter adapter = mListView.getAdapter();

			if (null == adapter || adapter.isEmpty()) {
				if (DEBUG) {
					Log.d(LOG_TAG, "isLastItemVisible. Empty View.");
				}
				return true;
			} else {
				final int lastItemPosition = mListView.getCount() - 1;
				final int lastVisiblePosition = mListView.getLastVisiblePosition();

				if (DEBUG) {
					Log.d(LOG_TAG, "isLastItemVisible. Last Item Position: " + lastItemPosition + " Last Visible Pos: "
							+ lastVisiblePosition);
				}

				/**
				 * This check should really just be: lastVisiblePosition ==
				 * lastItemPosition, but PtRListView internally uses a FooterView
				 * which messes the positions up. For me we'll just subtract one to
				 * account for it and rely on the inner condition which checks
				 * getBottom().
				 */
				if (lastVisiblePosition >= lastItemPosition - 1) {
					final int childIndex = lastVisiblePosition - mListView.getFirstVisiblePosition();
					final View lastVisibleChild = mRefreshableView.getChildAt(childIndex);
					if (lastVisibleChild != null) {
						return lastVisibleChild.getBottom() <= mRefreshableView.getBottom();
					}
				}
			}
		}
		
		

		return false;
	}
}
