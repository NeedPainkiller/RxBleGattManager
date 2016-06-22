package com.rainbow.kam.ble_gatt_manager.util;

import android.util.SparseArray;

import com.rainbow.kam.ble_gatt_manager.BuildConfig;

/**
 * Created by Kang Young Won on 2016-05-20.
 */
public class BluetoothDevices {

    public final static String BOND_STATE_NOT_BONDED = "NOT BONDED";
    public final static String BOND_STATE_BONDING = "BONDING";
    public final static String BOND_STATE_BONDED = "BONDED";
    private final static String BOND_STATE_UNKNOWN = BuildConfig.UNKNOWN;

    public final static String TYPE_CLASSIC = "CLASSIC";
    public final static String TYPE_BLE = "BLE";
    public final static String TYPE_DUAL = "DUAL";
    private final static String TYPE_UNKNOWN = BuildConfig.UNKNOWN;

    private final static SparseArray<String> BOND_LIST = new SparseArray<>();
    private final static SparseArray<String> TYPE_LIST = new SparseArray<>();


    public static String getBond(int bond) {
        return BOND_LIST.get(bond, BOND_STATE_UNKNOWN);
    }


    public static String getType(int type) {
        return TYPE_LIST.get(type, TYPE_UNKNOWN);
    }


    static {
        BOND_LIST.put(10, BOND_STATE_NOT_BONDED);
        BOND_LIST.put(11, BOND_STATE_BONDING);
        BOND_LIST.put(12, BOND_STATE_BONDED);

        TYPE_LIST.put(0, TYPE_UNKNOWN);
        TYPE_LIST.put(1, TYPE_CLASSIC);
        TYPE_LIST.put(2, TYPE_BLE);
        TYPE_LIST.put(3, TYPE_DUAL);
    }
}
