package com.rainbow.kam.ble_gatt_manager.model;

import android.bluetooth.BluetoothGattCharacteristic;

import com.google.common.base.Objects;

/**
 * Created by Kang Young Won on 2016-06-22.
 */
public class GattObserveData {

    public static final int STATE_ON_NEXT = 1;
    public static final int STATE_ON_START = -1;


    private final BluetoothGattCharacteristic gattCharacteristic;
    private final int state;


    public GattObserveData(BluetoothGattCharacteristic gattCharacteristic, int state) {
        this.gattCharacteristic = gattCharacteristic;
        this.state = state;
    }


    public BluetoothGattCharacteristic getGattCharacteristic() {
        return gattCharacteristic;
    }


    public byte[] getValues() {
        return gattCharacteristic.getValue();
    }


    public int getState() {
        return state;
    }


    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GattObserveData that = (GattObserveData) o;

        return state == that.state && (gattCharacteristic != null ? gattCharacteristic.equals(that.gattCharacteristic) : that.gattCharacteristic == null);
    }


    @Override public int hashCode() {
        return Objects.hashCode(gattCharacteristic);
    }
}