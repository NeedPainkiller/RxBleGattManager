package com.rainbow.kam.ble_gatt_manager.legacy.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.rainbow.kam.ble_gatt_manager.model.BleDevice;

import java.util.UUID;

import io.realm.RealmObject;

/**
 * Created by Kang Young Won on 2016-07-01.
 */
public class GattRecodeModel extends RealmObject {
    private String deviceName;
    private String deviceAddress;

    private String gattOperationType;
    private String gattOperationUUID;
    private String gattState;


    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }


    public void setDeviceAddress(BleDevice bleDevice) {
        deviceAddress = bleDevice.getAddress();
    }


    public void setDeviceName(String deviceName) {

        this.deviceName = deviceName;
    }


    public void setDeviceName(BleDevice bleDevice) {

        deviceName = bleDevice.getName();
    }


    public void setGattOperationType(String gattOperationType) {
        this.gattOperationType = gattOperationType;
    }


    public void setGattOperationUUID(String gattOperationUUID) {
        this.gattOperationUUID = gattOperationUUID;
    }


    public void setGattOperationUUID(UUID gattOperationUUID) {
        this.gattOperationUUID = gattOperationUUID.toString();
    }


    public void setGattState(String gattState) {
        this.gattState = gattState;
    }


    public String getDeviceName() {
        return deviceName;
    }


    public String getDeviceAddress() {
        return deviceAddress;
    }


    public String getGattOperationType() {
        return gattOperationType;
    }


    public String getGattOperationUUID() {
        return gattOperationUUID;
    }


    public String getGattState() {
        return gattState;
    }


    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GattRecodeModel that = (GattRecodeModel) o;
        return Objects.equal(this.deviceName, that.deviceName)
                || Objects.equal(this.deviceAddress, that.deviceAddress)
                || Objects.equal(this.gattOperationType, that.gattOperationType)
                || Objects.equal(this.gattOperationUUID, that.gattOperationUUID)
                || Objects.equal(this.gattState, that.gattState);
    }


    @Override public int hashCode() {
        return Objects.hashCode(deviceName, deviceAddress, gattOperationType, gattOperationUUID, gattState);
    }


    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("deviceName", deviceName)
                .add("deviceAddress", deviceAddress)
                .add("gattOperationType", gattOperationType)
                .add("gattOperationUUID", gattOperationUUID)
                .add("gattState", gattState).toString();
    }
}