package com.rainbow.kam.ble_gatt_manager.manager.realm;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.google.common.primitives.Bytes;
import com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattConnectException;
import com.rainbow.kam.ble_gatt_manager.helper.RealmHelper;
import com.rainbow.kam.ble_gatt_manager.manager.GattManager;
import com.rainbow.kam.ble_gatt_manager.model.BleDevice;
import com.rainbow.kam.ble_gatt_manager.model.GattObserveData;
import com.rainbow.kam.ble_gatt_manager.model.realm.GattRecodeModel;
import com.rainbow.kam.ble_gatt_manager.util.BluetoothGatts;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import rx.Observable;

import static com.rainbow.kam.ble_gatt_manager.util.GattRecodes.*;

/**
 * Created by Kang Young Won on 2016-06-30.
 */
public class RealmGattManager extends GattManager {
    private final RealmHelper realmHelper;

    private final GattRecodeModel gattRecodeModel = new GattRecodeModel();


    @Inject public RealmGattManager(Application application) {
        super(application);
        realmHelper = new RealmHelper(application);
    }


    @Override public Observable<Boolean> observeConnection() {
        return observeConnection(getBleDevice());
    }


    @Override
    public Observable<Boolean> observeConnection(BleDevice bleDevice) {
        return super.observeConnection(bleDevice)
                .doOnSubscribe(() -> {
                    gattRecodeModel.setDeviceName(bleDevice.getName());
                    gattRecodeModel.setDeviceAddress(bleDevice.getAddress());
                })
                .doOnNext(isConnected -> {
                    gattRecodeModel.setGattOperationUUID(UUID_NONE);
                    gattRecodeModel.setGattOperationType(TYPE_CONNECTION);
                    if (isConnected) {
                        gattRecodeModel.setGattState(STATE_CONNECTED);
                    } else {
                        gattRecodeModel.setGattState(STATE_DISCONNECTED);
                    }
                    realmHelper.recodeGatt(gattRecodeModel);
                });
    }


    @Override public boolean isConnected() {
        return super.isConnected();
    }


    @Override public void disconnect() {
        super.disconnect();
    }


    @Override public Observable<BluetoothDevice> observeBond()  throws GattConnectException {
        return super.observeBond()
                .doOnSubscribe(() -> {
                    gattRecodeModel.setGattOperationUUID(UUID_NONE);
                    gattRecodeModel.setGattOperationType(TYPE_BOND);
                })
                .doOnNext(device -> gattRecodeModel.setGattState(BOND_BONDING))
                .doOnCompleted(() -> gattRecodeModel.setGattState(BOND_BONDED))
                .doOnError(throwable -> gattRecodeModel.setGattState(BOND_NONE))
                .doOnEach(notification -> realmHelper.recodeGatt(gattRecodeModel));
    }


    @Override
    public Observable<Integer> observeRssi(long rssiUpdateTimeInterval) {
        return super.observeRssi(rssiUpdateTimeInterval);
    }


    @Override
    public Observable<List<BluetoothGattService>> observeDiscoverService() {
        return super.observeDiscoverService()
                .doOnSubscribe(() -> {
                    gattRecodeModel.setGattOperationUUID(UUID_NONE);
                    gattRecodeModel.setGattOperationType(TYPE_DISCOVER);
                })
                .doOnNext(device -> {
                    gattRecodeModel.setGattState(BOND_BONDING);
                    realmHelper.recodeGatt(gattRecodeModel);
                })
                .doOnError(throwable -> {
                    gattRecodeModel.setGattState(BOND_NONE);
                    realmHelper.recodeGatt(gattRecodeModel);
                });
    }


    @Override public Observable<BluetoothGattCharacteristic> observeBattery() {
        return observeRead(BluetoothGatts.BATTERY_CHARACTERISTIC_UUID);
    }


    @Override public Observable<BluetoothGattCharacteristic> observeRead(
            final UUID uuidToRead) {
        return observeRead(findCharacteristic(uuidToRead));
    }


    @Override
    public Observable<BluetoothGattCharacteristic> observeRead(BluetoothGattCharacteristic characteristicToRead) {
        return super.observeRead(characteristicToRead)
                .doOnSubscribe(() -> {
                    gattRecodeModel.setGattOperationUUID(characteristicToRead.getUuid());
                    gattRecodeModel.setGattOperationType(TYPE_READ);
                })
                .doOnNext(device -> {
                    gattRecodeModel.setGattState(GATT_SUCCESS);
                    realmHelper.recodeGatt(gattRecodeModel);
                })
                .doOnError(throwable -> {
                    gattRecodeModel.setGattState(GATT_FAILURE);
                    realmHelper.recodeGatt(gattRecodeModel);
                });
    }


    @Override public Observable<GattObserveData> observeWrite(
            final UUID uuidToWrite, final List<Byte> valuesToWrite) {
        return observeWrite(findCharacteristic(uuidToWrite), Bytes.toArray(valuesToWrite));
    }


