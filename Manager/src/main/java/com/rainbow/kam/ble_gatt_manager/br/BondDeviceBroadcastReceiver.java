package com.rainbow.kam.ble_gatt_manager.br;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattException;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by Kang Young Won on 2016-06-24.
 */
public class BondDeviceBroadcastReceiver implements Observable.OnSubscribe<BluetoothDevice> {
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