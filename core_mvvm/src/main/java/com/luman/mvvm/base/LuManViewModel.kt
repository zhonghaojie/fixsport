package com.luman.mvvm.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luman.core.LumanHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @Editor luman
 * @Time 2019-10-29 09:24
 **/
open class LuManViewModel : ViewModel() {

    private val toastMsg = SingleEvent<String>()
    private val dialogFlag = SingleEvent<Boolean>()

    fun toastMsg(): LiveData<String> = toastMsg
    fun dialogFlag(): LiveData<Boolean> = dialogFlag

    fun showToast(str: String?) {
        LumanHelper.aboutToast().showShortToast(str ?: "未知错误")
    }

    fun showDialog() {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                dialogFlag.value = true
            }
        }
    }

    fun dismissDialog() {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                dialogFlag.value = false
            }
        }
    }


    protected fun launch(block: suspend () -> Unit) = viewModelScope.launch {
        try {
            showDialog()
            withContext(Dispatchers.IO) {
                block()
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            showToast(t.message)
        } finally {
            dismissDialog()
        }
    }
}