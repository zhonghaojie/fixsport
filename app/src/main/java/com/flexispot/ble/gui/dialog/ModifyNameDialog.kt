package com.flexispot.ble.gui.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.flexispot.ble.R
import com.flexispot.ble.data.bean.Device
import com.luman.core.LumanHelper

/**
 * @author luman
 * @date 19-11-29
 **/
class ModifyNameDialog : DialogFragment() {

    interface Callback {
        fun modified(name: String)
        fun dismiss()
    }

    companion object {
        fun getInstance(device: Device): ModifyNameDialog {
            val instance = ModifyNameDialog()
            val bundle = Bundle()
            bundle.putSerializable("params", device)
            instance.arguments = bundle
            return instance
        }
    }

    private var etName: EditText? = null
    private var device: Device? = null
    private var mCallback: Callback? = null

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            LumanHelper.aboutFunc().dp2px(300),
            LumanHelper.aboutFunc().dp2px(200)
        );
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_modify_name, container, false)
        etName = view.findViewById(R.id.et_name)
        view.findViewById<TextView>(R.id.tv_cancel)
            .setOnClickListener {
                dismiss()
            }
        view.findViewById<TextView>(R.id.tv_submit)
            .setOnClickListener {
                if (etName?.text.toString().trim() == "") {
                    LumanHelper.aboutToast().showShortToast(R.string.input_sth)
                } else {
                    dismiss()
                    mCallback?.modified(etName?.text.toString().trim())
                }
            }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (arguments != null && arguments!!["params"] != null) {
            device = arguments!!["params"] as Device?
            if (device != null) {
                etName?.setText(device!!.nickname)
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    fun setCallback(callback: Callback) {
        mCallback = callback
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mCallback?.dismiss()
    }
}