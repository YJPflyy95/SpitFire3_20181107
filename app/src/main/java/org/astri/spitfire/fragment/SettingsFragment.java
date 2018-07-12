package org.astri.spitfire.fragment;


import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
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
import com.vise.baseble.common.PropertyType;
import com.vise.baseble.core.BluetoothGattChannel;
import com.vise.baseble.core.DeviceMirror;
import com.vise.baseble.exception.BleException;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.utils.HexUtil;
import com.vise.log.ViseLog;
import com.vise.xsnow.cache.SpCache;
import com.vise.xsnow.event.BusManager;
import com.vise.xsnow.event.Subscribe;
import com.xw.repo.BubbleSeekBar;

import org.astri.spitfire.BleUUIDs;
import org.astri.spitfire.GodActivity;
import org.astri.spitfire.R;
import org.astri.spitfire.adapter.Algorithm;
import org.astri.spitfire.adapter.AlgorithmAdapter;
import org.astri.spitfire.ble.common.BluetoothDeviceManager;
import org.astri.spitfire.ble.common.ToastUtil;
import org.astri.spitfire.ble.event.CallbackDataEvent;
import org.astri.spitfire.ble.event.NotifyDataEvent;
import org.astri.spitfire.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.astri.spitfire.GodActivity.CHARA_MAP;
import static org.astri.spitfire.GodActivity.SERVICE_MAP;

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
    private TextView heartReateTv;
    private Button settingAlgBtn;

    private Button stopBtn;

    private SpCache mSpCache;

    private BluetoothLeDevice mDevice;

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

    private NotifyDataEvent notifyDataEvent = new NotifyDataEvent();
    private CallbackDataEvent callbackDataEvent = new CallbackDataEvent();

//    /**
//     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
//     * or the first access to SingletonHolder.INSTANCE, not before.
//     */
//    private static class SingletonHolder {
//        public static final SettingsFragment INSTANCE = new SettingsFragment();
//    }
//
//    public static SettingsFragment getInstance() {
//        return SingletonHolder.INSTANCE;
//    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // 停止算法
        stopBtn = view.findViewById(R.id.bt_stop);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 停止算法
                alg.setIndex(ALGORITHM_STOP);
                alg.setName(STOP);
                String algParam = alg.genAlgSettingPara();
                BluetoothDeviceManager.getInstance().write(mDevice, HexUtil.decodeHex(algParam.toCharArray()));
                LogUtil.d(TAG, "停止算法，当前算法为：" + alg);
                ToastUtil.showShortToast(getContext(), "" + algParam);
            }
        });

        // 初始化算法
        initAlgorithms();
        AlgorithmAdapter adapter = new AlgorithmAdapter(getActivity(), R.layout.list_view_item_algorithms, algorithmList);
        ListView alglist = view.findViewById(R.id.algorithm_list);

        alglist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ToastUtil.showShortToast(getContext(), "" + position);
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
                alg = algorithmList.get(position);
                int intense = seekBar.getProgress() - 1; // 注意减1
                alg.setIntensify(intense);
                alg.setIndex(position + 1);
                String algParam = alg.genAlgSettingPara(); // 0303

                LogUtil.d(TAG, "调整算法序号，当前算法为：" + alg);

                // 写入数据
                BluetoothDeviceManager.getInstance().write(mDevice, HexUtil.decodeHex(algParam.toCharArray()));
                ToastUtil.showShortToast(getContext(), "" + algParam);
            }
        });

        alglist.setAdapter(adapter);

        init(view);
        LogUtil.d(TAG, "onCreate");

        // 设置 算法 强度调整进度条
        seekBar = view.findViewById(R.id.intense_seek_bar_1);
        seekBar.getConfigBuilder()
                .min(1)
                .max(5)
                .progress(2)
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
                ToastUtil.showShortToast(getContext(), "" + s);

                // 只改变算法强度
                int intense = (progress - 1);
                alg.setIntensify(intense);
                String algParam = alg.genAlgSettingPara();

                BluetoothDeviceManager.getInstance().write(mDevice, HexUtil.decodeHex(algParam.toCharArray()));
                LogUtil.d(TAG, "调整算法强度，当前算法为：" + alg);
                ToastUtil.showShortToast(getContext(), "" + algParam);
            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                String s = String.format(Locale.CHINA, "onFinally int:%d, float:%.1f", progress, progressFloat);
                LogUtil.d(TAG, s);
            }
        });

        return view;
    }

    private void init(View view) {

        settingAlgResultTv = view.findViewById(R.id.algorithm_setting_result_tv);
        heartReateTv = view.findViewById(R.id.heartrate_result_tv);
        settingAlgBtn = view.findViewById(R.id.algorithm_setting_btn);
        settingAlgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GodActivity.getDevice() != null) { // 设备
                    ToastUtil.showShortToast(getContext(), "设备已连接");
                    LogUtil.d(TAG, "已经存在连接的设备！");
                    // 尝试读数据
                    BluetoothGattService algService = SERVICE_MAP.get(mAlgorithmServiceUuid.toString());
                    BluetoothGattCharacteristic algCharacteristic = CHARA_MAP.get(mAlgorithmIntensifyUuid.toString());
                    setAlgCharaPropBindReadChnnel(algService, algCharacteristic);
                } else {
                    ToastUtil.showShortToast(getContext(), "设备未连接");
                    LogUtil.d(TAG, "设备未连接！");
                }
            }
        });

        mSpCache = new SpCache(getContext());
        // 获取已经连接的设备
        mDevice = GodActivity.getDevice();
        if (mDevice != null) {

            // 重要：注册事件驱动， 祖册以后才能得到通知。
            BusManager.getBus().register(this);

            LogUtil.d(TAG, "已经存在设备，开始绑定通道！");
            BluetoothGattService heartRateService = SERVICE_MAP.get(mHeartRateServiceUuid.toString());
            BluetoothGattCharacteristic heartRateCharacteristic = CHARA_MAP.get(mHeartRateCharacteristicUuid.toString());
            BluetoothGattService algService = SERVICE_MAP.get(mAlgorithmServiceUuid.toString());
            BluetoothGattCharacteristic algCharacteristic = CHARA_MAP.get(mAlgorithmIntensifyUuid.toString());
            // 绑定channel
            // TODO: 设定心率
            setHearRateCharaPropBindChnnel(heartRateService, heartRateCharacteristic);
            // TODO: 设定算法
            setAlgCharaPropBindReadChnnel(algService,algCharacteristic);
            setAlgCharaPropBindNotifyChnnel(algService, algCharacteristic);
            setAlgCharaPropBindWriteChnnel(algService, algCharacteristic);
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
            BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_WRITE, service.getUuid(), characteristic.getUuid(), null);
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

                // 设定显示
                String param = HexUtil.encodeHexStr(data);
                int algIndex = Integer.parseInt(param.subSequence(1,2).toString());
                int intense = Integer.parseInt(param.subSequence(3,param.length()).toString());
                // TODO: 设定选中状态

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
//        mGattUUID.setText(uuid != null ? uuid : getString(R.string.no_data));
//        mGattUUIDDesc.setText(GattAttributeResolver.getAttributeName(uuid, getString(R.string.unknown)));
//        mDataAsArray.setText(HexUtil.encodeHexStr(dataArr));
//        mDataAsString.setText(new String(dataArr));
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
     * 初始化算法
     * 用于在listview中显示
     */
    private void initAlgorithms() {
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
                final int heartRate = characteristic.getIntValue(format, 1);
                heartReateTv.setText(heartRate + " BMP");
                Log.d(TAG, String.format("Received heart rate: %d", heartRate));
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
}
