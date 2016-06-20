
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

import com.google.common.base.Strings;
import com.google.common.primitives.Bytes;
import com.rainbow.kam.ble_gatt_manager.data.BleDevice;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.ConnectedFailException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.DisconnectedFailException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.GattResourceNotDiscoveredException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.NotificationCharacteristicException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.ReadCharacteristicException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.RssiMissException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.WriteCharacteristicException;
import com.rainbow.kam.ble_gatt_manager.util.BluetoothGatts;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class GattManager implements GattManagerObserves {

    private final static String TAG = GattManager.class.getSimpleName();

    private final static long RSSI_UPDATE_TIME_INTERVAL = 3;
    private final Application app;

    private BleDevice device;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothDevice bluetoothDevice;

    private PublishSubject<Boolean> connectionSubject;

    private PublishSubject<Integer> rssiSubject;
    private Subscription rssiTimerSubscription;

    private PublishSubject<List<BluetoothGattService>> serviceSubject;

    private PublishSubject<BluetoothGattCharacteristic> readSubject;
    private PublishSubject<BluetoothGattCharacteristic> writeSubject;

    private PublishSubject<BluetoothGattCharacteristic> notificationSubject;
    private BluetoothGattCharacteristic currentNotificationCharacteristic;
    private PublishSubject<BluetoothGattCharacteristic> indicationSubject;
    private BluetoothGattCharacteristic currentIndicationCharacteristic;


    private static final GattResourceNotDiscoveredException UUID_CHARACTERLESS;

    static {
        UUID_CHARACTERLESS = new GattResourceNotDiscoveredException("CHECK UUID / CHARACTERISTIC");
    }

    @Inject public GattManager(final Application application) {
        app = application;
        setBluetooth();
    }


    private void setBluetooth() {
        bluetoothManager = (BluetoothManager) app.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }


    public BluetoothGatt getGatt() {
        return bluetoothGatt;
    }


    public BleDevice getBleDevice() {
        return device;
    }


    @Override public Observable<Boolean> observeConnection() {
        return observeConnection(device);
    }


    @Override
    public Observable<Boolean> observeConnection(BleDevice bleDevice) {
        return Observable.merge(
                connectionSubject = PublishSubject.create(),
                Observable.create(subscriber -> {
                    device = bleDevice;
                    String deviceAddress = device.getAddress();


                    if (app == null) {
                        subscriber.onError(new NullPointerException("Application is Null"));
                    }

                    if (bluetoothManager == null || bluetoothAdapter == null) {
                        setBluetooth();

                        if (!bluetoothAdapter.isEnabled()) {
                            subscriber.onError(new ConnectedFailException(deviceAddress, "Adapter is not available or disabled"));
                        }
                    }
                    if (device == null) {
                        subscriber.onError(new NullPointerException("BleDevice is Null"));
                    }

                    if (Strings.isNullOrEmpty(deviceAddress)) {
                        subscriber.onError(new ConnectedFailException(deviceAddress, "Address is not available"));
                    }

                    bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);

                    if (bluetoothDevice == null) {
                        subscriber.onError(new ConnectedFailException(deviceAddress, "this Device not Supported BLE"));
                    }


                    if (isConnected()) {
                        subscriber.onNext(true);
                    } else {
                        bluetoothGatt = bluetoothDevice.connectGatt(app, false, bluetoothGattCallback);
                    }
                }));
    }


    public boolean isConnected() {
        return bluetoothManager.getConnectionState(device.getDevice(),
                BluetoothProfile.GATT) == BluetoothProfile.STATE_CONNECTED;
    }


    public void disconnect() {
        if (bluetoothGatt != null && bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled() && isConnected()) {
                bluetoothGatt.disconnect();
            } else {
                closeGatt();
            }
        } else {
            connectionSubject.onError(new DisconnectedFailException(device.getAddress(), "Gatt / Adapter is not available or disabled"));
        }
    }


    private void closeGatt() {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
        }
        disableAllGattSubject();
    }


    private <T> void completeSubject(PublishSubject<T> subject) {
        if (subject != null) {
            subject.onCompleted();
        }
    }


    private void disableAllGattSubject() {
        completeSubject(connectionSubject);
        completeSubject(serviceSubject);
        completeSubject(readSubject);
        completeSubject(writeSubject);
        completeSubject(notificationSubject);
        completeSubject(indicationSubject);
        completeSubject(rssiSubject);
    }


    @Override public Observable<Integer> observeRssi() {
        rssiSubject = PublishSubject.create();
        Observable<Integer> rssiObservable = Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
            if (isConnected()) {
                rssiTimerSubscription = Observable.interval(RSSI_UPDATE_TIME_INTERVAL, TimeUnit.SECONDS)
                        .subscribe(aLong -> bluetoothGatt.readRemoteRssi());
            } else {
                subscriber.unsubscribe();
            }
        }).doOnUnsubscribe(() -> rssiTimerSubscription.unsubscribe());
        return Observable.merge(rssiSubject, rssiObservable);
    }


    @Override
    public Observable<List<BluetoothGattService>> observeDiscoverService() {
        serviceSubject = PublishSubject.create();
        Observable<List<BluetoothGattService>> discoverGattServiceObservable =
                Observable.create((Observable.OnSubscribe<List<BluetoothGattService>>) subscriber -> bluetoothGatt.discoverServices())
                        ;
        return Observable.merge(serviceSubject, discoverGattServiceObservable);
    }


    @Override
    public Observable<BluetoothGattCharacteristic> observeBattery() {
        return observeRead(BluetoothGatts.BATTERY_CHARACTERISTIC_UUID);
    }


    @Override
    public Observable<BluetoothGattCharacteristic> observeRead(UUID uuidToRead) {
        return observeRead(findCharacteristic(uuidToRead));
    }


    @Override
    public Observable<BluetoothGattCharacteristic> observeRead(BluetoothGattCharacteristic characteristicToRead) {
        readSubject = PublishSubject.create();
        Observable<BluetoothGattCharacteristic> readObservable = Observable.create((Observable.OnSubscribe<BluetoothGattCharacteristic>) subscriber -> {
            if (characteristicToRead != null) {
                bluetoothGatt.readCharacteristic(characteristicToRead);
            } else {
                subscriber.onError(UUID_CHARACTERLESS);
            }
        });
        return Observable.merge(readSubject, readObservable);
    }


    @Override
    public Observable<BluetoothGattCharacteristic> observeWrite(UUID uuidToWrite, List<Byte> valuesToWrite) {
        return observeWrite(findCharacteristic(uuidToWrite), valuesToWrite);
    }


    @Override
    public Observable<BluetoothGattCharacteristic> observeWrite(UUID uuidToWrite, byte[] valuesToWrite) {
        return observeWrite(findCharacteristic(uuidToWrite), valuesToWrite);
    }


    @Override
    public Observable<BluetoothGattCharacteristic> observeWrite(BluetoothGattCharacteristic characteristicToWrite, List<Byte> valuesToWrite) {
        return observeWrite(characteristicToWrite, Bytes.toArray(valuesToWrite));
    }


    @Override
    public Observable<BluetoothGattCharacteristic> observeWrite(BluetoothGattCharacteristic characteristicToWrite, byte[] valuesToWrite) {
        writeSubject = PublishSubject.create();
        Observable<BluetoothGattCharacteristic> writeObservable = Observable.create((Observable.OnSubscribe<BluetoothGattCharacteristic>) subscriber -> {
            if (characteristicToWrite != null) {
                if (valuesToWrite == null || valuesToWrite.length == 0) {
                    subscriber.onError(new WriteCharacteristicException(characteristicToWrite, "data is Null or Empty"));
                }
                characteristicToWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                characteristicToWrite.setValue(valuesToWrite);
            } else {
                subscriber.onError(UUID_CHARACTERLESS);
            }
            bluetoothGatt.writeCharacteristic(characteristicToWrite);
        });
        return Observable.merge(writeSubject, writeObservable);
    }


    @Override
    public Observable<BluetoothGattCharacteristic> observeNotification(UUID uuidToNotification, boolean enableNotification) {
        return observeNotification(findCharacteristic(uuidToNotification), enableNotification);
    }


    @Override
    public Observable<BluetoothGattCharacteristic> observeNotification(BluetoothGattCharacteristic characteristicToNotification, boolean enableNotification) {
        notificationSubject = PublishSubject.create();
        currentNotificationCharacteristic = characteristicToNotification;
        Observable<BluetoothGattCharacteristic> notificationObservable =
                Observable.create((Observable.OnSubscribe<BluetoothGattCharacteristic>) subscriber -> {
                    if (currentNotificationCharacteristic != null) {
                        bluetoothGatt.setCharacteristicNotification(currentNotificationCharacteristic, enableNotification);
                        BluetoothGattDescriptor notificationDescriptor;
                        notificationDescriptor = currentNotificationCharacteristic.getDescriptor(BluetoothGatts.CLIENT_CHARACTERISTIC_CONFIG);
                        byte[] value = enableNotification ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                        notificationDescriptor.setValue(value);
                        bluetoothGatt.writeDescriptor(notificationDescriptor);
                    } else {
                        subscriber.onError(UUID_CHARACTERLESS);
                    }
                });
        return Observable.merge(notificationSubject, notificationObservable);
    }


    public Observable<Boolean> isNotificationEnabled(BluetoothGattCharacteristic characteristic) {
        return Observable.create(subscriber -> {
            if (characteristic != null) {
                byte[] descriptorValue = characteristic.getDescriptor(BluetoothGatts.CLIENT_CHARACTERISTIC_CONFIG).getValue();
                boolean isEnabled = (descriptorValue == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                subscriber.onNext(isEnabled);
            } else {
                subscriber.onError(UUID_CHARACTERLESS);
            }
        });
    }


    @Override
    public Observable<BluetoothGattCharacteristic> observeIndication(UUID uuidToIndication, boolean enableIndication) {
        return observeIndication(findCharacteristic(uuidToIndication), enableIndication);
    }


    @Override
    public Observable<BluetoothGattCharacteristic> observeIndication(BluetoothGattCharacteristic characteristicToIndication, boolean enableIndication) {
        indicationSubject = PublishSubject.create();
        currentIndicationCharacteristic = characteristicToIndication;
        Observable<BluetoothGattCharacteristic> indicationObservable =
                Observable.create((Observable.OnSubscribe<BluetoothGattCharacteristic>) subscriber -> {
                    if (currentIndicationCharacteristic != null) {
                        bluetoothGatt.setCharacteristicNotification(currentIndicationCharacteristic, enableIndication);
                        if (enableIndication) {
                            BluetoothGattDescriptor indicationDescriptor;
                            indicationDescriptor = currentIndicationCharacteristic.getDescriptor(BluetoothGatts.CLIENT_CHARACTERISTIC_CONFIG);
                            indicationDescriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                            bluetoothGatt.writeDescriptor(indicationDescriptor);
                        }
                    } else {
                        subscriber.onError(UUID_CHARACTERLESS);
                    }
                });
        return Observable.merge(indicationSubject, indicationObservable);
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
        public void onConnectionStateChange(BluetoothGatt bluetoothGatt,
                                            int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectionSubject.onNext(true);
            } else {
                connectionSubject.onNext(false);
                closeGatt();
            }
        }


        @DebugLog @Override
        public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                serviceSubject.onNext(bluetoothGatt.getServices());
                serviceSubject.onCompleted();
            } else {
                serviceSubject.onError(new GattResourceNotDiscoveredException("ServicesDiscovered FAIL"));
            }
        }


        @DebugLog @Override
        public void onCharacteristicRead(BluetoothGatt bluetoothGatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                readSubject.onNext(characteristic);
                readSubject.onCompleted();
            } else {
                readSubject.onError(
                        new ReadCharacteristicException(characteristic, "Check Gatt Service Available or Device Connection!", status));
            }
        }


        @DebugLog @Override
        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic) {
            if (currentNotificationCharacteristic != null && characteristic.equals(currentNotificationCharacteristic)) {
                notificationSubject.onNext(currentNotificationCharacteristic);
            }
            if (currentIndicationCharacteristic != null && characteristic.equals(currentIndicationCharacteristic)) {
                indicationSubject.onNext(currentIndicationCharacteristic);
            }
        }


        @DebugLog @Override
        public void onCharacteristicWrite(BluetoothGatt bluetoothGatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                writeSubject.onNext(characteristic);
            } else {
                writeSubject.onError(
                        new WriteCharacteristicException(characteristic, "Check Gatt Service Available or Device Connection!", status));
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
            BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                onCharacteristicChanged(bluetoothGatt, characteristic);
            } else {
                NotificationCharacteristicException exception = new NotificationCharacteristicException(descriptor.getCharacteristic(), descriptor, "DescriptorWrite FAIL", status);
                if (currentNotificationCharacteristic != null && characteristic.equals(currentNotificationCharacteristic)) {
                    notificationSubject.onError(exception);
                }
                if (currentIndicationCharacteristic != null && characteristic.equals(currentIndicationCharacteristic)) {
                    indicationSubject.onError(exception);
                }
            }
        }
    };
}