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

package com.rainbow.kam.ble_gatt_manager.util;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.SparseArray;

import com.google.common.collect.Maps;
import com.rainbow.kam.ble_gatt_manager.BuildConfig;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static android.bluetooth.BluetoothGattCharacteristic.*;

public class BluetoothGatts {

    private final static String UNKNOWN = BuildConfig.UNKNOWN;

    public final static String UUID_LABEL = "-0000-1000-8000-00805F9B34FB";

    public final static UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");

    public final static UUID BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB");
    public final static UUID BATTERY_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB");

    public final static String SERVICE_TYPE_PRIMARY = "PRIMARY";
    public final static String SERVICE_TYPE_SECONDARY = "SECONDARY";

    private final static Map<String, String> SERVICES = Maps.newHashMap();
    private final static Map<String, String> CHARACTERISTICS = Maps.newHashMap();

    private final static SparseArray<String> VALUE_FORMATS = new SparseArray<>();
    private final static LinkedHashMap<Integer, String> PROPERTIES = Maps.newLinkedHashMap();

    static {
        SERVICES.put("00001811-0000-1000-8000-00805F9B34FB", "Alert Notification Service");
        SERVICES.put(BATTERY_SERVICE_UUID.toString(), "Battery Service");
        SERVICES.put("00001810-0000-1000-8000-00805F9B34FB", "Blood Pressure");
        SERVICES.put("00001805-0000-1000-8000-00805F9B34FB", "Current Time Service");
        SERVICES.put("00001818-0000-1000-8000-00805F9B34FB", "Cycling Power");
        SERVICES.put("00001816-0000-1000-8000-00805F9B34FB", "Cycling Speed and Cadence");
        SERVICES.put("0000180A-0000-1000-8000-00805F9B34FB", "Device Information");
        SERVICES.put("00001800-0000-1000-8000-00805F9B34FB", "Generic Access");
        SERVICES.put("00001801-0000-1000-8000-00805F9B34FB", "Generic Attribute");
        SERVICES.put("00001808-0000-1000-8000-00805F9B34FB", "Glucose");
        SERVICES.put("00001809-0000-1000-8000-00805F9B34FB", "Health Thermometer");
        SERVICES.put("0000180D-0000-1000-8000-00805F9B34FB", "Heart Rate");
        SERVICES.put("00001812-0000-1000-8000-00805F9B34FB", "Human Interface Device");
        SERVICES.put("00001802-0000-1000-8000-00805F9B34FB", "Immediate Alert");
        SERVICES.put("00001803-0000-1000-8000-00805F9B34FB", "Link Loss");
        SERVICES.put("00001819-0000-1000-8000-00805F9B34FB", "Location and Navigation");
        SERVICES.put("00001807-0000-1000-8000-00805F9B34FB", "Next DST Change Service");
        SERVICES.put("0000180E-0000-1000-8000-00805F9B34FB", "Phone Alert Status Service");
        SERVICES.put("00001806-0000-1000-8000-00805F9B34FB", "Reference Time Update Service");
        SERVICES.put("00001814-0000-1000-8000-00805F9B34FB", "Running Speed and Cadence");
        SERVICES.put("00001813-0000-1000-8000-00805F9B34FB", "Scan Parameters");
        SERVICES.put("00001804-0000-1000-8000-00805F9B34FB", "Tx Power");

        CHARACTERISTICS.put("00002A43-0000-1000-8000-00805F9B34FB", "Alert Category ID");
        CHARACTERISTICS.put("00002A42-0000-1000-8000-00805F9B34FB", "Alert Category ID Bit Mask");
        CHARACTERISTICS.put("00002A06-0000-1000-8000-00805F9B34FB", "Alert Level");
        CHARACTERISTICS.put("00002A44-0000-1000-8000-00805F9B34FB", "Alert Notification Control Point");
        CHARACTERISTICS.put("00002A3F-0000-1000-8000-00805F9B34FB", "Alert Status");
        CHARACTERISTICS.put("00002A01-0000-1000-8000-00805F9B34FB", "Appearance");
        CHARACTERISTICS.put(BATTERY_CHARACTERISTIC_UUID.toString(), "Battery Level");
        CHARACTERISTICS.put("00002A49-0000-1000-8000-00805F9B34FB", "Blood Pressure Feature");
        CHARACTERISTICS.put("00002A35-0000-1000-8000-00805F9B34FB", "Blood Pressure Measurement");
        CHARACTERISTICS.put("00002A38-0000-1000-8000-00805F9B34FB", "Body Sensor Location");
        CHARACTERISTICS.put("00002A22-0000-1000-8000-00805F9B34FB", "Boot Keyboard Input Report");
        CHARACTERISTICS.put("00002A32-0000-1000-8000-00805F9B34FB", "Boot Keyboard Output Report");
        CHARACTERISTICS.put("00002A33-0000-1000-8000-00805F9B34FB", "Boot Mouse Input Report");
        CHARACTERISTICS.put("00002A5C-0000-1000-8000-00805F9B34FB", "CSC Feature");
        CHARACTERISTICS.put("00002A5B-0000-1000-8000-00805F9B34FB", "CSC Measurement");
        CHARACTERISTICS.put("00002A2B-0000-1000-8000-00805F9B34FB", "Current Time");
        CHARACTERISTICS.put("00002A66-0000-1000-8000-00805F9B34FB", "Cycling Power Control Point");
        CHARACTERISTICS.put("00002A65-0000-1000-8000-00805F9B34FB", "Cycling Power Feature");
        CHARACTERISTICS.put("00002A63-0000-1000-8000-00805F9B34FB", "Cycling Power Measurement");
        CHARACTERISTICS.put("00002A64-0000-1000-8000-00805F9B34FB", "Cycling Power Vector");
        CHARACTERISTICS.put("00002A08-0000-1000-8000-00805F9B34FB", "Date Time");
        CHARACTERISTICS.put("00002A0A-0000-1000-8000-00805F9B34FB", "Day Date Time");
        CHARACTERISTICS.put("00002A09-0000-1000-8000-00805F9B34FB", "Day of Week");
        CHARACTERISTICS.put("00002A00-0000-1000-8000-00805F9B34FB", "Device Name");
        CHARACTERISTICS.put("00002A0D-0000-1000-8000-00805F9B34FB", "DST Offset");
        CHARACTERISTICS.put("00002A0C-0000-1000-8000-00805F9B34FB", "Exact Time 256");
        CHARACTERISTICS.put("00002A26-0000-1000-8000-00805F9B34FB", "Firmware Revision String");
        CHARACTERISTICS.put("00002A51-0000-1000-8000-00805F9B34FB", "Glucose Feature");
        CHARACTERISTICS.put("00002A18-0000-1000-8000-00805F9B34FB", "Glucose Measurement");
        CHARACTERISTICS.put("00002A34-0000-1000-8000-00805F9B34FB", "Glucose Measurement Context");
        CHARACTERISTICS.put("00002A27-0000-1000-8000-00805F9B34FB", "Hardware Revision String");
        CHARACTERISTICS.put("00002A39-0000-1000-8000-00805F9B34FB", "Heart Rate Control Point");
        CHARACTERISTICS.put("00002A37-0000-1000-8000-00805F9B34FB", "Heart Rate Measurement");
        CHARACTERISTICS.put("00002A4C-0000-1000-8000-00805F9B34FB", "HID Control Point");
        CHARACTERISTICS.put("00002A4A-0000-1000-8000-00805F9B34FB", "HID Information");
        CHARACTERISTICS.put("00002A2A-0000-1000-8000-00805F9B34FB", "IEEE 11073-20601 Regulatory Certification Data List");
        CHARACTERISTICS.put("00002A36-0000-1000-8000-00805F9B34FB", "Intermediate Cuff Pressure");
        CHARACTERISTICS.put("00002A1E-0000-1000-8000-00805F9B34FB", "Intermediate Temperature");
        CHARACTERISTICS.put("00002A6B-0000-1000-8000-00805F9B34FB", "LN Control Point");
        CHARACTERISTICS.put("00002A6A-0000-1000-8000-00805F9B34FB", "LN Feature");
        CHARACTERISTICS.put("00002A0F-0000-1000-8000-00805F9B34FB", "Local Time Information");
        CHARACTERISTICS.put("00002A67-0000-1000-8000-00805F9B34FB", "Location and Speed");
        CHARACTERISTICS.put("00002A29-0000-1000-8000-00805F9B34FB", "Manufacturer Name String");
        CHARACTERISTICS.put("00002A21-0000-1000-8000-00805F9B34FB", "Measurement Interval");
        CHARACTERISTICS.put("00002A24-0000-1000-8000-00805F9B34FB", "Model Number String");
        CHARACTERISTICS.put("00002A68-0000-1000-8000-00805F9B34FB", "Navigation");
        CHARACTERISTICS.put("00002A46-0000-1000-8000-00805F9B34FB", "New Alert");
        CHARACTERISTICS.put("00002A04-0000-1000-8000-00805F9B34FB", "Peripheral Preferred Connection Parameters");
        CHARACTERISTICS.put("00002A02-0000-1000-8000-00805F9B34FB", "Peripheral Privacy Flag");
        CHARACTERISTICS.put("00002A50-0000-1000-8000-00805F9B34FB", "PnP ID");
        CHARACTERISTICS.put("00002A69-0000-1000-8000-00805F9B34FB", "Position Quality");
        CHARACTERISTICS.put("00002A4E-0000-1000-8000-00805F9B34FB", "Protocol Mode");
        CHARACTERISTICS.put("00002A03-0000-1000-8000-00805F9B34FB", "Reconnection Address");
        CHARACTERISTICS.put("00002A52-0000-1000-8000-00805F9B34FB", "Record Access Control Point");
        CHARACTERISTICS.put("00002A14-0000-1000-8000-00805F9B34FB", "Reference Time Information");
        CHARACTERISTICS.put("00002A4D-0000-1000-8000-00805F9B34FB", "Report");
        CHARACTERISTICS.put("00002A4B-0000-1000-8000-00805F9B34FB", "Report Map");
        CHARACTERISTICS.put("00002A40-0000-1000-8000-00805F9B34FB", "Ringer Control Point");
        CHARACTERISTICS.put("00002A41-0000-1000-8000-00805F9B34FB", "Ringer Setting");
        CHARACTERISTICS.put("00002A54-0000-1000-8000-00805F9B34FB", "RSC Feature");
        CHARACTERISTICS.put("00002A53-0000-1000-8000-00805F9B34FB", "RSC Measurement");
        CHARACTERISTICS.put("00002A55-0000-1000-8000-00805F9B34FB", "SC Control Point");
        CHARACTERISTICS.put("00002A4F-0000-1000-8000-00805F9B34FB", "Scan Interval Window");
        CHARACTERISTICS.put("00002A31-0000-1000-8000-00805F9B34FB", "Scan Refresh");
        CHARACTERISTICS.put("00002A5D-0000-1000-8000-00805F9B34FB", "Sensor Location");
        CHARACTERISTICS.put("00002A25-0000-1000-8000-00805F9B34FB", "Serial Number String");
        CHARACTERISTICS.put("00002A05-0000-1000-8000-00805F9B34FB", "Service Changed");
        CHARACTERISTICS.put("00002A28-0000-1000-8000-00805F9B34FB", "Software Revision String");
        CHARACTERISTICS.put("00002A47-0000-1000-8000-00805F9B34FB", "Supported New Alert Category");
        CHARACTERISTICS.put("00002A48-0000-1000-8000-00805F9B34FB", "Supported Unread Alert Category");
        CHARACTERISTICS.put("00002A23-0000-1000-8000-00805F9B34FB", "System ID");
        CHARACTERISTICS.put("00002A1C-0000-1000-8000-00805F9B34FB", "Temperature Measurement");
        CHARACTERISTICS.put("00002A1D-0000-1000-8000-00805F9B34FB", "Temperature Type");
        CHARACTERISTICS.put("00002A12-0000-1000-8000-00805F9B34FB", "Time Accuracy");
        CHARACTERISTICS.put("00002A13-0000-1000-8000-00805F9B34FB", "Time Source");
        CHARACTERISTICS.put("00002A16-0000-1000-8000-00805F9B34FB", "Time Update Control Point");
        CHARACTERISTICS.put("00002A17-0000-1000-8000-00805F9B34FB", "Time Update State");
        CHARACTERISTICS.put("00002A11-0000-1000-8000-00805F9B34FB", "Time with DST");
        CHARACTERISTICS.put("00002A0E-0000-1000-8000-00805F9B34FB", "Time Zone");
        CHARACTERISTICS.put("00002A07-0000-1000-8000-00805F9B34FB", "Tx Power Level");
        CHARACTERISTICS.put("00002A45-0000-1000-8000-00805F9B34FB", "Unread Alert Status");

        VALUE_FORMATS.put(FORMAT_FLOAT, "32bit float");
        VALUE_FORMATS.put(FORMAT_SFLOAT, "16bit float");
        VALUE_FORMATS.put(FORMAT_SINT16, "16bit signed int");
        VALUE_FORMATS.put(FORMAT_SINT32, "32bit signed int");
        VALUE_FORMATS.put(FORMAT_SINT8, "8bit signed int");
        VALUE_FORMATS.put(FORMAT_UINT16, "16bit unsigned int");
        VALUE_FORMATS.put(FORMAT_UINT32, "32bit unsigned int");
        VALUE_FORMATS.put(FORMAT_UINT8, "8bit unsigned int");

        PROPERTIES.put(PROPERTY_BROADCAST, "BROADCAST \b");
        PROPERTIES.put(PROPERTY_READ, "READ \b");
        PROPERTIES.put(PROPERTY_WRITE_NO_RESPONSE, "WRITE NO RESPONSE \b");
        PROPERTIES.put(PROPERTY_WRITE, "WRITE \b");
        PROPERTIES.put(PROPERTY_NOTIFY, "NOTIFY \b");
        PROPERTIES.put(PROPERTY_INDICATE, "INDICATE \b");
        PROPERTIES.put(PROPERTY_SIGNED_WRITE, "SIGNED WRITE \b");
        PROPERTIES.put(PROPERTY_EXTENDED_PROPS, "EXTENDED PROPS \b");
    }

