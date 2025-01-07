package com.maximintegrated.maximsensorsapp.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.maximintegrated.maximsensorsapp.R
import com.maximintegrated.maximsensorsapp.ScannerActivity
import com.maximintegrated.maximsensorsapp.databinding.ActivitySplashBinding

private const val ANIMATION_DURATION = 2000L

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    lateinit var viewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SplashViewModel::class.java)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        Handler().postDelayed(
            {
                viewModel.onSplashAnimationEnd()
            },
            ANIMATION_DURATION
        )
    }

    private fun observeViewModel() {
        viewModel.navigateToNextScreen.observe(this, Observer {
            val intent = Intent(this, ScannerActivity::class.java)
            startActivity(intent)
            finish()
        })
    }
}