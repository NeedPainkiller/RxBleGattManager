package com.rainbow.kam.ble_gatt_manager.manager;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import java.util.List;

/**
 * Created by Kang Young Won on 2016-08-03.
 */
public class RxGattListeners {
    protected interface GattConnectionOnSubscribe {

        void onGattConnected();

        void onGattDisconnected();
    }

    protected interface GattRssiOnSubscribe {

        void onRssiUpdated(int rssi);

        void onRssiInvalidate(int status);
    }

    protected interface GattServiceOnSubscribe {

        void onServiceDiscovered(List<BluetoothGattService> services);

        void onServiceNotFound(int status);
    }

    protected interface GattReadCharacteristicOnSubscribe {

        void onCharacteristicReadSucceeded(BluetoothGattCharacteristic characteristic);

        void onCharacteristicReadFailed(BluetoothGattCharacteristic characteristic, int status);
    }

    protected interface GattWriteCharacteristicOnSubscribe {

        void onCharacteristicWritePrepared(BluetoothGattCharacteristic characteristic);

        void onCharacteristicWriteSucceeded(BluetoothGattCharacteristic characteristic);

        void onCharacteristicWriteFailed(BluetoothGattCharacteristic characteristic, int status);
    }

    protected interface GattNotifyCharacteristicOnSubscribe {

        void onCharacteristicNotifyPrepared(BluetoothGattCharacteristic characteristic);

        void onCharacteristicNotifySucceeded(BluetoothGattCharacteristic characteristic);

        void onCharacteristicNotifyFailed(BluetoothGattDescriptor descriptor, int status);
    }

    protected interface GattIndicateCharacteristicOnSubscribe {

        void onCharacteristicIndicatePrepared(BluetoothGattCharacteristic characteristic);

        void onCharacteristicIndicateSucceeded(BluetoothGattCharacteristic characteristic);

        void onCharacteristicIndicateFailed(BluetoothGattDescriptor descriptor, int status);
    }
}