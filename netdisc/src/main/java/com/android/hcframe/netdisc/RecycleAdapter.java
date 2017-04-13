package com.android.hcframe.netdisc;

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

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by pc on 2016/6/22.
 */
public class RecycleAdapter extends BaseAdapter {
    private JSONArray mySkydriveItem;
    private Context mContext;
    private LayoutInflater mInflater;
    // 用来控制CheckBox的选中状况
    private static HashMap<Integer, Boolean> isSelected;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;

    public RecycleAdapter(Context context, JSONArray jsonArray) {
        mySkydriveItem = jsonArray;
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
        for (int i = 0; i < mySkydriveItem.length(); i++) {
            getIsSelected().put(i, false);
        }
    }

    @Override
    public int getCount() {
        return mySkydriveItem.length();
    }

    @Override
    public Object getItem(int position) {
        try {
            return mySkydriveItem.get(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        MySkydriveViewHolder mySkydriveViewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.netdisc_recycle_list_item,
                    parent, false);
            mySkydriveViewHolder = new MySkydriveViewHolder();
            mySkydriveViewHolder.mNetdiscListImg = (ImageView) convertView
                    .findViewById(R.id.netdisc_search_list_img);
            mySkydriveViewHolder.netdisc_search_list_date = (TextView) convertView.findViewById(R.id.netdisc_search_list_date);
            mySkydriveViewHolder.netdisc_search_list_text = (TextView) convertView.findViewById(R.id.netdisc_search_list_text);
            mySkydriveViewHolder.netdisc_id_tv_date = (TextView) convertView.findViewById(R.id.netdisc_id_tv_date);
            mySkydriveViewHolder.mNetdiscListCheckbox = (CheckBox) convertView.findViewById(R.id.netdisc_search_list_checkbox);
            mySkydriveViewHolder.mNetdiscListlinear = (LinearLayout) convertView.findViewById(R.id.netdisc_list_linear);
            convertView.setTag(mySkydriveViewHolder);
        } else {
            mySkydriveViewHolder = (MySkydriveViewHolder) convertView.getTag();
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) mySkydriveItem.get(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String type = jsonObject.optString("extType");
        if ("0".equals(type)) { // 未知
            mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_none_file);
        } else if ("1".equals(type)) {
            mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_search_list_img);
        } else if ("2".equals(type)) {
            mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_word_file);
        } else if ("3".equals(type)) {
            mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_excel_file);
        } else if ("4".equals(type)) {
            mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_ppt_file);
        } else if ("5".equals(type)) {
            mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_img_file);
        } else if ("6".equals(type)) {
            mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_zip_file);
        } else if ("7".equals(type)) {
            mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_none_file);
        }
        mySkydriveViewHolder.netdisc_search_list_date.setText(jsonObject.optString("deleteTimestr") + "    删除");
        mySkydriveViewHolder.netdisc_search_list_text.setText(jsonObject.optString("infoname"));
        mySkydriveViewHolder.netdisc_id_tv_date.setText(jsonObject.optString("between_time") + "天");
        mySkydriveViewHolder.mNetdiscListCheckbox.setChecked(getIsSelected().get(position));
        //找到需要选中的条目
        if (isSelected != null && isSelected.containsKey(position)) {
            mySkydriveViewHolder.mNetdiscListCheckbox.setChecked(isSelected.get(position));
        } else {
            mySkydriveViewHolder.mNetdiscListCheckbox.setChecked(false);
        }
        return convertView;
    }

    public static HashMap<Integer, Boolean> getIsSelected() {
        return isSelected;
    }

    public static void setIsSelected(HashMap<Integer, Boolean> isSelected) {
        RecycleAdapter.isSelected = isSelected;
    }

    public static class MySkydriveViewHolder {
        ImageView mNetdiscListImg;
        TextView netdisc_search_list_date;
        TextView netdisc_search_list_text;
        TextView netdisc_id_tv_date;
        CheckBox mNetdiscListCheckbox;
        LinearLayout mNetdiscListlinear;
    }
}
