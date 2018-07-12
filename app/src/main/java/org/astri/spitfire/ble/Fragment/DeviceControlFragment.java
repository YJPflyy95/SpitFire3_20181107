package org.astri.spitfire.ble.Fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.vise.baseble.ViseBle;
import com.vise.baseble.common.ConnectState;
import com.vise.baseble.common.PropertyType;
import com.vise.baseble.core.DeviceMirror;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.model.resolver.GattAttributeResolver;
import com.vise.baseble.utils.HexUtil;
import com.vise.xsnow.cache.SpCache;
import com.vise.xsnow.event.BusManager;
import com.vise.xsnow.event.Subscribe;

import org.astri.spitfire.BleUUIDs;
import org.astri.spitfire.R;
import org.astri.spitfire.ble.common.BluetoothDeviceManager;
import org.astri.spitfire.ble.common.ToastUtil;
import org.astri.spitfire.ble.event.CallbackDataEvent;
import org.astri.spitfire.ble.event.ConnectEvent;
import org.astri.spitfire.ble.event.NotifyDataEvent;
import org.astri.spitfire.component.CircleImageView;
import org.astri.spitfire.fragment.MeFragment;
import org.astri.spitfire.util.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.astri.spitfire.GodActivity.CHARA_MAP;
import static org.astri.spitfire.GodActivity.SERVICE_MAP;

/**
 * @Description: 主页，展示已连接设备列表
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2017/10/20 17:35
 */
public class DeviceControlFragment extends Fragment {

    private static final String TAG = "DeviceControlFragment";

    private static final String LIST_NAME = "NAME";
    private static final String LIST_UUID = "UUID";
    public static final String WRITE_CHARACTERISTI_UUID_KEY = "write_uuid_key";
    public static final String NOTIFY_CHARACTERISTIC_UUID_KEY = "notify_uuid_key";
    public static final String WRITE_DATA_KEY = "write_data_key";

    private SimpleExpandableListAdapter simpleExpandableListAdapter;

    private TextView mConnectionState;
    private TextView mDeviceAddress;
    private TextView mDeviceName;

    private Button btShowHeartRate;
    private BluetoothGattCharacteristic heartRateChara;

    final static private UUID mHeartRateServiceUuid = BleUUIDs.Service.HEART_RATE;
    final static private UUID mHeartRateCharacteristicUuid = BleUUIDs.Characteristic.HEART_RATE_MEASUREMENT;
    final static private UUID mAlgorithmServiceUuid = BleUUIDs.Service.ALGORITHEM_SERVICE;
    final static private UUID mAlgorithmIntensifyUuid = BleUUIDs.Characteristic.ALGORITHEM_AND_INTENSIFY;


    private TextView mGattUUID;
    private TextView mGattUUIDDesc;
    private TextView mDataAsString;
    private TextView mDataAsArray;
    private EditText mInput;
    private EditText mOutput;
    private Button bConnectionState;

    private SpCache mSpCache;
    //设备信息
    private BluetoothLeDevice mDevice;
    //输出数据展示
    private StringBuilder mOutputInfo = new StringBuilder();
    private List<BluetoothGattService> mGattServices = new ArrayList<>();
    //设备特征值集合
    private List<List<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();
    private static final int PHOTO_REQUEST_CAREMA = 1;
    private static final int PHOTO_REQUEST_GALLERY = 2;
    private static final int PHOTO_REQUEST_CUT = 3;
    private static final String PHOTO_FILE_NAME = "smart-wristband.png";
    private File tempFile;
    private CircleImageView headIcon;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_control, container, false);
        setHasOptionsMenu(true);
        BusManager.getBus().register(this);
        Button back = view.findViewById(R.id.Back_bt);
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.ll_content, new MeFragment())
                        .commit();


            }
        });
        Button connect = view.findViewById(R.id.connect);
        connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//        if (!BluetoothDeviceManager.getInstance().isConnected(mDevice)) {
//            BluetoothDeviceManager.getInstance().connect(mDevice);
//            getActivity().invalidateOptionsMenu();
//        }
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.ll_content, new BLEMainFragment())
                        .commit();
            }
        });
        init(view);
        initView(view);
        onCreateOptionsMenu(view);
        BusManager.getBus().register(this);
