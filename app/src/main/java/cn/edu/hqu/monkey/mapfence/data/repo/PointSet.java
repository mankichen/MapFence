package cn.edu.hqu.monkey.mapfence.data.repo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.ArraySet;
import android.util.Log;

import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import cn.edu.hqu.monkey.mapfence.data.map.BDMap;

import static android.support.constraint.Constraints.TAG;

/**
 * Created by monkey on 2018/6/13.
 */

public class PointSet implements IPointSet {
    private List<LatLng> pointList = new ArrayList<>();

    private int index;

    private static PointSet INSTANCE;
    private PointSet(){}

    public static PointSet getInstance() {
        synchronized (PointSet.class) {

            if (null == INSTANCE) {
                INSTANCE = new PointSet();
            }
        }
        return INSTANCE;
    }

    @Override
    public void savePoint(LatLng point) {
        Log.d(TAG, "savePoint: "+point.toString());
        if (!pointList.contains(point))
            pointList.add(point);
    }

    @Override
    public void deletePoint(LatLng point) {
        Log.d(TAG, "deletePoint: "+point.toString());
        pointList.remove(point);
    }

    @Override
    public int getPointCnt() {
        Log.d(TAG, "getPointCnt: "+pointList.size());
        return pointList.size();
    }

    @Override
    public void clearAllPoint() {
        Log.d(TAG, "clearAllPoint");
        pointList.clear();
    }

    @Override
    public boolean isEmpty() {
        Log.d(TAG, "isEmpty: judge");
        return pointList.isEmpty();
    }

    @Override
    public List<LatLng> getPointList() {
        return pointList;
    }

    @Override
    public void flgMovePoint(LatLng latLng) {
        double distance;
        index = 0;
        distance = DistanceUtil.getDistance(latLng, pointList.get(0));
        for (int i = 1; i < pointList.size(); i++){
            if (distance > DistanceUtil.getDistance(latLng, pointList.get(i))){
                index = i;
                distance = DistanceUtil.getDistance(latLng, pointList.get(i));
            }
        }
        Log.d(TAG, "flgMovePoint: find "+index+"of"+pointList.size());
    }

    @Override
    public void movePoint(LatLng latLng) {
        Log.d(TAG, "movePoint: moving "+index);
        pointList.remove(index);
        pointList.add(index, latLng);
    }
}
