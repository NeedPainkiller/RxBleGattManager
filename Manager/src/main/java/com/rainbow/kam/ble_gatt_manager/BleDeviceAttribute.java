package com.rainbow.kam.ble_gatt_manager;

import android.util.SparseArray;

/**
 * Created by Kang Young Won on 2016-05-20.
 */
public class BleDeviceAttribute {

    private final static String UNKNOWN = BuildConfig.UNKNOWN;

    private final static SparseArray<String> BOND_LIST = new SparseArray<>();
    private final static SparseArray<String> TYPE_LIST = new SparseArray<>();


    public static String getBond(int bond) {
        return BOND_LIST.get(bond, UNKNOWN);
    }


    public static String getType(int type) {
        return TYPE_LIST.get(type, UNKNOWN);
    }


    static {
        BOND_LIST.put(10, "NOT BONDED");
        BOND_LIST.put(11, "BONDING");
        BOND_LIST.put(12, "BONDED");

        TYPE_LIST.put(0, UNKNOWN);
        TYPE_LIST.put(1, "CLASSIC");
        TYPE_LIST.put(2, "BLE");
        TYPE_LIST.put(3, "DUAL");
    }
}
