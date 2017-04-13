package com.android.hcframe.pcenter;

import java.util.Observable;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.container.ContainerCircleImageView;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.modifypwd.ModifyAct;
import com.android.hcframe.pcenter.headportrait.HeadPortraitActivity;
import com.android.hcframe.sql.SettingHelper;
//import com.android.hcframe.zxing.activity.CaptureActivity;
import com.android.hcframe.zxing.activity.CaptureActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.TELEPHONY_SERVICE;

public class PersonCenterPage extends AbstractPage implements IHttpResponse {

    private LinearLayout center_photo_lly;

    private ContainerCircleImageView center_photo_iv;

    private LinearLayout center_nickname_lly;

    private TextView center_nickname_value_tv;

    private LinearLayout center_bindphone_lly;

    private TextView center_bindphone_value_tv;

    private LinearLayout center_modifypwd_lly;

    private LinearLayout center_imei_lly;

    private TextView center_imei_value_tv;
    /**
     * czx
     * 2016.5.6
     * 扫码登录
     */
    private LinearLayout center_scanlogin_lly;

    private boolean isFirst = true;

    private static final int REQ_CODE_NICKNAME = 2;

    private static final int REQ_CODE_BINDPHONE = 3;

    private static final int REQ_CODE_MODIFYPWD = 4;

    private static final int REQ_CODE_SCANLOGIN = 4;

    private String nickname;

    private String mobilephone;

    private String icon;

    protected PersonCenterPage(Activity context, ViewGroup group) {
        super(context, group);
    }

    @Override
    public void update(Observable observable, Object data) {

    }

