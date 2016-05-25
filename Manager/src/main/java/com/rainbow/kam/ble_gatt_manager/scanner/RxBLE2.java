package com.rainbow.kam.ble_gatt_manager.scanner;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;

import com.rainbow.kam.ble_gatt_manager.util.BluetoothHelper;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by Kang Young Won on 2016-05-24.
 */
public class RxBLE2 {

    private final BluetoothAdapter bluetoothAdapter;
    private final BluetoothLeScanner scanner;

    private Subscriber<? super BleDevice> subscriber;
    private final ScanCallback callback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            subscriber.onNext(BleDevice.create(result));
        }
    };


    public RxBLE2(Activity activity) {
        BluetoothManager manager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = manager.getAdapter();
        scanner = bluetoothAdapter.getBluetoothLeScanner();
    }


    public Observable<BleDevice> observe() {
        if (!BluetoothHelper.IS_BLE_SUPPORTED || !bluetoothAdapter.isEnabled()) {
            return Observable.empty();
        }

        return Observable.create(new Observable.OnSubscribe<BleDevice>() {
            @Override
            public void call(Subscriber<? super BleDevice> subscriber) {
                RxBLE2.this.subscriber = subscriber;
                scanner.startScan(callback);
            }
        }).distinctUntilChanged().doOnSubscribe(() -> {
            if (bluetoothAdapter.isEnabled()) {
                scanner.stopScan(callback);
            }
        });
    }
}


