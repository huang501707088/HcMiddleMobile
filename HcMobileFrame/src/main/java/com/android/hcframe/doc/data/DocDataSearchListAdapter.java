/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-8-30 下午7:24:16
 */
package com.android.hcframe.doc.data;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.adapter.HcBaseAdapter;

public class DocDataSearchListAdapter extends HcBaseAdapter<SearchDocInfo> {

	private static final String TAG = "DocDataSearchListAdapter";

	private String key = "";

	public DocDataSearchListAdapter(Context context, List<SearchDocInfo> infos) {
		super(context, infos);
		// TODO Auto-generated constructor stub
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		SearchDocInfo info = getItem(position);
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.doc_list_item, parent,
					false);
			holder = new ViewHolder();
			holder.mTitle = (TextView) convertView
					.findViewById(R.id.doc_file_name);
			holder.mSize = (TextView) convertView
					.findViewById(R.id.doc_file_size);
			holder.mDatetime=(TextView) convertView.findViewById(R.id.doc_file_datetime);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.mTitle.setText(info.getFileName());

		if (!HcUtil.isEmpty(key)) {
			HcUtil.highlight(holder.mTitle, key);
		}

		holder.mSize.setText("" + info.getFileSizeForUnit());

		return convertView;
	}

	private class ViewHolder {
		TextView mTitle;
		TextView mSize;
		TextView mDatetime;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
