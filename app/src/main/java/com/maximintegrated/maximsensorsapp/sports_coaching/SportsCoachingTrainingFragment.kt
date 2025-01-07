package com.maximintegrated.maximsensorsapp.sports_coaching

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.maximintegrated.algorithms.sports.SportsCoachingSession
import com.maximintegrated.hsp.HspViewModel
import com.maximintegrated.maximsensorsapp.BleConnectionInfo
import com.maximintegrated.maximsensorsapp.R
import com.maximintegrated.maximsensorsapp.exts.addFragment
import kotlinx.android.synthetic.main.fragment_sports_coaching_training.*
import kotlinx.android.synthetic.main.include_app_bar.*

class SportsCoachingTrainingFragment : Fragment() {

    companion object {
        fun newInstance() = SportsCoachingTrainingFragment()
    }

    private lateinit var hspViewModel: HspViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        hspViewModel = ViewModelProviders.of(requireActivity()).get(HspViewModel::class.java)

        hspViewModel.connectionState
            .observe(this) { (device, connectionState) ->
                toolbar.connectionInfo = if (hspViewModel.bluetoothDevice != null) {
                    BleConnectionInfo(connectionState, device?.name, device?.address)
                } else {
                    null
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sports_coaching_training, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar!!.hide()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.pageTitle = getString(R.string.sports_coaching)
        SportsCoachingManager.currentSession = SportsCoachingSession.UNDEFINED

        readinessMenuItemView.setOnClickListener {
            SportsCoachingManager.currentSession = SportsCoachingSession.READINESS
            requireActivity().addFragment(SportsCoachingReadinessFragment.newInstance())
        }
        vo2MaxMenuItemView.setOnClickListener {
            SportsCoachingManager.currentSession = SportsCoachingSession.VO2MAX_RELAX
            requireActivity().addFragment(SportsCoachingVO2MaxFragment.newInstance())
        }
        epocRecoveryMenuItemView.setOnClickListener {
            SportsCoachingManager.currentSession = SportsCoachingSession.EPOC_RECOVERY
            requireActivity().addFragment(SportsCoachingEpocRecoveryFragment.newInstance())
        }
        recoveryTimeMenuItemView.setOnClickListener {
            SportsCoachingManager.currentSession = SportsCoachingSession.RECOVERY_TIME
            requireActivity().addFragment(SportsCoachingRecoveryTimeFragment.newInstance())
        }
        fitnessAgeMenuItemView.setOnClickListener {
            SportsCoachingManager.currentSession = SportsCoachingSession.VO2MAX_FROM_HISTORY
            requireActivity().addFragment(SportsCoachingFitnessAgeFragment.newInstance())
        }
    }
}
