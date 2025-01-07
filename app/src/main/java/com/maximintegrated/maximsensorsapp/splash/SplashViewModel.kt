package com.maximintegrated.maximsensorsapp.splash

import androidx.lifecycle.ViewModel
import com.maximintegrated.maximsensorsapp.exts.SingleLiveEvent

class SplashViewModel : ViewModel() {
    val navigateToNextScreen = SingleLiveEvent<Boolean>()

    fun onSplashAnimationEnd() {
        navigateToNextScreen.postValue(true)
    }
}