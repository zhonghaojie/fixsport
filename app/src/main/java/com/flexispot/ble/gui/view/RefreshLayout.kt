package com.flexispot.ble.gui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.customview.widget.ViewDragHelper
import androidx.recyclerview.widget.RecyclerView
import com.luman.core.LumanHelper

/**
 * @author luman
 * @date 19-12-17
 **/
class RefreshLayout : LinearLayout {

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mHeaderView: View
    private val mDragHelper: ViewDragHelper
    private var mHeaderHeight: Int = 0
    private var mRvTop: Int = 0

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        mDragHelper = ViewDragHelper.create(this, MyDragCallback())
        mHeaderHeight = LumanHelper.aboutFunc().dp2px(30)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (ev == null) {
            return false
        } else {
            return mDragHelper.shouldInterceptTouchEvent(ev)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            mDragHelper.processTouchEvent(event)
        }
        return true
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mDragHelper.continueSettling(true)) {
            invalidate()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        mHeaderView = getChildAt(0)
        mRecyclerView = getChildAt(1) as RecyclerView
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mRvTop = mHeaderView.top
    }

    inner class MyDragCallback : ViewDragHelper.Callback() {

        override fun tryCaptureView(child: View, pointerId: Int) =
            (child == mRecyclerView && mHeaderView.tag != "0")

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            if (top < mRvTop) {
                return mRvTop
            } else if (top > mHeaderHeight + mRvTop) {
                return mHeaderHeight + mRvTop
            }
            return top
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)
            mDragHelper.settleCapturedViewAt(0, mRvTop)
            mHeaderView.translationY = (top - mRvTop).toFloat()
            mHeaderView.translationY = -mHeaderView.measuredHeight.toFloat()
            postInvalidate()
        }

        override fun onViewPositionChanged(
            changedView: View,
            left: Int,
            top: Int,
            dx: Int,
            dy: Int
        ) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)
        }

        override fun getViewVerticalDragRange(child: View) = LumanHelper.aboutFunc().dp2px(30)
    }
}