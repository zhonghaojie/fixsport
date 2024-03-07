package com.koylo.ble

import com.koylo.ble.flexispot.DeviceType

/**
 * @author luman
 * @date 19-12-6
 **/
object TypeRelation {

    fun getTypeByName(name: String): Int? {
        var type: Int? = null
        when {
            name.startsWith("EMW102") ||
                    name.startsWith("EMW104") ||
                    name.startsWith("EMT123") -> {
                type = DeviceType.RACK.type
            }
            name.startsWith("BTD") ||
                name.startsWith("BDT") -> {
            type = DeviceType.DESK.type
        }
            name.startsWith("FT01") -> {
                type = DeviceType.THREAD.type
            }
            name.startsWith("MTS101") ||
                    name.startsWith("MTS102") ||
                    name.startsWith("MTS103") ||
                    name.startsWith("MTS104") ||
                    name.startsWith("MTS105") ||
                    name.startsWith("MTS106") ||
                    name.startsWith("MTS107") ||
                    name.startsWith("MTS109") ||
                    name.startsWith("MTS108") ||
                    name.startsWith("ETW501") ||
                    name.startsWith("EMW101") -> {
                type = DeviceType.MEDIA.type
            }
            name.startsWith("EMW103") -> {
                if (name.startsWith("EMW103_C")) {
                    type = DeviceType.MEDIA.type
                } else {
                    type = DeviceType.RACK.type
                }
            }
        }
        return type
    }

}