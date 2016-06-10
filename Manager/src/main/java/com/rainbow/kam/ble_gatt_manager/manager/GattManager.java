
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
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.common.primitives.Bytes;
import com.rainbow.kam.ble_gatt_manager.data.BleDevice;
import com.rainbow.kam.ble_gatt_manager.util.BluetoothGatts;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.ConnectedFailException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.DisconnectedFailException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.GattResourceNotDiscoveredException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.NotificationCharacteristicException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.ReadCharacteristicException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.RssiMissException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.WriteCharacteristicException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class GattManager {

    private final static String TAG = GattManager.class.getSimpleName();

    private final static long RSSI_UPDATE_TIME_INTERVAL = 3;

    private final static UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = UUID.fromString(BluetoothGatts.CLIENT_CHARACTERISTIC_CONFIG);
    private final static UUID BATTERY_CHARACTERISTIC_UUID = UUID.fromString(BluetoothGatts.BATTERY_CHARACTERISTIC_UUID);

    private BleDevice device;
    private String deviceAddress;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothDevice bluetoothDevice;

    private BluetoothGattDescriptor notificationDescriptor;

    private PublishSubject<Boolean> connectionSubject = PublishSubject.create();
    private PublishSubject<List<BluetoothGattService>> serviceSubject = PublishSubject.create();
    private PublishSubject<BluetoothGattCharacteristic> readSubject = PublishSubject.create();
    private PublishSubject<BluetoothGattCharacteristic> writeSubject = PublishSubject.create();
    private PublishSubject<BluetoothGattCharacteristic> notificationSubject = PublishSubject.create();
    private PublishSubject<Integer> rssiSubject = PublishSubject.create();

    private Subscription rssiTimerSubscription;

    private static final GattResourceNotDiscoveredException UUID_CHARACTERLESS;

    static {
        UUID_CHARACTERLESS = new GattResourceNotDiscoveredException("CHECK UUID / CHARACTERISTIC");
    }


    private GattManager() {
    }


    private static class GattManagerHolder {
        private static final GattManager instance = new GattManager();
    }


    public static GattManager getInstance() {
        return GattManagerHolder.instance;
    }


    public Observable<Boolean> establishConnection(final Application application) {
        return establishConnection(application, this.device);
    }


    public Observable<Boolean> establishConnection(@NonNull final Application application, @NonNull final BleDevice bleDevice) {
        return Observable.merge(connectionSubject = PublishSubject.create(), Observable.create((Observable.OnSubscribe<Boolean>) subscriber -> {
            if (application == null) {
                subscriber.onError(new NullPointerException("Application is Null"));
            }

            if (bleDevice == null) {
                subscriber.onError(new NullPointerException("BleDevice is Null"));
            }

            device = bleDevice;
            deviceAddress = bleDevice.getAddress();


            if (TextUtils.isEmpty(deviceAddress)) {
                subscriber.onError(new ConnectedFailException(deviceAddress, "Address is not available"));
            }


            if (bluetoothManager == null || bluetoothAdapter == null) {
                bluetoothManager = (BluetoothManager) application.getSystemService(Context.BLUETOOTH_SERVICE);
                bluetoothAdapter = bluetoothManager.getAdapter();
            }


            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                subscriber.onError(new ConnectedFailException(deviceAddress, "Adapter is not available or disabled"));
            }

            bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);

            if (bluetoothDevice == null) {
                subscriber.onError(new ConnectedFailException(deviceAddress, "this Device not Supported BLE"));
            }

            if (isConnected()) {
                subscriber.onNext(true);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    bluetoothGatt = bluetoothDevice.connectGatt(application, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
                } else {
                    bluetoothGatt = bluetoothDevice.connectGatt(application, false, bluetoothGattCallback);
                }
            }
        })).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
    }


    public BluetoothGatt getGatt() {
        return bluetoothGatt;
    }


    public BleDevice getBleDevice() {
        return device;
    }


    public void disconnect() {
        if (bluetoothGatt != null && bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled() && isConnected()) {
                bluetoothGatt.disconnect();
            } else {
                bluetoothGatt.close();
                connectionSubject.onError(new DisconnectedFailException(device.getAddress(), "Device already Disconnected"));
            }
        } else {
            connectionSubject.onError(new DisconnectedFailException(device.getAddress(), "Gatt / Adapter is not available or disabled"));
        }
    }


    public void closeGatt() {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
        }
        disableAllGattSubject();
    }


    private void disableAllGattSubject() {
        if (connectionSubject != null) {
            connectionSubject.onCompleted();
        }
        if (serviceSubject != null) {
            serviceSubject.onCompleted();
        }
        if (readSubject != null) {
            readSubject.onCompleted();
        }
        if (writeSubject != null) {
            writeSubject.onCompleted();
        }
        if (notificationSubject != null) {
            notificationSubject.onCompleted();
        }
        if (rssiSubject != null) {
            rssiSubject.onCompleted();
        }
        if (rssiTimerSubscription != null) {
            rssiTimerSubscription.unsubscribe();
        }
    }


    public boolean isConnected() {
        return bluetoothManager.getConnectionState(device.getDevice(), BluetoothProfile.GATT) == BluetoothProfile.STATE_CONNECTED;
    }


    public Observable<Integer> readRssiValue() {
        return Observable.merge(rssiSubject = PublishSubject.create(), Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
                    if (isConnected()) {
                        rssiTimerSubscription = Observable.interval(RSSI_UPDATE_TIME_INTERVAL, TimeUnit.SECONDS)
                                .subscribe(aLong -> bluetoothGatt.readRemoteRssi());
                    } else {
                        subscriber.unsubscribe();
                    }
                }
        )).doOnSubscribe(() -> {
            if (rssiTimerSubscription != null) {
                rssiTimerSubscription.unsubscribe();
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
    }


    public Observable<List<BluetoothGattService>> discoverService() {
        return Observable.merge(serviceSubject = PublishSubject.create(), Observable.create((Observable.OnSubscribe<List<BluetoothGattService>>) subscriber -> bluetoothGatt.discoverServices()));
    }


    public Observable<BluetoothGattCharacteristic> readBatteryValue() {
        return readValue(BATTERY_CHARACTERISTIC_UUID);
    }


    public Observable<BluetoothGattCharacteristic> readValue(final UUID readUUID) {
        return readValue(findCharacteristic(readUUID));

    }


    public Observable<BluetoothGattCharacteristic> readValue(final BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        return Observable.merge(readSubject = PublishSubject.create(), Observable.create((Observable.OnSubscribe<BluetoothGattCharacteristic>) subscriber -> {
            if (bluetoothGattCharacteristic != null) {
                bluetoothGatt.readCharacteristic(bluetoothGattCharacteristic);
            } else {
                subscriber.onError(UUID_CHARACTERLESS);
            }
        }));
    }


    public Observable<BluetoothGattCharacteristic> writeValue(
            final UUID writeUUID, final List<Byte> dataToWrite) {
        return writeValue(findCharacteristic(writeUUID), Bytes.toArray(dataToWrite));
    }


    public Observable<BluetoothGattCharacteristic> writeValue(
            final UUID writeUUID, final byte[] dataToWrite) {
        return writeValue(findCharacteristic(writeUUID), dataToWrite);
    }


    public Observable<BluetoothGattCharacteristic> writeValue(
            final BluetoothGattCharacteristic bluetoothGattCharacteristic,
            final List<Byte> dataToWrite) {
        return writeValue(bluetoothGattCharacteristic, Bytes.toArray(dataToWrite));
    }


    public Observable<BluetoothGattCharacteristic> writeValue(final BluetoothGattCharacteristic bluetoothGattCharacteristic, final byte[] dataToWrite) {
        return Observable.merge(writeSubject = PublishSubject.create(), Observable.create((Observable.OnSubscribe<BluetoothGattCharacteristic>) subscriber -> {
            if (bluetoothGattCharacteristic != null) {
                if (dataToWrite.length != 0) {
                    bluetoothGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    bluetoothGattCharacteristic.setValue(dataToWrite);
                    bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
                } else {
                    subscriber.onError(new WriteCharacteristicException(bluetoothGattCharacteristic, "data is Null or Empty"));
                }
            } else {
                subscriber.onError(UUID_CHARACTERLESS);
            }
        })).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
    }


    public Observable<BluetoothGattCharacteristic> setNotification(UUID
                                                                           notificationUUID, boolean enabled) {
        return setNotification(findCharacteristic(notificationUUID), enabled);
    }


    public Observable<BluetoothGattCharacteristic> setNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        return Observable.merge(notificationSubject = PublishSubject.create(), Observable.create((Observable.OnSubscribe<BluetoothGattCharacteristic>) subscriber -> {
            if (characteristic != null) {
                bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
                notificationDescriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
                byte[] value = enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                notificationDescriptor.setValue(value);
                bluetoothGatt.writeDescriptor(notificationDescriptor);
            } else {
                subscriber.onError(UUID_CHARACTERLESS);
            }
        })).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());

    }


    public Observable<Boolean> isNotificationEnabled
            (BluetoothGattCharacteristic characteristic) {
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
                connectionSubject.onNext(true);
            } else {
                connectionSubject.onNext(false);
                closeGatt();
            }
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                serviceSubject.onNext(bluetoothGatt.getServices());
            } else {
                serviceSubject.onError(new GattResourceNotDiscoveredException("ServicesDiscovered FAIL"));
            }
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                readSubject.onNext(characteristic);
            } else {
                readSubject.onError(new ReadCharacteristicException(characteristic, "Check Gatt Service Available or Device Connection!", status));
            }
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic) {
            writeSubject.onNext(characteristic);
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                writeSubject.onNext(characteristic);
                Log.i(TAG, "CharacteristicWrite SUCCESS " + characteristic.getUuid().toString());
            } else {
                writeSubject.onError(new WriteCharacteristicException(characteristic, "Check Gatt Service Available or Device Connection!", status));
            }
        }


        @Override
        public void onReadRemoteRssi(BluetoothGatt bluetoothGatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                rssiSubject.onNext(rssi);
            } else {
                rssiSubject.onError(new RssiMissException(status));
            }
        }


        @Override
        public void onDescriptorWrite(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS && descriptor.equals(notificationDescriptor)) {
                notificationSubject.onNext(descriptor.getCharacteristic());
            } else {
                notificationSubject.onError(new NotificationCharacteristicException(descriptor.getCharacteristic(), descriptor, "DescriptorWrite FAIL", status));
            }
        }

    };
}
