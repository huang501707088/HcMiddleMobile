package com.android.hcframe.netdisc.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.netdisc.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-7-28 10:52.
 */
public class ImageAdpater extends HcBaseAdapter<NetdiscImageInfo> {

    private DisplayImageOptions mOptions;

    private ImageLoader mImageLoader;

    private AbsListView.LayoutParams mParams;

    private RelativeLayout.LayoutParams mImageParams;

    public ImageAdpater(Context context, List<NetdiscImageInfo> infos) {
        super(context, infos);
        mImageLoader = ImageLoader.getInstance();
        mOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageOnLoading(R.drawable.netdisc_image_empty)
                .showImageForEmptyUri(R.drawable.netdisc_image_empty)
                .showImageOnFail(R.drawable.netdisc_image_empty)
                .cacheOnDisk(false)
                .cacheInMemory(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888).build();
        int width = (int) (HcUtil.getScreenWidth() - 3 * HcUtil.getScreenDensity() * 2) / 3;
        mParams = new AbsListView.LayoutParams(width, width);
        mImageParams = new RelativeLayout.LayoutParams(width, width);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NetdiscImageInfo info = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.netdisc_image_item_layout, parent, false);
            holder.mSrc = (ImageView) convertView.findViewById(R.id.image_item_src);
            holder.mSelected = (ImageView) convertView.findViewById(R.id.image_item_selected);
            convertView.setTag(holder);
            convertView.setLayoutParams(mParams);
            holder.mSrc.setLayoutParams(mImageParams);
            holder.mSelected.setLayoutParams(mImageParams);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (info.isSelected()) {
            if (holder.mSelected.getVisibility() != View.VISIBLE)
                holder.mSelected.setVisibility(View.VISIBLE);
        } else {
            if (holder.mSelected.getVisibility() != View.GONE)
                holder.mSelected.setVisibility(View.GONE);
        }
        mImageLoader.displayImage(info.getThumbnailsUri(), holder.mSrc, mOptions, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                HcLog.D("ImageAdpater #onLoadingComplete bitmap width= " + bitmap.getWidth() + " height = " + bitmap.getHeight());
            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });
        return convertView;
    }

    private class ViewHolder {
        ImageView mSrc;
        ImageView mSelected;
    }
}
