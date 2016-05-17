package com.rainbow.kam.ble_gatt_manager.exceptions;

/**
 * Created by Kang Young Won on 2016-05-12.
 */
public class GattException extends RuntimeException {
    public static final int STATUS_UNKNOWN = -1;


    public GattException() {
    }


    public GattException(String detailMessage) {
        super(detailMessage);
    }
}
