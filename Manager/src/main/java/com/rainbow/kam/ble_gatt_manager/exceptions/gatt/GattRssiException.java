package com.rainbow.kam.ble_gatt_manager.exceptions.gatt;

import com.google.common.base.MoreObjects;

/**
 * Created by Kang Young Won on 2016-05-12.
 */
public class GattRssiException extends GattException {
    private final int state;


    public GattRssiException(int state) {
        this.state = state;
    }


    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("state", state).toString();
    }
}
