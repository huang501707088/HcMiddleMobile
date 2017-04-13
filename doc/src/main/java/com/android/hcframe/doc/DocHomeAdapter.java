package com.android.hcframe.doc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.hcframe.doc.data.DocColumn;
import com.android.hcframe.doc.data.DocHistoricalRecord;
import com.android.hcframe.doc.data.SearchDocInfo;

import java.util.List;

public class DocHomeAdapter extends BaseAdapter {

	private List<DocColumn> docColumns;

	private List<String> mSearchKey;

	private List<SearchDocInfo> docInfos;

	private List<DocHistoricalRecord> historicalRecords;

	private int flag = 0;

	public static final int History = 0;

	public static final int Column = 1;

	public static final int SearchKey = 2;

	public static final int DATA_LIST = 3;

	private Context mContext;

	private LayoutInflater mInflater;

	private IDocCenter iDocCenter;

	public DocHomeAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		int type = getItemViewType(position);

		ViewHolder viewHolder = null;

		ColumnHolderView columnHolderView = null;

		KeyHolderView keyHolderView = null;

		ReturnHolderView returnHolderView = null;

		DocColumn docColumn = null;

		String key = null;

		SearchDocInfo docInfo = null;

		if (getItem(position) instanceof String) {
			key = (String) getItem(position);
		} else if (getItem(position) instanceof DocColumn) {
			docColumn = (DocColumn) getItem(position);
		} else if (getItem(position) instanceof SearchDocInfo) {
			docInfo = (SearchDocInfo) getItem(position);
		}

		if (convertView == null) {
			if (type == History || type == DATA_LIST) {
				viewHolder = new ViewHolder();
				convertView = initHolder(parent, viewHolder);
			} else if (type == Column) {
				if (position == 0) {
					returnHolderView = new ReturnHolderView();
					convertView = initReturnHolder(parent, returnHolderView);
				} else {
					columnHolderView = new ColumnHolderView();
					convertView = initColumnHolder(parent, columnHolderView);
				}
			} else if (type == SearchKey) {
				keyHolderView = new KeyHolderView();
				convertView = initKeyHolder(parent, keyHolderView);
			}
		} else {
			if (type == History || type == DATA_LIST) {
				viewHolder = (ViewHolder) convertView.getTag();
				// if (viewHolder == null) {
				viewHolder = new ViewHolder();
				convertView = initHolder(parent, viewHolder);
				// }
			} else if (type == Column) {
				if (position == 0) {
					returnHolderView = (ReturnHolderView) convertView.getTag();
					// if (returnHolderView == null) {
					returnHolderView = new ReturnHolderView();
					convertView = initReturnHolder(parent, returnHolderView);
					// }
				} else {
					columnHolderView = (ColumnHolderView) convertView.getTag();
					// if (columnHolderView == null) {
					columnHolderView = new ColumnHolderView();
					convertView = initColumnHolder(parent, columnHolderView);
					// }
				}
			} else if (type == SearchKey) {
				keyHolderView = (KeyHolderView) convertView.getTag();
				// if (keyHolderView == null) {
				keyHolderView = new KeyHolderView();
				convertView = initKeyHolder(parent, keyHolderView);
				// }
			}
		}

