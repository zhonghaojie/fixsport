package com.flexispot.ble.data.repository.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * @author luman
 * @date 19-11-26
 **/
class AddDeviceFactory : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(AddDeviceRepository::class.java)
            .newInstance(AddDeviceRepository())
    }
}