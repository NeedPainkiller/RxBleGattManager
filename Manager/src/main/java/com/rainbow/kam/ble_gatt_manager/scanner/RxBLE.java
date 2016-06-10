package com.rainbow.kam.ble_gatt_manager.scanner;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;

import com.rainbow.kam.ble_gatt_manager.data.BleDevice;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by Kang Young Won on 2016-05-24.
 */
public class RxBle {

    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner scanner;

    private PublishSubject<BleDevice> scanSubject = PublishSubject.create();

    private final ScanCallback callback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            scanSubject.onNext(BleDevice.create(result));
        }
    };


    @Inject public RxBle(final Activity activity) {
        this(activity.getApplication());
    }


    public RxBle(final Application application) {
        BluetoothManager manager = (BluetoothManager) application.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = manager.getAdapter();
        scanner = bluetoothAdapter.getBluetoothLeScanner();
    }


    public Observable<BleDevice> observe() {
        if (!bluetoothAdapter.isEnabled()) {
            return Observable.empty();
        }
        return Observable.merge(Observable.create((Observable.OnSubscribe<BleDevice>) subscriber -> {
            if (scanner == null) {
                scanner = bluetoothAdapter.getBluetoothLeScanner();
            }
            scanner.startScan(callback);
        }), scanSubject).onBackpressureBuffer().doOnSubscribe(() -> {
            if (scanner != null && bluetoothAdapter.isEnabled()) {
                scanner.stopScan(callback);
            }
        });
    }
}


