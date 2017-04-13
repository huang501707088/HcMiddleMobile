package com.android.hcframe.netdisc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.hcframe.netdisc.netdisccls.MySkydriveInfoItem;

import java.util.HashMap;
import java.util.List;

/**
 * Created by pc on 2016/6/22.
 */
public class MySkydriveAdapter extends BaseAdapter {

    private List<MySkydriveInfoItem> mySkydriveItem;
    private Context mContext;
    private LayoutInflater mInflater;
    private final int headerType = 0;
    private final int skydriveType = 1;
    private int type;

    public interface OnClickCallback {
        void onCheckBoxClick(View view, int position, MySkydriveInfoItem item);
    }

    private static OnClickCallback mCallback;

    public static void setOnClickCallback(OnClickCallback callback) {
        mCallback = callback;
    }

    public interface OnLinearClickCallback {
        void onItemClick(View view, int position, MySkydriveInfoItem item);

        void onItemLinearClick(View view, int position, MySkydriveInfoItem item);
    }

    private static OnLinearClickCallback lCallback;
    private static boolean mFlag;

    public static void setOnLinearClickCallback(OnLinearClickCallback callback, boolean flag) {
        lCallback = callback;
        mFlag = flag;
    }

    public MySkydriveAdapter(Context context, List<MySkydriveInfoItem> list) {
        mySkydriveItem = list;
        mContext = context;
        mInflater = LayoutInflater.from(context);

    }


    @Override
    public int getCount() {
        return mySkydriveItem.size();
    }

    @Override
    public Object getItem(int position) {
        return mySkydriveItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getItemViewType(int position) {
//        if ("R".equals(mySkydriveItem.get(0).getNetdiscUpdirId())) {
        if (!"".equals(mySkydriveItem.get(0).getNetdiscListDate())&&mySkydriveItem.get(0).getNetdiscListDate()!= null) {
            return skydriveType;
        } else {
            if (position == 0) {
                return headerType;
            } else {
                return skydriveType;
            }
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final MySkydriveInfoItem item = (MySkydriveInfoItem) getItem(position);
        type = getItemViewType(position);
        MySkydriveHeaderViewHolder mySkydriveHeaderViewHolder = null;
        MySkydriveViewHolder mySkydriveViewHolder = null;
        if (convertView == null) {
            switch (type) {
                case headerType:
                    convertView = mInflater.inflate(R.layout.netdisc_search_edit_list_item_header,
                            parent, false);
                    mySkydriveHeaderViewHolder = new MySkydriveHeaderViewHolder();
                    mySkydriveHeaderViewHolder.mNetdiscListHeader = (TextView) convertView.findViewById(R.id.netdisc_search_list_header);
                    mySkydriveHeaderViewHolder.mNetdiscListlinear = (LinearLayout) convertView.findViewById(R.id.netdisc_list_linear_header);
                    convertView.setTag(mySkydriveHeaderViewHolder);
                    break;
                case skydriveType:
                    convertView = mInflater.inflate(R.layout.netdisc_search_edit_list_item,
                            parent, false);
                    mySkydriveViewHolder = new MySkydriveViewHolder();
                    mySkydriveViewHolder.mNetdiscListImg = (ImageView) convertView
                            .findViewById(R.id.netdisc_search_list_img);
                    mySkydriveViewHolder.mNetdiscListText = (TextView) convertView.findViewById(R.id.netdisc_search_list_text);
                    mySkydriveViewHolder.mNetdiscListDate = (TextView) convertView.findViewById(R.id.netdisc_search_list_date);
                    mySkydriveViewHolder.mNetdiscListCheckbox = (CheckBox) convertView.findViewById(R.id.netdisc_search_list_checkbox);
                    mySkydriveViewHolder.mNetdiscListlinear = (LinearLayout) convertView.findViewById(R.id.netdisc_list_linear);
                    convertView.setTag(mySkydriveViewHolder);
                    break;
            }
        } else {
            switch (type) {
                case headerType:
                    mySkydriveHeaderViewHolder = (MySkydriveHeaderViewHolder) convertView.getTag();
                    break;
                case skydriveType:
                    mySkydriveViewHolder = (MySkydriveViewHolder) convertView.getTag();
                    break;
                default:
                    break;
            }

        }
        switch (type) {
            case headerType:
                final View view = convertView;
                mySkydriveHeaderViewHolder.mNetdiscListHeader.setText(item.getNetdiscListSharedSize());
                mySkydriveHeaderViewHolder.mNetdiscListlinear.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        lCallback.onItemClick(view, position, item);
                    }
                });
                break;
            case skydriveType:
                if ("0".equals(item.getNetdiscListType())) { // 未知
                    mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_none_file);
                } else if ("1".equals(item.getNetdiscListType())) {
                    mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_search_list_img);
                } else if ("2".equals(item.getNetdiscListType())) {
                    mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_word_file);
                } else if ("3".equals(item.getNetdiscListType())) {
                    mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_excel_file);
                } else if ("4".equals(item.getNetdiscListType())) {
                    mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_ppt_file);
                } else if ("5".equals(item.getNetdiscListType())) {
                    mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_img_file);
                } else if ("6".equals(item.getNetdiscListType())) {
                    mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_zip_file);
                } else if ("7".equals(item.getNetdiscListType())||"8".equals(item.getNetdiscListType())||"9".equals(item.getNetdiscListType())) {
                    mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_music_file);
                }
//                else if ("8".equals(item.getNetdiscListType())) {
//                    mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_none_file);
//                }else if ("9".equals(item.getNetdiscListType())) {
//                    mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_none_file);
//                }

                mySkydriveViewHolder.mNetdiscListText.setText(item.getNetdiscListText());
                mySkydriveViewHolder.mNetdiscListDate.setText(item.getNetdiscListDate());

                mySkydriveViewHolder.mNetdiscListCheckbox.setChecked(item.getChecked());
                final View finalConvertView = convertView;
                mySkydriveViewHolder.mNetdiscListCheckbox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //回调接口，通知MySkydriveActivity
                        item.setChecked(!item.getChecked());
                        ((CheckBox) v).setChecked(item.getChecked());
                        if (mCallback != null) {
                            mCallback.onCheckBoxClick(finalConvertView, position, item);
                        }
                    }

                });
                mySkydriveViewHolder.mNetdiscListlinear.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        //回调接口，通知MySkydriveActivity
                        if (lCallback != null && mFlag) {
                            if ("1".equals(mySkydriveItem.get(position).getNetdiscListType())) {
                                //跳转到下一级页面
                                lCallback.onItemClick(finalConvertView, position, item);
                            }
                        } else {
                            lCallback.onItemLinearClick(finalConvertView, position, item);
                        }
                    }
                });
                break;
        }
        return convertView;
    }

    public static class MySkydriveHeaderViewHolder {
        TextView mNetdiscListHeader;
        LinearLayout mNetdiscListlinear;
    }

    public static class MySkydriveViewHolder {
        ImageView mNetdiscListImg;
        TextView mNetdiscListText;
        TextView mNetdiscListDate;
        CheckBox mNetdiscListCheckbox;
        LinearLayout mNetdiscListlinear;
    }
}
