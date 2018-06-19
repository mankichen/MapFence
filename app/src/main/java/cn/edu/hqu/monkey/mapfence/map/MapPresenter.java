package cn.edu.hqu.monkey.mapfence.map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.SpatialRelationUtil;

import java.util.logging.LogRecord;

import cn.edu.hqu.monkey.mapfence.R;
import cn.edu.hqu.monkey.mapfence.data.map.BDMap;
import cn.edu.hqu.monkey.mapfence.data.map.MapDataSource;
import cn.edu.hqu.monkey.mapfence.data.repo.IPointSet;
import cn.edu.hqu.monkey.mapfence.data.repo.PointSet;
import terranovaproductions.newcomicreader.FloatingActionMenu;

import static android.support.constraint.Constraints.TAG;


/**
 * P层
 * Created by monkey on 2018/5/25.
 */

public class MapPresenter implements MapContract.Presenter {

    /* 两个状态 */
    /* 会否处于绘制边界 */
    private static boolean isDrawingFence = false;
    /* 是否开始监听位置变化 */
    private static boolean isStartListen = false;

    @NonNull
    private final BDMap mBDMap;

    @NonNull
    private final MapContract.View mView;

    @NonNull
    private final IPointSet pointSet;

    @NonNull
    private final MapDataSource mapDataSource;

    @NonNull
    private final MapActivity mapActivity;

    /* 消息队列区 */
    private final int MSG_ALARM = 0x01;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ALARM:
                    Log.d(TAG, "handleMessage: alarm");
                    Vibrator vibrator = (Vibrator)mapActivity.getSystemService(mapActivity.VIBRATOR_SERVICE);
                    vibrator.vibrate(1000);

