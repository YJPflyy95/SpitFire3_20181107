package org.astri.spitfire.ble.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.vise.baseble.ViseBle;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.utils.BleUtil;
import com.vise.log.ViseLog;
import com.vise.log.inner.LogcatTree;
import com.vise.xsnow.event.BusManager;
import com.vise.xsnow.permission.OnPermissionCallback;
import com.vise.xsnow.permission.PermissionManager;

import org.astri.spitfire.R;
import org.astri.spitfire.ble.activity.DeviceControlActivity;
import org.astri.spitfire.ble.activity.DeviceDetailActivity;
import org.astri.spitfire.ble.adapter.DeviceMainAdapter;
import org.astri.spitfire.ble.common.BluetoothDeviceManager;
import org.astri.spitfire.ble.common.ToastUtil;
import org.astri.spitfire.ble.event.ConnectEvent;
import org.astri.spitfire.ble.event.NotifyDataEvent;
import org.astri.spitfire.fragment.HomeFragment;
import org.astri.spitfire.util.LogUtil;

import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static org.litepal.LitePalBase.TAG;

/**
 * @Description: 主页，展示已连接设备列表
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2017/10/20 17:35
 */
public class BLEMainFragment extends Fragment {

    public static final String TAG = "BLEMainFragment";

    private TextView supportTv;
    private TextView statusTv;
    private ListView deviceLv;
    private TextView emptyTv;
    private TextView countTv;

    private DeviceMainAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        LogUtil.d(TAG, "onCreateView");

        ViseLog.getLogConfig().configAllowLog(true);//配置日志信息
        ViseLog.plant(new LogcatTree());//添加Logcat打印信息

        BluetoothDeviceManager.getInstance().init(getContext());
        BusManager.getBus().register(this);
        init(view);

        // 扫描设备fragment
        Button add = (Button) view.findViewById(R.id.Add_bt);
        add.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.ll_content,new DeviceScanFragment())
                        .commit();
            }
        });

        // 返回到home
        Button Back = (Button) view.findViewById(R.id.Back_bt);
        Back.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.ll_content,new HomeFragment())
                        .commit();
            }
        });
        return view;
    }
    private void init(View view) {
        supportTv = (TextView) view.findViewById(R.id.main_ble_support);
        statusTv = (TextView) view.findViewById(R.id.main_ble_status);
        deviceLv = (ListView) view.findViewById(android.R.id.list);
        emptyTv = (TextView) view.findViewById(android.R.id.empty);
        countTv = (TextView) view.findViewById(R.id.connected_device_count);

        adapter = new DeviceMainAdapter(getContext());
        deviceLv.setAdapter(adapter);

        deviceLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                LogUtil.d(TAG, "deviceLv: onItemClick");

                BluetoothLeDevice device = (BluetoothLeDevice) adapter.getItem(position);
                if (device == null) return;
                Intent intent = new Intent(getActivity(), DeviceControlActivity.class);
                intent.putExtra(DeviceDetailActivity.EXTRA_DEVICE, device);
                startActivity(intent);
            }
        });
    }

    public void showConnectedDevice(ConnectEvent event) {
        if (event != null) {
            updateConnectedDevice();
            if (event.isDisconnected()) {
                ToastUtil.showToast(getContext(), "Disconnect!");
            }
        }
    }
    public void showDeviceNotifyData(NotifyDataEvent event) {
        if (event != null && adapter != null) {
            adapter.setNotifyData(event.getBluetoothLeDevice(), event.getData());
        }
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
     * 菜单栏的显示
     *
     * @param menu 菜单
     * @return 返回是否拦截操作
     */

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.e(TAG, "onCreateOptionsMenu()");
        menu.clear();
        inflater.inflate(R.menu.about, menu);
    }

    /**
     * 点击菜单栏的处理
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about://关于
                displayAboutDialog();
                break;
        }
        return true;
    }

    /**
     * 打开或关闭蓝牙后的回调
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                statusTv.setText(getString(R.string.on));
                enableBluetooth();
            }
        } else if (resultCode == RESULT_CANCELED) {
            getActivity().finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 检查蓝牙权限
     */
    private void checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //校验是否已具有模糊定位权限
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                PermissionManager.instance().with(getActivity()).request(new OnPermissionCallback() {
                    @Override
                    public void onRequestAllow(String permissionName) {
                        enableBluetooth();
                    }

                    @Override
                    public void onRequestRefuse(String permissionName) {
                        getActivity().finish();
                    }

                    @Override
                    public void onRequestNoAsk(String permissionName) {
                        getActivity().finish();
                    }
                }, Manifest.permission.ACCESS_COARSE_LOCATION);
            } else {
                enableBluetooth();
            }
        } else {
            enableBluetooth();
        }
    }

    @SuppressLint("RestrictedApi")
    private void enableBluetooth() {
        if (!BleUtil.isBleEnable(getContext())) {
            BleUtil.enableBluetooth(getActivity(), 1);
        } else {
            boolean isSupport = BleUtil.isSupportBle(getContext());
            boolean isOpenBle = BleUtil.isBleEnable(getContext());
            if (isSupport) {
                supportTv.setText(getString(R.string.supported));
            } else {
                supportTv.setText(getString(R.string.not_supported));
            }
            if (isOpenBle) {
                statusTv.setText(getString(R.string.on));
            } else {
                statusTv.setText(getString(R.string.off));
            }
            getActivity().invalidateOptionsMenu();
            updateConnectedDevice();
        }
    }

    /**
     * 更新已经连接到的设备
     */
    private void updateConnectedDevice() {
        if (adapter != null && ViseBle.getInstance().getDeviceMirrorPool() != null) {
            List<BluetoothLeDevice> bluetoothLeDeviceList = ViseBle.getInstance().getDeviceMirrorPool().getDeviceList();
            if (bluetoothLeDeviceList != null && bluetoothLeDeviceList.size() > 0) {
                deviceLv.setVisibility(View.VISIBLE);
            } else {
                deviceLv.setVisibility(View.GONE);
            }
            adapter.setListAll(bluetoothLeDeviceList);
            updateItemCount(adapter.getCount());
        } else {
            deviceLv.setVisibility(View.GONE);
        }
    }

    /**
     * 更新已经连接的设备个数
     *
     * @param count
     */
    private void updateItemCount(final int count) {
        countTv.setText(getString(R.string.formatter_item_count, String.valueOf(count)));
    }

    /**
     * 显示项目信息
     */
    private void displayAboutDialog() {
    }
}
