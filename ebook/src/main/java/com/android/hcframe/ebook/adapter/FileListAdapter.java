package com.android.hcframe.ebook.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.hcframe.ebook.R;
import com.android.hcframe.ebook.entity.FileEntity;

public class FileListAdapter extends ArrayAdapter<FileEntity> {

	public FileListAdapter(Context context) {
		super(context, 0);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_file_list, null);
			holder.titleTxt = (TextView) convertView.findViewById(R.id.title);
			holder.timeTxt = (TextView) convertView.findViewById(R.id.time);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.titleTxt.setText(getItem(position).getTitle());
		holder.timeTxt.setText(getItem(position).getCreatedtime());
		return convertView;
	}

	class ViewHolder {
		private TextView titleTxt;
		private TextView timeTxt;
	}

}
