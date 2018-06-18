package cn.edu.hqu.monkey.mapfence.data.map;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.model.LatLng;

import java.util.List;

import cn.edu.hqu.monkey.mapfence.data.repo.IPointSet;
import cn.edu.hqu.monkey.mapfence.data.repo.PointSet;

/**
 * Created by monkey on 2018/5/24.
 */

public interface MapDataSource {

    /**
     * 开启定位图层
     * @param enable
     */
    void enLocation(boolean enable);


    void setLocationMode(MyLocationConfiguration.LocationMode mode);

    void setClickListen(BaiduMap.OnMapClickListener listen);

    void setDoubleClickListen(BaiduMap.OnMapDoubleClickListener listen);

    void setMarkerClickListener(BaiduMap.OnMarkerClickListener listener);

    void setMarkerDragListener(BaiduMap.OnMarkerDragListener listener);

    void drawMarker(LatLng latLng);

    void drawMarker(List<LatLng> latLngs);

    void drawLine(List<LatLng> latLngs);

    void drawPolygon(List<LatLng> latLngs);

    void clearAllMarker();

    void drawMarkLine(IPointSet pointSet);

    void setLocationListener(BDMap.OnLocationListener locationListener);

    LatLng getMyLocation();

    void focusedCurrentLocation();
}
