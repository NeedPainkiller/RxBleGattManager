package com.rainbow.kam.ble_gatt_manager.exceptions.gatt.details;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattException;

import java.util.UUID;

/**
 * Created by Kang Young Won on 2016-05-12.
 */
public class NotificationCharacteristicException extends GattException {
    private final BluetoothGattCharacteristic characteristic;
    private final UUID UUID;
    private final BluetoothGattDescriptor descriptor;
    private final int status;


    public NotificationCharacteristicException(BluetoothGattDescriptor descriptor, String subMessage) {
        this(descriptor, subMessage, STATUS_UNKNOWN);
    }


    public NotificationCharacteristicException(BluetoothGattDescriptor descriptor, String subMessage, int status) {
        super(subMessage);
        this.descriptor = descriptor;
        this.characteristic = descriptor.getCharacteristic();
        this.UUID = characteristic.getUuid();
        this.status = status;
    }


    @Override public String toString() {
        return "NotificationCharacteristicException{" +
                ", UUID=" + UUID +
                ", descriptor=" + descriptor +
                ", subMessage=" + getMessage() +
                ", state=" + status +
                '}';
    }
}
