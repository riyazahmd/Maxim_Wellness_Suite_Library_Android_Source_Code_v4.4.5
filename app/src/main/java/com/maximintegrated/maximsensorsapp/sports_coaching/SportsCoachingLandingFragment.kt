package com.maximintegrated.maximsensorsapp.sports_coaching

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.maximintegrated.hsp.HspViewModel
import com.maximintegrated.maximsensorsapp.BleConnectionInfo
import com.maximintegrated.maximsensorsapp.MaximSensorsApp
import com.maximintegrated.maximsensorsapp.R
import com.maximintegrated.maximsensorsapp.exts.addFragment
import com.maximintegrated.maximsensorsapp.profile.UpdateProfileFragment
import com.maximintegrated.maximsensorsapp.profile.UserViewModel
import com.maximintegrated.maximsensorsapp.profile.UserViewModelFactory
import kotlinx.android.synthetic.main.fragment_sports_coaching_landing.*
import kotlinx.android.synthetic.main.include_app_bar.*

class SportsCoachingLandingFragment : Fragment() {

    companion object {
        fun newInstance() = SportsCoachingLandingFragment()
    }

    private lateinit var hspViewModel: HspViewModel
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sports_coaching_landing, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar!!.hide()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hspViewModel = ViewModelProviders.of(requireActivity()).get(HspViewModel::class.java)

        hspViewModel.connectionState
            .observe(this) { (device, connectionState) ->
                toolbar.connectionInfo = if (hspViewModel.bluetoothDevice != null) {
                    BleConnectionInfo(connectionState, device?.name, device?.address)
                } else {
                    null
                }
            }

        userViewModel = ViewModelProviders.of(
            requireActivity(), UserViewModelFactory(
                (requireActivity().application as MaximSensorsApp).userDao
            )
        ).get(UserViewModel::class.java)

        toolbar.pageTitle = getString(R.string.sports_coaching)

        profileMenuItemView.setOnClickListener {
            requireActivity().addFragment(UpdateProfileFragment.newInstance(userViewModel.getCurrentUser()))
        }
        trainingMenuItemView.setOnClickListener {
            requireActivity().addFragment(SportsCoachingTrainingFragment.newInstance())
        }
        historyMenuItemView.setOnClickListener {
            requireActivity().addFragment(SportsCoachingHistoryFragment.newInstance())
        }
    }
}
