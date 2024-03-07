package com.luman.core.util

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.luman.core.LumanHelper

/**
 * @Editor luman
 * @Time 2019-10-28 17:02
 **/
object ToastUtil {

    private var mToast: Toast? = null
    private var mRunnable: Runnable? = null
    private val mHandler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    fun showLongToast(msg: String?) {
        showToast(msg, Toast.LENGTH_LONG)
    }

    fun showShortToast(msg: String?) {
        showToast(msg, Toast.LENGTH_SHORT)
    }

    fun showLongToast(msgId: Int) {
        if (ContextUtil.getApplication() == null) return

        showLongToast(
            ContextUtil.getApplication()!!.getString(
                msgId
            )
        )
    }

    fun showShortToast(msgId: Int) {
        if (ContextUtil.getApplication() == null) return

        showShortToast(
            ContextUtil.getApplication()!!.getString(
                msgId
            )
        )
    }

    fun clear() {
        if (mRunnable != null) {
            mHandler.removeCallbacks(mRunnable)
            mRunnable = null
        }
        if (mToast != null) {
            mToast?.cancel()
            mToast = null
        }
    }

    private fun showToast(msg: String?, duration: Int) {
        if (ContextUtil.getApplication() == null) return

        if (msg == null) {
            return
        }
        if (mRunnable != null) {
            mHandler.removeCallbacks(mRunnable)
        }
        mRunnable = Runnable {
            if (mToast == null) {
                mToast =
                    Toast.makeText(LumanHelper.getApplicationContext(), msg, Toast.LENGTH_SHORT)
            }
            mToast?.setText(msg)
            mToast?.show()
            mRunnable = null
        }
        mHandler.post(mRunnable)
    }

}