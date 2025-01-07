package com.maximintegrated.maximsensorsapp.bpt

import android.content.Context
import android.os.Handler
import android.text.Editable
import android.util.AttributeSet
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.card.MaterialCardView
import com.maximintegrated.maximsensorsapp.R
import com.maximintegrated.maximsensorsapp.showAlertDialog
import com.maximintegrated.maximsensorsapp.toIntOrZero
import kotlinx.android.synthetic.main.view_old_protocol_calibration_card.view.*
import java.util.*

class OldProtocolCalibrationView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        const val WAITING_TIME_FOR_NEW_REFERENCE_DATA_IN_SEC = 2
    }

    var status = CalibrationStatus.IDLE
        set(value) {
            field = value
            when (value) {
                CalibrationStatus.IDLE -> {
                    progressBar.isVisible = false
                    statusImageView.isVisible = true
                    calibrationButton.text = context.getString(R.string.start)
                    repeatButton.isVisible = false
                    confirmCheckBox1.isEnabled = true
                    confirmCheckBox2.isEnabled = true
                    confirmCheckBox3.isEnabled = true
                }
                CalibrationStatus.STARTED -> {
                    progressBar.isVisible = false
                    statusImageView.isVisible = false
                    calibrationButton.text = context.getString(R.string.stop)
                    repeatButton.isVisible = false
                    confirmCheckBox1.isEnabled = false
                    confirmCheckBox2.isEnabled = false
                    confirmCheckBox3.isEnabled = false
                    myHandler.removeCallbacks(tickRunnable)
                }
                CalibrationStatus.PROCESSING -> {
                    progressBar.isVisible = true
                    statusImageView.isVisible = false
                    calibrationButton.text = context.getString(R.string.wait)
                    repeatButton.isVisible = false
                    confirmCheckBox1.isEnabled = false
                    confirmCheckBox2.isEnabled = false
                    confirmCheckBox3.isEnabled = false
                }
                CalibrationStatus.SUCCESS -> {
                    progressBar.isVisible = false
                    statusImageView.isVisible = true
                    statusImageView.setImageResource(R.drawable.ic_check)
                    calibrationButton.text = context.getString(R.string.done)
                    repeatButton.isVisible = true
                    confirmCheckBox1.isEnabled = true
                    confirmCheckBox2.isEnabled = true
                    confirmCheckBox3.isEnabled = true
                }
                CalibrationStatus.FAIL -> {
                    progressBar.isVisible = false
                    statusImageView.isVisible = true
                    statusImageView.setImageResource(R.drawable.ic_warning)
                    calibrationButton.text = context.getString(R.string.start)
                    repeatButton.isVisible = false
                    confirmCheckBox1.isEnabled = true
                    confirmCheckBox2.isEnabled = true
                    confirmCheckBox3.isEnabled = true
                }
            }
        }

    var sbp1: Int = 0
        get() = sbpEditText1.text.toString().toIntOrZero()
        set(value) {
            field = value
            sbpEditText1.setText(value.toString())
        }

    var dbp1: Int = 0
        get() = dbpEditText1.text.toString().toIntOrZero()
        set(value) {
            field = value
            dbpEditText1.setText(value.toString())
        }

    var sbp2: Int = 0
        get() = sbpEditText2.text.toString().toIntOrZero()
        set(value) {
            field = value
            sbpEditText2.setText(value.toString())
        }

    var dbp2: Int = 0
        get() = dbpEditText2.text.toString().toIntOrZero()
        set(value) {
            field = value
            dbpEditText2.setText(value.toString())
        }

    var sbp3: Int = 0
        get() = sbpEditText3.text.toString().toIntOrZero()
        set(value) {
            field = value
            sbpEditText3.setText(value.toString())
        }

    var dbp3: Int = 0
        get() = dbpEditText3.text.toString().toIntOrZero()
        set(value) {
            field = value
            dbpEditText3.setText(value.toString())
        }

    private var referenceMeasurementState = 1
        set(value) {
            field = value
            updateCalibrationViews()
        }

    private val order = LinkedList<Int>()

    private val myHandler = Handler()

    private fun updateCalibrationViews() {
        when (referenceMeasurementState) {
            1 -> {
                availableTextView1.isInvisible = false
                timerTextView1.isInvisible = false
                confirmCheckBox1.isInvisible = true
            }
            2 -> {
                availableTextView2.isInvisible = false
                timerTextView2.isInvisible = false
                confirmCheckBox2.isInvisible = true
            }
            3 -> {
                availableTextView3.isInvisible = false
                timerTextView3.isInvisible = false
                confirmCheckBox3.isInvisible = true
            }
        }
    }

    private var counter = WAITING_TIME_FOR_NEW_REFERENCE_DATA_IN_SEC
    private val tickRunnable = object : Runnable {
        override fun run() {
            if (counter != 0) {
                counter--
                myHandler.postDelayed(this, 1000)
            }
            if (counter == 0) {
                when (referenceMeasurementState) {
                    1 -> {
                        confirmCheckBox1.isInvisible = false
                        availableTextView1.isInvisible = true
                        timerTextView1.isInvisible = true
                        sbpEditText1.isEnabled = true
                        dbpEditText1.isEnabled = true
                    }
                    2 -> {
                        confirmCheckBox2.isInvisible = false
                        availableTextView2.isInvisible = true
                        timerTextView2.isInvisible = true
                        sbpEditText2.isEnabled = true
                        dbpEditText2.isEnabled = true
                    }
                    3 -> {
                        confirmCheckBox3.isInvisible = false
                        availableTextView3.isInvisible = true
                        timerTextView3.isInvisible = true
                        sbpEditText3.isEnabled = true
                        dbpEditText3.isEnabled = true
                    }
                }
                myHandler.removeCallbacks(this)
            }
            when (referenceMeasurementState) {
                1 -> {
                    timerTextView1.text = String.format("00:%02d", counter)
                }
                2 -> {
                    timerTextView2.text = String.format("00:%02d", counter)
                }
                3 -> {
                    timerTextView3.text = String.format("00:%02d", counter)
                }
            }
        }
    }

    init {
        inflate(context, R.layout.view_old_protocol_calibration_card, this)
        order.addAll(listOf(1, 2, 3))
        with(
            context.obtainStyledAttributes(
                attrs,
                R.styleable.OldProtocolCalibrationView,
                defStyleAttr,
                0
            )
        ) {
            titleTextView.text = getText(R.styleable.OldProtocolCalibrationView_opcv_title) ?: ""
            recycle()
        }
        confirmCheckBox1.setOnCheckedChangeListener { _, isChecked ->
            sbpEditText1.isEnabled = !isChecked
            dbpEditText1.isEnabled = !isChecked
            val enableButton =
                confirmCheckBox1.isChecked && confirmCheckBox2.isChecked && confirmCheckBox3.isChecked
            calibrationButton.isEnabled = enableButton
            repeatButton.isEnabled = enableButton
            if (isChecked && referenceMeasurementState == 1) {
                if (order.isNotEmpty()) {
                    showAlertDialog(
                        context,
                        context.getString(R.string.warning),
                        "Wait at least 1 minute before taking a reference measurement.",
                        context.getString(R.string.ok)
                    )
                    handler.removeCallbacks(tickRunnable)
                    counter = WAITING_TIME_FOR_NEW_REFERENCE_DATA_IN_SEC
                    handler.postDelayed(tickRunnable, 1000)
                    referenceMeasurementState = order.removeFirst()
                } else {
                    referenceMeasurementState = 0
                }
            }
        }
        confirmCheckBox2.setOnCheckedChangeListener { _, isChecked ->
            sbpEditText2.isEnabled = !isChecked
            dbpEditText2.isEnabled = !isChecked
            val enableButton =
                confirmCheckBox1.isChecked && confirmCheckBox2.isChecked && confirmCheckBox3.isChecked
            calibrationButton.isEnabled = enableButton
            repeatButton.isEnabled = enableButton
            if (isChecked && referenceMeasurementState == 2) {
                if (order.isNotEmpty()) {
                    showAlertDialog(
                        context,
                        context.getString(R.string.warning),
                        "Wait at least 1 minute before taking a reference measurement.",
                        context.getString(R.string.ok)
                    )
                    myHandler.removeCallbacks(tickRunnable)
                    counter = WAITING_TIME_FOR_NEW_REFERENCE_DATA_IN_SEC
                    myHandler.postDelayed(tickRunnable, 1000)
                    referenceMeasurementState = order.removeFirst()
                } else {
                    referenceMeasurementState = 0
                }
            }
        }
        confirmCheckBox3.setOnCheckedChangeListener { _, isChecked ->
            sbpEditText3.isEnabled = !isChecked
            dbpEditText3.isEnabled = !isChecked
            val enableButton =
                confirmCheckBox1.isChecked && confirmCheckBox2.isChecked && confirmCheckBox3.isChecked
            calibrationButton.isEnabled = enableButton
            repeatButton.isEnabled = enableButton
            if (isChecked && referenceMeasurementState == 3) {
                if (order.isNotEmpty()) {
                    showAlertDialog(
                        context,
                        context.getString(R.string.warning),
                        "Wait at least 1 minute before taking a reference measurement.",
                        context.getString(R.string.ok)
                    )
                    myHandler.removeCallbacks(tickRunnable)
                    counter = WAITING_TIME_FOR_NEW_REFERENCE_DATA_IN_SEC
                    myHandler.postDelayed(tickRunnable, 1000)
                    referenceMeasurementState = order.removeFirst()
                } else {
                    referenceMeasurementState = 0
                }
            }
        }

        sbpEditText1.doAfterTextChanged {
            confirmCheckBox1.isEnabled = checkValueExists(it, dbpEditText1.text)
        }
        dbpEditText1.doAfterTextChanged {
            confirmCheckBox1.isEnabled = checkValueExists(sbpEditText1.text, it)
        }
        sbpEditText2.doAfterTextChanged {
            confirmCheckBox2.isEnabled = checkValueExists(it, dbpEditText2.text)
        }
        dbpEditText2.doAfterTextChanged {
            confirmCheckBox2.isEnabled = checkValueExists(sbpEditText2.text, it)
        }
        sbpEditText3.doAfterTextChanged {
            confirmCheckBox3.isEnabled = checkValueExists(it, dbpEditText3.text)
        }
        dbpEditText3.doAfterTextChanged {
            confirmCheckBox3.isEnabled = checkValueExists(sbpEditText3.text, it)
        }

        order.removeFirst()
    }

    private fun checkValueExists(sbpEditable: Editable?, dbpEditable: Editable?): Boolean {
        return if (sbpEditable == null || dbpEditable == null) {
            false
        } else {
            !(sbpEditable.toString() == "" || dbpEditable.toString() == "")
        }
    }

    fun discardRefMeasurement(index: Int) {
        when (index) {
            1 -> {
                sbp1 = sbp2
                sbp2 = sbp3
                dbp1 = dbp2
                dbp2 = dbp3
            }
            2 -> {
                sbp2 = sbp3
                dbp2 = dbp3
            }
        }
        sbpEditText3.setText("")
        dbpEditText3.setText("")
        confirmCheckBox3.isChecked = false
    }
}