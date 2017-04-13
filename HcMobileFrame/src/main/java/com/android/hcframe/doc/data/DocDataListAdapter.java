/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-8-30 下午7:15:56
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

public class DocDataListAdapter extends HcBaseAdapter<DocInfo> {

	private static final String TAG = "DocDataListAdapter";

	public DocDataListAdapter(Context context, List<DocInfo> infos) {
		super(context, infos);
		// TODO Auto-generated constructor stub
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		DocInfo info = getItem(position);
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.doc_list_item, parent,
					false);
			holder = new ViewHolder();
			holder.mTitle = (TextView) convertView
					.findViewById(R.id.doc_file_name);
			holder.mSize = (TextView) convertView
					.findViewById(R.id.doc_file_size);
			holder.mDatetime = (TextView) convertView
					.findViewById(R.id.doc_file_datetime);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.mTitle.setText(info.getDataTitle());
		String size = "";
		for (DocFileInfo fileInfo : info.getDocInfos()) {
			if (fileInfo.getFlag() == DocInfo.FLAG_MAIN) {
				size = "" + fileInfo.getFileSizeForUnit();
				break;
			}
		}
		holder.mSize.setText(size);
		holder.mDatetime.setText(HcUtil.changeDateFormat(info.getDate(),
				HcUtil.FORMAT_POLLUTION_S, HcUtil.FORMAT_POLLUTION_NEW));

		return convertView;
	}

	private class ViewHolder {
		TextView mTitle;
		TextView mSize;
		TextView mDatetime;
	}
}
