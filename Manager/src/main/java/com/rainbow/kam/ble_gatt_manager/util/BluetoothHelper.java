package com.rainbow.kam.ble_gatt_manager.util;

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

import com.rainbow.kam.ble_gatt_manager.R;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;


/**
 * Created by kam6512 on 2015-11-20.
 */
public class BluetoothHelper {

    public static final boolean IS_BLE_SUPPORTED = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    private static final int REQUEST_ENABLE_BLE = 1;
    private static final int RESULT_OK = -1;

    private static final int PERMISSION_GRANTED = PackageManager.PERMISSION_GRANTED;
    private static final String PERMISSION_COARSE = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String PERMISSION_FINE = Manifest.permission.ACCESS_FINE_LOCATION;


    @TargetApi(Build.VERSION_CODES.M)
    public Action0 requestBluetoothPermission(Activity activity) {
        return () -> {
            if (ContextCompat.checkSelfPermission(activity, PERMISSION_COARSE) != PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(activity, PERMISSION_FINE) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{PERMISSION_COARSE, PERMISSION_FINE}, REQUEST_ENABLE_BLE);
            }
        };
    }


    public Action0 requestBluetoothEnable(Activity activity) {
        return () -> {
            BluetoothAdapter adapter = ((BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
            if (adapter != null && !adapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ActivityCompat.startActivityForResult(activity, intent, REQUEST_ENABLE_BLE, null);
            }
        };
    }


    public Observable<Integer> onRequestPermissionsResult(int requestCode, int[] grantResults) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override public void call(Subscriber<? super Integer> subscriber) {
                if (requestCode == BluetoothHelper.REQUEST_ENABLE_BLE && grantResults.length != 0) {
                    if (grantResults[0] == PERMISSION_GRANTED || grantResults[1] == PERMISSION_GRANTED) {
                        subscriber.onNext(R.string.permission_thanks);
                    } else {
                        subscriber.onNext(R.string.permission_request);
                        subscriber.onCompleted();
                    }
                } else {
                    subscriber.onNext(R.string.permission_denial);
                }
            }
        });
    }


    public Observable<Integer> onRequestEnableResult(int requestCode, int resultCode) {
        return Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
            if (requestCode == REQUEST_ENABLE_BLE && resultCode == RESULT_OK) {
                subscriber.onNext(R.string.bluetooth_on);
            } else {
                subscriber.onNext(R.string.bluetooth_not_init);
                subscriber.onCompleted();
            }
        });
    }

   /* private static WeakReference<Activity> reference;


    @TargetApi(Build.VERSION_CODES.M)
    public static void requestBluetoothPermission(Activity activity) {
        reference = new WeakReference<>(activity);
        if (checkPermission(PERMISSION_COARSE) != PERMISSION_GRANTED || checkPermission(PERMISSION_FINE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(reference.get(), new String[]{PERMISSION_COARSE, PERMISSION_FINE}, REQUEST_ENABLE_BLE);
        }
        reference.clear();
    }


    private static int checkPermission(String permissionTag) {
        return ContextCompat.checkSelfPermission(reference.get(), permissionTag);
    }


    public static void requestBluetoothEnable(Activity activity) {
        reference = new WeakReference<>(activity);
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        ActivityCompat.startActivityForResult(reference.get(), intent, REQUEST_ENABLE_BLE, null);
        reference.clear();
    }


    public static void onRequestPermissionsResult(int requestCode, int[] grantResults, Activity activity) {
        reference = new WeakReference<>(activity);
        if (requestCode == BluetoothHelper.REQUEST_ENABLE_BLE) {
            if (grantResults.length != 0) {
                if (grantResults[0] == PERMISSION_GRANTED || grantResults[1] == PERMISSION_GRANTED) {
                    Toast.makeText(reference.get(), R.string.permission_thanks, Toast.LENGTH_SHORT).show();
                } else {
                    Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + reference.get().getPackageName()));
                    myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                    myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ActivityCompat.startActivityForResult(reference.get(), myAppSettings, 0, null);

                    Toast.makeText(reference.get(), R.string.permission_request, Toast.LENGTH_SHORT).show();
                    reference.get().finish();
                }
            }
        } else {
            Toast.makeText(reference.get(), R.string.permission_denial, Toast.LENGTH_SHORT).show();
        }
        reference.clear();
    }


    public static void onRequestEnableResult(int requestCode, int resultCode, Activity activity) {
        reference = new WeakReference<>(activity);
        if (requestCode == REQUEST_ENABLE_BLE && resultCode == RESULT_OK) {
            Toast.makeText(reference.get(), R.string.bluetooth_on, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(reference.get(), R.string.bluetooth_not_init, Toast.LENGTH_SHORT).show();
            reference.get().finish();
        }
        reference.clear();
    }*/

}
