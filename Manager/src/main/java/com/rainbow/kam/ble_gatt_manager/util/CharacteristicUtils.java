package com.rainbow.kam.ble_gatt_manager.util;

import android.bluetooth.BluetoothGattCharacteristic;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Kang Young Won on 2016-05-20.
 */
public class CharacteristicUtils {


    public static Observable<String> getStringValue(BluetoothGattCharacteristic characteristic, String format) {

        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override public void call(Subscriber<? super String> subscriber) {
                final byte[] value = characteristic.getValue();
                final StringBuilder valueBuilder = new StringBuilder(value.length);
                for (byte byteChar : value) {
                    valueBuilder.append(String.format(format, byteChar));
                }
                subscriber.onNext(valueBuilder.toString());
            }
        }).subscribeOn(AndroidSchedulers.mainThread());
    }
}
