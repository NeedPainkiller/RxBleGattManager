package com.rainbow.kam.ble_gatt_manager.legacy.manager.realm;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.google.common.primitives.Bytes;
import com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattConnectException;
import com.rainbow.kam.ble_gatt_manager.legacy.helper.RealmHelper;
import com.rainbow.kam.ble_gatt_manager.legacy.manager.rx.GattManager;
import com.rainbow.kam.ble_gatt_manager.legacy.model.GattObserveData;
import com.rainbow.kam.ble_gatt_manager.legacy.model.GattRecodeModel;
import com.rainbow.kam.ble_gatt_manager.model.BleDevice;
import com.rainbow.kam.ble_gatt_manager.model.BluetoothGatts;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import rx.Observable;

import static com.rainbow.kam.ble_gatt_manager.legacy.model.GattObserveData.*;
import static com.rainbow.kam.ble_gatt_manager.legacy.util.GattRecodes.*;

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
                .doOnSubscribe(() -> gattRecodeModel.setDeviceName(bleDevice.getName()))
                .doOnSubscribe(() -> gattRecodeModel.setDeviceAddress(bleDevice.getAddress()))
                .doOnNext(isConnected -> gattRecodeModel.setGattOperationUUID(UUID_NONE))
                .doOnNext(isConnected -> gattRecodeModel.setGattOperationType(TYPE_CONNECTION))
                .doOnNext(isConnected -> gattRecodeModel.setGattState(isConnected ? STATE_CONNECTED : STATE_DISCONNECTED))
                .doOnNext(isConnected -> realmHelper.recodeGatt(gattRecodeModel));
    }


    @Override public Observable<BluetoothDevice> observeBond() throws GattConnectException {
        return super.observeBond()
                .doOnSubscribe(() -> gattRecodeModel.setGattOperationUUID(UUID_NONE))
                .doOnSubscribe(() -> gattRecodeModel.setGattOperationType(TYPE_BOND))
                .doOnNext(device -> gattRecodeModel.setGattState(BOND_BONDING))
                .doOnCompleted(() -> gattRecodeModel.setGattState(BOND_BONDED))
                .doOnError(throwable -> gattRecodeModel.setGattState(BOND_NONE))
                .doOnEach(notification -> realmHelper.recodeGatt(gattRecodeModel));
    }


    @Override
    public Observable<List<BluetoothGattService>> observeDiscoverService() {
        return super.observeDiscoverService()
                .doOnSubscribe(() -> gattRecodeModel.setGattOperationUUID(UUID_NONE))
                .doOnSubscribe(() -> gattRecodeModel.setGattOperationType(TYPE_DISCOVER))
                .doOnNext(device -> gattRecodeModel.setGattState(BOND_BONDING))
                .doOnNext(device -> realmHelper.recodeGatt(gattRecodeModel))
                .doOnError(throwable -> gattRecodeModel.setGattState(BOND_NONE))
                .doOnError(throwable -> realmHelper.recodeGatt(gattRecodeModel));
    }


    @Override public Observable<BluetoothGattCharacteristic> observeBattery() {
        return observeRead(BluetoothGatts.BATTERY_CHARACTERISTIC_UUID);
    }


    @Override public Observable<BluetoothGattCharacteristic> observeRead(final UUID uuidToRead) {
        return observeRead(findCharacteristic(uuidToRead));
    }


    @Override
    public Observable<BluetoothGattCharacteristic> observeRead(BluetoothGattCharacteristic characteristicToRead) {
        return super.observeRead(characteristicToRead)
                .doOnSubscribe(() -> gattRecodeModel.setGattOperationUUID(characteristicToRead.getUuid()))
                .doOnSubscribe(() -> gattRecodeModel.setGattOperationType(TYPE_READ))
                .doOnNext(device -> gattRecodeModel.setGattState(GATT_SUCCESS))
                .doOnNext(device -> realmHelper.recodeGatt(gattRecodeModel))
                .doOnError(throwable -> gattRecodeModel.setGattState(GATT_FAILURE))
                .doOnError(throwable -> realmHelper.recodeGatt(gattRecodeModel));
    }


    @Override public Observable<GattObserveData> observeWrite(final UUID uuidToWrite, final List<Byte> valuesToWrite) {
        return observeWrite(findCharacteristic(uuidToWrite), Bytes.toArray(valuesToWrite));
    }


    @Override public Observable<GattObserveData> observeWrite(final UUID uuidToWrite, final byte[] valuesToWrite) {
        return observeWrite(findCharacteristic(uuidToWrite), valuesToWrite);
    }


    @Override
    public Observable<GattObserveData> observeWrite(final BluetoothGattCharacteristic characteristicToWrite, final List<Byte> valuesToWrite) {
        return observeWrite(characteristicToWrite, Bytes.toArray(valuesToWrite));
    }


    @Override
    public Observable<GattObserveData> observeWrite(BluetoothGattCharacteristic characteristicToWrite, byte[] valuesToWrite) {
        return super.observeWrite(characteristicToWrite, valuesToWrite)
                .doOnSubscribe(() -> gattRecodeModel.setGattOperationUUID(characteristicToWrite.getUuid()))
                .doOnNext(gattObserveData -> gattRecodeModel.setGattOperationType(gattObserveData.getState() == STATE_ON_START ? TYPE_WRITE_EMPTY : TYPE_WRITE))
                .doOnNext(gattObserveData -> gattRecodeModel.setGattState(GATT_SUCCESS))
                .doOnNext(gattObserveData -> realmHelper.recodeGatt(gattRecodeModel))
                .doOnError(throwable -> gattRecodeModel.setGattState(GATT_FAILURE))
                .doOnError(throwable -> realmHelper.recodeGatt(gattRecodeModel));
    }


    @Override
    public Observable<GattObserveData> observeNotification(UUID uuidToNotification, boolean enableNotification) {
        return observeNotification(findCharacteristic(uuidToNotification), enableNotification);
    }


    @Override
    public Observable<GattObserveData> observeNotification(BluetoothGattCharacteristic characteristicToNotification, boolean enableNotification) {
        return super.observeNotification(characteristicToNotification, enableNotification)
                .doOnSubscribe(() -> gattRecodeModel.setGattOperationUUID(characteristicToNotification.getUuid()))
                .doOnNext(gattObserveData -> gattRecodeModel.setGattOperationType(gattObserveData.getState() == STATE_ON_START ? TYPE_NOTIFICATION_EMPTY : TYPE_NOTIFICATION))
                .doOnNext(gattObserveData -> gattRecodeModel.setGattState(GATT_SUCCESS))
                .doOnNext(gattObserveData -> realmHelper.recodeGatt(gattRecodeModel))
                .doOnError(throwable -> gattRecodeModel.setGattState(GATT_FAILURE))
                .doOnError(throwable -> realmHelper.recodeGatt(gattRecodeModel));
    }


    @Override
    public Observable<GattObserveData> observeIndication(UUID uuidToIndication) {
        return observeIndication(findCharacteristic(uuidToIndication));
    }


    @Override
    public Observable<GattObserveData> observeIndication(BluetoothGattCharacteristic characteristicToIndication) {
        return super.observeIndication(characteristicToIndication)
                .doOnSubscribe(() -> gattRecodeModel.setGattOperationUUID(characteristicToIndication.getUuid()))
                .doOnNext(gattObserveData -> gattRecodeModel.setGattOperationType(gattObserveData.getState() == STATE_ON_START ? TYPE_INDICATION_EMPTY : TYPE_INDICATION))
                .doOnNext(gattObserveData -> gattRecodeModel.setGattState(GATT_SUCCESS))
                .doOnNext(gattObserveData -> realmHelper.recodeGatt(gattRecodeModel))
                .doOnError(throwable -> gattRecodeModel.setGattState(GATT_FAILURE))
                .doOnError(throwable -> realmHelper.recodeGatt(gattRecodeModel));
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
