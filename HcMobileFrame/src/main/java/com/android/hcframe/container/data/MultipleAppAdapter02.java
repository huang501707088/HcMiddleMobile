/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-23 下午2:34:50
*/
package com.android.hcframe.container.data;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import com.android.hcframe.R;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.container.ContainerImageView;
import com.android.hcframe.container.ContainerTextView;

/**
 * ViewInfo is AppInfo
 * @author jrjin
 * @time 2015-11-23 下午2:35:45
 */
public class MultipleAppAdapter02 extends HcBaseAdapter<ViewInfo> {

//	private final Drawable mBG;
	
	private final int mSize;
	
//	private final int mPadding;
	
	public MultipleAppAdapter02(Context context, List<ViewInfo> infos) {
		super(context, infos);
		// TODO Auto-generated constructor stub
//		mBG = context.getResources().getDrawable(R.drawable.item_default_bottom_bg);
		mSize = infos.size();
//		mPadding = context.getResources().getDimensionPixelSize(R.dimen.container_margin_left);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewInfo appInfo = getItem(position);
		ViewHolder mHolder;
		if (convertView == null) {
			mHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.container_item_multiple_grid_layout02, parent, false);
			mHolder.mDivider = convertView.findViewById(R.id.container_item_grid02_divider);
			mHolder.mIcon = (ContainerImageView) convertView.findViewById(R.id.container_item_grid02_img01);
			mHolder.mTitle = (ContainerTextView) convertView.findViewById(R.id.container_item_grid02_text01);
			mHolder.mSubTitle = (ContainerTextView) convertView.findViewById(R.id.container_item_grid02_text02);
			mHolder.mDividerH = convertView.findViewById(R.id.container_item_grid02_divider_H);
			convertView.setTag(mHolder);
		} else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		if (position % 2 == 0) { // 0,2,4 ...
			mHolder.mDivider.setVisibility(View.VISIBLE);
		} else { // 1,3,5 ...
			mHolder.mDivider.setVisibility(View.GONE);
		}
		if (mSize % 2 == 0) {
			if (position == mSize - 1 || position == mSize - 2) {
//				convertView.setBackground(null);
				mHolder.mDividerH.setVisibility(View.GONE);
			} else {
//				convertView.setBackground(mBG);
				mHolder.mDividerH.setVisibility(View.VISIBLE);
			}
		} else {
			if (position == mSize - 1) {
//				convertView.setBackground(null);
				mHolder.mDividerH.setVisibility(View.GONE);
			} else {
//				convertView.setBackground(mBG);
				mHolder.mDividerH.setVisibility(View.VISIBLE);
			}
		}
//		convertView.setPadding(mPadding, 0, 0, 0);
		mHolder.mIcon.setValue(appInfo);
		mHolder.mTitle.setValue(appInfo);
		mHolder.mSubTitle.setValue(appInfo);
		return convertView;
	}

	private class ViewHolder {
		
		private ContainerImageView mIcon;
		private ContainerTextView mTitle;
		private ContainerTextView mSubTitle;
		private View mDivider;
		private View mDividerH; // 底下横条
	} 
}
