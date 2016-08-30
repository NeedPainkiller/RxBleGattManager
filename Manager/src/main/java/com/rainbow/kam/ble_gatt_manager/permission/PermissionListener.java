package com.rainbow.kam.ble_gatt_manager.permission;

import java.util.ArrayList;

/**
 * Created by Kang Young Won on 2016-08-29.
 */
public interface PermissionListener {
    void permissionGranted();

    void permissionDenied(ArrayList<String> deniedPermissions);
}