		if (type == History) {
			if (historicalRecords != null
					&& historicalRecords.get(position) != null) {
				viewHolder.material_history_title.setText(historicalRecords
						.get(position).getFileName());
				viewHolder.material_item_size.setText(historicalRecords.get(
						position).getFileSizeForUnit());
			}
		} else if (type == DATA_LIST) {
			if (docInfos != null && docInfos.get(position) != null) {
				viewHolder.material_history_title.setText(docInfos
						.get(position).getFileName());
				viewHolder.material_item_size.setText(docInfos.get(position)
						.getFileSize());
			}
		} else if (type == Column) {
			if (position == 0) {
				returnHolderView.doc_column_home_btn
						.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View arg0) {
								iDocCenter.onReturnClick();
							}
						});
			} else {
				if (docColumns != null
						&& docColumns.get(2 * (position - 1)) != null) {
					columnHolderView.center_column_left_tv.setText(docColumns
							.get(2 * (position - 1)).getmName());
					final int posLeft = 2 * (position - 1);
					columnHolderView.center_column_left_tv
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View view) {
									iDocCenter.onColumnClick(posLeft);
								}
							});

					if ((position + 1) == getCount()
							&& docColumns.size() % 2 != 0) {
						columnHolderView.center_column_right_tv
								.setVisibility(View.INVISIBLE);
					} else {

						columnHolderView.center_column_right_tv
								.setText(docColumns.get(2 * (position - 1) + 1)
										.getmName());
						final int posRight = 2 * (position - 1) + 1;
						columnHolderView.center_column_right_tv
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View view) {
										iDocCenter.onColumnClick(posRight);
									}
								});

					}
				}
			}
		} else if (type == SearchKey) {
			final int posKey = position;
			if (mSearchKey != null && mSearchKey.get(position) != null) {
				keyHolderView.center_history_key_item_tv.setText(mSearchKey
						.get(position));
				keyHolderView.center_history_key_item_iv
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View view) {
								iDocCenter.onKeyClick(posKey);
							}
						});
			}
		}

		return convertView;
	}

	@Override
	public int getItemViewType(int pos) {

		return flag;
	}

	@Override
	public int getViewTypeCount() {

		return 4;
	}

	@Override
	public int getCount() {
		if (flag == History) {
			if (docInfos != null) {
				return docInfos.size();
			}
		} else if (flag == SearchKey) {
			if (mSearchKey != null) {
				return mSearchKey.size();
			}
		} else if (flag == Column) {
			if (docColumns != null) {
				if (docColumns.size() % 2 == 0) {
					return docColumns.size() / 2 + 1;
				} else {
					return docColumns.size() / 2 + 2;
				}
			}
		}
		return 0;
	}

	@Override
	public Object getItem(int pos) {
		if (flag == History) {
			if (docInfos != null) {
				return docInfos.get(pos);
			}
		} else if (flag == SearchKey) {
			if (mSearchKey != null) {
				return mSearchKey.get(pos);
			}
		} else if (flag == Column) {
			if (docColumns != null) {
				if (pos == 0) {
					return null;
				} else {
					return docColumns.get(2 * (pos - 1));
				}

			}
		}
		return null;
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	public void setDocColumns(List<DocColumn> docColumns) {
		this.docColumns = docColumns;
	}

	public void setmSearchKey(List<String> mSearchKey) {
		this.mSearchKey = mSearchKey;
	}

	public void setDocHistorys(List<SearchDocInfo> docHistorys) {
		this.docInfos = docHistorys;
	}

	private class ViewHolder {

		TextView material_history_title;

		TextView material_item_size;
	}

	private class ColumnHolderView {

		TextView center_column_left_tv;

		TextView center_column_right_tv;
	}

	private class KeyHolderView {

		TextView center_history_key_item_tv;

		ImageView center_history_key_item_iv;
	}

	private class ReturnHolderView {
		RelativeLayout doc_column_home_btn;
	}

	public View initReturnHolder(ViewGroup parent,
			ReturnHolderView returnHolderView) {
		View convertView;
		convertView = mInflater.inflate(R.layout.return_center_item, parent,
				false);

		returnHolderView.doc_column_home_btn = (RelativeLayout) convertView
				.findViewById(R.id.doc_column_home_btn);

		convertView.setTag(returnHolderView);
		return convertView;
	}

	public View initHolder(ViewGroup parent, ViewHolder viewHolder) {
		View convertView;
		convertView = mInflater.inflate(R.layout.material_center_item, parent,
				false);

		viewHolder.material_history_title = (TextView) convertView
				.findViewById(R.id.material_history_title);
		viewHolder.material_item_size = (TextView) convertView
				.findViewById(R.id.material_item_size);

		convertView.setTag(viewHolder);
		return convertView;
	}

	public View initKeyHolder(ViewGroup parent, KeyHolderView keyHolderView) {
		View convertView;
		convertView = mInflater.inflate(R.layout.material_center_item, parent,
				false);

		keyHolderView.center_history_key_item_tv = (TextView) convertView
				.findViewById(R.id.center_history_key_item_tv);
		keyHolderView.center_history_key_item_iv = (ImageView) convertView
				.findViewById(R.id.center_history_key_item_iv);

		convertView.setTag(keyHolderView);
		return convertView;
	}

	public View initColumnHolder(ViewGroup parent,
			ColumnHolderView columnHolderView) {
		View convertView;
		convertView = mInflater.inflate(R.layout.center_column_item, parent,
				false);

		columnHolderView.center_column_left_tv = (TextView) convertView
				.findViewById(R.id.center_column_left_tv);
		columnHolderView.center_column_right_tv = (TextView) convertView
				.findViewById(R.id.center_column_right_tv);

		convertView.setTag(columnHolderView);
		return convertView;
	}

	public void setiDocCenter(IDocCenter iDocCenter) {
		this.iDocCenter = iDocCenter;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public void setHistoricalRecords(List<DocHistoricalRecord> historicalRecords) {
		this.historicalRecords = historicalRecords;
	}
}
