
package com.rainbow.kam.ble_gatt_manager.manager.legacy;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.common.primitives.Bytes;
import com.rainbow.kam.ble_gatt_manager.util.BluetoothGatts;
import com.rainbow.kam.ble_gatt_manager.exceptions.GattException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.ConnectedFailException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.DisconnectedFailException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.GattResourceNotDiscoveredException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.NotificationCharacteristicException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.ReadCharacteristicException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.RssiMissException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.WriteCharacteristicException;
import com.rainbow.kam.ble_gatt_manager.data.BleDevice;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;

/**
 * Created by kam6512 on 2015-10-29.
 */

public class GattManagerOriginalSingleton {

    private final static String TAG = GattManagerOriginalSingleton.class.getSimpleName();

    private final static long RSSI_UPDATE_TIME_INTERVAL = 3;

    private final static UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = UUID.fromString(BluetoothGatts.CLIENT_CHARACTERISTIC_CONFIG);
    private final static UUID BATTERY_SERVICE_UUID = UUID.fromString(BluetoothGatts.BATTERY_SERVICE_UUID);
    private final static UUID BATTERY_CHARACTERISTIC_UUID = UUID.fromString(BluetoothGatts.BATTERY_CHARACTERISTIC_UUID);

    private Application application;

    private GattCustomCallbacks gattCustomCallbacks;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattDescriptor notificationDescriptor;

    private Subscription rssiSubscription;

    private BluetoothGattCharacteristic writeCharacteristic;
    private BluetoothGattCharacteristic notificationCharacteristic;

    private BleDevice bleDevice;


    private GattManagerOriginalSingleton() {
    }


    private static class GattManagerHolder {
        private static final GattManagerOriginalSingleton instance = new GattManagerOriginalSingleton();
    }


    public static GattManagerOriginalSingleton getInstance() {
        return GattManagerHolder.instance;
    }


    public void establishConnection(final Application application, final GattCustomCallbacks gattCustomCallbacks, final BleDevice bleDevice) {

        this.application = application;

        if (bluetoothManager == null || bluetoothAdapter == null) {
            bluetoothManager = (BluetoothManager) this.application.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
        }


        this.bleDevice = bleDevice;
        this.gattCustomCallbacks = gattCustomCallbacks;


        if (!bluetoothAdapter.isEnabled()) {
            gattCustomCallbacks.onError(new GattException("Adapter is disabled"));
        }

        String deviceAddress = bleDevice.getAddress();
        if (TextUtils.isEmpty(deviceAddress)) {
            gattCustomCallbacks.onError(new ConnectedFailException(deviceAddress, "Address is not available"));
            return;
        }

        bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
        if (bluetoothDevice == null) {

            gattCustomCallbacks.onError(new ConnectedFailException(deviceAddress, "RemoteDevice is not available"));
        } else {

            if (isConnected()) {
                closeGatt();
            }

            bluetoothGatt = bluetoothDevice.connectGatt(application, false, bluetoothGattCallback);
        }
    }


    public BluetoothGatt getGatt() {
        return bluetoothGatt;
    }


    public BleDevice getBleDevice() {
        return bleDevice;
    }


    public void substituteCallback(final GattCustomCallbacks callbacks) {
        gattCustomCallbacks = callbacks;
    }


    public void disconnectImmediately() {
        disconnect();
        closeGatt();
    }


    public void disconnect() {
        String deviceAddress;
        if (bluetoothGatt != null) {
            deviceAddress = bluetoothGatt.getDevice().getAddress();
            if (isConnected() || bluetoothAdapter.isEnabled()) {
                bluetoothGatt.disconnect();
            } else {
                gattCustomCallbacks.onError(new DisconnectedFailException(deviceAddress, "Device already Disconnected"));
            }
        }
//        else {
//            gattCustomCallbacks.onError(new DisconnectedFailException("BluetoothGatt is not Available"));
//        }
    }


