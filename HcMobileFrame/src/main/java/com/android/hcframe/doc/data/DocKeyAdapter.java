/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-8-30 下午7:26:56
*/
package com.android.hcframe.doc.data;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.hcframe.R;
import com.android.hcframe.adapter.HcBaseAdapter;

public class DocKeyAdapter extends HcBaseAdapter<String> {

	private static final String TAG = "DocKeyAdapter";
	
	private onSearchKeyClick mKeyClick;
	
	public DocKeyAdapter(Context context, List<String> infos) {
		super(context, infos);
		// TODO Auto-generated constructor stub
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final String info = getItem(position);
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.doc_key_list_item, parent, false);
			holder = new ViewHolder();
			holder.mKey = (TextView) convertView.findViewById(R.id.doc_key_name);
			holder.mDelete = (ImageView) convertView.findViewById(R.id.doc_key_delete);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.mKey.setText(info);
		holder.mKey.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				if (mContext instanceof DocColumnActivity) {
//					((DocColumnActivity) mContext).setKey(info);
//				}
				if (mKeyClick != null) {
					mKeyClick.setKey(info);
				}
			}
		});
		
		holder.mDelete.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DocCacheData.getInstance().deleteKey(info);
				mInfos.remove(info);
				notifyDataSetChanged();
			}
		});
		
		return convertView;
	}

	private class ViewHolder {
		TextView mKey;
		ImageView mDelete;
	}
	
	public void setSearchKeyClickListener(onSearchKeyClick keyClick) {
		mKeyClick = keyClick;
	}
	
	public interface onSearchKeyClick {
		
		public void setKey(String key);
	}
}
