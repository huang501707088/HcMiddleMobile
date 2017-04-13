/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com
* @author jinjr
* @data 2016-1-4 下午2:21:39
*/
package com.android.hcframe.internalservice.sign;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcApplication;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.container.ContainerActivity;
import com.android.hcframe.internalservice.fragment.LocalSignFragment;
import com.android.hcframe.internalservice.fragment.RemoteSignFragment;
import com.android.hcframe.internalservice.signin.R;
import com.android.hcframe.push.HcAppState;
import com.android.hcframe.push.HcPushManager;
import com.android.hcframe.sql.SettingHelper;


import java.util.Observable;

public class SignMenuHomePage extends AbstractPage implements SignLoctionUtils.DistanceCallback {

    private static final String TAG = "SignMenuHomePage";
    /**
     * 进入打卡页面
     */
    private Fragment mRemoteSignFragment;
    private Fragment mLocalSignFragment;
    private FragmentManager fm;
    private FragmentTransaction transaction;
    private FrameLayout mParent;
    private final String mAppId;

    protected SignMenuHomePage(Activity context, ViewGroup group, String appId) {
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
            mView = mInflater.inflate(R.layout.sign_home_layout2, null);
            mParent = (FrameLayout) mView.findViewById(R.id.sign_home_layout_parent);
            fm = mContext.getFragmentManager();
        }
    }


    @Override
    public void update(Observable observable, Object data) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onResume() {
        HcPushManager.getInstance().setPushInfo(null);
        SignCache.getInstance().repeatConfigExist(mContext);
        if (!HcUtil.isGPS(mContext)) {
            final AlertDialog dialog = new AlertDialog.Builder(mContext)
                    .create();
            dialog.setCancelable(false);
            dialog.show();
            dialog.getWindow().setContentView(R.layout.enable_gps_dialog);
            TextView netGpsTv = (TextView) dialog.getWindow().findViewById(R.id.net_gps_tv);
            Button unagree_dialog = (Button) dialog.getWindow()
                    .findViewById(R.id.unagree_dialog);
            Button agree_dialog = (Button) dialog.getWindow().findViewById(
                    R.id.agree_dialog);
            netGpsTv.setText("签到考勤需要开启Gps，你同意吗？");
            unagree_dialog.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    // 这里要增加一个判断,要是在一级菜单里就不能退出,因为退出会把应用关闭.
                    if (mContext instanceof ContainerActivity) {
                        HcAppState.getInstance().removeActivity(mContext);
                        mContext.finish();
                    }
                }
            });
            agree_dialog.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    HcUtil.openGPS(mContext);
                }
            });
        } else {
            /**
             * @jrjin
             * @2016-06-20
             */
            SignLoctionUtils.setDistanceCallback(this);
            SignLoctionUtils.startLocation();
        }
        selectFragment();
    }

    @Override
    public void onDestory() {
        SignLoctionUtils.stopLocation();
        if (fm == null) return;
        transaction = fm.beginTransaction();
        if (mLocalSignFragment != null) {
            transaction.remove(mLocalSignFragment);
        }
        if (mRemoteSignFragment != null) {
            transaction.remove(mRemoteSignFragment);
        }
    }

    private void selectFragment() {
        transaction = fm.beginTransaction();
        if (HcUtil.isNetWorkAvailable(mContext)) {
            // 判断距离
            String lng = SignCache.getInstance().getLongitude();
            String lat = SignCache.getInstance().getLatitude();
            int distance = SignCache.getInstance().getMaxDistance();
            if (TextUtils.isEmpty(lng) || TextUtils.isEmpty(lat)) {
                // 先显示远距离
                replaceFragment(Location.REMOTE);
            } else {
                // 判断距离
                String dis = SignLoctionUtils.getDistance();
                if (TextUtils.isEmpty(dis)) {
                    // 显示远距离
                    replaceFragment(Location.REMOTE);
                } else {
                    double d = Double.parseDouble(dis);
                    if (d >= distance) {
                        //  本地打卡
                        replaceFragment(Location.LOCAL);
                    } else {
                        // 远距离打卡
                        replaceFragment(Location.REMOTE);
                    }
                }

            }
        } else { // 网络不可以用
            replaceFragment(Location.REMOTE);
        }
        transaction.commit();//提交事务
    }

    @Override
    public void notifyDistance(String distance) {
        // 判断距离
        transaction = fm.beginTransaction();
        if (TextUtils.isEmpty(distance)) {
            // 显示远距离
            replaceFragment(Location.REMOTE);
        } else {
            double d = Double.parseDouble(distance);
            if (d > SignCache.getInstance().getMaxDistance()) {
                //  本地打卡(用replace,每一次都要去new,这是没办法避免的事，除非用add,hide,show)
                replaceFragment(Location.LOCAL);
            } else {
                // 远距离打卡
                replaceFragment(Location.REMOTE);
            }
        }
        transaction.commit();
    }

    private Location mLocation = Location.NONE;

    private enum Location {
        NONE,
        LOCAL,
        REMOTE
    }

    private void replaceFragment(Location location) {
        if (mLocation == location) return;
        mLocation = location;
        switch (location) {
            case LOCAL:
                if (mLocalSignFragment == null)
                    mLocalSignFragment = new LocalSignFragment();
                transaction.replace(R.id.sign_home_layout_parent, mLocalSignFragment);
                break;
            case REMOTE:
                if (mRemoteSignFragment == null)
                    mRemoteSignFragment = new RemoteSignFragment();
                transaction.replace(R.id.sign_home_layout_parent, mRemoteSignFragment);
                break;
            default:
                break;
        }
    }
}
