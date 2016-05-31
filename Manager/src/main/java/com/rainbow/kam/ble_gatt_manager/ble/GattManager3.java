
package com.rainbow.kam.ble_gatt_manager.ble;

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
import com.rainbow.kam.ble_gatt_manager.attributes.GattAttributes;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.ConnectedFailException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.DisconnectedFailException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.GattResourceNotDiscoveredException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.NotificationCharacteristicException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.ReadCharacteristicException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.WriteCharacteristicException;
import com.rainbow.kam.ble_gatt_manager.scanner.BleDevice;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import hugo.weaving.DebugLog;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by kam6512 on 2015-10-29.
 */

public class GattManager3 {

    private final static String TAG = GattManager3.class.getSimpleName();

    private final static long RSSI_UPDATE_TIME_INTERVAL = 3;

    private final static UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG);
    private final static UUID BATTERY_CHARACTERISTIC_UUID = UUID.fromString(GattAttributes.BATTERY_CHARACTERISTIC_UUID);


    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattDescriptor notificationDescriptor;

    private Subscriber<? super Boolean> gattConnectionSubscriber;
    private Subscriber<? super BluetoothGattCharacteristic> gattReadSubscriber;
    private Subscriber<? super BluetoothGattCharacteristic> gattWriteSubscriber;
    private Subscriber<? super BluetoothGattCharacteristic> gattNotificationSubscriber;
    private Subscriber<? super Integer> rssiSubscriber;

    private Subscription rssiTimerSubscription;


    private GattManager3() {
    }


    private static class GattManagerHolder {
        private static final GattManager3 instance = new GattManager3();
    }


    @DebugLog
    public static GattManager3 getInstance() {
        return GattManagerHolder.instance;
    }


    @DebugLog
    public Observable<Boolean> establishConnection(final Application application, final BleDevice bleDevice) {

        return Observable.create((Observable.OnSubscribe<Boolean>) subscriber -> {
            gattConnectionSubscriber = subscriber;

            if (bluetoothManager == null || bluetoothAdapter == null) {
                bluetoothManager = (BluetoothManager) application.getSystemService(Context.BLUETOOTH_SERVICE);
                bluetoothAdapter = bluetoothManager.getAdapter();
            }


            String deviceAddress = bleDevice.getAddress();

            if (!bluetoothAdapter.isEnabled()) {
                gattConnectionSubscriber.onError(new ConnectedFailException(deviceAddress, "Adapter is disabled"));
            }
            if (TextUtils.isEmpty(deviceAddress)) {
                gattConnectionSubscriber.onError(new ConnectedFailException(deviceAddress, "Address is not available"));
            }

            bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);

            if (bluetoothDevice == null) {
                gattConnectionSubscriber.onError(new ConnectedFailException(deviceAddress, "RemoteDevice is not available"));
            }

            isConnected().subscribe(isConnected -> {
                if (isConnected) closeGatt();
            }).unsubscribe();

            bluetoothGatt = bluetoothDevice.connectGatt(application, false, bluetoothGattCallback);

        });
    }


    @DebugLog
    public BluetoothGatt getGatt() {
        return bluetoothGatt;
    }


    @DebugLog
    public BleDevice getBleDevice() {
        return BleDevice.create(bluetoothGatt.getDevice());
    }


    @DebugLog
    public void disconnect() {
        if (bluetoothGatt != null) {
            String deviceAddress = bluetoothGatt.getDevice().getAddress();
            isConnected().filter(aBoolean -> bluetoothAdapter.isEnabled()).subscribe(isConnected -> {
                if (isConnected) {
                    bluetoothGatt.disconnect();
                } else {
                    bluetoothGatt.close();
                    gattConnectionSubscriber.onError(new DisconnectedFailException(deviceAddress, "Device already Disconnected"));
                }
            });
        }
    }


    @DebugLog
    public void closeGatt() {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
        }
        gattConnectionSubscriber.unsubscribe();
        gattReadSubscriber.unsubscribe();
        gattWriteSubscriber.unsubscribe();
        gattNotificationSubscriber.unsubscribe();
        rssiTimerSubscription.unsubscribe();
    }


    @DebugLog
    public Observable<Boolean> isConnected() {
        return Observable.create((Observable.OnSubscribe<Boolean>) subscriber ->
                Observable.from(bluetoothManager.getConnectedDevices(BluetoothProfile.GATT))
                        .filter(device -> bluetoothDevice != null && bluetoothDevice.getAddress().equals(device.getAddress()))
                        .subscribe(device -> {
                            subscriber.onNext(true);
                        }).unsubscribe());
    }


    @DebugLog
    private void readRssiValue() {
        isConnected().subscribe(isConnected -> {
            if (isConnected) {
                rssiTimerSubscription = Observable.interval(RSSI_UPDATE_TIME_INTERVAL, TimeUnit.SECONDS).subscribe(aLong -> {
                    bluetoothGatt.readRemoteRssi();
                });
            } else {
                if (rssiTimerSubscription != null) {
                    rssiTimerSubscription.unsubscribe();
                }
            }
        }).unsubscribe();
    }


    @DebugLog
    private void startServiceDiscovery() {
        bluetoothGatt.discoverServices();
    }


    @DebugLog
    public Observable<BluetoothGattCharacteristic> readBatteryValue() {
        return readValue(BATTERY_CHARACTERISTIC_UUID);
    }


    @DebugLog
    public Observable<BluetoothGattCharacteristic> readValue(final UUID readUUID) {
        return readValue(findCharacteristic(readUUID));

    }


    @DebugLog
    public Observable<BluetoothGattCharacteristic> readValue(final BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        return Observable.create(new Observable.OnSubscribe<BluetoothGattCharacteristic>() {
            @Override
            public void call(Subscriber<? super BluetoothGattCharacteristic> subscriber) {
                gattReadSubscriber = subscriber;
                if (bluetoothGattCharacteristic == null) {
                    gattReadSubscriber.onError(new GattResourceNotDiscoveredException("UUID NOT MATCH!"));
                } else {
                    bluetoothGatt.readCharacteristic(bluetoothGattCharacteristic);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread());
    }


    @DebugLog
    public Observable<BluetoothGattCharacteristic> writeValue(final UUID writeUUID, final List<Byte> dataToWrite) {
        return writeValue(findCharacteristic(writeUUID), Bytes.toArray(dataToWrite));
    }


    @DebugLog
    public Observable<BluetoothGattCharacteristic> writeValue(final UUID writeUUID, final byte[] dataToWrite) {
        return writeValue(findCharacteristic(writeUUID), dataToWrite);
    }


    @DebugLog
    public Observable<BluetoothGattCharacteristic> writeValue(final BluetoothGattCharacteristic bluetoothGattCharacteristic, final List<Byte> dataToWrite) {
        return writeValue(bluetoothGattCharacteristic, Bytes.toArray(dataToWrite));
    }


    @DebugLog
    public Observable<BluetoothGattCharacteristic> writeValue(final BluetoothGattCharacteristic bluetoothGattCharacteristic, final byte[] dataToWrite) {
        return Observable.create(new Observable.OnSubscribe<BluetoothGattCharacteristic>() {
            @Override
            public void call(Subscriber<? super BluetoothGattCharacteristic> subscriber) {
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
                    gattWriteSubscriber.onError(new WriteCharacteristicException(bluetoothGattCharacteristic, "write Characteristic is null"));
                }
            }
        }).observeOn(AndroidSchedulers.mainThread());
    }


    @DebugLog
    public Observable<BluetoothGattCharacteristic> setNotification(UUID notificationUUID, boolean enabled) {
        return setNotification(findCharacteristic(notificationUUID), enabled);
    }


    @DebugLog
    public Observable<BluetoothGattCharacteristic> setNotification(BluetoothGattCharacteristic notificationCharacteristic, boolean enabled) {

        return Observable.create(new Observable.OnSubscribe<BluetoothGattCharacteristic>() {
            @Override
            public void call(Subscriber<? super BluetoothGattCharacteristic> subscriber) {
                gattNotificationSubscriber = subscriber;
                bluetoothGatt.setCharacteristicNotification(notificationCharacteristic, enabled);
                notificationDescriptor = notificationCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
                byte[] value = enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                notificationDescriptor.setValue(value);
                bluetoothGatt.writeDescriptor(notificationDescriptor);
            }
        }).observeOn(AndroidSchedulers.mainThread());
    }


    @DebugLog
    public Observable<Boolean> isNotificationEnabled(BluetoothGattCharacteristic notificationCharacteristic) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override public void call(Subscriber<? super Boolean> subscriber) {
                byte[] descriptorValue = notificationCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID).getValue();
                if (descriptorValue == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) {
                    subscriber.onNext(true);
                } else if (descriptorValue == BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE) {
                    subscriber.onNext(false);
                }
                subscriber.onNext(false);
            }
        });
    }


    @DebugLog
    private BluetoothGattCharacteristic findCharacteristic(UUID uuid) {
        for (BluetoothGattService service : bluetoothGatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (characteristic.getUuid().equals(uuid)) {
                    return characteristic;
                }
            }
        }
        return null;
