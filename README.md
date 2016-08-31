# RxBleGattManager [ ![Download](https://api.bintray.com/packages/kam6512/maven/RxBleGattManager/images/download.svg) ](https://bintray.com/kam6512/maven/RxBleGattManager/_latestVersion)
RxBleGattManager is Helper Library for Bluetooth Low Energy Device Scan and Gatt Control operations


### Bluetooth Device Scan

##### Create RxBleScanner (Support Injection With Dagger2)
```java
RxBleScanner rxBleScanner = new RxBleScanner();
```
##### Device Scan
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

### Bluetooth Gatt Control
##### Create GattManager (Support Injection With Dagger2)
```java
GattManager gattManager = new GattManager(Application);
```
##### Connection / Observing connection state[^]
```java
gattManager.observeConnection(bleDevice)
	.subscribe(isConnected -> {
      if (isConnected) {
        // when Device connected
      } else {
        // when Device disconnected
      }
	}, throwable -> {
      // GattConnectException
    });
```

##### Bond Device
```java
gattManager.observeBond()
    .subscribe(bluetoothDevice -> {
    	// BOND_BONDING
    }, throwable -> {
    	// BOND_NONE
        // GattConnectException
    }, () -> {
    	//BOND_BONDED
    });
```

##### Discover Gatt Services
```java
gattManager.observeDiscoverService()
    .subscribe(services -> {
    	// Gatt ServiceList Found
    }, throwable -> {
    	// Gatt ServiceList Not Found
        // GattConnectException | GattResourceNotDiscoveredException
    });
```

##### Read Rssi Value
```java
gattManager.observeRssi(intervalTime) //long
	.subscribe(rssi -> {
    	// Rssi Int value
    }, throwable -> {
        // GattConnectException | GattRssiException
    });
```
##### Read
```java
gattManager.observeRead(characteristic) // or UUID
	.subscribe(characteristic -> {
 	   // Read Characteristic Successed
    }, throwable -> {
    	// Read Characteristic Failed
        // GattResourceNotDiscoveredException | GattConnectException | GattReadCharacteristicException
    });
```
##### Read Battery
###### this operation needs Battery Service and Characteristic / if Device not supported Battery Service - Characteristic operation will be throw Exception
###### Battery Service [0000180F-0000-1000-8000-00805F9B34FB]
###### Battery Characteristic [00002A19-0000-1000-8000-00805F9B34FB]
```java
gattManager.observeBattery()
	.subscribe(characteristic -> {
    	// Read Battery Successed
    }, throwable -> {
    	// Read Battery Failed
        // GattResourceNotDiscoveredException | GattConnectException | GattReadCharacteristicException
    });
```
##### Write
```java
gattManager.observeWrite(characteristic, data //byte[])
	 .unsafeSubscribe(new Subscriber<BluetoothGattCharacteristic>() {
        @Override public void onStart() {
       		// onCharacteristicWrite
        }
        @Override public void onCompleted() {
        	// --
        }
        @Override public void onError(Throwable e) {
        	// Write Characteristic Failed
        }
        @Override public void onNext(BluetoothGattCharacteristic characteristic) {
            // Write Characteristic Successed
            // onCharacteristicChanged
            // MUST NEED SET NOTIFICATION ENABLED
        }
    });
```
##### Set Characteristic Notification
```java
gattManager.observeNotification(characteristic, enabled //boolean)
	.unsafeSubscribe(new Subscriber<BluetoothGattCharacteristic>() {
        @Override public void onStart() {
       		 // onDescriptorWrite
        }
        @Override public void onCompleted() {
        	// --
        }
        @Override public void onError(Throwable e) {
        	//Set Characteristic notification Failed
        }
        @Override public void onNext(BluetoothGattCharacteristic characteristic) {
             // Set Characteristic notification Successed
             // onCharacteristicChanged
        }
    });
```
##### Set Characteristic Indication
```java
gattManager.observeIndication(characteristic)
	.unsafeSubscribe(new Subscriber<BluetoothGattCharacteristic>() {
        @Override public void onStart() {
       		 // onDescriptorWrite
        }
        @Override public void onCompleted() {
        	// --
        }
        @Override public void onError(Throwable e) {
        	//Set Characteristic Indication Failed
        }
        @Override public void onNext(BluetoothGattCharacteristic characteristic) {
            // Set Characteristic Indication Successed
            // onCharacteristicChanged
        }
    });
```


## Download
<a href='https://bintray.com/kam6512/maven/RxBleGattManager?source=watch' alt='Get automatic notifications about new "RxBleGattManager" versions'><img src='https://www.bintray.com/docs/images/bintray_badge_color.png'></a>
### Gradle

```java
compile 'com.rainbow.library:RxBleGattManager:0.1.5.0'
```
### Maven

```xml
<dependency>
  <groupId>com.rainbow.library</groupId>
  <artifactId>RxBleGattManager</artifactId>
  <version>0.1.5.0</version>
  <type>pom</type>
</dependency>
```
### Ivy
```xml
<dependency org='com.rainbow.library' name='RxBleGattManager' rev='0.1.5.0'>
  <artifact name='$AID' ext='pom'></artifact>
</dependency>
```

## Developers
* YoungWon Kang (ywkang@rainbow-wireless.com)
* Hyeon Ji  (jihyeon@rainbow-wireless.com)

## Developed by [RainbowWireless](http://www.rainbow-wireless.com/)
