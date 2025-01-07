package com.maximintegrated.maximsensorsapp


import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.material.card.MaterialCardView
import com.maximintegrated.maximsensorsapp.exts.parseTypedArray
import kotlinx.android.synthetic.main.view_result_card.view.*
import java.text.DecimalFormat

class ResultCardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        const val EMPTY_VALUE = "--"
        const val FLASH_PERIOD_IN_MILLIS = 1000L
    }

    var title: CharSequence
        get() = titleView.text
        set(value) {
            titleView.text = value
        }

    var value: Float? = null
        set(value) {
            field = value
            updateValueText()
        }

    var unit: CharSequence = ""
        set(value) {
            field = value
            updateValueText()
        }

    var emptyValue: CharSequence
        get() = emptyTextView.text
        set(value) {
            emptyTextView.text = value
            updateValueText()
        }

    var flashingText: CharSequence?
        get() = flashingTextView.text
        set(value) {
            flashingTextView.text = value
        }

    private var isFlashing = false
        set(value) {
            if (value != field) {
                field = value
                valueView.isInvisible = value
                flashingTextView.isVisible = value
            }
        }

    private val decimalFormatter = DecimalFormat("#.0")
    var decimalPlaces
        get() = decimalFormatter.maximumFractionDigits
        set(value) {
            decimalFormatter.minimumFractionDigits = value
            decimalFormatter.maximumFractionDigits = value
        }

    private var enableProgress = false
        set(value) {
            field = value
            confidenceGroup.visibility = if(value) View.VISIBLE else View.GONE
        }

    private var enableScdView = false
        set(value) {
            field = value
            scdGroup.visibility = if(value) View.VISIBLE else View.GONE
        }

    init {
        inflate(context, R.layout.view_result_card, this)

        emptyValue = EMPTY_VALUE

        attrs?.let {
            parseTypedArray(attrs, R.styleable.MenuItemView) {
                getString(R.styleable.MenuItemView_miv_title)?.let { title = it }
                val iconDrawableRes = getResourceId(R.styleable.MenuItemView_miv_icon, 0)
                if (iconDrawableRes != 0) {
                    iconImageView.setImageResource(iconDrawableRes)
                }
            }
        }

        with(context.obtainStyledAttributes(attrs, R.styleable.ResultCardView, defStyleAttr, 0)) {
            title = getText(R.styleable.ResultCardView_rcv_title)
            if (hasValue(R.styleable.ResultCardView_rcv_value)) {
                value = getFloat(R.styleable.ResultCardView_rcv_value, 0f)
            }
            unit = getString(R.styleable.ResultCardView_rcv_unit) ?: ""
            decimalPlaces = getInt(R.styleable.ResultCardView_rcv_decimal_places, decimalPlaces)

            if (hasValue(R.styleable.ResultCardView_rcv_flashing_text)) {
                flashingText = getText(R.styleable.ResultCardView_rcv_flashing_text)
            }

            enableProgress = getBoolean(R.styleable.ResultCardView_rcv_enable_progress, false)
            enableScdView = getBoolean(R.styleable.ResultCardView_rcv_enable_scd_view, false)
            scdStateTextView.text = Scd.NO_DECISION.displayName
            recycle()
        }
    }

    private fun updateValueText() {
        if (!isFlashing) {
            if (value == null) {
                valueView.isInvisible = true
                emptyTextView.isVisible = true
            } else {
                emptyTextView.isInvisible = true
                valueView.isVisible = true

                valueView.text = buildSpannedString {
                    append(decimalFormatter.format(value))
                    scale(0.6f) {
                        append(' ')
                        append(unit)
                    }
                }
            }
        } else {
            emptyTextView.isInvisible = true
            valueView.isInvisible = true
        }
    }

    fun startFlashing() {
        if (!isFlashing) {
            isFlashing = true
            updateValueText()
            updateFlashingTextVisibility()
        }
    }

    fun stopFlashing() {
        isFlashing = false
        updateValueText()
    }

    private fun updateFlashingTextVisibility() {
        if (isFlashing) {
            postDelayed({
                if (isFlashing) {
                    flashingTextView.isInvisible = flashingTextView.isVisible
                    updateFlashingTextVisibility()
                }
            }, FLASH_PERIOD_IN_MILLIS)
        }
    }
}