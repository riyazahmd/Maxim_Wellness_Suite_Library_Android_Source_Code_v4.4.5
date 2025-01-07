package com.maximintegrated.maximsensorsapp.bpt

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro2
import com.github.appintro.AppIntroFragment
import com.maximintegrated.maximsensorsapp.R

class BpTutorialActivity : AppIntro2() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setImmersiveMode()
        addSlide(AppIntroFragment.newInstance(title = "Sensor Placement", description = "Please place your right index finger on the PPG sensor of MAXREFDES220 board.", imageDrawable = R.drawable.tutorial1))
        addSlide(AppIntroFragment.newInstance(title = "User Selection", description = "Enter a new username or select the existing one from the list.", imageDrawable = R.drawable.tutorial2))
        addSlide(AppIntroFragment.newInstance(title = "Calibration", description = "Enter the reference BP values and click START to collect PPG data. PPG signal will appear, keep measuring until the status turns to SUCCESS.", imageDrawable = R.drawable.tutorial3))
        addSlide(AppIntroFragment.newInstance(title = "Calibration", description = "Calibration is done when SUCCESS flag appears. Click DONE button to return back to main page.", imageDrawable = R.drawable.tutorial4))
        addSlide(AppIntroFragment.newInstance(title = "Measurement", description = "Click START button to start the measurement. PPG will appear, Keep measuring util the status turns to SUCCESS. BP Estimation will be reported here.", imageDrawable = R.drawable.tutorial5))
        addSlide(AppIntroFragment.newInstance(title = "History", description = "Please track of all your calibrations and measurements in the History page.", imageDrawable = R.drawable.tutorial6))
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        finish()
    }
}