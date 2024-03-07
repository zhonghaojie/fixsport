package com.koylo.ble.core

import android.bluetooth.BluetoothGattCharacteristic

class CharacteristicDto {
    var write: BluetoothGattCharacteristic? = null
    var read: BluetoothGattCharacteristic? = null
}