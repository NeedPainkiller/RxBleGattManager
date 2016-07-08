package com.rainbow.kam.ble_gatt_manager.exceptions.gatt;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

/**
 * Created by Kang Young Won on 2016-05-12.
 */
public class GattReadCharacteristicException extends GattException {

    private final BluetoothGattCharacteristic Characteristic;
    private final UUID UUID;
    private final int state;


    public GattReadCharacteristicException(BluetoothGattCharacteristic Characteristic, String subMessage, int state) {
        super(subMessage);
        this.Characteristic = Characteristic;
        this.UUID = Characteristic.getUuid();
        this.state = state;
    }


    @Override public String toString() {
        return "GattReadCharacteristicException{" +
                "Characteristic=" + Characteristic +
                ", UUID=" + UUID +
                ", subMessage=" + getMessage() +
                ", state=" + state +
                '}';
    }
}
