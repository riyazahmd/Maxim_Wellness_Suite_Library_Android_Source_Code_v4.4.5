package com.maximintegrated.maximsensorsapp.sleep

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.google.android.material.tabs.TabLayout
import com.maximintegrated.algorithms.AlgorithmInitConfig
import com.maximintegrated.algorithms.MaximAlgorithms
import com.maximintegrated.maximsensorsapp.R
import com.maximintegrated.maximsensorsapp.sleep.adapters.TabAdapter
import com.maximintegrated.maximsensorsapp.sleep.viewmodels.SourceAndAllSleepsViewModel
import kotlinx.android.synthetic.main.include_sleep_details_fragment_content.*
import kotlinx.android.synthetic.main.view_sleep_details_chart.*
import java.text.DecimalFormat

class SleepDetailsFragment(private val dataModel: SleepDataModel) : Fragment() {
    companion object {
        fun newInstance(dataModel: SleepDataModel) = SleepDetailsFragment(dataModel)
    }

    private lateinit var sourceAndAllSleepsViewModel: SourceAndAllSleepsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sleep_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sourceAndAllSleepsViewModel =
            ViewModelProviders.of(this).get(SourceAndAllSleepsViewModel::class.java)

        sourceAndAllSleepsViewModel.setInput(dataModel.sourceId)

        details.setupChart(dataModel)
        calculateSQI()

        sourceAndAllSleepsViewModel.sourceList.observe(this) {
            val tabAdapter = TabAdapter(requireContext(), it)
            tabPager.adapter = tabAdapter
            with(tabLayout) {
                setupWithViewPager(tabPager)
                isTabIndicatorFullWidth = true
                tabGravity = TabLayout.GRAVITY_CENTER
                textAlignment = TabLayout.TEXT_ALIGNMENT_CENTER
            }
        }

    }

    private fun calculateSQI() {
        val algorithmInitConfig = AlgorithmInitConfig()
        algorithmInitConfig.enableAlgorithmsFlag = MaximAlgorithms.FLAG_SLEEP
        MaximAlgorithms.init(algorithmInitConfig)
        val sqi = MaximAlgorithms.calculateSQI(
            dataModel.deepCount.toFloat(),
            dataModel.remCount.toFloat(),
            dataModel.wakeCount.toFloat(),
            dataModel.numberOfWakeInSleep
        )
        sqiProgressBar.progress = sqi.toInt()
        sqiTextView.text = DecimalFormat("#.##").format(sqi)
        MaximAlgorithms.end(MaximAlgorithms.FLAG_SLEEP)
    }
}