package com.android.hcframe.doc.data;

import java.util.List;

import com.android.hcframe.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DetailsAttAdapter extends BaseAdapter {

	private Context context;

	private List<DocFileInfo> docFiles;

	protected final LayoutInflater mInflater;

	public DetailsAttAdapter(Context context, List<DocFileInfo> docFiles) {
		this.context = context;
		this.docFiles = docFiles;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return docFiles.size() - 1;
	}

	@Override
	public DocFileInfo getItem(int pos) {
		return docFiles.get(pos + 1);
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		DocFileInfo info = getItem(position);
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.details_attachment_item, parent,
					false);
			holder = new ViewHolder();
			holder.details_doc_file_name = (TextView) convertView
					.findViewById(R.id.details_doc_file_name);
			holder.details_doc_file_size = (TextView) convertView
					.findViewById(R.id.details_doc_file_size);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.details_doc_file_name.setText(info.getFileName());

		holder.details_doc_file_size.setText("" + info.getFileSizeForUnit());

		return convertView;
	}

	private class ViewHolder {
		TextView details_doc_file_name;
		TextView details_doc_file_size;
	}
}
