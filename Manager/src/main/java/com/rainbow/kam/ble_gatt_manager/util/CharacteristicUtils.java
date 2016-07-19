package com.rainbow.kam.ble_gatt_manager.util;

import android.bluetooth.BluetoothGattCharacteristic;
import android.text.TextUtils;

import com.google.common.collect.Maps;
import com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattWriteCharacteristicException;

import java.util.HashMap;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Kang Young Won on 2016-05-20.
 */
public class CharacteristicUtils {


    public static Observable<HashMap<String, String>> getFormattedValues(BluetoothGattCharacteristic characteristic) {

        final byte[] value = characteristic.getValue();
        final HashMap<String, String> valueMap = Maps.newHashMap();
        final StringBuilder hexBuilder = new StringBuilder(value.length);
        final StringBuilder decBuilder = new StringBuilder(value.length);
        final StringBuilder strBuilder = new StringBuilder(value.length);

        return Observable.create((Observable.OnSubscribe<HashMap<String, String>>) subscriber -> {
            for (byte byteChar : value) {
                hexBuilder.append("0x").append(String.format("%02X", byteChar));
                decBuilder.append(byteChar & 0xff);
                strBuilder.append(String.format("%c", byteChar));
            }
            valueMap.put("HEX", hexBuilder.toString());
            valueMap.put("DEC", decBuilder.toString());
            valueMap.put("STR", strBuilder.toString());

            subscriber.onNext(valueMap);
            subscriber.onCompleted();
            subscriber.unsubscribe();
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
    }


    public static Observable<byte[]> createHexByteValue(final BluetoothGattCharacteristic characteristic, final String hex) {
        return Observable.create(subscriber -> {
            if (!TextUtils.isEmpty(hex) || hex.length() > 1) {
                String writeHexValue = hex.replaceAll("[^[0-9][a-f]]", "");
                byte[] bytes = new byte[(writeHexValue.length() / 2) + 1];
                int length = bytes.length;
                for (int i = 0; i < length; ++i) {
                    bytes[i] = Long.decode("0x" + hex.substring(i * 2, i * 2 + 2)).byteValue();
                }
                subscriber.onNext(bytes);
                subscriber.onCompleted();
            } else {
                subscriber.onError(new GattWriteCharacteristicException(characteristic, "value is null or empty"));
            }
        });
    }


    @Override public String toString() {
        return "CharacteristicUtils{" +
                "Observable<HashMap<String, String>> getFormattedValues(BluetoothGattCharacteristic characteristic)" +
                "Observable<byte[]> createHexByteValue(final BluetoothGattCharacteristic characteristic, final String hex)" +
                "}";
    }
}
