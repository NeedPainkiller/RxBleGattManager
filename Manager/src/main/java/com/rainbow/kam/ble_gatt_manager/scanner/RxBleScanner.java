package com.rainbow.kam.ble_gatt_manager.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;

import com.rainbow.kam.ble_gatt_manager.exceptions.scan.ScanException;
import com.rainbow.kam.ble_gatt_manager.helper.BluetoothHelper;
import com.rainbow.kam.ble_gatt_manager.model.BleDevice;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

import static com.rainbow.kam.ble_gatt_manager.exceptions.scan.ScanException.*;

/**
 * Created by Kang Young Won on 2016-05-24.
 */
public class RxBleScanner implements IRxBleScanner {

    private final BluetoothAdapter bluetoothAdapter;
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


    @Override public Observable<BleDevice> observeScan() {
        return Observable.merge(scanSubject = PublishSubject.create(),
                Observable.create((Observable.OnSubscribe<BleDevice>) subscriber -> {
                    if (!isBleSupported()) {
                        subscriber.onError(new ScanException(STATUS_BLE_NOT_SUPPORTED));
                    }
                    if (!isBleEnabled()) {
                        subscriber.onError(new ScanException(STATUS_BLE_NOT_ENABLED));
                    }
                    setScanner();
                    scanner.startScan(callback);
                }))
                .onBackpressureBuffer()
                .doOnSubscribe(this::stopScan);
    }


    private boolean isBleSupported() {
        return BluetoothHelper.IS_BLE_SUPPORTED;
    }


    private boolean isBleEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }


    private void setScanner() {
        if (scanner == null) {
            scanner = bluetoothAdapter.getBluetoothLeScanner();
        }
    }


    private void stopScan() {
        if (scanner != null && isBleSupported() && isBleEnabled()) {
            scanner.stopScan(callback);
        }
    }

}