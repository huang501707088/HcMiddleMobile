package com.android.hcframe.netdisc;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.hcframe.netdisc.data.SettingSharedHelper;


/**
 * Created by pc on 2016/6/22.
 */
public class NetdiscFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "NetdiscFragment";
    private boolean isFirst = true;
    private View netdiscView;
    private LinearLayout mySkydriveLinear;
    private LinearLayout myWorkingGroup;
    private LinearLayout myShare;
    private LinearLayout recycleBin;
    private LinearLayout transportList;
    private ImageView switchBtn;
    private boolean switchBtnFlag = false;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        netdiscView = inflater.inflate(R.layout.netdisc_expand_list, container, false);//关联布局文件
        mySkydriveLinear = (LinearLayout) netdiscView.findViewById(R.id.my_skydrive);
        myWorkingGroup = (LinearLayout) netdiscView.findViewById(R.id.my_working_group);
        myShare = (LinearLayout) netdiscView.findViewById(R.id.my_share);
        recycleBin = (LinearLayout) netdiscView.findViewById(R.id.recycle_bin);
        transportList = (LinearLayout) netdiscView.findViewById(R.id.transport_list);
        switchBtn = (ImageView) netdiscView.findViewById(R.id.switch_btn);
        initData();
        mySkydriveLinear.setOnClickListener(this);
        myWorkingGroup.setOnClickListener(this);
        myShare.setOnClickListener(this);
        recycleBin.setOnClickListener(this);
        transportList.setOnClickListener(this);
        switchBtn.setOnClickListener(this);
        return netdiscView;
    }

    private void initData() {
        //设置wifi按钮的状态
        isFirst = SettingSharedHelper.getIsFirst(getActivity());
        if (isFirst) {
            switchBtn.setImageResource(R.drawable.netdisc_switch_on);
            SettingSharedHelper.setSharedPreference(getActivity(), true);
            switchBtnFlag = true;
            isFirst = false;
            SettingSharedHelper.setIsFirst(getActivity(), isFirst);
        } else {
            switchBtnFlag = SettingSharedHelper.getSharePreference(getActivity());
            if (switchBtnFlag == true) {
                switchBtn.setImageResource(R.drawable.netdisc_switch_on);
            } else {
                switchBtn.setImageResource(R.drawable.netdisc_switch_off);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.my_skydrive) {
            Intent intent = new Intent(getActivity(), MySkydriveActivity.class);
            startActivity(intent);
        } else if (i == R.id.my_working_group) {
//            Intent intent = new Intent(getActivity(), MonthCalendarActivity.class);
//            startActivity(intent);
        } else if (i == R.id.my_working_group) {
//            Intent intent = new Intent(getActivity(), MonthCalendarActivity.class);
//            startActivity(intent);
        } else if (i == R.id.my_share) {
//            Intent intent = new Intent(getActivity(), MonthCalendarActivity.class);
//            startActivity(intent);
        } else if (i == R.id.recycle_bin) {
//            Intent intent = new Intent(getActivity(), MonthCalendarActivity.class);
//            startActivity(intent);
        } else if (i == R.id.transport_list) {
            Intent intent = new Intent(getActivity(), TransListActivity.class);
            startActivity(intent);
        } else if (i == R.id.switch_btn) {
            if (switchBtnFlag == false) {
                switchBtn.setImageResource(R.drawable.netdisc_switch_on);
                SettingSharedHelper.setSharedPreference(getActivity(), true);
                switchBtnFlag = true;
            } else if (switchBtnFlag == true) {
                switchBtn.setImageResource(R.drawable.netdisc_switch_off);
                SettingSharedHelper.setSharedPreference(getActivity(), false);
                switchBtnFlag = false;
            }
        }
    }

}


