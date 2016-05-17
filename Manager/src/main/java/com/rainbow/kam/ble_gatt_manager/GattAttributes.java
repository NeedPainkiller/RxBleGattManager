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
import android.bluetooth.BluetoothGattService;
import android.util.SparseArray;

import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GattAttributes {

    private final static String UUID_LABEL = "-0000-1000-8000-00805F9B34FB";

    public final static String CLIENT_CHARACTERISTIC_CONFIG = ("00002902" + UUID_LABEL);

    public final static String BATTERY_SERVICE_UUID = ("0000180F" + UUID_LABEL);
    public final static String BATTERY_CHARACTERISTIC_UUID = ("00002A19" + UUID_LABEL);

    public final static String SERVICE_TYPE_PRIMARY = "PRIMARY";
    public final static String SERVICE_TYPE_SECONDARY = "SECONDARY";


    private final static Map<String, String> SERVICES = Maps.newHashMap();
    private final static Map<String, String> CHARACTERISTICS = Maps.newHashMap();
    private final static SparseArray<String> VALUE_FORMATS = new SparseArray<>();

    public final static String UNKNOWN = "UNKNOWN";

    private final static List<Integer> FORMAT_LIST;
    private final static SparseArray<String> BOND_LIST = new SparseArray<>();
    private final static SparseArray<String> TYPE_LIST = new SparseArray<>();


    public static String resolveServiceName(final String uuid) {
        String res = SERVICES.get(uuid.toUpperCase(Locale.getDefault()));
        if (res != null) {
            return res;
        }
        return UNKNOWN;
    }


    public static String resolveCharacteristicName(final String uuid) {
        String res = CHARACTERISTICS.get(uuid.toUpperCase(Locale.getDefault()));
        if (res != null) {
            return res;
        }
        return UNKNOWN;
    }


    public static String resolveValueTypeDescription(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        Integer format = getValueFormat(bluetoothGattCharacteristic);
        return VALUE_FORMATS.get(format, UNKNOWN);
    }


    public static String resolveServiceType(int serviceType) {
        return (serviceType == BluetoothGattService.SERVICE_TYPE_PRIMARY) ? SERVICE_TYPE_PRIMARY : SERVICE_TYPE_SECONDARY;
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


    public static String getBond(int bond) {
        return BOND_LIST.get(bond, UNKNOWN);
    }


    public static String getType(int type) {
        return TYPE_LIST.get(type, UNKNOWN);
    }


    static {
        SERVICES.put("00001811" + UUID_LABEL, "Alert Notification Service");
        SERVICES.put(BATTERY_SERVICE_UUID, "Battery Service");
        SERVICES.put("00001810" + UUID_LABEL, "Blood Pressure");
        SERVICES.put("00001805" + UUID_LABEL, "Current Time Service");
        SERVICES.put("00001818" + UUID_LABEL, "Cycling Power");
        SERVICES.put("00001816" + UUID_LABEL, "Cycling Speed and Cadence");
        SERVICES.put("0000180A" + UUID_LABEL, "Device Information");
        SERVICES.put("00001800" + UUID_LABEL, "Generic Access");
        SERVICES.put("00001801" + UUID_LABEL, "Generic Attribute");
        SERVICES.put("00001808" + UUID_LABEL, "Glucose");
        SERVICES.put("00001809" + UUID_LABEL, "Health Thermometer");
        SERVICES.put("0000180D" + UUID_LABEL, "Heart Rate");
        SERVICES.put("00001812" + UUID_LABEL, "Human Interface Device");
        SERVICES.put("00001802" + UUID_LABEL, "Immediate Alert");
        SERVICES.put("00001803" + UUID_LABEL, "Link Loss");
        SERVICES.put("00001819" + UUID_LABEL, "Location and Navigation");
        SERVICES.put("00001807" + UUID_LABEL, "Next DST Change Service");
        SERVICES.put("0000180E" + UUID_LABEL, "Phone Alert Status Service");
        SERVICES.put("00001806" + UUID_LABEL, "Reference Time Update Service");
        SERVICES.put("00001814" + UUID_LABEL, "Running Speed and Cadence");
        SERVICES.put("00001813" + UUID_LABEL, "Scan Parameters");
        SERVICES.put("00001804" + UUID_LABEL, "Tx Power");

        CHARACTERISTICS.put("00002A43" + UUID_LABEL, "Alert Category ID");
        CHARACTERISTICS.put("00002A42" + UUID_LABEL, "Alert Category ID Bit Mask");
        CHARACTERISTICS.put("00002A06" + UUID_LABEL, "Alert Level");
        CHARACTERISTICS.put("00002A44" + UUID_LABEL, "Alert Notification Control Point");
        CHARACTERISTICS.put("00002A3F" + UUID_LABEL, "Alert Status");
        CHARACTERISTICS.put("00002A01" + UUID_LABEL, "Appearance");
        CHARACTERISTICS.put(BATTERY_CHARACTERISTIC_UUID, "Battery Level");
        CHARACTERISTICS.put("00002A49" + UUID_LABEL, "Blood Pressure Feature");
        CHARACTERISTICS.put("00002A35" + UUID_LABEL, "Blood Pressure Measurement");
        CHARACTERISTICS.put("00002A38" + UUID_LABEL, "Body Sensor Location");
        CHARACTERISTICS.put("00002A22" + UUID_LABEL, "Boot Keyboard Input Report");
        CHARACTERISTICS.put("00002A32" + UUID_LABEL, "Boot Keyboard Output Report");
        CHARACTERISTICS.put("00002A33" + UUID_LABEL, "Boot Mouse Input Report");
        CHARACTERISTICS.put("00002A5C" + UUID_LABEL, "CSC Feature");
        CHARACTERISTICS.put("00002A5B" + UUID_LABEL, "CSC Measurement");
        CHARACTERISTICS.put("00002A2B" + UUID_LABEL, "Current Time");
        CHARACTERISTICS.put("00002A66" + UUID_LABEL, "Cycling Power Control Point");
        CHARACTERISTICS.put("00002A65" + UUID_LABEL, "Cycling Power Feature");
        CHARACTERISTICS.put("00002A63" + UUID_LABEL, "Cycling Power Measurement");
        CHARACTERISTICS.put("00002A64" + UUID_LABEL, "Cycling Power Vector");
        CHARACTERISTICS.put("00002A08" + UUID_LABEL, "Date Time");
        CHARACTERISTICS.put("00002A0A" + UUID_LABEL, "Day Date Time");
        CHARACTERISTICS.put("00002A09" + UUID_LABEL, "Day of Week");
        CHARACTERISTICS.put("00002A00" + UUID_LABEL, "Device Name");
        CHARACTERISTICS.put("00002A0D" + UUID_LABEL, "DST Offset");
        CHARACTERISTICS.put("00002A0C" + UUID_LABEL, "Exact Time 256");
        CHARACTERISTICS.put("00002A26" + UUID_LABEL, "Firmware Revision String");
        CHARACTERISTICS.put("00002A51" + UUID_LABEL, "Glucose Feature");
        CHARACTERISTICS.put("00002A18" + UUID_LABEL, "Glucose Measurement");
        CHARACTERISTICS.put("00002A34" + UUID_LABEL, "Glucose Measurement Context");
        CHARACTERISTICS.put("00002A27" + UUID_LABEL, "Hardware Revision String");
        CHARACTERISTICS.put("00002A39" + UUID_LABEL, "Heart Rate Control Point");
        CHARACTERISTICS.put("00002A37" + UUID_LABEL, "Heart Rate Measurement");
        CHARACTERISTICS.put("00002A4C" + UUID_LABEL, "HID Control Point");
        CHARACTERISTICS.put("00002A4A" + UUID_LABEL, "HID Information");
        CHARACTERISTICS.put("00002A2A" + UUID_LABEL, "IEEE 11073-20601 Regulatory Certification Data List");
        CHARACTERISTICS.put("00002A36" + UUID_LABEL, "Intermediate Cuff Pressure");
        CHARACTERISTICS.put("00002A1E" + UUID_LABEL, "Intermediate Temperature");
        CHARACTERISTICS.put("00002A6B" + UUID_LABEL, "LN Control Point");
        CHARACTERISTICS.put("00002A6A" + UUID_LABEL, "LN Feature");
        CHARACTERISTICS.put("00002A0F" + UUID_LABEL, "Local Time Information");
        CHARACTERISTICS.put("00002A67" + UUID_LABEL, "Location and Speed");
        CHARACTERISTICS.put("00002A29" + UUID_LABEL, "Manufacturer Name String");
        CHARACTERISTICS.put("00002A21" + UUID_LABEL, "Measurement Interval");
        CHARACTERISTICS.put("00002A24" + UUID_LABEL, "Model Number String");
        CHARACTERISTICS.put("00002A68" + UUID_LABEL, "Navigation");
        CHARACTERISTICS.put("00002A46" + UUID_LABEL, "New Alert");
        CHARACTERISTICS.put("00002A04" + UUID_LABEL, "Peripheral Preferred Connection Parameters");
        CHARACTERISTICS.put("00002A02" + UUID_LABEL, "Peripheral Privacy Flag");
        CHARACTERISTICS.put("00002A50" + UUID_LABEL, "PnP ID");
        CHARACTERISTICS.put("00002A69" + UUID_LABEL, "Position Quality");
        CHARACTERISTICS.put("00002A4E" + UUID_LABEL, "Protocol Mode");
        CHARACTERISTICS.put("00002A03" + UUID_LABEL, "Reconnection Address");
        CHARACTERISTICS.put("00002A52" + UUID_LABEL, "Record Access Control Point");
        CHARACTERISTICS.put("00002A14" + UUID_LABEL, "Reference Time Information");
        CHARACTERISTICS.put("00002A4D" + UUID_LABEL, "Report");
        CHARACTERISTICS.put("00002A4B" + UUID_LABEL, "Report Map");
        CHARACTERISTICS.put("00002A40" + UUID_LABEL, "Ringer Control Point");
        CHARACTERISTICS.put("00002A41" + UUID_LABEL, "Ringer Setting");
        CHARACTERISTICS.put("00002A54" + UUID_LABEL, "RSC Feature");
        CHARACTERISTICS.put("00002A53" + UUID_LABEL, "RSC Measurement");
        CHARACTERISTICS.put("00002A55" + UUID_LABEL, "SC Control Point");
        CHARACTERISTICS.put("00002A4F" + UUID_LABEL, "Scan Interval Window");
        CHARACTERISTICS.put("00002A31" + UUID_LABEL, "Scan Refresh");
        CHARACTERISTICS.put("00002A5D" + UUID_LABEL, "Sensor Location");
        CHARACTERISTICS.put("00002A25" + UUID_LABEL, "Serial Number String");
        CHARACTERISTICS.put("00002A05" + UUID_LABEL, "Service Changed");
        CHARACTERISTICS.put("00002A28" + UUID_LABEL, "Software Revision String");
        CHARACTERISTICS.put("00002A47" + UUID_LABEL, "Supported New Alert Category");
        CHARACTERISTICS.put("00002A48" + UUID_LABEL, "Supported Unread Alert Category");
        CHARACTERISTICS.put("00002A23" + UUID_LABEL, "System ID");
        CHARACTERISTICS.put("00002A1C" + UUID_LABEL, "Temperature Measurement");
        CHARACTERISTICS.put("00002A1D" + UUID_LABEL, "Temperature Type");
        CHARACTERISTICS.put("00002A12" + UUID_LABEL, "Time Accuracy");
        CHARACTERISTICS.put("00002A13" + UUID_LABEL, "Time Source");
        CHARACTERISTICS.put("00002A16" + UUID_LABEL, "Time Update Control Point");
        CHARACTERISTICS.put("00002A17" + UUID_LABEL, "Time Update State");
        CHARACTERISTICS.put("00002A11" + UUID_LABEL, "Time with DST");
        CHARACTERISTICS.put("00002A0E" + UUID_LABEL, "Time Zone");
        CHARACTERISTICS.put("00002A07" + UUID_LABEL, "Tx Power Level");
        CHARACTERISTICS.put("00002A45" + UUID_LABEL, "Unread Alert Status");

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
        BOND_LIST.put(11, "BONDING");
        BOND_LIST.put(12, "BONDED");

        TYPE_LIST.put(0, UNKNOWN);
        TYPE_LIST.put(1, "CLASSIC");
        TYPE_LIST.put(2, "BLE");
        TYPE_LIST.put(3, "DUAL");
    }
}
