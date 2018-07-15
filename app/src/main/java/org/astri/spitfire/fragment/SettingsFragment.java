package org.astri.spitfire.fragment;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.vise.baseble.ViseBle;
import com.vise.baseble.callback.IBleCallback;
import com.vise.baseble.callback.IConnectCallback;
import com.vise.baseble.callback.scan.IScanCallback;
import com.vise.baseble.callback.scan.ScanCallback;
import com.vise.baseble.callback.scan.SingleFilterScanCallback;
import com.vise.baseble.common.PropertyType;
import com.vise.baseble.core.BluetoothGattChannel;
import com.vise.baseble.core.DeviceMirror;
import com.vise.baseble.exception.BleException;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.model.BluetoothLeDeviceStore;
import com.vise.baseble.utils.BleUtil;
import com.vise.baseble.utils.HexUtil;
import com.vise.log.ViseLog;
import com.vise.xsnow.cache.SpCache;
import com.vise.xsnow.event.BusManager;
import com.vise.xsnow.event.Subscribe;
import com.vise.xsnow.permission.OnPermissionCallback;
import com.vise.xsnow.permission.PermissionManager;
import com.xw.repo.BubbleSeekBar;

import org.astri.spitfire.BleUUIDs;
import org.astri.spitfire.R;
import org.astri.spitfire.adapter.Algorithm;
import org.astri.spitfire.adapter.AlgorithmAdapter;
import org.astri.spitfire.ble.common.BluetoothDeviceManager;
import org.astri.spitfire.ble.common.ServiceCharacUtil;
import org.astri.spitfire.ble.common.ToastUtil;
import org.astri.spitfire.ble.event.CallbackDataEvent;
import org.astri.spitfire.ble.event.ConnectEvent;
import org.astri.spitfire.ble.event.NotifyDataEvent;
import org.astri.spitfire.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.astri.spitfire.util.Constants.IS_PRODUCTION;

