package com.rainbow.kam.ble_gatt_manager.helper;

import android.app.Application;
import android.util.Log;

import com.rainbow.kam.ble_gatt_manager.model.realm.GattRecodeModel;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.exceptions.RealmMigrationNeededException;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Kang Young Won on 2016-06-30.
 */
public class RealmHelper {

    private Realm realmDB;


    public RealmHelper(Application application) {
        RealmConfiguration configuration = new RealmConfiguration.Builder(application).deleteRealmIfMigrationNeeded().build();
        try {
            Realm.setDefaultConfiguration(configuration);
            realmDB = Realm.getDefaultInstance();
        } catch (RealmMigrationNeededException e) {
            Realm.deleteRealm(configuration);
            realmDB = Realm.getDefaultInstance();
        }
    }


    public void recodeGatt(GattRecodeModel gattRecodeModel) {
        Observable.just(realmDB)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(realm -> {
                    realm.beginTransaction();
                    realm.copyToRealm(gattRecodeModel);
                    realm.commitTransaction();
                });
    }


    public void readRecodedGatt() {
        RealmResults<GattRecodeModel> gattRecodeModels = realmDB.where(GattRecodeModel.class).findAll();
        for (GattRecodeModel model : gattRecodeModels) {
            Log.e("READ", model.toString());
        }
    }
}
