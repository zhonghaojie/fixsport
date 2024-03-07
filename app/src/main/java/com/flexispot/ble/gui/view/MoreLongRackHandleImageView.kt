package com.flexispot.ble.gui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.orhanobut.logger.Logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class MoreLongRackHandleImageView(context: Context?, attrs: AttributeSet?) : ImageView(context, attrs) {

    private var mListener: HandleButtonOnClickListener? = null

    /**
     * 时间值记录
     */
    private var timeMills: Long = Calendar.getInstance().timeInMillis

    private var longPressJob: Job? = null

    /**
     * 处理touch事件
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mListener?.onDown(this)
                timeMills = Calendar.getInstance().timeInMillis
                longPressJob = GlobalScope.launch {
                    mListener?.onLongPress(this@MoreLongRackHandleImageView)
                }
            }
            MotionEvent.ACTION_UP -> {
                mListener?.onUp(this@MoreLongRackHandleImageView)
                longPressJob?.cancel()
            }
        }
        super.onTouchEvent(event)
        return true
    }

    /**
     * 给长按btn控件注册一个监听器。
     *
     * @param listener 监听器的实现。
     */
    fun setHandleButtonOnClickListener(listener: HandleButtonOnClickListener) {
        mListener = listener
    }

    /**
     * 按键监听事件
     */
    interface HandleButtonOnClickListener {

        /**
         * 短按
         */
        fun onShortPress(view: View)

        /**
         * 长按
         */
        fun onLongPress(view: View)

        /**
         * 前往极点
         */
        fun toMax(view: View)

        /**
         * 松开
         */
        fun onUpspring(view: View)

        /**
         * 手指落下
         */
        fun onDown(view: View)

        /**
         * 手指松开
         */
        fun onUp(view: View)
    }
}