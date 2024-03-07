package com.flexispot.ble.gui.threadMill;

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * @author luman
 * @date 19-11-26
 **/
class TreadFactory : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(TreadMillRepository::class.java)
            .newInstance(TreadMillRepository())
    }
}