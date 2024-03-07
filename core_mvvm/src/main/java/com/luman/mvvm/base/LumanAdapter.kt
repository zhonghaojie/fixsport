package com.luman.mvvm.base

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextWatcher
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luman.mvvm.R

/**
 * 通用Adapter
 */
abstract class LumanAdapter<T> : RecyclerView.Adapter<LumanAdapter<T>.BaseViewHolder> {

    private val context: Context
    private val inflater: LayoutInflater

    private var data: MutableList<T>?
    private val layoutId: Int

    constructor(context: Context, data: MutableList<T>?, layoutId: Int) {
        this.context = context
        this.inflater = LayoutInflater.from(context)
        this.data = data
        this.layoutId = layoutId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        BaseViewHolder(inflater.inflate(layoutId, parent, false))

    override fun getItemCount() = if (data == null) 0 else data!!.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (data.isNullOrEmpty()) return
        setWidget(data!![position], holder, position)
    }

    /**
     * 设置项内容
     */
    protected abstract fun setWidget(data: T, holder: BaseViewHolder, position: Int)

    /**
     * 更改data引用式刷新
     */
    fun reloadData(data: MutableList<T>) {
        this.data = data
        notifyDataSetChanged()
    }

    inner class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /**
         * 视图缓存集
         */
        private val mViews: SparseArray<View> = SparseArray()

        private fun getView(widget: Int): View? {
            var view: View? = mViews.get(widget)
            if (view == null) {
                view = itemView.findViewById(widget)
            }
            return view
        }

        /*设置文字控件*/
        fun setText(widgetId: Int, str: String?): BaseViewHolder {
            (getView(widgetId) as TextView).text = str ?: ""
            return this
        }

        fun setHint(widgetId: Int, str: String?): BaseViewHolder {
            (getView(widgetId) as EditText).hint = str ?: ""
            return this
        }

        /*设置文字控件*/
        fun setText(widgetId: Int, strSourceId: Int): BaseViewHolder {
            (getView(widgetId) as TextView).setText(strSourceId)
            return this
        }

        fun setRadioCheck(widgetId: Int, state: Boolean): BaseViewHolder {
            (getView(widgetId) as RadioButton).isChecked = state
            return this
        }

        fun setImage(widgetId: Int, resourceId: Int): BaseViewHolder {
            (getView(widgetId) as ImageView).setImageResource(resourceId)
            return this
        }

        fun ifShadow(state: Boolean): BaseViewHolder {
            if (state) {
                val loadAnimation = AnimationUtils.loadAnimation(context, R.anim.shadow)
                itemView.startAnimation(loadAnimation)
            }
            return this
        }

        /*设置TextViewDrawable*/
        fun setTextDrawable(
            widgetId: Int,
            left: Drawable?,
            top: Drawable?,
            right: Drawable?,
            bottom: Drawable?
        ): BaseViewHolder {
            left?.setBounds(0, 0, left.intrinsicWidth, left.minimumHeight)
            top?.setBounds(0, 0, top.intrinsicWidth, top.minimumHeight)
            right?.setBounds(0, 0, right.intrinsicWidth, right.minimumHeight)
            bottom?.setBounds(0, 0, bottom.intrinsicWidth, bottom.minimumHeight)
            (getView(widgetId) as TextView).setCompoundDrawables(left, top, right, bottom)
            return this
        }

        /*设置本地图形控件*/
        fun loadImage(widgetId: Int, resorceId: Int): BaseViewHolder {
            (getView(widgetId) as ImageView).setImageResource(resorceId)
            return this
        }

        /*设置控件点击监听*/
        fun setClickListner(widgetId: Int, listner: View.OnClickListener): BaseViewHolder {
            getView(widgetId)?.setOnClickListener(listner)
            return this
        }

        /*设置控件长按监听*/
        fun setLongClickListner(widgetId: Int, listener: View.OnLongClickListener): BaseViewHolder {
            getView(widgetId)?.setOnLongClickListener(listener)
            return this
        }

        /*设置Enable属性*/
        fun setEnable(widdgetId: Int, enable: Boolean): BaseViewHolder {
            getView(widdgetId)?.isEnabled = enable
            return this
        }

        /*设置空间可见性*/
        fun setVisible(widgetId: Int, visible: Int): BaseViewHolder {
            getView(widgetId)?.visibility = visible
            return this
        }

        fun setInputType(widgetId: Int, inputType: Int): BaseViewHolder {
            (getView(widgetId) as EditText).inputType = inputType
            return this
        }

        fun getVisisble(widgetId: Int) = getView(widgetId)?.visibility

        /*输入监听*/
        fun addOnTextChangeListner(widgetId: Int, watcher: TextWatcher) {
            (getView(widgetId) as EditText).addTextChangedListener(watcher)
        }

        /*设置背景*/
        fun setBackGroundResource(widgetId: Int, resourceId: Int): BaseViewHolder {
            getView(widgetId)?.setBackgroundResource(resourceId)
            return this
        }

        /*设置背景*/
        fun setBackGround(widgetId: Int, drawable: Drawable): BaseViewHolder {
            getView(widgetId)?.background = drawable
            return this
        }

        /*设置字体颜色*/
        fun setTextColor(widgetId: Int, color: Int): BaseViewHolder {
            (getView(widgetId) as TextView).setTextColor(color)
            return this
        }

        /* 设置字体风格 */
        fun setTextStyle(widgetId: Int, type: Typeface): BaseViewHolder {
            (getView(widgetId) as TextView).setTypeface(type)
            return this
        }

        /*设置复选框选中状态*/
        fun setChecked(widget: Int, checketd: Boolean): BaseViewHolder {
            (getView(widget) as CheckBox).isChecked = checketd
            return this
        }

        fun setOnCheckedChangeListener(
            widgetId: Int,
            listener: CompoundButton.OnCheckedChangeListener
        ): BaseViewHolder {
            (getView(widgetId) as CheckBox).setOnCheckedChangeListener(listener)
            return this
        }

        /*设置适配器*/
        fun setAdapter(widgetId: Int, adapter: LumanAdapter<*>): BaseViewHolder {
            val view = getView(widgetId) as RecyclerView?
            view!!.layoutManager = LinearLayoutManager(context)
            view.adapter = adapter
            return this
        }
    }

    /**
     * 视图抖动
     */
    fun showShakeAnim(view: View): ObjectAnimator {
        val anim = ObjectAnimator.ofFloat(view, "rotation", -2F, 2F)
        anim.duration = 100
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.repeatCount = Integer.MAX_VALUE
        anim.repeatMode = ObjectAnimator.REVERSE
        anim.start()
        return anim
    }

}