/**
 * <pre>
 *     author : HuGuoDong
 *     e-mail : guodong_hu@126.com
 *     time   : 2018/07/09
 *     desc   :
 *     modified by :
 *     e-mail : xx_xxx@xx
 *     time   :
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";
    private static final String DEVICE_NAME = "Spitfire HRS";

    public static final String WRITE_CHARACTERISTI_UUID_KEY = "write_uuid_key";
    public static final String NOTIFY_CHARACTERISTIC_UUID_KEY = "notify_uuid_key";
    public static final String WRITE_DATA_KEY = "write_data_key";

    final static private UUID mHeartRateServiceUuid = BleUUIDs.Service.HEART_RATE;
    final static private UUID mHeartRateCharacteristicUuid = BleUUIDs.Characteristic.HEART_RATE_MEASUREMENT;
    final static private UUID mAlgorithmServiceUuid = BleUUIDs.Service.ALGORITHEM_SERVICE;
    final static private UUID mAlgorithmIntensifyUuid = BleUUIDs.Characteristic.ALGORITHEM_AND_INTENSIFY;

    private Activity mActivity;

    private TextView settingAlgTv;
    private TextView settingAlgResultTv;
    private TextView heartRateTv;
    private Button heartRateBtn;
    private Button settingAlgBtn;
    private ListView alglist;

    private Button stopBtn;
    private Button meBtn;

    private SpCache mSpCache;

    private BluetoothLeDevice mDevice;
    private DeviceMirror mDeviceMirror;

    // 一共四种算法 index: 0, 1, 2, 3
    private String[] algorithms = {
            "Learning Zone",
            "Awareness Zone",
            "Recovery Zone",
            "Relaxing Zone",
    };

    private List<Algorithm> algorithmList = new ArrayList<>();

    private Algorithm alg;
    private static final int ALGORITHM_STOP = 0;
    private static final String STOP = "stop algorithm";


    // 算法点击view
    private int lastPosition = -1;
    private ImageView oldImageView;
    private ImageView newImageView;

    //算法强度设置
    private BubbleSeekBar seekBar;

    private NotifyDataEvent heartRateDataEvent = new NotifyDataEvent();
    private NotifyDataEvent notifyDataEvent = new NotifyDataEvent();
    private CallbackDataEvent callbackDataEvent = new CallbackDataEvent();
    private ConnectEvent connectEvent = new ConnectEvent();

    private Map<String, BluetoothGattService> serviceMap = new HashMap<>();
    private Map<String, BluetoothGattCharacteristic> charaMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // 重要：注册事件驱动， 祖册以后才能得到通知。
        BusManager.getBus().register(this);

        // 停止算法
        stopBtn = view.findViewById(R.id.bt_stop);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isConnected()){
                    // 停止算法
                    if(alg == null){
                        alg = new Algorithm("");
                    }
                    alg.setIndex(ALGORITHM_STOP);
                    alg.setName(STOP);
                    String algParam = alg.genAlgSettingPara();
                    BluetoothDeviceManager.getInstance().write(mDevice, HexUtil.decodeHex(algParam.toCharArray()));
                    LogUtil.d(TAG, "停止算法，当前算法为：" + alg);
                    ToastUtil.showShortToast(getContext(), "" + "Stop Algo success");
                }else{
                    ToastUtil.showShortToast(getContext(), "" + "No connected device");
                }


            }
        });

        // 点击Me Button
        // 1. 停止算法, 断开蓝牙连接
        // 2. 返回 Me Fragment
        meBtn = view.findViewById(R.id.bt_me);
        meBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 1. 停止算法, 断开蓝牙连接
                if(isConnected()){
                    if (BluetoothDeviceManager.getInstance().isConnected(mDevice)) {
                        BluetoothDeviceManager.getInstance().disconnect(mDevice);
                        ToastUtil.showShortToast(getContext(), "Device Disconnected!");
                        LogUtil.d(TAG, "Device Disconnected!");
                    }
                }

                // 2. 返回 Me Fragment
                getFragmentManager().popBackStack();

            }
        });


        // 初始化算法
        initAlgorithms();
        final AlgorithmAdapter adapter = new AlgorithmAdapter(getActivity(), R.layout.list_view_item_algorithms, algorithmList);
        alglist = view.findViewById(R.id.algorithm_list);

        alglist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                oldImageView = view.findViewById(R.id.algorithm_index_img);
                // 设定选中
                oldImageView.setImageResource(R.drawable.checkmark);
                if (lastPosition != -1 && lastPosition != position) {
                    //如果已经单击过条目并且上次保存的item位置和当前位置不同
                    // TODO: 暂时使用白色透明图片代替
                    newImageView.setImageResource(R.drawable.blank);//把上次选中的样式去掉
                }
                newImageView = oldImageView;//把当前的条目保存下来
                lastPosition = position;//把当前的位置保存下来

                // 设定算法
                try{
                    alg = algorithmList.get(position);
                } catch (Exception e){
                    LogUtil.e(TAG, e.toString());
                    alg = new Algorithm("");
                }


                int intense = seekBar.getProgress() - 1; // 注意减1
                alg.setIntensify(intense);
                alg.setIndex(position + 1);
                String algParam = alg.genAlgSettingPara(); // 0303

                LogUtil.d(TAG, "调整算法序号，当前算法为：" + alg);

                if(isConnected()){
                    // 写入数据
                    BluetoothDeviceManager.getInstance().write(mDevice, HexUtil.decodeHex(algParam.toCharArray()));
                    ToastUtil.showShortToast(getContext(), "" + "Adjust algo success");
                }else {
                    ToastUtil.showShortToast(getContext(), "" + "No connected device");
                }
            }
        });

        alglist.setAdapter(adapter);

        // 设置 算法 强度调整进度条
        seekBar = view.findViewById(R.id.intense_seek_bar_1);
        seekBar.getConfigBuilder()
                .min(1)
                .max(5)
                .progress(1) // 默认选中 开始
                .sectionCount(4)
                .trackColor(ContextCompat.getColor(mActivity, R.color.gray))
                .secondTrackColor(ContextCompat.getColor(mActivity, R.color.appmain))
                .thumbColor(ContextCompat.getColor(mActivity, R.color.appmain))
                .thumbRadius(10) // add by huguodong
                .showSectionText()
                .sectionTextColor(ContextCompat.getColor(mActivity, R.color.gray))
                .sectionTextSize(18)
                .showThumbText()
                .thumbTextColor(ContextCompat.getColor(mActivity, R.color.appmain))
                .thumbTextSize(18)
//                .bubbleColor(ContextCompat.getColor(mActivity, R.color.color_red))
//                .bubbleTextSize(18)
                .hideBubble()
                .showSectionMark()
                .seekStepSection()
                .touchToSeek()
                .sectionTextPosition(BubbleSeekBar.TextPosition.BELOW_SECTION_MARK)
                .build();

        seekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {

            // 监听值的改变
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                String s = String.format(Locale.CHINA, "onChanged int:%d, float:%.1f", progress, progressFloat);
                LogUtil.d(TAG, s);
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

                // TODO: 此处设置 算法强度
                // 写入蓝牙设备
                String s = String.format(Locale.CHINA, "onActionUp int:%d, float:%.1f", progress, progressFloat);
                LogUtil.d(TAG, s);
//                ToastUtil.showShortToast(getContext(), "" + s);



                if(isConnected()){
                    // 只改变算法强度
                    int intense = (progress - 1);
                    if(alg == null){
                        alg = new Algorithm("");
                        alg.setIndex(0);
                        alg.setIntensify(intense);
                    }else{
                        alg.setIntensify(intense);
                    }
                    String algParam = alg.genAlgSettingPara();
                    BluetoothDeviceManager.getInstance().write(mDevice, HexUtil.decodeHex(algParam.toCharArray()));
                    LogUtil.d(TAG, "调整算法强度，当前算法为：" + alg);
//                    ToastUtil.showShortToast(getContext(), "" + algParam);
                    ToastUtil.showShortToast(getContext(), "" + "Adjust intensity success");
                }else{
                    ToastUtil.showShortToast(getContext(), "" + "No connected device");
                }
            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                String s = String.format(Locale.CHINA, "onFinally int:%d, float:%.1f", progress, progressFloat);
                LogUtil.d(TAG, s);
            }
        });

        init(view);

        return view;
    }

    private void init(View view) {


        settingAlgResultTv = view.findViewById(R.id.algorithm_setting_result_tv);
        heartRateTv = view.findViewById(R.id.heartrate_result_tv);
        heartRateBtn = view.findViewById(R.id.heartrate_btn);
        settingAlgBtn = view.findViewById(R.id.algorithm_setting_btn);

        if(IS_PRODUCTION){
            heartRateTv.setVisibility(View.GONE);
            settingAlgResultTv.setVisibility(View.GONE);
            settingAlgBtn.setVisibility(View.GONE);
            heartRateBtn.setVisibility(View.GONE);
        }


        mSpCache = new SpCache(getContext());
        // 获取已经连接的设备

//        mDevice = GodActivity.getDevice();

        // 如果不存在设备，扫描，并连接设备


        if (mDevice != null && ViseBle.getInstance().isConnect(mDevice)) {

            LogUtil.d(TAG, "已经存在设备，开始绑定通道！");
        } else {

            connect();

        }

        setAlgBtn();

    }

    private void bindServiceCharac(){
        BluetoothGattService heartRateService = serviceMap.get(mHeartRateServiceUuid.toString());
        BluetoothGattCharacteristic heartRateCharacteristic = charaMap.get(mHeartRateCharacteristicUuid.toString());
        BluetoothGattService algService = serviceMap.get(mAlgorithmServiceUuid.toString());
        BluetoothGattCharacteristic algCharacteristic = charaMap.get(mAlgorithmIntensifyUuid.toString());
        // 绑定channel
        // TODO: 设定心率
        try {

            Thread.sleep(100);
        }catch (Exception e){

        }
        setHearRateCharaPropBindChnnel(heartRateService, heartRateCharacteristic);
        // TODO: 设定算法
        try {

            Thread.sleep(100);
        }catch (Exception e){

        }
        setAlgCharaPropBindReadChnnel(algService,algCharacteristic);
        try {

            Thread.sleep(100);
        }catch (Exception e){

        }
        setAlgCharaPropBindNotifyChnnel(algService, algCharacteristic);
        try {

            Thread.sleep(100);
        }catch (Exception e){

        }
        setAlgCharaPropBindWriteChnnel(algService, algCharacteristic);
        try {

            Thread.sleep(100);
        }catch (Exception e){

        }
    }


    /**
     * 设定 心率 绑定
     * @param service
     * @param characteristic
     */
    private void setHearRateCharaPropBindChnnel(BluetoothGattService service, BluetoothGattCharacteristic characteristic) {

        final int charaProp = characteristic.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            mSpCache.put(WRITE_CHARACTERISTI_UUID_KEY + mDevice.getAddress(), characteristic.getUuid().toString());
//            BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_WRITE, service.getUuid(), characteristic.getUuid(), null);
            BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_READ, service.getUuid(), characteristic.getUuid(), null);
            BluetoothDeviceManager.getInstance().read(mDevice);
        } else if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_READ, service.getUuid(), characteristic.getUuid(), null);
            BluetoothDeviceManager.getInstance().read(mDevice);
        }
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            mSpCache.put(NOTIFY_CHARACTERISTIC_UUID_KEY + mDevice.getAddress(), characteristic.getUuid().toString());
            DeviceMirror deviceMirror = ViseBle.getInstance().getDeviceMirror(mDevice);
            BluetoothGattChannel bluetoothGattChannel = new BluetoothGattChannel.Builder()
                    .setBluetoothGatt(deviceMirror.getBluetoothGatt())
                    .setPropertyType(PropertyType.PROPERTY_NOTIFY)
                    .setServiceUUID(service.getUuid())
                    .setCharacteristicUUID(characteristic.getUuid())
                    .setDescriptorUUID(null)
                    .builder();
            deviceMirror.bindChannel(new IBleCallback() {
                @Override
                public void onSuccess(final byte[] data, BluetoothGattChannel bluetoothGattInfo, BluetoothLeDevice bluetoothLeDevice) {
                    if (data == null) {
                        return;
                    }
                    ViseLog.i("notify callback success:" + HexUtil.encodeHexStr(data));

                    // 事件
                    BusManager.getBus().post(callbackDataEvent.setData(data).setSuccess(true)
                            .setBluetoothLeDevice(bluetoothLeDevice)
                            .setBluetoothGattChannel(bluetoothGattInfo));

                    if (bluetoothGattInfo != null && (bluetoothGattInfo.getPropertyType() == PropertyType.PROPERTY_INDICATE
                            || bluetoothGattInfo.getPropertyType() == PropertyType.PROPERTY_NOTIFY)) {
                        DeviceMirror deviceMirror = ViseBle.getInstance().getDeviceMirror(mDevice);
                        if (deviceMirror != null) {
                            deviceMirror.setNotifyListener(bluetoothGattInfo.getGattInfoKey(), receiveHeartRateCallBack);
                        }
                    }
                }

                @Override
                public void onFailure(BleException exception) {
                    if (exception == null) {
                        return;
                    }
                    ViseLog.i("callback fail:" + exception.getDescription());
                    BusManager.getBus().post(callbackDataEvent.setSuccess(false));
                }
            }, bluetoothGattChannel);

            deviceMirror.registerNotify(false);


        } else if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
            mSpCache.put(NOTIFY_CHARACTERISTIC_UUID_KEY + mDevice.getAddress(), characteristic.getUuid().toString());
            BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_INDICATE, service.getUuid(), characteristic.getUuid(), null);
            BluetoothDeviceManager.getInstance().registerNotify(mDevice, true);
        }
    }

    /**
     * 接收数据回调
     */
    private IBleCallback receiveHeartRateCallBack = new IBleCallback() {
        @Override
        public void onSuccess(final byte[] data, BluetoothGattChannel bluetoothGattInfo, BluetoothLeDevice bluetoothLeDevice) {
            if (data == null) {
                return;
            }
            ViseLog.i("receiveHeartRateCallBack notify success:" + HexUtil.encodeHexStr(data));
            BusManager.getBus().post(heartRateDataEvent.setData(data)
                    .setBluetoothLeDevice(bluetoothLeDevice)
                    .setBluetoothGattChannel(bluetoothGattInfo));
        }

        @Override
        public void onFailure(BleException exception) {
            if (exception == null) {
                return;
            }
            ViseLog.i("notify fail:" + exception.getDescription());
        }
    };


    /**
     * 接收数据回调
     */
    private IBleCallback receiveAlgCallBack = new IBleCallback() {
        @Override
        public void onSuccess(final byte[] data, BluetoothGattChannel bluetoothGattInfo, BluetoothLeDevice bluetoothLeDevice) {
            if (data == null) {
                return;
            }
            ViseLog.i("receiveAlgCallBack notify success:" + HexUtil.encodeHexStr(data));
            BusManager.getBus().post(notifyDataEvent.setData(data)
                    .setBluetoothLeDevice(bluetoothLeDevice)
                    .setBluetoothGattChannel(bluetoothGattInfo));
        }

        @Override
        public void onFailure(BleException exception) {
            if (exception == null) {
                return;
            }
            ViseLog.i("notify fail:" + exception.getDescription());
        }
    };

    /**
     * 设定 【算法】 write channel
     * @param service
     * @param characteristic
     */
    private void setAlgCharaPropBindWriteChnnel(BluetoothGattService service, BluetoothGattCharacteristic characteristic) {
        final int charaProp = characteristic.getProperties();
        mSpCache.put(WRITE_CHARACTERISTI_UUID_KEY + mDevice.getAddress(), characteristic.getUuid().toString());
        BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_WRITE, service.getUuid(), characteristic.getUuid(), null);
        LogUtil.d(TAG, "setAlgCharaPropBindWriteChnnel done!");
    }




    /**
     * 设定 【算法】 notify channel
     * @param service
     * @param characteristic
     */
    private void setAlgCharaPropBindNotifyChnnel(BluetoothGattService service, BluetoothGattCharacteristic characteristic) {
        mSpCache.put(NOTIFY_CHARACTERISTIC_UUID_KEY + mDevice.getAddress(), characteristic.getUuid().toString());
        DeviceMirror deviceMirror = ViseBle.getInstance().getDeviceMirror(mDevice);
        BluetoothGattChannel bluetoothGattChannel = new BluetoothGattChannel.Builder()
                .setBluetoothGatt(deviceMirror.getBluetoothGatt())
                .setPropertyType(PropertyType.PROPERTY_NOTIFY)
                .setServiceUUID(service.getUuid())
                .setCharacteristicUUID(characteristic.getUuid())
                .setDescriptorUUID(null)
                .builder();
        deviceMirror.bindChannel(new IBleCallback() {
            @Override
            public void onSuccess(final byte[] data, BluetoothGattChannel bluetoothGattInfo, BluetoothLeDevice bluetoothLeDevice) {
                if (data == null) {
                    return;
                }
                // 读取成功，显示数
                settingAlgResultTv.setText(HexUtil.encodeHexStr(data));
                ViseLog.i("notify callback success:" + HexUtil.encodeHexStr(data));
                BusManager.getBus().post(callbackDataEvent.setData(data).setSuccess(true)
                        .setBluetoothLeDevice(bluetoothLeDevice)
                        .setBluetoothGattChannel(bluetoothGattInfo));
                // 读取成功，显示数
//                    settingAlgResultTv.setText(HexUtil.encodeHexStr(data));

                if (bluetoothGattInfo != null && (bluetoothGattInfo.getPropertyType() == PropertyType.PROPERTY_INDICATE
                        || bluetoothGattInfo.getPropertyType() == PropertyType.PROPERTY_NOTIFY)) {
                    DeviceMirror deviceMirror = ViseBle.getInstance().getDeviceMirror(mDevice);
                    if (deviceMirror != null) {
                        deviceMirror.setNotifyListener(bluetoothGattInfo.getGattInfoKey(), receiveAlgCallBack);
                    }
                }

            }

            @Override
            public void onFailure(BleException exception) {
                if (exception == null) {
                    return;
                }
                ViseLog.i("callback fail:" + exception.getDescription());
            }
        }, bluetoothGattChannel);
        deviceMirror.registerNotify(false);
        LogUtil.d(TAG, "setAlgCharaPropBindNotifyChnnel done!");
    }


    /**
     * 由于读取设备信息基本每次的通道都不一样，所以这里与上面收发数据有点不一样，每次读取数据都需要绑定一次通道.
     *
     * @param service
     * @param characteristic
     */
    private void setAlgCharaPropBindReadChnnel(BluetoothGattService service, BluetoothGattCharacteristic characteristic) {
        DeviceMirror deviceMirror = ViseBle.getInstance().getDeviceMirror(mDevice);
        BluetoothGattChannel bluetoothGattChannel = new BluetoothGattChannel.Builder()
                .setBluetoothGatt(deviceMirror.getBluetoothGatt())
                .setPropertyType(PropertyType.PROPERTY_READ)
                .setServiceUUID(service.getUuid())
                .setCharacteristicUUID(characteristic.getUuid())
                .setDescriptorUUID(null)
                .builder();
        deviceMirror.bindChannel(new IBleCallback() {
            @Override
            public void onSuccess(final byte[] data, BluetoothGattChannel bluetoothGattInfo, BluetoothLeDevice bluetoothLeDevice) {
                if (data == null) {
                    return;
                }

                ViseLog.i("read callback success:" + HexUtil.encodeHexStr(data));
                BusManager.getBus().post(callbackDataEvent.setData(data).setSuccess(true)
                        .setBluetoothLeDevice(bluetoothLeDevice)
                        .setBluetoothGattChannel(bluetoothGattInfo));
            }

            @Override
            public void onFailure(BleException exception) {
                if (exception == null) {
                    return;
                }
                ViseLog.i("callback fail:" + exception.getDescription());
            }
        }, bluetoothGattChannel);
        deviceMirror.readData();


    }

    private void showReadInfo(String uuid, byte[] dataArr) {
        LogUtil.d(TAG, uuid + ": " + HexUtil.encodeHexStr(dataArr));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mActivity = (Activity) context;
    }

    /**
     * 设置算法和强度
     */
    private void setAlgIntense() {

    }


    private void stopAlg() {

    }


    /**
     * 连接设备
     */
    private void connect(){
        LogUtil.d(TAG, "Start to connect device!");
        ViseBle.getInstance().connectByName(DEVICE_NAME, deviceConnectCallback);
    }

    private IConnectCallback deviceConnectCallback = new IConnectCallback() {

        @Override
        public void onConnectSuccess(DeviceMirror deviceMirror) {
            LogUtil.d(TAG, "onConnectSuccess");
            mDeviceMirror = deviceMirror;
            mDevice = mDeviceMirror.getBluetoothLeDevice();
            BusManager.getBus().post(connectEvent.setDeviceMirror(deviceMirror).setSuccess(true));

        }

        @Override
        public void onConnectFailure(BleException exception) {
            ViseLog.i("Connect Failure!");
            ToastUtil.showShortToast(getContext(), "Device Not Found!");
            BusManager.getBus().post(connectEvent.setSuccess(false).setDisconnected(false));
        }

        @Override
        public void onDisconnect(boolean isActive) {
            ViseLog.i("Disconnect!");
            BusManager.getBus().post(connectEvent.setSuccess(false).setDisconnected(true));
        }
    };


    /**
     * 开始扫描
     */
    @SuppressLint("RestrictedApi")
    private void startScan() {

        ViseBle.getInstance().startScan(filterDeviceNameScanCallback);
        getActivity().invalidateOptionsMenu();
    }

    /**
     * 停止扫描
     */
    @SuppressLint("RestrictedApi")
    private void stopScan() {
        ViseBle.getInstance().stopScan(filterDeviceNameScanCallback);
        getActivity().invalidateOptionsMenu();
    }


    /**
     * 扫描指定设备【名称】的设备
     *
     */
    private ScanCallback filterDeviceNameScanCallback = new SingleFilterScanCallback(new IScanCallback() {
        @Override
        public void onDeviceFound(BluetoothLeDevice bluetoothLeDevice) {

        }

        @Override
        public void onScanFinish(BluetoothLeDeviceStore bluetoothLeDeviceStore) {

        }

        @Override
        public void onScanTimeout() {

        }
    }).setDeviceName(DEVICE_NAME);

    /**
     * 初始化算法
     * 用于在listview中显示
     */
    private void initAlgorithms() {
        algorithmList.clear();
        for (int i = 0; i < algorithms.length; i++) {
            Algorithm alg = new Algorithm(algorithms[i]);
            alg.setIndex(i + 1); // 注意加一
            algorithmList.add(alg);
        }

    }

    /**
     * 订阅：显示通知数据
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
                characteristic.setValue(event.getData());
                final int heartRate = characteristic.getIntValue(format, 1);
                heartRateTv.setText(heartRate + " BMP");
                Log.d(TAG, String.format("Received heart rate: %d", heartRate));
                LogUtil.d(TAG, characteristic.getUuid() + ": "+HexUtil.encodeHexStr(event.getData()));
            } else {
                // For all other profiles, writes the data formatted in HEX.
                final byte[] data = characteristic.getValue();

                // 如果是算法的chara，则做出相应的处理
                if (characteristic.getUuid().equals(mAlgorithmIntensifyUuid)){
                    settingAlgResultTv.setText(HexUtil.encodeHexStr(data));
                }
                LogUtil.d(TAG, characteristic.getUuid() + ": "+HexUtil.encodeHexStr(data));
            }

        }
    }

    @SuppressLint("RestrictedApi")
    @Subscribe
    public void showConnectedDevice(ConnectEvent event) {
        LogUtil.d(TAG, "showConnectedDevice");
        if (event != null) {
            if (event.isSuccess()) {
                ToastUtil.showToast(getContext(), "Connect Success!");
                if (event.getDeviceMirror() != null && event.getDeviceMirror().getBluetoothGatt() != null) {
                    mDevice = event.getDeviceMirror().getBluetoothLeDevice();
                    LogUtil.d(TAG, "设定app的服务和属性到Map");

                    // 获取服务和属性
                    List<BluetoothGattService> gattServices = event.getDeviceMirror().getBluetoothGatt().getServices();
                    ServiceCharacUtil.getAppServicesCharcs(gattServices,serviceMap, charaMap);

                    // TODO: 线程等待一下才能成功！，没想明白原因。
                    try{
                        Thread.sleep(500);
                    }catch (Exception e){

                    }
                    // 设定服务和通知
                    bindServiceCharac();


                    // 设定视图中的控件：
                    // 此时设定按钮
//                    setAlgBtn();
                    LogUtil.d(TAG, "设定app的服务和属性成功！");
                }
            } else {
                if (event.isDisconnected()) {
                    clearData();
                    //ToastUtil.showToast(getContext(), "Disconnect!");
                } else {
                    clearData();
                    //ToastUtil.showToast(getContext(), "Connect Failure!");
                }
            }
        }
    }

    private void setAlgBtn() {
        settingAlgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDevice != null) { // 设备
                    ToastUtil.showShortToast(getContext(), "Connected!");
                    LogUtil.d(TAG, "已经存在连接的设备！");
                    // 尝试读数据
                    BluetoothGattService algService = serviceMap.get(mAlgorithmServiceUuid.toString());
                    BluetoothGattCharacteristic algCharacteristic = charaMap.get(mAlgorithmIntensifyUuid.toString());
                    setAlgCharaPropBindReadChnnel(algService, algCharacteristic);
                } else {
                    ToastUtil.showShortToast(getContext(), "Not Connected!");
                    LogUtil.d(TAG, "设备未连接！");
                }
            }
        });
    }


    @Subscribe
    public void showDeviceCallbackData(CallbackDataEvent event) {
        LogUtil.d(TAG, "showDeviceCallbackData");
        if (event != null) {
            if (event.isSuccess()) {
                if (event.getBluetoothGattChannel() != null && event.getBluetoothGattChannel().getCharacteristic() != null
                        && event.getBluetoothGattChannel().getPropertyType() == PropertyType.PROPERTY_READ) {
                    showReadInfo(event.getBluetoothGattChannel().getCharacteristic().getUuid().toString(), event.getData());

                    BluetoothGattCharacteristic chara = event.getBluetoothGattChannel().getCharacteristic();
                    // 如果是算法的chara，则做出相应的处理
                    if(chara.getUuid().equals(mAlgorithmIntensifyUuid)){
                        final byte[] data = event.getData(); // 使用这种方式才能拿到数据
                        // 设定显示
                        String param = HexUtil.encodeHexStr(data);
                        int algIndex = Integer.parseInt(param.subSequence(1,2).toString());
                        int intense = Integer.parseInt(param.subSequence(3,param.length()).toString());
                        if(algIndex != 0){
                            alg = algorithmList.get(algIndex - 1);
                        }

                        if(algIndex == 0){
                            alg = algorithmList.get(algIndex);
                        }

                        if(alg == null) {
                            alg = new Algorithm("");
                        }

                        // TODO: 设定选中状态
                        alg.setIndex(algIndex);
                        alg.setIntensify(intense);
                        if(algIndex > 0){
                            alg.setName(algorithms[algIndex - 1]);
                        }

                        seekBar.setProgress(intense + 1);
                        if(algIndex > 0){
                            newImageView = alglist.getChildAt(algIndex - 1).findViewById(R.id.algorithm_index_img);
                            newImageView.setImageResource(R.drawable.checkmark);
                            lastPosition = algIndex - 1;
                        }


                        settingAlgResultTv.setText(HexUtil.encodeHexStr(data));

                        LogUtil.d(TAG, "Alg Intense " + ": "+HexUtil.encodeHexStr(data));
                    }
                }
            }
        }
    }

    /**
     * 清空数据
     */
    private void clearData(){
        serviceMap.clear();
        charaMap.clear();
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
        }
    }

    /**
     * 判断设备是否已连接
     * @return
     */
    private boolean isConnected(){
        return mDevice != null && ViseBle.getInstance().isConnect(mDevice);
    }

}
