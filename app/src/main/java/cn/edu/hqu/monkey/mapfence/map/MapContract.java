package cn.edu.hqu.monkey.mapfence.map;

import android.content.DialogInterface;
import android.view.View;

import com.baidu.mapapi.map.BaiduMap;

import cn.edu.hqu.monkey.mapfence.BasePresenter;
import cn.edu.hqu.monkey.mapfence.BaseView;
import terranovaproductions.newcomicreader.FloatingActionMenu;

/**
 * Created by monkey on 2018/5/25.
 */

public interface MapContract {

    interface View extends BaseView<Presenter> {
        void showToast(String msg);
        void showSnackbar(String msg);

        void modifyFABEditBG(int res);
        void modifyFABListenBG(int res);

        void showAlarmDialog(DialogInterface.OnClickListener listener);
    }

    interface Presenter extends BasePresenter{

        void loadMap(BaiduMap baiduMap);

        void requestLocation();

        FloatingActionMenu.OnMenuItemClickListener getMenuItemClickListener();

        android.view.View.OnClickListener getOnClickListener();


    }
}
