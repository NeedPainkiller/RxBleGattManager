package com.rainbow.kam.ble_gatt_manager.exceptions.gatt;

/**
 * Created by Kang Young Won on 2016-05-12.
 */
public class GattConnectException extends GattException {

    public static final String NOT_CONNECTED = "if Ble Device not connected, you can not observe any operations";
    public static final String NONE_APPLICATION = "Application is not available";
    public static final String NONE_BLE_DEVICE = "BleDevice is not available";
    public static final String NONE_ADDRESS = "Address is not available";


    private final String macAddress;
    private final String subMessage;


    public GattConnectException(String macAddress, String subMessage) {
        this.macAddress = macAddress;
        this.subMessage = subMessage;
    }


    public GattConnectException(String subMessage) {
        this.macAddress = UNKNOWN;
        this.subMessage = subMessage;
    }


    @Override public String toString() {
        return "GattConnectException{ " +
                "macAddress -> '" + macAddress + '\'' +
                "subMessage -> '" + subMessage + '\'' +
                '}';
    }
}
