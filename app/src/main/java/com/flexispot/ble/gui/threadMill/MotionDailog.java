package com.flexispot.ble.gui.threadMill;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.flexispot.ble.R;


public class MotionDailog extends Dialog {

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
    protected TextView mEditText;

    public MotionDailog(Context context) {
        super(context, R.style.CustomDialogStyle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setCancelable(false);  // 是否可以撤销
        setContentView(R.layout.dailog_motionedit);
        hintTv = (TextView) findViewById(R.id.tv_title);
        doubleLeftBtn = (TextView) findViewById(R.id.tv_left);
        doubleRightBtn = (TextView) findViewById(R.id.tv_right);
        mEditText = (TextView) findViewById(R.id.message);

    }

    /**
     * 设置右键文字和点击事件
     *
     * @param rightStr 文字
     * @param clickListener 点击事件
     */
    public void setRightButton(String rightStr, View.OnClickListener clickListener) {
        doubleRightBtn.setOnClickListener(clickListener);
        doubleRightBtn.setText(rightStr);
    }

    /**
     * 设置左键文字和点击事件
     *
     * @param leftStr 文字
     * @param clickListener 点击事件
     */
    public void setLeftButton(String leftStr, View.OnClickListener clickListener) {
        doubleLeftBtn.setOnClickListener(clickListener);
        doubleLeftBtn.setText(leftStr);
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

    public String getEditText() {
        return mEditText.getText().toString();
    }


    public void setEditText(String s){
        mEditText.setText(s);
    }


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
