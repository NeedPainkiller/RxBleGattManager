package com.rainbow.kam.ble_gatt_manager.exceptions.scan;

/**
 * Created by Kang Young Won on 2016-06-23.
 */
public class ScanException extends RuntimeException {
    public static final String STATUS_BLE_NOT_SUPPORTED = "STATUS_BLE_NOT_SUPPORTED";
    public static final String STATUS_BLE_NOT_ENABLED = "STATUS_BLE_NOT_ENABLED";
    public static final String STATUS_UNKNOWN = "STATUS_UNKNOWN";


    public ScanException() {
        super(STATUS_UNKNOWN);
    }


    public ScanException(String detailMessage) {
        super(detailMessage);
    }
}
