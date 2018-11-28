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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import static org.astri.spitfire.util.Constants.ALG_SETTING_PARAM;
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

    // hard code device name
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

    private TextView settingAlgResultTv;
    private TextView heartRateTv;
    private Button settingAlgBtn;
    private Button heartRateBtn;
    private ListView alglist;
    private Button stopBtn;

    // mSpCache store key-value
    // i.e. store alg setting, we can show setting on app UI, regardless device is connected or nor.
    private SpCache mSpCache;

    private BluetoothLeDevice mDevice;
    private DeviceMirror mDeviceMirror;

    // polling data from device
    private PollingDevice mPollingDevice;

    // 4 algorithms indices are : 0, 1, 2, 3
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

    // alg intense setting
    private BubbleSeekBar seekBar;

    // event
    private NotifyDataEvent heartRateDataEvent = new NotifyDataEvent();
    private NotifyDataEvent notifyDataEvent = new NotifyDataEvent();
    private CallbackDataEvent callbackDataEvent = new CallbackDataEvent();
    private ConnectEvent connectEvent = new ConnectEvent();

    // serviceMap, charaMap
    private Map<String, BluetoothGattService> serviceMap = new HashMap<>();
    private Map<String, BluetoothGattCharacteristic> charaMap = new HashMap<>();

    // wait time
    private static final long SLEEP_INTERVAL = 100;

    // DEFAULT_ALG_SETTING is used to show default setting ui
    private static final String DEFAULT_ALG_SETTING = "0000";

    // AsyncTask
    private class BLETask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            // bind service and characteristic
            bindServiceCharac();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }


    /**
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        LogUtil.d(TAG, "LifeCycle onCreateView");

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // we must register event bus, then app can receive notification
        BusManager.getBus().register(SettingsFragment.this);

        // initAlgorithms
        initAlgorithms();

        // initStop btn
        initStopBtn(view);

        // initMisc alg list view
        initAlgListView(view);

        // init seek bar
        initSeekBar(view);

        // init  miscellaneous: test, connect device ...
        initMisc(view);

        // connect device
        connect();

        return view;
    }

    private void initStopBtn(View view) {
        // stop
        stopBtn = view.findViewById(R.id.bt_stop);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (connected()) {
                    // stop algorithms, and set params, send to device
                    if (alg == null) {
                        alg = new Algorithm("");
                    }
                    alg.setIndex(ALGORITHM_STOP);
                    alg.setName(STOP);

                    String algParam = alg.genAlgSettingPara();
                    BluetoothDeviceManager.getInstance().write(mDevice, HexUtil.decodeHex(algParam.toCharArray()));
                    LogUtil.d(TAG, "Stop alg, current setting is：" + alg);
                    ToastUtil.showShortToast(mActivity, "" + "Stop Algo Success");
                } else {
                    ToastUtil.showShortToast(mActivity, "" + "No connected device");
                }
            }
        });


    }

    private void initAlgListView(View view) {
        final AlgorithmAdapter adapter = new AlgorithmAdapter(mActivity, R.layout.list_view_item_algorithms, algorithmList);
        alglist = view.findViewById(R.id.algorithm_list);
        alglist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // setting algorithm index
                try {
                    alg = algorithmList.get(position);
                } catch (Exception e) {
                    LogUtil.e(TAG, e.toString());
                    alg = new Algorithm("");
                }

                int intense = seekBar.getProgress() - 1; // attention minus 1 !
                alg.setIntensify(intense);
                alg.setIndex(position + 1);
                String algParam = alg.genAlgSettingPara(); // i.e. 0303

                LogUtil.d(TAG, "modify algorithm index，current index is：" + alg);

                if (connected()) {
                    // write data
                    BluetoothDeviceManager.getInstance().write(mDevice, HexUtil.decodeHex(algParam.toCharArray()));
                    // write data, we end polling
                    mPollingDevice.endPolling(r);
                    ToastUtil.showShortToast(mActivity, "" + "Adjust algo success");
                } else {
                    ToastUtil.showShortToast(mActivity, "" + "No connected device");
                }
            }
        });


        alglist.setAdapter(adapter);
    }


    /**
     * initSeekBar setting
     *
     * @param view
     */
    private void initSeekBar(View view) {
        // Algorithm intense progress bar
        seekBar = view.findViewById(R.id.intense_seek_bar_1);
        seekBar.getConfigBuilder()
                .min(1)
                .max(5)
                .progress(1) // default setting
                .sectionCount(4)
                .trackColor(ContextCompat.getColor(mActivity, R.color.gray))
                .secondTrackColor(ContextCompat.getColor(mActivity, R.color.appmain))
                .thumbColor(ContextCompat.getColor(mActivity, R.color.appmain))
                .thumbRadius(10) // thumb scale add by huguodong
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

            // Listener
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                String s = String.format(Locale.CHINA, "onChanged int:%d, float:%.1f", progress, progressFloat);
                LogUtil.d(TAG, s);
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                String s = String.format(Locale.CHINA, "onActionUp int:%d, float:%.1f", progress, progressFloat);
                LogUtil.d(TAG, s);

                if (connected()) {
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

                    // write data, we end polling
                    mPollingDevice.endPolling(r);

                    LogUtil.d(TAG, "Algorithm intense setting is：" + alg);
                    ToastUtil.showShortToast(mActivity, "" + "Adjust intensity success");
                } else {
                    ToastUtil.showShortToast(mActivity, "" + "No connected device");
                }
            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser)
            {
                String s = String.format(Locale.CHINA, "onFinally int:%d, float:%.1f", progress, progressFloat);
                LogUtil.d(TAG, s);
            }
        });

    }//initSeekBar( )

    /**
     * init  miscellaneous
     *
     * @param view
     */
    private void initMisc(View view) {
        settingAlgResultTv = view.findViewById(R.id.algorithm_setting_result_tv);
        heartRateTv = view.findViewById(R.id.heartrate_result_tv);
        heartRateBtn = view.findViewById(R.id.heartrate_btn);
        settingAlgBtn = view.findViewById(R.id.algorithm_setting_btn);

        if (IS_PRODUCTION)  //是上线产品的话，这些控件就不显示了
        { // show some testing view or not
            heartRateTv.setVisibility(View.GONE);
            settingAlgResultTv.setVisibility(View.GONE);
            settingAlgBtn.setVisibility(View.GONE);
            heartRateBtn.setVisibility(View.GONE);
        }

        // init PollingDevice
        mPollingDevice = new PollingDevice(new Handler());
        mSpCache = new SpCache(mActivity);

        // just for testing
        setAlgBtn();
    } //initMisc( )

    /**
     * binding specific services and characteristic
     */
    private void bindServiceCharac() {
        BluetoothGattService heartRateService = serviceMap.get(mHeartRateServiceUuid.toString());
        BluetoothGattCharacteristic heartRateCharacteristic = charaMap.get(mHeartRateCharacteristicUuid.toString());
        BluetoothGattService algService = serviceMap.get(mAlgorithmServiceUuid.toString());
        BluetoothGattCharacteristic algCharacteristic = charaMap.get(mAlgorithmIntensifyUuid.toString());
        // bind channels
        try {

            // set hr
            Thread.sleep(SLEEP_INTERVAL);
            setHearRateCharaPropBindChnnel(heartRateService, heartRateCharacteristic);

            // set alg
            Thread.sleep(SLEEP_INTERVAL);
            setAlgCharaPropBindReadChnnel(algService, algCharacteristic);

            Thread.sleep(SLEEP_INTERVAL);
            setAlgCharaPropBindWriteChnnel(algService, algCharacteristic);

            Thread.sleep(SLEEP_INTERVAL);
            setAlgCharaPropBindNotifyChnnel(algService, algCharacteristic);

            Thread.sleep(SLEEP_INTERVAL);

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }


    /**
     * set HR binding
     *
     * @param service
     * @param characteristic
     */
    private void setHearRateCharaPropBindChnnel(BluetoothGattService service, BluetoothGattCharacteristic characteristic)
    {

        final int charaProperties = characteristic.getProperties();
        if ((charaProperties | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0)
        {
            mSpCache.put(WRITE_CHARACTERISTI_UUID_KEY + mDevice.getAddress(), characteristic.getUuid().toString());
            BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_WRITE, service.getUuid(), characteristic.getUuid(), null);
            BluetoothDeviceManager.getInstance().read(mDevice);
        }
        else if ((charaProperties & BluetoothGattCharacteristic.PROPERTY_READ) > 0)
        {
            BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_READ, service.getUuid(), characteristic.getUuid(), null);
            BluetoothDeviceManager.getInstance().read(mDevice);
        }
        if ((charaProperties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0)
        {
            mSpCache.put(NOTIFY_CHARACTERISTIC_UUID_KEY + mDevice.getAddress(), characteristic.getUuid().toString());
            BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_NOTIFY, service.getUuid(), characteristic.getUuid(), null);
            BluetoothDeviceManager.getInstance().registerNotify(mDevice, false);
        }
        else if ((charaProperties & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0)
        {
            mSpCache.put(NOTIFY_CHARACTERISTIC_UUID_KEY + mDevice.getAddress(), characteristic.getUuid().toString());
            BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_INDICATE, service.getUuid(), characteristic.getUuid(), null);
            BluetoothDeviceManager.getInstance().registerNotify(mDevice, true);
        }
    }


    /**
     * receive IBleCallback for setting
     */
    private IBleCallback receiveAlgCallBack = new IBleCallback() {
        @Override
        public void onSuccess(final byte[] data, BluetoothGattChannel bluetoothGattInfo, BluetoothLeDevice bluetoothLeDevice) {
            if (data == null)
            {
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

    /**
     * when fragment is created.
     * first call onAttach
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        LogUtil.d(TAG, "LifeCycle onAttach");
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
     * initAlgorithms
     * show in the ListView
     */
    private void initAlgorithms()
    {
        algorithmList.clear();
        for (int i = 0; i < algorithms.length; i++)
        {
            Algorithm alg = new Algorithm(algorithms[i]);
            alg.setIndex(i + 1); // attention add 1
            algorithmList.add(alg);
        }

    }

    /**
     * IMPORTANT!!
     * subscribe device notification data
     *
     * @param event
     */
    @Subscribe
    public void showDeviceNotifyData(NotifyDataEvent event) {
        LogUtil.d(TAG, "showDeviceNotifyData");
        if (event != null && event.getData() != null && event.getBluetoothLeDevice() != null
                && event.getBluetoothLeDevice().getAddress().equals(mDevice.getAddress()))
        {

            BluetoothGattCharacteristic characteristic = event.getBluetoothGattChannel().getCharacteristic();
            if (mHeartRateCharacteristicUuid.equals(characteristic.getUuid()))
            {
                int flag = characteristic.getProperties();
                int format = -1;
                if ((flag & 0x01) != 0)
                {
                    Log.d(TAG, "format: " + format);
                    format = BluetoothGattCharacteristic.FORMAT_UINT16;
                    Log.d(TAG, "Heart rate format UINT16.");
                } else
                {
                    Log.d(TAG, "format: " + format);
                    format = BluetoothGattCharacteristic.FORMAT_UINT8;
                    Log.d(TAG, "Heart rate format UINT8.");
                }
                characteristic.setValue(event.getData( ));
                final int heartRate = characteristic.getIntValue(format, 1);
                heartRateTv.setText(heartRate + " BMP");
                Log.d(TAG, String.format("Received heart rate: %d", heartRate));
                LogUtil.d(TAG, characteristic.getUuid() + ": " + HexUtil.encodeHexStr(event.getData()));

            }
            else if (characteristic.getUuid().equals(mAlgorithmIntensifyUuid))
            {
                // do nothing...
                LogUtil.d(TAG, "Alg Intense notify received....");
            }
            else
            {
                // For all other profiles, writes the data formatted in HEX.
                final byte[] data = characteristic.getValue();
                LogUtil.d(TAG, characteristic.getUuid() + ": " + HexUtil.encodeHexStr(data));
            }

        }
    }


    @SuppressLint("RestrictedApi")
    @Subscribe
    public void showConnectedDevice(ConnectEvent connectEvent) {
        LogUtil.d(TAG, "showConnectedDevice");
        if (connectEvent != null) {
            if (connectEvent.isSuccess())
            {
                ToastUtil.showToast(mActivity, "Device Connected");
                if (connectEvent.getDeviceMirror() != null && connectEvent.getDeviceMirror().getBluetoothGatt() != null)
                {
                    mDevice = connectEvent.getDeviceMirror().getBluetoothLeDevice();

                    // set device service and characteristic
                    List<BluetoothGattService> gattServices = connectEvent.getDeviceMirror().getBluetoothGatt().getServices();
                    ServiceCharacUtil.getAppServicesCharcs(gattServices, serviceMap, charaMap);

                    // TODO: must wait a little while, don't figure out why?
//                    try {Thread.sleep(500);}
//                   catch (Exception e) {}

                    // binding service
//                    new BLETask().execute();

                    bindServiceCharac();
                    LogUtil.d(TAG, "set device service and characteristic success！");
                }
            }//if (connectEvent.isSuccess())
            else
            {
                if (connectEvent.isDisconnected())
                {
                    clearData();
                    ToastUtil.showToast(mActivity, "Device Disconnected");
                }
                else
                {
                    clearData();
                    ToastUtil.showToast(mActivity, "Connect Failure!");
                }
            }
        }
    }


    /**
     * just for testing
     */
    private void setAlgBtn() {
        settingAlgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
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


    /**
     * get data from callback
     * i.e. read characteristic
     *
     * @param event
     */
    @Subscribe
    public void showDeviceCallbackData(CallbackDataEvent event) {
        LogUtil.d(TAG, "showDeviceCallbackData");
        if (event != null)
        {
            if (event.isSuccess())
            {
                if (event.getBluetoothGattChannel() != null
                        && event.getBluetoothGattChannel().getCharacteristic() != null)
                {
                    // read data from device!
                    if (event.getBluetoothGattChannel().getPropertyType() == PropertyType.PROPERTY_READ)
                    {
                        showReadInfo(event.getBluetoothGattChannel().getCharacteristic().getUuid().toString(), event.getData());

                        BluetoothGattCharacteristic chara = event.getBluetoothGattChannel().getCharacteristic();
                        // if chara is AlgorithmIntensify
                        if (chara.getUuid().equals(mAlgorithmIntensifyUuid))
                        {
                            LogUtil.d(TAG, "polling read device alg data...");

                            // deal with param, get string like "0202"
                            String param = getAlgSettingStringFromCharac(chara);

                            // update alg setting UI （更新UI）
                            preUpdateAlgSettingUI(param);
                        }
                    }

                    if (event.getBluetoothGattChannel().getPropertyType() == PropertyType.PROPERTY_WRITE)
                    {    // start polling
                        mPollingDevice.startPolling(r, 1000);
                    }
                }
            }
        }
    }


    /**
     * update Algorithm display in UI
     *
     * @param param
     */

    private void preUpdateAlgSettingUI(String param, boolean isWaitShow) {

        try {

            mSpCache.put(ALG_SETTING_PARAM, param); // store setting

            settingAlgResultTv.setText(param);
            final int algIndex = Integer.parseInt(param.subSequence(1, 2).toString());
            final int intense = Integer.parseInt(param.subSequence(3, param.length()).toString());
            if (alg == null) {
                alg = new Algorithm("");
            }
            alg.setIndex(algIndex);
            alg.setIntensify(intense);
            alglist.post(new Runnable() {
                @Override
                public void run() {
                    updateAlgIndexUi(algIndex);
                }
            });

            seekBar.post(new Runnable() {
                @Override
                public void run() {
                    updateSeekBarUi(intense);
                }
            });

            stopBtn.post(new Runnable() {
                @Override
                public void run() {
                    updateStopBtnUi(algIndex);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 1. prepare data
     * 2. then update UI
     *
     * @param param
     */
    private void preUpdateAlgSettingUI(String param) {

        try {
            mSpCache.put(ALG_SETTING_PARAM, param); // store
            // 如果是算法的chara，则做出相应的处理
            settingAlgResultTv.setText(param);
            final int algIndex = Integer.parseInt(param.subSequence(1, 2).toString());
            final int intense = Integer.parseInt(param.subSequence(3, param.length()).toString());
            if (alg == null) {
                alg = new Algorithm("");
            }
            alg.setIndex(algIndex);
            alg.setIntensify(intense);

            // update UI
            updateAlgIndexUi(algIndex); // index
            updateSeekBarUi(intense); // intense
            updateStopBtnUi(algIndex); // stop button UI

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Set Alg list index checked or not
     *
     * @param index
     */
    private void updateAlgIndexUi(int index)
    {
        int count = alglist.getCount();
        index -= 1;
        for (int i = 0; i < count; i++)
        {
            ImageView tempImageView = alglist.getChildAt(i).findViewById(R.id.algorithm_index_img);
            int blankId = R.drawable.blank;
            int checkedId = R.drawable.checkmark;
            if (i == index && index >= 0)
            {
                tempImageView.setImageResource(checkedId);
            }
            else
            {
                tempImageView.setImageResource(blankId);
            }
        }
    }

    /**
     * update progress bar
     *
     * @param intense
     */
    private void updateSeekBarUi(int intense)
    {
        // for intense set seek bar progress
        seekBar.setProgress(intense + 1);
    }

    /**
     * stop btn setting
     *
     * @param algIndex
     */
    private void updateStopBtnUi(int algIndex) {
        if (algIndex == 0)
        {
            stopBtn.setEnabled(false);
            stopBtn.setTextColor(getResources().getColor(R.color.gray));
        }
        else
        {
            stopBtn.setEnabled(true);
            stopBtn.setTextColor(getResources().getColor(R.color.white));
        }
    }

    /**
     * get length=4 string from charac
     *
     * @param chara
     */
    private String getAlgSettingStringFromCharac(BluetoothGattCharacteristic chara) {
        final byte[] data = chara.getValue();  // get data from characteristic
        LogUtil.d(TAG, chara.getUuid() + ": " + HexUtil.encodeHexStr(data));

        // setting param
        String algParams = HexUtil.encodeHexStr(data);

        try {
            LogUtil.d(TAG, "Alg Intense " + ": " + HexUtil.encodeHexStr(data));
            // TODO: if data length is larger than 4, sub str
            if (algParams.length() > 4)
            {
                algParams = algParams.substring(0, 4);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return algParams;
    }


    /**
     * clear saved services and characteristics
     */
    private void clearData()
    {
        serviceMap.clear();
        charaMap.clear();
    }


    /**
     * when fragment is visible to user
     * call onResume
     * Now we connect our device.
     */
    @Override
    public void onResume() {
        LogUtil.d(TAG, "LifeCycle onResume");
        super.onResume( );
        checkBluetoothPermission();
        if (mDevice == null || !BluetoothDeviceManager.getInstance().isConnected(mDevice))
        {
            BluetoothDeviceManager.getInstance().connectByName(DEVICE_NAME);
        }

        preUpdateAlgSettingUI(mSpCache.get(ALG_SETTING_PARAM, DEFAULT_ALG_SETTING), true);

        // 开始轮询
        mPollingDevice.startPolling(r, 1000);

    }


    /**
     * pause
     * stop polling
     */
    @Override
    public void onPause() {
        // end polling
        mPollingDevice.endPolling(r);

        if (connected())
        {
            BluetoothDeviceManager.getInstance().disconnect(mDevice);
        }
        super.onPause();
        LogUtil.d(TAG, "LifeCycle onPause");
    }

    /**
     * when fragment is destoryed
     * call onDestory
     */
    @Override
    public void onDestroy() {
        LogUtil.d(TAG, "LifeCycle onDestroy");

        ViseBle.getInstance().clear();
        BusManager.getBus().unregister(this);

        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        LogUtil.d(TAG, "LifeCycle onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDetach()
    {
        LogUtil.d(TAG, "LifeCycle onDetach");
        super.onDetach();
//        mActivity = null;
    }

    /**
     * check BLE permission
     */
    private void checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //校验是否已具有模糊定位权限
            if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                PermissionManager.instance().with(mActivity).request(new OnPermissionCallback() {
                    @Override
                    public void onRequestAllow(String permissionName) {enableBluetooth();}

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

    /**
     * enableBluetooth
     */
    @SuppressLint("RestrictedApi")
    private void enableBluetooth()
    {
        if (!BleUtil.isBleEnable(mActivity))
        {
            BleUtil.enableBluetooth(mActivity, 1);
        }
    }

    /**
     * check device is connected
     *
     * @return
     */
    private boolean connected()
    {
        return mDevice != null && BluetoothDeviceManager.getInstance().isConnected(mDevice);
    }


    /**
     * read algorithm setting parameter in the device
     */
    private void readAlgSettingData() {
        if (connected() && !serviceMap.isEmpty() && !charaMap.isEmpty())
        {
            LogUtil.d(TAG, "readAlgSettingData...");
            // the task in need of polling
            BluetoothGattService algService = serviceMap.get(mAlgorithmServiceUuid.toString());
            BluetoothGattCharacteristic algCharacteristic = charaMap.get(mAlgorithmIntensifyUuid.toString());
            setAlgCharaPropBindReadChnnel(algService, algCharacteristic);
        }

    }

    /**
     * a runnable thread
     * run to read data from device
     */
    private Runnable r = new Runnable() {
        @Override
        public void run() {
            readAlgSettingData();
        }
    };


}
