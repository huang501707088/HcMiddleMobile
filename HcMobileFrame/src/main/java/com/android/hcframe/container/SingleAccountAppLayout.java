/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-24 下午8:42:55
*/
package com.android.hcframe.container;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.hcframe.CacheManager;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.badge.BadgeCache;
import com.android.hcframe.container.data.ViewInfo;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.login.LoginActivity;
import com.android.hcframe.pcenter.PersonCenterActivity;
import com.android.hcframe.sql.SettingHelper;
import com.android.hcframe.sys.SysMassageActivity;
import com.android.hcframe.zxing.activity.CaptureActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 用户个人信息模版
 */
public class SingleAccountAppLayout extends AppViewLayout {

    private static final String TAG = "SingleAccountAppLayout";

    private LinearLayout mUnLoginParent;
    private TextView mLoginBtn;
    private LinearLayout mLoginBtnParent;

    private RelativeLayout mLoginParent;
    private ImageView mAccountIcon;
    private TextView mAccountName;
    private TextView mLogout;
    private TextView mMessage;
    private TextView mAccountInfo;

    /**
     * 判断用户是否主动退出过，要是主动退出，再登录的时候就不需要去保存数据
     */
    private boolean mLoginOut = false;

    public SingleAccountAppLayout() {
        super();
        mLayoutId = R.layout.container_singleapp_account_layout;
    }

    @Override
    public View createAppView(Context context, ViewGroup parent, ViewInfo info) {
        // TODO Auto-generated method stub
        if (mLayoutId != 0) {
            mContext = context;
            View layout = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
                    inflate(mLayoutId, parent, false);
            mUnLoginParent = (LinearLayout) layout.findViewById(R.id.container_account_unlogin_parent);
            mLoginBtn = (TextView) layout.findViewById(R.id.container_account_login_btn);
            mLoginBtnParent = (LinearLayout) layout.findViewById(R.id.container_account_login_btn_parent);

            mLoginParent = (RelativeLayout) layout.findViewById(R.id.container_account_login_parent);
            mLogout = (TextView) layout.findViewById(R.id.container_account_logout_btn);
            mAccountIcon = (ImageView) layout.findViewById(R.id.container_account_icon);
            mAccountName = (TextView) layout.findViewById(R.id.container_account_name);
            mAccountInfo = (TextView) layout.findViewById(R.id.container_account_account_btn);
            mMessage = (TextView) layout.findViewById(R.id.container_account_message_btn);

            mLogout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    logout();
                }
            });

            mMessage.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    /**
                     * czx
                     * 扫码登录
                     * 2016-5-12
                     */
                    startScanActivity();
