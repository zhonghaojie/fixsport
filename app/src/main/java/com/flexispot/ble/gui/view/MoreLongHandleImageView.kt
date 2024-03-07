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

class MoreLongHandleImageView(context: Context?, attrs: AttributeSet?) : ImageView(context, attrs) {

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
                    delay(200)
                    Logger.d("发送延续指令")
                    mListener?.onLongPress(this@MoreLongHandleImageView)
                    delay(1200)
                    Logger.d("发送极值指令")
                    mListener?.toMax(this@MoreLongHandleImageView)
                }
            }
            MotionEvent.ACTION_UP -> {
                mListener?.onUp(this@MoreLongHandleImageView)
                longPressJob?.cancel()
                val tempMills = Calendar.getInstance().timeInMillis
                when (tempMills - timeMills) {
                    in 0..200 -> {
                        Logger.d("发送短按指令")
                        mListener?.onShortPress(this@MoreLongHandleImageView)
                    }
                    in 201..120000 -> {
                        Logger.d("发送停止指令")
                        mListener?.onUpspring(this@MoreLongHandleImageView)
                    }
                }
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