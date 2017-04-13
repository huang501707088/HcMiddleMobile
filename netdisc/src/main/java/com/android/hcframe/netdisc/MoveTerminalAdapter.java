package com.android.hcframe.netdisc;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.hcframe.netdisc.netdisccls.MySkydriveInfoItem;

import java.util.List;

/**
 * Created by pc on 2016/6/28.
 */
public class MoveTerminalAdapter extends BaseAdapter {
    private List<MySkydriveInfoItem> mMoveTerminalItem;
    private Context mContext;
    private LayoutInflater mInflater;

    public MoveTerminalAdapter(Context context, List<MySkydriveInfoItem> list) {
        super();
        mMoveTerminalItem = list;
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mMoveTerminalItem.size();
    }

    @Override
    public Object getItem(int position) {
         return mMoveTerminalItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final MySkydriveInfoItem item = (MySkydriveInfoItem) getItem(position);
        MoveTerminalHolder moveTerminalHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.netdisc_move_terminal_list_item,
                    parent, false);
            moveTerminalHolder = new MoveTerminalHolder();
            moveTerminalHolder.mNetdiscListImg = (ImageView) convertView.findViewById(R.id.netdisc_move_list_img);
            moveTerminalHolder.mNetdiscListText = (TextView) convertView.findViewById(R.id.netdisc_move_list_text);
            moveTerminalHolder.mNetdiscListDate = (TextView) convertView.findViewById(R.id.netdisc_move_list_one_date);
            convertView.setTag(moveTerminalHolder);
        } else {
            moveTerminalHolder = (MoveTerminalHolder) convertView.getTag();
        }
        if ("0".equals(item.getNetdiscListType())) { // 未知
            moveTerminalHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_none_file);
        } else if ("1".equals(item.getNetdiscListType())) {
            moveTerminalHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_search_list_img);
        } else if ("2".equals(item.getNetdiscListType())) {
            moveTerminalHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_word_file);
        } else if ("3".equals(item.getNetdiscListType())) {
            moveTerminalHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_excel_file);
        } else if ("4".equals(item.getNetdiscListType())) {
            moveTerminalHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_ppt_file);
        } else if ("5".equals(item.getNetdiscListType())) {
            moveTerminalHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_img_file);
        } else if ("6".equals(item.getNetdiscListType())) {
            moveTerminalHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_zip_file);
        } else if ("7".equals(item.getNetdiscListType())) {
            moveTerminalHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_none_file);
        }

        moveTerminalHolder.mNetdiscListText.setText(item.getNetdiscListText());
//        moveTerminalHolder.mNetdiscListDate.setText(item.getNetdiscListDate());
        return convertView;
    }

    public class MoveTerminalHolder {
        ImageView mNetdiscListImg;
        TextView mNetdiscListText;
        TextView mNetdiscListDate;
    }
}
