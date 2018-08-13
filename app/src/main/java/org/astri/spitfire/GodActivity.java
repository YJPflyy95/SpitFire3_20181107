package org.astri.spitfire;

import com.vise.baseble.ViseBle;
import com.vise.baseble.model.BluetoothLeDevice;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.vise.baseble.utils.BleUtil;
import com.vise.log.ViseLog;
import com.vise.log.inner.LogcatTree;
import com.vise.xsnow.event.BusManager;
import com.vise.xsnow.permission.OnPermissionCallback;
import com.vise.xsnow.permission.PermissionManager;

import org.astri.spitfire.ble.common.BluetoothDeviceManager;
import org.astri.spitfire.entities.History;
import org.astri.spitfire.fragment.ActivitiesFragment;
import org.astri.spitfire.fragment.HistoryFragment;
import org.astri.spitfire.fragment.HomeFragment;
import org.astri.spitfire.fragment.LiveFragment;
import org.astri.spitfire.fragment.MeFragment;
import org.astri.spitfire.fragment.SettingsFragment;
import org.astri.spitfire.util.DataUtil;
import org.astri.spitfire.util.LogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 *     author : HuGuoDong
 *     e-mail : guodong_hu@126.com
 *     time   : 2018/02/03
 *     desc   :
 *     modified by :
 *     e-mail : xx_xxx@xx
 *     time   :
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class GodActivity extends AppCompatActivity implements BottomNavigationBar.OnTabSelectedListener {



    private static final String TAG = "GodActivity";

    private Fragment homeFragment;
    private Fragment liveFragment;
    private Fragment historyFragment;
    private Fragment activitiesFragment;
    private Fragment meFragment;

    // settings -> moments
    private Fragment momentsFragment;

    private BottomNavigationBar bottomNavigationBar;

    // 保存 设备，服务，属性
    private static BluetoothLeDevice mDevice;
    public static Map<String, BluetoothGattService> SERVICE_MAP = new HashMap<>();
    public static Map<String, BluetoothGattCharacteristic> CHARA_MAP = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_god);
        getSupportActionBar().hide();

        bottomNavigationBar = findViewById(R.id.bottom_navigation_bar);

        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.homefilled, "Home").setInactiveIconResource(R.drawable.home).setActiveColorResource(R.color.appmain))
                .addItem(new BottomNavigationItem(R.drawable.livefilled, "Live").setInactiveIconResource(R.drawable.live).setActiveColorResource(R.color.appmain))
                .addItem(new BottomNavigationItem(R.drawable.historyfilled, "History").setInactiveIconResource(R.drawable.history).setActiveColorResource(R.color.appmain))
//                .addItem(new BottomNavigationItem(R.drawable.exercisefilled, "Activities").setInactiveIconResource(R.drawable.exercise).setActiveColorResource(R.color.appmain))
                .addItem(new BottomNavigationItem(R.drawable.momentsfilled, "Moments").setInactiveIconResource(R.drawable.moments).setActiveColorResource(R.color.appmain))
                .addItem(new BottomNavigationItem(R.drawable.myfilled, "Me").setInactiveIconResource(R.drawable.my).setActiveColorResource(R.color.appmain))
                .initialise();

        bottomNavigationBar.setTabSelectedListener(this);

//        homeFragment = new HomeFragment();
//        switchContent(homeFragment); // 默认显示home
        setDefaultFragment();
        //guodongjia
//        DataUtil.genUsers();
    }

    private void setDefaultFragment() {
        homeFragment = new HomeFragment();
        replaceFragment(homeFragment);
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.ll_content, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onTabSelected(int position) {
        switch (position) {
            case 0:
                if (homeFragment == null) {
                    homeFragment = new HomeFragment();
                }
                replaceFragment(homeFragment);
//                switchContent(homeFragment);
                break;
            case 1:
                if (liveFragment == null) {
                    liveFragment = new LiveFragment();
                }
                replaceFragment(liveFragment);
//                switchContent(liveFragment);
                break;
            case 2:
                if (historyFragment == null) {
                    historyFragment = new HistoryFragment();
                }
                replaceFragment(historyFragment);
//                switchContent(historyFragment);
                break;
            case 3:
//                if (activitiesFragment == null) {
//                    activitiesFragment = new ActivitiesFragment();
//                }
//                replaceFragment(activitiesFragment);


                if(momentsFragment == null){
                    momentsFragment = new SettingsFragment();
                }
                replaceFragment(momentsFragment);
//                switchContent(activitiesFragment);
                break;
            case 4:
                if (meFragment == null) {
                    meFragment = new MeFragment();
                }
                replaceFragment(meFragment);
//                switchContent(meFragment);
                break;
            default:
                if (homeFragment == null) {
                    homeFragment = new HomeFragment();
                }
                replaceFragment( homeFragment);
//                switchContent( homeFragment);
                break;
        }
    }


    public void switchContent(Fragment to) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideFragments(transaction);
        if (!to.isAdded()) {	// 先判断是否被add过
            transaction.add(R.id.ll_content, to).show(to).commit(); // 隐藏当前的fragment，add下一个到Activity中
        } else {
            transaction.show(to).commit(); // 隐藏当前的fragment，显示下一个
        }
    }

    @Override
    public void onTabUnselected(int position) {

    }

    @Override
    public void onTabReselected(int position) {

    }

    /**
     * 隐藏所有fragment防止显示多个
     */

    private void hideFragments(FragmentTransaction transaction) {

        LogUtil.d(TAG, "hideFragments");

        if (homeFragment != null) {
            transaction.hide(homeFragment);
        }
        if (liveFragment != null) {
            transaction.hide(liveFragment);
        }
        if (historyFragment != null) {
            transaction.hide(historyFragment);
        }
        if (activitiesFragment != null) {
            transaction.hide(activitiesFragment);
        }
        if (meFragment != null) {
            transaction.hide(meFragment);
        }
    }

    public static void setDevice(BluetoothLeDevice device){
        if(device!=null){
            mDevice = device;
        }
    }

    public static BluetoothLeDevice getDevice(){
        return mDevice;
    }


    @Override
    public void onResume() {
        super.onResume();
        checkBluetoothPermission();
    }

    @Override
    public void onDestroy() {
        ViseBle.getInstance().clear();
        BusManager.getBus().unregister(this);
        super.onDestroy();
    }

    /**
     * 检查蓝牙权限
     */
    private void checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //校验是否已具有模糊定位权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                PermissionManager.instance().with(this).request(new OnPermissionCallback() {
                    @Override
                    public void onRequestAllow(String permissionName) {
                        enableBluetooth();
                    }

                    @Override
                    public void onRequestRefuse(String permissionName) {
                        finish();
                    }

                    @Override
                    public void onRequestNoAsk(String permissionName) {
                        finish();
                    }
                }, Manifest.permission.ACCESS_COARSE_LOCATION);
            } else {
                enableBluetooth();
            }
        } else {
            enableBluetooth();
        }
    }

    private void enableBluetooth() {
        if (!BleUtil.isBleEnable(this)) {
            BleUtil.enableBluetooth(this, 1);
        }
    }

}
