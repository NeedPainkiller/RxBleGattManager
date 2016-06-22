
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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.google.common.base.Strings;
import com.google.common.primitives.Bytes;
import com.rainbow.kam.ble_gatt_manager.exceptions.GattException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.ConnectedFailException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.DisconnectedFailException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.GattResourceNotDiscoveredException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.NotificationCharacteristicException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.ReadCharacteristicException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.RssiMissException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.WriteCharacteristicException;
import com.rainbow.kam.ble_gatt_manager.model.BleDevice;
import com.rainbow.kam.ble_gatt_manager.model.GattObserveData;
import com.rainbow.kam.ble_gatt_manager.util.BluetoothGatts;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subscriptions.Subscriptions;

import static com.rainbow.kam.ble_gatt_manager.model.GattObserveData.STATE_ON_NEXT;
import static com.rainbow.kam.ble_gatt_manager.model.GattObserveData.STATE_ON_START;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class GattManager implements GattManagerObserves {

    private final static long RSSI_UPDATE_TIME_INTERVAL = 3;
    private final Application app;

    private BleDevice device;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;

    private PublishSubject<Boolean> connectionSubject;

    private PublishSubject<Integer> rssiSubject;
    private Subscription rssiTimerSubscription;

    private PublishSubject<List<BluetoothGattService>> serviceSubject;

    private PublishSubject<GattObserveData> readSubject;
    private PublishSubject<GattObserveData> writeSubject;

    private PublishSubject<GattObserveData> notificationSubject;
    private BluetoothGattCharacteristic currentNotificationCharacteristic;
    private PublishSubject<GattObserveData> indicationSubject;
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


    @Override public BluetoothGatt getGatt() {
        return bluetoothGatt;
    }


    @Override public BleDevice getBleDevice() {
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
                    BluetoothDevice bluetoothDevice = device.getDevice();


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
//                    bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
//                    if (bluetoothDevice == null) {
//                        subscriber.onError(new ConnectedFailException(deviceAddress, "this Device not Supported BLE"));
//                    }else {
//                        bluetoothDevice.createBond();
//                    }
                    if (isConnected()) {
                        subscriber.onNext(true);
                    } else {
                        GattManagerCallBack callBack = new GattManagerCallBack();
                        bluetoothGatt = bluetoothDevice.connectGatt(app, false, callBack);
                    }
                }));
    }


    @Override public boolean isConnected() {
        return bluetoothManager.getConnectionState(device.getDevice(),
                BluetoothProfile.GATT) == BluetoothProfile.STATE_CONNECTED;
    }


    @Override public void disconnect() {
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


    private void disableAllGattSubject() {
        completeSubject(connectionSubject);
        completeSubject(serviceSubject);
        completeSubject(readSubject);
        completeSubject(writeSubject);
        completeSubject(notificationSubject);
        completeSubject(indicationSubject);
        completeSubject(rssiSubject);
    }


    private <T> void completeSubject(PublishSubject<T> subject) {
        if (subject != null) {
            subject.onCompleted();
        }
    }


    @Override public Observable<BluetoothDevice> observeBond() {
        final BluetoothDevice bluetoothDevice = getBleDevice().getDevice();
        bluetoothDevice.createBond();
        final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        return Observable.create(new BondDeviceBroadcastReceiver(app, filter));
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
                Observable.create((Observable.OnSubscribe<List<BluetoothGattService>>) subscriber -> {
                    bluetoothGatt.discoverServices();
                });
        return Observable.merge(serviceSubject, discoverGattServiceObservable);
    }


    @Override public Observable<GattObserveData> observeBattery() {
        return observeRead(BluetoothGatts.BATTERY_CHARACTERISTIC_UUID);
    }


    @Override public Observable<GattObserveData> observeRead(UUID uuidToRead) {
        return observeRead(findCharacteristic(uuidToRead));
    }


    @Override
    public Observable<GattObserveData> observeRead(BluetoothGattCharacteristic characteristicToRead) {
        readSubject = PublishSubject.create();
        Observable<GattObserveData> readObservable = Observable.create((Observable.OnSubscribe<GattObserveData>) subscriber -> {
            if (characteristicToRead != null) {
                bluetoothGatt.readCharacteristic(characteristicToRead);
            } else {
                subscriber.onError(UUID_CHARACTERLESS);
            }
        });
        return Observable.merge(readSubject, readObservable);
    }


    @Override
    public Observable<GattObserveData> observeWrite(UUID uuidToWrite, List<Byte> valuesToWrite) {
        return observeWrite(findCharacteristic(uuidToWrite), valuesToWrite);
    }


    @Override
    public Observable<GattObserveData> observeWrite(UUID uuidToWrite, byte[] valuesToWrite) {
        return observeWrite(findCharacteristic(uuidToWrite), valuesToWrite);
    }


    @Override
    public Observable<GattObserveData> observeWrite(BluetoothGattCharacteristic characteristicToWrite, List<Byte> valuesToWrite) {
        return observeWrite(characteristicToWrite, Bytes.toArray(valuesToWrite));
    }


    @Override
    public Observable<GattObserveData> observeWrite(BluetoothGattCharacteristic characteristicToWrite, byte[] valuesToWrite) {
        writeSubject = PublishSubject.create();
        Observable<GattObserveData> writeObservable = Observable.create((Observable.OnSubscribe<GattObserveData>) subscriber -> {
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
    public Observable<GattObserveData> observeNotification(UUID uuidToNotification, boolean enableNotification) {
        return observeNotification(findCharacteristic(uuidToNotification), enableNotification);
    }


    @Override
    public Observable<GattObserveData> observeNotification(BluetoothGattCharacteristic characteristicToNotification, boolean enableNotification) {
        notificationSubject = PublishSubject.create();
        currentNotificationCharacteristic = characteristicToNotification;
        Observable<GattObserveData> notificationObservable =
                Observable.create((Observable.OnSubscribe<GattObserveData>) subscriber -> {
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


    @Override
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
    public Observable<GattObserveData> observeIndication(UUID uuidToIndication, boolean enableIndication) {
        return observeIndication(findCharacteristic(uuidToIndication), enableIndication);
    }


    @Override
    public Observable<GattObserveData> observeIndication(BluetoothGattCharacteristic characteristicToIndication, boolean enableIndication) {
        indicationSubject = PublishSubject.create();
        currentIndicationCharacteristic = characteristicToIndication;
        Observable<GattObserveData> indicationObservable =
                Observable.create((Observable.OnSubscribe<GattObserveData>) subscriber -> {
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


    @Override public BluetoothGattCharacteristic findCharacteristic(UUID uuid) {
        for (BluetoothGattService service : bluetoothGatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (characteristic.getUuid().equals(uuid)) {
                    return characteristic;
                }
            }
        }
        return null;
    }


    class GattManagerCallBack extends BluetoothGattCallback {
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
                readSubject.onNext(new GattObserveData(characteristic, STATE_ON_NEXT));
                readSubject.onCompleted();
            } else {
                readSubject.onError(
                        new ReadCharacteristicException(characteristic, "Check Gatt Service Available or Device Connection!", status));
            }
        }


        @DebugLog @Override
        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic) {
            if (writeSubject != null) {
                writeSubject.onNext(new GattObserveData(characteristic, STATE_ON_NEXT));
            }
            if (currentNotificationCharacteristic != null && characteristic.equals(currentNotificationCharacteristic)) {
                notificationSubject.onNext(new GattObserveData(currentNotificationCharacteristic, STATE_ON_NEXT));
            }
            if (currentIndicationCharacteristic != null && characteristic.equals(currentIndicationCharacteristic)) {
                indicationSubject.onNext(new GattObserveData(currentIndicationCharacteristic, STATE_ON_NEXT));
            }
        }


        @DebugLog @Override
        public void onCharacteristicWrite(BluetoothGatt bluetoothGatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                writeSubject.onNext(new GattObserveData(characteristic, STATE_ON_START));
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
                if (currentNotificationCharacteristic != null && characteristic.equals(currentNotificationCharacteristic)) {
                    notificationSubject.onNext(new GattObserveData(currentNotificationCharacteristic, STATE_ON_START));
                }
                if (currentIndicationCharacteristic != null && characteristic.equals(currentIndicationCharacteristic)) {
                    indicationSubject.onNext(new GattObserveData(currentIndicationCharacteristic, STATE_ON_START));
                }
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
    }

    class BondDeviceBroadcastReceiver implements Observable.OnSubscribe<BluetoothDevice> {
        private final Context context;
        private final IntentFilter intentFilter;


        public BondDeviceBroadcastReceiver(Context context, IntentFilter intentFilter) {
            this.context = context;
            this.intentFilter = intentFilter;
        }


        @Override
        public void call(Subscriber<? super BluetoothDevice> subscriber) {
            final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    switch (device.getBondState()) {
                        case BluetoothDevice.BOND_BONDING:
                            subscriber.onNext(device);
                            break;
                        case BluetoothDevice.BOND_BONDED:
                            subscriber.onCompleted();
                            break;
                        case BluetoothDevice.BOND_NONE:
                        default:
                            subscriber.onError(new GattException("NOT BONDED"));
                            break;
                    }
                }
            };

            final Subscription subscription = Subscriptions.create(() -> context.unregisterReceiver(broadcastReceiver));

            subscriber.add(subscription);
            context.registerReceiver(broadcastReceiver, intentFilter);
        }
    }
}