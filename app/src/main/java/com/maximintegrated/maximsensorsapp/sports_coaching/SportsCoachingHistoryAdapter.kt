package com.maximintegrated.maximsensorsapp.sports_coaching

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.maximintegrated.algorithms.sports.SportsCoachingAlgorithmOutput
import com.maximintegrated.maximsensorsapp.FILE_TIMESTAMP_FORMAT
import com.maximintegrated.maximsensorsapp.R
import kotlinx.android.synthetic.main.sports_coaching_history_item.view.*
import java.util.*

class SportsCoachingHistoryAdapter(private val listener: HistoryViewHolder.HistoryItemClickListener) :
    RecyclerView.Adapter<HistoryViewHolder>() {

    var outputs: MutableList<SportsCoachingAlgorithmOutput> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        HistoryViewHolder(listener, parent)

    override fun getItemCount() = outputs.size

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) =
        holder.bind(outputs[position])
}

class HistoryViewHolder(
    private val listener: HistoryItemClickListener,
    private val parent: ViewGroup
) :
    RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.sports_coaching_history_item,
            parent,
            false
        )
    ) {

    private val sessionTextView: TextView by lazy { itemView.historySessionTextView }
    private val scoreTextView: TextView by lazy { itemView.historyScoreTextView }
    private val dateTextView: TextView by lazy { itemView.historyDateTextView }
    private lateinit var output: SportsCoachingAlgorithmOutput

    init {
        itemView.setOnClickListener {
            listener.onItemClicked(output)
        }
    }

    fun bind(output: SportsCoachingAlgorithmOutput) {
        this.output = output
        sessionTextView.text = getStringValueOfSession(parent.context, output.session)
        dateTextView.text = FILE_TIMESTAMP_FORMAT.format(Date(output.timestamp * 1000L))
        scoreTextView.text = "%d".format(getScore(output).toInt())
    }

    interface HistoryItemClickListener {
        fun onItemClicked(output: SportsCoachingAlgorithmOutput)
    }
}