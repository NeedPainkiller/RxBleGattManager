package com.rainbow.kam.ble_gatt_manager.exceptions.gatt;

import com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattException;

/**
 * Created by Kang Young Won on 2016-05-12.
 */
public class GattRssiException extends GattException {
    private final int state;


    public GattRssiException(int state) {
        this.state = state;
    }


    @Override public String toString() {
        return "GattWriteCharacteristicException{" +
                ", state=" + state +
                '}';
    }
}
