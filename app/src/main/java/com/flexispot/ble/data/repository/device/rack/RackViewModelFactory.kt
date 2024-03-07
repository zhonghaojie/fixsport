package com.flexispot.ble.data.repository.device.rack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * @author luman
 * @date 19-11-27
 **/
class RackViewModelFactory : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(RackRepository::class.java).newInstance(
            RackRepository()
        )
    }

}