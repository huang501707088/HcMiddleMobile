package com.android.hcframe.netdisc;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.hcframe.HcUtil;
import com.android.hcframe.container.ContainerCircleImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by pc on 2016/6/22.
 */
public class PersonnelAdapter extends BaseAdapter {
    private List<JSONObject> mySkydriveItem;
    private Context mContext;
    private LayoutInflater mInflater;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;

    public PersonnelAdapter(Context context, List<JSONObject> jsonArray) {
        mySkydriveItem = jsonArray;
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageOnLoading(R.drawable.netdisc_icon_default_head)
                .showImageForEmptyUri(R.drawable.netdisc_icon_default_head)
                .showImageOnFail(R.drawable.netdisc_icon_default_head).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888).build();
        mImageLoader = ImageLoader.getInstance();
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        MySkydriveViewHolder mySkydriveViewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.netdisc_personnel_item,
                    parent, false);
            mySkydriveViewHolder = new MySkydriveViewHolder();
            mySkydriveViewHolder.netdisc_id_tv_type = (TextView) convertView.findViewById(R.id.netdisc_id_tv_type);
            mySkydriveViewHolder.netdisc_id_tv_name = (TextView) convertView.findViewById(R.id.netdisc_id_tv_name);
            mySkydriveViewHolder.netdisc_change_img = (ContainerCircleImageView) convertView.findViewById(R.id.netdisc_change_img);
            mySkydriveViewHolder.netdisc_id_iv_add = (ImageView) convertView.findViewById(R.id.netdisc_id_iv_add);
            convertView.setTag(mySkydriveViewHolder);
        } else {
            mySkydriveViewHolder = (MySkydriveViewHolder) convertView.getTag();
        }
        JSONObject jsonObject = (JSONObject) mySkydriveItem.get(position);
        mySkydriveViewHolder.netdisc_id_tv_name.setText(jsonObject.optString("username"));
        String userroleType = jsonObject.optString("userroleType");
        String url = HcUtil.getScheme() + "/terminalServer/file/getfile?fileid=" + jsonObject.optString("userhead") + "&type=common";

        if ("1".equals(userroleType)) {
            mySkydriveViewHolder.netdisc_id_tv_type.setVisibility(View.VISIBLE);
            mySkydriveViewHolder.netdisc_id_tv_type.setText("主");
            mySkydriveViewHolder.netdisc_change_img.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(url,
                    mySkydriveViewHolder.netdisc_change_img, mOptions);
            mySkydriveViewHolder.netdisc_id_iv_add.setVisibility(View.GONE);
        } else if ("2".equals(userroleType)) {
            mySkydriveViewHolder.netdisc_id_tv_type.setVisibility(View.VISIBLE);
            mySkydriveViewHolder.netdisc_id_tv_type.setText("助");
            mySkydriveViewHolder.netdisc_change_img.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(url,
                    mySkydriveViewHolder.netdisc_change_img, mOptions);
            mySkydriveViewHolder.netdisc_id_iv_add.setVisibility(View.GONE);
        } else if ("3".equals(userroleType)) {
            mySkydriveViewHolder.netdisc_id_tv_type.setVisibility(View.GONE);
            mySkydriveViewHolder.netdisc_id_tv_type.setText("");
            mySkydriveViewHolder.netdisc_change_img.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(url,
                    mySkydriveViewHolder.netdisc_change_img, mOptions);
            mySkydriveViewHolder.netdisc_id_iv_add.setVisibility(View.GONE);
        } else if ("4".equals(userroleType)) {
            mySkydriveViewHolder.netdisc_id_tv_type.setVisibility(View.VISIBLE);
            mySkydriveViewHolder.netdisc_id_tv_type.setText("访");
            mySkydriveViewHolder.netdisc_change_img.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(url,
                    mySkydriveViewHolder.netdisc_change_img, mOptions);
            mySkydriveViewHolder.netdisc_id_iv_add.setVisibility(View.GONE);
        } else if ("0".equals(userroleType)) {
            mySkydriveViewHolder.netdisc_id_iv_add.setVisibility(View.VISIBLE);
            mySkydriveViewHolder.netdisc_id_tv_type.setVisibility(View.GONE);
            mySkydriveViewHolder.netdisc_id_tv_type.setText("");
            mySkydriveViewHolder.netdisc_change_img.setVisibility(View.GONE);
        }


        return convertView;
    }

    public static class MySkydriveViewHolder {
        TextView netdisc_id_tv_type;
        TextView netdisc_id_tv_name;
        ContainerCircleImageView netdisc_change_img;
        ImageView netdisc_id_iv_add;
    }
}
