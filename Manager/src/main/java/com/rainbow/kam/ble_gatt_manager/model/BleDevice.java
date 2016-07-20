package com.rainbow.kam.ble_gatt_manager.model;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.rainbow.kam.ble_gatt_manager.util.BluetoothDevices;
import com.rainbow.kam.ble_gatt_manager.BuildConfig;

/**
 * Created by Kang Young Won on 2016-05-23.
 */
public class BleDevice implements Comparable<BleDevice> {

    private final static String UNKNOWN = BuildConfig.UNKNOWN;

    private final BluetoothDevice device;
    private final int rssi;


    private BleDevice(BluetoothDevice device, int rssi) {
        this.device = device;
        this.rssi = rssi;
    }


    public static BleDevice create(BluetoothDevice device, int rssi) {
        return new BleDevice(device, rssi);
    }


    public static BleDevice create(BluetoothDevice device) {
        return new BleDevice(device, 0);
    }


    public static BleDevice create(ScanResult result) {
        return create(result.getDevice(), result.getRssi());
    }


    public BluetoothDevice getDevice() {
        return device;
    }


    public String getName() {
        String deviceName = device.getName();
        if (Strings.isNullOrEmpty(deviceName)) {
            return UNKNOWN;
        }
        return deviceName;
    }


    public String getAddress() {
        return device.getAddress();
    }


    public String getType() {
        return BluetoothDevices.getType(device.getType());
    }


    public String getBondState() {
        return BluetoothDevices.getBond(device.getBondState());
    }


    public int getRssi() {
        return rssi;
    }


    public String getRssiString() {
        return String.valueOf(rssi);
    }


    @Override public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (object instanceof BleDevice) {
            BleDevice other = (BleDevice) object;
            return Objects.equal(this.getAddress(), other.getAddress());
        } else {
            return false;
        }
    }


    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", getName())
                .add("address", getAddress())
                .add("bondState", getBondState())
                .add("type", getType())
                .add("rssi", rssi).toString();
    }


    @Override public int hashCode() {
        return Objects.hashCode(getAddress());
    }


    @Override public int compareTo(@NonNull final BleDevice anotherDevice) {
        return getAddress().compareTo(anotherDevice.getAddress());
    }
}