package org.astri.spitfire.ble.common;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.vise.baseble.model.resolver.GattAttributeResolver;

import org.astri.spitfire.BleUUIDs;
import org.astri.spitfire.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.astri.spitfire.GodActivity.CHARA_MAP;
import static org.astri.spitfire.GodActivity.SERVICE_MAP;

/**
 * <pre>
 *     author : HuGuoDong
 *     e-mail : guodong_hu@126.com
 *     time   : 2018/07/14
 *     desc   :
 *     modified by :
 *     e-mail : xx_xxx@xx
 *     time   :
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class ServiceCharacUtil {

    private static final String TAG = "ServiceCharacUtil";

    //设备特征值集合
    private static List<List<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();
    private static List<BluetoothGattService> mGattServices = new ArrayList<>();
    private static final String LIST_NAME = "NAME";
    private static final String LIST_UUID = "UUID";


    final static private UUID mHeartRateServiceUuid = BleUUIDs.Service.HEART_RATE;
    final static private UUID mHeartRateCharacteristicUuid = BleUUIDs.Characteristic.HEART_RATE_MEASUREMENT;
    final static private UUID mAlgorithmServiceUuid = BleUUIDs.Service.ALGORITHEM_SERVICE;
    final static private UUID mAlgorithmIntensifyUuid = BleUUIDs.Characteristic.ALGORITHEM_AND_INTENSIFY;

    //
    private ServiceCharacUtil(){}

    /**
     * 根据GATT服务显示该服务下的所有特征值
     *
     * @param gattServices GATT服务
     * @return
     */
    private static void setGattServices(final List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        mGattCharacteristics.clear();
        mGattServices.clear();

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
    }


    /**
     * 获取设备需要的服务和属性
     * @param gattServices
     * @param serviceMap
     * @param charaMap
     */
    public static void getAppServicesCharcs(final List<BluetoothGattService> gattServices,
                                        Map<String, BluetoothGattService> serviceMap,
                                        Map<String, BluetoothGattCharacteristic> charaMap) {

        if(serviceMap == null || charaMap == null){
            LogUtil.e(TAG, "serviceMap or charaMap is null!");
            return;
        }

        setGattServices(gattServices);

        LogUtil.d(TAG, "将服务和属性保存在MAP中");

        for (BluetoothGattService service : mGattServices) {
            if (service.getUuid().equals(mAlgorithmServiceUuid)
                    || service.getUuid().equals(mHeartRateServiceUuid)) {

                serviceMap.put(service.getUuid().toString(), service);

                for (BluetoothGattCharacteristic chrc : service.getCharacteristics()) {
                    if (chrc.getUuid().equals(mAlgorithmIntensifyUuid)
                            || chrc.getUuid().equals(mHeartRateCharacteristicUuid)) {
                        // 保存chara
                        charaMap.put(chrc.getUuid().toString(), chrc);
                    }
                }
            }
        }

    }
}
