package com.rainbow.kam.ble_gatt_manager.exceptions.gatt.details;

import com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattException;

/**
 * Created by Kang Young Won on 2016-05-12.
 */
public class RssiMissException extends GattException {
    private final int state;


    public RssiMissException(int state) {
        this.state = state;
    }


    @Override public String toString() {
        return "WriteCharacteristicException{" +
                ", state=" + state +
                '}';
    }
}
