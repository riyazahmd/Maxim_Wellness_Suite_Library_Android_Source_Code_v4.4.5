package com.maximintegrated.maximsensorsapp.sports_coaching

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.maximintegrated.algorithms.sports.SportsCoachingAlgorithmOutput
import com.maximintegrated.algorithms.sports.SportsCoachingSession
import com.maximintegrated.hsp.HspViewModel
import com.maximintegrated.maximsensorsapp.BleConnectionInfo
import com.maximintegrated.maximsensorsapp.MaximSensorsApp
import com.maximintegrated.maximsensorsapp.R
import com.maximintegrated.maximsensorsapp.profile.UserViewModel
import com.maximintegrated.maximsensorsapp.profile.UserViewModelFactory
import kotlinx.android.synthetic.main.fragment_sports_coaching_history.*
import kotlinx.android.synthetic.main.include_app_bar.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class SportsCoachingHistoryFragment : Fragment(), HistoryViewHolder.HistoryItemClickListener {

    companion object {
        fun newInstance() = SportsCoachingHistoryFragment()
    }

    private lateinit var hspViewModel: HspViewModel
    private lateinit var userViewModel: UserViewModel

    private val adapter: SportsCoachingHistoryAdapter by lazy { SportsCoachingHistoryAdapter(this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sports_coaching_history, container, false)
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

        toolbar.pageTitle = getString(R.string.history)

        initRecyclerView()

        progressBar.visibility = View.VISIBLE
        doAsync {
            adapter.outputs =
                getSportsCoachingOutputsFromFiles(userViewModel.getCurrentUser().username, requireContext().contentResolver).filter { it.session != SportsCoachingSession.VO2MAX_RELAX }
                    .toMutableList()
            uiThread {
                progressBar.visibility = View.GONE
                if (adapter.outputs.isEmpty()) {
                    fileRecyclerView.visibility = View.GONE
                    warningGroup.visibility = View.VISIBLE
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun initRecyclerView() {
        fileRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        fileRecyclerView.adapter = adapter
    }

    override fun onItemClicked(output: SportsCoachingAlgorithmOutput) {
        //TODO go to detailed page
    }
}