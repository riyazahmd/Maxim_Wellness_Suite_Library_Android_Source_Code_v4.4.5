package com.maximintegrated.maximsensorsapp

import android.content.res.ColorStateList
import android.graphics.Color.RED
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.RecyclerView
import com.maximintegrated.maximsensorsapp.exts.color
import com.maximintegrated.maximsensorsapp.exts.drawable
import kotlinx.android.synthetic.main.archive_file_item.view.*

class FileListViewHolder(
    private val onItemClicked: RecyclerViewClickListener,
    parent: ViewGroup
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.archive_file_item,
        parent,
        false
    )
) {
    private val textViewFileName: TextView  by lazy { itemView.file_name }
    private val textViewCreateDate: TextView  by lazy { itemView.file_create_date }
    private val imageViewDelete: ImageView  by lazy { itemView.delete_icon }
    private val imageViewShare: ImageView  by lazy { itemView.share_icon }
    private val imageViewSleep: ImageView  by lazy { itemView.sleep_icon }
    private val imageViewAddInfo: ImageView  by lazy { itemView.add_info_icon }

    private lateinit var item: DocumentFile

    init {
        itemView.setOnClickListener { onItemClicked.onRowClicked(item) }
        imageViewDelete.setOnClickListener { onItemClicked.onDeleteClicked(item) }
        imageViewShare.setOnClickListener { onItemClicked.onShareClicked(item) }
        imageViewSleep.setOnClickListener { onItemClicked.onSleepClicked(item) }
        imageViewAddInfo.setOnClickListener { onItemClicked.onAddInfoClicked(item) }
    }

    fun bind(item: DocumentFile) {
        this.item = item

        val name = (item.name!!.substring(0, item.name!!.lastIndexOf('.'))).replace(BASE_FILE_NAME_PREFIX,"")
        val nameWithoutDate = name.substringAfter("_").substringAfter("_").substringAfter("_")
        var date = name.replace("_$nameWithoutDate","")
        textViewFileName.text = nameWithoutDate
        textViewCreateDate.text = date //FILE_TIMESTAMP_FORMAT.format(Date(item.lastModified()))
        if( name.indexOf("SpO2",0) == -1){
            imageViewAddInfo.isEnabled = false
            imageViewAddInfo.setColorFilter(itemView.context.color(R.color.color_archive_add_info_disabled))
        } else{
            imageViewAddInfo.isEnabled = true
            imageViewAddInfo.setColorFilter(itemView.context.color(R.color.color_archive_add_info_enabled))
        }
    }
}