//        showConnectedDevice(event);
        return view;
    }

    private void initView(View view) {

        headIcon = (CircleImageView) view.findViewById(R.id.headIcon);
        headIcon.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                changeHeadIcon();
            }
        });
        changeTheme();
        File file = new File(getActivity().getFilesDir(), "_head_icon.jpg");
        if (file.exists()) {
            headIcon.setImageURI(Uri.fromFile(file));
        }
    }

    private void changeTheme() {
        Calendar c = Calendar.getInstance();
        System.out.println(c.get(Calendar.HOUR_OF_DAY));
        if (c.get(Calendar.HOUR_OF_DAY) < 18 && c.get(Calendar.HOUR_OF_DAY) >= 6) {
            headIcon.setImageResource(R.drawable.live);
        } else {
            headIcon.setImageResource(R.drawable.my);
        }
    }

    private void changeHeadIcon() {
        final CharSequence[] items = {"相册", "拍照"};
        AlertDialog dlg = new AlertDialog.Builder(getContext()).setTitle("选择图片").setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // 这里item是根据选择的方式，
                if (item == 0) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent,
                            PHOTO_REQUEST_GALLERY);
                } else {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        tempFile = new File(Environment.getExternalStorageDirectory(),
                                PHOTO_FILE_NAME);
                        Uri uri = Uri.fromFile(tempFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        startActivityForResult(intent, PHOTO_REQUEST_CAREMA);
                    } else {
                        Toast.makeText(getContext(), "未找到存储卡，无法存储照片！",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }).create();
        dlg.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_REQUEST_GALLERY) {
            if (data != null) {
                Uri uri = data.getData();
                Log.e("图片路径？", data.getData() + "");
                crop(uri);
            }

        } else if (requestCode == PHOTO_REQUEST_CAREMA) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                crop(Uri.fromFile(tempFile));
            } else {
                Toast.makeText(getContext(), "未找到存储卡，无法存储照片！",
                        Toast.LENGTH_SHORT).show();
            }

        } else if (requestCode == PHOTO_REQUEST_CUT) {
            if (data != null) {
                final Bitmap bitmap = data.getParcelableExtra("data");
                headIcon.setImageBitmap(bitmap);
                // 保存图片到internal storage
                FileOutputStream outputStream;
                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    // out.close();
                    // final byte[] buffer = out.toByteArray();
                    // outputStream.write(buffer);
                    outputStream = getActivity().openFileOutput("_head_icon.jpg", Context.MODE_PRIVATE);
                    out.writeTo(outputStream);
                    out.close();
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                if (tempFile != null && tempFile.exists())
                    tempFile.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void crop(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 250);
        intent.putExtra("outputY", 250);
        intent.putExtra("outputFormat", "JPEG");
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    private void init(View view) {

        // 设备名，和地址
        mDeviceName = view.findViewById(R.id.AddYourDeviceName_et);
        mDeviceAddress = view.findViewById(R.id.device_address);

        btShowHeartRate = view.findViewById(R.id.bt_show_heart_rate);

        btShowHeartRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGattServices();

            }
        });

        mConnectionState = (TextView) view.findViewById(R.id.connection_state);

        mDataAsString = (TextView) view.findViewById(R.id.data_as_string);
        mDataAsArray = (TextView) view.findViewById(R.id.data_as_array);

