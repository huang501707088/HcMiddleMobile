/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2016-1-11 下午5:36:29
*/
package com.android.hcframe.internalservice.annual;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.http.ResponseNewsInfo;
import com.android.hcframe.sql.SettingHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONObject;

public class AnnualIntroActivity extends HcBaseActivity implements IHttpResponse {

    private static final String TAG = "AnnualIntroActivity";
    /**
     * 员工姓名
     */
    private TextView mName;
    /**
     * 年会简介
     */
    private TextView mContent;

    private TextView mDate;
    private TextView mAddress;

    private TextView mArea;

    private ImageView mAreaIcon;

    private LinearLayout mParent;

    private String mIconUri;

    private String mMd5Url;

    private DisplayImageOptions mOptions;

    private int mWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annual_intro);
        mOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
        /*.showImageOnLoading(R.drawable.annual_home_logo)
        .showImageForEmptyUri(R.drawable.annual_home_logo)
		.showImageOnFail(R.drawable.annual_home_logo)*/
                .cacheInMemory(true).cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888).build();
        mWidth = (int) (340 * HcUtil.getScreenDensity());
        initViews();
        initdata();
    }

    @Override
    public void notify(Object data, RequestCategory request,
                       ResponseCategory category) {
        // TODO Auto-generated method stub
        mMd5Url = null;
        HcDialog.deleteProgressDialog();
        switch (category) {
            case NETWORK_ERROR:
            case SESSION_TIMEOUT:
                HcUtil.toastNetworkError(this);
                break;
            case SYSTEM_ERROR:
                HcUtil.toastSystemError(this, data);
                break;
            case SUCCESS:
                if (data != null && data instanceof String) {
                    HcLog.D(TAG + " #notify parse SUCCESS data = " + data);
                    try {
                        JSONObject object = new JSONObject((String) data);
                        int code = object.getInt("code");
                        if (code == 0) {
                            // 设置更新时间、图片、信息
                            object = object.getJSONObject("body");
                            if (HcUtil.hasValue(object, "updateTime")) {
                                SettingHelper.setModuleTime(this, AnnualHomeView.MODULE_ID, object.getString("updateTime"), false);
                            }
                            if (HcUtil.hasValue(object, "area_pic")) {
                                ImageLoader.getInstance().displayImage(object.getString("area_pic"), mAreaIcon, mOptions);
                            }
                            SettingHelper.setAnnualInfo(this, object.toString());
                            parseJson(object.toString());
                        } else {
                            String msg = object.getString("msg");
                            /**
                             * czx
                             * 2016.4.13
                             */
                            if (HcHttpRequest.REQUEST_ACCOUT_EXCLUDED == code
                                    || HcHttpRequest.REQUEST_TOKEN_FAILED == code
                                    ) {
                                if (HcUtil.hasValue(object, "body")) {
                                    HcUtil.reLogining(object.getJSONObject("body").toString(), this, msg);
                                }
                            } else {
                                HcUtil.showToast(this, msg);
                            }
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                        HcLog.D(TAG + " #notify parse Error e = " + e);
                        HcUtil.toastDataError(this);
                    }

                }
                // error
                break;

            default:
                break;
        }
    }

    @Override
    public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
        // TODO Auto-generated method stub
        mMd5Url = md5Url;

    }

    private void initViews() {
        mParent = (LinearLayout) findViewById(R.id.annual_intro_parent);

        mName = (TextView) findViewById(R.id.annual_intro_account_value);
        mContent = (TextView) findViewById(R.id.annual_intro_content);

        mDate = (TextView) findViewById(R.id.annual_date);
        mAddress = (TextView) findViewById(R.id.annual_address);

        mArea = (TextView) findViewById(R.id.annual_seat);
        mAreaIcon = (ImageView) findViewById(R.id.annual_seat_icon);

        mAreaIcon.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (!TextUtils.isEmpty(mIconUri)) {
                    Intent intent = new Intent(AnnualIntroActivity.this, ImageActivity.class);
                    intent.putExtra("uri", mIconUri);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            }
        });
    }

    private void initdata() {
        mName.setText(SettingHelper.getName(this));

        String info = SettingHelper.getAnnualInfo(this);
        if (TextUtils.isEmpty(info)) {
            HcDialog.showProgressDialog(this, R.string.dialog_title_get_data);
            HcHttpRequest.getRequest().sendAnnualInfoCommand(this);
        } else {
            parseJson(info);
        }
    }

    private void parseJson(String data) {
        try {
            JSONObject object = new JSONObject(data);
            if (HcUtil.hasValue(object, "area")) {
                mArea.setText(object.getString("area"));
            }
            if (HcUtil.hasValue(object, "welcome_memo")) {
                mContent.setText(object.getString("welcome_memo"));
            }
            if (HcUtil.hasValue(object, "show_time")) {
                mDate.setText(object.getString("show_time"));
            }
            if (HcUtil.hasValue(object, "show_address")) {
                mAddress.setText(object.getString("show_address"));
            }
            if (HcUtil.hasValue(object, "area_pic")) {
                mIconUri = object.getString("area_pic");
            }

            /** 年会流程 */
            if (HcUtil.hasValue(object, "flowList")) {
                JSONArray array = object.getJSONArray("flowList");
                int size = array.length();
                HcLog.D(TAG + " #parseJson size = " + size);
                for (int i = 0; i < size; i++) {
                    object = array.getJSONObject(i);
                    String date = object.getString("flow_time");
                    String title = object.getString("flow_memo");
                    addView(title, date);
                }
            }

        } catch (Exception e) {
            // TODO: handle exception
            HcLog.D(TAG + " #parseJson data = " + data + " ||||||||error = " + e);

        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        if (!TextUtils.isEmpty(mMd5Url)) {
            HcHttpRequest.getRequest().cancelRequest(mMd5Url);
        }
        super.onDestroy();
    }

    private void addView(String title, String date) {
        View view = getLayoutInflater().inflate(R.layout.annual_intro_list_item, null);
        TextView dateView = (TextView) view.findViewById(R.id.annual_intro_list_item_date);
        TextView titleView = (TextView) view.findViewById(R.id.annual_intro_list_item_title);
        dateView.setText(date);
        titleView.setText(title);
        mParent.addView(view);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (mAreaIcon.getBackground() == null && !TextUtils.isEmpty(mIconUri)) {
//			ImageLoader.getInstance().displayImage(mIconUri, mAreaIcon, mOptions);
            ImageLoader.getInstance().loadImage(mIconUri, new ImageSize(mWidth, mWidth), mOptions, new ImageLoadingListener() {

                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    // TODO Auto-generated method stub
                    mAreaIcon.setVisibility(View.GONE);
                    HcLog.D(TAG + "#onLoadingStarted imageUri = " + imageUri);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view,
                                            FailReason failReason) {
                    // TODO Auto-generated method stub
                    mAreaIcon.setVisibility(View.GONE);
                    HcLog.D(TAG + "#onLoadingFailed imageUri = " + imageUri + " failReason = " + failReason);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    // TODO Auto-generated method stub
                    HcLog.D(TAG + "#onLoadingComplete imageUri = " + imageUri + " loadedImage = " + loadedImage + " width = " + loadedImage.getWidth() + " height = " + loadedImage.getHeight());
                    mAreaIcon.setVisibility(View.VISIBLE);
                    mAreaIcon.setBackgroundDrawable(new BitmapDrawable(getResources(), loadedImage));
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    // TODO Auto-generated method stub
                    mAreaIcon.setVisibility(View.GONE);
                    HcLog.D(TAG + "#onLoadingCancelled imageUri = " + imageUri);
                }
            });
        }
    }


}
