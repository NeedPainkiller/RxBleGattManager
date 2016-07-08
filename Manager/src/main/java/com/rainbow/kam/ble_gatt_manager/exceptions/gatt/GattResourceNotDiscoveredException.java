package com.rainbow.kam.ble_gatt_manager.exceptions.gatt;

/**
 * Created by Kang Young Won on 2016-05-12.
 */
public class GattResourceNotDiscoveredException extends GattException {
    public static final String NONE_UUID_CHARACTERISTIC = "CHECK UUID / CHARACTERISTIC";
    public static final String NONE_SERVICES = "ServicesDiscovered FAIL";
    public GattResourceNotDiscoveredException(String detailMessage) {
        super(detailMessage);
    }
}
