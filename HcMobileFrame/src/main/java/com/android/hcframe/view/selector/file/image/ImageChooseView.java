package com.android.hcframe.view.selector.file.image;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.pcenter.headportrait.CropImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-10-9 14:20.
 */

public class ImageChooseView extends AbstractChooseView {

    private static final String TAG = "IMImageChooseView";

    private List<ImageItemInfo> mInfos = new ArrayList<ImageItemInfo>();

    private List<ImageItemInfo> mSelected = new ArrayList<ImageItemInfo>();

    private final boolean mMultipled;

    private final boolean mCrop;

    private static final int DEFAULT_SIZE = 9;

    private int mCount = DEFAULT_SIZE;


    /**
     * 图片选择的组件
     * @param context
     * @param group
     * @param data 一般的数据,一般可以为appId,或者appName
     */
    public ImageChooseView(Activity context, ViewGroup group, String data) {
        this(context, group, data, false, false, 0);
    }

    /**
     * 图片选择的组件
     * @param context
     * @param group
     * @param data 一般的数据,一般可以为appId,或者appName
     * @param count 多选时最多选择的数量
     */
    public ImageChooseView(Activity context, ViewGroup group, String data, int count) {
        this(context, group, data, true, false, count);
    }

    /**
     * 图片选择的组件
     * @param context
     * @param group
     * @param data 一般的数据,一般可以为appId,或者appName
     * @param multipled 是否为多选
     * @param crop 是否剪切,多选就不剪辑
     */
    public ImageChooseView(Activity context, ViewGroup group, String data, boolean multipled, boolean crop) {
        this(context, group, data, multipled, crop, DEFAULT_SIZE);
    }

