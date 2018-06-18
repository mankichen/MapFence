package cn.edu.hqu.monkey.mapfence.data.repo;

import com.baidu.mapapi.model.LatLng;

import java.util.List;

/**
 * Created by monkey on 2018/6/13.
 */

public interface IPointSet {

    void savePoint(LatLng point);

    void deletePoint(LatLng point);

    int getPointCnt();

    void clearAllPoint();

    boolean isEmpty();

    List<LatLng> getPointList();

    void flgMovePoint(LatLng latLng);

    void movePoint(LatLng latLng);
}
