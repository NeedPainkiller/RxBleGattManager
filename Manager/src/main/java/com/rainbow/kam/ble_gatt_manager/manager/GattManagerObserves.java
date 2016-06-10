package com.rainbow.kam.ble_gatt_manager.manager;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.rainbow.kam.ble_gatt_manager.data.BleDevice;

import java.util.List;
import java.util.UUID;

import rx.Observable;

/**
 * Created by Kang Young Won on 2016-06-10.
 */
public interface GattManagerObserves {

    Observable<Boolean> observeConnection();

    Observable<Boolean> observeConnection(BleDevice bleDevice);

    Observable<Integer> observeRssi();

    Observable<List<BluetoothGattService>> observeDiscoverService();

    Observable<BluetoothGattCharacteristic> observeBattery();

    Observable<BluetoothGattCharacteristic> observeRead(final UUID uuidToRead);

    Observable<BluetoothGattCharacteristic> observeRead(final BluetoothGattCharacteristic characteristicToRead);

    Observable<BluetoothGattCharacteristic> observeWrite(final UUID uuidToWrite, final List<Byte> valuesToWrite);

    Observable<BluetoothGattCharacteristic> observeWrite(final UUID uuidToWrite, final byte[] valuesToWrite);

    Observable<BluetoothGattCharacteristic> observeWrite(final BluetoothGattCharacteristic characteristicToWrite,
                                                         final List<Byte> valuesToWrite);

    Observable<BluetoothGattCharacteristic> observeWrite(final BluetoothGattCharacteristic characteristicToWrite,
                                                         final byte[] valuesToWrite);

    Observable<BluetoothGattCharacteristic> observeNotification(
            final UUID uuidToNotification, boolean enableNotification);

    Observable<BluetoothGattCharacteristic> observeNotification(
            final BluetoothGattCharacteristic characteristicToNotification, boolean enableNotification);
}
