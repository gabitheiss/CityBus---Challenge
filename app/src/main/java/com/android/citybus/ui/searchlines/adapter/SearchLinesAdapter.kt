package com.android.citybus.ui.searchlines.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.citybus.R
import com.android.citybus.databinding.ItemBusLineBinding
import com.android.citybus.domain.model.BusesLines
import com.android.citybus.domain.model.DirectionLine
import com.android.citybus.ext.gone
import com.android.citybus.ext.visible

class SearchLinesAdapter(private var items: List<BusesLines>, private val listener: BusesLinesAdapterListener, private val context: Context):
    RecyclerView.Adapter<SearchLinesAdapter.BusLineViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchLinesAdapter.BusLineViewHolder {
        return BusLineViewHolder(ItemBusLineBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BusLineViewHolder, position: Int) {
        items[position].apply {
            holder.bind(this)
            holder.itemView.setOnClickListener {
                listener.onLineClick(this)
            }
        }
    }

    inner class BusLineViewHolder(private val binding: ItemBusLineBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(busesLines: BusesLines) {
            with(binding) {
                directionView.text = when(busesLines.directionLine) {
                    DirectionLine.PRINCIPAL_TERMINAL.directionLine -> context.getString(R.string.direction_description, busesLines.principalTerminal, busesLines.secondaryTerminal)
                    else -> context.getString(R.string.direction_description, busesLines.secondaryTerminal, busesLines.principalTerminal)
                }

                if (busesLines.isCircularLine) {
                    circularIndicatorView.visible()
                } else {
                    circularIndicatorView.gone()
                }

                codeLineView.text = context.getString(R.string.code_line, busesLines.codeLine.toString())
            }
        }
    }

    fun clear() {
        items = emptyList()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size
}