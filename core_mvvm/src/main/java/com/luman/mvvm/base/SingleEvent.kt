package com.luman.mvvm.base

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.orhanobut.logger.Logger
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @Editor luman
 * @Time 2019-10-29 10:16
 **/
class SingleEvent<T> : MutableLiveData<T>() {

    private val mWriting: AtomicBoolean = AtomicBoolean(false)

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        if (hasActiveObservers()) {
            Logger.w("Multiple observers registered, but only one will be executed")
        }
        super.observe(owner, Observer<T> {
            if (mWriting.compareAndSet(true, false)) {
                observer.onChanged(it)
            }
        })
    }

    override fun setValue(value: T?) {
        mWriting.set(true)
        super.setValue(value)
    }

    @MainThread
    fun simpleCall() {
        value = null
    }
}