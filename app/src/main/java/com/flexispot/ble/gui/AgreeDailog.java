package com.flexispot.ble.gui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.flexispot.ble.R;


public class AgreeDailog extends Dialog {

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


    public AgreeDailog(Context context) {
        super(context, R.style.CustomDialogStyle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setCancelable(false);  // 是否可以撤销
        setContentView(R.layout.layout_accept_user_agreement);
        hintTv = (TextView) findViewById(R.id.privacy);
        doubleLeftBtn = (TextView) findViewById(R.id.tv_not_accept);
        doubleRightBtn = (TextView) findViewById(R.id.rl_accept);

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
     */
    public void setHintText(View.OnClickListener clickListener) {

        hintTv.setOnClickListener(clickListener);
    }

}
