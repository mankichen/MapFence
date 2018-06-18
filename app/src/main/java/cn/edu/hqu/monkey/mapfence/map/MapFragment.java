package cn.edu.hqu.monkey.mapfence.map;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import cn.edu.hqu.monkey.mapfence.R;
import cn.edu.hqu.monkey.mapfence.data.map.BDMap;
import terranovaproductions.newcomicreader.FloatingActionMenu;

import static android.content.ContentValues.TAG;
import static android.support.v4.util.Preconditions.checkNotNull;

/**
 * A placeholder fragment containing a simple view.
 */
public class MapFragment extends Fragment implements MapContract.View{

    private MapContract.Presenter presenter;

    private MapView mMapView;

    private ImageButton ibtn_current;
    private FloatingActionButton fbtn_edit;
    private FloatingActionButton fbtn_listen;
    AlertDialog alertDialog;

    public MapFragment() {
    }

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_main, container, false);

        mMapView = root.findViewById(R.id.mmap);
        ibtn_current = root.findViewById(R.id.img_btn_current);
        fbtn_edit = root.findViewById(R.id.fab_edit_circle);
        fbtn_listen = root.findViewById(R.id.fab_listen_circle);

        FloatingActionMenu menu = (FloatingActionMenu) getActivity().findViewById(R.id.fab_menu_circle);
        menu.setMultipleOfFB(3.2f);
        menu.setIsCircle(true);
        menu.setOnMenuItemClickListener(presenter.getMenuItemClickListener());
        ibtn_current.setOnClickListener(presenter.getOnClickListener());

        /* 移除放大缩小图标 */
        mMapView.showZoomControls(false);

        presenter.loadMap(mMapView.getMap());

        return root;
    }

    @Override
    public void showToast(String msg) {
        Toast.makeText(getActivity().getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSnackbar(String msg) {
        Snackbar.make(getActivity().findViewById(R.id.coordinatorLayout), msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public void modifyFABEditBG(int res) {
        fbtn_edit.setBackgroundResource(res);
    }

    @Override
    public void modifyFABListenBG(int res) {
        fbtn_listen.setBackgroundResource(res);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(getActivity(), "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            getActivity().finish();
                            return;
                        }
                    }
                    // requestLocation();
                } else {
                    Toast.makeText(getActivity(), "发生未知错误", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
                break;
            default:
        }
    }



    @Override
    public void showAlarmDialog(DialogInterface.OnClickListener listener){

        if (null == alertDialog) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("是否停止边界检查？");
            builder.setPositiveButton("确定", listener);
            builder.setNegativeButton("取消", null);
            alertDialog = builder.create();
        }

        if (!alertDialog.isShowing()){
            alertDialog.show();
        }
    }


    @Override
    public void setPresenter(@NonNull MapContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
        mMapView.onResume();
        mMapView.getMap().setMyLocationEnabled(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        mMapView.getMap().setMyLocationEnabled(false);
    }
}
