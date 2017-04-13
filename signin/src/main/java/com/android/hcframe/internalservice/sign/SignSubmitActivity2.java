package com.android.hcframe.internalservice.sign;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
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
import com.android.hcframe.view.gallery.GalleryView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 17-3-8 08:54.
 */

public class SignSubmitActivity2 extends HcBaseActivity implements View.OnClickListener,
        IHttpResponse {

    private static final String TAG = "SignSubmitActivity";

    private TextView mTime;

    private TextView mAddr;

    private TextView mSwitch;

    private EditText mDescription;

    private TextView mSubmit;

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

    private GalleryView mGalleryView;
    private int mCount; // 需要上传的图片数量

    private static final int MAX_IMAGE_SIZE = 320 * 480 / 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_submit_layout);

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

        mSubmit = (TextView) findViewById(R.id.field_sign_submit_btn);

        mTopBarView = (TopBarView) findViewById(R.id.field_sign_top_bar);

        mGalleryView = (GalleryView) findViewById(R.id.sign_submit_add_galleryview);

        mSwitch.setOnClickListener(this);
        mSubmit.setOnClickListener(this);
    }

    private void initData() {
        mHandler = new DateHandler();
        mTime.setText(HcUtil.getDate(HcUtil.FORMAT_CITY_HOUR, System.currentTimeMillis()));


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

//        mGalleryView.setAddButtonSrc("drawable://" + R.drawable.sign_add_src);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.field_sign_switch_addr) {
            startMapActivity();
        } else if (id == R.id.field_sign_submit_btn) {
            if (HcUtil.isNetWorkAvailable(this)) {
                sign();
            } else {
                Toast.makeText(getApplicationContext(), "网络不给力！",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        HcLog.D(TAG + " onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode + " data = " + data);

        switch (resultCode) {
            case RESULT_OK:
                switch (requestCode) {
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
                    case HcUtil.REQUEST_CODE_FROM_CAMERA:
                        if (mGalleryView != null) {
                            mGalleryView.onActivityResult(requestCode, resultCode, data);
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
        //把图片放入uris中
        List<GalleryView.GalleryItemInfo> infos = mGalleryView.getImages();
        List<String> uris = new ArrayList<String>();
        if (infos.isEmpty()) {
            uris = null;
        } else {
            mCount = infos.size();
            for (GalleryView.GalleryItemInfo info : infos) {
                uris.add(info.mUri);
            }
            infos.clear();
        }
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
        HcDialog.showProgressDialog(this, R.string.dialog_title_post_data);
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