    /**
     *
     * @param context
     * @param group
     * @param data 一般的数据,一般可以为appId,或者appName
     * @param multipled 是否为多选
     * @param crop 是否剪切,多选就不剪辑
     * @param count 多选时最多选择的数量
     */
    public ImageChooseView(Activity context, ViewGroup group, String data, boolean multipled, boolean crop, int count) {
        super(context, group, data, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        mMultipled = multipled;
        mCrop = crop;
        mCount = count;
        if (mMultipled) {
            if (mCount <= 0)
                mCount = DEFAULT_SIZE;
        } else {
            mCount = 0;
        }
    }

    @Override
    public void initAdapter() {
        ImageItemInfo info = new ImageItemInfo();
        info.setThumbnailsUri("drawable://" + R.drawable.takeup);
        mInfos.add(0, info);
        mAdapter = new ImageAdpater(mContext, mInfos);
    }

    @Override
    public void scanFiles() {
        ContentResolver cr = mContext.getContentResolver();
        String[] projection = {MediaStore.Images.Media._ID, //图片 id，从 1 开始自增
                MediaStore.Images.Media.DATA, // 图片绝对路径
                MediaStore.Images.Media.TITLE, //不带扩展名的文件名
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.SIZE};
        Cursor c = cr.query(mScanUrl, projection, null, null, null);
        if (c != null && c.getCount() > 0) {
            HcLog.D(TAG + " scanImages count =" + c.getCount());
            c.moveToFirst();
            ImageItemInfo info = null;
            File file = null;
            while (!c.isAfterLast()) {
                int imageId = c.getInt(0);
                String data = c.getString(1);
                int height = c.getInt(3);
                int width = c.getInt(4);
                String name = c.getString(2);
                long size = c.getLong(5);
//                HcLog.D(TAG + "#scanImages imageId ="+imageId + " data="+data
//                        + " height ="+height + " width = "+width + " name = "+name + " size = "+size);
                if (width < 100 || height < 100 || height * width < 10 * 1024) {
                    c.moveToNext();
                    continue;
                }
                file = new File(data);
                if (!file.exists()) {
                    c.moveToNext();
                    file = null;
                    continue;
                }
                info = new ImageItemInfo();
                /** 注意这里没有增加"file://"  */
                info.setFilePath(data); // 这里要是为了适应图片加载框架,需要增加file://
//                HcLog.D(TAG + "#scanImages uri = " + ContentUris.withAppendedId(IMAGE_URI, imageId).toString());
                info.setThumbnailsUri(ContentUris.withAppendedId(mScanUrl, imageId).toString());
                info.setImageUri(ContentUris.withAppendedId(mScanUrl, imageId));
                info.setImageId(imageId);
                info.setFileName(name);
                info.setSize(size + "");
                synchronized (mInfos) {
                    mInfos.add(info);
                }
                c.moveToNext();
            }
        }

        if (c != null) {
            c.close();
            c = null;
        }

        HcLog.D(TAG + " #scanFiles infos size = " + mInfos.size());
    }

    @Override
    public void setContentView() {
        if (mView == null) {
            mView = mInflater.inflate(R.layout.file_image_choose_page, null);

            mAbsView = (GridView) mView.findViewById(R.id.file_image_gridView);

            mAbsView.setOnItemClickListener(this);
            mSend.setOnClickListener(this);
        }
    }

    @Override
    public void update(Observable observable, Object data) {

    }

    @Override
    public void onClick(View v) {
        if (v == mSend) {
            Intent intent = new Intent();
//            String[] selected = new String[mSelectCount];
//            for (int i = 0; i < mSelectCount; i++) {
//            	selected[i] = mSelected.get(i).getImagePath();
//            }
//            intent.putExtra("path", selected);
            intent.putParcelableArrayListExtra("images", new ArrayList<ImageItemInfo>(mSelected));
            mContext.setResult(Activity.RESULT_OK, intent);
            mSelected.clear();
            mInfos.clear();
            mContext.finish();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            startCamera();
        } else {
            ImageItemInfo imageInfo = (ImageItemInfo) parent.getItemAtPosition(position);
            HcLog.D(TAG + " #onItemClick view = " + view + " position = " + position);
            if (!mMultipled) {
                if (mCrop) {
                    startCrop(imageInfo.getImageUri(), null);
                } else {
                    Intent intent = new Intent();
//                    intent.putExtra("path", new String[] {imageInfo.getImagePath()});
                    imageInfo.setSelected(false);
                    intent.putExtra("image", imageInfo);
                    mContext.setResult(Activity.RESULT_OK, intent);
                    mContext.finish();
                }

                return;
            }

            if (imageInfo.isSelected()) { // 选中->不选中
                mSelectCount--;
                mSelected.remove(imageInfo);
                view.findViewById(R.id.image_item_selected).setVisibility(View.GONE);
            } else {
                if (mSelectCount == mCount) return;
                mSelectCount++;
                mSelected.add(imageInfo);
                view.findViewById(R.id.image_item_selected).setVisibility(View.VISIBLE);
            }
            imageInfo.setSelected(!imageInfo.isSelected());
            if (mSelectCount < 0) {
                mSelectCount = 0;
            } else if (mSelectCount > mInfos.size()) {
                mSelectCount = mInfos.size();
            }
            if (mSelectCount > 0) {
                mSend.setEnabled(true);
                mSend.setText("发送(" + mSelectCount + "/" + mCount + ")");
            } else {
                mSend.setEnabled(false);
                mSend.setText("发送");
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case Activity.RESULT_OK:
                switch (requestCode) {
                    case HcUtil.REQUEST_CODE_FROM_CAMERA:
                        Intent intent = new Intent();
//                        String[] image = new String[1];
                        if (data == null) {
                            // 使用 mImagePath
                            if (mCrop) {
                                startCrop(Uri.fromFile(new File(mImagePath)), null);
                                return;
                            } else {
//                                image[0] = mImagePath;
//                                intent.putExtra("path", image);
                                ImageItemInfo info = new ImageItemInfo();
                                info.setSelected(false);
                                info.setFilePath(mImagePath);
                                intent.putExtra("image", info);
                                mContext.setResult(Activity.RESULT_OK, intent);
                                mContext.finish();
                            }

                        } else if (data.getData() != null) {
                            // 使用 data.getData()
                            if (mCrop) {
                                startCrop(data.getData(), null);
                            }
                        } else if (data.getExtras() != null) {
                            // 使用 (Bitmap) data.getExtras().get("data")
                            if (mCrop) {
                                startCrop(null, (Bitmap) data.getExtras().get("data"));
                            }
                        }
                        if (data != null) {
                            HcLog.D(TAG + " onActivityResult uri = "+data.getData() + " extras = "+data.getExtras());
                        }
                        break;
                    case HcUtil.REQUEST_CODE_FROM_CROP:
                        mContext.setResult(Activity.RESULT_OK, data);
                        mContext.finish();
                        break;

                    default:
                        break;
                }
                break;

            default:
                break;
        }
    }

    private String mImagePath = null;

    private void startCamera() {

        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        String directory = HcApplication.getImagePhotoPath();
        String filename = System.currentTimeMillis() + ".jpg";
        File dir = new File(directory);
        if (!dir.exists()) dir.mkdirs();
        File file = new File(directory, filename);
        mImagePath = file.getAbsolutePath();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        mContext.startActivityForResult(intent, HcUtil.REQUEST_CODE_FROM_CAMERA);
    }

    private static final int CROP_WIDTH = 120;

    private void startCrop(Uri uri, Bitmap data) {
        Intent intent = new Intent(mContext, CropImage.class);
//		intent.setAction("com.android.camera.action.CROP");
//		intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        if (data != null) {
            intent.putExtra("data", data);
        }
        if (uri != null) {
            intent.setDataAndType(uri, "image/*");
        }
        //下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", (int)(CROP_WIDTH * mContext.getResources().getDisplayMetrics().density));
        intent.putExtra("outputY", (int)(CROP_WIDTH * mContext.getResources().getDisplayMetrics().density));
        intent.putExtra("return-data", true);
        intent.putExtra("scale", true);
        mContext.startActivityForResult(intent, HcUtil.REQUEST_CODE_FROM_CROP);
    }
}
