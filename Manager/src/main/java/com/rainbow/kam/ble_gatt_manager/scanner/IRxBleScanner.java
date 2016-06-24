package com.rainbow.kam.ble_gatt_manager.scanner;

import com.rainbow.kam.ble_gatt_manager.model.BleDevice;

import rx.Observable;

/**
 * Created by Kang Young Won on 2016-06-23.
 */
public interface IRxBleScanner {
    Observable<BleDevice> observeScan();
}
