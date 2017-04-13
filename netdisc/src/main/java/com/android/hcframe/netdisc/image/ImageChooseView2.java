package com.android.hcframe.netdisc.image;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.android.frame.download.FileColumn;
import com.android.frame.download.HcDownloadService;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.netdisc.R;
import com.android.hcframe.netdisc.ServiceCallBack;
import com.android.hcframe.netdisc.data.AbstractChooseView;
import com.android.hcframe.netdisc.data.UploadFileInfo;
import com.android.hcframe.netdisc.util.NetdiscUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-8-1 11:23.
 */
public class ImageChooseView2 extends AbstractChooseView implements ServiceConnection, ServiceCallBack.TransferCallback {

    private static final String TAG = "ImageChooseView2";

    private List<NetdiscImageInfo> mInfos = new ArrayList<NetdiscImageInfo>();

    private List<NetdiscImageInfo> mSelected = new ArrayList<NetdiscImageInfo>();

    public ImageChooseView2(Activity context, ViewGroup group, String data) {
        super(context, group, data, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }

    @Override
    public void initAdapter() {
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
            Intent intent = new Intent().setClass(mContext, HcDownloadService.class);
            mContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
            mView = mInflater.inflate(R.layout.netdisc_image_choose_page, null);

            mAbsView = (GridView) mView.findViewById(R.id.image_gridView);
            mDir = (TextView) mView.findViewById(R.id.image_dir_name);
            mCommit = (TextView) mView.findViewById(R.id.image_commit_btn);

            mAbsView.setOnItemClickListener(this);
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
            ArrayList<String> filePath = new ArrayList<>();
            List<FileColumn> fileColumnList = new ArrayList<FileColumn>();
            for (NetdiscImageInfo imageInfo : mSelected) {
                imageInfo.setSelected(false);
                info = new UploadFileInfo();
                info.setFileName(imageInfo.getFileName());
                File file = null;
                try {
                    file = new File(imageInfo.getImagePath());
                } catch (Exception e) {
                    HcLog.D(TAG + " #onClick 创建文件失败 Exception e=" + e);
                }
                String MD5 = file != null ? NetdiscUtil.getFileMD5(file, Long.valueOf(imageInfo.getSize())) : "";
                info.setMd5(MD5);
                info.setFileDir(mDirId);
                info.setFileSize(imageInfo.getSize());
                info.setFilePath(imageInfo.getImagePath());
                info.setFileExt(getExtByPath(imageInfo.getImagePath()));
                info.setFileKey(HcUtil.getMD5String(info.getFilePath() + "_" + info.getFileSize() + "_" + mDirId));
                infos.add(info);
                filePath.add(imageInfo.getImagePath());
                FileColumn fileColumn = NetdiscUtil.getFileInfo(imageInfo.getImagePath(), mDirId, MD5);
                if (fileColumn != null) {
                    fileColumnList.add(fileColumn);
                }

            }
            Iterator<FileColumn> iterator = fileColumnList.iterator();
            while (iterator.hasNext()) {
                FileColumn fileColumn = iterator.next();
                inSend = Parcel.obtain();
                inSend.writeSerializable(fileColumn);
                try {
                    iBinder.transact(0, inSend, null, IBinder.FLAG_ONEWAY);
                    inSend.recycle();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            mSelected.clear();
            mContext.finish();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        NetdiscImageInfo imageInfo = (NetdiscImageInfo) parent.getItemAtPosition(position);
        HcLog.D(TAG + " #onItemClick view = " + view + " position = " + position);
        if (imageInfo.isSelected()) { // 选中->不选中
            mSelectCount--;
            mSelected.remove(imageInfo);
            view.findViewById(R.id.image_item_selected).setVisibility(View.GONE);
        } else {
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
            mCommit.setEnabled(true);
            mCommit.setText("立即上传(" + mSelectCount + ")");
        } else {
            mCommit.setEnabled(false);
            mCommit.setText("立即上传");
        }
    }


    Parcel inSend;
    IBinder iBinder;
    HcDownloadService mService;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (name.getShortClassName().endsWith("HcDownloadService")) {
            this.iBinder = service;
            mService = ((HcDownloadService.MyBind) iBinder).getMyService();
            ServiceCallBack.getInstance().getService(mService);
            ServiceCallBack.getInstance().setTransferCallback(this);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    public void onDestory() {
        mContext.unbindService(this);
        super.onDestory();

    }

    @Override
    public void transferCallback(FileColumn fileColumn) {

    }
}
