package com.rainbow.kam.ble_gatt_manager.util;

import android.bluetooth.BluetoothGattCharacteristic;

import com.google.common.collect.Maps;

import java.util.HashMap;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Kang Young Won on 2016-05-20.
 */
public class CharacteristicUtils {


    public static Observable<HashMap<String, String>> getFormattedValues(BluetoothGattCharacteristic characteristic) {

        final String hex = "%02X";
        final String str = "%c";

        final byte[] value = characteristic.getValue();
        final HashMap<String, String> valueMap = Maps.newHashMap();
        final StringBuilder hexBuilder = new StringBuilder(value.length).append("0x");
        final StringBuilder decBuilder = new StringBuilder(value.length);
        final StringBuilder strBuilder = new StringBuilder(value.length);

        return Observable.create(new Observable.OnSubscribe<HashMap<String, String>>() {
            @Override
            public void call(Subscriber<? super HashMap<String, String>> subscriber) {

                for (byte byteChar : value) {
                    hexBuilder.append(String.format(hex, byteChar));
                    decBuilder.append(byteChar & 0xff);
                    strBuilder.append(String.format(str, byteChar));
                }
                valueMap.put("HEX", hexBuilder.toString());
                valueMap.put("DEC", decBuilder.toString());
                valueMap.put("STR", strBuilder.toString());

                subscriber.onNext(valueMap);
                subscriber.unsubscribe();
            }
        }).subscribeOn(AndroidSchedulers.mainThread());
    }
}
