package com.rainbow.kam.ble_gatt_manager.broadcast;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.rainbow.kam.ble_gatt_manager.exceptions.gatt.GattException;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

/**
 * Created by Kang Young Won on 2016-06-24.
 */
public class BondDeviceBroadcastReceiver implements Observable.OnSubscribe<BluetoothDevice> {
    private final Context context;
    private final IntentFilter intentFilter;


    public BondDeviceBroadcastReceiver(Context context) {
        this.context = context;
        this.intentFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
    }


    @Override public void call(Subscriber<? super BluetoothDevice> subscriber) {
        final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
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

        subscriber.add(Subscriptions.create(() -> context.unregisterReceiver(broadcastReceiver)));
        context.registerReceiver(broadcastReceiver, intentFilter);
    }


    @Override public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (object instanceof BondDeviceBroadcastReceiver) {
            BondDeviceBroadcastReceiver other = (BondDeviceBroadcastReceiver) object;
            return java.util.Objects.equals(this.intentFilter, other.intentFilter);
        } else {
            return false;
        }
    }


    @Override public int hashCode() {
        return Objects.hashCode(intentFilter);
    }


    @Override public String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }
}