    private void startHeadPortraitActivity() {
        Intent intent = new Intent(mContext, HeadPortraitActivity.class);
        mContext.startActivityForResult(intent, HcUtil.REQUEST_CODE_HEAD_PORTRAIT);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.center_photo_lly) {
            startHeadPortraitActivity();
        } else if (id == R.id.center_nickname_lly) {
            startActivityForResult(NickNameActivity.class, REQ_CODE_NICKNAME,
                    nickname);
        } else if (id == R.id.center_bindphone_lly) {
            startActivityForResult(ModifyBindOneActivity.class,
                    REQ_CODE_BINDPHONE, mobilephone);
        } else if (id == R.id.center_modifypwd_lly) {
            startActivityForResult(ModifyAct.class, REQ_CODE_MODIFYPWD, null);
        } else if (id == R.id.center_scanlogin_lly) {
            startActivityForResult(CaptureActivity.class,
                    REQ_CODE_SCANLOGIN, "");
        }
    }

    @Override
    public void initialized() {
        if (isFirst) {
            isFirst = !isFirst;
            center_photo_lly.setOnClickListener(this);
            center_nickname_lly.setOnClickListener(this);
            if (HcConfig.getConfig().canBindPhone())
                center_bindphone_lly.setOnClickListener(this);
            center_modifypwd_lly.setOnClickListener(this);
            center_scanlogin_lly.setOnClickListener(this);
            // 初始化信息
            icon = SettingHelper.getIcon(mContext);
            nickname = SettingHelper.getName(mContext);
            mobilephone = SettingHelper.getMobile(mContext);
            center_nickname_value_tv.setText(HcUtil.isEmpty(nickname) ? ""
                    : nickname);
            center_bindphone_value_tv.setText(HcUtil.isEmpty(mobilephone) ? ""
                    : mobilephone);
            ImageLoader.getInstance().displayImage(icon, center_photo_iv,
                    HcUtil.getAccountImageOptions());
        }
    }

    @Override
    public void setContentView() {
        if (mView == null) {
            mView = mInflater.inflate(R.layout.person_center_page, null);
            center_photo_lly = (LinearLayout) mView
                    .findViewById(R.id.center_photo_lly);
            center_photo_iv = (ContainerCircleImageView) mView
                    .findViewById(R.id.center_photo_iv);
            center_nickname_lly = (LinearLayout) mView
                    .findViewById(R.id.center_nickname_lly);
            center_nickname_value_tv = (TextView) mView
                    .findViewById(R.id.center_nickname_value_tv);
            center_bindphone_lly = (LinearLayout) mView
                    .findViewById(R.id.center_bindphone_lly);
            center_bindphone_value_tv = (TextView) mView
                    .findViewById(R.id.center_bindphone_value_tv);
            center_modifypwd_lly = (LinearLayout) mView
                    .findViewById(R.id.center_modifypwd_lly);
            center_imei_lly = (LinearLayout) mView
                    .findViewById(R.id.center_imei_lly);
            center_imei_value_tv = (TextView) mView
                    .findViewById(R.id.center_imei_value_tv);
            center_scanlogin_lly = (LinearLayout) mView.findViewById(R.id.center_scanlogin_lly);
            if (!HcConfig.getConfig().canModifyPw()) {
                center_modifypwd_lly.setVisibility(View.INVISIBLE);
            }
        }
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        center_imei_value_tv.setText(tm.getDeviceId());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        HcLog.D("PersonCenterPage onActivityResult requestCode =" + requestCode + " resultCode = " + resultCode + " data = " + data);
        if (requestCode == REQ_CODE_BINDPHONE) {
            if (resultCode == 1) {
                mobilephone = SettingHelper.getMobile(mContext);
                center_bindphone_value_tv
                        .setText(HcUtil.isEmpty(mobilephone) ? "" : mobilephone);
            }
        } else if (requestCode == REQ_CODE_NICKNAME) {
            if (resultCode == 1) {
                nickname = SettingHelper.getName(mContext);
                center_nickname_value_tv.setText(HcUtil.isEmpty(nickname) ? ""
                        : nickname);
            }
        } else if (requestCode == REQ_CODE_MODIFYPWD) {
            ;//
        } else if (requestCode == HcUtil.REQUEST_CODE_HEAD_PORTRAIT) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
//						for (String key : bundle.keySet()) {
//							HcLog.D("PersonCenterPage  onActivityResult key = "+key);
//						}
                        Bitmap src = bundle.getParcelable("data");
                        if (src != null) {
                            if (!HcUtil.isNetWorkError(mContext)) {
                                center_photo_iv.setImageBitmap(src);
                                HcLog.D("PersonCenterPage  onActivityResult bitmap width = " + src.getWidth() + " height = " + src.getHeight());

                                HcDialog.showProgressDialog(mContext, "上传图片...");
                                HcHttpRequest.getRequest().sendPostImage(src, this);
                            }

                        }


                    }
                }
            }
        }

    }

    public void startActivityForResult(Class<?> clazz, int requestCode,
                                       String data) {
        Intent intent = new Intent(mContext, clazz);
        intent.putExtra("initdata", data);
        mContext.startActivityForResult(intent, requestCode);
    }

    @Override
    public void notify(Object data, RequestCategory request,
                       ResponseCategory category) {
        // TODO Auto-generated method stub
        if (request == RequestCategory.POST_IMAGE) {
            HcDialog.deleteProgressDialog();
            switch (category) {
                case SUCCESS:
                    if (data instanceof String) {
                        String icon = (String) data;
                        try {
                            JSONObject object = new JSONObject(icon);
                            int code = object.getInt("code");
                            String msg = object.getString("msg");
                            if (code == 0) {
                                object = object.getJSONObject("body");
                                HcLog.D("PersonCenterPage #notify filePath = "+object.getString("filePath"));
                                SettingHelper.setIcon(mContext, object.getString("filePath"));
                                HcUtil.showToast(mContext, R.string.upload_success);
                            } else if (code == HcHttpRequest.REQUEST_ACCOUT_EXCLUDED ||
                                    code == HcHttpRequest.REQUEST_TOKEN_FAILED) {
                                ImageLoader.getInstance().displayImage(SettingHelper.getIcon(mContext), center_photo_iv, HcUtil.getAccountImageOptions());
                                HcUtil.reLogining(object.getJSONObject("body").toString(), mContext, msg);
                            } else {
                                ImageLoader.getInstance().displayImage(SettingHelper.getIcon(mContext), center_photo_iv, HcUtil.getAccountImageOptions());
                                HcUtil.showToast(mContext, msg);
                            }
                        } catch (JSONException e) {
                            HcLog.D("PersonCenterPage #notify JSONException e = "+e);
                            ImageLoader.getInstance().displayImage(SettingHelper.getIcon(mContext), center_photo_iv, HcUtil.getAccountImageOptions());
                            HcUtil.showToast(mContext, R.string.update_fail);
                        }

                    } else {
                        ImageLoader.getInstance().displayImage(SettingHelper.getIcon(mContext), center_photo_iv, HcUtil.getAccountImageOptions());
                        HcUtil.showToast(mContext, R.string.update_fail);
                    }
                    break;

                default:
                    ImageLoader.getInstance().displayImage(SettingHelper.getIcon(mContext), center_photo_iv, HcUtil.getAccountImageOptions());
                    HcUtil.showToast(mContext, R.string.update_fail);
                    break;
            }
        }
    }

    @Override
    public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub

    }

}
