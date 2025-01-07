package com.maximintegrated.maximsensorsapp.bpt

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
import com.maximintegrated.maximsensorsapp.R
import kotlinx.android.synthetic.main.fragment_bpt_history.*
import kotlinx.android.synthetic.main.include_app_bar.*

class BptHistoryFragment : Fragment() {

    companion object {
        fun newInstance() = BptHistoryFragment()
    }

    private lateinit var hspViewModel: HspViewModel
    private lateinit var bptViewModel: BptViewModel

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
        return inflater.inflate(R.layout.fragment_bpt_history, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar!!.hide()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.pageTitle = getString(R.string.history)

        val adapter = BptHistoryAdapter()
        bptHistoryRecyclerView.adapter = adapter

        bptViewModel = ViewModelProviders.of(requireActivity()).get(BptViewModel::class.java)
        bptViewModel.historyDataList.observe(this){
            adapter.submitList(it)
        }
    }
}