/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-6-16 上午11:47:14
*/
package com.android.hcframe.view.tab;

import java.util.ArrayList;
import java.util.List;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.R;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class TabActivity extends HcBaseActivity {

	private static final String[] CONTENT = new String[]{"Recent", "Artists", "Albums", "Songs", "Playlists", "Genres","aaaaa","bbbbb","ccccc","ddddd","eeeee"};
	
	private List<View> mViews = new ArrayList<View>();
	
	private LayoutParams mParams;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tabs);

		MyAdapter adapter = new MyAdapter();
		TextView view;
		
		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		mParams = pager.getLayoutParams();
		mParams.height = LayoutParams.MATCH_PARENT;
		mParams.width = LayoutParams.MATCH_PARENT;
		for (int i = 0, n = CONTENT.length; i < n; i++) {
			view = new TextView(this);
			view.setLayoutParams(mParams);
//			view.setBackgroundColor(Color.RED);
			view.setText(CONTENT[i]);
			view.setTextSize(30f);
			view.setGravity(Gravity.CENTER);
			mViews.add(view);
		}
		
		pager.setAdapter(adapter);

        TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.indicator);
        indicator.setViewHolderCreator(new TabPageIndicator.ViewHolderCreator() {
            @Override
            public TabPageIndicator.ViewHolderBase createViewHolder() {
                return new DemoViewHolder();
            }
        });
        indicator.setViewPager(pager, 0);
	}

	private class MyAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return CONTENT.length;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			// TODO Auto-generated method stub
			return view == object;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
			if (position < mViews.size()) {
				((ViewPager) container).removeView(mViews.get(position));
			}
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			// TODO Auto-generated method stub
			container.addView(mViews.get(position));
			return mViews.get(position);
		}
		
		
	}
	
	private class DemoViewHolder extends TabPageIndicator.ViewHolderBase {

        private TextView mTitleTextView;
        private View mViewSelected;
        private final int COLOR_TEXT_SELECTED = Color.parseColor("#ffffff");
        private final int COLOR_TEXT_NORMAL = Color.parseColor("#999999");

        @Override
        public View createView(LayoutInflater layoutInflater, int position) {
            View view = layoutInflater.inflate(R.layout.ht_views_bimai_cat_item, null);
            mTitleTextView = (TextView) view.findViewById(R.id.tv_ht_bimai_cat_item_title);
            mViewSelected = view.findViewById(R.id.tv_ht_bimai_cat_item_selected);
            return view;
        }

        @Override
        public void updateView(int position, boolean isCurrent) {
            mTitleTextView.setText(CONTENT[position]);
            if (isCurrent) {
                mTitleTextView.setTextColor(COLOR_TEXT_SELECTED);
                mViewSelected.setVisibility(View.VISIBLE);
            } else {
                mTitleTextView.setTextColor(COLOR_TEXT_NORMAL);
                mViewSelected.setVisibility(View.INVISIBLE);
            }
        }
    }
}
