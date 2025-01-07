package com.maximintegrated.maximsensorsapp.temp

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import com.maximintegrated.maximsensorsapp.R
import kotlinx.android.synthetic.main.view_temp_value.view.*
import java.text.DecimalFormat

class TempValueView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    companion object {
        const val EMPTY_VALUE = "--"
    }

    var temperatureInCelsius: Float? = null
        set(value) {
            field = value
            updateValueTexts()
        }

    private val temperatureInFahrenheit: Float?
        get() = temperatureInCelsius?.let {
            celsiusToFahrenheit(it)
        }

    private val celsiusValueFormatter = DecimalFormat("#.00")
    private val fahrenheitValueFormatter = DecimalFormat("#.0")

    private val celsiusUnit = context.getString(R.string.temp_unit_celsius)
    private val fahrenheitUnit = context.getString(R.string.temp_unit_fahrenheit)

    init {
        inflate(context, R.layout.view_temp_value, this)

        updateValueTexts()
    }

    private fun updateValueTexts() {
        val formattedCelsius = temperatureInCelsius?.let { celsiusValueFormatter.format(it) }
        val formattedFahrenheit = temperatureInFahrenheit?.let { fahrenheitValueFormatter.format(it) }
        updateValueView(celsiusValueView, formattedCelsius, celsiusUnit)
        updateValueView(fahrenheitValueView, formattedFahrenheit, fahrenheitUnit)
    }

    private fun updateValueView(valueView: TextView, formattedValue: String?, unit: String) {
        valueView.text = if (formattedValue == null) {
            EMPTY_VALUE
        } else {
            buildSpannedString {
                append(formattedValue)
                append(' ')
                scale(0.75f) {
                    append(unit)
                }
            }
        }
    }
}