package org.astri.spitfire.ble.Fragment;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.vise.baseble.ViseBle;
import com.vise.baseble.callback.scan.IScanCallback;
import com.vise.baseble.callback.scan.ScanCallback;
import com.vise.baseble.common.BluetoothServiceType;
import com.vise.baseble.common.ConnectState;
import com.vise.baseble.core.DeviceMirror;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.model.BluetoothLeDeviceStore;
import com.vise.baseble.model.adrecord.AdRecord;
import com.vise.baseble.model.resolver.GattAttributeResolver;
import com.vise.baseble.utils.AdRecordUtil;
import com.vise.baseble.utils.HexUtil;
import com.vise.log.ViseLog;
import com.vise.xsnow.cache.SpCache;

import org.astri.spitfire.ConnectedDeviceActivity;
import org.astri.spitfire.R;
import org.astri.spitfire.ble.activity.DeviceControlActivity;
import org.astri.spitfire.ble.activity.DeviceDetailActivity;
import org.astri.spitfire.ble.adapter.DeviceAdapter;
import org.astri.spitfire.ble.adapter.MergeAdapter;
import org.astri.spitfire.ble.common.BluetoothDeviceManager;
import org.astri.spitfire.fragment.NewActivityFragment;
import org.astri.spitfire.util.LogUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import static android.hardware.camera2.params.RggbChannelVector.COUNT;
import static com.vise.utils.handler.HandlerUtil.runOnUiThread;

/**
 * @Description: 主页，展示已连接设备列表
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date:
 */
public class DeviceDetailFragment extends Fragment {

    public static final String TAG = "DeviceDetailFragment";

    public static final String EXTRA_DEVICE = "extra_device";
    private ListView mList;
    private View mEmpty;
    private BluetoothLeDevice mDevice;

    private static final String LIST_NAME = "NAME";
    private static final String LIST_UUID = "UUID";
    public static final String WRITE_CHARACTERISTI_UUID_KEY = "write_uuid_key";
    public static final String NOTIFY_CHARACTERISTIC_UUID_KEY = "notify_uuid_key";
    public static final String WRITE_DATA_KEY = "write_data_key";

    private SimpleExpandableListAdapter simpleExpandableListAdapter;
    private TextView mConnectionState;
    private TextView mGattUUID;
    private TextView mGattUUIDDesc;
    private TextView mDataAsString;
    private TextView mDataAsArray;
    private EditText mInput;
    private EditText mOutput;
    private Button bConnectionState;

    private SpCache mSpCache;
    //设备信息
//    private BluetoothLeDevice mDevice;
    //输出数据展示
    private StringBuilder mOutputInfo = new StringBuilder();
    private List<BluetoothGattService> mGattServices = new ArrayList<>();
    //设备特征值集合
    private List<List<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();


    /**
     * 创建视图
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_detail, container, false);
        init(view);
        Button connect = (Button) view.findViewById(R.id.Connect_bt);
//        add.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getActivity(),DeviceControlActivity.class);
//                startActivity(intent);
//            }
//        });
        connect.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                LogUtil.d(TAG, "点击 Connect 按钮，连接设备。");


                if (!BluetoothDeviceManager.getInstance().isConnected(mDevice)) {
                    LogUtil.d(TAG, "设备未连接。");
                    BluetoothDeviceManager.getInstance().connect(mDevice);
                    getActivity().invalidateOptionsMenu();
                }else{
                    LogUtil.d(TAG, "设备已连接。");
                }

                LogUtil.d(TAG, "准备跳转Fragment");
//                FragmentManager fm = getActivity().getSupportFragmentManager();
//                fm.beginTransaction()
//                        .replace(R.id.ll_content,new DeviceControlFragment())
//                        .commit();
            }
        });
        Button back = (Button) view.findViewById(R.id.Back_bt);
        back.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.ll_content,new DeviceScanFragment())
                        .commit();
            }
        });
//        onCreateOptionsMenu(view);
        return view;
    }

    /**
     * 追加广播包信息
     *
     * @param adapter
     * @param title
     * @param record
     */
    private void appendAdRecordView(final MergeAdapter adapter, final String title, final AdRecord record) {
        final LinearLayout lt = (LinearLayout) getLayoutInflater().inflate(R.layout.list_item_view_adrecord, null);
        final TextView tvString = (TextView) lt.findViewById(R.id.data_as_string);
        final TextView tvArray = (TextView) lt.findViewById(R.id.data_as_array);
        final TextView tvTitle = (TextView) lt.findViewById(R.id.title);

        tvTitle.setText(title);
        tvString.setText("'" + AdRecordUtil.getRecordDataAsString(record) + "'");
        tvArray.setText("'" + HexUtil.encodeHexStr(record.getData()) + "'");

        adapter.addView(lt);
    }

