package com.rainbow.kam.ble_gatt_manager.helper;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import rx.Observable;
import rx.functions.Action0;


/**
 * Created by kam6512 on 2015-11-20.
 */
public class BluetoothHelper {

    private static final boolean IS_BLE_SUPPORTED = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    private static final int REQUEST_ENABLE_BLE = 1;
    private static final int RESULT_OK = -1;

    private static final int PERMISSION_GRANTED = PackageManager.PERMISSION_GRANTED;

    private static final String PERMISSION_COARSE = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String PERMISSION_FINE = Manifest.permission.ACCESS_FINE_LOCATION;


    public boolean isBleSupported() {
        return IS_BLE_SUPPORTED;
    }


    @TargetApi(Build.VERSION_CODES.M)
    public Action0 requestBluetoothPermission(Activity activity) {
        return () -> {
            if (ContextCompat.checkSelfPermission(activity, PERMISSION_COARSE) != PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(activity, PERMISSION_FINE) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{PERMISSION_COARSE, PERMISSION_FINE}, REQUEST_ENABLE_BLE);
            }
        };
    }


    @TargetApi(Build.VERSION_CODES.M)
    public Observable<Boolean> onRequestPermissionsResult(int requestCode, int[] grantResults) {
        return Observable.create((Observable.OnSubscribe<Boolean>) subscriber -> {
            if (requestCode == BluetoothHelper.REQUEST_ENABLE_BLE && grantResults.length != 0) {
                if (grantResults[0] == PERMISSION_GRANTED || grantResults[1] == PERMISSION_GRANTED) {
                    subscriber.onNext(true);
                } else {
                    subscriber.onNext(false);
                    subscriber.onCompleted();
                }
            } else {
                subscriber.onNext(false);
                subscriber.onCompleted();
            }
        });
    }


    public Action0 requestBluetoothEnable(Activity activity) {
        return () -> {
            BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter adapter = bluetoothManager.getAdapter();
            if (adapter != null && !adapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ActivityCompat.startActivityForResult(activity, intent, REQUEST_ENABLE_BLE, null);
            }
        };
    }


    public Observable<Boolean> onRequestEnableResult(int requestCode, int resultCode) {
        return Observable.create((Observable.OnSubscribe<Boolean>) subscriber -> {
            if (requestCode == REQUEST_ENABLE_BLE && resultCode == RESULT_OK) {
                subscriber.onNext(true);
            } else {
                subscriber.onNext(false);
                subscriber.onCompleted();
            }
        });
    }
}
