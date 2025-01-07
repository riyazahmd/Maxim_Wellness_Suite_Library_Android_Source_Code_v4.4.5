package com.maximintegrated.maximsensorsapp.ecg

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import com.maximintegrated.maximsensorsapp.R
import kotlinx.android.synthetic.main.view_ecg_value.view.*

class EcgValueView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    companion object {
        const val EMPTY_VALUE = "--"
    }

    var title1 = ""
        set(value) {
            titleView1.text = value
            field = value
        }

    var title2 = ""
        set(value) {
            titleView2.text = value
            field = value
        }

    var value1: String? = null
        set(value) {
            valueView1.text = value ?: EMPTY_VALUE
            field = value
        }

    var value2: String? = null
        set(value) {
            valueView2.text = value ?: EMPTY_VALUE
            field = value
        }

    init {
        inflate(context, R.layout.view_ecg_value, this)
    }
}