                    mView.showAlarmDialog(alarmDialogListener);
                    break;
            }
            super.handleMessage(msg);

        }
    };


    public MapPresenter(@NonNull BDMap mBDMap, @NonNull MapContract.View mView, @NonNull MapActivity mapActivity) {
        this.mBDMap = mBDMap;
        this.mView = mView;

        pointSet = PointSet.getInstance();

        mapDataSource = mBDMap;

        this.mapActivity = mapActivity;

        mView.setPresenter(this);


    }


    private BaiduMap.OnMapClickListener clickListener = new BaiduMap.OnMapClickListener() {
        /**
         * 地图单击事件回调函数
         * @param point 点击的地理坐标
         */
        @Override
        public void onMapClick(LatLng point){
            Log.d(TAG, "onMapClick: "+point.toString());

            // mView.showToast(point.toString());

            if (isDrawingFence){
                pointSet.savePoint(point);

                mapDataSource.drawMarkLine(pointSet);
            }

        }
        /**
         * 地图内 Poi 单击事件回调函数
         * @param poi 点击的 poi 信息
         */
        @Override
        public boolean onMapPoiClick(MapPoi poi){
            Log.d(TAG, "onMapPoiClick: "+poi.toString());
            return false;
        }
    };

    private BaiduMap.OnMarkerClickListener markerClickListener = new BaiduMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            Log.d(TAG, "onMarkerClick: click");
            if (isDrawingFence && !pointSet.isEmpty()) {
                pointSet.deletePoint(marker.getPosition());
                marker.remove();
                mapDataSource.drawMarkLine(pointSet);
            }
            return false;
        }
    };

    private FloatingActionMenu.OnMenuItemClickListener menuItemClickListener = new FloatingActionMenu.OnMenuItemClickListener() {
        @Override
        public void onMenuItemClick(FloatingActionMenu fam, int index, FloatingActionButton item) {
            switch (index) {
                case 0:
                    startDrawFence();
                    break;
                case 1:
                    mapDataSource.clearAllMarker();
                    pointSet.clearAllPoint();
                    break;
                case 2:
                    Log.d(TAG, "onMenuItemClick: 2");
                    startLocationListen();
                    break;
                default:
            }
            // Toast.makeText(getActivity().getApplicationContext(), str, Toast.LENGTH_SHORT).show();
        }
    };


    private BaiduMap.OnMarkerDragListener markerDragListener = new BaiduMap.OnMarkerDragListener() {
        @Override
        public void onMarkerDrag(Marker marker) {
            //拖拽中
            Log.d(TAG, "onMarkerDrag: ing");
            pointSet.movePoint(marker.getPosition());

        }

        @Override
        public void onMarkerDragEnd(Marker marker) {
            //拖拽结束
            Log.d(TAG, "onMarkerDragEnd: 结束");
            mapDataSource.drawMarkLine(pointSet);
        }

        @Override
        public void onMarkerDragStart(Marker marker) {
            //开始拖拽
            Log.d(TAG, "onMarkerDragStart: 开始");
            pointSet.flgMovePoint(marker.getPosition());
        }
    };

    private BDMap.OnLocationListener locationListener = new BDMap.OnLocationListener() {
        @Override
        public void onMapLocation(LatLng latLng) {
            Log.d(TAG, "onMapLocation: "+latLng.toString());
            if (isStartListen && !SpatialRelationUtil. isPolygonContainsPoint(pointSet.getPointList(), latLng)){
                handler.sendEmptyMessage(MSG_ALARM);
            }
        }
    };

    /**
     * 移动到当前位置
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mapDataSource.focusedCurrentLocation();
        }
    };

    private DialogInterface.OnClickListener alarmDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            startLocationListen();
        }
    };
    
    public void start() {
        Log.d(TAG, "start: ");
        mapDataSource.setClickListen(clickListener);
        mapDataSource.setMarkerClickListener(markerClickListener);
        mapDataSource.setMarkerDragListener(markerDragListener);
        mapDataSource.setLocationListener(locationListener);
    }

    @Override
    public void loadMap(BaiduMap baiduMap) {
        mBDMap.setBaiduMap(baiduMap);
    }

    @Override
    public void requestLocation() {

    }

    /**
     * 开始绘制栅栏
     */
    private void startDrawFence() {

        UiSettings settings = mBDMap.getmBaiduMap().getUiSettings();
        settings.setAllGesturesEnabled(isDrawingFence);   //关闭一切手势操作
        settings.setOverlookingGesturesEnabled(isDrawingFence);//屏蔽双指下拉时变成3D地图
        settings.setRotateGesturesEnabled(isDrawingFence);//屏蔽旋转
        settings.setZoomGesturesEnabled(isDrawingFence);//获取是否允许缩放手势返回:是否允许缩放手势

        if (isDrawingFence){

            if (pointSet.getPointCnt() < 3) {
                if (0 != pointSet.getPointCnt()){

                    mView.showToast("请选择超过3个点");
                    return;
                }
            } else {
            /* 先清空点与面 */
                mapDataSource.clearAllMarker();

            /* 绘制多边形 */
                mapDataSource.drawPolygon(pointSet.getPointList());
            }

            /* 更换图标 */
            // mView.modifyFABEditBG(R.drawable.ok);
            mapActivity.setToolBarHide(false);
        }else {
            /* 在绘制边界状态时，不在检测外界变化 */
            isStartListen = false;


            /* 先清空点与面 */
            mapDataSource.clearAllMarker();

            /* 绘制多边形 */
            mapDataSource.drawMarkLine(pointSet);

            // mView.modifyFABEditBG(R.drawable.edit_fence);
            mapActivity.setToolBarHide(true);
        }

        /* 翻转状态 */
        isDrawingFence = !isDrawingFence;

    }

    private void startLocationListen(){

        /* 在不是绘制边检状态才可以进行任务 */
        if (isDrawingFence){
            mView.showToast("请先选择区域。");
            return;
        }

        if (!isStartListen){

            /* 判断是否已经选择3个点 */
            if (pointSet.getPointCnt() < 3){
                mView.showToast("请先选择区域。");
                return;
            }

            /* 判断现在位置是否处于用户的画的多边形里面 */
            if (!SpatialRelationUtil. isPolygonContainsPoint(pointSet.getPointList(),
                    mapDataSource.getMyLocation())){
                Log.d(TAG, "startLocationListen: 超过边界");
                mView.showToast("您的位置不在区域内！");

                return;
            }

            /* 设置为跟随模式 */
            mapDataSource.setLocationMode(MyLocationConfiguration.LocationMode.FOLLOWING);
            /* 更换图标 */
            // mView.modifyFABListenBG(R.drawable.start);
            mapActivity.modifyToolBarTitle("正在边界检测任务");
            Log.d(TAG, "startLocationListen: runing");
        }else {
            /* 设置为正常模式 */
            mapDataSource.setLocationMode(MyLocationConfiguration.LocationMode.NORMAL);
            // mView.modifyFABListenBG(R.drawable.stop);
            mapActivity.modifyToolBarTitle("MapFence");
            Log.d(TAG, "startLocationListen: mapfence");
        }

        /* 翻转状态 */
        isStartListen = !isStartListen;
    }


    @Override
    public FloatingActionMenu.OnMenuItemClickListener getMenuItemClickListener() {
        return menuItemClickListener;
    }

    @Override
    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

}
