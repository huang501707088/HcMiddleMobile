package com.android.hcframe.internalservice.sign;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.android.hcframe.BigImageActivity;
import com.android.hcframe.HcApplication;
import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.http.ResponseCodeInfo;
import com.android.hcframe.internalservice.signin.R;
import com.android.hcframe.sql.SettingHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-5-5 16:04.
 */
public class SignSubmitActivity extends HcBaseActivity implements View.OnClickListener,
        View.OnLongClickListener, IHttpResponse {

    private static final String TAG = "SignSubmitActivity";

    private TextView mTime;

    private TextView mAddr;

    private TextView mSwitch;

    private EditText mDescription;

    private LinearLayout mParent;

    private FrameLayout mImageParent1;
    private ImageView mImage1;
    private ImageView mDelete1;

    private FrameLayout mImageParent2;
    private ImageView mImage2;
    private ImageView mDelete2;

    private FrameLayout mImageParent3;
    private ImageView mImage3;
    private ImageView mDelete3;

    private FrameLayout mImageParent4;
    private ImageView mImage4;
    private ImageView mDelete4;

    private ImageView mAddBtn;

    private TextView mSubmit;

    private DisplayImageOptions mOptions;

    private int mWidth;

    /**
     * 未被添加的图片的layout
     */
    private List<FrameLayout> mLayouts = new ArrayList<>();
    /**
     * 图片uri列表
     */
    private Map<FrameLayout, String> mUris = new HashMap<FrameLayout, String>();

    private TopBarView mTopBarView;
    /**
     * mAddr中的值
     */
    private String result;
    /**
     * 选定位置的经纬坐标
     */
    private String mLatitude;
    private String mLongitude;
    /**
     * 根据选定位置的经纬坐标定位
     */
    private MapView mapView;
    private AMap aMap;
    /**
     * Mark标记
     */
    private Marker marker;
    private LatLng markPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_field_sign_layout);
        mOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheInMemory(false).cacheOnDisk(false)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888).build();
        mWidth = (int) (55 * HcUtil.getScreenDensity());
        initViews(savedInstanceState);
        initData();
    }

    private void initViews(Bundle savedInstanceState) {
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        aMap = mapView.getMap();
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 普通地图模式
        mTime = (TextView) findViewById(R.id.field_sign_time);
        mAddr = (TextView) findViewById(R.id.field_sign_addr);
        mSwitch = (TextView) findViewById(R.id.field_sign_switch_addr);

        mDescription = (EditText) findViewById(R.id.field_sign_description);

        mParent = (LinearLayout) findViewById(R.id.field_sign_images_parent);

        mImageParent1 = (FrameLayout) findViewById(R.id.field_sign_image_parent1);
        mImage1 = (ImageView) findViewById(R.id.field_sign_image1);
        mDelete1 = (ImageView) findViewById(R.id.field_sign_delete_image1);

        mImageParent2 = (FrameLayout) findViewById(R.id.field_sign_image_parent2);
        mImage2 = (ImageView) findViewById(R.id.field_sign_image2);
        mDelete2 = (ImageView) findViewById(R.id.field_sign_delete_image2);

        mImageParent3 = (FrameLayout) findViewById(R.id.field_sign_image_parent3);
        mImage3 = (ImageView) findViewById(R.id.field_sign_image3);
        mDelete3 = (ImageView) findViewById(R.id.field_sign_delete_image3);

        mImageParent4 = (FrameLayout) findViewById(R.id.field_sign_image_parent4);
        mImage4 = (ImageView) findViewById(R.id.field_sign_image4);
        mDelete4 = (ImageView) findViewById(R.id.field_sign_delete_image4);

        mAddBtn = (ImageView) findViewById(R.id.field_sign_add_btn);

        mSubmit = (TextView) findViewById(R.id.field_sign_submit_btn);

        mTopBarView = (TopBarView) findViewById(R.id.field_sign_top_bar);
        mSwitch.setOnClickListener(this);
        mAddBtn.setOnClickListener(this);
        mSubmit.setOnClickListener(this);

        mImage1.setOnClickListener(this);
        mImage2.setOnClickListener(this);
        mImage3.setOnClickListener(this);
        mImage4.setOnClickListener(this);

        mDelete1.setOnClickListener(this);
        mDelete2.setOnClickListener(this);
        mDelete3.setOnClickListener(this);
        mDelete4.setOnClickListener(this);

        mImage1.setOnLongClickListener(this);
        mImage2.setOnLongClickListener(this);
        mImage3.setOnLongClickListener(this);
        mImage4.setOnLongClickListener(this);
    }

    private void initData() {
        mHandler = new DateHandler();
        mTime.setText(HcUtil.getDate(HcUtil.FORMAT_CITY_HOUR, System.currentTimeMillis()));
        mLayouts.add(mImageParent1);
        mLayouts.add(mImageParent2);
        mLayouts.add(mImageParent3);
        mLayouts.add(mImageParent4);
        mParent.removeViews(0, 4);
//        mTopBarView.setTitle(SettingHelper.getAccount(this));
        mTopBarView.setTitle("考勤签到");
        mLongitude = String.valueOf(SignLoctionUtils.getLng());
        mLatitude = String.valueOf(SignLoctionUtils.getLat());
        markPlace = new LatLng(Double.valueOf(mLatitude), Double.valueOf(mLongitude));
        changeCamera(
                CameraUpdateFactory.newCameraPosition(new CameraPosition(
                        markPlace, 18, 0, 30)), null);
        //以下这个是标记上面这个经纬度在地图的位置是
        marker = aMap.addMarker(new MarkerOptions()
                .position(new LatLng(Double.valueOf(mLatitude), Double.valueOf(mLongitude)))
                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.poi_marker_1)))
                .draggable(false));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.field_sign_add_btn) {
            startCamera();
        } else if (id == R.id.field_sign_switch_addr) {
            startMapActivity();
        } else if (id == R.id.field_sign_submit_btn) {
            if (HcUtil.isNetWorkAvailable(this)) {
                //button点击频率控制,避免多次提交
//                if(ButtonUtil.isFastDoubleClick(v.getId())){
//                    return;
//                }
                sign();
            } else {
                Toast.makeText(getApplicationContext(), "网络不给力！",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.field_sign_delete_image1) {
            HcLog.D(TAG + " #onClick child size = " + mParent.getChildCount());
            mUris.remove(mImageParent1);
            mLayouts.add(mImageParent1);
            mDelete1.setVisibility(View.GONE);
            mParent.removeView(mImageParent1);
            Bitmap src = ((BitmapDrawable) mImage1.getDrawable()).getBitmap();
            mImage1.setImageBitmap(null);
            if (src != null) {
                src.recycle();
                src = null;
            }

            mAddBtn.setVisibility(View.VISIBLE);
        } else if (id == R.id.field_sign_delete_image2) {
            mUris.remove(mImageParent2);
            HcLog.D(TAG + " #onClick child size = " + mParent.getChildCount());
            mLayouts.add(mImageParent2);
            mDelete2.setVisibility(View.GONE);
            mParent.removeView(mImageParent2);
            Bitmap src = ((BitmapDrawable) mImage2.getDrawable()).getBitmap();
            mImage2.setImageBitmap(null);
            if (src != null) {
                src.recycle();
                src = null;
            }
            mAddBtn.setVisibility(View.VISIBLE);
        } else if (id == R.id.field_sign_delete_image3) {
            mUris.remove(mImageParent3);
            HcLog.D(TAG + " #onClick child size = " + mParent.getChildCount());
            mLayouts.add(mImageParent3);
            mDelete3.setVisibility(View.GONE);
            mParent.removeView(mImageParent3);
            Bitmap src = ((BitmapDrawable) mImage3.getDrawable()).getBitmap();
            mImage3.setImageBitmap(null);
            if (src != null) {
                src.recycle();
                src = null;
            }
            mAddBtn.setVisibility(View.VISIBLE);
        } else if (id == R.id.field_sign_delete_image4) {
            mUris.remove(mImageParent4);
            mLayouts.add(mImageParent4);
            mParent.removeView(mImageParent4);
            mDelete4.setVisibility(View.GONE);
            Bitmap src = ((BitmapDrawable) mImage4.getDrawable()).getBitmap();
            mImage4.setImageBitmap(null);
            if (src != null) {
                src.recycle();
                src = null;
            }
            mAddBtn.setVisibility(View.VISIBLE);
        } else if (id == R.id.field_sign_image1) {
            if (mDelete1.getVisibility() == View.VISIBLE) {
                mDelete1.setVisibility(View.GONE);
            } else {
                startBigImageActivity(mUris.get(mImageParent1));
            }
        } else if (id == R.id.field_sign_image2) {
            if (mDelete2.getVisibility() == View.VISIBLE) {
                mDelete2.setVisibility(View.GONE);
            } else {
                startBigImageActivity(mUris.get(mImageParent2));
            }
        } else if (id == R.id.field_sign_image3) {
            if (mDelete3.getVisibility() == View.VISIBLE) {
                mDelete3.setVisibility(View.GONE);
            } else {
                startBigImageActivity(mUris.get(mImageParent3));
            }
        } else if (id == R.id.field_sign_image4) {
            if (mDelete4.getVisibility() == View.VISIBLE) {
                mDelete4.setVisibility(View.GONE);
            } else {
                startBigImageActivity(mUris.get(mImageParent4));
            }
        }
    }

    private void startBigImageActivity(String uri) {
        Intent intent = new Intent(this, BigImageActivity.class);
        intent.putExtra("uri", uri);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onLongClick(View v) {
        int id = v.getId();
        if (id == R.id.field_sign_image1) {
            if (mDelete1.getVisibility() != View.VISIBLE) {
                mDelete1.setVisibility(View.VISIBLE);
            }
        } else if (id == R.id.field_sign_image2) {
            if (mDelete2.getVisibility() != View.VISIBLE) {
                mDelete2.setVisibility(View.VISIBLE);
            }
        } else if (id == R.id.field_sign_image3) {
            if (mDelete3.getVisibility() != View.VISIBLE) {
                mDelete3.setVisibility(View.VISIBLE);
            }
        } else if (id == R.id.field_sign_image4) {
            if (mDelete4.getVisibility() != View.VISIBLE) {
                mDelete4.setVisibility(View.VISIBLE);
            }
        }
        return true;
    }

    private String mImagePath = null;

    private void startCamera() {

        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        String directory = HcApplication.getImagePhotoPath();
        String filename = System.currentTimeMillis() + ".jpg";
        File dir = new File(directory);
        if (!dir.exists()) dir.mkdirs();
        File file = new File(directory, filename);
        mImagePath = file.getAbsolutePath();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        startActivityForResult(intent, HcUtil.REQUEST_CODE_FROM_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        HcLog.D(TAG + " onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode + " data = " + data);
//		if (data == null) return;
        switch (resultCode) {
            case RESULT_OK:
                switch (requestCode) {
                    case HcUtil.REQUEST_CODE_FROM_CAMERA:

                        if (data == null) {
                            final Uri uri = Uri.fromFile(new File(mImagePath));
                            HcLog.D(TAG + " #onActivityResult uri = " + uri);
                            ImageLoader.getInstance().loadImage(uri.toString(), new ImageSize(mWidth, mWidth), mOptions, new ImageLoadingListener() {

                                @Override
                                public void onLoadingStarted(String imageUri, View view) {
                                    // TODO Auto-generated method stub
                                    HcLog.D(TAG + "#onLoadingStarted imageUri = " + imageUri);
                                }

                                @Override
                                public void onLoadingFailed(String imageUri, View view,
                                                            FailReason failReason) {
                                    // TODO Auto-generated method stub
                                    HcLog.D(TAG + "#onLoadingFailed imageUri = " + imageUri + " failReason = " + failReason);
                                }

                                @Override
                                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                    // TODO Auto-generated method stub
                                    HcLog.D(TAG + "#onLoadingComplete imageUri = " + imageUri + " loadedImage = " + loadedImage + " width = " + loadedImage.getWidth() + " height = " + loadedImage.getHeight());
                                    FrameLayout layout = mLayouts.remove(0);
                                    mParent.addView(layout, mParent.getChildCount() - 1);
                                    layout.setVisibility(View.VISIBLE);
                                    if (layout == mImageParent1) {
                                        mImage1.setImageBitmap(loadedImage);
                                    } else if (layout == mImageParent2) {
                                        mImage2.setImageBitmap(loadedImage);
                                    } else if (layout == mImageParent3) {
                                        mImage3.setImageBitmap(loadedImage);
                                    } else if (layout == mImageParent4) {
                                        mImage4.setImageBitmap(loadedImage);
                                    }
                                    mUris.put(layout, uri.toString());
                                    if (mUris.size() == 4) {
                                        mAddBtn.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onLoadingCancelled(String imageUri, View view) {
                                    // TODO Auto-generated method stub
                                    HcLog.D(TAG + "#onLoadingCancelled imageUri = " + imageUri);
                                }
                            });
//                            startCrop(Uri.fromFile(new File(mImagePath)), null);
                        } else if (data.getData() != null) {
//                            startCrop(data.getData(), null);
                        } else if (data.getExtras() != null) {
//                            startCrop(null, (Bitmap) data.getExtras().get("data"));
                        }
                        if (data != null) {
                            HcLog.D(TAG + " onActivityResult uri = " + data.getData() + " extras = " + data.getExtras());
                        }

                        break;
                    case HcUtil.REQUEST_CODE_FOR_TEXT:
                        if (data != null) {
                            aMap.clear();
                            result = data.getExtras().getString("mLocationErrText");//得到新Activity关闭后返回的数据
                            mLatitude = data.getExtras().getString("mLatitude");
                            mLongitude = data.getExtras().getString("mLongitude");
                            markPlace = new LatLng(Double.valueOf(mLatitude), Double.valueOf(mLongitude));
                            changeCamera(
                                    CameraUpdateFactory.newCameraPosition(new CameraPosition(
                                            markPlace, 18, 0, 30)), null);
                            //以下这个是标记上面这个经纬度在地图的位置是
                            marker = aMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(Double.valueOf(mLatitude), Double.valueOf(mLongitude)))
                                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.poi_marker_1)))
                                    .draggable(false));
                        }
                        break;
                    default:
                        break;
                }
                break;

            default:
                break;
        }
    }

    private DateHandler mHandler;

    private class DateHandler extends Handler {

        private static final int WHAT_UPDATE_TIME = 100;
        private static final int WHAT_UPDATE_DISTANCE = 101;


        void resume(int what) {
            if (!hasMessages(what)) {
                sendEmptyMessage(what);
            }
        }

        void pause(int what) {
            removeMessages(what);
        }

        @Override
        public void handleMessage(Message message) {
            long delayMillis;
            int what;
            switch (message.what) {
                case WHAT_UPDATE_TIME:
                    delayMillis = 1 * 1000;
                    what = WHAT_UPDATE_TIME;
                    mTime.setText(HcUtil.getNowDate(HcUtil.FORMAT_CITY_HOUR));
                    break;
                case WHAT_UPDATE_DISTANCE:
                    what = WHAT_UPDATE_DISTANCE;
                    delayMillis = 5 * 1000;
                    break;

                default:
                    return;
            }
            sendEmptyMessageDelayed(what, delayMillis);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String address = SignLoctionUtils.getAddress();
//       if (!TextUtils.isEmpty(address) && !resultFlag) {
        if (!TextUtils.isEmpty(address)) {
            mAddr.setText(address);
        }
        if (!TextUtils.isEmpty(result)) {
            mAddr.setText(result);
        }
        if (mHandler != null) {
            mHandler.resume(DateHandler.WHAT_UPDATE_TIME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mHandler != null) {
            mHandler.pause(DateHandler.WHAT_UPDATE_TIME);
        }
    }

    @Override
    protected void onDestroy() {
        mUris.clear();
        mLayouts.clear();
        super.onDestroy();
    }


    private void sign() {
        String address = mAddr.getText().toString();
        if (TextUtils.isEmpty(address)) {
            address = SignLoctionUtils.getAddress();
            if (TextUtils.isEmpty(address)) {
                HcUtil.showToast(this, "签到地址不能为空!");
                return;
            }
        }

        String account = SettingHelper.getAccount(this);
//        String lng = SignCache.getInstance().getLongitude(); // "130.3454353";
//        String lat = SignCache.getInstance().getLatitude(); // "30.4234243";
        String remark = mDescription.getText().toString();
        List<String> uris = new ArrayList<>(mUris.values());
        HcLog.D("uris = "+uris);
        if (TextUtils.isEmpty(remark)) {
            remark = "";
        }
        String lng = String.valueOf(SignLoctionUtils.getLng());
        String lat = String.valueOf(SignLoctionUtils.getLat());
        if (TextUtils.isEmpty(lng) || TextUtils.isEmpty(lat)) {
            HcUtil.showToast(this, "定位失败!");
            return;
        }
        if (!TextUtils.isEmpty(mLatitude) && !TextUtils.isEmpty(mLongitude)) {
            lat = mLatitude;
            lng = mLongitude;
        }
        HcDialog.showProgressDialog(this,  R.string.dialog_title_post_data);
        HcHttpRequest.getRequest().sendFieldSignCommand(account, lng, lat, address, remark, uris, this);
    }

    @Override
    public void notify(Object data, RequestCategory request, ResponseCategory category) {
        HcLog.D(TAG + " #notify data = " + data + " request = " + request + " category = " + category);
        HcDialog.deleteProgressDialog();
        switch (request) {
            case SIGN:
                switch (category) {
                    case SUCCESS:
                        if (data != null && data instanceof String) {
                            try {
                                JSONObject object = new JSONObject((String) data);
                                if (HcUtil.hasValue(object, "msg")) {
                                    HcUtil.showToast(this, object.getString("msg"));
                                }
                            } catch (Exception e) {
                                // TODO: handle exception
                            }
                        } else {
                            ; // do nothing
                        }
                        // 刷新?
                        finish();
                        break;
                    case NETWORK_ERROR:
                        HcUtil.toastNetworkError(this);

                        break;
                    case DATA_ERROR:
                        HcUtil.toastDataError(this);

                        break;
                    case SESSION_TIMEOUT:
                        HcUtil.toastTimeOut(this);

                        break;
                    case SYSTEM_ERROR:
                        HcUtil.toastSystemError(this, data);

                        break;
                    case REQUEST_FAILED:
                        ResponseCodeInfo info = (ResponseCodeInfo) data;

                        if (HcHttpRequest.REQUEST_TOKEN_FAILED == info.getCode() ||
                                HcHttpRequest.REQUEST_ACCOUT_EXCLUDED == info.getCode()) {
                            HcUtil.reLogining(info.getBodyData(), this, info.getMsg());
                        } else {
                            HcUtil.showToast(this, info.getMsg());
                        }
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

    }

    private void startMapActivity() {
        Intent intent = new Intent(this, MapTrunActivity.class);
        startActivityForResult(intent, HcUtil.REQUEST_CODE_FOR_TEXT);
    }

    /**
     * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
     */
    private void changeCamera(CameraUpdate update, AMap.CancelableCallback callback) {
        //带动画的移动
        aMap.animateCamera(update, 1000, callback);
    }
}
