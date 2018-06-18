package cn.edu.hqu.monkey.mapfence.data.map;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.edu.hqu.monkey.mapfence.R;
import cn.edu.hqu.monkey.mapfence.data.repo.IPointSet;
import cn.edu.hqu.monkey.mapfence.data.repo.PointSet;
import cn.edu.hqu.monkey.mapfence.map.MapContract;

import static android.support.constraint.Constraints.TAG;

/**
 * Model层
 * Created by monkey on 2018/5/24.
 */

public class BDMap implements MapDataSource {


    private static BDMap INSTANCE = null;

    public LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();
    private MyLocationConfiguration.LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    private BDMap.OnLocationListener locationListener;

    private LatLng latLng;

    private static final int accuracyCircleFillColor = 0xAAFFFF88;
    private static final int accuracyCircleStrokeColor = 0xAA00FF00;


    private BaiduMap mBaiduMap;

    private boolean isFirstLoc = true;  // 是否是首次定位


    @NonNull
    private Context context;

    private MapContract.Presenter mPresenter;

    private BDMap(){}

    private BDMap(@NonNull Context context) {
        this.context = context;
        SDKInitializer.initialize(context);

    }

    public static BDMap getInstance(@NonNull Context context) {
        synchronized (BDMap.class) {

            if (null == INSTANCE) {
                INSTANCE = new BDMap(context);
            }
        }
        return INSTANCE;

    }

    public void setBaiduMap(BaiduMap mBaiduMap) {
        this.mBaiduMap = mBaiduMap;
        initMap(context);
    }

    private void initMap(Context context)
    {
        mBaiduMap.setMyLocationEnabled(true);

        //默认显示普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //开启交通图
        //mBaiduMap.setTrafficEnabled(true);
        //开启热力图
        //mBaiduMap.setBaiduHeatMapEnabled(true);


        // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）

       // mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);
        MyLocationConfiguration config = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL,
                false, null);
        mBaiduMap.setMyLocationConfiguration(config);

        // 开启定位图层
        mLocationClient = new LocationClient(context);

        mLocationClient.registerLocationListener(myListener);
        //配置定位SDK参数
        initLocation();

        // setLocationMode(MyLocationConfiguration.LocationMode.FOLLOWING);

