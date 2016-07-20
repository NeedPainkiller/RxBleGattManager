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


    @Override public boolean equals(Object object) {

        if (object == this) {
            return true;
        }

        if (object instanceof GattObserveData) {
            GattObserveData other = (GattObserveData) object;
            return Objects.equal(this.gattCharacteristic.getUuid().toString(), other.gattCharacteristic.getUuid().toString())
                    && Objects.equal(this.state, other.state);
        } else {
            return false;
        }
    }


    @Override public int hashCode() {
        return Objects.hashCode(gattCharacteristic) + Objects.hashCode(state);
    }
}