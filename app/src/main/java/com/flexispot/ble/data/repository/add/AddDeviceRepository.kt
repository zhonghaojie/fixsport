package com.flexispot.ble.data.repository.add

import com.koylo.ble.flexispot.DeviceType

/**
 * @author luman
 * @date 19-11-26
 **/
class AddDeviceRepository {

    //    val data : ArrayList<AddLabel> = ArrayList()
    val data2: ArrayList<DeviceType> = ArrayList()

    init {
//        data.add(AddLabel(0, R.string.desk, DeviceType.ALL))
//        data.add(AddLabel(1, R.string.desk, DeviceType.DESK))
//        data.add(AddLabel(0, R.string.rack, DeviceType.ALL))
//        data.add(AddLabel(1, R.string.desk, DeviceType.RACK))
//        data.add(AddLabel(0, R.string.media, DeviceType.ALL))
//        data.add(AddLabel(1, R.string.desk, DeviceType.MEDIA))

        data2.add(DeviceType.DESK)
        data2.add(DeviceType.MEDIA)
        data2.add(DeviceType.RACK)
        data2.add(DeviceType.THREAD)

    }

}