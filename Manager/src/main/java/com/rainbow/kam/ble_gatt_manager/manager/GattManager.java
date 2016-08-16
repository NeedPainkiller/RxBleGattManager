package com.rainbow.kam.ble_gatt_manager.manager;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.primitives.Bytes;
import com.rainbow.kam.ble_gatt_manager.broadcast.BondDeviceBroadcastReceiver;
import com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattConnectException;
import com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattException;
import com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattNotificationCharacteristicException;
import com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattReadCharacteristicException;
import com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattResourceNotDiscoveredException;
import com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattRssiException;
import com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattWriteCharacteristicException;
import com.rainbow.kam.ble_gatt_manager.model.BleDevice;
import com.rainbow.kam.ble_gatt_manager.model.BluetoothGatts;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;

import static android.bluetooth.BluetoothProfile.GATT;
import static com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattConnectException.NONE_ADDRESS;
import static com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattConnectException.NONE_APPLICATION;
import static com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattConnectException.NONE_BLE_DEVICE;
import static com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattConnectException.NONE_BT;
import static com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattConnectException.NOT_CONNECTED;
import static com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattException.STATUS_RESULT_FAIL;
import static com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattNotificationCharacteristicException.DESCRIPTION_WRITE_FAIL;
import static com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattResourceNotDiscoveredException.NONE_SERVICES;
import static com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattResourceNotDiscoveredException.NONE_UUID_CHARACTERISTIC;
import static com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattWriteCharacteristicException.NULL_OR_EMPTY_DATA;
import static com.rainbow.kam.ble_gatt_manager.manager.RxGattListeners.*;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class GattManager implements IGattManager {

    private final Application application;
    private final GattManagerCallBack gattManagerCallBack;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BleDevice bleDevice;

    private Subscription rssiTimerSubscription;

    private BluetoothGattCharacteristic currentWriteCharacteristic;
    private BluetoothGattCharacteristic currentNotificationCharacteristic;
    private BluetoothGattCharacteristic currentIndicationCharacteristic;


    @Inject public GattManager(final Application application) {
        Preconditions.checkArgument(application != null, NONE_APPLICATION);
        this.application = application;
        this.gattManagerCallBack = new GattManagerCallBack();
        setBluetooth();
    }


    private void setBluetooth() {
        if (bluetoothManager == null || bluetoothAdapter == null) {
            bluetoothManager = (BluetoothManager) application.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
    }


    @Override public Observable<Boolean> observeConnection() {
        return observeConnection(bleDevice);
    }


    @Override
    public Observable<Boolean> observeConnection(final BleDevice bleDevice) {
        return Observable.create((Observable.OnSubscribe<Boolean>) subscriber -> {
            if (application == null) {
                subscriber.onError(new GattConnectException(NONE_APPLICATION));
            } else {
                setBluetooth();
            }
            if (!bluetoothAdapter.isEnabled()) {
                subscriber.onError(new GattConnectException(NONE_BT));
            }
            if (bleDevice == null) {
                subscriber.onError(new GattConnectException(NONE_BLE_DEVICE));
            } else {
                if (Strings.isNullOrEmpty(bleDevice.getAddress())) {
                    subscriber.onError(new GattConnectException(NONE_ADDRESS));
                } else {
                    this.bleDevice = bleDevice;
                }
            }

            gattManagerCallBack.setConnectionListener(new GattConnectionOnSubscribe() {
                @Override public void onGattConnected() {
                    subscriber.onNext(true);
                }


                @Override public void onGattDisconnected() {
                    subscriber.onNext(false);
                }
            });

            if (isConnected()) {
                subscriber.onNext(true);
            } else {
                bluetoothGatt = this.bleDevice.getDevice().connectGatt(application, false, gattManagerCallBack);
            }
        }).doOnUnsubscribe(() -> gattManagerCallBack.setConnectionListener(null)).doOnUnsubscribe(this::disconnect);
    }


    @Override public boolean isConnected() {
        return bluetoothManager.getConnectionState(bleDevice.getDevice(), GATT) == BluetoothProfile.STATE_CONNECTED;
    }


    @Override public void disconnect() {
        if (bluetoothGatt != null && bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled() && isConnected()) {
                bluetoothGatt.disconnect();
            } else {
                closeGatt();
            }
        }
    }


    protected void closeGatt() {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
        }
    }


    @Override public Observable<BluetoothDevice> observeBond()
            throws GattConnectException {
        if (isConnected()) {
            final BondDeviceBroadcastReceiver receiver = new BondDeviceBroadcastReceiver(application);
            return Observable.create(receiver).doOnSubscribe(() -> bleDevice.getDevice().createBond());
        } else {
            throw new GattConnectException(NOT_CONNECTED);
        }
    }


    @Override
    public Observable<Integer> observeRssi(final long rssiUpdateTimeInterval) {
        return Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
            if (isConnected()) {
                gattManagerCallBack.setRssiListener(new GattRssiOnSubscribe() {
                    @Override public void onRssiUpdated(int rssi) {
                        subscriber.onNext(rssi);
                    }


                    @Override public void onRssiInvalidate(int status) {
                        subscriber.onError(new GattRssiException(status));
                    }
                });
                rssiTimerSubscription = Observable.interval(rssiUpdateTimeInterval, TimeUnit.SECONDS).subscribe(aLong -> bluetoothGatt.readRemoteRssi());
            } else {
                subscriber.onError(new GattConnectException(NOT_CONNECTED));
                subscriber.unsubscribe();
            }
        }).doOnUnsubscribe(() -> rssiTimerSubscription.unsubscribe()).doOnUnsubscribe(() -> gattManagerCallBack.setRssiListener(null));
    }


    @Override
    public Observable<List<BluetoothGattService>> observeDiscoverService() {
        return Observable.create((Observable.OnSubscribe<List<BluetoothGattService>>) subscriber -> {
            if (isConnected()) {
                gattManagerCallBack.setServiceListener(new GattServiceOnSubscribe() {
                    @Override public void onServiceDiscovered(List<BluetoothGattService> services) {
                        subscriber.onNext(services);
                        subscriber.onCompleted();
                    }


                    @Override public void onServiceNotFound(int status) {
                        subscriber.onError(new GattResourceNotDiscoveredException(NONE_SERVICES));
                    }
                });
                bluetoothGatt.discoverServices();
            } else {
                subscriber.onError(new GattConnectException(NOT_CONNECTED));
            }
        }).doOnUnsubscribe(() -> gattManagerCallBack.setServiceListener(null));
    }


    @Override public Observable<BluetoothGattCharacteristic> observeBattery() {
        return observeRead(BluetoothGatts.BATTERY_CHARACTERISTIC_UUID);
    }


    @Override
    public Observable<BluetoothGattCharacteristic> observeRead(final UUID uuidToRead) {
        return observeRead(findCharacteristic(uuidToRead));
    }


    @Override
    public Observable<BluetoothGattCharacteristic> observeRead(final BluetoothGattCharacteristic characteristicToRead) {
        return Observable.create((Observable.OnSubscribe<BluetoothGattCharacteristic>) subscriber -> {
            GattException exception = checkGattStatusSuccess(characteristicToRead);
            if (exception != null) {
                subscriber.onError(exception);
                return;
            }
            gattManagerCallBack.setReadListener(new GattReadCharacteristicOnSubscribe() {
                @Override
                public void onCharacteristicReadSucceeded(BluetoothGattCharacteristic characteristic) {
                    subscriber.onNext(characteristic);
                    subscriber.onCompleted();
                }


                @Override
                public void onCharacteristicReadFailed(BluetoothGattCharacteristic characteristic, int status) {
                    subscriber.onError(new GattReadCharacteristicException(
                            characteristic, STATUS_RESULT_FAIL, status));
                }
            });
            bluetoothGatt.readCharacteristic(characteristicToRead);
        }).doOnUnsubscribe(() -> gattManagerCallBack.setReadListener(null));
    }


    @Override public Observable<BluetoothGattCharacteristic> observeWrite(
            final UUID uuidToWrite, final List<Byte> valuesToWrite) {
        return observeWrite(findCharacteristic(uuidToWrite), Bytes.toArray(valuesToWrite));
    }


    @Override public Observable<BluetoothGattCharacteristic> observeWrite(
            final UUID uuidToWrite, final byte[] valuesToWrite) {
        return observeWrite(findCharacteristic(uuidToWrite), valuesToWrite);
    }


    @Override public Observable<BluetoothGattCharacteristic> observeWrite(
            final BluetoothGattCharacteristic characteristicToWrite, final List<Byte> valuesToWrite) {
        return observeWrite(characteristicToWrite, Bytes.toArray(valuesToWrite));
    }


    @Override
    public Observable<BluetoothGattCharacteristic> observeWrite(final BluetoothGattCharacteristic characteristicToWrite, final byte[] valuesToWrite) {
        return Observable.create((Observable.OnSubscribe<BluetoothGattCharacteristic>) subscriber -> {
            GattException exception = checkGattStatusSuccess(characteristicToWrite);
            if (exception != null) {
                subscriber.onError(exception);
                return;
            }
            if (valuesToWrite == null || valuesToWrite.length == 0) {
                subscriber.onError(new GattWriteCharacteristicException(characteristicToWrite, NULL_OR_EMPTY_DATA));
                return;
            }
            gattManagerCallBack.setWriteListener(new GattWriteCharacteristicOnSubscribe() {
                @Override
                public void onCharacteristicWritePrepared(BluetoothGattCharacteristic characteristic) {
                    subscriber.onStart();
                }


                @Override
                public void onCharacteristicWriteSucceeded(BluetoothGattCharacteristic characteristic) {
                    if (currentWriteCharacteristic != null && characteristic.equals(currentNotificationCharacteristic)) {
                        subscriber.onNext(characteristic);
                    }
                }


                @Override
                public void onCharacteristicWriteFailed(BluetoothGattCharacteristic characteristic, int status) {
                    subscriber.onError(new GattWriteCharacteristicException(characteristic, STATUS_RESULT_FAIL, status));
                }
            });
            characteristicToWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            characteristicToWrite.setValue(valuesToWrite);
            bluetoothGatt.writeCharacteristic(characteristicToWrite);
        }).doOnSubscribe(() -> currentWriteCharacteristic = characteristicToWrite).doOnUnsubscribe(() -> gattManagerCallBack.setWriteListener(null));
    }


    @Override public Observable<BluetoothGattCharacteristic> observeNotification(
            final UUID uuidToNotification, final boolean enableNotification) {
        return observeNotification(findCharacteristic(uuidToNotification), enableNotification);
    }


    @Override public Observable<BluetoothGattCharacteristic> observeNotification(
            final BluetoothGattCharacteristic characteristicToNotification, final boolean enableNotification) {
        return Observable.create((Observable.OnSubscribe<BluetoothGattCharacteristic>) subscriber -> {
            GattException exception = checkGattStatusSuccess(characteristicToNotification);
            if (exception != null) {
                subscriber.onError(exception);
                return;
            }
            gattManagerCallBack.setNotifyListener(new GattNotifyCharacteristicOnSubscribe() {
                @Override
                public void onCharacteristicNotifyPrepared(BluetoothGattCharacteristic characteristic) {
                    if (isCharacteristicAvailable(characteristic, currentNotificationCharacteristic)) {
                        subscriber.onStart();
                    }
                }


                @Override
                public void onCharacteristicNotifySucceeded(BluetoothGattCharacteristic characteristic) {
                    if (isCharacteristicAvailable(characteristic, currentNotificationCharacteristic)) {
                        subscriber.onNext(characteristic);
                    }
                }


                @Override
                public void onCharacteristicNotifyFailed(BluetoothGattDescriptor descriptor, int status) {
                    if (isCharacteristicAvailable(descriptor.getCharacteristic(), currentNotificationCharacteristic)) {
                        subscriber.onError(new GattNotificationCharacteristicException(descriptor, DESCRIPTION_WRITE_FAIL, status));
                    }
                }
            });
            bluetoothGatt.setCharacteristicNotification(characteristicToNotification, enableNotification);
            BluetoothGattDescriptor notificationDescriptor = characteristicToNotification.getDescriptor(BluetoothGatts.CLIENT_CHARACTERISTIC_CONFIG);
            byte[] value = enableNotification ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
            notificationDescriptor.setValue(value);
            bluetoothGatt.writeDescriptor(notificationDescriptor);
        }).doOnSubscribe(() -> currentNotificationCharacteristic = characteristicToNotification)
                .doOnUnsubscribe(() -> gattManagerCallBack.setNotifyListener(null));
    }


    @Override
    public Boolean isNotificationEnabled(final BluetoothGattCharacteristic characteristic) {
        if (characteristic != null && isConnected()) {
            BluetoothGattDescriptor notificationDescriptor = characteristic.getDescriptor(BluetoothGatts.CLIENT_CHARACTERISTIC_CONFIG);
            return notificationDescriptor != null && notificationDescriptor.getValue() == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
        } else {
            return false;
        }
    }


    @Override
    public Observable<BluetoothGattCharacteristic> observeIndication(final UUID uuidToIndication) {
        return observeIndication(findCharacteristic(uuidToIndication));
    }


    @Override public Observable<BluetoothGattCharacteristic> observeIndication(
            final BluetoothGattCharacteristic characteristicToIndication) {
        return Observable.create((Observable.OnSubscribe<BluetoothGattCharacteristic>) subscriber -> {
            GattException exception = checkGattStatusSuccess(characteristicToIndication);
            if (exception != null) {
                subscriber.onError(exception);
                return;
            }
            gattManagerCallBack.setIndicateListener(new GattIndicateCharacteristicOnSubscribe() {
                @Override
                public void onCharacteristicIndicatePrepared(BluetoothGattCharacteristic characteristic) {
                    if (isCharacteristicAvailable(characteristic, currentIndicationCharacteristic)) {
                        subscriber.onStart();
                    }
                }


                @Override
                public void onCharacteristicIndicateSucceeded(BluetoothGattCharacteristic characteristic) {
                    if (isCharacteristicAvailable(characteristic, currentIndicationCharacteristic)) {
                        subscriber.onNext(characteristic);
                    }
                }


                @Override
                public void onCharacteristicIndicateFailed(BluetoothGattDescriptor descriptor, int status) {
                    if (isCharacteristicAvailable(descriptor.getCharacteristic(), currentIndicationCharacteristic)) {
                        subscriber.onError(new GattNotificationCharacteristicException(descriptor, DESCRIPTION_WRITE_FAIL, status));
                    }
                }
            });
            bluetoothGatt.setCharacteristicNotification(characteristicToIndication, true);
            BluetoothGattDescriptor indicationDescriptor = characteristicToIndication.getDescriptor(BluetoothGatts.CLIENT_CHARACTERISTIC_CONFIG);
            indicationDescriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            bluetoothGatt.writeDescriptor(indicationDescriptor);
        }).doOnSubscribe(() -> currentIndicationCharacteristic = characteristicToIndication)
                .doOnUnsubscribe(() -> gattManagerCallBack.setIndicateListener(null));
    }


    @Override
    public Boolean isIndicationEnabled(final BluetoothGattCharacteristic characteristic) {
        if (characteristic != null && isConnected()) {
            BluetoothGattDescriptor indicationDescriptor = characteristic.getDescriptor(BluetoothGatts.CLIENT_CHARACTERISTIC_CONFIG);
            return indicationDescriptor != null && indicationDescriptor.getValue() == BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
        } else {
            return false;
        }
    }


    @Override
    public BluetoothGattCharacteristic findCharacteristic(final UUID uuid) {
        for (BluetoothGattService service : bluetoothGatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (characteristic.getUuid().equals(uuid)) {
                    return characteristic;
                }
            }
        }
        return null;
    }


    @Override public BluetoothGatt getGatt() {
        return bluetoothGatt;
    }


    @Override public BleDevice getBleDevice() {
        return bleDevice;
    }


    private GattException checkGattStatusSuccess(final BluetoothGattCharacteristic characteristic) {
        if (!isConnected()) {
            return new GattConnectException(NOT_CONNECTED);
        }
        if (characteristic == null) {
            return new GattResourceNotDiscoveredException(NONE_UUID_CHARACTERISTIC);
        }
        return null;
    }


    private boolean isCharacteristicAvailable(final BluetoothGattCharacteristic callbackCharacteristic, final BluetoothGattCharacteristic currentCharacteristic) {
        return (currentCharacteristic != null && callbackCharacteristic.equals(currentCharacteristic));
    }
}