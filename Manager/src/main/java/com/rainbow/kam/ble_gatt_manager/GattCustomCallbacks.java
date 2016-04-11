package com.rainbow.kam.ble_gatt_manager;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

/**
 * Created by kam6512 on 2015-10-29.
 */
public interface GattCustomCallbacks {

    void onDeviceConnected();

    void onDeviceConnectFail(Exception e);

    void onDeviceDisconnected();

    void onDeviceDisconnectFail(Exception e);

    void onServicesFound(final BluetoothGatt bluetoothGatt);

    void onServicesNotFound(Exception e);

    void onDeviceReady();

    void onDeviceNotify(final BluetoothGattCharacteristic ch);

    void onSetNotificationSuccess();

    void onSetNotificationFail(Exception e);

    void onReadSuccess(final BluetoothGattCharacteristic ch);

    void onReadFail(Exception e);

    void onWriteSuccess();

    void onWriteFail(Exception e);

    void onRSSIUpdate(final int rssi);

    void onRSSIMiss();

    class GattCallbacks implements GattCustomCallbacks {

        private static final String TAG = "GattCallbacks";


        @Override public void onDeviceConnected() {

        }


        @Override public void onDeviceConnectFail(Exception e) {
            Log.e(TAG, "onDeviceConnectFail : " + e.getMessage());
        }


        @Override public void onDeviceDisconnected() {

        }


        @Override public void onDeviceDisconnectFail(Exception e) {
            Log.e(TAG, "onDeviceDisconnectFail : " + e.getMessage());
        }


        @Override public void onServicesFound(BluetoothGatt bluetoothGatt) {

        }


        @Override public void onServicesNotFound(Exception e) {
            Log.e(TAG, "onServicesNotFound : " + e.getMessage());
        }


        @Override public void onDeviceReady() {

        }


        @Override public void onDeviceNotify(BluetoothGattCharacteristic ch) {

        }


        @Override public void onSetNotificationSuccess() {

        }


        @Override public void onSetNotificationFail(Exception e) {
            Log.e(TAG, "onSetNotificationFail : " + e.getMessage());
        }


        @Override public void onReadSuccess(BluetoothGattCharacteristic ch) {

        }


        @Override public void onReadFail(Exception e) {
            Log.e(TAG, "onReadFail : " + e.getMessage());
        }


        @Override public void onWriteSuccess() {

        }


        @Override public void onWriteFail(Exception e) {
            Log.e(TAG, "onWriteFail : " + e.getMessage());
        }


        @Override public void onRSSIUpdate(int rssi) {

        }


        @Override public void onRSSIMiss() {

        }
    }
}
