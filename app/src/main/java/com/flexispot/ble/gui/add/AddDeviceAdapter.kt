package com.flexispot.ble.gui.add

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.flexispot.ble.ImgRelation
import com.flexispot.ble.LabelRelation
import com.flexispot.ble.R
import com.flexispot.ble.data.bean.AddLabel
import com.flexispot.ble.data.bean.Device

/**
 * @author luman
 * @date 19-11-26
 * 添加设备的适配器
 **/
class AddDeviceAdapter(
    private var data: ArrayList<AddLabel>,
    private val context: Context,
    private var localDevices: ArrayList<Device>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0) {
            return HeaderViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.item_add_label,
                    parent,
                    false
                )
            )
        } else {
            return ContentViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.item_add_device,
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val tempData = data[position]
        if (getItemViewType(position) == 0) {
            val headerHolder = (holder as HeaderViewHolder)
            headerHolder.tvTitle.setText(tempData.name)
        } else {
            val contentHolder = (holder as ContentViewHolder)
            contentHolder.tvName.setText(LabelRelation.getLabelByType(tempData.deviceType.type))
            contentHolder.ivPic.setImageResource(ImgRelation.getTypeInDevices(tempData.deviceType.type))
            contentHolder.item.setOnClickListener {
                val bundle = Bundle()
                bundle.putSerializable("params", localDevices)
                bundle.putSerializable("type", tempData.deviceType)
                Navigation.findNavController(contentHolder.item)
                    .navigate(R.id.action_addFragment_to_aroundFragment, bundle)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].type
    }

    /**
     * 标题栏
     */
    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_label)

    }

    /**
     * 设备栏
     */
    class ContentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val ivPic: ImageView = itemView.findViewById(R.id.iv_pic)
        val item: LinearLayout = itemView.findViewById(R.id.ll_item)

    }
}