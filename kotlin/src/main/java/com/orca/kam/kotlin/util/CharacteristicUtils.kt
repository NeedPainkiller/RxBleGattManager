package com.orca.kam.kotlin.util

import android.bluetooth.BluetoothGattCharacteristic
import android.text.TextUtils
import com.google.common.collect.Maps
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*

/**
 * Created by Kang Young Won on 2016-07-19.
 */
class CharacteristicUtils {


    override fun toString(): String {
        return "CharacteristicUtils{" +
                "Observable<HashMap<String, String>> getFormattedValues(BluetoothGattCharacteristic characteristic)" +
                "Observable<byte[]> createHexByteValue(final BluetoothGattCharacteristic characteristic, final String hex)" +
                "}"
    }

    companion object {
        fun getFormattedValues(characteristic: BluetoothGattCharacteristic): Observable<HashMap<String, String>> {
            val value: ByteArray = characteristic.value
            val valueMap: HashMap<String, String> = Maps.newHashMap<String, String>()
            val hexBuilder: StringBuilder = StringBuilder(value.size)
            val decBuilder: StringBuilder = StringBuilder(value.size)
            val strBuilder: StringBuilder = StringBuilder(value.size)


            return Observable.create(Observable.OnSubscribe<HashMap<String, String>> { subscriber ->
                for (byteChar: Byte in value) {
                    hexBuilder.append("0x").append(String.format("%02X", byteChar))
//                    decBuilder.append(Byte.Companion.and)
//                    decBuilder.append(byteChar and 0xff)
                    strBuilder.append(String.format("%c", byteChar))
                }
                valueMap.put("HEX", hexBuilder.toString())
                valueMap.put("DEC", decBuilder.toString())
                valueMap.put("STR", strBuilder.toString())

                subscriber.onNext(valueMap)
                subscriber.onCompleted()
                subscriber.unsubscribe()
            }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
        }


        fun createHexByteValue(characteristic: BluetoothGattCharacteristic, hex: String): Observable<ByteArray> {
            return Observable.create<ByteArray> { subscriber ->
                if (!TextUtils.isEmpty(hex) || hex.length > 1) {
                    val writeHexValue = hex.replace("[^[0-9][a-f]]".toRegex(), "")
                    val bytes = ByteArray(writeHexValue.length / 2 + 1)
                    val length = bytes.size
                    for (i in 0..length - 1) {
                        bytes[i] = java.lang.Long.decode("0x" + hex.substring(i * 2, i * 2 + 2))!!.toByte()
                    }
                    subscriber.onNext(bytes)
                    subscriber.onCompleted()
                } else {
//                    subscriber.onError(GattWriteCharacteristicException(characteristic, "value is null or empty"))
                }
            }
        }
    }
}