    public void closeGatt() {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
        }
    }


    public boolean isConnected() {
        List<BluetoothDevice> bluetoothDevices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
        for (BluetoothDevice bluetoothDeviceItem : bluetoothDevices) {
            if (bluetoothDevice != null && bluetoothDevice.getAddress().equals(bluetoothDeviceItem.getAddress())) {
                return true;
            }
        }
        return false;
    }


    private void readRssiValue() {
        if (isConnected()) {
            rssiSubscription = Observable.interval(RSSI_UPDATE_TIME_INTERVAL, TimeUnit.SECONDS).subscribe(aLong -> {
                bluetoothGatt.readRemoteRssi();
            });
        } else {
            if (rssiSubscription != null) {
                rssiSubscription.unsubscribe();
            }
        }
    }


    public void startServiceDiscovery() {
        bluetoothGatt.discoverServices();
    }


    public void setNotification(BluetoothGattCharacteristic notificationCharacteristic, boolean enabled) {

        Observable.create((Observable.OnSubscribe<Boolean>) subscriber -> {
            subscriber.onNext(bluetoothGatt.setCharacteristicNotification(notificationCharacteristic, enabled));
            subscriber.onCompleted();
        }).subscribe(aBoolean -> gattCustomCallbacks.onSetNotificationSuccess(),
                throwable -> gattCustomCallbacks.onError(new NotificationCharacteristicException(notificationDescriptor.getCharacteristic(), notificationDescriptor, throwable.getMessage())),
                () -> {
                    notificationDescriptor = notificationCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
                    byte[] value = enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                    notificationDescriptor.setValue(value);
                    bluetoothGatt.writeDescriptor(notificationDescriptor);
                })
                .unsubscribe();


        /*Observable.just(notificationCharacteristic)
                .map(characteristic -> {
                    bluetoothGatt.setCharacteristicNotification(notificationCharacteristic, enabled);
                    notificationDescriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
                    byte[] value = enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                    notificationDescriptor.setValue(value);
                    return notificationDescriptor;
                })
                .subscribe(bluetoothGattDescriptor -> {
                    bluetoothGatt.writeDescriptor(notificationDescriptor);
                    gattCustomCallbacks.onSetNotificationSuccess();
                }, throwable -> {
                    gattCustomCallbacks.onError(new NotificationCharacteristicException(notificationDescriptor.getCharacteristic(), notificationDescriptor, throwable.getMessage()));
                })
                .unsubscribe();*/
    }


    public void setNotification(UUID notificationUUID, boolean enabled) {
        if (notificationCharacteristic == null || !notificationCharacteristic.getUuid().equals(notificationUUID)) {
            notificationCharacteristic = findCharacteristic(notificationUUID);
        }
        setNotification(notificationCharacteristic, enabled);
    }


    public boolean isNotificationEnabled(BluetoothGattCharacteristic notificationCharacteristic) {
        byte[] descriptorValue = notificationCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID).getValue();
        if (descriptorValue == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) {
            return true;
        } else if (descriptorValue == BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE) {
            return false;
        }
        return false;
    }


    public void readValue(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        bluetoothGatt.readCharacteristic(bluetoothGattCharacteristic);
    }


    public void writeValue(final BluetoothGattCharacteristic bluetoothGattCharacteristic, final List<Byte> dataToWrite) {
        this.writeValue(bluetoothGattCharacteristic, Bytes.toArray(dataToWrite));
    }


    public void writeValue(final UUID writeUUID, final byte[] dataToWrite) {
        if (writeCharacteristic == null || !writeCharacteristic.getUuid().equals(writeUUID)) {
            writeCharacteristic = findCharacteristic(writeUUID);
        }
        writeValue(writeCharacteristic, dataToWrite);
    }


    public void writeValue(final UUID writeUUID, final List<Byte> dataToWrite) {
        this.writeValue(writeUUID, Bytes.toArray(dataToWrite));
    }


    public void writeValue(final BluetoothGattCharacteristic bluetoothGattCharacteristic, final byte[] dataToWrite) {
        Observable
                .create(subscriber -> {
                    if (bluetoothGattCharacteristic != null) {
                        if (dataToWrite.length != 0) {
                            subscriber.onNext(bluetoothGattCharacteristic);
                        } else {
                            subscriber.onError(new Exception("data is Null or Empty"));
                        }
                    } else {
                        subscriber.onError(new Exception("write Characteristic is null"));
                    }
                })
                .map(bytes -> {
                    bluetoothGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    bluetoothGattCharacteristic.setValue(dataToWrite);
                    return bluetoothGattCharacteristic;
                })
                .subscribe(characteristic -> {
                    bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
                }, throwable -> {
                    gattCustomCallbacks.onError(new WriteCharacteristicException(writeCharacteristic, throwable.getMessage()));
                })
                .unsubscribe();
    }


    public void readBatteryValue() {
        readValue(bluetoothGatt.getService(BATTERY_SERVICE_UUID).getCharacteristic(BATTERY_CHARACTERISTIC_UUID));
    }


    private BluetoothGattCharacteristic findCharacteristic(UUID uuid) {

        List<BluetoothGattService> serviceList = bluetoothGatt.getServices();
        List<BluetoothGattCharacteristic> characteristicList;

        for (BluetoothGattService service : serviceList) {
            characteristicList = service.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic : characteristicList) {
                if (characteristic.getUuid().equals(uuid)) {
                    return characteristic;
                }
            }
        }
        gattCustomCallbacks.onError(new GattResourceNotDiscoveredException("UUID NOT MATCH!"));
        return null;
    }


    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {


        @Override
        public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {

                startServiceDiscovery();

                gattCustomCallbacks.onDeviceConnected();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                gattCustomCallbacks.onDeviceDisconnected();
                closeGatt();
            }
            readRssiValue();
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gattCustomCallbacks.onServicesFound(bluetoothGatt);
                Log.i(TAG, "ServicesDiscovered SUCCESS");
            } else {
                gattCustomCallbacks.onError(new GattResourceNotDiscoveredException("ServicesDiscovered FAIL"));
                Log.i(TAG, "ServicesDiscovered FAIL");
            }
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gattCustomCallbacks.onReadSuccess(characteristic);
                Log.i(TAG, "CharacteristicRead SUCCESS " + characteristic.getUuid().toString());
            } else {
                gattCustomCallbacks.onError(new ReadCharacteristicException(characteristic, "Check Gatt Service Available or Device Connection!", status));
                Log.i(TAG, "CharacteristicRead FAIL " + characteristic.getUuid().toString());
            }
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic) {
            gattCustomCallbacks.onDeviceNotify(characteristic);
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gattCustomCallbacks.onWriteSuccess();
                Log.i(TAG, "CharacteristicWrite SUCCESS " + characteristic.getUuid().toString());
            } else {
                gattCustomCallbacks.onError(new WriteCharacteristicException(characteristic, "Check Gatt Service Available or Device Connection!", status));
                Log.i(TAG, "CharacteristicWrite FAIL " + characteristic.getUuid().toString());
            }
        }


        @Override
        public void onReadRemoteRssi(BluetoothGatt bluetoothGatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gattCustomCallbacks.onRSSIUpdate(rssi);
            } else {
                gattCustomCallbacks.onError(new RssiMissException(status));
            }
        }


        @Override
        public void onDescriptorWrite(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                gattCustomCallbacks.onSetNotificationSuccess();

                Log.i(TAG, "DescriptorWrite SUCCESS " + descriptor.getCharacteristic().getUuid().toString() + " \n " + descriptor.getUuid().toString());

                if (descriptor.equals(notificationDescriptor)) {
                    gattCustomCallbacks.onDeviceReady();
                }

            } else {
                gattCustomCallbacks.onError(new NotificationCharacteristicException(descriptor.getCharacteristic(), descriptor, "DescriptorWrite FAIL", status));
            }
        }

    };
}