    public static String resolveServiceName(final String uuid) {
        String res = SERVICES.get(uuid.toUpperCase(Locale.getDefault()));
        return res == null ? UNKNOWN : res;
    }


    public static String resolveCharacteristicName(final String uuid) {
        String res = CHARACTERISTICS.get(uuid.toUpperCase(Locale.getDefault()));
        return res == null ? UNKNOWN : res;
    }


    public static String resolveServiceType(int serviceType) {
        return (serviceType == BluetoothGattService.SERVICE_TYPE_PRIMARY) ? SERVICE_TYPE_PRIMARY : SERVICE_TYPE_SECONDARY;
    }


    public static String resolveValueTypeDescription(int properties) {
        int formatListLength = VALUE_FORMATS.size();
        for (int i = 0; i < formatListLength; i++) {
            int format = VALUE_FORMATS.keyAt(i);
            if ((format & properties) != 0)
                return VALUE_FORMATS.get(format, UNKNOWN);
        }
        return UNKNOWN;
    }


    public static LinkedHashMap<Integer, String> getProperties() {
        return PROPERTIES;
    }


    public static String getAvailableProperties(int properties) {
        StringBuilder propertiesString = new StringBuilder();

        propertiesString.append(String.format("0x%04X", properties));
        for (int props : PROPERTIES.keySet()) {
            if (isPropertyAvailable(properties, props)) {
                propertiesString.append(PROPERTIES.get(props));
            }
        }
        return propertiesString.toString();
    }


    public static boolean isPropertyAvailable(BluetoothGattCharacteristic bluetoothGattCharacteristic, int prop) {
        return (bluetoothGattCharacteristic.getProperties() & prop) != 0;
    }


    public static boolean isPropertiesAvailable(BluetoothGattCharacteristic bluetoothGattCharacteristic, int... props) {
        boolean isPropertiesAvailable = false;
        for (int prop : props) {
            isPropertiesAvailable = (bluetoothGattCharacteristic.getProperties() & prop) != 0;
        }
        return isPropertiesAvailable;
    }


    public static boolean isPropertyAvailable(int properties, int prop) {
        return (properties & prop) != 0;
    }


    @Override public String toString() {
        return "BluetoothGatts{}";
    }
}