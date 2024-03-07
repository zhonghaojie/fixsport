package com.luman.core.manager

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.luman.core.LumanHelper
import java.util.*

/**
 * 活动栈管理
 */
object ActivityStackManager {

    private val activityStack by lazy {
        Stack<FragmentActivity>()
    }

    fun size() = activityStack.size

    //压入
    fun push(activity: FragmentActivity) {
        activityStack.push(activity)
    }

    //出栈
    fun pop(activity: FragmentActivity) {
        if (activityStack.contains(activity)) {
            activityStack.pop()
            activityStack.remove(activity)
            if (activityStack.size == 0) {
                LumanHelper.clear()
            }
        }
    }

    //全部出栈
    fun popAll() {
        for (index in activityStack.size - 1 downTo (0)) {
            activityStack[index].finish()
        }
    }

    //全部出栈，开启一个新的activity
    fun popAllForOne(activity: Class<FragmentActivity>) {
        var tempAc = activityStack.peek()
        tempAc.startActivity(Intent(tempAc, activity))
        tempAc = null
        if (activityStack.size > 1) {
            for (index in activityStack.size - 2 downTo (0)) {
                activityStack[index].finish()
            }
        }
    }

    fun findAc(className: String): FragmentActivity? {
        for (tempAc in activityStack) {
            if (tempAc.javaClass.name == className) {
                return tempAc
            }
        }
        return null
    }
}