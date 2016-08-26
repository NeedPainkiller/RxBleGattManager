package com.rainbow.kam.ble_gatt_manager.util;

import static com.rainbow.kam.ble_gatt_manager.BuildConfig.UUID_HEX;
import static com.rainbow.kam.ble_gatt_manager.BuildConfig.UUID_REGEX;

/**
 * Created by Kang Young Won on 2016-06-01.
 */
public class UuidUtils {
    public static String splitUUID(final String uuid) {
        return UUID_HEX + uuid.replaceAll(UUID_REGEX, "").substring(4, 8).toUpperCase();
    }


    @Override public String toString() {
        return "UuidUtils{String splitUUID(String uuid)}";
    }
}
