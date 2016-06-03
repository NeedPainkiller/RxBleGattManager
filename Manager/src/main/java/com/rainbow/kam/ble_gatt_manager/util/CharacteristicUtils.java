package com.rainbow.kam.ble_gatt_manager.util;

import android.bluetooth.BluetoothGattCharacteristic;

import com.google.common.collect.Maps;

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
}
