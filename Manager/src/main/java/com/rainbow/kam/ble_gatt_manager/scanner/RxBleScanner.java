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
import rx.Subscriber;
import rx.android.MainThreadSubscription;

import static com.rainbow.kam.ble_gatt_manager.exceptions.scan.ScanException.STATUS_BLE_NOT_ENABLED;
import static com.rainbow.kam.ble_gatt_manager.exceptions.scan.ScanException.STATUS_BLE_NOT_SUPPORTED;

/**
 * Created by Kang Young Won on 2016-05-24.
 */
public class RxBleScanner {

    @Inject public RxBleScanner() {
    }


    public Observable<BleDevice> observeScan() {
        return Observable.create(new RxBleScannerOnSubscribe()).onBackpressureBuffer();
    }


    private class RxBleScannerOnSubscribe implements Observable.OnSubscribe<BleDevice> {
        private BluetoothAdapter bluetoothAdapter;
        private BluetoothLeScanner scanner;


        @Override public void call(Subscriber<? super BleDevice> subscriber) {
            setAdapter();
            setScanner();

            if (!isBleSupported()) {
                subscriber.onError(new ScanException(STATUS_BLE_NOT_SUPPORTED));
            }
            if (!isBleEnabled()) {
                subscriber.onError(new ScanException(STATUS_BLE_NOT_ENABLED));
            }

            final ScanCallback callback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    BleDevice discoveredDevice = BleDevice.create(result);
                    subscriber.onNext(discoveredDevice);
                }
            };

            subscriber.add(new MainThreadSubscription() {
                @Override protected void onUnsubscribe() {
                    if (scanner != null && isBleSupported() && isBleEnabled()) {
                        scanner.stopScan(callback);
                    }
                }
            });
            scanner.stopScan(callback);
            scanner.startScan(callback);
        }


        private void setAdapter() {
            if (bluetoothAdapter == null) {
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            }
        }


        private void setScanner() {
            if (scanner == null) {
                scanner = bluetoothAdapter.getBluetoothLeScanner();
            }
        }


        private boolean isBleSupported() {
            return BluetoothHelper.IS_BLE_SUPPORTED;
        }


        private boolean isBleEnabled() {
            return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
        }
    }
}