package com.rainbow.kam.ble_gatt_manager.exceptions.gatt.details;

import android.bluetooth.BluetoothGattCharacteristic;

import com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattException;

import java.util.UUID;

/**
 * Created by Kang Young Won on 2016-05-12.
 */
public class WriteCharacteristicException extends GattException {
    private final BluetoothGattCharacteristic characteristic;
    private final UUID UUID;
    private final int state;


    public WriteCharacteristicException(BluetoothGattCharacteristic characteristic, String subMessage) {
        this(characteristic, subMessage, STATUS_UNKNOWN);
    }


    public WriteCharacteristicException(BluetoothGattCharacteristic characteristic, String subMessage, int state) {
        super(subMessage);
        this.characteristic = characteristic;
        this.UUID = characteristic.getUuid();
        this.state = state;
    }


    @Override public String toString() {
        return "WriteCharacteristicException{" +
                "characteristic=" + characteristic +
                ", UUID=" + UUID +
                ", subMessage=" + getMessage() +
                ", state=" + state +
                '}';
    }
}
