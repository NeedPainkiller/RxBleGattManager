package com.rainbow.kam.ble_gatt_manager.exceptions.gatt;

import com.google.common.base.MoreObjects;

/**
 * Created by Kang Young Won on 2016-05-12.
 */
public class GattException extends Throwable {
    public static final int STATE_UNKNOWN = 9999;
    public static final String STATUS_RESULT_FAIL = "Check Gatt Service Available or Connection!";
    public static final String UNKNOWN = "UNKNOWN";
    public static final String NONE_BT = "GATT / BLE Power or Permission is not available or disabled";


    public GattException() {
        super(UNKNOWN);
    }


    public GattException(String detailMessage) {
        super(detailMessage);
    }


    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Message", getMessage()).toString();
    }
}