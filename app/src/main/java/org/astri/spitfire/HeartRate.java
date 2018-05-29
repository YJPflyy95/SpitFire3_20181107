package org.astri.spitfire;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.astri.spitfire.ble.activity.DeviceControlActivity;
import org.astri.spitfire.ble.common.ToastUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;
import static android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;

public class HeartRate extends AppCompatActivity {

    private final static String TAG = HeartRate.class.getSimpleName();
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private Handler mHandler = new Handler();

    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";



    private int mConnectionState = STATE_DISCONNECTED;//jia

    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";

    public BluetoothGatt mGgatt;

    private BluetoothGattCharacteristic mHearRateCharac;


    /**
     * 寻找服务
     */

    private LeGattCallback mGattCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        /**
         * 判断当前设备是否支持ble
         */
        if (!getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            System.out.println("不支持BLE设备");
        }


        //得到蓝牙适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT); // 连接
        }


        adapter = new LeDeviceListAdapter();
        Button start = (Button) findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                scanLeDevice(true);
                lv.setAdapter(adapter);
                adapter.clear();
            }

        });

        mGattCallback = new LeGattCallback();

        lv = (ListView) findViewById(R.id.lv);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //把搜索到的设备信息放到listview中，然后连接设备
                BluetoothDevice device = adapter.getDevice(position);
                mGgatt = device.connectGatt(getApplicationContext(), false,
                        mGattCallback); // 回调

            }
        });

    }

    final static private UUID mHeartRateServiceUuid = BleUUIDs.Service.HEART_RATE;
    final static private UUID mHeartRateCharacteristicUuid = BleUUIDs.Characteristic.HEART_RATE_MEASUREMENT;


    private class LeGattCallback extends BluetoothGattCallback{

        private BluetoothGattCharacteristic mCharacteristic;

        /**
         * 当连接状态发生改变的时候回调的方法
         */
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {

            String intentAction;//jia
            //判断蓝牙连接状态
            if (newState == STATE_CONNECTED) {
                Log.d(TAG,"设备已连接");

                intentAction = ACTION_GATT_CONNECTED;//jia
                mConnectionState = STATE_CONNECTED;//jia
                broadcastUpdate(intentAction);//jia
                //寻找设备中的服务
                gatt.discoverServices();
                mGgatt=gatt;
            } else if (newState == STATE_DISCONNECTED) {
                Log.d(TAG,"设备已断开连接");

                intentAction = ACTION_GATT_DISCONNECTED;//jia
                mConnectionState = STATE_DISCONNECTED;//jia
                broadcastUpdate(intentAction);//jia
            }
        }


        //00002a37-0000-1000-8000-00805f9b34fb

        /**
         * 当服务发现后所调用的方法
         */
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                System.out.println("Services discovered");



                List<BluetoothGattService> services = gatt.getServices();
                if (services != null) {
                    Log.d(TAG, "onServicesDiscovered num: " + services.size());
                }

                for (BluetoothGattService bluetoothGattService : services) {
                    Log.d(TAG, "onServicesDiscovered service: " + bluetoothGattService.getUuid());
                    List<BluetoothGattCharacteristic> charc = bluetoothGattService.getCharacteristics();

                    for (BluetoothGattCharacteristic charac : charc) {

                        // 设备的Characteristic UUID
                        Log.d(TAG, "Characteristic UUID: " + charac.getUuid().toString());
                        if(charac.getUuid().equals(mHeartRateCharacteristicUuid)){
                            // HEART_RATE_MEASUREMENT
                            Log.d(TAG, "mHearRateCharac found!");
                            mHearRateCharac = charac;
                            Log.d(TAG, "onServicesDiscovered: " + mHearRateCharac);
                            setCharacteristicNotification(mHearRateCharac,true);
                        }
                    }
                }

//                Log.d(TAG, "mHearRateCharac: " +mHearRateCharac.getValue().toString());

                // ? 确认一下
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);//xin



                //得到心率信息的service
                BluetoothGattService service = gatt
                        .getService(mHeartRateServiceUuid);
                if (service == null) {
                    Log.d(TAG,"没有得到心率服务");
                } else {
                    Log.d(TAG,"得到心率服务");
                    mCharacteristic = service
                            .getCharacteristic(mHeartRateCharacteristicUuid);



                    System.out.println("++++++++++++++++++++++++++++++++++++++++++11111");
                    System.out.println(mCharacteristic.getValue());
//                        Log.e("CharacteristicChanged中", "数据接收了哦"+bytesToHexString(characteristic.getValue()));
//                        tx_receive.append(bytesToHexString(characteristic.getValue()) + "\n");

                    System.out.println("++++++++++++++++++++++++++++++++++++++++++");
                  /*  if (!isHexData(mCharacteristic.toString())) {
//                       System.out.print("asdasdas");
                    }
*/
                    if (mCharacteristic == null) {
                        Log.d(TAG,"不能找到心率");
                    } else {
                        boolean success = gatt.setCharacteristicNotification(
                                mCharacteristic, true);
                        if (!success) {
                            Log.d(TAG,"Enabling notification failed!");
                            return;
                        }

                        BluetoothGattDescriptor descriptor = mCharacteristic
                                .getDescriptor(BleUUIDs.Descriptor.CHAR_CLIENT_CONFIG);
                        if (descriptor != null) {
                            descriptor
                                    .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                            Log.d(TAG,"Notification enabled");
                        } else {
                            Log.d(TAG,"Could not get descriptor for characteristic! Notification are not enabled.");
                        }
                    }
                }
            } else {
                Log.d(TAG,"Unable to discover services");
            }
        }

        /**
         * 当service里边的characteristic发生改变调用
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            Log.d(TAG, "onCharacteristicChanged UUID : " + characteristic.getUuid());
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);//jia

            //得到心率数据
            if (characteristic.equals(mHearRateCharac)) {

                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);//jia
                byte[] raw = characteristic.getValue();
                Log.d(TAG,"心率****=" + raw);
                int index = ((raw[0] & 0x01) == 1) ? 2 : 1;
                int format = (index == 1) ? BluetoothGattCharacteristic.FORMAT_UINT8 : BluetoothGattCharacteristic.FORMAT_UINT16;
                int value = mCharacteristic.getIntValue(format, index);
                final String description = value + " bpm";

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("心率：" + description);
                    }
                });

            }
        }

        /* the rest of callbacks are not interested for us */

        private void broadcastUpdate(final String action) {
            final Intent intent = new Intent(action);
            sendBroadcast(intent);
        }

        private void broadcastUpdate(final String action, BluetoothGattCharacteristic characteristic) {//xin
            final Intent intent = new Intent(action);
            sendBroadcast(intent);

            // This is special handling for the Heart Rate Measurement profile.  Data parsing is
            // carried out as per profile specifications:
            // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
            if (mHeartRateCharacteristicUuid.equals(characteristic.getUuid())) {
                int flag = characteristic.getProperties();
                int format = -1;
                if ((flag & 0x01) != 0) {
                    format = BluetoothGattCharacteristic.FORMAT_UINT16;
                    Log.d(TAG, "Heart rate format UINT16.");
                } else {
                    format = BluetoothGattCharacteristic.FORMAT_UINT8;
                    Log.d(TAG, "Heart rate format UINT8.");
                }
                final int heartRate = characteristic.getIntValue(format, 1);
                Log.d(TAG, String.format("Received heart rate: %d", heartRate));
                intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
            } else {
                // For all other profiles, writes the data formatted in HEX.
                final byte[] data = characteristic.getValue();
                if (data != null && data.length > 0) {
                    final StringBuilder stringBuilder = new StringBuilder(data.length);
                    for(byte byteChar : data)
                        stringBuilder.append(String.format("%02X ", byteChar));
                    intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
                }
            }
            sendBroadcast(intent);
        }



        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicRead UUID : " + characteristic.getUuid());
            Log.d(TAG, "onCharacteristicRead: " + characteristic.getValue());
            System.out.println("onCharacteristicRead");
            if (status == BluetoothGatt.GATT_SUCCESS)
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            Log.d(TAG, "characteristic----------1:" + characteristic);
            Log.d(TAG, "mGgatt----------1:" + mGgatt.getServices().size());
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(TAG, "onDescriptorWriteonDescriptorWrite = " + status
                    + ", characteristic =" + characteristic.getUuid().toString());

            Log.d(TAG, "--------write success----- status:" + status);
            Log.d(TAG, "--------write success----- WriteType:" + characteristic.getWriteType());

            Log.d(TAG, "onCharacteristicWrite" + Arrays.toString(characteristic.getValue()));
        }

        ;

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {

            System.out.println("rssi = " + rssi);
        }

    }





    private boolean mScanning;

    /**
     * 搜索蓝牙设备
     *
     * @param enable
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void scanLeDevice(boolean enable) {
        // TODO Auto-generated method stub
        if (enable) {
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(callback);
                }
            }, 10000);
            mScanning = true;
            mBluetoothAdapter.startLeScan(callback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(callback);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_ENABLE_BT
                && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    /**
     * 蓝牙搜索的回调方法
     */
    private BluetoothAdapter.LeScanCallback callback = new BluetoothAdapter.LeScanCallback() {

        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {
            Log.d(TAG, device + "****" + device.getName() + "***"
                    + device.getAddress());
            runOnUiThread(new Runnable() {
                public void run() {
                    adapter.addDevice(device);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    };
    private ListView lv;
    private LeDeviceListAdapter adapter;

    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = HeartRate.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view
                        .findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view
                        .findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText("未知设备");
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }


    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mGgatt == null) {
            Log.d(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mGgatt.setCharacteristicNotification(characteristic, enabled);
//        boolean notification = mGgatt.setCharacteristicNotification(characteristic, enabled);
//        System.out.print(notification);
         /*//notifiction默认是关闭的  需要设置0x01打开
                        List<BluetoothGattDescriptor> descriptors = localBluetoothGattCharacteristic.getDescriptors();
                        for (int i = 0; i < descriptors.size(); i++) {
                            if (descriptors.get(i).getUuid().toString().equals(DISENABLE)) {
                                BluetoothGattDescriptor bluetoothGattDescriptor = descriptors.get(i);
                                bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.PERMISSION_READ);
                                mBluetoothGatt.writeDescriptor(bluetoothGattDescriptor);
                            }
                        }*/

        // This is specific to Heart Rate Measurement.

       /* for(BluetoothGattDescriptor dp:characteristic.getDescriptors()){
            dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mGgatt.writeDescriptor(dp);
        }*/

        if (mHeartRateCharacteristicUuid.equals(characteristic.getUuid())) {
            for(BluetoothGattDescriptor dp:characteristic.getDescriptors()) {

                if (dp != null) {
                    if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                        dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    } else if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                        dp.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                    }
                    mGgatt.writeDescriptor(dp);
                }
            }
        }
    }//jia

    /*private boolean isHexData(String str) {
        if (str == null) {
            return false;
        }
        char[] chars = str.toCharArray();
        if ((chars.length & 1) != 0) {//个数为奇数，直接返回false
            return false;
        }
        for (char ch : chars) {
            if (ch >= '0' && ch <= '9') continue;
            if (ch >= 'A' && ch <= 'F') continue;
            if (ch >= 'a' && ch <= 'f') continue;
            return false;
        }
        return true;
    }*/
}

