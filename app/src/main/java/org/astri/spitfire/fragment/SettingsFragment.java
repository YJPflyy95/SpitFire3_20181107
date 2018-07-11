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
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import org.astri.spitfire.ble.common.BluetoothDeviceManager;
import org.astri.spitfire.ble.common.ToastUtil;
import org.astri.spitfire.ble.event.CallbackDataEvent;
import org.astri.spitfire.ble.event.NotifyDataEvent;
import org.astri.spitfire.util.LogUtil;
import org.w3c.dom.Text;

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
    private Button settingAlgBtn;

    private SpCache mSpCache;

    private BluetoothLeDevice mDevice;

    // 一共四种算法 index: 0, 1, 2, 3
    private String[] algorithms = {
           "Learning Zone",
           "Awareness Zone",
           "Recovery Zone",
           "Relaxing Zone",
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, algorithms);
        ListView alglist = view.findViewById(R.id.algorithm_list);

        // TODO: 添加点击事件监听器
//        alglist.getOnItemClickListener(new ListView);
        alglist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ToastUtil.showShortToast(getContext(), ""+position);
            }
        });

        alglist.setAdapter(adapter);
        init(view);
        LogUtil.d(TAG, "onCreate");

        BubbleSeekBar bubbleSeekBar1 = view.findViewById(R.id.intense_seek_bar_1);

        bubbleSeekBar1.getConfigBuilder()
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


        bubbleSeekBar1.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {

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
                ToastUtil.showShortToast(getContext(), ""+s);

                String intensityLevel = "0" + (progress - 1);
                String algIndex = "03";
                String param = algIndex + intensityLevel;

                BluetoothDeviceManager.getInstance().write(mDevice, HexUtil.decodeHex(param.toCharArray()));
                ToastUtil.showShortToast(getContext(), ""+param);
            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                String s = String.format(Locale.CHINA, "onFinally int:%d, float:%.1f", progress, progressFloat);
                LogUtil.d(TAG, s);
            }
        });

//        bubbleSeekBar1.setProgress(2); // 设置进度


        return view;
    }

    private void init(View view) {

        settingAlgTv = view.findViewById(R.id.algorithm_setting_tv);
        settingAlgResultTv = view.findViewById(R.id.algorithm_setting_result_tv);

        settingAlgBtn = view.findViewById(R.id.algorithm_setting_btn);

        settingAlgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(GodActivity.getDevice()!=null){
                    LogUtil.d(TAG, "已经存在连接的设备！");
                    // 尝试读数据
                    BluetoothGattService algService = SERVICE_MAP.get(mAlgorithmServiceUuid.toString());
                    BluetoothGattCharacteristic algCharacteristic = CHARA_MAP.get(mAlgorithmIntensifyUuid.toString());
                    setAlgCharaPropBindReadChnnel(algService,algCharacteristic);

                }else {
                    LogUtil.d(TAG, "设备未连接！");
                }
            }
        });


        mSpCache = new SpCache(getContext());

        // 获取已经连接的设备
        mDevice = GodActivity.getDevice();
        if(mDevice != null){

            LogUtil.d(TAG, "已经存在设备，开始绑定通道！");

            SERVICE_MAP.get(mHeartRateServiceUuid.toString());
            CHARA_MAP.get(mHeartRateCharacteristicUuid.toString());

            BluetoothGattService heartRateService = SERVICE_MAP.get(mHeartRateServiceUuid.toString());
            BluetoothGattCharacteristic heartRateCharacteristic = CHARA_MAP.get(mHeartRateCharacteristicUuid.toString());
            BluetoothGattService algService = SERVICE_MAP.get(mAlgorithmServiceUuid.toString());
            BluetoothGattCharacteristic algCharacteristic = CHARA_MAP.get(mAlgorithmIntensifyUuid.toString());


            // 绑定channel
//            setHearRateCharaPropBindChnnel(heartRateService, heartRateCharacteristic);
            setAlgCharaPropBindNotifyChnnel(algService, algCharacteristic);
            setAlgCharaPropBindWriteChnnel(algService, algCharacteristic);

        }


    }


    /**
     * 设定服务：读，写，通知等
     * @param service
     * @param characteristic
     */
    private void setCharaPropBindChnnel(BluetoothGattService service, BluetoothGattCharacteristic characteristic){

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

    private void setHearRateCharaPropBindChnnel(BluetoothGattService service, BluetoothGattCharacteristic characteristic){

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


    private void setAlgCharaPropBindWriteChnnel(BluetoothGattService service, BluetoothGattCharacteristic characteristic){
        final int charaProp = characteristic.getProperties();
        mSpCache.put(WRITE_CHARACTERISTI_UUID_KEY + mDevice.getAddress(), characteristic.getUuid().toString());
        BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_WRITE, service.getUuid(), characteristic.getUuid(), null);
        LogUtil.d(TAG, "setAlgCharaPropBindWriteChnnel done!");

//        setAlgCharaPropBindNotifyChnnel(service, characteristic);
    }


    private void setAlgCharaPropBindNotifyChnnel(BluetoothGattService service, BluetoothGattCharacteristic characteristic){
        final int charaProp = characteristic.getProperties();
        mSpCache.put(NOTIFY_CHARACTERISTIC_UUID_KEY + mDevice.getAddress(), characteristic.getUuid().toString());
        BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_NOTIFY, service.getUuid(), characteristic.getUuid(), null);
        BluetoothDeviceManager.getInstance().registerNotify(mDevice, false);
        LogUtil.d(TAG, "setAlgCharaPropBindNotifyChnnel done!");

    }


    /**
     * 由于读取设备信息基本每次的通道都不一样，所以这里与上面收发数据有点不一样，每次读取数据都需要绑定一次通道.
     * @param service
     * @param characteristic
     */
    private void setAlgCharaPropBindReadChnnel(BluetoothGattService service, BluetoothGattCharacteristic characteristic){

//        final int charaProp = characteristic.getProperties();
////        if((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//            BluetoothDeviceManager.getInstance().bindChannel(mDevice, PropertyType.PROPERTY_READ, service.getUuid(), characteristic.getUuid(), null);
//            BluetoothDeviceManager.getInstance().read(mDevice);
////        }

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

        LogUtil.d(TAG, uuid + ": "+HexUtil.encodeHexStr(dataArr));
//        mGattUUID.setText(uuid != null ? uuid : getString(R.string.no_data));
//        mGattUUIDDesc.setText(GattAttributeResolver.getAttributeName(uuid, getString(R.string.unknown)));
//        mDataAsArray.setText(HexUtil.encodeHexStr(dataArr));
//        mDataAsString.setText(new String(dataArr));
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
                if (data != null && data.length > 0) {
                    final StringBuilder stringBuilder = new StringBuilder(data.length);
                    for(byte byteChar : data)
                        stringBuilder.append(String.format("%02X ", byteChar));
//                    Log.d(TAG, String.format("Received data: %d", heartRate));
                }
            }

            // 暂时注释掉
//            mOutputInfo.append(HexUtil.encodeHexStr(event.getData())).append("\n");
//            LogUtil.d(TAG, mOutputInfo.toString());
//            mOutput.setText(mOutputInfo.toString());
        }
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
            }
//            else {
//                ((EditText) findViewById(R.id.show_write_characteristic)).setText("");
//                ((EditText) findViewById(R.id.show_notify_characteristic)).setText("");
//            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mActivity = (Activity) context;
    }

    /**
     * 设置算法和强度
     */
    private void setAlgIntense(){

    }


    private void stopAlg(){

    }
}
