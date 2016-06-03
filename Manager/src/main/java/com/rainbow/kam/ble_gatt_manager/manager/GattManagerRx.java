
package com.rainbow.kam.ble_gatt_manager.manager;

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
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.common.primitives.Bytes;
import com.rainbow.kam.ble_gatt_manager.data.attributes.GattAttributes;
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
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by kam6512 on 2015-10-29.
 */

public class GattManagerRx {

    private final static String TAG = GattManagerRx.class.getSimpleName();

    private final static long RSSI_UPDATE_TIME_INTERVAL = 3;

    private final static UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG);
    private final static UUID BATTERY_CHARACTERISTIC_UUID = UUID.fromString(GattAttributes.BATTERY_CHARACTERISTIC_UUID);

    private BleDevice bleDevice;
    private String deviceAddress;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothDevice bluetoothDevice;

    private BluetoothGattDescriptor notificationDescriptor;

    private Subscriber<? super Boolean> gattConnectionSubscriber;
    private Subscriber<? super BluetoothGattCharacteristic> gattReadSubscriber;
    private Subscriber<? super BluetoothGattCharacteristic> gattWriteSubscriber;
    private Subscriber<? super BluetoothGattCharacteristic> gattNotificationSubscriber;
    private Subscriber<? super Integer> rssiSubscriber;
    private Subscription rssiTimerSubscription;

    private static final GattResourceNotDiscoveredException UUID_CHARACTERLESS;

    static {
        UUID_CHARACTERLESS = new GattResourceNotDiscoveredException("CHECK UUID / CHARACTERISTIC");
    }


    private GattManagerRx() {
    }


    private static class GattManagerHolder {
        private static final GattManagerRx instance = new GattManagerRx();
    }


    public static GattManagerRx getInstance() {
        return GattManagerHolder.instance;
    }


    public Observable<Boolean> establishConnection(final Application application) {
        return establishConnection(application, this.bleDevice);
    }


    public Observable<Boolean> establishConnection(@NonNull final Application application, @NonNull final BleDevice bleDevice) {

        return Observable.create((Observable.OnSubscribe<Boolean>) subscriber -> {

            gattConnectionSubscriber = subscriber;

            if (application == null) {
                gattConnectionSubscriber.onError(new NullPointerException("Application is Null"));
            }

            if (bleDevice == null) {
                gattConnectionSubscriber.onError(new NullPointerException("BleDevice is Null"));
            }

            this.bleDevice = bleDevice;
            deviceAddress = bleDevice.getAddress();


            if (TextUtils.isEmpty(deviceAddress)) {
                gattConnectionSubscriber.onError(new ConnectedFailException(deviceAddress, "Address is not available"));
            }


            if (bluetoothManager == null || bluetoothAdapter == null) {
                bluetoothManager = (BluetoothManager) application.getSystemService(Context.BLUETOOTH_SERVICE);
                bluetoothAdapter = bluetoothManager.getAdapter();
            }


            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                gattConnectionSubscriber.onError(new ConnectedFailException(deviceAddress, "Adapter is not available or disabled"));
            }

            bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);

            if (bluetoothDevice == null) {
                gattConnectionSubscriber.onError(new ConnectedFailException(deviceAddress, "this Device not Supported BLE"));
            }

            if (isConnected()) {
                gattConnectionSubscriber.onNext(true);
            } else {
                bluetoothGatt = bluetoothDevice.connectGatt(application, false, bluetoothGattCallback);
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
    }


    public BluetoothGatt getGatt() {
        return bluetoothGatt;
    }


    public BleDevice getBleDevice() {
        return bleDevice;
    }


    public void disconnect() {
        if (bluetoothGatt != null && bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled() && isConnected()) {
                bluetoothGatt.disconnect();
            } else {
                bluetoothGatt.close();
                gattConnectionSubscriber.onError(new DisconnectedFailException(bleDevice.getAddress(), "Device already Disconnected"));
            }
        } else {
            gattConnectionSubscriber.onError(new DisconnectedFailException(bleDevice.getAddress(), "Gatt / Adapter is not available or disabled"));
        }
    }


    public void closeGatt() {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
        }
        disableAllGattSubscriber();
    }


    private void disableAllGattSubscriber() {
        if (rssiSubscriber != null) {
            rssiSubscriber.unsubscribe();
        }
        if (gattConnectionSubscriber != null) {
            gattConnectionSubscriber.unsubscribe();
        }
        if (gattReadSubscriber != null) {
            gattReadSubscriber.unsubscribe();
        }
        if (gattWriteSubscriber != null) {
            gattWriteSubscriber.unsubscribe();
        }
        if (gattNotificationSubscriber != null) {
            gattNotificationSubscriber.unsubscribe();
        }
    }


    public boolean isConnected() {
        return bluetoothManager.getConnectionState(bleDevice.getDevice(), BluetoothProfile.GATT) == BluetoothProfile.STATE_CONNECTED;
    }


    public Observable<Integer> readRssiValue() {
        return Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
            rssiSubscriber = subscriber;
            if (isConnected()) {
                rssiTimerSubscription = Observable.interval(RSSI_UPDATE_TIME_INTERVAL, TimeUnit.SECONDS)
                        .subscribe(aLong -> bluetoothGatt.readRemoteRssi());
            } else {
                rssiSubscriber.unsubscribe();
            }
        }).doOnUnsubscribe(() -> {
            if (rssiTimerSubscription != null) {
                rssiTimerSubscription.unsubscribe();
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
    }


    private void startServiceDiscovery() {
        bluetoothGatt.discoverServices();
    }


    public Observable<BluetoothGattCharacteristic> readBatteryValue() {
        return readValue(BATTERY_CHARACTERISTIC_UUID);
    }


    public Observable<BluetoothGattCharacteristic> readValue(final UUID readUUID) {
        return readValue(findCharacteristic(readUUID));

    }


    public Observable<BluetoothGattCharacteristic> readValue(final BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        return Observable.create((Observable.OnSubscribe<BluetoothGattCharacteristic>) subscriber -> {
            gattReadSubscriber = subscriber;
            if (bluetoothGattCharacteristic != null) {
                bluetoothGatt.readCharacteristic(bluetoothGattCharacteristic);
            } else {
                gattReadSubscriber.onError(UUID_CHARACTERLESS);
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
    }


    public Observable<BluetoothGattCharacteristic> writeValue(final UUID writeUUID, final List<Byte> dataToWrite) {
        return writeValue(findCharacteristic(writeUUID), Bytes.toArray(dataToWrite));
    }


    public Observable<BluetoothGattCharacteristic> writeValue(final UUID writeUUID, final byte[] dataToWrite) {
        return writeValue(findCharacteristic(writeUUID), dataToWrite);
    }


    public Observable<BluetoothGattCharacteristic> writeValue(final BluetoothGattCharacteristic bluetoothGattCharacteristic,
                                                              final List<Byte> dataToWrite) {
        return writeValue(bluetoothGattCharacteristic, Bytes.toArray(dataToWrite));
    }


    public Observable<BluetoothGattCharacteristic> writeValue(final BluetoothGattCharacteristic bluetoothGattCharacteristic,
                                                              final byte[] dataToWrite) {
        return Observable.create((Observable.OnSubscribe<BluetoothGattCharacteristic>) subscriber -> {
            gattWriteSubscriber = subscriber;
            if (bluetoothGattCharacteristic != null) {
                if (dataToWrite.length != 0) {
                    bluetoothGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    bluetoothGattCharacteristic.setValue(dataToWrite);
                    bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
                } else {
                    gattWriteSubscriber.onError(new WriteCharacteristicException(bluetoothGattCharacteristic, "data is Null or Empty"));
                }
            } else {
                gattWriteSubscriber.onError(UUID_CHARACTERLESS);
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
    }


    public Observable<BluetoothGattCharacteristic> setNotification(UUID notificationUUID, boolean enabled) {
        return setNotification(findCharacteristic(notificationUUID), enabled);
    }


    public Observable<BluetoothGattCharacteristic> setNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        return Observable.create((Observable.OnSubscribe<BluetoothGattCharacteristic>) subscriber -> {
            gattNotificationSubscriber = subscriber;
            if (characteristic != null) {
                bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
                notificationDescriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
                byte[] value = enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                notificationDescriptor.setValue(value);
                bluetoothGatt.writeDescriptor(notificationDescriptor);
            } else {
                gattNotificationSubscriber.onError(UUID_CHARACTERLESS);
            }

        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
    }


    public Observable<Boolean> isNotificationEnabled(BluetoothGattCharacteristic characteristic) {
        return Observable.create((Observable.OnSubscribe<Boolean>) subscriber -> {
            if (characteristic != null) {
                byte[] descriptorValue = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID).getValue();
                if (descriptorValue == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) {
                    subscriber.onNext(true);
                } else {
                    subscriber.onNext(false);
                }
            } else {
                subscriber.onError(UUID_CHARACTERLESS);
            }
        });
    }


    public BluetoothGattCharacteristic findCharacteristic(UUID uuid) {
        for (BluetoothGattService service : bluetoothGatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (characteristic.getUuid().equals(uuid)) {
                    return characteristic;
                }
            }
        }
        return null;
    }


    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {


        @Override
        public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gattConnectionSubscriber.onNext(true);
                startServiceDiscovery();
            } else {
                gattConnectionSubscriber.onNext(false);
                closeGatt();
            }
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gattConnectionSubscriber.onCompleted();
            } else {
                gattConnectionSubscriber.onError(new GattResourceNotDiscoveredException("ServicesDiscovered FAIL"));
            }
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gattReadSubscriber.onNext(characteristic);
            } else {
                gattReadSubscriber.onError(new ReadCharacteristicException(characteristic, "Check Gatt Service Available or Device Connection!", status));
            }
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic) {
            gattWriteSubscriber.onNext(characteristic);
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "CharacteristicWrite SUCCESS " + characteristic.getUuid().toString());
            } else {
                gattWriteSubscriber.onError(new WriteCharacteristicException(characteristic, "Check Gatt Service Available or Device Connection!", status));
            }
        }


        @Override
        public void onReadRemoteRssi(BluetoothGatt bluetoothGatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                rssiSubscriber.onNext(rssi);
            } else {
                rssiSubscriber.onError(new RssiMissException(status));
            }
        }


        @Override
        public void onDescriptorWrite(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS && descriptor.equals(notificationDescriptor)) {
                gattNotificationSubscriber.onNext(descriptor.getCharacteristic());
            } else {
                gattNotificationSubscriber.onError(new NotificationCharacteristicException(descriptor.getCharacteristic(), descriptor, "DescriptorWrite FAIL", status));
            }
        }
    };
}
