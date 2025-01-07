package com.maximintegrated.maximsensorsapp.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import androidx.core.view.isVisible
import com.google.android.material.card.MaterialCardView
import com.maximintegrated.maximsensorsapp.R
import kotlinx.android.synthetic.main.view_measurement_result_text.view.*
import java.util.concurrent.atomic.AtomicInteger

class MeasurementResultTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    var unit: CharSequence = ""
        set(value) {
            field = value
            updateResultText()
        }

    var isMeasuring: Boolean = false
        set(value) {
            field = value
            timerCount.set(0)
            if (value) {
                handler?.postDelayed(tickRunnable, 1000)
            } else {
                resultView.clearAnimation()
                resultView.alpha = 1f
                handler?.removeCallbacks(tickRunnable)
            }
            updateViewVisibilities()
        }

    var result: Int? = null
        set(value) {
            field = value
            timerCount.set(0)
            updateResultText()
            updateViewVisibilities()
            animateResultView(animation)
        }

    var animation = false

    private var animationType = AnimationType.FLASH

    private var flashAnimation: AlphaAnimation
    private var fadeAnimation: AlphaAnimation
    private var scaleUpAnimation: Animation
    private var scaleDownAnimation: Animation

    var continuousMode = false

    private var defaultResultViewTextColor = 0

    private val obsoleteThresholdInSeconds = 10

    private var timerCount = AtomicInteger(0)

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (isMeasuring) {
                if (timerCount.incrementAndGet() >= obsoleteThresholdInSeconds && continuousMode) {
                    resultView.clearAnimation()
                    resultView.alpha = 1f
                    //resultView.text = "No Report"
                }
                handler?.postDelayed(this, 1000)
            } else {
                timerCount.set(0)
                handler?.removeCallbacks(this)
            }
        }
    }

    init {
        inflate(context, R.layout.view_measurement_result_text, this)

        with(
            context.obtainStyledAttributes(
                attrs,
                R.styleable.MeasurementResultView,
                defStyleAttr,
                0
            )
        ) {
            unit = getString(R.styleable.MeasurementResultView_mrv_unit) ?: ""
            animation = getBoolean(R.styleable.MeasurementResultView_mrv_enable_animation, false)
            animationType = AnimationType.values()[getInt(
                R.styleable.MeasurementResultView_mrv_animation_type,
                0
            )]
            recycle()
        }

        flashAnimation = AlphaAnimation(0.1f, 1f)
        flashAnimation.duration = 500L
        flashAnimation.startOffset = 0
        flashAnimation.repeatMode = Animation.RESTART
        flashAnimation.repeatCount = 0
        flashAnimation.fillAfter = true

        fadeAnimation = AlphaAnimation(1f, 0.2f)
        fadeAnimation.interpolator = DecelerateInterpolator(6f)
        fadeAnimation.duration = 10000L
        fadeAnimation.startOffset = 0
        fadeAnimation.repeatMode = Animation.RESTART
        fadeAnimation.repeatCount = 0
        fadeAnimation.fillAfter = true

        scaleUpAnimation = AnimationUtils.loadAnimation(context, R.anim.scale_up)
        scaleDownAnimation = AnimationUtils.loadAnimation(context, R.anim.scale_down)
        defaultResultViewTextColor = resultView.currentTextColor
    }

    private fun animateResultView(animationEnabled: Boolean) {
        if (animationEnabled && resultView.isVisible) {
            when (animationType) {
                AnimationType.FLASH -> {
                    if (!flashAnimation.hasStarted() || flashAnimation.hasEnded()) {
                        resultView.startAnimation(flashAnimation)
                    }
                }
                AnimationType.FADE -> {
                    if (fadeAnimation.hasStarted() && !fadeAnimation.hasEnded()) {
                        fadeAnimation.cancel()
                        fadeAnimation.reset()
                    }
                    resultView.startAnimation(fadeAnimation)
                }

                AnimationType.HEART_BEAT -> {
                    resultView.clearAnimation()
                    resultView.startAnimation(scaleUpAnimation)
                    resultView.startAnimation(scaleDownAnimation)
                }
            }
        }
    }

    private fun updateResultText() {
        resultView.text = buildSpannedString {
            append((result ?: 0).toString())
            scale(0.6f) {
                append(' ')
                append(unit)
            }
        }
    }

    private fun updateViewVisibilities() {
        if ((result == 0) and isMeasuring) {
            resultView.text = "--"
        }
    }
}