//        gattCustomCallbacks.onError(new GattResourceNotDiscoveredException("UUID NOT MATCH!"));
    }


    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {


        @DebugLog @Override
        public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gattConnectionSubscriber.onNext(true);
                startServiceDiscovery();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                gattConnectionSubscriber.onNext(false);
                closeGatt();
//                gattConnectionSubscriber.unsubscribe();
            }
            readRssiValue();
        }


        @DebugLog @Override
        public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gattConnectionSubscriber.onCompleted();
                Log.i(TAG, "ServicesDiscovered SUCCESS");
            } else {
                gattConnectionSubscriber.onError(new GattResourceNotDiscoveredException("ServicesDiscovered FAIL"));
                Log.i(TAG, "ServicesDiscovered FAIL");
            }
        }


        @DebugLog @Override
        public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gattReadSubscriber.onNext(characteristic);
                Log.i(TAG, "CharacteristicRead SUCCESS " + characteristic.getUuid().toString());
            } else {
                gattReadSubscriber.onError(new ReadCharacteristicException(characteristic, "Check Gatt Service Available or Device Connection!", status));
                Log.i(TAG, "CharacteristicRead FAIL " + characteristic.getUuid().toString());
            }
        }


        @DebugLog @Override
        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic) {
//            gattNotificationSubscriber.onNext(characteristic);
            gattWriteSubscriber.onNext(characteristic);
        }


        @DebugLog @Override
        public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                gattWriteSubscriber.onNext(characteristic);
                Log.i(TAG, "CharacteristicWrite SUCCESS " + characteristic.getUuid().toString());
            } else {
                gattWriteSubscriber.onError(new WriteCharacteristicException(characteristic, "Check Gatt Service Available or Device Connection!", status));
                Log.i(TAG, "CharacteristicWrite FAIL " + characteristic.getUuid().toString());
            }
        }


        @DebugLog @Override
        public void onReadRemoteRssi(BluetoothGatt bluetoothGatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                gattCustomCallbacks.onRSSIUpdate(rssi);
            } else {
//                gattCustomCallbacks.onError(new RssiMissException(status));
            }
        }


        @DebugLog @Override
        public void onDescriptorWrite(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS && descriptor.equals(notificationDescriptor)) {
                gattNotificationSubscriber.onNext(descriptor.getCharacteristic());
            } else {
                gattNotificationSubscriber.onError(new NotificationCharacteristicException(descriptor.getCharacteristic(), descriptor, "DescriptorWrite FAIL", status));
            }
        }

    };
}