//        mDevice = getActivity().getIntent().getParcelableExtra(DeviceDetailFragment.EXTRA_DEVICE);


        // 已经连接的设备
        if (getArguments() != null) { // getArguments 可能为null
            mDevice = getArguments().getParcelable(DeviceDetailFragment.EXTRA_DEVICE);
        }

        if (mDevice != null) {
            // 显示名字
            mDeviceAddress.setText(mDevice.getAddress());
            mDeviceName.setText(mDevice.getName());
        }

        mSpCache = new SpCache(getContext());

    }


    @SuppressLint("RestrictedApi")
    @Subscribe
    public void showConnectedDevice(ConnectEvent event) {
//        event.getDeviceMirror().getBluetoothLeDevice()
        if (event != null) {
            if (event.isSuccess()) {
                ToastUtil.showToast(getContext(), "Connect Success!");
                mDevice = event.getDeviceMirror().getBluetoothLeDevice();

                // 显示设备 详细信息
                mDeviceAddress.setText(mDevice.getAddress());
                mDeviceName.setText(mDevice.getName());
                mConnectionState.setText("true");
//                getActivity().invalidateOptionsMenu();
                if (event.getDeviceMirror() != null && event.getDeviceMirror().getBluetoothGatt() != null) {
                    LogUtil.d(TAG, "显示属性");
                    displayGattServices(event.getDeviceMirror().getBluetoothGatt().getServices());
//                    simpleExpandableListAdapter = displayGattServices(event.getDeviceMirror().getBluetoothGatt().getServices());
                }
            } else {
                if (event.isDisconnected()) {
                    ToastUtil.showToast(getContext(), "Disconnect!");
                } else {
                    ToastUtil.showToast(getContext(), "Connect Failure!");
                }
                mConnectionState.setText("false");
//                getActivity().invalidateOptionsMenu();
//                clearUI();
            }
        }
    }



    /**
     * 根据GATT服务显示该服务下的所有特征值
     *
     * @param gattServices GATT服务
     * @return
     */
    private void displayGattServices(final List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid;
        final String unknownServiceString = "Unknown service";
        final String unknownCharaString = "Unknown characteristic";

        final List<Map<String, String>> gattServiceData = new ArrayList<>();
        final List<List<Map<String, String>>> gattCharacteristicData = new ArrayList<>();

        mGattServices = new ArrayList<>();
        mGattCharacteristics = new ArrayList<>();

        // Loops through available GATT Services.
        for (final BluetoothGattService gattService : gattServices) {
            final Map<String, String> currentServiceData = new HashMap<>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME, GattAttributeResolver.getAttributeName(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            final List<Map<String, String>> gattCharacteristicGroupData = new ArrayList<>();
            final List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            final List<BluetoothGattCharacteristic> charas = new ArrayList<>();

            // Loops through available Characteristics.
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                final Map<String, String> currentCharaData = new HashMap<>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME, GattAttributeResolver.getAttributeName(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }

            mGattServices.add(gattService);
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        LogUtil.d(TAG, gattCharacteristicData.toString());
//        showGattServices();
    }


    private void showGattServices() {

        LogUtil.d(TAG, "将服务和属性保存在MAP中");
        BluetoothGattService service = null;
        BluetoothGattCharacteristic characteristic = null;

        for(BluetoothGattService svc : mGattServices){
            if(svc.getUuid().equals( mAlgorithmServiceUuid)
                    || svc.getUuid().equals(mHeartRateServiceUuid)){
                service = svc;


                // TODO: 这样做合适么？
                // 保存service
                SERVICE_MAP.put(service.getUuid().toString(), service);

                for(BluetoothGattCharacteristic chrc : service.getCharacteristics()){
                    if(chrc.getUuid().equals(mAlgorithmIntensifyUuid)
                            || chrc.getUuid().equals(mHeartRateCharacteristicUuid)){

                        // 保存chara
                        CHARA_MAP.put(chrc.getUuid().toString(), chrc);


                        characteristic = chrc;
                        heartRateChara = chrc;
                    }
                }
            }
        }

//        setCharaPropBindChnnel(service, characteristic);

    }


    /**
     * 设定服务：读，写，通知等
     * @param service
     * @param characteristic
     */
    private void setCharaPropBindChnnel(BluetoothGattService service,BluetoothGattCharacteristic characteristic){

        final int charaProp = characteristic.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            mSpCache.put(WRITE_CHARACTERISTI_UUID_KEY + mDevice.getAddress(), characteristic.getUuid().toString());
            BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_WRITE, service.getUuid(), characteristic.getUuid(), null);
            BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_READ, service.getUuid(), characteristic.getUuid(), null);
            BluetoothDeviceManager.getInstance().read(mDevice);
        } else if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_READ, service.getUuid(), characteristic.getUuid(), null);
            BluetoothDeviceManager.getInstance().read(mDevice);
        }
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            mSpCache.put(NOTIFY_CHARACTERISTIC_UUID_KEY + mDevice.getAddress(), characteristic.getUuid().toString());
            BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_NOTIFY, service.getUuid(), characteristic.getUuid(), null);
            BluetoothDeviceManager.getInstance().registerNotify(mDevice, false);
        } else if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
            mSpCache.put(NOTIFY_CHARACTERISTIC_UUID_KEY + mDevice.getAddress(), characteristic.getUuid().toString());
            BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_INDICATE, service.getUuid(), characteristic.getUuid(), null);
            BluetoothDeviceManager.getInstance().registerNotify(mDevice, true);
        }
    }





    private void setReadAndWrite(){

//        LogUtil.d(TAG, "read");
//        BluetoothGattService service = null;
//        BluetoothGattCharacteristic characteristic = null;
//        for(BluetoothGattService svc : mGattServices){
//            if(svc.getUuid().equals( mHeartRateServiceUuid)){
//                service = svc;
//                for(BluetoothGattCharacteristic chrc : service.getCharacteristics()){
//                    if(chrc.getUuid().equals(mHeartRateCharacteristicUuid)){
//                        characteristic = chrc;
//                        heartRateChara = chrc;
//                        break;
//                    }
//                }
//                break;
//            }
//        }
//        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
//            mSpCache.put(WRITE_CHARACTERISTI_UUID_KEY + mDevice.getAddress(), characteristic.getUuid().toString());
//            ((EditText) findViewById(R.id.show_write_characteristic)).setText(characteristic.getUuid().toString());
//            BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_WRITE, service.getUuid(), characteristic.getUuid(), null);
//        } else if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//            BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_READ, service.getUuid(), characteristic.getUuid(), null);
//            BluetoothDeviceManager.getInstance().read(mDevice);
//        }
    }

    @Subscribe
    public void showDeviceCallbackData(CallbackDataEvent event) {
        LogUtil.d(TAG, "showDeviceCallbackData");
        if (event != null) {
            if (event.isSuccess()) {
                if (event.getBluetoothGattChannel() != null && event.getBluetoothGattChannel().getCharacteristic() != null
                        && event.getBluetoothGattChannel().getPropertyType() == PropertyType.PROPERTY_READ) {
                    showReadInfo(event.getBluetoothGattChannel().getCharacteristic().getUuid().toString(), event.getData());
                }
//                if (event.getBluetoothGattChannel() != null && event.getBluetoothGattChannel().getCharacteristic() != null
//                        && event.getBluetoothGattChannel().getPropertyType() == PropertyType.PROPERTY_NOTIFY) {
//                    showReadInfo(event.getBluetoothGattChannel().getCharacteristic().getUuid().toString(), event.getData());
//                }
            }
//            else {
//                ((EditText) findViewById(R.id.show_write_characteristic)).setText("");
//                ((EditText) findViewById(R.id.show_notify_characteristic)).setText("");
//            }
        }
    }


    /**
     * 显示通知数据
     * @param event
     */
    @Subscribe
    public void showDeviceNotifyData(NotifyDataEvent event) {
        LogUtil.d(TAG, "showDeviceNotifyData");
        if (event != null && event.getData() != null && event.getBluetoothLeDevice() != null
                && event.getBluetoothLeDevice().getAddress().equals(mDevice.getAddress())) {

            BluetoothGattCharacteristic characteristic = event.getBluetoothGattChannel().getCharacteristic();
            if (mHeartRateCharacteristicUuid.equals(characteristic.getUuid())) {
                int flag = characteristic.getProperties();
                int format = -1;
                if ((flag & 0x01) != 0) {
                    Log.d(TAG, "format: "+format);
                    format = BluetoothGattCharacteristic.FORMAT_UINT16;
                    Log.d(TAG, "Heart rate format UINT16.");
                } else {
                    Log.d(TAG, "format: "+format);
                    format = BluetoothGattCharacteristic.FORMAT_UINT8;
                    Log.d(TAG, "Heart rate format UINT8.");
                }
                final int heartRate = characteristic.getIntValue(format, 1);
                Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            } else {
                // For all other profiles, writes the data formatted in HEX.
                final byte[] data = characteristic.getValue();
                LogUtil.d(TAG, characteristic.getUuid() + ": "+HexUtil.encodeHexStr(data));
//                if (data != null && data.length > 0) {
//                    final StringBuilder stringBuilder = new StringBuilder(data.length);
//                    for(byte byteChar : data)
//                        stringBuilder.append(String.format("%02X ", byteChar));
//                }
            }

            final byte[] data = characteristic.getValue();
            LogUtil.d(TAG, characteristic.getUuid() + ": "+HexUtil.encodeHexStr(data));

            // 暂时注释掉
//            mOutputInfo.append(HexUtil.encodeHexStr(event.getData())).append("\n");
//            LogUtil.d(TAG, mOutputInfo.toString());
//            mOutput.setText(mOutputInfo.toString());
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onResume() {
        getActivity().invalidateOptionsMenu();
        super.onResume();
    }

    public boolean onCreateOptionsMenu(View view) {
//        menu.clear();
//        MenuInflater inflater = getActivity().getMenuInflater();
//        inflater.inflate(R.menu.connect, menu);
        Button connect = (Button) view.findViewById(R.id.connect);
        if (BluetoothDeviceManager.getInstance().isConnected(mDevice)) {
            mConnectionState.setText("true");
            DeviceMirror deviceMirror = ViseBle.getInstance().getDeviceMirror(mDevice);
            if (deviceMirror != null) {
//                simpleExpandableListAdapter = displayGattServices(deviceMirror.getBluetoothGatt().getServices());
            }
//            showDefaultInfo();
        } else {
            mConnectionState.setText("false");
//            clearUI();
        }
        if (ViseBle.getInstance().getConnectState(mDevice) == ConnectState.CONNECT_PROCESS) {

        } else {

        }
        return true;
    }
//    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
//        menu.clear();
//        inflater.inflate(R.menu.connect, menu);
//        if (BluetoothDeviceManager.getInstance().isConnected(mDevice)) {
//            menu.findItem(R.id.menu_connect).setVisible(false);
//            menu.findItem(R.id.menu_disconnect).setVisible(true);
//            mConnectionState.setText("true");
//            DeviceMirror deviceMirror = ViseBle.getInstance().getDeviceMirror(mDevice);
//            if (deviceMirror != null) {
//                simpleExpandableListAdapter = displayGattServices(deviceMirror.getBluetoothGatt().getServices());
//            }
////            showDefaultInfo();
//        } else {
//            menu.findItem(R.id.menu_connect).setVisible(true);
//            menu.findItem(R.id.menu_disconnect).setVisible(false);
//            mConnectionState.setText("false");
////            clearUI();
//        }
//        if (ViseBle.getInstance().getConnectState(mDevice) == ConnectState.CONNECT_PROCESS) {
//            menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_progress_indeterminate);
//        } else {
//            menu.findItem(R.id.menu_refresh).setActionView(null);
//        }
//    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connect://连接设备
                if (!BluetoothDeviceManager.getInstance().isConnected(mDevice)) {
                    BluetoothDeviceManager.getInstance().connect(mDevice);
                    getActivity().invalidateOptionsMenu();
                }
                break;
            case R.id.menu_disconnect://断开设备
                if (BluetoothDeviceManager.getInstance().isConnected(mDevice)) {
                    BluetoothDeviceManager.getInstance().disconnect(mDevice);
                    getActivity().invalidateOptionsMenu();
                }
                break;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        BusManager.getBus().unregister(this);
        super.onDestroy();
    }

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

    private void showReadInfo(String uuid, byte[] dataArr) {

        LogUtil.d(TAG, uuid + ": "+HexUtil.encodeHexStr(dataArr));
//        mGattUUID.setText(uuid != null ? uuid : getString(R.string.no_data));
//        mGattUUIDDesc.setText(GattAttributeResolver.getAttributeName(uuid, getString(R.string.unknown)));
//        mDataAsArray.setText(HexUtil.encodeHexStr(dataArr));
//        mDataAsString.setText(new String(dataArr));
    }

//    private void showDefaultInfo() {
//        mGattUUID.setText(R.string.no_data);
//        mGattUUIDDesc.setText(R.string.no_data);
//        mDataAsArray.setText(R.string.no_data);
//        mDataAsString.setText(R.string.no_data);
//        mInput.setText(mSpCache.get(WRITE_DATA_KEY + mDevice.getAddress(), ""));
//        mOutput.setText("");
////        ((EditText) findViewById(R.id.show_write_characteristic)).setText(mSpCache.get(WRITE_CHARACTERISTI_UUID_KEY + mDevice.getAddress(), ""));
////        ((EditText) findViewById(R.id.show_notify_characteristic)).setText(mSpCache.get(NOTIFY_CHARACTERISTIC_UUID_KEY + mDevice.getAddress(), ""));
//        mOutputInfo = new StringBuilder();
//    }

//    private void clearUI() {
//        mGattUUID.setText(R.string.no_data);
//        mGattUUIDDesc.setText(R.string.no_data);
//        mDataAsArray.setText(R.string.no_data);
//        mDataAsString.setText(R.string.no_data);
//        mInput.setText("");
//        mOutput.setText("");
////        ((EditText) findViewById(R.id.show_write_characteristic)).setText("");
////        ((EditText) findViewById(R.id.show_notify_characteristic)).setText("");
//        mOutputInfo = new StringBuilder();
//        simpleExpandableListAdapter = null;
//        mSpCache.remove(WRITE_CHARACTERISTI_UUID_KEY + mDevice.getAddress());
//        mSpCache.remove(NOTIFY_CHARACTERISTIC_UUID_KEY + mDevice.getAddress());
//        mSpCache.remove(WRITE_DATA_KEY + mDevice.getAddress());
//    }

//    /**
//     * 显示GATT服务展示的信息
//     */
//    private void showGattServices() {
//        if (simpleExpandableListAdapter == null) {
//            return;
//        }
//        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_gatt_services, null);
//        ExpandableListView expandableListView = (ExpandableListView) view.findViewById(R.id.dialog_gatt_services_list);
//        expandableListView.setAdapter(simpleExpandableListAdapter);
//        builder.setView(view);
//        final AlertDialog dialog = builder.show();
////        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
////            @Override
////            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
////                dialog.dismiss();
////                final BluetoothGattService service = mGattServices.get(groupPosition);
////                final BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(groupPosition).get(childPosition);
////                final int charaProp = characteristic.getProperties();
////                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
////                    mSpCache.put(WRITE_CHARACTERISTI_UUID_KEY + mDevice.getAddress(), characteristic.getUuid().toString());
////                    ((EditText) findViewById(R.id.show_write_characteristic)).setText(characteristic.getUuid().toString());
////                    BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_WRITE, service.getUuid(), characteristic.getUuid(), null);
////                } else if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
////                    BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_READ, service.getUuid(), characteristic.getUuid(), null);
////                    BluetoothDeviceManager.getInstance().read(mDevice);
////                }
////                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
////                    mSpCache.put(NOTIFY_CHARACTERISTIC_UUID_KEY + mDevice.getAddress(), characteristic.getUuid().toString());
////                    ((EditText) findViewById(R.id.show_notify_characteristic)).setText(characteristic.getUuid().toString());
////                    BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_NOTIFY, service.getUuid(), characteristic.getUuid(), null);
////                    BluetoothDeviceManager.getInstance().registerNotify(mDevice, false);
////                } else if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
////                    mSpCache.put(NOTIFY_CHARACTERISTIC_UUID_KEY + mDevice.getAddress(), characteristic.getUuid().toString());
////                    ((EditText) findViewById(R.id.show_notify_characteristic)).setText(characteristic.getUuid().toString());
////                    BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_INDICATE, service.getUuid(), characteristic.getUuid(), null);
////                    BluetoothDeviceManager.getInstance().registerNotify(mDevice, true);
////                }
////                return true;
////            }
////        });
//    }


    /**
     * 判断是否是16进制数
     * @param str
     * @return
     */
    private boolean isHexData(String str) {
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
    }
}
