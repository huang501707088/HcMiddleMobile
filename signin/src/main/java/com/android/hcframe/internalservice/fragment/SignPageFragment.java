package com.android.hcframe.internalservice.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcUtil;
import com.android.hcframe.internalservice.signin.R;
import com.android.hcframe.sql.SettingHelper;

/**
 * Created by Administrator on 2016/4/29 0029.
 */
public class SignPageFragment extends Fragment implements View.OnClickListener{

    private View signPageView;
    private Button refreshBtn;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       //关联布局文件
        signPageView = inflater.inflate(R.layout.sign_no_network_layout, container, false);
        refreshBtn  = (Button) signPageView.findViewById(R.id.refresh_btn);
        refreshBtn.setOnClickListener(this);
        return signPageView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i==R.id.refresh_btn){
            if(!HcUtil.isGPS(getActivity())){
                final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .create();
                dialog.setCancelable(false);
                dialog.show();
                dialog.getWindow().setContentView(R.layout.enable_gps_dialog);
                Button unagree_dialog = (Button) dialog.getWindow()
                        .findViewById(R.id.unagree_dialog);
                Button agree_dialog = (Button) dialog.getWindow().findViewById(
                        R.id.agree_dialog);
                unagree_dialog.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                agree_dialog.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        SettingHelper.setAutoSignin(getActivity(), true);
                        // 打开 gps
                        HcUtil.openGPS(getActivity());
                        // 网络判断
                        if (!HcUtil.isNetworkConnected(getActivity())) {
                            HcUtil.showToast(HcApplication.getContext(),
                                    R.string.open_netdata);
                        }else{
                            //跳到首页
                        }

                    }
                });
            }

        }
    }

}
