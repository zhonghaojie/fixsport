package com.flexispot.ble.data.repository.device.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * @author luman
 * @date 19-11-27
 **/
class MediaViewModelFactory : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(MediaRepository::class.java).newInstance(
            MediaRepository()
        )
    }

}