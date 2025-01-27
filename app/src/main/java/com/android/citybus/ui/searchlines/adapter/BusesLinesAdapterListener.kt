package com.android.citybus.ui.searchlines.adapter

import com.android.citybus.domain.model.BusesLines

interface BusesLinesAdapterListener {
    fun onLineClick(line: BusesLines)
}