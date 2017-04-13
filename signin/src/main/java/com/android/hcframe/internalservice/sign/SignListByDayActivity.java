package com.android.hcframe.internalservice.sign;


import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.internalservice.signcls.SignImgList;
import com.android.hcframe.internalservice.signcls.SignListByDay;
import com.android.hcframe.internalservice.signin.R;
import com.android.hcframe.sql.SettingHelper;
import com.android.hcframe.view.toast.NoDataView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignListByDayActivity extends HcBaseActivity implements View.OnClickListener, AMap.OnMarkerClickListener {
    private static final String TAG = "SignListByDayActivity";
    private Handler mHandler = new Handler();
    private SignListByDayResponse mResponse;
    private MapView mapView;
    private AMap aMap;
    private LinearLayout mSignListByDay;
    private NoDataView mNotSignListByDay;
    private UiSettings mUiSettings;
    private ImageView imgLeftBtn;
    private ImageView imgRightBtn;
    private Calendar calendar;
    private TextView dateText;
    private List<SignListByDay> signListByDayList;
    private List<SignImgList> signImgByDayList;
    private SignListByDayAdapter signListByDayAdapter;
    private String signDay;
    private String account;
    private String name;
    /**
     * 下拉刷新
     */
    private ListView signListByDayListView;
    private TextView mEmptyText;
    /**
     * 点击标记物弹出popWindow信息
     */
    private StationInfoPopupWindow popWindow;
    /**
     * Mark标记
     */
    private Marker marker;
    /**
     * 展示popWindow布局
     */
    private RelativeLayout mpop;
    /**
     * 自定义头部
     */
    private TopBarView mTopBarView;
    private Handler signListByDayHandler = new Handler() {
        public void handleMessage(Message msg) {
            addMarkerToMap();
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_list_by_day);
        init(savedInstanceState);
        initData();
        SignListByDayRequest request = new SignListByDayRequest(signDay, account);
        if (mResponse == null) {
            mResponse = new SignListByDayResponse();
        }
        HcDialog.showProgressDialog(this, R.string.dialog_title_get_data);
        request.sendRequestCommand(RequestCategory.SIGNLISTBYDAY, mResponse, false);
    }

    private void initData() {
        /***
         *获取点击时间，并向服务器发送数据查询
         */
        Intent intent = getIntent();
        signDay = intent.getStringExtra("signDay");
        account = intent.getStringExtra("account");
        name = intent.getStringExtra("name");
        if (name != null) {
            mTopBarView.setTitle(name);
        } else {
            mTopBarView.setTitle(SettingHelper.getName(SignListByDayActivity.this));
        }
        String[] signDays = signDay.split("-");
        dateText.setText(signDays[0] + "年" + signDays[1] + "月" + signDays[2] + "日");
        isEmptyMap();
    }


    private void init(Bundle savedInstanceState) {
        signListByDayList = new ArrayList<>();
        signImgByDayList = new ArrayList<>();
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        aMap = mapView.getMap();
        mUiSettings = aMap.getUiSettings();//实例化UiSettings类
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 普通地图模式
        aMap.setOnMarkerClickListener(this);
        mUiSettings.setZoomControlsEnabled(false);
        mTopBarView = (TopBarView) findViewById(R.id.center_top_bar);
        mpop = (RelativeLayout) findViewById(R.id.rent_map_pop);
        imgLeftBtn = (ImageView) findViewById(R.id.img_left);
        imgRightBtn = (ImageView) findViewById(R.id.img_right);
        dateText = (TextView) findViewById(R.id.map_tun_tv);
        signListByDayListView = (ListView) findViewById(R.id.sign_list_by_day_lv);
        mSignListByDay = (LinearLayout) findViewById(R.id.sign_list_by_day);
        mNotSignListByDay = (NoDataView) findViewById(R.id.not_sign_list_by_day);
        mEmptyText = (TextView) findViewById(R.id.listview_empty_text);
        calendar = Calendar.getInstance();
        imgLeftBtn.setOnClickListener(this);
        imgRightBtn.setOnClickListener(this);
        signListByDayListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick() : pos=" + position + ", id=" + id);
                // do something
                SignListByDay signListByDay = (SignListByDay) parent.getAdapter().getItem(position);
                String mLongitude = signListByDay.getAddressLongitude();
                String mLatitude = signListByDay.getAddressLatitude();
                LatLng markPlace = new LatLng(Double.valueOf(mLatitude), Double.valueOf(mLongitude));
                changeCamera(
                        CameraUpdateFactory.newCameraPosition(new CameraPosition(
                                markPlace, 18, 0, 30)), null);
            }

        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        addMarkerToMap();
    }


    /**
     * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
     */
    private void changeCamera(CameraUpdate update, AMap.CancelableCallback callback) {
        //带动画的移动
        aMap.animateCamera(update, 1000, callback);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void addMarkerToMap() {
        marker.remove();
        if (signListByDayList.size() == 0 && signListByDayList.isEmpty() && signListByDayList == null) {
            isEmptyMap();
        } else {
            /**
             * 获取第i个的经纬度并显示在地图上
             * */
            for (int i = 0; i < signListByDayList.size(); i++) {
                String longitude = signListByDayList.get(i).getAddressLongitude();
                String latitude = signListByDayList.get(i).getAddressLatitude();
                String signInTime = signListByDayList.get(i).getSignInTime();
                String signOutTime = signListByDayList.get(i).getSignOutTime();
                LatLng markPlace = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
                changeCamera(
                        CameraUpdateFactory.newCameraPosition(new CameraPosition(
                                markPlace, 18, 0, 30)), null);
                //以下这个是标记上面这个经纬度在地图的位置是
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(Double.valueOf(latitude), Double.valueOf(longitude)));
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.poi_marker_1)));
                markerOptions.draggable(false);
                if (signOutTime != null) {
                    markerOptions.snippet(signOutTime);//里面的内容自己定义
                    markerOptions.title(signInTime);
                } else {
                    markerOptions.title(signInTime);
                    markerOptions.snippet("");
                }
                marker = aMap.addMarker(markerOptions);
            }
            /**
             * 初始化signListByDayAdapter
             * */
            signListByDayAdapter = new SignListByDayAdapter(SignListByDayActivity.this, signListByDayList);
            signListByDayListView.setAdapter(signListByDayAdapter);
            signListByDayAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 显示框框在图标上popupwindows ***
     */
    public boolean onMarkerClick(Marker marker) {
//        Toast.makeText(this, "你点击了的是marker", Toast.LENGTH_LONG).show();
//        popWindow = new StationInfoPopupWindow(this);
//        popWindow.showAsDropDown(mpop);
        return false;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        aMap.clear();// 清理地图上的所有覆盖物
        if (i == R.id.img_left) {
            //得到前一天
            signDay = getSpecifiedDayBefore(signDay);
            HcLog.D("onClick#img_left = " + signDay);
            if (!TextUtils.isEmpty(signDay)) {
                String[] signDays = signDay.split("-");
                dateText.setText(signDays[0] + "年" + signDays[1] + "月" + signDays[2] + "日");
                SignListByDayRequest request = new SignListByDayRequest(signDay, account);
                if (mResponse == null) {
                    mResponse = new SignListByDayResponse();
                }
                HcDialog.showProgressDialog(this, R.string.dialog_title_get_data);
                request.sendRequestCommand(RequestCategory.SIGNLISTBYDAY, mResponse, false);
//                HcDialog.showProgressDialog(SignListByDayActivity.this, R.string.dialog_title_post_data);
//                HcHttpRequest.getRequest().getSignListByDay(signDay, this);
            }
        } else if (i == R.id.img_right) {
            signDay = getSpecifiedDayAfter(signDay);
            HcLog.D("onClick#img_right = " + signDay);
            if (!TextUtils.isEmpty(signDay)) {
                String[] signDays = signDay.split("-");
                dateText.setText(signDays[0] + "年" + signDays[1] + "月" + signDays[2] + "日");
                SignListByDayRequest request = new SignListByDayRequest(signDay, account);
                if (mResponse == null) {
                    mResponse = new SignListByDayResponse();
                }
                HcDialog.showProgressDialog(this, R.string.dialog_title_get_data);
                request.sendRequestCommand(RequestCategory.SIGNLISTBYDAY, mResponse, false);
//                HcDialog.showProgressDialog(SignListByDayActivity.this, R.string.dialog_title_post_data);
//                HcHttpRequest.getRequest().getSignListByDay(signDay, this);
            }
        }
    }

    /**
     * 获得指定日期的前一天
     *
     * @param specifiedDay
     * @return
     * @throws Exception
     */
    public static String getSpecifiedDayBefore(String specifiedDay) {
        Calendar c = Calendar.getInstance();
        Date date = null;
        try {
            date = new SimpleDateFormat("yy-MM-dd").parse(specifiedDay);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.setTime(date);
        int day = c.get(Calendar.DATE);
        c.set(Calendar.DATE, day - 1);

        String dayBefore = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
        return dayBefore;
    }

    /**
     * 获得指定日期的后一天
     *
     * @param specifiedDay
     * @return
     */
    public static String getSpecifiedDayAfter(String specifiedDay) {
        Calendar c = Calendar.getInstance();
        Date date = null;
        try {
            date = new SimpleDateFormat("yy-MM-dd").parse(specifiedDay);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.setTime(date);
        int day = c.get(Calendar.DATE);
        c.set(Calendar.DATE, day + 1);

        String dayAfter = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
        return dayAfter;
    }

    private class SignListByDayRequest extends AbstractHttpRequest {

        private static final String TAG = SignListByDayActivity.TAG + "$LocalSignRequest";
        Map<String, String> httpparams = new HashMap<>();

        public SignListByDayRequest(String searchDate, String account) {
            httpparams.put("searchDate", searchDate);
//            httpparams.put("account", account);
            httpparams.put("userId", account);
        }

        @Override
        public String getParameterUrl() {
            String signListByDayUrl = "";
            try {
                signListByDayUrl = HcUtil.getGetParams(HcUtil.mapToList(httpparams));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return signListByDayUrl;
        }

        @Override
        public String getRequestMethod() {
            return "getSignListByDay";
        }
    }

    private class SignListByDayResponse extends AbstractHttpResponse {

        private static final String TAG = SignListByDayActivity.TAG + "$SignListByDayResponse";

        public SignListByDayResponse() {
        }

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            HcDialog.deleteProgressDialog();
            if (signListByDayList.size() > 0)
                signListByDayList.clear();
            if (signImgByDayList.size() > 0)
                signImgByDayList.clear();
            try {
                JSONObject objectBody = new JSONObject((String) data);
                if (HcUtil.hasValue(objectBody, "searchDate")) {
                    String searchDate = objectBody.getString("searchDate");
                }
                JSONArray jsonBodyArray = objectBody.getJSONArray("signlist");
                if (jsonBodyArray.length() == 0) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mNotSignListByDay.setVisibility(View.VISIBLE);
                            mSignListByDay.setVisibility(View.GONE);
                        }
                    });
                } else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mNotSignListByDay.setVisibility(View.GONE);
                            mSignListByDay.setVisibility(View.VISIBLE);
                        }
                    });
                }
                for (int i = 0; i < jsonBodyArray.length(); i++) {
                    SignListByDay signListByDay = new SignListByDay();
                    JSONObject signListObj = jsonBodyArray.getJSONObject(i);
                    if (HcUtil.hasValue(signListObj, "addressLatitude")) {
                        String addressLatitude = signListObj.getString("addressLatitude");
                        signListByDay.setAddressLatitude(addressLatitude);
                    }
                    if (HcUtil.hasValue(signListObj, "addressLongitude")) {
                        String addressLongitude = signListObj.getString("addressLongitude");
                        signListByDay.setAddressLongitude(addressLongitude);
                    }
                    if (HcUtil.hasValue(signListObj, "addressName")) {
                        String addressName = signListObj.getString("addressName");
                        signListByDay.setAddressName(addressName);
                    }
                    if (HcUtil.hasValue(signListObj, "id")) {
                        String id = signListObj.getString("id");
                        signListByDay.setId(id);
                    }
                    if (HcUtil.hasValue(signListObj, "maxDistance")) {
                        String maxDistance = signListObj.getString("maxDistance");
                        signListByDay.setMaxDistance(maxDistance);
                    }
                    if (HcUtil.hasValue(signListObj, "signDate")) {
                        String signDate = signListObj.getString("signDate");
                        signListByDay.setSignDate(signDate);
                    }
                    String signType = null;
                    if (HcUtil.hasValue(signListObj, "signType")) {
                        signType = signListObj.getString("signType");
                        signListByDay.setSignType(signType);
                    }
                    if (HcUtil.hasValue(signListObj, "remark")) {
                        String remark = signListObj.getString("remark");
                        signListByDay.setRemark(remark);
                    }
                    if (!"0".equals(signType)) {
                        int imgNum = 0;
                        signImgByDayList = new ArrayList<>();
                        JSONArray signImgListArray = signListObj.getJSONArray("signImgList");
                        for (int j = 0; j < signImgListArray.length(); j++) {
                            SignImgList signImgList = new SignImgList();
                            JSONObject signImgListArrayObj = signImgListArray.getJSONObject(j);
                            if (HcUtil.hasValue(signImgListArrayObj, "filePath")) {
                                String filePath = signImgListArrayObj.getString("filePath");
                                if (imgNum == 0) {
                                    signImgList.setFilePath(filePath);
                                } else if (imgNum == 1) {
                                    signImgList.setFilePath1(filePath);
                                } else if (imgNum == 2) {
                                    signImgList.setFilePath2(filePath);
                                } else if (imgNum == 3) {
                                    signImgList.setFilePath3(filePath);
                                }
                                imgNum++;
                            }
                            signImgByDayList.add(signImgList);
                        }
                        signListByDay.setSignImgList(signImgByDayList);
                        HcLog.D("#onSuccess signImgByDayList size=" + signImgByDayList.size());
                    }
                    if (HcUtil.hasValue(signListObj, "signInTime")) {
                        String signInTime = signListObj.getString("signInTime");
                        HcLog.D(TAG + "#onSuccess signInTime = " + signInTime);
                        String signInTimes[] = signInTime.split(":");
                        signListByDay.setSignInTime(signInTimes[0] + ":" + signInTimes[1]);
                    }
                    if (HcUtil.hasValue(signListObj, "signOutTime")) {
                        String signOutTime = signListObj.getString("signOutTime");
                        HcLog.D(TAG + "#onSuccess signOutTime = " + signOutTime);
                        String signOutTimes[] = signOutTime.split(":");
                        if (signOutTimes.length > 1)
                            signListByDay.setSignOutTime(signOutTimes[0] + ":" + signOutTimes[1]);
                        else {
                            signListByDay.setSignOutTime(signOutTime);
                        }
                    }
                    String userId = signListObj.getString("userId");
                    signListByDay.setUserId(userId);
                    signListByDayList.add(signListByDay);
                }
            } catch (JSONException e) {
                HcLog.D(TAG + " #onSuccess parseJson error =" + e);
            }
            Message message = Message.obtain();
            signListByDayHandler.sendMessage(message);
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//
//                }
//            }).start();
//            addMarkerToMap();
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
            HcLog.D(TAG + " #notifyRequestMd5Url md5Url = " + md5Url + " request = " + request);
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcDialog.deleteProgressDialog();
            HcUtil.reLogining(data, SignListByDayActivity.this, msg);
        }
    }

    private void isEmptyMap() {
        /**
         * 如果数据为空，则显示当前定位的点，并标注在地图上
         * */
        LatLng markPlace = new LatLng(SignLoctionUtils.getLat(), SignLoctionUtils.getLng());
        changeCamera(
                CameraUpdateFactory.newCameraPosition(new CameraPosition(
                        markPlace, 18, 0, 30)), null);
        //以下这个是标记上面这个经纬度在地图的位置是
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(SignLoctionUtils.getLat(), SignLoctionUtils.getLng()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.poi_marker_1)));
        markerOptions.draggable(false);
        marker = aMap.addMarker(markerOptions);
    }
}
