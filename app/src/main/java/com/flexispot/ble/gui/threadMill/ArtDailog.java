package com.flexispot.ble.gui.threadMill;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.flexispot.ble.R;


public class ArtDailog extends Dialog {

    /**
     * 提示
     */
    protected TextView hintTv;

    /**
     * 左边按钮
     */
    protected TextView doubleLeftBtn;

    /**
     * 右边按钮
     */
    protected TextView doubleRightBtn;

    /**
     * 输入框
     */
//    protected EditText mEditText;

    public ArtDailog(Context context) {
        super(context, R.style.CustomDialogStyle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setCancelable(false);  // 是否可以撤销
        setContentView(R.layout.dialog_treadmill_reset);
        hintTv = (TextView) findViewById(R.id.tv_title);
        doubleLeftBtn = (TextView) findViewById(R.id.tv_cancel);
        doubleRightBtn = (TextView) findViewById(R.id.tv_submit);
//        mEditText = (EditText) findViewById(R.id.et_dailog);
    }

    /**
     * 设置右键文字和点击事件
     *
     * @param clickListener 点击事件
     */
    public void setRightButton( View.OnClickListener clickListener) {
        doubleRightBtn.setOnClickListener(clickListener);
    }

    /**
     * 设置左键文字和点击事件
     *
     * @param clickListener 点击事件
     */
    public void setLeftButton( View.OnClickListener clickListener) {
        doubleLeftBtn.setOnClickListener(clickListener);
    }

    /**
     * 设置提示内容
     *
     * @param str 内容
     */
    public void setHintText(String str) {
        hintTv.setText(str);
        hintTv.setVisibility(View.VISIBLE);
    }

//    public String getEditText() {
//        return mEditText.getText().toString();
//    }

    /**
     * 给两个按钮 设置文字
     *
     * @param leftStr 左按钮文字
     * @param rightStr 右按钮文字
     */
    public void setBtnText(String leftStr, String rightStr) {
        doubleLeftBtn.setText(leftStr);
        doubleRightBtn.setText(rightStr);
    }
}
