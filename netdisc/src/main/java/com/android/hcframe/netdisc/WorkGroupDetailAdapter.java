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

import com.android.hcframe.netdisc.netdisccls.MySkydriveInfoItem;

import java.util.List;

/**
 * Created by pc on 2016/6/22.
 */
public class WorkGroupDetailAdapter extends BaseAdapter {

    private List<MySkydriveInfoItem> mySkydriveItem;
    private Context mContext;
    private LayoutInflater mInflater;
    private final int headerType = 0;
    private final int skydriveType = 1;

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

    public WorkGroupDetailAdapter(Context context, List<MySkydriveInfoItem> list) {
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

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final MySkydriveInfoItem item = (MySkydriveInfoItem) getItem(position);
        MySkydriveHeaderViewHolder mySkydriveHeaderViewHolder = null;
        MySkydriveViewHolder mySkydriveViewHolder = null;
        if (convertView == null) {
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
        } else {
            mySkydriveViewHolder = (MySkydriveViewHolder) convertView.getTag();
        }
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
        } else if ("7".equals(item.getNetdiscListType())) {
            mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_music_file);
        }else if ("8".equals(item.getNetdiscListType())) {
            mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_music_file);
        }else if ("9".equals(item.getNetdiscListType())) {
            mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_music_file);
        }

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
        return convertView;
    }

    public static class MySkydriveHeaderViewHolder {
        TextView mNetdiscListHeader;
    }

    public static class MySkydriveViewHolder {
        ImageView mNetdiscListImg;
        TextView mNetdiscListText;
        TextView mNetdiscListDate;
        CheckBox mNetdiscListCheckbox;
        LinearLayout mNetdiscListlinear;
    }
}
