package com.rainbow.kam.ble_gatt_manager.exceptions.gatt;

/**
 * Created by Kang Young Won on 2016-05-12.
 */
public class GattDisconnectException extends GattException {
    private final String macAddress;


    public GattDisconnectException(String macAddress, String subMessage) {
        super(subMessage);
        this.macAddress = macAddress;
    }


    public GattDisconnectException(String subMessage) {
        super(subMessage);
        this.macAddress = "?";
    }


    @Override public String toString() {
        return "GattDisconnectException{ " +
                "macAddress -> '" + macAddress + '\'' +
                "subMessage -> '" + getMessage() + '\'' +
                '}';
    }
}
