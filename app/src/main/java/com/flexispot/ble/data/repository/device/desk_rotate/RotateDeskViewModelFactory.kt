package com.flexispot.ble.data.repository.device.desk_rotate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * @author luman
 * @date 19-11-29
 **/
class RotateDeskViewModelFactory : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(RotateDeskRepository::class.java).newInstance(
            RotateDeskRepository()
        )
    }

}