/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-12 下午4:00:32
*/
package com.android.hcframe.container;

import java.util.List;

import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.PointImageView;
import com.android.hcframe.R;
import com.android.hcframe.badge.BadgeCache;
import com.android.hcframe.container.data.AppInfo;
import com.android.hcframe.container.data.ElementInfo;
import com.android.hcframe.container.data.ViewInfo;
import com.android.hcframe.http.HcHttpRequest;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

public class ContainerImageView extends PointImageView implements ViewElement {

	private static final String TAG = "ContainerImageView";
	
	private String mId;
	
	private static final int WIDTH = 30;
	
	public ContainerImageView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public ContainerImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public ContainerImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.AppView_Element, defStyle, 0);
		mId = a.getString(R.styleable.AppView_Element_element_id);
		a.recycle();
	}

	@Override
	public String getElementId() {
		// TODO Auto-generated method stub
		return mId;
	}

	@Override
	public void setValue(String value, int type, String attrId) {
		// TODO Auto-generated method stub
		if (TextUtils.isEmpty(value)) {
			setVisibility(View.GONE);
			return;
		}
		if (getVisibility() != View.VISIBLE)
			setVisibility(View.VISIBLE);
		/** 图片的地址是绝对路径调用这个
		 * @date 2015-12-2 上午9:13:48
		ImageLoader.getInstance().displayImage(HcUtil.CHANDED ? HcUtil.mappedUrl(value) : value,
				this, HcUtil.getImageOptions());
		*/
		/** 
		 * @author jrjin
		 * @date 2015-12-2 上午9:14:25
		 * 图片的地址为相对地址 */
//		HcLog.D(TAG + " setValue value = "+HcUtil.getScheme() + "/terminalServer" + value);
//		String urlString = "http://115.238.28.38:8002/terminalServer/file/getContainerImageFile?filePath=/appcontainer/elementImages/6_img01.png";
		ImageLoader.getInstance().displayImage(HcUtil.getScheme() + "/terminalServer" + value,
				this, HcUtil.getImageOptions());
//		ImageLoader.getInstance().displayImage(urlString/*HcUtil.getScheme() + "/terminalServer" + value*/,
//				this, HcUtil.getImageOptions(), new ImageLoadingListener() {
//					
//					@Override
//					public void onLoadingStarted(String imageUri, View view) {
//						// TODO Auto-generated method stub
////						HcLog.D(TAG + "#onLoadingStarted imageUri = "+imageUri + " view = "+view);
//					}
//					
//					@Override
//					public void onLoadingFailed(String imageUri, View view,
//							FailReason failReason) {
//						// TODO Auto-generated method stub
////						HcLog.D(TAG + "#onLoadingFailed imageUri = "+imageUri + " view = "+view);
//					}
//					
//					@Override
//					public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//						// TODO Auto-generated method stub
//						HcLog.D(TAG + "#onLoadingComplete imageUri = "+imageUri + " view = "+view
//								+ " width ======== "+loadedImage.getWidth() + " height ========= "+loadedImage.getHeight());
//						if (loadedImage != null) {
//							int iconWidth = loadedImage.getWidth();
//							int iconHeight = loadedImage.getHeight();
//							float iconDefaultWidth = WIDTH * HcUtil.getScreenDensity();
//							if (iconWidth < (int) iconDefaultWidth || iconHeight < (int) iconDefaultWidth) {
//								// 需要放大图片
//								float scaleWidth = iconDefaultWidth / iconWidth;
//								float scaleHeight = iconDefaultWidth / iconHeight;
//								float scale = 2.0f;//scaleWidth >= scaleHeight ? scaleWidth : scaleHeight;
////								Matrix matrix = new Matrix();
////								matrix.postScale(iconWidth * scale, iconHeight * scale);
//								Bitmap bitmap = Bitmap.createScaledBitmap(loadedImage, 
//										(int)(iconWidth * scale), (int)(iconHeight * scale), true);
//								setImageBitmap(bitmap);
//								HcLog.D(TAG + "#onLoadingComplete "
//										+ " width ======== "+bitmap.getWidth() + " height ========= "+bitmap.getHeight());
////								loadedImage.recycle();
////								loadedImage = null;
//							}
//						}
//						
//					}
//					
//					@Override
//					public void onLoadingCancelled(String imageUri, View view) {
//						// TODO Auto-generated method stub
////						HcLog.D(TAG + "#onLoadingCancelled imageUri = "+imageUri + " view = "+view);
//					}
//				});

		
	}

	@Override
	public void setValue(ViewInfo info) {
		// TODO Auto-generated method stub
		if (info instanceof ElementInfo) { // 单应用容器传递的数据
			BadgeCache.getInstance().addAppBadgeObserver(info.getAppId(), info.getContainerId() + "_" + info.getAppId(), this);
			setElementValue(info);
		} else if (info instanceof AppInfo) { // 多应用容器传递的数据
			BadgeCache.getInstance().addAppBadgeObserver(info.getAppId(), info.getContainerId() + "_" + info.getAppId(), this);
			List<ViewInfo> elements = info.getViewInfos();
			boolean visibility = false;
			for (ViewInfo viewInfo : elements) {
				if (mId.equals(viewInfo.getViewId())) {
					setElementValue(viewInfo);					
					return;
				}
			}
			if (!visibility) 
				setVisibility(View.GONE);
		} else {
			throw new IllegalArgumentException("ContainerTextView setValue ViewInfo must ElementInfo or AppInfo  info = "+info);
		}
	}

	/**
	 * 
	 * @author jrjin
	 * @time 2015-11-20 下午2:02:14
	 * @param info {@link ElementInfo}
	 */
	private void setElementValue(final ViewInfo info) {
		String value = null;
		String attrId = null;
		String action = null;
		int type = ViewInfo.VALUE_REQUEST_NONE;
		value = info.getElementValue();
		type = info.getRequestType();
		attrId = info.getAttrId();
		action = info.getViewAction();
		setValue(value, type, attrId);
		if (!TextUtils.isEmpty(action)) {
			setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					BadgeCache.getInstance().operateBadge(info.getContainerId() + "_" +info.getAppId());
					info.onClick(getContext(), ViewInfo.CLICK_TYPE_IMAGE);
				}
			});
		}
	}
}
