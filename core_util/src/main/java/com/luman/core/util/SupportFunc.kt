package com.luman.core.util

import android.content.Context
import android.graphics.Point
import android.view.WindowManager
import com.luman.core.LumanHelper
import java.lang.reflect.ParameterizedType


/**
 * @Editor luman
 * @Time 2019-10-28 19:46
 **/
object SupportFunc {

    /**
     * 根据范型位置返回Class
     * @param o 范型所在类对象
     * @param pos 范型下标
     */
    fun <T> getClassByGenericityPos(o: Any, pos: Int): Class<T>? {
        try {
            return ((o.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[pos]) as Class<T>
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * dp转px
     */
    fun dp2px(dp: Int) =
        (LumanHelper.getApplicationContext()!!.resources.getDisplayMetrics().density * dp + 0.5f).toInt()

    /**
     * 获取屏幕高度(px)
     */
    fun getScreenHeight(): Int {
        return LumanHelper.getApplicationContext()!!.getResources().getDisplayMetrics().heightPixels
    }

    /**
     * 获取屏幕宽度(px)
     */
    fun getScreenWidth(): Int {
        return LumanHelper.getApplicationContext()!!.getResources().getDisplayMetrics().widthPixels
    }

    /**
     * 获取屏幕尺寸
     */
    fun getWindowSize(): Point {
        val vm =
            LumanHelper.getApplicationContext()!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        vm.defaultDisplay.getSize(point)
        return point
    }
}