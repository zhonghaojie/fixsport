package com.luman.mvvm.base

/**
 * @Editor luman
 * @Time 2019-10-29 09:08
 **/
interface IView {

    //vm值的绑定监听
    fun observeVM()

    //界面的初始逻辑
    fun viewOpe()
}