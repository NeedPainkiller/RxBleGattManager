package com.rainbow.kam.ble_gatt_manager.exceptions.gatt;

import com.google.common.base.MoreObjects;

/**
 * Created by Kang Young Won on 2016-05-12.
 */
public class GattConnectException extends GattException {

    public static final String NOT_CONNECTED = "if Ble Device not connected, you can not observe any operations";
    public static final String NONE_APPLICATION = "Application is not available";
    public static final String NONE_BLE_DEVICE = "BleDevice is not available";
    public static final String NONE_ADDRESS = "Address is not available";


    private final String macAddress;


    public GattConnectException(String macAddress, String subMessage) {
        super(subMessage);
        this.macAddress = macAddress;
    }


    public GattConnectException(String subMessage) {
        this(UNKNOWN, subMessage);
    }


    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("MacAddress", macAddress)
                .add("Message", getMessage())
                .toString();
    }
}