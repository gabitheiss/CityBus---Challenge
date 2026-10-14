package com.android.citybus.ui.busesline.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.citybus.domain.model.BusesPositionToLine
import com.android.citybus.repository.SearchLinesRepository
import java.net.HttpURLConnection.HTTP_OK
import kotlinx.coroutines.launch

class BusesLineViewModel(
    private val repository: SearchLinesRepository,
) : ViewModel() {

    private val _busesPositionToLineLive = MutableLiveData<BusesPositionToLine>()
    val busesPositionToLineLive: LiveData<BusesPositionToLine> = _busesPositionToLineLive

    fun getBusesPositionToLine(line: String) {
        viewModelScope.launch {
            repository.getBusesPositionToLine(line).let { response ->
                println("body result: ${response.body()}")
                if (response.code() == HTTP_OK) {
                    _busesPositionToLineLive.value = response.body()
                }
            }
        }
    }
}
