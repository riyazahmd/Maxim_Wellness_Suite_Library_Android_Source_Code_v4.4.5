package com.maximintegrated.maximsensorsapp

import androidx.documentfile.provider.DocumentFile


interface RecyclerViewClickListener {
    fun onRowClicked(file: DocumentFile)
    fun onDeleteClicked(file: DocumentFile)
    fun onShareClicked(file: DocumentFile)
    fun onSleepClicked(file: DocumentFile)
    fun onAddInfoClicked(file: DocumentFile)
}
