package org.astri.spitfire.fragment;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import com.vise.baseble.common.PropertyType;
import com.vise.baseble.core.BluetoothGattChannel;
import com.vise.baseble.core.DeviceMirror;
import com.vise.baseble.exception.BleException;
import com.vise.baseble.model.BluetoothLeDevice;
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
import org.astri.spitfire.ble.common.PollingDevice;
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

    private static final String TAG = SettingsFragment.class.getSimpleName();
    private static final String DEVICE_NAME = "Spitfire HRS";

    public static final String WRITE_CHARACTERISTI_UUID_KEY = "write_uuid_key";
    public static final String NOTIFY_CHARACTERISTIC_UUID_KEY = "notify_uuid_key";
    public static final String WRITE_DATA_KEY = "write_data_key";

    final static private UUID mHeartRateServiceUuid = BleUUIDs.Service.HEART_RATE;
    final static private UUID mHeartRateCharacteristicUuid = BleUUIDs.Characteristic.HEART_RATE_MEASUREMENT;
    final static private UUID mAlgorithmServiceUuid = BleUUIDs.Service.ALGORITHEM_SERVICE;
    final static private UUID mAlgorithmIntensifyUuid = BleUUIDs.Characteristic.ALGORITHEM_AND_INTENSIFY;

    private Activity mActivity;
    private Context mContext;

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

    // 轮询设备
    private PollingDevice mPollingDevice;

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


    private static final long SLEEP_INTERVAL = 100;

    private class BLETask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            // 设定服务和通知
            bindServiceCharac();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        // 重要：注册事件驱动， 注册以后才能得到通知。
        BusManager.getBus().register(SettingsFragment.this);

        // 停止算法
        stopBtn = view.findViewById(R.id.bt_stop);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isConnected()) {
                    // stop algorithms, and set params, send to device
                    if (alg == null) {
                        alg = new Algorithm("");
                    }
                    alg.setIndex(ALGORITHM_STOP);
                    alg.setName(STOP);

                    String algParam = alg.genAlgSettingPara();
                    BluetoothDeviceManager.getInstance().write(mDevice, HexUtil.decodeHex(algParam.toCharArray()));
                    LogUtil.d(TAG, "停止算法，当前算法为：" + alg);
                    ToastUtil.showShortToast(mActivity, "" + "Stop Algo success");
                } else {
                    ToastUtil.showShortToast(mActivity, "" + "No connected device");
                }
            }
        });


        // 初始化算法
        initAlgorithms();

        final AlgorithmAdapter adapter = new AlgorithmAdapter(mActivity, R.layout.list_view_item_algorithms, algorithmList);
        alglist = view.findViewById(R.id.algorithm_list);
        alglist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 设定算法
                try {
                    alg = algorithmList.get(position);
                } catch (Exception e) {
                    LogUtil.e(TAG, e.toString());
                    alg = new Algorithm("");
                }

                int intense = seekBar.getProgress() - 1; // 注意减1
                alg.setIntensify(intense);
                alg.setIndex(position + 1);
                String algParam = alg.genAlgSettingPara(); // 0303

                LogUtil.d(TAG, "调整算法序号，当前算法为：" + alg);

                if (isConnected()) {
                    // 写入数据
                    BluetoothDeviceManager.getInstance().write(mDevice, HexUtil.decodeHex(algParam.toCharArray()));
                    ToastUtil.showShortToast(mActivity, "" + "Adjust algo success");
                } else {
                    ToastUtil.showShortToast(mActivity, "" + "No connected device");
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
//                ToastUtil.showShortToast(mActivity, "" + s);
                if (isConnected()) {
                    // 只改变算法强度
                    int intense = (progress - 1);
                    if (alg == null) {
                        alg = new Algorithm("");
                        alg.setIndex(0);
                        alg.setIntensify(intense);
                    } else {
                        alg.setIntensify(intense);
                    }
                    String algParam = alg.genAlgSettingPara();
                    BluetoothDeviceManager.getInstance().write(mDevice, HexUtil.decodeHex(algParam.toCharArray()));
                    LogUtil.d(TAG, "调整算法强度，当前算法为：" + alg);
//                    ToastUtil.showShortToast(mActivity, "" + algParam);
                    ToastUtil.showShortToast(mActivity, "" + "Adjust intensity success");
                } else {
                    ToastUtil.showShortToast(mActivity, "" + "No connected device");
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

        if (IS_PRODUCTION) {
            heartRateTv.setVisibility(View.GONE);
            settingAlgResultTv.setVisibility(View.GONE);
            settingAlgBtn.setVisibility(View.GONE);
            heartRateBtn.setVisibility(View.GONE);
        }

        // 初始化 PollingDevice
        mPollingDevice = new PollingDevice(new Handler());

        mSpCache = new SpCache(mActivity);

        setAlgBtn();

        connect();

    }

    private void bindServiceCharac() {
        BluetoothGattService heartRateService = serviceMap.get(mHeartRateServiceUuid.toString());
        BluetoothGattCharacteristic heartRateCharacteristic = charaMap.get(mHeartRateCharacteristicUuid.toString());
        BluetoothGattService algService = serviceMap.get(mAlgorithmServiceUuid.toString());
        BluetoothGattCharacteristic algCharacteristic = charaMap.get(mAlgorithmIntensifyUuid.toString());
        // 绑定channel
        try {

            // TODO: 设定心率
            Thread.sleep(SLEEP_INTERVAL);
            setHearRateCharaPropBindChnnel(heartRateService, heartRateCharacteristic);
            // TODO: 设定算法
            Thread.sleep(SLEEP_INTERVAL);
            setAlgCharaPropBindReadChnnel(algService, algCharacteristic);

            Thread.sleep(SLEEP_INTERVAL);
            setAlgCharaPropBindNotifyChnnel(algService, algCharacteristic);

            Thread.sleep(SLEEP_INTERVAL);
            setAlgCharaPropBindWriteChnnel(algService, algCharacteristic);

            Thread.sleep(SLEEP_INTERVAL);

        } catch (Exception e) {
            Log.w(TAG, e.toString());
        }
    }


    /**
     * 设定 心率 绑定
     *
     * @param service
     * @param characteristic
     */
    private void setHearRateCharaPropBindChnnel(BluetoothGattService service, BluetoothGattCharacteristic characteristic) {

        final int charaProp = characteristic.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            mSpCache.put(WRITE_CHARACTERISTI_UUID_KEY + mDevice.getAddress(), characteristic.getUuid().toString());
//            BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_WRITE, service.getUuid(), characteristic.getUuid(), null);
            BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_WRITE, service.getUuid(), characteristic.getUuid(), null);
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
            ViseLog.w("notify fail:" + exception.getDescription());
        }
    };

    /**
     * Set ALGORITHMS WRITE channel
     *
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
     * Set ALGORITHMS NOTIFY channel
     *
     * @param service
     * @param characteristic
     */
    private void setAlgCharaPropBindNotifyChnnel(BluetoothGattService service, BluetoothGattCharacteristic characteristic) {
        mSpCache.put(NOTIFY_CHARACTERISTIC_UUID_KEY + mDevice.getAddress(), characteristic.getUuid().toString());
        BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_NOTIFY, service.getUuid(), characteristic.getUuid(), null);
        BluetoothDeviceManager.getInstance().registerNotify(mDevice, false);
    }


    /**
     * Set ALGORITHMS READ channel
     * Since the information of the reading device is basically different each time,
     * the data is slightly different from the above, and each time the data is read,
     * the channel needs to be bound once.
     *
     * @param service
     * @param characteristic
     */
    private void setAlgCharaPropBindReadChnnel(BluetoothGattService service, BluetoothGattCharacteristic characteristic) {
        BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_READ, service.getUuid(), characteristic.getUuid(), null);
        BluetoothDeviceManager.getInstance().read(mDevice);
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
     * Connect Device
     */
    private void connect() {
        LogUtil.d(TAG, "Start to connect device!");
        BluetoothDeviceManager.getInstance().connectByName(DEVICE_NAME);

    }

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
     *
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
                    Log.d(TAG, "format: " + format);
                    format = BluetoothGattCharacteristic.FORMAT_UINT16;
                    Log.d(TAG, "Heart rate format UINT16.");
                } else {
                    Log.d(TAG, "format: " + format);
                    format = BluetoothGattCharacteristic.FORMAT_UINT8;
                    Log.d(TAG, "Heart rate format UINT8.");
                }
                characteristic.setValue(event.getData());
                final int heartRate = characteristic.getIntValue(format, 1);
                heartRateTv.setText(heartRate + " BMP");
                Log.d(TAG, String.format("Received heart rate: %d", heartRate));
                LogUtil.d(TAG, characteristic.getUuid() + ": " + HexUtil.encodeHexStr(event.getData()));

            } else if (characteristic.getUuid().equals(mAlgorithmIntensifyUuid)) {

                // do nothing...
                LogUtil.d(TAG, "Alg Intense notify received....");


            } else {
                // For all other profiles, writes the data formatted in HEX.
                final byte[] data = characteristic.getValue();
                LogUtil.d(TAG, characteristic.getUuid() + ": " + HexUtil.encodeHexStr(data));
            }

        }
    }




    @SuppressLint("RestrictedApi")
    @Subscribe
    public void showConnectedDevice(ConnectEvent event) {
        LogUtil.d(TAG, "showConnectedDevice");
        if (event != null) {
            if (event.isSuccess()) {
                ToastUtil.showToast(mActivity, "Connect Success!");
                if (event.getDeviceMirror() != null && event.getDeviceMirror().getBluetoothGatt() != null) {
                    mDevice = event.getDeviceMirror().getBluetoothLeDevice();
                    LogUtil.d(TAG, "设定app的服务和属性到Map");

                    // 获取服务和属性
                    List<BluetoothGattService> gattServices = event.getDeviceMirror().getBluetoothGatt().getServices();
                    ServiceCharacUtil.getAppServicesCharcs(gattServices, serviceMap, charaMap);

                    // TODO: 线程等待一下才能成功！，没想明白原因。
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {

                    }

                    // 绑定服务
                    new BLETask().execute();

                    // 设定视图中的控件：
                    // 此时设定按钮
//                    setAlgBtn();
                    LogUtil.d(TAG, "设定app的服务和属性成功！");
                }
            } else {
                if (event.isDisconnected()) {
                    clearData();
                    ToastUtil.showToast(mActivity, "Disconnect!");
                } else {
                    clearData();
                    ToastUtil.showToast(mActivity, "Connect Failure!");
                }
            }
        }
    }


    private void setAlgBtn() {
        settingAlgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDevice != null) { // 设备
                    ToastUtil.showShortToast(mActivity, "Connected!");
                    LogUtil.d(TAG, "已经存在连接的设备！");
                    // 尝试读数据
                    BluetoothGattService algService = serviceMap.get(mAlgorithmServiceUuid.toString());
                    BluetoothGattCharacteristic algCharacteristic = charaMap.get(mAlgorithmIntensifyUuid.toString());
                    setAlgCharaPropBindReadChnnel(algService, algCharacteristic);
                } else {
                    ToastUtil.showShortToast(mActivity, "Device Not Connected!");
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
                    if (chara.getUuid().equals(mAlgorithmIntensifyUuid)) {
                        LogUtil.d(TAG, "polling read device alg data...");
                        setAlgUi(chara);
                    }
                }
            }
        }
    }

    /**
     * 设置算法强度的UI显示
     * @param chara
     */
    private void setAlgUi(BluetoothGattCharacteristic chara){
        final byte[] data = chara.getValue(); // 使用这种方式才能拿到数据
        LogUtil.d(TAG, chara.getUuid() + ": " + HexUtil.encodeHexStr(data));

        // 设定显示
        String algParams = HexUtil.encodeHexStr(data);


        try{
            LogUtil.d(TAG, "Alg Intense " + ": " + HexUtil.encodeHexStr(data));
            if(algParams.length() > 4){ // TODO: 返回的数据长度过长，此处截取4
                algParams = algParams.substring(0, 4);
            }

            // 如果是算法的chara，则做出相应的处理
            settingAlgResultTv.setText(algParams);

            int algIndex = Integer.parseInt(algParams.subSequence(1, 2).toString());
            int intense = Integer.parseInt(algParams.subSequence(3, algParams.length()).toString());

            // set UI change
            setAlgIndexUi(algIndex);

            // TODO: 设定选中状态
            alg.setIndex(algIndex);
            alg.setIntensify(intense);

            // for intense set seekbar progress
            seekBar.setProgress(intense + 1);


        } catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Set Alg list index checked or not
     * @param index
     */
    private void setAlgIndexUi(int index) {
        int count = alglist.getCount();
        index -= 1;
        for(int i=0; i<count; i++){
            ImageView tempImageView = alglist.getChildAt(i).findViewById(R.id.algorithm_index_img);
            int blankId = R.drawable.blank;
            int checkedId = R.drawable.checkmark;
            if(i == index && index >= 0){
                tempImageView.setImageResource(checkedId);
            }else{
                tempImageView.setImageResource(blankId);
            }
        }
    }

    /**
     * 清空数据 保存的 service 和 characteristic
     */
    private void clearData() {
        serviceMap.clear();
        charaMap.clear();
    }




    @Override
    public void onResume() {
        super.onResume();
        checkBluetoothPermission();
        if (mDevice == null || !BluetoothDeviceManager.getInstance().isConnected(mDevice)) {
            BluetoothDeviceManager.getInstance().connectByName(DEVICE_NAME);
        }

        // 开始轮询
        mPollingDevice.startPolling(r, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        // 结束轮询
        mPollingDevice.endPolling(r);
    }

    @Override
    public void onDestroy() {
        ViseBle.getInstance().clear();
        BusManager.getBus().unregister(this);
        if ( mDevice !=null && BluetoothDeviceManager.getInstance().isConnected(mDevice)) {
            BluetoothDeviceManager.getInstance().disconnect(mDevice);
        }
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        BusManager.getBus().unregister(this);
        if ( mDevice !=null && BluetoothDeviceManager.getInstance().isConnected(mDevice)) {
            BluetoothDeviceManager.getInstance().disconnect(mDevice);
        }
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mActivity = null;
    }

    /**
     * 检查蓝牙权限
     */
    private void checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //校验是否已具有模糊定位权限
            if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                PermissionManager.instance().with(mActivity).request(new OnPermissionCallback() {
                    @Override
                    public void onRequestAllow(String permissionName) {
                        enableBluetooth();
                    }

                    @Override
                    public void onRequestRefuse(String permissionName) {
                        mActivity.finish();
                    }

                    @Override
                    public void onRequestNoAsk(String permissionName) {
                        mActivity.finish();
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
        if (!BleUtil.isBleEnable(mActivity)) {
            BleUtil.enableBluetooth(mActivity, 1);
        }
    }

    /**
     * 判断设备是否已连接
     *
     * @return
     */
    private boolean isConnected() {
        return mDevice != null && BluetoothDeviceManager.getInstance().isConnected(mDevice);
    }


    /**
     * 读取设备算法设置数据
     */
    private void readAlgSettingData() {
        LogUtil.d(TAG, "readAlgSettingData...");
        if(isConnected()
                && !serviceMap.isEmpty()
                && !charaMap.isEmpty()){
            // the task in need of polling
            BluetoothGattService algService = serviceMap.get(mAlgorithmServiceUuid.toString());
            BluetoothGattCharacteristic algCharacteristic = charaMap.get(mAlgorithmIntensifyUuid.toString());
            setAlgCharaPropBindReadChnnel(algService, algCharacteristic);
        }


    }

    private Runnable r = new Runnable() {
        @Override
        public void run() {
            readAlgSettingData();
        }
    };


}
