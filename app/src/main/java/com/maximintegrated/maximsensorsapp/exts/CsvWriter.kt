package com.maximintegrated.maximsensorsapp.exts

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.lang.StringBuilder
import java.util.concurrent.LinkedBlockingDeque

class CsvWriter private constructor(var filePath: Uri, var context: Context) {

    companion object {
        private val POISON_PILL = Any()
        const val LOG_VERSION_HEADER = "Log Version"

        fun open(fileUri: Uri, header: Array<String> = emptyArray(), version: Int? = null, context: Context): CsvWriter {
            val csvWriter = CsvWriter(fileUri, context)
            csvWriter.headerLineLength = 0
            if(version != null) {
                csvWriter.headerLineLength++
                csvWriter.write(LOG_VERSION_HEADER, version)
            }
            if (header.isNotEmpty()) {
                csvWriter.headerLineLength++
                csvWriter.write(*header)
            }
            return csvWriter
        }

        interface CsvWriterListener {
            fun onCompleted(isSuccessful: Boolean)
        }
    }

    private val linesQueue = LinkedBlockingDeque<Any>()

    private var headerLineLength = 0

    var isOpen = true
        private set

    var listener: CsvWriterListener? = null

    private var delete = false

    private var packetLossOccurred = false

    init {
        ioThread {
            val file = DocumentFile.fromTreeUri(context, filePath)!!
            var count = 0
            var flushed = false
            context.contentResolver.openOutputStream(filePath)!!.writer().use { out ->

                while (true) {
                    val line = linesQueue.take()
                    if (line == POISON_PILL) {
                        break
                    }
                    count++

                    out.appendln(line.toString())
                    if (count > 10000) {
                        out.flush()
                        flushed = true
                        count = 0
                    }

                }
            }
            if ((count == headerLineLength && !flushed) || delete) {
                listener?.onCompleted(false)
                file.delete()
            }else if(packetLossOccurred){
                listener?.onCompleted(false)
                val stringBuilder = StringBuilder()
                val lastIndex = file.name!!.lastIndexOf('.')
                stringBuilder.append(file.name!!.substring(0, lastIndex)) //file name without extension
                stringBuilder.append("!!!PacketLoss!!!")
                stringBuilder.append(file.name!!.substring(lastIndex))
                file.renameTo(stringBuilder.toString())
            }else{
                listener?.onCompleted(true)
            }
        }
    }

    fun write(vararg columns: Any) {
        if (isOpen) {
            val line = columns.joinToString(",")
            linesQueue.offer(line)
        } else {
            throw IllegalStateException("Writer is not open!")
        }
    }

    fun close(packetLossOccurred: Boolean = false) {
        this.packetLossOccurred = packetLossOccurred
        if (isOpen) {
            isOpen = false
            linesQueue.offer(POISON_PILL)
        } else {
            throw IllegalStateException("Writer is already closed!")
        }
    }

    fun delete(){
        delete = true
    }
}

