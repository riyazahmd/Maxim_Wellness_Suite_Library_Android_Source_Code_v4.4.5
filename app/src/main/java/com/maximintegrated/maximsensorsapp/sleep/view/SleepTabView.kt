package com.maximintegrated.maximsensorsapp.sleep.view

import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.maximintegrated.maximsensorsapp.R
import com.maximintegrated.maximsensorsapp.sleep.LineChartValueFormatter
import com.maximintegrated.maximsensorsapp.sleep.StagesYAxisFormatter
import com.maximintegrated.maximsensorsapp.sleep.database.entity.Sleep
import com.maximintegrated.maximsensorsapp.sleep.utils.ColorUtil
import kotlinx.android.synthetic.main.view_sleep_line_chart.view.lineChart

class SleepTabView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    companion object {
        const val SLEEP_STAGES_INDEX = 0
        const val MOTION_INDEX = 1
        const val HR_INDEX = 2
        const val IBI_INDEX = 3
        const val SpO2_INDEX = 4
    }

    fun setupChart(position: Int, sleepList: List<Sleep>?, containerHeight: Int) {
        inflate(context, R.layout.view_sleep_line_chart, this)
        val isSleepStage = position == SLEEP_STAGES_INDEX
        lineChart.extraBottomOffset = 10f
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(!isSleepStage)
        lineChart.setDrawGridBackground(false)
        lineChart.isDragEnabled = !isSleepStage
        //lineChart.legend.isEnabled = false
        lineChart.setScaleEnabled(!isSleepStage)
        lineChart.setPinchZoom(false)
        lineChart.axisRight.isEnabled = false
        //lineChart.setVisibleXRangeMaximum(1000f)
        //lineChart.axisLeft.setDrawGridLines(false)

        val xAxis = lineChart.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = LineChartValueFormatter()
        xAxis.textSize = 14f
        lineChart.axisLeft.textSize = 14f

        val dataSet = LineDataSet(emptyList(), "")
        dataSet.setDrawCircles(false)

        if (sleepList == null) return

        when (position) {
            SLEEP_STAGES_INDEX -> {
                val leftAxis = lineChart.axisLeft
                leftAxis.isEnabled = true
                leftAxis.axisMaximum = 5f
                leftAxis.labelCount = 4

                leftAxis.valueFormatter = StagesYAxisFormatter()
                val stages = sleepList.map {
                    Pair(
                        if (it.sleepPhasesOutputProcessed <= 1) {
                            1f
                        } else {
                            it.sleepPhasesOutputProcessed.toFloat()
                        },
                        it.date
                    )
                }
                dataSet.values = stages.map { Entry(it.second.time.toFloat(), it.first) }
                dataSet.label = "Stages"

                dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
                dataSet.cubicIntensity = 0.2f
                dataSet.setDrawFilled(false)

                val paint: Paint = lineChart.renderer.paintRender
                paint.shader = LinearGradient(
                    0f, containerHeight.toFloat(), 0f, 0f,
                    intArrayOf(
                        ColorUtil.COLOR_WAKE,
                        ColorUtil.COLOR_REM,
                        ColorUtil.COLOR_DEEP,
                        ColorUtil.COLOR_LIGHT
                    ),
                    floatArrayOf(0.15f, 0.5f, 0.75f, 1f),
                    Shader.TileMode.REPEAT
                )
                dataSet.lineWidth = 4f

                dataSet.color = ColorUtil.COLOR_WAKE
                dataSet.setCircleColor(Color.WHITE)
                dataSet.mode = LineDataSet.Mode.STEPPED
            }
            MOTION_INDEX -> {
                val max = sleepList.maxBy { it.accMag }!!.accMag.toFloat()
                val min = sleepList.minBy { it.accMag }!!.accMag.toFloat()
                val diff = max - min
                val leftAxis = lineChart.axisLeft
                leftAxis.isEnabled = true
                leftAxis.axisMinimum = kotlin.math.max(min - diff / 10, 0f)
                leftAxis.axisMaximum = max + diff / 10
                //leftAxis.labelCount = 10
                //leftAxis.valueFormatter = StagesYAxisFormatter2()

                //val max = sleepList?.maxBy { it.accMag }!!.accMag.toFloat()
                /*val movements = sleepList?.map { Pair(it.accMag.toFloat(), it.date) }
                val values =
                    movements?.map { Entry(it.second.time.toFloat(), 50 * (it.first / max)) }*/

                val values = sleepList.map { Entry(it.date.time.toFloat(), it.accMag.toFloat()) }

                dataSet.values = values
                dataSet.label = "Motion (mg)"
                dataSet.setDrawFilled(false)

                /*val paintDrawable = PaintDrawable()
                paintDrawable.shape = RectShape()
                paintDrawable.shaderFactory = sf

                dataSet.fillDrawable = paintDrawable
                dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
                dataSet.cubicIntensity = 0.2f*/
            }
            HR_INDEX -> {
                val list = sleepList.filter { it.hr != 0.0 }

                val max = list.maxBy { it.hr }!!.hr.toFloat()
                val min = list.minBy { it.hr }!!.hr.toFloat()
                val diff = max - min

                val leftAxis = lineChart.axisLeft
                leftAxis.axisMinimum = kotlin.math.max(min - diff / 10, 0f)
                leftAxis.axisMaximum = max + diff / 10
                //leftAxis.labelCount = 10

                //val hr = sleepList?.mapIndexed { idx, it -> Pair(it.hr.toFloat(), idx) }

                dataSet.values = list.map { Entry(it.date.time.toFloat(), it.hr.toFloat()) }
//                dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
//
//                val paint: Paint = lineChart.renderer.paintRender
//                paint.shader = LinearGradient(
//                    0f, containerHeight.toFloat(), 0f, 0f,
//                    intArrayOf(
//                        Color.rgb(150, 212, 212),
//                        Color.rgb(255, 219, 59),
//                        Color.rgb(255, 169, 41),
//                        Color.rgb(230, 61, 0)
//                    ),
//                    floatArrayOf(0.3f, 0.6f, 0.85f, 1f),
//                    Shader.TileMode.REPEAT
//                )
                dataSet.lineWidth = 2f
                dataSet.label = "HR (bpm)"
                dataSet.setDrawFilled(false)
            }
            IBI_INDEX -> {
                val hrv = sleepList.filter { it.ibi != 0.0 }

                val max = hrv.maxBy { it.ibi }!!.ibi.toFloat()
                val min = hrv.minBy { it.ibi }!!.ibi.toFloat()
                val diff = max - min

                val leftAxis = lineChart.axisLeft
                leftAxis.axisMinimum = kotlin.math.max(min - diff / 10, 0f)
                leftAxis.axisMaximum = max + diff / 10
                //leftAxis.labelCount = 10

                dataSet.values = hrv.map { Entry(it.date.time.toFloat(), it.ibi.toFloat()) }
                dataSet.label = "IBI (ms)"
                dataSet.setDrawFilled(false)
            }
            SpO2_INDEX -> {
                dataSet.values = sleepList.map { Entry(it.date.time.toFloat(), it.spo2.toFloat()) }
                dataSet.label = "SpO2 (%)"
                dataSet.setDrawFilled(false)
                //dataSet.fillFormatter = IFillFormatter { _, _ -> lineChart.axisLeft.axisMinimum }
            }
        }
        val data = LineData(dataSet)

        lineChart.data = data
        lineChart.fitScreen()
        lineChart.invalidate()
    }
}