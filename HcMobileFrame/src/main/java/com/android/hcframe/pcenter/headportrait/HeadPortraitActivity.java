/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-23 上午9:44:48
*/
package com.android.hcframe.pcenter.headportrait;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.TopBarView;

public class HeadPortraitActivity extends HcBaseActivity implements OnItemClickListener {

	private static final String TAG = "HeadPortraitActivity";
	
	private static final Uri THUMB_URI_EXTERNAL = Images.Thumbnails.EXTERNAL_CONTENT_URI;
	
	private static final Uri THUMB_URI_INTERNAL = Images.Thumbnails.INTERNAL_CONTENT_URI;
	
	private static final Uri IMAGE_URI = Images.Media.EXTERNAL_CONTENT_URI;
	
	private Thread mScanThread;
	
	private List<ImageInfo> mInfos = new ArrayList<ImageInfo>();
	
	private PhotoAdapter mAdapter;
	
	private Handler mHandler = new Handler();
	
	private TopBarView mTopBarView;
	
	private GridView mGridView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_home_layout);
		initViews();
		initData();
		start();
	}

	private void initViews() {
		mTopBarView = (TopBarView) findViewById(R.id.photo_top_bar);
		mGridView = (GridView) findViewById(R.id.photo_gridview);
		mGridView.setOnItemClickListener(this);
	}
	
	private void initData() {
		mTopBarView.setReturnBtnIcon(R.drawable.center_close);
		mTopBarView.setTitle(getString(R.string.modify_photo));
		mAdapter = new PhotoAdapter(this, mInfos);
		ImageInfo imageInfo = new ImageInfo();
		imageInfo.setFilePath("");
		imageInfo.setImageUri(THUMB_URI_EXTERNAL);
		imageInfo.setThumbnailsUri("");
		mInfos.add(/*new ImageInfo()*/imageInfo);
		mGridView.setAdapter(mAdapter);
	}
	
	private void scanThumbnails(Uri uri) {
		ContentResolver cr = getContentResolver();
		String[] projection = {Images.Thumbnails._ID, Images.Thumbnails.IMAGE_ID,
				Images.Thumbnails.DATA, /*Images.Thumbnails.THUMB_DATA,*/ 
				Images.Thumbnails.HEIGHT, Images.Thumbnails.WIDTH};
		Cursor c = cr.query(uri, projection, null, null, null);
		if (c != null && c.getCount() > 0) {
			HcLog.D(TAG + " scanThumbnails count =" +c.getCount());
			c.moveToFirst();
			ImageInfo info = null;
			File file = null;
			while (!c.isAfterLast()) {
				int thumbId = c.getInt(0);
				int imageId = c.getInt(1);
				String data = c.getString(2);
				String thumbnailsUri = ContentUris.withAppendedId(uri, thumbId).toString();
				int height = c.getInt(3);
				int width = c.getInt(4);
				HcLog.D(TAG + " thumbId ="+thumbId + " imageId ="+imageId + " data="+data
						+ " thumbIdData = "+thumbnailsUri + " height ="+height + " width = "+width);
				if (height * width < 10 * 1024) {
					c.moveToNext();
					continue;
				}
				file = new File(data);
				if (!file.exists()) {
					c.moveToNext();
					file = null;
					continue;
				}
				info = new ImageInfo();
				info.setFilePath("file://" + data); // 这里主要是为了适应图片加载框架
				info.setThumbnailsUri(thumbnailsUri);
				info.setImageUri(ContentUris.withAppendedId(IMAGE_URI, imageId));
				info.setImageId(imageId);
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
	}
	
	private void start() {
		if (mScanThread != null) {
            return;
        }
        Thread t = new Thread(new WorkerThread());
        t.setName("image-scan");
        mScanThread = t;
        t.start();
	}
	
	private class WorkerThread implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			scanImages(IMAGE_URI);
//			scanThumbnails(THUMB_URI_EXTERNAL);
//			scanThumbnails(THUMB_URI_INTERNAL);
//			scanImages();
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (mAdapter != null) {
						HcLog.D(TAG + " $WorkerThread#run list size = "+mInfos.size());
						mAdapter.notifyDataSetChanged();
						stop();
					}
				}
			});
		}
		
	}
	
	private void stop() {
		if (mScanThread != null) {
            try {
                Thread t = mScanThread;
                t.join();
                mScanThread = null;
            } catch (InterruptedException ex) {
                // so now what?
            }
        }
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		stop();
		super.onDestroy();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		ImageInfo info = (ImageInfo) parent.getItemAtPosition(position);
		if (position == 0) { // 拍照
			startCamera();
		} else { // 裁剪
			startCrop(info.getImageUri(), null);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		HcLog.D(TAG + " onActivityResult requestCode="+requestCode + " resultCode="+resultCode + " data = "+data);
//		if (data == null) return;
		switch (resultCode) {
		case RESULT_OK:
			switch (requestCode) {
			case HcUtil.REQUEST_CODE_FROM_CAMERA:
				
				if (data == null) {
					startCrop(Uri.fromFile(new File(mImagePath)), null);
				} else if (data.getData() != null) {
					startCrop(data.getData(), null);
				} else if (data.getExtras() != null) {
					startCrop(null, (Bitmap) data.getExtras().get("data"));
				}
				if (data != null) {
					HcLog.D(TAG + " onActivityResult uri = "+data.getData() + " extras = "+data.getExtras());
				}
//				Bundle bundle = data.getExtras();
//				if (bundle != null) {
//					for (String key : bundle.keySet()) {
//						HcLog.D(TAG + " key = "+key);
//					}
//				}
				break;
			case HcUtil.REQUEST_CODE_FROM_CROP:
				setResult(RESULT_OK, data);
				finish();
				break;

			default:
				break;
			}
			break;

		default:
			break;
		}
	}

	private static final int CROP_WIDTH = 120;
	
	private void startCrop(Uri uri, Bitmap data) {
		Intent intent = new Intent(this, CropImage.class); 
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
        intent.putExtra("outputX", (int)(CROP_WIDTH * getResources().getDisplayMetrics().density));  
        intent.putExtra("outputY", (int)(CROP_WIDTH * getResources().getDisplayMetrics().density));  
        intent.putExtra("return-data", true); 
        intent.putExtra("scale", true);
        startActivityForResult(intent, HcUtil.REQUEST_CODE_FROM_CROP);
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
		startActivityForResult(intent, HcUtil.REQUEST_CODE_FROM_CAMERA);
	}
	
//	public void startCamera() {
//		Intent intent = new Intent();
//		intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
//		startActivityForResult(intent, HcUtil.REQUEST_CODE_FROM_CAMERA);
//	}

	private void scanImages(Uri uri) {
		ContentResolver cr = getContentResolver();
		String[] projection = {Images.Media._ID, //图片 id，从 1 开始自增
				Images.Media.DATA, // 图片绝对路径
				Images.Media.TITLE, //不带扩展名的文件名
				Images.Media.HEIGHT,
				Images.Media.WIDTH};
		Cursor c = cr.query(uri, projection, null, null, null);
		if (c != null && c.getCount() > 0) {
			HcLog.D(TAG + " scanImages count =" +c.getCount());
			c.moveToFirst();
			ImageInfo info = null;
			File file = null;
			while (!c.isAfterLast()) {
				int imageId = c.getInt(0);
				String data = c.getString(1);
				int height = c.getInt(3);
				int width = c.getInt(4);
				HcLog.D(TAG + "#scanImages imageId ="+imageId + " data="+data
						+ " height ="+height + " width = "+width);
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
				info = new ImageInfo();
				info.setFilePath("file://" + data); // 这里主要是为了适应图片加载框架
				HcLog.D(TAG + "#scanImages uri = "+ContentUris.withAppendedId(IMAGE_URI, imageId).toString());
				info.setThumbnailsUri(ContentUris.withAppendedId(IMAGE_URI, imageId).toString());
				info.setImageUri(ContentUris.withAppendedId(IMAGE_URI, imageId));
				info.setImageId(imageId);
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
	}

	private void scanImages() {
		ContentResolver cr = getContentResolver();
		String[] projection = {Images.Thumbnails._ID, Images.Thumbnails.IMAGE_ID,
				Images.Thumbnails.DATA, /*Images.Thumbnails.THUMB_DATA,*/
				Images.Thumbnails.HEIGHT, Images.Thumbnails.WIDTH};
		Uri uri = MediaStore.Images.Thumbnails.getContentUri("external");
		HcLog.D(TAG + " #scanImages uri = "+uri);
		Cursor c = Images.Thumbnails.queryMiniThumbnails(cr, uri,
				Images.Thumbnails.MINI_KIND, projection);

		if (c != null && c.getCount() > 0) {
			HcLog.D(TAG + " scanImages count =" +c.getCount());
			c.moveToFirst();
			ImageInfo info = null;
			File file = null;
			while (!c.isAfterLast()) {
				int thumbId = c.getInt(0);
				int imageId = c.getInt(1);
				String data = c.getString(2);
				String thumbnailsUri = ContentUris.withAppendedId(uri, thumbId).toString();
				int height = c.getInt(3);
				int width = c.getInt(4);
				HcLog.D(TAG + " thumbId ="+thumbId + " imageId ="+imageId + " data="+data
						+ " thumbIdData = "+thumbnailsUri + " height ="+height + " width = "+width);
				if (height * width < 10 * 1024) {
					c.moveToNext();
					continue;
				}
				file = new File(data);
				if (!file.exists()) {
					c.moveToNext();
					file = null;
					continue;
				}
				info = new ImageInfo();
				info.setFilePath("file://" + data); // 这里主要是为了适应图片加载框架
				info.setThumbnailsUri(thumbnailsUri);
				info.setImageUri(ContentUris.withAppendedId(IMAGE_URI, imageId));
				info.setImageId(imageId);
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

	}
}
