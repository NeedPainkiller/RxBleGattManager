package com.rainbow.kam.ble_gatt_manager.model.realm;

import com.google.common.base.MoreObjects;
import com.rainbow.kam.ble_gatt_manager.model.BleDevice;

import java.util.UUID;

import io.realm.RealmObject;

/**
 * Created by Kang Young Won on 2016-07-01.
 */
public class GattRecodeModel extends RealmObject {
    private String DeviceName;
    private String DeviceAddress;

    private String gattOperationType;
    private String gattOperationUUID;
    private String gattState;


    /*public GattRecodeModel(BleDevice bleDevice) {
        DeviceName = bleDevice.getName();
        DeviceAddress = bleDevice.getAddress();
    }*/


    public void setDeviceAddress(String deviceAddress) {
        DeviceAddress = deviceAddress;
    }


    public void setDeviceAddress(BleDevice bleDevice) {
        DeviceAddress = bleDevice.getAddress();
    }


    public void setDeviceName(String deviceName) {

        DeviceName = deviceName;
    }


    public void setDeviceName(BleDevice bleDevice) {

        DeviceName = bleDevice.getName();
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
        return DeviceName;
    }


    public String getDeviceAddress() {
        return DeviceAddress;
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


    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("DeviceName", DeviceName)
                .add("DeviceAddress", DeviceAddress)
                .add("gattOperationType", gattOperationType)
                .add("gattOperationUUID", gattOperationUUID)
                .add("gattState", gattState).toString();
    }
}