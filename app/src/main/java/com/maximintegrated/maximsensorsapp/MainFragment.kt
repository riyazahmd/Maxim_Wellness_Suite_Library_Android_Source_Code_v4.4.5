package com.maximintegrated.maximsensorsapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.maximintegrated.hsp.*
import com.maximintegrated.maximsensorsapp.alignment.AlignmentFragment
import com.maximintegrated.maximsensorsapp.archive.ArchiveFragment
import com.maximintegrated.maximsensorsapp.bpt.BptMainFragment
import com.maximintegrated.maximsensorsapp.ecg.EcgFragment
import com.maximintegrated.maximsensorsapp.exts.addFragment
import com.maximintegrated.maximsensorsapp.hrv.HrvFragment
import com.maximintegrated.maximsensorsapp.log_parser.LogParserFragment
import com.maximintegrated.maximsensorsapp.profile.ProfileFragment
import com.maximintegrated.maximsensorsapp.profile.UserViewModel
import com.maximintegrated.maximsensorsapp.profile.UserViewModelFactory
import com.maximintegrated.maximsensorsapp.respiratory.RespiratoryFragment
import com.maximintegrated.maximsensorsapp.sleep.SleepFragment
import com.maximintegrated.maximsensorsapp.spo2.Spo2Fragment
import com.maximintegrated.maximsensorsapp.sports_coaching.SportsCoachingLandingFragment
import com.maximintegrated.maximsensorsapp.stress.StressFragment
import com.maximintegrated.maximsensorsapp.temp.TempFragment
import com.maximintegrated.maximsensorsapp.whrm.WhrmFragment
import kotlinx.android.synthetic.main.include_app_bar.*
import kotlinx.android.synthetic.main.include_main_fragment_content.*


class MainFragment : Fragment(), LandingPage {

    companion object {
        private const val ARG_DEVICE_SENSORS = "device_sensors"
        private const val ARG_FIRMWARE_ALGORITHMS = "firmware_algorithms"

        fun newInstance(
            deviceSensors: Array<String>,
            firmwareAlgorithms: Array<String>
        ): MainFragment {
            return MainFragment().apply {
                arguments = Bundle().apply {
                    putStringArray(ARG_DEVICE_SENSORS, deviceSensors)
                    putStringArray(ARG_FIRMWARE_ALGORITHMS, firmwareAlgorithms)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        inflater.inflate(R.layout.fragment_main, container, false)

    private lateinit var hspViewModel: HspViewModel
    private lateinit var userViewModel: UserViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hspViewModel = ViewModelProviders.of(requireActivity()).get(HspViewModel::class.java)
        userViewModel = ViewModelProviders.of(
            requireActivity(), UserViewModelFactory(
                (requireActivity().application as MaximSensorsApp).userDao
            )
        ).get(UserViewModel::class.java)

        bptMenuItemView.isVisible = hspViewModel.deviceModel.algorithms.contains(BPT)

        spo2MenuItemView.isVisible = hspViewModel.deviceModel.algorithms.contains(SPO2)

        tempMenuItemView.isVisible = hspViewModel.deviceModel.algorithms.contains(TEMP)

        ecgMenuItemView.isVisible = hspViewModel.deviceModel.algorithms.contains(ECG)

        sleepMenuItemView.isVisible = hspViewModel.deviceModel.algorithms.contains(SLEEP)

        sportsCoachingMenuItemView.isVisible = hspViewModel.deviceModel.algorithms.contains(SPORTS)

        parserMenuItemView.isVisible = hspViewModel.deviceModel.algorithms.contains(LOG_PARSER)

        hspViewModel.connectionState
            .observe(this) { (device, connectionState) ->
                toolbar.connectionInfo = if (hspViewModel.bluetoothDevice != null) {
                    BleConnectionInfo(connectionState, device?.name, device?.address)
                } else {
                    null
                }
            }

        toolbar.pageTitle = getString(R.string.maxim_hsp)

        profileMenuItemView.setOnClickListener {
            requireActivity().addFragment(ProfileFragment.newInstance())
        }

        spo2MenuItemView.setOnClickListener {
            requireActivity().addFragment(Spo2Fragment.newInstance())
        }

        tempMenuItemView.setOnClickListener {
            requireActivity().addFragment(TempFragment.newInstance())
        }

        ecgMenuItemView.setOnClickListener {
            requireActivity().addFragment(EcgFragment.newInstance())
        }

        bptMenuItemView.setOnClickListener {
            if (checkUser()) {
                requireActivity().addFragment(BptMainFragment.newInstance())
            }
        }

        whrmMenuItemView.setOnClickListener {
            requireActivity().addFragment(WhrmFragment.newInstance())
        }

        hrvMenuItemView.setOnClickListener {
            requireActivity().addFragment(HrvFragment.newInstance())
        }

        respiratoryMenuItemView.setOnClickListener {
            requireActivity().addFragment(RespiratoryFragment.newInstance())
        }

        sleepMenuItemView.setOnClickListener {
            if (checkUser()) {
                requireActivity().addFragment(SleepFragment.newInstance())
            }
        }

        stressMenuItemView.setOnClickListener {
            requireActivity().addFragment(StressFragment.newInstance())
        }

        sportsCoachingMenuItemView.setOnClickListener {
            if (checkUser()) {
                requireActivity().addFragment(SportsCoachingLandingFragment.newInstance())
            }
        }

        archiveMenuItemView.setOnClickListener {
            if (checkUser()) {
                requireActivity().addFragment(ArchiveFragment.newInstance())
            }
        }

        parserMenuItemView.setOnClickListener {
            requireActivity().addFragment(LogParserFragment.newInstance())
        }

        alignmentMenuItemView.setOnClickListener {
            requireActivity().addFragment(AlignmentFragment.newInstance())
        }
    }

    private fun checkUser(): Boolean {
        if (userViewModel.currentUser.value != null) {
            return true
        } else {
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setMessage(getString(R.string.you_should_select_user_first))
                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                    dialog.dismiss()
                    requireActivity().addFragment(ProfileFragment.newInstance())
                }.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
            alertDialog.show()
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar!!.hide()
    }
}