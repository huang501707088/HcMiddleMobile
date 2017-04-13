/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com
* @author jinjr
* @data 2015-12-11 下午12:52:54
*/
package com.android.hcframe.ad;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.android.hcframe.DotView;
import com.android.hcframe.HcLog;
import com.android.hcframe.R;
import com.android.hcframe.adapter.ViewHolderBase;
import com.android.hcframe.adapter.ViewHolderFactory;
import com.android.hcframe.data.NewsInfo;
import com.android.hcframe.menu.DownloadPDFActivity;
import com.android.hcframe.menu.MenuBaseActivity;
import com.android.hcframe.servicemarket.photoscan.ImageScanActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.view.ViewPager.PageTransformer;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class AdPanelView extends FrameLayout {

	private static final String TAG = "AdPanelView";

	private DotView mDotView;

	private LoopViewPager mViewPager;

	private LoopPagerAdapter mAdapter;

	private static final long PLAY_TIME = 2 * 1000;

	private long mPlayTime = PLAY_TIME;

	private AdHander mAdHander;

	private int mCurrentItem;

	private OnPageChangeListener mPageChangeListener;

	private List<NewsInfo> mNewsInfos = new ArrayList<NewsInfo>();

	private ViewPagerScroller mViewPagerScroller;

	private TextView mDotText;

	private int mTotle;

	public AdPanelView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public AdPanelView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public AdPanelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.topBar, defStyle, 0);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		int layoutResourceId = a.getResourceId(
				R.styleable.topBar_topbar_layout, R.layout.news_scroll_parent_layout);
		HcLog.D(TAG + " AdPanelView before inflater!");
		a.recycle();

		inflater.inflate(layoutResourceId, this, true);

		HcLog.D(TAG + " AdPanelView after inflater!");

		mDotView = (DotView) findViewById(R.id.news_scroll_dot_parent);
		mViewPager = (LoopViewPager) findViewById(R.id.news_scroll_viewpager);
		mDotText = (TextView) findViewById(R.id.news_scroll_text);
		mViewPager.setOnPageChangeListener(mDefault);

		initViewPagerScroll();

		setPageTransformer(Transformer.DefaultTransformer);
	}

	public void createLayout(List<NewsInfo> infos) {
		int size = infos.size();
		if (size == 0) {
			setVisibility(View.GONE);
			return;
		} else {
			setVisibility(View.VISIBLE);
		}
		int oldSize = mNewsInfos.size();
		if (size != oldSize) {
			mTotle = size;
			// 需要暂停轮播，设置初始值
			if (mAdHander != null) {
				mAdHander.pause();
			}

			mNewsInfos.clear();
			mNewsInfos.addAll(infos);

			if (mAdapter == null) {
				mAdapter = new AdLoopPagerAdapter(mNewsInfos);
				mViewPager.setLoopAdapter(mAdapter);
			} else {
				mAdapter.notifyDataSetChanged();
			}

			// 重新设置dot
			if (mDotView != null) {
				mDotView.setGravity(DotView.GRAVITY_RIGHT);
				mDotView.setTotalItems(size);
			}
			if (size > 1) {
				/**
				 * @jrjin
				 * @date 2017.03.08
				 * 换成显示数字
				if (mDotView.getVisibility() != View.VISIBLE) {
					mDotView.setVisibility(View.VISIBLE);
				}
				 */
				if (mDotText.getVisibility() != View.VISIBLE) {
					mDotText.setVisibility(View.VISIBLE);
					mDotText.setText("1/" + mTotle);
				}
				// 重新开始论播
				if (mAdHander == null) {
					mAdHander = new AdHander();
				}
				mAdHander.resume();
			} else {
				/**
				 * @jrjin
				 * @date 2017.03.08
				 * 换成显示数字
				if (mDotView.getVisibility() != View.GONE) {
					mDotView.setVisibility(View.GONE);
				}*/
				if (mDotText.getVisibility() != View.GONE) {
					mDotText.setVisibility(View.GONE);
				}
				if (mAdHander != null) {
					mAdHander = null;
				}
			}

		}


	}

	private class AdHander extends Handler {

		void resume() {
            if (!hasMessages(0)) {
//                sendEmptyMessage(0);
            	sendEmptyMessageDelayed(0, mPlayTime);
            }
        }

        void pause() {
            removeMessages(0);
        }

        @Override
        public void handleMessage(Message message) {
        	// 这里只做viewpage的页面切换,其他的事情放到监听里面去处理mDefalt
        	HcLog.D(TAG + " handleMessage current item = "+mViewPager.getCurrentItem());
        	if (mAdHander == null) {
        		pause();
        		return;
        	}
        	if (message.what == 0) {
				mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
				sendEmptyMessageDelayed(0, mPlayTime);
			}


//            // 切换dot
//        	if (mDotView != null) {
//        		mDotView.setCurrentItem(mCurrentItem);
//        	}
//        	// 切换viewpage
//
//
//
//            sendEmptyMessageDelayed(0, mPlayTime);
        }
	}

	/**
	 * 设置轮播的时间间隔
	 * @author jrjin
	 * @time 2015-12-11 下午2:52:28
	 * @param time
	 */
	public void setPlayTime(long time) {
		if (mPlayTime != time) {
			mPlayTime = time;
		}
	}

	private OnPageChangeListener mDefault = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int position) {
			// TODO Auto-generated method stub
//			HcLog.D(TAG + " OnPageChangeListener onPageSelected position = "+position);
			/**
			 * @jrjin
			 * @date 2016-06-01
			 * 放在{@link #dispatchTouchEvent(MotionEvent ev)}里面处理了
			 * @see #dispatchTouchEvent

			if (mAdHander != null) {
				mAdHander.pause();
				mAdHander.resume();
			}
			 */

			/**
			 * @jrjin
			 * @date 2017.03.08
			 * 换成显示数字
			if (mDotView != null) {
				mDotView.setCurrentItem(position);
			}*/
			mDotText.setText(position + 1 + "/" + mTotle);
		}
		
		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			// TODO Auto-generated method stub

		}
		
		@Override
		public void onPageScrollStateChanged(int state) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private class AdLoopPagerAdapter extends LoopPagerAdapter<NewsInfo> {

		public AdLoopPagerAdapter(List<NewsInfo> data) {
			super(data, true, new NewsViewHolder());
			// TODO Auto-generated constructor stub
		}
		
	}
	
	private class NewsViewHolder implements ViewHolderFactory<NewsInfo> {

		private static final String TAG = AdPanelView.TAG + "$NewsViewHolder";
		
		private DisplayImageOptions mOptions;
		
		private ImageLoader mLoader;
		
		public NewsViewHolder() {
			mOptions = new DisplayImageOptions.Builder()
			.imageScaleType(ImageScaleType.EXACTLY)
			.showImageOnLoading(R.drawable.news_scrolll_default_icon)
			.showImageForEmptyUri(R.drawable.news_scrolll_default_icon)
			.showImageOnFail(R.drawable.news_scrolll_default_icon).cacheInMemory(true)
			.cacheOnDisk(true).considerExifParams(true)
			.bitmapConfig(Bitmap.Config.ARGB_8888).build();
			mLoader = ImageLoader.getInstance();
		}
		
		@Override
		public ViewHolderBase<NewsInfo> createViewHolder() {
			// TODO Auto-generated method stub
			return new ViewHolderBase<NewsInfo>() {

				private TextView mTitle;
				
				private TextView mSubTitle;
				
				private ImageView mImageView;
				
				@Override
				public View createView(LayoutInflater inflater) {
					// TODO Auto-generated method stub
					View view = inflater.inflate(R.layout.news_scroll_item_layout, null);
					mImageView = (ImageView) view.findViewById(R.id.news_scroll_icon);
					mTitle = (TextView) view.findViewById(R.id.news_scroll_main_title);
					mSubTitle = (TextView) view.findViewById(R.id.news_scroll_sub_title);
					return view;
				}

				@Override
				public void setItemData(int position, final NewsInfo data) {
					// TODO Auto-generated method stub
					if (data != null) {
						mTitle.setText(data.mTitle);
						mLoader.displayImage(data.mIconUrl, mImageView, mOptions);
						if (TextUtils.isEmpty(data.mContentType)) return;
						mImageView.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								int type = Integer.valueOf(data.mContentType);
								switch (type) {
								case 0:
								case 2:
									if (getContext() instanceof MenuBaseActivity) {
										((MenuBaseActivity) getContext())
												.startHtmlActivity(data.mContentUrl);
									}
									break;
								case 1:
									startPDFActivity(data.mContentUrl, data.mTitle);
									break;
								case 3:
									startImageScanActivity(0, data.mImgs, data.mId);
									break;

								default:
									break;
								}
							}
						});
						
						
					}
				}
				
			};
		}
		
	}
	
	private void initViewPagerScroll() {
        try {
            Field mScroller = null;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            mViewPagerScroller = new ViewPagerScroller(
            		mViewPager.getContext());
//			scroller.setScrollDuration(1500);
            mScroller.set(mViewPager, mViewPagerScroller);
            mViewPager.setScroller(mViewPagerScroller);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
	
	public enum Transformer {
        DefaultTransformer("DefaultTransformer"), AccordionTransformer(
                "AccordionTransformer"), BackgroundToForegroundTransformer(
                "BackgroundToForegroundTransformer"), CubeInTransformer(
                "CubeInTransformer"), CubeOutTransformer("CubeOutTransformer"), DepthPageTransformer(
                "DepthPageTransformer"), FlipHorizontalTransformer(
                "FlipHorizontalTransformer"), FlipVerticalTransformer(
                "FlipVerticalTransformer"), ForegroundToBackgroundTransformer(
                "ForegroundToBackgroundTransformer"), RotateDownTransformer(
                "RotateDownTransformer"), RotateUpTransformer(
                "RotateUpTransformer"), StackTransformer("StackTransformer"), TabletTransformer(
                "TabletTransformer"), ZoomInTransformer("ZoomInTransformer"), ZoomOutSlideTransformer(
                "ZoomOutSlideTransformer"), ZoomOutTranformer(
                "ZoomOutTranformer");

        private final String mClassName;  
        
        Transformer(String className) {
            mClassName = className;
        }

        public String getClassName() {
            return mClassName;
        }
    }
	
	private void setPageTransformer(Transformer transformer) {
        try {
            String pkName = getContext().getPackageName();
            mViewPager.setPageTransformer(true, (PageTransformer) Class.forName(/*pkName*/"com.android.hcframe" +
					".transforms." + transformer.getClassName()).newInstance());
        } catch (Exception e) {
            HcLog.D(TAG + " setPageTransformer e = "+e);
        }
    }
	
	public void resume() {
		if (mAdHander != null)
			mAdHander.resume();
	}
	
	public void pause() {
		HcLog.D(TAG + " #pause AdHander = "+mAdHander);
		if (mAdHander != null)
			mAdHander.pause();
	}
	
	private void startImageScanActivity(int position, ArrayList<String> iamgeUrls,
			String newsId) {
		Intent intent = new Intent(getContext(), ImageScanActivity.class);
		// 图片url,为了演示这里使用常量，一般从数据库中或网络中获取
		intent.putExtra(ImageScanActivity.EXTRA_IMAGE_URLS, iamgeUrls);
		intent.putExtra(ImageScanActivity.EXTRA_IMAGE_INDEX, position);
		intent.putExtra(ImageScanActivity.EXTRA_IMAGE_ID, newsId);
		getContext().startActivity(intent);
	}
	
	private void startPDFActivity(String url, String title) {
		Intent intent = new Intent(getContext(), DownloadPDFActivity.class);
		intent.putExtra("url", url);
		intent.putExtra("title", title);
		getContext().startActivity(intent);
//		mContext.overridePendingTransition(0, 0);
	}

	//触碰控件的时候，翻页应该停止，离开的时候如果之前是开启了翻页的话则重新启动翻页
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		int action = ev.getAction();
		if (action == MotionEvent.ACTION_UP||action == MotionEvent.ACTION_CANCEL||action == MotionEvent.ACTION_OUTSIDE) {
			// 开始翻页
			if (mAdHander != null) {
//				mAdHander.pause();
				mAdHander.resume();
			}
		} else if (action == MotionEvent.ACTION_DOWN) {
			// 停止翻页
			if (mAdHander != null) {
				mAdHander.pause();
			}
		}
		return super.dispatchTouchEvent(ev);
	}
}
