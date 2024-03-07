package com.flexispot.ble.gui.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.flexispot.ble.R
import com.luman.core.LumanHelper

/**
 * @Editor luman
 * @Time 2019-12-27 09:14
 **/
class OpeLaterDialog(val source : Int)  : DialogFragment() {

    interface Callback {
        fun sure()
    }

    private var mCallback: Callback? = null

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            LumanHelper.aboutFunc().dp2px(302),
            LumanHelper.aboutFunc().dp2px(190)
        );
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_ope_later, container, false)
        view.findViewById<TextView>(R.id.tv_text)
            .text = getString(source)
        view.findViewById<TextView>(R.id.tv_submit)
            .setOnClickListener {
                dismiss()
                mCallback?.sure()
            }
        return view
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mCallback?.sure()
    }

    fun setCallback(callback: Callback) {
        mCallback = callback
    }

}
