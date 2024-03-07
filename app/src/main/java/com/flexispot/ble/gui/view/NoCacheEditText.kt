package com.flexispot.ble.gui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText

/**
 * @author luman
 * @date 19-11-29
 **/
class NoCacheEditText : EditText {

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun getFreezesText() = false
}