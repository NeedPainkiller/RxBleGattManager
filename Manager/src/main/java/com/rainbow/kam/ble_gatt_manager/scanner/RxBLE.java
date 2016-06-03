package com.rainbow.kam.ble_gatt_manager.scanner;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;

import com.rainbow.kam.ble_gatt_manager.data.BleDevice;
import com.rainbow.kam.ble_gatt_manager.helper.BluetoothHelper;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import rx.Observable;

/**
 * Created by Kang Young Won on 2016-05-23.
 */
public class RxBle {

    private BluetoothAdapter bluetoothAdapter;


    public RxBle(Activity activity) {
        String bluetoothService = Context.BLUETOOTH_SERVICE;
        BluetoothManager manager = (BluetoothManager) activity.getSystemService(bluetoothService);
        bluetoothAdapter = manager.getAdapter();
    }


    public Observable<BleDevice> observe() {
        if (!BluetoothHelper.IS_BLE_SUPPORTED || !bluetoothAdapter.isEnabled()) {
            return Observable.empty();
        }

        return new Scanner(bluetoothAdapter).observe();
    }


    class Scanner {

        private final BluetoothLeScanner scanner;
        private final ScanCallbackAdapter callback;


        public Scanner(final BluetoothAdapter adapter) {
            this.scanner = adapter.getBluetoothLeScanner();
            this.callback = new ScanCallbackAdapter();
        }


        public Observable<BleDevice> observe() {
//            scanner.startScan(callback);
//            return callback.toObservable().repeat().distinctUntilChanged().doOnSubscribe(() -> scanner.startScan(callback)).doOnUnsubscribe(() -> scanner.startScan(callback));
            return callback.toObservable().repeat().distinctUntilChanged().doOnSubscribe(() -> scanner.startScan(callback)).doOnUnsubscribe(() -> scanner.stopScan(callback));
        }

    }

    class ScanCallbackAdapter extends ScanCallback {
        private final FutureAdapter futureAdapter = new FutureAdapter();


        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            futureAdapter.setDevice(BleDevice.create(result));
        }


        public Observable<BleDevice> toObservable() {
            return Observable.from(futureAdapter);
        }
    }

    class FutureAdapter implements Future<BleDevice> {
        private boolean done;
        private BleDevice bleDevice;


        public void setDevice(BleDevice bleDevice) {
            synchronized (this) {
                this.bleDevice = bleDevice;
                this.done = true;
                this.notify();
            }
        }


        @Override public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }


        @Override public boolean isCancelled() {
            return false;
        }


        @Override public boolean isDone() {
            return done;
        }


        @Override public BleDevice get() throws InterruptedException {
            synchronized (this) {
                while (bleDevice == null) {
                    this.wait();
                }
            }
            return bleDevice;
        }


        @Override
        public BleDevice get(long t, TimeUnit u) throws InterruptedException {
            return get();
        }
    }

}


