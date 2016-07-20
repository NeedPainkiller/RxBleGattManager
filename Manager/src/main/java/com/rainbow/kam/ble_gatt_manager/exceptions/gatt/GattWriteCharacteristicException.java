package com.rainbow.kam.ble_gatt_manager.exceptions.gatt;

import android.bluetooth.BluetoothGattCharacteristic;

import com.google.common.base.MoreObjects;

import java.util.UUID;

/**
 * Created by Kang Young Won on 2016-05-12.
 */
public class GattWriteCharacteristicException extends GattException {

    public static final String NULL_OR_EMPTY_DATA = "data is Null or Empty";

    private final BluetoothGattCharacteristic characteristic;
    private final UUID UUID;
    private final int state;


    public GattWriteCharacteristicException(BluetoothGattCharacteristic characteristic, String subMessage) {
        this(characteristic, subMessage, STATE_UNKNOWN);
    }


    public GattWriteCharacteristicException(BluetoothGattCharacteristic characteristic, String subMessage, int state) {
        super(subMessage);
        this.characteristic = characteristic;
        this.UUID = characteristic.getUuid();
        this.state = state;
    }


    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("characteristic", characteristic.getValue())
                .add("UUID", UUID.toString())
                .add("subMessage", getMessage())
                .add("state", state)
                .toString();
    }
}
