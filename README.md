# BLE-Gatt_Manager
## Introduction

BLE-Gatt_Manager 는 BLE 디바이스 스캔과 Gatt 연결 그리고 컨트롤을 RxJava / RxAndroid 로 처리할 수 있는 라이브러리 입니다 

 * BLE / 일반 Classic 블루투스 기기 스캔
 * RxJava 와 함께 Gatt 연결 부터 Bond, Discover, Read, Write, Notification, Indication, Rssi 지원
 * [스캔 - 연결 - 명령] 예외 처리 간편하게 가능

## Usage
### Create RxBleScanner (Support Injection With Dagger2)
```java
RxBleScanner rxBleScanner = new RxBleScanner();
```
</br>
### Device Scan
Scanning devices in the near area:

```java
Subscription scanSubscription = rxBleScanner.observeScan()
	.subscribe(bleDevice -> {
	    BluetoothDevice device = bleDevice.getDevice();
	    int rssi = bleDevice.getRssi();
	    String rssiString = bleDevice.getRssiString();
	});
// stop the scan
scanSubscription.unsubscribe();
```
</br>
</br>
</br>
### Create GattManager (Support Injection With Dagger2)
```java
GattManager gattManager = new GattManager(Application);
```
</br>
### Connection / Observing connection state
For further BLE interactions the connection is required.

```java
gattManager.observeConnection(bleDevice).subscribe(isConnected -> {
  if (isConnected) {
    // when Device connected
  } else {
    // when Device disconnected
  }
}, throwable -> {
  // onError(ConnectedFailException e)
});
```
</br>
### Bond Device
For further BLE interactions the connection is required.

```java
gattManager.observeBond()
    .subscribe(bluetoothDevice -> {	//BOND_BONDING
    }),
    throwable -> {	//BOND_NONE
    }),
    () -> {	//BOND_BONDED
    });
```
</br>
### Discover Gatt Services
For further BLE interactions the connection is required.

```java
gattManager.observeDiscoverService()
    .subscribe(services -> {	//Gatt ServiceList Found
    });
```
</br>
### Read Rssi Value
```java
gattManager.observeRssi(intervalTime //long)
	.subscribe(rssi -> {	//rssi Int value
    });
```
</br>
### Read
```java
gattManager.observeRead(characteristic)
	.subscribe(characteristic -> {	//Read Characteristic Successed
    },
    throwable -> {	//Read Characteristic Failed
    });
```
</br>
##### Read Battery
###### this operation needs Battery Service and Characteristic / if Device not supported Battery Service - Characteristic operation will be throw Exception
###### Battery Service [0000180F-0000-1000-8000-00805F9B34FB]
###### Battery Characteristic [00002A19-0000-1000-8000-00805F9B34FB]
```java
gattManager.observeBattery()
	.subscribe(characteristic -> {	//Read Battery Successed
    },
    throwable -> {	//Read Battery Failed
    });
```
</br>
### Write
```java
gattManager.observeWrite(characteristic, data //byte[])
	.subscribe(gattObserveData -> {
                    if (gattObserveData.getState() == GattObserveData.STATE_ON_START) {
                        // onCharacteristicWrite
                    } else if (gattObserveData.getState() == GattObserveData.STATE_ON_NEXT) {
                        // Write Characteristic Successed
                        // onCharacteristicChanged
                        // MUST NEED SET NOTIFICATION ENABLED
                    }
                },
    throwable -> {	//Write Characteristic Failed
    });
```
</br></br>
### Set Characteristic Notification
```java
gattManager.observeNotification(characteristic, enabled //boolean)
	.subscribe(gattObserveData -> {
                    if (gattObserveData.getState() == GattObserveData.STATE_ON_START) {
                        // onDescriptorWrite
                    } else if (gattObserveData.getState() == GattObserveData.STATE_ON_NEXT) {
                        // Set Characteristic notification Successed
                        // onCharacteristicChanged
                    }
                },
    throwable -> {	//Set Characteristic notification Failed
    });
```
</br>
### Set Characteristic Indication
```java
gattManager.observeIndication(characteristic)
	.subscribe(gattObserveData -> {
                    if (gattObserveData.getState() == GattObserveData.STATE_ON_START) {
                        // onDescriptorWrite
                    } else if (gattObserveData.getState() == GattObserveData.STATE_ON_NEXT) {
                        // Set Characteristic Indication Successed
                        // onCharacteristicChanged
                    }
                },
    throwable -> {	//Set Characteristic Indication Failed
    });
```


## Download
### Gradle

```java
compile 'com.rainbow.library:android-ble-gatt-manager:0.1.2.2'
```
### Maven

```xml
<dependency>
  <groupId>com.rainbow.library</groupId>
  <artifactId>android-ble-gatt-manager</artifactId>
  <version>0.1.2.2</version>
  <type>pom</type>
</dependency>
```
### Ivy
```xml
<dependency org='com.rainbow.library' name='android-ble-gatt-manager' rev='0.1.2.2'>
  <artifact name='$AID' ext='pom'></artifact>
</dependency>
```

## Developers
* YoungWon Kang (ywkang@rainbow-wireless.com)
* Hyeon Ji  (jihyeon@rainbow-wireless.com)

## Developed by [RainbowWireless](http://www.rainbow-wireless.com/)
