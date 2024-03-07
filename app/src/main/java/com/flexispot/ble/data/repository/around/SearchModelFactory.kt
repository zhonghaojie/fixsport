package com.flexispot.ble.data.repository.around

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SearchModelFactory : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(SearchRepository::class.java)
            .newInstance(SearchRepository())
    }
}