    @Override public Observable<GattObserveData> observeWrite(
            final UUID uuidToWrite, final byte[] valuesToWrite) {
        return observeWrite(findCharacteristic(uuidToWrite), valuesToWrite);
    }


    @Override public Observable<GattObserveData> observeWrite(
            final BluetoothGattCharacteristic characteristicToWrite, final List<Byte> valuesToWrite) {
        return observeWrite(characteristicToWrite, Bytes.toArray(valuesToWrite));
    }


    @Override
    public Observable<GattObserveData> observeWrite(BluetoothGattCharacteristic characteristicToWrite, byte[] valuesToWrite) {
        return super.observeWrite(characteristicToWrite, valuesToWrite)
                .doOnSubscribe(() -> gattRecodeModel.setGattOperationUUID(characteristicToWrite.getUuid()))
                .doOnNext(gattObserveData -> {
                    if (gattObserveData.getState() == GattObserveData.STATE_ON_START) {
                        gattRecodeModel.setGattOperationType(TYPE_WRITE_EMPTY);
                    } else if (gattObserveData.getState() == GattObserveData.STATE_ON_NEXT) {
                        gattRecodeModel.setGattOperationType(TYPE_WRITE);
                    }
                    gattRecodeModel.setGattState(GATT_SUCCESS);
                    realmHelper.recodeGatt(gattRecodeModel);
                })
                .doOnError(throwable -> {
                    gattRecodeModel.setGattState(GATT_FAILURE);
                    realmHelper.recodeGatt(gattRecodeModel);
                });
    }


    @Override
    public Observable<GattObserveData> observeNotification(UUID uuidToNotification, boolean enableNotification) {
        return observeNotification(findCharacteristic(uuidToNotification), enableNotification);
    }


    @Override
    public Observable<GattObserveData> observeNotification(BluetoothGattCharacteristic characteristicToNotification, boolean enableNotification) {
        return super.observeNotification(characteristicToNotification, enableNotification)
                .doOnSubscribe(() -> gattRecodeModel.setGattOperationUUID(characteristicToNotification.getUuid()))
                .doOnNext(gattObserveData -> {
                    if (gattObserveData.getState() == GattObserveData.STATE_ON_START) {
                        gattRecodeModel.setGattOperationType(TYPE_NOTIFICATION_EMPTY);
                    } else if (gattObserveData.getState() == GattObserveData.STATE_ON_NEXT) {
                        gattRecodeModel.setGattOperationType(TYPE_NOTIFICATION);
                    }
                    gattRecodeModel.setGattState(GATT_SUCCESS);
                    realmHelper.recodeGatt(gattRecodeModel);
                })
                .doOnError(throwable -> {
                    gattRecodeModel.setGattState(GATT_FAILURE);
                    realmHelper.recodeGatt(gattRecodeModel);
                });
    }


    @Override
    public Boolean isNotificationEnabled(BluetoothGattCharacteristic characteristic) {
        return super.isNotificationEnabled(characteristic);
    }


    @Override
    public Observable<GattObserveData> observeIndication(UUID uuidToIndication) {
        return observeIndication(findCharacteristic(uuidToIndication));
    }


    @Override
    public Observable<GattObserveData> observeIndication(BluetoothGattCharacteristic characteristicToIndication) {
        return super.observeIndication(characteristicToIndication)
                .doOnSubscribe(() -> gattRecodeModel.setGattOperationUUID(characteristicToIndication.getUuid()))
                .doOnNext(gattObserveData -> {
                    if (gattObserveData.getState() == GattObserveData.STATE_ON_START) {
                        gattRecodeModel.setGattOperationType(TYPE_INDICATION_EMPTY);
                    } else if (gattObserveData.getState() == GattObserveData.STATE_ON_NEXT) {
                        gattRecodeModel.setGattOperationType(TYPE_INDICATION);
                    }
                    gattRecodeModel.setGattState(GATT_SUCCESS);
                    realmHelper.recodeGatt(gattRecodeModel);
                })
                .doOnError(throwable -> {
                    gattRecodeModel.setGattState(GATT_FAILURE);
                    realmHelper.recodeGatt(gattRecodeModel);
                });
    }


    @Override
    public Boolean isIndicationEnabled(BluetoothGattCharacteristic characteristic) {
        return super.isIndicationEnabled(characteristic);
    }


    @Override
    public BluetoothGattCharacteristic findCharacteristic(UUID uuid) {
        return super.findCharacteristic(uuid);
    }


    @Override public BluetoothGatt getGatt() {
        return super.getGatt();
    }


    @Override public BleDevice getBleDevice() {
        return super.getBleDevice();
    }


    public void showRecode() {
        realmHelper.readRecodedGatt();
    }
}
