package com.rainbow.kam.ble_gatt_manager.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import com.rainbow.kam.ble_gatt_manager.model.BleDevice;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by Kang Young Won on 2016-05-24.
 */
public class RxBleScanner {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner scanner;
    private PublishSubject<BleDevice> scanSubject;


    @Inject public RxBleScanner() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    private final ScanCallback callback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BleDevice discoveredDevice = BleDevice.create(result);
            scanSubject.onNext(discoveredDevice);
        }
    };


    private boolean isScanAvailable() {
        return !(bluetoothAdapter == null || !bluetoothAdapter.isEnabled());
    }


    public Observable<BleDevice> observeScan() {
        if (!isScanAvailable()) {
            return Observable.empty();
        }

        return Observable.merge(
                scanSubject = PublishSubject.create(),
                Observable.create((Observable.OnSubscribe<BleDevice>) subscriber -> {
                    if (isScanAvailable() && scanner == null) {
                        scanner = bluetoothAdapter.getBluetoothLeScanner();
                    }
                    try {
                        scanner.startScan(callback);
                    } catch (Exception e) {
                        Log.e("Scanner",e.getMessage());
                    }


                }))
                .onBackpressureBuffer()
                .doOnSubscribe(() -> {
                    if (scanner != null && isScanAvailable()) {
                        scanner.stopScan(callback);
                    }
                });
    }
}


