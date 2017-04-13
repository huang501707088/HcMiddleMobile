package com.android.hcframe.netdisc.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.hcframe.netdisc.R;
import com.android.hcframe.netdisc.netdisccls.MySkydriveInfoItem;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.HashMap;
import java.util.List;

/**
 * Created by pc on 2016/6/22.
 */
public class FileChooseAdapter extends BaseAdapter {
    private List<MySkydriveInfoItem> mySkydriveItem;
    private Context mContext;
    private LayoutInflater mInflater;
    // 用来控制CheckBox的选中状况
    private static HashMap<Integer, Boolean> isSelected;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;
    public interface CheckBoxCallback {
        void notifyCheckBox(View view, int position);
    }

    private static CheckBoxCallback mCallback;

    public static void setCheckBoxCallback(CheckBoxCallback callback) {
        mCallback = callback;
    }
    public FileChooseAdapter(Context context, List<MySkydriveInfoItem> list) {
        mySkydriveItem = list;
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageOnLoading(R.drawable.default_icon)
                .showImageForEmptyUri(R.drawable.default_icon)
                .showImageOnFail(R.drawable.default_icon).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888).build();
        mImageLoader = ImageLoader.getInstance();
        isSelected = new HashMap<Integer, Boolean>();
        // 初始化数据
        initDate();
    }

    // 初始化isSelected的数据
    private void initDate() {
        for (int i = 0; i < mySkydriveItem.size(); i++) {
            getIsSelected().put(i, false);
        }
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
    public View getView(final int position,View convertView, ViewGroup parent) {
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
//        mImageLoader.displayImage(mySkydriveItem.get(position).getNetdiscListImg(), mySkydriveViewHolder.mNetdiscListImg, mOptions);
        mySkydriveViewHolder.mNetdiscListText.setText(mySkydriveItem.get(position).getNetdiscListText());
        mySkydriveViewHolder.mNetdiscListDate.setText(mySkydriveItem.get(position).getNetdiscListDate());
        mySkydriveViewHolder.mNetdiscListCheckbox.setChecked(getIsSelected().get(position));
        //找到需要选中的条目
        if (isSelected != null && isSelected.containsKey(position)) {
            mySkydriveViewHolder.mNetdiscListCheckbox.setChecked(isSelected.get(position));
        } else {
            mySkydriveViewHolder.mNetdiscListCheckbox.setChecked(false);
        }
        final View finalConvertView = convertView;
        mySkydriveViewHolder.mNetdiscListCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //回调接口，通知MySkydriveActivity
                if (mCallback != null) {
                    mCallback.notifyCheckBox(finalConvertView, position);
                }
            }
        });
        mySkydriveViewHolder.mNetdiscListlinear.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //跳转到下一级页面
                Toast.makeText(mContext, "跳转到下一级页面",
                        Toast.LENGTH_SHORT).show();
            }
        });
        return convertView;
    }

    public static HashMap<Integer, Boolean> getIsSelected() {
        return isSelected;
    }

    public static void setIsSelected(HashMap<Integer, Boolean> isSelected) {
        FileChooseAdapter.isSelected = isSelected;
    }

    public static class MySkydriveViewHolder {
        ImageView mNetdiscListImg;
        TextView mNetdiscListText;
        TextView mNetdiscListDate;
        CheckBox mNetdiscListCheckbox;
        LinearLayout mNetdiscListlinear;
    }
}