        //开启定位
        mLocationClient.start();
        //图片点击事件，回到定位点
        mLocationClient.requestLocation();

    }

    private void initLocation()
    {
        LocationClientOption option = new LocationClientOption();

        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
//可选，设置定位模式，默认高精度
//LocationMode.Hight_Accuracy：高精度；
//LocationMode. Battery_Saving：低功耗；
//LocationMode. Device_Sensors：仅使用设备；

        option.setCoorType("bd09ll");
//可选，设置返回经纬度坐标类型，默认gcj02
//gcj02：国测局坐标；
//bd09ll：百度经纬度坐标；
//bd09：百度墨卡托坐标；
//海外地区定位，无需设置坐标类型，统一返回wgs84类型坐标

        option.setScanSpan(1000);
//可选，设置发起定位请求的间隔，int类型，单位ms
//如果设置为0，则代表单次定位，即仅定位一次，默认为0
//如果设置非0，需设置1000ms以上才有效

        option.setOpenGps(true);
//可选，设置是否使用gps，默认false
//使用高精度和仅用设备两种定位模式的，参数必须设置为true

        option.setLocationNotify(true);
//可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false

        option.setIgnoreKillProcess(true);
//可选，定位SDK内部是一个service，并放到了独立进程。
//设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)

        option.disableCache(true);
//可选，设置是否收集Crash信息，默认收集，即参数为false

        option.setWifiCacheTimeOut(5*60*1000);
//可选，7.2版本新增能力
//如果设置了该接口，首次启动定位时，会先判断当前WiFi是否超出有效期，若超出有效期，会先重新扫描WiFi，然后定位

        option.setEnableSimulateGps(false);
//可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false

        option.setIsNeedAddress(true);
//可选，是否需要地址信息，默认为不需要，即参数为false
//如果开发者需要获得当前点的地址信息，此处必须为true



        mLocationClient.setLocOption(option);
//mLocationClient为第二步初始化过的LocationClient对象
//需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
//更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明
    }




    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location){
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

            if (null == mBaiduMap || null == location){
                return;
            }

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();

            latLng = new LatLng(location.getLatitude(),
                    location.getLongitude());


            // 设置定位数据
            mBaiduMap.setMyLocationData(locData);

           if (isFirstLoc){
                isFirstLoc = false;
                MapStatus.Builder status=new MapStatus.Builder();

                //设置缩放的等级和中心点+
                status.zoom(19).target(latLng);
                //地图改变的是的状态的动画
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(status.build()));
            }

             /* 提供外部位置监听 */
            locationListener.onMapLocation(new LatLng(location.getLatitude(),
                    location.getLongitude()));
        }
    }


    @Override
    public void enLocation(boolean enable) {
        mBaiduMap.setMyLocationEnabled(enable);

    }

    @Override
    public void setLocationMode(MyLocationConfiguration.LocationMode mode) {
        mCurrentMode = mode;
        MyLocationConfiguration config = new MyLocationConfiguration(mode, false, null);
        mBaiduMap.setMyLocationConfiguration(config);
    }

    public BaiduMap getmBaiduMap() {
        return mBaiduMap;
    }

    @Override
    public void setClickListen(@NonNull BaiduMap.OnMapClickListener listen) {
        mBaiduMap.setOnMapClickListener(listen);
    }

    @Override
    public void setDoubleClickListen(@NonNull BaiduMap.OnMapDoubleClickListener listen) {
        mBaiduMap.setOnMapDoubleClickListener(listen);
    }

    @Override
    public void setMarkerClickListener(BaiduMap.OnMarkerClickListener listener) {
        mBaiduMap.setOnMarkerClickListener(listener);
    }

    @Override
    public void setMarkerDragListener(BaiduMap.OnMarkerDragListener listener) {
        mBaiduMap.setOnMarkerDragListener(listener);
    }

    @Override
    public void drawMarker(LatLng latLng) {

        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_gcoding);

        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(latLng)
                .icon(bitmap);

        //在地图上添加Marker，并显示

        mBaiduMap.addOverlay(option);
    }

    @Override
    public void drawMarker(List<LatLng> latLngs) {
        List<OverlayOptions> options = new ArrayList<OverlayOptions>();

        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_gcoding);

        Log.d(TAG, "drawMarker: "+latLngs.size());
        for (int i = 0 ; i < latLngs.size(); i++) {
            //构建MarkerOption，用于在地图上添加Marker
            options.add(new MarkerOptions()
                    .position(latLngs.get(i))
                    .icon(bitmap)
                    .zIndex(9)  //设置Marker所在层级
                    .draggable(true));  //设置手势拖拽;
        }

        //在地图上添加Marker，并显示
        mBaiduMap.addOverlays(options);

    }

    @Override
    public void drawLine(List<LatLng> latLngs) {

        List<Integer> colors = new ArrayList<>();
        colors.add(Integer.valueOf(Color.BLUE));

        OverlayOptions ooPolyline = new PolylineOptions().width(10)
                .colorsValues(colors).points(latLngs);

        // 添加在地图中
        Polyline mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);

        // 构成一个闭包
        List<LatLng> points = new ArrayList<LatLng>();

        points.add(latLngs.get(0));
        points.add(latLngs.get(latLngs.size()-1));

        ooPolyline = new PolylineOptions().width(10)
                .colorsValues(colors).points(points);

        mBaiduMap.addOverlay(ooPolyline);
    }

    @Override
    public void drawPolygon(List<LatLng> latLngs) {

        //构建用户绘制多边形的Option对象
        OverlayOptions polygonOption = new PolygonOptions()
                .points(latLngs)
                .stroke(new Stroke(5, 0xAA00FF00))
                .fillColor(0xAAFFFF00);

        //在地图上添加多边形Option，用于显示
        mBaiduMap.addOverlay(polygonOption);
    }

    @Override
    public void clearAllMarker() {
        mBaiduMap.clear();
    }

    @Override
    public void drawMarkLine(IPointSet pointSet) {

        mBaiduMap.clear();
        drawMarker(pointSet.getPointList());

        if (pointSet.getPointCnt() > 1){
            drawLine(pointSet.getPointList());
        }

    }

    @Override
    public LatLng getMyLocation() {
        return latLng;
    }

    @Override
    public void focusedCurrentLocation() {
        Log.d(TAG, "focusedCurrentLocation: ");
        isFirstLoc = true;
    }

    @Override
    public void setLocationListener(BDMap.OnLocationListener locationListener) {
        this.locationListener = locationListener;
    }

    /**
     * 提供外部监听实时位置接口
      */
    public interface OnLocationListener{
        void onMapLocation(LatLng latLng);
    }
}
