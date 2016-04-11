package com.rainbow.kam.ble_gatt_manager;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;


/**
 * Created by kam6512 on 2015-11-20.
 */
public class BluetoothHelper {
    public static final boolean IS_BUILD_VERSION_LM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int RESULT_OK = -1;


    @TargetApi(Build.VERSION_CODES.M)
    public static void requestBluetoothPermission(Activity activity) {
        if (activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ENABLE_BT);
        }
    }


    public static void requestBluetoothEnable(Activity activity) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, REQUEST_ENABLE_BT);
    }


    public static void onRequestPermissionsResult(int requestCode, int[] grantResults, Activity activity) {
        if (requestCode == BluetoothHelper.REQUEST_ENABLE_BT) {
            if (grantResults.length != 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(activity, R.string.permission_thanks, Toast.LENGTH_SHORT).show();
                } else {
                    Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + activity.getPackageName()));
                    myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                    myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivityForResult(myAppSettings, 0);

                    Toast.makeText(activity, R.string.permission_request, Toast.LENGTH_SHORT).show();
                    activity.finish();
                }
            }
        } else {
            Toast.makeText(activity, R.string.permission_denial, Toast.LENGTH_SHORT).show();
        }
    }


    public static void onRequestEnableResult(int requestCode, int resultCode, Activity activity) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            Toast.makeText(activity, R.string.bluetooth_on, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, R.string.bluetooth_not_init, Toast.LENGTH_SHORT).show();
            activity.finish();
        }
    }
}