    /**
     * 追加设备基础信息
     *
     * @param adapter
     * @param device
     */
    private void appendDeviceInfo(final MergeAdapter adapter, final BluetoothLeDevice device) {
        final LinearLayout lt = (LinearLayout) getLayoutInflater().inflate(R.layout.list_item_view_device_info, null);
        final TextView tvName = (TextView) lt.findViewById(R.id.deviceName);
        final TextView tvAddress = (TextView) lt.findViewById(R.id.deviceAddress);

        tvName.setText(device.getName());
        tvAddress.setText(device.getAddress());

        final String supportedServices;
        if (device.getBluetoothDeviceKnownSupportedServices().isEmpty()) {
            supportedServices = getString(R.string.no_known_services);
        } else {
            final StringBuilder sb = new StringBuilder();

            for (final BluetoothServiceType service : device.getBluetoothDeviceKnownSupportedServices()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }

                sb.append(service);
            }
            supportedServices = sb.toString();
        }

//        tvServices.setText(supportedServices);

        adapter.addView(lt);
    }

    /**
     * 追加信息头
     *
     * @param adapter
     * @param title
     */
    private void appendHeader(final MergeAdapter adapter, final String title) {
        final LinearLayout lt = (LinearLayout) getLayoutInflater().inflate(R.layout.list_item_view_header, null);
        final TextView tvTitle = (TextView) lt.findViewById(R.id.title);
        tvTitle.setText(title);

        adapter.addView(lt);
    }

    /**
     * 追加设备信号信息
     *
     * @param adapter
     * @param device
     */
    private void appendRssiInfo(final MergeAdapter adapter, final BluetoothLeDevice device) {
        final LinearLayout lt = (LinearLayout) getLayoutInflater().inflate(R.layout.list_item_view_rssi_info, null);
        final TextView tvFirstTimestamp = (TextView) lt.findViewById(R.id.firstTimestamp);
        final TextView tvFirstRssi = (TextView) lt.findViewById(R.id.firstRssi);
        final TextView tvLastTimestamp = (TextView) lt.findViewById(R.id.lastTimestamp);
        final TextView tvLastRssi = (TextView) lt.findViewById(R.id.lastRssi);
        final TextView tvRunningAverageRssi = (TextView) lt.findViewById(R.id.runningAverageRssi);

        tvFirstTimestamp.setText(formatTime(device.getFirstTimestamp()));
        tvFirstRssi.setText(formatRssi(device.getFirstRssi()));
        tvLastTimestamp.setText(formatTime(device.getTimestamp()));
        tvLastRssi.setText(formatRssi(device.getRssi()));
        tvRunningAverageRssi.setText(formatRssi(device.getRunningAverageRssi()));

        adapter.addView(lt);
    }

    /**
     * 追加简单信息
     *
     * @param adapter
     * @param data
     */
    private void appendSimpleText(final MergeAdapter adapter, final byte[] data) {
        appendSimpleText(adapter, HexUtil.encodeHexStr(data));
    }

    private void appendSimpleText(final MergeAdapter adapter, final String data) {
        final LinearLayout lt = (LinearLayout) getLayoutInflater().inflate(R.layout.list_item_view_textview, null);
        final TextView tvData = (TextView) lt.findViewById(R.id.data);

        tvData.setText(data);

        adapter.addView(lt);
    }


    private String formatRssi(final double rssi) {
        return getString(R.string.formatter_db, String.valueOf(rssi));
    }

    private String formatRssi(final int rssi) {
        return getString(R.string.formatter_db, String.valueOf(rssi));
    }

    private void init(View view) {
        mEmpty = view.findViewById(android.R.id.empty);
        mList = (ListView) view.findViewById(android.R.id.list);
        mConnectionState = (TextView) view.findViewById(R.id.connection_state);
        mList.setEmptyView(mEmpty);
        mDevice = getArguments().getParcelable(EXTRA_DEVICE);
        pupulateDetails(mDevice);
    }
