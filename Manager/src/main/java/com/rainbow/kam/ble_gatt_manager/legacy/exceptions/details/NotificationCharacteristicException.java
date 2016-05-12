package com.rainbow.kam.ble_gatt_manager.legacy.exceptions.details;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import com.rainbow.kam.ble_gatt_manager.legacy.exceptions.GattException;

import java.util.UUID;

/**
 * Created by Kang Young Won on 2016-05-12.
 */
public class NotificationCharacteristicException extends GattException {
    private final BluetoothGattCharacteristic characteristic;
    private final UUID UUID;
    private final BluetoothGattDescriptor descriptor;
    private final int status;


    public NotificationCharacteristicException(BluetoothGattCharacteristic characteristic, BluetoothGattDescriptor descriptor, String subMessage) {
        this(characteristic, descriptor, subMessage, STATUS_UNKNOWN);
    }


    public NotificationCharacteristicException(BluetoothGattCharacteristic characteristic, BluetoothGattDescriptor descriptor, String subMessage, int status) {
        super(subMessage);
        this.characteristic = characteristic;
        this.UUID = characteristic.getUuid();
        this.descriptor = descriptor;
        this.status = status;
    }


    @Override public String toString() {
        return "NotificationCharacteristicException{" +
                "characteristic=" + characteristic +
                ", UUID=" + UUID +
                ", descriptor=" + descriptor +
                ", subMessage=" + getMessage() +
                ", state=" + status +
                '}';
    }
}
