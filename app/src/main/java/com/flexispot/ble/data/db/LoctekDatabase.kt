package com.flexispot.ble.data.db

object flexispotDatabase {

    private var deviceDao: DeviceDao? = null

    fun getDeviceDao(): DeviceDao {
        if (deviceDao == null) {
            deviceDao = DeviceDao()
        }
        return deviceDao!!
    }
}