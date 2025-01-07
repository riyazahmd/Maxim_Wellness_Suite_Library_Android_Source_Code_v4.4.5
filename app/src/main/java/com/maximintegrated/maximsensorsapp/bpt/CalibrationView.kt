package com.maximintegrated.maximsensorsapp.bpt

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.isVisible
import com.google.android.material.card.MaterialCardView
import com.maximintegrated.maximsensorsapp.R
import com.maximintegrated.maximsensorsapp.toIntOrZero
import kotlinx.android.synthetic.main.view_calibration_card.view.*

class CalibrationView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    var isAutoReference = false

    var status = CalibrationStatus.IDLE
        set(value) {
            field = value
            when (value) {
                CalibrationStatus.IDLE -> {
                    progressBar.isVisible = false
                    statusImageView.isVisible = true
                    repeatButton.isVisible = false
                    newButton.isVisible = false
                    if(isAutoReference){
                        calibrationButton.text = context.getString(R.string.repeat)
                        calibrationButton.isEnabled = true
                        sbpEditText1.isEnabled = false
                        dbpEditText1.isEnabled = false
                    } else {
                        calibrationButton.text = context.getString(R.string.start)
                        calibrationButton.isEnabled = true
                        sbpEditText1.isEnabled = true
                        dbpEditText1.isEnabled = true
                    }
                }
                CalibrationStatus.REF_STARTED -> {
                    progressBar.isVisible = false
                    statusImageView.isVisible = false
                    calibrationButton.text = context.getString(R.string.bpt_ref_measuring)
                    calibrationButton.isEnabled = false
                    repeatButton.isVisible = false
                    newButton.isVisible = false
                    sbpEditText1.isEnabled = false
                    dbpEditText1.isEnabled = false
                }
                CalibrationStatus.STARTED -> {
                    progressBar.isVisible = false
                    statusImageView.isVisible = false
                    calibrationButton.text = context.getString(R.string.stop)
                    calibrationButton.isEnabled = true
                    repeatButton.isVisible = false
                    newButton.isVisible = false
                    sbpEditText1.isEnabled = false
                    dbpEditText1.isEnabled = false

                }
                CalibrationStatus.PROCESSING -> {
                    progressBar.isVisible = true
                    statusImageView.isVisible = false
                    calibrationButton.text = context.getString(R.string.wait)
                    calibrationButton.isEnabled = false
                    repeatButton.isVisible = false
                    newButton.isVisible = false
                    sbpEditText1.isEnabled = false
                    dbpEditText1.isEnabled = false
                }
                CalibrationStatus.SUCCESS -> {
                    progressBar.isVisible = false
                    statusImageView.isVisible = true
                    statusImageView.setImageResource(R.drawable.ic_check)
                    calibrationButton.text = context.getString(R.string.done)
                    calibrationButton.isEnabled = true
                    repeatButton.isVisible = true
                    newButton.isVisible = true
                    if(isAutoReference){
                        sbpEditText1.isEnabled = false
                        dbpEditText1.isEnabled = false
                    } else {
                        sbpEditText1.isEnabled = true
                        dbpEditText1.isEnabled = true
                    }
                }
                CalibrationStatus.FAIL -> {
                    progressBar.isVisible = false
                    statusImageView.isVisible = true
                    statusImageView.setImageResource(R.drawable.ic_warning)
                    calibrationButton.isEnabled = true
                    repeatButton.isVisible = false
                    newButton.isVisible = false
                    if(isAutoReference){
                        calibrationButton.text = context.getString(R.string.repeat)
                        sbpEditText1.isEnabled = false
                        dbpEditText1.isEnabled = false
                    } else {
                        calibrationButton.text = context.getString(R.string.start)
                        sbpEditText1.isEnabled = true
                        dbpEditText1.isEnabled = true
                    }
                }
            }
        }

    var sbp: Int = 0
        get() = sbpEditText1.text.toString().toIntOrZero()
        set(value) {
            field = value
            sbpEditText1.setText(value.toString())
        }

    var dbp: Int = 0
        get() = dbpEditText1.text.toString().toIntOrZero()
        set(value) {
            field = value
            dbpEditText1.setText(value.toString())
        }

    init {
        inflate(context, R.layout.view_calibration_card, this)
        with(
            context.obtainStyledAttributes(
                attrs,
                R.styleable.CalibrationView,
                defStyleAttr,
                0
            )
        ) {
            titleTextView.text = getText(R.styleable.CalibrationView_cv_title) ?: ""
            recycle()
        }
    }

    fun reset() {
        sbpEditText1.setText("")
        dbpEditText1.setText("")
    }
}