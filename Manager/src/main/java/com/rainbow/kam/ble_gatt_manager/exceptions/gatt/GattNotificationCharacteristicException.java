package com.rainbow.kam.ble_gatt_manager.exceptions.gatt;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import java.util.UUID;

/**
 * Created by Kang Young Won on 2016-05-12.
 */
public class GattNotificationCharacteristicException extends GattException {

    public static final String DESCRIPTION_WRITE_FAIL = "DescriptorWrite FAIL";

    private final BluetoothGattCharacteristic characteristic;
    private final UUID UUID;
    private final BluetoothGattDescriptor descriptor;
    private final int status;


    public GattNotificationCharacteristicException(BluetoothGattDescriptor descriptor, String subMessage) {
        this(descriptor, subMessage, STATE_UNKNOWN);
    }


    public GattNotificationCharacteristicException(BluetoothGattDescriptor descriptor, String subMessage, int status) {
        super(subMessage);
        this.descriptor = descriptor;
        this.characteristic = descriptor.getCharacteristic();
        this.UUID = characteristic.getUuid();
        this.status = status;
    }


    @Override public String toString() {
        return "GattNotificationCharacteristicException{" +
                ", UUID=" + UUID +
                ", descriptor=" + descriptor +
                ", subMessage=" + getMessage() +
                ", state=" + status +
                '}';
    }
}
