package com.rainbow.kam.ble_gatt_manager.exceptions.details;

import android.bluetooth.BluetoothGattCharacteristic;

import com.rainbow.kam.ble_gatt_manager.exceptions.GattException;

import java.util.UUID;

/**
 * Created by Kang Young Won on 2016-05-12.
 */
public class ReadCharacteristicException extends GattException {
    private final BluetoothGattCharacteristic Characteristic;
    private final UUID UUID;
    private final int state;


    public ReadCharacteristicException(BluetoothGattCharacteristic Characteristic, String subMessage, int state) {
        super(subMessage);
        this.Characteristic = Characteristic;
        this.UUID = Characteristic.getUuid();
        this.state = state;
    }


    @Override public String toString() {
        return "ReadCharacteristicException{" +
                "Characteristic=" + Characteristic +
                ", UUID=" + UUID +
                ", subMessage=" + getMessage() +
                ", state=" + state +
                '}';
    }
}