//jiashangbu
//    public boolean onCreateOptionsMenu(View view) {
////        menu.clear();
////        MenuInflater inflater = getActivity().getMenuInflater();
////        inflater.inflate(R.menu.connect, menu);
//        Button connect = (Button) view.findViewById(R.id.connect);
//        if (BluetoothDeviceManager.getInstance().isConnected(mDevice)) {
//            mConnectionState.setText("true");
//            DeviceMirror deviceMirror = ViseBle.getInstance().getDeviceMirror(mDevice);
//            if (deviceMirror != null) {
//                simpleExpandableListAdapter = displayGattServices(deviceMirror.getBluetoothGatt().getServices());
//            }
////            showDefaultInfo();
//        } else {
//            mConnectionState.setText("false");
////            clearUI();
//        }
//        if (ViseBle.getInstance().getConnectState(mDevice) == ConnectState.CONNECT_PROCESS) {
//
//        } else {
//
//        }
//        return true;
//    }
//
//    /**
//     * 根据GATT服务显示该服务下的所有特征值
//     *
//     * @param gattServices GATT服务
//     * @return
//     */
//    private SimpleExpandableListAdapter displayGattServices(final List<BluetoothGattService> gattServices) {
//        if (gattServices == null) return null;
//        String uuid;
//        final String unknownServiceString = getResources().getString(R.string.unknown_service);
//        final String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
//        final List<Map<String, String>> gattServiceData = new ArrayList<>();
//        final List<List<Map<String, String>>> gattCharacteristicData = new ArrayList<>();
//
//        mGattServices = new ArrayList<>();
//        mGattCharacteristics = new ArrayList<>();
//
//        // Loops through available GATT Services.
//        for (final BluetoothGattService gattService : gattServices) {
//            final Map<String, String> currentServiceData = new HashMap<>();
//            uuid = gattService.getUuid().toString();
//            currentServiceData.put(LIST_NAME, GattAttributeResolver.getAttributeName(uuid, unknownServiceString));
//            currentServiceData.put(LIST_UUID, uuid);
//            gattServiceData.add(currentServiceData);
//
//            final List<Map<String, String>> gattCharacteristicGroupData = new ArrayList<>();
//            final List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
//            final List<BluetoothGattCharacteristic> charas = new ArrayList<>();
//
//            // Loops through available Characteristics.
//            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
//                charas.add(gattCharacteristic);
//                final Map<String, String> currentCharaData = new HashMap<>();
//                uuid = gattCharacteristic.getUuid().toString();
//                currentCharaData.put(LIST_NAME, GattAttributeResolver.getAttributeName(uuid, unknownCharaString));
//                currentCharaData.put(LIST_UUID, uuid);
//                gattCharacteristicGroupData.add(currentCharaData);
//            }
//
//            mGattServices.add(gattService);
//            mGattCharacteristics.add(charas);
//            gattCharacteristicData.add(gattCharacteristicGroupData);
//        }
//
//        final SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(getContext(), gattServiceData, android.R.layout
//                .simple_expandable_list_item_2, new String[]{LIST_NAME, LIST_UUID}, new int[]{android.R.id.text1, android.R.id.text2},
//                gattCharacteristicData, android.R.layout.simple_expandable_list_item_2, new String[]{LIST_NAME, LIST_UUID}, new
//                int[]{android.R.id.text1, android.R.id.text2});
//        return gattServiceAdapter;
//    }
//    private void showReadInfo(String uuid, byte[] dataArr) {
//        mGattUUID.setText(uuid != null ? uuid : getString(R.string.no_data));
//        mGattUUIDDesc.setText(GattAttributeResolver.getAttributeName(uuid, getString(R.string.unknown)));
//        mDataAsArray.setText(HexUtil.encodeHexStr(dataArr));
//        mDataAsString.setText(new String(dataArr));
//    }
////////////jiadibu
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.scan, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                if (mDevice == null) return false;
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.ll_content,new DeviceControlFragment())
                        .commit();
                break;
        }
        return true;
    }

    /**
     * 展示设备详细信息
     *
     * @param device 设备信息
     */
    private void pupulateDetails(final BluetoothLeDevice device) {
        final MergeAdapter adapter = new MergeAdapter();
        if (device == null) {
            appendHeader(adapter, getString(R.string.header_device_info));
            appendSimpleText(adapter, getString(R.string.invalid_device_data));
        } else {
            appendHeader(adapter, getString(R.string.header_device_info));
            appendDeviceInfo(adapter, device);
        }
        mList.setAdapter(adapter);
    }

    /**
     * 格式化时间
     *
     * @param time
     * @return
     */
    private static String formatTime(final long time) {
        String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS zzz";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ISO_FORMAT, Locale.CHINA);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(new Date(time));
    }
}
