package com.maximintegrated.maximsensorsapp

import android.view.ViewGroup
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.RecyclerView
import com.maximintegrated.maximsensorsapp.exts.color

class FileListAdapter(private val itemClickListener: RecyclerViewClickListener) :
    RecyclerView.Adapter<FileListViewHolder>() {

    var fileList: MutableList<DocumentFile> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileListViewHolder {
        return FileListViewHolder(itemClickListener, parent)
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    override fun onBindViewHolder(holder: FileListViewHolder, position: Int) {
        holder.bind(fileList[position])
        if (position % 2 != 0) {
            holder.itemView.setBackgroundColor(holder.itemView.context.color(R.color.color_archive_primary))
        } else {
            holder.itemView.setBackgroundColor(holder.itemView.context.color(R.color.color_archive_secondary))
        }
    }
}
