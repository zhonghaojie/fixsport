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
 * @author luman
 * @date 19-12-17
 **/
class ResetDialog : DialogFragment() {

    interface Callback {
        fun sure()
        fun cancel()
    }

    private var mCallback: Callback? = null

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            LumanHelper.aboutFunc().dp2px(300),
            LumanHelper.aboutFunc().dp2px(185)
        );
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_reset, container, false)
        view.findViewById<TextView>(R.id.tv_cancel)
            .setOnClickListener {
                dismiss()
            }
        view.findViewById<TextView>(R.id.tv_submit)
            .setOnClickListener {
                dismiss()
                mCallback?.sure()
            }
        return view
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
//        mCallback?.cancel()
    }

    fun setCallback(callback: Callback) {
        mCallback = callback
    }

}
