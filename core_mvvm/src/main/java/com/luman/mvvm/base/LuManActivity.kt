package com.luman.mvvm.base

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.PersistableBundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.gyf.barlibrary.ImmersionBar
import com.luman.core.LumanHelper
import com.luman.mvvm.R


/**
 * Activity基类
 */
abstract class LuManActivity<V : ViewDataBinding, VM : LuManViewModel> : FragmentActivity(), IView {

    lateinit var dataBinding: V
    open lateinit var viewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)

        LumanHelper.aboutActivityManager().push(this)
        dataBinding = DataBindingUtil.setContentView(this, layoutId())
        viewModel = ViewModelProviders.of(this, vmFactory())
            .get(LumanHelper.aboutFunc().getClassByGenericityPos<VM>(this, 1)!!)
        dataBinding.lifecycleOwner = this
        observeVM()
        viewOpe()
//        ImmersionBar.with(this).statusBarDarkFont(ifDark()).fitsSystemWindows(true)
//            .statusBarColor(barColor()).init()

    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putBoolean("SaveState",true);
//
//    }
//    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
//        super.onRestoreInstanceState(savedInstanceState)
//        savedInstanceState.putBoolean("SaveState",true);
//    }
//
//    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
//        super.onSaveInstanceState(outState, outPersistentState)
//        outState.putBoolean("SaveState",true);
//    }

    override fun onDestroy() {
        super.onDestroy()
        LumanHelper.aboutActivityManager().pop(this)
        dataBinding.unbind()
    }

    abstract fun layoutId(): Int
    open fun barColor(): Int = R.color.bg_color
    open fun ifDark(): Boolean = true
    open fun vmFactory(): ViewModelProvider.NewInstanceFactory? = null
    override fun viewOpe() {}
    override fun observeVM() {
        viewModel.toastMsg().observe(this, Observer {
            LumanHelper.aboutToast().showShortToast(it)
        })
        viewModel.dialogFlag().observe(this, Observer {

        })
    }

//    protected override fun onResume() {
//        super.onResume()
//    }
}