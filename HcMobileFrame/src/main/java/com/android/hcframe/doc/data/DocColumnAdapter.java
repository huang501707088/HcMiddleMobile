/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-8-30 下午5:15:11
 */
package com.android.hcframe.doc.data;

import java.util.List;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.hcframe.R;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.badge.BadgeCache;
import com.android.hcframe.badge.BadgeObserver;
import com.android.hcframe.menu.MenuTextView;

/**
 * 栏目的适配器
 * 
 * @author jrjin
 * @time 2015-8-30 下午6:24:18
 */
public class DocColumnAdapter extends HcBaseAdapter<DocColumn> {

	private static final String TAG = "DocColumnAdapter";

	/** 当前栏目的ID, 也可能为空，即在主页里的时候 */
	private String mCurrentColumnId;

	private int selectPos = -1;

	private final String mAppId;

	public DocColumnAdapter(Context context, List<DocColumn> infos,
			String columnId, String appId) {
		super(context, infos);
		// TODO Auto-generated constructor stub
		mCurrentColumnId = columnId;
		mAppId = appId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		DocColumn column = getItem(position);
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.doc_column_item, parent,
					false);
			holder = new ViewHolder();
			holder.mDivider = convertView
					.findViewById(R.id.column_item_divider);
			holder.mTitle = (TextView) convertView
					.findViewById(R.id.column_item_name);
			/**
			 * 添加角标界面
			 */
			holder.menuTextView= (MenuTextView) convertView.findViewById(R.id.point);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.mTitle.setText(column.getmName());

//		if (selectPos == position) {
//			holder.mTitle.setTextColor(Color.RED);
//		} else {
//			holder.mTitle.setTextColor(Color.BLACK);
//		}
		BadgeCache.getInstance().addBadgeObserver(mAppId + "_" +
						column.getNewsId(), (BadgeObserver) holder.menuTextView
		);
		if (position % 2 == 0) {
			holder.mDivider.setVisibility(View.INVISIBLE);
		} else {
			holder.mDivider.setVisibility(View.VISIBLE);
		}
		if (mCurrentColumnId != null
				&& mCurrentColumnId.equals(column.getNewsId())) {
			holder.mTitle.setSelected(true);
			BadgeCache.getInstance().operateBadge(mCurrentColumnId + "_" +
					column.getNewsId());
//			BadgeCache.getInstance().removeAllBadgeObserver(column.getNewsId());
		} else {
			holder.mTitle.setSelected(false);
		}
		return convertView;
	}

	private class ViewHolder {

		View mDivider;
		TextView mTitle;
		MenuTextView menuTextView;
	}

	public void setColumnId(String columnId) {
		mCurrentColumnId = columnId;
		notifyDataSetChanged();
	}

	public void setSelectPos(int selectPos) {
		this.selectPos = selectPos;
		notifyDataSetChanged();
	}
}
