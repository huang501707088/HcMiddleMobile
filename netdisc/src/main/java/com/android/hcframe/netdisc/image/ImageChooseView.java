package com.android.hcframe.netdisc.image;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.netdisc.MySkydriveActivity;
import com.android.hcframe.netdisc.R;
import com.android.hcframe.netdisc.data.UploadFileInfo;
import com.android.hcframe.netdisc.service.HcNetDiscService;
import com.android.hcframe.netdisc.util.NetdiscUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-7-28 10:02.
 */
public class ImageChooseView extends AbstractPage implements AdapterView.OnItemClickListener {

    private static final String TAG = "ImageChooseView";

    private GridView mGridView;

    private TextView mDir;

    private TextView mCommit;

    /** 当前上传的文件夹 */
    private String mFileDir;

    private static final Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    private Thread mScanThread;

    private List<NetdiscImageInfo> mInfos = new ArrayList<NetdiscImageInfo>();

    private Handler mHandler = new Handler();

    private ImageAdpater mAdapter;

    private int mSelectCount;
    /** 当前上传的文件夹的ID */
    private String mDirId;

    private List<NetdiscImageInfo> mSelected = new ArrayList<NetdiscImageInfo>();


    public ImageChooseView(Activity context, ViewGroup group, String data) {
        super(context, group);
        parseJson(data);
    }

    @Override
    public void initialized() {
        if (isFirst) {
            isFirst = !isFirst;
            mDir.setText(mContext.getResources().getString(R.string.netdisc_upload_position) + "  " + mFileDir);
            mCommit.setEnabled(false);
            mAdapter = new ImageAdpater(mContext, mInfos);
            mGridView.setAdapter(mAdapter);
            start();
        }

    }

    @Override
    public void setContentView() {
        if (mView == null) {
            mView = mInflater.inflate(R.layout.netdisc_image_choose_page, null);

            mGridView = (GridView) mView.findViewById(R.id.image_gridView);
            mDir = (TextView) mView.findViewById(R.id.image_dir_name);
            mCommit = (TextView) mView.findViewById(R.id.image_commit_btn);

            mGridView.setOnItemClickListener(this);
            mCommit.setOnClickListener(this);
        }

    }

    @Override
    public void update(Observable observable, Object data) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.image_commit_btn) {
            ArrayList<UploadFileInfo> infos = new ArrayList<UploadFileInfo>();
            UploadFileInfo info;
            for (NetdiscImageInfo imageInfo : mSelected) {
                imageInfo.setSelected(false);
            	info = new UploadFileInfo();
                info.setFileName(imageInfo.getFileName());
                File file = null;
                try {
                    file = new File(imageInfo.getImagePath());
                } catch(Exception e) {
                    HcLog.D(TAG + " #onClick 创建文件失败 Exception e="+e);
                }
                info.setMd5(file != null ? NetdiscUtil.getFileMD5(file, Long.valueOf(imageInfo.getSize())) : "");
                info.setFileDir(mDirId);
                info.setFileSize(imageInfo.getSize());
                info.setFilePath(imageInfo.getImagePath());
                info.setFileExt(getExtByPath(imageInfo.getImagePath()));
                info.setFileKey(HcUtil.getMD5String(info.getFilePath() + "_" + info.getFileSize() + "_" + mDirId));
                infos.add(info);
            }
            Intent intent = new Intent();
            intent.setPackage(mContext.getPackageName());
            intent.setAction(HcNetDiscService.UPLOAD);
            intent.putParcelableArrayListExtra(HcNetDiscService.UPLOAD_KEY, infos);
            mContext.startService(intent);
            mSelected.clear();
            mContext.finish();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        NetdiscImageInfo imageInfo = (NetdiscImageInfo) parent.getItemAtPosition(position);
        HcLog.D(TAG + " #onItemClick view = " + view + " position = " + position);
        if (imageInfo.isSelected()) { // 选中->不选中
            mSelectCount --;
            mSelected.remove(imageInfo);
            view.findViewById(R.id.image_item_selected).setVisibility(View.GONE);
        } else {
            mSelectCount ++;
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
            mCommit.setEnabled(true);
            mCommit.setText("立即上传(" + mSelectCount + ")");
        } else {
            mCommit.setEnabled(false);
            mCommit.setText("立即上传");
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
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    if (mAdapter != null) {
                        HcLog.D(TAG + " $WorkerThread#run list size = " + mInfos.size());
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

    private void scanImages(Uri uri) {
        ContentResolver cr = mContext.getContentResolver();
        String[] projection = {MediaStore.Images.Media._ID, //图片 id，从 1 开始自增
                MediaStore.Images.Media.DATA, // 图片绝对路径
                MediaStore.Images.Media.TITLE, //不带扩展名的文件名
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.SIZE};
        Cursor c = cr.query(uri, projection, null, null, null);
        if (c != null && c.getCount() > 0) {
            HcLog.D(TAG + " scanImages count =" +c.getCount());
            c.moveToFirst();
            NetdiscImageInfo info = null;
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
                info = new NetdiscImageInfo();
                /** 注意这里没有增加"file://"  */
                info.setFilePath(data); // 这里主要是为了适应图片加载框架
//                HcLog.D(TAG + "#scanImages uri = " + ContentUris.withAppendedId(IMAGE_URI, imageId).toString());
                info.setThumbnailsUri(ContentUris.withAppendedId(IMAGE_URI, imageId).toString());
                info.setImageUri(ContentUris.withAppendedId(IMAGE_URI, imageId));
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
    }

    @Override
    public void onDestory() {
        stop();
        mGridView.setAdapter(null);
        mAdapter.releaseAdatper();
        mContext = null;
    }

    private void parseJson(String data) {
        try {
            JSONObject object = new JSONObject(data);
            mFileDir = object.getString(MySkydriveActivity.FILE_NAME);
            mDirId = object.getString(MySkydriveActivity.FILE_ID);
        } catch(JSONException e) {
            HcLog.D(TAG + "#parseJson JSONException e ="+e);
        }
    }

    /**
     * 获取文件的扩展名
     * @param filePath
     * @return
     */
    private String getExtByPath(String filePath) {
        int position = filePath.lastIndexOf('.');
        String ext = filePath.substring(position + 1, filePath.length());
        HcLog.D(TAG + " #getExtByPath filePath ="+filePath + " position = "+position + " ext = "+ext);
        return ext;
    }
}
