package com.flexispot.ble.gui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.flexispot.ble.R;


public class AlertDialog extends Dialog implements View.OnClickListener{
    private TextView tv_submit;



    private Context mContext;
    private String content;
    private OnCloseListener listener;
    private String positiveName;
    private String negativeName;
    private String title;


    public AlertDialog(Context context) {
        super(context);
        this.mContext = context;
    }


    public AlertDialog(Context context, int themeResId, String content) {
        super(context, themeResId);
        this.mContext = context;
        this.content = content;
    }


    public AlertDialog(Context context, int themeResId, String content, OnCloseListener listener) {
        super(context, themeResId);
        this.mContext = context;
        this.content = content;
        this.listener = listener;
    }


    protected AlertDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.mContext = context;
    }


    public AlertDialog setTitle(String title){
        this.title = title;
        return this;
    }


    public AlertDialog setPositiveButton(String name){
        this.positiveName = name;
        return this;
    }


    public AlertDialog setNegativeButton(String name){
        this.negativeName = name;
        return this;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_disconnected);
        setCanceledOnTouchOutside(false);
        initView();
    }


    private void initView(){

        tv_submit = (TextView)findViewById(R.id.tv_submit);
        tv_submit.setOnClickListener(this);


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.tv_submit:
                if(listener != null){
                    listener.onClick(this, true);
                }
                break;
        }
    }


    public interface OnCloseListener{
        void onClick(Dialog dialog, boolean confirm);
    }
}