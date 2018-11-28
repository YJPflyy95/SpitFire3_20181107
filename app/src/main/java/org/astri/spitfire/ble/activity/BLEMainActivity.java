package org.astri.spitfire.ble.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.vise.baseble.ViseBle;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.utils.BleUtil;
import com.vise.log.ViseLog;
import com.vise.log.inner.LogcatTree;
import com.vise.xsnow.event.BusManager;
import com.vise.xsnow.event.Subscribe;
import com.vise.xsnow.permission.OnPermissionCallback;
import com.vise.xsnow.permission.PermissionManager;

import org.astri.spitfire.Activities;
import org.astri.spitfire.HistoryActivity;
import org.astri.spitfire.HomeActivity;
import org.astri.spitfire.MeActivity;
import org.astri.spitfire.R;
import org.astri.spitfire.ble.adapter.DeviceMainAdapter;
import org.astri.spitfire.ble.common.BluetoothDeviceManager;
import org.astri.spitfire.ble.common.ToastUtil;
import org.astri.spitfire.ble.event.ConnectEvent;
import org.astri.spitfire.ble.event.NotifyDataEvent;

import java.util.List;

/**
 * @Description: 主页，展示已连接设备列表
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2017/10/20 17:35
 */
public class BLEMainActivity extends AppCompatActivity {

    private TextView supportTv;
    private TextView statusTv;
    private ListView deviceLv;
    private TextView emptyTv;
    private TextView countTv;

    private DeviceMainAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViseLog.getLogConfig().configAllowLog(true);//配置日志信息
        ViseLog.plant(new LogcatTree());//添加Logcat打印信息
        BluetoothDeviceManager.getInstance().init(this);
        BusManager.getBus().register(this);
        init();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        ImageView Home_iv = (ImageView) findViewById(R.id.Homeiv);
        Home_iv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(BLEMainActivity.this,HomeActivity.class);
                startActivity(intent);
            }
        });
        TextView Home_tv = (TextView) findViewById(R.id.Hometv);
        Home_tv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(BLEMainActivity.this,HomeActivity.class);
                startActivity(intent);
            }
        });
        ImageView Activities_iv = (ImageView) findViewById(R.id.Activitiesiv);
        Activities_iv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(BLEMainActivity.this,Activities.class);
                startActivity(intent);
            }
        });
        TextView Activities_tv = (TextView) findViewById(R.id.Activitiestv);
        Activities_tv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(BLEMainActivity.this,Activities.class);
                startActivity(intent);
            }
        });
        ImageView historyBt = findViewById(R.id.Historyiv);
        historyBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BLEMainActivity.this,HistoryActivity.class);
                startActivity(intent);
            }});
        TextView History_tv = (TextView) findViewById(R.id.Historytv);
        History_tv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(BLEMainActivity.this,HistoryActivity.class);
                startActivity(intent);
            }
        });
    }

    private void init() {
        supportTv = (TextView) findViewById(R.id.main_ble_support);
        statusTv = (TextView) findViewById(R.id.main_ble_status);
        deviceLv = (ListView) findViewById(android.R.id.list);
        emptyTv = (TextView) findViewById(android.R.id.empty);
        countTv = (TextView) findViewById(R.id.connected_device_count);

        Button add = (Button) findViewById(R.id.Add_bt);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BLEMainActivity.this, DeviceScanActivity.class);
                startActivity(intent);
            }
        });

        adapter = new DeviceMainAdapter(this);
        deviceLv.setAdapter(adapter);

        deviceLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                BluetoothLeDevice device = (BluetoothLeDevice) adapter.getItem(position);
                if (device == null) return;
                Intent intent = new Intent(BLEMainActivity.this, DeviceControlActivity.class);
                intent.putExtra(DeviceDetailActivity.EXTRA_DEVICE, device);
                startActivity(intent);
            }
        });
    }

    @Subscribe
    public void showConnectedDevice(ConnectEvent event) {
        if (event != null) {
            updateConnectedDevice();
            if (event.isDisconnected()) {
                ToastUtil.showToast(BLEMainActivity.this, "Disconnect!");
            }
        }
    }

    @Subscribe
    public void showDeviceNotifyData(NotifyDataEvent event) {
        if (event != null && adapter != null) {
            adapter.setNotifyData(event.getBluetoothLeDevice(), event.getData());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBluetoothPermission();
    }

    @Override
    protected void onDestroy() {
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
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.about, menu);
        return true;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                statusTv.setText(getString(R.string.on));
                enableBluetooth();
            }
        } else if (resultCode == RESULT_CANCELED) {
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
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

    @SuppressLint("RestrictedApi")
    private void enableBluetooth() {
        if (!BleUtil.isBleEnable(this)) {
            BleUtil.enableBluetooth(this, 1);
        } else {
            boolean isSupport = BleUtil.isSupportBle(this);
            boolean isOpenBle = BleUtil.isBleEnable(this);
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
            invalidateOptionsMenu();
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
    private void updateItemCount(final int count)
    {
        countTv.setText(getString(R.string.formatter_item_count, String.valueOf(count)));
    }

    /**
     * 显示项目信息
     */
    private void displayAboutDialog()
    {

    }

}
