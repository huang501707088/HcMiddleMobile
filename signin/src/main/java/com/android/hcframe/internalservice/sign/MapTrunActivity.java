package com.android.hcframe.internalservice.sign;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.SearchBound;
import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcUtil;
import com.android.hcframe.internalservice.signcls.MapTun;
import com.android.hcframe.internalservice.signin.R;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapTrunActivity extends HcBaseActivity implements View.OnClickListener,
        AMap.OnInfoWindowClickListener, AMap.InfoWindowAdapter,
        PoiSearch.OnPoiSearchListener, LocationSource, AMapLocationListener {
    private String Tag = "MapTrunActivity";
    private TextView mLocationErrText;
    private Button confirmBtn;
    /**
     * 定位
     */
    private UiSettings mUiSettings;//不显示重新定位按钮
    private MapView mapView;
    private AMap aMap;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private boolean isSeachered = false;
    /**
     * Poi周边搜索
     */
    private PoiResult poiResult; // poi返回的结果
    private int currentPage = 0;// 当前页面，从0开始计数
    private PoiSearch.Query query;// Poi查询条件类
    private LatLonPoint lp;//定位经纬度点
    private PoiSearch poiSearch;
    private myPoiOverlay poiOverlay;// poi图层
    private List<PoiItem> poiItems;// poi数据
    private String keyWord = "";//查询关键字

    /**
     * Mark标记
     */
    private Marker marker;

    /**
     * checkboxlistviewda当选listview
     */
    List<MapTun> mList = new ArrayList<MapTun>();
    private CheckBoxListAdapter mCheckBoxAdapter;
    private Map<Integer, Boolean> isSelected;
    private List beSelectedData = new ArrayList();
    /**
     * 下拉刷新
     */
    private PullToRefreshListView checkBoxList;
    private TextView mEmptyText;
    /**
     * 选定位置的经纬坐标
     */
    private double mLatitude;
    private double mLongitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_gps_map);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        checkBoxList = (PullToRefreshListView) findViewById(R.id.map_lv);
        mLocationErrText = (TextView) findViewById(R.id.map_tun_tv);
        confirmBtn = (Button) findViewById(R.id.confirm_btn);
        mEmptyText = (TextView) findViewById(R.id.listview_empty_text);
        init();
        //初始化数据
        mLocationErrText.setText("");
        confirmBtn.setOnClickListener(this);
        checkBoxList.setScrollingWhileRefreshingEnabled(false);
        checkBoxList.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
        checkBoxList.setOnRefreshBothListener(new PullToRefreshBase.OnRefreshBothListener<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {

            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
//              Toast.makeText(getApplication(), "pullup", Toast.LENGTH_SHORT).show();
                if (HcUtil.isNetWorkAvailable(MapTrunActivity.this)) {
                    mEmptyText.setText("正在获取数据...");
                    doSearchQuery();
                    checkBoxList.onRefreshComplete();
                } else {
                    mEmptyText.setText("网络不给力！");
                    checkBoxList.onRefreshComplete();
                }
            }
        });
    }

    //    checkBoxList.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
