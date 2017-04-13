package com.android.hcframe.netdisc;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.hcframe.netdisc.netdisccls.DownFragItem;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.List;

/**
 * Created by pc on 2016/6/27.
 */
public class DownFragAdapter extends BaseAdapter {
    private List<DownFragItem> mDownFragItem;
    private Context mContext;
    private LayoutInflater mInflater;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;

    public DownFragAdapter(Context context, List<DownFragItem> list) {
        super();
        mDownFragItem = list;
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
    }

    @Override
    public int getCount() {
        return mDownFragItem.size();
    }

    @Override
    public Object getItem(int position) {
        return mDownFragItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DownFragItemHolder downFragItemHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.netdisc_down_frag_list_item,
                    parent, false);
            downFragItemHolder = new DownFragItemHolder();
            downFragItemHolder.downImg = (ImageView) convertView.findViewById(R.id.netdisc_down_list_img);
            downFragItemHolder.downFileName = (TextView) convertView.findViewById(R.id.netdisc_down_list_text);
            downFragItemHolder.downFileM = (TextView) convertView.findViewById(R.id.netdisc_down_list_data);
            downFragItemHolder.downDate = (TextView) convertView.findViewById(R.id.netdisc_down_list_date);
            downFragItemHolder.downTime = (TextView) convertView.findViewById(R.id.netdisc_down_list_time);
            downFragItemHolder.downDeleteImg = (ImageView) convertView.findViewById(R.id.netdisc_down_delete_img);
            convertView.setTag(downFragItemHolder);
        } else {
            downFragItemHolder = (DownFragItemHolder) convertView.getTag();
        }
//        mImageLoader.displayImage(mDownFragItem.get(position).getDownImg(),downFragItemHolder.downImg, mOptions);
        downFragItemHolder.downFileName.setText(mDownFragItem.get(position).getDownFileName());
        downFragItemHolder.downFileM.setText(mDownFragItem.get(position).getDownFileM());
        downFragItemHolder.downTime.setText(mDownFragItem.get(position).getDownTime());
        readExt(mDownFragItem.get(position).getDownImg(), downFragItemHolder);
        return convertView;
    }

    public class DownFragItemHolder {
        ImageView downImg;
        TextView downFileName;
        TextView downFileM;
        TextView downDate;
        TextView downTime;
        ImageView downDeleteImg;
    }

    private void readExt(String ext, DownFragItemHolder downFragItemHolder) {
        if (ext == null || "".equals(ext)) {
            downFragItemHolder.downImg.setImageResource(R.drawable.netdisc_none_file);
        } else if (ext.equals("txt")) {
            downFragItemHolder.downImg.setImageResource(R.drawable.netdisc_none_file);
        } else if (ext.equals("doc") || ext.equals("docx")) {
            downFragItemHolder.downImg.setImageResource(R.drawable.netdisc_word_file);
        } else if (ext.equals("ppt") || ext.equals("pptx")) {
            downFragItemHolder.downImg.setImageResource(R.drawable.netdisc_ppt_file);
        } else if (ext.equals("xls") || ext.equals("xlsx")) {
            downFragItemHolder.downImg.setImageResource(R.drawable.netdisc_excel_file);
        } else if (ext.equals("zip") || ext.equals("rar") ||
                ext.equals("tar") || ext.equals("7z")) {
            downFragItemHolder.downImg.setImageResource(R.drawable.netdisc_zip_file);
        } else if (ext.equals("apk") || ext.equals("ipa")) {
            downFragItemHolder.downImg.setImageResource(R.drawable.netdisc_none_file);
        } else {
            downFragItemHolder.downImg.setImageResource(R.drawable.netdisc_none_file);
        }
    }
}
