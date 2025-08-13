package com.aliJafari.bbarq.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aliJafari.bbarq.data.Outage
import com.aliJafari.bbarq.databinding.ScheduleInfoLayoutBinding

class OutagesAdapter(
    private val outages: List<Outage>
) : RecyclerView.Adapter<OutagesAdapter.OutageViewHolder>() {

    inner class OutageViewHolder(private val binding: ScheduleInfoLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(outage: Outage) {
            binding.scheduleDate.text = outage.date ?: "-"
            binding.scheduleStartTime.text = outage.startTime ?: "-"
            binding.scheduleEndTime.text = outage.endTime ?: "-"
            binding.scheduleReason.text = outage.reason ?: "-"
            binding.scheduleBillId.text = outage.billId?.toString() ?: "-"
            binding.scheduleAddress.text = outage.address?.toString() ?: "-"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OutageViewHolder {
        val binding = ScheduleInfoLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OutageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OutageViewHolder, position: Int) {
        holder.bind(outages[position])
    }

    override fun getItemCount(): Int = outages.size
}
