package com.flexispot.ble.gui

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.PopupWindow
import android.widget.TextView
import com.flexispot.ble.R
import com.luman.core.LumanHelper

/**
 * @author luman
 * @date 19-11-28
 **/
class ModifyWindow : PopupWindow {

    interface Callback {
        fun modify()
        fun delete()
    }

    private val callback: Callback
    private lateinit var tvModify: TextView

    constructor(context: Context?, callback: Callback) : super(context) {
        this.callback = callback
        contentView = LayoutInflater.from(context).inflate(R.layout.window_modify, null)
        height = LumanHelper.aboutFunc().dp2px(112)
        width = LumanHelper.aboutFunc().dp2px(345)
        setBackgroundDrawable(null)
        isOutsideTouchable = false
        isFocusable = false
        isTouchable = true
        tvModify = contentView.findViewById<TextView>(R.id.tv_rename)
        tvModify.setOnClickListener {
            callback.modify()
                dismiss()
            }
        contentView.findViewById<TextView>(R.id.tv_delete)
            .setOnClickListener {
                callback.delete()
                dismiss()
            }
    }

    fun changeEnabel(enable: Boolean) {
        tvModify.isEnabled = enable
        if (enable) {
            tvModify.setTextColor(Color.BLACK)
        } else {
            tvModify.setTextColor(Color.LTGRAY)
        }
    }
}