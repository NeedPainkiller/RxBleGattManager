package com.rainbow.kam.ble_gatt_manager.exceptions.gatt.details;

import com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattException;

/**
 * Created by Kang Young Won on 2016-05-12.
 */
public class ConnectedFailException extends GattException {
    private final String macAddress;
    private final String subMessage;


    public ConnectedFailException(String macAddress, String subMessage) {
        this.macAddress = macAddress;
        this.subMessage = subMessage;
    }


    @Override public String toString() {
        return "ConnectedFailException{ " +
                "macAddress -> '" + macAddress + '\'' +
                "subMessage -> '" + subMessage + '\'' +
                '}';
    }
}
