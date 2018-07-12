package org.astri.spitfire.ble.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
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
import com.vise.baseble.callback.scan.IScanCallback;
import com.vise.baseble.callback.scan.ScanCallback;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.model.BluetoothLeDeviceStore;
import com.vise.log.ViseLog;

import org.astri.spitfire.ConnectedDeviceActivity;
import org.astri.spitfire.GodActivity;
import org.astri.spitfire.R;
import org.astri.spitfire.ble.activity.DeviceDetailActivity;
import org.astri.spitfire.ble.activity.DeviceScanActivity;
import org.astri.spitfire.ble.activity.DeviceScanActivityStart;
import org.astri.spitfire.ble.adapter.DeviceAdapter;
import org.astri.spitfire.fragment.MeFragment;
import org.astri.spitfire.util.LogUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static android.hardware.camera2.params.RggbChannelVector.COUNT;
import static com.vise.utils.handler.HandlerUtil.runOnUiThread;
import static org.astri.spitfire.ble.Fragment.DeviceDetailFragment.EXTRA_DEVICE;

/**
 * @Description: 主页，展示已连接设备列表
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2017/10/20 17:35
 */
public class DeviceScanFragment extends Fragment {

    private static final String TAG = "DeviceScanFragment";

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 100;

    private ListView deviceLv;
    private TextView scanCountTv;
    private TextView countDown;

    //设备扫描结果展示适配器
    private DeviceAdapter adapter;

    private BluetoothLeDeviceStore bluetoothLeDeviceStore = new BluetoothLeDeviceStore();

    /**
     * 扫描回调
     */
    private ScanCallback periodScanCallback = new ScanCallback(new IScanCallback() {
        @Override
        public void onDeviceFound(final BluetoothLeDevice bluetoothLeDevice) {
            ViseLog.i("Founded Scan Device:" + bluetoothLeDevice);
            bluetoothLeDeviceStore.addDevice(bluetoothLeDevice);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (adapter != null && bluetoothLeDeviceStore != null) {
                        adapter.setListAll(bluetoothLeDeviceStore.getDeviceList());
                        updateItemCount(adapter.getCount());
                    }
                }
            });
        }

        @Override
        public void onScanFinish(BluetoothLeDeviceStore bluetoothLeDeviceStore) {
            ViseLog.i("scan finish " + bluetoothLeDeviceStore);
        }

        @Override
        public void onScanTimeout() {
            ViseLog.i("scan timeout");
        }
    });

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_scan, container, false);

//        // TODO: ~!!!!!
//        Button back = (Button) view.findViewById(R.id.Back_bt);
//        back.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View v)
//            {
//                Intent intent = new Intent(getActivity(),ConnectedDeviceActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        // TODO: stop ???
//        Button stop = (Button) view.findViewById(R.id.Stop_bt);
//        stop.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View v) {
//
//                FragmentManager fm = getActivity().getSupportFragmentManager();
//                fm.beginTransaction()
//                        .replace(R.id.ll_content,new DeviceScanStartFragment())
//                        .commit();
//            }
//        });
        initView(view);
        init(view);

        Button back = (Button) view.findViewById(R.id.Back_bt);
        back.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.ll_content,new DeviceControlFragment())
                        .commit();
            }
        });
        Button stop = (Button) view.findViewById(R.id.Stop_bt);
        stop.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.ll_content,new DeviceScanStartFragment())
                        .commit();
            }
        });
        return view;
    }
    //sehedule的第而个参数是第一次启动延时的时间，第三个是每隔多长时间执行一次。单位都是ms。
//因此这里是每一秒发送一次消息给handler更新UI。
    //然后三秒后时间到了，在timer的第二个sehedule中进行跳转到另外一个界面
    private void initView(View view) {
        countDown =  (TextView) view.findViewById(R.id.count_down);
        final Timer timer = new Timer();
        final long end = System.currentTimeMillis() + 1000*20;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(COUNT);
            }
        }, 0, 1000);
//        这里的schedule的第二个参数意义是到了这个时间尽快运行run里面的方法
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
//                Intent i = new Intent(DeviceScanActivity.this, ReadinessDetail.class);
//                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(i);
//                finish();
                timer.cancel();
            }
        }, new Date(end));

    }

    private Handler handler = new Handler(){
        int num = 20;
        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {
                case COUNT:
                    countDown.setText(String.valueOf(num));
                    num--;
                    break;

                default:
                    break;
            }
        };
    };

    private void init(View view) {
        deviceLv = (ListView) view.findViewById(android.R.id.list);
//        scanCountTv = (TextView) view.findViewById(R.id.scan_device_count);

        adapter = new DeviceAdapter(getContext());
        deviceLv.setAdapter(adapter);

        LogUtil.d(TAG, "DeviceScanFragment: 点击某个扫描到的设备进入设备详细信息界面");

        deviceLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //点击某个扫描到的设备进入设备详细信息界面
                BluetoothLeDevice device = (BluetoothLeDevice) adapter.getItem(position);
                if (device == null) return;
//                Intent intent = new Intent(getActivity(), DeviceDetailActivity.class);
//                intent.putExtra(DeviceDetailActivity.EXTRA_DEVICE, device);
//                startActivity(intent);

                // 填充 fragment
                Bundle bundle = new Bundle();
                // 设备
                GodActivity.setDevice(device);

                // 全局设备
                bundle.putParcelable(EXTRA_DEVICE, device);

                DeviceDetailFragment deviceDetailFragment = new DeviceDetailFragment();
                // 设定设备
                deviceDetailFragment.setArguments(bundle);

                replaceFragment(deviceDetailFragment);

            }
        });
    }

    /**
     * 填充Fragment
     * @param fragment
     */
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.ll_content, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onResume() {
        super.onResume();
        startScan();
        getActivity().invalidateOptionsMenu();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onPause() {
        super.onPause();
        stopScan();
        getActivity().invalidateOptionsMenu();
        bluetoothLeDeviceStore.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 菜单栏的显示
     *
     * @param menu 菜单
     * @return 返回是否拦截操作
     */

    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.scan, menu);
        if (periodScanCallback != null && !periodScanCallback.isScanning()) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_progress_indeterminate);
        }
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
//            case R.id.menu_scan://开始扫描
//                startScan();
//                break;
            case R.id.menu_stop://停止扫描
                stopScan();
                break;
        }
        return true;
    }

    /**
     * 开始扫描
     */
    @SuppressLint("RestrictedApi")
    private void startScan() {
        updateItemCount(0);
        if (adapter != null) {
            adapter.setListAll(new ArrayList<BluetoothLeDevice>());
        }
        ViseBle.getInstance().startScan(periodScanCallback);
        getActivity().invalidateOptionsMenu();
    }

    /**
     * 停止扫描
     */
    @SuppressLint("RestrictedApi")
    private void stopScan() {
        ViseBle.getInstance().stopScan(periodScanCallback);
        getActivity().invalidateOptionsMenu();
    }

    /**
     * 更新扫描到的设备个数
     *
     * @param count
     */
    private void updateItemCount(final int count) {
//        scanCountTv.setText(getString(R.string.formatter_item_count, String.valueOf(count)));
    }

}
