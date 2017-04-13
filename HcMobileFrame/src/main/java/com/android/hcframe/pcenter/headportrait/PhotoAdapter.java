/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-23 下午1:14:47
*/
package com.android.hcframe.pcenter.headportrait;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;

import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class PhotoAdapter extends HcBaseAdapter<ImageInfo> {

	private DisplayImageOptions mOptions; 
	
	private ImageLoader mImageLoader;

	private AbsListView.LayoutParams mParams;
	
	public PhotoAdapter(Context context, List<ImageInfo> infos) {
		super(context, infos);
		// TODO Auto-generated constructor stub
		mImageLoader = ImageLoader.getInstance();
		mOptions = new DisplayImageOptions.Builder()
		.imageScaleType(ImageScaleType.EXACTLY)
		.showImageOnLoading(R.drawable.photo_empty)
		.showImageForEmptyUri(R.drawable.photo_empty)
		.showImageOnFail(R.drawable.photo_empty)
		.cacheOnDisk(false)
		.cacheInMemory(true)
		.considerExifParams(true)
		.bitmapConfig(Bitmap.Config.ARGB_8888).build();
		int width = (int) (HcUtil.getScreenWidth() - 3 * HcUtil.getScreenDensity() * 2) / 3;
		mParams = new AbsListView.LayoutParams(width, width);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ImageInfo info = getItem(position);
		
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.photo_item_layout, parent, false);
		} 
		final ImageView icon = (ImageView) convertView;
		icon.setLayoutParams(mParams);
//		HcLog.D("PhotoAdapter #getView icon width = "+icon.getWidth() + " icon height = "+icon.getHeight());
		if (position == 0) {
//			icon.setImageResource(R.drawable.takeup);
			mImageLoader.displayImage("drawable://" + R.drawable.takeup, icon, mOptions);
		} else {
			mImageLoader.displayImage(info.getThumbnailsUri(), icon, mOptions, new ImageLoadingListener() {
				@Override
				public void onLoadingStarted(String s, View view) {

				}

				@Override
				public void onLoadingFailed(String s, View view, FailReason failReason) {

				}

				@Override
				public void onLoadingComplete(String s, View view, Bitmap bitmap) {
					HcLog.D("PhotoAdapter #onLoadingComplete bitmap width= "+bitmap.getWidth() + " height = "+bitmap.getHeight());
				}

				@Override
				public void onLoadingCancelled(String s, View view) {

				}
			});
		}
		
		return convertView;
	}

}
