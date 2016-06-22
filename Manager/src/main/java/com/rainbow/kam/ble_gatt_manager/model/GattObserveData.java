package com.rainbow.kam.ble_gatt_manager.model;

import android.bluetooth.BluetoothGattCharacteristic;

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


    public int getState() {
        return state;
    }
}
