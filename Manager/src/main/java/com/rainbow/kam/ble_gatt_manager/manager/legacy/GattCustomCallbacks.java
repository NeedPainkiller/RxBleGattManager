package com.rainbow.kam.ble_gatt_manager.manager.legacy;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattException;

/**
 * Created by kam6512 on 2015-10-29.
 */
public interface GattCustomCallbacks {

    void onDeviceConnected();

    void onDeviceDisconnected();

    void onServicesFound(final BluetoothGatt bluetoothGatt);

    void onDeviceReady();

    void onDeviceNotify(final BluetoothGattCharacteristic ch);

    void onSetNotificationSuccess();

    void onReadSuccess(final BluetoothGattCharacteristic ch);

    void onWriteSuccess();

    void onRSSIUpdate(final int rssi);

    void onError(GattException e);

    class GattCallbacks implements GattCustomCallbacks {

        private static final String TAG = GattCallbacks.class.getSimpleName();


        @Override public void onDeviceConnected() {
        }


        @Override public void onDeviceDisconnected() {
        }


        @Override public void onServicesFound(BluetoothGatt bluetoothGatt) {
        }


        @Override public void onDeviceReady() {
        }


        @Override public void onDeviceNotify(BluetoothGattCharacteristic ch) {
        }


        @Override public void onSetNotificationSuccess() {
        }


        @Override public void onReadSuccess(BluetoothGattCharacteristic ch) {
        }


        @Override public void onWriteSuccess() {
        }


        @Override public void onRSSIUpdate(int rssi) {
        }


        @Override public void onError(GattException e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
