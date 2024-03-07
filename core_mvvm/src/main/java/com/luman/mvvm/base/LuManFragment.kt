package com.luman.mvvm.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.luman.core.LumanHelper

/**
 * @Editor luman
 * @Time 2019-10-29 10:43
 **/
abstract class LuManFragment<V : ViewDataBinding, VM : LuManViewModel> : Fragment(), IView {

    protected lateinit var dataBinding: V
    protected lateinit var viewModel: VM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataBinding = DataBindingUtil.inflate(inflater, layoutId(), container, false)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, vmFactory())
            .get(LumanHelper.aboutFunc().getClassByGenericityPos<VM>(this, 1)!!)
        observeVM()
        viewOpe()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dataBinding.unbind()
    }

    abstract fun layoutId(): Int
    open fun vmFactory(): ViewModelProvider.NewInstanceFactory? = null
    override fun viewOpe() {}
    override fun observeVM() {
        viewModel.toastMsg().observe(this, Observer {
            LumanHelper.aboutToast().showShortToast(it)
        })
        viewModel.dialogFlag().observe(this, Observer {

        })
    }
}