package com.orca.kam.kotlin.util

import android.util.SparseArray
import com.orca.kam.kotlin.BuildConfig

/**
 * Created by Kang Young Won on 2016-07-19.
 */
class BluetoothDevices {

    override fun toString(): String {
        return "BluetoothDevices()"
    }

    companion object {
        val BOND_STATE_NOT_BONDED: String = "NOT BONDED"
        val BOND_STATE_BONDING: String = "BONDING"
        val BOND_STATE_BONDED: String = "BONDED"
        private val BOND_STATE_UNKNOWN: String = BuildConfig.UNKNOWN

        val TYPE_CLASSIC: String = "CLASSIC"
        val TYPE_BLE: String = "BLE"
        val TYPE_DUAL: String = "DUAL"
        private val TYPE_UNKNOWN: String = BuildConfig.UNKNOWN

        private val BOND_LIST: SparseArray<String> = SparseArray()
        private val TYPE_LIST: SparseArray<String> = SparseArray()

        fun getBond(bond: Int): String {
            return BOND_LIST.get(bond, BOND_STATE_UNKNOWN)
        }

        fun getType(type: Int): String {
            return TYPE_LIST.get(type, TYPE_UNKNOWN)
        }

        init {
            BOND_LIST.put(10, BOND_STATE_NOT_BONDED)
            BOND_LIST.put(11, BOND_STATE_BONDING)
            BOND_LIST.put(12, BOND_STATE_BONDED)

            TYPE_LIST.put(0, TYPE_UNKNOWN)
            TYPE_LIST.put(1, TYPE_CLASSIC)
            TYPE_LIST.put(2, TYPE_BLE)
            TYPE_LIST.put(3, TYPE_DUAL)
        }
    }
}