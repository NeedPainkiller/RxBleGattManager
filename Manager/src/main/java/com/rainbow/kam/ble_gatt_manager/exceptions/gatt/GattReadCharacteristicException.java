package com.rainbow.kam.ble_gatt_manager.exceptions.gatt;

import android.bluetooth.BluetoothGattCharacteristic;

import com.google.common.base.MoreObjects;

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
        return MoreObjects.toStringHelper(this)
                .add("Characteristic", Characteristic.getValue())
                .add("UUID", UUID.toString())
                .add("Message", getMessage())
                .add("state", state)
                .toString();
    }
}
