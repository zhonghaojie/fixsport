package com.flexispot.ble.gui.add

import com.flexispot.ble.data.repository.add.AddDeviceRepository
import com.luman.mvvm.base.LuManViewModel

/**
 * @author luman
 * @date 19-11-26
 **/
class AddViewModel(val repository: AddDeviceRepository) : LuManViewModel() {

    fun types() = repository.data2

}