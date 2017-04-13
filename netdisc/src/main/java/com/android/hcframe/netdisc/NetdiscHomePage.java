package com.android.hcframe.netdisc;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.frame.download.HcDownloadService;
import com.android.hcframe.AbstractPage;
import com.android.hcframe.netdisc.data.SettingSharedHelper;

import java.util.Observable;

/**
 * Created by pc on 2016/6/22.
 */
public class NetdiscHomePage extends AbstractPage {
    private static final String TAG = "NetdiscHomePage";
    private final String mAppId;
    private LinearLayout mySkydriveLinear;
    private LinearLayout myWorkingGroup;
    private LinearLayout myShare;
    private LinearLayout recycleBin;
    private LinearLayout transportList;
    private ImageView switchBtn;
    boolean opened;

    protected NetdiscHomePage(Activity context, ViewGroup group, String appId) {
        super(context, group);
        mAppId = appId;
    }

    @Override
    public void initialized() {

    }

    @Override
    public void setContentView() {
        if (isFirst) {
            isFirst = !isFirst;
            mView = mInflater.inflate(R.layout.netdisc_expand_list, null);//关联布局文件
            mySkydriveLinear = (LinearLayout) mView.findViewById(R.id.my_skydrive);
            myWorkingGroup = (LinearLayout) mView.findViewById(R.id.my_working_group);
            myShare = (LinearLayout) mView.findViewById(R.id.my_share);
            recycleBin = (LinearLayout) mView.findViewById(R.id.recycle_bin);
            transportList = (LinearLayout) mView.findViewById(R.id.transport_list);
            switchBtn = (ImageView) mView.findViewById(R.id.switch_btn);
            initData();
            mySkydriveLinear.setOnClickListener(this);
            myWorkingGroup.setOnClickListener(this);
            myShare.setOnClickListener(this);
            recycleBin.setOnClickListener(this);
            transportList.setOnClickListener(this);
            switchBtn.setOnClickListener(this);
//            Intent intent1 = new Intent(mContext, HcDownloadService.class);
//            mContext.startService(intent1);
        }
    }

    private void initData() {
        //设置wifi按钮的状态
        opened = SettingSharedHelper.getSharePreference(mContext);
        if (opened) {
            switchBtn.setImageResource(R.drawable.netdisc_switch_on);
        } else {
            switchBtn.setImageResource(R.drawable.netdisc_switch_off);
        }
    }

    @Override
    public void onResume() {

    }

    @Override
    public void update(Observable observable, Object data) {

    }

    @Override
    public void onDestory() {

        super.onDestory();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.my_skydrive) {
            Intent intent = new Intent(mContext, MySkydriveActivity.class);
            mContext.startActivity(intent);
        } else if (i == R.id.my_working_group) {//工作组
            Intent intent = new Intent(mContext, WorkGroupActivity.class);
            intent.putExtra(WorkGroupActivity.TYPE, WorkGroupActivity.HOME);
            mContext.startActivity(intent);
        } else if (i == R.id.my_share) {//我的分享
            Intent intent = new Intent(mContext, MyShareActivity.class);
            mContext.startActivity(intent);
        } else if (i == R.id.recycle_bin) {//回收站
            Intent intent = new Intent(mContext, RecycleActivity.class);
            mContext.startActivity(intent);
        } else if (i == R.id.transport_list) {
            Intent intent = new Intent(mContext, TransListActivity.class);
            mContext.startActivity(intent);
        } else if (i == R.id.switch_btn) {
//            boolean opened = SettingSharedHelper.getSharePreference(mContext);
            SettingSharedHelper.setSharedPreference(mContext, !opened);
            if (!opened) {
                opened = !opened;
                switchBtn.setImageResource(R.drawable.netdisc_switch_on);
            } else {
                opened = !opened;
                switchBtn.setImageResource(R.drawable.netdisc_switch_off);
            }

        }
    }
}
