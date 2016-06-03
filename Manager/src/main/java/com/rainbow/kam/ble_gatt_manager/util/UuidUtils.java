package com.rainbow.kam.ble_gatt_manager.util;

import com.rainbow.kam.ble_gatt_manager.BuildConfig;

/**
 * Created by Kang Young Won on 2016-06-01.
 */
public class UuidUtils {


    public static String splitUUID(final String uuid) {
        return BuildConfig.UUID_HEX + uuid.replaceAll(BuildConfig.UUID_REGEX, "").substring(4, 8).toUpperCase();
    }
}
