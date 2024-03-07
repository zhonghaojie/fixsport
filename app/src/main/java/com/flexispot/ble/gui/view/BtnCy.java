package com.flexispot.ble.gui.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.constraintlayout.widget.ConstraintLayout;

public class BtnCy extends ConstraintLayout {

    public BtnCy(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 记录是否开始按下
     */
    private boolean startLong = false;


    private ConstraintLayoutOnClickListener mListener;

    /**
     * 按钮长按时 间隔多少毫秒来处理 回调方法
     */
    private int mtime;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mListener != null)
                mListener.onLongPressh();
            startLong = false;
        }
    };

    /**
     * 处理touch事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            startLong = true;
            handler.sendMessageDelayed(handler.obtainMessage(1), 200);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (startLong) {
                handler.removeMessages(1);
                if (mListener != null)
                    mListener.onShortPressh();
            }
            if (mListener != null)
                mListener.onUpspring();
        }
        return true;
    }

    /**
     * 使当前线程睡眠指定的毫秒数。
     *
     * @param time 指定当前线程睡眠多久，以毫秒为单位
     */
    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 给长按btn控件注册一个监听器。
     *
     * @param listener 监听器的实现。
     */
    public void setConstraintLayoutOnClickListener(ConstraintLayoutOnClickListener listener) {
        mListener = listener;
    }

    /**
     * 按键监听事件
     */
    public interface ConstraintLayoutOnClickListener {

        /**
         * 短按
         */
        void onShortPressh();

        /**
         * 长按
         */
        void onLongPressh();

        /**
         * 松开
         */
        void onUpspring();
    }
}
