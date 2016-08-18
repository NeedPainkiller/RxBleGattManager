package com.rainbow.kam.ble_gatt_manager.manager;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;

import static com.rainbow.kam.ble_gatt_manager.manager.RxGattListeners.*;

public class GattManagerCallBack extends BluetoothGattCallback {

    private GattConnectionOnSubscribe connectionListener;
    private GattRssiOnSubscribe rssiListener;
    private GattServiceOnSubscribe serviceListener;
    private GattReadCharacteristicOnSubscribe readListener;
    private GattWriteCharacteristicOnSubscribe writeListener;
    private GattNotifyCharacteristicOnSubscribe notifyListener;
    private GattIndicateCharacteristicOnSubscribe indicateListener;


    public void setConnectionListener(GattConnectionOnSubscribe connectionListener) {
        this.connectionListener = connectionListener;
    }


    public void setRssiListener(GattRssiOnSubscribe rssiListener) {
        this.rssiListener = rssiListener;
    }


    public void setServiceListener(GattServiceOnSubscribe serviceListener) {
        this.serviceListener = serviceListener;
    }


    public void setReadListener(GattReadCharacteristicOnSubscribe readListener) {
        this.readListener = readListener;
    }


    public void setWriteListener(GattWriteCharacteristicOnSubscribe writeListener) {
        this.writeListener = writeListener;
    }


    public void setNotifyListener(GattNotifyCharacteristicOnSubscribe notifyListener) {
        this.notifyListener = notifyListener;
    }


    public void setIndicateListener(GattIndicateCharacteristicOnSubscribe indicateListener) {
        this.indicateListener = indicateListener;
    }


    @Override public void onConnectionStateChange(
            final BluetoothGatt bluetoothGatt, final int status, final int newState) {
        if (connectionListener != null) {
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                connectionListener.onGattConnected();
            } else {
                connectionListener.onGattDisconnected();
            }
        }
    }


    @Override public void onReadRemoteRssi(
            final BluetoothGatt bluetoothGatt, final int rssi, final int status) {
        if (rssiListener != null) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                rssiListener.onRssiUpdated(rssi);
            } else {
                rssiListener.onRssiInvalidate(status);
            }
        }
    }


    @Override public void onServicesDiscovered(
            final BluetoothGatt bluetoothGatt, final int status) {
        if (serviceListener != null) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                serviceListener.onServiceDiscovered(bluetoothGatt.getServices());
            } else {
                serviceListener.onServiceNotFound(status);
            }
        }
    }


    @Override public void onCharacteristicRead(
            final BluetoothGatt bluetoothGatt, final BluetoothGattCharacteristic characteristic, final int status) {
        if (readListener != null) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                readListener.onCharacteristicReadSucceeded(characteristic);
            } else {
                readListener.onCharacteristicReadFailed(characteristic, status);
            }
        }
    }


    @Override public void onCharacteristicWrite(
            final BluetoothGatt bluetoothGatt, final BluetoothGattCharacteristic characteristic, final int status) {
        if (writeListener != null) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                writeListener.onCharacteristicWritePrepared(characteristic);
            } else {
                writeListener.onCharacteristicWriteFailed(characteristic, status);
            }
        }
    }


    @Override public void onCharacteristicChanged(
            final BluetoothGatt bluetoothGatt, final BluetoothGattCharacteristic characteristic) {
        if (writeListener != null) {
            writeListener.onCharacteristicWriteSucceeded(characteristic);
        }
        if (notifyListener != null) {
            notifyListener.onCharacteristicNotifySucceeded(characteristic);
        }
        if (indicateListener != null) {
            indicateListener.onCharacteristicIndicateSucceeded(characteristic);
        }
    }


    @Override
    public void onDescriptorWrite(final BluetoothGatt bluetoothGatt, final BluetoothGattDescriptor descriptor, final int status) {
        BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (notifyListener != null) {
                notifyListener.onCharacteristicNotifyPrepared(characteristic);
            }
            if (indicateListener != null) {
                indicateListener.onCharacteristicIndicatePrepared(characteristic);
            }
        } else {
            if (notifyListener != null) {
                notifyListener.onCharacteristicNotifyFailed(descriptor, status);
            }
            if (indicateListener != null) {
                indicateListener.onCharacteristicIndicateFailed(descriptor, status);
            }
        }
    }
}