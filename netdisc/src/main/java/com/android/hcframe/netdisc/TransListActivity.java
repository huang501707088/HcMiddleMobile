package com.android.hcframe.netdisc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.frame.download.HcDownloadService;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.netdisc.netdisccls.DownFragItem;
import com.android.hcframe.netdisc.sql.OperateDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TransListActivity extends FragmentActivity implements ServiceConnection, ServiceCallBack.TransferCallback {
    private TopBarView mTopBarView;
    private ViewPager mPager;
    private ArrayList<Fragment> fragmentList;
    private ImageView imageIcon;
    private TextView implText, downText;
    /**
     * 当前页卡编号
     */
    private int currIndex;
    /**
     * 横线图片宽度
     */
    private int bmpWidth;
    /**
     * 横图片移动的偏移量
     */
    private int offset;
    /**
     * 执行中fragment和已下载fragment
     */
    private ImplFragment implFragment;
    private DownFragment downFragment;
    ProgressCallback progressCallback;
    Parcel inSend;
    IBinder iBinder;
    HcDownloadService mService;
    com.android.frame.download.FileColumn mFileColumn;

//    @Override
//    public void serviceCallback(com.android.frame.download.FileColumn fileColumn) {
//        HcLog.D("TransListActivity:###############################");
//        mFileColumn = fileColumn;
//        progressCallback.serviceCallBack(fileColumn);
//        ServiceCallBack.getInstance().getFileColumn(fileColumn, mService);
//    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (name.getShortClassName().endsWith("HcDownloadService")) {
            this.iBinder = service;
            mService = ((HcDownloadService.MyBind) service).getMyService();
            ServiceCallBack.getInstance().getService(mService);
            ServiceCallBack.getInstance().setTransferCallback(this);
//            mService.setServiceCallback(this);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    public void transferCallback(com.android.frame.download.FileColumn fileColumn) {
        if (progressCallback != null) {
            progressCallback.serviceCallBack(fileColumn);
        }
        if (downloadCallback != null && fileColumn != null && fileColumn.getSuccess() == 1) {
            downloadCallback.notifyDownload();
        }
    }

    public interface ProgressCallback {
        void serviceCallBack(com.android.frame.download.FileColumn fileColumn);
    }

    public void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    DownloadCallback downloadCallback;

    public interface DownloadCallback {
        void notifyDownload();
    }

    public void setDownlaodCallback(DownloadCallback downloadCallback) {
        this.downloadCallback = downloadCallback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.netdisc_trans_list_layout);
        Intent intent = new Intent().setClass(this, HcDownloadService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
        InitTextView();
        InitImage();
        InitViewPager();
    }

    private void InitViewPager() {
        mPager = (ViewPager) findViewById(R.id.viewpager);
        fragmentList = new ArrayList<Fragment>();
        implFragment = new ImplFragment();
        downFragment = new DownFragment();
        fragmentList.add(implFragment);
        implFragment.setActivityCallback(new ImplFragment.ActivityCallback() {
            @Override
            public void activityCallBack(com.android.frame.download.FileColumn fileColumn) {
                inSend = Parcel.obtain();
                inSend.writeSerializable(fileColumn);
                try {
                    iBinder.transact(0, inSend, null, IBinder.FLAG_ONEWAY);
                    inSend.recycle();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        fragmentList.add(downFragment);
        //给ViewPager设置适配器
        downFragment.setNumCallback(new DownFragment.NumCallback() {
            @Override
            public void notifynum(int size) {
                downText.setText("已下载(" + size + ")");
            }
        });
        mPager.setAdapter(new TransListFragmentPagerAdapter(getSupportFragmentManager(), fragmentList));
        mPager.setCurrentItem(0);//设置当前显示标签页为第一页
        mPager.setOnPageChangeListener(new TransListOnPageChangeListener());//页面变化时的监听器
    }

    private void InitImage() {
        imageIcon = (ImageView) findViewById(R.id.cursor);
        bmpWidth = BitmapFactory.decodeResource(getResources(), R.drawable.netdisc_cursor).getWidth();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;
        offset = (screenW / 2 - bmpWidth) / 2;

        //imgageview设置平移，使下划线平移到初始位置（平移一个offset）
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        imageIcon.setImageMatrix(matrix);
    }

    private void InitTextView() {
        mTopBarView = (TopBarView) findViewById(R.id.details_top_bar);
        implText = (TextView) findViewById(R.id.impl_tv);
        downText = (TextView) findViewById(R.id.down_tv);
        mTopBarView.setTitle("传输列表");
//        implText.setText("执行中(3)");
        File dir = new File(HcDownloadService.DWONLOAD_PATH);
        if (dir.exists()) {
            File[] files = dir.listFiles();// 读取
            DownFragItem downFragItem;
            List<DownFragItem> mDownFragItem = new ArrayList<DownFragItem>();
            for (File file : files) {
                downFragItem = new DownFragItem();
                String fileName = file.getName();
                long lastModified = file.lastModified();//最后修改时间
                String time = HcUtil.getDate("yyyy-MM-dd HH:mm:ss", lastModified);
                downFragItem.setDownFileName(fileName);
                downFragItem.setDownTime(time);
                downFragItem.setDownFileM(file.getAbsolutePath());
                mDownFragItem.add(downFragItem);
            }
            List<com.android.frame.download.FileColumn> mDownloading = OperateDatabase.getDownloadList(getApplication());
            Iterator<DownFragItem> iterator = mDownFragItem.iterator();
            while (iterator.hasNext()) {
                DownFragItem downFragItem1 = iterator.next();
                for (int i = 0; i < mDownloading.size(); i++) {
                    if (downFragItem1.getDownFileName().equals(mDownloading.get(i).getName())) {
                        iterator.remove();
                        break;
                    }
                }
            }
            downText.setText("已下载(" + mDownFragItem.size() + ")");
        }
        implText.setTextColor(Color.parseColor("#51afe6"));
        downText.setTextColor(Color.parseColor("#333333"));
        implText.setOnClickListener(new PagerViewListener(0));
        downText.setOnClickListener(new PagerViewListener(1));
    }

    public class TransListOnPageChangeListener implements ViewPager.OnPageChangeListener {
        private int oneOffset = offset * 2 + bmpWidth;//两个相邻页面的偏移量

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            Animation animation = new TranslateAnimation(currIndex * oneOffset, position * oneOffset, 0, 0);//平移动画
            currIndex = position;
            animation.setFillAfter(true);//动画终止时停留在最后一帧，不然会回到没有执行前的状态
            animation.setDuration(200);//动画持续时间0.2秒
            imageIcon.startAnimation(animation);//是用ImageView来显示动画的
            int i = currIndex + 1;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    @Override
    protected void onDestroy() {
        unbindService(this);
        super.onDestroy();
    }

    public class PagerViewListener implements View.OnClickListener {
        private int index = 0;

        public PagerViewListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            mPager.setCurrentItem(index);
            if (index == 0) {
                implText.setTextColor(Color.parseColor("#51afe6"));
                downText.setTextColor(Color.parseColor("#333333"));
            } else if (index == 1) {
                implText.setTextColor(Color.parseColor("#333333"));
                downText.setTextColor(Color.parseColor("#51afe6"));
            }
        }
    }
}
