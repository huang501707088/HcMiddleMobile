/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-28 下午11:45:26
*/
package com.android.hcframe.internalservice.news;

import java.io.InputStream;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.adapter.HcBaseAdapter2;
import com.android.hcframe.adapter.ViewHolderBase;
import com.android.hcframe.data.NewsInfo;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.ant.liao.GifView;
import com.ant.liao.GifView.GifImageType;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class NewsItemAdapter extends HcBaseAdapter2<NewsInfo> {

	private ImageLoader mImageLoader;
	
	private DisplayImageOptions mOptions; 
	
	public NewsItemAdapter(Context context, List<NewsInfo> infos) {
		super(context, infos);
		// TODO Auto-generated constructor stub
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
	public ViewHolderBase<NewsInfo> createViewHolder() {
		// TODO Auto-generated method stub
		return new NewsViewHolder();
	}

	private class NewsViewHolder implements ViewHolderBase<NewsInfo> {

		private LinearLayout mTextNewsParent;
		private RelativeLayout mIconParent;
		private ImageView mIcon;
		private GifView mGifView;
		
		private TextView mTitle;
		private TextView mContent;
		private TextView mDate;
		
		
		private RelativeLayout mImageNewsParent;
		private TextView mImageTitle;
		private TextView mImageCount;
		private ImageView mImage1;
		private ImageView mImage2;
		private ImageView mImage3;

		private RelativeLayout mNoImageParent;
		private TextView mNoImageTitle;
		private TextView mNoImageDate;
		
		
		@Override
		public View createView(LayoutInflater inflater) {
			// TODO Auto-generated method stub
			View view = inflater.inflate(R.layout.news_list_item_layout, null);
			mTextNewsParent = (LinearLayout) view.findViewById(R.id.item_news_parent);
			mIconParent = (RelativeLayout) view.findViewById(R.id.item_news_imagetext_image_parent);
			mIcon = (ImageView) view.findViewById(R.id.item_news_imagetext_image);
			mGifView = (GifView) view.findViewById(R.id.item_news_imagetext_image_gif);
			
			mTitle = (TextView) view.findViewById(R.id.item_news_imagetext_title);
			mContent = (TextView) view.findViewById(R.id.item_news_imagetext_content);
			mDate = (TextView) view.findViewById(R.id.item_news_imagetext_date);
			
			mImageNewsParent = (RelativeLayout) view.findViewById(R.id.item_news_images_parent);
			mImageTitle = (TextView) view.findViewById(R.id.item_news_imagetext_title2);
			mImageCount = (TextView) view.findViewById(R.id.item_news_imagetext_numpic);
			mImage1 = (ImageView) view.findViewById(R.id.item_news_image1);
			mImage2 = (ImageView) view.findViewById(R.id.item_news_image2);
			mImage3 = (ImageView) view.findViewById(R.id.item_news_image3);

			mNoImageParent = (RelativeLayout) view.findViewById(R.id.item_news_no_image_parent);
			mNoImageDate = (TextView) view.findViewById(R.id.item_news_no_imagetext_date);
			mNoImageTitle = (TextView) view.findViewById(R.id.item_news_no_imagetext_title);
			HcLog.D("NewsItemAdapter$NewsViewHolder#createView view = "+view);
			return view;
		}

		@Override
		public void setItemData(int position, NewsInfo data) {
			// TODO Auto-generated method stub
			if ("3".equals(data.mContentType)) { // 图片新闻
				if (mTextNewsParent.getVisibility() != View.GONE)
					mTextNewsParent.setVisibility(View.GONE);
				if (mNoImageParent.getVisibility() != View.GONE)
					mNoImageParent.setVisibility(View.GONE);
				if (mImageNewsParent.getVisibility() != View.VISIBLE)
					mImageNewsParent.setVisibility(View.VISIBLE);
				mImageTitle.setText(data.mTitle);
				mImageCount.setText(String.format(
						mContext.getString(R.string.sum_pics),
						data.mCount));
				if (data.mImgs.size() >= 3) {
					mImageLoader.displayImage(data.mImgs.get(0), mImage1, mOptions);
					mImageLoader.displayImage(data.mImgs.get(1), mImage2, mOptions);
					mImageLoader.displayImage(data.mImgs.get(2), mImage3, mOptions);
				} else if (data.mImgs.size() >= 2) {
					mImageLoader.displayImage(data.mImgs.get(0), mImage1, mOptions);
					mImageLoader.displayImage(data.mImgs.get(1), mImage2, mOptions);
				} else if (data.mImgs.size() >= 1) {
					mImageLoader.displayImage(data.mImgs.get(0), mImage1, mOptions);
				}
				
			} else { // 一般新闻
				if (mImageNewsParent.getVisibility() != View.GONE)
					mImageNewsParent.setVisibility(View.GONE);
				if (TextUtils.isEmpty(data.mIconUrl) && TextUtils.isEmpty(data.newsSummary)) {
					// 既没有图片又没有简介
					if (mTextNewsParent.getVisibility() != View.GONE)
						mTextNewsParent.setVisibility(View.GONE);
					if (mNoImageParent.getVisibility() != View.VISIBLE)
						mNoImageParent.setVisibility(View.VISIBLE);
					if (!TextUtils.isEmpty(data.mTitle))
						mNoImageTitle.setText(data.mTitle);
					mNoImageDate.setText(data.mDate);
				} else {
					if (mNoImageParent.getVisibility() != View.GONE)
						mNoImageParent.setVisibility(View.GONE);
					if (mTextNewsParent.getVisibility() != View.VISIBLE)
						mTextNewsParent.setVisibility(View.VISIBLE);

					if (!TextUtils.isEmpty(data.mTitle))
						mTitle.setText(data.mTitle);
					mDate.setText(data.mDate);
					mContent.setText(data.newsSummary);
					if (TextUtils.isEmpty(data.mIconUrl)) {
						if (mIconParent.getVisibility() != View.GONE)
							mIconParent.setVisibility(View.GONE);
					} else {
						if (mIconParent.getVisibility() != View.VISIBLE)
							mIconParent.setVisibility(View.VISIBLE);

						if (data.mIconUrl.contains("gif")) {
							mIcon.setVisibility(View.GONE);
							mGifView.setVisibility(View.VISIBLE);
							final GifView mGifT = mGifView;
							HcHttpRequest.getRequest().sendDownGifCommand(
									data.mIconUrl, new IHttpResponse() {

										@Override
										public void notifyRequestMd5Url(
												RequestCategory request,
												String md5Url) {
										}

										@Override
										public void notify(Object data,
														   RequestCategory request,
														   ResponseCategory category) {
											if (request == RequestCategory.DOWNLOAD_GIF) {
												if (category == ResponseCategory.SUCCESS) {
													InputStream is = (InputStream) data;
													mGifT.setGifImageType(GifImageType.COVER);
													mGifT.setGifImage(is);
												}
											}
										}
									});
						} else {
							mIcon.setVisibility(View.VISIBLE);
							mGifView.setVisibility(View.GONE);
							mImageLoader.displayImage(
									HcUtil.CHANDED ? HcUtil.mappedUrl(data.mIconUrl)
											: data.mIconUrl,
									mIcon, mOptions);
						}
					}
				}


				
				
			}
		}
		
	}
	
}
