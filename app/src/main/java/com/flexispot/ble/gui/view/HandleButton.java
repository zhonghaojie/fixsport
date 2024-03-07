package com.flexispot.ble.gui.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatImageView;

import com.orhanobut.logger.Logger;

/**
 * ━━━━━━神兽出没━━━━━━
 * 　　　┏┓　　　┏┓
 * 　　┏┛┻━━━┛┻┓
 * 　　┃　　　　　　　┃
 * 　　┃　　　━　　　┃
 * 　　┃　┳┛　┗┳　┃
 * 　　┃　　　　　　　┃
 * 　　┃　　　┻　　　┃
 * 　　┃　　　　　　　┃
 * 　　┗━┓　　　┏━┛
 * 　　　　┃　　　┃  神兽保佑
 * 　　　　┃　　　┃  代码无bug
 * 　　　　┃　　　┗━━━┓
 * 　　　　┃　　　　　　　┣┓
 * 　　　　┃　　　　　　　┏┛
 * 　　　　┗┓┓┏━┳┓┏┛
 * 　　　　　┃┫┫　┃┫┫
 * 　　　　　┗┻┛　┗┻┛
 * ━━━━━━感觉萌萌哒━━━━━━
 * <p>
 * 作者：司海林
 * 邮箱：sihailin@sina.cn
 * 时间：2017/12/4
 * 描述：
 */
public class HandleButton extends AppCompatImageView {

    /**
     * 记录是否开始按下
     */
    private boolean startLong = false;


    private HandleButtonOnClickListener mListener;

    /**
     * 按钮长按时 间隔多少毫秒来处理 回调方法
     */
    private int mtime;

    /**
     * 构造函数
     *
     * @param context
     * @param attrs
     */
    public HandleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setClickable(true);
    }

    Handler mHandler = new Handler() {
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
            Logger.d("按压down");
            startLong = true;
            mHandler.sendMessageDelayed(mHandler.obtainMessage(1), 300);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            Logger.d("按压up");
            if (startLong) {
                Logger.d("按压up1");
                mHandler.removeMessages(1);
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
    public void setHandleButtonOnClickListener(HandleButtonOnClickListener listener) {
        mListener = listener;
    }

    /**
     * 按键监听事件
     */
    public interface HandleButtonOnClickListener {

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