//
//        public void onLastItemVisible() {
//            // TODO Auto-generated method stub
//            if (mListView.getMode() == PullToRefreshBase.Mode.PULL_FROM_START) {
//                HcUtil.showToast(mContext, "没有更多数据！");
//            }
//        }
//    });
    @Override
    protected void onResume() {
        super.onResume();

    }

    public class CheckBoxListAdapter extends BaseAdapter {
        private Context context;
        private List<MapTun> mapTunList;
        private LayoutInflater inflater;

        public CheckBoxListAdapter(Context context, List data) {
            this.context = context;
            this.mapTunList = data;
            initLayoutInflater();
        }

        void initLayoutInflater() {
            inflater = LayoutInflater.from(context);
        }

        public int getCount() {
            return mapTunList.size();
        }

        public Object getItem(int position) {
            return mapTunList.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position1, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            View view = null;
            final int position = position1;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.map_lv_layout,
                        null);
                holder = new ViewHolder();
                holder.checkBoxLv = (LinearLayout) convertView
                        .findViewById(R.id.checkbox_lv);
                holder.checkBox = (CheckBox) convertView
                        .findViewById(R.id.list_select);
                holder.address = (TextView) convertView
                        .findViewById(R.id.list_address);
                holder.addressOne = (TextView) convertView
                        .findViewById(R.id.list_address_one);
                convertView.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }
            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mLatitude = mList.get(position).getLatitude();
                    mLongitude = mList.get(position).getLongitude();
                    if (marker != null) {
                        marker.remove();
                    }
                    // 当前点击的CB
                    boolean cu = !isSelected.get(position);
                    // 先将所有的置为FALSE
                    for (Integer p : isSelected.keySet()) {
                        isSelected.put(p, false);
                    }
                    // 再将当前选择CB的实际状态
                    isSelected.put(position, cu);
                    LatLng markPlace = new LatLng(mLatitude, mLongitude);
                    changeCamera(
                            CameraUpdateFactory.newCameraPosition(new CameraPosition(
                                    markPlace, 18, 0, 30)), null);
                    //绘制marker
                    marker = aMap.addMarker(new MarkerOptions()
                            .position(new LatLng(mLatitude, mLongitude))
                            .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                    .decodeResource(getResources(), R.drawable.poi_marker_1)))
                            .draggable(false));
                    marker.setVisible(true);
                    mLocationErrText.setText(mList.get(position).getAddress().toString().trim());
                    CheckBoxListAdapter.this.notifyDataSetChanged();
                }
            });
            holder.checkBoxLv.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                     mLatitude = mList.get(position).getLatitude();
                     mLongitude = mList.get(position).getLongitude();
                    if (marker != null) {
                        marker.remove();
                    }
                    // 当前点击的CB
                    boolean cu = !isSelected.get(position);
                    // 先将所有的置为FALSE
                    for (Integer p : isSelected.keySet()) {
                        isSelected.put(p, false);
                    }
                    // 再将当前选择CB的实际状态
                    isSelected.put(position, cu);
                    LatLng markPlace = new LatLng(mLatitude, mLongitude);
                    changeCamera(
                            CameraUpdateFactory.newCameraPosition(new CameraPosition(
                                    markPlace, 18, 0, 30)), null);
                    //绘制marker
                    marker = aMap.addMarker(new MarkerOptions()
                            .position(new LatLng(mLatitude, mLongitude))
                            .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                    .decodeResource(getResources(), R.drawable.poi_marker_1)))
                            .draggable(false));
                    marker.setVisible(true);
                    mLocationErrText.setText(mList.get(position).getAddress().toString().trim());
                    CheckBoxListAdapter.this.notifyDataSetChanged();
                }
            });
            holder.address.setText(mapTunList.get(position).getAddress());
            holder.addressOne.setText(mapTunList.get(position).getAddressOne());
            holder.checkBox.setChecked(isSelected.get(position));
            return convertView;
        }
    }

    class ViewHolder {
        private CheckBox checkBox;
        private TextView address;
        private TextView addressOne;
        private LinearLayout checkBoxLv;
    }

    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            mUiSettings = aMap.getUiSettings();
            setUpMap();
        }
        mUiSettings.setMyLocationButtonEnabled(false);// 是否显示定位按钮
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
//        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
    }

    /**
     * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
     */
    private void changeCamera(CameraUpdate update, AMap.CancelableCallback callback) {
        //带动画的移动
        aMap.animateCamera(update, 1000, callback);
    }

    /**
     * 定位成功后回调函数
     */
    public void onLocationChanged(AMapLocation amapLocation) {
        if (!isSeachered) {
            if (mListener != null && amapLocation != null) {
                if (amapLocation != null
                        && amapLocation.getErrorCode() == 0) {
                    lp = new LatLonPoint(amapLocation.getLatitude(), amapLocation.getLongitude());
                    keyWord = "商务住宅|政府机构及社会团体|科教文化服务|交通设施服务|金融保险服务|公司企业";
//                    MapTun mapTun = new MapTun();
//                    mapTun.setAddress(amapLocation.gn.getAddress());
//                    mapTun.setLatitude(amapLocation.getLatitude());
//                    mapTun.setLongitude(amapLocation.getLongitude());
//                    mList.add(mapTun);etPoiName());
//                    mapTun.setAddressOne(amapLocatio
                    doSearchQuery();
                    mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
                    isSeachered = true;
//                    Toast.makeText(getApplicationContext(), amapLocation.getLongitude() + " " + amapLocation.getLatitude() + " " + amapLocation.getAddress() + "\n" + amapLocation.getLocationDetail() + "\n" + amapLocation.getAltitude() + "\n" + amapLocation.getCity(),
//                            Toast.LENGTH_SHORT).show();
                } else {
                    String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
                    mLocationErrText.setVisibility(View.VISIBLE);
                    mLocationErrText.setText(errText);
                }
            }
        }
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    /**
     * 开始进行poi搜索
     */
    protected void doSearchQuery() {
        query = new PoiSearch.Query(keyWord, "", "");// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query.setPageSize(6);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页
        Log.v("currentPage", currentPage + " ");
        if (lp != null) {
            poiSearch = new PoiSearch(this, query);
            poiSearch.setOnPoiSearchListener(this);
            poiSearch.setBound(new SearchBound(lp,SignCache.getInstance().getMaxDistance() == 0 ? 500 : SignCache.getInstance().getMaxDistance(), true));//
            // 设置搜索区域为以lp点为圆心，其周围5000米范围
            poiSearch.searchPOIAsyn();// 异步搜索
        }
    }

    @Override
    public void onPoiSearched(PoiResult result, int rcode) {
        if (rcode == 1000) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    poiItems = poiResult.getPois();
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息
                    if (poiItems != null && poiItems.size() > 0) {
                        aMap.clear();
                        poiOverlay = new myPoiOverlay(aMap, poiItems);
                        if (currentPage == 0) {
                            poiOverlay.addToMap();
                        } else {
                            poiOverlay.addTomMap();
                        }
                        currentPage++;
                        aMap.addMarker(new MarkerOptions()
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory
                                        .fromBitmap(BitmapFactory.decodeResource(
                                                getResources(), R.drawable.point)))
                                .position(new LatLng(lp.getLatitude(), lp.getLongitude())));
                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                        showSuggestCity(suggestionCities);
                    } else {
                        Toast.makeText(getApplicationContext(), "对不起，搜索不到周边你所需要的位置!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(getApplicationContext(), "对不起，搜索不到周边你所需要的位置!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.confirm_btn) {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("mLocationErrText", mLocationErrText.getText().toString().trim());
            if(!"".equals(String.valueOf(mLatitude))&&!"".equals(String.valueOf(mLongitude))){
                bundle.putString("mLatitude",String.valueOf(mLatitude));
                bundle.putString("mLongitude",String.valueOf(mLongitude));
            }
            intent.putExtras(bundle);
            setResult(Activity.RESULT_OK, intent); //intent为A传来的带有Bundle的intent，当然也可以自己定义新的Bundle
            finish();//此处一定要调用finish()方法
        } else {
            //do nothing
        }

    }

    /**
     * poi没有搜索到数据，返回一些推荐城市的信息
     */
    private void showSuggestCity(List<SuggestionCity> cities) {
        String infomation = "推荐城市\n";
        for (int i = 0; i < cities.size(); i++) {
            infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
                    + cities.get(i).getCityCode() + "城市编码:"
                    + cities.get(i).getAdCode() + "\n";
        }
        Toast.makeText(getApplicationContext(), "推荐的城市信息为" + infomation,
                Toast.LENGTH_SHORT).show();
    }

    /**
     * 自定义PoiOverlay
     */

    private class myPoiOverlay {
        private AMap mamap;
        private List<PoiItem> mPois;

        public myPoiOverlay(AMap amap, List<PoiItem> pois) {
            mamap = amap;
            mPois = pois;
        }

        /**
         * 获取周边地点信息
         *
         * @since V2.1.0
         */
        public void addToMap() {
            for (int i = 0; i < mPois.size(); i++) {
                MapTun mapTun = new MapTun();
                mapTun.setLongitude(mPois.get(i).getLatLonPoint().getLongitude());
                mapTun.setLatitude(mPois.get(i).getLatLonPoint().getLatitude());
                mapTun.setAddress(mPois.get(i) + "");
                mapTun.setAddressOne(mPois.get(i).getProvinceName() + " " + mPois.get(i).getCityName() + " " + mPois.get(i).getAdName() + " " + mPois.get(i).getBusinessArea() + " " + mPois.get(i));
                mList.add(mapTun);
            }
            initList();
        }

        public void addTomMap() {
            Log.v("MapTrunActivity", 6 * currentPage + " " + mPois.size() * (currentPage + 1));
            for (int i = 6 * currentPage, j = 0; i < mPois.size() * (currentPage + 1) && j < mPois.size(); i++, j++) {
                MapTun mapTun = new MapTun();
                mapTun.setLongitude(mPois.get(j).getLatLonPoint().getLongitude());
                mapTun.setLatitude(mPois.get(j).getLatLonPoint().getLatitude());
                mapTun.setAddress(mPois.get(j) + "");
                mapTun.setAddressOne(mPois.get(j).getProvinceName() + " " + mPois.get(j).getCityName() + " " + mPois.get(j).getAdName() + " " + mPois.get(j).getBusinessArea() + " " + mPois.get(j));
                mList.add(mapTun);
            }
            initList();
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }


    @Override
    public void onInfoWindowClick(Marker marker) {

    }


    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    public void initList() {
        if (mList == null || mList.size() == 0)
            return;
        if (isSelected != null)
            isSelected = null;
        isSelected = new HashMap<Integer, Boolean>();
        for (int i = 0; i < mList.size(); i++) {
            isSelected.put(i, false);
        }
        // 清除已经选择的项
        if (beSelectedData.size() > 0) {
            beSelectedData.clear();
        }
        mCheckBoxAdapter = new CheckBoxListAdapter(MapTrunActivity.this, mList);
        checkBoxList.setAdapter(mCheckBoxAdapter);
//        checkBoxList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mCheckBoxAdapter.notifyDataSetChanged();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (null != mlocationClient) {
            mlocationClient.onDestroy();
        }
    }
}