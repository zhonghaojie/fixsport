package com.flexispot.ble.data.repository.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * @author luman
 * @date 19-11-26
 **/
class DevicesFactory : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(DevicesRepository::class.java)
            .newInstance(DevicesRepository())
    }
}