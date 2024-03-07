package com.flexispot.ble.gui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

/**
 * @author luman
 * @date 19-12-6
 **/
class DisAllowTouchRecycleView : RecyclerView {

    private var canTouch = true

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (canTouch) {
            return super.dispatchTouchEvent(ev)
        } else {
            return false
        }
    }

    fun canTouch(value: Boolean) {
        canTouch = value
    }
}