//                    startSysMessageActivity();
                }
            });

            mAccountInfo.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    startUserActivity();
                }
            });

            mLoginBtnParent.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    startLoginActivity();
                }
            });

            return layout;
        }
        return null;
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        if (mLayoutId != 0) {
            if (TextUtils.isEmpty(SettingHelper.getAccount(mContext))) {
                mUnLoginParent.setVisibility(View.VISIBLE);
                mLoginParent.setVisibility(View.GONE);
            } else {
                mUnLoginParent.setVisibility(View.GONE);
                mLoginParent.setVisibility(View.VISIBLE);
                mAccountName.setText(SettingHelper.getName(mContext));
            }
            String accountUrl = SettingHelper.getIcon(mContext);
            HcLog.D(TAG + " onResume account url = " + accountUrl);
            ImageLoader.getInstance().displayImage(accountUrl, mAccountIcon,
                    HcUtil.getAccountImageOptions());

        }
    }

    @Override
    public void onRelease() {
        // TODO Auto-generated method stub
        mAccountIcon = null;
        mAccountInfo = null;
        mAccountName = null;
        mLoginBtn = null;
        mLoginParent = null;
        mLogout = null;
        mMessage = null;
        mUnLoginParent = null;
        super.onRelease();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra("loginout", mLoginOut);
        mContext.startActivity(intent);
        if (mContext instanceof Activity) {
            ((Activity) mContext).overridePendingTransition(0, 0);
        }
    }

    @Override
    public void notify(Object data, RequestCategory request,
                       ResponseCategory category) {
        // TODO Auto-generated method stub
        HcDialog.deleteProgressDialog();
        switch (request) {
            case LOGOUT:
                switch (category) {
                    case SUCCESS:
                        mLoginOut = true;
                        /**
                         * @date 2016-1-28 下午12:33:43
                         * 放到DocCacheData.getInstance().clearDocCache()处理
                        OperateDatabase.insertDataRecords(this, DocCacheData
                        .getInstance().getHistoricalRecords());
                         */

                        /**
                         *@author jinjr
                         *@date 16-3-11 下午4:30
                         */
                        CacheManager.getInstance().clearCaches(false);

				        SettingHelper.setUserId(mContext, "");
                        SettingHelper.setToken(mContext, "");
                        SettingHelper.setAccount(mContext, "");
                        SettingHelper.setIcon(mContext, "");
                        SettingHelper.setMobile(mContext, "");
                        SettingHelper.setName(mContext, "");

                        /**
                         *@author jinjr
                         *@date 17-3-16 上午9:16
                         */
                        SettingHelper.setEmail(mContext, "");

                        String appIds = "";
                        if (data != null && data instanceof String) {
                            try {
                                JSONObject object = new JSONObject((String) data).getJSONObject("body");
                                if (HcUtil.hasValue(object, "appIds")) {
                                    appIds = object.getString("appIds");
                                }
                            } catch (JSONException e) {
                                appIds = "";
                            }

                        }
                        SettingHelper.setOperatePermisstion(mContext, appIds);
                        HcConfig.getConfig().updatePermisstion(mContext, false);
                        BadgeCache.getInstance().createBadge(mContext);
                        onResume();
                        // 连接IM,这里需要判断,通过广播告知IM模块
                        Intent intent = new Intent("com.android.hcframe.logout");
                        intent.setPackage(mContext.getPackageName());
                        mContext.sendBroadcast(intent);
                        break;
                    case SESSION_TIMEOUT:
                    case NETWORK_ERROR:
                        HcUtil.toastTimeOut(mContext);
                        break;

                    default:
                        break;
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
        // TODO Auto-generated method stub

    }

    private void logout() {
        if (!HcUtil.isNetWorkError(mContext)) {
            HcDialog.showProgressDialog(mContext, R.string.logout_toast);
            HcHttpRequest.getRequest().sendLogoutCommand(this,
                    HcUtil.getIMEI(mContext));
        }
    }

    private void startUserActivity() {
//		Intent intent = new Intent(mContext, UserInfoAct.class);
        Intent intent = new Intent(mContext, PersonCenterActivity.class);
        mContext.startActivity(intent);
        // mContext.overridePendingTransition(R.anim.wallpaper_intra_open_enter,
        // R.anim.wallpaper_intra_open_exit);
        if (mContext instanceof Activity) {
            ((Activity) mContext).overridePendingTransition(0, 0);
        }
    }

    private void startSysMessageActivity() {
        Intent intent = new Intent(mContext, SysMassageActivity.class);
        mContext.startActivity(intent);
        // mContext.overridePendingTransition(R.anim.wallpaper_intra_open_enter,
        // R.anim.wallpaper_intra_open_exit);
        if (mContext instanceof Activity) {
            ((Activity) mContext).overridePendingTransition(0, 0);
        }
    }
    private void startScanActivity() {
        Intent intent = new Intent(mContext, CaptureActivity.class);
        mContext.startActivity(intent);
        // mContext.overridePendingTransition(R.anim.wallpaper_intra_open_enter,
        // R.anim.wallpaper_intra_open_exit);
        if (mContext instanceof Activity) {
            ((Activity) mContext).overridePendingTransition(0, 0);
        }
    }
}
