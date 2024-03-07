package com.flexispot.ble.gui.around

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import com.koylo.ble.flexispot.DeviceType
import com.flexispot.ble.data.bean.Device
import com.flexispot.ble.data.repository.around.SearchRepository
import com.luman.mvvm.base.LuManViewModel
import com.luman.mvvm.base.SingleEvent

/**
 * @author luman
 * @date 19-11-26
 **/
class AroundViewModel(val repository: SearchRepository) : LuManViewModel() {

    private val addFlag = SingleEvent<Boolean>()
    private val endFlag = SingleEvent<Boolean>()

    fun refresh(): LiveData<Boolean> = addFlag
    fun end(): LiveData<Boolean> = endFlag
    fun devices() = repository.devicesForShow
    fun bleTool() = repository.mTool

    /**
     * 开启搜索
     * @param fragment 为了进行生命周期绑定
     */
    fun beginSearch(
        localData: ArrayList<Device>?,
        fragment: FragmentActivity,
        type: DeviceType?,
        devName: String?
    ) {
        repository.initData(localData, type)
        repository.searchDevice(fragment, addFlag, endFlag, devName)
    }

    /**
     * 重启搜索
     */
    fun searchAgain() {
        repository.searchAgain()
    }

    /**
     * 清空占用
     */
    fun destroy() {
        repository.clear()
    }
}