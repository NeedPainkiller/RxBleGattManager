/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rainbow.kam.ble_gatt_manager;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.SparseArray;

import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GattAttributes {

    private final static String UUID_LABEL = "-0000-1000-8000-00805F9B34FB";

    public final static String CLIENT_CHARACTERISTIC_CONFIG = "00002902" + UUID_LABEL;

    public final static String BATTERY_SERVICE_UUID = "0000180F" + UUID_LABEL;
    public final static String BATTERY_CHARACTERISTIC_UUID = "00002A19" + UUID_LABEL;


    private final static Map<String, String> SERVICES = Maps.newHashMap();
    private final static Map<String, String> CHARACTERISTICS = Maps.newHashMap();
    private final static SparseArray<String> VALUE_FORMATS = new SparseArray<>();

    private final static String UNKNOWN = "UNKNOWN";

    private final static List<Integer> FORMAT_LIST;
    public static final SparseArray<String> BOND_LIST = new SparseArray<>();
    public static final SparseArray<String> TYPE_LIST = new SparseArray<>();


    public static String resolveServiceName(final String uuid) {
        String res = SERVICES.get(uuid);
        if (res != null) {
            return res;
        }
        return UNKNOWN;
    }


    public static String resolveCharacteristicName(final String uuid) {
        String res = CHARACTERISTICS.get(uuid);
        if (res != null) {
            return res;
        }
        return UNKNOWN;
    }


    public static String resolveValueTypeDescription(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        Integer format = getValueFormat(bluetoothGattCharacteristic);
        return VALUE_FORMATS.get(format, UNKNOWN);
    }


    private static int getValueFormat(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        int properties = bluetoothGattCharacteristic.getProperties();
        int format;
        int formatListLength = FORMAT_LIST.size();
        for (int i = 0; i < formatListLength; i++) {
            format = FORMAT_LIST.get(i);
            if ((format & properties) != 0) return format;
        }
        return 0;
    }


    static {
        SERVICES.put("00001811" + UUID_LABEL, "Alert Notification Service");
        SERVICES.put(BATTERY_SERVICE_UUID, "Battery Service");
        SERVICES.put("00001810" + UUID_LABEL, "Blood Pressure");
        SERVICES.put("00001805" + UUID_LABEL, "Current Time Service");
        SERVICES.put("00001818" + UUID_LABEL, "Cycling Power");
        SERVICES.put("00001816" + UUID_LABEL, "Cycling Speed and Cadence");
        SERVICES.put("0000180a" + UUID_LABEL, "Device Information");
        SERVICES.put("00001800" + UUID_LABEL, "Generic Access");
        SERVICES.put("00001801" + UUID_LABEL, "Generic Attribute");
        SERVICES.put("00001808" + UUID_LABEL, "Glucose");
        SERVICES.put("00001809" + UUID_LABEL, "Health Thermometer");
        SERVICES.put("0000180d" + UUID_LABEL, "Heart Rate");
        SERVICES.put("00001812" + UUID_LABEL, "Human Interface Device");
        SERVICES.put("00001802" + UUID_LABEL, "Immediate Alert");
        SERVICES.put("00001803" + UUID_LABEL, "Link Loss");
        SERVICES.put("00001819" + UUID_LABEL, "Location and Navigation");
        SERVICES.put("00001807" + UUID_LABEL, "Next DST Change Service");
        SERVICES.put("0000180e" + UUID_LABEL, "Phone Alert Status Service");
        SERVICES.put("00001806" + UUID_LABEL, "Reference Time Update Service");
        SERVICES.put("00001814" + UUID_LABEL, "Running Speed and Cadence");
        SERVICES.put("00001813" + UUID_LABEL, "Scan Parameters");
        SERVICES.put("00001804" + UUID_LABEL, "Tx Power");

        CHARACTERISTICS.put("00002a43" + UUID_LABEL, "Alert Category ID");
        CHARACTERISTICS.put("00002a42" + UUID_LABEL, "Alert Category ID Bit Mask");
        CHARACTERISTICS.put("00002a06" + UUID_LABEL, "Alert Level");
        CHARACTERISTICS.put("00002a44" + UUID_LABEL, "Alert Notification Control Point");
        CHARACTERISTICS.put("00002a3f" + UUID_LABEL, "Alert Status");
        CHARACTERISTICS.put("00002a01" + UUID_LABEL, "Appearance");
        CHARACTERISTICS.put(BATTERY_CHARACTERISTIC_UUID, "Battery Level");
        CHARACTERISTICS.put("00002a49" + UUID_LABEL, "Blood Pressure Feature");
        CHARACTERISTICS.put("00002a35" + UUID_LABEL, "Blood Pressure Measurement");
        CHARACTERISTICS.put("00002a38" + UUID_LABEL, "Body Sensor Location");
        CHARACTERISTICS.put("00002a22" + UUID_LABEL, "Boot Keyboard Input Report");
        CHARACTERISTICS.put("00002a32" + UUID_LABEL, "Boot Keyboard Output Report");
        CHARACTERISTICS.put("00002a33" + UUID_LABEL, "Boot Mouse Input Report");
        CHARACTERISTICS.put("00002a5c" + UUID_LABEL, "CSC Feature");
        CHARACTERISTICS.put("00002a5b" + UUID_LABEL, "CSC Measurement");
        CHARACTERISTICS.put("00002a2b" + UUID_LABEL, "Current Time");
        CHARACTERISTICS.put("00002a66" + UUID_LABEL, "Cycling Power Control Point");
        CHARACTERISTICS.put("00002a65" + UUID_LABEL, "Cycling Power Feature");
        CHARACTERISTICS.put("00002a63" + UUID_LABEL, "Cycling Power Measurement");
        CHARACTERISTICS.put("00002a64" + UUID_LABEL, "Cycling Power Vector");
        CHARACTERISTICS.put("00002a08" + UUID_LABEL, "Date Time");
        CHARACTERISTICS.put("00002a0a" + UUID_LABEL, "Day Date Time");
        CHARACTERISTICS.put("00002a09" + UUID_LABEL, "Day of Week");
        CHARACTERISTICS.put("00002a00" + UUID_LABEL, "Device Name");
        CHARACTERISTICS.put("00002a0d" + UUID_LABEL, "DST Offset");
        CHARACTERISTICS.put("00002a0c" + UUID_LABEL, "Exact Time 256");
        CHARACTERISTICS.put("00002a26" + UUID_LABEL, "Firmware Revision String");
        CHARACTERISTICS.put("00002a51" + UUID_LABEL, "Glucose Feature");
        CHARACTERISTICS.put("00002a18" + UUID_LABEL, "Glucose Measurement");
        CHARACTERISTICS.put("00002a34" + UUID_LABEL, "Glucose Measurement Context");
        CHARACTERISTICS.put("00002a27" + UUID_LABEL, "Hardware Revision String");
        CHARACTERISTICS.put("00002a39" + UUID_LABEL, "Heart Rate Control Point");
        CHARACTERISTICS.put("00002a37" + UUID_LABEL, "Heart Rate Measurement");
        CHARACTERISTICS.put("00002a4c" + UUID_LABEL, "HID Control Point");
        CHARACTERISTICS.put("00002a4a" + UUID_LABEL, "HID Information");
        CHARACTERISTICS.put("00002a2a" + UUID_LABEL, "IEEE 11073-20601 Regulatory Certification Data List");
        CHARACTERISTICS.put("00002a36" + UUID_LABEL, "Intermediate Cuff Pressure");
        CHARACTERISTICS.put("00002a1e" + UUID_LABEL, "Intermediate Temperature");
        CHARACTERISTICS.put("00002a6b" + UUID_LABEL, "LN Control Point");
        CHARACTERISTICS.put("00002a6a" + UUID_LABEL, "LN Feature");
        CHARACTERISTICS.put("00002a0f" + UUID_LABEL, "Local Time Information");
        CHARACTERISTICS.put("00002a67" + UUID_LABEL, "Location and Speed");
        CHARACTERISTICS.put("00002a29" + UUID_LABEL, "Manufacturer Name String");
        CHARACTERISTICS.put("00002a21" + UUID_LABEL, "Measurement Interval");
        CHARACTERISTICS.put("00002a24" + UUID_LABEL, "Model Number String");
        CHARACTERISTICS.put("00002a68" + UUID_LABEL, "Navigation");
        CHARACTERISTICS.put("00002a46" + UUID_LABEL, "New Alert");
        CHARACTERISTICS.put("00002a04" + UUID_LABEL, "Peripheral Preferred Connection Parameters");
        CHARACTERISTICS.put("00002a02" + UUID_LABEL, "Peripheral Privacy Flag");
        CHARACTERISTICS.put("00002a50" + UUID_LABEL, "PnP ID");
        CHARACTERISTICS.put("00002a69" + UUID_LABEL, "Position Quality");
        CHARACTERISTICS.put("00002a4e" + UUID_LABEL, "Protocol Mode");
        CHARACTERISTICS.put("00002a03" + UUID_LABEL, "Reconnection Address");
        CHARACTERISTICS.put("00002a52" + UUID_LABEL, "Record Access Control Point");
        CHARACTERISTICS.put("00002a14" + UUID_LABEL, "Reference Time Information");
        CHARACTERISTICS.put("00002a4d" + UUID_LABEL, "Report");
        CHARACTERISTICS.put("00002a4b" + UUID_LABEL, "Report Map");
        CHARACTERISTICS.put("00002a40" + UUID_LABEL, "Ringer Control Point");
        CHARACTERISTICS.put("00002a41" + UUID_LABEL, "Ringer Setting");
        CHARACTERISTICS.put("00002a54" + UUID_LABEL, "RSC Feature");
        CHARACTERISTICS.put("00002a53" + UUID_LABEL, "RSC Measurement");
        CHARACTERISTICS.put("00002a55" + UUID_LABEL, "SC Control Point");
        CHARACTERISTICS.put("00002a4f" + UUID_LABEL, "Scan Interval Window");
        CHARACTERISTICS.put("00002a31" + UUID_LABEL, "Scan Refresh");
        CHARACTERISTICS.put("00002a5d" + UUID_LABEL, "Sensor Location");
        CHARACTERISTICS.put("00002a25" + UUID_LABEL, "Serial Number String");
        CHARACTERISTICS.put("00002a05" + UUID_LABEL, "Service Changed");
        CHARACTERISTICS.put("00002a28" + UUID_LABEL, "Software Revision String");
        CHARACTERISTICS.put("00002a47" + UUID_LABEL, "Supported New Alert Category");
        CHARACTERISTICS.put("00002a48" + UUID_LABEL, "Supported Unread Alert Category");
        CHARACTERISTICS.put("00002a23" + UUID_LABEL, "System ID");
        CHARACTERISTICS.put("00002a1c" + UUID_LABEL, "Temperature Measurement");
        CHARACTERISTICS.put("00002a1d" + UUID_LABEL, "Temperature Type");
        CHARACTERISTICS.put("00002a12" + UUID_LABEL, "Time Accuracy");
        CHARACTERISTICS.put("00002a13" + UUID_LABEL, "Time Source");
        CHARACTERISTICS.put("00002a16" + UUID_LABEL, "Time Update Control Point");
        CHARACTERISTICS.put("00002a17" + UUID_LABEL, "Time Update State");
        CHARACTERISTICS.put("00002a11" + UUID_LABEL, "Time with DST");
        CHARACTERISTICS.put("00002a0e" + UUID_LABEL, "Time Zone");
        CHARACTERISTICS.put("00002a07" + UUID_LABEL, "Tx Power Level");
        CHARACTERISTICS.put("00002a45" + UUID_LABEL, "Unread Alert Status");

//        SERVICES.put("00001811", "Alert Notification Service");
//        SERVICES.put("0000180f", "Battery Service");
//        SERVICES.put("00001810", "Blood Pressure");
//        SERVICES.put("00001805", "Current Time Service");
//        SERVICES.put("00001818", "Cycling Power");
//        SERVICES.put("00001816", "Cycling Speed and Cadence");
//        SERVICES.put("0000180a", "Device Information");
//        SERVICES.put("00001800", "Generic Access");
//        SERVICES.put("00001801", "Generic Attribute");
//        SERVICES.put("00001808", "Glucose");
//        SERVICES.put("00001809", "Health Thermometer");
//        SERVICES.put("0000180d", "Heart Rate");
//        SERVICES.put("00001812", "Human Interface Device");
//        SERVICES.put("00001802", "Immediate Alert");
//        SERVICES.put("00001803", "Link Loss");
//        SERVICES.put("00001819", "Location and Navigation");
//        SERVICES.put("00001807", "Next DST Change Service");
//        SERVICES.put("0000180e", "Phone Alert Status Service");
//        SERVICES.put("00001806", "Reference Time Update Service");
//        SERVICES.put("00001814", "Running Speed and Cadence");
//        SERVICES.put("00001813", "Scan Parameters");
//        SERVICES.put("00001804", "Tx Power");
//
//        CHARACTERISTICS.put("00002a43", "Alert Category ID");
//        CHARACTERISTICS.put("00002a42", "Alert Category ID Bit Mask");
//        CHARACTERISTICS.put("00002a06", "Alert Level");
//        CHARACTERISTICS.put("00002a44", "Alert Notification Control Point");
//        CHARACTERISTICS.put("00002a3f", "Alert Status");
//        CHARACTERISTICS.put("00002a01", "Appearance");
//        CHARACTERISTICS.put("00002a19", "Battery Level");
//        CHARACTERISTICS.put("00002a49", "Blood Pressure Feature");
//        CHARACTERISTICS.put("00002a35", "Blood Pressure Measurement");
//        CHARACTERISTICS.put("00002a38", "Body Sensor Location");
//        CHARACTERISTICS.put("00002a22", "Boot Keyboard Input Report");
//        CHARACTERISTICS.put("00002a32", "Boot Keyboard Output Report");
//        CHARACTERISTICS.put("00002a33", "Boot Mouse Input Report");
//        CHARACTERISTICS.put("00002a5c", "CSC Feature");
//        CHARACTERISTICS.put("00002a5b", "CSC Measurement");
//        CHARACTERISTICS.put("00002a2b", "Current Time");
//        CHARACTERISTICS.put("00002a66", "Cycling Power Control Point");
//        CHARACTERISTICS.put("00002a65", "Cycling Power Feature");
//        CHARACTERISTICS.put("00002a63", "Cycling Power Measurement");
//        CHARACTERISTICS.put("00002a64", "Cycling Power Vector");
//        CHARACTERISTICS.put("00002a08", "Date Time");
//        CHARACTERISTICS.put("00002a0a", "Day Date Time");
//        CHARACTERISTICS.put("00002a09", "Day of Week");
//        CHARACTERISTICS.put("00002a00", "Device Name");
//        CHARACTERISTICS.put("00002a0d", "DST Offset");
//        CHARACTERISTICS.put("00002a0c", "Exact Time 256");
//        CHARACTERISTICS.put("00002a26", "Firmware Revision String");
//        CHARACTERISTICS.put("00002a51", "Glucose Feature");
//        CHARACTERISTICS.put("00002a18", "Glucose Measurement");
//        CHARACTERISTICS.put("00002a34", "Glucose Measurement Context");
//        CHARACTERISTICS.put("00002a27", "Hardware Revision String");
//        CHARACTERISTICS.put("00002a39", "Heart Rate Control Point");
//        CHARACTERISTICS.put("00002a37", "Heart Rate Measurement");
//        CHARACTERISTICS.put("00002a4c", "HID Control Point");
//        CHARACTERISTICS.put("00002a4a", "HID Information");
//        CHARACTERISTICS.put("00002a2a", "IEEE 11073-20601 Regulatory Certification Data List");
//        CHARACTERISTICS.put("00002a36", "Intermediate Cuff Pressure");
//        CHARACTERISTICS.put("00002a1e", "Intermediate Temperature");
//        CHARACTERISTICS.put("00002a6b", "LN Control Point");
//        CHARACTERISTICS.put("00002a6a", "LN Feature");
//        CHARACTERISTICS.put("00002a0f", "Local Time Information");
//        CHARACTERISTICS.put("00002a67", "Location and Speed");
//        CHARACTERISTICS.put("00002a29", "Manufacturer Name String");
//        CHARACTERISTICS.put("00002a21", "Measurement Interval");
//        CHARACTERISTICS.put("00002a24", "Model Number String");
//        CHARACTERISTICS.put("00002a68", "Navigation");
//        CHARACTERISTICS.put("00002a46", "New Alert");
//        CHARACTERISTICS.put("00002a04", "Peripheral Preferred Connection Parameters");
//        CHARACTERISTICS.put("00002a02", "Peripheral Privacy Flag");
//        CHARACTERISTICS.put("00002a50", "PnP ID");
//        CHARACTERISTICS.put("00002a69", "Position Quality");
//        CHARACTERISTICS.put("00002a4e", "Protocol Mode");
//        CHARACTERISTICS.put("00002a03", "Reconnection Address");
//        CHARACTERISTICS.put("00002a52", "Record Access Control Point");
//        CHARACTERISTICS.put("00002a14", "Reference Time Information");
//        CHARACTERISTICS.put("00002a4d", "Report");
//        CHARACTERISTICS.put("00002a4b", "Report Map");
//        CHARACTERISTICS.put("00002a40", "Ringer Control Point");
//        CHARACTERISTICS.put("00002a41", "Ringer Setting");
//        CHARACTERISTICS.put("00002a54", "RSC Feature");
//        CHARACTERISTICS.put("00002a53", "RSC Measurement");
//        CHARACTERISTICS.put("00002a55", "SC Control Point");
//        CHARACTERISTICS.put("00002a4f", "Scan Interval Window");
//        CHARACTERISTICS.put("00002a31", "Scan Refresh");
//        CHARACTERISTICS.put("00002a5d", "Sensor Location");
//        CHARACTERISTICS.put("00002a25", "Serial Number String");
//        CHARACTERISTICS.put("00002a05", "Service Changed");
//        CHARACTERISTICS.put("00002a28", "Software Revision String");
//        CHARACTERISTICS.put("00002a47", "Supported New Alert Category");
//        CHARACTERISTICS.put("00002a48", "Supported Unread Alert Category");
//        CHARACTERISTICS.put("00002a23", "System ID");
//        CHARACTERISTICS.put("00002a1c", "Temperature Measurement");
//        CHARACTERISTICS.put("00002a1d", "Temperature Type");
//        CHARACTERISTICS.put("00002a12", "Time Accuracy");
//        CHARACTERISTICS.put("00002a13", "Time Source");
//        CHARACTERISTICS.put("00002a16", "Time Update Control Point");
//        CHARACTERISTICS.put("00002a17", "Time Update State");
//        CHARACTERISTICS.put("00002a11", "Time with DST");
//        CHARACTERISTICS.put("00002a0e", "Time Zone");
//        CHARACTERISTICS.put("00002a07", "Tx Power Level");
//        CHARACTERISTICS.put("00002a45", "Unread Alert Status");

        VALUE_FORMATS.put(52, "32bit float");
        VALUE_FORMATS.put(50, "16bit float");
        VALUE_FORMATS.put(34, "16bit signed int");
        VALUE_FORMATS.put(36, "32bit signed int");
        VALUE_FORMATS.put(33, "8bit signed int");
        VALUE_FORMATS.put(18, "16bit unsigned int");
        VALUE_FORMATS.put(20, "32bit unsigned int");
        VALUE_FORMATS.put(17, "8bit unsigned int");

        FORMAT_LIST = Arrays.asList(
                BluetoothGattCharacteristic.FORMAT_FLOAT,
                BluetoothGattCharacteristic.FORMAT_SFLOAT,
                BluetoothGattCharacteristic.FORMAT_SINT16,
                BluetoothGattCharacteristic.FORMAT_SINT32,
                BluetoothGattCharacteristic.FORMAT_SINT8,
                BluetoothGattCharacteristic.FORMAT_UINT16,
                BluetoothGattCharacteristic.FORMAT_UINT32,
                BluetoothGattCharacteristic.FORMAT_UINT8
        );

        BOND_LIST.put(10, "NOT BONDED");
        BOND_LIST.put(11, "BONDING...");
        BOND_LIST.put(12, "BONDED");

        TYPE_LIST.put(0, UNKNOWN);
        TYPE_LIST.put(1, "CLASSIC BLUETOOTH");
        TYPE_LIST.put(2, "BLUETOOTH LOW ENERGY");
        TYPE_LIST.put(3, "DUAL");
    }
}
