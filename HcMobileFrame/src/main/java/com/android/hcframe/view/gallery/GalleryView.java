package com.android.hcframe.view.gallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.hcframe.BigImageActivity;
import com.android.hcframe.HcApplication;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.view.selector.file.image.ImageChooseActivity;
import com.android.hcframe.view.selector.file.image.ImageItemInfo;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-11-29 09:49.
 */

public class GalleryView extends LinearLayout {

    private static final String TAG = "GalleryView";

    private RecyclerView mRecyclerView;

    private List<GalleryItemInfo> mImages = new ArrayList<GalleryItemInfo>();

    private DisplayImageOptions mOptions;

    private GalleryRecycleAdapter mRecycleAdapter;

    private GalleryItemInfo mCamera;

    private static final int MAX_COUNT = 9;

    private int mMaxCount = MAX_COUNT;

    /**
     * 是否显示添加图片的按钮
     */
    private boolean mShowCamera;

    /** 添加图片的按钮是否在第一个 */
    private boolean mAddFirst;

    /** 点击添加按钮的时候是否显示图片 */
    private boolean mShowImages;

    public GalleryView(Context context) {
        this(context, null);
    }

    public GalleryView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GalleryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.GalleryView, defStyleAttr, 0);
        mMaxCount = a.getInt(R.styleable.GalleryView_max_count, MAX_COUNT);
        mShowCamera = a.getBoolean(R.styleable.GalleryView_show_camera, true);
        mAddFirst = a.getInt(R.styleable.GalleryView_btn_position, 0) == 0;
        mShowImages = a.getBoolean(R.styleable.GalleryView_show_images, true);
        a.recycle();
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.gallery_view_layout, this, true);
        mRecyclerView = (RecyclerView) findViewById(R.id.gallery_view_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        mOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageOnLoading(R.drawable.photo_empty)
                .showImageForEmptyUri(R.drawable.photo_empty)
                .showImageOnFail(R.drawable.photo_empty)
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888).build();
        mCamera = new GalleryItemInfo();
        mCamera.mSeleted = false;
        mCamera.mUri = "drawable://" + R.drawable.gallery_item_camera_icon;
        if (mShowCamera)
            mImages.add(mCamera);
        createAdapter();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        HcLog.D(TAG + " #onActivityResult requestCode ="+requestCode + " resultCode ="+resultCode + " data = "+data);
        if (resultCode == Activity.RESULT_OK && requestCode == HcUtil.REQUEST_CODE_FROM_CAMERA) {
            if (data != null && data.getExtras() != null) {
                ImageItemInfo info = data.getParcelableExtra("image");
                if (info != null) {
                    addItem("file://" + info.getImagePath());
//                    GalleryItemInfo itemInfo = new GalleryItemInfo();
//                    itemInfo.mUri = "file://" + info.getImagePath();
//                    itemInfo.mSeleted = true;
//                    if (mAddFirst) {
//                        mImages.add(itemInfo);
//                    } else {
//                        mImages.add(mImages.size() - 1, itemInfo);
//                    }
//
//                    if (mRecycleAdapter != null)
//                        mRecycleAdapter.notifyDataSetChanged();
                } else {
                    ArrayList<ImageItemInfo> infos = data.getParcelableArrayListExtra("images");
                    if (infos != null) {
                        GalleryItemInfo itemInfo;
                        for (ImageItemInfo imageItemInfo : infos) {
                        	itemInfo = new GalleryItemInfo();
                            itemInfo.mUri = "file://" + imageItemInfo.getImagePath();
                            itemInfo.mSeleted = true;
                            if (mAddFirst) {
                                mImages.add(itemInfo);
                            } else {
                                mImages.add(mImages.size() - 1, itemInfo);
                            }
                        }
                        if (mRecycleAdapter != null)
                            mRecycleAdapter.notifyDataSetChanged();
                    }
                }
            } else if (data == null) { // 拍照返回
                addItem("file://" + mImagePath);
            }
        }
    }

    private void createAdapter() {
        createAdapter(R.layout.gallery_view_item_layout, R.id.gallery_item_image, R.id.gallery_item_delete_image);
    }

    public void createAdapter(int layoutResId, int imageResId, int deleteResId) {

        mRecycleAdapter = new GalleryRecycleAdapter(layoutResId, imageResId, deleteResId);
        mRecyclerView.setAdapter(mRecycleAdapter);
    }

    public static class GalleryItemInfo {

        /** 文件的本地路径或者网络上的url */
        public String mUri;
        public boolean mSeleted;
    }

    private class GalleryRecycleAdapter extends RecyclerView.Adapter<GalleryView.ViewHolder> {

        private int mLayoutId;

        private int mImageId;

        private int mdeleteId;

        public GalleryRecycleAdapter(int layoutId, int imageId, int deleteId) {
            mLayoutId = layoutId;
            mImageId = imageId;
            mdeleteId = deleteId;
        }


        @Override
        public int getItemCount() {
            return mImages.size();
        }

        @Override
        public GalleryView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            HcLog.D(TAG + " $ChooseRecycleAdapter#onCreateViewHolder!!!!!");
            return new GalleryView.ViewHolder(LayoutInflater.from(getContext()).inflate(mLayoutId, viewGroup, false), mImageId, mdeleteId);
        }

        @Override
        public void onBindViewHolder(final GalleryView.ViewHolder viewHolder, final int position) {
            HcLog.D(TAG + " $ChooseRecycleAdapter#onBindViewHolder viewHolder="+viewHolder + " position = "+position);
            final GalleryItemInfo info = mImages.get(position);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HcLog.D(TAG + " $GalleryRecycleAdapter#onBindViewHolder#onClick position = " + position + " info name = " + info.mUri);
                    if (info == mCamera) {
                        startImageChooseActivity();
                    } else {
                        /**
                         * @jrjin
                         * @date 2017.03.07
                          *@deprecated 删除按钮会一直显示
                         *
                        if (mShowCamera) {
                            if (info.mSeleted) {
                                info.mSeleted = false;
                                viewHolder.mDelete.setVisibility(View.GONE);
                            } else {
                                startBigImageActivity(info.mUri);
                            }
                        } else {
                            startBigImageActivity(info.mUri);
                        }
                         */
                        startBigImageActivity(info.mUri);
                    }
                }
            });
            /**
             * @jrjin
             * @date 2017.03.07
             * @deprecated 没有长按的功能了,删除按钮需要一直显示

            if (mShowCamera) {
                viewHolder.itemView.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        if (position > 0 && !info.mSeleted) {
                            info.mSeleted = true;
                            viewHolder.mDelete.setVisibility(View.VISIBLE);
                        }
                        return true;
                    }
                });
            }
             */

            viewHolder.mDelete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mImages.remove(info);
                    mRecycleAdapter.notifyItemRemoved(viewHolder.getPosition());
                }
            });

            if (info.mSeleted) {
                if (viewHolder.mDelete.getVisibility() != View.VISIBLE) {
                    viewHolder.mDelete.setVisibility(View.VISIBLE);
                }
            } else {
                if (viewHolder.mDelete.getVisibility() != View.GONE) {
                    viewHolder.mDelete.setVisibility(View.GONE);
                }
            }
            if (info == mCamera) {
                viewHolder.mImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                viewHolder.mImage.setBackgroundResource(R.drawable.gallery_item_bg);
            } else {
                viewHolder.mImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                viewHolder.mImage.setBackgroundColor(Color.TRANSPARENT);
            }
            ImageLoader.getInstance().displayImage(info.mUri, viewHolder.mImage, mOptions);
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView mImage;

        ImageView mDelete;

        public ViewHolder(View itemView, int imageResId, int deleteResId) {
            super(itemView);

            mImage = (ImageView) itemView.findViewById(imageResId);
            mDelete = (ImageView) itemView.findViewById(deleteResId);
        }
    }

    private void startImageChooseActivity() {
        int offset = mMaxCount - mImages.size() + 1; // 第一项要去除
        Context context = getContext();
        if (offset <= 0) {
            HcUtil.showToast(context, "图片已达到最大张数!");
            return;
        }
        if (mShowImages) {
            Intent intent = new Intent(context, ImageChooseActivity.class);
            intent.putExtra(ImageChooseActivity.SELECT_COUNT, offset);
            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, HcUtil.REQUEST_CODE_FROM_CAMERA);
            } else {
                HcLog.D(TAG + " #startImageChooseActivity 出错了,当期的context不是Activity!");
                context.startActivity(intent);
            }
        } else {
            startCamera(context);
        }

    }

    private String mImagePath = null;

    private void startCamera(Context context) {

        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        String directory = HcApplication.getImagePhotoPath();
        String filename = System.currentTimeMillis() + ".jpg";
        File dir = new File(directory);
        if (!dir.exists()) dir.mkdirs();
        File file = new File(directory, filename);
        mImagePath = file.getAbsolutePath();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, HcUtil.REQUEST_CODE_FROM_CAMERA);
        } else {
            HcLog.D(TAG + " #startCamera 出错了,当期的context不是Activity!");
            context.startActivity(intent);
        }
    }

    private void startBigImageActivity(String uri) {
        Intent intent = new Intent(getContext(), BigImageActivity.class);
        intent.putExtra("uri", uri);
        getContext().startActivity(intent);
    }

    public List<GalleryItemInfo> getImages() {
        List<GalleryItemInfo> images = new ArrayList<GalleryItemInfo>(mImages);
        if (mShowCamera) {
            if (mAddFirst) {
                images.remove(0);
            } else {
                images.remove(images.size() - 1);
            }
        }

        return images;
    }

    public void addItem(GalleryItemInfo info) {
        if (!mImages.contains(info)) {
            if (mShowCamera) {
                if (mAddFirst) {
                    mImages.add(info);
                } else {
                    mImages.add(mImages.size() - 1, info);
                }
            } else {
                mImages.add(info);
            }

            if (mRecycleAdapter != null)
                mRecycleAdapter.notifyDataSetChanged();
        }
    }

    private void addItem(String uri) {
        GalleryItemInfo itemInfo = new GalleryItemInfo();
        itemInfo.mUri = uri;
        itemInfo.mSeleted = true;
        if (mAddFirst) {
            mImages.add(itemInfo);
        } else {
            mImages.add(mImages.size() - 1, itemInfo);
        }

        if (mRecycleAdapter != null)
            mRecycleAdapter.notifyDataSetChanged();
    }

    public void addAllItems(List<GalleryItemInfo> infos) {
        if (mShowCamera) {
            if (mAddFirst) {
                mImages.addAll(infos);
            } else {
                mImages.addAll(mImages.size() - 1, infos);
            }
        } else {
            mImages.addAll(infos);
        }

        if (mRecycleAdapter != null)
            mRecycleAdapter.notifyDataSetChanged();
    }

    public void clearItems() {
        mImages.clear();
        if (mShowCamera)
            mImages.add(mCamera);
    }

    /**
     * 设置图片的最大张数
     * @param maxCount
     */
    public void setMaxCount(int maxCount) {
        mMaxCount = maxCount;
        if (mMaxCount <= 0)
            mMaxCount = MAX_COUNT;
    }

    public int getSize() {
        return mShowCamera ? mImages.size() - 1 : mImages.size();
    }

    /**
     * 设置添加图片按钮的uri
     * @param uri "drawable://" + R.drawable.gallery_item_camera_icon
     */
    public void setAddButtonSrc(String uri) {
        if (mCamera != null) {
            mCamera.mUri = uri;
            if (mRecycleAdapter != null)
                mRecycleAdapter.notifyDataSetChanged();
        }
    }
}
